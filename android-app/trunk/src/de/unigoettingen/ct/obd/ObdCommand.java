package de.unigoettingen.ct.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unigoettingen.ct.data.Tuple;

public abstract class ObdCommand {
	
	private static final String CHARSET_USED = "ASCII";
	private static final Map<String, ObdCommand> commandMap = initializeCommandMap();
	
	public static synchronized ObdCommand getCommand(String commandName){
		return commandMap.get(commandName);
	}
	
	private static Map<String, ObdCommand> initializeCommandMap(){
		Map<String, ObdCommand> map = new HashMap<String, ObdCommand>();
		//available commands will be added here as anonymous classes
		map.put("MAF_AIR_FLOW", new ObdCommand() {	
			@Override
			public void queryResult(Tuple tuple, InputStream in, OutputStream out) throws  IOException {
				out.write("0110\r\n".getBytes(CHARSET_USED));
				out.flush();
				List<Byte> result = this.readResult(in);
				tuple.setMafAirflowRate(((result.get(0)*256)+result.get(1)) / 100D);
			}
			
		});
		return map;
	}
	
	public abstract void queryResult(Tuple tuple, InputStream in, OutputStream out) throws IOException;
	
	protected List<Byte> readResult(InputStream in) throws IOException {
		byte c = 0;
		ArrayList<Byte> result = new ArrayList<Byte>();
		while ( (c = (byte) in.read()) != '>')
			result.add(c);
		return result;
	}

}
