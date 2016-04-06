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

package cmu.xprize.sm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CSm_Component extends ScrollView implements ILoadableObject, ILaunchListener {

    private Context                     mContext;
    private LinearLayout                mContainer;
    private ArrayList<CSm_RowContainer> mRows;

    // json loadable
    public CSm_Class[]      dataSource;

    static final String TAG = "CSm_Component";


    public CSm_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CSm_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSm_Component(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs ) {

        inflate(getContext(), R.layout.smcomp_layout, this);

        mContext = context;

        mContainer = (LinearLayout)findViewById(R.id.SclassContainer);
    }


    private void buildInterface() {

        mRows = new ArrayList<>();
        mContainer.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        for (CSm_Class Row : dataSource) {

            Log.d(TAG, "Creating Sm Row : " + Row.description);

            CSm_RowContainer newRow = new CSm_RowContainer(mContext);

            newRow.buildInterface(Row, this);

            mRows.add(newRow);
            mContainer.addView(newRow);
        }

        requestLayout();
        invalidate();
    }


    /**
     * This must be overridden in the tutor T subclass to initialize variables in the
     * tutor scope.
     *
     * @param intent
     * @param intentData
     */
    public void setTutorIntent(String intent, String intentData, String features) {
    }


    /**
     * This must be overridden in the tutor T subclass to initialize variables in the
     * tutor scope.
     *
     * @param symbol
     */
    public void onTutorSelect(String symbol) {
    }


    /**
     * This must be overridden in the tutor T subclass to initialize variables in the
     * tutor scope.
     *
    */
    protected void applyEventNode(String nodeName) {
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

        buildInterface();
    }
}
