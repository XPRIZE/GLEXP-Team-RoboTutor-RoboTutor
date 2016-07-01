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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CBubble extends FrameLayout {

    private   Context       mContext;

    private ImageView       mIcon;
    private TextView        mText;
    private float           mScale;

    private float           mAngle;
    private float           mLocation;


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

        mIcon = (ImageView) findViewById(R.id.SIcon);
        mText = (TextView) findViewById(R.id.SText);
        mScale = 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;
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


    public void setIntrinsicScale(float newScale) {
        mScale = newScale;
    }


    public void setLocation(float newLoc) {
        mLocation = newLoc;
    }


    public void setAngle(float newAngle) {
        mAngle = newAngle;
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
