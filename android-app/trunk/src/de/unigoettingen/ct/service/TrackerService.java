package de.unigoettingen.ct.service;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.obd.MockMeasurementSubsystem;
import de.unigoettingen.ct.ui.CallbackUI;

public class TrackerService extends Service implements SubsystemStatusListener{

	private boolean active=false;
	private CallbackUI ui;
	private Handler mainThread;
	private AsynchronousSubsystem cachingStrat;
	private AsynchronousSubsystem measurementSystem;
	
	private static final String LOG_TAG = "TrackerService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mainThread = new Handler();
	}
	
	private void setUpAndMeasure() {
		if (!active) {
			Log.d(LOG_TAG, "Creating subsystems");
			this.ui.indicateRunning(true);
			TrackCache cache = new TrackCache(new OngoingTrack(Calendar.getInstance(), "SAMPLEVIN", "Some description", new Person("Heinz", "Harald")));
			this.cachingStrat = new SimpleCachingStratgey(cache);
			this.cachingStrat.setStatusListener(this);
			this.measurementSystem = new MockMeasurementSubsystem(cache, 0xDEADCAFE);
			this.measurementSystem.setStatusListener(this);
			this.active = true;
			this.cachingStrat.setUp();
			this.measurementSystem.setUp();
		}
	}
	
	private void terminate(){
		this.ui.indicateRunning(false);
		this.ui.indicateLoading(true);
		this.cachingStrat.stop(); 
		this.measurementSystem.stop();
		this.cachingStrat = null;
		this.measurementSystem = null;
		this.active = false;
	}
	
	@Override
	public void notify(final SubsystemStatus status, final AsynchronousSubsystem sender) {
		//this method gets called from another thread
		//it is necessary to handle the status update in thread main, just as everything else in this class
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				assert(measurementSystem == sender);
				Log.d(LOG_TAG, status.toString()+ " from "+sender);
				switch(status.getState()){
					case SETTING_UP:
						ui.indicateLoading(true);
						break;
					case SET_UP: 
						ui.indicateLoading(true);
						sender.start();
						break;
					case IN_PROGRESS:
						if(sender == measurementSystem){
							ui.diplayText("Data is beeing retrieved.");
							ui.indicateLoading(false);
							ui.indicateRunning(true);
						}
						else{
							ui.diplayText("Caching mechanism is active.");
						}
						break;
					case STOPPED_BY_USER:
						//TODO do this only if both subsystems have stopped
						ui.indicateLoading(false);
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
			Log.d(LOG_TAG, "Termination is requested.");
			terminate();
		}
		
		public void setUIforCallbacks(CallbackUI ui){
			TrackerService.this.ui = ui;
		}
	}

}
