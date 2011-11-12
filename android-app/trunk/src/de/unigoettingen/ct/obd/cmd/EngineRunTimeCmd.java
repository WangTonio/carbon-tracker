package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

public class EngineRunTimeCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "0181";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		if(response.length() != 4){
			throw new IOException("Engine Run Time command expected 2 bytes, but "+response.length()+" hex digits were returned.");
		}
		measure.setErt(Integer.parseInt(response, 16)); //this is in minutes since last DTC reset
	}

}
