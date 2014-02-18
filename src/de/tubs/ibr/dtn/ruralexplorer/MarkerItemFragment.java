package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;

public class MarkerItemFragment extends Fragment {

	private Node mNode = null;
	private TextView mInfoTitle = null;
	private ImageView mInfoIcon = null;
	
	public static MarkerItemFragment newInstance(Node n) {
		MarkerItemFragment f = new MarkerItemFragment();
		f.mNode = n;
		return f;
	}
	
	public MarkerItemFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_marker_item, container, false);
		mInfoTitle = (TextView)v.findViewById(R.id.marker_title);
		mInfoIcon = (ImageView)v.findViewById(R.id.marker_icon);
		
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StatsFragment f = (StatsFragment)getFragmentManager().findFragmentById(R.id.stats_fragment);
				f.setNode(mNode);
			}
		});
		
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		bind(mNode);
	}
	
	public Node getNode() {
		return mNode;
	}

	public void bind(Node n) {
		mNode = n;
		
		if (mInfoTitle == null) return;
		mInfoTitle.setText(n.getEndpoint().toString());
		mInfoIcon.setImageResource(Node.getResource(n.getType()));
	}
}
