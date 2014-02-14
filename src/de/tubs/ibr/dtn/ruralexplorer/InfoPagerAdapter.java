package de.tubs.ibr.dtn.ruralexplorer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class InfoPagerAdapter extends FragmentStatePagerAdapter {
	
	private NodeManager mNodeManager = null;

	public InfoPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void setNodeManager(NodeManager nm) {
		mNodeManager = nm;
	}

	@Override
	public Fragment getItem(int position) {
		Node n = mNodeManager.get(position);
		return NodeInfoFragment.newInstance(n);
	}

	@Override
	public int getCount() {
		if (mNodeManager == null) {
			return 0;
		}
		return mNodeManager.getCount();
	}
	
	public void add(Node n) {
		Log.d("InfoPagerAdapter", "Node added: " + n.toString());
		notifyDataSetChanged();
	}
	
	public void remove(Node n) {
		Log.d("InfoPagerAdapter", "Node removed: " + n.toString());
		notifyDataSetChanged();
	}
	
	public void updated(Node n) {
		Log.d("InfoPagerAdapter", "Node updated: " + n.toString());
	}
}
