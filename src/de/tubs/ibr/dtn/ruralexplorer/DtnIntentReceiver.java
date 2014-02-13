package de.tubs.ibr.dtn.ruralexplorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class DtnIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(de.tubs.ibr.dtn.Intent.STATE))
		{
			String state = intent.getStringExtra("state");
			if (state.equals("ONLINE"))
			{
				// respect user settings
				if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Preferences.KEY_BEACON_ENABLED, false))
				{
					// register scheduled presence update
					BeaconGenerator.activate(context);
				}
			}
			else if (state.equals("OFFLINE"))
			{
				// unregister scheduled presence update
				BeaconGenerator.deactivate(context);
			}
		}
		else if (action.equals(de.tubs.ibr.dtn.Intent.RECEIVE))
		{
			// We received a notification about a new bundle and
			// wake-up the local service to received the bundle.
			Intent i = new Intent(context, ExplorerService.class);
			i.setAction(de.tubs.ibr.dtn.Intent.RECEIVE);
			context.startService(i);
		}
		else if (action.equals(de.tubs.ibr.dtn.Intent.STATUS_REPORT))
		{
			// We received a status report about a bundle and
			// wake-up the local service to process this report.
			Intent i = new Intent(context, ExplorerService.class);
			i.setAction(ExplorerService.REPORT_DELIVERED_INTENT);
			i.putExtra("source", intent.getParcelableExtra("source"));
			i.putExtra("bundleid", intent.getParcelableExtra("bundleid"));
			context.startService(i);
		}
	}
}
