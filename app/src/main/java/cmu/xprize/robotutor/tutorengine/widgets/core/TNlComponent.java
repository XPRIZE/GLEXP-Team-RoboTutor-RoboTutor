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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

import cmu.xprize.nl_component.CNl_Component;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IArraySource;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.robotutor.tutorengine.graph.vars.type_array;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.util.TCONST.ASREventMap;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;


/**
 * Scriptable number listener component
 */
public class TNlComponent extends CNl_Component implements IBehaviorManager, ITutorObject, IArraySource, IDataSink, IEventSource
{
    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager   mMediaManager;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap = new HashMap<>();

    private String          debugHypSet;

    private int             _wrong   = 0;
    private int             _correct = 0;


    static final private String TAG = "TNlComponent";
    private TTextView debugHypothesisView;


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

        // Create a listener to process the ASR input
        // Note this ability to switch processors is primarily for development purposes to
        // test different listener designs
        //
        createInputProcessor(TCONST.PLRT);
        //createInputProcessor(TCONST.JSGF);

        // Push the ASR listener reference into the super class in the Java domain
        //
        prepareListener(CMediaController.getTTS());
    }


    @Override
    public void onDestroy() {

        CMediaController.removeListener(mListener);

        if(mListener != null)
                mListener.stop();

        // Publish the Stimulus state
        if(!mIsResponse) {

            TScope scope = mTutor.getScope();

            // Clear the scope variables
            //
            scope.addUpdateVar(name() + ".digits", null);
            scope.addUpdateVar(name() + ".value",  null);
            scope.addUpdateVar(name() + ".string", null);
            scope.addUpdateVar(name() + ".text",   null);

            // Clear the scope variables
            //
            scope.addUpdateVar(name() + TCONST.DIGIT_STRING_VAR, null);
            scope.addUpdateVar(name() + TCONST.PLACE_STRING_VAR, null);
            scope.addUpdateVar(name() + TCONST.DIGIT_TEXT_VAR, null);
            scope.addUpdateVar(name() + TCONST.PLACE_TEXT_VAR, null);

            scope.addUpdateVar(name() + ".error", null);
            scope.addUpdateVar(name() + ".warning", null);
        }
    }


    @Override
    public void addEventListener(String linkedView) {

        mListeners.add((IEventListener) mTutor.getViewByName(linkedView));
    }

    /**
     *  Inject the listener into the MediaManageer
     */
    @Override
    public void setListener(ListenerBase listener) {
        CMediaController.setListener(listener);
    }

    /**
     *  Remove the listener from the MediaManageer
     */
    @Override
    public void removeListener(ListenerBase listener) {
        CMediaController.removeListener(listener);
    }

    /**
     * Return tutor current working language
     */
    @Override
    public String getLanguage() {
        return mMediaManager.getLanguageIANA_2(mTutor);
    }

    /**
     * Return tutor current working language
     */
    @Override
    public String getLanguageFeature() {
        return mMediaManager.getLanguageFeature(mTutor);
    }


    /**
     * Override in Tutor sub-class to access text view id in layout
     * @param newValue
     */
    @Override
    public void updateDebugText(String newValue) {

        if(newValue == "") {
            debugHypSet = "";
        }
        else {
            if (debugHypothesisView == null)
                debugHypothesisView = (TTextView) mTutor.getViewById(R.id.Shypothesis, null);

            debugHypSet += newValue + "\n";
            debugHypothesisView.setText(debugHypSet);
        }
    }


    private void clearPlaceValueFeatures() {

        for(int i1 = 4 ; i1 >= 1 ; i1--) {
            mTutor.delFeature(TCONST.FTR_PLACE_ + i1 + TCONST._USED);
            mTutor.delFeature(TCONST.FTR_P + i1 + TCONST._1WORDS);
            mTutor.delFeature(TCONST.FTR_P + i1 + TCONST._2WORDS);
            mTutor.delFeature(TCONST.FTR_P + i1 + TCONST._3WORDS);

            mTutor.delFeature(TCONST.FTR_D + i1 + TCONST._1WORDS);
            mTutor.delFeature(TCONST.FTR_D + i1 + TCONST._2WORDS);
            mTutor.delFeature(TCONST.FTR_D + i1 + TCONST._3WORDS);
        }
    }


    private void publishPlaceValueFeatures() {

        clearPlaceValueFeatures();

        for(int i1 = 4 ; i1 >= 1 ; i1--) {
            if(mInputProcessor.isPlaceValueUsed(i1)) {
                mTutor.addFeature(TCONST.FTR_PLACE_ + i1 + TCONST._USED);

                int wordCnt = mInputProcessor.wordsInPlaceValue(i1);

                if(wordCnt >=1)
                    mTutor.addFeature(TCONST.FTR_P + i1 + TCONST._1WORDS);
                if(wordCnt >=2)
                    mTutor.addFeature(TCONST.FTR_P + i1 + TCONST._2WORDS);
                if(wordCnt >=3)
                    mTutor.addFeature(TCONST.FTR_P + i1 + TCONST._3WORDS);


                wordCnt = mInputProcessor.wordsInDigitValue(i1);

                if(wordCnt >=1)
                    mTutor.addFeature(TCONST.FTR_D + i1 + TCONST._1WORDS);
                if(wordCnt >=2)
                    mTutor.addFeature(TCONST.FTR_D + i1 + TCONST._2WORDS);
                if(wordCnt >=3)
                    mTutor.addFeature(TCONST.FTR_D + i1 + TCONST._3WORDS);

            }
        }
    }


    /**
     * Publish the Stimulus value as Scope variables for script access
     * We have several things
     *
     * sStimulus.digits      - 3  (number of digits)
     *
     * sStimulusString      - "238"
     * sStimulusValue       - 238
     * sStimulusText        - "TWO HUNDRED THIRTY EIGHT"
     *
     * Note: the lists are in order of increasing place value
     *
     * sStimulusDigitString - ["8", "3", "2"]
     * sStimulusDigitValue  - [8, 3, 2]
     * sStimulusDigitText   - ["EIGHT", "THREE", "TWO"]
     *
     * sStimulusPlaceString - ["8", "30", "200"]
     * sStimulusPlaceValue  - [8, 30, 200]
     * sStimulusPlacetext   - ["eight", "thirty", "two hundred"]
     *
     */
    protected void publishStimulus() {

        // Publish the Stimulus state
        if(!mIsResponse) {

            TScope scope = mTutor.getScope();

            // publish the number of digits in the stimulus - e.g. Sstimulus.digits
            //
            scope.addUpdateVar(name() + ".digits", new TInteger(mInputProcessor.getLength()));
            scope.addUpdateVar(name() + ".value",  new TString(mInputProcessor.getString()));
            scope.addUpdateVar(name() + ".string", new TInteger(mInputProcessor.getValue()));
            scope.addUpdateVar(name() + ".text",   new TString(mInputProcessor.getText()));

            // publish the Place Values for the stimulus - e.g.
            //
            // If we haven't published this component before create the type_array objects
            // to access the state variables from scripts
            //
            try {
                if(!scope.containsSymbol(name() + TCONST.DIGIT_STRING_VAR)) {

                    scope.addUpdateVar(name() + TCONST.DIGIT_STRING_VAR, new type_array(this, TCONST.DIGIT_STRING_VAR));
                    scope.addUpdateVar(name() + TCONST.PLACE_STRING_VAR, new type_array(this, TCONST.PLACE_STRING_VAR));

                    scope.addUpdateVar(name() + TCONST.DIGIT_TEXT_VAR, new type_array(this, TCONST.DIGIT_TEXT_VAR));
                    scope.addUpdateVar(name() + TCONST.PLACE_TEXT_VAR, new type_array(this, TCONST.PLACE_TEXT_VAR));

                    scope.addUpdateVar(name() + TCONST.PLACE4_WORDS_VAR, new type_array(this, TCONST.PLACE4_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.PLACE3_WORDS_VAR, new type_array(this, TCONST.PLACE3_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.PLACE2_WORDS_VAR, new type_array(this, TCONST.PLACE2_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.PLACE1_WORDS_VAR, new type_array(this, TCONST.PLACE1_WORDS_VAR));

                    scope.addUpdateVar(name() + TCONST.DIGIT4_WORDS_VAR, new type_array(this, TCONST.DIGIT4_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.DIGIT3_WORDS_VAR, new type_array(this, TCONST.DIGIT3_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.DIGIT2_WORDS_VAR, new type_array(this, TCONST.DIGIT2_WORDS_VAR));
                    scope.addUpdateVar(name() + TCONST.DIGIT1_WORDS_VAR, new type_array(this, TCONST.DIGIT1_WORDS_VAR));
                }

                // Note that we only process 4 possible place values - to go for numbers above 9999 you would need
                // to extend this.
                //
                publishPlaceValueFeatures();

            } catch (Exception e) {
            }
        }
    }

    @Override
    public void publishState(int error, int warn) {

        TScope scope = mTutor.getScope();

        scope.addUpdateVar(name() + ".error", new TInteger(error));
        scope.addUpdateVar(name() + ".warning", new TInteger(warn));
    }


    //**********************************************************
    //**********************************************************
    //*****************  Scripting Interface


    // This provides access to the Stimulus array values as Strings
    //
    @Override
    public String deReference(String _listName, int index) {

        return mInputProcessor.deReference(_listName, index);
    }


    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        _correct = 0;
        _wrong   = 0;

        // Assume all correct unless proven otherwise
        // But clear result flags to start
        //
        mTutor.addFeature(TCONST.ALL_CORRECT);
        mTutor.delFeature(TCONST.FWCORRECT);
        mTutor.delFeature(TCONST.FWINCORRECT);


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
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), e, false);
        }

        // Pass an array of strings as the data source.
        //
        setDataSource(dataSource.split(","));

        // This sends the LANG_FTR to the response control where the listener is attached.
        // This allows the respsonse control to initialize the ASR language
        //
        dispatchEvent(new CEvent(TCONST.SET_LANG_FTR, TCONST.VALUE, getLanguageFeature()));
    }


    public void next() {

        reset();

        try {
            if (_data != null) {
                mStimulusString = _data.get(_dataIndex);
                preProcessStimulus();

                updateNumberString(mStimulusString);

                // Publish scriptable variables for the stimulus state
                //
                if(!mIsResponse)
                    publishStimulus();

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data  - ", e, false);
        }

        // Kill the recognizer thread and set the End Of Data flag
        //
        if(dataExhausted()) {
            mTutor.addFeature(TCONST.FTR_EOD);
        }
    }


    /**
     * @param language
     */
    @Override
    public void setLanguage(String language) {

        super.setLanguage(language);
    }


    public void reset() {

        mTutor.delFeature(TCONST.GENERIC_RIGHT);
        mTutor.delFeature(TCONST.GENERIC_WRONG);

    }


    @Override
    public void updateOutcomeState(boolean error) {

        if(error == TCONST.TRUE_ERROR) {
            mTutor.addFeature(TCONST.GENERIC_WRONG);
            mTutor.delFeature(TCONST.ALL_CORRECT);
        }
        else
            mTutor.addFeature(TCONST.GENERIC_RIGHT);
    }





    public void onStartTalking(String symbol) {
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



    // Scripting Interface  End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START


    public void setVolatileBehavior(String event, String behavior) {

        enableOnClickBehavior(event, behavior);

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (volatileMap.containsKey(event)) {
                volatileMap.remove(event);
            }
        } else {
            volatileMap.put(event, behavior);
        }

        // Configure the ASR static events in the listener itself
        //
        int eventType = ASREventMap.get(event);

        switch(eventType) {

            case TCONST.SILENCE_EVENT:
            case TCONST.SOUND_EVENT:
            case TCONST.WORD_EVENT:

                if (behavior.toUpperCase().equals(TCONST.NULL)) {

                    mListener.resetStaticEvent(eventType);
                }
                else {
                    mListener.configStaticEvent(eventType);
                }
                break;
        }
    }


    /** Special Behavior processing for timed ASR events which must be setup in the listener component
     *
     * @param event
     * @param behavior
     * @param timeout
     */
    public void setVolatileBehavior(String event, String behavior, int timeout) {

        // Setup the behavior
        //
        setVolatileBehavior(event, behavior);

        // Configure the ASR timed events in the listener itself
        //
        int eventType = ASREventMap.get(event);

        switch (eventType) {

            case TCONST.TIMEDSILENCE_EVENT:
            case TCONST.TIMEDSOUND_EVENT:
            case TCONST.TIMEDWORD_EVENT:

                if (behavior.toUpperCase().equals(TCONST.NULL)) {

                    mListener.resetTimedEvent(eventType);
                }
                else {
                    mListener.configTimedEvent(eventType, timeout);
                }
                break;
        }
    }


    public void setStickyBehavior(String event, String behavior) {

        enableOnClickBehavior(event, behavior);

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (stickyMap.containsKey(event)) {
                stickyMap.remove(event);
            }
        } else {
            stickyMap.put(event, behavior);
        }

        // Configure the ASR static events in the listener itself
        //
        int eventType = ASREventMap.get(event);

        switch(eventType) {

            case TCONST.SILENCE_EVENT:
            case TCONST.SOUND_EVENT:
            case TCONST.WORD_EVENT:

                if (behavior.toUpperCase().equals(TCONST.NULL)) {

                    mListener.resetStaticEvent(eventType);
                }
                else {
                    mListener.configStaticEvent(eventType);
                }
                break;
        }
    }


    /** Special Behavior processing for timed ASR events which must be setup in the listener component
     *
     * @param event
     * @param behavior
     * @param timeout
     */
    public void setStickyBehavior(String event, String behavior, int timeout) {

        // Setup the behavior
        //
        setStickyBehavior(event, behavior);

        // Configure the ASR timed events in the listener itself
        //
        int eventType = ASREventMap.get(event);

        switch(eventType) {

            case TCONST.TIMEDSILENCE_EVENT:
            case TCONST.TIMEDSOUND_EVENT:
            case TCONST.TIMEDWORD_EVENT:

                if (behavior.toUpperCase().equals(TCONST.NULL)) {

                    mListener.resetTimedEvent(eventType);
                }
                else {
                    mListener.configTimedEvent(eventType, timeout);
                }
                break;
        }

    }


    // Execute script target if behavior is defined for this event
    //
    @Override
    public boolean applyBehavior(String event) {

        boolean result = false;

        if(!(result = super.applyBehavior(event))) {

            if (volatileMap.containsKey(event)) {

                RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehavior,type:volatile,behavior:" + event);
                applyBehaviorNode(volatileMap.get(event));

                // clear the volatile behavior after use and update the listener if the event is a
                // listener event.
                //
                setVolatileBehavior(event, TCONST.NULL, 0);

                result = true;

            } else if (stickyMap.containsKey(event)) {

                RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehavior,type:sticky,behavior:" + event);
                applyBehaviorNode(stickyMap.get(event));

                result = true;
            }
        }

        return result;
    }


    /**
     * Apply Events in the Tutor Domain.
     *
     * @param nodeName
     */
    @Override
    public void applyBehaviorNode(String nodeName) {
        IScriptable2 obj = null;

        if (nodeName != null && !nodeName.equals("") && !nodeName.toUpperCase().equals("NULL")) {

            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if (obj != null) {

                    RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehaviornode,type:" + obj.getType() + ",behavior:" + nodeName);

                    switch(obj.getType()) {

                        case TCONST.SUBGRAPH:

                            mTutor.getSceneGraph().post(this, TCONST.SUBGRAPH_CALL, nodeName);
                            break;

                        case TCONST.MODULE:
                            // Disallow module "calls"
                            RoboTutor.logManager.postEvent_E(QGRAPH_MSG, "target:" + TAG + ",action:applybehaviornode,type:modulecall,behavior:" + nodeName +  ",ERROR:MODULE Behaviors are not supported");
                            break;

                        // Note that we should not preEnter queues - they may need to be cancelled
                        // which is done internally.
                        //
                        case TCONST.QUEUE:

                            if(obj.testFeatures()) {
                                obj.applyNode();
                            }
                            break;

                        default:

                            if(obj.testFeatures()) {
                                obj.preEnter();
                                obj.applyNode();
                            }
                            break;
                    }
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }


    /**
     * Do button like behavior defined for component itself - i.e. click anywhere
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        if(v == this) {
            Log.v(QGRAPH_MSG, "event.click: " + " TNlComponent: onClick");

            applyBehavior(TCONST.ON_CLICK);
        }

    }


    private void enableOnClickBehavior(String event, String behavior) {

        if(event.toUpperCase().equals(TCONST.ON_CLICK)) {

            if (behavior.toUpperCase().equals(TCONST.NULL)) {
                setOnClickListener(null);
            } else {
                setOnClickListener(this);
            }
        }
    }

    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IEventSource Interface START


    @Override
    public String getEventSourceName() {
        return name();
    }

    @Override
    public String getEventSourceType() {
        return "Reading_Component";
    }


    // IEventSource Interface END
    //************************************************************************
    //************************************************************************



    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

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

    // Do deferred configuration - anything that cannot be done until after the
    // view has been inflated and init'd - where it is connected to the TutorEngine
    //
    @Override
    public void onCreate() {

        // Do deferred listeners configuration - this cannot be done until after the inflation
        // and CTutor.childMap is complete
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
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

}
