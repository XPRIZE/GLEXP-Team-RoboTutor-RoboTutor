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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.rt_component.CRt_Component;
import cmu.xprize.rt_component.ICRt_ViewManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.Listener;

public class TRtComponent extends CRt_Component implements ITutorObjectImpl {

    private CTutor               mTutor;
    private CObjectDelegate      mSceneObject;

    private Listener             mListener;
    private String               mLanguage;
    private boolean              mIndexLoaded = false;

    // This is used to map "language features" to the story resources
    // these are located in the assets/<lang>
    // Note: on Android these are case sensitive filenames

    static private HashMap<String, String> langMap = new HashMap<String, String>();

    static {
        langMap.put("LANG_EN", "en");
        langMap.put("LANG_SW", "sw");
    }

    static final private String TAG = "TMnComponent";



    public TRtComponent(Context context) {
        super(context);
        initT(context, null);
    }

    public TRtComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initT(context, attrs);
    }

    public TRtComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initT(context, attrs);
    }

    public void initT(Context context, AttributeSet attrs) {

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        // Default to English language stories
        //
        mLanguage = langMap.get("LANG_EN");
        mListener = CTutor.ASR;

        // Push the ASR listener reference into the super class in the Java domain
        //
        prepareListener(CTutor.ASR, CTutor.TTS);
    }


    public void publishTargetWord(String word) {
        // update the response variable  "<SreadingComp>.nextword"
        mTutor.getScope().addUpdate(name() + ".nextword", new TString(word));

    }


    public void publishTargetWordIndex(int index) {
        // update the response variable  "<SreadingComp>.nextword"
        mTutor.getScope().addUpdate(name() + ".wordindex", new TInteger(index));

    }


    public void publishTargetSentence(String sentence) {
        // update the response variable  "<SreadingComp>.sentence"
        mTutor.getScope().addUpdate(name() + ".sentence", new TString(sentence));

    }



    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface

    /**
     * @param language
     */
    @Override
    public void setLanguage(String language) {

        super.setLanguage(language);

        mLanguage = langMap.get(language);
        setDataSource( TCONST.SOURCEFILE + TCONST.STORYINDEX);
    }


    /**
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + mLanguage + "/" + dataSource);
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataSource.startsWith("db|")) {


            } else if (dataSource.startsWith("{")) {

                loadJSON(new JSONObject(dataSource), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Invalid Data Source for : " + name());
            System.exit(1);
        }

        mIndexLoaded = true;
    }


    /**
     * @param storyName
     */
    public void setStory(String storyName) {

        for(int i1 = 0 ; i1 < dataSource.length ; i1++ ) {

            if(storyName.equals(dataSource[i1].story)) {

                Class<?> storyClass = viewClassMap.get(dataSource[i1].viewtype);

                try {
                    try {

                        mViewManager = (ICRt_ViewManager)storyClass.getConstructor(new Class[]{CRt_Component.class, Listener.class}).newInstance(this,mListener);
                        mViewManager.setPublishListener(this);

                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS +
                                                            "/" + mLanguage + "/" + dataSource[i1].folder + "/" + TCONST.STORYDATA);

                    mViewManager.loadJSON(new JSONObject(jsonData), null);

                } catch (InstantiationException e) {
                    // TODO: Manage Exceptions
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // we're done
                break;
            }
        }
    }


    public void next() {

        reset();

        super.next();

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOI);
    }


    public TBoolean test() {
        boolean correct = isCorrect();

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
}
