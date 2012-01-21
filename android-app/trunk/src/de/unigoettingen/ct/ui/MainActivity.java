package de.unigoettingen.ct.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.unigoettingen.ct.R;
import de.unigoettingen.ct.service.TrackerService;
import de.unigoettingen.ct.service.TrackerService.TrackerServiceBinder;

/**
 * Represents the starting Activity of the app which is also the most important one for any user interaction.
 * The {@link TrackerService} will use the methods described in the interface {@link CallbackUI} to communicate
 * events from deep down the system to the user via this Activity.
 * @author Fabian Sudau
 *
 */
public class MainActivity extends Activity implements OnClickListener, CallbackUI{

    private static final int SETTINGS = 3;
    private static final String LOG_TAG = "MainActivity";
    
    private Button startMeasurementBtn;
    private Button preferencesBtn;
    private Button viewLogBtn;
    private Button uploadCacheBtn;
    private TextView statusLine;
    private ProgressDialog loadingDialog;
    
    private TrackerServiceBinder serviceBinder;
    private boolean hasRunningService = false;
    private boolean startMeasurementAutomatically = false;
     
    
    /** Defines callbacks for service binding, initiated by bindService().
     *  Does not have any functionality besides these two callback methods- */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        	//service is bound by the system
        	//store a reference to the binder, as it is the communication interface for the service
        	//register this activity as a listener for state changes
        	Log.i(LOG_TAG, "Service bound");
            serviceBinder = (TrackerServiceBinder) service;
            serviceBinder.setUIforCallbacks(MainActivity.this);
            startAutomaticallyIfRequired();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	//the service exited; this will only happen in cases of unrecoverable errors
        }
    };
    
    @Override
    protected void onNewIntent(Intent intent) {
    	//this is getting called whenever an intent to 'show up' is received,
    	//but an instance of this activity already exists.
    	//remember, if the intent dictated automatic mode. 
    	//that information is required at certain stages in the activity life cycle.
    	if(!startMeasurementAutomatically){
    		startMeasurementAutomatically = intent.getBooleanExtra("automaticMode", false);
    	}
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		startAutomaticallyIfRequired();
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.startMeasurementBtn = (Button) findViewById(R.id.startMeasurementBtn);
        this.startMeasurementBtn.setOnClickListener(this);
        this.preferencesBtn = (Button) findViewById(R.id.preferencesBtn);
        this.preferencesBtn.setOnClickListener(this);
        this.viewLogBtn = (Button) findViewById(R.id.logBtn);
        this.viewLogBtn.setOnClickListener(this);	
        this.uploadCacheBtn= (Button) findViewById(R.id.uploadCacheBtn);
        this.uploadCacheBtn.setOnClickListener(this);
        this.statusLine = (TextView) findViewById(R.id.statusLineTextView);
        this.startMeasurementAutomatically = getIntent().getBooleanExtra("automaticMode", false);
        this.startAndBindService();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SETTINGS, 0, "Settings");
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case SETTINGS:
        	updateConfig();
        	return true;
        }
        return false;
    }
    
    private void updateConfig() {
    	Intent configIntent = new Intent(this,PrefsActivity.class);
    	startActivity(configIntent);
    }
    
    private void goToLoggActivity(){
    	Intent configIntent = new Intent(this,LoggActivity.class);
    	startActivity(configIntent);
    }
    
	private void startAndBindService(){
        Intent intent = new Intent(this, TrackerService.class);
		startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //the service will be bound and created
	}
	
	private void startAutomaticallyIfRequired(){
		if (startMeasurementAutomatically && serviceBinder != null) {
			startMeasurementAutomatically = false;
			serviceBinder.start(true);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(this.serviceBinder!=null){
			this.serviceBinder.onActivityResult(requestCode, resultCode, data);
		}
	}
    
	
	public void onClick(View v) {
		if(v==this.startMeasurementBtn){
			if(hasRunningService){
				this.serviceBinder.stop(false);
			}
			else{
				this.serviceBinder.start(false);
			}
		}
		else if(v == this.preferencesBtn){
			this.updateConfig();
		}
		else if(v == this.viewLogBtn){
			this.goToLoggActivity();
		}
		else if(v == this.uploadCacheBtn){
			if(!hasRunningService){
				this.serviceBinder.uploadCachedData();
			}
		}
	}

	@Override
	public void diplayText(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	@Override
	public void indicateRunning(boolean running) {
		if(running){
			this.statusLine.setText("Running ...");
			this.startMeasurementBtn.setText("Stop Measurement");
			this.hasRunningService = true;
		}
		else{
			this.statusLine.setText("Stopped");
			this.startMeasurementBtn.setText("Start Measurement");
			this.hasRunningService = false;
		}
	}

	@Override
	public void indicateLoading(boolean loading) {
		if(loading && this.loadingDialog == null){
			this.loadingDialog = ProgressDialog.show(this, null, "Loading ...");
			this.loadingDialog.show();
		}
		else if(!loading && this.loadingDialog != null){
			this.loadingDialog.dismiss();
			this.loadingDialog = null;
		}
	}

	@Override
	public void promptUserToChooseFrom(final int promptCode, String title, String[] options) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(options, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if(serviceBinder != null){
		        	serviceBinder.returnUserHasSelected(promptCode, item);
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void promptUserToChooseYesOrNo(final int promptCode, String question) {
		new AlertDialog.Builder(this).
		setMessage(question).
		setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(serviceBinder != null){
		        	serviceBinder.returnUserHasSelected(promptCode, 1);
		        }
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(serviceBinder != null){
		        	serviceBinder.returnUserHasSelected(promptCode, 0);
		        }
			}
		}).
		show();
	}

	@Override
	public void promtUserToEnterText(final int promptCode, String message) {
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this).
		setMessage(message).
		setView(input).
		setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				serviceBinder.returnUserHasEntered(promptCode, input.getText().toString());
			}
		}).
		setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				serviceBinder.returnUserHasEntered(promptCode, null);
			}
		}).
		show();

		
	}
	
}