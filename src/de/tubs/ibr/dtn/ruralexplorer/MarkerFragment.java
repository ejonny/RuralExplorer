
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeNotFoundException;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class MarkerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	@SuppressWarnings("unused")
	private static final String TAG = "MarkerFragment";
	
	private static final int MARKER_LOADER_ID = 1;

	private FrameLayout mLayout = null;

	private Node mNode = null;
	private NodeAdapter mNodeAdapter = null;
	private MarkerPagerAdapter mMarkerPagerAdapter = null;
	private ViewPager mViewPager = null;

	private OnWindowChangedListener mListener = null;
	private DataService mDataService = null;
	
	public interface OnWindowChangedListener {
		public void onMarkerWindowChanged(boolean visible, int height, int width);
		public void onMarkerNodeSelected(Node n);
	}

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
			mDataService = ((DataService.LocalBinder) service).getService();
			
			getLoaderManager().initLoader(MARKER_LOADER_ID,  null, MarkerFragment.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			getLoaderManager().destroyLoader(MARKER_LOADER_ID);
			mDataService = null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create an empty adapter we will use to display the loaded data.
		mNodeAdapter = new NodeAdapter(getActivity(), null, new NodeAdapter.ColumnsMap());
		
		// create a marker pager adapter
		mMarkerPagerAdapter = new MarkerPagerAdapter(getFragmentManager(), mNodeAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_marker, container, false);
		mLayout = (FrameLayout) v.findViewById(R.id.node_fragment_layout);

		mViewPager = (ViewPager) v.findViewById(R.id.info_pager);
		mViewPager.setAdapter(mMarkerPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				MarkerItemFragment f = (MarkerItemFragment)mMarkerPagerAdapter.getItem(position);
				if (f != null) {
					mNode = f.getNode();
					mListener.onMarkerNodeSelected(f.getNode());
				}
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
		getLoaderManager().destroyLoader(MARKER_LOADER_ID);

		super.onDetach();
		mListener = null;
	}
	
	public Node getNode() {
		return mNode;
	}

	public void bind(Node n) {
		mNode = n;
		
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
			mListener.onMarkerWindowChanged(false, 0, 0);
		} else {
			mLayout.setVisibility(View.VISIBLE);
			mListener.onMarkerWindowChanged(true, mLayout.getHeight(), mLayout.getWidth());
			
			try {
				// set pager to position
				mViewPager.setCurrentItem(mMarkerPagerAdapter.getPosition(n), true);
			} catch (NodeNotFoundException e) {
				// node not found
			}
		}
	}

	private class MarkerPagerAdapter extends FragmentStatePagerAdapter {
		private NodeAdapter mAdapter = null;

		public MarkerPagerAdapter(FragmentManager fm, NodeAdapter adapter) {
			super(fm);
			mAdapter = adapter;
			mAdapter.registerDataSetObserver(mObserver);
		}

		private DataSetObserver mObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				notifyDataSetChanged();
				super.onChanged();
			}

			@Override
			public void onInvalidated() {
				notifyDataSetChanged();
				super.onInvalidated();
			}
		};

		@Override
		public Fragment getItem(int position) {
			try {
				if (mDataService == null)
					throw new NodeNotFoundException();

				Cursor c = mAdapter.getCursor();

				if (c == null)
					throw new NodeNotFoundException();

				// move to right position
				if (!c.moveToPosition(position)) throw new NodeNotFoundException();
				
				Node n = new Node(getActivity(), c, new NodeAdapter.ColumnsMap());
				return MarkerItemFragment.newInstance(n);
			} catch (NodeNotFoundException ex) {
				return null;
			}
		}
		
		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public int getCount() {
			return mAdapter.getCount();
		}
		
		public int getPosition(Node n) throws NodeNotFoundException {
			if (mDataService == null)
				throw new NodeNotFoundException();
			
			Cursor c = mAdapter.getCursor();

			if (c == null)
				throw new NodeNotFoundException();

			// move to initial position
			c.moveToPosition(-1);
			
			int ret = 0;
			
			while (c.moveToNext()) {
				Node node = new Node(getActivity(), c, new NodeAdapter.ColumnsMap());
				if (node.equals(n)) {
					return ret;
				}
				ret++;
			}
			
			throw new NodeNotFoundException();
		}
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
		mNodeAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> data) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mNodeAdapter.swapCursor(null);
	}
}
