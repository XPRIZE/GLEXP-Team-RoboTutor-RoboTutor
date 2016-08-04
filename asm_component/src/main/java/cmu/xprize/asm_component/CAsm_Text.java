package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
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
 *
 */
public class CAsm_Text extends TextView implements IEventListener {

    public boolean isWritable = false;
    public boolean isClicked = false;

    private boolean isStruck = false;
    private boolean isBorrowable = false;

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

        setTextColor(Color.BLACK);
        //setAlpha(.5f);
        setBackground(null);
        setPaintFlags(0);
        setTypeface(null);

        isStruck = false;
        isWritable = false;
        isBorrowable = false;
        isClicked = false;

    }

    public void setResult(){

        setEnabled(true);
        setBackground(getResources().getDrawable(R.drawable.back));
        isWritable = true;

    }

    public void setStruck(boolean _isStruck){

        this.isStruck = _isStruck;

        if (isStruck) {
            setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            setPaintFlags(getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

    }

    public boolean getIsStruck() {return this.isStruck;}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            if (isWritable || isBorrowable) {
                isClicked = true;
            }
        }
        return false;
    }

    public void onEvent(IEvent event) {
        //Called when user responds through the fingerwriter, to change the text to user's response.
        String response  = (String)event.getString(TCONST.FW_VALUE);
        this.setText(response);
    }

    public void setIsClicked(boolean _isClicked) {this.isClicked = _isClicked;}

    public void setBorrowable(boolean _isBorrowable) {this.isBorrowable = _isBorrowable;}

    public boolean getIsBorrowable() {return isBorrowable;}

    public Integer getDigit() {

        String input = getText().toString();

        if (input.equals("")) {
            return null;
        }
        else {
            try {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

    }

}
