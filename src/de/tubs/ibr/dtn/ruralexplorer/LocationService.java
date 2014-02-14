
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;

public class LocationService extends Service
	implements LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {
	
	// indicates updated location to other components
	public static final String LOCATION_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	
	// additional parameters
	public static final String EXTRA_LOCATION = "de.tubs.ibr.dtn.ruralexplorer.DATA_LOCATION";
	
	private LocationClient mLocationClient = null;
	private boolean mConnected = false;
	
	private Handler mBeaconHandler = null;
	
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	public LocationService getService() {
            return LocationService.this;
        }
    }
	
	public LocationService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public Location getMyLocation() {
		if (!mConnected) return null;
		return mLocationClient.getLastLocation();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// create a new handler
		mBeaconHandler = new Handler();
		
		// create a new location client
		mLocationClient = new LocationClient(this, this, this);
		
		// connect to location services
		mLocationClient.connect();
	}

	@Override
	public void onDestroy() {
		// stop beaconing
		mBeaconHandler.removeCallbacks(mBeaconProcess);
		
		// disconnect from location services
		mLocationClient.disconnect();
		
		super.onDestroy();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Intent i = new Intent(LOCATION_UPDATED);
		i.putExtra(EXTRA_LOCATION, location);
		sendBroadcast(i);
	}
	
	@Override
	public void onConnected(Bundle bundle) {
		mConnected = true;
		
		// start beaconing
		mBeaconHandler.post(mBeaconProcess);
		
		// send first location
		onLocationChanged(mLocationClient.getLastLocation());
	}
	
	@Override
	public void onDisconnected() {
		mConnected = false;
		
		// stop beaconing
		mBeaconHandler.removeCallbacks(mBeaconProcess);
	}
	
	private Runnable mBeaconProcess = new Runnable() {
		@Override
		public void run() {
			// generate beacon
			Intent intent = new Intent(LocationService.this, CommService.class);
			intent.setAction(CommService.ACTION_GENERATE_BEACON);
			intent.putExtra(LocationService.EXTRA_LOCATION, mLocationClient.getLastLocation());
			startService(intent);
			
			// next update in 10 seconds
			mBeaconHandler.postDelayed(mBeaconProcess, 10000);
		}
	};
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TOOD: handle google play services not found
		mConnected = false;
	}
}
