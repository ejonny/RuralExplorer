package de.tubs.ibr.dtn.ruralexplorer.backend;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import de.tubs.ibr.dtn.api.Bundle;

public class Database {
	// indicates updated data to other components
	public static final String DATA_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	public static final String EXTRA_NODE = "de.tubs.ibr.dtn.ruralexplorer.NODE";
	
	private static final Database mDatabase = new Database();
	
	public static Database getInstance() {
		return mDatabase;
	}
	
	public void process(Context context, Bundle b) {
		// notify other components of the updated value
		Intent updatedIntent = new Intent(DATA_UPDATED);
		updatedIntent.putExtra(CommService.EXTRA_ENDPOINT, (Parcelable)b.getSource());
		context.sendBroadcast(updatedIntent);
	}
}
