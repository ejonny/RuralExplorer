package de.tubs.ibr.dtn.ruralexplorer;

import de.tubs.ibr.dtn.ruralexplorer.backend.Node;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class NodeInfoFragment extends Fragment {

	private Node mNode = null;
	private TextView mInfoTitle = null;
	private ImageView mInfoIcon = null;
	private ImageButton mInfoButton = null;
	
	public static NodeInfoFragment newInstance(Node n) {
		NodeInfoFragment f = new NodeInfoFragment();
		f.mNode = n;
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
		mInfoTitle = (TextView)v.findViewById(R.id.info_title);
		mInfoIcon = (ImageView)v.findViewById(R.id.device_icon);
		mInfoButton = (ImageButton)v.findViewById(R.id.info_button);
		
		mInfoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), NodeInfoActivity.class);
				getActivity().startActivity(i);
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
