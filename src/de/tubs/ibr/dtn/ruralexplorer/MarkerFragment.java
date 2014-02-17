
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.backend.Database;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeNotFoundException;

public class MarkerFragment extends Fragment {
	
	private static final String TAG = "MarkerFragment";

	private FrameLayout mLayout = null;

	private MarkerPagerAdapter mMarkerAdapter = null;
	private ViewPager mViewPager = null;
	
	private OnInfoWindowListener mListener = null;
	private DataService mDataService = null;

	public static MarkerFragment newInstance() {
		MarkerFragment fragment = new MarkerFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public MarkerFragment() {
		// Required empty public constructor
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMarkerAdapter = new MarkerPagerAdapter(getFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_marker, container, false);
		mLayout = (FrameLayout)v.findViewById(R.id.node_fragment_layout);
		
		mViewPager = (ViewPager)v.findViewById(R.id.info_pager);
		mViewPager.setAdapter(mMarkerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mListener.onInfoWindowPageChanged(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnInfoWindowListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnInfoWindowListener");
		}
		
		// register to data updates
		activity.registerReceiver(mDataUpdateReceiver, new IntentFilter(Database.DATA_UPDATED));
		
		// bind to service
		Intent service = new Intent(activity, DataService.class);
		activity.bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDetach() {
		mDataService = null;
		getActivity().unbindService(mServiceConnection);
		getActivity().unregisterReceiver(mDataUpdateReceiver);
		
		super.onDetach();
		mListener = null;
	}
	
	private BroadcastReceiver mDataUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mMarkerAdapter.notifyDatabaseChanged();
		}
	};
	
	public interface OnInfoWindowListener {
		public void onInfoWindowStateChanged(boolean visible, int height, int width);
		public void onInfoWindowPageChanged(int position);
	}

	public void setNode(Node n, int position) {
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
			mListener.onInfoWindowStateChanged(false, 0, 0);
		} else {
			mLayout.setVisibility(View.VISIBLE);
			mListener.onInfoWindowStateChanged(true, mLayout.getHeight(), mLayout.getWidth());
			mViewPager.setCurrentItem(position, true);
		}
	}
	
	private class MarkerPagerAdapter extends FragmentStatePagerAdapter {
		private int mCount = 0;
		
		public MarkerPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			try {
				if (mDataService == null) throw new NodeNotFoundException();
				
				Database db = mDataService.getDatabase();
				
				// load node on position X
				Cursor c = db.raw().query(Database.TABLE_NAME_NODES, NodeAdapter.PROJECTION, null, null, null, null, Node.ENDPOINT, position + ",1");
				
				if (c == null) throw new NodeNotFoundException();
				
				Node n = null;
				
				if (c.moveToNext()) {
					n = new Node(getActivity(), c, new NodeAdapter.ColumnsMap());
					c.close();
				} else {
					c.close();
					throw new NodeNotFoundException();
				}
				
				return MarkerItemFragment.newInstance(n);
			} catch (NodeNotFoundException ex) {
				return null;
			}
		}

		@Override
		public int getCount() {
			return mCount;
		}
		
		public void notifyDatabaseChanged() {
			if (mDataService == null) {
				mCount = 0;
			} else {
				mCount = mDataService.getDatabase().getCount();
			}
			notifyDataSetChanged();
		}
	}
}
