package de.unigoettingen.ct.data;

import java.util.Calendar;

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
