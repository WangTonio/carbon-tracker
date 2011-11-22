package de.unigoettingen.ct.service;

import static de.unigoettingen.ct.service.SubsystemStatus.States.ERROR_BUT_ONGOING;
import static de.unigoettingen.ct.service.SubsystemStatus.States.IN_PROGRESS;
import static de.unigoettingen.ct.service.SubsystemStatus.States.SET_UP;
import static de.unigoettingen.ct.service.SubsystemStatus.States.STOPPED_BY_USER;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.GenericObserver;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.TrackSummary;
import de.unigoettingen.ct.data.io.Person;
import de.unigoettingen.ct.data.io.TrackPart;
import de.unigoettingen.ct.upload.AbstractUploader;
import de.unigoettingen.ct.upload.TrackPartUploader;

public abstract class AbstractCachingStrategy implements AsynchronousSubsystem, GenericObserver<List<TrackSummary>>{

	private static final String LOG_TAG = "Caching";
	
	private ExecutorService executor;
	private PersistenceBinder persistence;
	private String currentForename;
	private String currentLastname;
	private TrackCache cache;
	private volatile AbstractUploader currentUpload;
	private volatile TrackPart currentlyUploadedTrackPart;
	private volatile int currentlyUploadedIndex;
	private SubsystemStatusListener statusListener;
	private volatile boolean running; //indicates, whether this must react to changes in cache status
	
