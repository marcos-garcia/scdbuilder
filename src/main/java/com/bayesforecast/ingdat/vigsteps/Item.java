package com.bayesforecast.ingdat.vigsteps;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * The Class Item.
 */
public class Item implements Comparable<Item>{

	/** The id. */
	private List<Object> id;
	
	/** The attributes. */
	private List<Object> attributes;
	
	/** The states. */
	private TreeMap<Date,State> states;
	
	/**
	 * Instantiates a new item.
	 *
	 * @param id the id
	 * @param attributes the attributes
	 */
	public Item(List<Object> id, List<Object> attributes) {
		super();
		this.id = id;
		this.attributes = attributes;
		this.states = new TreeMap<Date,State>();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public List<Object> getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(List<Object> id) {
		this.id = id;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public List<Object> getAttributes() {
		return attributes;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attributes the new attributes
	 */
	public void setAttributes(List<Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets the states.
	 *
	 * @return the states
	 */
	public TreeMap<Date, State> getStates() {
		return states;
	}

	/**
	 * Sets the states.
	 *
	 * @param states the states
	 */
	public void setStates(TreeMap<Date, State> states) {
		this.states = states;
	}

	/**
	 * Adds the state.
	 *
	 * @param date the date
	 * @param state the state
	 * @return the string
	 * @throws StateConflictException the state conflict exception
	 */
	public String addState(Date date, State state) throws StateConflictException{
		String retorno = "";
		retorno += "Comienza addState\n";
		// 1. Se comprueba si existe un estado en la fecha
		if(this.states.containsKey(date)){
			retorno += "\tContiene la fecha\n";
			// 1.1 Comprobar si se tiene el mismo estado ( en cuyo caso ignora registro)
			if(this.states.get(date).compareTo(state) != 0){
				// Excepción: no pueden existir diferentes estados en una misma fecha
				throw new StateConflictException(this.states.get(date), state);
			}
		}else{
			retorno += "\tNo contiene la fecha\n";
			state.setNext(null);
			state.setPrevious(null);
			boolean insert = true;
			// 2. Si está vacío, se inserta el nuevo estado y listo
			if(!this.states.isEmpty()){
				retorno += "\t\tNo está vacío\n";
				// 2.1 Si no lo está, se hacen las comprobaciones
				Map.Entry<Date, State> previous = this.states.lowerEntry(date);
				Map.Entry<Date, State> next = this.states.higherEntry(date);
				
				// 2.2 Si no es el primero, se comprueban los punteros por la izquierda
				if(previous != null){
					retorno += "\t\tEl previo no es nulo\n";
					// 2.2.1 Actualizar punteros
					state.setPrevious(previous.getValue());
					previous.getValue().setNext(state);
				}
					
				// 2.3 Si no es el último, se comprueban los punteross por la derecha
				if(next != null){
					retorno += "\t\tEl next no es nulo\n";
					// 2.3.1 Actualizar punteros
					next.getValue().setPrevious(state);
					state.setNext(next.getValue());
				}
				retorno += "\t\tFin de no está vacio";
				
			}
			// 3. Inserta el estado cuando se haya marcado a true el booleano insert
			if(insert){
				retorno += "\tInserta en arbol {{"+date.toString()+"},{"+state.toString()+"}}";
				this.states.put(date, state);
			}
		}
		retorno += "Termina addState\n";
		return retorno;
	}


	/**
	 * Adds the states.
	 *
	 * @param states the states
	 * @throws StateConflictException the state conflict exception
	 */
	public void addStates(Map<Date,State> states) throws StateConflictException{
		// Algoritmo para insertar varios estados en vigencias estado
		for(Map.Entry<Date,State> entry : states.entrySet()){
			this.addState(entry.getKey(), entry.getValue());
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Item o) {
		// Comparador de todos los objetos de la lista
		List<Object> id = o.getId();
		int result = 0;
		// 1. Comprobación del tamaño del array
		if(id.size() == this.id.size()){
			// 2. Comparación objeto a objeto.
			for(int i = 0;i < id.size();i++){
				if(o == null || !this.id.get(i).equals(id.get(i))){
					result = 1;
				}
			}
		}else{
			result = -1;
		}
		return result;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", attributes=" + attributes + ", states=" + states.toString() + "]";
	}
	
}
