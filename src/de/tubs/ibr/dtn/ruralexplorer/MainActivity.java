
package de.tubs.ibr.dtn.ruralexplorer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.backend.GeoTagAdapter;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class MainActivity extends FragmentActivity implements
		MarkerFragment.OnWindowChangedListener,
		StatsFragment.OnWindowChangedListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	
	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	
	private static final int MARKER_LOADER_ID = 1;
	private static final int GEOTAG_LOADER_ID = 2;
	
	private HashMap<Long, Marker> mMarkerSet = new HashMap<Long, Marker>();
	private HashSet<Node> mNodeSet = new HashSet<Node>();
	private HashMap<Marker, Node> mNodeMap = new HashMap<Marker, Node>();
	
	private HashSet<GeoTag> mGeoTagSet = new HashSet<GeoTag>();
	private HashMap<Marker, GeoTag> mGeoTagMap = new HashMap<Marker, GeoTag>();

	private Boolean mLocationInitialized = false;
	private Marker mSelectionMarker = null;
	
	private Marker mActiveGeoTag = null;
	
	private StatsFragment mStatsFragment = null;
	private MarkerFragment mMarkerFragment = null;
	private RescueFragment mRescueFragment = null;
	private GoogleMap mMap = null;
	
	private RelativeLayout mMarkerLayout = null;

	private DataService mDataService = null;
	private boolean mBound = false;
	
	// If this tag is set, move to the tag instead of the own position
	private GeoTag mInitialGeoTag = null;
	
	private ServiceConnection mDataHandler = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDataService = ((DataService.LocalBinder)service).getService();
			
			getSupportLoaderManager().initLoader(MARKER_LOADER_ID,  null, MainActivity.this);
			getSupportLoaderManager().initLoader(GEOTAG_LOADER_ID,  null, MainActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			getSupportLoaderManager().destroyLoader(MARKER_LOADER_ID);
			getSupportLoaderManager().destroyLoader(GEOTAG_LOADER_ID);
			mDataService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get google map fragment
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		if (mMap != null) {
			// Other supported types include: MAP_TYPE_NORMAL,
			// MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			
			// enable own location
			mMap.setMyLocationEnabled(true);
			
			// set listener for clicks on marker
			mMap.setOnMarkerClickListener(mMarkerListener);
		}
		
		// Marker layout
		mMarkerLayout = (RelativeLayout) findViewById(R.id.marker_layout);
		
		// get marker fragment
		mMarkerFragment = (MarkerFragment) getSupportFragmentManager().findFragmentById(R.id.marker_fragment);
		
		// get stats fragment
		mStatsFragment = (StatsFragment) getSupportFragmentManager().findFragmentById(R.id.stats_fragment);
		
		// get rescue fragment
		mRescueFragment = (RescueFragment) getSupportFragmentManager().findFragmentById(R.id.rescue_fragment);
		mRescueFragment.getView().setVisibility(View.GONE);
		
		// read geotag from intent if present
		if (getIntent().hasExtra(DataService.EXTRA_GEOTAG)) {
			mInitialGeoTag = (GeoTag)getIntent().getSerializableExtra(DataService.EXTRA_GEOTAG);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mStatsFragment.getNode() != null) {
			mStatsFragment.bind(null);
		}
		else if (mMarkerFragment.getNode() != null) {
			// show / hide marker frame
			mMarkerFragment.bind(null);
			
			if (mSelectionMarker != null) {
				mSelectionMarker.setVisible(false);
			}
		}
		else if (mActiveGeoTag != null) {
			// hide rescue
			setRescue(null, null);
		}
		else {
			super.onBackPressed();
		}
	}
	
	public void setRescue(GeoTag tag, Marker marker) {
		boolean animate = ((marker != null) && (mActiveGeoTag == null)) || ((marker == null) && (mActiveGeoTag != null));
		
		// clear previous tag
		if (mActiveGeoTag != null) {
			mActiveGeoTag.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
		}
		
		if (tag == null) {
			if (animate) {
				final Animation a = AnimationUtils.makeOutAnimation(this, false);
				mRescueFragment.getView().startAnimation(a);
			}
			
			// hide rescue indicator
			mRescueFragment.getView().setVisibility(View.GONE);
			
			if (mActiveGeoTag != null) {
				// set previous marker icon to inactive
				mActiveGeoTag.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
			}
		} else {
			// bind to tag
			mRescueFragment.bind(tag);
			
			if (animate) {
				final Animation a = AnimationUtils.makeInAnimation(this, true);
				mRescueFragment.getView().startAnimation(a);
			}
			
			// show rescue indicator
			mRescueFragment.getView().setVisibility(View.VISIBLE);
			
			// set marker icon to active
			marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_active));
		}
		
		mActiveGeoTag = marker;
	}

	private GoogleMap.OnMarkerClickListener mMarkerListener = new GoogleMap.OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			// check if the marker is a tag
			if (mGeoTagMap.containsKey(marker)) {
				// select rescue tag
				setRescue(mGeoTagMap.get(marker), marker);
			}
			else {
				// move to the marker
				mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
				
				// show / hide marker frame
				Node n = mNodeMap.get(marker);
				
				if (n == null) {
					mMarkerFragment.bind(null);
					return true;
				}
				mMarkerFragment.bind(n);
				
				// set selection marker
				if (mSelectionMarker == null) {
					mSelectionMarker = mMap.addMarker(new MarkerOptions()
						.position(marker.getPosition())
						.icon(BitmapDescriptorFactory.defaultMarker())
					);
				}
				
				// set position of selection marker
				mSelectionMarker.setPosition(marker.getPosition());
				mSelectionMarker.setVisible(true);
			}
			
			return true;
		}
	};

	@Override
	protected void onDestroy() {
		if (mBound) {
			unregisterReceiver(mLocationReceiver);
			unbindService(mDataHandler);
			
			getSupportLoaderManager().destroyLoader(MARKER_LOADER_ID);
			getSupportLoaderManager().destroyLoader(GEOTAG_LOADER_ID);
			
			mBound = false;
		}
		
		super.onDestroy();
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
		
		if (!mLocationInitialized && mInitialGeoTag != null) {
			centerTo(mInitialGeoTag.getLocation());
			mLocationInitialized = true;
			mInitialGeoTag = null;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// read geotag from intent if present
		if (intent.hasExtra(DataService.EXTRA_GEOTAG)) {
			GeoTag tag = (GeoTag)intent.getSerializableExtra(DataService.EXTRA_GEOTAG);
			centerTo(tag.getLocation());
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// start up location broadcasting
		Intent i  = new Intent(this, DataService.class);
		i.setAction(DataService.ACTION_BACKGROUND_ON);
		startService(i);
	}

	@Override
	protected void onStop() {
		// start up location broadcasting
		Intent i  = new Intent(this, DataService.class);
		i.setAction(DataService.ACTION_BACKGROUND_OFF);
		startService(i);
		
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
			case R.id.action_show_marker:
				if (!mNodeSet.isEmpty()) {
					mMarkerFragment.bind(mNodeSet.iterator().next());
				}
				return true;
				
			case R.id.action_rescue:
			{
				Intent i = new Intent(this, DataService.class);
				i.setAction(DataService.ACTION_CALL_RESCUE);
				startService(i);
				return true;
			}
				
			case R.id.action_settings:
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
				
			case R.id.action_add_dummies:
				mDataService.getDatabase().addDummies();
				return true;
				
			case R.id.action_clear:
				mDataService.getDatabase().clear();
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void centerTo(LocationData data) {
		if (!data.hasLatitude() || !data.hasLongitude()) return;
		
		LatLng position = new LatLng(data.getLatitude(), data.getLongitude());
		
		if (!mLocationInitialized) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20.0f));
		} else {
			mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
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
	public void onMarkerWindowChanged(boolean visible) {
		FrameLayout layout = (FrameLayout)mMarkerLayout.findViewById(R.id.node_fragment_layout);
		
		if (visible) {
			final Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
			mMarkerLayout.startAnimation(a);
			mMarkerLayout.setVisibility(View.VISIBLE);
			
			mMap.setPadding(0, 0, 0, layout.getHeight());
		} else {
			final Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
			mMarkerLayout.startAnimation(a);
			mMarkerLayout.setVisibility(View.INVISIBLE);
			
			mMap.setPadding(0, 0, 0, 0);
		}
	}
	
	@Override
	public void onStatsWindowChanged(boolean visible) {
	}
	
	private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DataService.LOCATION_UPDATED.equals(intent.getAction())) {
				Location l = intent.getParcelableExtra(DataService.EXTRA_LOCATION);
				
				if ( l != null ) {
					mRescueFragment.setPosition(l);
					
					if (!mLocationInitialized) {
						centerTo(l);
						mLocationInitialized = true;
					}
				}
			}
		}
	};
	
	@Override
	public void onMarkerNodeSelected(Node n) {
		Marker marker = mMarkerSet.get(n.getId());
		
		if (marker == null) {
			if (mSelectionMarker != null) {
				mSelectionMarker.setVisible(false);
			}
			return;
		}

		// move to the marker
		mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
		
		// set selection marker
		if (mSelectionMarker == null) {
			mSelectionMarker = mMap.addMarker(new MarkerOptions()
				.position(marker.getPosition())
				.icon(BitmapDescriptorFactory.defaultMarker())
			);
		}
		
		// set position of selection marker
		mSelectionMarker.setPosition(marker.getPosition());
		mSelectionMarker.setVisible(true);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == MARKER_LOADER_ID) {
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			return new MarkerLoader(this, mDataService);
		}
		else if (id == GEOTAG_LOADER_ID) {
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			return new GeoTagLoader(this, mDataService);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (loader instanceof MarkerLoader) {
			updateMarkers(c);
		}
		else if (loader instanceof GeoTagLoader) {
			updateGeoTags(c);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader instanceof MarkerLoader) {
			for (Marker m : mMarkerSet.values()) {
				m.remove();
			}
			mMarkerSet.clear();
			mNodeSet.clear();
		}
		else if (loader instanceof GeoTagLoader) {
			for (Marker m : mGeoTagMap.keySet()) {
				m.remove();
			}
			mActiveGeoTag = null;
			mGeoTagSet.clear();
			mGeoTagMap.clear();
		}
	}
	
	private void updateGeoTags(Cursor c) {
		GeoTagAdapter.ColumnsMap tagmap = new GeoTagAdapter.ColumnsMap();
		
		// set to filter out inactive tags
		HashSet<GeoTag> inactiveTags = new HashSet<GeoTag>(mGeoTagSet);
		
		while (c.moveToNext()) {
			GeoTag t = new GeoTag(this, c, tagmap);
			
			// remove from inactive tags
			inactiveTags.remove(t);
			
			if (!mGeoTagSet.contains(t)) {
				mGeoTagSet.add(t);

				LocationData l = t.getLocation();
				
				if (l.hasLatitude() && l.hasLongitude()) {
					LatLng position = new LatLng(l.getLatitude(), l.getLongitude());
				
					// create a new marker
					Marker m = mMap.addMarker(
							new MarkerOptions()
									.position(position)
									.icon( BitmapDescriptorFactory.fromResource(R.drawable.ic_marker) )
									.anchor(0.5f, 0.5f)
									.flat(true)
						);
					
					// add marker to tag set
					mGeoTagMap.put(m, t);
				}
			}
		}
		
		// remove all inactive tags
		for (GeoTag t : inactiveTags) {
			for (Entry<Marker, GeoTag> e : mGeoTagMap.entrySet()) {
				if (e.getValue().equals(t)) {
					Marker m = e.getKey();
					m.remove();
					mGeoTagMap.remove(m);
					mGeoTagSet.remove(t);
					
					if (m.equals(mActiveGeoTag)) {
						mActiveGeoTag = null;
					}
					break;
				}
			}
		}
		
		if (mGeoTagSet.isEmpty()) {
			setRescue(null, null);
		}
	}
	
	private void updateMarkers(Cursor c) {
		NodeAdapter.ColumnsMap cm = new NodeAdapter.ColumnsMap();
		
		// clear all nodes in the node-set
		mNodeSet.clear();
		
		// set to filter out inactive markers
		HashSet<Marker> inactiveMarkers = new HashSet<Marker>(mMarkerSet.values());
		
		while (c.moveToNext()) {
			Node n = new Node(this, c, cm);
			
			// add node to node set
			mNodeSet.add(n);
			
			// update node views
			if (n.equals( mMarkerFragment.getNode() )) {
				mMarkerFragment.bind(n);
			}
			if (n.equals( mStatsFragment.getNode() )) {
				mStatsFragment.bind(n);
			}
			
			// update marker location
			LocationData l = n.getLocation();
			Marker m = mMarkerSet.get(n.getId());
			
			if (l.hasLatitude() && l.hasLongitude()) {
				LatLng position = new LatLng(l.getLatitude(), l.getLongitude());
				
				if (m == null) {
					// create a new marker
					m = mMap.addMarker(
					new MarkerOptions()
							.position(position)
							.icon(Node.getBitmap(n.getType()))
							.anchor(0.5f, 0.5f)
							.flat(true)
						);
				} else {
					// update marker
					m.setPosition(position);
					
					if (n.equals(mMarkerFragment.getNode()) && mSelectionMarker != null) {
						// set position of selection marker
						mSelectionMarker.setPosition(position);
					}
				}
				
				m.setVisible(true);
				
				// set bearing
				if (l.hasBearing()) {
					m.setRotation(l.getBearing());
				}
				
				// remove this from inactive markers
				inactiveMarkers.remove(m);
				
				mMarkerSet.put(n.getId(), m);
				mNodeMap.put(m, n);
			} else {
				mMarkerSet.remove(n.getId());
			}
		}
		
		// finally remove all inactive markers
		for (Marker inactive_marker : inactiveMarkers) {
			mNodeSet.remove(inactive_marker);
			inactive_marker.remove();
		}
		
		// if there are not more markers close marker window
		if (mNodeSet.isEmpty()) {
			mStatsFragment.bind(null);

			// show / hide marker frame
			mMarkerFragment.bind(null);
			
			if (mSelectionMarker != null) {
				mSelectionMarker.setVisible(false);
			}
		}
	}
}
