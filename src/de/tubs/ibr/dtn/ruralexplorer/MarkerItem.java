package de.tubs.ibr.dtn.ruralexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.Marker;

public class MarkerItem extends RelativeLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "MarkerItem";
	
	private Marker mMarker = null;
	
    public MarkerItem(Context context) {
        super(context);
    }

    public MarkerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Long getMarkerId() {
    	return mMarker.getId();
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
	
	public void bind(Marker m, int position) {
		mMarker = m;
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
