/**
 Copyright(c) 2015-2017 Kevin Willows
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

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONObject;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TStimRespBase extends CStimRespBase implements ITutorObject, IDataSink  {


    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private int   _wrong   = 0;
    private int   _correct = 0;


    private static final String  TAG = TStimRespBase.class.getSimpleName();



    public TStimRespBase(Context context) {
        super(context);
        init(context, null);
    }

    public TStimRespBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TStimRespBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null);
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


    @Override
    public void addEventListener(String linkedView) {

        mListeners.add((IEventListener) mTutor.getViewByName(linkedView));
    }


    private void cachedataSource() {

    }


    @Override
    public void updateText(String newChar) {

        super.updateText(newChar);

        if(mIsResponse) {

            // update the response variable  "<Sresponse>.value"
            mTutor.getScope().addUpdateVar(name() + ".value", new TString(mValue));
            mTutor.getScope().addUpdateVar(name() + ".valueUC", new TString(mValue.toUpperCase()));

            if (newChar.equals("???")) {
                mTutor.addFeature(TCONST.FWUNKNOWN);
                _wrong++;
            } else {

                if (mValue.equals(mStimulusString)) {
                    mTutor.addFeature(TCONST.FWCORRECT);
                    _correct++;
                } else {
                    mTutor.addFeature(TCONST.FWINCORRECT);
                    _wrong++;
                }

                // Set a flag if they're all correct when we are out of data
                //
                if (_dataEOI) {
                    if (_wrong == 0)
                        mTutor.addFeature(TCONST.ALL_CORRECT);
                }
            }


            // Do any on rec behaviors
            applyEventNode(_onRecognition);
        }
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }


    /**
     *
     * @param dataPacket
     */
    public void setDataSource(String dataPacket) {

        _correct = 0;
        _wrong   = 0;

        mTutor.delFeature(TCONST.ALL_CORRECT);
        mTutor.delFeature(TCONST.FWCORRECT);
        mTutor.delFeature(TCONST.FWINCORRECT);

        try {
            if (dataPacket.startsWith(TCONST.SOURCEFILE)) {
                dataPacket = dataPacket.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataPacket);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

                // Pass the loaded json dataSource array
                //
                setDataSource(dataSource);

            } else if (dataPacket.startsWith("db|")) {
                dataPacket = dataPacket.substring(3);

            } else if (dataPacket.startsWith("[")) {

                dataPacket = dataPacket.substring(1, dataPacket.length()-1);

                // Pass an array of strings as the data source.
                //
                setDataSource(dataPacket.split(","));

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), null, false);
        }

    }


    public void next() {

        mTutor.delFeature(TCONST.ALL_CORRECT);
        mTutor.delFeature(TCONST.FWCORRECT);
        mTutor.delFeature(TCONST.FWINCORRECT);

        super.next();

        // update the Scope response variable  "<Sstimulus>.value"
        //
        mTutor.getScope().addUpdateVar(name() + ".value", new TString(mValue));
        mTutor.getScope().addUpdateVar(name() + ".valueUC", new TString(mValue.toUpperCase()));

        if(dataExhausted()) {

            // set the script 'Feature'
            mTutor.addFeature(TCONST.FTR_EOI);

            // For stimulus controls broadcast the change so the response knows
            // Let interested listeners know the stimulus has been exhausted
            //
            dispatchEvent(new CEvent(TCONST.FW_EOI));
        }
    }

    public void show(Boolean showHide) {

        super.show(showHide);
    }

    public void clear() {

        super.clear();

    }


    public void setBackGround(String Color) {

        super.setBackGround(Color);
    }


    public void setForeGround(String Color) {

        super.setForeGround(Color);
    }


    /**
     * Deprecated - in favor of onRecognitionEvent
     *
     * @param symbol
     */
    public void onRecognitionComplete(String symbol) {
        onRecognitionEvent(symbol);
    }


    public void onRecognitionEvent(String symbol) {
        super.onRecognitionEvent(symbol);
    }


    /**
     * Deprecated Feb 17 2016
     *
     * @param flagState
     * @param Color
     */
    public void flagError(Boolean flagState, String Color) {
        CErrorManager.logEvent(TAG, "Unsuppported Function: " + "flagError", null, false);
    }


    public void applyEventNode(String nodeName) {
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


    // Tutor methods  End
    //************************************************************************
    //************************************************************************




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

    // Do deferred configuration - anything that cannot be done until after the
    // view has been inflated and init'd - where it is connected to the TutorEngine
    //
    @Override
    public void onCreate() {

        // Do deferred listeners configuration - this cannot be done until after the tutor is instantiated
        //
        if(!mListenerConfigured) {

            if(mLinkedViews != null) {
                for (String linkedView : mLinkedViews) {
                    addEventListener(linkedView);
                }
            }
            mListenerConfigured = true;
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



    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, (IScope2) scope);

    }
}
