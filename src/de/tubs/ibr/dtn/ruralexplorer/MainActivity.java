
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import de.tubs.ibr.dtn.ruralexplorer.MarkerFragment.OnInfoWindowListener;
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;

public class MainActivity extends FragmentActivity implements
		OnInfoWindowListener {

	private Boolean mLocationInitialized = false;
	private FrameLayout mLayoutDropShadow = null;
	private Boolean mInfoVisible = false;
	private Marker mSelectionMarker = null;

	private DataService mDataService = null;
	private boolean mBound = false;
	
	private ServiceConnection mDataHandler = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDataService = ((DataService.LocalBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDataService = null;
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
		
		// get info fragment
		MarkerFragment infoFragment = ((MarkerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.info_short));
	}
	
	@Override
	public void onBackPressed() {
		if (mInfoVisible) {
			// show / hide marker frame
			MarkerFragment infoFragment = ((MarkerFragment) getSupportFragmentManager()
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
			MarkerFragment infoFragment = ((MarkerFragment) getSupportFragmentManager()
					.findFragmentById(R.id.info_short));
			
//			try {
//				Node n = mNodeManager.get(marker);
//				int position = mNodeManager.getIndex(n);
//				infoFragment.setNode(n, position);
//				
//				// set selection marker
//				if (mSelectionMarker == null) {
//					mSelectionMarker = map.addMarker(new MarkerOptions()
//						.position(marker.getPosition())
//						.icon(BitmapDescriptorFactory.defaultMarker())
//					);
//				}
//				
//				// set position of selection marker
//				mSelectionMarker.setPosition(marker.getPosition());
//				mSelectionMarker.setVisible(true);
//			} catch (NodeNotFoundException e) {
//				infoFragment.setNode(null, 0);
//			}
			
			return true;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mBound) {
			unregisterReceiver(mLocationReceiver);
			unbindService(mDataHandler);
			mBound = false;
		}

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
			IntentFilter filter = new IntentFilter(DataService.LOCATION_UPDATED);
			registerReceiver(mLocationReceiver, filter);
			bindService(new Intent(this, DataService.class), mDataHandler, Context.BIND_AUTO_CREATE);
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
			if (!mLocationInitialized && DataService.LOCATION_UPDATED.equals(intent.getAction())) {
				Location l = intent.getParcelableExtra(DataService.EXTRA_LOCATION);
				if ( l != null ) {
					centerTo(l);
					mLocationInitialized = true;
				}
			}
		}
	};
	
	@Override
	public void onInfoWindowPageChanged(int position) {
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
//		try {
//			Node n = mNodeManager.get(position);
//			Marker marker = n.getMarker();
//
//			// move to the marker
//			map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
//			
//			// set selection marker
//			if (mSelectionMarker == null) {
//				mSelectionMarker = map.addMarker(new MarkerOptions()
//					.position(marker.getPosition())
//					.icon(BitmapDescriptorFactory.defaultMarker())
//				);
//			}
//			
//			// set position of selection marker
//			mSelectionMarker.setPosition(marker.getPosition());
//			mSelectionMarker.setVisible(true);
//		} catch (NodeNotFoundException e) {
//			// nothing to do.
//		}
	}
}
