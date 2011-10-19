package de.unigoettingen.ct.data;

public interface GenericObserver<T> {

	public abstract void update(T observable);
}

