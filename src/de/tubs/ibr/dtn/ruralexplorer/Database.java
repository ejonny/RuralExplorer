package de.tubs.ibr.dtn.ruralexplorer;

import de.tubs.ibr.dtn.api.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

public class Database {
	// indicates updated data to other components
	public static final String DATA_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	
	// additional parameters
	public static final String EXTRA_ENDPOINT = "de.tubs.ibr.dtn.ruralexplorer.DATA_ENDPOINT";
	public static final String EXTRA_LOCATION = "de.tubs.ibr.dtn.ruralexplorer.DATA_LOCATION";
	
	private static final Database mDatabase = new Database();
	
	public static Database getInstance() {
		return mDatabase;
	}
	
	public void process(Context context, Bundle b) {
		// notify other components of the updated value
		Intent updatedIntent = new Intent(DATA_UPDATED);
		updatedIntent.putExtra(EXTRA_ENDPOINT, (Parcelable)b.getSource());
		context.sendBroadcast(updatedIntent);
	}
}
