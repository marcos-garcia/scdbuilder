package com.bayesforecast.ingdat.vigsteps.vigmake;

import java.util.Date;
import java.util.Set;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;

public interface StateTreeAlgorithm {
	
	public void addState(VigMakeStep vigMakeStep, Item item, Date date, State state) throws StateConflictException;
	
	public void cleanStates(Item item);
	
	public void markAbsences(Item item, Set<Date> dates);
}