	public AbstractCachingStrategy(TrackCache cache, PersistenceBinder persistence, String currentForename, String currentLastname){
		this.cache = cache;
		this.persistence= persistence;
		this.currentForename = currentForename;
		this.currentLastname = currentLastname;
		this.running = false;
		this.executor = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void setUp() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				List<OngoingTrack> storedTracks = persistence.loadAllTracksEmpty();
				//so far, everything gets loaded into ram as a whole
				for(OngoingTrack currTrack: storedTracks){
					Logg.log(Log.INFO, LOG_TAG, "Loaded a track stored on the local device.");
					persistence.loadMeasurementsIntoTrack(currTrack, Integer.MAX_VALUE);
					persistence.deleteTrackCompletely(currTrack.getEmptyTrackPart());
				}
				
				//this half-way mock implementation just starts a new track on every service restart
				//TODO later, the user will have the choice to resume a stored track
				OngoingTrack activeTrack = new OngoingTrack(Calendar.getInstance(), null, "Dummy description", new Person(currentForename, currentLastname));
				cache.setTracks(storedTracks, activeTrack);
				cache.addObserver(AbstractCachingStrategy.this);
				statusListener.notify(new SubsystemStatus(SET_UP), AbstractCachingStrategy.this);
			}
		});
	}
	
	@Override
	public void start() {
		this.running = true;
		this.statusListener.notify(new SubsystemStatus(IN_PROGRESS), this);
	}
	
	@Override
	public void setStatusListener(SubsystemStatusListener listener) {
		this.statusListener = listener;
	}
	
	@Override
	public void update(final List<TrackSummary> observable) {
		if(!this.running){
			//if this is not activated, do not respond to events
			return;
		}
		this.executor.execute(new Runnable() {	
			@Override
			public void run() {
				if(currentUpload == null){
					//no upload pending, let the subclass decide whether to start one
					handleCacheChange(observable);
				}
				else{
					//upload object is present, ask for it's state
					if(currentUpload.isDone()){
						if(currentUpload.hasErrorOccurred()){
							if(currentUpload.isRetryingPossible()){
								//retry the upload, do not ask the subclass yet
								Logg.log(Log.INFO, LOG_TAG, "Last upload failed. Retrying..");
								currentUpload = new TrackPartUploader(currentlyUploadedTrackPart);
								currentUpload.startUpload();
							}
							else{
								Logg.log(Log.ERROR, LOG_TAG, "Can not upload measurements due to misconfiguration. Still "+
										currentlyUploadedTrackPart.getMeasurements().length+" measurements in the pipe.");
								statusListener.notify(new SubsystemStatus(ERROR_BUT_ONGOING, "Upload not possible, data will be stored persistently upon termination."), AbstractCachingStrategy.this);
								//TODO use persistence right now (not just when terminating the app) to reduce RAM impact
							}
						}
						else{
							//upload just finished successfully. remove uploaded data from ram, then ask subclass what to do
							Logg.log(Log.INFO, LOG_TAG, "Last upload was successful !");
							handleSuccessfulUpload();
							handleCacheChange(cache.getSummary());
						}
					}
					//if the upload is still in progress, do not ask the subclass anything yet
				}
			}
		});
	}
	
	private void handleSuccessfulUpload(){
		cache.markMeasurementsAsUploaded(currentlyUploadedIndex, currentlyUploadedTrackPart.getMeasurements().length);
		currentUpload = null;
		currentlyUploadedTrackPart = null;
		currentlyUploadedIndex = -1;
	}
	
	public void stop(){
		//make sure not to respond to cache events anymore
		this.running = false;
		//TODO: the following is a mock-line:
		this.cache.setTrackToClosed(cache.getSummary().size()-1);
		//the rest involves I/O and is performed asynchronously
		executor.execute(new Runnable() {	
			@Override
			public void run() {
				//wait for a possible pending upload to finish
				//the upload will either succeed or fail, it has a timeout on it's own (AT LEAST I HOPE SO)
				if(currentlyUploadedTrackPart != null){
					Log.i(LOG_TAG, "is asked to terminate, but an Upload is still in progress. Waiting.");
					while(!currentUpload.isDone()){
						try {
							Thread.sleep(500);
						}
						catch (InterruptedException e) {
							Log.wtf(LOG_TAG, "Thread caught in interrupt that was not expected.");
						}
					}
					if(!currentUpload.hasErrorOccurred()){
						handleSuccessfulUpload();
					}
					else{
						currentUpload = null;
					}
				}
				
				//check, if there is still data in the pipe.
				//if the active track still has data, try to upload it.
				//if the upload fails, store the data. non-active tracks will be stored without trying an upload.

				List<TrackSummary> currentCacheState = cache.getSummary();
				int activeTrackIndex = currentCacheState.size()-1;
				if(currentCacheState.get(activeTrackIndex).getMeasurementCount() > 0){
					//active track has data
					Logg.log(Log.INFO, LOG_TAG, "Active Track still has data in RAM, trying to upload it.");
					invokeUpload(activeTrackIndex);
					while(!currentUpload.isDone()){
						try {
							Thread.sleep(500);
						}
						catch (InterruptedException e) {
							Log.wtf(LOG_TAG, "Thread caught in interrupt that was not expected.");
						}
					}
					if(currentUpload.hasErrorOccurred()){
						Logg.log(Log.INFO, LOG_TAG, "Active Track could not be uploaded and is stored persistently.");
						persistence.writeFullTrack(currentlyUploadedTrackPart); //this is the whole active track
					}
					//the following line does the right thing in both cases:
					//it removes the data from the ram as it was either uploaded or stored persistently
					handleSuccessfulUpload();
				}
				
				//now store the non-active tracks
				Log.i(LOG_TAG, (activeTrackIndex)+" non-active Tracks are stored persistently.");
				for(int i=0; i<activeTrackIndex; i++){
					//TODO so far, open tracks without any remaining data will not be stored
					if(currentCacheState.get(i).getMeasurementCount() > 0){
						persistence.writeFullTrack(cache.getTrackPart(i));
					}
				}
				persistence.close();
				statusListener.notify(new SubsystemStatus(STOPPED_BY_USER), AbstractCachingStrategy.this);
			}
		});
		this.executor.shutdown(); //make sure the thread is getting terminated after the jobs are through
	}
	
	protected void invokeUpload(int trackIndex){
		if(currentUpload != null){
			throw new IllegalStateException("Can not start another upload while one is still in progress.");
		}
		currentlyUploadedIndex = trackIndex;
		currentlyUploadedTrackPart = cache.getTrackPart(trackIndex);
		Logg.log(Log.INFO, LOG_TAG, "Starting an upload to the server with "+currentlyUploadedTrackPart.getMeasurements().length+" measurements.");
		currentUpload = new TrackPartUploader(currentlyUploadedTrackPart);
		currentUpload.startUpload();
	}
	
	protected abstract void handleCacheChange(List<TrackSummary> tracks);
	
	@Override
	public String toString() {
		return "CachingSys";
	}
}
