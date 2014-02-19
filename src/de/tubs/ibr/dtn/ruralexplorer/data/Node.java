package de.tubs.ibr.dtn.ruralexplorer.data;

import java.io.Serializable;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.ruralexplorer.R;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter.ColumnsMap;

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
	private RuralLocation mLocation = null;
	
	public Node(Node.Type t, SingletonEndpoint endpoint) {
		mType = t;
		mEndpoint = endpoint;
		mLocation = new RuralLocation();
	}
	
	public Node(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap)
	{
		this.mId = cursor.getLong(cmap.mColumnId);
		this.mEndpoint = new SingletonEndpoint( cursor.getString(cmap.mColumnEndpoint) );
		this.mType = Type.valueOf( cursor.getString(cmap.mColumnType) );
		this.mLocation = new RuralLocation(context, cursor, cmap);
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
	
	public void setLocation(RuralLocation l) {
		mLocation = l;
	}
	
	public RuralLocation getLocation() {
		return mLocation;
	}
	
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
