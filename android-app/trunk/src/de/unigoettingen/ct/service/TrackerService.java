package de.unigoettingen.ct.service;

import java.util.Calendar;

import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.obd.MeasurementStatus;
import de.unigoettingen.ct.obd.MeasurementStatusListener;
import de.unigoettingen.ct.obd.MeasurementSubsystem;
import de.unigoettingen.ct.obd.MockMeasurementSubsystem;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TrackerService extends Service implements MeasurementStatusListener{

	private boolean active=false;
	private AbstractCachingStrategy cachingStrat;
	private MeasurementSubsystem measurementSystem;
	
	private static final String LOG_TAG = "TrackerService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
	}
	
	private void setUpAndMeasure() {
		if (!active) {
			Log.i(LOG_TAG, "Creating subsystems");
			TrackCache cache = new TrackCache(new OngoingTrack(Calendar.getInstance(), "SAMPLEVIN", "Some description", new Person("Heinz", "Harald")));
			this.cachingStrat = new SimpleCachingStratgey(cache);
			this.measurementSystem = new MockMeasurementSubsystem(cache, 0xDEADCAFE);
			this.measurementSystem.addStatusListener(this);
			this.active = true;
			this.measurementSystem.startMeasurement();
		}
	}
	
	private void terminate(){
		this.cachingStrat.shutDown();
		this.measurementSystem.stopMeasurement();
		this.cachingStrat = null;
		this.measurementSystem = null;
		this.active = false;
	}
	
	@Override
	public void notify(MeasurementStatus status, MeasurementSubsystem sender) {
		assert(this.measurementSystem == sender);
		Log.d(LOG_TAG, status.toString());
		switch(status.getState()){
			case SETTING_UP:
				Toast.makeText(getApplicationContext(), "Setting up", 0);
				break;
			case SET_UP: 
				Toast.makeText(getApplicationContext(), "Set up! ", 0);
				break;
			case IN_PROGRESS:
				break;
			default:
				Toast.makeText(getApplicationContext(), status.toString(), 0);
				break;		
		}
	}
	
	
	
	//binder below ---------------------------------------------------------------
	
	private final IBinder mBinder = new TrackerServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder; 
	}
	
	public class TrackerServiceBinder extends Binder{
		
		public void start(){
			setUpAndMeasure();
		}
		
		public void stop(){
			terminate();
		}
	}

}
