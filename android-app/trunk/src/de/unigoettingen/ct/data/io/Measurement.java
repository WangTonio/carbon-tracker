package de.unigoettingen.ct.data.io;

import java.util.Calendar;

/**
 * Represents a set of measurement values of different types taken at a certain point of time.
 * All these values are optionally present. <br>
 * If the client is interested in the value of type X, use hasX() before accessing the value with the getX() method.
 * If hasX() returns false, the value returned by the getter method has no meaning at all.<br>
 * These objects play the role of 'export' objects as they will directly be marshalled / serialized and uploaded to the server.
 * DO NOT TOUCH.
 * @author Fabian Sudau
 *
 */
public class Measurement {

	//obligatory field (serves as a primary key in many occasions)
	private Calendar pointOfTime;
	
	//gps fields
	private double longitude;
	private double latitude;
	private double altitude;
	
	//fields required for standard MAF calculation
	private int rpm;
	private int iat;
	private int map;
	
	//good-to-know fields
	private int speed;
	private int eot;
	private int ert;

	//rarely, maf can be obtained directly
	private double maf;
	
	//oxygen sensor voltage essential for co2 calculations
	private double lambda;
	
	//bit mask indicating which values were set
	private int availableValues;

	public Measurement() {
		this.availableValues=0;
		//the other field values do not need a default value.
		//there values are of no meaning, if the bit mask says 'unavailable'.
	}
	
	//bit mask methods --------------------------------------------------
	
	private boolean hasValue(int position){
		return ((1 << position) & availableValues) != 0;
	}
	
	private void setValue(int position){
		availableValues = availableValues | (1 << position);
	}
	
	//check-availability methods ----------------------------------------
	
	public boolean hasLongitude(){
		return hasValue(0);
	}
	
	public boolean hasLatitude(){
		return hasValue(1);
	}

	public boolean hasAltitude(){
		return hasValue(2);
	}
	
	public boolean hasRpm(){
		return hasValue(3);
	}
	
	public boolean hasIat(){
		return hasValue(4);
	}
	
	public boolean hasMap(){
		return hasValue(5);
	}
	
	public boolean hasSpeed(){
		return hasValue(6);
	}
	
	public boolean hasEot(){
		return hasValue(7);
	}
	
	public boolean hasErt(){
		return hasValue(8);
	}
	
	public boolean hasMaf(){
		return hasValue(9);
	}
	
	public boolean hasLambda(){
		return hasValue(10);
	}
	
	//setters --------------------------------------------------------

	public void setPointOfTime(Calendar pointOfTime) {
		this.pointOfTime = pointOfTime;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
		setValue(0);
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
		setValue(1);
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
		setValue(2);
	}

	public void setRpm(int rpm) {
		this.rpm = rpm;
		setValue(3);
	}

	public void setIat(int iat) {
		this.iat = iat;
		setValue(4);
	}

	public void setMap(int map) {
		this.map = map;
		setValue(5);
	}

	public void setSpeed(int speed) {
		this.speed = speed;
		setValue(6);
	}

	public void setEot(int eot) {
		this.eot = eot;
		setValue(7);
	}

	public void setErt(int ert) {
		this.ert = ert;
		setValue(8);
	}

	public void setMaf(double maf) {
		this.maf = maf;
		setValue(9);
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
		setValue(10);
	}
	
	//getters --------------------------------------------------------

	public Calendar getPointOfTime() {
		return pointOfTime;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public int getRpm() {
		return rpm;
	}

	public int getIat() {
		return iat;
	}

	public int getMap() {
		return map;
	}

	public int getSpeed() {
		return speed;
	}

	public int getEot() {
		return eot;
	}

	public int getErt() {
		return ert;
	}

	public double getMaf() {
		return maf;
	}

	public double getLambda() {
		return lambda;
	}

	@Override
	public String toString() {
		return "Measurement["+
		"pointOfTime=" + DateUtils.calendarToString(pointOfTime) + 
		(hasLongitude() ? ", longitude=" + longitude : "")+ 
		(hasLatitude() ? ", latitude=" + latitude : "")+ 
		(hasAltitude() ? ", altitude=" + altitude : "")+ 
		(hasRpm() ? ", rpm=" + rpm :"")+ 
		(hasIat() ? ", iat=" + iat :"")+ 
		(hasMap() ? ", map=" + map :"")+ 
		(hasSpeed() ? ", speed=" + speed :"")+ 
		(hasEot() ? ", eot=" + eot :"")+ 
		(hasErt() ? ", ert=" + ert :"")+ 
		(hasMaf() ? ", maf=" + maf :"")+ 
		(hasLambda() ? ", lambda=" + lambda :"")+ 
		"]";
	}
	
	

}
