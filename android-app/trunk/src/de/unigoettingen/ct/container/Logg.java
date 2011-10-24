package de.unigoettingen.ct.container;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.unigoettingen.ct.data.DebugLog;
import de.unigoettingen.ct.data.DebugMessage;
import de.unigoettingen.ct.data.GenericObservable;

import android.util.Log;

/**
 * A custom logger. Log messages send to this one will also be visible in a log activity and can be send to the server.
 * This class is implemented as a singleton with static methods forwarding calls for convenience.
 * @author Fabian Sudau
 *
 */
public class Logg extends GenericObservable<Logg>{
	
	public static final Logg INSTANCE = new Logg();
	
	private List<DebugMessage> messages;
	
	public Logg(){
		this.messages = new ArrayList<DebugMessage>();
	}
	
	public DebugLog getLogDump(){
		return new DebugLog(android.os.Build.MODEL != null ? android.os.Build.MODEL : "UNKNOWN DEVICE", this.getMessagesAsArray());
	}
	
	public synchronized DebugMessage[] getMessagesAsArray(){
		DebugMessage[] msgArray = new DebugMessage[messages.size()];
		msgArray = messages.toArray(msgArray);
		return msgArray;
	}
	
	public static void log(int loglevel,String tag, String msg, Throwable th){
		Log.println(loglevel, tag, msg);
		Log.println(loglevel, tag, Log.getStackTraceString(th));
		synchronized (INSTANCE) {
			INSTANCE.messages.add(new DebugMessage(Calendar.getInstance(), msg+" \n "+th.getClass().getName()+" "+th.getMessage()));
			INSTANCE.fireUpdates(INSTANCE);
		}
	}

	public static void log(int loglevel,String tag, String msg){
		Log.println(loglevel, tag, msg);
		synchronized (INSTANCE) {
			INSTANCE.messages.add(new DebugMessage(Calendar.getInstance(), msg));
			INSTANCE.fireUpdates(INSTANCE);
		}
	}
}
