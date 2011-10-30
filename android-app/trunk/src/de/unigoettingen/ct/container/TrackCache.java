package de.unigoettingen.ct.container;

import java.util.ArrayList;
import java.util.List;

import de.unigoettingen.ct.data.GenericObservable;
import de.unigoettingen.ct.data.Measurement;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.TrackPart;
import de.unigoettingen.ct.data.TrackSummary;

/**
 * Represents a container for {@link OngoingTrack}s. The container is a Thread-safe inter-Thread-gateway, which
 * lets one client add data to it (PRODUCE). Another client can register for updates, which will lead to callbacks
 * whenever this object recieves new data. The thread can then retrieve & remove (CONSUME) some of the data. 
 * @author Fabian Sudau
 *
 */
public class TrackCache extends GenericObservable<List<TrackSummary>>{

	private List<OngoingTrack> tracks; //the last track will always be the active one (by convention)
									// having another field for it would introduce unnecessary complexity

	
	public TrackCache(OngoingTrack activeTrack){
		this.tracks = new ArrayList<OngoingTrack>(); //later, this will be read from sqlite
		this.tracks.add(activeTrack);
	}
	
	/**
	 * Adds one tuple of measurement data to the currently active Track.
	 * Listener(s) will be informed about this.
	 * @param m tuple to add
	 */
	public void addMeasurementToActiveTrack(Measurement m){
		List<TrackSummary> summary;
		synchronized (this) {
			this.tracks.get(this.tracks.size()-1).addMeasurement(m);
			summary = this.generateSummary();
		}
		this.fireUpdates(summary);
	}
	
	/**
	 * Retrieves all data that is present in the specified Track as a {@link TrackPart} object.
	 * @param index index of the target Track, use size-1 for the active Track
	 * @return an object 'export'
	 */
	public synchronized TrackPart getTrackPart(int index){
		return this.tracks.get(index).getTrackPart();
	}
	
	/**
	 * Does the same as {@link #getTrackPart(int)} except all possible Tracks are targeted.
	 * @return all TrackParts for all Tracks in the same order
	 */
	public synchronized List<TrackPart> getAllPossibleTrackParts(){
		List<TrackPart> retVal = new ArrayList<TrackPart>();
		for(OngoingTrack og: this.tracks){
			retVal.add(og.getTrackPart());
		}
		return retVal;
	}
	
	/**
	 * Sets the specified Track to closed meaning that no more tuples can be added.
	 * @param index index of the target Track, use size-1 for the active Track
	 */
	public synchronized void setTrackToClosed(int index){
		this.tracks.get(index).setClosed();
	}
	
	/**
	 * Removes the first x measurement tuples of the specified Track from the cache (they will be subject to
	 * garbage collection). If the Track is closed and now empty, the whole Track information will be erased.
	 * @param trackIndex index of the target Track, use size-1 for the active Track
	 * @param measurementCount the number of elements to remove
	 */
	public synchronized void markMeasurementsAsUploaded(int trackIndex, int measurementCount){
		OngoingTrack targetTrack = this.tracks.get(trackIndex);
		targetTrack.removeFirstMeasurements(measurementCount);
		//if this was the last possible upload, remove the track object from the memory
		if(targetTrack.isClosed() && targetTrack.getMeasurementCount() == 0){
			this.tracks.remove(targetTrack);
		}
	}
	
	/**
	 * Returns a summary describing the current cache state.
	 * @return a list of which each element describes the state of the Track at the same index
	 */
	public synchronized List<TrackSummary> getSummary(){
		return this.generateSummary();
	}
	
	/**
	 * Attempts to set the VIN of the currently active track. If a VIN is already present
	 * and it differs from the passed-in one, false is returned and the vin is not changed.
	 * @param vin VIN to set
	 * @return true, if set successfully
	 */
	public synchronized boolean matchVinOfActiveTrack(String vin){
		OngoingTrack activeTrack = this.tracks.get(this.tracks.size()-1);
		if(activeTrack.getVin() == null || activeTrack.getVin().equals(vin)){
			activeTrack.setVin(vin);
			return true;
		}
		else{
			return false;
		}
	}
	
	private List<TrackSummary> generateSummary(){
		List<TrackSummary> retVal = new ArrayList<TrackSummary>(tracks.size());
		for(OngoingTrack ot: this.tracks){
			retVal.add(new TrackSummary(ot.getMeasurementCount(), ot.isClosed()));
		}
		return retVal;	
	}
	
}
