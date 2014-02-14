package de.tubs.ibr.dtn.ruralexplorer;

import de.tubs.ibr.dtn.api.SingletonEndpoint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NodeInfoFragment extends Fragment {

	private TextView mInfoTitle = null;
	
	public static NodeInfoFragment newInstance(Node n) {
		NodeInfoFragment f = new NodeInfoFragment();
		Bundle args = new Bundle();
		args.putSerializable("node", n);
		f.setArguments(args);
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
		return v;
	}
	
	public void update() {
		if (mInfoTitle == null) return;
		Node n = (Node)getArguments().getSerializable("node");
		mInfoTitle.setText(n.getEndpoint().toString());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// register to update intents
		IntentFilter filter = new IntentFilter(Database.DATA_UPDATED);
		activity.registerReceiver(mUpdateReceiver, filter);
	}

	@Override
	public void onDetach() {
		// unregister from update intents
		getActivity().unregisterReceiver(mUpdateReceiver);
		
		super.onDetach();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		update();
	}
	
	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Database.DATA_UPDATED.equals(action)) {
				// update nodes
				SingletonEndpoint endpoint = intent.getParcelableExtra(CommService.EXTRA_ENDPOINT);
				
				// get current node
				Node n = (Node)getArguments().getSerializable("node");

				if (n.equals(endpoint)) {
					// set location if available
					if (intent.hasExtra(LocationService.EXTRA_LOCATION)) {
						Location l = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
						n.setLocation(l);
					}
				}
			}
		}
	};
}
