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

package cmu.xprize.fw_component;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;


public class CStimRespBase extends TextView  implements View.OnClickListener, IEventListener, IEventDispatcher {

    private Context             mContext;
    public List<IEventListener> mListeners = new ArrayList<IEventListener>();
    protected List<String>      mLinkedViews;
    protected boolean           mListenerConfigured = false;

    // Used by control in response mode to maintain state info
    protected String        mStimulus;
    protected String        mResponse;
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

    protected String        _onRecognition;

    protected float         mAspect;           //   = 0.82f w/h

    static public String TAG = "CStimRespBase";



    static protected HashMap<String, Integer> colorMap = new HashMap<String,Integer>();
    //
    // This is used to map "states" to colors

    static {
        colorMap.put("wrong", new Integer(0xffff0000));
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
                    R.styleable.CStimResp,
                    0, 0);

            try {
                mIsResponse = a.getBoolean(R.styleable.CStimResp_isResponse, false);
                mAspect     = a.getFloat(R.styleable.CStimResp_aspectratio, -1.0f);
                linkedViews = a.getNonResourceString(R.styleable.CStimResp_linked_views);
                mTextColor  = getCurrentTextColor();

                mLinkedViews = Arrays.asList(linkedViews.split(","));

            } finally {
                a.recycle();
            }
        }
    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    /**
     * Must be Overridden to access mTutor
     * @param linkedView
     */
    @Override
    public void addEventListener(String linkedView) {
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
                if (mIsResponse) {
                    mStimulus = (String)event.getString(TCONST.FW_VALUE);

                    preProcessStimulus();
                }
                break;

            // Message from the recognizer to update the response state
            case TCONST.FW_RESPONSE:
                if (mIsResponse) {
                    mResponse = (String)event.getString(TCONST.FW_VALUE);
                    updateText(mResponse);
                }
                break;

            case TCONST.FW_EOI:
                _dataEOI = true;        // tell the response that the data is exhausted
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    // Override in sub-class to provide non-standard stimulus processing.
    // e.g. turn a stimulus of "6" into an expected response of "six"
    //
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

//        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
//                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
//        super.onMeasure(
//                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }


    @Override
    public void onClick(View v) {

    }


    /**
     * Ths Stimulus variant of the control broadcasts its value to the response which is the
     *
     * @param newValue
     */
    protected void updateText(String newValue) {

        mValue = newValue;
        setText(mValue);

        // For stimulus controls broadcast the change
        if(!mIsResponse) {
            // Let interested listeners know the stimulus has changed
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
                    Log.e(TAG, "Error no DataSource : ");
                    System.exit(1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Data Exhuasted: call past end of data");
                System.exit(1);
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
            Log.e(TAG, "Invalid Color Name: "  + Color + " : " + e);
            System.exit(1);
        }
    }


    public void setForeGround(String Color) {
        try {
            setTextColor(colorMap.get(Color));
        }
        catch(Exception e) {
            Log.e(TAG, "Invalid Color Name: "  + Color + " : " + e);
            System.exit(1);
        }
    }

    public void onRecognitionComplete(String symbol) {
        _onRecognition = symbol;
    }


    // Must override in TClass
    // TClass domain provides access to tutor scriptables
    //
    protected void applyEventNode(String nodeName) {
    }

    // Scripting Interface  End
    //************************************************************************
    //************************************************************************

}
