package de.tubs.ibr.dtn.ruralexplorer;

import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class NodeManager {
	private HashSet<Node> mNodes = new HashSet<Node>();
	private GoogleMap mMap = null;
	private Context mContext = null;
	
	public NodeManager(Context context, GoogleMap map) {
		mMap = map;
		mContext = context;
	}
	
	public Node get(SingletonEndpoint endpoint) {
		// check if the node already exists
		for (Node n : mNodes) {
			if (endpoint.equals( n.getEndpoint() )) {
				return n;
			}
		}
		
		Node n = Node.create(mMap);
		n.setEndpoint(endpoint);
		mNodes.add(n);
		return n;
	}
	
	public void onStart() {
		// register to update intents
		IntentFilter filter = new IntentFilter(ExplorerService.DATA_UPDATED);
		mContext.registerReceiver(mUpdateReceiver, filter);
		
		// load all nodes from the database and display them
	}
	
	public void onStop() {
		// unregister from update intents
		mContext.unregisterReceiver(mUpdateReceiver);
	}
	
	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (ExplorerService.DATA_UPDATED.equals(action)) {
				// update nodes
				SingletonEndpoint endpoint = intent.getParcelableExtra(ExplorerService.EXTRA_ENDPOINT);
				
				// get node
				Node n = NodeManager.this.get(endpoint);
				
				// set location if available
				if (intent.hasExtra(ExplorerService.EXTRA_LOCATION)) {
					Location l = intent.getParcelableExtra(ExplorerService.EXTRA_LOCATION);
					n.setLocation(l);
				}
			}
		}
	};
}
