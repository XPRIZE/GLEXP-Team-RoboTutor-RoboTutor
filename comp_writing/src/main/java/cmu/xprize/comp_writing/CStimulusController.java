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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.util.CClassMap;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import android.util.Log;


/**
 */
@RemoteViews.RemoteView
public class CStimulusController extends RelativeLayout implements IEventListener, IEventDispatcher, ILoadableObject {

    private Context  mContext;

    public List<IEventListener>     mListeners          = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews        = new ArrayList<String>();
    protected boolean               mListenerConfigured = false;

    private CLinkedScrollView       mScrollView;
    private IWritingComponent mWritingController;

    private int                     _scrollX;
    private boolean                 _scrollChanged = false;

    protected final Handler         mainHandler = new Handler(Looper.getMainLooper());
    protected HashMap               queueMap    = new HashMap();
    protected boolean               _qDisabled  = false;

    // Used in response-mode to maintain state info

    protected String        mStimulusString;        // String representation - even for numbers e.g. "34"
    protected Boolean       singleStimulus;

    private Paint           _Paint;
    private Typeface        _fontFace;
    private TextView        mRecogChar;
    private ImageView       mUnderline;
    private GradientDrawable    mUnderlineDrawable;

    private int             mTextColor;
    public  String          mValue;
    protected boolean       mShowState;

    protected List<String>  _data;
    protected int           _dataIndex = 0;
    protected boolean       _dataEOI   = false;

    protected String[]      _placeValueColor = new String[20];

    protected String        _onRecognition;
    protected String        _onRecognitionError;

    protected float         mAspect;           //   = 0.82f w/h


    // json loadable
    public String[]               dataSource;


    static public String TAG = "CStimulusContainer";

    public CStimulusController(Context context) {
        super(context);
        init(context, null);
    }

    public CStimulusController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CStimulusController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
//                _aspect       = a.getFloat(R.styleable.RoboTutor_aspectratio, 1.0f);
//                _stroke_weight= a.getFloat(R.styleable.RoboTutor_strokeweight, 45f);

            } finally {
                a.recycle();
            }
        }

        // Create a paint object to hold the glyph and font draw parameters
        _Paint = new Paint();

        _Paint.setColor(TCONST.colorMap.get(TCONST.COLORNONE));
        _Paint.setStyle(Paint.Style.STROKE);
        _Paint.setStrokeWidth(5f);
        _Paint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRecogChar = (TextView)findViewById(R.id.recog_char);
        mUnderline = (ImageView)findViewById(R.id.recog_box);
        mUnderlineDrawable = (GradientDrawable) mUnderline.getDrawable();
        mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, TCONST.colorMap.get(TCONST.COLORNORMAL));

        // Font Face selection
        selectFont(TCONST.GRUNDSCHRIFT);

    }


    public void setLinkedScroll(CLinkedScrollView linkedScroll) {

        mScrollView = linkedScroll;
    }


    public void setWritingController(IWritingComponent writingController) {

        mWritingController = writingController;
    }


    public void selectFont(String fontSource) {

        String fontPath = TCONST.fontMap.get(fontSource.toLowerCase());

        if(fontPath != null) {

            _fontFace = Typeface.createFromAsset(mContext.getAssets(), fontPath);

            mRecogChar.setTypeface(_fontFace);
        }
    }


    public void setStimulusChar(String stim, Boolean singleStim) {

        mStimulusString = stim;
        singleStimulus = singleStim;

        if(singleStimulus) {
            mUnderline.setVisibility(View.INVISIBLE);
        }

        mRecogChar.setText(mStimulusString);
    }


    public void updateStimulusState(boolean match) {

        int charColor;
        int borderColor = TCONST.colorMap.get(TCONST.COLORNONE);

        if(!match) {
            charColor = TCONST.colorMap.get(TCONST.COLORERROR);
            mUnderline.setVisibility(View.VISIBLE);
            mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, charColor);
        }
        else {
            charColor = TCONST.colorMap.get(TCONST.COLORRIGHT);
//            charColor = new Integer(0xff0000ff);
            mUnderline.setVisibility(View.INVISIBLE);
        }

        mRecogChar.setTextColor(charColor);
        _Paint.setColor(borderColor);

        invalidate();
    }

    //amogh added
    //this is for changing the color of the response
    public void updateResponseState(boolean match) {

        int charColor;
        int borderColor = TCONST.colorMap.get(TCONST.COLORNONE);

        if(!match) {
            charColor = TCONST.colorMap.get(TCONST.COLORWRONG);
            mUnderline.setVisibility(View.INVISIBLE);
            mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, charColor);
        }
        else {
            if(mStimulusString.equals(" ") || mStimulusString.equals(" ")){
                charColor = TCONST.colorMap.get(TCONST.COLORRIGHT);
                mUnderline.setVisibility(View.INVISIBLE);
                mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, TCONST.colorMap.get(TCONST.COLORNORMAL));
            }
            else {
                charColor = TCONST.colorMap.get(TCONST.COLORRIGHT);
                mUnderline.setVisibility(View.INVISIBLE);
            }
        }

        mRecogChar.setTextColor(charColor);
        _Paint.setColor(borderColor);

        invalidate();
    }

    public void setUnderlineVisible(boolean visible){
        if(visible){
            mUnderline.setVisibility(View.VISIBLE);
        }
        else{
            mUnderline.setVisibility(View.INVISIBLE);
            int a = 0;
        }
    }
    //amogh added ends



    public boolean testStimulus(String resp) {
        return mStimulusString.equals(resp);
    }


    public void resetStimulusState() {
        int charColor   = TCONST.colorMap.get(TCONST.COLORNORMAL);
        int borderColor = TCONST.colorMap.get(TCONST.COLORNONE);

        mRecogChar.setTextColor(charColor);
        mUnderline.setVisibility(View.VISIBLE);
        mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, charColor);
        _Paint.setColor(borderColor);

        invalidate();
    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    @Override
    public boolean isGraphEventSource() {
        return false;
    }

    /**
     * Must be Overridden in app module to access tutor engine
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
                mStimulusString = (String)event.getString(TCONST.FW_VALUE);

                preProcessStimulus();
                break;

            // Message from the recognizer to update the response state
            case TCONST.FW_RESPONSE:
//                mResponseString = (String)event.getString(TCONST.FW_VALUE);
//
//                if (mIsResponse) {
//                    updateText(mResponseString);
//                }
                break;

            case TCONST.FW_EOI:
                _dataEOI = true;        // tell the response that the data is exhausted
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        Rect drawRegion = new Rect();
        mRecogChar.getDrawingRect(drawRegion);

//        canvas.drawLine(drawRegion.left, drawRegion.bottom, drawRegion.right, drawRegion.bottom, _Paint);

    }


    /**
     *  Override in sub-class to provide non-standard stimulus processing.
     *  e.g. turn a stimulus of "6" into an expected response of "six"
     * This is only ever to be called on a Stimulus Object
     */
    protected void preProcessStimulus() {
    }


