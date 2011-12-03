package de.unigoettingen.ct.data.io;

import java.util.Calendar;


/**
 * These objects play the role of 'export' objects as they will directly be marshalled and uploaded to the server.
 * DO NOT TOUCH.
 * @author Fabian Sudau
 *
 */
public class DebugMessage {
	
	private Calendar pointOfTime;
	private String message;
	
	public DebugMessage(Calendar pointOfTime, String message) {
		this.pointOfTime = pointOfTime;
		this.message = message;
	}

	public Calendar getPointOfTime() {
		return pointOfTime;
	}

	public void setPointOfTime(Calendar pointOfTime) {
		this.pointOfTime = pointOfTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return DateUtils.calendarToString(pointOfTime)+": "+message;
	}
	
}
