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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.util.TCONST;


public class CStimRespBase extends TextView  implements View.OnClickListener  {

    private Context       mContext;

    // Used by control in response mode to maintain state info
    protected String      mStimulus;
    protected String      mResponse;
    protected boolean     mIsResponse;

    private String[]      mLexemes;
    private String[]      mLinkLex;
    private int[]         mLexEnds;             // records location of the end of lexemes in mDisplayText string
    private boolean       mEmpty = false;
    private int           mTextColor;
    public  String        mValue;
    protected boolean     mShowState;

    private List<String>  _data;
    private int           _dataIndex = 0;
    protected boolean     _dataEOI   = false;
    protected String      _onRecognition;

    protected float       mAspect;           //   = 0.82f w/h

    protected LocalBroadcastManager bManager;



    static private HashMap<String, Integer> colorMap = new HashMap<String,Integer>();
    //
    // This is used to map "states" to colors

    static {
        colorMap.put("wrong", new Integer(0xffff0000));
        colorMap.put("normal", new Integer(0xff000000));
    }

    final static public String TAG = "CStimRespBase";




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


    private void init(Context context, AttributeSet attrs) {
        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CStimResp,
                    0, 0);

            try {
                mIsResponse   = a.getBoolean(R.styleable.CStimResp_isResponse, false);
                mAspect       = a.getFloat(R.styleable.CStimResp_aspectratio, 1.0f);
                mTextColor    = getCurrentTextColor();
            } finally {
                a.recycle();
            }
        }

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(TCONST.FW_STIMULUS);
        filter.addAction(TCONST.FW_RESPONSE);
        filter.addAction(TCONST.FW_EOI);
        bManager.registerReceiver(new ChangeReceiver(), filter);
    }


    class ChangeReceiver extends BroadcastReceiver {
        public void onReceive (Context context, Intent intent) {

            switch(intent.getAction()) {

                case TCONST.FW_STIMULUS:
                    if (mIsResponse) {
                        mStimulus = intent.getStringExtra(TCONST.FW_VALUE);
                    }
                    break;

                case TCONST.FW_RESPONSE:
                    if (mIsResponse) {
                        mResponse = intent.getStringExtra(TCONST.FW_VALUE);
                        updateText(mResponse);
                    }
                    break;

                case TCONST.FW_EOI:
                    _dataEOI = true;        // tell the response that the data is exhausted
                    break;
            }
        }
    }




    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int finalWidth, finalHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec );

        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        finalWidth  = (int)(originalHeight * mAspect);
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

    protected void updateText(String newValue) {

        mValue = newValue;
        setText(mValue);

        // For stimulus controls broadcast the change
        if(!mIsResponse) {
            // Let interested listeners know the stimulus has changed
            //
            Intent msg = new Intent(TCONST.FW_STIMULUS);
            msg.putExtra(TCONST.FW_VALUE, newValue);

            bManager.sendBroadcast(msg);
        }
    }

    public boolean allCorrect(int numCorrect) {
        return (numCorrect == _data.size());
    }


    public boolean dataExhausted() {
        return (_dataIndex >= _data.size())? true:false;
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    public void setDataSource(String[] dataSource) {

        _data      = new ArrayList<String>(Arrays.asList(dataSource));
        _dataIndex = 0;
        _dataEOI   = false;
    }

    public String getValue() {
        return mValue;
    }

    public void next() {

        try {
            if (_data != null) {
                updateText(_data.get(_dataIndex));

                _dataIndex++;
            } else {
                Log.e(TAG, "Error no DataSource : ");
                System.exit(1);
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Data Exhuasted: call past end of data");
            System.exit(1);
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



    // Tutor methods  End
    //************************************************************************
    //************************************************************************

}
