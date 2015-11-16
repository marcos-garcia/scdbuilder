package com.bayesforecast.ingdat.vigsteps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Item implements Comparable{

	private List<Object> id;
	private List<Object> attributes;
	private TreeMap<Date,State> states;
	
	public Item(List<Object> id, List<Object> attributes) {
		super();
		this.id = id;
		this.attributes = attributes;
		this.states = new TreeMap<Date,State>();
	}

	public List<Object> getId() {
		return id;
	}

	public void setId(List<Object> id) {
		this.id = id;
	}

	public List<Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Object> attributes) {
		this.attributes = attributes;
	}

	public TreeMap<Date, State> getStates() {
		return states;
	}

	public void setStates(TreeMap<Date, State> states) {
		this.states = states;
	}

	public String addState(Date date, State state) throws StateConflictException{
		String retorno = "";
		retorno += "Comienza addState\n";
		if(this.states.containsKey(date)){
			retorno += "\tContiene la fecha\n";
			// Comprobar si se tiene el mismo estado (ignora registro)
			if(this.states.get(date).compareTo(state) != 0){
				// Excepción: no pueden existir diferentes estados en una misma fecha
				throw new StateConflictException(this.states.get(date), state);
			}
		}else{
			retorno += "\tNo contiene la fecha\n";
			state.setNext(null);
			state.setPrevious(null);
			boolean insert = true;
			// Si está vacío, se inserta el nuevo estado y listo
			if(!this.states.isEmpty()){
				retorno += "\t\tNo está vacío\n";
				// Si no lo está, se hacen las comprobaciones
				Map.Entry<Date, State> previous = this.states.lowerEntry(date);
				Map.Entry<Date, State> next = this.states.higherEntry(date);
				Map.Entry<Date, State> first = this.states.firstEntry();
				Map.Entry<Date, State> last = this.states.lastEntry();
				
				// Si no es el primero, se comprueba por la izquierda
				if(previous != null){
					retorno += "\t\tEl previo no es nulo\n";
						// Actualizar punteros
						state.setPrevious(previous.getValue());
						previous.getValue().setNext(state);
				}
					
				// Si no es el último, se comprueba por la derecha
				if(next != null){
					retorno += "\t\tEl next no es nulo\n";
						next.getValue().setPrevious(state);
						state.setNext(next.getValue());
				}
				retorno += "\t\tFin de no está vacio";
				
			}
			if(insert){
				retorno += "\tInserta en arbol {{"+date.toString()+"},{"+state.toString()+"}}";
				this.states.put(date, state);
			}
		}
		retorno += "Termina addState\n";
		return retorno;
	}
	
	/*
	public String addState(Date date, State state) throws StateConflictException{
		String retorno = "";
		retorno += "Comienza addState\n";
		if(this.states.containsKey(date)){
			retorno += "\tContiene la fecha\n";
			// Comprobar si se tiene el mismo estado (ignora registro)
			if(this.states.get(date).compareTo(state) != 0){
				// Excepción: no pueden existir diferentes estados en una misma fecha
				throw new StateConflictException(this.states.get(date), state);
			}
		}else{
			retorno += "\tNo contiene la fecha\n";
			state.setNext(null);
			state.setPrevious(null);
			boolean insert = true;
			// Si está vacío, se inserta el nuevo estado y listo
			if(!this.states.isEmpty()){
				retorno += "\t\tNo está vacío\n";
				// Si no lo está, se hacen las comprobaciones
				Map.Entry<Date, State> previous = this.states.lowerEntry(date);
				Map.Entry<Date, State> next = this.states.higherEntry(date);
				Map.Entry<Date, State> first = this.states.firstEntry();
				Map.Entry<Date, State> last = this.states.lastEntry();
				
				// Si no es el primero, se comprueba por la izquierda
				if(previous != null){
					retorno += "\t\tEl previo no es nulo\n";
					if(previous.getValue().compareTo(state) == 0){
						retorno += "\t\t\tIgual que el previous: {"+previous.getValue().toString()+"} vs {"+
								state.toString()+"} --> NO INSERTA\n";
						insert = false;
					}else{
						retorno += "\t\t\tDistinto que el previous: {"+previous.getValue().toString()+"} vs {"+
								state.toString()+"} --> INSERTA\n";
						// Actualizar punteros
						state.setPrevious(previous.getValue());
						previous.getValue().setNext(state);
					}
				}
					
				// Si no es el último, se comprueba por la derecha
				if(next != null){
					retorno += "\t\tEl next no es nulo\n";
					if(next.getValue().compareTo(state) == 0){
						retorno += "\t\t\tIgual que el next: {"+next.getValue().toString()+"} vs {"+
								state.toString()+"}\n";
						State nextOfnext = next.getValue().getNext(); 
						if(nextOfnext != null){
							retorno += "\t\t\tHabemus nextofnext\n";
							state.setNext(nextOfnext);
							nextOfnext.setPrevious(state);
						}
						this.states.remove(next.getKey());
					}else{
						next.getValue().setPrevious(state);
						state.setNext(next.getValue());
					}
				}
				retorno += "\t\tFin de no está vacio";
				
			}
			if(insert){
				retorno += "\tInserta en arbol {{"+date.toString()+"},{"+state.toString()+"}}";
				this.states.put(date, state);
			}
		}
		retorno += "Termina addState\n";
		return retorno;
	}
*/

	public void addStates(Map<Date,State> states) throws StateConflictException{
		// Algoritmo para insertar varios estados en vigencias estado
		for(Map.Entry<Date,State> entry : states.entrySet()){
			this.addState(entry.getKey(), entry.getValue());
		}
	}
	
	public void cleanStates(){
		ArrayList<Date> datesToRemove = new ArrayList<Date>();
		for(Entry<Date, State> entry : states.entrySet()){
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
			states.remove(date);
		}
	}
	
	public void markAbsences(Set<Date> dates){
		for(Date date : dates){
			// Si no hay estado con la fecha y hay estados previos  a la fecha, hay que insertar una ausencia
			State previous = states.floorEntry(date).getValue();
			if(!states.containsKey(date) && previous != null){
				State absenceState = new State();
				State next = previous.getNext();
				previous.setNext(absenceState);
				absenceState.setPrevious(previous);
				if(next != null){
					next.setPrevious(absenceState);
					absenceState.setNext(next);
				}
				states.put(date, absenceState);
			}
		}
	}

	public int compareTo(Object o) {
		// Comparador de todos los objetos de la lista
		List<Object> id = ((Item) o).getId();
		int result = 0;
		if(id.size() == this.id.size()){
			for(int i = 0;i < id.size();i++){
				if(!this.id.get(i).equals(id.get(i))){
					result = 1;
				}
			}
		}else{
			result = -1;
		}
		return result;
	}
	
}
