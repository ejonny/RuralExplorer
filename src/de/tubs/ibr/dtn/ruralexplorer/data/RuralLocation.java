package de.tubs.ibr.dtn.ruralexplorer.data;

import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter.ColumnsMap;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;

public class RuralLocation {
	public static final String LAT = "latitude";
	public static final String LNG = "longitude";
	public static final String ALT = "altitude";
	public static final String BEARING = "bearing";
	public static final String SPEED = "speed";
	public static final String ACCURACY = "accuracy";
	
	private Double mLatitude = null;
	private Double mLongitude = null;
	private Double mAltitude = null;
	private Float mBearing = null;
	private Float mSpeed = null;
	private Float mAccurarcy = null;
	
	public RuralLocation() {
	}
	
	public RuralLocation(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap)
	{
		mLatitude = cursor.getDouble(cmap.mColumnLocationLat);
		mLongitude = cursor.getDouble(cmap.mColumnLocationLng);
		mAltitude = cursor.getDouble(cmap.mColumnLocationAlt);
		mBearing = cursor.getFloat(cmap.mColumnLocationBearing);
		mSpeed = cursor.getFloat(cmap.mColumnLocationSpeed);
		mAccurarcy = cursor.getFloat(cmap.mColumnLocationAccuracy);
	}
	
	public RuralLocation(Location l) {
		mLatitude = l.getLatitude();
		mLongitude = l.getLongitude();
		mAltitude = l.getAltitude();
		mBearing = l.getBearing();
		mSpeed = l.getSpeed();
		mAccurarcy = l.getAccuracy();
	}
	
	public Double getLatitude() {
		return mLatitude;
	}
	
	public boolean hasLatitude() {
		return (mLatitude != null);
	}

	public void setLatitude(Double latitude) {
		mLatitude = latitude;
	}

	public Double getLongitude() {
		return mLongitude;
	}
	
	public boolean hasLongitude() {
		return (mLongitude != null);
	}

	public void setLongitude(Double longitude) {
		mLongitude = longitude;
	}

	public Double getAltitude() {
		return mAltitude;
	}
	
	public boolean hasAltitude() {
		return (mAltitude != null);
	}

	public void setAltitude(Double altitude) {
		mAltitude = altitude;
	}

	public Float getBearing() {
		return mBearing;
	}
	
	public boolean hasBearing() {
		return (mBearing != null);
	}

	public void setBearing(Float bearing) {
		mBearing = bearing;
	}

	public Float getSpeed() {
		return mSpeed;
	}
	
	public boolean hasSpeed() {
		return (mSpeed != null);
	}

	public void setSpeed(Float speed) {
		mSpeed = speed;
	}

	public Float getAccurarcy() {
		return mAccurarcy;
	}
	
	public boolean hasAccurarcy() {
		return (mAccurarcy != null);
	}

	public void setAccurarcy(Float accurarcy) {
		mAccurarcy = accurarcy;
	}
}
