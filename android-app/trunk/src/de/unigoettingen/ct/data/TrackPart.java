package de.unigoettingen.ct.data;

import java.util.Calendar;

public class TrackPart {

	private Calendar startedAt;
	private String vin;
	private boolean lastPart;
	private String description;
	private Person driver;
	private Measurement[] measurements;

	public TrackPart() {

	}

	public TrackPart(Calendar startedAt, String vin, boolean lastPart, String description, Person driver, Measurement[] measurements) {
		super();
		this.startedAt = startedAt;
		this.vin = vin;
		this.lastPart = lastPart;
		this.description = description;
		this.driver = driver;
		this.measurements = measurements;
	}


	/**
	 * it transmits the relevant data to the insertInMySQLDB-object, which inserts the data into the mysql-database
	 * 
	 * @return Message
	 */
	
	@Override
	public String toString() {
		//
		StringBuilder sb = new StringBuilder();
		sb.append("(Track: startedAt=" + DateUtils.calendarToString(startedAt) + " ,vin=" + vin + " ,lastPart=" + lastPart + " , description="
				+ description + " ,driver=" + driver.toString() + " ,measurements=[ \n");

		if (measurements != null) {

			for (Measurement m : measurements) {

				sb.append(m.toString());
				sb.append("\n");
			}
		}
		sb.append("])");

		return sb.toString();

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
