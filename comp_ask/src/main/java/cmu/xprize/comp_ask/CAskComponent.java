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

package cmu.xprize.comp_ask;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CAskComponent extends FrameLayout implements ILoadableObject {

    protected Context           mContext;

    protected List<String>      _data;
    protected int               _dataIndex = 0;
    protected boolean           _dataEOI   = false;

    // json loadable
    public String[]             dataSource;

    final private String  TAG = "CAskComponent";



    public CAskComponent(Context context) {
        super(context);
        init(context, null);

    }

    public CAskComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAskComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        mContext = context;
    }


    public void onDestroy() {

    }



    //**********************************************************
    //**********************************************************
    //*****************  DataSink Interface


    public boolean dataExhausted() {
        return true;
    }


    public void setDataSource(String[] dataSource) {}


    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
    }
}
