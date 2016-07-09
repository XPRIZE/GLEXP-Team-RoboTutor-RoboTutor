package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jacky on 2016/7/1.
 */

public class Player extends GameObject {
    protected enum Lane{LEFT, MID, RIGHT}

    protected boolean isPlaying;
    protected int score;
    protected int rearNum;
    private Lane lane;

    private Drawable mDrawable;
    private int width = 75, height = 75;


    private Rect mViewRegion = new Rect();
    public Player(Context context) {
        init(context, null);
    }


    protected void init(Context context, AttributeSet attrs) {
        mDrawable = ContextCompat.getDrawable(context, R.drawable.car_rear);
        mDrawable.setBounds(450,450,525,525);
        x = 450;
        y = 450;
        this.lane = Lane.MID;
    }


//    protected void onDraw(Canvas canvas) {
//
//        mDrawable.draw(canvas);
//    }

    //    public Player(Bitmap res, int w, int h) {
//        image = res;
//        width = w;
//        height = h;
//        rearNum = 14;
//        lane = Lane.MID;
//        x = 487 - width / 2;
//        y = 450;
//    }
//
    public void update() {
        switch (lane) {
            case LEFT:
                x = 330 - width / 2;
                break;
            case MID:
                x = 487 - width / 2;
                break;
            case RIGHT:
                x = 667 - width / 2;
                break;
        }

    }
//
    public void draw(Canvas canvas) {
        mDrawable.setBounds(x, y, x + width, y + height);
        mDrawable.draw(canvas);
    }

//
    public void onTouchEvent(MotionEvent event, float scaleFactorX){
        float touchX = event.getX() / scaleFactorX;
        //Change to left lane
        if(touchX < x + width / 2) {
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
