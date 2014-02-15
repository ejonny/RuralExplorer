package de.tubs.ibr.dtn.ruralexplorer.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DtnIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(de.tubs.ibr.dtn.Intent.RECEIVE))
		{
			// We received a notification about a new bundle and
			// wake-up the local service to received the bundle.
			Intent i = new Intent(context, CommService.class);
			i.setAction(de.tubs.ibr.dtn.Intent.RECEIVE);
			context.startService(i);
		}
		else if (action.equals(de.tubs.ibr.dtn.Intent.STATUS_REPORT))
		{
			// We received a status report about a bundle and
			// wake-up the local service to process this report.
			Intent i = new Intent(context, CommService.class);
			i.setAction(CommService.REPORT_DELIVERED_INTENT);
			i.putExtra("source", intent.getParcelableExtra("source"));
			i.putExtra("bundleid", intent.getParcelableExtra("bundleid"));
			context.startService(i);
		}
	}
}
