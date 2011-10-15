package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

public class MassAirFlowCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "0110";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		//TODO i hope this is right but can not guarantee anything
		measure.setMaf( (Integer.parseInt(response, 16) / 100D) );
	}
	
	

}
