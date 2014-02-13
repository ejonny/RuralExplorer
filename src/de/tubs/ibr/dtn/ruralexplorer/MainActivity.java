
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// check if this is the first start-up
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.contains(Preferences.KEY_BEACON_ENABLED)) {
			// enable beaconing
			prefs.edit().putBoolean(Preferences.KEY_BEACON_ENABLED, true).commit();
			
			// activate beacon timer
			BeaconGenerator.activate(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
