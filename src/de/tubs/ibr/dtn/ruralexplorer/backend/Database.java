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
	
	private static final String DATABASE_CREATE_NODES =
			"CREATE TABLE " + TABLE_NAME_NODES + " (" +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				Node.ENDPOINT + " TEXT NOT NULL, " +
				Node.TYPE + " TEXT NOT NULL, " +
				Node.LOCATION + " TEXT" +
			");";
	
	private class DBOpenHelper extends SQLiteOpenHelper {
		
		private static final String DATABASE_NAME = "rural_explorer";
		private static final int DATABASE_VERSION = 1;
		
		public DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_NODES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DBOpenHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_NODES);
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
			Location l1 = new Location(l);
			l1.setLatitude(l.getLatitude() + 0.005);
			n.setLocation(l1);
			update(n);
			
			n = new Node( Node.Type.PI, new SingletonEndpoint("dtn://test2") );
			Location l2 = new Location(l);
			l2.setLatitude(l.getLatitude() - 0.005);
			n.setLocation(l2);
			update(n);
			
			n = new Node( Node.Type.ANDROID, new SingletonEndpoint("dtn://test3") );
			Location l3 = new Location(l);
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
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void update(Node n) {
		if (n.getId() == null) {
			// create a new node
			createNode(n);
		}
		
		ContentValues values = new ContentValues();
		
//		if (n.getLocation() != null) {
//			try {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				ObjectOutputStream oos = new ObjectOutputStream( baos );
//				oos.writeObject(n.getLocation());
//			
//				// update node's location
//				values.put(Node.LOCATION, Base64.encodeBytes(baos.toByteArray()));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
			values.putNull(Node.LOCATION);
//		}
		
		// update buddy data
		mDatabase.update(TABLE_NAME_NODES, values, Node.ID + " = ?", new String[] { n.getId().toString() });
		
		// send refresh intent
		notifyNodeChanged(n.getId());
	}
	
	public void notifyNodeChanged(Long nodeId) {
		if (mContext != null) {
			Intent i = new Intent(DATA_UPDATED);
			i.putExtra(EXTRA_NODE_ID, nodeId);
			mContext.sendBroadcast(i);
		}
	}
}
