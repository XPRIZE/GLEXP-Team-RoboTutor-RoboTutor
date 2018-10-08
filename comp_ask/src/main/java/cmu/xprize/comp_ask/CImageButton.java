package cmu.xprize.comp_ask;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 10/8/18.
 */

public class CImageButton extends ImageButton {
    public CImageButton(Context context) {
        super(context);
    }

    public CImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        Log.wtf("BUTTON_BOY", "setting pressed of " + this.toString() + " to " + pressed);
        this.setBackgroundColor(getResources().getColor(R.color.buttonPressedColor));
    }
}
