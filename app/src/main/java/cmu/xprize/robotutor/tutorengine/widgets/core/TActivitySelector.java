package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.comp_session.CActivitySelector;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
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
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.LAUNCH_EVENT;
import static cmu.xprize.comp_session.AS_CONST.VAR_DATASOURCE;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENT;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENTDATA;
import static cmu.xprize.robotutor.tutorengine.util.CClassMap2.classMap;
import static cmu.xprize.util.TCONST.PERFORMANCE_TAG;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

public class TActivitySelector extends CActivitySelector implements IBehaviorManager, ITutorSceneImpl, IDataSink, IEventSource, IPublisher, ITutorLogger {

    private static boolean          DEBUG_LANCHER = false;
    public  static String           DEBUG_TUTORID = "";

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;
    private TLangToggle             mLangButton;

    private TTextView               SversionText;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private String      activeSkill = AS_CONST.SELECT_NONE;
    private String      activeTutor = "";
    private String      nextTutor   = "";
    private String      rootTutor;
    private boolean     askButtonsEnabled = false;

    private HashMap<String, CAt_Data> transitionMap;

    private String      writingTutorID;
    private String      storiesTutorID;
    private String      mathTutorID;
    private String      shapesTutorID;

    private CAt_Data    writingVector = null;
    private CAt_Data    storiesVector = null;
    private CAt_Data    mathVector    = null;
    private CAt_Data    shapesVector  = null;

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
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
        SversionText   = (TTextView)findViewById(R.id.StutorVersion);

        SaskActivity.setButtonController(this);
        SdebugActivity.setButtonController(this);

