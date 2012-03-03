package de.unigoettingen.ct.cache;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import de.unigoettingen.ct.data.GenericObservable;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.TrackSummary;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.data.io.TrackPart;

/**
 * Represents a container for {@link OngoingTrack}s. The container is a Thread-safe inter-Thread-gateway, which
 * lets one client add data to it (PRODUCE). Another client can register for updates, which will lead to callbacks
 * whenever this object receives new data. The thread can then retrieve & remove (CONSUME) some of the data. 
 * @author Fabian Sudau
 *
 */
public class TrackCache extends GenericObservable<List<TrackSummary>>{

	private List<OngoingTrack> tracks; //the last track will always be the active one (by convention)
									// having another field for it would introduce unnecessary complexity

	public TrackCache(){
		this.tracks = new ArrayList<OngoingTrack>(0);
	}
	
	/**
	 * Sets some optional non-active track and a single mandatory active track as the content of this containter.
	 * Does not trigger an observer update.
	 * @param storedTracks non-active tracks, can be empty or null
	 * @param activeTrack not null
	 */
	public synchronized void setTracks(List<OngoingTrack> storedTracks, OngoingTrack activeTrack){
		if(storedTracks == null){
			storedTracks = new ArrayList<OngoingTrack>(1);
		}
		this.tracks = storedTracks;
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
			OngoingTrack activeTrack = this.tracks.get(this.tracks.size()-1);
			if(activeTrack.isClosed()){
				Log.i("TrackCache", "A Measurement will be discarded, as the active track is already closed.");
				return;
			}
			activeTrack.addMeasurement(m);
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
	 * Sets the currently active Track to closed meaning that no more tuples can be added.
	 */
	public synchronized void setActiveTrackToClosed(){
		this.tracks.get(tracks.size()-1).setClosed();
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
	
	/**
	 * Returns a user-friendly string representation of the track at the specified index.
	 * @param trackId index of the target Track, use size-1 for the active Track
	 * @return 
	 */
	public synchronized String getDetailedDescription(int trackId){
		return this.tracks.get(trackId).toDescription();
	}
	
	/**
	 * The active track and all it's associated measurements will be lost completely after this call.
	 */
	public synchronized void destroyActiveTrack(){
		this.tracks.remove(this.tracks.size()-1);
	}
	
	private List<TrackSummary> generateSummary(){
		List<TrackSummary> retVal = new ArrayList<TrackSummary>(tracks.size());
		for(OngoingTrack ot: this.tracks){
			retVal.add(new TrackSummary(ot.getMeasurementCount(), ot.isClosed()));
		}
		return retVal;	
	}
	
}
