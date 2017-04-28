package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.asm_component.ASM_CONST;
import cmu.xprize.asm_component.CAsm_Alley;
import cmu.xprize.asm_component.CAsm_Component;
import cmu.xprize.asm_component.CAsm_Data;
import cmu.xprize.asm_component.CAsm_DotBag;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static java.lang.Thread.sleep;

public class TAsmComponent extends CAsm_Component implements ITutorObjectImpl, IDataSink, IBehaviorManager, IEventSource {

    private CTutor           mTutor;
    private CObjectDelegate  mSceneObject;
    private CMediaManager    mMediaManager;

    private ArrayList<TAsmComponent.CDataSourceImg> _dataStack  = new ArrayList<>();

    //used to store the current features about overhead
    //
    private List<String> curFeatures = new ArrayList<String>();

    private ArrayList<String>       _FeatureSet = new ArrayList<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    static final String TAG = "TAsmComponent";


    public TAsmComponent(Context context) {
        super(context);
    }

    public TAsmComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAsmComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    public void evaluateWhole () {

        reset();

        boolean correct = isWholeCorrect();

        // If the Problem is complete and correct then set FTR and continue
        // otherwise evaluate the digit for errors
        //
        if(correct) {
            publishFeature(TCONST.FTR_COMPLETE);
        }
        else {
            evaluateDigit();
        }
    }


