package de.tubs.ibr.dtn.ruralexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;

public class MarkerItemFragment extends Fragment {

	private Node mNode = null;
	private TextView mInfoTitle = null;
	private ImageView mInfoIcon = null;
	private ImageButton mInfoButton = null;
	
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
		mInfoTitle = (TextView)v.findViewById(R.id.info_title);
		mInfoIcon = (ImageView)v.findViewById(R.id.device_icon);
		mInfoButton = (ImageButton)v.findViewById(R.id.info_button);
		
		mInfoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StatsFragment f = (StatsFragment)getFragmentManager().findFragmentById(R.id.info_extended);
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

	public void bind(Node n) {
		mNode = n;
		
		if (mInfoTitle == null) return;
		mInfoTitle.setText(n.getEndpoint().toString());
		
		switch (n.getType()) {
			case ANDROID:
				mInfoIcon.setImageResource(R.drawable.ic_android);
				break;
			case INGA:
				mInfoIcon.setImageResource(R.drawable.ic_inga);
				break;
			case PI:
				mInfoIcon.setImageResource(R.drawable.ic_raspberrypi);
				break;
			default:
				mInfoIcon.setImageResource(R.drawable.ic_node);
				break;
		}
	}
}
