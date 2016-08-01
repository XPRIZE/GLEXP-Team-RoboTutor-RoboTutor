package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

/**
 * Created by dexte on 7/27/2016.
 */
public class CAsm_Text extends TextView implements IEventListener {

    public boolean isWritable;

    public boolean isClicked;

    public CAsm_Text(Context context) {

        super(context);
        init();
    }

    public CAsm_Text(Context context, AttributeSet attrs) {

        super(context, attrs);
        init();
    }

    public CAsm_Text(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init();
    }

    private void init() {

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

    public void reset() {

        setEnabled(false);
        isWritable = false;
        setTextColor(Color.BLACK);
        setAlpha(.5f);
        setBackground(null);
        setPaintFlags(0);
        setTypeface(null);

    }

    public void setResult(){

        setEnabled(true);
        setBackground(getResources().getDrawable(R.drawable.back));
        isWritable = true;

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
    }
}
