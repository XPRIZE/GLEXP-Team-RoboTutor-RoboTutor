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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import cmu.xprize.ltkplus.CAffineXform;
import cmu.xprize.ltkplus.CStroke;
import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.ltkplus.CStrokeInfo;
import cmu.xprize.util.TCONST;


public class ReplayComponent extends View implements Animator.AnimatorListener {

    private ArrayList<Path> mPath;
    private Path            cPath;
    private Paint           mPaint;
    private PointF          tPoint;
    private int             _index;

    private AnimatorSet             fullAnimation;
    private ArrayList<Animator>     drawAnimation;
    private Iterator<CStrokeInfo>   StrokeIterator;

    private CStroke                 _strokeToDraw;
    private CAffineXform            _glyphXform;

    private static final float TOLERANCE = 3;

    private int[]                 _screenCoord = new int[2];
    private IReplayListener       _callback;
    private LocalBroadcastManager bManager;

    final private String TAG = "REPLAY";


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public ReplayComponent(Context context) {
        super(context);

        init();
    }

    /**
     * Create a Persona object
     *
     * @param context
     * @param attrs
     */
    public ReplayComponent(Context context, AttributeSet attrs) throws XmlPullParserException, IOException {
        super(context, attrs);

        init();
    }

    private void init() {
        setVisibility(INVISIBLE);
        setAlpha(0);

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    @Override
    public void onDraw(Canvas canvas) {

        // Immediate mode graphics -
        // Redraw the current path
        for(Path path : mPath)
            canvas.drawPath(path, mPaint);

        canvas.drawPath(cPath, mPaint);

        if(tPoint != null)
            broadcastLocation(TCONST.LOOKAT, tPoint);
    }


    private void broadcastLocation(String Action, PointF touchPt) {
        getLocationOnScreen(_screenCoord);

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

        bManager.sendBroadcast(msg);
    }

    private void clearReplay() {

        // Create a path object to hold the vector stream
        mPath = new ArrayList<>();
        invalidate();
    }


    public boolean isPlaying() {
        boolean result = false;

        if(fullAnimation != null) {
            result = fullAnimation.isRunning();
        }
        return result;
    }


    private void animateGlyph(IReplayListener callback, CGlyph glyphToDraw, CAffineXform showXform) {

        _callback = callback;
        clearReplay();

        _glyphXform = showXform;

        StrokeIterator = glyphToDraw.iterator();

        animateNextStroke();
    }

    private boolean animateNextStroke() {

        boolean result = false;

        if(StrokeIterator.hasNext()) {

            _strokeToDraw = StrokeIterator.next().getStroke();

            animateStroke();
            result = true;
        }

        return result;
    }



    //************************************************
    /** AnimatorListener Implmentation start */

    @Override
    public void onAnimationStart(Animator animation) {
        invalidate();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        broadcastEnd();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        invalidate();
    }

    @Override
    public void onAnimationEnd(Animator animation) {

        mPath.add(cPath);

        if (!animateNextStroke()) {
            broadcastEnd();
        }
    }

    private void broadcastEnd() {

        // Tell the touchpane to reset.
        if(_callback != null) {
            _callback.endReplay();
        }

        if (tPoint != null)
            broadcastLocation(TCONST.LOOKATEND, tPoint);
    }

    /** AnimatorListener Implmentation start */
    //************************************************


    private Animator createDrawAnimation(String prop, float endPt, long time ) {

        ValueAnimator drawAnim = null;
        drawAnim = ObjectAnimator.ofFloat(this, prop, endPt).setDuration(time);

        return drawAnim;
    }


    private void animateStroke() {

        fullAnimation = new AnimatorSet();

        Animator Frame;
        int      index;
        long     delta;

        ArrayList<Animator> outerColl = new ArrayList<Animator>();

        Iterator<CStroke.StrokePoint> PathIterator = _strokeToDraw.iterator();

        if(PathIterator.hasNext()) {

            CStroke.StrokePoint nextPoint = PathIterator.next();

            _glyphXform.setOrigX((int) ((nextPoint.getX() * _glyphXform.getScaleX()) + _glyphXform.getOffsetX()));
            _glyphXform.setOrigY((int) ((nextPoint.getY() * _glyphXform.getScaleY()) + _glyphXform.getOffsetY()));

            setIndex(-1);
            index = 1;
            delta = 0;

            cPath = _strokeToDraw.initReplayPath(_glyphXform);

            drawAnimation = new ArrayList<Animator>();
            drawAnimation.add(createDrawAnimation("alpha", 1, 10));

            while (PathIterator.hasNext()) {
                nextPoint = PathIterator.next();

                delta += (long) (nextPoint.getTime() / _glyphXform.getTimeScale());

                // Don't run a faster than 30 frames / sec
                if (delta > 40) {
                    drawAnimation.add(ObjectAnimator.ofInt(this, "index", index).setDuration(delta));
                    delta = 0;
                }
                index++;
            }

            // Fade out on the last stroke
            if(!StrokeIterator.hasNext())
                drawAnimation.add(createDrawAnimation("alpha", 0, 750));

            fullAnimation.addListener(this);

            fullAnimation.playSequentially(drawAnimation);
            fullAnimation.start();
        }
    }


    //***********************************************************************
    /** Custom Animatable properties */

    public void setIndex(int index) {

        if(_index != index) {
            _index = index;

            if(_index != -1) {
                cPath  = _strokeToDraw.incrReplayPath(_glyphXform, _index);
                tPoint = _strokeToDraw.getReplayPoint(_glyphXform, _index);

                Log.i(TAG, "replay Index: " + index);
                invalidate();
            }
        }
    }
    public int getIndex() { return _index;  }

    /** Custom Animatable properties */
    //***********************************************************************


    // Notes:
    //  The replayRegion is relative to the upper left corner of the replayComponent.
    //  The baseline is also relative to the upper left corner of the replayComponent.
    //
    public void replayGlyph(CGlyph glyph, float baseLine, String type, IReplayListener callback) {

        ViewGroup parentObj = (ViewGroup) getParent();
        float vscaleX = 1.0f;
        float vscaleY = 1.0f;

        float scaleX    = 1.0f;
        float scaleY    = 1.0f;
        float timeScale = 1.0f;

        RectF replayRegion = new RectF(0,0,getWidth(), getHeight());
        RectF glyphBounds  = glyph.getGlyphBoundingBox();

        switch(type) {
            // Draw relative to replay box with variable mAspect ratio of replay box
            //
            case "ABS_VAR_ASPECT":
                scaleX = vscaleX;
                scaleY = vscaleY;
                break;

            // Draw relative to replay box with mAspect ratio of the glyph bounding box -
            // use ratio of replaybox height to glyph height as scale factor
            // Horizontally center about the replayRegion.
            //
            case "ABS_CONST_ASPECT":
                scaleX = vscaleY;
                scaleY = vscaleY;

                replayRegion.left += (replayRegion.width() - (glyphBounds.width() * scaleX)) / 2;
                break;

            // Draw relative to the baseline with mAspect ratio of the glyph bounding box -
            // use ratio of replaybox height to glyph height as scale factor
            // Horizontally center about the replayRegion.
            //
            case "GLYPHREL_VAR_ASPECT":
                scaleX = vscaleX;
                scaleY = vscaleY;

                replayRegion.top   = baseLine - (replayRegion.height() * glyph.getGlyphBaselineRatio());
                replayRegion.left += (replayRegion.width() - (glyphBounds.width() * scaleX)) / 2;
                break;

            case "GLYPHREL_CONST_ASPECT":
                scaleX = vscaleY;
                scaleY = vscaleY;

                replayRegion.top   = baseLine - (replayRegion.height() * glyph.getGlyphBaselineRatio());
                replayRegion.left += (replayRegion.width() - (glyphBounds.width() * scaleX)) / 2;
                break;
        }

        animateGlyph(callback, glyph,
                new CAffineXform(scaleX, scaleY, replayRegion.left, replayRegion.top, timeScale));

        invalidate();
    }

}