        SversionText.setText(RoboTutor.VERSION_RT);
    }

    @Override
    public void onCreate() {

        // Set the debug launcher based on the variant built
        //
        if (BuildConfig.SHOW_DEBUGLAUNCHER) {
            DEBUG_LANCHER = true;
        }

        mLangButton = (TLangToggle)findViewById(R.id.SlangToggle);
        mLangButton.setTransformationMethod(null);

        // Hide the language toggle on the release builds
        // TODO : Use build Variant to ensure release configurations
        //
        if(!BuildConfig.LANGUAGE_SWITCH) {

            mLangButton.setVisibility(INVISIBLE);
            requestLayout();
        }
    }

    @Override
    public void onDestroy() {

        // Ensure the queue is teminated and flushed
        //
        super.onDestroy();
        mTutorScene.onDestroy();

        // Allow the ask component to relesase drawable resources.
        //
        SaskActivity.onDestroy();
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

                    activeTutor   = writingTutorID;
                    transitionMap = writeTransitions;
                    break;

                case AS_CONST.SELECT_STORIES:

                    activeTutor   = storiesTutorID;
                    transitionMap = storyTransitions;
                    break;

                case AS_CONST.SELECT_MATH:

                    activeTutor   = mathTutorID;
                    transitionMap = mathTransitions;
                    break;

                case AS_CONST.SELECT_SHAPES:

                    activeTutor   = shapesTutorID;
                    transitionMap = shapeTransitions;
                    break;

            }

            SdebugActivity.initGrid(activeSkill, activeTutor, transitionMap);
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


    public void enableAskButtons(Boolean enable) {

        askButtonsEnabled = true;
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



    //************************************************************************
    //************************************************************************
    // IButtonController Interface START

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

    /** This allows us to update the current tutor for a given skill from the CDebugComponent
     *
      */
    @Override
    public void doDebugLaunchAction(String debugTutor) {

        publishValue(AS_CONST.VAR_BUT_BEHAVIOR, debugTutor);

        applyBehavior(AS_CONST.SELECT_DEBUGLAUNCH);
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


        // If we are in debug mode then there is a third selection phase where we are presented
        // the transition table for the active skill - The author can select a new target tutor
        // from any of the transition entries.
        //
        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DEBUG_SELECT)) {
            buttonid = processDebugSelectMode(buttonid);
        }

        // If we are in Assessment mode we have prompted the student to assess the difficulty of the
        // tutor they have just completed.
        // Difficulty selection
        //

        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DIFFICULTY_ASSESS)) {
            buttonid = processDifficultyAssessMode(buttonid);

        }

        // If on the Tutor Select (home) screen, or we have triggered a launch from the debug screen,
        // RoboTutor will go into an activity.
        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_TUTOR_SELECT) ||
           RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DEBUG_LAUNCH)) {
            processTutorSelectMode(buttonid);

        }
    }

    /**
     * Method for processing button press on the TUTOR_SELECT (home) screen
     * @param buttonid
     */
    private void processTutorSelectMode(String buttonid) {
        boolean     buttonFound = false;

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

                activeSkill   = AS_CONST.SELECT_WRITING;
                activeTutor   = writingTutorID;
                rootTutor     = rootSkillWrite;
                transitionMap = writeTransitions;
                buttonFound   = true;
                break;

            case AS_CONST.SELECT_STORIES:

                activeSkill   = AS_CONST.SELECT_STORIES;
                activeTutor   = storiesTutorID;
                rootTutor     = rootSkillStories;
                transitionMap = storyTransitions;
                buttonFound = true;

                break;

            case AS_CONST.SELECT_MATH:

                activeSkill   = AS_CONST.SELECT_MATH;
                activeTutor   = mathTutorID;
                rootTutor     = rootSkillMath;
                transitionMap = mathTransitions;
                buttonFound   = true;
                break;

            case AS_CONST.SELECT_SHAPES:

                activeSkill   = AS_CONST.SELECT_SHAPES;
                activeTutor   = shapesTutorID;
                rootTutor     = rootSkillShapes;
                transitionMap = shapeTransitions;
                buttonFound   = true;

                break;

        }

        if (buttonFound) {

            publishValue(TCONST.SKILL_SELECTED, activeSkill);
            publishValue(TCONST.TUTOR_SELECTED, activeTutor);
            publishValue(TCONST.SELECTOR_MODE, RoboTutor.SELECTOR_MODE);
        }

        if (buttonFound) {

            // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
            // reenable the ASK buttons if we don't execute the story_tutor
            //
            if (!BuildConfig.NO_ASR_APPS || (transitionMap != storyTransitions)) {

                CAt_Data tutor = (CAt_Data) transitionMap.get(activeTutor);

                // This is just to make sure we go somewhere if there is a bad link - which
                // there shuoldn't be :)
                //
                if (tutor == null) {
                    tutor = (CAt_Data) transitionMap.get(rootTutor);
                }

                // #Mod 330 Show TutorID in Banner in debug builds
                // DEBUG_TUTORID is used to communicate the active tutor to the Banner in DEBUG mode
                //
                if (BuildConfig.SHOW_TUTORVERSION) {
                    DEBUG_TUTORID = activeTutor;
                }

                // If we are using the debug selector and mode is not launching a tutor then
                // switch to debug view
                //
                if(DEBUG_LANCHER && RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_TUTOR_SELECT)) {

                    mTutor.post(TCONST.ENDTUTOR);
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;
                }
                else {
                    // Update the tutor id shown in the log stream
                    //
                    CLogManager.setTutor(activeTutor);

                    doLaunch(tutor.tutor_desc, TCONST.TUTOR_NATIVE, tutor.tutor_data);
                }

                // Serialize the new state
                // #Mod 329 language switch capability
                //
                SharedPreferences prefs = getStudentSharedPreferences();
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(TCONST.SKILL_SELECTED, activeSkill);
                editor.apply();
            } else
                SaskActivity.enableButtons(true);
        }
    }

    /**
     * Method for processing button press on the DIFFICULTY_ASSESS screen
     * @param buttonid
     * @return new buttonid, to be used for next screen
     */
    private String processDifficultyAssessMode(String buttonid) {
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

        boolean usePerformance = false;
        if(TCONST.CONSIDER_STUDENT_PERFORMANCE) {
            // returns true if performance-measuring is implemented for the current activity
            usePerformance = assessStudentPerformance();
        }

        switch (buttonid.toUpperCase()) {

            case AS_CONST.SELECT_CONTINUE:
                if(!usePerformance) {
                    if(TCONST.OVERRIDE_SELF_ASSESSMENT) {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                    } else {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                    }
                }

                mTutor.post(TCONST.ENDTUTOR);
                RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                break;

            case AS_CONST.SELECT_MAKE_HARDER:
                if(!usePerformance) {
                    if(TCONST.OVERRIDE_SELF_ASSESSMENT) {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                    } else {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).harder;
                    }
                }

                mTutor.post(TCONST.ENDTUTOR);
                RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                break;

            case AS_CONST.SELECT_MAKE_EASIER:
                if(!usePerformance) {
                    if(TCONST.OVERRIDE_SELF_ASSESSMENT) {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                    } else {
                        nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).easier;
                    }
                }

                mTutor.post(TCONST.ENDTUTOR);
                RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                break;

            case AS_CONST.SELECT_EXIT:
                if(!usePerformance) {
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).tutor_id;
                }

                mTutor.post(TCONST.FINISH);
                RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                break;

            // If user selects "Let robotutor decide" then use student model to decide how to adjust the
            // difficulty level.  We also flip mode to tutor_select to skip the tutor select phase and
            // let the model do the tutor selection.
            // At the moment default to continue to "next" link
            //
            case AS_CONST.SELECT_AUTO_DIFFICULTY:
                if(!usePerformance) {
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                }

                // just reselect the current skill and continue with next tutor
                // no skill selection phase
                buttonid = activeSkill;
                RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
                break;

            case AS_CONST.SELECT_REPEAT:
                // do this regardless of performance
                nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).tutor_id; // if the select "repeat", then it will be the same tutor
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
        // #Mod 329 language switch capability
        //
        SharedPreferences prefs = getStudentSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE);

        // only one will have been changed but update all
        //
        editor.putString(TCONST.SKILL_WRITING, writingTutorID);
        editor.putString(TCONST.SKILL_STORIES, storiesTutorID);
        editor.putString(TCONST.SKILL_MATH, mathTutorID);
        editor.putString(TCONST.SKILL_SHAPES, shapesTutorID);

        editor.apply();

        publishValue(TCONST.SKILL_SELECTED, nextTutor);
        publishValue(TCONST.SKILL_WRITING, writingTutorID);
        publishValue(TCONST.SKILL_STORIES, storiesTutorID);
        publishValue(TCONST.SKILL_MATH, mathTutorID);
        publishValue(TCONST.SKILL_SHAPES, shapesTutorID);
        publishValue(TCONST.SELECTOR_MODE, RoboTutor.SELECTOR_MODE);

        return buttonid;
    }

    /**
     * Method for processing button press on the DEBUG_SELECT screen
     * @param buttonid
     * @return new button id, to be selected for the DEBUG_LAUNCH screen
     */
    private String processDebugSelectMode(String buttonid) {
        // Update the active skill
        //
        switch (activeSkill) {

            case AS_CONST.SELECT_WRITING:

                writingTutorID = buttonid;
                break;

            case AS_CONST.SELECT_STORIES:

                storiesTutorID = buttonid;
                break;

            case AS_CONST.SELECT_MATH:

                mathTutorID = buttonid;
                break;

            case AS_CONST.SELECT_SHAPES:

                shapesTutorID = buttonid;
                break;
        }

        // just reselect the current skill and continue with next tutor
        // no skill selection phase
        buttonid = activeSkill;
        RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;

        // Serialize the new state
        // #Mod 329 language switch capability
        //
        SharedPreferences prefs = getStudentSharedPreferences();

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE);

        // only one will have been changed but update all
        //
        editor.putString(TCONST.SKILL_WRITING, writingTutorID);
        editor.putString(TCONST.SKILL_STORIES, storiesTutorID);
        editor.putString(TCONST.SKILL_MATH, mathTutorID);
        editor.putString(TCONST.SKILL_SHAPES, shapesTutorID);

        publishValue(TCONST.SKILL_SELECTED, activeSkill);
        publishValue(TCONST.SKILL_WRITING, writingTutorID);
        publishValue(TCONST.SKILL_STORIES, storiesTutorID);
        publishValue(TCONST.SKILL_MATH, mathTutorID);
        publishValue(TCONST.SKILL_SHAPES, shapesTutorID);
        publishValue(TCONST.SELECTOR_MODE, RoboTutor.SELECTOR_MODE);

        editor.apply();

        return buttonid;
    }

    private SharedPreferences getStudentSharedPreferences() {
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += mMediaManager.getLanguageFeature(mTutor);

        RoboTutor.logManager.postEvent_I(TAG, "Getting SharedPreferences: " + prefsName);
        return RoboTutor.ACTIVITY.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    /**
     * Checks if the last finished activity can be graded based on performance.
     * If so, updates the "nextTutor" based on student performance and returns true.
     * Otherwise, returns false.
     *
     * @return
     */
    private boolean assessStudentPerformance() {

        boolean usePerformance = false; // usePerformance will only be true if performance metrics (correct, incorrect)
        // are tracked for that activity

        String childScope = null;
        if(activeTutor.startsWith("bpop")) {
            childScope = "bubble_pop";
            usePerformance = true;
        } else if (activeTutor.startsWith("akira")) {
            childScope = "akira";
            usePerformance = true;
        } else if (activeTutor.startsWith("math")) {
            childScope = "math";
            usePerformance = true;
        }

        if(usePerformance) {
            TScope lastScope = TScope.root().getChildScope(childScope);
            CTutor lastTutor;
            if(lastScope != null) {
                lastTutor = lastScope.tutor();

                int correct = lastTutor.getScore();
                int attempts = lastTutor.getAttempts();
                double percent = 0;
                if (attempts > 0) {// don't divide by zero
                    percent = correct / (double) attempts;
                }
                Log.i(PERFORMANCE_TAG, "performance = " + correct + " / " + attempts);

                Log.i(PERFORMANCE_TAG, "activeTutor: " + activeTutor);
                Log.i(PERFORMANCE_TAG, "nextTutor: " + nextTutor);


                if (percent >= TCONST.HIGH_PERFORMANCE_THRESHOLD) {
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).harder;
                } else if (percent >= TCONST.MID_PERFORMANCE_THRESHOLD) {
                    // if user just quits...
                    // note that as long as the student does "MIN_ATTEMPTS_TO_GRADE",
                    // their percentage will be graded the same as if they had completed the entire activity
                    // REVIEW what is the proper number?
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                } else {
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).easier;
                }

                // if player doesn't acheive a minimum number of attempts, don't use performance data
                int attemptsExpected = TCONST.MIN_ATTEMPTS_TO_GRADE;
                if (attempts < attemptsExpected) {
                    nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
                    usePerformance = false;
                }

            } else {
                usePerformance = false; // in case of unexpected error
            }

        } else {
            nextTutor = ((CAt_Data) transitionMap.get(activeTutor)).next;
        }
        return usePerformance;
    }


    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void doLaunch(String intent, String intentData, String dataSource) {

        RoboTutor.SELECTOR_MODE = TCONST.FTR_DIFFICULTY_ASSESS;

        // update the response variable  "<Sresponse>.value"

        publishValue(VAR_INTENT, intent);
        publishValue(VAR_INTENTDATA, intentData);
        publishValue(VAR_DATASOURCE, dataSource);

        applyBehavior(LAUNCH_EVENT);
    }


    // IButtonController Interface END
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

        RoboTutor.logManager.postTutorState(TUTOR_STATE_MSG, "target#activity_selector," + logData + builder.toString());
    }

    // ITutorLogger - End
    //***********************************************************




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
        return "Activity_Selector";
    }


    // IEventSource Interface END
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // IPublish component state data - START

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





    // *** Serialization




    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        // Log.d(TAG, "Loader iteration");


        // Note we load in the TClass as we need to use the tutor classMap to permit
        // instantiation of type_audio objects
        //
        JSON_Helper.parseSelf(jsonObj, this, classMap, scope);

        // de-serialize state
        // #Mod 329 language switch capability
        //
        SharedPreferences prefs = getStudentSharedPreferences();

        if(prefs.getAll().entrySet().isEmpty())
            RoboTutor.logManager.postEvent_W(TAG, "SharedPreferences is empty");

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            RoboTutor.logManager.postEvent_D(TAG, "SharedPreferences: " + entry.getKey() + " -- " + entry.getValue().toString());
        }

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


    private void validateTable(HashMap transMap, String transtype) {

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

            // Validate there is a Tutor Variant defined for the transition vector
            //
            outcome = "";
            outcome = validateVector(CTutorEngine.tutorVariants, transition.tutor_desc, " - tutor_desc:");

            if(!outcome.equals("")) {
                Log.e("Map Fault ", transtype + entry.getKey() + " - MISSING VARIANT: " +  outcome);
            }

        }
    }

    private void validateTables() {

        validateTable(writeTransitions,  "writeTransition: ");
        validateTable(storyTransitions,  "storyTransition: ");
        validateTable(mathTransitions ,  "mathTransition: ");
        validateTable(shapeTransitions,  "shapeTransition: " );
    }
}
