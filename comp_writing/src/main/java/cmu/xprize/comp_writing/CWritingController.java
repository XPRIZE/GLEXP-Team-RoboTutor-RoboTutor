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
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.CGlyphSet;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;


/**
 * TODO: document your custom view class.
 *
 *  !!!! NOTE: This requires com.android.support:percent:23.2.0' at least or the aspect ratio
 *  settings will not work correctly.
 *
 */
public class CWritingController extends PercentRelativeLayout implements IEventListener, IWritingController {

    protected Context         mContext;
    protected char[]          mStimulusData;

    private CLinkedScrollView mRecognizedScroll;
    private CLinkedScrollView mDrawnScroll;

    private LinearLayout      mRecogList;
    protected LinearLayout    mDrawnList;

    private int               mMaxLength = 1; //GCONST.ALPHABET.length();                // Maximum string length

    protected final Handler   mainHandler = new Handler(Looper.getMainLooper());
    protected HashMap         queueMap    = new HashMap();
    protected boolean         _qDisabled  = false;

    protected IGlyphSink      _recognizer;
    private CGlyphSet         _glyphSet;

    private String            mResponse;
    private String            mStimulus;

    protected boolean         _dataEOI   = false;

    final private String  TAG        = "CWritingController";


    public CWritingController(Context context) {
        super(context);
        init(context, null);
    }

    public CWritingController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CWritingController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        mContext = context;

