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

package cmu.xprize.fw_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by Kevin on 1/27/2016.
 */
public class CTextView extends TextView {

    private Context mContext;
    private Paint   mPaint;
    private boolean mIsFocus;
    private boolean mIsSpace = false;


    public CTextView(Context context) {
        super(context);

        init(context);
    }


    public CTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init(context);
    }


    public CTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }


    private void init(Context context) {
        mContext = context;

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(0xefe);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setAlpha(15);
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Rect viewRegion = new Rect();

        if(mIsFocus) {
            getDrawingRect(viewRegion);
            canvas.drawRect(viewRegion, mPaint);
        }

        super.onDraw(canvas);
    }

    public boolean isSpace() {
        return mIsSpace;
    }

    public void setAsSpace(boolean isSpace) {
        mIsSpace = isSpace;
    }


    public void setFocus(boolean bfocus) {

        if(mIsFocus != bfocus) {
            mIsFocus = bfocus;
            invalidate();
        }
    }
}
