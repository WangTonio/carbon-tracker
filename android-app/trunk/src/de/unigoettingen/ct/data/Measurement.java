package de.unigoettingen.ct.data;

import java.util.Calendar;

public class Measurement {

	private Calendar pointOfTime;
	private double longitude;
	private double latitude;
	private double altitude;
	private int rpm;
	private double maf;
	private int speed;
	private int eot;
	private long ert;
	private double lambda;

	public Measurement(Calendar pointOfTime, double longitude, double latitude, double altitude, int rpm, double maf, int speed, int eot, long ert,
			double lambda) {
		super();
		this.pointOfTime = pointOfTime;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.rpm = rpm;
		this.maf = maf;
		this.speed = speed;
		this.eot = eot;
		this.ert = ert;
		this.lambda = lambda;
	}

	public Measurement() {
		
	}
	
	public Calendar getPointOfTime() {
		return pointOfTime;
	}

	public void setPointOfTime(Calendar pointOfTime) {
		this.pointOfTime = pointOfTime;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public int getRpm() {
		return rpm;
	}

	public void setRpm(int rpm) {
		this.rpm = rpm;
	}

	public double getMaf() {
		return maf;
	}

	public void setMaf(double maf) {
		this.maf = maf;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getEot() {
		return eot;
	}

	public void setEot(int eot) {
		this.eot = eot;
	}

	public long getErt() {
		return ert;
	}

	public void setErt(long ert) {
		this.ert = ert;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	@Override
	public String toString() {
		return "(Measurement: pointOfTime=" + DateUtils.calendarToString(pointOfTime) + " ,longitude=" + longitude + " , latitude=" + latitude + " ,altitude=" + altitude + " "
				+ ",rpm=" + rpm + " ,maf=" + maf + " speed=" + speed + " ,eot=" + eot + " ,ert=" + ert + " ,lamda=" + lambda + ")";
	}

}
