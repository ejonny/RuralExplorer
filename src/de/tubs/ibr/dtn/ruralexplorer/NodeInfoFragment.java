
package de.tubs.ibr.dtn.ruralexplorer;

import java.util.LinkedList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeNotFoundException;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class NodeInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<LinkedList<Node>> {

	@SuppressWarnings("unused")
	private static final String TAG = "MarkerFragment";
	
	private static final int NODE_LOADER_ID = 4;

	private Node mNode = null;
	private MarkerPagerAdapter mMarkerPagerAdapter = null;
	private ViewPager mViewPager = null;

	private OnWindowChangedListener mListener = null;
	private DataService mDataService = null;
	
	public interface OnWindowChangedListener {
		public void onMarkerWindowChanged(boolean visible);
		public void onMarkerNodeSelected(Node n);
	}

	public static NodeInfoFragment newInstance() {
		NodeInfoFragment fragment = new NodeInfoFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public NodeInfoFragment() {
		// Required empty public constructor
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDataService = ((DataService.LocalBinder) service).getService();
			
			getLoaderManager().initLoader(NODE_LOADER_ID,  null, NodeInfoFragment.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			getLoaderManager().destroyLoader(NODE_LOADER_ID);
			mDataService = null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create a marker pager adapter
		mMarkerPagerAdapter = new MarkerPagerAdapter(getFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_node_info, container, false);

		mViewPager = (ViewPager) v.findViewById(R.id.info_pager);
		mViewPager.setAdapter(mMarkerPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				NodeItemFragment f = (NodeItemFragment)mMarkerPagerAdapter.getItem(position);
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
		getLoaderManager().destroyLoader(NODE_LOADER_ID);

		super.onDetach();
		mListener = null;
	}

	public Node getNode() {
		return mNode;
	}

	public void bind(Node n) {
		// stop if nothing has changed
		boolean animate = ((n != null) && (mNode == null)) || ((n == null) && (mNode != null));
		
		mNode = n;
		
		if (n == null) {
			if (animate) mListener.onMarkerWindowChanged(false);
		} else {
			if (animate) mListener.onMarkerWindowChanged(true);
			
			try {
				// set pager to position
				mViewPager.setCurrentItem(mMarkerPagerAdapter.getPosition(n), true);
			} catch (NodeNotFoundException e) {
				// node not found
			}
		}
	}

	private class MarkerPagerAdapter extends FragmentStatePagerAdapter {
		private LinkedList<Node> mData = null;

		public MarkerPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (mDataService == null) return null;

			if (mData == null) return null;

			if (mData.size() < position+1) return null;
			
			return NodeItemFragment.newInstance( mData.get(position) );
		}
		
		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public int getCount() {
			if (mData == null) return 0;
			return mData.size();
		}
		
		public int getPosition(Node n) throws NodeNotFoundException {
			if (n == null)
				throw new NodeNotFoundException();
			
			if (mDataService == null)
				throw new NodeNotFoundException();
			
			if (mData == null)
				throw new NodeNotFoundException();
			
			for (Node node : mData) {
				n.equals(node);
			}
			
			throw new NodeNotFoundException();
		}
		
		public void swapData(LinkedList<Node> data) {
			mData = data;
			notifyDataSetChanged();
		}
	}

	@Override
	public Loader<LinkedList<Node>> onCreateLoader(int id, Bundle args) {
		return new NodeLoader(getActivity(), mDataService);
	}

	@Override
	public void onLoadFinished(Loader<LinkedList<Node>> loader, LinkedList<Node> data) {
		mMarkerPagerAdapter.swapData(data);
	}

	@Override
	public void onLoaderReset(Loader<LinkedList<Node>> data) {
		mMarkerPagerAdapter.swapData(null);
	}
}
