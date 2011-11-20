package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.io.Measurement;

public class EngineOilTemperatureCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "015c";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		if(response.length() != 2){
			throw new IOException("EOT command expected only 1 byte, but "+response.length()+" hex digits were returned.");
		}
		measure.setEot(Integer.parseInt(response, 16)-40); //this is in degrees celsius
	}

}
