//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.mn_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class CMn_Text extends TextView {

    private Paint borderPaint;

    private int _eyeStrokeColor = 0xDDFFFFFF;
    private int _eyeStrokeWidth = 2;

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
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(_eyeStrokeColor);
        borderPaint.setStrokeWidth(_eyeStrokeWidth);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(changed) {
            setTextSize(mHeight * 0.3f);
            setTypeface(getTypeface(), Typeface.BOLD);
        }

        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Rect viewRegion = new Rect();
        getDrawingRect(viewRegion);

        canvas.drawRoundRect(new RectF(viewRegion), 5, 5, borderPaint);

        super.onDraw(canvas);
    }



}
