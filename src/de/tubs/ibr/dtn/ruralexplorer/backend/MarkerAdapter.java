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
import de.tubs.ibr.dtn.ruralexplorer.MarkerItem;
import de.tubs.ibr.dtn.ruralexplorer.R;

public class MarkerAdapter extends CursorAdapter {
	
	@SuppressWarnings("unused")
	private final static String TAG = "MarkerAdapter";

	private LayoutInflater mInflater = null;
	private Context mContext = null;

	public static final String[] PROJECTION = new String[] {
			BaseColumns._ID,
			Marker.NODE_ID,
			RuralLocation.LAT,
			RuralLocation.LNG,
			RuralLocation.ALT,
			RuralLocation.BEARING,
			RuralLocation.SPEED,
			RuralLocation.ACCURACY
	};

	// The indexes of the default columns which must be consistent
	// with above PROJECTION.
	static final int COLUMN_MARKER_ID = 0;
	static final int COLUMN_MARKER_NODE_ID = 1;
	static final int COLUMN_MARKER_LOCATION_LAT = 2;
	static final int COLUMN_MARKER_LOCATION_LNG = 3;
	static final int COLUMN_MARKER_LOCATION_ALT = 4;
	static final int COLUMN_MARKER_LOCATION_BEARING = 5;
	static final int COLUMN_MARKER_LOCATION_SPEED = 6;
	static final int COLUMN_MARKER_LOCATION_ACCURACY = 7;

	private static final int CACHE_SIZE = 50;

	private final MarkerCache mMarkerCache;
	private final ColumnsMap mColumnsMap;

	public MarkerAdapter(Context context, Cursor c, ColumnsMap cmap)
	{
		super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);

		mMarkerCache = new MarkerCache(CACHE_SIZE);
		mColumnsMap = cmap;
	}

	@SuppressLint("NewApi")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof MarkerItem) {
			long markerId = cursor.getLong(mColumnsMap.mColumnId);

			Marker marker = getCachedMarker(markerId, cursor);
			if (marker != null) {
				MarkerItem itm = (MarkerItem) view;
				int position = cursor.getPosition();
				itm.bind(marker, position);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.marker_item, parent, false);
		return view;
	}

	public static class ColumnsMap {
		public int mColumnId;
		public int mColumnNodeId;
		public int mColumnLocationLat;
		public int mColumnLocationLng;
		public int mColumnLocationAlt;
		public int mColumnLocationSpeed;
		public int mColumnLocationBearing;
		public int mColumnLocationAccuracy;

		public ColumnsMap() {
			mColumnId = COLUMN_MARKER_ID;
			mColumnNodeId = COLUMN_MARKER_NODE_ID;
			mColumnLocationLat = COLUMN_MARKER_LOCATION_LAT;
			mColumnLocationLng = COLUMN_MARKER_LOCATION_LNG;
			mColumnLocationAlt = COLUMN_MARKER_LOCATION_ALT;
			mColumnLocationSpeed = COLUMN_MARKER_LOCATION_SPEED;
			mColumnLocationBearing = COLUMN_MARKER_LOCATION_BEARING;
			mColumnLocationAccuracy = COLUMN_MARKER_LOCATION_ACCURACY;
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
				mColumnNodeId = cursor.getColumnIndexOrThrow(Marker.NODE_ID);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}

			try {
				mColumnLocationLat = cursor.getColumnIndexOrThrow(RuralLocation.LAT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationLng = cursor.getColumnIndexOrThrow(RuralLocation.LNG);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationAlt = cursor.getColumnIndexOrThrow(RuralLocation.ALT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationBearing = cursor.getColumnIndexOrThrow(RuralLocation.BEARING);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationSpeed = cursor.getColumnIndexOrThrow(RuralLocation.SPEED);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnLocationAccuracy = cursor.getColumnIndexOrThrow(RuralLocation.ACCURACY);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mMarkerCache.evictAll();
	}

	private boolean isCursorValid(Cursor cursor) {
		// Check whether the cursor is valid or not.
		if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
			return false;
		}
		return true;
	}

	public Marker getCachedMarker(long buddyId, Cursor c) {
		Marker item = mMarkerCache.get(buddyId);
		if (item == null && c != null && isCursorValid(c)) {
			item = new Marker(mContext, c, mColumnsMap);
			mMarkerCache.put(buddyId, item);
		}
		return item;
	}

	private static class MarkerCache extends LruCache<Long, Marker> {
		public MarkerCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected void entryRemoved(boolean evicted, Long key,
				Marker oldValue, Marker newValue) {
			// oldValue.cancelPduLoading();
		}
	}
}
