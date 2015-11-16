package com.bayesforecast.ingdat.vigsteps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class State implements Comparable{

	public static void main(String[] args){
		State s1, s2;
		Object[] o1 = {"11","1"};
		Object[] o2 = {"1","1"};
		
		s1 = new State(Arrays.asList(o1));
		s2 = new State(Arrays.asList(o2));
		
		System.out.println(s1.compareTo(s2));
	}
	
	private List<Object> status;
	private State previous;
	private State next;

	public State(List<Object> status) {
		super();
		this.status = status;
	}

	public State() {
		this.status = new ArrayList<Object> ();// TODO Auto-generated constructor stub
	}

	public List<Object> getStatus() {
		return status;
	}

	public void setStatus(List<Object> status) {
		this.status = status;
	}
	
	public void addStatus(Object o){
		this.status.add(o);
	}

	public State getPrevious() {
		return previous;
	}

	public void setPrevious(State previous) {
		this.previous = previous;
	}

	public State getNext() {
		return next;
	}

	public void setNext(State next) {
		this.next = next;
	}

	public int compareTo(Object o) {
		// Comparador de todos los objetos de la lista
		List<Object> status = ((State) o).getStatus();
		int result = 0;
		if(status.size() == this.status.size()){
			for(int i = 0;i < status.size();i++){
				if(!this.status.get(i).equals(status.get(i))){
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
		return "State ["+this.status.toString()+"]";
	}

	public boolean isNullState() {
		for(Object o : this.status){
			if(o != null){
				return false;
			}
		}
		return true;
	}
	
	
}
