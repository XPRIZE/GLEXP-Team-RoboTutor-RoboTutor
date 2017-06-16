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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import cmu.xprize.util.TCONST;

public class CMn_Icon  {

    private int _iconStrokeColor = 0xFF000000;
    private int _iconFillColor   = 0xFF000000;
    private int _iconStrokeWidth = 2;

    private RectF   mBounds;
    private String  mIconType;
    private Paint   mIconPaint;
    private float   mRadius;


    static final private String TAG ="CMn_Icon";


    public CMn_Icon() {

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setStyle(Paint.Style.STROKE);
        mIconPaint.setColor(_iconStrokeColor);
        mIconPaint.setStrokeWidth(_iconStrokeWidth);

        mBounds   = new RectF();
        mIconType = TCONST.OVALICON;
        mRadius   = 10;
    }


    public void onDraw(Canvas canvas) {

        switch(mIconType) {
            case TCONST.OVALICON:
                canvas.drawOval(mBounds, mIconPaint);
                break;

            case TCONST.RECTICON:
                canvas.drawRoundRect(mBounds, mRadius, mRadius, mIconPaint);
                break;
        }
    }


    public void updateIconBounds(float x, float y, float width, float height) {

        mBounds.left   = x;
        mBounds.right  = x + width;
        mBounds.top    = y;
        mBounds.bottom = y + height;
    }

}
