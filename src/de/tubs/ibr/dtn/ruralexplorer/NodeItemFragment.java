package de.tubs.ibr.dtn.ruralexplorer;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class NodeItemFragment extends Fragment {

	private Node mNode = null;
	private TextView mInfoTitle = null;
	private TextView mInfoDescription = null;
	private TextView mInfoDistance = null;
	private TextView mInfoDistanceLabel = null;
	private ImageView mInfoIcon = null;
	
	public static NodeItemFragment newInstance(Node n) {
		NodeItemFragment f = new NodeItemFragment();
		f.mNode = n;
		return f;
	}
	
	public NodeItemFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_node_item, container, false);
		mInfoTitle = (TextView)v.findViewById(R.id.marker_title);
		mInfoDescription = (TextView)v.findViewById(R.id.marker_description);
		mInfoIcon = (ImageView)v.findViewById(R.id.marker_icon);
		mInfoDistance = (TextView)v.findViewById(R.id.marker_distance);
		mInfoDistanceLabel = (TextView)v.findViewById(R.id.marker_distance_title);
		
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StatsFragment f = (StatsFragment)getFragmentManager().findFragmentById(R.id.stats_fragment);
				f.bind(mNode);
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
		
		if ((mInfoTitle == null) || (n == null)) return;
		
		if (n.hasName()) {
			mInfoTitle.setText(n.getName());
		} else {
			mInfoTitle.setText(getResources().getString(R.string.name_anonymous));
		}
		
		mInfoIcon.setImageResource(Node.getResource(n.getType()));
		
		mInfoDescription.setText(n.getEndpoint().toString());
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, -1);
		
		if (mNode.hasDistance() && mNode.getLastUpdate().after(c.getTime())) {
			mInfoDistance.setText(LocationData.formatDistance(getString(R.string.data_unit_distance), n.getDistance()));
			mInfoDistance.setVisibility(View.VISIBLE);
			mInfoDistanceLabel.setVisibility(View.VISIBLE);
		} else {
			mInfoDistanceLabel.setVisibility(View.INVISIBLE);
			mInfoDistance.setVisibility(View.INVISIBLE);
		}
	}
}
