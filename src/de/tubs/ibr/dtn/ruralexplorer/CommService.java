package de.tubs.ibr.dtn.ruralexplorer;

import android.app.IntentService;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import de.tubs.ibr.dtn.api.Block;
import de.tubs.ibr.dtn.api.Bundle;
import de.tubs.ibr.dtn.api.BundleID;
import de.tubs.ibr.dtn.api.DTNClient;
import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.DataHandler;
import de.tubs.ibr.dtn.api.GroupEndpoint;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionConnection;
import de.tubs.ibr.dtn.api.SessionDestroyedException;
import de.tubs.ibr.dtn.api.SingletonEndpoint;
import de.tubs.ibr.dtn.api.TransferMode;

public class CommService extends IntentService {
	
	// This TAG is used to identify this class (e.g. for debugging)
	private static final String TAG = "ExplorerService";
	
	// group endpoint for beacons
	public static final GroupEndpoint RURAL_GROUP_DTN_EID = new GroupEndpoint("dtn://broadcast.dtn/rural-explorer");
	public static final GroupEndpoint RURAL_GROUP_IPN_EID = new GroupEndpoint("ipn:666.2990520854");
	
	// mark a specific bundle as delivered
	public static final String MARK_DELIVERED_INTENT = "de.tubs.ibr.dtn.ruralexplorer.MARK_DELIVERED";
	public static final String EXTRA_BUNDLEID = "de.tubs.ibr.dtn.ruralexplorer.BUNDLEID";
	
	// process a status report
	public static final String REPORT_DELIVERED_INTENT = "de.tubs.ibr.dtn.ruralexplorer.REPORT_DELIVERED";

	// this action generated a beacon
	public static final String ACTION_GENERATE_BEACON = "de.tubs.ibr.dtn.ruralexplorer.GENERATE_BEACON";
	
	// beacon parameters
	public static final String EXTRA_BEACON_EMERGENCY = "de.tubs.ibr.dtn.ruralexplorer.BEACON_EMERGENCY";
	
	// The communication with the DTN service is done using the DTNClient
	private DTNClient mClient = null;

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();

		if (de.tubs.ibr.dtn.Intent.RECEIVE.equals(action))
		{
			// Received bundles from the DTN service here
			try {
				// We loop here until no more bundles are available
				// (queryNext() returns false)
				while (mClient.getSession().queryNext());
			} catch (SessionDestroyedException e) {
				Log.e(TAG, "Can not query for bundle", e);
			} catch (InterruptedException e) {
				Log.e(TAG, "Can not query for bundle", e);
			}
		}
		else if (MARK_DELIVERED_INTENT.equals(action))
		{
			// retrieve the bundle ID of the intent
			BundleID bundleid = intent.getParcelableExtra(EXTRA_BUNDLEID);

			try {
				// mark the bundle ID as delivered
				mClient.getSession().delivered(bundleid);
			} catch (Exception e) {
				Log.e(TAG, "Can not mark bundle as delivered.", e);
			}
		}
		else if (REPORT_DELIVERED_INTENT.equals(action))
		{
			// retrieve the source of the status report
			SingletonEndpoint source = intent.getParcelableExtra("source");

			// retrieve the bundle ID of the intent
			BundleID bundleid = intent.getParcelableExtra(EXTRA_BUNDLEID);

			Log.d(TAG,
					"Status report received for " + bundleid.toString() + " from "
							+ source.toString());
		}
		else if (ACTION_GENERATE_BEACON.equals(action))
		{
			Log.d(TAG, "send beacon");
			try {
				mClient.getSession().send(RURAL_GROUP_DTN_EID, 20, "Hello World".getBytes());
			} catch (SessionDestroyedException e) {
				Log.e(TAG, "Can not query for bundle", e);
			} catch (InterruptedException e) {
				Log.e(TAG, "Can not query for bundle", e);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// create a new DTN client
		mClient = new DTNClient(new SessionConnection() {
			@Override
			public void onSessionConnected(Session session) {
				Log.d(TAG, "Session connected");
				session.setDataHandler(mDataHandler);
			}

			@Override
			public void onSessionDisconnected() {
				Log.d(TAG, "Session disconnected");
			}
		});

		// create registration with "rural-explorer" as endpoint
		Registration registration = new Registration("rural-explorer");

		// additionally join rural broadcast groups
		registration.add(RURAL_GROUP_DTN_EID);
		registration.add(RURAL_GROUP_IPN_EID);

		try {
			// initialize the connection to the DTN service
			mClient.initialize(this, registration);
			Log.d(TAG, "Connection to DTN service established.");
		} catch (ServiceNotAvailableException e) {
			// The DTN service has not been found
			Log.e(TAG, "DTN service unavailable. Is IBR-DTN installed?", e);
		} catch (SecurityException e) {
			// The service has not been found
			Log.e(TAG,
					"The app has no permission to access the DTN service. It is important to install the DTN service first and then the app.",
					e);
		}
	}

	@Override
	public void onDestroy() {
		// terminate the DTN service
		mClient.terminate();
		mClient = null;

		super.onDestroy();
	}

	/**
	 * This data handler is used to process incoming bundles
	 */
	private DataHandler mDataHandler = new DataHandler() {

		private Bundle mBundle = null;

		@Override
		public void startBundle(Bundle bundle) {
			// store the bundle header locally
			mBundle = bundle;
		}

		@Override
		public void endBundle() {
			// complete bundle received
			BundleID received = new BundleID(mBundle);

			// mark the bundle as delivered
			Intent i = new Intent(CommService.this, CommService.class);
			i.setAction(MARK_DELIVERED_INTENT);
			i.putExtra(EXTRA_BUNDLEID, received);
			startService(i);

			// free the bundle header
			mBundle = null;
			
			// process the data
			Database.getInstance().process(CommService.this, mBundle);
		}

		@Override
		public TransferMode startBlock(Block block) {
			// we are only interested in payload blocks (type = 1)
			if (block.type == 1) {
				// return SIMPLE mode to received the payload as "payload()"
				// calls
				return TransferMode.SIMPLE;
			} else {
				// return NULL to discard the payload of this block
				return TransferMode.NULL;
			}
		}

		@Override
		public void endBlock() {
			// nothing to do here.
		}

		@Override
		public ParcelFileDescriptor fd() {
			// This method is used to hand-over a file descriptor to the
			// DTN service. We do not need the method here and always return
			// null.
			return null;
		}

		@Override
		public void payload(byte[] data) {
			// payload is received here
			Log.d(TAG, "payload received: " + data);
		}

		@Override
		public void progress(long offset, long length) {
			// if payload is written to a file descriptor, the progress
			// will be announced here
			Log.d(TAG, offset + " of " + length + " bytes received");
		}
	};
	
	public CommService() {
		super(TAG);
	}
}
