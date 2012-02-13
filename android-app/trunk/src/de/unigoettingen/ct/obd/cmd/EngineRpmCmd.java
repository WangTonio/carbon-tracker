package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class EngineRpmCmd extends ObdCommand{
	@Override
	public String getCommandString() {
		return "010C";
	}
	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		if(response.length() != 4){
			throw new UnsupportedObdCommandException("Expected 2 bytes, but "+response.length()+" hex digits were returned.");
		}
		double floatingResult = Integer.parseInt(response, 16) / 4D;
		measure.setRpm((int) Math.round(floatingResult)); //int overflow is not possible here, as the max value is 16384
	}
}
