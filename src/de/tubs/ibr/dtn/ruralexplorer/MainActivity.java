
package de.tubs.ibr.dtn.ruralexplorer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
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
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;

public class MainActivity extends FragmentActivity implements
		OnInfoWindowListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "MainActivity";
	
	private static final int MARKER_LOADER_ID = 1;
	
	private NodeAdapter mNodeAdapter = null;

	private Boolean mLocationInitialized = false;
	private FrameLayout mLayoutDropShadow = null;
	private Boolean mInfoVisible = false;
	private Marker mSelectionMarker = null;
	
	private MarkerFragment mMarkerInfo = null;
	private GoogleMap mMap = null;

	private DataService mDataService = null;
	private boolean mBound = false;
	
	private ServiceConnection mDataHandler = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDataService = ((DataService.LocalBinder)service).getService();
			
			getSupportLoaderManager().initLoader(MARKER_LOADER_ID,  null, MainActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			getLoaderManager().destroyLoader(MARKER_LOADER_ID);
			mDataService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get info window drop shadow
		mLayoutDropShadow = (FrameLayout)findViewById(R.id.info_drop_shadow);
		
		// get google map fragment
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		// Other supported types include: MAP_TYPE_NORMAL,
		// MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		
		// enable own location
		mMap.setMyLocationEnabled(true);
		
		// set listener for clicks on marker
		mMap.setOnMarkerClickListener(mMarkerListener);
		
		// get info fragment
		mMarkerInfo = ((MarkerFragment) getSupportFragmentManager().findFragmentById(R.id.marker_info_fragment));
		
		// Create an empty adapter we will use to display the loaded data.
		mNodeAdapter = new NodeAdapter(this, null, new NodeAdapter.ColumnsMap());
		mNodeAdapter.registerDataSetObserver(mMarkerObserver);
	}
	
	@Override
	public void onBackPressed() {
		if (mInfoVisible) {
			// show / hide marker frame
			mMarkerInfo.setNode(null, 0);
			
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
			mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
			
			// show / hide marker frame
			
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
			
			getLoaderManager().destroyLoader(MARKER_LOADER_ID);
			
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
		
		// show marker info
		mMarkerInfo.setVisible(true);
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
		LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
		
		if (!mLocationInitialized) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20.0f));
		} else {
			mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
		}
	}

	@Override
	public void onInfoWindowStateChanged(boolean visible, int height, int width) {
		if (visible) {
			mMap.setPadding(0, 0, 0, height);
			mLayoutDropShadow.setVisibility(View.VISIBLE);
			mInfoVisible = true;
		} else {
			mMap.setPadding(0, 0, 0, 0);
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
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "Marker: onCreateLoader()");
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new MarkerLoader(this, mDataService);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mNodeAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mNodeAdapter.swapCursor(null);
	}
	
	private DataSetObserver mMarkerObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			Log.d(TAG, "Marker: onChanged()");
			super.onChanged();
		}

		@Override
		public void onInvalidated() {
			Log.d(TAG, "Marker: onInvalidated()");
			super.onInvalidated();
		}
	};
}
