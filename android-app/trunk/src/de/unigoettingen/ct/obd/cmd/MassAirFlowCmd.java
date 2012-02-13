package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class MassAirFlowCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "0110";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		if(response.length() != 4){
			throw new UnsupportedObdCommandException("Expected 2 bytes, but "+response.length()+" hex digits were returned.");
		}
		//the following is right according to standard scaling
		measure.setMaf( (Integer.parseInt(response, 16) / 100D) );
	}
	
	

}
