package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import cmu.xprize.mn_component.CMn_Icons;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CTutor;

/**
 * Created by Kevin on 2/28/2016.
 */
public class TMn_Icons extends CMn_Icons {


    static final String TAG = "TMn_Icons";


    public TMn_Icons(Context context) {
        super(context);
        init(context, null);
    }

    public TMn_Icons(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TMn_Icons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
    }


    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
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

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = height;
            Log.d(TAG, "Width Using EXACTLY: " + width);
        } else if (widthMode == MeasureSpec.AT_MOST) {

            View sibling = (View)CTutor.getViewById(R.id.Snumber, null);

            //Can't be bigger than...
            width = sibling.getWidth();
            Log.d(TAG, "Width Using AT MOST: " + width);
        } else {
            //Be whatever you want
            width = height;
            Log.d(TAG, "Width Using UNSPECIFIED: " + width);
        }


        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

}
