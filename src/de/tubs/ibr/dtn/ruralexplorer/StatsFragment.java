
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	@SuppressWarnings("unused")
	private static final String TAG = "StatsFragment";
	
	private static final int STATS_LOADER_ID = 1;
	
	private RelativeLayout mLayout = null;
	
	private MarkerItemFragment mMarkerFragment = null;
	private SensorFragment mSensorFragment = null;
	
	private OnWindowChangedListener mListener = null;
	private DataService mDataService = null;
	
	public interface OnWindowChangedListener {
		public void onStatsWindowChanged(boolean visible);
	}
	
	public static StatsFragment newInstance() {
		StatsFragment fragment = new StatsFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public StatsFragment() {
		// Required empty public constructor
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDataService = ((DataService.LocalBinder) service).getService();
			
			getLoaderManager().initLoader(STATS_LOADER_ID,  null, StatsFragment.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			getLoaderManager().destroyLoader(STATS_LOADER_ID);
			mDataService = null;
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_stats, container, false);
		mLayout = (RelativeLayout)v.findViewById(R.id.stats_fragment_layout);
		mMarkerFragment = (MarkerItemFragment)getFragmentManager().findFragmentById(R.id.marker_item_fragment);
		mSensorFragment = (SensorFragment)getFragmentManager().findFragmentById(R.id.sensor_fragment);
		
		v.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// catch all touch events
				return true;
			}
		});
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnWindowChangedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnInfoWindowListener");
		}

		// bind to service
		Intent service = new Intent(activity, DataService.class);
		activity.bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDetach() {
		mDataService = null;
		getActivity().unbindService(mServiceConnection);
		getLoaderManager().destroyLoader(STATS_LOADER_ID);

		super.onDetach();
		mListener = null;
	}
	
	public void setNode(Node n) {
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
		} else {
			mLayout.setVisibility(View.VISIBLE);
		}
		
		mListener.onStatsWindowChanged(n != null);
		mMarkerFragment.bind(n);
		mSensorFragment.bind(n);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new MarkerLoader(getActivity(), mDataService);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		//mNodeAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		//mNodeAdapter.swapCursor(null);
	}
}
