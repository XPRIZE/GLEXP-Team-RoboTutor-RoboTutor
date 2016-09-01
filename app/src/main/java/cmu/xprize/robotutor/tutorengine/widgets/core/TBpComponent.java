package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.bp_component.BP_CONST;
import cmu.xprize.bp_component.CBP_Component;
import cmu.xprize.bp_component.CBp_Data;
import cmu.xprize.bp_component.CBubble;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TBpComponent extends CBP_Component implements ITutorObjectImpl, IDataSink {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private CBubble _touchedBubble;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap = new HashMap<>();


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


    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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


    private void reset() {

        resetValid();
        resetState();
    }


    private void resetValid() {

        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
        mTutor.setDelFeature(TCONST.LAST_ATTEMPT);

    }


    private void resetState() {

        mTutor.setDelFeature(TCONST.SAY_STIMULUS);
        mTutor.setDelFeature(TCONST.SHOW_STIMULUS);
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
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        // We make the assumption that all are correct until proven wrong
        //
        mTutor.setAddFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

            } else if (dataSource.startsWith("db|")) {


            } else if (dataSource.startsWith("{")) {

                loadJSON(new JSONObject(dataSource), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source - " + dataSource + " for : " + name() + " : ", e, false);
        }
    }


    public void next() {

        // If wrong reset ALLCORRECT
        //
        if (mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {

            mTutor.setDelFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.next();

        if (dataExhausted()) {
            mTutor.setAddFeature(TCONST.FTR_EOD);
            Log.d("BPOP", "Data Exhausted ");
        }
    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
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
    public boolean applyEvent(String event) {

        boolean result = false;

        if (volatileMap.containsKey(event)) {
            Log.d(TAG, "Processing BP_ApplyEvent: " + event);
            applyEventNode(volatileMap.get(event));

            volatileMap.remove(event);

            result = true;

        } else if (stickyMap.containsKey(event)) {
            applyEventNode(stickyMap.get(event));

            result = true;
        }

        return result;
    }


    /**
     * Apply Events in the Tutor Domain.
     *
     * @param nodeName
     */
    protected void applyEventNode(String nodeName) {
        IScriptable2 obj = null;

        if (nodeName != null && !nodeName.equals("") && !nodeName.toUpperCase().equals("NULL")) {

            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if (obj != null) {
                    obj.preEnter();
                    obj.applyNode();
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }

    // Scripting Interface  End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // publish component state data - START

    /**
     * Publish the Stimulus value as Scope variables for script access
     */
    @Override
    protected void publishState(CBubble bubble) {

        _touchedBubble = bubble;

        TScope scope = mTutor.getScope();

        String answer = bubble.getStimulus();

        if(answer.length() == 1)
            answer = answer.toUpperCase();

        scope.addUpdateVar(name() + ".ansValue", new TString(answer));

        resetValid();

        if (bubble.isCorrect()) {
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
            Log.d("BPOP", "Correct" );
            correct_Count++;
        } else {
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
            Log.d("BPOP", "Wrong" );
            attempt_count--;

            if(attempt_count <= 0) {
                mTutor.setAddFeature(TCONST.LAST_ATTEMPT);

                Log.d("BPOP", "Publish Last Attempt" );
            }
        }

        Log.d("BPOP", "Publish correct Count: " + correct_Count);
        Log.d("BPOP", "Publish attempt Count: " + attempt_count);

    }


    protected void publishQuestionState(CBp_Data data) {

        TScope scope = mTutor.getScope();

        resetState();

        String correctVal = _stimulus_data[data.dataset[data.stimulus_index]];

        if(correctVal.length() == 1)
            correctVal = correctVal.toUpperCase();

        scope.addUpdateVar(name() + ".questValue", new TString(correctVal));

        if (data.question_say) {
            mTutor.setAddFeature(TCONST.SAY_STIMULUS);
        }

        if (data.question_show) {
            mTutor.setAddFeature(TCONST.SHOW_STIMULUS);
        }
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void publishValue(String varName, String value) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void publishValue(String varName, int value) {
    }


    // publish component state data - EBD
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
    public void postInflate() {

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
        Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, (IScope2) scope);

        // Map the language specific data source
        //
        _stimulus_data = stimulus_map.get(((IScope2) scope).tutor().getLanguageFeature());
    }
}
