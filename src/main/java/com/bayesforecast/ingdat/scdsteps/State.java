package com.bayesforecast.ingdat.scdsteps;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class State.
 */
public class State implements Comparable<State>{
	
	/** The status. */
	private List<Object> status;
	
	/** The previous. */
	private State previous;
	
	/** The next. */
	private State next;

	/**
	 * Instantiates a new state.
	 *
	 * @param status the status
	 */
	public State(List<Object> status) {
		super();
		this.status = status;
	}

	/**
	 * Instantiates a new state.
	 */
	public State() {
		this.status = new ArrayList<Object> ();// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public List<Object> getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(List<Object> status) {
		this.status = status;
	}
	
	/**
	 * Adds the status.
	 *
	 * @param o the o
	 */
	public void addStatus(Object o){
		this.status.add(o);
	}

	/**
	 * Gets the previous.
	 *
	 * @return the previous
	 */
	public State getPrevious() {
		return previous;
	}

	/**
	 * Sets the previous.
	 *
	 * @param previous the new previous
	 */
	public void setPrevious(State previous) {
		this.previous = previous;
	}

	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public State getNext() {
		return next;
	}

	/**
	 * Sets the next.
	 *
	 * @param next the new next
	 */
	public void setNext(State next) {
		this.next = next;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(State o) {
		// Comparador de todos los objetos de la lista
		List<Object> status = o.getStatus();
		int result = 0;
		// 1. Comprueba si son ambos nulos (iguales)
		if(!(this.isNullState() && o.isNullState())){
			// 2. Comprueba si solo uno de ellos es nulo (distintos)
			if(!(this.isNullState() || o.isNullState())){
				// 3. Comprueba el tama�o de ambos estados
				if(status.size() == this.status.size()){
					// 4. Para cada elemento del estado, se comprueba si son iguales
					for(int i = 0;i < status.size();i++){
						if(status.get(i) == null || !this.status.get(i).equals(status.get(i))){
							result = 1;
						}
					}
				}else{
					result = -1;
				}
			}else{
				result = -1;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "State ["+this.status.toString()+"]";
	}

	/**
	 * Checks if is null state.
	 *
	 * @return true, if is null state
	 */
	public boolean isNullState() {
		// Si todos los status son nulos,el estado es nulo.
		for(Object o : this.status){
			if(o != null){
				return false;
			}
		}
		return true;
	}
	
	
}
