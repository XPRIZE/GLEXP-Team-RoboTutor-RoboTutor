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
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cmu.xprize.ltkplus.CLipiTKJNIInterface;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.ltkplus.CStroke;
import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;


public class CFingerWriter extends View implements OnTouchListener, IEventDispatcher, IEventListener {

    private Context               mContext;
    public List<IEventListener>   mListeners = new ArrayList<IEventListener>();
    protected List<String>        mLinkedViews;
    protected boolean             mListenerConfigured = false;

    protected boolean             _enabled = false;
    protected float               mAspect = 1;  // w/h

    private float                 mTopLine;
    private float                 mBaseLine;
    private boolean               mLogGlyphs = true;

    private Paint                 mPaint;
    private Paint                 mPaintBase;
    private Paint                 mPaintUpper;
    private PointF                mPoint;

    private static final float    TOLERANCE = 5;

    private CLipiTKJNIInterface   _recognizer;
    private String                _configFolder;
    private boolean               _initialized   = false;
    private RecognizerThread      _recThread;
    private String                _recogId;                // for logging
    private boolean               _isRecognizing = false;

    private CStroke[]              _recStrokes;
    private CRecResult[]           _recResults;
    protected ArrayList<String>   _recChars;
    private String                _constraint;

    private CGlyph                _currentGlyph;
    private int[]                 _screenCoord  = new int[2];
    private Boolean               _watchable    = false;

    private Boolean               _touchStarted = false;
    private String                _onStartWriting;

    private RecogDelay            _counter;
    private long                  _time;
    private long                  _prevTime;

    private String                mResponse;
    private String                mStimulus;

    private Rect                  _viewBnds          = new Rect();  // The view draw bounds
    private float                 _dotSize;

    private LocalBroadcastManager bManager;

    private static int            RECDELAY   = 400;              // Just want the end timeout
    private static int            RECDELAYNT = RECDELAY+500;     // inhibit ticks

    // This is used to map "recognizer types" to LTK project ids
    //
    static public HashMap<String, String> recogMap = new HashMap<String, String>();

    static {
        recogMap.put("EN_STD_ALPHA", "SHAPEREC_ALPHANUM");
        recogMap.put("EN_STD_NUM", "SHAPEREC_NUMERALS");
    }

    // This is used to map "recognizer types" to LTK project folders
    //
    static public HashMap<String, String> folderMap = new HashMap<String, String>();

    static {
        folderMap.put("EN_STD_ALPHA", "/projects/alphanumeric/config/");
        folderMap.put("EN_STD_NUM", "/projects/demonumerals/config/");
    }


    private static final String   TAG = "CFingerWriter";



    public CFingerWriter(Context context) {
        super(context);
        init(context, null);
    }

