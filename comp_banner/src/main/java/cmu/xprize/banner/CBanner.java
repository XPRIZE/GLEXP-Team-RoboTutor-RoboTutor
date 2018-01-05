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

package cmu.xprize.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.banner.persona.Persona;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

public class CBanner extends PercentRelativeLayout implements IEventListener {

    public Context  mContext;

    private Persona mPersona;

    private Paint mPaint      = new Paint();
    private Rect  mViewRegion = new Rect();

    private int   mColor = Color.parseColor("#75C043");


    public CBanner(Context context) {
        super(context);
        init(context, null);
    }

    public CBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        inflate(getContext(), R.layout.banner_layout, this);

        mContext = context;

        mPersona = (Persona) findViewById(R.id.Spersona);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);

        setWillNotDraw(false);
    }


    public void onDestroy() {
        mPersona.onDestroy();
    }


    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        mPaint.setColor(mColor);

        getDrawingRect(mViewRegion);
        canvas.drawRect(mViewRegion, mPaint);
    }


    @Override
    public void onEvent(IEvent event) {

        switch(event.getType()) {

            // Message from Stimiulus variant to share state with response variant
            //
            case TCONST.SET_BANNER_COLOR:
                mColor = Color.parseColor((String)event.getString(TCONST.VALUE));
                break;
        }
    }
}
