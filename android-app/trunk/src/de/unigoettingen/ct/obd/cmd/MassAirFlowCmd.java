package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
import de.unigoettingen.ct.data.Logg;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class MassAirFlowCmd extends ObdCommand{
	
	//STATE PATTERN:
	//on first use of this command, a special PID is used once to determine the correct scaling / interpretation to the MAP result.
	//that special 'scaling' command is just tried once. If it fails, no exception is thrown and standard scaling is assumed.
	
	private double scaling = 0.01; //default
	private ObdCommand wrappedCommand = new ScalingCommand();
	boolean firstExecution = true;

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
			return "0110";
		}

		@Override
		public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
			measure.setMaf(Integer.parseInt(response, 16) * scaling); //in g/s
		}

		@Override
		public int getNumberOfExpectedChars() {
			return 4;
		}
		
		@Override
		public String toString() {
			return "Mass Air Flow (MAF)";
		}
	}
	
	private class ScalingCommand extends ObdCommand{

		@Override
		public String getCommandString() {
			return "0150";
		}

		@Override
		public void processResponse(String response, Measurement measure) throws UnsupportedObdCommandException {
			int raw = Integer.parseInt(response.substring(0, 2),16);
			if(raw == 0){
				Logg.log(Log.INFO, "MAF Scaling", "MAF uses default scaling.");
			}
			else{
				scaling = raw / 6553.5;
			}
		}

		@Override
		public int getNumberOfExpectedChars() {
			return 8;
		}
		
		@Override
		public String toString() {
			return "Maximum Value of Mass Air Flow (MAF)";
		}
		
	}

}
