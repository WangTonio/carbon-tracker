package de.unigoettingen.ct.ui;

import de.unigoettingen.ct.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Represents the preferences Activity which lets the user configure anything available in the DefaultSharedPreferences.
 * @author Fabian Sudau
 *
 */
public class PrefsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefsettings);
	}
}
