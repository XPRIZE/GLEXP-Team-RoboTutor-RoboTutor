package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import android.graphics.Color;
import static cmu.xprize.ak_component.CAkTeachFinger.Lane.LEFT;
import static cmu.xprize.ak_component.CAkTeachFinger.Lane.MID;
import static cmu.xprize.ak_component.CAkTeachFinger.Lane.RIGHT;

/**
 * Created by Iris on 16/7/7.
 */

public class CAkTeachFinger extends TextView {
    public CAkTeachFinger(Context context) {
        super(context);
        init(context, null);
    }

    public CAkTeachFinger(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAkTeachFinger(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected enum Lane{LEFT, MID, RIGHT};
    private Lane lane;
    private CAkPlayer.Lane PLane;

    private String words= "Try touching here to change lane";
    protected enum FirstRight{TRUE,FALSE,DONE};
    protected enum SecondLeft{TRUE,FALSE,DONE};
    private FirstRight firstright=FirstRight.TRUE;
    private SecondLeft secondleft=SecondLeft.TRUE;
    private float carpos;
    public boolean finishTeaching = false;

    private PercentRelativeLayout.LayoutParams params;

    protected void init(Context context, AttributeSet attrs) {

        Drawable image = context.getResources().getDrawable( R.drawable.finger);
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h);
        setCompoundDrawables(null, image,
                null, null);
        setText(words);

        lane = RIGHT;
        PLane = CAkPlayer.Lane.MID;
    }

    public void reset(Context context, AttributeSet attrs)
    {
        words="Try touching here to change lane";
        firstright=FirstRight.TRUE;
        secondleft=SecondLeft.TRUE;
        finishTeaching = false;
        init(context,attrs);
    }

    public void update() {
        if(getLayoutParams() != null)
            params = (PercentRelativeLayout.LayoutParams) getLayoutParams();
        if(params != null) {
            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
            switch (lane) {
                case LEFT:
                    info.leftMarginPercent = 0.20f;
                    break;
                case MID:
                    info.leftMarginPercent = 0.34f;
                    break;
                case RIGHT:
                    info.leftMarginPercent = 0.52f;
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


    public void onTouch(MotionEvent event, CAkPlayer CAkPlayer){
        int touchX = (int)event.getX();
        int width = getWidth();
        PLane=CAkPlayer.getLane();

        if(touchX >= CAkPlayer.getX() + width / 2 &&
                firstright==FirstRight.TRUE && PLane== cmu.xprize.ak_component.CAkPlayer.Lane.MID) {
            firstright=FirstRight.DONE;
            words="Excellent!Now try this way";
            //CAkPlayer.lane = cmu.xprize.ak_component.CAkPlayer.Lane.RIGHT;
            PLane= cmu.xprize.ak_component.CAkPlayer.Lane.RIGHT;
            lane = MID;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(touchX < CAkPlayer.getX() + width / 2 &&
                firstright == FirstRight.TRUE &&
                PLane == cmu.xprize.ak_component.CAkPlayer.Lane.MID) {
            PLane = cmu.xprize.ak_component.CAkPlayer.Lane.LEFT;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(touchX >= CAkPlayer.getX() + width / 2 &&
                firstright == FirstRight.TRUE && PLane == cmu.xprize.ak_component.CAkPlayer.Lane.LEFT) {
            PLane = cmu.xprize.ak_component.CAkPlayer.Lane.MID;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.TRUE &&
                touchX < CAkPlayer.getX() + width / 2){
            words="Great!And this way?";
            secondleft=SecondLeft.DONE;
            PLane = cmu.xprize.ak_component.CAkPlayer.Lane.MID;
            lane = LEFT;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX < CAkPlayer.getX() + width / 2 &&
                PLane == cmu.xprize.ak_component.CAkPlayer.Lane.MID)
        {
            words="Great!Let's get start?Choose the correct lane!";
            finishTeaching=true;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX < CAkPlayer.getX() + width / 2 &&
                CAkPlayer.lane == cmu.xprize.ak_component.CAkPlayer.Lane.RIGHT)
        {
            PLane = cmu.xprize.ak_component.CAkPlayer.Lane.MID;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX >= CAkPlayer.getX() + width / 2)
        {
            PLane = cmu.xprize.ak_component.CAkPlayer.Lane.RIGHT;
            //CAkPlayer.update();
            update();
            invalidate();
        }
        setText(words);
    }
    public void setPostion(CAkPlayer.Lane rightlane){
        setTextColor(Color.argb(0, 0, 255, 0));
        if(rightlane == CAkPlayer.Lane.LEFT)
            lane = LEFT;
        else if(rightlane == CAkPlayer.Lane.MID)
            lane = MID;
        else
            lane = RIGHT;
        update();
    }
}
