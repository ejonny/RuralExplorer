package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NodeInfoFragment extends Fragment {
	
	public static NodeInfoFragment newInstance(Node n) {
		NodeInfoFragment f = new NodeInfoFragment();
		Bundle args = new Bundle();
		args.putSerializable("node", n);
		f.setArguments(args);
		return f;
	}
	
	public NodeInfoFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_info_node, container, false);
		//mLayout = (FrameLayout)v.findViewById(R.id.node_fragment_layout);
		//mViewPager = (ViewPager)v.findViewById(R.id.info_pager);
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
