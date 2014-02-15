
package de.tubs.ibr.dtn.ruralexplorer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.tubs.ibr.dtn.ruralexplorer.InfoFragment.OnInfoWindowListener;
import de.tubs.ibr.dtn.ruralexplorer.backend.LocationService;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeManager;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeNotFoundException;

public class MainActivity extends FragmentActivity implements
		OnInfoWindowListener {

	private NodeManager mNodeManager = null;
	
	private Boolean mLocationInitialized = false;
	private FrameLayout mLayoutDropShadow = null;
	private Boolean mInfoVisible = false;
	private Marker mSelectionMarker = null;
	
	private LocationService mLocationService = null;
	private boolean mBound = false;
	
	private ServiceConnection mServiceHandler = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mLocationService = ((LocationService.LocalBinder)service).getService();
			
			Location l = mLocationService.getMyLocation();
			if ((l != null) && (!mLocationInitialized)) {
				centerTo(l);
				mLocationInitialized = true;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mLocationService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get info window drop shadow
		mLayoutDropShadow = (FrameLayout)findViewById(R.id.info_drop_shadow);
		
		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// Other supported types include: MAP_TYPE_NORMAL,
		// MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		
		// enable own location
		map.setMyLocationEnabled(true);
		
		// set listener for clicks on marker
		map.setOnMarkerClickListener(mMarkerListener);
		
		// move camera to zoom level 20
		map.moveCamera(CameraUpdateFactory.zoomTo(20.0f));
		
		// create a new NodeManager
		mNodeManager = new NodeManager(this, map);
		
		// get info fragment
		InfoFragment infoFragment = ((InfoFragment) getSupportFragmentManager()
				.findFragmentById(R.id.info_short));
		
		// assign node manager to info fragment
		infoFragment.setNodeManager(mNodeManager);
		
		// add info fragment as listener of the node manager
		mNodeManager.addListener(infoFragment);
	}
	
	@Override
	public void onBackPressed() {
		if (mInfoVisible) {
			// show / hide marker frame
			InfoFragment infoFragment = ((InfoFragment) getSupportFragmentManager()
					.findFragmentById(R.id.info_short));
			
			infoFragment.setNode(null, 0);
			
			if (mSelectionMarker != null) {
				mSelectionMarker.setVisible(false);
			}
		} else {
			super.onBackPressed();
		}
	}

	private GoogleMap.OnMarkerClickListener mMarkerListener = new GoogleMap.OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			// move to the marker
			GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
			
			// show / hide marker frame
			InfoFragment infoFragment = ((InfoFragment) getSupportFragmentManager()
					.findFragmentById(R.id.info_short));
			
			try {
				Node n = mNodeManager.get(marker);
				int position = mNodeManager.getIndex(n);
				infoFragment.setNode(n, position);
				
				// set selection marker
				if (mSelectionMarker == null) {
					mSelectionMarker = map.addMarker(new MarkerOptions()
						.position(marker.getPosition())
						.icon(BitmapDescriptorFactory.defaultMarker())
					);
				}
				
				// set position of selection marker
				mSelectionMarker.setPosition(marker.getPosition());
				mSelectionMarker.setVisible(true);
			} catch (NodeNotFoundException e) {
				infoFragment.setNode(null, 0);
			}
			
			return true;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		mNodeManager.onStart();
	}

	@Override
	protected void onStop() {
		if (mBound) {
			unregisterReceiver(mLocationReceiver);
			unbindService(mServiceHandler);
			mBound = false;
		}
		
		mNodeManager.onStop();
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!mBound) {
			IntentFilter filter = new IntentFilter(LocationService.LOCATION_UPDATED);
			registerReceiver(mLocationReceiver, filter);
			
			Intent i = new Intent(this, LocationService.class);
			bindService(i, mServiceHandler, Context.BIND_AUTO_CREATE);
			mBound = true;
		}
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
			case R.id.action_settings:
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void centerTo(Location location) {
		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLng(position));
	}

	@Override
	public void onInfoWindowStateChanged(boolean visible, int height, int width) {
		GoogleMap map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		if (visible) {
			map.setPadding(0, 0, 0, height);
			mLayoutDropShadow.setVisibility(View.VISIBLE);
			mInfoVisible = true;
		} else {
			map.setPadding(0, 0, 0, 0);
			mLayoutDropShadow.setVisibility(View.GONE);
			mInfoVisible = false;
		}
	}
	
	private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!mLocationInitialized && LocationService.LOCATION_UPDATED.equals(intent.getAction())) {
				Location l = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
				centerTo(l);
				mLocationInitialized = true;
			}
		}
	};
	
	@Override
	public void onInfoWindowPageChanged(int position) {
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		try {
			Node n = mNodeManager.get(position);
			Marker marker = n.getMarker();

			// move to the marker
			map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
			
			// set selection marker
			if (mSelectionMarker == null) {
				mSelectionMarker = map.addMarker(new MarkerOptions()
					.position(marker.getPosition())
					.icon(BitmapDescriptorFactory.defaultMarker())
				);
			}
			
			// set position of selection marker
			mSelectionMarker.setPosition(marker.getPosition());
			mSelectionMarker.setVisible(true);
		} catch (NodeNotFoundException e) {
			// nothing to do.
		}
	}
}
