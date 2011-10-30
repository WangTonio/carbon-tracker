package de.unigoettingen.ct.data;

/**
 * These objects play the role of 'export' objects as they will directly be marshalled and uploaded to the server.
 * DO NOT TOUCH.
 * @author Fabian Sudau
 *
 */
public class DebugLog {
	
	private String deviceName;
	private DebugMessage[] debugMessages;
	
	public DebugLog(String deviceName, DebugMessage[] debugMessages) {
		this.deviceName = deviceName;
		this.debugMessages = debugMessages;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public DebugMessage[] getDebugMessages() {
		return debugMessages;
	}

	public void setDebugMessages(DebugMessage[] debugMessages) {
		this.debugMessages = debugMessages;
	}
	
}
