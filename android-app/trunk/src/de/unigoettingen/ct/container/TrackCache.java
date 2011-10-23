package de.unigoettingen.ct.container;

import java.util.ArrayList;
import java.util.List;

import de.unigoettingen.ct.data.GenericObservable;
import de.unigoettingen.ct.data.Measurement;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.TrackPart;
import de.unigoettingen.ct.data.TrackSummary;


public class TrackCache extends GenericObservable<List<TrackSummary>>{

	private List<OngoingTrack> tracks; //the last track will always be the active one (by convention)
									// having another field for it would introduce unnecessary complexity

	
	public TrackCache(OngoingTrack activeTrack){
		this.tracks = new ArrayList<OngoingTrack>(); //later, this will be read from sqlite
		this.tracks.add(activeTrack);
	}
	
	public void addMeasurementToActiveRoute(Measurement m){
		List<TrackSummary> summary;
		synchronized (this) {
			this.tracks.get(this.tracks.size()-1).addMeasurement(m);
			summary = this.generateSummary();
		}
		this.fireUpdates(summary);
	}
	
	public synchronized TrackPart getTrackPart(int index){
		return this.tracks.get(index).getTrackPart();
	}
	
	public synchronized List<TrackPart> getAllPossibleTrackParts(){
		List<TrackPart> retVal = new ArrayList<TrackPart>();
		for(OngoingTrack og: this.tracks){
			retVal.add(og.getTrackPart());
		}
		return retVal;
	}
	
	public synchronized void setTrackToClosed(int index){
		this.tracks.get(index).setClosed();
	}
	
	public synchronized void markMeasurementsAsUploaded(int trackIndex, int measurementCount){
		OngoingTrack targetTrack = this.tracks.get(trackIndex);
		targetTrack.removeFirstMeasurements(measurementCount);
		//if this was the last possible upload, remove the track object from the memory
		if(targetTrack.isClosed() && targetTrack.getMeasurementCount() == 0){
			this.tracks.remove(targetTrack);
		}
	}
	
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
