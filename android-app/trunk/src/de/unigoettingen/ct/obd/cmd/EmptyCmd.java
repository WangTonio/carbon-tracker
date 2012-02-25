package de.unigoettingen.ct.obd.cmd;

import android.util.Log;
import de.unigoettingen.ct.data.io.Measurement;

/**
 * This command sends nothing to the adapter and does nothing with the response (except logging it). However, running this command 
 * will RETRIEVE (consume) the adapter output until the next command prompt is received. <br>
 * This command is useful right after connection establishment, as the adapter will say it's name immediately and thus
 * congest the pipe.
 * @author Fabian Sudau
 *
 */
public class EmptyCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return null;
	}

	@Override
	public void processResponse(String response, Measurement measure){
		Log.d("EmptyCmd", "Initial messag from the adapter after strip is :"+response);
	}

	@Override
	public int getNumberOfExpectedChars() {
		return -1;
	}
	
	@Override
	public String toString() {
		return "Empty";
	}

}
