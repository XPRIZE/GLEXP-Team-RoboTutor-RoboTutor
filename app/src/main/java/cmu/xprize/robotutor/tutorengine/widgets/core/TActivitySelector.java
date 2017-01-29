package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.comp_session.CActivitySelector;
import cmu.xprize.util.CAs_Data;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.robotutor.BuildConfig;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.robotutor.tutorengine.util.CClassMap2.classMap;

public class TActivitySelector extends CActivitySelector implements IBehaviorManager, ITutorSceneImpl, IDataSink, IEventSource {

    private static final boolean    DEBUG_LANCHER = true;

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private String      activeSkill = AS_CONST.SELECT_NONE;
    private String      activeTutor = "";
    private String      nextTutor   = "";
    private String      rootTutor;
    private boolean     askButtonsEnabled = false;

    private HashMap<String, CAs_Data> initiatorMap;
    private HashMap<String, CAt_Data> transitionMap;

    private String      writingTutorID;
    private String      storiesTutorID;
    private String      mathTutorID;
    private String      shapesTutorID;

    private CAt_Data    writingVector = null;
    private CAt_Data    storiesVector = null;
    private CAt_Data    mathVector    = null;
    private CAt_Data    shapesVector  = null;

    private ArrayList<String>       _FeatureSet = new ArrayList<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();


    final private String  TAG = "TActivitySelector";



    public TActivitySelector(Context context) {
        super(context);
    }

    public TActivitySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TActivitySelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        SaskActivity   = (TAskComponent)findViewById(R.id.SaskActivity);
        SdebugActivity = (CDebugComponent)findViewById(R.id.SdebugActivity);

