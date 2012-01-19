package de.unigoettingen.ct.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import de.unigoettingen.ct.cache.PersistenceBinder;
import de.unigoettingen.ct.cache.SimpleCachingSystem;
import de.unigoettingen.ct.cache.TrackCache;
import de.unigoettingen.ct.data.Logg;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.io.Person;
import de.unigoettingen.ct.obd.CommandProvider;
import de.unigoettingen.ct.obd.DefaultMeasurementSubsystem;
import de.unigoettingen.ct.obd.cmd.ObdCommand;
import de.unigoettingen.ct.ui.CallbackUI;
import de.unigoettingen.ct.upload.ManualUploadSystem;

/**
 * Implemented as a local {@link Service}, this class is the bridge in between all the IO code running in different threads 
 * and the one and only user interface. The job of this class is to keep the measurement running in the background and to allow
 * the Activity to bind / unbind at any time; always being responsive. This service is intended to be permanently active, however,
 * if no measurement is going on, no resources are bound or consumed. <br>
 * All of code of this class runs in Thread Main. The UI can communicate with this service by calling the methods of the
 * service binder ({@link TrackerServiceBinder}). This service, however, will take over the control flow occasionally and call
 * methods of the UI interface described in {@link CallbackUI}.
 * @author Fabian Sudau
 *
 */
public class TrackerService extends Service implements SubsystemStatusListener{
	
	//Sadly, this class does a bit to much and has too many fields & dependencies.
	//This is due to this class' job as the big coordinator / facade to the subsystems.
	//A better design would be appreciated.

	//state and thread control
	private boolean active=false;
	private boolean automaticMode=false; //if true, do not ask the user anything and use defaults
	private Handler mainThread;
	
	//very important reference to interact with the ui
	private CallbackUI ui;
	
	//fields for temporary use, that are null most of the time
	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket;
	private List<BluetoothDevice> btDevices;
	private List<OngoingTrack> tracksToChooseFrom;
	
	//important references to subsystems doing the real work
	private AsynchronousSubsystem cachingStrat;
	private AsynchronousSubsystem measurementSystem;
	//the manualUploadSystem is used temporarily and is null most of the time
	private AsynchronousSubsystem manualUploadSystem;
	
	//last received states are remembered for future decision making
	private SubsystemStatus.States cachingState;
	private SubsystemStatus.States measurementState;
	
	//the data cache; a reference must be kept for track choosing and closing
	private TrackCache trackCache;
	
