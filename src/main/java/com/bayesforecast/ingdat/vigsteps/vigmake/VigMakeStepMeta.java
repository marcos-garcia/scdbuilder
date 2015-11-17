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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */

@Step(	
		id = "VigMakeStep",
		image = "com/bayesforecast/ingdat/vigsteps/vigmake/resources/vigmake.svg",
		i18nPackageName="com.bayesforecast.ingdat.vigsteps.vigmake",
		name="VigMakeStep.Name",
		description = "VigMakeStep.TooltipDesc",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform"
)
public class VigMakeStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = VigMakeStepMeta.class; // for i18n purposes
	
	/**
	 * Stores the name of the field added to the row-stream. 
	 */
	private String stepName;
	private String dateField;
	private String previousSufix;
	private String nextSufix;
	private String startFieldName;
	private String endFieldName;
	private String preloadedVig;
	private boolean isOrderedData;
	private List<String> idFields;
	private List<String> attributeFields;
	private List<String> statusFields;

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public VigMakeStepMeta() {
		super(); 
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new VigMakeStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new VigMakeStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new VigMakeStepData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		previousSufix = "_prev";
		nextSufix = "_next";
		startFieldName = "date_start";
		endFieldName = "date_end";
		dateField = "";
		stepName = "";
		preloadedVig = "";
		isOrderedData = true;
		idFields = new ArrayList<String>();
		attributeFields = new ArrayList<String>();
		statusFields = new ArrayList<String>();

	}

	
	public String getDateField() {
		return dateField;
	}

	public void setDateField(String dateField) {
		this.dateField = dateField;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public String getPreviousSufix() {
		return previousSufix;
	}

	public void setPreviousSufix(String previousSufix) {
		this.previousSufix = previousSufix;
	}

	public String getNextSufix() {
		return nextSufix;
	}

	public void setNextSufix(String nextSufix) {
		this.nextSufix = nextSufix;
	}

	public List<String> getIdFields() {
		return idFields;
	}

	public void setIdFields(List<String> idFields) {
		this.idFields = idFields;
	}

	public List<String> getAttributeFields() {
		return attributeFields;
	}

	public void setAttributeFields(List<String> attributeFields) {
		this.attributeFields = attributeFields;
	}

	public List<String> getStatusFields() {
		return statusFields;
	}

	public void setStatusFields(List<String> statusFields) {
		this.statusFields = statusFields;
	}

	public String getStartFieldName() {
		return startFieldName;
	}

	public void setStartFieldName(String startFieldName) {
		this.startFieldName = startFieldName;
	}

	public String getEndFieldName() {
		return endFieldName;
	}

	public void setEndFieldName(String endFieldName) {
		this.endFieldName = endFieldName;
	}

	public boolean isOrderedData() {
		return isOrderedData;
	}

	public void setOrderedData(boolean isOrderedData) {
		this.isOrderedData = isOrderedData;
	}
	
	public void setOrderedData(String isOrderedData) {
		if(isOrderedData == "Y"){
			this.isOrderedData = true;
		}else{
			this.isOrderedData = false;
		}
	}

	public String getPreloadedVig() {
		return preloadedVig;
	}

	public void setPreloadedVig(String preloadedVig) {
		this.preloadedVig = preloadedVig;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		// only one field to serialize
		List<String> xmlFields = new ArrayList<String>();
		String dateField_xml = XMLHandler.addTagValue("dateField", dateField);
		xmlFields.add(dateField_xml);
		String startFieldName_xml = XMLHandler.addTagValue("startFieldName", startFieldName);
		xmlFields.add(startFieldName_xml);
		String endFieldName_xml = XMLHandler.addTagValue("endFieldName", endFieldName);
		xmlFields.add(endFieldName_xml);
		String previousSufix_xml = XMLHandler.addTagValue("previousSufix", previousSufix);
		xmlFields.add(previousSufix_xml);
		String nextSufix_xml = XMLHandler.addTagValue("nextSufix", nextSufix);
		xmlFields.add(nextSufix_xml);
		String orderedData_xml = XMLHandler.addTagValue("orderedData", isOrderedData);
		xmlFields.add(orderedData_xml);
		String preloadedVig_xml = XMLHandler.addTagValue("preloadedVig", preloadedVig);
		xmlFields.add(preloadedVig_xml);
		
		String idFields_xml = XMLHandler.addTagValue("idFields", StringUtils.join(idFields, ","));
		xmlFields.add(idFields_xml);
		String attributeFields_xml = XMLHandler.addTagValue("attributeFields", StringUtils.join(attributeFields, ","));
		xmlFields.add(attributeFields_xml);
		String statusFields_xml = XMLHandler.addTagValue("statusFields", StringUtils.join(statusFields, ","));
		xmlFields.add(statusFields_xml);
		
		return StringUtils.join(xmlFields, "");
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param metaStore the metaStore to optionally read from
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {

		try {
			setStepName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "stepName")));
			setDateField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "dateField")));
			setStartFieldName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "startFieldName")));
			setEndFieldName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "endFieldName")));
			setPreviousSufix(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "previousSufix")));
			setNextSufix(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "nextSufix")));
			setOrderedData(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "orderedData")).startsWith("Y"));
			setPreloadedVig(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "preloadedVig")));
			setIdFields(Arrays.asList(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "idFields")).split(",")));
			setAttributeFields(Arrays.asList(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "attributeFields")).split(",")));
			setStatusFields(Arrays.asList(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "statusFields")).split(",")));
		} catch (Exception e) {
			throw new KettleXMLException("VigMake plugin unable to read step info from XML node", e);
		}

	}	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param metaStore				the metaStore to optionally write to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "stepName", stepName);
			rep.saveStepAttribute(id_transformation, id_step, "dateField", dateField);
			rep.saveStepAttribute(id_transformation, id_step, "startFieldName", startFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "endFieldName", endFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "previousSufix", previousSufix);
			rep.saveStepAttribute(id_transformation, id_step, "nextSufix", nextSufix);
			rep.saveStepAttribute(id_transformation, id_step, "orderedData", isOrderedData);
			rep.saveStepAttribute(id_transformation, id_step, "preloadedVig", preloadedVig);

			rep.saveStepAttribute(id_transformation, id_step, "idFields", StringUtils.join(idFields,","));
			rep.saveStepAttribute(id_transformation, id_step, "attributeFields", StringUtils.join(attributeFields,","));
			rep.saveStepAttribute(id_transformation, id_step, "statusFields", StringUtils.join(statusFields,","));
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param metaStore	the metaStore to optionally read from
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException  {
		try{
			stepName = rep.getStepAttributeString(id_step, "stepName");
			dateField = rep.getStepAttributeString(id_step, "dateField");
			startFieldName = rep.getStepAttributeString(id_step, "startFieldName");
			endFieldName = rep.getStepAttributeString(id_step, "endFieldName");
			previousSufix = rep.getStepAttributeString(id_step, "previousSufix");
			nextSufix = rep.getStepAttributeString(id_step, "nextSufix");
			isOrderedData = rep.getStepAttributeBoolean(id_step, "orderedData");
			preloadedVig = rep.getStepAttributeString(id_step, "preloadedVig");

			idFields = Arrays.asList(rep.getStepAttributeString(id_step, "idFields").split(","));
			attributeFields = Arrays.asList(rep.getStepAttributeString(id_step, "attributeFields").split(","));
			statusFields = Arrays.asList(rep.getStepAttributeString(id_step, "statusFields").split(","));
			
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param inputRowMeta		the row structure coming in to the step
	 * @param name 				the name of the step making the changes
	 * @param info				row structures of any info steps coming in
	 * @param nextStep			the description of a step this step is passing rows to
	 * @param space				the variable space for resolving variables
	 * @param repository		the repository instance optionally read from
	 * @param metaStore			the metaStore to optionally read from
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException{

		/*
		 * Appends the date_start and date_end fields
		 */
		try {
			inputRowMeta.removeValueMeta(dateField);
		} catch (KettleValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ValueMetaInterface vDateStart = new ValueMeta(startFieldName, ValueMeta.TYPE_DATE);
		ValueMetaInterface vDateEnd = new ValueMeta(endFieldName, ValueMeta.TYPE_DATE);
		vDateStart.setOrigin(name);
		vDateEnd.setOrigin(name);
		inputRowMeta.addValueMeta(vDateStart);
		inputRowMeta.addValueMeta(vDateEnd);
		
		for(String statusField : statusFields){
			ValueMetaInterface vFieldPrev = new ValueMeta(statusField+previousSufix, 	inputRowMeta.searchValueMeta(statusField).getType());
			vFieldPrev.setOrigin(name);
			inputRowMeta.addValueMeta(vFieldPrev);
		}
		
		for(String statusField : statusFields){
			ValueMetaInterface vFieldNext = new ValueMeta(statusField+nextSufix, 		inputRowMeta.searchValueMeta(statusField).getType());
			vFieldNext.setOrigin(name);
			inputRowMeta.addValueMeta(vFieldNext);
		}
		
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 *   @param metaStore	metaStore to optionally read from
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)  {
		
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}	
    	
	}

	@Override
	public void check(List<CheckResultInterface> arg0, TransMeta arg1,
			StepMeta arg2, RowMetaInterface arg3, String[] arg4, String[] arg5,
			RowMetaInterface arg6) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadXML(Node arg0, List<DatabaseMeta> arg1,
			Map<String, Counter> arg2) throws KettleXMLException {
		// TODO Auto-generated method stub
	}

	@Override
	public void readRep(Repository arg0, ObjectId arg1,
			List<DatabaseMeta> arg2, Map<String, Counter> arg3)
			throws KettleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRep(Repository arg0, ObjectId arg1, ObjectId arg2)
			throws KettleException {
		// TODO Auto-generated method stub
		
	}

}