        SaskActivity.setButtonController(this);
        SdebugActivity.setButtonController(this);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        mTutorScene.onDestroy();
    }

    @Override
    public void setVisibility(String visible) {

    }


    public void setLayout(String name) {

        if(name.equals(TCONST.FTR_DEBUG_SELECT)) {

            SaskActivity.setVisibility(GONE);
            SdebugActivity.setVisibility(VISIBLE);

            // Init the skill pointers
            //
            switch (activeSkill) {

                case AS_CONST.SELECT_WRITING:

                    activeTutor = writingTutorID;
                    transitionMap = writeTransitions;
                    initiatorMap  = writeInitiators;
                    break;

                case AS_CONST.SELECT_STORIES:

                    activeTutor = storiesTutorID;
                    transitionMap = storyTransitions;
                    initiatorMap  = storyInitiators;
                    break;

                case AS_CONST.SELECT_MATH:

                    activeTutor = mathTutorID;
                    transitionMap = mathTransitions;
                    initiatorMap  = mathInitiators;
                    break;

                case AS_CONST.SELECT_SHAPES:

                    activeTutor = shapesTutorID;
                    transitionMap = shapeTransitions;
                    initiatorMap  = shapeInitiators;
                    break;

            }

            SdebugActivity.initGrid(activeSkill, activeTutor, transitionMap, initiatorMap);
        }
        else {

            SaskActivity.setVisibility(VISIBLE);
            SdebugActivity.setVisibility(GONE);

            for (CAsk_Data layout : dataSource) {

                if (layout.name.equals(name)) {

                    _activeLayout = layout;

                    SaskActivity.setDataSource(layout);
                    break;
                }
            }
        }
    }


    public void rippleDescribe() {
        _describeIndex = 0;

        describeNext();
    }

    public void describeNext() {

        // In debug selector mode - _activeLayout may be null
        //
        if(_activeLayout != null) {

            if (_describeIndex < _activeLayout.items.length) {

                publishValue(AS_CONST.VAR_BUTTONID, _activeLayout.items[_describeIndex].componentID);
                publishValue(AS_CONST.VAR_BUT_BEHAVIOR, _activeLayout.items[_describeIndex].behavior);
                publishValue(AS_CONST.VAR_HELP_AUDIO, _activeLayout.items[_describeIndex].help);
                publishValue(AS_CONST.VAR_PROMPT_AUDIO, _activeLayout.items[_describeIndex].prompt);

                applyBehavior(AS_CONST.DESCRIBE_BEHAVIOR);

                _describeIndex++;
            } else {

                applyBehavior(AS_CONST.DESCRIBE_COMPLETE);
            }
        }
    }


    @Override
    public void doAskButtonAction(String actionid) {

        Log.d(TAG, "ASK Button : " + actionid);

        if(askButtonsEnabled) {

            askButtonsEnabled = false;

            applyBehavior(AS_CONST.SELECT_BEHAVIOR);

            for (CAskElement element : _activeLayout.items) {

                if (element.componentID.equals(actionid)) {

                    publishValue(AS_CONST.VAR_BUTTONID, element.componentID);
                    publishValue(AS_CONST.VAR_BUT_BEHAVIOR, element.behavior);
                    publishValue(AS_CONST.VAR_HELP_AUDIO, element.help);
                    publishValue(AS_CONST.VAR_PROMPT_AUDIO, element.prompt);

                    applyBehavior(element.behavior);
                }
            }
        }
    }

    public void enableAskButtons(Boolean enable) {

        askButtonsEnabled = true;
    }


    /**
     * This is a kludge at the moment to utilize the tutor initiators that exist already.
     * TODO: make this process more sane.
     *
     * @param localArray
     * @param tutor_id
     * @return
     */
    public int decodeSkill(CAs_Data[] localArray, String tutor_id) {

        int tutorVector = 0;

        for(int i1 = 0 ; i1 < localArray.length ; i1++) {

            if(localArray[i1].buttonvalue == tutor_id) {
                tutorVector = i1;
                break;
            }
        }

        return tutorVector;
    }


    /** This allows us to update the current tutor for a given skill from the CDebugComponent
     *
      */
    public void setDebugTutor(String debugTutor) {

        // Update the active skill
        //
        switch (activeSkill) {

            case AS_CONST.SELECT_WRITING:

                writingTutorID = debugTutor;
                break;

            case AS_CONST.SELECT_STORIES:

                storiesTutorID = debugTutor;
                break;

            case AS_CONST.SELECT_MATH:

                mathTutorID = debugTutor;
                break;

            case AS_CONST.SELECT_SHAPES:

                shapesTutorID = debugTutor;
                break;
        }
    }


    /**
     * Button clicks may come from either the skill selector ASK component or the Difficulty
     * selector ASK component.
     *
     * @param buttonid
     */
    @Override
    public void doButtonBehavior(String buttonid) {

        Log.d(TAG, "Button Selected: " + buttonid);

        boolean     buttonFound = false;


        // If we are in debug mode then there is a third selection phase where we are presented
        // the transition table for the active skill - The author can select a new target tutor
        // from any of the transition entries.
        //
        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DEBUG_SELECT)) {

            // We pass the selected tutor from the debugcomponent in the buttonid
            //
            setDebugTutor(buttonid);

            // just reselect the current skill and continue with next tutor
            // no skill selection phase
            buttonid = activeSkill;
            RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;

            // Serialize the new state
            //
            SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE);

            // only one will have been changed but update all
            //
            editor.putString(TCONST.SKILL_WRITING, writingTutorID);
            editor.putString(TCONST.SKILL_STORIES, storiesTutorID);
            editor.putString(TCONST.SKILL_MATH, mathTutorID);
            editor.putString(TCONST.SKILL_SHAPES, shapesTutorID);

            editor.apply();
        }


        // If we are in Assessment mode we have prompted the student to assess the difficulty of the
        // tutor they have just completed.
        // Difficulty selection
        //

        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DIFFICULTY_ASSESS)) {

            // Init the skill pointers
            //
            switch (activeSkill) {

                case AS_CONST.SELECT_WRITING:

                    activeTutor = writingTutorID;
                    transitionMap = writeTransitions;
                    break;

                case AS_CONST.SELECT_STORIES:

                    activeTutor = storiesTutorID;
                    transitionMap = storyTransitions;
                    break;

                case AS_CONST.SELECT_MATH:

                    activeTutor = mathTutorID;
                    transitionMap = mathTransitions;
                    break;

                case AS_CONST.SELECT_SHAPES:

                    activeTutor = shapesTutorID;
                    transitionMap = shapeTransitions;
                    break;

            }

            switch (buttonid.toUpperCase()) {

                case AS_CONST.SELECT_CONTINUE:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;

                    mTutor.post(TCONST.ENDTUTOR);
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;

                case AS_CONST.SELECT_MAKE_HARDER:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).harder;

                    mTutor.post(TCONST.ENDTUTOR);
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;

                case AS_CONST.SELECT_MAKE_EASIER:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).easier;

                    mTutor.post(TCONST.ENDTUTOR);
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;

                case AS_CONST.SELECT_EXIT:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).tutor_id;

                    mTutor.post(TCONST.FINISH);
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;

                // If user selects "Let robotutor decide" then use student model to decide how to adjust the
                // difficulty level.  We also flip mode to tutor_select to skip the tutor select phase and
                // let the model do the tutor selection.
                // At the moment default to continue to "next" link
                //
                case AS_CONST.SELECT_AUTO_DIFFICULTY:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;

                    // just reselect the current skill and continue with next tutor
                    // no skill selection phase
                    buttonid = activeSkill;
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;

                case AS_CONST.SELECT_REPEAT:
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).tutor_id;
                    // just reselect the current skill and continue with next tutor
                    // no skill selection phase
                    buttonid = activeSkill;
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                    break;
            }

            // Update the active skill
            //
            switch (activeSkill) {

                case AS_CONST.SELECT_WRITING:

                    writingTutorID = nextTutor;
                    break;

                case AS_CONST.SELECT_STORIES:

                    storiesTutorID = nextTutor;
                    break;

                case AS_CONST.SELECT_MATH:

                    mathTutorID = nextTutor;
                    break;

                case AS_CONST.SELECT_SHAPES:

                    shapesTutorID = nextTutor;
                    break;
            }

            // Serialize the new state
            //
            SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE);

            // only one will have been changed but update all
            //
            editor.putString(TCONST.SKILL_WRITING, writingTutorID);
            editor.putString(TCONST.SKILL_STORIES, storiesTutorID);
            editor.putString(TCONST.SKILL_MATH, mathTutorID);
            editor.putString(TCONST.SKILL_SHAPES, shapesTutorID);

            editor.apply();
        }


        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_TUTOR_SELECT) ||
           RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DEBUG_LAUNCH)) {

            // If user selects "Let robotutor decide" then use student model to decide skill to work next
            // At the moment default to Stories
            //
            if (buttonid.toUpperCase().equals(AS_CONST.SELECT_ROBOTUTOR)) {
                buttonid = AS_CONST.SELECT_STORIES;
            }

            // First check if it is a skill selection button =
            //
            switch (buttonid.toUpperCase()) {

                case AS_CONST.SELECT_WRITING:

                    activeSkill = AS_CONST.SELECT_WRITING;
                    activeTutor = writingTutorID;
                    initiatorMap = writeInitiators;
                    rootTutor = rootSkillWrite;
                    buttonFound = true;
                    break;

                case AS_CONST.SELECT_STORIES:

                    activeSkill = AS_CONST.SELECT_STORIES;
                    activeTutor = storiesTutorID;
                    initiatorMap = storyInitiators;
                    rootTutor = rootSkillStories;
                    buttonFound = true;

                    break;

                case AS_CONST.SELECT_MATH:

                    activeSkill = AS_CONST.SELECT_MATH;
                    activeTutor = mathTutorID;
                    initiatorMap = mathInitiators;
                    rootTutor = rootSkillMath;
                    buttonFound = true;
                    break;

                case AS_CONST.SELECT_SHAPES:

                    activeSkill = AS_CONST.SELECT_SHAPES;
                    activeTutor = shapesTutorID;
                    initiatorMap = shapeInitiators;
                    rootTutor = rootSkillShapes;
                    buttonFound = true;

                    break;

            }

            if (buttonFound) {

                // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
                // reenable the ASK buttons if we don't execute the story_tutor
                //
                if (!BuildConfig.NO_ASR_APPS || (initiatorMap != storyTransitions)) {

                    CAs_Data tutor = (CAs_Data) initiatorMap.get(activeTutor);

                    // This is just to make sure we go somewhere if there is a bad link - which
                    // there shuoldn't be :)
                    //
                    if (tutor == null) {
                        tutor = (CAs_Data) initiatorMap.get(rootTutor);
                    }

                    // If we are using the edbug selector and it is not lauching a tutor then
                    // switch to its view through a relaunch
                    //
                    if(DEBUG_LANCHER && RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_TUTOR_SELECT)) {

                        mTutor.post(TCONST.ENDTUTOR);
                        RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;
                    }
                    else {
                        doLaunch(tutor.intent, tutor.intentdata, tutor.datasource, tutor.features);
                    }

                    // Serialize the new state
                    //
                    SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString(TCONST.SKILL_SELECTED, activeSkill);
                    editor.apply();
                } else
                    SaskActivity.enableButtons(true);
            }
        }
    }


    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void doLaunch(String intent, String intentData, String dataSource, String features) {

        RoboTutor.SELECTOR_MODE = TCONST.FTR_DIFFICULTY_ASSESS;

        // update the response variable  "<Sresponse>.value"

        mTutor.getScope().addUpdateVar(name() + ".intent", new TString(intent));
        mTutor.getScope().addUpdateVar(name() + ".intentData", new TString(intentData));
        mTutor.getScope().addUpdateVar(name() + ".dataSource", new TString(dataSource));
        mTutor.getScope().addUpdateVar(name() + ".features", new TString(features));

        applyBehavior(AS_CONST.LAUNCH_EVENT);
    }


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

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor);
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



    //************************************************************************
    //************************************************************************
    // DataSink Implementation Start


    @Override
    public void setDataSource(String dataNameDescriptor) {

        // TODO: globally make startWith type TCONST
        try {
            if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

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

    // DataSink Implementation END
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

            Log.d(TAG, "Clearing Event: " + event);
            if (stickyMap.containsKey(event)) {
                stickyMap.remove(event);
            }

        } else {

            Log.d(TAG, "Setting Event: " + event + " - behavior : " + behavior);
            stickyMap.put(event, behavior);
        }
    }


    // Execute scirpt target if behavior is defined for this event
    //
    public boolean applyBehavior(String event) {

        boolean result = false;

        if (volatileMap.containsKey(event)) {
            Log.d(TAG, "Processing BP_ApplyEvent: " + event);
            applyBehaviorNode(volatileMap.get(event));

            volatileMap.remove(event);

            result = true;

        } else if (stickyMap.containsKey(event)) {
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

                    switch(obj.getType()) {

                        case TCONST.SUBGRAPH:

                            mTutor.getSceneGraph().post(this, TCONST.SUBGRAPH_CALL, nodeName);
                            break;

                        case TCONST.MODULE:

                            // Disallow module "calls"
                            Log.e(TAG, "MODULE Behaviors are not supported");
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
        return "Activity_Selector";
    }


    // IEventSource Interface END
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

    // publish component state data - EBD
    //************************************************************************
    //************************************************************************





    // *** Serialization




    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        Log.d(TAG, "Loader iteration");

        // Note we load in the TClass as we need to use the tutor classMap to permit
        // instantiation of type_audio objects
        //
        JSON_Helper.parseSelf(jsonObj, this, classMap, scope);

        // de-serialize state
        //
        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        activeSkill = prefs.getString(TCONST.SKILL_SELECTED, TCONST.SKILL_STORIES);

        validateRootVectors();

        writingTutorID = prefs.getString(TCONST.SKILL_WRITING, rootSkillWrite);
        storiesTutorID = prefs.getString(TCONST.SKILL_STORIES, rootSkillStories);
        mathTutorID    = prefs.getString(TCONST.SKILL_MATH,    rootSkillMath);
        shapesTutorID  = prefs.getString(TCONST.SKILL_SHAPES,  rootSkillShapes);

        generateTransitionVectors();

        validateTables();
    }

    private void generateTransitionVectors() {

        writingVector = (CAt_Data) writeTransitions.get(writingTutorID);
        storiesVector = (CAt_Data) storyTransitions.get(storiesTutorID);
        mathVector    = (CAt_Data) mathTransitions.get(mathTutorID);
        shapesVector  = (CAt_Data) shapeTransitions.get(shapesTutorID);
    }


    private String validateMap(HashMap map, String key) {

        String result = "";

        if(!map.containsKey(key)) {

            result = "\'" + key + "\'";
        }

        return result;
    }

    private String validateVector(HashMap map, String key, String field) {

        String outcome;

        outcome = validateMap(map, key);

        if(!outcome.equals("")) {

            outcome = field + outcome;
        }

        return outcome;
    }


    private String validateVectors(HashMap map, CAt_Data object) {

        String result = "";
        String outcome;

        result = result + validateVector(map, object.tutor_id, " - tutor_id:");
        result = result + validateVector(map, object.easier, " - easier:");
        result = result + validateVector(map, object.harder, " - harder:");
        result = result + validateVector(map, object.same, " - same:");
        result = result + validateVector(map, object.next, " - next:");

        return result;
    }


    private void validateRootVectors() {

        String outcome;

        outcome = validateMap(writeTransitions, rootSkillWrite  );
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillWrite : nomatch");
        }

        outcome = validateMap(storyTransitions, rootSkillStories);
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillStories : nomatch");
        }

        outcome = validateMap(mathTransitions,  rootSkillMath   );
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillMath : nomatch");
        }

        outcome = validateMap(shapeTransitions, rootSkillShapes );
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillShapes : nomatch");
        }

    }


    private void validateTable(HashMap transMap, HashMap initMap, String transtype, String initType) {

        String outcome = "";

        Iterator<?> tObjects = transMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            CAt_Data transition = ((CAt_Data)(entry.getValue()));

            // Validate there are transition entries for all links
            //
            outcome = "";
            outcome = validateVectors(transMap, transition);

            if(!outcome.equals("")) {
                Log.e("Map Fault ", transtype + entry.getKey() + " - MISSING LINKS: " + outcome);
            }

            // Validate there are initiators for all links
            //
            outcome = "";
            outcome = validateVectors(initMap, transition);

            if(!outcome.equals("")) {
                Log.e("Map Fault ", transtype + entry.getKey() + " - MISSING INITIATORS: " +  outcome);
            }

        }
    }

    private void validateTables() {

        validateTable(writeTransitions, writeInitiators, "writeTransition: ", "writeInitiator: ");
        validateTable(storyTransitions, storyInitiators, "storyTransition: ", "storyInitiator: ");
        validateTable(mathTransitions , mathInitiators , "mathTransition: ", "mathInitiator: ");
        validateTable(shapeTransitions, shapeInitiators, "shapeTransition: ", "shapeInitiator: ");
    }
}
