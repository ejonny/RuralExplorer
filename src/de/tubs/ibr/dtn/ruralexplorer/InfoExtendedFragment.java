
package de.tubs.ibr.dtn.ruralexplorer;

import de.tubs.ibr.dtn.ruralexplorer.backend.Node;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class InfoExtendedFragment extends Fragment {
	
	private FrameLayout mLayout = null;
	
	public static InfoExtendedFragment newInstance() {
		InfoExtendedFragment fragment = new InfoExtendedFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public InfoExtendedFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_info_extended, container, false);
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
