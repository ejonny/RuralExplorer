package de.tubs.ibr.dtn.ruralexplorer.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import de.tubs.ibr.dtn.api.SDNV;

public class ExplorerBeacon implements Parcelable {
	
	private final static int TLV_TYPE_BEACON 	= 128;
	private final static int TLV_TYPE_GPS 		= 129;
	private final static int TLV_TYPE_ACC 		= 130;
	private final static int TLV_TYPE_SENSOR 	= 131;
	private final static int TLV_TYPE_RESCUE 	= 132;

	// Beacon
	private int mType = 0;
	private String mName = null;
	
	// GPS
	private Location mPosition = null;
	
	// Acceleration
	private boolean mHasAcceleration = false;
	private float[] mAcceleration = { 0.0f, 0.0f, 0.0f };
	
	// Sensors: Pressure, Temperature
	private boolean mHasSensors = false;
	private float[] mSensors = { 0.0f, 0.0f };
	
	// Rescue location
	private Location mRescueLocation = null;
	
	public ExplorerBeacon() {
		// empty constructor required
		
		// set type to ANDROID (value: 1)
		mType = 1;
	}
	
	public ExplorerBeacon(int type) {
		mType = type;
	}

	public int getType() {
		return mType;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public Location getPosition() {
		return mPosition;
	}

	public void setPosition(Location position) {
		mPosition = position;
	}

	public float[] getAcceleration() {
		return mAcceleration;
	}

	public void setAcceleration(float[] acceleration) {
		mHasAcceleration = true;
		mAcceleration = acceleration;
	}

	public float[] getSensors() {
		return mSensors;
	}

	public void setSensors(float[] sensors) {
		mHasSensors = true;
		mSensors = sensors;
	}

	public Location getRescueLocation() {
		return mRescueLocation;
	}

	public void setRescueLocation(Location rescueLocation) {
		mRescueLocation = rescueLocation;
	}

	public static void write(DataOutputStream out, ExplorerBeacon b) throws IOException {
		int items = 0;
		Location l = b.getPosition();
		
		// we plan to add location as an item
		if (l != null) items++;
		
		// number of services: beacon + location
		SDNV.Write(out, items);

		// create a TLV encoder
		TypeLengthValue.Encoder encoder = new TypeLengthValue.Encoder(out);
		
		/**
		 * BEACON
		 */
		int len = (b.mName == null) ? 5 : 4 + SDNV.getEncodedLength(b.mName.length()) + b.mName.length();
		encoder.write(TLV_TYPE_BEACON, len);
		
		// encode type
		encoder.writeFixed16(b.mType);
		
		if (b.mName == null) {
			// encode empty string
			encoder.write("");
		} else {
			// encode string
			encoder.write(b.mName);
		}
		
		/**
		 * GPS
		 */
		if (l != null) {
			encoder.write(TLV_TYPE_GPS, 16);
			encoder.write((float)l.getLatitude());
			encoder.write((float)l.getLongitude());
			encoder.write((float)l.getAltitude());
			encoder.write(l.getBearing());
		}
	}
	
	public static ExplorerBeacon parse(DataInputStream in) throws IOException {
		// read the number of items
		int items = SDNV.Read(in);
		
		// create a TLV decoder
		TypeLengthValue.Decoder decoder = new TypeLengthValue.Decoder(in);
		
		ExplorerBeacon ret = new ExplorerBeacon();
		
		for (int i = 0; i < items; i++) {
			switch (decoder.next()) {
				case TLV_TYPE_BEACON:
					// open TLV data
					decoder.open();
					
					ret.mType = (Integer)decoder.read();
					ret.mName = (String)decoder.read();
					break;
					
				case TLV_TYPE_GPS:
					Location l = new Location("beacon");
					l.setLatitude((Float)decoder.read());
					l.setLongitude((Float)decoder.read());
					l.setAltitude((Float)decoder.read());
					l.setBearing((Float)decoder.read());
					ret.setPosition(l);
					break;
					
//				case TLV_TYPE_ACC:
//					break;
//					
//				case TLV_TYPE_SENSOR:
//					break;
//					
//				case TLV_TYPE_RESCUE:
//					break;
					
				default:
					// skip the current TLV
					decoder.skip();
					break;
			}
		}
		
		return ret;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		boolean nullMarker[] = {
			mName != null,
			mPosition != null,
			mHasAcceleration,
			mHasSensors,
			mRescueLocation != null
		};
		
		dest.writeInt(mType);
		
		// write null marker
		dest.writeBooleanArray(nullMarker);
		
		if (nullMarker[0]) dest.writeString(mName);
		if (nullMarker[1]) dest.writeParcelable(mPosition, flags);
		if (nullMarker[2]) dest.writeFloatArray(mAcceleration);
		if (nullMarker[3]) dest.writeFloatArray(mSensors);
		if (nullMarker[4]) dest.writeParcelable(mRescueLocation, flags);
	}
	
	public static final Creator<ExplorerBeacon> CREATOR = new Creator<ExplorerBeacon>() {
		public ExplorerBeacon createFromParcel(final Parcel source) {
			// create a new beacon
			ExplorerBeacon ret = new ExplorerBeacon();
			
			// read type
			ret.mType = source.readInt();
			
			// read null marker array
			boolean nullMarker[] = { false, false, false, false, false };
			source.readBooleanArray(nullMarker);
			
			ret.mName = nullMarker[0] ? source.readString() : null;
			ret.mPosition = nullMarker[1] ? (Location)source.readParcelable(Location.class.getClassLoader()) : null;
			
			if (nullMarker[2]) {
				source.readFloatArray(ret.mAcceleration);
				ret.mHasAcceleration = true;
			} else {
				ret.mAcceleration = new float[] { 0.0f, 0.0f, 0.0f };
				ret.mHasAcceleration = false;
			}
			
			if (nullMarker[3]) {
				source.readFloatArray(ret.mSensors);
				ret.mHasSensors = true;
			} else {
				ret.mSensors = new float[] { 0.0f, 0.0f };
				ret.mHasSensors = false;
			}
			
			ret.mRescueLocation = nullMarker[4] ? (Location)source.readParcelable(Location.class.getClassLoader()) : null;

			return null;
		}

		@Override
		public ExplorerBeacon[] newArray(int size) {
			return new ExplorerBeacon[size];
		}
	};
}
