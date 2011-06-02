package de.unigoettingen.ct.data;

import java.util.Calendar;

public class Tuple {
	private Calendar timeStamp;
	private double mafAirflowRate;
	private double longitude;
	private double latitude;
	//optional fields must also be included, maybe by a ressource bundle
	
	public Tuple(){
		this.timeStamp = null;
		this.mafAirflowRate = Double.NaN;
		this.longitude = Double.NaN;
		this.latitude = Double.NaN;
	}

	public Calendar getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Calendar timeStamp) {
		if(this.timeStamp != null)
			throw new UnmodifiableException();
		this.timeStamp = timeStamp;
	}

	public double getMafAirflowRate() {
		return mafAirflowRate;
	}

	public void setMafAirflowRate(double mafAirflowRate) {
		if(this.mafAirflowRate != Double.NaN)
			throw new UnmodifiableException();
		this.mafAirflowRate = mafAirflowRate;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		if(this.longitude != Double.NaN)
			throw new UnmodifiableException();
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		if(this.latitude != Double.NaN)
			throw new UnmodifiableException();
		this.latitude = latitude;
	}
	
	
	
}
