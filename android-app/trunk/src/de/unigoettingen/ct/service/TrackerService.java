package de.unigoettingen.ct.service;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import de.unigoettingen.ct.container.Logg;
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
	
	private SubsystemStatus.States cachingState;
	private SubsystemStatus.States measurementState;
	
	private static final String LOG_TAG = "TrackerService";
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mainThread = new Handler();
	}
	
	private void setUpAndMeasure() {
		if (!active) {
			Log.d(LOG_TAG, "Creating subsystems");
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
		this.cachingStrat.stop(); 
		this.measurementSystem.stop();
		this.active = false;
	}
	
	@Override
	public void notify(final SubsystemStatus status, final AsynchronousSubsystem sender) {
		//this method gets called from another thread
		//it is necessary to handle the status update in thread main, just as everything else runs in thread main in this class
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				Log.d(LOG_TAG, status.toString()+ " from "+sender);
				Log.d(LOG_TAG, "Caching: "+cachingState+"  Measurement: "+measurementState);
				
				//1. remember the subsystem states for future decision making
				if(sender == cachingStrat){
					cachingState = status.getState();
				}
				else if (sender == measurementSystem){
					measurementState = status.getState();
				}
				
				//2. decide on the interaction with the ui
				ui.indicateLoading(oneStateIs(SubsystemStatus.States.SETTING_UP) || oneStateIs(SubsystemStatus.States.SET_UP));
				ui.indicateRunning(bothStatesAre(SubsystemStatus.States.IN_PROGRESS));

				switch(status.getState()){
					case SETTING_UP:
						break;
					case SET_UP: 
						break;
					case IN_PROGRESS:
						if(sender == measurementSystem){
							ui.diplayText("Data is beeing retrieved.");
						}
						else{
							ui.diplayText("Caching mechanism is active.");
						}
						break;
					case STOPPED_BY_USER:
						break;
					case ERROR_BUT_ONGOING: //both type of error cause the same message so far
					case FATAL_ERROR_STOPPED:
						Logg.log(Log.ERROR, sender.toString(), sender.toString()+" says: "+status.getAdditionalInfo());
						ui.diplayText(sender.toString()+" says: "+status.getAdditionalInfo());
						break;
					default:
						ui.diplayText(status.toString());
						break;		
				}
				
				//3. decide on interaction with the subsystems
				switch(status.getState()){
					case SETTING_UP:
						//just wait
						break;
					case SET_UP: 
						//ready? then go
						sender.start();
						break;
					case IN_PROGRESS:
						//fine
						break;
					case STOPPED_BY_USER:
						//TODO think about clean up later
						if(bothStatesAre(SubsystemStatus.States.STOPPED_BY_USER)){
							cachingState=null;
							cachingStrat=null;
							measurementSystem=null;
							measurementState=null;
						}
						break;
					case ERROR_BUT_ONGOING:
						break;
					case FATAL_ERROR_STOPPED:
						if(sender == measurementSystem){
							cachingStrat.stop();
						}
						else if(sender == cachingStrat){
							measurementSystem.stop();
						}
						break;
					default:
						break;		
				}
			}
		});
	}
	
	private boolean oneStateIs(SubsystemStatus.States state){
		return cachingState == state || measurementState == state;
	}
	
	private boolean bothStatesAre(SubsystemStatus.States state){
		return cachingState == state && measurementState == state;
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
