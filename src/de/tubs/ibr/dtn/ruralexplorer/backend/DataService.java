
package de.tubs.ibr.dtn.ruralexplorer.backend;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.ruralexplorer.MainActivity;
import de.tubs.ibr.dtn.ruralexplorer.R;
import de.tubs.ibr.dtn.ruralexplorer.data.AccelerationData;
import de.tubs.ibr.dtn.ruralexplorer.data.ExplorerBeacon;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;
import de.tubs.ibr.dtn.ruralexplorer.data.SensorData;

public class DataService extends Service {

	private static final String TAG = "DataService";
	
	private static final int FOREGROUND_ID = 1;
	private static final int RESCUE_NOTIFICATION = 2;
	
	public static final String ACTION_BACKGROUND_ON = "de.tubs.ibr.dtn.ruralexplorer.BACKGROUND_ON";
	public static final String ACTION_BACKGROUND_OFF = "de.tubs.ibr.dtn.ruralexplorer.BACKGROUND_OFF";
	
	// indicates updated location to other components
	public static final String LOCATION_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	
	// call rescue action
	public static final String ACTION_CALL_RESCUE = "de.tubs.ibr.dtn.ruralexplorer.RESCUE";
	
	// additional parameters
	public static final String EXTRA_LOCATION = "de.tubs.ibr.dtn.ruralexplorer.DATA_LOCATION";
	public static final String EXTRA_GEOTAG = "de.tubs.ibr.dtn.ruralexplorer.EXTRA_GEOTAG";
	
	private LocationRequest mLocationRequest = null;
	
	private volatile Looper mServiceLooper = null;
	private volatile ServiceHandler mServiceHandler = null;
	
	private Database mDatabase = null;
	
	private boolean mPersistent = false;
	
	NotificationCompat.Builder mNotificationBuilder = null;
	
