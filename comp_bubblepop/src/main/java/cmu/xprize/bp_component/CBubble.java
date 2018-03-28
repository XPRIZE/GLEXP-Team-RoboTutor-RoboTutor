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

package cmu.xprize.bp_component;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.TypedValue;

public class CBubble extends FrameLayout {

    private   Context         mContext;

    private boolean           mOnScreen = false;

    private String            mCorrectVal;
    private String            mStimulusVal;

    private ImageView         mAudio;
    private ImageView         mIcon;
    private TextView          mText;
    private float             mScale;
    private String            mColor;

    private float             mAngle;
    private float             mDistance;
    private float[]           mRange;
    private PointF            mPosition = new PointF();

    private float             mScaleCorrection = 1.0f;

    private ImageView         bubblepop = null;
    private AnimationDrawable popping   = null;

    private String            mProblemType;


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


    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        float instanceDensity = mContext.getResources().getDisplayMetrics().density;
        mScaleCorrection      = BP_CONST.DESIGN_SCALE / instanceDensity;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setClipChildren(false);

        mAudio =  (ImageView) findViewById(R.id.SAudioFeedback);
        mIcon  = (ImageView) findViewById(R.id.SIcon);
        mText  = (TextView) findViewById(R.id.SText);

        setScale(1.0f);
    }


    public void onDestroy() {

        if(bubblepop != null) {
            removeView(bubblepop);
            bubblepop = null;
        }
    }

    public void configData(String stimulusVal, String correctVal, String problemType) {
        mCorrectVal = correctVal;
        mStimulusVal = stimulusVal;
        mProblemType = problemType;
    }

    public boolean isCorrect() {

        return mCorrectVal.equals(mStimulusVal);
    }


    public String getStimulus() {
        return mStimulusVal;
    }


    public boolean getOnScreen() {
        return mOnScreen;
    }


    public void setOnScreen(boolean isOnScreen) {
        mOnScreen = isOnScreen;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    public void setColor(String color) {
        mColor = color;

        if(mProblemType != null && mProblemType.toLowerCase().startsWith("word")) {
            setBackgroundResource(BP_CONST.elongatedBubbleMap.get(mColor));
        }
        else {
            setBackgroundResource(BP_CONST.bubbleMap.get(mColor));
        }
    }

//    public void setFeedbackColor(String color) {
//        mColor = color;
//
//        mAudio.setImageResource(BP_CONST.audioBubbleMap.get(mColor));
//    }
//

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

        setAssignedScale(newScale);

        setScaleX(mScale);
        setScaleY(mScale);
    }

    public float getScaledWidth() {
        return getWidth() * mScale;
    }

    public float getScaledHeight() {
        return getHeight() * mScale;
    }


    @Override
    public void setScaleX(float newScale) {
        super.setScaleX(newScale *  mScaleCorrection);
    }

    @Override
    public void setScaleY(float newScale) {
        super.setScaleY(newScale *  mScaleCorrection);
    }

    @Override
    public float getScaleX() {
        return super.getScaleX() / mScaleCorrection;
    }

    @Override
    public float getScaleY() {
        return super.getScaleY() / mScaleCorrection;
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


    public void setPosition(int x, int y) {
        PointF nPosition = new PointF();

        nPosition.x = x;
        nPosition.y = y;

        setX(x);
        setY(y);
    }

    public void setCenterPosition(int x, int y) {
        PointF nPosition = new PointF();

        nPosition.x = x + (getWidth() / 2);
        nPosition.y = y + (getHeight() / 2);

        setX(x);
        setY(y);
    }


    public PointF getCenterPosition() {
        PointF nPosition = new PointF();

        nPosition.x = getX();
        nPosition.y = getY();

        float w = (getWidth() / 2);
        float h = (getHeight() / 2);

        nPosition.x += w;
        nPosition.y += h;

        return nPosition;
    }



    /**
     * Hit rectangle in parent's coordinates
     *
     * @param outRect The hit rectangle of the view.
     */
    public void getHitRect(Rect outRect) {
        super.getHitRect(outRect);
    }

    public void setVectorPosition(Point relOrigin, float vecWidthDist, float vecHeightDist, float angle) {
        mAngle    = angle;

        float xOrigin = relOrigin.x;
        float yOrigin = relOrigin.y;
        mPosition.x = ((float) (xOrigin + vecWidthDist * Math.cos(mAngle)) - (getWidth() / 2));
        mPosition.y = ((float) (yOrigin + (vecHeightDist * Math.sin(mAngle))) - (getHeight() / 2));

        setX(mPosition.x);
        setY(mPosition.y);

//        float lowerX = mPosition.x - (getWidth() / 2);
//        float higherX = mPosition.x + (getWidth() / 2);
//        float lowerY = mPosition.y - (getHeight() / 2);
//        float higherY = mPosition.y + (getHeight() / 2);

    }

    public PointF getVectorPosition() {
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

            //If bubble contains equation, change font size, ensure monospace font, and align to the right
            if(text.matches(".*\n+.*")) {
                mText.setTypeface(Typeface.MONOSPACE);
                mText.setGravity(Gravity.RIGHT);
                mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80);
            }
            else {
                if(text.length() < 5) {
                    mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 128);
                }
                else {
                    mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (60));
                }
            }
        }

        invalidate();
    }

    public TextView getTextView() {
        return mText;
    }


    public long pop() {

        long animTime = 0L;

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

            int duration = BP_CONST.POP_FRAME_TIME[ndx++];
            animTime    += duration;

            popping.addFrame(mContext.getResources().getDrawable(animID, null), duration);
        }
        popping.setOneShot(true);

        // Or you can use Animation Drawables that predefine the animation
        //
//        popping = (AnimationDrawable) mParent.getResources().getDrawable(R.drawable.bubble_b_pop, null);

        bubblepop.setBackground(popping);

        popping.start();

        return animTime;
    }

}
