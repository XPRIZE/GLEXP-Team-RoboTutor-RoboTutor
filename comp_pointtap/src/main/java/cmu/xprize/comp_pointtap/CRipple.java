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

package cmu.xprize.comp_pointtap;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CRipple extends View {

    private Paint   _paint = new Paint();
    private float   _radius;

    private float   _originX = 0;
    private float   _originY = 0;


    public CRipple(Context context) {
        super(context);
    }

    public CRipple(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CRipple(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    
    public void initRipple(int color, float strokeWeight, float radius) {

        _paint.setColor(color);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(strokeWeight);
        _paint.setAntiAlias(true);

        _radius = radius;

        setAlpha(0);
    }

    public void setOrigin(float x, float y) {

        _originX = x;
        _originY = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawCircle(_originX, _originY, _radius, _paint);
    }
}
