package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
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
import de.tubs.ibr.dtn.ruralexplorer.data.AccelerationData;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;
import de.tubs.ibr.dtn.ruralexplorer.data.SensorData;

public class Database {
	private static final String TAG = "Database";
	
	// indicates updated data to other components
	public static final String DATA_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	public static final String EXTRA_NODE_ID = "de.tubs.ibr.dtn.ruralexplorer.NODE_ID";
	public static final String EXTRA_TAG_ID = "de.tubs.ibr.dtn.ruralexplorer.TAG_ID";
	public static final String EXTRA_NODE = "de.tubs.ibr.dtn.ruralexplorer.NODE";
	
	private DBOpenHelper mHelper = null;
	private SQLiteDatabase mDatabase = null;
	private Context mContext = null;
	
	public static final String TABLE_NAME_NODES = "nodes";
	public static final String TABLE_NAME_GEOTAG = "geotag";
	public static final String TABLE_NAME_DATA = "history";
	
	private static final String DATABASE_CREATE_NODES =
			"CREATE TABLE " + TABLE_NAME_NODES + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				Node.ENDPOINT + " TEXT NOT NULL, " +
				Node.TYPE + " TEXT NOT NULL, " +
				Node.NAME + " TEXT, " +
				Node.LAST_UPDATE + " TEXT, " +
				LocationData.LAT + " DOUBLE, " +
				LocationData.LNG + " DOUBLE, " +
				LocationData.ALT + " DOUBLE, " +
				LocationData.BEARING + " FLOAT, " +
				LocationData.SPEED + " FLOAT, " +
				LocationData.ACCURACY + " FLOAT, " +
				SensorData.PRESSURE + " FLOAT, " +
				SensorData.TEMPERATURE + " FLOAT, " +
				AccelerationData.ACCELERATION_X + " FLOAT, " +
				AccelerationData.ACCELERATION_Y + " FLOAT, " +
				AccelerationData.ACCELERATION_Z + " FLOAT" +
			");";
	
	private static final String DATABASE_CREATE_GEOTAGS =
			"CREATE TABLE " + TABLE_NAME_GEOTAG + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				GeoTag.SENT_TIME + " TEXT, " +
				GeoTag.RECV_TIME + " TEXT NOT NULL, " +
				GeoTag.ENDPOINT + " TEXT NOT NULL, " +
				LocationData.LAT + " DOUBLE, " +
				LocationData.LNG + " DOUBLE, " +
				LocationData.ALT + " DOUBLE, " +
				LocationData.BEARING + " FLOAT, " +
				LocationData.SPEED + " FLOAT, " +
				LocationData.ACCURACY + " FLOAT" +
			");";
	
	private class DBOpenHelper extends SQLiteOpenHelper {
		
		private static final String DATABASE_NAME = "rural_explorer";
		private static final int DATABASE_VERSION = 11;
		
