package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.io.Measurement;

public class MassAirFlowCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "0110";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		if(response.length() != 4){
			throw new IOException("Mass Air Flow Rate command expected 2 bytes, but "+response.length()+" hex digits were returned.");
		}
		//the following is right according to standard scaling
		measure.setMaf( (Integer.parseInt(response, 16) / 100D) );
	}
	
	

}
