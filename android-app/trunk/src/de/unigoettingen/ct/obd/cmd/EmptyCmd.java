package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import android.util.Log;

import de.unigoettingen.ct.data.io.Measurement;

public class EmptyCmd extends ObdCommand{

	@Override
	public String getCommandString() {
		return null;
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		Log.d("EmptyCmd", "Initial messag from the adapter after strip is :"+response);
	}

}