    public CFingerWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CFingerWriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Common object initialization for all constructors
     *
     */
    protected void init(Context context, AttributeSet attrs ) {
        String linkedViews;

        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                linkedViews = a.getNonResourceString(R.styleable.RoboTutor_linked_views);
                mAspect     = a.getFloat(R.styleable.RoboTutor_aspectratio, 1.0f);

                mLinkedViews = Arrays.asList(linkedViews.split(","));

            } finally {
                a.recycle();
            }
        }

        // Create a paint object to define the outline parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);

        // Create a paint object to define the baseline parameters
        mPaintBase = new Paint();

        mPaintBase.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintBase.setStyle(Paint.Style.STROKE);
        mPaintBase.setStrokeJoin(Paint.Join.ROUND);
        mPaintBase.setStrokeWidth(6);
        mPaintBase.setAntiAlias(true);

        // Create a paint object to define the topline parameters
        mPaintUpper = new Paint();

        mPaintUpper.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintUpper.setStyle(Paint.Style.STROKE);
        mPaintUpper.setStrokeJoin(Paint.Join.ROUND);
        mPaintUpper.setStrokeWidth(6);
        mPaintUpper.setAlpha(100);
        mPaintUpper.setPathEffect(new DashPathEffect(new float[]{25f, 12f}, 0f));
        mPaintUpper.setAntiAlias(true);

        _counter = new RecogDelay(RECDELAY, RECDELAYNT);

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        // Initialize the path object
        clearStroke();
    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    @Override
    public boolean isGraphEventSource() {
        return false;
    }

    /**
     * Must be Overridden to access mTutor
     * @param linkedView
     */
    @Override
    public void addEventListener(String linkedView) {
    }

    @Override
    public void addEventListener(IEventListener listener) {

    }

    @Override
    public void dispatchEvent(IEvent event) {

        // Do defferred listeners configuration - this cannot be done until after the
        // view has been inflated so cannot be in init()
        //
        if(!mListenerConfigured) {
            for (String linkedView : mLinkedViews) {
                addEventListener(linkedView);
            }
            mListenerConfigured = true;
        }
        for (IEventListener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {

        switch(event.getType()) {

            // Message from Stimiulus variant to share state with response variant
            case TCONST.FW_STIMULUS:
                    mStimulus = (String)event.getString(TCONST.FW_VALUE);
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int finalWidth, finalHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec );

        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        finalWidth  = (int)(originalHeight * mAspect);
        finalHeight = originalHeight;

        setMeasuredDimension(finalWidth, finalHeight);

//        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
//                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
//        super.onMeasure(
//                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
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

        getDrawingRect(_viewBnds);

        // We calc a view specific dot size for periods colons etc.
        //
        _dotSize = _viewBnds.width() / TCONST.DOT_SIZE;

        mTopLine  = getHeight() / 3;
        mBaseLine = mTopLine * 2;
    }


    private void broadcastLocation(String Action, PointF touchPt) {

        if(_watchable) {
            getLocationOnScreen(_screenCoord);

            // Let the persona know where to look
            Intent msg = new Intent(Action);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

            bManager.sendBroadcast(msg);
        }
    }


    /**
     * Add Root vector to path
     *
     * @param touchPt
     */
    private void startTouch(PointF touchPt) {

        if(!_touchStarted) {
            _touchStarted = true;
            applyEventNode(_onStartWriting);
        }

        if (_counter != null)
            _counter.cancel();

        // We always add the next stoke to the glyph set
        //
        if (_currentGlyph == null)
            _currentGlyph = new CGlyph(mContext, mBaseLine, _viewBnds, _dotSize);

        _currentGlyph.newStroke();
        _currentGlyph.addPoint(touchPt);

        // Track current position
        //
        mPoint = touchPt;

        invalidate();
        broadcastLocation(TCONST.LOOKATSTART, touchPt);
    }


    /**
     * Test whether the motion is greater than the jitter tolerance
     *
     * @param touchPt
     * @return
     */
    private boolean testPointTolerance(PointF touchPt) {

        float dx = Math.abs(touchPt.x - mPoint.x);
        float dy = Math.abs(touchPt.y - mPoint.y);

        return (dx >= TOLERANCE || dy >= TOLERANCE)? true:false;
    }



    /**
     * Update the glyph path if motion is greater than tolerance - remove jitter
     *
     * @param touchPt
     */
    private void moveTouch(PointF touchPt) {

        // only update if we've moved more than the jitter tolerance
        //
        if(testPointTolerance(touchPt)){

            _currentGlyph.addPoint(touchPt);

            mPoint = touchPt;

            invalidate();
            broadcastLocation(TCONST.LOOKAT, touchPt);
        }
    }


    /**
     * End the current glyph path
     * TODO: Manage debouncing
     *
     */
    private void endTouch(PointF touchPt) {

        _counter.start();

        // Only add a new point to the glyph if it is outside the jitter tolerance
        if(testPointTolerance(touchPt)) {

            _currentGlyph.addPoint(touchPt);

            invalidate();
        }
        broadcastLocation(TCONST.LOOKATEND, touchPt);

        _currentGlyph.endStroke();
    }


    public boolean onTouch(View view, MotionEvent event) {
        PointF touchPt;
        long   delta;
        final int action = event.getAction();

        // TODO: switch back to setting onTouchListener
        if(_enabled) {
            super.onTouchEvent(event);

            touchPt = new PointF(event.getX(), event.getY());

            // inhibit input while the recognizer is thinking
            //
            if (!_isRecognizing) {

                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        _prevTime = _time = System.nanoTime();
                        startTouch(touchPt);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        _time = System.nanoTime();
                        moveTouch(touchPt);
                        break;
                    case MotionEvent.ACTION_UP:
                        _time = System.nanoTime();
                        endTouch(touchPt);
                        break;
                }
                delta = _time - _prevTime;

                //Log.i(TAG, "Touch Time: " + _time + "  :  " + delta);
                return true;

            }
        }
        return false;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        float topLine  = getHeight() / 3;
        float baseLine = topLine * 2;

        Path linePath = new Path();
        linePath.moveTo(0, topLine);
        linePath.lineTo(getWidth(), topLine);

        canvas.drawPath(linePath,  mPaintUpper);
        canvas.drawLine(0, baseLine, getWidth(), baseLine, mPaintBase);

        Rect drawBnds = new Rect();
        getDrawingRect(drawBnds);

        // Immediate mode graphics -
        // Redraw the current path
        //
        if(_currentGlyph != null) {

            for(int i1 = 0 ; i1 < _currentGlyph.size() ; i1++) {

                CStroke stroke = _currentGlyph.getStroke(i1);

                if(stroke.isPoint(drawBnds)) {

                    PointF center = stroke.getPoint();

                    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(center.x, center.y, drawBnds.width() / 40, mPaint);
                    mPaint.setStyle(Paint.Style.STROKE);
                }
                else {
                    canvas.drawPath(stroke.getPath(), mPaint);
                }
            }
        }
    }


    private void clearStroke() {

        // Create a path object to hold the vector stream

        _currentGlyph = null;

        invalidate();
    }


    /**
     * Return the last recognized charater in the given index
     * TODO: Manage bound violations
     *
     * @param id
     * @return
     */
    public String getRecChar(int id) {
        if(_recChars == null)
        {
            Log.d(TAG,"getRecChar is NULL - *************************");
            return "o";
        }

        Log.d(TAG, "getRecChar is - " + _recChars.get(id));
        return _recChars.get(id);
    }


    /**
     * This is how recognition is initiated - if the user stops writing for a
     * predefined period we assume they are finished writing.
     * Here it is used to dictate when a glyph is complete.
     */
    public class RecogDelay extends CountDownTimer {

        public RecogDelay(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            Log.i(TAG, "Glyph Completed");

            // Do any required post processing on the glyph
            //
            _currentGlyph.terminateGlyph();

            _isRecognizing = true;
            Log.i(TAG, "Recognition Done");

            // Tasks can only run once so create a new one for each recognition task.
            _recThread = new RecognizerThread();
            _recThread.execute();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.i(TAG, "Tick Done");
        }

    }


    /**
     * The RecognizerThread provides a background thread on which to do the rocognition task
     * TODO: We may need a scrim on the UI thread depending on the observed performance
     */
    class RecognizerThread extends AsyncTask<Void, Void, String> {

        RecognizerThread() {
        }

        public void setStrokes(CStroke[] _recognitionStrokes) {
            _recStrokes = _recognitionStrokes;
        }

        /** This is processed on the background thread - when it returns OnPostExecute is called or
        // onCancel if it was cancelled -
        */
        @Override
        protected String doInBackground(Void... unsued) {
            _recResults = _recognizer.recognize(_recStrokes);

            return null;
        }


        /** OnPostExecute is guaranteed to run on the UI thread so we can update the view etc
         * TODO: update this to do something useful
         * TODO: fix the way we process the constraint.
        */
        @Override
        protected void onPostExecute(String sResponse) {

            Pattern pattern = Pattern.compile(_constraint);

            for (CRecResult result : _recResults) {
//                Log.d("jni", "ShapeID = " + result.Id + " Confidence = " + result.Confidence);
            }

            _recStrokes = null;
            _recChars   = new ArrayList<String>();

            for (int i = 0; i < _recResults.length ; i++) {
               String recChar = _recognizer.getSymbolName(_recResults[i].Id, _configFolder);

                Matcher matcher = pattern.matcher(recChar);

                if(matcher.find()) {
                    _recChars.add(recChar);
                }
            }

            _isRecognizing = false;

            // Don't do any processing if there isn't a hypothesis

            if(_recChars.size() > 0) {

                mResponse = _recChars.get(0);

                // Let anyone interested know there is a new recognition set available
                // Do synchronous update
                //
                dispatchEvent( new CEvent(TCONST.FW_RESPONSE, TCONST.FW_VALUE, mResponse));

                // TODO : Remove this after testing
//                Intent msg = new Intent(TCONST.FW_RESPONSE);
//                msg.putExtra(TCONST.FW_VALUE, mResponseString);
//
//                bManager.sendBroadcast(msg);
            }

            // TODO: check for performance issues and run this in a separate thread if required.
            //
            if(mLogGlyphs)
                _currentGlyph.writeGlyphToLog(_recogId, _constraint, mStimulus, mResponse);

            // reset the stroke data
            //
            clearStroke();
        }


        /**
         *  The recognizer expects an "array" of strokes so we generate that here in the UI thread
         *
         */
        @Override
        protected void onPreExecute() {

            _recStrokes = new CStroke[_currentGlyph.size()];

            for (int s = 0; s < _currentGlyph.size(); s++)
                _recStrokes[s] = _currentGlyph.getStroke(s);
        }
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId) {

        _initialized = false;
        _constraint = ".";          // By default don't filter anything

        // Initialize lipitk
        File externalFileDir = getContext().getExternalFilesDir(null);
        String path = externalFileDir.getPath();

        Log.d("JNI", "Path: " + path);

        try {
            _recogId      = recogId;
            _recognizer   = new CLipiTKJNIInterface(path, recogMap.get(recogId));
            _configFolder = _recognizer.getLipiDirectory() + folderMap.get(recogId);

            _recognizer.initialize();
            _initialized = true;
        }
        catch(Exception e)
        {
            // TODO: Manage initialization errors more effectively
            CErrorManager.logEvent(TAG, "Cannot create Recognizer - Error:1", e, false);
        }

        // reset the internal state
        clearStroke();
    }


    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId, String subset) {

        // Add a regex that will filter the recognized input.
        //
        setRecognizer(recogId);
        _constraint = subset;
    }


    /**
     * Enable or Disable the finger writer
     * @param enableState
     */
    protected void enableFW(Boolean enableState) {

        if(_initialized) {

            _enabled = enableState;

            if (enableState) {
                // Reset the flag so onStartWriting events will fire
                _touchStarted = false;
                this.setOnTouchListener(this);

            } else {
                this.setOnTouchListener(null);
            }
        }
    }


    /**
     * Enable or disable persona messages.  Whether or not the persona will
     * watch finger motion
     *
     * @param watchEnabled
     */
    protected void personaWatch(Boolean watchEnabled) {

        _watchable = watchEnabled;
    }


    public void onStartWriting(String symbol) {
        _onStartWriting = symbol;
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    protected void applyEventNode(String nodeName) {
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************

}