	private LocationClient mLocationClient = null;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public DataService getService() {
			return DataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@SuppressLint("HandlerLeak")
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Intent intent = (Intent) msg.obj;
			onHandleIntent(intent, msg.arg1);
		}
	}
	
	public void onHandleIntent(Intent intent, int startId) {
		String action = intent.getAction();
		
		// react to new beacon
		if (CommService.BEACON_RECEIVED.equals(action)) {
			// get beacon object
			ExplorerBeacon b = (ExplorerBeacon)intent.getParcelableExtra(CommService.EXTRA_BEACON);
			
			// get beacon source
			SingletonEndpoint source = (SingletonEndpoint)intent.getParcelableExtra(CommService.EXTRA_ENDPOINT);
			
			// source node type
			Node.Type type;
			
			switch (b.getType())
			{
				case 1:
					type = Node.Type.ANDROID;
					break;
					
				case 2:
					type = Node.Type.INGA;
					break;
					
				case 3:
					type = Node.Type.PI;
					break;
					
				default:
					type = Node.Type.GENERIC;
					break;
			}
			
			// debug
			Log.d(TAG, "new beacon received from " + source.toString());
			
			if (b.hasRescueLocation()) {
				// generate a rescue tag
				GeoTag tag = new GeoTag(source);
				
				// set sent time
				if ( intent.hasExtra(CommService.EXTRA_TIME) ) {
					tag.setSentTime( (Date)intent.getSerializableExtra(CommService.EXTRA_TIME) );
				} else {
					tag.setSentTime(null);
				}
				
				// set location
				tag.setLocation(new LocationData(b.getRescueLocation()));
				
				// write tag to the database
				mDatabase.create(tag);
				
				// create notification
				createNotification(tag);
			}
			else {
				// get node from database
				Node n = mDatabase.getNode(source, getLocation());
				
				if (n == null) {
					n = new Node(type, source);
				}
				
				// set last update time
				if ( intent.hasExtra(CommService.EXTRA_TIME) ) {
					n.setLastUpdate( (Date)intent.getSerializableExtra(CommService.EXTRA_TIME) );
				} else {
					n.setLastUpdate(new Date());
				}
				
				// update location
				n.setLocation(new LocationData(b.getPosition()));
				
				// update name
				n.setName(b.getName());
				
				// update sensor data
				if (b.hasSensors()) {
					n.setSensor(new SensorData(b.getSensors()));
				} else {
					n.setSensor(new SensorData());
				}
				
				// update acceleration data
				if (b.hasAcceleration()) {
					n.setAcceleration(new AccelerationData(b.getAcceleration()));
				} else {
					n.setAcceleration(new AccelerationData());
				}
				
				// write changed to the database
				mDatabase.update(n);
			}
		}
		else if (ACTION_BACKGROUND_ON.equals(action))
		{
			if (!mPersistent)
			{
				// run foreground
				mPersistent = true;
				mNotificationBuilder = new NotificationCompat.Builder(this);
				mNotificationBuilder.setContentTitle(getResources().getString(R.string.background_service_title));
				mNotificationBuilder.setContentText(getResources().getString(R.string.background_service_position_na));
				mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_rural);
		        mNotificationBuilder.setOngoing(true);
		        mNotificationBuilder.setOnlyAlertOnce(true);
		        mNotificationBuilder.setWhen(0);

		        Intent notifyIntent = new Intent(this, MainActivity.class);
		        notifyIntent.setAction("android.intent.action.MAIN");
		        notifyIntent.addCategory("android.intent.category.LAUNCHER");

		        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
		        mNotificationBuilder.setContentIntent(contentIntent);
		        
				startForeground(FOREGROUND_ID, mNotificationBuilder.build());
			}
			
			if (mLocationClient == null)
			{
				// create location request
				mLocationRequest = LocationRequest.create();
				mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
				mLocationRequest.setInterval(60000);
				mLocationRequest.setFastestInterval(10000);
				
				// create a new location client
				mLocationClient = new LocationClient(this, mConnectionCallbacks, mConnectionFailedListener);
				
				// connect to location services
				mLocationClient.connect();
			}
		}
		else if (ACTION_BACKGROUND_OFF.equals(action))
		{
			if (mLocationClient != null) {
				// disconnect from location services
				mLocationClient.disconnect();
			}
			
			mNotificationBuilder = null;
			stopForeground(true);
			mPersistent = false;
		}
		else if (ACTION_CALL_RESCUE.equals(action))
		{
			if (mLocationClient != null)
			{
				Location l = getLocation();
				
				if (l != null)
				{
					// generate beacon
					Intent i = new Intent(DataService.this, CommService.class);
					i.setAction(CommService.GENERATE_BEACON);
					i.putExtra(CommService.EXTRA_BEACON_EMERGENCY, true);
					i.putExtra(EXTRA_LOCATION, l);
					startService(i);
					
					// show toast, rescue beacon sent
					Toast.makeText(this, getString(R.string.toast_rescue_request_sent), Toast.LENGTH_SHORT).show();
				} else {
					// show toast, no location available
					Toast.makeText(this, getString(R.string.toast_no_location), Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		// stop the service if not persistent
		if (!mPersistent && (startId != -1)) stopSelf(startId);
	}
	
	private GooglePlayServicesClient.ConnectionCallbacks mConnectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {

		@Override
		public void onConnected(Bundle arg0) {
			Log.d(TAG, "Location services connected");
			
			Location l = getLocation();
			if (l != null) {
				// send location update intent
				Intent locationIntent = new Intent(LOCATION_UPDATED);
				locationIntent.putExtra(EXTRA_LOCATION, l);
				sendBroadcast(locationIntent);
			}
			
			// request location updates
			mLocationClient.requestLocationUpdates(mLocationRequest, locationListener);
		}

		@Override
		public void onDisconnected() {
			Log.d(TAG, "Location services disconnected");
		}
		
	};
	
	private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {

		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO: handle failure
		}
		
	};
	
	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// generate beacon
			Intent intent = new Intent(DataService.this, CommService.class);
			intent.setAction(CommService.GENERATE_BEACON);
			intent.putExtra(EXTRA_LOCATION, location);
			startService(intent);
			
			// send location update intent
			Intent locationIntent = new Intent(LOCATION_UPDATED);
			locationIntent.putExtra(EXTRA_LOCATION, location);
			sendBroadcast(locationIntent);
			
			// update notification
			if (mNotificationBuilder != null)
			{
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				mNotificationBuilder.setContentText(String.format(getString(R.string.background_service_position), location.getLatitude(), location.getLongitude()));
				nm.notify(FOREGROUND_ID, mNotificationBuilder.build());
			}
		}
		
	};
	
	public Location getLocation() {
		if (mLocationClient == null) return null;
		if (mLocationClient.isConnected()) {
			return mLocationClient.getLastLocation();
		} else {
			return null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "data services running");
		
		/*
		 * incoming Intents will be processed by ServiceHandler and queued in
		 * HandlerThread
		 */
		HandlerThread thread = new HandlerThread("DataService_IntentThread");
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		mDatabase = new Database();
		mDatabase.open(this);
	}

	@Override
	public void onDestroy() {
		if (mLocationClient != null) {
			// disconnect from location services
			mLocationClient.disconnect();
		}
		
		// stop looper that handles incoming intents
		mServiceLooper.quit();
		
		// close the database
		mDatabase.close();
		
		Log.d(TAG, "data services destroyed");
		
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		/*
		 * If no explicit intent is given start as ACTION_STARTUP. When this
		 * service crashes, Android restarts it without an Intent. Thus
		 * ACTION_STARTUP is executed!
		 */
		if (intent == null || intent.getAction() == null) {
			Log.d(TAG, "intent == null or intent.getAction() == null -> default to ACTION_STARTUP");

			intent = new Intent(ACTION_BACKGROUND_ON);
		}

		String action = intent.getAction();

		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, "Received start id " + startId + ": " + intent);
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, "Intent Action: " + action);

		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return START_STICKY;
	}
	
	public Database getDatabase() {
		return mDatabase;
	}
	
	private void createNotification(GeoTag t)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// do not generate notifications if disabled by the user
		if (!prefs.getBoolean("pref_notification_enabled", true))
			return;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		// enable auto-cancel
		builder.setAutoCancel(true);

		int defaults = 0;

		if (prefs.getBoolean("pref_notification_vibrate", true)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}

		/** CREATE AN INTENT TO OPEN THE MAIN ACTIVITY **/
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.putExtra(DataService.EXTRA_GEOTAG, t);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentTitle(getString(R.string.notification_rescue_title));
		
		LocationData loc_data = t.getLocation();
		
		if (loc_data.hasLatitude() && loc_data.hasLongitude()) {
			builder.setContentText(String.format(getString(R.string.data_unit_latlng), loc_data.getLatitude(), loc_data.getLongitude()));
		} else {
			builder.setContentText(getString(R.string.notification_rescue_invalid));
		}
		
		builder.setSmallIcon(R.drawable.ic_stat_rescue);
		builder.setDefaults(defaults);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(contentIntent);
		builder.setLights(0xff0080ff, 300, 1000);
		builder.setSound(Uri.parse(prefs.getString("pref_notification_ringtone",
				"content://settings/system/notification_sound")));

		Notification notification = builder.build();
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(RESCUE_NOTIFICATION, notification);
	}
}
