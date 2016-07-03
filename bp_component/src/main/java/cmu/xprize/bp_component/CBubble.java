//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

package cmu.xprize.bp_component;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CBubble extends FrameLayout {

    private   Context         mContext;

    private ImageView         mIcon;
    private TextView          mText;
    private float             mScale;
    private String            mColor;

    private float             mAngle;
    private float             mDistance;
    private float[]           mRange;
    private PointF            mPosition = new PointF();

    private ImageView         bubblepop = null;
    private AnimationDrawable popping   = null;


    public CBubble(Context context) {
        super(context);
        init(context, null);
    }

    public CBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setClipChildren(false);

        mIcon = (ImageView) findViewById(R.id.SIcon);
        mText = (TextView) findViewById(R.id.SText);
        mScale = 1;
    }

    public void onDestroy() {

        if(bubblepop != null) {
            removeView(bubblepop);
            bubblepop = null;
        }
    }

    public void pop() {

        // Remove the bubble graphic and content
        //
        setBackground(null);
        mIcon.setVisibility(View.INVISIBLE);
        mText.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        bubblepop = new ImageView(mContext);

        bubblepop.setScaleX(mScale);
        bubblepop.setScaleY(mScale);

        // Add the pop drawable view
        //
        addView(bubblepop, layoutParams);

        int[] popAnimSet = BP_CONST.popAnimationMap.get(mColor);

        popping = new AnimationDrawable();

        // There are 2 ways of animating the pop - you can use the BP constants for the animation
        // and add them programmatically - this could provide more control over the process if
        // required.  In this implementation it makes little/no difference.
        //
        int ndx = 0;
        for(int animID : popAnimSet) {
            popping.addFrame(mContext.getResources().getDrawable(animID), BP_CONST.POP_FRAME_TIME[ndx++]);
        }
        popping.setOneShot(true);

        // Or you can use Animation Drawables that predefine the animation
        //
//        popping = (AnimationDrawable) mParent.getResources().getDrawable(R.drawable.bubble_b_pop, null);

        bubblepop.setBackground(popping);

        popping.start();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;
    }


    public void setColor(String color) {

        mColor = color;

        setBackgroundResource(BP_CONST.bubbleMap.get(mColor));
    }


    public String getColor() {

        return mColor;
    }


    public void setColor(int resId) {

        setBackgroundResource(resId);
    }


    public void setColor(Drawable res) {

        setBackground(res);
    }


    public void setScale(float newScale) {
        setScaleX(newScale);
        setScaleY(newScale);

    }

    public void setRange(float[] _range) {
        mRange = _range;
    }

    public float[] getRange() {
        return mRange;
    }

    public void setAssignedScale(float newScale) {
        mScale = newScale;
    }

    public float getAssignedScale() {
        return mScale;
    }

    public void setAngle(float newAngle) {
        mAngle = newAngle;
    }

    public float getAngle() {
        return mAngle;
    }


    public PointF getPosition(Point relOrigin, float Loc, float angle) {

        PointF pos = new PointF();

        pos.x = ((float) (relOrigin.x + (Loc * Math.cos(angle))));
        pos.y = ((float) (relOrigin.y - (Loc * Math.sin(angle))));

        return pos;
    }

    public PointF getNormalPosition() {
        PointF nPosition = new PointF();

        nPosition.x = mPosition.x + (getWidth() / 2);
        nPosition.y = mPosition.y + (getHeight() / 2);

        return nPosition;
    }

    public void setViewPosition(Point relOrigin, float vecDist) {

        mDistance = vecDist;

//        Log.d("XFORM", "Origin X: " + relOrigin.x);
//        Log.d("XFORM", "Origin Y: " + relOrigin.y);
//
//        Log.d("XFORM", "Xcomp   : " + (mDistance * Math.cos(mAngle)));
//        Log.d("XFORM", "Ycomp   : " + (mDistance * Math.sin(mAngle)));
//        Log.d("XFORM", "Distance: " + mDistance);
//        Log.d("XFORM", "Angle   : " + mAngle);

        mPosition.x = ((float) (relOrigin.x + (mDistance * Math.cos(mAngle))) - (getWidth() / 2));
        mPosition.y = ((float) (relOrigin.y - (mDistance * Math.sin(mAngle))) - (getHeight() / 2));

        setX(mPosition.x);
        setY(mPosition.y);

//        Log.d("XFORM", "Pos X: " + getX());
//        Log.d("XFORM", "Pos Y: " + getY());

    }
    public PointF getViewPosition() {
        return mPosition;
    }



    public void setContents(int resID, String text) {

        if(text == null) {
            mIcon.setImageResource(resID);
            mIcon.setVisibility(View.VISIBLE);
        }
        else {
            mText.setText(text);
            mText.setVisibility(View.VISIBLE);
        }

        invalidate();
    }

    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

}
