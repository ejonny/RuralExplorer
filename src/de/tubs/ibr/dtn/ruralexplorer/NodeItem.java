package de.tubs.ibr.dtn.ruralexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import de.tubs.ibr.dtn.ruralexplorer.backend.Node;

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

//		mImage = (ImageView)findViewById(R.id.icon);
//		mLabel = (TextView)findViewById(R.id.label);
//		mBottomtext = (TextView)findViewById(R.id.bottomtext);
//		mHint = (ImageView)findViewById(R.id.hinticon);
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
//		mLabel.setText(mBuddy.getNickname());
//		mImage.setImageResource(R.drawable.online);
//		
//		String presence = mBuddy.getPresence();
//		
//		if (presence != null)
//		{
//			if (presence.equalsIgnoreCase("unavailable"))
//			{
//				mImage.setImageResource(R.drawable.offline);
//			}
//			else if (presence.equalsIgnoreCase("xa"))
//			{
//				mImage.setImageResource(R.drawable.xa);
//			}
//			else if (presence.equalsIgnoreCase("away"))
//			{
//				mImage.setImageResource(R.drawable.away);
//			}
//			else if (presence.equalsIgnoreCase("dnd"))
//			{
//				mImage.setImageResource(R.drawable.busy);
//			}
//			else if (presence.equalsIgnoreCase("chat"))
//			{
//				mImage.setImageResource(R.drawable.online);
//			}
//		}
//		
//		// if the presence is older than 60 minutes then mark the buddy as offline
//		if (!mBuddy.isOnline())
//		{
//			mImage.setImageResource(R.drawable.offline);
//		}
//		
//		if (mBuddy.getStatus() != null)
//		{
//			if (mBuddy.getStatus().length() > 0) { 
//				mBottomtext.setText(mBuddy.getStatus());
//			} else {
//				mBottomtext.setText(mBuddy.getEndpoint());
//			}
//		}
//		else
//		{
//			mBottomtext.setText(mBuddy.getEndpoint());
//		}
//		
//		if (mBuddy.hasDraft()) {
//			mHint.setVisibility(View.VISIBLE);
//			mHint.setImageResource(R.drawable.ic_draft);
//		} else {
//			mHint.setVisibility(View.GONE);
//		}
//		
//		if (mBuddy.getCountry() != null) {
//		    String resourceName = "ic_flag_" + mBuddy.getCountry().toLowerCase();
//		    
//		    Log.d(TAG, "Search for " + resourceName);
//		    int flagsId = getResources().getIdentifier(resourceName, "drawable", getContext().getPackageName());
//		    
//		    if (flagsId == 0) {
//		        mLabel.setCompoundDrawables(null, null, null, null);
//		    } else {
//		        mLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(flagsId), null);
//		    }
//		} else {
//		    mLabel.setCompoundDrawables(null, null, null, null);
//		}
//		
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			if (this.isActivated()) {
//				mHint.setVisibility(View.VISIBLE);
//				mHint.setImageResource(R.drawable.ic_selected);
//			}
//		}
	}
}
