package de.unigoettingen.ct.service;


public class SubsystemStatus {
	public enum States{SETTING_UP, SET_UP, IN_PROGRESS, ERROR_BUT_ONGOING, STOPPED_BY_USER, FATAL_ERROR_STOPPED};
	
	private States state;
	private String additionalInfo;
	
	
	public SubsystemStatus(States state) {
		super();
		this.state = state;
	}


	public SubsystemStatus(States state, String additionalInfo) {
		super();
		this.state = state;
		this.additionalInfo = additionalInfo;
	}


	public States getState() {
		return state;
	}


	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	@Override
	public String toString() {
		if(additionalInfo  == null){
			return state.toString();
		}
		else{
			return state+" : "+additionalInfo;
		}
	}

}
