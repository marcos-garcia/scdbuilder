/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package com.bayesforecast.ingdat.vigsteps.vigmake;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.GeneralFillStateTreeAlgorithm;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.GeneralStateTreeAlgorithm;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.OrderedFillStateTreeAlgorithm;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.OrderedStateTreeAlgorithm;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.StateTreeAlgorithm;


/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * 
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step 
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.  
 * 
 */

public class VigMakeStep extends BaseStep implements StepInterface {
 		
	
	/** The meta. */
	private VigMakeStepMeta meta;
	
	/** The data. */
	VigMakeStepData data;
	
	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public VigMakeStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as wall as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		meta = (VigMakeStepMeta) smi;
		data = (VigMakeStepData) sdi;
		
		// 1. Inicializaci�n del HashMap de items
		data.items = new HashMap<List<Object>,Item>();
		// 2. Inicializaci�n del listado de fechas procesadas
		data.processedDates = new HashMap<Date,Boolean>();
		// 3. Inicializaci�n del controladordel arbol de estados,
		//    dependiendo del campos isOrderedData y de preloadedVig
		if(meta.isOrderedData()){
			if(meta.getPreloadedVig() == ""){
				data.preloadingTree = false;
				data.preloadingInputRowset = null;
				data.fillStateInsertionAlgo = null;
				data.stateInsertionAlgo = new OrderedStateTreeAlgorithm();
			}else{
				data.preloadingTree = true;
				for(RowSet r : this.getInputRowSets()){
					logDebug(r.getOriginStepName() +" vs "+ meta.getPreloadedVig());
					if(r.getOriginStepName().equals(meta.getPreloadedVig())){
						logDebug("Ordenado: "+r.getOriginStepName());
						data.preloadingInputRowset = r;
					}
				}
				data.fillStateInsertionAlgo = new OrderedFillStateTreeAlgorithm();
				data.stateInsertionAlgo = data.fillStateInsertionAlgo;
			}
		}else{
			if(meta.getPreloadedVig() == ""){
				data.preloadingTree = false;
				data.preloadingInputRowset = null;
				data.fillStateInsertionAlgo = null;
				data.stateInsertionAlgo = new GeneralStateTreeAlgorithm();
			}else{
				data.preloadingTree = true;
				for(RowSet r : this.getInputRowSets()){
					logDebug(r.getOriginStepName() +" vs "+ meta.getPreloadedVig());
					if(r.getOriginStepName().equals(meta.getPreloadedVig())){
						logDebug("No Ordenado: "+r.getOriginStepName());
						data.preloadingInputRowset = r;
					}
				}
				data.fillStateInsertionAlgo = new GeneralFillStateTreeAlgorithm();
				data.stateInsertionAlgo = data.fillStateInsertionAlgo;
			}
		}
		return super.init(meta, data);
	}

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 *
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * @return true to indicate that the function should be called again, false if the step is done
	 * @throws KettleException the kettle exception
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		
		Object[] r = null;

		
		if(data.preloadingTree && !(data.preloadingInputRowset.isDone() && data.preloadingInputRowset.size() == 0)){
			r = getRowFrom(data.preloadingInputRowset);
		}else{
			if(data.preloadingTree){
				data.preloadingTree = false;
				logBasic("Preloading rowset size: "+data.preloadingInputRowset.size());
				logBasic("Cambiando algo from: "+data.stateInsertionAlgo.getClass().getCanonicalName());
				data.stateInsertionAlgo = data.fillStateInsertionAlgo.getNextTreeAlgorithm();
				logBasic("Cambiando algo to: "+data.stateInsertionAlgo.getClass().getCanonicalName());
				data.processedDates.clear();
			}
		}
		
		if(r == null){
			r = getRow();
		}


		
		/*HashMap<List<Object>,Item> items = data.items;
		HashMap<Date,Boolean> processedDates = data.processedDates;
		if(data.preloadingTree && data.preloadingInputRowset.isDone() && data.preloadingInputRowset.size() == 0){
			data.preloadingTree = false;
			logDebug("Preloading rowset size: "+data.preloadingInputRowset.size());
			logDebug("Cambiando algo from: "+data.stateInsertionAlgo.getClass().getCanonicalName());
			data.stateInsertionAlgo = data.fillStateInsertionAlgo.getNextTreeAlgorithm();
			logDebug("Cambiando algo to: "+data.stateInsertionAlgo.getClass().getCanonicalName());
			processedDates.clear();
		}*/
		
		StateTreeAlgorithm insertionAlgo = data.stateInsertionAlgo;
		
		// if no more rows are expected, indicate step is finished and processRow() should not be called again
		if (r == null){
			setOutputDone();
			// indicate that processRow() should not be called again
			return false;
		}
		
		if(first){
			if(data.preloadingTree && data.preloadingInputRowset.getRowMeta() != null){
				data.inputRowMeta = data.preloadingInputRowset.getRowMeta().clone();
			}else{
				data.inputRowMeta = getInputRowMeta().clone();
			}
			data.outputRowMeta = data.inputRowMeta.clone();
			// use meta.getFields() to change it, so it reflects the output row structure 
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
		}		
		
		
		Item item = getItemFromRow(r,data.inputRowMeta);
		Date date = getDateFromRow(r,data.inputRowMeta);
		State state = getStateFromRow(r,data.inputRowMeta);
		
		data.processedDates.put(date, true);
		
		try {
			// Llamada al algoritmo para computar la fila
			insertionAlgo.addState(this, item, date, state);
		} catch (StateConflictException e) {
			throw new KettleException("Date conflict between 2 different status.\n"+"Item: "+item.toString()+"\nState"+state.toString()+"\nDate: "+date.toString());
		}

		// indicate that processRow() should be called again
		return true;
	}

	/**
	 * Gets the state from row.
	 *
	 * @param r the r
	 * @return the state from row
	 */
	private State getStateFromRow(Object[] r, RowMetaInterface rmi) {
		List<String> statusFields = meta.getStatusFields();
		List<Object> status = new ArrayList<Object>();
		for(String statusField : statusFields){
			status.add(r[rmi.indexOfValue(statusField)]);
		}
		State state = new State(status);
		return state;
	}

	/**
	 * Gets the date from row.
	 *
	 * @param r the r
	 * @return the date from row
	 * @throws KettleValueException the kettle value exception
	 */
	private Date getDateFromRow(Object[] r, RowMetaInterface rmi) throws KettleValueException {
		String dateField = meta.getDateField();
		int dateIndex = rmi.indexOfValue(dateField);
		Date date = rmi.getDate(r, dateIndex);
		return date;
	}

	/**
	 * Gets the item from row. Generate new item if it is not present in data.items.
	 *
	 * @param r the r
	 * @return the item from row
	 */
	private Item getItemFromRow(Object[] r, RowMetaInterface rmi) {
		HashMap<List<Object>,Item> items = data.items;
		
		List<String> attributeFields = meta.getAttributeFields();
		List<String> idFields = meta.getIdFields();
		
		List<Object> ids = new ArrayList<Object>();
		List<Object> attributes = new ArrayList<Object>();

		logDebug("RowMeta: "+rmi);
		for(String idField : idFields){
			logDebug(idField);
			ids.add(r[rmi.indexOfValue(idField)]);
		}
		for(String attributeField : attributeFields){
			attributes.add(r[rmi.indexOfValue(attributeField)]);
		}
		Item item; 
		
		if (!items.containsKey(ids)){
			item = new Item(ids, attributes);
			items.put(ids, item);
		} else {
			item = items.get(ids);
		}
		return item;
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		// Se limpia el �rbol de items y se ejecuta el recolector de basura de java.
		((VigMakeStepData) sdi).items.clear();
		super.dispose(smi, sdi);
		System.gc();
	}

	/**
	 * Emit tree.
	 */
	private void emitTree(){
		VigMakeStepData data = (VigMakeStepData) this.getStepDataInterface();
		
		Set<Date> processedDates = data.processedDates.keySet();
		HashMap<List<Object>,Item> items = data.items;

		logDebug("Fechas procesadas: "+processedDates.size());
		logDebug("Items procesados: "+items.size());
		logDebug("Algoritmo: "+data.stateInsertionAlgo.getClass().getSimpleName());
		
		// Para cada item, marca estados ausentes y limpia los estados. Despu�s emite el item
		for(Item item : items.values()){
			
			if(item.getId().get(0).toString().compareTo("1971367") == 0){
				logBasic("Before marking");
				logBasic(item.toString());
				Set<Entry<Date, State>> set = item.getStates().entrySet();
				
				// 2. Recorre los estados de cada item
				for( Entry<Date, State> status : set){
					logBasic("\tStatus: "+status.toString());
					logBasic("\tPrevious: "+status.getValue().getPrevious());
					logBasic("\tNext: "+status.getValue().getNext());
				}
			}
			data.stateInsertionAlgo.markAbsences(item, processedDates);
			if(item.getId().get(0).toString().compareTo("1971367") == 0){
				logBasic("After marking");
				logBasic(item.toString());
				Set<Entry<Date, State>> set = item.getStates().entrySet();
				
				// 2. Recorre los estados de cada item
				for( Entry<Date, State> status : set){
					logBasic("\tStatus: "+status.toString());
					logBasic("\tPrevious: "+status.getValue().getPrevious());
					logBasic("\tNext: "+status.getValue().getNext());
				}
			}
			data.stateInsertionAlgo.cleanStates(item);
			emitItem(data, item, true);
		}	

	}
	
	
	/**
	 * Emit item.
	 *
	 * @param data the data
	 * @param item the item
	 * @param emitLastState the emit last state
	 */
	public void emitItem(VigMakeStepData data, Item item, boolean emitLastState){

		TreeMap<Date, Boolean> processedDates = new TreeMap<Date, Boolean>(data.processedDates);

		if(item.getId().get(0).toString().compareTo("1971367") == 0){
			logBasic("Emmitting marking");
			logBasic(item.toString());
		}
		// 1. Array con los datos del item
		Object[] ids = item.getId().toArray();
		Object[] attributes = item.getAttributes().toArray();
		Object[] itemData = ArrayUtils.addAll(ids, attributes);
		
		Set<Entry<Date, State>> set = item.getStates().entrySet();
		
		// 2. Recorre los estados de cada item
		for( Entry<Date, State> status : set){
			// Emite estado si no es nulo y si no es el �ltimo y no est� indicado emitirlo.
			if(!status.getValue().isNullState() && !(item.getStates().lastKey().equals(status.getKey()) && !emitLastState )){
				
				// 2.1 A�ade el estado al array de salida
				Object[] itemStateData = ArrayUtils.addAll(itemData, status.getValue().getStatus().toArray());
				Object[] itemStateData2 = ArrayUtils.add(itemStateData, status.getKey());
				
				Date dateEnd = null;
				Object[] statusPrev = new Object[meta.getStatusFields().size()];
				Object[] statusNext = new Object[meta.getStatusFields().size()];
				
				// 2.2 A�ade el estado previo si no es nulo
				if(status.getValue().getPrevious() != null && !status.getValue().getPrevious().isNullState()){
					statusPrev = status.getValue().getPrevious().getStatus().toArray();
				}
				
				// 2.3 A�ade el estado posterior si le hubiera
				if(status.getValue().getNext() != null){
					if(!status.getValue().getNext().isNullState()){
						// 2.3.1 Si no es un estado nulo, se marca como fecha fin su anterior
						statusNext = status.getValue().getNext().getStatus().toArray();
					}
					dateEnd = DateUtils.addDays(item.getStates().higherKey(status.getKey()), -1);
				}
				
				// 2.4 Concatena los estados previos y posteriores al array final
				Object[] itemStateData3 = ArrayUtils.add(itemStateData2, dateEnd);
				Object[] itemStateData4 = ArrayUtils.addAll(itemStateData3, statusPrev);
				Object[] itemStateData5 = ArrayUtils.addAll(itemStateData4, statusNext);
				
				// put the row to the output row stream
				try {
					putRow(data.outputRowMeta, itemStateData5);
				} catch (KettleStepException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.BaseStep#setOutputDone()
	 */
	@Override
	public void setOutputDone() {
		// 1. Cuando la recepci�n de inputs termina, se emite el �rbol
		emitTree();
		// 2. Finalizaci�n del paso
		super.setOutputDone();
	}

	
	
}
