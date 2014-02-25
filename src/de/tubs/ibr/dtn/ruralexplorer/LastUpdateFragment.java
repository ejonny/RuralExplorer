package de.tubs.ibr.dtn.ruralexplorer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class LastUpdateFragment extends Fragment {
	
	private Node mNode = null;
	private TextView mLastUpdateText = null;
	
	public static LastUpdateFragment newInstance(Node n) {
		LastUpdateFragment f = new LastUpdateFragment();
		f.mNode = n;
		return f;
	}
	
	public LastUpdateFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_lastupdate_data, container, false);
		mLastUpdateText = (TextView)v.findViewById(R.id.text_lastupdate);
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

	@SuppressLint("SimpleDateFormat")
	public void bind(Node n) {
		mNode = n;
		
		if ((mLastUpdateText == null) || (n == null)) return;
		
		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date d = n.getLastUpdate();
		
		if (d != null) {
			mLastUpdateText.setText(String.format(getString(R.string.data_unit_date), formatter.format(d)));
		} else {
			mLastUpdateText.setText(getString(R.string.data_na));
		}
	}
}