        setClipChildren(false);
    }

    public void onCreate() {

        CDrawnInputController v;
        CResponseContainer    r;

        mStimulusData = new char[mMaxLength];

        // Setup the Recycler for the recognized input views
        mRecognizedScroll = (CLinkedScrollView) findViewById(R.id.Sresponse);
        mRecogList = (LinearLayout) findViewById(R.id.Srecognized_glyphs);

        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //
        for(int i1 =0 ; i1 < mStimulusData.length ; i1++)
        {
            // create a new view
            r = (CResponseContainer)LayoutInflater.from(getContext())
                                        .inflate(R.layout.recog_resp_comp, null, false);

            mRecogList.addView(r);
        }

        //****************************

        mDrawnScroll = (CLinkedScrollView) findViewById(R.id.SfingerWriter);
        mDrawnScroll.setClipChildren(false);

        mDrawnList = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
        mDrawnList.setClipChildren(false);

        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //
        for(int i1 = 0 ; i1 < mStimulusData.length ; i1++)
        {
            // create a new view
            v = (CDrawnInputController)LayoutInflater.from(getContext())
                                        .inflate(R.layout.drawn_input_comp, null, false);

            v.setIsLast(i1 ==  mStimulusData.length-1);

            mDrawnList.addView(v);
            ((CDrawnInputController)v).setRecognizer(_recognizer);
            ((CDrawnInputController)v).setLinkedScroll(mDrawnScroll);
            ((CDrawnInputController)v).setWritingController(this);
        }

        // Obtain the prototype glyphs from the singleton
        //
        _recognizer = CRecognizerPlus.getInstance();

        _glyphSet = _recognizer.getGlyphPrototypes(); //new GlyphSet(TCONST.ALPHABET);

        for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

            CDrawnInputController comp = (CDrawnInputController) mDrawnList.getChildAt(i1);

            comp.setProtoGlyph(GCONST.ALPHABET.substring(i1,i1+1), _glyphSet.cloneGlyph(GCONST.ALPHABET.substring(i1,i1+1)));
        }

        mRecogList.setOnTouchListener(new RecogTouchListener());
        mDrawnList.setOnTouchListener(new drawnTouchListener());

        mRecognizedScroll.setLinkedScroll(mDrawnScroll);
        mDrawnScroll.setLinkedScroll(mRecognizedScroll);
    }

    public void onDestroy() {

    }


    //************************************************************************
    //************************************************************************
    // IWritingController Start


    /**
     * Note that only the mDrawnList will initiate this call
     *
     * @param child
     */
    public void deleteItem(View child) {
        int index = mDrawnList.indexOfChild(child);

        mDrawnList.removeViewAt(index);
        mRecogList.removeViewAt(index);
    }


    /**
     * Note that only the mDrawnList will initiate this call
     *
     * @param child
     */
    public void addItemAt(View child, int inc) {

        CDrawnInputController v;
        CResponseContainer    r;

        int index = mDrawnList.indexOfChild(child);

        // create a new view
        r = (CResponseContainer)LayoutInflater.from(getContext())
                                    .inflate(R.layout.recog_resp_comp, null, false);

        mRecogList.addView(r, index + inc);


        // create a new view
        v = (CDrawnInputController)LayoutInflater.from(getContext())
                                    .inflate(R.layout.drawn_input_comp, null, false);

        // Update the last child flag
        //
        if(index == mDrawnList.getChildCount()-1) {
            ((CDrawnInputController)child).setIsLast(false);
            v.setIsLast(true);
        }

        mDrawnList.addView(v, index + inc);

        ((CDrawnInputController)v).setRecognizer(_recognizer);
        ((CDrawnInputController)v).setLinkedScroll(mDrawnScroll);
        ((CDrawnInputController)v).setWritingController(this);

    }


    public void updateGlyph(IDrawnInputController child, String glyph) {

        int index = mDrawnList.indexOfChild((View)child);

        CResponseContainer respText = (CResponseContainer)mRecogList.getChildAt(index);

        respText.setResponseChar(glyph);
    }


    // Debug component requirement
    @Override
    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB) {
    }


    // IWritingController End
    //************************************************************************
    //************************************************************************



    //***********************************************************
    // Event Listener/Dispatcher - Start

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {

        CDrawnInputController v;
        CResponseContainer    r;

        switch(event.getType()) {

            // Message from Stimiulus variant to share state with response variant
            case TCONST.FW_STIMULUS:

                mStimulus = (String)event.getString(TCONST.FW_VALUE);


                // Add the recognized response display containers
                //
                mRecogList = (LinearLayout) findViewById(R.id.Srecognized_glyphs);

                for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
                {
                    // create a new view
                    r = (CResponseContainer)LayoutInflater.from(getContext())
                                                .inflate(R.layout.recog_resp_comp, null, false);

                    mRecogList.addView(r);
                }


                // Add the Glyph input containers
                //
                mDrawnList = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
                mDrawnList.setClipChildren(false);

                for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
                {
                    // create a new view
                    v = (CDrawnInputController)LayoutInflater.from(getContext())
                                                .inflate(R.layout.drawn_input_comp, null, false);

                    v.setIsLast(i1 ==  mStimulus.length()-1);

                    mDrawnList.addView(v);
                    ((CDrawnInputController)v).setRecognizer(_recognizer);
                    ((CDrawnInputController)v).setLinkedScroll(mDrawnScroll);
                    ((CDrawnInputController)v).setWritingController(this);
                }

                break;

            // Message from the recognizer to update the response state
            case TCONST.FW_RESPONSE:
                break;

            case TCONST.FW_EOI:
                _dataEOI = true;        // tell the response that the data is exhausted
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    public class RecogTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            PointF touchPt;
            final int action = event.getAction();

            touchPt = new PointF(event.getX(), event.getY());

            //Log.i(TAG, "ActionID" + action);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "RECOG _ ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "RECOG _ ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "RECOG _ ACTION_UP");
                    break;
            }
            return true;
        }
    }


    public class drawnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            PointF touchPt;
            final int action = event.getAction();

            touchPt = new PointF(event.getX(), event.getY());

            //Log.i(TAG, "ActionID" + action);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "DRAWN _ ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "DRAWN _ ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "DRAWN _ ACTION_UP");
                    break;
            }
            return true;
        }
    }


    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _command    = "";
        protected String _target     = "";
        protected String _item       = "";

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, String target) {
            _command = command;
            _target  = target;
        }

        public Queue(String command, String target, String item) {
            _command = command;
            _target  = target;
            _item    = item;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                switch(_target) {
                    case "STIMULUS":
                        break;

                    case "RESPONSE":
                        break;

                    case "RECOGNIZER":
                        _recognizer.execCommand(_command);
                        break;

                    case "GLYPH":
                        break;

                    default:

                        break;
                }


            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new Queue(command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new Queue(command, target, item), delay); }




    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************






    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // TODO: DBUG DEBUG DEBUG   START

        int size = 1298;

        final int specMode = MeasureSpec.getMode(widthMeasureSpec);
        final int specSize = MeasureSpec.getSize(widthMeasureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }

        // TODO: DBUG DEBUG DEBUG  END

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "width  : " + getMeasuredWidth());
        Log.d(TAG, "height : " + getMeasuredHeight());

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


}
