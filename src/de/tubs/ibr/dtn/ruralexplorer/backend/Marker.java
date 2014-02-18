package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.io.Serializable;

import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node.Type;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Marker implements Serializable, Comparable<Marker> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4037293627087182386L;
	
	public static final String ID = BaseColumns._ID;
	public static final String NODE_ID = "node";
	
	private Long mId = null;
	private Long mNodeId = null;
	
	public Marker(Node n) {
		mId = null;
		mNodeId = null;
	}
	
	public Marker(Context context, Cursor cursor, MarkerAdapter.ColumnsMap cmap)
	{
		this.mId = cursor.getLong(cmap.mColumnId);
		this.mNodeId = cursor.getLong(cmap.mColumnNodeId);
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}
	
	@Override
	public int compareTo(Marker another) {
		if (mId == null) return -1;
		return mId.compareTo(another.mId);
	}
}
