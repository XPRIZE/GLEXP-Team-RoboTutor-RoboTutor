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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.util.CClassMap;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


public class CStimRespBase extends TextView  implements View.OnClickListener, IEventListener, IEventDispatcher, ILoadableObject {

    protected Context               mContext;

    public    List<IEventListener>  mListeners          = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews        = new ArrayList<String>();
    protected boolean               mListenerConfigured = false;

    // Used in response-mode to maintain state info

    protected String        mStimulusString;        // String representation - even for numbers e.g. "34"
    protected String        mResponseString;        // String representation - even for numbers e.g. "34"
    protected boolean       mIsResponse;

    private String[]        mLexemes;
    private String[]        mLinkLex;
    private int[]           mLexEnds;             // records location of the end of lexemes in mDisplayText string
    private boolean         mEmpty = false;

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


    static public String TAG = "CStimRespBase";



    static protected HashMap<String, Integer> colorMap = new HashMap<String,Integer>();
    //
    // This is used to map "states" to colors

    static {
        colorMap.put("wrong",  new Integer(0xffff0000));
        colorMap.put("right",  new Integer(0xff00ff00));
        colorMap.put("normal", new Integer(0xff000000));
    }





    public CStimRespBase(Context context) {
        super(context);
        init(context, null);
    }

    public CStimRespBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CStimRespBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        String linkedViews;

        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mIsResponse = a.getBoolean(R.styleable.RoboTutor_isResponse, false);
                mAspect     = a.getFloat(R.styleable.RoboTutor_aspectratio, -1.0f);
                linkedViews = a.getNonResourceString(R.styleable.RoboTutor_linked_views);
                mTextColor  = getCurrentTextColor();

                if(linkedViews != null) {
                    mLinkedViews = Arrays.asList(linkedViews.split(","));
                }

            } finally {
                a.recycle();
            }
        }
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
                mResponseString = (String)event.getString(TCONST.FW_VALUE);

                if (mIsResponse) {
                    updateText(mResponseString);
                }
                break;

            case TCONST.FW_EOI:
                _dataEOI = true;        // tell the response that the data is exhausted
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************


    /**
     *  Override in sub-class to provide non-standard stimulus processing.
     *  e.g. turn a stimulus of "6" into an expected response of "six"
     * This is only ever to be called on a Stimulus Object
     */
    protected void preProcessStimulus() {
    }


    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int finalWidth, finalHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec );

        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(mAspect >= 0) {
            finalWidth = (int) (originalHeight * mAspect);
        }
        else  {
            finalWidth = (int) (originalWidth);
        }
        finalHeight = originalHeight;

        setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeight * 0.7f);

        setMeasuredDimension(finalWidth, finalHeight);
    }


    @Override
    public void onClick(View v) {

    }


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

                    if(colorMap.containsKey(_placeValueColor[i1])) {
                        color = colorMap.get(_placeValueColor[i1]);
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

        setText(doPlaceValueHighlight());

        // For stimulus controls broadcast the change - ignore style changes
        //
        if(!mIsResponse && changed) {

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
        if(!mIsResponse) {

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
    }

    public void show(Boolean showHide) {

        mShowState = showHide;

        setVisibility(mShowState? View.VISIBLE:View.INVISIBLE);
    }


    /**
     * Note that we must not call updateText here.
     */
    public void clear() {

        setText("");
    }


    public void setBackGround(String Color) {
        try {
            setBackgroundColor(colorMap.get(Color));
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Color Name: "  + Color + " : ", e, false);
        }
    }


    public void setForeGround(String Color) {

        clearPlaceValueColors();

        try {
            setTextColor(colorMap.get(Color));
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

    // Scripting Interface  End
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
