
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
import de.tubs.ibr.dtn.ruralexplorer.data.AccelerationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.SensorData;

public class NodeAdapter extends CursorAdapter {

	@SuppressWarnings("unused")
	private final static String TAG = "NodeAdapter";

	private LayoutInflater mInflater = null;
	private Context mContext = null;

	public static final String[] PROJECTION = new String[] {
			BaseColumns._ID,
			Node.ENDPOINT,
			Node.TYPE,
			Node.NAME,
			Node.LAST_UPDATE,
			LocationData.LAT,
			LocationData.LNG,
			LocationData.ALT,
			LocationData.BEARING,
			LocationData.SPEED,
			LocationData.ACCURACY,
			SensorData.PRESSURE,
			SensorData.TEMPERATURE,
			AccelerationData.ACCELERATION_X,
			AccelerationData.ACCELERATION_Y,
			AccelerationData.ACCELERATION_Z
	};

	// The indexes of the default columns which must be consistent
	// with above PROJECTION.
	static final int COLUMN_NODE_ID = 0;
	static final int COLUMN_NODE_ENDPOINT = 1;
	static final int COLUMN_NODE_TYPE = 2;
	static final int COLUMN_NODE_NAME = 3;
	static final int COLUMN_NODE_LAST_UPDATE = 4;
	static final int COLUMN_NODE_LOCATION_LAT = 5;
	static final int COLUMN_NODE_LOCATION_LNG = 6;
	static final int COLUMN_NODE_LOCATION_ALT = 7;
	static final int COLUMN_NODE_LOCATION_BEARING = 8;
	static final int COLUMN_NODE_LOCATION_SPEED = 9;
	static final int COLUMN_NODE_LOCATION_ACCURACY = 10;
	static final int COLUMN_NODE_SENSOR_PRESSURE = 11;
	static final int COLUMN_NODE_SENSOR_TEMPERATURE = 12;
	static final int COLUMN_NODE_ACCELERATION_X = 13;
	static final int COLUMN_NODE_ACCELERATION_Y = 14;
	static final int COLUMN_NODE_ACCELERATION_Z = 15;

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
		public int mColumnName;
		public int mColumnLastUpdate;
		public int mColumnLocationLat;
		public int mColumnLocationLng;
		public int mColumnLocationAlt;
		public int mColumnLocationSpeed;
		public int mColumnLocationBearing;
		public int mColumnLocationAccuracy;
		public int mColumnSensorPressure;
		public int mColumnSensorTemperature;
		public int mColumnAccelerationX;
		public int mColumnAccelerationY;
		public int mColumnAccelerationZ;

		public ColumnsMap() {
			mColumnId = COLUMN_NODE_ID;
			mColumnEndpoint = COLUMN_NODE_ENDPOINT;
			mColumnType = COLUMN_NODE_TYPE;
			mColumnName = COLUMN_NODE_NAME;
			mColumnLastUpdate = COLUMN_NODE_LAST_UPDATE;
			mColumnLocationLat = COLUMN_NODE_LOCATION_LAT;
			mColumnLocationLng = COLUMN_NODE_LOCATION_LNG;
			mColumnLocationAlt = COLUMN_NODE_LOCATION_ALT;
			mColumnLocationSpeed = COLUMN_NODE_LOCATION_SPEED;
			mColumnLocationBearing = COLUMN_NODE_LOCATION_BEARING;
			mColumnLocationAccuracy = COLUMN_NODE_LOCATION_ACCURACY;
			mColumnSensorPressure = COLUMN_NODE_SENSOR_PRESSURE;
			mColumnSensorTemperature = COLUMN_NODE_SENSOR_TEMPERATURE;
			mColumnAccelerationX = COLUMN_NODE_ACCELERATION_X;
			mColumnAccelerationY = COLUMN_NODE_ACCELERATION_Y;
			mColumnAccelerationZ = COLUMN_NODE_ACCELERATION_Z;
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
				mColumnName = cursor.getColumnIndexOrThrow(Node.NAME);
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
			
			try {
				mColumnSensorPressure = cursor.getColumnIndexOrThrow(SensorData.PRESSURE);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnSensorTemperature = cursor.getColumnIndexOrThrow(SensorData.TEMPERATURE);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnAccelerationX = cursor.getColumnIndexOrThrow(AccelerationData.ACCELERATION_X);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}
			
			try {
				mColumnAccelerationY = cursor.getColumnIndexOrThrow(AccelerationData.ACCELERATION_Y);
			} catch (IllegalArgumentException e) {
				Log.w("colsMap", e.getMessage());
			}

			try {
				mColumnAccelerationZ = cursor.getColumnIndexOrThrow(AccelerationData.ACCELERATION_Z);
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
