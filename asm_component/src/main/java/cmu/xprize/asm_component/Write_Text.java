package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dexte on 7/27/2016.
 */
public class Write_Text extends TextView {

    public boolean isWritable;

    public boolean isClicked;

    public Write_Text(Context context) {
        super(context);
        isWritable = false;
    }

    public Write_Text(Context context, AttributeSet attrs) {
        super(context, attrs);
        isWritable = false;
    }

    public Write_Text(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        isWritable = false;
    }

    public void disableWrite() {
        isWritable = false;
    }

    public void enableWrite() {isWritable = true;}

    public boolean getIsClicked() {
        if (isClicked) {
            isClicked = false;
            return isClicked;
        } else return false;
    }

}
