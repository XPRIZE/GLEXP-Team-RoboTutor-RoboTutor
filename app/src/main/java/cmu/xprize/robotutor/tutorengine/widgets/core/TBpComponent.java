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
import android.content.res.Resources;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import cmu.xprize.robotutor.tutorengine.ITutorObject;
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

public class TBpComponent extends CBP_Component implements IBehaviorManager, ITutorObject, IDataSink, IEventSource, IPublisher, ITutorLogger {

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

    private String mProblemType = "";


    public TBpComponent(Context context) {
        super(context);
    }

    public TBpComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TBpComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Helper function that converts 3 digit number to list of digits
    private int[] getListDigits(int num) {
        int hundredsDigit = 0;  int tensDigit = 0;
        if(num >= 100) {
            hundredsDigit = (num / 100) * 100;
        }
        num = num % 100;
        return (new int[]{hundredsDigit, num});
    }

    //Helper function that splits expression into operand1, key, and operand2
    private String[] splitExpression(String exp, String key) {
        int op_index = exp.indexOf(key);
        String operand1 = exp.substring(0, op_index);

        if(key.equals("\n")) {
            op_index += 1;
        }

        String operation = exp.substring(op_index, op_index+1);
        String operand2 = exp.substring(op_index+1);

        if (operation.equals("+") || operation.equals("plus")) {
            key = "plus";
        }
        else {
            key = "minus";
        }

        return (new String[]{operand1, key, operand2});
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
        mProblemType = problem_type;
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

            } else if (dataNameDescriptor.startsWith(TCONST.DEBUG_FILE_PREFIX)) { // this must be reproduced in every robo_debuggable component

                String dataFile = dataNameDescriptor.substring(TCONST.DEBUG_FILE_PREFIX.length());

                String dataPath = TCONST.DEBUG_RT_PATH + "/";
                String jsonData = JSON_Helper.cacheDataByName(dataPath + dataFile);
                loadJSON(new JSONObject(jsonData), mTutor.getScope());

                // these two code statements below are the same as in the "startsWith SOURCEFILE" condition
                // set the total number of questions
                if(question_count == 0) {
                    mTutor.setTotalQuestions(gen_stimulusSet.length);
                }

                // preprocess the datasource e.g. populate instance arrays with general types
                //
                preProcessDataSource();

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


    /**
     * Chooses one track randomly out of a list kept in string.xml
     */
    public void setSoundTrack() {

        Resources res = getResources();
        String[] sound_tracks = res.getStringArray(cmu.xprize.bp_component.R.array.sound_tracks);
        Random rand = new Random();
        String sound_track = sound_tracks[rand.nextInt(sound_tracks.length)];

        Log.i("BPOP", "Chosen sound track: " + sound_track);

        publishValue(BP_CONST.SOUND_TRACK, sound_track);
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

        // Add an exclusion around stimulus
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

        if(mProblemType.equals("EXPRESSION_N2E")) {
            publishFeature(BP_CONST.FTR_N2E);

            String[] expTerms = splitExpression(answer, "\n");
            int operand1 = Integer.parseInt(expTerms[0]); int operand2 = Integer.parseInt(expTerms[2]);
            String operation = expTerms[1];
            int[] operand1Digits = getListDigits(operand1); int[] operand2Digits = getListDigits(operand2);

            //Publish features and values for each digit of first operand so that audios for each digit can be played separately
            if(operand1Digits[0] >= 100) {
                publishFeature(BP_CONST.FTR_ANS_STIM_ONE_HUNDREDS); // ND_SCAFFOLD NEXT(1) make sure to publish the right features (e.g.. don't play hundred when it's a 2digit)
                publishValue(BP_CONST.ANS_VAR_STIM_ONE_HUNDREDS, operand1Digits[0]); // ND_SCAFFOLD √√√ mimic this
            }
            else {
                removeFeature(BP_CONST.FTR_ANS_STIM_ONE_HUNDREDS);
            }
            if(operand1Digits[1] >= 1 || operand1Digits[0] == 0) {
                publishFeature(BP_CONST.FTR_ANS_STIM_ONE_TENS);
                publishValue(BP_CONST.ANS_VAR_STIM_ONE_TENS, operand1Digits[1]);
            }
            else {
                removeFeature(BP_CONST.FTR_ANS_STIM_ONE_TENS);
            }

            publishValue(BP_CONST.ANS_VAR_OPERAND, operation);

            //Publish features and values for each digit of second operand so that audios for each digit can be played separately
            if(operand2Digits[0] >= 100) {
                publishFeature(BP_CONST.FTR_ANS_STIM_TWO_HUNDREDS);
                publishValue(BP_CONST.ANS_VAR_STIM_TWO_HUNDREDS, operand2Digits[0]);
            }
            else {
                removeFeature(BP_CONST.FTR_ANS_STIM_TWO_HUNDREDS);
            }
            if(operand2Digits[1] >= 1 || operand2Digits[0] == 0 ) {
                publishFeature(BP_CONST.FTR_ANS_STIM_TWO_TENS);
                publishValue(BP_CONST.ANS_VAR_STIM_TWO_TENS, operand2Digits[1]);
            }
            else {
                removeFeature(BP_CONST.FTR_ANS_STIM_TWO_TENS);
            }
        }
        else  {
            if(mProblemType.startsWith("EXPRESSION_E2N")) {
                publishFeature(BP_CONST.FTR_E2N);
            }

            if(answer != null && answer.matches("[-+]?\\d*\\.?\\d+")) {
                int ans = Integer.parseInt(answer);
                int[] ansDigits = getListDigits(ans);

                if(ansDigits[0] >= 100) {
                    publishFeature(BP_CONST.FTR_ANS_HUNDREDS);
                    publishValue(BP_CONST.ANS_VAR_HUNDREDS, ansDigits[0]);
                }
                else {
                    removeFeature(BP_CONST.FTR_ANS_HUNDREDS);
                }
                if(ansDigits[1] >= 1 || ansDigits[0] == 0) {
                    publishFeature(BP_CONST.FTR_ANS_TENS);
                    publishValue(BP_CONST.ANS_VAR_TENS, ansDigits[1]);
                }
                else {
                    removeFeature(BP_CONST.FTR_ANS_TENS);
                }
            }
            else {
                publishValue(BP_CONST.ANS_VAR, answer);

            }
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
        Log.wtf("WARRIOR_MAN", mTutor.getTutorId());
        event.setTutorId(mTutor.getTutorId());
        event.setPromotionMode(RoboTutor.getPromotionMode(event.getMatrixName()));
        event.setProblemName(problemName);
        event.setProblemNumber(logQuestionIndex);
        event.setTotalProblemsCount(mTutor.getTotalQuestions());
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

        RoboTutor.perfLogManager.postPerformanceLog(event);
    }

    protected void publishQuestionState(CBp_Data data) {

        TScope scope = mTutor.getScope();

        resetState();
        String correctVal = data.stimulus;

        // Ensure letters are lowercase for mp3 matching
        //
        correctVal = correctVal.toLowerCase();

        //Cases over the problem type to publish diffferent features and values
        if(mProblemType.startsWith("EXPRESSION_E2N")) {

            publishFeature("FTR_E2N");

            String key = "\n";
            if(mProblemType.equals("EXPRESSION_E2N_ADD")) {
                key = "+";
            }
            else if(mProblemType.equals("EXPRESSION_E2N_SUB")) {
                key = "-";
            }

            String[] expTerms = splitExpression(correctVal, key);
            int operand1 = Integer.parseInt(expTerms[0]); int operand2 = Integer.parseInt(expTerms[2]);

            String operation = expTerms[1];
            int[] operand1Digits = getListDigits(operand1); int[] operand2Digits = getListDigits(operand2);

            //Publish features and values for each digit of first operand so that audios can be played separately
            if(operand1Digits[0] >= 100) {
                publishFeature(BP_CONST.FTR_QUEST_STIM_ONE_HUNDREDS);
                publishValue(BP_CONST.QUEST_VAR_STIM_ONE_HUNDREDS, operand1Digits[0]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_STIM_ONE_HUNDREDS);
            }
            if(operand1Digits[1] >= 1 || operand1Digits[0] == 0) {
                publishFeature(BP_CONST.FTR_QUEST_STIM_ONE_TENS);
                publishValue(BP_CONST.QUEST_VAR_STIM_ONE_TENS, operand1Digits[1]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_STIM_ONE_TENS);
            }

            publishValue(BP_CONST.QUEST_VAR_OPERAND, operation);

            //Publish features and values for each digit of second operand so that audios can be played separately
            if(operand2Digits[0] >= 100) {
                publishFeature(BP_CONST.FTR_QUEST_STIM_TWO_HUNDREDS);
                publishValue(BP_CONST.QUEST_VAR_STIM_TWO_HUNDREDS, operand2Digits[0]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_STIM_TWO_HUNDREDS);
            }
            if(operand2Digits[1] >= 1 || operand2Digits[0] == 0) {
                publishFeature(BP_CONST.FTR_QUEST_STIM_TWO_TENS);
                publishValue(BP_CONST.QUEST_VAR_STIM_TWO_TENS, operand2Digits[1]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_STIM_TWO_TENS);
            }
        }
        else if(mProblemType.equals("EXPRESSION_N2E")) {
            publishFeature(BP_CONST.FTR_N2E);

            int ans = Integer.parseInt(correctVal);
            int[] ansDigits = getListDigits(ans);

            if(ansDigits[0] >= 100) {
                publishFeature(BP_CONST.FTR_QUEST_HUNDREDS);
                publishValue(BP_CONST.QUEST_VAR_HUNDREDS, ansDigits[0]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_HUNDREDS);
            }
            if(ansDigits[1] >= 1 || ansDigits[0] == 0) {
                publishFeature(BP_CONST.FTR_QUEST_TENS);
                publishValue(BP_CONST.QUEST_VAR_TENS, ansDigits[1]);
            }
            else {
                removeFeature(BP_CONST.FTR_QUEST_TENS);
            }
        }
        else {
            if(mProblemType.equals("MIS_NUM")) {
                correctVal = "What number belongs here";
            }
            if(mProblemType.equals("GL_GT")) {
                correctVal = "Touch the largest number";
            }
            if(mProblemType.equals("GL_LT")) {
                correctVal = "Touch the smallest number";
            }
            if(correctVal != null && correctVal.matches("[-+]?\\d*\\.?\\d+")) {
                int ans = Integer.parseInt(correctVal);
                int[] ansDigits = getListDigits(ans);

                if(ansDigits[0] >= 100) {
                    publishFeature(BP_CONST.FTR_QUEST_HUNDREDS);
                    publishValue(BP_CONST.QUEST_VAR_HUNDREDS, ansDigits[0]);
                }
                else {
                    removeFeature(BP_CONST.FTR_QUEST_HUNDREDS);
                }
                if(ansDigits[1] >= 1 || ansDigits[0] == 0) {
                    publishFeature(BP_CONST.FTR_QUEST_TENS);
                    publishValue(BP_CONST.QUEST_VAR_TENS, ansDigits[1]);
                }
                else {
                    removeFeature(BP_CONST.FTR_QUEST_TENS);
                }
            }
            else {
                if(mProblemType.equals("WORD_STARTS_WITH")) {
                    publishFeature(BP_CONST.FTR_WRD_STARTS_WITH);
                    publishValue(BP_CONST.QUEST_VAR, correctVal);
                }
                if(mProblemType.equals("WORD_ENDS_WITH")) {
                    publishFeature(BP_CONST.FTR_WRD_ENDS_WITH);
                    publishValue(BP_CONST.QUEST_VAR, correctVal);
                }
                else {
                    publishValue(BP_CONST.QUEST_VAR, correctVal);
                }
            }

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

    public void removeFeature(String feature) {
        _FeatureMap.remove(feature);
        mTutor.delFeature(feature);
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


    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        // Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, (IScope2) scope);

        // set the total number of questions
        mTutor.setTotalQuestions(question_count);
    }
}