//    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
//    {
//        int finalWidth, finalHeight;
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec );
//
//        int width = getMeasuredWidth();
//
//        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
//        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
//
//        if(mAspect >= 0) {
//            finalWidth = (int) (originalHeight * mAspect);
//        }
//        else  {
//            finalWidth = (int) (originalWidth);
//        }
//        finalHeight = originalHeight;
//
//        //mRecogChar.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeight * 0.7f);
//
//        setMeasuredDimension(width, finalHeight);
//    }



    /**
     * Do any requested place value hightlihgting before displaying the text
     *
     * @return
     */
    private SpannableStringBuilder doPlaceValueHighlight() {

        SpannableStringBuilder str = new SpannableStringBuilder(mValue);

        int color;

        try {
            for (int i1 = 1; i1 < mValue.length() + 1 ; i1++) {

                if (_placeValueColor[i1] != null) {

                    int place = (mValue.length() - i1);

                    if(TCONST.colorMap.containsKey(_placeValueColor[i1])) {
                        color = TCONST.colorMap.get(_placeValueColor[i1]);
                    }
                    else {
                        color = Color.parseColor(_placeValueColor[i1]);
                    }

                    str.setSpan(
                            new ForegroundColorSpan(color),
                            place,
                            place + 1,
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }
        }
        catch(Exception e) {
            // Just ignore it if we go beyond 20 characters - which will cause and error
            str = new SpannableStringBuilder(mValue);
        }

        return str;
    }



    /**
     * Ths Stimulus variant of the control broadcasts its value to the response variant
     *
     * @param newValue
     */
    public void updateText(String newValue) {

        boolean changed = false;

        if(mValue != newValue) changed = true;

        mValue = newValue;

        mRecogChar.setText(doPlaceValueHighlight());

        // For stimulus controls broadcast the change - ignore style changes
        //
        if(changed) {

            // Let interested listeners know the stimulus has changed -
            //
            dispatchEvent( new CEvent(TCONST.FW_STIMULUS, TCONST.FW_VALUE, newValue));
        }
    }


    public boolean allCorrect(int numCorrect) {
        return (numCorrect == _data.size());
    }


    public boolean dataExhausted() {
        return (_dataIndex >= _data.size())? true:false;
    }



    //**********************************************************
    //**********************************************************
    //*****************  Scripting Interface


    public void setDataSource(String[] dataSource) {

        // _data takes the form - ["92","3","146"]
        //
        _data      = new ArrayList<String>(Arrays.asList(dataSource));
        _dataIndex = 0;
        _dataEOI   = false;
    }


    public String getValue() {
        return mValue;
    }


    public void next() {

        // May only call next on stimulus variants
        //
        try {
            if (_data != null) {
                updateText(_data.get(_dataIndex));

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }


    public void show(Boolean showHide) {

        mShowState = showHide;

        setVisibility(mShowState? View.VISIBLE:View.INVISIBLE);
    }


    /**
     * Note that we must not call updateText here.
     */
    public void clear() {

        mRecogChar.setText("");
    }


    public void setBackGround(String Color) {
        try {
            setBackgroundColor(TCONST.colorMap.get(Color));
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Color Name: "  + Color + " : ", e, false);
        }
    }


    public void setForeGround(String Color) {

        clearPlaceValueColors();

        try {
            mRecogChar.setTextColor(TCONST.colorMap.get(Color));
            mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, TCONST.colorMap.get(Color));
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Color Name: "  + Color + " : ", e, false);
        }
    }


    /**
     * Allow scripted control over individual characters -
     * Used primarily for number highlighting
     * @param place
     * @param color
     */
    public void setPlaceValueColor(Integer place, String color) {

        if(place <= 0 || color == "")
            clearPlaceValueColors();
        else {
            _placeValueColor[place] = color;
        }

        // update the display
        updateText(mValue);
    }
    private void clearPlaceValueColors() {

        for(int i1 = 0 ; i1 < _placeValueColor.length ; i1++) {
            _placeValueColor[i1] = null;
        }
    }


    public void onRecognitionEvent(String symbol) {
        _onRecognition = symbol;
    }

    public void onRecognitionError(String symbol) {
        _onRecognitionError = symbol;
    }


    // Must override in TClass
    // TClass domain provides access to tutor scriptables
    //
    public void applyEventNode(String nodeName) {
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF    touchPt;
        boolean   result = true;
        final int action = event.getAction();

        super.onTouchEvent(event);

        touchPt = new PointF(event.getX(), event.getY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                _scrollX       = mScrollView.getScrollX();
                _scrollChanged = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if(_scrollX != mScrollView.getScrollX()) {
                    _scrollChanged = true;
                }
                break;

            case MotionEvent.ACTION_UP:

                if(!_scrollChanged) {
                    int currentIndex = ((LinearLayout) this.getParent()).indexOfChild(this); //amogh changed
                    mWritingController.stimulusClicked(currentIndex);
                }
                break;
        }

        return result;
    }


    // Scripting Interface  End
    //************************************************************************
    //************************************************************************


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

                switch(_command) {

                    case TCONST.HIGHLIGHT:

                        if(singleStimulus){
                            mUnderline.setVisibility(View.INVISIBLE);
                        } else {
                            mUnderline.setVisibility(View.VISIBLE);
                        }

                        mRecogChar.setTextColor(WR_CONST.HLCOLOR);
                        mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, WR_CONST.HLCOLOR);
                        invalidate();
                        post(TCONST.SHOW_NORMAL, WR_CONST.HIGHLIGHT_TIME);
                        break;

                    case TCONST.SHOW_NORMAL:

                        if(singleStimulus){
                            mUnderline.setVisibility(View.INVISIBLE);
                        } else {
                            mUnderline.setVisibility(View.VISIBLE);
                        }

                        mRecogChar.setTextColor(TCONST.colorMap.get(TCONST.COLORNORMAL));
                        mUnderlineDrawable.setStroke(TCONST.STROKE_STIM_UNDERLINE, TCONST.colorMap.get(TCONST.COLORNORMAL));
                        invalidate();
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

            mainHandler.removeCallbacks((CWritingComponent.Queue)(entry.getValue()));
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

        enQueue(new CStimulusController.Queue(command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new CStimulusController.Queue(command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new CStimulusController.Queue(command, target, item), delay); }




    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************




    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

    }


}