package de.unigoettingen.ct.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.unigoettingen.ct.data.io.DateUtils;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.data.io.Person;
import de.unigoettingen.ct.data.io.TrackPart;

/**
 * A 'dynamic' Track intended to grow and change as measurement is being performed. Note that both, this class and {@link TrackPart}, 
 * somehow represent a Track. However, the TrackPart class is solely used for data export and can be retrieved from objects of this class
 * by calling {@link #getTrackPart()}. 
 * @author Fabian Sudau
 *
 */
public class OngoingTrack {
	
	//internally, this class wraps a TrackPart object to save some redundancy
	
	private TrackPart wrappedTrackPart;
	private List<Measurement> measurementList;
	
	public OngoingTrack(Calendar startedAt, String vin, String description, Person driver){
		this.wrappedTrackPart = new TrackPart(startedAt, vin, false, description, driver, null);
		this.measurementList = new ArrayList<Measurement>();
	}
	
	public void addMeasurement(Measurement m){
		this.measurementList.add(m);
	}
	
	public int getMeasurementCount(){
		return this.measurementList.size();
	}
	
	public void removeFirstMeasurements(int count){
		if(count > this.measurementList.size()){
			throw new IndexOutOfBoundsException("Removing "+count+" measurements not possible, when only "+this.measurementList.size()+" present.");
		}
		//the first 'count' elements are cut off, and will be subject to garbage collection
		this.measurementList = new ArrayList<Measurement>(this.measurementList.subList(count, this.measurementList.size()));
	}
	
	public TrackPart getTrackPart(){
		Measurement[] mArray = new Measurement[this.measurementList.size()];
		mArray = this.measurementList.toArray(mArray);
		TrackPart retVal = this.wrappedTrackPart.getCloneWithoutMeasurements();
		retVal.setMeasurements(mArray);
		return retVal;
	}
	
	public TrackPart getEmptyTrackPart(){
		return this.wrappedTrackPart.getCloneWithoutMeasurements();
	}
	
	public String toDescription(){
		return DateUtils.calendarToString(this.wrappedTrackPart.getStartedAt())+" ("+this.wrappedTrackPart.getDescription()+")";
	}
	
	public boolean equalsIgnoringMeasurements(OngoingTrack other){
		if(other==null){
			return false;
		}
		//the following looks like data envy; however we want to keep modifications of the upload-related classed to a minimum
		TrackPart otherTp = other.getEmptyTrackPart();
		return this.wrappedTrackPart.getStartedAt().equals(otherTp.getStartedAt()) &&
			this.wrappedTrackPart.getDescription().equals(otherTp.getDescription()) &&
			this.wrappedTrackPart.getDriver().equals(otherTp.getDriver()) &&
			this.wrappedTrackPart.getVin().equals(otherTp.getVin()) &&
			this.wrappedTrackPart.isLastPart() == otherTp.isLastPart();
	}
	
	public void setClosed(){
		this.wrappedTrackPart.setLastPart(true);
	}
	
	public boolean isClosed(){
		return this.wrappedTrackPart.isLastPart();
	}

	public String getVin() {
		return wrappedTrackPart.getVin();
	}

	public void setVin(String vin) {
		wrappedTrackPart.setVin(vin);
	}
	
}
