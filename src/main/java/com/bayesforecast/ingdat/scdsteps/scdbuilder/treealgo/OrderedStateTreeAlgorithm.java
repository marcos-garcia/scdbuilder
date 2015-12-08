package com.bayesforecast.ingdat.scdsteps.scdbuilder.treealgo;

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

import com.bayesforecast.ingdat.scdsteps.Item;
import com.bayesforecast.ingdat.scdsteps.State;
import com.bayesforecast.ingdat.scdsteps.StateConflictException;
import com.bayesforecast.ingdat.scdsteps.scdbuilder.SCDBuildStep;
import com.bayesforecast.ingdat.scdsteps.scdbuilder.SCDBuildStepData;

public class OrderedStateTreeAlgorithm implements StateTreeAlgorithm {

	private Date actual;
	private Date previousDate;
	private Map<Item,Date> lastProcessedDates;
	
	public OrderedStateTreeAlgorithm(){
		lastProcessedDates = new HashMap<Item,Date>();
	}
	
	public OrderedStateTreeAlgorithm(Map<Item, Date> lastProcessedDates, Date actual, Date previousDate) {
		this.actual = actual;
		this.previousDate = previousDate;
		this.lastProcessedDates = lastProcessedDates;
	}

	@Override
	public void addState(SCDBuildStep vigMakeStep, Item item, Date date, State state) throws StateConflictException {
		if(!date.equals(actual)){
			previousDate = actual;
			actual = date;
		}
		TreeMap<Date, State> states = item.getStates();
		if(states.containsKey(date)){
			// Comprobar si se tiene el mismo estado (ignora registro)
			if(states.get(date).compareTo(state) != 0){
				// Excepci�n: no pueden existir diferentes estados en una misma fecha
				throw new StateConflictException(states.get(date), state);
			}
		}else{
			state.setNext(null);
			state.setPrevious(null);
			boolean insert = true;
			// Si est� vac�o, se inserta el nuevo estado y listo
			if(!states.isEmpty()){
				// Si no lo est�, se hacen las comprobaciones
				Map.Entry<Date, State> previous = states.lowerEntry(date);

				// Si no es el primero, se comprueba por la izquierda
				if(previous != null){
						if(previous.getValue().compareTo(state) == 0){
							if(lastProcessedDates.get(item).before(previousDate)){
								State absenceState = new State();
								previous.getValue().setNext(absenceState);
								absenceState.setPrevious(previous.getValue());
								item.getStates().put(previousDate, absenceState);
								vigMakeStep.emitItem((SCDBuildStepData) vigMakeStep.getStepDataInterface(), item, true);
								states.clear();
							}else{
								lastProcessedDates.put(item, date);
								return;
							}
						}else{
							// Actualizar punteros
							if(lastProcessedDates.get(item).before(previousDate)){
								State absenceState = new State();
								previous.getValue().setNext(absenceState);
								absenceState.setPrevious(previous.getValue());
								item.getStates().put(previousDate, absenceState);
								previous = item.getStates().ceilingEntry(previousDate);
							}
							state.setPrevious(previous.getValue());
							previous.getValue().setNext(state);
							states.put(date, state);
							vigMakeStep.emitItem((SCDBuildStepData) vigMakeStep.getStepDataInterface(), item, false);
							states.clear();
							previous.getValue().setPrevious(null);
						}
				}
				
			}
			lastProcessedDates.put(item, date);
			states.put(date, state);
		}
	}
	
	public void cleanStates(Item item){
	}

	public void markAbsences(Item item, Set<Date> dates){
		SortedSet<Date> sortedDates = new TreeSet<Date>(dates);
		Date lastDate = sortedDates.last();
		Date lastItemDate = lastProcessedDates.get(item);
		if(lastDate.after(lastItemDate)){
			State absenceState = new State();
			Date nextDate = sortedDates.tailSet(DateUtils.addDays(lastItemDate, 1)).first();
			item.getStates().lastEntry().getValue().setNext(absenceState);
			absenceState.setNext(item.getStates().lastEntry().getValue());
			item.getStates().put(nextDate, absenceState);
		}
	}

}
