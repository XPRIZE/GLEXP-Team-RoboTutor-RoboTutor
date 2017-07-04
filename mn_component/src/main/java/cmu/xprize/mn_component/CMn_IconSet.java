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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cmu.xprize.util.View_Helper;


public class CMn_IconSet extends View {

    private Paint borderPaint;

    private int _containerStrokeColor = 0xFF000000;
    private int _containerFillColor   = 0xFF000000;
    private int _containerStrokeWidth = 2;

    private int mMaxBalls;
    private int mNumBalls;
    private int mBallSize;
    private int mBallMargin = 10;

    private ArrayList<CMn_Icon> _icons = new ArrayList<>();

    private float mRadMul;
    private float mRadius;

    private int DEFAULT_MIN = 1;

    private Rect mBorderRegion = new Rect();


    static final String TAG = "CMn_IconSet";


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
        borderPaint.setColor(_containerStrokeColor);
        borderPaint.setStrokeWidth(_containerStrokeWidth);
    }


    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawRoundRect(new RectF(mBorderRegion), mRadius, mRadius, borderPaint);

        for(CMn_Icon icon : _icons) {
            icon.onDraw(canvas);
        }
    }


    public void setCornerRadius(float multiplier) {

        mRadMul = multiplier;
        mRadius = getWidth() * mRadMul;
    }


    public void setMaxIcons(int maxIconCount ) {

        mMaxBalls = maxIconCount;
    }


    public void setIconCount(int numIcons, boolean update) {

        mNumBalls = numIcons;

        int delta = mNumBalls -_icons.size();

        // More icons than we need
        //
        if(delta < 0) {
            while(delta < 0) {
                trimIcon();
                delta++;
            }
        }

        // Fewer icons than we need
        //
        else if(delta > 0) {
            while(delta > 0) {
                addIcon();
                delta--;
            }
        }

        if(update)
            requestLayout();
    }


    private CMn_Icon addIcon() {

        // Setting the parameters on the TextView
        CMn_Icon icon = new CMn_Icon();

        _icons.add(icon);

        return icon;
    }


    private void trimIcon() {

        _icons.remove(_icons.size() - 1);
    }


    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int desiredWidth  = 1000;
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

        View sibling = (View) View_Helper.getViewById(R.id.Snumber, (ViewGroup)getParent());

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {

            width = sibling.getMeasuredWidth();
            Log.d(TAG, "Width Using EXACTLY: " + width);

        } else if (widthMode == MeasureSpec.AT_MOST) {

            width = sibling.getMeasuredWidth();
            Log.d(TAG, "Width Using AT MOST: " + width);

        } else {

            width = sibling.getMeasuredWidth();
            Log.d(TAG, "Width Using UNSPECIFIED: " + width);
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }


    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {

        //if(changed)
        {
            setCornerRadius(0.43f);

            mBallSize = (getHeight() - (mBallMargin * (mMaxBalls+1))) / mMaxBalls;

            updateBorder();

            layoutIcons();

            super.onLayout(changed, left, top, right, bottom);
        }

    }

    /**
     * Update the size of the Alley - If there are no balls default to 2 so it looks like
     * a number 0
     */
    public void updateBorder() {

        int freeSpace = (mNumBalls == 0)? DEFAULT_MIN:mNumBalls;

        int borderHeight = (mBallMargin * (freeSpace+1)) + (mBallSize * freeSpace);

        getDrawingRect(mBorderRegion);

        mBorderRegion.top += getHeight() - borderHeight;
    }


    private void layoutIcons() {

        float yOffset = mBorderRegion.top + mBallMargin;
        float xOffset = (getWidth() - mBallSize) / 2;

        for(int i1 = 0 ; i1 < mNumBalls ; i1++) {
            _icons.get(i1).updateIconBounds(xOffset, yOffset, mBallSize, mBallSize);

            yOffset += mBallSize + mBallMargin;
        }
    }


}
