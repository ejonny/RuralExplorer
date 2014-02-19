package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class AccelerationFragment extends Fragment {
	
	private Node mNode = null;
	private TextView mAccelerationText = null;
	
	public static AccelerationFragment newInstance(Node n) {
		AccelerationFragment f = new AccelerationFragment();
		f.mNode = n;
		return f;
	}
	
	public AccelerationFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_acceleration_data, container, false);
		mAccelerationText = (TextView)v.findViewById(R.id.text_acceleration);
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
		
		if ((mAccelerationText == null) || (n == null)) return;
		
		float[] data = n.getAcceleration().getData();
		
		mAccelerationText.setText(String.format(getString(R.string.data_unit_acceleration), data[0], data[1], data[2]));
	}

}
