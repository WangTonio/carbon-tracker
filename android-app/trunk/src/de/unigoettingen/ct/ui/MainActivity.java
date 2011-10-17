package de.unigoettingen.ct.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.unigoettingen.ct.R;
import de.unigoettingen.ct.service.TrackerService;
import de.unigoettingen.ct.service.TrackerService.TrackerServiceBinder;

public class MainActivity extends Activity implements OnClickListener{

    private static final int SETTINGS = 3;
    private static final String LOG_TAG = "MainActivity";
    
    private Button startMeasurementBtn;
    private Button preferencesBtn;
    private Button viewLogBtn;
    
    private TrackerServiceBinder serviceBinder;
     
    
    /** Defines callbacks for service binding, initiated by bindService().
     *  Does not have any functionality besides these two callback methods- */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        	//service is bound by the system
        	//store a reference to the binder, as it is the communication interface for the service
        	//register this activity as a listener for state changes
            serviceBinder = (TrackerServiceBinder) service;
           // serviceBinder.setStatusListener(MainActivity.this);
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
    
	private void startAndBindRadioPlayerService(){
        Intent intent = new Intent(this, TrackerService.class);
		startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //the service will be bound and created
        //when the service is set up, this object will receive an asynchronous call back
	}
	
    
	public void onClick(View v) {
		if(v==this.startMeasurementBtn){
			
		}
		else if(v == this.preferencesBtn){
			this.updateConfig();
		}
		else if(v == this.viewLogBtn){
			
		}
	}
}