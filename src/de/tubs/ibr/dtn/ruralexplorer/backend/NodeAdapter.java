
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
			Node.LOCATION
	};

	// The indexes of the default columns which must be consistent
	// with above PROJECTION.
	static final int COLUMN_NODE_ID = 0;
	static final int COLUMN_NODE_ENDPOINT = 1;
	static final int COLUMN_NODE_TYPE = 2;
	static final int COLUMN_NODE_LOCATION = 3;

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
		public int mColumnLocation;

		public ColumnsMap() {
			mColumnId = COLUMN_NODE_ID;
			mColumnEndpoint = COLUMN_NODE_ENDPOINT;
			mColumnType = COLUMN_NODE_TYPE;
			mColumnLocation = COLUMN_NODE_LOCATION;
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
				mColumnLocation = cursor.getColumnIndexOrThrow(Node.LOCATION);
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
