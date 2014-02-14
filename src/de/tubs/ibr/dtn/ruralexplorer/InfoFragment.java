
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class InfoFragment extends Fragment implements NodeManager.NodeManagerListener {

	private FrameLayout mLayout = null;

	private InfoPagerAdapter mInfoAdapter = null;
	private ViewPager mViewPager = null;
	
	private OnInfoWindowListener mListener = null;

	public static InfoFragment newInstance() {
		InfoFragment fragment = new InfoFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public InfoFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInfoAdapter = new InfoPagerAdapter(getFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_info, container, false);
		mLayout = (FrameLayout)v.findViewById(R.id.node_fragment_layout);
		
		mViewPager = (ViewPager)v.findViewById(R.id.info_pager);
		mViewPager.setAdapter(mInfoAdapter);
		
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
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	public interface OnInfoWindowListener {
		public void onInfoWindowStateChanged(boolean visible, int height, int width);
	}

	public void setNode(Node n) {
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
			mListener.onInfoWindowStateChanged(false, 0, 0);
		} else {
			mLayout.setVisibility(View.VISIBLE);
			mListener.onInfoWindowStateChanged(true, mLayout.getHeight(), mLayout.getWidth());
		}
	}
	
	public void setNodeManager(NodeManager nm) {
		mInfoAdapter.setNodeManager(nm);
	}

	@Override
	public void onNodeAdded(Node n) {
		mInfoAdapter.add(n);
	}

	@Override
	public void onNodeRemoved(Node n) {
		mInfoAdapter.remove(n);
	}

	@Override
	public void onNodeUpdated(Node n) {
		mInfoAdapter.updated(n);
	}
}
