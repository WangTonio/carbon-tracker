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
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import de.unigoettingen.ct.container.Logg;
import de.unigoettingen.ct.container.TrackCache;
import de.unigoettingen.ct.data.OngoingTrack;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.obd.DefaultMeasurementSubsystem;
import de.unigoettingen.ct.obd.cmd.CommandProvider;
import de.unigoettingen.ct.obd.cmd.ObdCommand;
import de.unigoettingen.ct.ui.CallbackUI;

public class TrackerService extends Service implements SubsystemStatusListener{

	private boolean active=false;
	private CallbackUI ui;
	private Handler mainThread;
	
	private BluetoothAdapter btAdapter;
	private List<BluetoothDevice> btDevices;
	
	private AsynchronousSubsystem cachingStrat;
	private AsynchronousSubsystem measurementSystem;
	
	private SubsystemStatus.States cachingState;
	private SubsystemStatus.States measurementState;
	
	private static final String LOG_TAG = "TrackerService";
	private static final String MY_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"; //api documentation says this uuid must match the one of the
	//bt server (the adapter). however, i can not find it out and this random one seems to work !
	private static final int REQUESTCODE_ENABLE_BT = 1000;
	
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
				presentAvailableBluetoothDevices();
			}
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
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			ui.startActivityForResult(enableBtIntent, REQUESTCODE_ENABLE_BT);
			// returns to onActivityResult
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
		//note that it is not queried for already paired devices.
		//TODO as we hope, that they are included in the scanning process
		if(btDevices.isEmpty()){
			logAndShowError("No Bluetooth devices found!");
			cleanUp();
			return;
		}
		String[] userChoices = new String[btDevices.size()];
		for(int i=0; i<userChoices.length; i++){
			String stringForUi = "";
			if(btDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED){
				stringForUi += "P: "; //paired devices will get this prefix
			}
			stringForUi += btDevices.get(i).getName();
			userChoices[i]= stringForUi;
		}
		//the following will result in a call back to returnUserHasSelected(int index) of the service binder
		ui.promptUserToChooseFrom("Connect to Bluetooth device", userChoices);
	}
	
	private void connectToSelectedDevice(BluetoothDevice device){
		BluetoothSocket btSocket = null;
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
		setUpSubsystems(btSocket);
	}
	
	private void setUpSubsystems(BluetoothSocket btSocket) {
		if (!active) {
			Log.d(LOG_TAG, "Creating subsystems");
			TrackCache cache = new TrackCache(new OngoingTrack(Calendar.getInstance(), "SAMPLEVIN", "Some description", new Person("Heinz", "Harald")));
			this.cachingStrat = new SimpleCachingStratgey(cache);
			this.cachingStrat.setStatusListener(this);
			List<ObdCommand> commands = CommandProvider.getDesiredObdCommands(PreferenceManager.getDefaultSharedPreferences(this));
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			this.measurementSystem = new DefaultMeasurementSubsystem(cache, 2000, btSocket, locationManager, commands);
			this.measurementSystem.setStatusListener(this);
			this.active = true;
			this.cachingStrat.setUp();
			this.measurementSystem.setUp();
		}
	}
	
	private void terminate(){
		cleanUp();
	}
	
	@Override
	public void notify(final SubsystemStatus status, final AsynchronousSubsystem sender) {
		//this method gets called from another thread
		//it is necessary to handle the status update in thread main, just as everything else runs in thread main in this class
		this.mainThread.post(new Runnable() {	
			@Override
			public void run() {
				Log.d(LOG_TAG, status.toString()+ " from "+sender);
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
				ui.indicateRunning(bothStatesAre(SubsystemStatus.States.IN_PROGRESS));

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
							ui.diplayText("Caching mechanism is active.");
						}
						break;
					case STOPPED_BY_USER:
						break;
					case ERROR_BUT_ONGOING: //both type of error cause the same message so far
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
	
	//helper methods (for convenience) below -------------------------------------
	
	private boolean oneStateIs(SubsystemStatus.States state){
		return cachingState == state || measurementState == state;
	}
	
	private boolean bothStatesAre(SubsystemStatus.States state){
		return cachingState == state && measurementState == state; 
	}
	
	private void logAndShowError(String msg){
		Logg.log(Log.ERROR, LOG_TAG, msg);
		if(ui != null){
			ui.diplayText(msg);
		}
	}
	
	/**
	 * Stops every subsystem, that is not already stopped and nulls everything in order to bind as few resources as possible.
	 * The service will be in the idle state again, the system keeps it in that state.
	 */
	private void cleanUp(){
		btAdapter = null;
		active = false;
		btDevices = null;
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
	}
	
	//binder below ---------------------------------------------------------------
	
	private final IBinder mBinder = new TrackerServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder; 
	}
	
	public class TrackerServiceBinder extends Binder{
		
		public void start(){
			setUpBluetooth();
		}
		
		public void stop(){
			Log.d(LOG_TAG, "Termination is requested.");
			terminate();
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
				}
			}
		}
		
		public void returnUserHasSelected(int index){
			connectToSelectedDevice(btDevices.get(index));
		}
	}

}
