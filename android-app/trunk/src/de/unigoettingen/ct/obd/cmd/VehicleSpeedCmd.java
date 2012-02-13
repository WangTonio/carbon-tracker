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
		if(response.length() != 2){
			throw new UnsupportedObdCommandException("Expected only 1 byte, but "+response.length()+" hex digits were returned.");
		}
		measure.setSpeed(Integer.parseInt(response, 16)); //this is in km/h. 255 is max ( poor ghost rider :( )
	}

}
