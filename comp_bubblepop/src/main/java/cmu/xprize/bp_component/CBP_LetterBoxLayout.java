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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 *
 */
public class CBP_LetterBoxLayout extends FrameLayout {

    public Context   mContext;

    private Paint    mPaint       = new Paint();
    private Rect     mViewRegion  = new Rect();

    private float scale;
    private float dx;
    private float dy;

    private Matrix  mDrawMatrix = new Matrix();
    private boolean DBG         = false;


    public CBP_LetterBoxLayout(Context context) {
        super(context);
        init(context, null);
    }

    public CBP_LetterBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public CBP_LetterBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        // Allow onDraw to be called to start animations
        //
        if(DBG) {

            mPaint.setColor(Color.parseColor("#66FF00FF"));
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(false);

            setWillNotDraw(false);
        }
    }


    private void configureBounds() {

        int dwidth  = BP_CONST.DESIGNWIDTH;
        int dheight = BP_CONST.DESIGNHEIGHT;

        int vwidth  = ((ViewGroup)getParent()).getWidth();
        int vheight = ((ViewGroup)getParent()).getHeight();

        if (dwidth <= vwidth && dheight <= vheight) {
            scale = 1.0f;
        } else {
            scale = Math.min((float) vwidth  / (float) dwidth,
                    (float) vheight / (float) dheight);
        }

        dx = Math.round((vwidth - dwidth * scale) * 0.5f);
        dy = Math.round((vheight - dheight * scale) * 0.5f);

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate(dx, dy);
    }


    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(BP_CONST.DESIGNWIDTH, BP_CONST.DESIGNHEIGHT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if(changed)
            configureBounds();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

        int saveCount = canvas.getSaveCount();
        canvas.save();

        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix);
        }

        super.dispatchDraw(canvas);

        // Debug support
        //
        if(DBG) {
            CBP_Component parent = (CBP_Component) getParent();

            if(parent.getMechanics() != null) {
                // debug - To use this you must disable the background view
                parent.getMechanics().onDraw(canvas);
            }
        }

        canvas.restoreToCount(saveCount);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        PointF hitTest = new PointF(ev.getX(), ev.getY());

        GlobalTolocal(hitTest);

        ev.setLocation(hitTest.x, hitTest.y);

        return super.dispatchTouchEvent(ev);
    }


    @Override
    public void onDraw(Canvas canvas) {

        int saveCount = canvas.getSaveCount();
        canvas.save();

        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix);
        }

        super.onDraw(canvas);

        // Debug support
        //
        if(DBG) {
            getDrawingRect(mViewRegion);
            canvas.drawRect(mViewRegion, mPaint);
        }

        canvas.restoreToCount(saveCount);
    }


    public PointF localToGlobal(PointF location) {

        location.x *= scale;
        location.y *= scale;

        location.x += dx;
        location.y += dy;

        return location;
    }


    public PointF GlobalTolocal(PointF location) {

        location.x -= dx;
        location.y -= dy;

        location.x /= scale;
        location.y /= scale;

        return location;
    }


}
