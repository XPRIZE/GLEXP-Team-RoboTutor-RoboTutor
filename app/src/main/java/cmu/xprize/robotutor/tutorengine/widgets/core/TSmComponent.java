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

package cmu.xprize.robotutor.tutorengine.widgets.core;


import android.content.Context;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.sm_component.CSm_Component;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TSmComponent extends CSm_Component implements ITutorObject, IDataSink {

    private CTutor               mTutor;
    private CObjectDelegate      mSceneObject;
    private CMediaManager        mMediaManager;
    private TLangToggle          mLangButton;
    private String               mSymbol;

    static final private String TAG = "TSmComponent";



    public TSmComponent(Context context) {
        super(context);
    }

    public TSmComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TSmComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

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


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    /**
     *
     * @param dataNameDescriptor
     */
    @Override
    public void setDataSource(String dataNameDescriptor) {

        try {

            if (dataNameDescriptor.startsWith(TCONST.LOCAL_FILE)) {
                String dataRelPath = dataNameDescriptor.substring(TCONST.LOCAL_FILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. sdcard/Download/RoboTutor/Math/en/button_data.json
                //
                String dataPath = TCONST.DOWNLOAD_RT_PATH + "/" +  dataRelPath + "/button_data.json";
                String jsonData = JSON_Helper.cacheDataByName(dataPath);

                loadJSON(new JSONObject(jsonData), null);
            }
            else if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.SOURCEFILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/session_manager/assets/data/<iana2_language_id>/
                // e.g. tutors/session_manager/assets/data/sw/
                //
                String dataPath = TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS;
                dataPath += "/" +  TCONST.DATA_PATH + "/" + mMediaManager.getLanguageIANA_2(mTutor) + "/";
                String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope());

            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

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

        // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
        //
        if(!(Configuration.noAsrApps(getContext()) && (intent.equals(TCONST.STORY_INTENT) || intent.equals(TCONST.QUESTIONS_INTENT)) )) {

            // update the response variable  "<Sresponse>.value"

            mTutor.getScope().addUpdateVar(name() + ".intent", new TString(intent));
            mTutor.getScope().addUpdateVar(name() + ".intentData", new TString(intentData));
            mTutor.getScope().addUpdateVar(name() + ".dataSource", new TString(dataSource));
            mTutor.getScope().addUpdateVar(name() + ".features", new TString(features));

            applyEventNode(mSymbol);
        }
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

                if(obj.testFeatures()) {
                    obj.applyNode();
                }

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

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    @Override
    public void onCreate() {

        ViewGroup parent = (ViewGroup)getParent();

        mLangButton = (TLangToggle)parent.findViewById(R.id.SlangToggle);
        mLangButton.setTransformationMethod(null);

        // Hide the language toggle on the release builds
        // TODO : Use build Variant to ensure release configurations
        //
        if (Configuration.languageOverride(getContext())) {

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
}
