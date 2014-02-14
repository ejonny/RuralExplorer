
package de.tubs.ibr.dtn.ruralexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class InfoFragment extends Fragment {

	private FrameLayout mLayout = null;

	private OnInfoWindowListener mListener;

	public static InfoFragment newInstance() {
		InfoFragment fragment = new InfoFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public InfoFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_node, container, false);
		mLayout = (FrameLayout)v.findViewById(R.id.node_fragment_layout);
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnInfoWindowListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnInfoWindowListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	public interface OnInfoWindowListener {
		public void onInfoWindowStateChanged(boolean visible, int height, int width);
	}

	public void setNode(Node n) {
		if (n == null) {
			mLayout.setVisibility(View.INVISIBLE);
			mListener.onInfoWindowStateChanged(false, 0, 0);
		} else {
			mLayout.setVisibility(View.VISIBLE);
			mListener.onInfoWindowStateChanged(true, mLayout.getHeight(), mLayout.getWidth());
		}
	}
}
