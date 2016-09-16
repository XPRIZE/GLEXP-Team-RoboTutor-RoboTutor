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

package cmu.xprize.comp_writing;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import android.widget.TextView;


/**
 */
@RemoteViews.RemoteView
public class CResponseContainer extends PercentRelativeLayout {

    private Context  mContext;
    private Paint    mPaint;
    private TextView mRecogChar;

    public CResponseContainer(Context context) {
        super(context);
        init(context, null);
    }

    public CResponseContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CResponseContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
//                _aspect       = a.getFloat(R.styleable.RoboTutor_aspectratio, 1.0f);
//                _stroke_weight= a.getFloat(R.styleable.RoboTutor_strokeweight, 45f);

            } finally {
                a.recycle();
            }
        }


        // Create a paint object to hold the glyph and font draw parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);

        //setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRecogChar = (TextView)findViewById(R.id.recog_char);
    }

    public void setResponseChar(String resp) {
        mRecogChar.setText(resp);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect drawRegion = new Rect();
        getDrawingRect(drawRegion);

        canvas.drawRect(drawRegion, mPaint);

    }
}