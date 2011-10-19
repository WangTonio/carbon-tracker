package de.unigoettingen.ct.data;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericObservable <T> {
	
	private List<GenericObserver<T>> observers;
	
	public GenericObservable(){
		this.observers = new ArrayList<GenericObserver<T>>();
	}
	
	public void addObserver(GenericObserver<T> obs){
		if(!this.observers.contains(obs)){
			this.observers.add(obs);
		}
	}
	
	public void removeObserver(GenericObserver<T> obs){
		this.observers.remove(obs);
	}
	

	public void fireUpdates(T observable){
		for(GenericObserver<T> currObs: this.observers){
			currObs.update(observable);
		}
	}

}
