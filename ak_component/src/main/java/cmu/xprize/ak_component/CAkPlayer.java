package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by jacky on 2016/7/1.
 */

public class CAkPlayer extends TextView{
    protected enum Lane{LEFT, MID, RIGHT}

    protected boolean isPlaying;
    protected int score;
    protected int rearNum;
    protected Lane lane = Lane.MID;



    private PercentRelativeLayout.LayoutParams params;

    public CAkPlayer(Context context) {
        super(context);
        init(context, null);
    }

    public CAkPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAkPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        Drawable image = context.getResources().getDrawable( R.drawable.car_rear);
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        setCompoundDrawables(null, image,
                null, null);
        rearNum = new Random().nextInt(100);
        setText("" + rearNum);
    }

    public void update() {
        if(getLayoutParams() != null)
            params = (PercentRelativeLayout.LayoutParams) getLayoutParams();
        if(params != null) {
            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
            switch (lane) {
                case LEFT:
                    info.leftMarginPercent = 0.25f;
                    break;
                case MID:
                    info.leftMarginPercent = 0.45f;
                    break;
                case RIGHT:
                    info.leftMarginPercent = 0.65f;
                    break;
            }
            requestLayout();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        update();
        super.onDraw(canvas);
    }


    public void onTouchEvent(MotionEvent event, float scaleFactorX){
        float touchX = event.getX();
        //Change to left lane
        if(touchX < getX() + getWidth()/2) {
            lane = lane == Lane.RIGHT? Lane.MID : Lane.LEFT;
            System.out.println("To left");
        }
//        //change to right lane
        else {
            lane = lane == Lane.LEFT? Lane.MID : Lane.RIGHT;
            System.out.println("To right");
        }
    }

    public Lane getLane() {
        return lane;
    }


}
