package cmu.xprize.comp_numberscale;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;


/**
 * Created by yuanx on 2/26/2018.
 */

public class CNumberScale_player extends LinearLayout {
    public Context _context;
    private CNumberScale_Component _component;
    private TextView decrement;
    private TextView increment;
    private TextView displaynumber;


    public CNumberScale_player(Context context) {
        super(context);
        init(context, null);
    }

    public CNumberScale_player(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CNumberScale_player(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        _context = context;


    }

    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            System.out.print(x);
            System.out.print(y);
            return true;

        }

        return false;
    }
}
