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

package cmu.xprize.mn_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.View_Helper;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

public class CMn_Alley extends android.support.percent.PercentRelativeLayout {

    private IValueListener    _owner;
    private boolean           _isMissingNumber;

    private Button            Splus;
    private Button            Sminus;
    private CMn_IconSet       SiconSet;
    private CMn_Text          Snumber;

    private List<String>      mOptions;

    private int               mMaxValue;
    private int               mCurValue;


    static final String TAG = "CMn_Alley";


    public CMn_Alley(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Alley(Context context, IValueListener owner) {
        super(context);
        _owner = owner;     // callback target for value updates
        init(context, null);
    }

    public CMn_Alley(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Alley(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.mn_comp_vert, this);

        Splus    = (Button) View_Helper.getViewById(R.id.Splus, this);
        Sminus   = (Button) View_Helper.getViewById(R.id.Sminus, this);
        SiconSet = (CMn_IconSet) View_Helper.getViewById(R.id.SiconSet, this);
        Snumber  = (CMn_Text) View_Helper.getViewById(R.id.Snumber, this);

        // When plus or minus are pressed we also allow the component to perform any top level
        // behaviors defined in scripts
        //
        Splus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurValue < mMaxValue) {
                    Log.v(QGRAPH_MSG, "event.click: " + " CMn_Alley:Splus-clicked");

                    updateValue(mCurValue + 1, true);
                    _owner.performClick();
                }
            }
        });

        Sminus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurValue > 0) {
                    Log.v(QGRAPH_MSG, "event.click: " + " CMn_Alley:Sminus-clicked");

                    updateValue(mCurValue - 1, true);
                    _owner.performClick();
                }
            }
        });
    }

    private void updateValue(int newValue, boolean invalidate) {

        mCurValue = newValue;

        Snumber.setText(String.valueOf(newValue));
        SiconSet.setIconCount(newValue, invalidate);

        if(_isMissingNumber)
            _owner.UpdateValue(newValue);
    }


    public void wigglePart(String direction, float magnitude, long duration, int repetition, String part ) {

        View wiggleTarget = null;

        switch(part.toLowerCase()) {
            case "plus":
                wiggleTarget = Splus;
                break;

            case "minus":
                wiggleTarget = Sminus;
                break;

            case "number":
                wiggleTarget = Snumber;
                break;

            case "icons":
                wiggleTarget = SiconSet;
                break;

            default:
                wiggleTarget = this;
                break;
        }

        CAnimatorUtil.wiggle(wiggleTarget, direction, magnitude, duration, repetition);
    }



    public void setData(CMn_Data data, int index, int mnIndex) {

        _isMissingNumber = (index == mnIndex)? true:false;

        int visible = (_isMissingNumber)? View.VISIBLE: View.INVISIBLE;

        mMaxValue = data.maxvalue;

        if(data.options != null) {
            mOptions = Arrays.asList(data.options.split(","));

            SiconSet.setVisibility(View.GONE);
            Snumber.setVisibility(View.GONE);

            for(String option : mOptions) {

                switch(option) {
                    case TCONST.SHOWICONS:
                        SiconSet.setVisibility(View.VISIBLE);
                        break;

                    case TCONST.SHOWNUM:
                        Snumber.setVisibility(View.VISIBLE);
                        break;
                }
            }
            requestLayout();
        }

        Splus.setVisibility(visible);
        Sminus.setVisibility(visible);
        SiconSet.setMaxIcons(mMaxValue);

        if(visible == View.INVISIBLE) {
            updateValue(data.dataset[index], true);
        }
        else {
            updateValue(0, true);
        }

    }

    public boolean isCorrect(int corValue) {
        return mCurValue == corValue;
    }
}
