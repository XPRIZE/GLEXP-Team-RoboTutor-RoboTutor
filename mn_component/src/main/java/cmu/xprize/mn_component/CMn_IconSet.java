package cmu.xprize.mn_component;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CMn_IconSet extends View {

    private Paint borderPaint;
    private int _eyeStrokeColor = 0xFF000000;
    private int _eyeStrokeWidth = 2;

    private int mMaxBalls   = 10;
    private int mNumBalls   = (int)(Math.random() * 9) + 1;
    private int mBallMargin = 3;

    private float mRadMul;
    private float mRadius;


    static final String TAG = "CMn_Icons";


    public CMn_IconSet(Context context) {
        super(context);
        init(context,null);

    }

    public CMn_IconSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public CMn_IconSet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        setWillNotDraw(false);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(_eyeStrokeColor);
        borderPaint.setStrokeWidth(_eyeStrokeWidth);
    }


    public void addIcon() {
    }


    @Override
    public void onDraw(Canvas canvas) {

        int borderRegion = getHeight();

        int ballSize     = (borderRegion - (mBallMargin * (mMaxBalls+1))) / mMaxBalls;
        int borderHeight = (mBallMargin * (mNumBalls+1)) + (ballSize * mNumBalls);

        Rect viewRegion = new Rect();
        getDrawingRect(viewRegion);

        viewRegion.top += borderRegion - borderHeight;

        canvas.drawRoundRect(new RectF(viewRegion), mRadius, mRadius, borderPaint);

    }


    public void setCornerRadius(float multiplier) {

        mRadMul = multiplier;
        mRadius = getWidth() * mRadMul;
    }


    public void setBorder() {

        int borderRegion = getHeight();

        int ballSize     = (borderRegion - (mBallMargin * (mMaxBalls+1))) / mMaxBalls;
        int borderHeight = (mBallMargin * (mNumBalls+1)) + (ballSize * mNumBalls);

        Rect viewRegion = new Rect();
        getDrawingRect(viewRegion);

        viewRegion.top += borderRegion - borderHeight;

        //canvas.drawRoundRect(new RectF(viewRegion), 50, 50, borderPaint);

    }


    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {

        int desiredWidth = 1000;
        int desiredHeight = 1000;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
            Log.d(TAG, "Height Using EXACTLY: " + height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
            Log.d(TAG, "Height Using AT MOST: " + height);
        } else {
            //Be whatever you want
            height = desiredHeight;
            Log.d(TAG, "Height Using UNSPECIFIED: " + height);
        }

        View sibling = (View)getViewById(R.id.Snumber, (ViewGroup)getParent());

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {

            //Can't be bigger than...
            width = sibling.getWidth();
            Log.d(TAG, "Width Using EXACTLY: " + width);
        } else if (widthMode == MeasureSpec.AT_MOST) {

//            View sibling = (View)getViewById(R.id.Snumber, (ViewGroup)getParent());
//
//            //Can't be bigger than...
//            width = sibling.getWidth();
            width = sibling.getWidth();

            Log.d(TAG, "Width Using AT MOST: " + width);
        } else {
            //Be whatever you want
            width = sibling.getWidth();
            Log.d(TAG, "Width Using UNSPECIFIED: " + width);
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }


    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if(changed)
        {
            setCornerRadius(0.33f);
            super.onLayout(changed, left, top, right, bottom);
        }
    }


    public View getViewById(int findme, ViewGroup container) {
        View foundView = null;

        if(container != null) {

            try {
                for (int i = 0; (foundView == null) && (i < container.getChildCount()); ++i) {

                    View nextChild = (View) container.getChildAt(i);

                    if (((View) nextChild).getId() == findme) {
                        foundView = nextChild;
                        break;
                    } else {
                        if (nextChild instanceof ViewGroup)
                            foundView = getViewById(findme, (ViewGroup) nextChild);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "View walk error: " + e);
            }
        }
        return foundView;
    }

}
