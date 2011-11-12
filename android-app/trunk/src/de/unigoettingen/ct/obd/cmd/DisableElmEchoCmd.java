package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import de.unigoettingen.ct.data.Measurement;

/**
 * Disables the echo function of the ELM adapter (not part of the obd system). This means, that all following
 * commands are not getting echoed back. Helps a lot not to get confused with returned results.
 * @author Fabian Sudau
 *
 */
public class DisableElmEchoCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return "ate0";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		if(!response.equalsIgnoreCase("OK")){
			//if "OK" is not returned, something went wrong
			throw new UnsupportedObdCommandException("Adapter did not respond to ate0 command");
		}
	}

}
