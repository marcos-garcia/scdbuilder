package com.bayesforecast.ingdat.vigsteps.vigmake.treealgo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;
import com.bayesforecast.ingdat.vigsteps.vigmake.VigMakeStep;
import com.bayesforecast.ingdat.vigsteps.vigmake.VigMakeStepData;

public class GeneralFillStateTreeAlgorithm extends FillStateTreeAlgorithm {

	@Override
	public void addState(VigMakeStep vigMakeStep, Item item, Date date, State state) throws StateConflictException {

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

	}
	
	public void cleanStates(Item item){
	}
	
	public void markAbsences(Item item, Set<Date> dates){
	}	
	
	public StateTreeAlgorithm getNextTreeAlgorithm(){
		return new GeneralStateTreeAlgorithm();
	}

}
