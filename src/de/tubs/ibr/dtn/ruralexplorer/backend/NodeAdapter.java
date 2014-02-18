
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
import de.tubs.ibr.dtn.ruralexplorer.NodeItem;
import de.tubs.ibr.dtn.ruralexplorer.R;

public class NodeAdapter extends CursorAdapter {

	@SuppressWarnings("unused")
	private final static String TAG = "NodeAdapter";

	private LayoutInflater mInflater = null;
	private Context mContext = null;

	public static final String[] PROJECTION = new String[] {
			BaseColumns._ID,
			Node.ENDPOINT,
			Node.TYPE,
			RuralLocation.LAT,
			RuralLocation.LNG,
			RuralLocation.ALT,
			RuralLocation.BEARING,
			RuralLocation.SPEED,
			RuralLocation.ACCURACY
	};

	// The indexes of the default columns which must be consistent
	// with above PROJECTION.
	static final int COLUMN_NODE_ID = 0;
	static final int COLUMN_NODE_ENDPOINT = 1;
	static final int COLUMN_NODE_TYPE = 2;
	static final int COLUMN_NODE_LOCATION_LAT = 3;
	static final int COLUMN_NODE_LOCATION_LNG = 4;
	static final int COLUMN_NODE_LOCATION_ALT = 5;
	static final int COLUMN_NODE_LOCATION_BEARING = 6;
	static final int COLUMN_NODE_LOCATION_SPEED = 7;
	static final int COLUMN_NODE_LOCATION_ACCURACY = 8;

	private static final int CACHE_SIZE = 50;

	private final NodeCache mNodeCache;
	private final ColumnsMap mColumnsMap;

	public NodeAdapter(Context context, Cursor c, ColumnsMap cmap)
	{
		super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);

		mNodeCache = new NodeCache(CACHE_SIZE);
		mColumnsMap = cmap;
	}

	@SuppressLint("NewApi")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof NodeItem) {
			long nodeId = cursor.getLong(mColumnsMap.mColumnId);

			Node node = getCachedNode(nodeId, cursor);
			if (node != null) {
				NodeItem itm = (NodeItem) view;
				int position = cursor.getPosition();
				itm.bind(node, position);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.node_item, parent, false);
		return view;
	}

	public static class ColumnsMap {
		public int mColumnId;
		public int mColumnEndpoint;
		public int mColumnType;
		public int mColumnLocationLat;
		public int mColumnLocationLng;
		public int mColumnLocationAlt;
		public int mColumnLocationSpeed;
		public int mColumnLocationBearing;
		public int mColumnLocationAccuracy;

		public ColumnsMap() {
			mColumnId = COLUMN_NODE_ID;
			mColumnEndpoint = COLUMN_NODE_ENDPOINT;
			mColumnType = COLUMN_NODE_TYPE;
			mColumnLocationLat = COLUMN_NODE_LOCATION_LAT;
			mColumnLocationLng = COLUMN_NODE_LOCATION_LNG;
			mColumnLocationAlt = COLUMN_NODE_LOCATION_ALT;
			mColumnLocationSpeed = COLUMN_NODE_LOCATION_SPEED;
			mColumnLocationBearing = COLUMN_NODE_LOCATION_BEARING;
			mColumnLocationAccuracy = COLUMN_NODE_LOCATION_ACCURACY;
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
				mColumnEndpoint = cursor.getColumnIndexOrThrow(Node.ENDPOINT);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}

			try {
				mColumnType = cursor.getColumnIndexOrThrow(Node.TYPE);
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
		mNodeCache.evictAll();
	}

	private boolean isCursorValid(Cursor cursor) {
		// Check whether the cursor is valid or not.
		if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
			return false;
		}
		return true;
	}

	public Node getCachedNode(long buddyId, Cursor c) {
		Node item = mNodeCache.get(buddyId);
		if (item == null && c != null && isCursorValid(c)) {
			item = new Node(mContext, c, mColumnsMap);
			mNodeCache.put(buddyId, item);
		}
		return item;
	}

	private static class NodeCache extends LruCache<Long, Node> {
		public NodeCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected void entryRemoved(boolean evicted, Long key,
				Node oldValue, Node newValue) {
			// oldValue.cancelPduLoading();
		}
	}
}
