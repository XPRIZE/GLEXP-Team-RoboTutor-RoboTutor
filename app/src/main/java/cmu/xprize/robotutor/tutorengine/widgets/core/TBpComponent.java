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
import android.content.Intent;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.bp_component.BP_CONST;
import cmu.xprize.bp_component.CBP_Component;
import cmu.xprize.bp_component.CBp_Data;
import cmu.xprize.bp_component.CBubble;
import android.graphics.Rect;
import android.graphics.RectF;
import cmu.xprize.bp_component.CBubbleStimulus;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.PerformanceLogItem;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_clickmask.CM_CONST.*;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

public class TBpComponent extends CBP_Component implements IBehaviorManager, ITutorObjectImpl, IDataSink, IEventSource, IPublisher, ITutorLogger {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager   mMediaManager;

    private CBubble         _touchedBubble;
    private CBubbleStimulus _bubbleStimulus;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    static final String TAG = "TBpComponent";



    public TBpComponent(Context context) {
        super(context);
    }

    public TBpComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TBpComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    //***********************************************************
    // Event Listener/Dispatcher - Start


    @Override
    public void addEventListener(String linkedView) {

        mListeners.add((IEventListener) mTutor.getViewByName(linkedView));
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }


    private void reset() {

        resetValid();
        resetState();
    }


