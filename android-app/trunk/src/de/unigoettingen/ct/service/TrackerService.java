package de.unigoettingen.ct.service;

import de.unigoettingen.ct.io.CachingStrategy;
import de.unigoettingen.ct.io.PersistenceBridge;
import de.unigoettingen.ct.io.UplinkFacade;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TrackerService extends Service{

	private final IBinder mBinder = new TrackerServiceBinder();
	private UplinkFacade uplink;
	private CachingStrategy cachingStrat;
	private PersistenceBridge persistence;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class TrackerServiceBinder extends Binder{
		
	}

}
