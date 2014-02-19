package de.tubs.ibr.dtn.ruralexplorer.data;

import android.content.Context;
import android.database.Cursor;
import de.tubs.ibr.dtn.ruralexplorer.backend.NodeAdapter;

public class AccelerationData {
	public static final String ACCELERATION_X = "acceleration_x";
	public static final String ACCELERATION_Y = "acceleration_y";
	public static final String ACCELERATION_Z = "acceleration_z";
	
	private float[] mData = { 0.0f, 0.0f, 0.0f };
	
	public AccelerationData() {
	}
	
	public AccelerationData(Context context, Cursor cursor, NodeAdapter.ColumnsMap cmap) {
		mData[0] = cursor.getFloat(cmap.mColumnAccelerationX);
		mData[1] = cursor.getFloat(cmap.mColumnAccelerationY);
		mData[2] = cursor.getFloat(cmap.mColumnAccelerationZ);
	}

	public float[] getData() {
		return mData;
	}

	public void setData(float[] data) {
		mData = data;
	}
}
