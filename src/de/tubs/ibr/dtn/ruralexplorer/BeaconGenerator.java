/*
 * AlarmReceiver.java
 * 
 * Copyright (C) 2011 IBR, TU Braunschweig
 *
 * Written-by: Johannes Morgenroth <morgenroth@ibr.cs.tu-bs.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BeaconGenerator {

	private static final String TAG = "BeaconGenerator";

	// activate alarm every 10 seconds
	static public void activate(Context context)
	{
		// create a new wakeup intent
		Intent intent = new Intent(context, ExplorerService.class);
		intent.setAction(ExplorerService.ACTION_GENERATE_BEACON);

		// check if the beacon alarm is already active
		PendingIntent pi = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_NO_CREATE);

		if (pi == null) {
			// create pending intent
			PendingIntent sender = PendingIntent.getService(context, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 1000, sender);

			Log.i(TAG, "Beaconing enabled.");
		} else {
			Log.i(TAG, "Beaconing already enabled.");
		}
	}

	// deactivate alarm
	static public void deactivate(Context context)
	{
		// create a new wakeup intent
		Intent intent = new Intent(context, ExplorerService.class);
		intent.setAction(ExplorerService.ACTION_GENERATE_BEACON);

		// check if the beacon alarm is already active
		PendingIntent pi = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_NO_CREATE);

		if (pi != null) {
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(pi);
			pi.cancel();

			Log.i(TAG, "Beaconing disabled.");
		}
	}
}
