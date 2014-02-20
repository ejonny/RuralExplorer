
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class StatsFragment extends Fragment {
	
	@SuppressWarnings("unused")
	private static final String TAG = "StatsFragment";
	
	private Node mNode = null;
	private RelativeLayout mLayout = null;
	
	private MarkerItemFragment mMarkerFragment = null;
	private SensorFragment mSensorFragment = null;
	private LocationFragment mLocationFragment = null;
	private AccelerationFragment mAccelerationFragment = null;
	
	private OnWindowChangedListener mListener = null;
	
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_stats, container, false);
		mLayout = (RelativeLayout)v.findViewById(R.id.stats_fragment_layout);
		mMarkerFragment = (MarkerItemFragment)getFragmentManager().findFragmentById(R.id.marker_item_fragment);
		mSensorFragment = (SensorFragment)getFragmentManager().findFragmentById(R.id.sensor_fragment);
		mLocationFragment = (LocationFragment)getFragmentManager().findFragmentById(R.id.location_fragment);
		mAccelerationFragment = (AccelerationFragment)getFragmentManager().findFragmentById(R.id.acceleration_fragment);
		
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
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	public Node getNode() {
		return mNode;
	}
	
	public void bind(Node n) {
		boolean animate = ((mNode != null) && !mNode.equals(n)) || ((n != null) && !n.equals(mNode));
		
		mNode = n;
		
		if (n == null) {
			if (animate) {
				final Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
				mLayout.startAnimation(a);
			}
			
			mLayout.setVisibility(View.INVISIBLE);
		} else {
			if (animate) {
				final Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
				mLayout.startAnimation(a);
			}
			
			mLayout.setVisibility(View.VISIBLE);
		}
		
		mListener.onStatsWindowChanged(n != null);
		mMarkerFragment.bind(n);
		mSensorFragment.bind(n);
		mLocationFragment.bind(n);
		mAccelerationFragment.bind(n);
	}
}
