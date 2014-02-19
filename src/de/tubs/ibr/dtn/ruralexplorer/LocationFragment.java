package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class LocationFragment extends Fragment {
	
	private Node mNode = null;
	private TextView mLocationText = null;
	
	public static LocationFragment newInstance(Node n) {
		LocationFragment f = new LocationFragment();
		f.mNode = n;
		return f;
	}
	
	public LocationFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_location_data, container, false);
		mLocationText = (TextView)v.findViewById(R.id.text_location);
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
		
		if ((mLocationText == null) || (n == null)) return;
		
		LocationData l = n.getLocation();
		
		if (l.hasLatitude() && l.hasLongitude()) {
			mLocationText.setText(String.format(getString(R.string.data_unit_latlng), l.getLatitude(), l.getLongitude()));
		} else {
			mLocationText.setText(getString(R.string.data_na));
		}
	}
}
