package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import static cmu.xprize.ak_component.TeachFinger.Lane.LEFT;
import static cmu.xprize.ak_component.TeachFinger.Lane.MID;

/**
 * Created by Iris on 16/7/7.
 */

public class TeachFinger extends TextView {
    public TeachFinger(Context context) {
        super(context);
        init(context, null);
    }

    public TeachFinger(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TeachFinger(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected enum Lane{LEFT, MID, RIGHT};
    private Bitmap image;
    private Lane lane;


    private ImageView imageView;

    private String words= "Try touching here to change lane";
    protected enum FirstRight{TRUE,FALSE,DONE};
    protected enum SecondLeft{TRUE,FALSE,DONE};
    private FirstRight firstright=FirstRight.TRUE;
    private SecondLeft secondleft=SecondLeft.TRUE;
    private float carpos;
    public boolean finishTeaching = false;
    int width;
    public final float CARPOSRIGHT=854 - width / 2;
    public final float CARPOSMIDDLE=640 - width / 2;
    public final float CARPOSLEFT=423 - width / 2;
    public final float FINGETPOSRIGHT=760 - width / 2;
    public final float FINGERPOSMIDDLE=630 - width / 2;
    public final float FINGERPOSLEFT=420 - width / 2;


    private PercentRelativeLayout.LayoutParams params;

    public float x, y;

//    public TeachFinger(Bitmap res,int w, int h)
//    {
////        image=res;
////        width = w;
////        height = h;
////        x = FINGETPOSRIGHT;   //first right;
////        carpos= CARPOSMIDDLE;
////        y = 500;
//    }

    protected void init(Context context, AttributeSet attrs) {
//        image = BitmapFactory.decodeResource(getResources(),R.drawable.finger);
//        imageView = new ImageView(context);
//        imageView.setImageResource(R.drawable.finger);
//        setImageResource(R.drawable.finger);

        Drawable image = context.getResources().getDrawable( R.drawable.finger);
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        setCompoundDrawables(null, image,
                            null, null);
        setText(words);

        lane = MID;
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
                    info.leftMarginPercent = 0.40f;
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


    public void onTouch(MotionEvent event, Player player){
        int touchX = (int)event.getX();
        int width = getWidth();

        if(touchX >= player.getX() + width / 2 &&
                firstright==FirstRight.TRUE && player.lane == Player.Lane.MID) {
            firstright=FirstRight.DONE;
            words="Excellent!Now try this way";
            player.lane = Player.Lane.RIGHT;
            lane = MID;
            player.update();
            update();
            invalidate();
        }
        else if(touchX < player.getX() + width / 2 &&
                firstright == FirstRight.TRUE &&
                player.lane == Player.Lane.MID) {
            player.lane = Player.Lane.LEFT;
            player.update();
            update();
            invalidate();
        }
        else if(touchX >= player.getX() + width / 2 &&
                firstright == FirstRight.TRUE && player.lane == Player.Lane.LEFT) {
            player.lane = Player.Lane.MID;
            player.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.TRUE &&
                touchX < player.getX() + width / 2){
            words="Great!And this way?";
            secondleft=SecondLeft.DONE;
            player.lane = Player.Lane.MID;
            lane = LEFT;
            player.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX < player.getX() + width / 2 &&
                player.lane == Player.Lane.MID)
        {
            words="Great!Let's get start?Choose the correct lane!";
            finishTeaching=true;
            player.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX < player.getX() + width / 2 &&
                player.lane == Player.Lane.RIGHT)
        {
            player.lane = Player.Lane.MID;
            player.update();
            update();
            invalidate();
        }
        else if(firstright == FirstRight.DONE &&
                secondleft == SecondLeft.DONE &&
                touchX >= player.getX() + width / 2)
        {
            player.lane = Player.Lane.RIGHT;
            player.update();
            update();
            invalidate();
        }

        setText(words);
    }
}
