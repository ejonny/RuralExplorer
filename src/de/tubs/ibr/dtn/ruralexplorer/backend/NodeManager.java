package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.util.ArrayList;
import java.util.Collections;
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
	
	public int getIndex(Node n) throws NodeNotFoundException {
		for (int i = 0; i < mNodes.size(); ++i) {
			if (mNodes.get(i).equals( n )) {
				return i;
			}
		}
		
		throw new NodeNotFoundException();
	}
	
	public Node get(Marker m) throws NodeNotFoundException {
		for (Node n : mNodes) {
			if (n.equals( m )) {
				return n;
			}
		}
		
		throw new NodeNotFoundException();
	}
	
	public Node get(int position) throws NodeNotFoundException {
		try {
			return mNodes.get(position);
		} catch (IndexOutOfBoundsException ex) {
			throw new NodeNotFoundException();
		}
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
		
		Collections.sort(mNodes);
		
		for (NodeManagerListener l : mListener) {
			l.onNodeAdded(n);
		}
		
		return n;
	}
	
	public void update(Node n) {
		for (NodeManagerListener l : mListener) {
			l.onNodeUpdated(n);
		}
	}
	
	public void onStart() {
		// register to update intents
		IntentFilter filter = new IntentFilter(Database.DATA_UPDATED);
		mContext.registerReceiver(mUpdateReceiver, filter);
		
		// load all nodes from the database and display them
		// TODO: announce all nodes to the listener

		Location l = new Location("fake");
		l.setLatitude(52.273535);
		l.setLongitude(10.524711);
		
		Node n = get(new SingletonEndpoint("dtn://test1"));
		Location l1 = new Location(l);
		l1.setLatitude(l.getLatitude() + 0.005);
		n.setType(Node.Type.INGA);
		n.setLocation(l1);
		update(n);
		
		n = get(new SingletonEndpoint("dtn://test2"));
		Location l2 = new Location(l);
		l2.setLatitude(l.getLatitude() - 0.005);
		n.setType(Node.Type.PI);
		n.setLocation(l2);
		update(n);
		
		n = get(new SingletonEndpoint("dtn://test3"));
		Location l3 = new Location(l);
		l3.setLongitude(l.getLongitude() - 0.005);
		n.setType(Node.Type.ANDROID);
		n.setLocation(l3);
		update(n);
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
