
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private LocationClient mLocationClient = null;
	private Marker mSelfMarker = null;

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

		mLocationClient = new LocationClient(this, this, this);

		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// Other supported types include: MAP_TYPE_NORMAL,
		// MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_center_location:
				centerToLocation();
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			// showErrorDialog(connectionResult.getErrorCode());
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		centerToLocation();
	}
	
	private void centerToLocation() {
		Location location = mLocationClient.getLastLocation();

		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
		
		mSelfMarker = map.addMarker(new MarkerOptions().position(position).flat(true));

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20));
	}

	@Override
	public void onDisconnected() {
		// nothing to do.
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mSelfMarker != null) {
			LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
			mSelfMarker.setPosition(position);
		}
	}

}
