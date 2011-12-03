package de.unigoettingen.ct.upload;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.unigoettingen.ct.cache.AbstractCachingSystem;
import de.unigoettingen.ct.cache.PersistenceBinder;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.service.AsynchronousSubsystem;
import de.unigoettingen.ct.service.SubsystemStatus;
import de.unigoettingen.ct.service.SubsystemStatusListener;

/**
 * This asynchronous system can be used to upload all measurement data, that is currently stored persistently on the device.
 * The important difference between this class and {@link AbstractCachingSystem} is, that this class is for manual use.<br>
 * In other words: This class does not respond to a dynamically changing cache and it is strongly advised not to use this
 * while measurement is in progress. Instead, this is intended to be used in situations, where no obd system is present
 * but due to network difficulties, there is still data in the local cache that needs to be 'fired out'.
 * @author Fabian Sudau
 *
 */
public class ManualUploadSystem implements AsynchronousSubsystem{

	private PersistenceBinder persistence;
	private List<OngoingTrack> tracks;
	private volatile boolean stopRequested;
	
	private ExecutorService executor;
	private volatile SubsystemStatusListener listener;
	
	/**
	 * Constructs the uploader. No upload is going on, until setUp() and then start() is called.
	 * @param persistence persistence to load data from. Clients must ensure that there is no concurrent access.
	 */
	public ManualUploadSystem(PersistenceBinder persistence) {
		this.persistence = persistence;
		this.stopRequested = false;
		this.executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void setStatusListener(SubsystemStatusListener listener) {
		this.listener = listener;
	}

	@Override
	public void setUp() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				informListener(SubsystemStatus.States.SETTING_UP);
				//first, create a list that contains all tracks having measurements associated
				tracks = persistence.loadAllTracksEmpty();
				for(Iterator<OngoingTrack> it = tracks.iterator(); it.hasNext(); ){
					OngoingTrack currTrack = it.next();
					persistence.loadMeasurementsIntoTrack(currTrack, Integer.MAX_VALUE);
					if(currTrack.getMeasurementCount() == 0){
						it.remove();
					}
				}
				if(tracks.isEmpty()){
					informListener(SubsystemStatus.States.STOPPED_BY_USER, "There is nothing to upload.");
					executor.shutdown();
				}
				else{
					informListener(SubsystemStatus.States.SET_UP, "Uploading data of "+tracks.size()+" stored Tracks ...");
				}
			}
		});
	}

	@Override
	public void start() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				informListener(SubsystemStatus.States.IN_PROGRESS);
				for(int i=0; i<tracks.size(); i++){
					AbstractUploader uploader = new TrackPartUploader(tracks.get(i).getTrackPart());
					uploader.startUpload();
					while(!uploader.isDone()){
						try {
							Thread.sleep(2000);
						}
						catch (InterruptedException e) {
							//we do not care
						}
					}
					if(uploader.hasErrorOccurred()){
						String msg = "Upload failed.";
						if(i>0){
							msg+= " But "+i+" Tracks were already uploaded successfully.";
						}
						informListener(SubsystemStatus.States.FATAL_ERROR_STOPPED, msg);
						executor.shutdown();
						return; //exits the loop
					}
					//upload was successful: no need to store the data locally any more
					persistence.deleteTrackCompletely(tracks.get(i).getEmptyTrackPart());
					if(stopRequested){
						informListener(SubsystemStatus.States.STOPPED_BY_USER,
								"Upload stopped. Already uploaded "+i+" of "+tracks.size()+" Tracks.");
						executor.shutdown();
						return; //exits the loop
					}
					//if the upload went fine and no stop was requested, go on with the next track !
				}
			}
		});
	}

	@Override
	public void stop() {
		this.stopRequested = true;
	}
	
	@Override
	public String toString() {
		return "ManualUploadSys";
	}

	
	//helper methods below:
	
	private void informListener(SubsystemStatus.States state, String msg){
		listener.notify(new SubsystemStatus(state, msg), this);
	}
	
	private void informListener(SubsystemStatus.States state){
		listener.notify(new SubsystemStatus(state), this);
	}
	
}
