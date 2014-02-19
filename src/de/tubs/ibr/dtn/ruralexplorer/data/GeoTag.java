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
import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.ruralexplorer.backend.GeoTagAdapter;

public class GeoTag implements Serializable, Comparable<GeoTag> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5802219880676614539L;
	
	private static final String TAG = "GeoTag";
	
	public static final String ID = BaseColumns._ID;
	public static final String ENDPOINT = "endpoint";
	public static final String SENT_TIME = "senttime";
	public static final String RECV_TIME = "recvtime";
	
	private Long mId = null;
	private Date mSentTime = null;
	private Date mReceivedTime = null;
	private SingletonEndpoint mEndpoint = null;
	private LocationData mLocation = null;
	
	public GeoTag(SingletonEndpoint endpoint) {
		mId = null;
		mEndpoint = endpoint;
		mLocation = null;
		mSentTime = null;
		mReceivedTime = new Date();
	}
	
	@SuppressLint("SimpleDateFormat")
	public GeoTag(Context context, Cursor cursor, GeoTagAdapter.ColumnsMap cmap)
	{
		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		this.mId = cursor.getLong(cmap.mColumnId);
		
		try {
			this.mSentTime = formatter.parse(cursor.getString(cmap.mColumnSentTime));
			this.mReceivedTime = formatter.parse(cursor.getString(cmap.mColumnRecvTime));
		} catch (ParseException e) {
			Log.e(TAG, "failed to convert date");
		}
		
		this.mEndpoint = new SingletonEndpoint( cursor.getString(cmap.mColumnEndpoint) );
		this.mLocation = new LocationData(context, cursor, cmap);
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}
	
	public SingletonEndpoint getEndpoint() {
		return mEndpoint;
	}

	public void setEndpoint(SingletonEndpoint endpoint) {
		mEndpoint = endpoint;
	}

	public LocationData getLocation() {
		return mLocation;
	}

	public void setLocation(LocationData location) {
		mLocation = location;
	}

	public Date getSentTime() {
		return mSentTime;
	}

	public void setSentTime(Date sentTime) {
		mSentTime = sentTime;
	}

	public Date getReceivedTime() {
		return mReceivedTime;
	}

	public void setReceivedTime(Date receivedTime) {
		mReceivedTime = receivedTime;
	}

	@Override
	public int compareTo(GeoTag another) {
		if (mId == null) return -1;
		return mId.compareTo(another.mId);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GeoTag) {
			if (mId == null) return false;
			return mId.equals(((GeoTag)o).mId);
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return mId.hashCode();
	}
}
