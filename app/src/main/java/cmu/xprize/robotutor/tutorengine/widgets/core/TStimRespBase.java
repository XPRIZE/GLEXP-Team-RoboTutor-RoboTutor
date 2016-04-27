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

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TStimRespBase extends CStimRespBase implements ITutorObjectImpl {


    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private int   _wrong   = 0;
    private int   _correct = 0;

    private static final String  TAG = TStimResp.class.getSimpleName();



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
                mTutor.setAddFeature(TCONST.FWUNKNOWN);
                _wrong++;
            } else {

                if (mValue.equals(mStimulus)) {
                    mTutor.setAddFeature(TCONST.FWCORRECT);
                    _correct++;
                } else {
                    mTutor.setAddFeature(TCONST.FWINCORRECT);
                    _wrong++;
                }

                // Set a flag if they're all correct when we are out of data
                //
                if (_dataEOI) {
                    if (_wrong == 0)
                        mTutor.setAddFeature(TCONST.FWALLCORRECT);
                }
            }


            // Do any on rec behaviors
            applyEventNode(_onRecognition);
        }
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        _correct = 0;
        _wrong   = 0;

        mTutor.setDelFeature(TCONST.FWALLCORRECT);
        mTutor.setDelFeature(TCONST.FWCORRECT);
        mTutor.setDelFeature(TCONST.FWINCORRECT);

        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.TASSETS + "/" + dataSource);

            } else if (dataSource.startsWith("db|")) {
                dataSource = dataSource.substring(3);

            } else if (dataSource.startsWith("[")) {
                dataSource = dataSource.substring(1, dataSource.length()-1);

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Invalid Data Source for : " + name());
            System.exit(1);
        }

        // Pass an array of strings as the data source.
        //
        setDataSource(dataSource.split(","));
    }


    public void next() {

        mTutor.setDelFeature(TCONST.FWALLCORRECT);
        mTutor.setDelFeature(TCONST.FWCORRECT);
        mTutor.setDelFeature(TCONST.FWINCORRECT);

        super.next();

        // update the Scope response variable  "<Sstimulus>.value"
        //
        mTutor.getScope().addUpdateVar(name() + ".value", new TString(mValue));
        mTutor.getScope().addUpdateVar(name() + ".valueUC", new TString(mValue.toUpperCase()));

        if(dataExhausted()) {

            // set the script 'Feature'
            mTutor.setAddFeature(TCONST.FTR_EOI);

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

    public void onRecognitionComplete(String symbol) {
        super.onRecognitionComplete(symbol);
    }


    /**
     * Deprecated Feb 17 2016
     *
     * @param flagState
     * @param Color
     */
    public void flagError(Boolean flagState, String Color) {
        Log.e(TAG, "Unsuppported Function: " + "flagError");
        System.exit(1);
    }


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
    public void postInflate() {

        // Do deferred listeners configuration - this cannot be done until after the
        //
        if(!mListenerConfigured) {
            for (String linkedView : mLinkedViews) {
                addEventListener(linkedView);
            }
            mListenerConfigured = true;
        }
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {
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
    public void setAlpha(Float alpha) {
        mSceneObject.setAlpha(alpha);
    }
}
