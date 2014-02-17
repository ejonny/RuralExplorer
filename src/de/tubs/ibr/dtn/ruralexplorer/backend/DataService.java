
package de.tubs.ibr.dtn.ruralexplorer.backend;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class DataService extends Service {

	private static final String TAG = "DataService";
	
	public static final String ACTION_BACKGROUND_ON = "de.tubs.ibr.dtn.ruralexplorer.BACKGROUND_ON";
	public static final String ACTION_BACKGROUND_OFF = "de.tubs.ibr.dtn.ruralexplorer.BACKGROUND_OFF";
	
	// indicates updated location to other components
	public static final String LOCATION_UPDATED = "de.tubs.ibr.dtn.ruralexplorer.DATA_UPDATED";
	
	// additional parameters
	public static final String EXTRA_LOCATION = "de.tubs.ibr.dtn.ruralexplorer.DATA_LOCATION";
	
	private LocationRequest mLocationRequest = null;
	
	private volatile Looper mServiceLooper = null;
	private volatile ServiceHandler mServiceHandler = null;
	
	private Database mDatabase = null;
	
	private boolean mPersistent = false;
	
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
		
		// TODO: react to intent
		
		// stop the service if not persistent
		if (!mPersistent && (startId != -1)) stopSelf(startId);
	}
	
	private GooglePlayServicesClient.ConnectionCallbacks mConnectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {

		@Override
		public void onConnected(Bundle arg0) {
			Log.d(TAG, "Location services connected");
			
			// start beaconing
			mServiceHandler.post(mBeaconProcess);
			
			// request location updates
			mLocationClient.requestLocationUpdates(mLocationRequest, locationListener);
		}

		@Override
		public void onDisconnected() {
			Log.d(TAG, "Location services disconnected");
			
			// stop beaconing
			mServiceHandler.removeCallbacks(mBeaconProcess);
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
			// 
			Log.d(TAG, "Location changed: " + location.toString());
		}
		
	};

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
		
		// create location request
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(20000);
		mLocationRequest.setFastestInterval(5000);
		
		// create a new location client
		mLocationClient = new LocationClient(this, mConnectionCallbacks, mConnectionFailedListener);
		
		// connect to location services
		mLocationClient.connect();
	}

	@Override
	public void onDestroy() {
		// stop beaconing
		mServiceHandler.removeCallbacks(mBeaconProcess);
		
		// disconnect from location services
		mLocationClient.disconnect();
		
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
	
	private Runnable mBeaconProcess = new Runnable() {
		@Override
		public void run() {
			// generate beacon
			Intent intent = new Intent(DataService.this, CommService.class);
			intent.setAction(CommService.ACTION_GENERATE_BEACON);
			intent.putExtra(EXTRA_LOCATION, mLocationClient.getLastLocation());
			startService(intent);
			
			// next update in 10 seconds
			mServiceHandler.postDelayed(mBeaconProcess, 10000);
		}
	};
}