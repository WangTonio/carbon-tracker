package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

public class VehicleSpeedCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "010d";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		if(response.length() > 2){
			throw new IOException("Vehicle speed command expected only 1 byte, but "+response.length()+" hex digits were returned.");
		}
		measure.setEot(Integer.parseInt(response, 16)); //this is in km/h. 255 is max ( poor ghost rider :( )
	}

}
