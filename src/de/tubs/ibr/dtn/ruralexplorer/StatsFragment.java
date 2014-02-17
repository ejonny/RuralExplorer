
package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;

public class StatsFragment extends Fragment {
	
	private FrameLayout mLayout = null;
	
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
		mLayout = (FrameLayout)v.findViewById(R.id.extended_fragment_layout);
		return v;
	}
	
	public void setNode(Node n) {
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
		} else {
			mLayout.setVisibility(View.VISIBLE);
		}
	}
}
