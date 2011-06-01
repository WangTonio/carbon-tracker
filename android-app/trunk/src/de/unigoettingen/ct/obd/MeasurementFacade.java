package de.unigoettingen.ct.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.bluetooth.BluetoothSocket;

public class MeasurementFacade {
	
	private BluetoothSocket socket;
	private InputStream inStream;
	private OutputStream outStream;
	private List<MeasurementStatusListener> listeners;
	private List<ObdCommand> obdCmds;
	private ExecutorService exec;
	private volatile boolean forceStop;
	
	public MeasurementFacade(){
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
						for(ObdCommand currentCmd : obdCmds){
							//TODO fire each cmd, record data
						}
					}
					
				}
			});
		}
	}
	
	public void stopMeasurement(){
		
	}

}
