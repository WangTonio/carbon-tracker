package de.unigoettingen.ct.obd;

public class MeasurementStatus {
	public enum States{SETTING_UP, SET_UP, IN_PROGRESS, ERROR_BUT_ONGOING, STOPPED_BY_USER, FATAL_ERROR_STOPPED};
	
	private States state;
	private String additionalInfo;
	
	
	public MeasurementStatus(States state) {
		super();
		this.state = state;
	}


	public MeasurementStatus(States state, String additionalInfo) {
		super();
		this.state = state;
		this.additionalInfo = additionalInfo;
	}
	
	

}
