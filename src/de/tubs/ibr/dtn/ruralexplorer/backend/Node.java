package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Base64;
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
	public static final String LOCATION = "location";

	public enum Type {
		GENERIC,
		ANDROID,
		INGA,
		PI
	};
	
	private Long mId = null;
	private final SingletonEndpoint mEndpoint;
	private final Type mType;
	private Location mLocation = null;
	
	public Node(Node.Type t, SingletonEndpoint endpoint) {
		mType = t;
		mEndpoint = endpoint;
	}
	
	public Node(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap)
	{
		this.mId = cursor.getLong(cmap.mColumnId);
		this.mEndpoint = new SingletonEndpoint( cursor.getString(cmap.mColumnEndpoint) );
		this.mType = Type.valueOf( cursor.getString(cmap.mColumnType) );
		
//		String locationData = cursor.getString(cmap.mColumnLocation);
//		byte [] data = Base64.decode( locationData, Base64.DEFAULT );
//		
//		try {
//			ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream(  data ) );
//			mLocation  = (Location)ois.readObject();
//			ois.close();
//		} catch (StreamCorruptedException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
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
	
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation(Location location) {
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
		
		mLocation = location;
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
