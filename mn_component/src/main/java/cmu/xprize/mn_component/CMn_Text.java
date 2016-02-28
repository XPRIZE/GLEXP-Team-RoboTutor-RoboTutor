package cmu.xprize.mn_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class CMn_Text extends TextView {

    private float aspect  = 1f;  // w/h
    private int   mHeight = 100;

    static final String TAG = "CMn_Text";

    public CMn_Text(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Text(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Text(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
    }

    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {

        int desiredWidth = 1000;
        int desiredHeight = 1000;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = mHeight;
        int height;

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            mHeight = height = heightSize;
            width  = mHeight;
            //Log.d(TAG, "Height Using EXACTLY: " + height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
            //Log.d(TAG, "Height Using AT MOST: " + height);
        } else {
            //Be whatever you want
            height = desiredHeight;
            //Log.d(TAG, "Height Using UNSPECIFIED: " + height);
        }

        //Measure Width
//        if (widthMode == MeasureSpec.EXACTLY) {
//            //Must be this size
//            width = height;
//            Log.d(TAG, "Width Using EXACTLY: " + width);
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            //Can't be bigger than...
//            width = height;
//            Log.d(TAG, "Width Using AT MOST: " + width);
//        } else {
//            //Be whatever you want
//            width = height;
//            Log.d(TAG, "Width Using UNSPECIFIED: " + width);
//        }


        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }




}
