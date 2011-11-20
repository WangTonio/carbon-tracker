package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.io.Measurement;

public class EngineRunTimeCmd extends ObdCommand{

	//this command is officially named 'run time since engine start'
	//it is not to be confused with command 0181 as that represents the time since the last DTC reset
	
	@Override
	public String getCommandString() {
		return "011f";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		if(response.length() != 4){
			throw new IOException("Run Time Since Engine Start command expected 2 bytes, but "+response.length()+" hex digits were returned.");
		}
		measure.setErt(Integer.parseInt(response, 16)); //this is in seconds with 65535 max
	}

}
