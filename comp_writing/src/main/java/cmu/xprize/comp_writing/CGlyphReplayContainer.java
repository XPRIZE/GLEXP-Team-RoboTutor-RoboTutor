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

package cmu.xprize.comp_writing;

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
import android.graphics.Rect;
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
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.ltkplus.CStroke;
import cmu.xprize.ltkplus.CStrokeInfo;
import cmu.xprize.util.TCONST;


public class CGlyphReplayContainer extends View implements Animator.AnimatorListener {

    private IWritingComponent       mWritingComponent;
    private Rect                    replayRegion = new Rect();

    private CGlyph                  _currentGlyph;
    private int                     _index;
    private Path                    cPath;
    private Paint                   mPaint;
    private PointF                  tPoint;

    private AnimatorSet             fullAnimation;
    private ArrayList<Animator>     drawAnimation;
    private Iterator<CStrokeInfo>   StrokeIterator;
    private CStroke                 _strokeToDraw;
    private boolean                 _pointAtStroke = true;
    private CAffineXform            _glyphXform;

    private int[]                   _screenCoord = new int[2];
    private IGlyphReplayListener    _callback;

    private LocalBroadcastManager   bManager;

    public static final int FIT_VERT        = 0x01;
    public static final int FIT_HORZ        = 0x02;
    public static final int ALIGN_BASELINE  = 0x04;
    public static final int ALIGN_CENTER    = 0x08;
    public static final int ALIGN_ORIGVIEW  = 0x10;
    public static final int SCALE_TIME      = 0x20;
    public static final int ALIGN_CONTAINER = 0x40;

    private static final float POINT_SIZE = 40f;

    final private String TAG  = "REPLAY";

    private float _replayTime = 1200f;
    private boolean  DBG = false;


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CGlyphReplayContainer(Context context) {
        super(context);

        init();
    }


    /**
     * Create a Persona object
     *
     * @param context
     * @param attrs
     */
    public CGlyphReplayContainer(Context context, AttributeSet attrs) throws XmlPullParserException, IOException {
        super(context, attrs);

        init();
    }


