package de.unigoettingen.ct.obd.cmd;

import android.util.Log;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

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
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		Log.i("DisableElmEchoCmd", "response received");
		if(!response.equalsIgnoreCase("OK")){
			//if "OK" is not returned, something went wrong
			throw new UnsupportedObdCommandException("Adapter did not respond to ate0 command");
		}
	}

	@Override
	public int getNumberOfExpectedChars() {
		return -1;
	}
	
	@Override
	public String toString() {
		return "Disable Elm Echo [ate0]";
	}

}
