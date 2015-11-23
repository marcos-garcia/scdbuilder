package com.bayesforecast.ingdat.vigsteps.vigmake.treealgo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;
import com.bayesforecast.ingdat.vigsteps.vigmake.VigMakeStep;
import com.bayesforecast.ingdat.vigsteps.vigmake.VigMakeStepData;

public class OrderedFillStateTreeAlgorithm extends FillStateTreeAlgorithm {
	
	private Date actual;
	private Date previousDate;
	private Map<Item,Date> lastProcessedDates;
	private Date maxProcessedDate;
	
	public OrderedFillStateTreeAlgorithm(){
		lastProcessedDates = new HashMap<Item,Date>();
	}
	
	@Override
	public void addState(VigMakeStep vigMakeStep, Item item, Date date, State state) throws StateConflictException {
		
		if(maxProcessedDate == null || date.compareTo(maxProcessedDate) > 0){
			maxProcessedDate = date;
		}
		
		if(item.getId().get(0).toString().compareTo("1971367") == 0){
			vigMakeStep.logDebug(item.toString());
			Set<Entry<Date, State>> set = item.getStates().entrySet();
			
			// 2. Recorre los estados de cada item
			for( Entry<Date, State> status : set){
				vigMakeStep.logBasic("\tStatus: "+status.toString());
				vigMakeStep.logBasic("\tPrevious: "+status.getValue().getPrevious());
				vigMakeStep.logBasic("\tNext: "+status.getValue().getNext());
			}
			vigMakeStep.logBasic("To - Insert Status: "+state.toString());
		}		

		TreeMap<Date, State> states = item.getStates();
		Map.Entry<Date, State> previous = states.lowerEntry(date);
		Map.Entry<Date, State> next = states.higherEntry(date);

		state.setNext(null);
		state.setPrevious(null);
		
		if(previous != null){
			previous.getValue().setNext(state);
			state.setPrevious(previous.getValue());
		}

		if(next != null){
			state.setNext(next.getValue());
			next.getValue().setPrevious(state);
		}

		states.put(date, state);
		vigMakeStep.emitItem((VigMakeStepData) vigMakeStep.getStepDataInterface(), item, false);
		states.clear();
		
		lastProcessedDates.put(item, date);
		states.put(date, state);

	}
	
	public void cleanStates(Item item){
	}
	
	public void markAbsences(Item item, Set<Date> dates){
	}	
	
	public StateTreeAlgorithm getNextTreeAlgorithm(){
		for(Entry<Item,Date> i : lastProcessedDates.entrySet()){
			i.setValue(maxProcessedDate);
		}
		return new OrderedStateTreeAlgorithm(lastProcessedDates, maxProcessedDate, maxProcessedDate);
	}

}