    private void init() {

        setVisibility(VISIBLE);
        setAlpha(0);

        // Create a paint object to deine the line parameters
        //
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(GCONST.REPLAY_WEIGHT);
        mPaint.setAntiAlias(true);

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    public void setPointAtStroke(boolean point) {
        _pointAtStroke = point;
    }


    public void setWritingController(IWritingComponent writingController) {

        // We use callbacks on the parent control
        mWritingComponent = writingController;
    }


    @Override
    public void onDraw(Canvas canvas) {

        if(_strokeToDraw != null) {

            // Redraw the current replay path
            //
            if (_currentGlyph != null) {

                for (int i1 = 0; i1 < _currentGlyph.size(); i1++) {

                    CStroke stroke = _currentGlyph.getStroke(i1);

                    if (stroke.isPoint(replayRegion)) {

                        PointF center = stroke.getPoint();

                        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawCircle(center.x, center.y, replayRegion.width() / 40, mPaint);
                        mPaint.setStyle(Paint.Style.STROKE);
                    } else {
                        canvas.drawPath(stroke.getReplayPath(), mPaint);
                    }

                    if(tPoint != null) {

                        if(_pointAtStroke) {
                            broadcastLocation(TCONST.POINT_LIVE, tPoint);
                        }
                        else if(DBG) {
                            mPaint.setColor(Color.YELLOW);
                            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                            canvas.drawCircle(tPoint.x, tPoint.y, replayRegion.width() / 40, mPaint);
                            mPaint.setStyle(Paint.Style.STROKE);
                            mPaint.setColor(Color.BLUE);
                        }
                    }

                    // The replay path on the existing getStroke is generated a getStroke at a getTime
                    // from the strokeSet so once we reach the current getStroke being drawn we quit
                    // since the replay path of the subsequent getStroke are not valid yet
                    //
                    if (stroke == _strokeToDraw)
                        break;
                }
            }

            if (tPoint != null) {
                broadcastLocation(TCONST.LOOKAT, tPoint);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (changed) {
            getDrawingRect(replayRegion);
        }
    }


    private void broadcastLocation(String Action, PointF touchPt) {

        getLocationOnScreen(_screenCoord);

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

        bManager.sendBroadcast(msg);
    }


    public void clearReplay() {

        // Create a path object to hold the vector stream

        _currentGlyph = null;
        _strokeToDraw = null;

        invalidate();
    }


    public boolean isPlaying() {
        boolean result = false;

        if(fullAnimation != null) {
            result = fullAnimation.isRunning();
        }
        return result;
    }


    private void animateGlyph(IGlyphReplayListener callback, CAffineXform showXform) {

        _callback = callback;

        _glyphXform = showXform;

        StrokeIterator = _currentGlyph.iterator();

        broadcastLocation(TCONST.STARE_STOP, new PointF(0,0));
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
        mWritingComponent.applyBehavior(WR_CONST.ON_START_WRITING); //amogh added for hesitation.
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

        if (!animateNextStroke()) {

            // Tell the touchpane to reset.
            //
            if(_callback != null) {
                _callback.applyEvent(WR_CONST.FIELD_REPLAY_COMPLETE);
//                mWritingComponent.applyBehavior(WR_CONST.FIELD_REPLAY_COMPLETE); //amogh added for hesitation.
            }

            // update the persona and Pointer
            //
            broadcastEnd();
        }
    }

    private void broadcastEnd() {

        if (tPoint != null) {
            broadcastLocation(TCONST.LOOKATEND, tPoint);
        }

        mWritingComponent.applyBehavior(WR_CONST.ACTION_COMPLETE);
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

            cPath  = _strokeToDraw.initReplayPath(_glyphXform);
            tPoint = _strokeToDraw.getReplayPoint(_glyphXform, 0);

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
            // Always catch the last point in the path
            if(delta > 0) {
                index = _strokeToDraw.getPointsInPath() - 1;

                drawAnimation.add(ObjectAnimator.ofInt(this, "index", index).setDuration(delta));
            }

            // Fade out on the last getStroke
//            if(!StrokeIterator.hasNext())
//                drawAnimation.add(createDrawAnimation("alpha", 0, 750));

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
    public void replayGlyph(CGlyph glyph, float baseLine, int options, Rect Container, IGlyphReplayListener callback) {

        ViewGroup parentObj = (ViewGroup) getParent();
        float vscaleX = 1.0f;
        float vscaleY = 1.0f;

        float scaleX    = 1.0f;
        float scaleY    = 1.0f;
        float timeScale = 1.0f;

        int posX      = 0;
        int posY      = 0;

        clearReplay();

        _currentGlyph = glyph;

        RectF strokeBounds = _currentGlyph.getStrokeBoundingBox();
        RectF glyphBounds  = _currentGlyph.getGlyphBoundingBox();
        Rect  origBounds   = _currentGlyph.getOrigBoundingBox();

        if((options & FIT_VERT) == FIT_VERT) {
            scaleY = (replayRegion.height() - GCONST.STROKE_WEIGHT) / _currentGlyph.getGlyphHeight();
        }
        if((options & FIT_HORZ) == FIT_HORZ) {
            scaleX = (replayRegion.width() - GCONST.STROKE_WEIGHT) / _currentGlyph.getGlyphWidth();
        }


        if((options & ALIGN_BASELINE) == ALIGN_BASELINE) {

            posY  = replayRegion.top += baseLine - (_currentGlyph.getGlyphBaseline() * scaleY);
        }
        if((options & ALIGN_CENTER) == ALIGN_CENTER) {

            posX  = replayRegion.left += (replayRegion.width() - (_currentGlyph.getGlyphWidth() * scaleX)) / 2;
        }

        if((options & ALIGN_CONTAINER) == ALIGN_CONTAINER) {

            Log.d(TAG, "Height: " + Container.height() + "  Width: " + Container.width());

            scaleY = (float)Container.height() / strokeBounds.height();
            scaleX = (float)Container.width() / strokeBounds.width();

            posX = replayRegion.left = Container.left;
            posY = replayRegion.top  = Container.top;
        }

        if((options & ALIGN_ORIGVIEW) == ALIGN_ORIGVIEW) {

            Log.d(TAG, "Height: " + Container.height() + "  Width: " + Container.width());

            scaleY = (float)Container.height() / origBounds.height();
            scaleX = (float)Container.width() / origBounds.width();

            posX = replayRegion.left = (int) (_currentGlyph.getOrigOffsetX() * scaleX);
            posY = replayRegion.top  = (int) (_currentGlyph.getOrigOffsetY() * scaleY);
        }

        if((options & SCALE_TIME) == SCALE_TIME) {

            long duration = _currentGlyph.calcGlyphDuration();

            timeScale  = (float)duration / _replayTime;
        }

        animateGlyph(callback, new CAffineXform(scaleX, scaleY, posX, posY, timeScale));

        invalidate();
    }


}
