package de.unigoettingen.ct.ui;

import java.util.Calendar;
//import android.util.Log;

import de.unigoettingen.ct.R;
import de.unigoettingen.ct.data.Measurement;
import de.unigoettingen.ct.data.Person;
import de.unigoettingen.ct.data.TrackPart;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import de.unigoettingen.ct.ws.UploadService;

public class MainActivity extends Activity {
	TextView tv;
    private static final int SETTINGS = 3;
    public static final String LOG_TAG = "carbontracker";
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        Log.v(LOG_TAG, "Hallo Welt");
        tv = (TextView)findViewById(R.id.textView1);
        
        
		TrackPart trackPart = new TrackPart();
		Measurement measurements[] = new Measurement[2];

		measurements[0] = new Measurement();
		measurements[1] = new Measurement();
		
		measurements[0].setAltitude(11.1);
		measurements[0].setEot(90);
		measurements[0].setErt(1000);
		measurements[0].setLambda(1.00);
		measurements[0].setLatitude(52.510611);
		measurements[0].setLongitude(13.408056);
		measurements[0].setMaf(100);
		measurements[0].setPointOfTime(Calendar.getInstance());
		measurements[0].setRpm(1500);
		measurements[0].setSpeed(101);
		
		measurements[1].setAltitude(12.1);
		measurements[1].setEot(91);
		measurements[1].setErt(1001);
		measurements[1].setLambda(1.01);
		measurements[1].setLatitude(53.510611);
		measurements[1].setLongitude(14.408056);
		measurements[1].setMaf(101);
		Calendar oneSecLater = Calendar.getInstance();
		oneSecLater.roll(Calendar.SECOND, true);
		measurements[1].setPointOfTime(oneSecLater);
		measurements[1].setRpm(1501);
		measurements[1].setSpeed(102);
		
//		measurements[0] = new Measurement(11.1, 90, 1000, 1, 52.510611, 13.408056, 100, Calendar.getInstance(), 1500, 101);
//		measurements[1] = new Measurement(12.1, 91, 1001, 1, 52.810611, 13.808056, 101, Calendar.getInstance(), 1501, 102);
		// (altitude, eot, ert, lambda, latitude, longitude, maf, pointOfTime, rpm, speed)

		trackPart.setDescription("Betrunken durch Göttingen gerast");
		trackPart.setDriver(new Person("Heinz", "Harald"));
		trackPart.setLastPart(false);
		trackPart.setStartedAt(measurements[0].getPointOfTime());
		trackPart.setVin("SAMPLEVIN");
		trackPart.setMeasurements(measurements);

		UploadService uploadService = new UploadService();
		uploadService.callWebservice(trackPart);
		
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