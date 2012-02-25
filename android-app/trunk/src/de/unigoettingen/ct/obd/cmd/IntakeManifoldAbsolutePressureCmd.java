package de.unigoettingen.ct.obd.cmd;

import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class IntakeManifoldAbsolutePressureCmd extends ObdCommand {

	@Override
	public String getCommandString() {
		return "010b";
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		// TODO adjust for possibly different scaling
		measure.setMap(Integer.parseInt(response, 16)); //in kPa according to standard scaling	
	}

	@Override
	public int getNumberOfExpectedChars() {
		return 2;
	}
	
	@Override
	public String toString() {
		return "Intake Manifold Absolute Pressure (MAP)";
	}

}
