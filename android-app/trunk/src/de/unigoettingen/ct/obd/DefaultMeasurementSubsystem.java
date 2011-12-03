package de.unigoettingen.ct.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothSocket;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import de.unigoettingen.ct.cache.TrackCache;
import de.unigoettingen.ct.data.Logg;
import de.unigoettingen.ct.data.io.Measurement;
import de.unigoettingen.ct.obd.cmd.DisableElmEchoCmd;
import de.unigoettingen.ct.obd.cmd.EmptyCmd;
import de.unigoettingen.ct.obd.cmd.ObdCommand;
import de.unigoettingen.ct.obd.cmd.VehicleIdentificationNumberCmd;
import de.unigoettingen.ct.service.AsynchronousSubsystem;
import de.unigoettingen.ct.service.SubsystemStatus;
import de.unigoettingen.ct.service.SubsystemStatusListener;

/**
 * Implementation of the subsystem doing the most important work in the whole application which is retrieving
 * OBD data from the vehicle and storing it in a data cache.
 * Once this subsystem is set up and started, it uses all passed-in {@link ObdCommand}s periodically to retrieve
 * the data.
 * @author Fabian Sudau
 *
 */
public class DefaultMeasurementSubsystem implements LocationListener, AsynchronousSubsystem{
	
	//sources of measurement data
	private BluetoothSocket socket;
	private InputStream inStream;
	private OutputStream outStream;
	private LocationManager locationMgr;
	
	//the 'outside' to send results to
	private TrackCache dataCache;
	private SubsystemStatusListener listener;
	
	//all of these are used periodically
	private List<ObdCommand> obdCmds;
	
	//thread management
	private ScheduledExecutorService exec;
	private volatile boolean goOnWithPeriodic;
	
	//timing
	private long measurementInterval;
	
	//these fields will be changed periodically by the GPS system in thread main
	//they are used for location stampts of measurements
	private double lastLongitude;
	private double lastLatitude;
	private double lastAltitude;
	
	private static final String LOG_TAG = "MeasurementSubsystem";
	
	/**
	 * Constructs an object, but does not perform any initialization yet. Call {@link #setUp()} to do so.
	 * After the set up has been performed successfully and {@link #start()} has been called, this object will
	 * periodically perform measurements and populate the data cache with those.
	 * @param dataCache the container to populate with measurement data
	 * @param measurementInterval every x milliseconds, a full tuple of measurements will be taken
	 * @param btsock fresh socket to connect to
	 * @param locationMgr the system object to request GPS updates from
	 * @param obdCmds the commands that will be used during each measurement
	 */
	public DefaultMeasurementSubsystem(TrackCache dataCache, long measurementInterval, BluetoothSocket btsock, 
			LocationManager locationMgr, List<ObdCommand> obdCmds){
		this.dataCache = dataCache;
		this.socket = btsock;
		this.locationMgr = locationMgr;
		this.measurementInterval = measurementInterval;
		this.exec = Executors.newSingleThreadScheduledExecutor();
		this.obdCmds = obdCmds;
	}
	
	@Override
	public void setStatusListener(SubsystemStatusListener listener){
		this.listener = listener;
	}
	
	@Override
	public void setUp(){
		//register for gps positioning signals
		//two updates every measurement interval are good enough; the frequency restriction saves power
		//the updates are probably invoked from the main thread
		//NOTE: the following call has to be performed from thread main, otherwise it will block for ever
		this.locationMgr.requestLocationUpdates( LocationManager.GPS_PROVIDER, measurementInterval/2, 0, this);
		Logg.log(Log.DEBUG, LOG_TAG, "Successfully enabled GPS.");
		this.exec.execute(new Runnable() {		
			public void run() {
				Log.d(LOG_TAG, "setUp executing in worker thread.");
				//establish the bluetooth connection and try to send the obligatory (and always supported) ELM system command
				try{
					DefaultMeasurementSubsystem.this.socket.connect();
					DefaultMeasurementSubsystem.this.inStream = DefaultMeasurementSubsystem.this.socket.getInputStream();
					DefaultMeasurementSubsystem.this.outStream = DefaultMeasurementSubsystem.this.socket.getOutputStream();
					Log.i(LOG_TAG, "Connected successfully to th Bluetooth socket.");
					//this hack is required, because the elm adapter says it's name right after startup
					new EmptyCmd().queryResult(null, DefaultMeasurementSubsystem.this.inStream, DefaultMeasurementSubsystem.this.outStream);
					Log.i(LOG_TAG, "Empty Command finished successfully.");
					new DisableElmEchoCmd().queryResult(null, DefaultMeasurementSubsystem.this.inStream, DefaultMeasurementSubsystem.this.outStream);
				}
				catch(IOException e){
					notifyListener(SubsystemStatus.States.FATAL_ERROR_STOPPED, "Could not establish connection to the ELM bluetooth adapter.");
					cleanUp();
					return;
				}		
				catch(UnsupportedObdCommandException e){
					notifyListener(SubsystemStatus.States.FATAL_ERROR_STOPPED, "ELM adapter did not respond to DISABLE_ECHO command.");
					cleanUp();
					return;
				}
				//if no exception is thrown, the link is established and the command is accepted
				//now query for the VIN and see if it matches the one in memory (makes sure, we are in the right vehicle)
				VehicleIdentificationNumberCmd vinCmd = new VehicleIdentificationNumberCmd();
				String retrievedVin = null;
				try {
					vinCmd.queryResult(null, inStream, outStream);
					retrievedVin = vinCmd.getVin();
				}
				catch (IOException e) {
					notifyListener(SubsystemStatus.States.FATAL_ERROR_STOPPED, "Vin Command caused an IOException: "+e.getMessage());
					cleanUp();
					return;
				}
				catch (UnsupportedObdCommandException e) {
					Logg.log(Log.WARN, LOG_TAG, "Vehicle does not privde vin. Using default value 'UNKNOWNVIN'");
					retrievedVin = "UNKNOWNVIN";
				}
				if(!dataCache.matchVinOfActiveTrack(retrievedVin)){
					notifyListener(SubsystemStatus.States.FATAL_ERROR_STOPPED, "The stored Track has a different VIN than the current vehicle. "+
							"Start a new Track or use the original vehicle again.");
					cleanUp();
					return;
				}
				//at this point everything went fine, the periodic measurement can be turned on using the start() method
				notifyListener(SubsystemStatus.States.SET_UP);
			}
		});

	}
	
