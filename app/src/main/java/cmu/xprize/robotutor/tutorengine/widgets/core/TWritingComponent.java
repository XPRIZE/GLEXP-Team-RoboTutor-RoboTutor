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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_writing.CWr_Data;
import cmu.xprize.comp_writing.CWritingComponent;
import cmu.xprize.comp_writing.WR_CONST;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.util.IEventSource;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.comp_logging.ILogManager;

import cmu.xprize.robotutor.R;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.EMPTY;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

public class TWritingComponent extends CWritingComponent implements IBehaviorManager, ITutorSceneImpl, IDataSink, IEventSource, IPublisher, ITutorLogger {

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;

    public List<IEventListener>     mListeners          = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews;
    protected boolean               mListenerConfigured = false;
    private int[]                   _screenCoord        = new int[2];

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap = new HashMap<>();

    private int                     _wrong   = 0;
    private int                     _correct = 0;

    private ArrayList<CDataSourceImg> _dataStack  = new ArrayList<>();

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    private static final String  TAG = "TWritingComponent";


    public TWritingComponent(Context context) {
        super(context);
    }

    public TWritingComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TWritingComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
    }


    @Override
    public void onCreate() {

        // Obtain the prototype glyphs from the singleton recognizer
        //
        _recognizer = CRecognizerPlus.getInstance();
        _glyphSet   = _recognizer.getGlyphPrototypes(); //new GlyphSet(TCONST.ALPHABET);

        _recognizer.setClassBoost(GCONST.NO_BOOST); // reset boost which may have been set from Asm

        mRecognizedScroll = (CLinkedScrollView) findViewById(R.id.Sstimulus);
        mRecogList        = (LinearLayout) findViewById(R.id.SstimulusList);

        mDrawnScroll = (CLinkedScrollView) findViewById(R.id.SfingerWriter);
        mGlyphList   = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
        mGlyphList.setClipChildren(false);

        mReplayButton = (ImageButton) findViewById(R.id.Sreplay);

// TODO: DEBUG only
//        mRecogList.setOnTouchListener(new RecogTouchListener());
//        mGlyphList.setOnTouchListener(new drawnTouchListener());

        mRecognizedScroll.setLinkedScroll(mDrawnScroll);
        mDrawnScroll.setLinkedScroll(mRecognizedScroll);

        // Iniitalize the static behaviors
        //
        setStickyBehavior(TCONST.NEXT_NODE, TCONST.NEXT_NODE);
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {
    }

    // Event Listener/Dispatcher - End
    //***********************************************************


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

        RoboTutor.logManager.postTutorState(TUTOR_STATE_MSG, "target#word_copy," + logData + builder.toString());
    }

    // ITutorLogger - End
    //***********************************************************



    public class replayClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Log.v(QGRAPH_MSG, "event.click: " + " replay");

            mReplayButton.setOnClickListener(null);

            applyBehavior(WR_CONST.ON_REPLAY_COMMAND);

            mReplayButton.setOnClickListener(new replayClickListener());
        }
    }


    public void enableReplayButton(Boolean enable) {

        mReplayButton.setOnClickListener(enable? new replayClickListener(): null);
    }


    @Override
    public void pointAtReplayButton() {

        broadcastLocation(TCONST.POINT_AND_TAP, mReplayButton);
    }


    private void broadcastLocation(String Action, View target) {

        target.getLocationOnScreen(_screenCoord);

        PointF centerPt = new PointF(_screenCoord[0] + (target.getWidth() / 2), _screenCoord[1] + (target.getHeight() / 2));
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{centerPt.x, (float) centerPt.y});

        bManager.sendBroadcast(msg);
    }



    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start

    @Override
    public void setVisibility(String visible) {

        mTutorScene.setVisibility(visible);
    }

    public void postEvent(String event) {
        postEvent(event,0);
    }

    public void postEvent(String event, Integer delay) {

        post(event, delay);
    }

    public void postEvent(String event, String param, Integer delay) {

        post(event, param, delay);
    }

    public void pointAtEraseButton() {
        super.pointAtEraseButton();
    }

    public void showReplayButton(Boolean show) { mReplayButton.setVisibility(show? VISIBLE:INVISIBLE); }

    public void highlightFields() {
        super.highlightFields();
    }

    public void clear() { super.clear(); }

    // TODO: Should renanme this to "enable"
    public void inhibitInput(Boolean inhibit) { super.inhibitInput(inhibit); }

    // Tutor methods  End
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


    // Execute script target if behavior is defined for this event
    //
    @Override
    public boolean applyBehavior(String event) {

        boolean result = false;

        if(!(result = super.applyBehavior(event))) {

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
    // publish component state data - START

    @Override
    public void publishState() {

        retractFeature(WR_CONST.ERROR_METRIC);
        retractFeature(WR_CONST.ERROR_CHAR);
        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_WRONG);

        if(_isValid) {

            publishFeature(TCONST.GENERIC_RIGHT);
        }
        else {

            publishFeature(TCONST.GENERIC_WRONG);

            if(!_metricValid) {
                publishFeature(WR_CONST.ERROR_METRIC);
            }
            if(!_charValid) {
                publishFeature(WR_CONST.ERROR_CHAR);
            }
        }
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

            publishFeature(feature);
        }
    }

    @Override
    public void retractFeatureSet(String featureSet) {

        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(featureSet.split(","));

        for(String feature : featArray) {

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

    // publish component state data - EBD
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // DataSink Implementation Start

    /**
     *
     * @param dataPacket
     */
    public void pushDataSource(String dataPacket) {

        if(dataSource != null) {
            _dataStack.add(new CDataSourceImg());
        }

        setDataSource(dataPacket);
    }


    /**
     *
     */
    public void popDataSource() {

        int popIndex = _dataStack.size()-1;

        if(popIndex >= 0) {
            CDataSourceImg popped = _dataStack.get(popIndex);
            popped.restoreDataSource();
            _dataStack.remove(popIndex);
        }
    }


    /**
     *
     * @param dataNameDescriptor
     */
    public void setDataSource(String dataNameDescriptor) {

        _correct = 0;
        _wrong   = 0;

        retractFeature(TCONST.FTR_EOI);
        retractFeature(TCONST.ALL_CORRECT);
        retractFeature(TCONST.FWCORRECT);
        retractFeature(TCONST.FWINCORRECT);

        try {
            if (dataNameDescriptor.startsWith(TCONST.LOCAL_FILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.LOCAL_FILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.DOWNLOAD_RT_TUTOR + "/" + mTutor.getTutorName() + "/";
                dataPath +=  mMediaManager.getLanguageIANA_2(mTutor) + "/";

                String jsonData = JSON_Helper.cacheDataByName(dataPath + dataFile);
                loadJSON(new JSONObject(jsonData), null);

                // Pass the loaded json dataSource array
                //
                setDataSource(dataSource);

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
                //
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

                // Pass the loaded json dataSource array
                //
                setDataSource(dataSource);

            } else if (dataNameDescriptor.startsWith("db|")) {
                dataNameDescriptor = dataNameDescriptor.substring(3);

            } else if (dataNameDescriptor.startsWith("[")) {

                dataNameDescriptor = dataNameDescriptor.substring(1, dataNameDescriptor.length()-1);

                // Pass an array of strings as the data source.
                //
//                setDataSource(dataNameDescriptor.split(","));

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), null, false);
        }

    }

    // Helper function that converts 3 digit number to list of digits
    private int[] getListDigits(int num) {
        int hundredsDigit = 0;  int tensDigit = 0;
        if(num >= 100) {
            hundredsDigit = (num / 100) * 100;
        }
        num = num % 100;

        tensDigit = num;

        Log.d("tadpolr", hundredsDigit + " " + tensDigit + " ");
        return (new int[]{hundredsDigit, tensDigit});
    }

    // Publish concatenated audio.
    public void publishConcatAudio(String constant, String value) {
            String publishValueConstHundreds = "";
            String publishValueConstTens = "";
            String publishFeatureValue = "";

            if(constant == "STIM_1") {
                Log.d("tadpolr", "publishConcat: STIM_1");
                publishFeatureValue = WR_CONST.FTR_STIM_1_CONCAT;
                publishValueConstHundreds = WR_CONST.AUDIO_STIM_1_CONCAT_HUNDREDS;
                publishValueConstTens = WR_CONST.AUDIO_STIM_1_CONCAT_TENS;
            } else if (constant == "STIM_3") {
                Log.d("tadpolr", "publishConcat: STIM_3");
                publishFeatureValue = WR_CONST.FTR_STIM_3_CONCAT;
                publishValueConstHundreds = WR_CONST.AUDIO_STIM_3_CONCAT_HUNDREDS;
                publishValueConstTens = WR_CONST.AUDIO_STIM_3_CONCAT_TENS;
            } else if (constant == "ANS") {
                Log.d("tadpolr", "publishConcat: ANS");
                publishFeatureValue = WR_CONST.FTR_ANS_CONCAT;
                publishValueConstHundreds = WR_CONST.AUDIO_ANS_CONCAT_HUNDREDS;
                publishValueConstTens = WR_CONST.AUDIO_ANS_CONCAT_TENS;
            }

        // attempt to parse non-int value will prevent _data
        // from changing from demo_data to data_source_data
        try {
            int operand1 = Integer.parseInt(value);
            int[] operand1Digits = getListDigits(operand1);

            //Publish features and values for each digit of first operand so that audios can be played separately
            if (operand1Digits[0] >= 100) {
                publishFeature(publishFeatureValue);
                publishValue(publishValueConstHundreds, operand1Digits[0]);
            } else {
                removeFeature(publishFeatureValue);
                publishValue(publishValueConstHundreds, "");
            }

            if (operand1Digits[1] >= 1) {
                publishValue(publishValueConstTens, operand1Digits[1]);
            } else {
                publishValue(publishValueConstTens, "");
            }
        } catch (NumberFormatException e) {
                Log.d("tadpolr", "tried parsing empty String as number");
        }

    }

    public void next() {

        retractFeature(TCONST.ALL_CORRECT);
        retractFeature(TCONST.FWCORRECT);
        retractFeature(TCONST.FWINCORRECT);

        super.next();

        // update the Scope response variable  "<Sstimulus>.value"
        //
        publishValue(WR_CONST.VALUE_VAR, mAnswer.toLowerCase());

        // For number activity that may need concatenation.
        //
        boolean isNumberActivity = activityFeature.contains("FTR_NUMBERS");
        boolean isDotCountActivity = activityFeature.contains("FTR_DOTCOUNT");
        boolean isArithActivity = activityFeature.contains("FTR_ARITH");

        if (isNumberActivity || isDotCountActivity || isArithActivity) {
            publishConcatAudio("ANS", mAnswer);
            publishConcatAudio("STIM_1", mAudioStimulus[0]);
            if (mAudioStimulus.length >= 3) {
                publishConcatAudio("STIM_3", mAudioStimulus[2]);
            }
        }

        // update the Scope response variable  "SWordCopy.audiostim"
        //
        if (mAudioStimulus.length == 1) {

            publishValue(WR_CONST.AUDIO_STIM_1, mAudioStimulus[0].toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_2, "");
            publishValue(WR_CONST.AUDIO_STIM_3, "");

        } else if (mAudioStimulus.length == 2) {

            publishValue(WR_CONST.AUDIO_STIM_1, mAudioStimulus[0].toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_2, mAudioStimulus[1].toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_3, "");

        } else if (mAudioStimulus.length >= 3) {

            publishValue(WR_CONST.AUDIO_STIM_1, mAudioStimulus[0].toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_2, mAudioStimulus[1].toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_3, mAudioStimulus[2].toLowerCase());

        } else {

            publishValue(WR_CONST.AUDIO_STIM_1, mStimulus.toLowerCase());
            publishValue(WR_CONST.AUDIO_STIM_2, "");
            publishValue(WR_CONST.AUDIO_STIM_3, "");
        }


        if(dataExhausted()) {

            // set the script 'Feature'
            //
            publishFeature(TCONST.FTR_EOI);
        }
    }


    /**
     * This is used to push pop a datasource at run time - i.e. it allows you to switch datasources
     * on the fly.
     */
    class CDataSourceImg {

        private int  _wrongStore   = 0;
        private int  _correctStore = 0;

        private HashMap<String,Boolean> _FeatureStore;

        protected List<CWr_Data>    _dataStore;
        protected int               _dataIndexStore;
        protected boolean           _dataEOIStore;

        CWr_Data[]                  _dataSourceStore;

        public CDataSourceImg() {

            _correctStore    = _correct;
            _wrongStore      = _wrong;

            _dataStore       = _data;
            _dataIndexStore  = _dataIndex;
            _dataEOIStore    = _dataEOI;

            _FeatureStore    = new HashMap<>(_FeatureMap);
            _dataSourceStore = dataSource;

            retractFeatureMap(_FeatureMap);
        }

        public void restoreDataSource() {

            // Retract the active feature set
            //
            retractFeatureMap(_FeatureMap);

            _correct = _correctStore;
            _wrong   = _wrongStore;

            _data      = _dataStore;
            _dataIndex = _dataIndexStore;
            _dataEOI   = _dataEOIStore;

            _FeatureMap= _FeatureStore;
            dataSource = _dataSourceStore;

            // publish the popped feature set
            //
            publishFeatureMap(_FeatureMap);
        }
    }

    // DataSink IMplementation End
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
        return "Writing_Component";
    }

    // IEventSource Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // ITutorObject Implementation Start

    @Override
    public void setName(String name) {
        mTutorScene.setName(name);
    }

    @Override
    public String name() {
        return mTutorScene.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mTutorScene.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {

        mTutor = tutor;
        mTutorScene.setTutor(tutor);

        activityFeature = mTutor.getFeatures();

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mTutorScene.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mTutorScene.setLogManager(logManager);
    }


    @Override
    public CSceneDelegate getimpl() {
        return mTutorScene;
    }


    @Override
    public ViewGroup getOwner() {
        return mTutorScene.getOwner();
    }

    @Override
    public String preEnterScene(scene_descriptor scene, String Direction) {
        return mTutorScene.preEnterScene(scene, Direction);
    }

    @Override
    public void onEnterScene() {
        mTutorScene.onEnterScene();
    }

    @Override
    public String preExitScene(String Direction, int sceneCurr) {
        return mTutorScene.preExitScene(Direction, sceneCurr);
    }

    @Override
    public void onExitScene() {
        mTutorScene.onExitScene();
    }

    // ITutorObject Implementation End
    //************************************************************************
    //************************************************************************

    /* For scoring */

    public void resetScore() {
        mTutor.resetScore();
    }
    public void countCorrect() {
        mTutor.countCorrect();
    }

    public void countIncorrect() {
        mTutor.countIncorrect();
    }


    // *** Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        bootFeatures = EMPTY;

        // Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, scope);

        // set total question numbers
        mTutor.setTotalQuestions(TCONST.WRITING_DATA_LIMIT);

        // Apply any features defined directly in the datasource itself - e.g. demo related features
        //
        if(!bootFeatures.equals(EMPTY)) {

            publishFeatureSet(bootFeatures);
        }
    }

}
