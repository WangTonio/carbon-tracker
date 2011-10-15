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

import android.bluetooth.BluetoothSocket;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import de.unigoettingen.ct.data.DataCache;
import de.unigoettingen.ct.data.Measurement;
import de.unigoettingen.ct.obd.cmd.CommandProvider;
import de.unigoettingen.ct.obd.cmd.ObdCommand;
import de.unigoettingen.ct.obd.cmd.UnsupportedObdCommandException;

public class MeasurementFacade implements LocationListener{
	
	private BluetoothSocket socket;
	private InputStream inStream;
	private OutputStream outStream;
	private DataCache dataCache;
	private List<MeasurementStatusListener> listeners;
	private List<ObdCommand> obdCmds;
	private ExecutorService exec;
	private long measurementInterval;
	private double lastLongitude;
	private double lastLatitude;
	private volatile boolean forceStop;
	
	
	public MeasurementFacade(DataCache dataCache, long measurementInterval){
		this.dataCache = dataCache;
		this.measurementInterval = measurementInterval;
		this.listeners = new ArrayList<MeasurementStatusListener>(1);
		this.exec = Executors.newSingleThreadExecutor();
		this.forceStop = false;
	}
	
	public void addStatusListener(MeasurementStatusListener listener){
		this.listeners.add(listener);
	}
	
	public void setUp(final BluetoothSocket socket, final LocationManager locationMgr){
		if(!this.exec.isTerminated())
			throw new IllegalStateException();
		this.exec.execute(new Runnable() {		
			//@Override
			public void run() {
				//register for gps positioning signals
				//two updates every measurement interval are good enough; the frequency restriction saves power
				locationMgr.requestLocationUpdates( LocationManager.GPS_PROVIDER, measurementInterval/2, 0, MeasurementFacade.this);
				MeasurementFacade.this.socket = socket;
				try{
					MeasurementFacade.this.socket.connect();
					MeasurementFacade.this.inStream = MeasurementFacade.this.socket.getInputStream();
					MeasurementFacade.this.outStream = MeasurementFacade.this.socket.getOutputStream();
					CommandProvider.getCommand("DISABLE_ELM_ECHO").queryResult(null, MeasurementFacade.this.inStream, MeasurementFacade.this.outStream);
					//if no exception is thrown, the link is established and the command is accepted
					MeasurementFacade.this.informListeners(new MeasurementStatus(MeasurementStatus.States.SET_UP));
				}
				catch(IOException e){
					//TODO handle this
				}		
				catch(UnsupportedObdCommandException e){
					//TODO unexpected
				}
			}
		});

	}
	
	private void informListeners(MeasurementStatus stat){
		for(MeasurementStatusListener currentListener: this.listeners){
			currentListener.notify(stat, this);
		}
	}
	
	public void startMeasurement(){
		if(this.exec.isTerminated()){
			this.exec.execute(new Runnable() {			
				//@Override
				public void run() {
					while(!forceStop){
						long beforeMeasurement = System.currentTimeMillis();
						double lngBeforeMeasurement = lastLongitude;
						double latBeforeMeasurement = lastLatitude;
						Measurement currentMeasurement = new Measurement();
						for(ObdCommand currentCmd : obdCmds){
							try{
								currentCmd.queryResult(currentMeasurement, inStream, outStream);
							}
							catch(IOException e){
								//TODO log this, inform listeners, but go on
							}
							catch(UnsupportedObdCommandException e2){
								//TODO remove the command from the job list
							}
						}
						long afterMeasurement = System.currentTimeMillis();
						Calendar timeStamp = new GregorianCalendar();
						timeStamp.setTimeInMillis((beforeMeasurement+afterMeasurement)/2);
						currentMeasurement.setPointOfTime(timeStamp);
						//coordinates are adjusted by calculating the arithmetic mean of the coordinates
						//before and after the obd measurement. It is an approximation.
						currentMeasurement.setLongitude((lastLongitude + lngBeforeMeasurement)/2);
						currentMeasurement.setLatitude((lastLatitude+latBeforeMeasurement)/2);
						//dataCache.addTupleToCurrentRoute(currentMeasurement);
						try {
							Thread.sleep(measurementInterval);
						} catch (InterruptedException e) {
							// TODO think about handling this
							Thread.currentThread().interrupt();
						}
					}
					
				}
			});
		}
	}
	
	public void stopMeasurement(){
		this.forceStop = true;
	}

	//@Override
	public void onLocationChanged(Location loc) {
		this.lastLongitude = loc.getLongitude();
		this.lastLatitude = loc.getLatitude();
	}

	//@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
