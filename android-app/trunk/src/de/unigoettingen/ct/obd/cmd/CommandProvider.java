package de.unigoettingen.ct.obd.cmd;

import java.util.HashMap;
import java.util.Map;


public class CommandProvider {
	
	private static final Map<String, ObdCommand> commandMap = initializeCommandMap();
	
	public static synchronized ObdCommand getCommand(String commandName){
		return commandMap.get(commandName);
	}

	private static Map<String, ObdCommand> initializeCommandMap() {
		Map<String, ObdCommand> ret = new HashMap<String, ObdCommand>();
		ret.put("DISABLE_ELM_ECHO", new DisableElmEchoCmd());
		ret.put("MASS_AIR_FLOW", new MassAirFlowCmd());
		return ret;
	}

}
