package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class IntakeAirTemperatureCmd extends ObdCommand {

	@Override
	public String getCommandString() {
		return "010f";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		measure.setIat(Integer.parseInt(response, 16) - 40); //this is in degrees celsius with an offset of 40
	}

	@Override
	public int getNumberOfExpectedChars() {
		return 2;
	}
	
	@Override
	public String toString() {
		return "Intake Air Temperature (IAT)";
	}

}