		public DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_NODES);
			db.execSQL(DATABASE_CREATE_GEOTAGS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DBOpenHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_NODES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GEOTAG);
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
	}
	
	public void addDummies() {
		// create fake entries
		Location l = new Location("fake");
		l.setLatitude(52.273535);
		l.setLongitude(10.524711);
		
		Node n = new Node( Node.Type.INGA, new SingletonEndpoint("dtn://test1") );
		n.setLastUpdate(new Date());
		LocationData l1 = new LocationData(l);
		l1.setLatitude(l.getLatitude() + 0.005);
		n.setLocation(l1);
		update(n);
		
		n = new Node( Node.Type.PI, new SingletonEndpoint("dtn://test2") );
		n.setLastUpdate(new Date());
		LocationData l2 = new LocationData(l);
		l2.setLatitude(l.getLatitude() - 0.005);
		n.setLocation(l2);
		update(n);
		
		n = new Node( Node.Type.ANDROID, new SingletonEndpoint("dtn://test3") );
		n.setLastUpdate(new Date());
		LocationData l3 = new LocationData(l);
		l3.setLongitude(l.getLongitude() - 0.005);
		n.setLocation(l3);
		update(n);
		
		// add dummy tag
		GeoTag t = new GeoTag( new SingletonEndpoint("dtn://test1") );
		LocationData l4 = new LocationData(l);
		l4.setLatitude(l.getLatitude() + 0.005);
		l4.setLongitude(l.getLongitude() - 0.005);
		t.setLocation(l4);
		t.setSentTime(new Date());
		create(t);
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
	
	private synchronized Node create(Node n) {
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
	
	private void add(ContentValues values, LocationData l) {
		if (l.hasLatitude()) {
			values.put(LocationData.LAT, l.getLatitude());
		} else {
			values.putNull(LocationData.LAT);
		}
		
		if (l.hasLongitude()) {
			values.put(LocationData.LNG, l.getLongitude());
		} else {
			values.putNull(LocationData.LNG);
		}
		
		if (l.hasAltitude()) {
			values.put(LocationData.ALT, l.getAltitude());
		} else {
			values.putNull(LocationData.ALT);
		}
		
		if (l.hasBearing()) {
			values.put(LocationData.BEARING, l.getBearing());
		} else {
			values.putNull(LocationData.BEARING);
		}
		
		if (l.hasSpeed()) {
			values.put(LocationData.SPEED, l.getSpeed());
		} else {
			values.putNull(LocationData.SPEED);
		}
		
		if (l.hasAccurarcy()) {
			values.put(LocationData.ACCURACY, l.getAccurarcy());
		} else {
			values.putNull(LocationData.ACCURACY);
		}
	}
	
	private void add(ContentValues values, SensorData data) {
		if (data.hasPressure()) {
			values.put(SensorData.TEMPERATURE, data.getPressure());
		} else {
			values.putNull(SensorData.TEMPERATURE);
		}
		
		if (data.hasTemperature()) {
			values.put(SensorData.PRESSURE, data.getTemperature());
		} else {
			values.putNull(SensorData.PRESSURE);
		}
	}
	
	private void add(ContentValues values, AccelerationData data) {
		float[] axis = data.getData();
		
		values.put(AccelerationData.ACCELERATION_X, axis[0]);
		values.put(AccelerationData.ACCELERATION_Y, axis[1]);
		values.put(AccelerationData.ACCELERATION_Z, axis[2]);
	}
	
	public Node getNode(SingletonEndpoint endpoint, Location l) {
		// node data
		Cursor c = mDatabase.query(Database.TABLE_NAME_NODES, NodeAdapter.PROJECTION, Node.ENDPOINT + " = ?", new String[] { endpoint.toString() }, null, null, Node.ENDPOINT);
		
		if (c.moveToNext()) {
			return new Node(mContext, c, l, new NodeAdapter.ColumnsMap(c));
		}
		
		return null;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void update(Node n) {
		if (n.getId() == null) {
			// create a new node
			create(n);
		}
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ContentValues values = new ContentValues();
		
		if (n.hasName()) {
			values.put(Node.NAME, n.getName());
		} else {
			values.putNull(Node.NAME);
		}
		
		if (n.getLastUpdate() != null) {
			values.put(Node.LAST_UPDATE, dateFormat.format(n.getLastUpdate()));
		}

		// add location
		add(values, n.getLocation());
		
		// add sensor data
		add(values, n.getSensor());
		
		// add acceleration data
		add(values, n.getAcceleration());
		
		try {
			// update data
			mDatabase.update(TABLE_NAME_NODES, values, Node.ID + " = ?", new String[] { n.getId().toString() });
		} catch (Exception e) {
			// could not update data
			Log.e(TAG, "Location update failed.", e);
		}

		// send refresh intent
		notifyNodeChanged(n.getId());
	}
	
	@SuppressLint("SimpleDateFormat")
	public void create(GeoTag tag) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			ContentValues values = new ContentValues();
			
			// set sent time if known
			if (tag.getSentTime() != null) {
				values.put(GeoTag.SENT_TIME, dateFormat.format(tag.getSentTime()));
			}
			
			// set received time
			values.put(GeoTag.RECV_TIME, dateFormat.format(tag.getReceivedTime()));
			
			// set endpoint
			values.put(GeoTag.ENDPOINT, tag.getEndpoint().toString());
			
			// add location
			add(values, tag.getLocation());
			
			// store the tag in the database
			long tagId = mDatabase.insert(TABLE_NAME_GEOTAG, null, values);
			
			// assign new node id
			tag.setId(tagId);
			
			// notify data listeners
			notifyGeoTagChanged(tagId);
		} catch (Exception e) {
			// could not insert data
			Log.e(TAG, "Tag insertion failed.", e);
		}
	}
	
	public void clear() {
		mDatabase.delete(Database.TABLE_NAME_NODES, null, null);
		mDatabase.delete(Database.TABLE_NAME_GEOTAG, null, null);
		
		// send refresh intent
		notifyDatabaseChanged();
	}
	
	public void notifyGeoTagChanged(Long tagId) {
		if (mContext != null) {
			Intent i = new Intent(DATA_UPDATED);
			i.putExtra(EXTRA_TAG_ID, tagId);
			mContext.sendBroadcast(i);
		}
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
