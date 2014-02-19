package de.tubs.ibr.dtn.ruralexplorer;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;
import de.tubs.ibr.dtn.ruralexplorer.data.LocationData;

public class RescueFragment extends Fragment {
	
	private GeoTag mGeoTag = null;
	private TextView mValueText = null;
	private Location mLocation = null;
	
	public static RescueFragment newInstance(GeoTag t) {
		RescueFragment f = new RescueFragment();
		f.mGeoTag = t;
		return f;
	}
	
	public RescueFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_rescue, container, false);
		mValueText = (TextView)v.findViewById(R.id.rescue_value);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		bind(mGeoTag);
	}
	
	public GeoTag getGeoTag() {
		return mGeoTag;
	}
	
	public void setPosition(Location l) {
		mLocation = l;
		bind(mGeoTag);
	}

	public void bind(GeoTag t) {
		mGeoTag = t;
		
		if ((mValueText == null) || (t == null)) return;
		
		LocationData loc_data = t.getLocation();
		
		if (loc_data.hasLatitude() && loc_data.hasLongitude() && (mLocation != null)) {
			Location l = new Location("stored");
			l.setLatitude(loc_data.getLatitude());
			l.setLongitude(loc_data.getLongitude());
			
			if (loc_data.hasAltitude()) {
				l.setAltitude(loc_data.getAltitude());
			}
			
			Float distance = l.distanceTo(mLocation);
			mValueText.setText(String.format(getString(R.string.data_unit_meters), distance));
		} else {
			mValueText.setText(getString(R.string.data_na));
		}
	}
}
