package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class VehicleSpeedCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "010d";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		measure.setSpeed(Integer.parseInt(response, 16)); //this is in km/h. 255 is max ( poor ghost rider :( )
	}

	@Override
	public int getNumberOfExpectedChars() {
		return 2;
	}
	
	@Override
	public String toString() {
		return "Vehicle Speed";
	}

}
