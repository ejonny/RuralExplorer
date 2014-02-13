package de.tubs.ibr.dtn.ruralexplorer;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class Node {
	public enum Type {
		GENERIC,
		ANDROID,
		INGA,
		PI
	};
	
	private SingletonEndpoint mEndpoint = null;
	private Type mType = Type.GENERIC;
	private Location mLocation = null;
	
	private Marker mMarker = null;
	private GoogleMap mMap = null;
	private Circle mCircle = null;
	
	private Node(GoogleMap map) {
		mMap = map;
	}
	
	public static Node create(GoogleMap map) {
		if (map == null) return null;
		return new Node(map);
	}
	
	public void setHightlight(boolean val) {
		if (mLocation == null) return;
		
		LatLng position = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
		
		if (val) {
			if (mCircle != null) return;
			mCircle = mMap.addCircle(
					new CircleOptions()
						.center(position)
						.radius(300)
						.fillColor(R.color.light_blue)
						.strokeColor(R.color.dark_blue)
						.strokeWidth(2)
					);
		} else {
			if (mCircle == null) return;
			mCircle.remove();
		}
	}
	
	public Type getType() {
		return mType;
	}
	
	public void setType(Type type) {
		mType = type;
		if (mMarker != null) {
			mMarker.setIcon(Node.getBitmap(mType));
		}
	}
	
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation(Location location) {
		if (location == null) {
			if (mMarker == null) return;
			
			// clear marker
			mMarker.remove();
		} else {
			LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
			
			if (mMarker == null) {
				mMarker = mMap.addMarker(
					new MarkerOptions()
							.position(position)
							.icon(Node.getBitmap(mType))
							.anchor(0.5f, 0.5f)
						);
			} else {
				mMarker.setPosition(position);
			}
		}
		
		mLocation = location;
	}
	
	public Marker getMarker() {
		return mMarker;
	}
	
	public void setMarker(Marker marker) {
		mMarker = marker;
	}

	public SingletonEndpoint getEndpoint() {
		if (mEndpoint == null) return new SingletonEndpoint("dtn:none");
		return mEndpoint;
	}

	public void setEndpoint(SingletonEndpoint endpoint) {
		mEndpoint = endpoint;
	}

	@Override
	public boolean equals(Object o) {
		return getEndpoint().equals(o);
	}

	@Override
	public int hashCode() {
		return getEndpoint().hashCode();
	}

	@Override
	public String toString() {
		return getEndpoint().toString();
	}
	
	private static BitmapDescriptor getBitmap(Node.Type t) {
		switch (t) {
			case ANDROID:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_android);
			case INGA:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_inga);
			case PI:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_raspberrypi);
			default:
				return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
		}
	}
}
