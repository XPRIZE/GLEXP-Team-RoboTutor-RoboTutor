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

package cmu.xprize.ltk;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import cmu.xprize.util.TCONST;


public class CFingerWriter extends View implements OnTouchListener {

    private Context            mContext;
    private ITextSink          mLinkedView;
    private int                mLinkedViewID = -1;
    protected boolean _enabled = false;

    private Path               mPath;
    private Paint              mPaint;
    private Paint              mPaintBase;
    private Paint              mPaintUpper;
    private float              mX, mY;

    private static final float TOLERANCE = 5;

    private LipiTKJNIInterface _recognizer;
    private RecognizerThread   _recThread;
    private boolean            _isRecognizing = false;
    private Stroke[]           _recStrokes;
    private RecResult[]        _recResults;
    private String[]           _recChars;

    private Stroke             _currentStroke;
    private ArrayList<Stroke>  _currentStrokeStore;
    private int[]              _screenCoord = new int[2];
    private Boolean            _watchable = false;

    private RecogDelay            _counter;
    private long                  _time;
    private long                  _prevTime;

    private LocalBroadcastManager bManager;
    public static String          RECMSG = "CHAR_RECOG";

    private static final String   TAG = "WritingComp";

    private static int            RECDELAY   = 400;              // Just want the end timeout
    private static int            RECDELAYNT = RECDELAY+500;      // inhibit ticks



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
    private void init(Context context, AttributeSet attrs ) {
        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CStimResp,
                    0, 0);

