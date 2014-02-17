package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.io.Serializable;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class Node implements Serializable, Comparable<Node> {
	/**
	 * serial ID
	 */
	private static final long serialVersionUID = -3021339683598377878L;
	
	private static final String TAG = "Node";

	public static final String ID = BaseColumns._ID;
	public static final String ENDPOINT = "endpoint";
	public static final String TYPE = "type";

	public enum Type {
		GENERIC,
		ANDROID,
		INGA,
		PI
	};
	
	private Long mId = null;
	private final SingletonEndpoint mEndpoint;
	private final Type mType;
	private NodeLocation mLocation = null;
	
	public Node(Node.Type t, SingletonEndpoint endpoint) {
		mType = t;
		mEndpoint = endpoint;
		mLocation = new NodeLocation();
	}
	
	public Node(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap)
	{
		this.mId = cursor.getLong(cmap.mColumnId);
		this.mEndpoint = new SingletonEndpoint( cursor.getString(cmap.mColumnEndpoint) );
		this.mType = Type.valueOf( cursor.getString(cmap.mColumnType) );
		this.mLocation = new NodeLocation(context, cursor, cmap);
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}
	
//	private Marker mMarker = null;
//	private GoogleMap mMap = null;
	
//	private Node(GoogleMap map) {
//		mMap = map;
//	}
//	
//	public static Node create(GoogleMap map) {
//		if (map == null) return null;
//		return new Node(map);
//	}
	
//	public void setHightlight(boolean val) {
//		if (mLocation == null) return;
//		
//		LatLng position = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
//		
//		if (val) {
//			if (mCircle != null) return;
//			mCircle = mMap.addCircle(
//					new CircleOptions()
//						.center(position)
//						.radius(300)
//						.fillColor(R.color.light_blue)
//						.strokeColor(R.color.dark_blue)
//						.strokeWidth(2)
//					);
//		} else {
//			if (mCircle == null) return;
//			mCircle.remove();
//		}
//	}
	
	public Type getType() {
		return mType;
	}
	
	public void setLocation(NodeLocation l) {
		mLocation = l;
	}
	
	public NodeLocation getLocation() {
		return mLocation;
//		if (location == null) {
//			if (mMarker == null) return;
//			
//			// clear marker
//			mMarker.remove();
//		} else {
//			LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
//			
//			if (mMarker == null) {
//				mMarker = mMap.addMarker(
//					new MarkerOptions()
//							.position(position)
//							.icon(Node.getBitmap(mType))
//							.anchor(0.5f, 0.5f)
//							.flat(true)
//						);
//			} else {
//				mMarker.setPosition(position);
//			}
//		}
	}
	
//	public Marker getMarker() {
//		return mMarker;
//	}
//	
//	public void setMarker(Marker marker) {
//		mMarker = marker;
//	}

	public SingletonEndpoint getEndpoint() {
		if (mEndpoint == null) return new SingletonEndpoint("dtn:none");
		return mEndpoint;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return getEndpoint().equals(((Node)o).getEndpoint());
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return getEndpoint().hashCode();
	}

	@Override
	public String toString() {
		return getEndpoint().toString();
	}
	
//	public boolean equals(Marker m) {
//		return mMarker.equals(m);
//	}
//	
//	private static BitmapDescriptor getBitmap(Node.Type t) {
//		switch (t) {
//			case ANDROID:
//				return BitmapDescriptorFactory.fromResource(R.drawable.ic_android);
//			case INGA:
//				return BitmapDescriptorFactory.fromResource(R.drawable.ic_inga);
//			case PI:
//				return BitmapDescriptorFactory.fromResource(R.drawable.ic_raspberrypi);
//			default:
//				return BitmapDescriptorFactory.fromResource(R.drawable.ic_node);
//		}
//	}

	@Override
	public int compareTo(Node another) {
		if (mEndpoint == null) return -1;
		return mEndpoint.compareTo(another.mEndpoint);
	}
}
