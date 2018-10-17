package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.comp_counting2.CCountX_Component;
import cmu.xprize.comp_counting2.COUNTX_CONST;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_logging.PerformanceLogItem;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


import cmu.xprize.robotutor.R;
import cmu.xprize.comp_counting2.CGlyphController_Simple;
import cmu.xprize.comp_counting2.CGlyphInputContainer_Simple;
import cmu.xprize.comp_counting2.IGlyphController_Simple;
import cmu.xprize.comp_counting2.IWritingComponent_Simple;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

/**
 * Created by kevindeland on 10/20/17.
 */

public class TCountXComponent extends CCountX_Component implements ITutorObject, IDataSink, IPublisher, ITutorLogger, IBehaviorManager, IEventSource {

    private Context mContext;
    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager mMediaManager;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    OnCharacterRecognizedListener _recognizedListener;


    CGlyphController_Simple _controller_1;
    CGlyphInputContainer_Simple _inputContainer_1;


    // Views to help you
    TextView _resultDisplay_1;
    TextView _resultDisplay_2;
    Spinner _boostSpinner;
    String[] BOOST_VALS = {GCONST.NO_BOOST, GCONST.BOOST_ALPHA, GCONST.BOOST_DIGIT, GCONST.FORCE_DIGIT};

    static final String TAG = "TCountComponent";

    public TCountXComponent(Context context) {
        super(context);
    }

