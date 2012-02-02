package de.unigoettingen.ct.obd.cmd;

import java.io.IOException;

import android.util.Log;

import de.unigoettingen.ct.data.Logg;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.UnsupportedObdCommandException;

public class LambdaCmd extends ObdCommand{
	
	//internally, the lambda command is a bit more complicated:
	//first, the command 0113 determines, which O2 sensors are available.
	//then, this command uses the first available sensor for every future query.
	//possible sensors:
	//[location] ; [obd command (hex)]
	//bank 1 - sensor 1 ; 0114
	//bank 1 - sensor 2 ; 0115
	//bank 1 - sensor 3 ; 0116
	//bank 1 - sensor 4 ; 0117
	//bank 2 - sensor 1 ; 0118
	//bank 2 - sensor 2 ; 0119
	//bank 2 - sensor 3 ; 011A
	//bank 2 - sensor 4 ; 011B
	
	private String commandString = "0113"; //changes dynamically and implicitly holds the state of this command
										//0113 is the initial 'location of oxygen sensors' command
	private static final int LAMBDA_CMD_OFFSET = 20; // (== 0x14) this is bank 1 - sensor 1 (suffix only)

	@Override
	public String getCommandString() {
		return this.commandString;
	}

	@Override
	public void processResponse(String response, Measurement measure) throws IOException, UnsupportedObdCommandException {
		if(this.commandString.equals("0113")){
			//interpret result as response to the 'which sensors are available?' query
			if(response.length() != 2){
				throw new IOException("Location of Oxygen Sensors command expected 1 byte, but "+response.length()+" hex digits were returned.");
			}
			int bitmask = Integer.parseInt(response,16); //3 leading 00 bytes here
			//map this bitmask to the table at the top.
			//the lowest order bit, that is set, wins the competition and becomes the future command
			if(bitmask != 0){
				int futureCommandSuffix = Integer.numberOfTrailingZeros(bitmask) + LAMBDA_CMD_OFFSET;
				this.commandString = "01" + Integer.toHexString(futureCommandSuffix);
				Logg.log(Log.INFO, getClass().getSimpleName(), "Determined "+this.commandString+" as the best supported O2 sensor.");
			}
			else{
				throw new UnsupportedObdCommandException("Vehicle does not have any O2 sensors; lambda value can not be measured.");
			}
		}
		else{
			//interpret result as actual oxygen sensor values
			if(response.length() != 4){
				throw new IOException("O2 sensor command expected 2 bytes, but "+response.length()+" hex digits were returned.");
			}
			//the first 2 bytes are the oxygen sensor output voltage, the following two bytes are the short term fuel trim
			double lambda = Integer.parseInt(response.substring(0, 2), 16);
			lambda = lambda * 0.005; //scaling in volt
			measure.setLambda(lambda);
		}
	}

}
