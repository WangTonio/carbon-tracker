package de.unigoettingen.ct.container;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.unigoettingen.ct.data.DebugLog;
import de.unigoettingen.ct.data.DebugMessage;

import android.util.Log;

/**
 * A custom logger. Log messages send to this one will also be visible in a log activity and can be send to the server.
 * @author Fabian Sudau
 *
 */
public class Logg {
	
	private static List<DebugMessage> messages = new ArrayList<DebugMessage>();
	
	public static DebugLog getLogDump(){
		DebugMessage[] msgArray = new DebugMessage[messages.size()];
		msgArray = messages.toArray(msgArray);
		return new DebugLog(android.os.Build.MODEL != null ? android.os.Build.MODEL : "UNKNOWN DEVICE", msgArray);
	}
	
	public static void log(int loglevel,String tag, String msg, Throwable th){
		Log.println(loglevel, tag, msg);
		Log.println(loglevel, tag, Log.getStackTraceString(th));
		messages.add(new DebugMessage(Calendar.getInstance(), msg+" \n "+th.getClass().getName()+" "+th.getMessage()));
	}

	public static void log(int loglevel,String tag, String msg){
		Log.println(loglevel, tag, msg);
		messages.add(new DebugMessage(Calendar.getInstance(), msg));
	}
}
