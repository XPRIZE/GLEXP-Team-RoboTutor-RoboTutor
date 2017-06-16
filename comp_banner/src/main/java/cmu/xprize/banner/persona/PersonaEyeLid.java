/**
 Copyright(c) 2015-2017 Kevin Willows
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cmu.xprize.banner.persona;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class PersonaEyeLid  {

    Paint         lidPaint;
    Paint         lidStroke;
    Path          lidPath;

    float         mLeft;
    float         mTop;
    float         mRight;
    float         mBottom;
    float         mBlink;
    float         mBlinkLevel = .15f;
    RectF         mLid;

    float         mWidth;
    float         mHeight;


    public PersonaEyeLid() {

        lidPaint = new Paint();
        lidPaint.setColor(Color.parseColor("#eFeFeF"));
        lidPaint.setStyle(Paint.Style.FILL);

        lidStroke = new Paint();

        lidStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        lidStroke.setStyle(Paint.Style.STROKE);
        lidStroke.setColor(Color.BLACK);
        lidStroke.setStrokeWidth(4);

        lidPath = new Path();
    }

    public void updateBounds() {

        // Init the far corner bounds
        mRight  = mLeft + mWidth;
        mBottom = mTop  + mHeight;
        mBlink  = mTop  + mHeight * mBlinkLevel;

        mLid = new RectF(mLeft, mBlink, mRight, mBottom );
    }

    public void updateBounds(float left, float top, float width, float height) {
        this.mLeft   = left;
        this.mTop    = top;
        this.mWidth  = width;
        this.mHeight = height;

        updateBounds();
    }

    protected void onDraw(Canvas canvas) {

        lidPath.reset(); // only needed when reusing this path for a new build
        lidPath.moveTo(mLeft, mTop); // used for first point
        lidPath.lineTo(mRight, mTop);
        lidPath.lineTo(mRight, mBottom);
        lidPath.arcTo(mLid, 0.0f, -180.0f, false);
        lidPath.lineTo(mLeft, mTop);

        canvas.drawPath(lidPath, lidPaint);

        lidPath.reset(); // only needed when reusing this path for a new build
        lidPath.moveTo(mRight, mBottom);
        lidPath.arcTo(mLid, 0.0f, -180.0f, false);
        canvas.drawPath(lidPath, lidStroke);

        //Log.i("Eyelid:", "Draw: " + mBlink);
    }

    public void setBlink(float blink) {
        mBlinkLevel = blink;

        updateBounds();
    }

    public float getBlink() {
        return mBlinkLevel;
    }

    private void setLeft(int left) {
        this.mLeft = left;
    }

    private void setTop(int top) {
        this.mTop = top;
    }

    private void setWidth(float width) {
        this.mWidth = width;
    }

    private void setHeight(float height) {
        this.mHeight = height;
    }
}

