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

package cmu.xprize.robotutor.tutorengine.widgets.core;


import android.content.Context;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import cmu.xprize.robotutor.BuildConfig;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.sm_component.CSm_Component;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TSmComponent extends CSm_Component implements ITutorObjectImpl, IDataSink {

    private CTutor               mTutor;
    private CObjectDelegate      mSceneObject;
    private TLangToggle          mLangButton;
    private String               mSymbol;

    static final private String TAG = "TSmComponent";



    public TSmComponent(Context context) {
        super(context);
        initT(context, null);
    }

    public TSmComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initT(context, attrs);
    }

    public TSmComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initT(context, attrs);
    }

    public void initT(Context context, AttributeSet attrs) {

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface

    /**
     *
     * @param dataSource
     */
    @Override
    public void setDataSource(String dataSource) {

        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);
                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataSource.startsWith("db|")) {


            } else if (dataSource.startsWith("{")) {

                loadJSON(new JSONObject(dataSource), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), null, false);
        }
    }


    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void setTutorIntent(String intent, String intentData, String dataSource, String features) {

        // update the response variable  "<Sresponse>.value"

        mTutor.getScope().addUpdateVar(name() + ".intent", new TString(intent));
        mTutor.getScope().addUpdateVar(name() + ".intentData", new TString(intentData));
        mTutor.getScope().addUpdateVar(name() + ".dataSource", new TString(dataSource));
        mTutor.getScope().addUpdateVar(name() + ".features", new TString(features));

        applyEventNode(mSymbol);
    }



    @Override
    public void onTutorSelect(String symbol) {
        mSymbol = symbol;
    }


    @Override
    protected void applyEventNode(String nodeName) {
        IScriptable2 obj = null;

        if(nodeName != null && !nodeName.equals("")) {
            try {
                obj = mTutor.getScope().mapSymbol(nodeName);
                obj.applyNode();

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }


    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mTutor = tutor;
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void onCreate() {

        ViewGroup parent = (ViewGroup)getParent();

        mLangButton = (TLangToggle)parent.findViewById(R.id.SlangToggle);
        mLangButton.setTransformationMethod(null);

        // Hide th language toggle on the release builds
        // TODO : ensure it is in place for trial releases
        if(!BuildConfig.DEBUG) {

            View view = findViewById(R.id.SsmComponent);
            PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) view.getLayoutParams();
            PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
            info.heightPercent = 0.88f;
            view.requestLayout();

            mLangButton.setVisibility(View.GONE);

            parent.requestLayout();
        }
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }


    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition ) {
        mSceneObject.wiggle(direction, magnitude, duration, repetition);
    }

    @Override
    public void setAlpha(Float alpha) {
        mSceneObject.setAlpha(alpha);
    }
}
