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
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
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
 		
	
	private VigMakeStepMeta meta;
	
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
		VigMakeStepData data = (VigMakeStepData) sdi;
		
		data.items = new HashMap<List<Object>,Item>();
		data.processedDates = new HashMap<Date,Boolean>();
		if(meta.isOrderedData()){
			logBasic("Algoritmo Ordenado");
			data.stateInsertionAlgo = new OrderedStateTreeAlgorithm();
		}else{
			logBasic("Algoritmo No Ordenado");
			data.stateInsertionAlgo = new GeneralStateTreeAlgorithm();
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
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		//VigMakeStepMeta meta = (VigMakeStepMeta) smi;
		VigMakeStepData data = (VigMakeStepData) sdi;
		HashMap<List<Object>,Item> items = data.items;
		HashMap<Date,Boolean> processedDates = data.processedDates;
		StateTreeAlgorithm insertionAlgo = data.stateInsertionAlgo;

		// get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
		Object[] r = getRow(); 
		
		// if no more rows are expected, indicate step is finished and processRow() should not be called again
		if (r == null){
			setOutputDone();
			return false;
		}

			// clone the input row structure and place it in our data object
			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			// use meta.getFields() to change it, so it reflects the output row structure 
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
			
			// generate new item if it is not present in items.
			List<String> attributeFields = meta.getAttributeFields();
			List<String> idFields = meta.getIdFields();
			List<Object> ids = new ArrayList<Object>();
			List<Object> attributes = new ArrayList<Object>();
			for(String idField : idFields){
				ids.add(r[getInputRowMeta().indexOfValue(idField)]);
			}
			for(String attributeField : attributeFields){
				attributes.add(r[getInputRowMeta().indexOfValue(attributeField)]);
			}
			Item item; 
			
			if (!items.containsKey(ids)){
				item = new Item(ids, attributes);
				items.put(ids, item);
			} else {
				item = items.get(ids);
			}
			
			
			// gets the date field
			String dateField = meta.getDateField();
			int dateIndex = getInputRowMeta().indexOfValue(dateField);
			Date date = getInputRowMeta().getDate(r, dateIndex);
			processedDates.put(date, true);
			
			// generate the new status
			List<String> statusFields = meta.getStatusFields();
			List<Object> status = new ArrayList<Object>();
			for(String statusField : statusFields){
				status.add(r[getInputRowMeta().indexOfValue(statusField)]);
			}
			State state = new State(status);
			try {
				insertionAlgo.addState(this, item, date, state);
			} catch (StateConflictException e) {
				throw new KettleException("Date conflict between 2 different status.\n"+"Item: "+item.toString()+"\nState"+state.toString()+"\nDate: "+date.toString());
			}

		// indicate that processRow() should be called again
		return true;
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

		((VigMakeStepData) sdi).items.clear();
		super.dispose(smi, sdi);
		System.gc();
	}

	private void emitTree(){
		//VigMakeStepMeta meta = (VigMakeStepMeta) this.getStepMetaInterface(); 
		VigMakeStepData data = (VigMakeStepData) this.getStepDataInterface();
		
		Set<Date> processedDates = data.processedDates.keySet();
		HashMap<List<Object>,Item> items = data.items;

		logBasic("Fechas procesadas: "+processedDates.size());
		logBasic("Items procesados: "+items.size());
		
		for(Item item : items.values()){
			data.stateInsertionAlgo.markAbsences(item, processedDates);
			data.stateInsertionAlgo.cleanStates(item);
			emitItem(data, item, true);
		}	

	}
	
	
	public void emitItem(VigMakeStepData data, Item item, boolean emitLastState){
		Set<Date> processedDates = data.processedDates.keySet();
		Object[] ids = item.getId().toArray();
		Object[] attributes = item.getAttributes().toArray();
		Object[] itemData = ArrayUtils.addAll(ids, attributes);
		Set<Entry<Date, State>> set = item.getStates().entrySet();
		for( Entry<Date, State> status : set){
			if(!status.getValue().isNullState() && !(item.getStates().lastKey().equals(status.getKey()) && !emitLastState )){
				Object[] itemStateData = ArrayUtils.addAll(itemData, status.getValue().getStatus().toArray());
				Object[] itemStateData2 = ArrayUtils.add(itemStateData, status.getKey());
				Date dateEnd = null;
				Object[] statusPrev = new Object[meta.getStatusFields().size()];
				Object[] statusNext = new Object[meta.getStatusFields().size()];
				if(status.getValue().getPrevious() != null && !status.getValue().getPrevious().isNullState()){
					statusPrev = status.getValue().getPrevious().getStatus().toArray();
				}
				if(status.getValue().getNext() != null){
					if(!status.getValue().getNext().isNullState()){
						statusNext = status.getValue().getNext().getStatus().toArray();
						dateEnd = item.getStates().higherKey(status.getKey());
					}else{
						dateEnd = DateUtils.addDays(item.getStates().higherKey(status.getKey()), -1);
					}
				}
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
	
	@Override
	public void setOutputDone() {
		// TODO Auto-generated method stub
		emitTree();
		super.setOutputDone();
	}

	
	
}
