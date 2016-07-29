package cmu.xprize.asm_component;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

/**
 * Created by dexte on 7/27/2016.
 */
public class Write_Text extends TextView implements IEventListener {

    public boolean isWritable;

    public boolean isClicked;

    public Write_Text(Context context) {
        super(context);
        isWritable = false;
        this.setFocusable(false);
    }

    public Write_Text(Context context, AttributeSet attrs) {
        super(context, attrs);
        isWritable = false;
        this.setFocusable(false);
    }

    public Write_Text(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        isWritable = false;
        this.setFocusable(false);
    }

    public void disableWrite() {
        isWritable = false;
    }

    public void enableWrite() {isWritable = true;}

    public boolean getIsClicked() {
        if (isClicked) {
            isClicked = false;
            return true;
        } else return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            if (isWritable) {
                isClicked = true;
            }
        }
        return false;
    }

    public void onEvent(IEvent event) {
        String response  = (String)event.getString(TCONST.FW_VALUE);
        this.setText(response);
        View Component = (View)this.getParent().getParent().getParent();
    }
}
