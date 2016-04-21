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
import android.util.AttributeSet;
import android.util.Log;

import cmu.xprize.nl_component.CNl_Component;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


/**
 * Scriptable number listener component
 */
public class TNlComponent extends CNl_Component implements ITutorObjectImpl{

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private int             _wrong   = 0;
    private int             _correct = 0;

    static final private String TAG = "TRtComponent";

    public TNlComponent(Context context) {
        super(context);
    }

    public TNlComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TNlComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }

    @Override
    public void onDestroy() {

    }


    @Override
    public void addEventListener(String linkedView) {

        mListeners.add((IEventListener) mTutor.getViewByName(linkedView));
    }


    /**
     * Return tutor current working language
     */
    @Override
    public String getLanguage() {
        return mTutor.getLanguage();
    }

    /**
     * Return tutor current working language
     */
    @Override
    public String getLanguageFeature() {
        return mTutor.getLanguageFeature();
    }


    //**********************************************************
    //**********************************************************
    //*****************  Scripting Interface

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

        reset();

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
            Log.e(TAG, "Data Exhuasted: call past end of data  - " + e);
            System.exit(1);
        }

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOI);
    }


    public TBoolean test() {
        boolean correct = false; //isCorrect();

        if(correct)
            mTutor.setAddFeature("FTR_RIGHT");
        else
            mTutor.setAddFeature("FTR_WRONG");

        return new TBoolean(correct);
    }


    public void reset() {

        mTutor.setDelFeature("FTR_RIGHT");
        mTutor.setDelFeature("FTR_WRONG");
    }


    public void onStartTalking(String symbol) {

    }
    public void onRecognitionComplete(String symbol) {

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

    // Scripting Interface  End
    //************************************************************************
    //************************************************************************



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

        // Push the TTS reference into the super class in the Java domain - Note that this must
        // be done after inflation as mTutor must have been initialized for getLanguage callback
        //
        prepareListener(CTutor.TTS);
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
    public void setNavigator(ITutorNavigator navigator) {
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