	//the following runnable will be scheduled periodically, if the set up worked
	//reuse this runnable to save allocation costs
	final Runnable periodicTask = new Runnable() {
		@Override
		public void run() {
			//this cancellation flag is necessary to prevent a race condition regarding the stop() runnable
			if(!goOnWithPeriodic){
				return;
			}
			
			//use these values for a simple 'interpolation'
			long beforeMeasurement = System.currentTimeMillis();
			double lngBeforeMeasurement = lastLongitude;
			double latBeforeMeasurement = lastLatitude;
			double altBeforeMeasurement = lastAltitude;
			
			Measurement currentMeasurement = new Measurement();
			for(Iterator<ObdCommand> iterator = obdCmds.iterator(); iterator.hasNext(); ){
				ObdCommand currentCmd = iterator.next();
				boolean success;
				try{
					currentCmd.queryResult(currentMeasurement, inStream, outStream);
					success = true;
				}
				catch(IOException e){
					Log.e(LOG_TAG,"IOException by periodic Command:", e);
					notifyListener(SubsystemStatus.States.ERROR_BUT_ONGOING, "Command "+currentCmd.getClass().getSimpleName()+" caused an IOException: "+e.getMessage());
					success=false;
//					cleanUp();
//					return;
				}
				catch(UnsupportedObdCommandException e2){
					notifyListener(SubsystemStatus.States.ERROR_BUT_ONGOING, "Command "+currentCmd.getClass().getSimpleName()+" is not supported.");
					success = false;
//					iterator.remove();
				}
				if(!currentCmd.useAgainRegardingOutcome(success)){
					Logg.log(Log.ERROR, LOG_TAG, "Command "+currentCmd.getClass().getSimpleName()+" failed too many times and is turned off.");
					iterator.remove();
				}
			}

			long afterMeasurement = System.currentTimeMillis();
			Calendar timeStamp = new GregorianCalendar();
			timeStamp.setTimeInMillis((beforeMeasurement+afterMeasurement)/2);
			currentMeasurement.setPointOfTime(timeStamp);
			//coordinates are adjusted by calculating the arithmetic mean of the coordinates
			//before and after the OBD measurement. It is an approximation.
			currentMeasurement.setLongitude((lastLongitude + lngBeforeMeasurement)/2);
			currentMeasurement.setLatitude((lastLatitude+latBeforeMeasurement)/2);
			currentMeasurement.setAltitude((lastAltitude+altBeforeMeasurement)/2);
			//push the new populated measurement object into the cache
			//the caching strategy will handle the event
			dataCache.addMeasurementToActiveTrack(currentMeasurement);
		}
	};
	
	private void notifyListener(SubsystemStatus.States state, String msg){
		this.listener.notify(new SubsystemStatus(state, msg), this);
	}
	
	private void notifyListener(SubsystemStatus.States state){
		this.listener.notify(new SubsystemStatus(state), this);
	}
	
	@Override
	public void start(){
		this.goOnWithPeriodic=true;
		exec.scheduleAtFixedRate(periodicTask, 250, measurementInterval, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void stop(){
		//do idiot-safe clean up and indicate that this terminated gracefully
		//this will hopefully be the last job
		//TODO there is a possible race condition that another periodic task gets executed after the clean up job
		//it happens, if after the execute call below and before the shutDown call the scheduler prefers other threads
		this.goOnWithPeriodic=false;
		this.locationMgr.removeUpdates(this); //it is possible this works from thread main only
		this.exec.execute(new Runnable() {
			@Override
			public void run() {
				cleanUp();
				notifyListener(SubsystemStatus.States.STOPPED_BY_USER);
			}
		});
		this.exec.shutdown();
	}
	
	/**
	 * Performs fail-safe clean up to prevent any resources from leaking.
	 * That means, all bluetooth / gps related stuff is turned off / released.
	 */
	private void cleanUp() {
		if (locationMgr != null) {
			try {
				locationMgr.removeUpdates(this);
				locationMgr = null;
			}
			catch (IllegalArgumentException e) {
				// this occurs, if this object is not registered. we do not care
			}
		}
		if (outStream != null) {
			try {
				outStream.close();
				outStream = null;
			}
			catch (IOException e) {
				// we do not care
			}
		}
		if (inStream != null) {
			try {
				inStream.close();
				inStream = null;
			}
			catch (IOException e) {
				// we do not care
			}
		}
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			}
			catch (IOException e) {
				// we do not care
			}
		}

	}

	@Override
	public void onLocationChanged(Location loc) {
		this.lastLongitude = loc.getLongitude();
		this.lastLatitude = loc.getLatitude();
		this.lastAltitude = loc.getAltitude();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString() {
		return "MeasurementSys";
	}

}
