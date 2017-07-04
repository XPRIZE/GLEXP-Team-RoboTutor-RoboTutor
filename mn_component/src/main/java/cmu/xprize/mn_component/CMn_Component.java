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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class CMn_Component extends LinearLayout implements ILoadableObject, IValueListener {

    private   Context      mContext;
    private   float        mAlleyRadius;
    private   int          mAlleyMargin = 3;
    protected String       mDataSource;

    private   ArrayList<CMn_Alley> _alleys = new ArrayList<>();
    private   int                  _dataIndex = 0;
    private   int                  _mnindex;
    private   int                  _corValue;

    private boolean correct;


    // json loadable
    public CMn_Data[]      dataSource;

    static final String TAG = "CMn_Component";


    public CMn_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mAlleyRadius = a.getFloat(R.styleable.RoboTutor_radAlley, 0.25f);
                mDataSource  = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }
    }


    public void setDataSource(CMn_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }


    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG,  "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }


    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length)? true:false;
    }


    protected void updateDataSet(CMn_Data data) {

        int delta = data.dataset.length -_alleys.size();

        // More alleys than we need
        if(delta < 0) {
            while(delta < 0) {
                trimAlley();
                delta++;
            }
        }
        // Fewer alleys than we need
        else if(delta > 0) {
            while(delta > 0) {
                addAlley();
                delta--;
            }
        }

        // decode the index of the missing number
        //
        switch(data.mn_index) {

            case TCONST.RAND:
                _mnindex = (int)(Math.random() * data.dataset.length);
                break;

            case TCONST.MINUSONE:
                for(int i1 = 0 ; i1 < data.dataset.length ; i1++) {
                    if(data.dataset[i1] == 0) {
                        _mnindex = i1;
                        break;
                    }
                }
                break;

            default:
                _mnindex = Integer.parseInt(data.mn_index);
                break;
        }

        // Record the correct value
        //
        _corValue = data.dataset[_mnindex];

        // Apply the dataset to the alleys
        for(int i1 = 0 ; i1 < data.dataset.length ; i1++) {
            _alleys.get(i1).setData(data, i1, _mnindex);
        }
    }


    protected boolean isCorrect() {

        boolean correct = _alleys.get(_mnindex).isCorrect(_corValue);

        return correct;
    }


    public boolean allCorrect(int numCorrect) {
        return (numCorrect == dataSource.length);
    }


    public void UpdateValue(int value) {
    }


    /**
     * We override the delegate implementation to wiggle just the icons if no index/part are specified
     * as in the other override.
     *
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     */
    public void wiggle(String direction, float magnitude, long duration, int repetition ) {
        wiggle( direction,  magnitude,  duration,  repetition,  "icons",  _mnindex );
    }


    /**
     * We override the delegate implementation to wiggle just the icons if no index/part are specified
     * as in the other override.
     *
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     * @param part
     *
     */
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition, String part) {
        wiggle( direction,  magnitude,  duration,  repetition,  part,  _mnindex );
    }


    /**
     * Note that the parameters here have to be non-intrinsic types so that the scripting engine can instantiate
     * them. i.e. they must have a constructor.
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     * @param part
     * @param index
     */
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition, String part, Integer index ) {

        index = (index == -1)? _mnindex: index;

        _alleys.get(index).wigglePart(direction, magnitude, duration, repetition, part);
    }


    private CMn_Alley addAlley() {

        // Defining the layout parameters of the TextView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        lp.weight     = 1;
        lp.leftMargin = mAlleyMargin;

        // Setting the parameters on the TextView
        CMn_Alley alley = new CMn_Alley(mContext, this);
        alley.setLayoutParams(lp);

        _alleys.add(alley);

        addView(alley);

        return alley;
    }


    private void trimAlley() {

       removeView(_alleys.get(_alleys.size()-1));

        _alleys.remove(_alleys.size() - 1);
    }


    private void delAllAlley() {

        for(CMn_Alley alley: _alleys) {
            removeView(alley);
        }

        _alleys.clear();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
    }



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
