package com.bayesforecast.ingdat.vigsteps.vigmake;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.bayesforecast.ingdat.vigsteps.Item;
import com.bayesforecast.ingdat.vigsteps.State;
import com.bayesforecast.ingdat.vigsteps.StateConflictException;

public class GeneralStateTreeAlgorithm implements StateTreeAlgorithm {

	@Override
	public void addState(VigMakeStep vigMakeStep, Item item, Date date, State state) throws StateConflictException {
		TreeMap<Date, State> states = item.getStates();
		if(states.containsKey(date)){
			// Comprobar si se tiene el mismo estado (ignora registro)
			if(states.get(date).compareTo(state) != 0){
				// Excepción: no pueden existir diferentes estados en una misma fecha
				throw new StateConflictException(states.get(date), state);
			}
		}else{
			state.setNext(null);
			state.setPrevious(null);
			boolean insert = true;
			// Si está vacío, se inserta el nuevo estado y listo
			if(!states.isEmpty()){
				// Si no lo está, se hacen las comprobaciones
				Map.Entry<Date, State> previous = states.lowerEntry(date);
				Map.Entry<Date, State> next = states.higherEntry(date);
				Map.Entry<Date, State> first = states.firstEntry();
				Map.Entry<Date, State> last = states.lastEntry();
				
				// Si no es el primero, se comprueba por la izquierda
				if(previous != null){
						// Actualizar punteros
						state.setPrevious(previous.getValue());
						previous.getValue().setNext(state);
				}
					
				// Si no es el último, se comprueba por la derecha
				if(next != null){
						next.getValue().setPrevious(state);
						state.setNext(next.getValue());
				}
				
			}
			if(insert){
				states.put(date, state);
			}
		}
	}
	
	public void cleanStates(Item item){
		ArrayList<Date> datesToRemove = new ArrayList<Date>();
		for(Entry<Date, State> entry : item.getStates().entrySet()){
			State s = entry.getValue();
			if(s.getPrevious() != null && s.getPrevious().compareTo(s) == 0){
				datesToRemove.add(entry.getKey());
				s.getPrevious().setNext(s.getNext());
				if(s.getNext() != null){
					s.getNext().setPrevious(s.getPrevious());
				}
			}
		}
		for(Date date : datesToRemove){
			item.getStates().remove(date);
		}
	}
	
	public void markAbsences(Item item, Set<Date> dates){
		for(Date date : dates){
			// Si no hay estado con la fecha y hay estados previos  a la fecha, hay que insertar una ausencia
			Entry<Date, State> previousEntry = item.getStates().lowerEntry(date);
			if(!item.getStates().containsKey(date) && previousEntry != null){
				State absenceState = new State();
				State previous = previousEntry.getValue();
				State next = previous.getNext();
				previous.setNext(absenceState);
				absenceState.setPrevious(previous);
				if(next != null){
					next.setPrevious(absenceState);
					absenceState.setNext(next);
				}
				item.getStates().put(date, absenceState);
			}
		}
	}	

}
