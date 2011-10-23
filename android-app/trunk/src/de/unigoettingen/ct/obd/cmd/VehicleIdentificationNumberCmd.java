package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

public class VehicleIdentificationNumberCmd extends ObdCommand {
	
	private String vin;

	@Override
	public String getCommandString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		// TODO Auto-generated method stub

	}
	
	/**
	 * This method is an exception to the normal ObdCommand interface.
	 * It is needed ins this case, as this command must return a value and can not modify the measurement object.
	 * Call this method after a call to {@link #processResponse(String, Measurement)}.
	 * @return the received vin
	 */
	public String getVin(){
		return this.vin;
	}

}
