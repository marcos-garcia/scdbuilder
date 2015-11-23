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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.FillStateTreeAlgorithm;
import com.bayesforecast.ingdat.vigsteps.vigmake.treealgo.StateTreeAlgorithm;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDataInterface.
 *   
 * Implementing classes inherit from BaseStepData, which implements the entire
 * interface completely. 
 * 
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are:
 * result sets, temporary data, caching indexes, etc.
 *   
 * The implementation for the demo step stores the output row structure in 
 * the data class. 
 *   
 */
public class VigMakeStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	public HashMap<List<Object>,Item> items; 
	public HashMap<Date,Boolean> processedDates;
	public StateTreeAlgorithm stateInsertionAlgo;
	public FillStateTreeAlgorithm fillStateInsertionAlgo;
	

	/** Flag indicating if the tree is preloading info. */
	public boolean preloadingTree;
	
	/** Preloading input RowSet*/
	public RowSet preloadingInputRowset;
	
	
    public VigMakeStepData()
	{
		super();
	}
}
	
