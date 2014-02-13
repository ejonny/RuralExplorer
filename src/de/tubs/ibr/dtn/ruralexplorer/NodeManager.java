package de.tubs.ibr.dtn.ruralexplorer;

import java.util.HashSet;

import com.google.android.gms.maps.GoogleMap;

import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class NodeManager {
	private HashSet<Node> mNodes = new HashSet<Node>();
	private GoogleMap mMap = null;
	
	public NodeManager(GoogleMap map) {
		mMap = map;
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
}
