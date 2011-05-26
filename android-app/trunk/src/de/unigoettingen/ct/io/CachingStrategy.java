package de.unigoettingen.ct.io;

import java.util.Observable;
import java.util.Observer;

public class CachingStrategy implements Observer{ //TODO exchange the observer lib for a custom, better one

	private UplinkFacade uplink;
	private PersistenceBridge persistence;
	
	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
