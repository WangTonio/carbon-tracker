package de.unigoettingen.ct.data.io;

import java.util.Calendar;


import flexjson.JSON;

/**
 * These objects play the role of 'export' objects as they will directly be marshalled and uploaded to the server.
 * DO NOT TOUCH.
 * @author Fabian Sudau
 *
 */
public class TrackPart {

	private Calendar startedAt;
	private String vin;
	private boolean lastPart;
	private String description;
	private Person driver;
	private Measurement[] measurements;

	public TrackPart() {

	}
	public TrackPart(Calendar startedAt, String vin, boolean lastPart, String description, Person driver, Measurement[] measurements ){
		super();
		this.startedAt = startedAt;
		this.vin = vin;
		this.lastPart = lastPart;
		this.description = description;
		this.driver = driver;
		this.measurements = measurements;
	}
	
	@JSON(include=false)
	public TrackPart getCloneWithoutMeasurements(){
		return new TrackPart((Calendar) startedAt.clone(), vin, lastPart, description, new Person(driver.getForename(), driver.getLastname()), null);
	}

	public Calendar getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Calendar startedAt) {
		this.startedAt = startedAt;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public boolean isLastPart() {
		return lastPart;
	}

	public void setLastPart(boolean lastPart) {
		this.lastPart = lastPart;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Person getDriver() {
		return driver;
	}

	public void setDriver(Person driver) {
		this.driver = driver;
	}

	public Measurement[] getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Measurement[] measurements) {
		this.measurements = measurements;
	}
}
