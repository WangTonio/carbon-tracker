package de.unigoettingen.ct.service;

import java.util.Calendar;

import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.obd.MockMeasurementSubsystem;
import de.unigoettingen.ct.ui.CallbackUI;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TrackerService extends Service implements SubsystemStatusListener{

	private boolean active=false;
	private CallbackUI ui;
	private Handler mainThread;
	private AbstractCachingStrategy cachingStrat;
	private AsynchronousSubsystem measurementSystem;
	
	private static final String LOG_TAG = "TrackerService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mainThread = new Handler();
	}
	
	private void setUpAndMeasure() {
		if (!active) {
			Log.i(LOG_TAG, "Creating subsystems");
			this.ui.indicateRunning(true);
			TrackCache cache = new TrackCache(new OngoingTrack(Calendar.getInstance(), "SAMPLEVIN", "Some description", new Person("Heinz", "Harald")));
			this.cachingStrat = new SimpleCachingStratgey(cache);
			this.measurementSystem = new MockMeasurementSubsystem(cache, 0xDEADCAFE);
			this.measurementSystem.addStatusListener(this);
			this.active = true;
			this.measurementSystem.start();
		}
	}
	
	private void terminate(){
		this.cachingStrat.stop(); //would be nice if this would be asynchronous
		this.measurementSystem.stop();
		this.cachingStrat = null;
		this.measurementSystem = null;
		this.active = false;
		this.ui.indicateRunning(false);
	}
	
	@Override
	public void notify(final SubsystemStatus status, final AsynchronousSubsystem sender) {
		//this method gets called from another thread
		//it is necessary to handle the status update in thread main, just as everything else in this class
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				assert(measurementSystem == sender);
				Log.d(LOG_TAG, status.toString());
				switch(status.getState()){
					case SETTING_UP:
						ui.indicateLoading(true);
						break;
					case SET_UP: 
						ui.indicateLoading(false);
						ui.indicateRunning(true);
						break;
					case IN_PROGRESS:
						ui.diplayText("Data is beeing retrieved!");
						break;
					default:
						ui.diplayText(status.toString());
						break;		
				}
			}
		});
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
		
		public void setUIforCallbacks(CallbackUI ui){
			TrackerService.this.ui = ui;
		}
	}

}
