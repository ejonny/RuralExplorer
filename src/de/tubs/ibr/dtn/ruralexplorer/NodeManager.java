package de.tubs.ibr.dtn.ruralexplorer;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class NodeManager {
	private ArrayList<Node> mNodes = new ArrayList<Node>();
	private GoogleMap mMap = null;
	private Context mContext = null;
	private HashSet<NodeManagerListener> mListener = new HashSet<NodeManagerListener>();
	
	public interface NodeManagerListener {
		void onNodeAdded(Node n);
		void onNodeRemoved(Node n);
		void onNodeUpdated(Node n);
	}
	
	public void addListener(NodeManagerListener listener) {
		mListener.add(listener);
	}
	
	public void removeListener(NodeManagerListener listener) {
		mListener.remove(listener);
	}
	
	public NodeManager(Context context, GoogleMap map) {
		mMap = map;
		mContext = context;
	}
	
	public Node get(Marker m) throws NodeNotFoundException {
		for (Node n : mNodes) {
			if (n.equals( m )) {
				return n;
			}
		}
		
		throw new NodeNotFoundException();
	}
	
	public Node get(int position) {
		return mNodes.get(position);
	}
	
	public int getCount() {
		return mNodes.size();
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
		
		for (NodeManagerListener l : mListener) {
			l.onNodeAdded(n);
		}
		
		return n;
	}
	
	public void onStart() {
		// register to update intents
		IntentFilter filter = new IntentFilter(Database.DATA_UPDATED);
		mContext.registerReceiver(mUpdateReceiver, filter);
		
		// load all nodes from the database and display them
		// TODO: announce all nodes to the listener
	}
	
	public void onStop() {
		for (Node n : mNodes) {
			for (NodeManagerListener l : mListener) {
				l.onNodeRemoved(n);
			}
		}
		
		// unregister from update intents
		mContext.unregisterReceiver(mUpdateReceiver);
	}
	
	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Database.DATA_UPDATED.equals(action)) {
				// update nodes
				SingletonEndpoint endpoint = intent.getParcelableExtra(CommService.EXTRA_ENDPOINT);
				
				if (endpoint != null) {
					// get node
					Node n = NodeManager.this.get(endpoint);
					
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
