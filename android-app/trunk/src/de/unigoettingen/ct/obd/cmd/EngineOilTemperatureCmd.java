package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class EngineOilTemperatureCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "015c";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		if(response.length() != 2){
			throw new UnsupportedObdCommandException("Expected only 1 byte, but "+response.length()+" hex digits were returned.");
		}
		measure.setEot(Integer.parseInt(response, 16)-40); //this is in degrees celsius
	}

}
