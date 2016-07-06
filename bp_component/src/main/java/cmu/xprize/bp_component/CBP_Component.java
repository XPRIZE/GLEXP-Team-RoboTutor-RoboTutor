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

package cmu.xprize.bp_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import org.json.JSONObject;

import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CBP_Component extends FrameLayout implements ILoadableObject {

    // Make this public and static so sub-components may use it during json load to instantiate
    // controls on the fly.
    //
    static public Context   mContext;

    protected String        mDataSource;
    private   int           _dataIndex = 0;

    private IBubbleMechanic _mechanics;

    private boolean        correct = false;

    // json loadable
    public String          stimulus_type;
    public String[]        stimulus_data;
    public CBp_Data[]      dataSource;
    public CBpBackground   view_background;

    static final String TAG = "CBP_Component";

    private CBp_Data        _currData;



    public CBP_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CBP_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CBP_Component(Context context, AttributeSet attrs, int defStyleAttr) {
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
                mDataSource  = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

        // Allow onDraw to be called to start animations
        //
        setWillNotDraw(false);
    }


    public void setDataSource(CBp_Data[] _dataSource) {

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


    protected void updateDataSet(CBp_Data data) {

        Log.d(TAG, "test");

        _currData = data;

        if(_mechanics  != null)
            _mechanics.onDestroy();

        switch(data.question_type) {
            case "multiple-choice":
                _mechanics = new CBp_Mechanic_MC(mContext, this);
                break;

            case "rising":

                _mechanics = new CBp_Mechanic_RISE(mContext, this);
                break;
        }

        _mechanics.populateView(_currData);

        requestLayout();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if(changed || ((_mechanics != null) && !_mechanics.isInitialized())) {
            int width = r - l;
            int height = b - t;

            if(_mechanics != null)
                _mechanics.doLayout(width, height, _currData);
        }

        if(_mechanics != null) {
            _mechanics.post(BP_CONST.SHOW_STIMULUS, _currData);
        }

    }


    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

//        if(_mechanics != null) {
//            _mechanics.startAnimation();
//
//            // debug - To use this you must disable the background view
//            //_mechanics.onDraw(canvas);
//        }
    }


    public void UpdateValue(int value) {
    }


    protected boolean isCorrect() {
        return correct;
    }


    public boolean allCorrect(int numCorrect) {
        return (numCorrect == dataSource.length);
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

        addView(view_background);
    }

}
