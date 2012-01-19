package de.unigoettingen.ct.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Statically registered to receive ACTION_POWER_CONNECTED Intents, this checks, if automatic mode is enabled.
 * If so, it creates an internal Intent starting the {@link MainActivity} in automatic mode.
 * @author Fabian Sudau
 *
 */
public class PowerConnectedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intentArg) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("automatic_mode", false)){
			Intent intent = new Intent("de.unigoettingen.ct.MAINACTIVITY");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("automaticMode", true);
			context.startActivity(intent);
		}
	}

}
