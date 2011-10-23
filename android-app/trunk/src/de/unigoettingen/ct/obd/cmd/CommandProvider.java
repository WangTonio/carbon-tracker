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
		retVal.add(new MassAirFlowCmd());
		//TODO other obligatory commands go here
		
		if(prefs.getBoolean("rpm", false)){
			retVal.add(new EngineRpmCmd());
		}
		//TODO other optional commands go here
		return retVal;
	}

}
