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

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepDialogInterface. Classes implementing
 * this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the
 * step's meta object) - write back any changes the user makes to the step's
 * meta object - report whether the user changed any settings when confirming
 * the dialog
 * 
 */
public class VigMakeStepDialog extends BaseStepDialog implements
		StepDialogInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package
	 * of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = VigMakeStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed
	private VigMakeStepMeta meta;

	// text field
	private Combo dateFieldName;
	private Text startFieldName;
	private Text endFieldName;
	private Text previousPrefix;
	private Text nextPrefix;
	private Button orderedData;

	// table fields
	private TableView idFields;
	private TableView attributesFields;
	private TableView statusFields;

	// label items
	private Label lStartFieldName;
	private Label lEndFieldName;
	private Label lPreviousSuf;
	private Label lNextSuf;
	private Label lOrderedData;
	private Label lDateField;
	private Label lIdFields;
	private Label lAttributesFields;
	private Label lStatusFields;

	// button items
	private Button bObtenerIdFields;
	private Button bObtenerAttrFields;
	private Button bObtenerStatFields;
	
	// auxiliar variable
	private String previousValuesTo;

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write
	 * settings from/to it.
	 * 
	 * @param parent
	 *            the SWT shell to open the dialog in
	 * @param in
	 *            the meta object holding the step's settings
	 * @param transMeta
	 *            transformation description
	 * @param sname
	 *            the step name
	 */
	public VigMakeStepDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (VigMakeStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of
	 * the step. It should open the dialog and return only once the dialog has
	 * been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the
	 * constructor) must be updated to reflect the new step settings. The
	 * changed flag of the meta object must reflect whether the step
	 * configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and
	 * its changed flag must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has
	 * confirmed the dialog, or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
				| SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);

		// Save the value of the changed flag on the meta object. If the user
		// cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();

		// The ModifyListener used on all controls. It will update the meta
		// object to
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};

		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "VigMake.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname
				.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// orderedData value
		lOrderedData = new Label(shell, SWT.RIGHT);
		lOrderedData.setText(BaseMessages.getString(PKG,
				"VigMake.OrderedData.Label"));
		props.setLook(lOrderedData);
		FormData fdllOrderedData = new FormData();
		fdllOrderedData.left = new FormAttachment(0, 0);
		fdllOrderedData.right = new FormAttachment(middle, -margin);
		fdllOrderedData.top = new FormAttachment(wStepname, margin);
		lOrderedData.setLayoutData(fdllOrderedData);

		orderedData = new Button(shell, SWT.CHECK);
		props.setLook(orderedData);
		//orderedData.addModifyListener(lsMod);
		FormData fdorderedData = new FormData();
		fdorderedData.left = new FormAttachment(middle, 0);
		fdorderedData.right = new FormAttachment(100, 0);
		fdorderedData.top = new FormAttachment(wStepname, margin);
		orderedData.setLayoutData(fdorderedData);

		// previous suffix value
		lPreviousSuf = new Label(shell, SWT.RIGHT);
		lPreviousSuf.setText(BaseMessages.getString(PKG,
				"VigMake.PrevSuffix.Label"));
		props.setLook(lPreviousSuf);
		FormData fdllPreviousSuf = new FormData();
		fdllPreviousSuf.left = new FormAttachment(0, 0);
		fdllPreviousSuf.right = new FormAttachment(middle, -margin);
		fdllPreviousSuf.top = new FormAttachment(orderedData, margin);
		lPreviousSuf.setLayoutData(fdllPreviousSuf);

		previousPrefix = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(previousPrefix);
		previousPrefix.addModifyListener(lsMod);
		FormData fdpreviousPrefix = new FormData();
		fdpreviousPrefix.left = new FormAttachment(middle, 0);
		fdpreviousPrefix.right = new FormAttachment(100, 0);
		fdpreviousPrefix.top = new FormAttachment(orderedData, margin);
		previousPrefix.setLayoutData(fdpreviousPrefix);

		// next suffix value
		lNextSuf = new Label(shell, SWT.RIGHT);
		lNextSuf.setText(BaseMessages
				.getString(PKG, "VigMake.NextSuffix.Label"));
		props.setLook(lNextSuf);
		FormData fdllNextSuf = new FormData();
		fdllNextSuf.left = new FormAttachment(0, 0);
		fdllNextSuf.right = new FormAttachment(middle, -margin);
		fdllNextSuf.top = new FormAttachment(previousPrefix, margin);
		lNextSuf.setLayoutData(fdllNextSuf);

		nextPrefix = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(nextPrefix);
		nextPrefix.addModifyListener(lsMod);
		FormData fdnextPrefix = new FormData();
		fdnextPrefix.left = new FormAttachment(middle, 0);
		fdnextPrefix.right = new FormAttachment(100, 0);
		fdnextPrefix.top = new FormAttachment(previousPrefix, margin);
		nextPrefix.setLayoutData(fdnextPrefix);

		// start field name value
		lStartFieldName = new Label(shell, SWT.RIGHT);
		lStartFieldName.setText(BaseMessages.getString(PKG,
				"VigMake.StartFieldName.Label"));
		props.setLook(lStartFieldName);
		FormData fdllStartFieldName = new FormData();
		fdllStartFieldName.left = new FormAttachment(0, 0);
		fdllStartFieldName.right = new FormAttachment(middle, -margin);
		fdllStartFieldName.top = new FormAttachment(nextPrefix, margin);
		lStartFieldName.setLayoutData(fdllStartFieldName);

		startFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(startFieldName);
		startFieldName.addModifyListener(lsMod);
		FormData fdstartFieldName = new FormData();
		fdstartFieldName.left = new FormAttachment(middle, 0);
		fdstartFieldName.right = new FormAttachment(100, 0);
		fdstartFieldName.top = new FormAttachment(nextPrefix, margin);
		startFieldName.setLayoutData(fdstartFieldName);

		// end field name value
		lEndFieldName = new Label(shell, SWT.RIGHT);
		lEndFieldName.setText(BaseMessages.getString(PKG,
				"VigMake.EndFieldName.Label"));
		props.setLook(lEndFieldName);
		FormData fdllEndFieldName = new FormData();
		fdllEndFieldName.left = new FormAttachment(0, 0);
		fdllEndFieldName.right = new FormAttachment(middle, -margin);
		fdllEndFieldName.top = new FormAttachment(startFieldName, margin);
		lEndFieldName.setLayoutData(fdllEndFieldName);

		endFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(endFieldName);
		endFieldName.addModifyListener(lsMod);
		FormData fdendFieldName = new FormData();
		fdendFieldName.left = new FormAttachment(middle, 0);
		fdendFieldName.right = new FormAttachment(100, 0);
		fdendFieldName.top = new FormAttachment(startFieldName, margin);
		endFieldName.setLayoutData(fdendFieldName);

		// date field value
		lDateField = new Label(shell, SWT.RIGHT);
		lDateField.setText(BaseMessages.getString(PKG,
				"VigMake.DateField.Label"));
		props.setLook(lDateField);
		FormData fdllDateField = new FormData();
		fdllDateField.left = new FormAttachment(0, 0);
		fdllDateField.right = new FormAttachment(middle, -margin);
		fdllDateField.top = new FormAttachment(endFieldName, margin);
		lDateField.setLayoutData(fdllDateField);

		dateFieldName = new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		try {
			dateFieldName.setItems(transMeta.getPrevStepFields(stepMeta).getFieldNames());
		} catch (KettleStepException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		props.setLook(dateFieldName);
		dateFieldName.addModifyListener(lsMod);
		FormData fddateFieldName = new FormData();
		fddateFieldName.left = new FormAttachment(middle, 0);
		fddateFieldName.right = new FormAttachment(100, 0);
		fddateFieldName.top = new FormAttachment(endFieldName, margin);
		dateFieldName.setLayoutData(fddateFieldName);

		// id fields
		lIdFields = new Label(shell, SWT.RIGHT);
		lIdFields.setText(BaseMessages.getString(PKG,
				"VigMake.IdFields.Label"));
		props.setLook(lIdFields);
		FormData fdllIdFields = new FormData();
		fdllIdFields.left = new FormAttachment(20, 0);
		//fdllIdFields.right = new FormAttachment(middle, -margin);
		fdllIdFields.top = new FormAttachment(dateFieldName, margin);
		lIdFields.setLayoutData(fdllIdFields);
		
		bObtenerIdFields = new Button( shell, SWT.PUSH );
		bObtenerIdFields.setText( BaseMessages.getString( PKG, "VigMake.ObtainFields.Button" ) );
		FormData fdbObtenerIdFields = new FormData();
		fdbObtenerIdFields.left = new FormAttachment(20, 0);
		//fdbObtenerIdFields.right = new FormAttachment( 100, 0 );
		fdbObtenerIdFields.bottom = new FormAttachment(80, 0);
		bObtenerIdFields.setLayoutData( fdbObtenerIdFields );

		ColumnInfo[] colinf = null;
		try {
			colinf = new ColumnInfo[] { new ColumnInfo(
					BaseMessages.getString(PKG, "VigMake.ColumnInfo.Fieldname"),
					ColumnInfo.COLUMN_TYPE_CCOMBO, transMeta.getPrevStepFields(stepMeta).getFieldNames(), false) };
		} catch (KettleStepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		idFields = new TableView(transMeta, shell, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI, colinf.clone(),  1,
				false, lsMod, props);
		props.setLook(idFields);
		idFields.addModifyListener(lsMod);
		FormData fdidFields = new FormData();
		fdidFields.left = new FormAttachment(20, 0);
		fdidFields.right = new FormAttachment(40, 0);
		fdidFields.top = new FormAttachment(lIdFields, margin);
		fdidFields.bottom = new FormAttachment(bObtenerIdFields, -margin);
		idFields.setLayoutData(fdidFields);

		// attribute fields
		lAttributesFields = new Label(shell, SWT.RIGHT);
		lAttributesFields.setText(BaseMessages.getString(PKG,
				"VigMake.AttributesFields.Label"));
		props.setLook(lAttributesFields);
		FormData fdllAttributesFields = new FormData();
		fdllAttributesFields.left = new FormAttachment(idFields, margin);
		//fdllAttributesFields.right = new FormAttachment(middle, -margin);
		fdllAttributesFields.top = new FormAttachment(dateFieldName, margin);
		lAttributesFields.setLayoutData(fdllAttributesFields);
		
		bObtenerAttrFields = new Button( shell, SWT.PUSH );
		bObtenerAttrFields.setText( BaseMessages.getString( PKG, "VigMake.ObtainFields.Button" ) );
		FormData fdbObtenerAttrFields = new FormData();
		fdbObtenerAttrFields.left = new FormAttachment(idFields, margin);
		//fdbObtenerAttrFields.right = new FormAttachment( 100, 0 );
		fdbObtenerAttrFields.bottom = new FormAttachment(80, 0);
		bObtenerAttrFields.setLayoutData( fdbObtenerAttrFields );

		attributesFields = new TableView(transMeta, shell, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI, colinf.clone(), 1,
				false, lsMod, props);
		props.setLook(attributesFields);
		attributesFields.addModifyListener(lsMod);
		FormData fdattributesFields = new FormData();
		fdattributesFields.left = new FormAttachment(idFields, margin);
		fdattributesFields.right = new FormAttachment(60, 0);
		fdattributesFields.top = new FormAttachment(lAttributesFields, margin);
		fdattributesFields.bottom = new FormAttachment(bObtenerAttrFields, -margin);
		attributesFields.setLayoutData(fdattributesFields);


		// status fields
		lStatusFields = new Label(shell, SWT.RIGHT);
		lStatusFields.setText(BaseMessages.getString(PKG,
				"VigMake.StatusFields.Label"));
		props.setLook(lStatusFields);
		FormData fdllStatusFields = new FormData();
		fdllStatusFields.left = new FormAttachment(attributesFields, margin);
		//fdllStatusFields.right = new FormAttachment(middle, -margin);
		fdllStatusFields.top = new FormAttachment(dateFieldName, margin);
		lStatusFields.setLayoutData(fdllStatusFields);
		
		bObtenerStatFields = new Button( shell, SWT.PUSH );
		bObtenerStatFields.setText( BaseMessages.getString( PKG, "VigMake.ObtainFields.Button" ) );
		FormData fdbObtenerStatFields = new FormData();
		fdbObtenerStatFields.left = new FormAttachment(attributesFields, margin);
		//fdbObtenerStatFields.right = new FormAttachment( 100, 0 );
		fdbObtenerStatFields.bottom = new FormAttachment(80, 0);
	    bObtenerStatFields.setLayoutData( fdbObtenerStatFields );

		statusFields = new TableView(transMeta, shell, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI, colinf.clone(), 1,
				false, lsMod, props);
		props.setLook(statusFields);
		statusFields.addModifyListener(lsMod);
		FormData fdstatusFields = new FormData();
		fdstatusFields.left = new FormAttachment(attributesFields, margin);
		fdstatusFields.right = new FormAttachment(80, 0);
		fdstatusFields.top = new FormAttachment(lStatusFields, margin);
		fdstatusFields.bottom = new FormAttachment(bObtenerStatFields, -margin);
		statusFields.setLayoutData(fdstatusFields);

		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell,
				new Button[] { wOK, wCancel }, margin, bObtenerIdFields);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		Listener lsGetId = new Listener() {
			public void handleEvent(Event e) {
				get(idFields);
			}
		};

		Listener lsGetAttributes = new Listener() {
			public void handleEvent(Event e) {
				get(attributesFields);
			}
		};

		Listener lsGetStatus = new Listener() {
			public void handleEvent(Event e) {
				get(statusFields);
			}
		};
		

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		bObtenerIdFields.addListener(SWT.Selection, lsGetId);
		bObtenerAttrFields.addListener(SWT.Selection, lsGetAttributes);
		bObtenerStatFields.addListener(SWT.Selection, lsGetStatus);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);
		previousPrefix.addSelectionListener(lsDef);
		nextPrefix.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the
		// dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();

		// restore the changed flag to original value, as the modify listeners
		// fire during dialog population
		meta.setChanged(changed);

		// open dialog and enter event loop
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have
		// been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		previousPrefix.setText(meta.getPreviousSufix());
		nextPrefix.setText(meta.getNextSufix());
		startFieldName.setText(meta.getStartFieldName());
		endFieldName.setText(meta.getEndFieldName());
		dateFieldName.setText(meta.getDateField());
		orderedData.setSelection(meta.isOrderedData());
		for(String id : meta.getIdFields()){
			idFields.add(id);
		}
		idFields.removeEmptyRows();
		idFields.setRowNums();
		for(String attr : meta.getAttributeFields()){
			attributesFields.add(attr);
		}
		attributesFields.removeEmptyRows();
		attributesFields.setRowNums();
		for(String stat : meta.getStatusFields()){
			statusFields.add(stat);
		}
		statusFields.removeEmptyRows();
		statusFields.setRowNums();
	}

	private void get(TableView tableToFill) {
		try {
		    RowMetaInterface r = transMeta.getPrevStepFields( stepname );
			BaseStepDialog.getFieldsFromPrevious(r, tableToFill, 1,
					new int[] { 1 }, new int[] {}, -1, -1, null);
		} catch (KettleException ke) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG,
							"VigMake.FailedToGetFields.DialogTitle"),
					BaseMessages
							.getString(PKG,
									"VigMake.FailedToGetFields.DialogMessage"),
					ke);
		}
	}

	/**
	 * Called when the user cancels the dialog.
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open()
		// method.
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}

	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open()
		// method.
		// Setting to step name from the dialog control
		stepname = wStepname.getText();
		// Setting the settings to the meta object
		logBasic("orderedData: "+orderedData.getSelection());
		meta.setOrderedData(orderedData.getSelection());
		meta.setPreviousSufix(previousPrefix.getText());
		meta.setNextSufix(nextPrefix.getText());
		meta.setStartFieldName(startFieldName.getText());
		meta.setEndFieldName(endFieldName.getText());
		meta.setDateField(dateFieldName.getText());
		meta.setIdFields(Arrays.asList(idFields.getItems(0)));
		meta.setAttributeFields(Arrays.asList(attributesFields.getItems(0)));
		meta.setStatusFields(Arrays.asList(statusFields.getItems(0)));
		// close the SWT dialog window
		dispose();
	}

	@Override
	public void setRepository(Repository arg0) {
		// TODO Auto-generated method stub

	}
}