    private void resetValid() {

        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_WRONG);
        retractFeature(TCONST.LAST_ATTEMPT);

    }


    private void resetState() {

        retractFeature(TCONST.SAY_STIMULUS);
        retractFeature(TCONST.SHOW_STIMULUS);
    }


    /**
     * Preprocess the data set
     *
     * @param data
     */
    @Override
    protected void updateDataSet(CBp_Data data) {

        // Let the compoenent process the new data set
        //
        super.updateDataSet(data);
        publishQuestionState(data);
    }


    /**
     * @param dataNameDescriptor
     */
    public void setDataSource(String dataNameDescriptor) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        if (dataNameDescriptor.startsWith("[file]bpop.num.mc_show_") ||
                dataNameDescriptor.startsWith("[file]bpop.num.rise_show_")
                ) {
            publishFeature(TCONST.BUBBLEPOP_MATH_EXPRESSION);
        }

        // We make the assumption that all are correct until proven wrong
        //
        publishFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        try {
            if (dataNameDescriptor.startsWith(TCONST.LOCAL_FILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.LOCAL_FILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.DOWNLOAD_RT_TUTOR + "/" + mTutor.getTutorName() + "/";

                String jsonData = JSON_Helper.cacheDataByName(dataPath + dataFile);
                loadJSON(new JSONObject(jsonData), mTutor.getScope());

            } else if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.SOURCEFILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS;
                dataPath += "/" +  TCONST.DATA_PATH + "/" + mMediaManager.getLanguageIANA_2(mTutor) + "/";

                String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

                //
                // set the total number of questions
                if(question_count == 0) {
                    mTutor.setTotalQuestions(gen_stimulusSet.length);
                }

                // preprocess the datasource e.g. populate instance arrays with general types
                //
                preProcessDataSource();

            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source - " + dataNameDescriptor + " for : " + name() + " : ", e, false);
        }
    }


    public void next() {

        // If wrong reset ALLCORRECT
        //
        if (mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {

            retractFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.next();

        if (dataExhausted()) {
            publishFeature(TCONST.FTR_EOD);
            Log.d("BPOP", "Data Exhausted ");
        }
    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }


    /**
     * Broadcast bubble exclusion and mask the screen during feedback
     *
     */
    public void maskBubble() {

        int[]   screenCoord = new int[2];
        PointF  centerPoint = _touchedBubble.getCenterPosition();

        getLocationOnScreen(screenCoord);

        PointF centerPt = new PointF(screenCoord[0] + centerPoint.x, screenCoord[1] + centerPoint.y);

        PointF center = Scontent.localToGlobal(centerPt);


        // Add an exclusion around the bubble the (incorrect) user tapped
        //
        Intent msg = new Intent(MASK_ADDEXCL);

        msg.putExtra(MASK_TYPE, EXCLUDE_CIRCLE);
        msg.putExtra(MASK_X, (int)center.x);
        msg.putExtra(MASK_Y, (int)center.y);
        msg.putExtra(MASK_R, (int)(_touchedBubble.getScaledWidth()/2.0 * 1.15));

        bManager.sendBroadcast(msg);


        // Set the mask transparency
        //
        msg = new Intent(MASK_SETALPHA);
        msg.putExtra(MASK_ALPHA, mask_alpha);

        bManager.sendBroadcast(msg);


        // Show the mask while the feedback is in progress
        //
        msg = new Intent(MASK_SHOWHIDE);
        msg.putExtra(MASK_SHOWHIDE, VISIBLE);

        bManager.sendBroadcast(msg);
    }

    public void maskStimulus() {
        RectF  boundRect = _bubbleStimulus.getRectBound();

        // Add an exclusion around the bubble the (incorrect) user tapped
        //
        Intent msg = new Intent(MASK_ADDEXCL);

        msg.putExtra(MASK_TYPE, EXCLUDE_SQUARE);
        msg.putExtra(MASK_BOTTOM, (int) boundRect.bottom);
        msg.putExtra(MASK_TOP, (int) boundRect.top);
        msg.putExtra(MASK_LEFT, (int) boundRect.left);
        msg.putExtra(MASK_RIGHT, (int) boundRect.right);

        bManager.sendBroadcast(msg);

        // Set the mask transparency
        //
        msg = new Intent(MASK_SETALPHA);
        msg.putExtra(MASK_ALPHA, mask_alpha);

        bManager.sendBroadcast(msg);


        // Show the mask while the feedback is in progress
        //
        msg = new Intent(MASK_SHOWHIDE);
        msg.putExtra(MASK_SHOWHIDE, VISIBLE);

        bManager.sendBroadcast(msg);


    }


    /**
     * Clear the feedback mask
     */
    public void clearMask() {

        Intent msg = new Intent(MASK_CLREXCL);
        bManager.sendBroadcast(msg);

        // Hide the mask
        //
        msg = new Intent(MASK_SHOWHIDE);
        msg.putExtra(MASK_SHOWHIDE, INVISIBLE);

        bManager.sendBroadcast(msg);
    }

    //**********************************************************
    //**********************************************************
    //*****************  Scripting Interface


    public void postEvent(String event) {

        switch (event) {

            case BP_CONST.PAUSE_ANIMATION:

                post(BP_CONST.PAUSE_ANIMATION);
                break;

            case BP_CONST.RESUME_ANIMATION:

                post(BP_CONST.RESUME_ANIMATION);
                break;

            case BP_CONST.SHOW_BUBBLE_MASK:

                maskBubble();
                break;

            case BP_CONST.HIDE_MASK:

                clearMask();
                break;

            case BP_CONST.SHOW_STIMULUS_MASK:

                maskStimulus();
                break;

            case BP_CONST.SHOW_SCORE:
                post(BP_CONST.SHOW_SCORE, new Integer(correct_Count));
                break;

            case BP_CONST.SHOW_STIMULUS:
                post(BP_CONST.SHOW_STIMULUS, _currData);
                break;

            case BP_CONST.SHOW_FEEDBACK:
                post(BP_CONST.SHOW_FEEDBACK, new Integer(correct_Count));
                break;

            case BP_CONST.SHOW_BUBBLES:
                post(BP_CONST.SHOW_BUBBLES);
                break;

            case BP_CONST.POP_BUBBLE:
                post(BP_CONST.POP_BUBBLE, _touchedBubble);
                break;

            case BP_CONST.WIGGLE_BUBBLE:
                post(BP_CONST.WIGGLE_BUBBLE, _touchedBubble);
                break;

            case BP_CONST.CLEAR_CONTENT:
                post(BP_CONST.CLEAR_CONTENT, _touchedBubble);
                break;
        }
    }


    public void enableTouchEvents() {
        super.enableTouchEvents();
    }

    // Scripting Interface  End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START

    public void setVolatileBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (volatileMap.containsKey(event)) {
                volatileMap.remove(event);
            }
        } else {
            volatileMap.put(event, behavior);
        }
    }


    public void setStickyBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (stickyMap.containsKey(event)) {
                stickyMap.remove(event);
            }
        } else {
            stickyMap.put(event, behavior);
        }
    }


    // Execute scirpt target if behavior is defined for this event
    //
    public boolean applyBehavior(String event) {

        boolean result = false;

        if (volatileMap.containsKey(event)) {

            RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehavior,type:volatile,behavior:" + event);
            applyBehaviorNode(volatileMap.get(event));

            volatileMap.remove(event);

            result = true;

        } else if (stickyMap.containsKey(event)) {

            RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehavior,type:sticky,behavior:" + event);
            applyBehaviorNode(stickyMap.get(event));

            result = true;
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
        return "BubblePop_Component";
    }


    // IEventSource Interface END
    //************************************************************************
    //************************************************************************



    //***********************************************************
    // ITutorLogger - Start

    private void extractHashContents(StringBuilder builder, HashMap map) {

        Iterator<?> tObjects = map.entrySet().iterator();

        while(tObjects.hasNext() ) {

            builder.append(',');

            Map.Entry entry = (Map.Entry) tObjects.next();

            String key   = entry.getKey().toString();
            String value = "#" + entry.getValue().toString();

            builder.append(key);
            builder.append(value);
        }
    }

    private void extractFeatureContents(StringBuilder builder, HashMap map) {

        StringBuilder featureset = new StringBuilder();

        Iterator<?> tObjects = map.entrySet().iterator();

        // Scan to build a list of active features
        //
        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean value = (Boolean) entry.getValue();

            if(value) {
                featureset.append(entry.getKey().toString() + ";");
            }
        }

        // If there are active features then trim the last ',' and add the
        // comma delimited list as the "$features" object.
        //
        if(featureset.length() != 0) {
            featureset.deleteCharAt(featureset.length()-1);

            builder.append(",$features#" + featureset.toString());
        }
    }

    @Override
    public void logState(String logData) {

        StringBuilder builder = new StringBuilder();

        extractHashContents(builder, _StringVar);
        extractHashContents(builder, _IntegerVar);
        extractFeatureContents(builder, _FeatureMap);

        RoboTutor.logManager.postTutorState(TUTOR_STATE_MSG, "target#bubble_pop," + logData + builder.toString());
    }

    // ITutorLogger - End
    //***********************************************************



    //************************************************************************
    //************************************************************************
    // IPublish component state data - START

    /**
     * Publish the Stimulus value as Scope variables for script access
     */
    @Override
    protected void publishState(CBubble bubble, CBubbleStimulus bubbleStimulus) {

        _touchedBubble = bubble;
        _bubbleStimulus = bubbleStimulus;

        TScope scope  = mTutor.getScope();
        String answer = bubble.getStimulus();

        // Ensure letters are lowercase for mp3 matching
        //
        if(answer.length() == 1) {
            answer = answer.toLowerCase();
        }

        if(answer.contains("\n")) {

            int index = answer.indexOf("\n");
            String firstNum = answer.substring(0, index);
            String operation = answer.substring(index + 1, index + 2);
            String secondNum = answer.substring(index+2);

            if(operation.equals("+")) {
                operation = "plus";
            }
            else {
                operation = "minus";
            }

            publishValue(BP_CONST.ANSWER_VAR, firstNum);
            publishValue(BP_CONST.ANSWER_VAR_TWO, operation);
            publishValue(BP_CONST.ANSWER_VAR_THREE, secondNum);
        }

        else {
            publishValue(BP_CONST.ANSWER_VAR, answer);
            publishValue(BP_CONST.ANSWER_VAR_TWO, "TRASH");
            publishValue(BP_CONST.ANSWER_VAR_THREE, "TRASH");
        }

        resetValid();

        if (bubble.isCorrect()) {
            publishFeature(TCONST.GENERIC_RIGHT);
            Log.d("BPOP", "Correct" );

            correct_Count++;

        } else {
            publishFeature(TCONST.GENERIC_WRONG);
            Log.d("BPOP", "Wrong" );
            attempt_count--;

            if(attempt_count <= 0) {
                publishFeature(TCONST.LAST_ATTEMPT);

                Log.d("BPOP", "Publish Last Attempt" );
            }
        }

        Log.d("BPOP", "Publish correct Count: " + correct_Count);
        Log.d("BPOP", "Publish attempt Count: " + attempt_count);

        trackAndLogPerformance(bubble);

    }

    /**
     * This method is to separate correctness-checking which informs game behavior from
     * tracking performance for Activity Selection and for Logging.
     */
    private void trackAndLogPerformance(CBubble bubble) {
        // XXX_LL Begin changes

        if (bubble.isCorrect()) {
            mTutor.countCorrect();
        } else {
            mTutor.countIncorrect();
        }

        PerformanceLogItem event = new PerformanceLogItem();

        String problemName = "BPOP_" + _currData.answer + "_";
        for(int i = 0; i < _currData.response_set.length-1; i++) {
            problemName += _currData.response_set[i] + "-";
        }
        problemName += _currData.response_set[_currData.response_set.length-1] + "";

        String promptType = "";
        if (_currData.question_say) promptType += "say";
        if (_currData.question_show) promptType += ((promptType.length() > 0) ? "+" : "") + "show";

        event.setUserId(RoboTutor.STUDENT_ID);
        event.setSessionId(RoboTutor.SESSION_ID);
        event.setGameId(mTutor.getUuid().toString());
        event.setLanguage(CTutorEngine.language);
        event.setTutorName(mTutor.getTutorName());
        event.setProblemName(problemName);
        event.setProblemNumber(question_Index);
        event.setTotalSubsteps(1);
        event.setSubstepNumber(1);
        event.setSubstepProblem(1);
        event.setAttemptNumber(1);
        event.setExpectedAnswer(_currData.answer);

        StringBuilder distractors = new StringBuilder();
        for(int i = 0; i < _currData.response_set.length; i++) {
            if(!_currData.response_set[i].equals(_currData.answer))
                distractors.append(_currData.response_set[i]+"+");
        }
        event.setDistractors(distractors.toString().substring(0, distractors.toString().length() - 1));

        event.setUserResponse(bubble.getStimulus());
        event.setCorrectness(bubble.isCorrect() ? TCONST.LOG_CORRECT : TCONST.LOG_INCORRECT);
        event.setScaffolding(null);
        event.setPromptType(promptType);
        event.setFeedbackType(null);

        event.setTimestamp(System.currentTimeMillis());

        RoboTutor.logManager.postEvent_I(TCONST.PERFORMANCE_TAG, event.toString());
    }

    protected void publishQuestionState(CBp_Data data) {

        TScope scope = mTutor.getScope();

        resetState();

        String correctVal = data.stimulus;
        String comp_pos = data.comp_pos;
        String comp_len = data.comp_len;

        // Ensure letters are lowercase for mp3 matching
        //
        correctVal = correctVal.toLowerCase();
        if(comp_pos != null && comp_len != null) {
            if(comp_pos == "Starts") {
                comp_pos = "kuanza";
            }
            else {
                comp_pos = "mwishoni";
            }
            if(comp_len== "With") {
                comp_len = "na";
            }
            else {
                comp_len = "kama";
            }
            publishValue(BP_CONST.QUEST_VAR, comp_pos);
            publishValue(BP_CONST.QUEST_VAR_TWO, comp_len);
            publishValue(BP_CONST.QUEST_VAR_THREE, correctVal);
        }

        else if(correctVal.contains("\n")) {

            int index = correctVal.indexOf("\n");
            String firstNum = correctVal.substring(0, index);
            String operation = correctVal.substring(index + 1, index + 2);
            String secondNum = correctVal.substring(index+2);

            if(operation.equals("+")) {
                operation = "plus";
            }
            else {
                operation = "minus";
            }

            publishValue(BP_CONST.QUEST_VAR, firstNum);
            publishValue(BP_CONST.QUEST_VAR_TWO, operation);
            publishValue(BP_CONST.QUEST_VAR_THREE, secondNum);

        }

        else {
            publishValue(BP_CONST.QUEST_VAR, correctVal);
            publishValue(BP_CONST.QUEST_VAR_TWO, "TRASH");
            publishValue(BP_CONST.QUEST_VAR_THREE, "TRASH");
        }

        if (data.question_say) {
            publishFeature(TCONST.SAY_STIMULUS);
        }

        if (data.question_show) {
            publishFeature(TCONST.SHOW_STIMULUS);
        }
    }
    
    @Override
    public void publishState() {
    }

    @Override
    public void publishValue(String varName, String value) {

        _StringVar.put(varName,value);

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));

    }

    @Override
    public void publishValue(String varName, int value) {

        _IntegerVar.put(varName,value);

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TInteger(value));

    }

    @Override
    public void publishFeatureSet(String featureSet) {

        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(featureSet.split(","));

        for(String feature : featArray) {

            _FeatureMap.put(feature, true);
            publishFeature(feature);
        }
    }

    @Override
    public void retractFeatureSet(String featureSet) {

        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(featureSet.split(","));

        for(String feature : featArray) {

            _FeatureMap.put(feature, false);
            retractFeature(feature);
        }
    }

    @Override
    public void publishFeature(String feature) {

        _FeatureMap.put(feature, true);
        mTutor.addFeature(feature);
    }

    /**
     * Note that we may retract features before they're published to add them to the
     * FeatureSet that should be pushed/popped when using pushDataSource
     * e.g. we want EOD to track even if it has never been set
     *
     * @param feature
     */
    @Override
    public void retractFeature(String feature) {

        _FeatureMap.put(feature, false);
        mTutor.delFeature(feature);
    }

    /**
     *
     * @param featureMap
     */
    @Override
    public void publishFeatureMap(HashMap featureMap) {

        Iterator<?> tObjects = featureMap.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean active = (Boolean)entry.getValue();

            if(active) {
                String feature = (String)entry.getKey();

                mTutor.addFeature(feature);
            }
        }
    }

    /**
     *
     * @param featureMap
     */
    @Override
    public void retractFeatureMap(HashMap featureMap) {

        Iterator<?> tObjects = featureMap.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean active = (Boolean)entry.getValue();

            if(active) {
                String feature = (String)entry.getKey();

                mTutor.delFeature(feature);
            }
        }
    }

    // IPublish component state data - EBD
    //************************************************************************
    //************************************************************************
    
    
    
    //**********************************************************
    //**********************************************************
    //*****************  ITutorObjectImpl Implementation

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    @Override
    public void onCreate() {

        // Do deferred listeners configuration - this cannot be done until after the tutor is instantiated
        //
        if(!mListenerConfigured) {
            for (String linkedView : mLinkedViews) {
                addEventListener(linkedView);
            }
            mListenerConfigured = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition) {

    }

    @Override
    public void setAlpha(Float alpha) {

    }


    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        // Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, (IScope2) scope);

        // set the total number of questions
        mTutor.setTotalQuestions(question_count);
    }
}
