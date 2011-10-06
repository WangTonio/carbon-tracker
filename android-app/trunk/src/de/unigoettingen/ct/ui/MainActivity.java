package de.unigoettingen.ct.ui;

import java.util.Calendar;

import de.unigoettingen.ct.R;
import de.unigoettingen.ct.R.layout;
import de.unigoettingen.ct.data.Measurement;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.data.TrackPart;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    private static final int SETTINGS = 3;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		TrackPart trackPart = new TrackPart();
		Measurement measurements[] = new Measurement[2];

		measurements[0] = new Measurement(11.1, 90, 1000, 1, 52.510611, 13.408056, 100, Calendar.getInstance(), 1500, 101);
		measurements[1] = new Measurement(12.1, 91, 1001, 1, 52.810611, 13.808056, 101, Calendar.getInstance(), 1501, 102);
		// (altitude, eot, ert, lambda, latitude, longitude, maf, pointOfTime, rpm, speed)

		trackPart.setDescription("Bierkasten abgegeben");
		trackPart.setDriver(new Person("Andre", "vonHof"));
		trackPart.setLastPart(false);
		trackPart.setStartedAt(Calendar.getInstance());
		trackPart.setVin("2SHDBV35JAS");
		trackPart.setMeasurements(measurements);
        
        
        
        
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
}