package com.bayesforecast.ingdat.vigsteps.vigmake;

import java.util.Date;
import java.util.Set;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;


/**
 * The Interface StateTreeAlgorithm.
 */
public interface StateTreeAlgorithm {
	
	/**
	 * Adds the state.
	 *
	 * @param vigMakeStep the vig make step
	 * @param item the item
	 * @param date the date
	 * @param state the state
	 * @throws StateConflictException the state conflict exception
	 */
	public void addState(VigMakeStep vigMakeStep, Item item, Date date, State state) throws StateConflictException;
	
	/**
	 * Clean states.
	 *
	 * @param item the item
	 */
	public void cleanStates(Item item);
	
	/**
	 * Mark absences.
	 *
	 * @param item the item
	 * @param dates the dates
	 */
	public void markAbsences(Item item, Set<Date> dates);
}
