package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 *
 */
public class CAsm_Dot_New extends ImageView {

    private Context context;

    private String imageName;
    int row, col;

    private boolean isClickable = true;
    private boolean isClicked = false;
    private boolean isHollow = false;

    static final private String TAG ="Dot";


    public CAsm_Dot_New(Context context) {
        super(context);
        this.context = context;
    }

    public CAsm_Dot_New(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
    }

    public CAsm_Dot_New(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
    }
}
