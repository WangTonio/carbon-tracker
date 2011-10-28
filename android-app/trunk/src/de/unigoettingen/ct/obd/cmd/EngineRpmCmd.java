package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

public class EngineRpmCmd extends ObdCommand{
	@Override
	public String getCommandString() {
		return "010C";
	}
	@Override
	public void processResponse(String response, Measurement measure) throws IOException {
		//TODO i hope this is right but can not guarantee anything
		double floatingResult = Integer.parseInt(response, 16) / 4D;
		measure.setRpm((int) Math.round(floatingResult)); //int overflow is not possible here, as the max value is 16384
	}
}
