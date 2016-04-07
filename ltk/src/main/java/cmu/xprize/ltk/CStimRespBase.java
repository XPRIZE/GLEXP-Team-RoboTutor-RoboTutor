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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;


public class CStimRespBase extends TextView  implements View.OnClickListener, ITextSink {

    private Context       mContext;

    private boolean       mIsResponse;
    private String[]      mLexemes;
    private String[]      mLinkLex;
    private int[]         mLexEnds;             // records location of the end of lexemes in mDisplayText string
    private boolean       mEmpty = false;
    private int           mTextColor;
    public  String        mValue;
    protected boolean     mShowState;

    private List<String>  _data;
    private int           _dataIndex = 0;

    protected CStimRespBase  mLinkedView;
    protected int            mLinkedViewID;

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
                mLinkedViewID = a.getResourceId(R.styleable.CStimResp_linkedView, 0);
                mTextColor    = getCurrentTextColor();
            } finally {
                a.recycle();
            }
        }
    }


    @Override
    public void addChar(String newChar) {

        updateText(newChar);
    }


    @Override
    public void setLinkedView(CStimResp respView) {
    }


    @Override
    public void onClick(View v) {

    }

    private void updateText(String newValue) {

        mValue = newValue;
        setText(mValue);
    }

    public boolean allCorrect(int numCorrect) {
        return (numCorrect == _data.size());
    }


    public boolean dataExhausted() {
        return (_dataIndex >= _data.size())? true:false;
    }

//    // We work left to right matching string lexemes
//    // If there is an error in a lexeme we hightlight the error.
//    // The remainder of the string is dehighlighted - indicating an indeterminate state
//    //
//    public void compareLinked(boolean compare) {
//
//        // This is only a valid call on response view types
//        if(mIsResponse) {
//
//            mComparing = compare;
//            try {
//                if (mComparing) {
//                    PercentRelativeLayout parentview = (PercentRelativeLayout)getParent();
//
//                    mLinkedView = (CStimResp)parentview.findViewById(mLinkedViewID);
//                    mLinkLex    = mLinkedView.getString().split(" ");
//                }
//            } catch (NullPointerException exp) {
//            }
//        }
//    }
//
//
//    public void setLinkedView(CStimResp respView) {
//        mLinkedView = respView;
//    }
//




    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    public void setDataSource(String[] dataSource) {

        _data      = new ArrayList<String>(Arrays.asList(dataSource));
        _dataIndex = 0;
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

    public void clear() {

        updateText("");
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


    // Tutor methods  End
    //************************************************************************
    //************************************************************************

}
