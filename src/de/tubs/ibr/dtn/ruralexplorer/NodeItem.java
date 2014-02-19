package de.tubs.ibr.dtn.ruralexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.data.Node;

public class NodeItem extends RelativeLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "NodeItem";
	
	private Node mNode = null;
	
    public NodeItem(Context context) {
        super(context);
    }

    public NodeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Long getNodeId() {
    	return mNode.getId();
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
	
	public void bind(Node n, int position) {
		mNode = n;
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
