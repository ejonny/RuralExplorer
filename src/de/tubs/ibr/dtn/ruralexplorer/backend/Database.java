package de.tubs.ibr.dtn.ruralexplorer.backend;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;
import de.tubs.ibr.dtn.api.SingletonEndpoint;

public class Database {
	private static final String TAG = "Database";
	
	// indicates updated data to other components
	public static final String DATA_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	public static final String EXTRA_NODE_ID = "de.tubs.ibr.dtn.ruralexplorer.NODE_ID";
	public static final String EXTRA_NODE = "de.tubs.ibr.dtn.ruralexplorer.NODE";
	
	private DBOpenHelper mHelper = null;
	private SQLiteDatabase mDatabase = null;
	private Context mContext = null;
	
	public static final String TABLE_NAME_NODES = "nodes";
	public static final String TABLE_NAME_MARKER = "marker";
	
	private static final String DATABASE_CREATE_NODES =
			"CREATE TABLE " + TABLE_NAME_NODES + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				Node.ENDPOINT + " TEXT NOT NULL, " +
				Node.TYPE + " TEXT NOT NULL, " +
				RuralLocation.LAT + " DOUBLE, " +
				RuralLocation.LNG + " DOUBLE, " +
				RuralLocation.ALT + " DOUBLE, " +
				RuralLocation.BEARING + " FLOAT, " +
				RuralLocation.SPEED + " FLOAT, " +
				RuralLocation.ACCURACY + " FLOAT" +
			");";
	
	private static final String DATABASE_CREATE_MARKER =
			"CREATE TABLE " + TABLE_NAME_MARKER + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				Marker.NODE_ID + " INTEGER NOT NULL, " +
				RuralLocation.LAT + " DOUBLE, " +
				RuralLocation.LNG + " DOUBLE, " +
				RuralLocation.ALT + " DOUBLE, " +
				RuralLocation.BEARING + " FLOAT, " +
				RuralLocation.SPEED + " FLOAT, " +
				RuralLocation.ACCURACY + " FLOAT" +
			");";
	
	private class DBOpenHelper extends SQLiteOpenHelper {
		
		private static final String DATABASE_NAME = "rural_explorer";
		private static final int DATABASE_VERSION = 3;
		
		public DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_NODES);
			db.execSQL(DATABASE_CREATE_MARKER);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DBOpenHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_NODES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MARKER);
			onCreate(db);
		}
	};
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void open(Context context) throws SQLException {
		this.mContext = context;
		mHelper = new DBOpenHelper(context);
		mDatabase = mHelper.getWritableDatabase();
		
		if (getCount() == 0) {
			// create fake entries
			Location l = new Location("fake");
			l.setLatitude(52.273535);
			l.setLongitude(10.524711);
			
			Node n = new Node( Node.Type.INGA, new SingletonEndpoint("dtn://test1") );
			RuralLocation l1 = new RuralLocation(l);
			l1.setLatitude(l.getLatitude() + 0.005);
			n.setLocation(l1);
			update(n);
			
			n = new Node( Node.Type.PI, new SingletonEndpoint("dtn://test2") );
			RuralLocation l2 = new RuralLocation(l);
			l2.setLatitude(l.getLatitude() - 0.005);
			n.setLocation(l2);
			update(n);
			
			n = new Node( Node.Type.ANDROID, new SingletonEndpoint("dtn://test3") );
			RuralLocation l3 = new RuralLocation(l);
			l3.setLongitude(l.getLongitude() - 0.005);
			n.setLocation(l3);
			update(n);
		}
	}
	
	public SQLiteDatabase raw() {
		return mDatabase;
	}
	
	public void close() {
		mHelper.close();
	}
	
	public int getCount() {
		int ret = 0;
		
		try {
			Cursor cur = mDatabase.query(TABLE_NAME_NODES, new String[] { "COUNT(*)" }, null, null, null, null, null);
			if (cur.moveToNext()) {
				ret = cur.getInt(0);
			}

			cur.close();
		} catch (Exception e) {
			// error
		}
		
		return ret;
	}
	
	private synchronized Node createNode(Node n) {
		ContentValues values = new ContentValues();
		
		try {
			values.put(Node.TYPE, n.getType().name());
			values.put(Node.ENDPOINT, n.getEndpoint().toString());
			
			// store the message in the database
			long nodeId = mDatabase.insert(TABLE_NAME_NODES, null, values);
			
			// assign new node id
			n.setId(nodeId);
			
			return n;
		} catch (Exception e) {
			// could not create node
			Log.e(TAG, "Could not create node.", e);
		}
		
		return null;
	}
	
	private void updateLocation(Long nodeId, RuralLocation l) {
		ContentValues values = new ContentValues();
		
		if (l.hasLatitude()) {
			values.put(RuralLocation.LAT, l.getLatitude());
		} else {
			values.putNull(RuralLocation.LAT);
		}
		
		if (l.hasLongitude()) {
			values.put(RuralLocation.LNG, l.getLongitude());
		} else {
			values.putNull(RuralLocation.LNG);
		}
		
		if (l.hasAltitude()) {
			values.put(RuralLocation.ALT, l.getAltitude());
		} else {
			values.putNull(RuralLocation.ALT);
		}
		
		if (l.hasBearing()) {
			values.put(RuralLocation.BEARING, l.getBearing());
		} else {
			values.putNull(RuralLocation.BEARING);
		}
		
		if (l.hasSpeed()) {
			values.put(RuralLocation.SPEED, l.getSpeed());
		} else {
			values.putNull(RuralLocation.SPEED);
		}
		
		if (l.hasAccurarcy()) {
			values.put(RuralLocation.ACCURACY, l.getAccurarcy());
		} else {
			values.putNull(RuralLocation.ACCURACY);
		}

		try {
			// update buddy data
			mDatabase.update(TABLE_NAME_NODES, values, Node.ID + " = ?", new String[] { nodeId.toString() });
		} catch (Exception e) {
			// could not update location
			Log.e(TAG, "Location update failed.", e);
		}
	}
	
	public Node getNode(SingletonEndpoint endpoint) {
		// node data
		Cursor c = mDatabase.query(Database.TABLE_NAME_NODES, NodeAdapter.PROJECTION, Node.ENDPOINT + " = ?", new String[] { endpoint.toString() }, null, null, Node.ENDPOINT);
		
		if (c.moveToNext()) {
			return new Node(mContext, c, new NodeAdapter.ColumnsMap());
		}
		
		return null;
	}
	
	public void update(Node n) {
		if (n.getId() == null) {
			// create a new node
			createNode(n);
		}

		// store location
		updateLocation(n.getId(), n.getLocation());

		// send refresh intent
		notifyNodeChanged(n.getId());
	}
	
	public void clear() {
		mDatabase.delete(Database.TABLE_NAME_NODES, null, null);
		
		// send refresh intent
		notifyDatabaseChanged();
	}
	
	public void notifyNodeChanged(Long nodeId) {
		if (mContext != null) {
			Intent i = new Intent(DATA_UPDATED);
			i.putExtra(EXTRA_NODE_ID, nodeId);
			mContext.sendBroadcast(i);
		}
	}
	
	public void notifyDatabaseChanged() {
		if (mContext != null) {
			Intent i = new Intent(DATA_UPDATED);
			mContext.sendBroadcast(i);
		}
	}
}
