package de.unigoettingen.ct.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.GenericObserver;
import de.unigoettingen.ct.data.TrackPart;
import de.unigoettingen.ct.data.TrackSummary;
import de.unigoettingen.ct.upload.AbstractUploader;
import de.unigoettingen.ct.upload.TrackPartUploader;

public abstract class AbstractCachingStrategy implements AsynchronousSubsystem, GenericObserver<List<TrackSummary>>{

	private static final String LOG_TAG = "Caching";
	
	private ExecutorService executor;
	private TrackCache cache;
	private AbstractUploader currentUpload;
	private TrackPart currentlyUploadedTrackPart;
	private int currentlyUploadedIndex;
	private SubsystemStatusListener statusListener;
	
	public AbstractCachingStrategy(TrackCache cache){
		this.cache = cache;
		this.cache.addObserver(this);
		this.executor = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addStatusListener(SubsystemStatusListener listener) {
		this.statusListener = listener;
	}
	
	@Override
	public void update(final List<TrackSummary> observable) {
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
								currentUpload = new TrackPartUploader(currentlyUploadedTrackPart);
								currentUpload.startUpload();
							}
							else{
								Logg.log(Log.ERROR, LOG_TAG, "Can not upload measurements due to misconfiguration. Still "+
										currentlyUploadedTrackPart.getMeasurements().length+" measurements in the pipe.");
								Logg.log(Log.ERROR, LOG_TAG, "Persistence is not yet implemented and data will be lost.");
								//TODO time to use the persistence features !
							}
						}
						else{
							//upload just finished successfully. remove uploaded data from ram, then ask subclass what to do
							handleSuccessfulUpload();
							handleCacheChange(observable);
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
		//submit a job waiting for a possible pending upload to finish
		//give that job a few seconds using executor.awaitTermination(...)
		executor.execute(new Runnable() {	
			@Override
			public void run() {
				if(currentlyUploadedTrackPart != null){
					Log.i(LOG_TAG, "is asked to terminate, but an Upload is still in progress. Waiting.");
					while(!currentUpload.isDone()){
						try {
							Thread.sleep(500);
						}
						catch (InterruptedException e) {
							//awaitTermination() kicked in => out of time
							Logg.log(Log.WARN, LOG_TAG, "Upload has not finished within limits. As no persistence is implemented yet, a loss "+
									"of data is possible.");
							return;
						}
					}
					handleSuccessfulUpload();
				}
			}
		});
		//let the client thread wait a bit, then force stop
		try {
			executor.awaitTermination(4, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			Log.e(null, "Unexpected interrupt.");
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		List<TrackSummary> currentCacheState = cache.generateSummary();
		if(currentCacheState.size() > 0){
			Logg.log(Log.WARN, LOG_TAG, "Application terminates allthough there are still measurements in RAM, which are not yet uploaded.");
		}
	}
	
	protected void invokeUpload(int trackIndex){
		if(currentUpload != null){
			throw new IllegalStateException("Can not start another upload while one is still in progress.");
		}
		Logg.log(Log.INFO, LOG_TAG, "Starting an upload to the server.");
		currentlyUploadedIndex = trackIndex;
		currentlyUploadedTrackPart = cache.getTrackPart(trackIndex);
		currentUpload = new TrackPartUploader(currentlyUploadedTrackPart);
		currentUpload.startUpload();
	}
	
	protected abstract void handleCacheChange(List<TrackSummary> tracks);
	
}
