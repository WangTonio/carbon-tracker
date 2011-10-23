package de.unigoettingen.ct.obd.cmd;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;


public class CommandProvider {
	
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
