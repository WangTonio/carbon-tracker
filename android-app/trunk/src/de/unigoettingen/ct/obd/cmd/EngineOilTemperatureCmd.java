package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class EngineOilTemperatureCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "015c";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		measure.setEot(Integer.parseInt(response, 16)-40); //this is in degrees celsius
	}

	@Override
	public int getNumberOfExpectedChars() {
		return 2;
	}
	
	@Override
	public String toString() {
		return "Engine Oil Temperature (EOT)";
	}

}