    public TCountXComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TCountXComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //**********************************************************
    //**********************************************************
    //*****************  ITutorObject Implementation

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        mContext = context;
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }

    @Override
    public void onCreate() {
        _recognizer = CRecognizerPlus.getInstance();
        _glyphSet   = _recognizer.getGlyphPrototypes(); //new GlyphSet(TCONST.ALPHABET);

        _recognizer.setClassBoost(GCONST.FORCE_DIGIT); // reset boost which may have been set from Asm
        _controller_1 = findViewById(R.id.glyph_controller_1);
        _inputContainer_1 = findViewById(R.id.drawn_box_1);
        _resultDisplay_1 = findViewById(R.id.result_display_1);

        _inputContainer_1.setVisibility(View.INVISIBLE);
        _resultDisplay_1.setVisibility(View.INVISIBLE);

        _controller_1.setInputContainer(_inputContainer_1);
        _controller_1.setIsLast(true);
        _controller_1.showBaseLine(false);
        _recognizedListener = new OnCharacterRecognizedListener(_controller_1, _resultDisplay_1,0);
        _controller_1.setWritingController(_recognizedListener);


    }

    @Override
    public void initRecognizer(int writePosition){
        _inputContainer_1.setVisibility(View.VISIBLE);

    }


    public void hideRecognizer(){
        _inputContainer_1.setVisibility(View.INVISIBLE);
    }

    @Override
    public void changeWritePosition(int writePosition){
        _recognizedListener = new OnCharacterRecognizedListener(_controller_1, _resultDisplay_1,writePosition);
        _controller_1.setWritingController(_recognizedListener);
    }


    private class OnCharacterRecognizedListener implements IWritingComponent_Simple {

        CGlyphController_Simple _controller;
        TextView _resultDisplay;
        int _writePosition;

        OnCharacterRecognizedListener(CGlyphController_Simple controller, TextView display,int writePosition) {

            this._controller = controller;
            this._resultDisplay = display;
            this._writePosition = writePosition;
        }

        /**
         * This is called when the recognizer is finished
         * @param child
         * @param _ltkPlusCandidates
         * @return
         */
        @Override
        public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {

            CRecResult candidate = _ltkPlusCandidates[0];

            Log.i(TAG, "the answer is... " + candidate.getRecChar() + "!!!");

            _resultDisplay.setText(candidate.getRecChar());


            updateWriteNumber(_writePosition,Integer.parseInt(candidate.getRecChar()));


            _controller.eraseGlyph();

            return false;
        }
    }




    @Override
    public void onDestroy() {
        stopQueue();
        trackAndLogPerformance("ENDPROBLEM","ENDPROBLEM","ENDPROBLEM","CORRECT");

    }

    @Override
    public void setVisibility(String visible) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {

    }

    @Override
    public void setTutor(CTutor tutor) {
        mTutor = tutor;
        mSceneObject.setTutor(tutor);

        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {

    }

    @Override
    public void setLogManager(ILogManager logManager) {

    }

    private void reset() {
        // TODO retract Features

    }

    public void next() {

        super.next();

        if (dataExhausted()) {
            publishFeature(TCONST.FTR_EOD);
            Log.d(TCONST.COUNTING_DEBUG_LOG, "Data Exhausted");
        }
    }

    @Override
    protected void trackAndLogPerformance(String expected,String actual,String movement,String cor) {

        if (expected.equals(actual)) {
            mTutor.countCorrect();
        } else {
            mTutor.countIncorrect();
        }

        String tutorName = mTutor.getTutorName();
        PerformanceLogItem event = new PerformanceLogItem();

        event.setUserId(RoboTutor.STUDENT_ID);
        event.setSessionId(RoboTutor.SESSION_ID);
        event.setGameId(mTutor.getUuid().toString()); // a new tutor is generated for each game, so this will be unique
        event.setLanguage(CTutorEngine.language);
        event.setTutorName(tutorName);
        Log.wtf("WARRIOR_MAN", mTutor.getTutorId());
        event.setTutorId(mTutor.getTutorId());
        event.setPromotionMode(RoboTutor.getPromotionMode(event.getMatrixName()));
        event.setLevelName(level);
        event.setTaskName(task);
        event.setProblemName(Integer.toString(countTarget));
        if(dataSource != null) {
            event.setTotalProblemsCount(dataSource.length);
        }
        event.setProblemNumber(_dataIndex);
        event.setSubstepNumber(-1);
        event.setAttemptNumber(-1);
        event.setExpectedAnswer(expected);
        event.setUserResponse(actual);
        event.setCorrectness(cor);
        event.setScaffolding(movement);

        event.setTimestamp(System.currentTimeMillis());

        RoboTutor.perfLogManager.postPerformanceLog(event);
    }


    /**
     * @param dataNameDescriptor
     */
    public void setDataSource(String dataNameDescriptor) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        Log.d(TCONST.COUNTING_DEBUG_LOG, "setDataSource");

        // We make the assumption that all are correct until proven wrong
        //
        publishFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        //
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

                // preprocess the datasource e.g. populate instance arrays with general types
                //

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

    /** Functionality methods...
     *
     */

    @Override
    public void playChime() {

        TScope scope = mTutor.getScope();

        // select which chime to play, via protected integer
        int scaledCount;
        if(mode != "placevalue"){
            switch(tenPower) {
                case 10:
                    scaledCount = currentCount / 10;
                    break;
                case 100:
                    scaledCount = currentCount / 100;
                    break;
                default:
                    scaledCount = currentCount;
            }
        } else {
            scaledCount = allTaps;
        }

        int chimeIndex = scaledCount == 0 ? 0 : (scaledCount - 1) % 10;
        String currentChime = COUNTX_CONST.CHIMES[1][chimeIndex];
        String octaveChime = COUNTX_CONST.CHIMES[2][chimeIndex];

        Log.d("PlayChime", currentChime);
        scope.addUpdateVar("CountChime", new TString(currentChime));
        scope.addUpdateVar("OctaveChime", new TString(octaveChime));
        if (mode == "placevalue"){
            scope.addUpdateVar("CurrentCount", new TString(String.valueOf(currentValue)));
        } else {
            scope.addUpdateVar("CurrentCount", new TString(String.valueOf(currentCount)));
        }



        postEvent(COUNTX_CONST.PLAY_COUNT);
    }

    @Override
    public void playCount(int count){
        TScope scope = mTutor.getScope();
//        scope.addUpdateVar("CurrentCount", new TString(String.valueOf(count)));
//        postEvent(COUNTX_CONST.PLAY_CHIME);
        if (count<=100||count%100==0){
            scope.addUpdateVar("CurrentCount", new TString(String.valueOf(count)));
            postEvent(COUNTX_CONST.PLAY_CHIME);
        } else {
            scope.addUpdateVar("CurrentCountt", new TString(String.valueOf((int)(count-count%100))));
            scope.addUpdateVar("CurrentCount",new TString(String.valueOf((int)(count%100))));
            postEvent(COUNTX_CONST.PLAY_CHIME_PLUS);
        }

    }

    @Override
    public void donePlaying(){
        applyBehavior(COUNTX_CONST.DONE_MOVING_TO_TEN_FRAME);
    }


    @Override
    public void playTwoAddition(){
        TScope scope = mTutor.getScope();
        if(targetNumbers[0] == 0){
            scope.addUpdateVar("first", new TString(String.valueOf((int)(targetNumbers[1]*10))));
            scope.addUpdateVar("second",new TString(String.valueOf((int)(targetNumbers[2]))));
        } else if (targetNumbers[1] == 0){
            scope.addUpdateVar("first", new TString(String.valueOf((int)(targetNumbers[0]*100))));
            scope.addUpdateVar("second",new TString(String.valueOf((int)(targetNumbers[2]))));
        } else{
            scope.addUpdateVar("first", new TString(String.valueOf((int)(targetNumbers[0]*100))));
            scope.addUpdateVar("second",new TString(String.valueOf((int)(targetNumbers[1]*10))));
        }
        postEvent(COUNTX_CONST.PLAY_TWO_ADDITION);
        postEvent(COUNTX_CONST.DONE_MOVING_TO_TEN_FRAME,8000);

    }

    @Override
    public void playThreeAddition(){
        TScope scope = mTutor.getScope();
        scope.addUpdateVar("first", new TString(String.valueOf((int)(targetNumbers[0]*100))));
        scope.addUpdateVar("second", new TString(String.valueOf((int)(targetNumbers[1]*10))));
        scope.addUpdateVar("third",new TString(String.valueOf((int)(targetNumbers[2]))));
        scope.addUpdateVar("result",new TString(String.valueOf((int)(countTarget))));
        postEvent(COUNTX_CONST.PLAY_THREE_ADDITION);


    }

    @Override
    public void playAudio(String filename){
        TScope scope = mTutor.getScope();
        scope.addUpdateVar("AudioName", new TString(filename));
        postEvent(COUNTX_CONST.PLAY_AUDIO);
    }


    @Override
    public void postFinalCount() {

        TScope scope = mTutor.getScope();

        scope.addUpdateVar("FinalCount", new TString(String.valueOf(countTarget)));

    }

    @Override
    public void playFinalCount(){
        if(difficulty!=1 || Arrays.equals(write_numbers, targetNumbers)){
            int count = countTarget;
            TScope scope = mTutor.getScope();
//        scope.addUpdateVar("CurrentCount", new TString(String.valueOf(count)));
//        postEvent(COUNTX_CONST.PLAY_CHIME);
            if (count<=100||count%100==0){
                scope.addUpdateVar("CurrentCount", new TString(String.valueOf(count)));
                postEvent(COUNTX_CONST.PLAY_CHIME);
            } else {
                scope.addUpdateVar("CurrentCountt", new TString(String.valueOf((int)(count-count%100))));
                scope.addUpdateVar("CurrentCount",new TString(String.valueOf((int)(count%100))));
                postEvent(COUNTX_CONST.PLAY_CHIME_PLUS);
            }

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            applyBehavior(COUNTX_CONST.DONE_MOVING_TO_TEN_FRAME);

                        }
                    },
                    3500
            );

        }




    }


    @Override
    public void displayWrittingIns(){
        postEvent(COUNTX_CONST.WRITTING_INS);
    }








    /**
     * IPublisher methods...
     *
     */
    @Override
    public void publishState() {

    }

    @Override
    public void publishValue(String varName, String value) {

        _StringVar.put(varName, value);

        // update the response variable "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));
    }

    @Override
    public void publishValue(String varName, int value) {

        _IntegerVar.put(varName, value);

        // update the response variable "<ComponentName>.<varName>"
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

    @Override
    public void retractFeature(String feature) {
        _FeatureMap.put(feature, false);
        mTutor.delFeature(feature);
    }

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

    public void loadJSON(JSONObject jsonObj, IScope scope) {
        super.loadJSON(jsonObj, scope);
    }

    @Override
    public void logState(String logData) {

        StringBuilder builder = new StringBuilder();

        extractHashContents(builder, _StringVar);
        extractHashContents(builder, _IntegerVar);
        extractFeatureContents(builder, _FeatureMap);


        RoboTutor.logManager.postTutorState(TCONST.TUTOR_STATE_MSG, "target#counting," + logData);
    }

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
            int i=0;
        }
    }


    // Execute scirpt target if behavior is defined for this event
    //
    @Override
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

    @Override
    public String getEventSourceName() {
        return name();
    }

    @Override
    public String getEventSourceType() {
        return "Counting_Component";
    }





    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************

}
