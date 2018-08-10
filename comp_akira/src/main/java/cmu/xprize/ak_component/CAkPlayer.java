package cmu.xprize.ak_component;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by jacky on 2016/7/1.
 */

public class CAkPlayer extends LinearLayout{
    public enum Lane{LEFT, MID, RIGHT, SIGH1, SIGH2L, SIGH2R, SIGH3L, SIGH3M, SIGH3R}
    public String[] choices;
    protected boolean isPlaying;
    public int score;
    public String rearString;
    public Lane lane = Lane.MID;
    public float textSize;

    private Drawable car_left, car_mid, car_right;
    private AnimationDrawable fishTail;

    public TextView aboveTextView;
    public TextView belowTextView;
    public ImageView carImage;
    protected int deviceX;
    protected int deviceY;

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

        setOrientation(VERTICAL);

        aboveTextView = new TextView(context);
        belowTextView = new TextView(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        AnimatorSet tmp;
        if(size.x > 1400){
            aboveTextView.setTextSize(32);
            belowTextView.setTextSize(32);
            aboveTextView.setTypeface(null, Typeface.BOLD);
            belowTextView.setTypeface(null, Typeface.BOLD);
            aboveTextView.setTextColor(Color.GREEN);
            belowTextView.setTextColor(Color.GREEN);
            textSize = 32;
        }else {
            aboveTextView.setTextSize(22);
            belowTextView.setTextSize(22);
            aboveTextView.setTypeface(null, Typeface.BOLD);
            belowTextView.setTypeface(null, Typeface.BOLD);
            aboveTextView.setTextColor(Color.GREEN);
            belowTextView.setTextColor(Color.GREEN);
            textSize = 22;
        }


        aboveTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);


        belowTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        carImage = new ImageView(context);
        carImage.setImageDrawable(car_mid);

        addView(aboveTextView);
        addView(carImage);
        addView(belowTextView);

        setGravity(Gravity.CENTER_VERTICAL);

        setWillNotDraw(false);

//        int h = car_mid.getIntrinsicHeight();
//        int w = car_mid.getIntrinsicWidth();
//        car_mid.setBounds(0, 0, (int)(w * 0.7), (int)(h * 0.7));
//        setCompoundDrawables(null, car_mid,
//                null, null);
//
//        car_left.setBounds(0, 0, w, h);
//        car_right.setBounds(0, 0, w, h);
//        fishTail.setBounds(0, 0, w, h);

    }

    public void update() {
        if(getLayoutParams() != null)
            params = (PercentRelativeLayout.LayoutParams) getLayoutParams();
        if(params != null) {
            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
            switch (lane) {
                case LEFT:
                    info.leftMarginPercent = 0.30f;
                    //Make sure fishTail animation is finished
                    if(carImage.getDrawable() != fishTail)
                        carImage.setImageDrawable(car_left);
                    break;
                case MID:
                    info.leftMarginPercent = 0.45f;
                    if(carImage.getDrawable() != fishTail)
                        carImage.setImageDrawable(car_mid);
                    break;
                case RIGHT:
                    info.leftMarginPercent = 0.60f;
                    if(carImage.getDrawable() != fishTail)
                        carImage.setImageDrawable(car_right);
                    break;
            }
            requestLayout();

//            ((LayoutParams)aboveTextView.getLayoutParams()).weight = 1;
//            ((LayoutParams)carImage.getLayoutParams()).weight = 3;
//            ((LayoutParams)belowTextView.getLayoutParams()).weight = 1;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        update();
        super.onDraw(canvas);
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b){
//        update();
//        int width = r - l;
//        int heigth = b - t;
//        super.onLayout(changed, l, t, r, b);
//        aboveTextView.layout(l, t, r, t + (int)(0.2 * heigth));
//        carImage.layout(l, (int)(0.2 * heigth), r, t + (int)(0.8 * heigth));
//        belowTextView.layout(l, t + (int)(0.8 * heigth), r, b);
//
//    }


    public void onTouch(MotionEvent event){
        float yratio = ((float)deviceY)/((float)1800);
        float xratio = ((float)deviceX)/((float)2560);
        float touchX=event.getX();
        float touchY = event.getY();
        if (xratio!=0 && yratio!=0){
        touchX = event.getX()/xratio; touchY = event.getY()/yratio;}

        //Change to left lane
        //float line1 = (float)(-1.018*touchY +1678);
        float line1 = (float)(-1.018*touchY +1287); // 400 pixel offset to exapnd left tap area
        float line2 = (float)(-0.3353*touchY+1398);
        float line3 = (float)(0.3736*touchY+1101);
        //float line4 = (float)(0.9898*touchY+860);
        float line4 = (float)(0.9898*touchY+1260); // 400 pixel offset to expand right tap area
        if(touchY>750){
            if (touchX>=line1 && touchX<line2){
                lane = Lane.LEFT;
                System.out.println("Left");
            } else if (touchX>=line2 && touchX<line3){
                lane = Lane.MID;
                System.out.println("Mid");
            } else if (touchX>=line3 && touchX<=line4){
                lane = Lane.RIGHT;
                System.out.println("Right");

            }
        }
        /*
        if(touchX < getX() + getWidth()/2) {

            lane = lane == Lane.RIGHT? Lane.MID: Lane.LEFT;
            System.out.println("To left");
        }
//        //change to right lane
        else {
            lane = lane == Lane.LEFT? Lane.MID : Lane.RIGHT;
            System.out.println("To right");
        }*/

    }

    public void crash() {
        final Drawable ori =carImage.getDrawable();
        carImage.setImageDrawable(fishTail);
        requestLayout();
        fishTail.start();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                fishTail.stop();
                fishTail.selectDrawable(0);
                carImage.setImageDrawable(ori);
            }
        }, 800);
    }

    public Lane getLane() {
        return lane;
    }



    public void setText(String above, String below) {
        aboveTextView.setTextSize(textSize);
        belowTextView.setTextSize(textSize);
        aboveTextView.setText(above);
        belowTextView.setText(below);
    }
}
