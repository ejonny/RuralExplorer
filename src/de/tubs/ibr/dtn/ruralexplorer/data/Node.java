package de.tubs.ibr.dtn.ruralexplorer.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.ruralexplorer.R;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;

public class Node implements Serializable, Comparable<Node> {
	/**
	 * serial ID
	 */
	private static final long serialVersionUID = -3021339683598377878L;
	
	private static final String TAG = "Node";
	
	public static final String ID = BaseColumns._ID;
	public static final String ENDPOINT = "endpoint";
	public static final String TYPE = "type";
	public static final String NAME = "name";
	public static final String LAST_UPDATE = "lastupdate";

	public enum Type {
		GENERIC,
		ANDROID,
		INGA,
		PI
	};
	
	private Long mId = null;
	private final SingletonEndpoint mEndpoint;
	private final Type mType;
	private String mName = null;
	private Date mLastUpdate = null;
	private LocationData mLocation = null;
	private SensorData mSensor = null;
	private AccelerationData mAcceleration = null;
	private Float mDistance = null;
	
	public Node(Node.Type t, SingletonEndpoint endpoint) {
		mType = t;
		mEndpoint = endpoint;
		mLastUpdate = null;
		mLocation = new LocationData();
		mSensor = new SensorData();
		mAcceleration = new AccelerationData();
		mDistance = null;
	}
	
	@SuppressLint("SimpleDateFormat")
	public Node(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap)
	{
		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		this.mId = cursor.getLong(cmap.mColumnId);
		this.mEndpoint = new SingletonEndpoint( cursor.getString(cmap.mColumnEndpoint) );
		this.mType = Type.valueOf( cursor.getString(cmap.mColumnType) );
		this.mName = cursor.getString(cmap.mColumnName);
		
		try {
			if (cursor.isNull(cmap.mColumnLastUpdate)) {
				mLastUpdate = null;
			} else {
				mLastUpdate = formatter.parse(cursor.getString(cmap.mColumnLastUpdate));
			}
		} catch (ParseException e) {
			Log.e(TAG, "failed to convert date");
		}
		
		this.mLocation = new LocationData(context, cursor, cmap);
		this.mSensor = new SensorData(context, cursor, cmap);
		this.mAcceleration = new AccelerationData(context, cursor, cmap);
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}
	
	public Type getType() {
		return mType;
	}
	
	public boolean hasName() {
		if (mName != null) {
			return mName.length() > 0;
		}
		return false;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public void setLocation(LocationData l) {
		mLocation = l;
	}
	
	public LocationData getLocation() {
		return mLocation;
	}
	
	public SensorData getSensor() {
		return mSensor;
	}

	public void setSensor(SensorData sensor) {
		mSensor = sensor;
	}

	public AccelerationData getAcceleration() {
		return mAcceleration;
	}

	public void setAcceleration(AccelerationData acceleration) {
		mAcceleration = acceleration;
	}

	public SingletonEndpoint getEndpoint() {
		if (mEndpoint == null) return new SingletonEndpoint("dtn:none");
		return mEndpoint;
	}

	public Date getLastUpdate() {
		return mLastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		mLastUpdate = lastUpdate;
	}
	
	public boolean hasDistance() {
		return mDistance != null;
	}

	public Float getDistance() {
		return mDistance;
	}

	public void setDistance(Float distance) {
		mDistance = distance;
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
	
	public static BitmapDescriptor getBitmap(Node.Type t) {
		switch (t) {
			case ANDROID:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_android);
			case INGA:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_inga);
			case PI:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_raspberrypi);
			default:
				return BitmapDescriptorFactory.fromResource(R.drawable.ic_node);
		}
	}
	
	public static int getResource(Node.Type t) {
		switch (t) {
			case ANDROID:
				return R.drawable.ic_android;
			case INGA:
				return R.drawable.ic_inga;
			case PI:
				return R.drawable.ic_raspberrypi;
			default:
				return R.drawable.ic_node;
		}
	}

	@Override
	public int compareTo(Node another) {
		if (mEndpoint == null) return -1;
		return mEndpoint.compareTo(another.mEndpoint);
	}
}
