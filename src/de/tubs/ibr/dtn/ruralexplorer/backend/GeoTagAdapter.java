package de.tubs.ibr.dtn.ruralexplorer.backend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tubs.ibr.dtn.ruralexplorer.GeoTagItem;
import de.tubs.ibr.dtn.ruralexplorer.R;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;

public class GeoTagAdapter extends CursorAdapter {
	@SuppressWarnings("unused")
	private final static String TAG = "GeoTagAdapter";

	private LayoutInflater mInflater = null;
	private Context mContext = null;

	public static final String[] PROJECTION = new String[] {
			BaseColumns._ID,
			GeoTag.SENT_TIME,
			GeoTag.RECV_TIME,
			GeoTag.ENDPOINT,
			LocationData.LAT,
			LocationData.LNG,
			LocationData.ALT,
			LocationData.BEARING,
			LocationData.SPEED,
			LocationData.ACCURACY
	};

	// The indexes of the default columns which must be consistent
	// with above PROJECTION.
	static final int COLUMN_GEOTAG_ID = 0;
	static final int COLUMN_GEOTAG_SENTTIME = 1;
	static final int COLUMN_GEOTAG_RECVTIME = 2;
	static final int COLUMN_GEOTAG_ENDPOINT = 3;
	static final int COLUMN_GEOTAG_LOCATION_LAT = 4;
	static final int COLUMN_GEOTAG_LOCATION_LNG = 5;
	static final int COLUMN_GEOTAG_LOCATION_ALT = 6;
	static final int COLUMN_GEOTAG_LOCATION_BEARING = 7;
	static final int COLUMN_GEOTAG_LOCATION_SPEED = 8;
	static final int COLUMN_GEOTAG_LOCATION_ACCURACY = 9;

	private static final int CACHE_SIZE = 50;

	private final GeoTagCache mGeoTagCache;
	private final ColumnsMap mColumnsMap;

	public GeoTagAdapter(Context context, Cursor c, ColumnsMap cmap)
	{
		super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);

		mGeoTagCache = new GeoTagCache(CACHE_SIZE);
		mColumnsMap = cmap;
	}

	@SuppressLint("NewApi")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof GeoTagItem) {
			long tagId = cursor.getLong(mColumnsMap.mColumnId);

			GeoTag tag = getCachedGeoTag(tagId, cursor);
			if (tag != null) {
				GeoTagItem itm = (GeoTagItem) view;
				int position = cursor.getPosition();
				itm.bind(tag, position);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.geotag_item, parent, false);
		return view;
	}

	public static class ColumnsMap {
		public int mColumnId;
		public int mColumnSentTime;
		public int mColumnRecvTime;
		public int mColumnEndpoint;
		public int mColumnLocationLat;
		public int mColumnLocationLng;
		public int mColumnLocationAlt;
		public int mColumnLocationSpeed;
		public int mColumnLocationBearing;
		public int mColumnLocationAccuracy;

		public ColumnsMap() {
			mColumnId = COLUMN_GEOTAG_ID;
			mColumnSentTime = COLUMN_GEOTAG_SENTTIME;
			mColumnRecvTime = COLUMN_GEOTAG_RECVTIME;
			mColumnEndpoint = COLUMN_GEOTAG_ENDPOINT;
			mColumnLocationLat = COLUMN_GEOTAG_LOCATION_LAT;
			mColumnLocationLng = COLUMN_GEOTAG_LOCATION_LNG;
			mColumnLocationAlt = COLUMN_GEOTAG_LOCATION_ALT;
			mColumnLocationSpeed = COLUMN_GEOTAG_LOCATION_SPEED;
			mColumnLocationBearing = COLUMN_GEOTAG_LOCATION_BEARING;
			mColumnLocationAccuracy = COLUMN_GEOTAG_LOCATION_ACCURACY;
		}

		public ColumnsMap(Cursor cursor) {
			// Ignore all 'not found' exceptions since the custom columns
			// may be just a subset of the default columns.
			try {
				mColumnId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnSentTime = cursor.getColumnIndexOrThrow(GeoTag.SENT_TIME);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnRecvTime = cursor.getColumnIndexOrThrow(GeoTag.RECV_TIME);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}

			try {
				mColumnEndpoint = cursor.getColumnIndexOrThrow(GeoTag.ENDPOINT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}

			try {
				mColumnLocationLat = cursor.getColumnIndexOrThrow(LocationData.LAT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationLng = cursor.getColumnIndexOrThrow(LocationData.LNG);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationAlt = cursor.getColumnIndexOrThrow(LocationData.ALT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationBearing = cursor.getColumnIndexOrThrow(LocationData.BEARING);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationSpeed = cursor.getColumnIndexOrThrow(LocationData.SPEED);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationAccuracy = cursor.getColumnIndexOrThrow(LocationData.ACCURACY);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mGeoTagCache.evictAll();
	}

	private boolean isCursorValid(Cursor cursor) {
		// Check whether the cursor is valid or not.
		if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
			return false;
		}
		return true;
	}

	public GeoTag getCachedGeoTag(long buddyId, Cursor c) {
		GeoTag item = mGeoTagCache.get(buddyId);
		if (item == null && c != null && isCursorValid(c)) {
			item = new GeoTag(mContext, c, mColumnsMap);
			mGeoTagCache.put(buddyId, item);
		}
		return item;
	}

	private static class GeoTagCache extends LruCache<Long, GeoTag> {
		public GeoTagCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected void entryRemoved(boolean evicted, Long key,
				GeoTag oldValue, GeoTag newValue) {
			// oldValue.cancelPduLoading();
		}
	}
}
