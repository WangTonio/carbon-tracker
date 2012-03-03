package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
import de.unigoettingen.ct.data.Logg;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class IntakeManifoldAbsolutePressureCmd extends ObdCommand {
	
	//STATE PATTERN:
	//on first use of this command, a special PID is used once to determine the correct scaling / interpretation to the MAP result.
	//that special 'scaling' command is just tried once. If it fails, no exception is thrown and standard scaling is assumed.
	
	private double scaling = 1D; //default
	boolean firstExecution = true;
	private ObdCommand wrappedCommand = new ScalingCommand();

	@Override
	public String getCommandString() {
		return wrappedCommand.getCommandString();
	}
	
	@Override
	public void queryResult(Measurement measure, InputStream in, OutputStream out) throws IOException, UnsupportedObdCommandException {
		if(!firstExecution){
			wrappedCommand= new RealCommand(); //state changes after first query
		}
		firstExecution = false;
		super.queryResult(measure, in, out);
	}

	@Override
	public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
		wrappedCommand.processResponse(response, measure); 
	}

	@Override
	public int getNumberOfExpectedChars() {
		return wrappedCommand.getNumberOfExpectedChars();
	}
	
	@Override
	public String toString() {
		return wrappedCommand.toString();
	}
	
	private class RealCommand extends ObdCommand{

		@Override
		public String getCommandString() {
			return "010b";
		}

		@Override
		public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
			measure.setMap((int)Math.round(Integer.parseInt(response, 16) * scaling)); //in kPa 
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
	
	private class ScalingCommand extends ObdCommand{

		@Override
		public String getCommandString() {
			return "014f";
		}

		@Override
		public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
			int raw = Integer.parseInt(response.substring(6, 8), 16);
			if (raw == 0) {
				Logg.log(Log.INFO, "MAP Scaling", "MAP uses default scaling.");
			}
			else {
				scaling = raw / 25.5;
			}
		}

		@Override
		public int getNumberOfExpectedChars() {
			return 8;
		}
		
		@Override
		public String toString() {
			return "Maximum Value of Intake Manifold Absolute Pressure (MAP)";
		}
		
	}

}
