
package de.tubs.ibr.dtn.ruralexplorer;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v4.content.AsyncTaskLoader;
import de.tubs.ibr.dtn.ruralexplorer.backend.DataService;
import de.tubs.ibr.dtn.ruralexplorer.backend.Database;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class NodeLoader extends AsyncTaskLoader<LinkedList<Node>> {

	private DataService mService = null;
	private LinkedList<Node> mData = null;
	private Boolean mObserving = false;
	
	public NodeLoader(Context context, DataService service) {
		super(context);
		mService = service;
	}

	@Override
	public LinkedList<Node> loadInBackground() {
		SQLiteDatabase db = mService.getDatabase().raw();
		
		LinkedList<Node> nodes = new LinkedList<Node>();

		// load all markers
		Cursor c = db.query(Database.TABLE_NAME_NODES, NodeAdapter.PROJECTION, null, null, null, null, null);
		
		// create a field map
		NodeAdapter.ColumnsMap map = new NodeAdapter.ColumnsMap(c);
		
		// own location
		Location myLocation = mService.getLocation();
		
		while (c.moveToNext()) {
			// create a new node object
			Node n = new Node(getContext(), c, myLocation, map);
			
			// add to list
			nodes.push(n);
		}
		
		// sort by distance
		Collections.sort(nodes, mDistanceComparator);
		
		return nodes;
	}
	
	private Comparator<Node> mDistanceComparator = new Comparator<Node>() {
		@Override
		public int compare(Node lhs, Node rhs) {
			if (lhs.hasDistance() && !rhs.hasDistance()) {
				return -1;
			}
			else if (!lhs.hasDistance() && rhs.hasDistance()) {
				return 1;
			}
			else if (lhs.hasDistance() && rhs.hasDistance())
			{
				if (lhs.getDistance() < rhs.getDistance()) return -1;
				if (lhs.getDistance() > rhs.getDistance()) return 1;
			}
			
			// if equal, fall-back to natural order
			return lhs.compareTo(rhs);
		}
	};

	@Override
	public void onCanceled(LinkedList<Node> data) {
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(data);
	}

	@Override
	public void deliverResult(LinkedList<Node> data) {
		if (isReset()) {
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			return;
		}

		// Hold a reference to the old data so it doesn't get garbage collected.
		// We must protect it until the new data has been delivered.
		mData = data;

		if (isStarted()) {
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(data);
		}
	}

	@Override
	protected void onReset() {
		// Ensure the loader has been stopped.
		onStopLoading();

		// At this point we can release the resources associated with 'mData'.
		mData = null;

		// The Loader is being reset, so we should stop monitoring for changes.
		if (mObserving) {
			getContext().unregisterReceiver(mDataUpdateListener);
			mObserving = false;
		}
	}

	@Override
	protected void onStartLoading() {
		if (mData != null) {
			// Deliver any previously loaded data immediately.
			deliverResult(mData);
		}

		// Begin monitoring the underlying data source.
		IntentFilter filter = new IntentFilter(Database.DATA_UPDATED);
		filter.addAction(DataService.LOCATION_UPDATED);
		getContext().registerReceiver(mDataUpdateListener, filter);

		mObserving = true;

		if (takeContentChanged() || mData == null) {
			// When the observer detects a change, it should call
			// onContentChanged()
			// on the Loader, which will cause the next call to
			// takeContentChanged()
			// to return true. If this is ever the case (or if the current data
			// is
			// null), we force a new load.
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		// The Loader is in a stopped state, so we should attempt to cancel the
		// current load (if there is one).
		cancelLoad();

		// Note that we leave the observer as is. Loaders in a stopped state
		// should still monitor the data source for changes so that the Loader
		// will know to force a new load if it is ever started again.
	}

	private BroadcastReceiver mDataUpdateListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Database.DATA_UPDATED.equals(intent.getAction())) {
				onContentChanged();
			}
			else if (DataService.LOCATION_UPDATED.equals(intent.getAction())) {
				onContentChanged();
			}
		}
	};
}
