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
    private TextView decrement;
    private TextView increment;
    private TextView displaynumber;
    private CNumberScale_Component _component;



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

    public void setComponent(CNumberScale_Component component) {
        _component = component;
    }

    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            System.out.println(x);
            System.out.println(y);
            System.out.println("tap");

            if (x>=320 && x<=620 && y>=420 && y<=705){
                //click on the minus button
                _component.add_delta();

                System.out.println("add");
            } else if (x>=1260 && x<=1530 && y>=450 && y<=730){
                //click on the add button
                _component.minus_delta();

                System.out.println("minus");
            } else if (x>=860 && x<=1000 && y>=65 && y<=200){
                //click on the reset button
                _component.reset_current_number();

                System.out.println("reset");
            }
            return true;

        }

        return false;
    }

}
