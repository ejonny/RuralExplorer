package de.tubs.ibr.dtn.ruralexplorer.data;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;

public class LocationData {
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
	
	public LocationData() {
	}
	
	public LocationData(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap) {
		mLatitude = cursor.isNull(cmap.mColumnLocationLat) ? null : cursor.getDouble(cmap.mColumnLocationLat);
		mLongitude = cursor.isNull(cmap.mColumnLocationLng) ? null : cursor.getDouble(cmap.mColumnLocationLng);
		mAltitude = cursor.isNull(cmap.mColumnLocationAlt) ? null : cursor.getDouble(cmap.mColumnLocationAlt);
		mBearing = cursor.isNull(cmap.mColumnLocationBearing) ? null : cursor.getFloat(cmap.mColumnLocationBearing);
		mSpeed = cursor.isNull(cmap.mColumnLocationSpeed) ? null : cursor.getFloat(cmap.mColumnLocationSpeed);
		mAccurarcy = cursor.isNull(cmap.mColumnLocationAccuracy) ? null : cursor.getFloat(cmap.mColumnLocationAccuracy);
	}
	
	public LocationData(Location l) {
		if (l == null) {
			mLatitude = null;
			mLongitude = null;
			mAltitude = null;
			mBearing = null;
			mSpeed = null;
			mAccurarcy = null;
		} else {
			mLatitude = l.getLatitude();
			mLongitude = l.getLongitude();
			mAltitude = l.getAltitude();
			mBearing = l.getBearing();
			mSpeed = l.getSpeed();
			mAccurarcy = l.getAccuracy();
		}
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
