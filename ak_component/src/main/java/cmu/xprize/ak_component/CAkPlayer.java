package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by jacky on 2016/7/1.
 */

public class CAkPlayer extends TextView{
    public enum Lane{LEFT, MID, RIGHT}

    protected boolean isPlaying;
    public int score;
    public String rearString;
    public Lane lane = Lane.MID;

    private Drawable car_left, car_mid, car_right;
    private AnimationDrawable fishTail;

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
        car_mid = context.getResources().getDrawable(R.drawable.car_mid);
        car_left = context.getResources().getDrawable(R.drawable.car_left);
        car_right = context.getResources().getDrawable(R.drawable.car_right);
        fishTail = (AnimationDrawable) context.getResources().getDrawable(R.drawable.fishtail_animation);

        int h = car_mid.getIntrinsicHeight();
        int w = car_mid.getIntrinsicWidth();
        car_mid.setBounds(0, 0, (int)(w * 0.7), (int)(h * 0.7));
        setCompoundDrawables(null, car_mid,
                null, null);

        car_left.setBounds(0, 0, w, h);
        car_right.setBounds(0, 0, w, h);
        fishTail.setBounds(0, 0, w, h);

    }

    public void update() {
        if(getLayoutParams() != null)
            params = (PercentRelativeLayout.LayoutParams) getLayoutParams();
        if(params != null) {
            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
            switch (lane) {
                case LEFT:
                    info.leftMarginPercent = 0.20f;

                    //Make sure fishTail animation is finished
                    if(getCompoundDrawables()[1] != fishTail)
                        setCompoundDrawables(null, car_left,
                            null, null);
                    break;
                case MID:
                    info.leftMarginPercent = 0.40f;
                    if(getCompoundDrawables()[1] != fishTail)
                        setCompoundDrawables(null, car_mid,
                            null, null);
                    break;
                case RIGHT:
                    info.leftMarginPercent = 0.60f;
                    if(getCompoundDrawables()[1] != fishTail)
                        setCompoundDrawables(null, car_right,
                            null, null);
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


    public void onTouch(MotionEvent event){
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

    public void crash() {
        final Drawable ori = getCompoundDrawables()[1];
        setCompoundDrawables(null, fishTail, null, null);
        requestLayout();
        fishTail.start();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                fishTail.stop();
                fishTail.selectDrawable(0);
                setCompoundDrawables(null, ori, null, null);
            }
        }, 800);
    }

    public Lane getLane() {
        return lane;
    }




}
