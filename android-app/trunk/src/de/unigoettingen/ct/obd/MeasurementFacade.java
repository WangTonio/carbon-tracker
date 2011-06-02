package de.unigoettingen.ct.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.unigoettingen.ct.data.DataCache;
import de.unigoettingen.ct.data.Tuple;

import android.bluetooth.BluetoothSocket;
import android.provider.CallLog;

public class MeasurementFacade {
	
	private BluetoothSocket socket;
	private InputStream inStream;
	private OutputStream outStream;
	private DataCache dataCache;
	private List<MeasurementStatusListener> listeners;
	private List<ObdCommand> obdCmds;
	private ExecutorService exec;
	private volatile boolean forceStop;
	
	
	public MeasurementFacade(DataCache dataCache){
		this.dataCache = dataCache;
		this.listeners = new ArrayList<MeasurementStatusListener>(1);
		this.exec = Executors.newSingleThreadExecutor();
		this.forceStop = false;
	}
	
	public void addStatusListener(MeasurementStatusListener listener){
		this.listeners.add(listener);
	}
	
	public void setUp(BluetoothSocket socket){
		this.socket = socket;
		try{
			this.socket.connect();
			this.inStream = this.socket.getInputStream();
			this.outStream = this.socket.getOutputStream();
		}
		catch(IOException e){
			//TODO handle this
		}
	}
	
	public void startMeasurement(){
		if(this.exec.isTerminated()){
			this.exec.execute(new Runnable() {			
				@Override
				public void run() {
					while(!forceStop){
						long beforeMeasurement = System.currentTimeMillis();
						Tuple currentTuple = new Tuple();
						for(ObdCommand currentCmd : obdCmds){
							try{
								currentCmd.queryResult(currentTuple, inStream, outStream);
							}
							catch(IOException e){
								//TODO log this, inform listeners, but go on
							}
							//TODO sleep some time
						}
						long afterMeasurement = System.currentTimeMillis();
						Calendar timeStamp = new GregorianCalendar();
						timeStamp.setTimeInMillis((beforeMeasurement+afterMeasurement)/2);
						currentTuple.setTimeStamp(timeStamp);
						dataCache.addTupleToCurrentRoute(currentTuple);
					}
					
				}
			});
		}
	}
	
	public void stopMeasurement(){
		this.forceStop = true;
	}

}
