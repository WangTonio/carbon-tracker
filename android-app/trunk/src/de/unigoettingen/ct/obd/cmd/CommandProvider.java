package de.unigoettingen.ct.obd.cmd;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

/**
 * Utility class to determine, which OBD commands should be used.
 * @author Fabian Sudau
 *
 */
public class CommandProvider {
	
	/**
	 * Returns a list of all OBD commands that must be used periodically.
	 * This includes the obligatory OBD commands and enabled optional commands as well.
	 * @param prefs the preferences to load the user choice from
	 * @return 
	 */
	public static List<ObdCommand> getDesiredObdCommands(SharedPreferences prefs){
		List<ObdCommand> retVal = new ArrayList<ObdCommand>();
		//obligatory commands:
		retVal.add(new MassAirFlowCmd());
		retVal.add(new LambdaCmd());

		//optional commands:
		if(prefs.getBoolean("rpm", false)){
			retVal.add(new EngineRpmCmd());
		}
		if(prefs.getBoolean("eot", false)){
			retVal.add(new EngineOilTemperatureCmd());
		}
		if(prefs.getBoolean("speed", false)){
			retVal.add(new VehicleSpeedCmd());
		}
		if(prefs.getBoolean("ert", false)){
			retVal.add(new EngineRunTimeCmd());
		}

		return retVal;
	}

}
