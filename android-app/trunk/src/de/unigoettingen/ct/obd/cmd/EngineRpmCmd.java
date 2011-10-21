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
		String[] values = response.split("\r");
		response = values[0].replace(" ", "");
        String byteStrOne = response.substring(4,6);
        String byteStrTwo = response.substring(6,8);
        int a = Integer.parseInt(byteStrOne,16);
        int b = Integer.parseInt(byteStrTwo,16);

		int rpm = ((a*256)+b)/4;
		
		measure.setRpm(rpm);
		
	}
}
