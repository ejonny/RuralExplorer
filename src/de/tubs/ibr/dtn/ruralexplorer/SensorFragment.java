package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;
import de.tubs.ibr.dtn.ruralexplorer.data.SensorData;

public class SensorFragment extends Fragment {
	
	private Node mNode = null;
	private TextView mTemperature = null;
	private TextView mPressure = null;
	
	public static SensorFragment newInstance(Node n) {
		SensorFragment f = new SensorFragment();
		f.mNode = n;
		return f;
	}
	
	public SensorFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_sensor_data, container, false);
		mTemperature = (TextView)v.findViewById(R.id.text_temperature);
		mPressure = (TextView)v.findViewById(R.id.text_pressure);
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
		
		if ((mTemperature == null) || (n == null)) return;
		
		SensorData sensors = n.getSensor();
		
		if (sensors.hasPressure()) {
			mPressure.setText(String.format(getString(R.string.data_unit_pressure), sensors.getPressure()));
		} else {
			mPressure.setText(getString(R.string.data_na));
		}
		
		if (sensors.hasTemperature()) {
			mTemperature.setText(String.format(getString(R.string.data_unit_degree_celsius), sensors.getTemperature()));
		} else {
			mTemperature.setText(getString(R.string.data_na));
		}
	}
}
