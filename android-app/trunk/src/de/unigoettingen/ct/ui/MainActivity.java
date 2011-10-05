package de.unigoettingen.ct.ui;

import de.unigoettingen.ct.R;
import de.unigoettingen.ct.R.layout;
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