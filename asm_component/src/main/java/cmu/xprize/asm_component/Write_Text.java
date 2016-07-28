package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
        setOnClickListener(clickListener);
        this.setFocusable(false);
    }

    public Write_Text(Context context, AttributeSet attrs) {
        super(context, attrs);
        isWritable = false;
        setOnClickListener(clickListener);
        this.setFocusable(false);
    }

    public Write_Text(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        isWritable = false;
        setOnClickListener(clickListener);
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

    private OnClickListener clickListener = new OnClickListener(){
        public void onClick(View v) {
            try {
                Write_Text t = (Write_Text) v;
                if (t.isWritable) {
                    t.isClicked = true;
                }
                CAsm_Text c = (CAsm_Text)t.getParent();
                c.setIsClicked(true);
                View Component = (View)v.getParent().getParent().getParent();
                Component.performClick();
            } catch (ClassCastException e) {
                return;
            }
    };

};

}
