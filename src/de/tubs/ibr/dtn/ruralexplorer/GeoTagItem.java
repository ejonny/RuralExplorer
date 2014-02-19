package de.tubs.ibr.dtn.ruralexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.data.GeoTag;

public class GeoTagItem extends RelativeLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "GeoTagItem";
	
	private GeoTag mGeoTag = null;
	
    public GeoTagItem(Context context) {
        super(context);
    }

    public GeoTagItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Long getGeoTagId() {
    	return mGeoTag.getId();
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
	
	public void bind(GeoTag m, int position) {
		mGeoTag = m;
		onDataChanged();
	}
	
	public void unbind() {
        // Clear all references to the message item, which can contain attachments and other
        // memory-intensive objects
	}
	
	@SuppressLint("NewApi")
	private void onDataChanged() {

	}
}