	//constants
	private static final String LOG_TAG = "TrackerService";
	//api documentation says this uuid must match the one of the
	//bt server (the adapter). however, i can not find it out and this random one seems to work !
	private static final String MY_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; 
	private static final int REQUESTCODE_ENABLE_BT = 1000;
	private static final int PROMPT_CODE_SELECT_BT_DEVICE = 60001;
	private static final int PROMPT_CODE_SELECT_TRACK = 60002;
	private static final int PROMPT_CODE_CLOSE_TRACK = 60003;
	private static final int PROMPT_CODE_NAME_NEW_TRACK = 60004;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mainThread = new Handler();
	}
	
	// Create a BroadcastReceiver for asynchronous call backs indicating bluetooth events.
	// this will be called, when a new bluetooth device is discovered while scanning and also when scanning is done
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent and remember it
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(!btDevices.contains(device)){
					btDevices.add(device);
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				unregisterReceiver(mReceiver);  // ==this
				ui.indicateLoading(false);
				//paired devices will only be displayed, if they are in range
				if(btDevices.isEmpty()){
					logAndShowError("No Bluetooth devices found!");
					cleanUp();
					return;
				}
				if(automaticMode){
					//prefer devices from diamex or those that show up early - automatic mode has it's disadvantages
					boolean deviceAccepted=false;
					for(BluetoothDevice currDevice: btDevices){
						if(!deviceAccepted && currDevice.getName().contains("DIAMEX")){
							deviceAccepted=true;
							connectToSelectedDevice(currDevice);
						}
					}
					if(!deviceAccepted){
						connectToSelectedDevice(btDevices.get(0));
					}
				}
				else{
					presentAvailableBluetoothDevices();
				}
			}
		}
	};
	
	//when started in automatic mode, this is registered for the power off event in order to
	//shut down taking measurements automatically, when the power cord is removed
	private final BroadcastReceiver powerOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mBinder.stop(true);
		}
	};
	
	private void setUpBluetooth(){
		// check adapter presence
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			logAndShowError("Device does not have a Bluetooth Adapter!");
			return;
		}
		// check, if bluetooth is turned on
		// if not, prompt the user to do so
		if (!btAdapter.isEnabled()) {
			if(automaticMode){
				logAndShowError("The bluetooth adapter is not enabled and can not be enabled without user interaction");
			}
			else{
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				ui.startActivityForResult(enableBtIntent, REQUESTCODE_ENABLE_BT);
				// returns to onActivityResult
			}
		}
		else {
			scanForBluetoothDevices();
		}
	}
	
	private void scanForBluetoothDevices(){
		// Register the BroadcastReceiver for asynchronous call backs
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister after scanning is done
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		btDevices = new ArrayList<BluetoothDevice>();
		
		ui.indicateLoading(true);
		if(!btAdapter.startDiscovery()){
			ui.indicateLoading(false);
			logAndShowError("Device discovery could not be started for unknown reasons.");
			unregisterReceiver(mReceiver);
			cleanUp();
		}
	}
	
	private void presentAvailableBluetoothDevices(){
		String[] userChoices = new String[btDevices.size()];
		for(int i=0; i<userChoices.length; i++){
			String stringForUi = "";
			if(btDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED){
				stringForUi += "P: "; //paired devices will get this prefix
			}
			stringForUi += btDevices.get(i).getName();
			userChoices[i]= stringForUi;
		}
		//the following will result in a call back to returnUserHasSelected(int promptCode, int index) of the service binder
		ui.promptUserToChooseFrom(PROMPT_CODE_SELECT_BT_DEVICE,"Connect to Bluetooth device", userChoices);
	}
	
	private void connectToSelectedDevice(BluetoothDevice device){
		try {
			btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID_STRING));
		}
		catch (IOException e) {
			logAndShowError("Can not create Bluetooth socket.");
			e.printStackTrace();
			cleanUp();
			return;
		}
		Log.d(LOG_TAG, "Created Bluetooth socket for device "+device.toString());
		Log.d(LOG_TAG, "Attempt to connect will be performed by the subsystem...");
		btDevices=null;
		if(automaticMode){
			setUpSubsystems(createNewTrack("In automatic mode"));
		}
		else{
			presentAvailableTracks();
		}
	}
	
	private void presentAvailableTracks(){
		Log.d(LOG_TAG, "Asking the user to select a track.");
		PersistenceBinder persistence = new PersistenceBinder(getApplicationContext());
		this.tracksToChooseFrom = persistence.loadOpenTracksEmpty();
		persistence.close();
		
		//the choices will be all open tracks plus 'new track' as the last option
		String[] options = new String[this.tracksToChooseFrom.size()+1];
		for(int i=0; i<this.tracksToChooseFrom.size(); i++){
			options[i]=this.tracksToChooseFrom.get(i).toDescription();
		}
		options[options.length-1]= "New Track";
		ui.promptUserToChooseFrom(PROMPT_CODE_SELECT_TRACK, "Select a Track", options);
		//will result in a call back to returnUserHasSelected
	}
	
	private void setUpSubsystems(OngoingTrack activeTrack) {
		if (!active) {
			Log.d(LOG_TAG, "Creating subsystems");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			this.trackCache = new TrackCache();
			
			String webServiceUrl = prefs.getString("serverurl", "http://134.76.21.30/CarbonTrackerWS/");
			this.cachingStrat = new SimpleCachingSystem(this.trackCache, activeTrack, new PersistenceBinder(getApplicationContext()), webServiceUrl);
			this.cachingStrat.setStatusListener(this);
			
			List<ObdCommand> commands = CommandProvider.getDesiredObdCommands(prefs);
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			long measurementInterval = Integer.valueOf(prefs.getString("measurement_interval", "1000"));
			this.measurementSystem = new DefaultMeasurementSubsystem(this.trackCache, measurementInterval, this.btSocket, locationManager, commands);
			this.measurementSystem.setStatusListener(this);
			
			this.active = true;
			this.cachingStrat.setUp();
			//this.measurementSystem.setUp(); is done when the caching system is done settingUp to prevent race conditions
			this.btSocket=null;
			this.btAdapter=null;
			this.ui.indicateLoading(true);
		}
	}
	
	private void terminate(){
		cleanUp();
	}
	
	@Override
	public void onDestroy() {
		cleanUp();
	}
	
	@Override
	public void notify(final SubsystemStatus status, final AsynchronousSubsystem sender) {
		//this method gets called from another thread
		//it is necessary to handle the status update in thread main, just as everything else runs in thread main in this class
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				Log.d(LOG_TAG, status.getState()+ " from "+sender);
				if(sender == manualUploadSystem){
					handleManualUploaderUpdate(status);
				}
				Log.d(LOG_TAG, "Caching: "+cachingState+"  Measurement: "+measurementState);
				
				//1. remember the subsystem states for future decision making
				if(sender == cachingStrat){
					cachingState = status.getState();
				}
				else if (sender == measurementSystem){
					measurementState = status.getState();
				}
				
				//2. decide on the interaction with the ui
				ui.indicateLoading(oneStateIs(SubsystemStatus.States.SETTING_UP) || oneStateIs(SubsystemStatus.States.SET_UP));
				ui.indicateRunning( (cachingState==SubsystemStatus.States.IN_PROGRESS || cachingState==SubsystemStatus.States.ERROR_BUT_ONGOING) &&
						(measurementState==SubsystemStatus.States.IN_PROGRESS || measurementState==SubsystemStatus.States.ERROR_BUT_ONGOING) );

				switch(status.getState()){
					case SETTING_UP:
						break;
					case SET_UP: 
						break;
					case IN_PROGRESS:
						if(sender == measurementSystem){
							ui.diplayText("Data is beeing retrieved.");
						}
						else{
							//this is not interesting so far as this is just a mock implementation
							//ui.diplayText("Caching mechanism is active.");
						}
						break;
					case STOPPED_BY_USER:
						break;
					case ERROR_BUT_ONGOING: //both types of error cause the same message so far
					case FATAL_ERROR_STOPPED:
						Logg.log(Log.ERROR, sender.toString(), sender.toString()+" says: "+status.getAdditionalInfo());
						ui.diplayText(sender.toString()+" says: "+status.getAdditionalInfo());
						break;
					default:
						ui.diplayText(status.toString());
						break;		
				}
				
				//3. decide on interaction with the subsystems
				switch(status.getState()){
					case SETTING_UP:
						//just wait
						break;
					case SET_UP: 
						//ready? then go
						sender.start();
						if(sender==cachingStrat){
							measurementSystem.setUp();
						}
						break;
					case IN_PROGRESS:
						//fine
						break;
					case STOPPED_BY_USER:
						if(sender == measurementSystem){
							measurementSystem = null;
							measurementState = null;
						}
						else{
							cachingStrat = null;
							cachingState=null;
						}
						break;
					case ERROR_BUT_ONGOING:
						break;
					case FATAL_ERROR_STOPPED:
						cleanUp();
						break;
					default:
						break;		
				}
			}
		});
	}
	
	public void startManualUpload(){
		String webServiceUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("serverurl", "http://134.76.21.30/CarbonTrackerWS/");
		this.manualUploadSystem = new ManualUploadSystem(webServiceUrl, new PersistenceBinder(getApplicationContext()));
		this.manualUploadSystem.setStatusListener(this);
		this.manualUploadSystem.setUp();
		this.ui.indicateLoading(true);
		//this object will receive asynchronous callbacks
	}
	
	private void handleManualUploaderUpdate(final SubsystemStatus state){
		mainThread.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOG_TAG, "Update from ManualUploader: "+state);
				switch(state.getState()){
					case SETTING_UP:
						ui.indicateLoading(true);
						break;
					case SET_UP:
						ui.diplayText(state.getAdditionalInfo());
						ui.indicateLoading(true);
						break;
					case IN_PROGRESS:
						ui.indicateLoading(true);
						break;
					case FATAL_ERROR_STOPPED:
					case STOPPED_BY_USER:
						manualUploadSystem = null;
						ui.indicateLoading(false);
						ui.diplayText(state.getAdditionalInfo());
						break;
					default:
						Log.wtf(LOG_TAG, "Can not handle state "+state+".");
				}
			}
		});
	}
	
	//helper methods (for convenience) below -------------------------------------
	
	private boolean oneStateIs(SubsystemStatus.States state){
		return cachingState == state || measurementState == state;
	}
	
	
	private void logAndShowError(String msg){
		Logg.log(Log.ERROR, LOG_TAG, msg);
		if(ui != null){
			ui.diplayText(msg);
		}
	}
	
	private OngoingTrack createNewTrack(String description){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TrackerService.this);
		return new OngoingTrack(Calendar.getInstance(), null, description,
				new Person(prefs.getString("forename", "Unknown"), prefs.getString("lastname", "Driver")));
	}
	
	/**
	 * Stops every subsystem, that is not already stopped and nulls everything in order to bind as few resources as possible.
	 * The service will be in the idle state again, the system keeps it in that state.
	 */
	private void cleanUp(){
		active = false;
		btAdapter = null;
		try{
			unregisterReceiver(mReceiver);
		}
		catch(IllegalArgumentException e){
			//thrown, if receiver is not registered. we do not care.
		}
		try{
			unregisterReceiver(powerOffReceiver);
		}
		catch(IllegalArgumentException e){
			//thrown, if receiver is not registered. we do not care.
		}
		btDevices = null;
		tracksToChooseFrom = null;
		trackCache=null;
		if(measurementState != null && measurementState!=SubsystemStatus.States.FATAL_ERROR_STOPPED &&
				measurementState != SubsystemStatus.States.STOPPED_BY_USER && measurementSystem!=null){
			measurementSystem.stop(); //nulling will happen in the notify method
		}
		else{
			measurementSystem=null;
			measurementState=null;
		}
		if(cachingState != null && cachingState != SubsystemStatus.States.FATAL_ERROR_STOPPED &&
				cachingState != SubsystemStatus.States.STOPPED_BY_USER && cachingStrat != null){
			cachingStrat.stop(); //nulling will happen in the notify method
		}
		else{
			cachingStrat=null;
			cachingStrat=null;
		}
		Log.i(LOG_TAG, "Released all bound resources. Idle state.");
	}
	
	//binder below ---------------------------------------------------------------
	
	private final TrackerServiceBinder mBinder = new TrackerServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder; 
	}
	
	public class TrackerServiceBinder extends Binder{
		
		public void start(boolean automatic){
			if(!active){
				automaticMode=automatic;
				registerReceiver(powerOffReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
				setUpBluetooth();
			}
			else{
				Log.e(LOG_TAG, "start() requested although the service is already running.");
			}
		}
		
		public void uploadCachedData(){
			if(manualUploadSystem == null){
				startManualUpload();
			}
		}
		
		public void stop(boolean automatic){
			Log.d(LOG_TAG, "Termination is requested.");
			if(active){
				if(automatic){
					trackCache.setActiveTrackToClosed();
					terminate(); 
				}
				else{
					ui.promptUserToChooseYesOrNo(PROMPT_CODE_CLOSE_TRACK, "Mark Track as done?");
				}
			}
			else{
				Log.e(LOG_TAG, "Stop requested without anything running.");
			}
		}
		
		public void setUIforCallbacks(CallbackUI ui){
			TrackerService.this.ui = ui;
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == REQUESTCODE_ENABLE_BT) {
				if (resultCode == Activity.RESULT_OK) {
					scanForBluetoothDevices();
				}
				else {
					Logg.log(Log.ERROR, LOG_TAG, "You did not enable bluetooth.");
					ui.diplayText("You did not enable bluetooth.");
					btAdapter=null;
				}
			}
		}
		
		public void returnUserHasSelected(int promptCode, int index){
			switch(promptCode){
				case PROMPT_CODE_SELECT_BT_DEVICE:
					connectToSelectedDevice(btDevices.get(index));
					break;
				case PROMPT_CODE_CLOSE_TRACK:
					if(index == 1){
						//user has chosen to close the track
						trackCache.setActiveTrackToClosed();
					}
					terminate(); 
					break;
				case PROMPT_CODE_SELECT_TRACK:
					if(index == tracksToChooseFrom.size()){
						//'New Track' was chosen
						tracksToChooseFrom=null;
						ui.promtUserToEnterText(PROMPT_CODE_NAME_NEW_TRACK, "Enter a Description:");
					}
					else{
						//an existing track is continued
						OngoingTrack wantedTrack = tracksToChooseFrom.get(index);
						tracksToChooseFrom=null;
						setUpSubsystems(wantedTrack);
					}
					break;
				default: 
					Log.wtf(LOG_TAG, "Unrecognized prompt code :"+promptCode);
			}
		}
		
		public void returnUserHasEntered(int promptCode, String text){
			switch(promptCode){
				case PROMPT_CODE_NAME_NEW_TRACK:
					if(text == null){
						//dialog was canceled
						ui.diplayText("Canceled");
						cleanUp();
					}
					else{
						setUpSubsystems(createNewTrack(text));
					}
					break;
				default: 
					Log.wtf(LOG_TAG, "Unrecognized prompt code :"+promptCode);
			}
		}
	}

}
