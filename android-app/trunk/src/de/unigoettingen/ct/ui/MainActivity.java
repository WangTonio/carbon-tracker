package de.unigoettingen.ct.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;
import de.unigoettingen.ct.R;
import de.unigoettingen.ct.service.TrackerService;
import de.unigoettingen.ct.service.TrackerService.TrackerServiceBinder;

public class MainActivity extends Activity implements OnClickListener, CallbackUI{

    private static final int SETTINGS = 3;
    private static final String LOG_TAG = "MainActivity";
    
    private Button startMeasurementBtn;
    private Button preferencesBtn;
    private Button viewLogBtn;
    private TextView statusLine;
    private ProgressDialog loadingDialog;
    
    private TrackerServiceBinder serviceBinder;
    private boolean hasRunningService = false;
     
    
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	//the service exited; this will only happen in cases of unrecoverable errors
        }
    };
    
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
        this.statusLine = (TextView) findViewById(R.id.statusLineTextView);
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
	
    
	
	public void onClick(View v) {
		if(v==this.startMeasurementBtn){
			if(hasRunningService){
				this.serviceBinder.stop();
			}
			else{
				this.serviceBinder.start();
			}
		}
		else if(v == this.preferencesBtn){
			this.updateConfig();
		}
		else if(v == this.viewLogBtn){
			this.goToLoggActivity();
		}
	}

	@Override
	public void diplayText(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
	
	
}