            try {
                mLinkedViewID = a.getResourceId(R.styleable.CStimResp_linkedView, 0);
            } finally {
                a.recycle();
            }
        }

        // Initialize lipitk
        File externalFileDir = getContext().getExternalFilesDir(null);
        String path = externalFileDir.getPath();

        Log.d("JNI", "Path: " + path);

        try {
            _recognizer = new LipiTKJNIInterface(path, "SHAPEREC_ALPHANUM");
//            _recognizer = new LipiTKJNIInterface(path, "SHAPEREC_NUMERALS");

            _recognizer.initialize();
        }
        catch(Exception e)
        {
            Log.d(TAG, "Cannot create Recognizer - Error:1");
            System.exit(1);
        }

        // reset the internal state
        clear();

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);

        // Create a paint object to deine the line parameters
        mPaintBase = new Paint();

        mPaintBase.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintBase.setStyle(Paint.Style.STROKE);
        mPaintBase.setStrokeJoin(Paint.Join.ROUND);
        mPaintBase.setStrokeWidth(6);
        mPaintBase.setAntiAlias(true);

        // Create a paint object to deine the line parameters
        mPaintUpper = new Paint();

        mPaintUpper.setColor(getResources().getColor(R.color.fingerWriterBackground));
        mPaintUpper.setStyle(Paint.Style.STROKE);
        mPaintUpper.setStrokeJoin(Paint.Join.ROUND);
        mPaintUpper.setStrokeWidth(6);
        mPaintUpper.setAlpha(100);
        mPaintUpper.setPathEffect(new DashPathEffect(new float[]{25f,12f},0f));
        mPaintUpper.setAntiAlias(true);

        _counter = new RecogDelay(RECDELAY, RECDELAYNT);

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    /**
     * Enable or Disable the finger writer
     * @param enableState
     */
    protected void enableFW(Boolean enableState) {

        if(enableState) {
            _enabled = enableState;
            this.setOnTouchListener(this);
        }
        else {
            _enabled = enableState;
            this.setOnTouchListener(null);
        }
    }


    /**
     * Enable or disable persona messages.  Whether or not the persona will
     * watch finger motion
     *
     * @param watchEnabled
     */
    protected void enablePersonaWatch(Boolean watchEnabled) {
        _watchable = watchEnabled;
    }


    /**
     * Add Root vector to path
     *
     * @param x
     * @param y
     */
    private void startTouch(float x, float y) {
        PointF touchPt;

        if (_counter != null)
            _counter.cancel();

        if (_currentStroke == null)
            _currentStroke = new Stroke();

        touchPt = new PointF(x, y);

        _currentStroke.addPoint(touchPt);

        // Add Root node
        mPath.moveTo(x, y);

        // Track current position
        mX = x;
        mY = y;

        invalidate();
        broadcastLocation(TCONST.LOOKATSTART, touchPt);
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

        Log.d(TAG, "getRecChar is - " + _recChars[id]);
        return _recChars[id];
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

            _currentStroke.addPoint(touchPt);

            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

            invalidate();
            broadcastLocation(TCONST.LOOKAT, touchPt);
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

        touchPt = new PointF(mX, mY);

        // Only add a new point to the glyph if it is outside the jitter tolerance
        if(testPointTolerance(x,y)) {

            _currentStroke.addPoint(touchPt);

            mPath.lineTo(x, y);
            invalidate();
        }
        broadcastLocation(TCONST.LOOKATEND, touchPt);

        // We always add the next stoke to the glyph set
        if (_currentStrokeStore == null)
            _currentStrokeStore = new ArrayList<Stroke>();

        _currentStrokeStore.add(_currentStroke);

        // TODO: to emulate current operation we store everything in a single stroke.
        //       This is not the intended use - we should clear it and start over
        //       also we should pass the currentStrokeStore to the recognizer not a single stroke

        _currentStroke = null;
    }


    public boolean onTouch(View view, MotionEvent event) {
        long   delta;
        final int action = event.getAction();

        super.onTouchEvent(event);

        // inhibit input while the recognizer is thinking
        //
        if(!_isRecognizing) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    _prevTime = _time = System.nanoTime();
                    startTouch(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    _time = System.nanoTime();
                    moveTouch(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    _time = System.nanoTime();
                    endTouch(x, y);
                    break;
            }
            delta = _time - _prevTime;

            Log.i(TAG, "Touch Time: " + _time + "  :  " + delta);
            return true;

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

        // Immediate mode graphics -
        // Redraw the current path
        canvas.drawPath(mPath, mPaint);
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


    private void clear() {

        // Create a path object to hold the vector stream
        mPath               = new Path();

        _currentStroke      = null;
        _currentStrokeStore = null;

        invalidate();
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


        public void setStrokes(Stroke[] _recognitionStrokes) {
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
        // TODO: update this to do something useful
        */
        @Override
        protected void onPostExecute(String sResponse) {
            clear();

            for (RecResult result : _recResults) {
                Log.e("jni", "ShapeID = " + result.Id + " Confidence = " + result.Confidence);
            }

            _recStrokes = null;

            String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/alphanumeric/config/";
//            String configFileDirectory = _recognizer.getLipiDirectory() + "/projects/demonumerals/config/";

            _recChars = new String[_recResults.length];

            for (int i = 0; i < _recChars.length; i++) {
                _recChars[i] = _recognizer.getSymbolName(_recResults[i].Id, configFileDirectory);
            }

            _isRecognizing = false;

            // If we are linked to a textSink then send it the new character
            if(mLinkedViewID != -1) {
                ViewGroup parentview = (ViewGroup)getParent().getParent();

                mLinkedView = (ITextSink)parentview.findViewById(mLinkedViewID);

                mLinkedView.addChar(_recChars[0]);
            }

            // Let anyone interested know there is a new recognition set available
            bManager.sendBroadcast(new Intent(RECMSG));
        }


        /**
         *  The recognizer expects an "array" of strokes so we generate that here in the UI thread
         *  from the ArrayList of captured strokes.
         *
         */
        @Override
        protected void onPreExecute() {

            _recStrokes = new Stroke[_currentStrokeStore.size()];

            for (int s = 0; s < _currentStrokeStore.size(); s++)
                _recStrokes[s] = _currentStrokeStore.get(s);
        }
    }
}

