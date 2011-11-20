package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.io.Measurement;

public class LambdaCmd extends ObdCommand{

	//the Bank 1 – Sensor 1 (wide range O2S) is used here by random choice

	@Override
	public String getCommandString() {
		return "0124";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		if(response.length() != 8){
			throw new IOException("EOT command expected 4 bytes, but "+response.length()+" hex digits were returned.");
		}
		//the first 2 bytes are the equivalence ratio, the following 2 are the oxygen sensor voltage
		double lambda = Integer.parseInt(response.substring(0, 4), 16);
		lambda = lambda * (2D/65535D);
		measure.setLambda(lambda);
	}

}