    public void evaluateDigit () {

        reset();
        retractFeature(TCONST.ASM_ADD_PROMPT);
        retractFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
        retractFeature(TCONST.ASM_SUB_PROMPT);

        boolean correct = isDigitCorrect();

        if (correct) {
            publishFeature(TCONST.GENERIC_RIGHT);
            publishFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);

            saveCurFeaturesAboutOverhead();
            delCurFeaturesAboutOverhead();
        } else {
            if (!mPopup.isActive && !mPopupSupplement.isActive) isWriting = false;

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDotBagsVisible(true, digitIndex);
                }
            }, 3000);

            publishFeature(TCONST.GENERIC_WRONG);

            if (resultCorrect == ASM_CONST.NOT_ALL_INPUT_RIGHT) {
                publishFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);
                resultCorrect = ASM_CONST.NO_INPUT;
            } else if (overheadCorrect == ASM_CONST.ALL_INPUT_RIGHT)
                publishFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
            else
                publishFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);
        }

    }


    public void saveCurFeaturesAboutOverhead() {

        if (mTutor.testFeature(TCONST.ASM_RA_START)) curFeatures.add(TCONST.ASM_RA_START);
        if (mTutor.testFeature(TCONST.ASM_NEXT_NUMBER)) curFeatures.add(TCONST.ASM_NEXT_NUMBER);
        if (mTutor.testFeature(TCONST.ASM_NEXT_RESULT)) curFeatures.add(TCONST.ASM_NEXT_RESULT);
        if (mTutor.testFeature(TCONST.ASM_RESULT_FIRST_TWO)) curFeatures.add(TCONST.ASM_RESULT_FIRST_TWO);
        if (mTutor.testFeature(TCONST.ASM_RESULT_NEXT_OR_LAST)) curFeatures.add(TCONST.ASM_RESULT_NEXT_OR_LAST);
    }


    public void delCurFeaturesAboutOverhead() {

        retractFeature(TCONST.ASM_RA_START);
        retractFeature(TCONST.ASM_NEXT_NUMBER);
        retractFeature(TCONST.ASM_NEXT_RESULT);
        retractFeature(TCONST.ASM_RESULT_FIRST_TWO);
        retractFeature(TCONST.ASM_RESULT_NEXT_OR_LAST);
    }

    public void retrieveCurFeaturesAboutOverhead() {
        for (int i = 0; i < curFeatures.size(); i++) {
            publishFeature(curFeatures.get(i));
        }
    }

    public void reset() {

        retractFeature(TCONST.FTR_COMPLETE);

        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_WRONG);

        retractFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
        retractFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);

        retractFeature(TCONST.ASM_ALL_DOTS_DOWN);
    }

    public void resetAll() {
        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_WRONG);
        retractFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
        retractFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);

        resetAllAboutAdd();
        resetAllAboutSub();
        resetAllAboutMulti();
    }

    public void resetAllAboutAdd() {
        retractFeature(TCONST.ASM_ADD);
        retractFeature(TCONST.ASM_ADD_PROMPT);
        retractFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
        retractFeature(TCONST.ASM_ALL_DOTS_DOWN);
    }

    public void resetAllAboutSub() {
        retractFeature(TCONST.ASM_SUB);
        retractFeature(TCONST.ASM_SUB_PROMPT);
    }

    public void resetAllAboutMulti() {
        retractFeature(TCONST.ASM_MULTI);
        retractFeature(TCONST.ASM_MULTI_PROMPT);
        retractFeature(TCONST.ASM_RA_START);
        retractFeature(TCONST.ASM_NEXT_NUMBER);
        retractFeature(TCONST.ASM_NEXT_RESULT);
        retractFeature(TCONST.ASM_RESULT_FIRST_TWO);
        retractFeature(TCONST.ASM_RESULT_NEXT_OR_LAST);
        retractFeature(TCONST.ASM_REPEATED_ADD_DOWN);
    }

    /**
     * Preprocess the data set
     *
     * @param data
     */
    @Override
    protected void updateDataSet(CAsm_Data data) {

        // Let the compoenent process the new data set
        //
        super.updateDataSet(data);
    }


    //************************************************************************
    //************************************************************************
    // DataSink Implementation Start

    /**
     *
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
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                dataNameDescriptor = dataNameDescriptor.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataNameDescriptor);
                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), e, false);
        }

        // This is just to init the features set to drive the PLAY_INTRO audio selectively
        //
        if(dataSource != null) {
            loadDataSet(dataSource[0]);
            initOperationFTR();
        }
    }


    public void next() {

        // If wrong reset ALLCORRECT
        //
        if (mTutor.testFeatureSet(TCONST.GENERIC_WRONG))
                            retractFeature(TCONST.ALL_CORRECT);
        resetAll();

        super.next();
        resetPlaceValue();

        mTutor.getScope().addUpdateVar(name() + ".image", new TString(curImage));

        // Permit changes to operation type within problem set/
        //
        initOperationFTR();

        if (dataExhausted()) publishFeature(TCONST.FTR_EOI);

        curFeatures.clear();
    }


    /**
     *
     * @param dataPacket
     */
    public void pushDataSource(String dataPacket) {

        if(dataSource != null) {
            _dataStack.add(new TAsmComponent.CDataSourceImg());
        }

        setDataSource(dataPacket);
    }


    /**
     *
     */
    public void popDataSource() {

        int popIndex = _dataStack.size()-1;

        retractAllFeatures();

        if(popIndex >= 0) {
            TAsmComponent.CDataSourceImg popped = _dataStack.get(popIndex);
            popped.restoreDataSource();
            _dataStack.remove(popIndex);
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

        protected int           _dataIndexStore;
        protected boolean       _dataEOIStore;

        CAsm_Data[]             _dataSourceStore;


        public CDataSourceImg() {

            _dataIndexStore  = _dataIndex;
            _dataEOIStore    = _dataEOI;

            _FeatureStore    = _FeatureMap;
            _dataSourceStore = dataSource;

            for(String feature : _FeatureSet) {
                mTutor.setDelFeature(feature);
            }

        }

        public void restoreDataSource() {

            dataSource = _dataSourceStore;
            _dataIndex = _dataIndexStore;
            _dataEOI   = _dataEOIStore;

            _FeatureMap= _FeatureStore;

            for(String feature : _FeatureSet) {
                if(_FeatureMap.get(feature)) {
                    mTutor.setAddFeature(feature);
                }
                else {
                    mTutor.setDelFeature(feature);
                }
            }
        }
    }

    // DataSink IMplementation End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // publish component state data - START

    @Override
    public void publishState() {
    }

    @Override
    public void publishValue(String varName, String value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));

    }

    @Override
    public void publishValue(String varName, int value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TInteger(value));

    }

    @Override
    public void publishFeature(String feature) {

        trackFeatures(feature);

        _FeatureMap.put(feature, true);
        mTutor.setAddFeature(feature);
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

        trackFeatures(feature);

        _FeatureMap.put(feature, false);
        mTutor.setDelFeature(feature);
    }

    /**
     * _FeatureSet keeps track of used features
     *
     * @param feature
     */
    private void trackFeatures(String feature) {

        if(_FeatureSet.indexOf(feature) == -1)
        {
            _FeatureSet.add(feature);
        }
    }

    private void retractAllFeatures() {

        for(String feature: _FeatureSet) {
            mTutor.setDelFeature(feature);
        }

        _FeatureSet = new ArrayList<>();
        _FeatureMap = new HashMap<>();
    }

    // publish component state data - EBD
    //************************************************************************
    //************************************************************************




    @Override
    public void playChime() {

        TScope scope = mTutor.getScope();

        for (CAsm_Alley alley: allAlleys) {

            CAsm_DotBag dotBag = alley.getDotBag();
            if (dotBag.getIsAudible()) {
                currentChime = dotBag.getCurrentChime();
            }
        }

        Log.d("PlayChime", currentChime);
        scope.addUpdateVar("TestChimes", new TString(currentChime));

        postEvent(ASM_CONST.CHIME_FEEDBACK);
    }


    private void initOperationFTR() {

        if (operation != null) {
            switch (operation) {
                case "+" :
                    publishFeature(TCONST.ASM_ADD);
                    mTutor.getScope().addUpdateVar(name() + ".operand1", new TString(dataset[0] + ""));
                    break;
                case "-" :
                    publishFeature(TCONST.ASM_SUB);
                    break;
                case "x" :
                    publishFeature(TCONST.ASM_MULTI);
                    mTutor.getScope().addUpdateVar(name() + ".operand1", new TString(dataset[0] + ""));
                    mTutor.getScope().addUpdateVar(name() + ".operand2", new TString(dataset[1] + ""));
                    break;
            }
        }
    }


    public void nextDigit() {

        reset();
        super.nextDigit();
        nextPlaceValue();

        retrieveCurFeaturesAboutOverhead();
    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
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
        }
    }


    // Execute script target if behavior is defined for this event
    //
    @Override
    public boolean applyBehavior(String event) {

        boolean result = false;

        if(!(result = super.applyBehavior(event))) {

            if (volatileMap.containsKey(event)) {
                Log.d(TAG, "Processing WC_ApplyEvent: " + event);
                applyBehaviorNode(volatileMap.get(event));

                volatileMap.remove(event);

                result = true;

            } else if (stickyMap.containsKey(event)) {

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

                    switch(obj.getType()) {

                        case TCONST.SUBGRAPH:

                            mTutor.getSceneGraph().post(this, TCONST.SUBGRAPH_CALL, nodeName);
                            break;

                        case TCONST.MODULE:

                            // Disallow module "calls"
                            Log.e(TAG, "MODULE Behaviors are not supported");
                            break;

                        // Note that we should not preEnter queues - they may need to be cancelled
                        // which is done internally.
                        //
                        case TCONST.QUEUE:
                            obj.applyNode();
                            break;

                        default:
                            obj.preEnter();
                            obj.applyNode();
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
        return "ASM_Component";
    }


    // IEventSource Interface END
    //************************************************************************
    //************************************************************************




    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods


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
    public void onCreate() {

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


    /**
     * These events come from the FingerWriter Component which is connected in the Layout XML
     *
     * @param event
     */    @Override
    public void onEvent(IEvent event) {

        retractFeature(TCONST.ASM_ALL_DOTS_DOWN);

        //retractFeature(TCONST.ASM_MULTI);
        super.onEvent(event);

        evaluateWhole();

        postEvent(ASM_CONST.INPUT_BEHAVIOR);
    }

    public void applyEventNode(String nodeName) {
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

    public void exitWrite() {
        super.exitWrite();
    }

    public void highlightOverheadOrResult(String whichToHighlight) {
        mechanics.highlightOverheadOrResult(whichToHighlight);
    }

    public void addMapToTutor(String key, String value) {
        mTutor.getScope().addUpdateVar(name() + key, new TString(value));
    }

    public void delAddFeature(String delFeature, String addFeature) {
        retractFeature(delFeature);
        publishFeature(addFeature);
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex, int startRow) {
        boolean oldState = hasShown;
        super.setDotBagsVisible(_dotbagsVisible, curDigitIndex, startRow);

        if (oldState == false && hasShown == true) {
            if (curStrategy.equals(ASM_CONST.STRATEGY_COUNT_FROM))
                publishFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
            else
                publishFeature(TCONST.ASM_ADD_PROMPT);
            publishFeature(TCONST.ASM_SUB_PROMPT);

            if (curNode.equals(ASM_CONST.NODE_ADD_PROMPT) || curNode.equals(ASM_CONST.NODE_SUB_PROMPT)) {
                publishFeature(TCONST.ASM_CLICK_ON_DOT);

                // Apply the script defined behavior -
                //
                postEvent(ASM_CONST.SHOW_BAG_BEHAVIOR);            }
        }
    }

    public void updateCurNode(String curNode) {
        this.curNode = curNode;
    }

}
