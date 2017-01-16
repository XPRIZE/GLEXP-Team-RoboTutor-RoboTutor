/**
 Copyright 2015 Kevin Willows
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

package cmu.xprize.comp_writing;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.ltkplus.IGlyphSource;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.TCONST;


public class CGlyphInputContainer extends View implements IGlyphSource, OnTouchListener, IGlyphReplayListener {

    private boolean               DBG           = false;
    private static final float    FONT_MARGIN   = 30f;

    private Context               mContext;

    private IGlyphSink            _recognizer;
    private boolean               _recPending = false;
    private int                   _recIndex;
    private boolean               _inhibit    = false;

    private IWritingComponent     mWritingComponent;
    private IGlyphController      mGlyphController;
    private CLinkedScrollView     mScrollView;
    private Boolean               _touchStarted = false;
    private int[]                 _screenCoord = new int[2];

    private Paint                 mPaint;
    private Paint                 mPaintBG;
    private Paint                 mPaintDBG;
    private Paint                 mPaintBase;
    private Paint                 mPaintUpper;
    private Typeface              _fontFace;

    private float                 mX, mY;

    private CGlyph                _userGlyph  = null;
    private CGlyph                _protoGlyph = null;
    private CGlyph                _drawGlyph  = null;
    private CGlyph                _animGlyph  = null;
    private boolean               _isDrawing  = false;

    private boolean               _showSampleChar = false;
    private boolean               _showUserGlyph  = true;
    private boolean               _showProtoGlyph = false;

    static private Bitmap         _bitmap;
    private boolean               _bitmapDirty       = false;
    private String                _ltkPlusResult;

    private String                _sampleExpected     = "";     // The expected character
    private float                 _sampleHorzAdjusted = 0;
    private float                 _sampleVertAdjusted = 0;

    private Rect                  _viewBnds          = new Rect();  // The view draw bounds
    private float                 _dotSize;

    private Rect                  _fontBnds          = new Rect();  // The bounds for the font size limits
    private Rect                  _lcaseBnds         = new Rect();  // Lower case character limits - gives upper bound line
    private Rect                  _fontCharBnds      = new Rect();  // The expected char bounds
    private Rect                  _parentBnds        = new Rect();  // This views parent draw bounds - for aspect calcs

    private float                 _topLine;
    private float                 _baseLine;
    private Rect                  _clipRect          = new Rect();

    protected float               _aspect            = 1;  // w/h
    private String                _fontHeightSample  = "Kg";
    private String                _fontWidthSample   = "W";
    private String                _lcaseSample       = "m";
    private int                   _fontVOffset       = 0;
    private float                 _stroke_weight     = 45f;
    private int                   _glyphColor        = Color.BLACK;
    private int                   _boxColor          = WR_CONST.BOX_COLOR;

    private boolean               mLogGlyphs = true;

    private RecogDelay            _counter;
    private long                  _time;
    private long                  _prevTime;

    private LocalBroadcastManager bManager;
    public static String          RECMSG = "CHAR_RECOG";

    private boolean               mHasGlyph = false;

    private boolean               _DEVMODE = false;             // Used in GlyphRecognizer project to update metrics etc.

    private static final float    TOLERANCE = 5;
    private static final int[]    STATE_HASGLYPH = {R.attr.state_hasglyph};

    private static int            RECDELAY   = 700;              // Just want the end timeout
    private static int            RECDELAYNT = RECDELAY+500;      // inhibit ticks

    private CGlyphReplayContainer mReplayComp;
    private boolean               isPlaying  = false;

    private boolean               _drawUpper = false;
    private boolean               _drawBase  = true;
    private boolean               _isLast    = false;
    private boolean               _correct   = false;



    private static final String   TAG = "CGlyphInputContainer";


    public CGlyphInputContainer(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CGlyphInputContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CGlyphInputContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /**
     * Common object initialization for all constructors
     *
     * @param attrs
     * @param defStyle
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {

        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                _aspect       = a.getFloat(R.styleable.RoboTutor_aspectratio, 1.0f);
                _stroke_weight= a.getFloat(R.styleable.RoboTutor_strokeweight, 45f);

            } finally {
                a.recycle();
            }
        }

        // Create a paint object to hold the line parameters
        mPaintDBG = new Paint();

        mPaintDBG.setColor(Color.BLUE);
        mPaintDBG.setStyle(Paint.Style.STROKE);
        mPaintDBG.setStrokeJoin(Paint.Join.ROUND);
        mPaintDBG.setStrokeCap(Paint.Cap.ROUND);
        mPaintDBG.setStrokeWidth(4);
        mPaintDBG.setAntiAlias(true);


        // Create a paint object to hold the glyph and font draw parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(_stroke_weight);
        mPaint.setAntiAlias(true);


        // Create a paint object for the background
        mPaintBG = new Paint();

        mPaintBG.setStrokeWidth(_stroke_weight);
        mPaintBG.setAntiAlias(true);


        // Font Face selection
        selectFont(TCONST.GRUNDSCHRIFT);

        // Create a paint object to define the baseline parameters
        mPaintBase = new Paint();

        //mPaintBase.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintBase.setColor(Color.parseColor("#000000"));

        mPaintBase.setStyle(Paint.Style.STROKE);
        mPaintBase.setStrokeJoin(Paint.Join.ROUND);
        mPaintBase.setStrokeWidth(GCONST.LINE_WEIGHT);
        mPaintBase.setAntiAlias(true);

        // Create a paint object to define the topline parameters
        mPaintUpper = new Paint();

        mPaintUpper.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintUpper.setStyle(Paint.Style.STROKE);
        mPaintUpper.setStrokeJoin(Paint.Join.ROUND);
        mPaintUpper.setStrokeWidth(GCONST.LINE_WEIGHT);
        mPaintUpper.setAlpha(100);
        mPaintUpper.setPathEffect(new DashPathEffect(new float[]{25f, 12f}, 0f));
        mPaintUpper.setAntiAlias(true);

        // Clear the gly[h state and start listening for touch events
        //
        erase();

        _counter = new RecogDelay(RECDELAY, RECDELAYNT);

        _recognizer = CRecognizerPlus.getInstance();

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }

    public void setWritingController(IWritingComponent writingController) {

        // We use callbacks on the parent control
        mWritingComponent = writingController;
    }


    public void setInputManager(IGlyphController glyphController) {

        // We use callbacks on the parent control
        mGlyphController = glyphController;
    }

    public void setLinkedScroll(CLinkedScrollView linkedScroll) {
        mScrollView = linkedScroll;
    }

    private void broadcastLocation(String Action, PointF touchPt) {

        getLocationOnScreen(_screenCoord);

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

        bManager.sendBroadcast(msg);
    }

    /**
     * Add Root vector to path
     *
     * @param x
     * @param y
     */
    private void startTouch(float x, float y) {
        PointF p;

        if(!_touchStarted) {

            // Set pending flag - Used to initiate recognition when moving between fields
            //
            _recPending = true;

            _touchStarted = true;
            mWritingComponent.applyBehavior(WR_CONST.ON_START_WRITING);

            // This is to support immediate feedback
            //
            mWritingComponent.scanForPendingRecognition(mGlyphController);
        }

        if (_counter != null)
            _counter.cancel();

        // We always add the next stoke start to the glyph set

        if (_drawGlyph == null) {
            _drawGlyph = new CGlyph(mContext, _baseLine, _viewBnds, _dotSize);
            _isDrawing = true;

            if(_drawGlyph == null) {
                Log.e(TAG, "_drawGlyph Creation Failed");
            }
        }

        PointF tPoint = new PointF(x, y);

        _drawGlyph.newStroke();
        _drawGlyph.addPoint(tPoint);

        mX = x;
        mY = y;

        broadcastLocation(TCONST.LOOKAT, tPoint);

        invalidate();
    }


    public void setBoxColor(int newColor) {

        _boxColor = newColor;
    }


    private void setHasGlyph(boolean hasGlyph) {

        final boolean needsRefresh = mHasGlyph != hasGlyph;

        mHasGlyph = hasGlyph;

        if (needsRefresh) {
           refreshDrawableState();
           invalidate();
        }
    }


    /**
     * Flag to indicate the last glyph in the input string - used to limit the baseline length
     *
     * @param isLast
     */
    public void setIsLast(boolean isLast) {
        _isLast = isLast;
    }

    /**
     * This is used for the background when this is an ImageView
     *
     * @param extraSpace
     * @return
     */
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mHasGlyph) {
            mergeDrawableStates(drawableState, STATE_HASGLYPH);
        }
        return drawableState;
    }


    /**
     * Test whether the motion is greater than the jitter tolerance
     *
     * @param x
     * @param y
     * @return
     */
    private boolean testPointTolerance(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        return (dx >= TOLERANCE || dy >= TOLERANCE)? true:false;
    }


    /**
     * Update the glyph path if motion is greater than tolerance - remove jitter
     *
     * @param x
     * @param y
     */
    private void moveTouch(float x, float y) {
        PointF touchPt;

        // only update if we've moved more than the jitter tolerance
        if(testPointTolerance(x,y)){

            touchPt = new PointF(x, y);

            mX = x;
            mY = y;

            broadcastLocation(TCONST.LOOKAT, touchPt);

            // TODO: BUG - drawGlyph may be null ?????
            //
            _drawGlyph.addPoint(touchPt);
            invalidate();
        }
    }


    /**
     * End the current glyph path
     * TODO: Manage debouncing
     *
     */
    private void endTouch(float x, float y) {
        PointF touchPt;

        _counter.start();

        touchPt = new PointF(x, y);

        // Only add a new point to the glyph if it is outside the jitter tolerance
        if(testPointTolerance(x,y)) {

            _drawGlyph.addPoint(touchPt);
            invalidate();
        }

        broadcastLocation(TCONST.LOOKATEND, touchPt);

        _drawGlyph.endStroke();
    }


    public boolean onTouch(View view, MotionEvent event) {
        PointF     p;
        boolean    result = false;
        long       delta;
        final int  action = event.getAction();

        // inhibit input while the recognizer is thinking
        //
        result = true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                mWritingComponent.applyBehavior(WR_CONST.WRITE_BEHAVIOR);

                mScrollView.setEnableScrolling(mHasGlyph);

                _prevTime = _time = System.nanoTime();

                if(!mHasGlyph) {

                    startTouch(x, y);
                }
                else if(_DEVMODE) {

                    if(_showUserGlyph)
                        _drawGlyph = _userGlyph;
                    else
                        _drawGlyph = _protoGlyph;

                    if(_drawGlyph != null) {

                        _recognizer.postToQueue(CGlyphInputContainer.this, _drawGlyph);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                _time = System.nanoTime();
                moveTouch(x, y);
                break;

            case MotionEvent.ACTION_UP:
                mScrollView.setEnableScrolling(true);

                _time = System.nanoTime();
                endTouch(x, y);
                break;
        }
        delta = _time - _prevTime;

        Log.i(TAG, "Touch Time: " + _time + "  :  " + delta);

        return result;
    }


    private void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        mPaint.setTextSize(TypedValue.applyDimension(
                unit, size, r.getDisplayMetrics()));

    }


    /**
     * This does a iterative search for a font size that fits within the given bounds.
     *
     * @param minMaxSample
     * @param min
     * @param max
     * @return
     */
    private Rect fontAdjust(String minMaxSample, float min, float max) {

        float tHeight;
        float fSize      = max;
        float bump       = max / 4;
        Rect  sampleBnds = new Rect();

        String direction = "";

        while(true) {

            setTextSize(TypedValue.COMPLEX_UNIT_PX, fSize);

            mPaint.getTextBounds(minMaxSample, 0, minMaxSample.length(), sampleBnds);

            tHeight = sampleBnds.height();

            // If we are larger than min target size
            //
            if(tHeight > min) {

                // If we are smaller than the max target size - we have found a usable size
                //
                if(tHeight < max) {
                    break;
                }
                else {
                    // Otherwise we need to bump down the size
                    // If we are changing direction then halve the increment so we don't
                    // continuously overshoot.
                    //
                    if(direction.equals("UP"))
                                        bump /= 2;
                    fSize -= bump;
                    direction = "DN";
                }
            }
            else {
                // Otherwise we need to bump up the size
                // If we are changing direction then halve the increment so we don't
                // continuously overshoot.
                //
                if(direction.equals("DN"))
                                    bump /= 2;
                fSize += bump;
                direction = "UP";
            }
        }

        return sampleBnds;
    }


    public float getFontAspect() {

        Rect _heightBnds = new Rect();
        Rect _widthBnds = new Rect();

        setTextSize(TypedValue.COMPLEX_UNIT_PX, 128);

        mPaint.getTextBounds(_fontHeightSample, 0, _fontHeightSample.length(), _heightBnds);
        mPaint.getTextBounds(_fontWidthSample, 0, _fontWidthSample.length(), _widthBnds);

        float width  = _widthBnds.width();
        float height = _heightBnds.height();

        _aspect = width / height;

        return _aspect; //.7f;
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

        if(changed) {

            getDrawingRect(_viewBnds);

            // We calc a view specific dot size for periods colons etc.
            //
            _dotSize = _viewBnds.width() / TCONST.DOT_SIZE;

            Log.d(TAG, "Height: " + getHeight() + "  Width: " + getWidth());

            rebuildProtoType(TCONST.VIEW_SCALED, _viewBnds);

            float height = _viewBnds.height();
            float margin = height / FONT_MARGIN;

            // Calculate the bounding box that will enclose the highest ascent and descent characters
            // in the dataset - give it to the metrics to use in calculating the max bitmap extent
            //
            _fontBnds = fontAdjust(_fontHeightSample, height - (margin * 2),  height - margin );

            _baseLine    = -_fontBnds.top;
            _fontVOffset = (int)((height - _fontBnds.height()) / 2);

            _fontBnds.offsetTo(0, _fontVOffset);
            _baseLine += _fontVOffset;

            // Set the lower case upper boundry line - This is done for a single exemplar lower case
            // character sample.
            //
            mPaint.getTextBounds(_lcaseSample, 0, _lcaseSample.length(), _lcaseBnds);

            _topLine = _baseLine - _lcaseBnds.height();

            // This is the expected character for this position in the input stream
            //
            _fontCharBnds = getFontCharBounds(_sampleExpected);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        Rect textBnds = new Rect();

        super.onDraw(canvas);

        // Display the background box.
        //
        if(!mHasGlyph) {

            RectF viewBndsF = new RectF(_viewBnds);
            viewBndsF.inset(GCONST.LINE_WEIGHT / 2, GCONST.LINE_WEIGHT / 2);

            mPaintBG.setStyle(Paint.Style.FILL);
            mPaintBG.setColor(Color.WHITE);
            canvas.drawRoundRect(viewBndsF, GCONST.CORNER_RAD, GCONST.CORNER_RAD, mPaintBG);

            mPaintBG.setStyle(Paint.Style.STROKE);
            mPaintBG.setStrokeWidth(GCONST.LINE_WEIGHT);
            mPaintBG.setColor(_boxColor);
            canvas.drawRoundRect(viewBndsF, GCONST.CORNER_RAD, GCONST.CORNER_RAD, mPaintBG);
        }

        // Set the lower case upper boundry line
        //
        if(_drawUpper) {
            Path linePath = new Path();
            linePath.moveTo(0, _topLine);
            linePath.lineTo(getWidth(), _topLine);
            canvas.drawPath(linePath, mPaintUpper);
        }

        if(_drawBase) {

            // We want the baseline to appear to be continuous.  However the baseline should not
            // extend past the last character in the string.
            //
            canvas.getClipBounds(_clipRect);

            int extension = _isLast? 0:_clipRect.width()/2;

            canvas.save();
            _clipRect.right += extension;
            canvas.clipRect(_clipRect, Region.Op.REPLACE);

            canvas.drawLine(0, _baseLine, _clipRect.width(), _baseLine, mPaintBase);
            canvas.restore();
        }

        mPaint.setStrokeWidth(1);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        if(_bitmapDirty) {
            _bitmapDirty = false;

            if(_showUserGlyph)
                _drawGlyph = _userGlyph;
            else
                _drawGlyph = _protoGlyph;

            if(_drawGlyph != null) {

                _bitmap = _drawGlyph.getMetric().generateVisualComparison(_fontBnds, _sampleExpected, _drawGlyph, mPaint, _stroke_weight, false);

                canvas.drawBitmap(_bitmap, _fontCharBnds.left, _fontCharBnds.top, null);
            }
            return;
        }

        // Draw the prototype char - adjusted for off-origin alignment - i.e. the font character
        // may be created so it's left edge doesn't align with x=0
        //
        if(_sampleExpected != "" && _showSampleChar) {
            getFontCharBounds(_sampleExpected);

            mPaint.setColor(WR_CONST.SAMPLE_COLOR);
            canvas.drawText(_sampleExpected, _sampleHorzAdjusted, _sampleVertAdjusted, mPaint);
        }

        mPaint.setAlpha(255);

        if(DBG) {

            // The getStroke is switching to strokeandfill spontaneously
            // This is an attempt to mitigate
            // Using existing Paint with forced STROKE was not successful
            //
            mPaintDBG = new Paint();

            mPaintDBG.setStyle(Paint.Style.STROKE);
            mPaintDBG.setStrokeJoin(Paint.Join.ROUND);
            mPaintDBG.setStrokeCap(Paint.Cap.ROUND);
            mPaintDBG.setStrokeWidth(4);
            mPaintDBG.setAntiAlias(true);

            mPaintDBG.setStyle(Paint.Style.STROKE);
            mPaintDBG.setColor(Color.BLUE);
            canvas.drawRect(_viewBnds, mPaintDBG);

            mPaintDBG.setStyle(Paint.Style.STROKE);
            mPaintDBG.setColor(Color.MAGENTA);
            canvas.drawRect(_fontBnds, mPaintDBG);

            mPaintDBG.setStyle(Paint.Style.STROKE);
            mPaintDBG.setColor(Color.RED);
            canvas.drawRect(_fontCharBnds, mPaintDBG);

            // This is the inflated font bounds to encompass the stroke weight
            //
            if(_showUserGlyph && _userGlyph != null) {
                mPaintDBG.setStyle(Paint.Style.STROKE);
                mPaintDBG.setColor(Color.MAGENTA);
                canvas.drawRect(_userGlyph.getGlyphViewBounds(_viewBnds, _stroke_weight), mPaintDBG);
            }
        }

        mPaint.setColor(_glyphColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(_stroke_weight);

        // Redraw the current glyph path
        //
        if(_showUserGlyph && _userGlyph != null) {
            _userGlyph.drawGylyph(canvas,  mPaint, _viewBnds);
        }

        // Redraw the current prototype glyph path
        //
        if(_showProtoGlyph && _protoGlyph != null) {
            _protoGlyph.drawGylyph(canvas,  mPaint, _viewBnds);
        }

        if(_isDrawing) {
            _drawGlyph.drawGylyph(canvas,  mPaint, _viewBnds);
        }

    }


    public void hideUserGlyph() {
        _showUserGlyph = false;
        invalidate();
    }

    public void replayGlyph(String replayTarget) {

        if(!isPlaying) {

            isPlaying  = true;

            switch(replayTarget) {

                case WR_CONST.REPLAY_PROTOGLYPH:
                    _animGlyph = _protoGlyph;
                    break;

                case WR_CONST.REPLAY_USERGLYPH:
                    _animGlyph = _userGlyph;
                    break;

                case WR_CONST.REPLAY_DEFAULT:
                    if (_showUserGlyph)
                        _animGlyph = _userGlyph;
                    else
                        _animGlyph = _protoGlyph;
                    break;
            }

            if(_animGlyph != null) {

                // Ensure this field is visible.
                //
                mWritingComponent.autoScroll(mGlyphController);

                switch (_animGlyph.getDrawnState()) {

                    case TCONST.STROKE_ORIGINAL:

                        mReplayComp.replayGlyph(_animGlyph, _baseLine, CGlyphReplayContainer.ALIGN_ORIGVIEW | CGlyphReplayContainer.SCALE_TIME, _viewBnds, this);
                        break;

                    case TCONST.STROKE_OVERLAY:

                        int inset = (int) (_stroke_weight / 2);
                        Rect protoBnds = new Rect(_fontCharBnds);

                        protoBnds.inset(inset, inset);

                        mReplayComp.replayGlyph(_animGlyph, _baseLine, CGlyphReplayContainer.ALIGN_CONTAINER | CGlyphReplayContainer.SCALE_TIME, protoBnds, this);
                        break;

                }
            }
        }
    }


    public void animateOverlay() {

        AnimatorSet animator = new AnimatorSet();

        RectF glyphBnds = null;
        RectF protoBnds = null;

        float inset = _stroke_weight / 2;

        if(_showUserGlyph)
            _animGlyph = _userGlyph;
        else
            _animGlyph = _protoGlyph;

        if(_animGlyph != null) {

            PointF wayPoints[]   = new PointF[2];
            PointF scalePoints[] = new PointF[2];
            PointF posFinal      = new PointF();

            glyphBnds = _animGlyph.getStrokeBoundingBox();
            protoBnds = new RectF(_fontCharBnds);

            protoBnds.inset(inset, inset);

            // Go from the current drawn state to the alternate state
            //
            switch(_animGlyph.getDrawnState()) {

                case TCONST.STROKE_ORIGINAL:
                    wayPoints[0] = new PointF(glyphBnds.left, glyphBnds.top);
                    wayPoints[1] = new PointF(protoBnds.left, protoBnds.top);

                    scalePoints[0] = new PointF(1.0f, 1.0f);
                    scalePoints[1] = new PointF(protoBnds.width() / glyphBnds.width(), protoBnds.height() / glyphBnds.height());

                    _animGlyph.setDrawnState(TCONST.STROKE_OVERLAY);
                    break;

                case TCONST.STROKE_OVERLAY:
                    wayPoints[0] = new PointF(protoBnds.left, protoBnds.top);
                    wayPoints[1] = new PointF(glyphBnds.left, glyphBnds.top);

                    scalePoints[0] = new PointF(protoBnds.width() / glyphBnds.width(), protoBnds.height() / glyphBnds.height());
                    scalePoints[1] = new PointF(1.0f, 1.0f);

                    _animGlyph.setDrawnState(TCONST.STROKE_ORIGINAL);
                    break;
            }


            Animator translator = CAnimatorUtil.configTranslator(this, 1500, 0, wayPoints);
            Animator scaler     = CAnimatorUtil.configScaler(this, 1500, 0, scalePoints);

            animator.playTogether(translator, scaler);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationCancel(Animator arg0) {
                    //Functionality here
                }

                @Override
                public void onAnimationStart(Animator arg0) {
                    //Functionality here
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    mWritingComponent.applyBehavior(WR_CONST.ACTION_COMPLETE);
                }

                @Override
                public void onAnimationRepeat(Animator arg0) {
                    //Functionality here
                }
            });

            animator.start();
        }
    }


    public void flashOverlay() {

        if(_userGlyph != null && _sampleExpected != "") {
            _bitmapDirty = true;
            invalidate();
        }
    }


    @Override
    public boolean applyEvent(String event) {

        boolean result = false;

        switch(event) {
            case WR_CONST.REPLAY_COMPLETE:
                isPlaying = false;
                break;
        }

        result = mWritingComponent.applyBehavior(event);

        return result;
    }


    public boolean toggleSampleChar() {

        showSampleChar(!_showSampleChar);

        rebuildGlyph();
        invalidate();

        return _showSampleChar;
    }


    public void showSampleChar(boolean show) {

        _showSampleChar = show;
        invalidate();
    }


    public boolean toggleProtoGlyph() {

        _showProtoGlyph = !_showProtoGlyph;
        _showUserGlyph  = !_showProtoGlyph;

        // Show the save button for dirty protoglyphs - i.e. changed but unsaved
        //
        if(mGlyphController != null) {
            boolean isDirty = (_protoGlyph != null)? _protoGlyph.getDirty():false;
            mGlyphController.setProtoTypeDirty(_showProtoGlyph ?  isDirty: false);
        }

        rebuildGlyph();
        invalidate();

        return _showProtoGlyph;
    }

    private void rebuildGlyph() {

        Rect protoBnds = null;

        if(_showUserGlyph) {
            setHasGlyph(_userGlyph != null);

            if(_userGlyph != null) {

                // Go from the current drawn state to the alternate state
                //
                switch(_userGlyph.getDrawnState()) {

                    case TCONST.STROKE_ORIGINAL:
                        _userGlyph.rebuildGlyph(TCONST.VIEW_SCALED, _viewBnds);
                        break;


                    case TCONST.STROKE_OVERLAY:

                        int inset = (int) (_stroke_weight / 2);

                        protoBnds = new Rect(_fontCharBnds);
                        protoBnds.inset(inset, inset);
                        _userGlyph.rebuildGlyph(TCONST.CONTAINER_SCALED, protoBnds);
                        break;
                }
            }
            _drawGlyph = _userGlyph;
        }
        else {
            setHasGlyph(_protoGlyph != null);

            if(_protoGlyph != null) {

                // Go from the current drawn state to the alternate state
                //
                switch (_protoGlyph.getDrawnState()) {

                    case TCONST.STROKE_ORIGINAL:
                        _protoGlyph.rebuildGlyph(TCONST.VIEW_SCALED, _viewBnds);
                        break;


                    case TCONST.STROKE_OVERLAY:

                        int inset = (int) (_stroke_weight / 2);

                        protoBnds = new Rect(_fontCharBnds);
                        protoBnds.inset(inset, inset);
                        _protoGlyph.rebuildGlyph(TCONST.CONTAINER_SCALED, protoBnds);
                        break;
                }
            }
            _drawGlyph = _protoGlyph;
        }
    }


    public boolean toggleDebugBounds() {

        DBG = !DBG;

        if(_userGlyph != null)
            _userGlyph.getMetric().showDebugBounds(DBG);

        if(_protoGlyph != null)
            _protoGlyph.getMetric().showDebugBounds(DBG);

        rebuildGlyph();
        invalidate();

        Log.d(TAG, "DebugBounds: " + DBG);

        return DBG;
    }


    public void selectFont(String fontSource) {

        String fontPath = TCONST.fontMap.get(fontSource.toLowerCase());

        if(fontPath != null) {

            _fontFace = Typeface.createFromAsset(mContext.getAssets(), fontPath);

            mPaint.setTypeface(_fontFace);

            rebuildGlyph();
            requestLayout();
        }
    }


    //***********************************************************************
    /** Custom Animatable properties */


    public void setGlyphX(float posX) {

        _animGlyph.setAnimOffsetX(posX);
        invalidate();
    }
    public float getGlyphX() { return _animGlyph.getAnimOffsetX();  }


    public void setGlyphY(float posY) {

        _animGlyph.setAnimOffsetY(posY);
        invalidate();
    }
    public float getGlyphY() { return _animGlyph.getAnimOffsetY();  }


    public void setGlyphScaleX(float posX) {

        _animGlyph.setAnimScaleX(posX);
        invalidate();
    }
    public float getGlyphScaleX() { return _animGlyph.getAnimScaleX();  }


    public void setGlyphScaleY(float posY) {

        _animGlyph.setAnimScaleY(posY);
        invalidate();
    }
    public float getGlyphScaleY() { return _animGlyph.getAnimScaleY();  }


    /** Custom Animatable properties */
    //***********************************************************************


    public void erase() {

        // This can be set by a feedback mode - so we always resest it when clearing
        _showSampleChar = false;
        _showUserGlyph  = true;

        clear();
        inhibitInput(false);
    }

    public boolean getGlyphStarted() {
        return _touchStarted;
    }


    private void clear() {

        // Create a path object to hold the vector stream

        if(_showUserGlyph)
            _userGlyph = null;
        else
            _protoGlyph = null;

        // Remove the save button
        if(mGlyphController != null)
            mGlyphController.setProtoTypeDirty(false);

        _glyphColor = TCONST.colorMap.get(TCONST.COLORNORMAL);

        // Reset the flag so onStartWriting events will fire
        //
        _touchStarted = false;

        _recPending = false;
        _isDrawing  = false;
        _correct    = false;

        _drawGlyph  = null;
        setHasGlyph(false);
        invalidate();
    }


    public void updateCorrectStatus(boolean correct) {

        _correct = correct;

        if(correct) {

            _glyphColor = TCONST.colorMap.get(TCONST.COLORRIGHT);
        }
        else {
            _glyphColor = TCONST.colorMap.get(TCONST.COLORWRONG);
        }

        invalidate();
    }


    public void setExpectedChar(String protoChar) {

        _sampleExpected = protoChar;
    }


    public void setProtoGlyph(CGlyph protoGlyph) {

        // If the prototype is not yet created in the glyphs Zip then this could be null
        //
        if(protoGlyph != null) {
            _protoGlyph = protoGlyph;
            _protoGlyph.setDotSize(_dotSize);
        }
    }

    public boolean isCorrect() {
        return _correct;
    }

    public void rebuildProtoType(String options, Rect region) {

        if(_protoGlyph != null)
            _protoGlyph.rebuildGlyph(options, region);
    }


    /**
     * This is used in Immedaite feedback mode when a error occurs in another field.
     * We inhibit further input and recognition on all other fields and reset the glyph state
     * on pending fields.
     */
    public void inhibitInput(boolean inhibit) {

        _inhibit = inhibit;

        if(_inhibit) {

            Log.d("INHIBIT", "muting: " + _sampleExpected);

            if(_counter != null)
                _counter.cancel();

            this.setOnTouchListener(null);

            // If user is in the process of writing in this field then clear it.
            //
            if(_recPending)
                    clear();
        }
        else {
            if(!mHasGlyph)
               this.setOnTouchListener(this);

            Log.d("INHIBIT", "UN-muting: " + _sampleExpected);
        }
    }

    public boolean hasGlyph() {
        return mHasGlyph;
    }


    /**
     * This is how recognition is initiated - if the user stops writing for a
     * predefined period we assume they are finished writing.
     */
    public class RecogDelay extends CountDownTimer {

        public RecogDelay(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            // Ensure the next field is visible.
            //
            mWritingComponent.autoScroll(mGlyphController);

            firePendingRecognition();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.i(TAG, "Tick Done");
        }

    }


    // This may be called by a foreign glyphinputcontainer (i.e. not this one) that has started
    // writing in order to initiate recognition on fields that are still pending -
    // i.e. haven't timed out
    //
    public boolean firePendingRecognition() {

        boolean result = false;

        if(_recPending) {

            // kill the countdown timer
            //
            _counter.cancel();

            _recPending = false;
            _isDrawing  = false;

            // Do any required post processing on the glyph
            //
            _drawGlyph.terminateGlyph();

            // This can be  used to generate protoglyph samples -
            // This is where we define either the user or proto glyph as what was just drawn.
            //
            if(_showUserGlyph)
                _userGlyph = _drawGlyph;
            else {
                _protoGlyph = _drawGlyph;
                _protoGlyph.setDirty(true);

                // Show the save button for this glyph
                //
                if(mGlyphController != null)
                    mGlyphController.setProtoTypeDirty(true);
            }

            setHasGlyph(true);
            Log.i(TAG, "Recognition Done");

            _recognizer.postToQueue(CGlyphInputContainer.this, _drawGlyph);

            result = true;
        }

        return result;
    }


    public void saveGlyphAsPrototype() {

        // Note that here we are assuming that the sample and the recognized value are the same -
        //
        if(_showProtoGlyph && _protoGlyph != null) {

            _protoGlyph.saveGlyphPrototype("SHAPEREC_ALPHANUM", "", _sampleExpected, _sampleExpected);
            _protoGlyph.setDirty(false);

            mGlyphController.setProtoTypeDirty(false);
        }
    }


    public void setReplayComp(CGlyphReplayContainer comp) {

        mReplayComp = comp;
    }


    //*******************************************************************************************
    //*******************************************************************************************
    //*******************************************************************************************
    // IGlyphSource interface Implementation
    //

    @Override
    public String       getExpectedChar() {return _sampleExpected; }
    @Override
    public CGlyph       getGlyph() { return _drawGlyph; }
    @Override
    public Rect         getViewBnds() { return _viewBnds; }
    @Override
    public Rect         getFontBnds() { return _fontBnds; }
    @Override
    public float        getBaseLine() { return _baseLine; }
    @Override
    public float        getDotSize() { return _dotSize; }
    @Override
    public Paint        getPaint() { return mPaint; }

    @Override
    public boolean recCallBack(CRecResult[] _ltkCandidates, CRecResult[] _ltkPlusCandidates, int sampleIndex) {

        boolean isValid;

        mWritingComponent.updateGlyphStats(_ltkPlusCandidates, _ltkCandidates, _ltkCandidates[sampleIndex].getGlyph().getMetric(), _ltkCandidates[0].getGlyph().getMetric());

        // Update the final result
        //
        _ltkPlusResult = _ltkPlusCandidates[0].getRecChar();

        // TODO: check for performance issues and run this in a separate thread if required.
        //
        if(mLogGlyphs)
            _drawGlyph.writeGlyphToLog("SHAPEREC_ALPHANUM", "", _sampleExpected, _ltkPlusResult);

        isValid = mWritingComponent.updateStatus((IGlyphController) mGlyphController, _ltkPlusCandidates);

        // Stop listening to glyph draw events - when there is a glyph
        //
        inhibitInput(true);

        // Reconstitute the path in the correct orientation after LTK+ post-processing
        //
        rebuildGlyph();
        invalidate();

        return isValid;
    }

    @Override
    public Rect getFontCharBounds(String sampleChar) {

        Rect fontCharBnds = new Rect();

        // This is the expected character for this position
        //
        mPaint.getTextBounds(sampleChar, 0, sampleChar.length(), fontCharBnds);

        // Calc the approx centering offet for the prototype character
        //
        float xloc = (_viewBnds.width() - fontCharBnds.width()) / 2;

        // The Font Prototype Boundry may not start at the left edge and may go below the
        // baseline - Here we adjust both so the character appears on the baseline in the
        // exact center of the draw region.
        // We do this since we are using it as an exemplar of good writing skills - if you
        // don't care about the discrepancy remove the protoBnds adjustments.
        //
        _sampleHorzAdjusted = xloc - fontCharBnds.left;
        _sampleVertAdjusted = _baseLine;

        // This is the translated sample font bounds - setting the width just makes it pretty
        // relative to the exemplar character.
        // All characters in the font should display within the preset vertical extent.
        //
        _fontBnds.left  = (int)xloc;
        _fontBnds.right = _fontBnds.left + fontCharBnds.width();

        fontCharBnds.offsetTo((int) xloc, (int) (_baseLine + fontCharBnds.top));

        return fontCharBnds;
    }


}

