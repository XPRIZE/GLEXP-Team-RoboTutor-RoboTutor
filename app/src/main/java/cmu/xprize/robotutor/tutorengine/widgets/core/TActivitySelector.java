package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformancePromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PlacementPromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
import cmu.xprize.robotutor.tutorengine.util.StudentDataModel;
import cmu.xprize.robotutor.tutorengine.util.TransitionMatrixModel;
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
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.VAR_TUTOR_ID;
import static cmu.xprize.comp_session.AS_CONST.VAR_DATASOURCE;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENT;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENTDATA;
import static cmu.xprize.util.TCONST.LAST_TUTOR;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_AKIRA;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_ASM;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_BPOP;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_TAP_COUNT;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

public class TActivitySelector extends CActivitySelector implements ITutorSceneImpl, IDataSink, IEventSource, IPublisher, ITutorLogger {


    // NEW_MENU TODO:
    // NEW_MENU (5) make button look tappable
    // NEW_MENU (6) variability (e.g. next tutor, etc)
    // NEW_MENU (7)

    private static boolean          DEBUG_LANCHER = false;
    public  static String           DEBUG_TUTORID = "";

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;
    private TLangToggle             mLangButton;

    private TTextView               SversionText;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    // FOR_MOM (goals)
    // goal 1: get repeat working properly...
    // option 1: story -> lit -> story -> math... show N and N+1
    // option 2: something more complicated?
    // goal 3: make double screen go away
    // goal 4: why is repeat button failing in dev branch? (requires a merge)

    private String      activeSkill = AS_CONST.SELECT_NONE;
    private boolean     askButtonsEnabled = false;


    private boolean IS_USING_PLACEMENT; // YYY true at first, then turns to false after a failure

    private TransitionMatrixModel matrix; // now holds the transition map things...
    private StudentDataModel studentModel; // holds student location and such...

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();


    final private String  TAG = "TActivitySelector";
    private int placementIndex;
    private boolean needsToCalculateNextTutor = false; // initialize as false... only set after tutor launched


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
            requestLayout(); // https://stackoverflow.com/questions/13856180/usage-of-forcelayout-requestlayout-and-invalidate
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


    // REMOVE_SA... perhaps call it here? how is TActivitySelector created?

    /**
     * Initialize Layout in Tutor Select mode...
     * Called from AG, don't delete.
     */
    public void setTutorSelectLayout() {

        // English version still defined in JSON...
        if (CTutorEngine.language.equals(TCONST.LANG_SW)) {
            initializeActiveLayout();
        }

        SaskActivity.setVisibility(VISIBLE);
        SdebugActivity.setVisibility(GONE);

        // REMOVE_SA put it here
        if (RoboTutor.MUST_CALCULATE_NEXT_TUTOR) {
            // FOR_MOM (4.1) simplify this... even better, can it go somewhere else???
            // what happens after a tutor finishes???
            // - bpop: NEXTSCENE
            // -akira "gotoNextScene"
            // - add_subtract "NEXTSCENE"...
            // - story_reading "NEXTSCENE"
            // - word_copy "NEXTSCENE"
            adjustPositionFromPreviousPerformance();
        }

        CAt_Data[] nextTutors = new CAt_Data[3];
        nextTutors[0] = (CAt_Data) matrix.writeTransitions.get(studentModel.getWritingTutorID());
        nextTutors[1] = (CAt_Data) matrix.storyTransitions.get(studentModel.getStoryTutorID());
        nextTutors[2] = (CAt_Data) matrix.mathTransitions.get(studentModel.getMathTutorID());
        SaskActivity.initializeButtonsAndSetButtonImages(_activeLayout, nextTutors);
        //SaskActivity.setButtonImages(); // NEW_MENU (6) can possibly be next.next
    }

    /**
     * Initialize Layout in Debug Mode...
     * Called from AG, don't delete.
     */
    public void setDebugLayout() {
        SaskActivity.setVisibility(GONE);
        SdebugActivity.setVisibility(VISIBLE);

        // Init the skill pointers
        //
        String activeTutorId = "";
        HashMap transitionMap = null;
        String rootTutor = "";

        // FOR_MOM (4.1) look up activeSkill every time
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                activeTutorId = studentModel.getWritingTutorID();
                transitionMap = matrix.writeTransitions;
                rootTutor = matrix.rootSkillWrite;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                activeTutorId = studentModel.getStoryTutorID();
                transitionMap = matrix.storyTransitions;
                rootTutor = matrix.rootSkillStories;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                activeTutorId = studentModel.getMathTutorID();
                transitionMap = matrix.mathTransitions;
                rootTutor = matrix.rootSkillMath;
                break;

        }

        SdebugActivity.initGrid(activeSkill, activeTutorId, transitionMap, rootTutor); // √
    }

    /**
     * moving this from JSON... because we need to make it more dynamic...
     * how will this affect the English version???
     */
    private void initializeActiveLayout() {

        _activeLayout = new CAsk_Data();
        //activeLayout.layoutID =
        _activeLayout.items = new CAskElement[5];

        _activeLayout.items[0] =  new CAskElement();
        _activeLayout.items[0].componentID = "SbuttonOption1";
        _activeLayout.items[0].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING; // FOR_MOM (2.0)
        _activeLayout.items[0].prompt = "reading and writing";
        _activeLayout.items[0].help = "Tap here for reading and writing";

        _activeLayout.items[1] =  new CAskElement();
        _activeLayout.items[1].componentID = "SbuttonOption2";
        _activeLayout.items[1].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;        // FOR_MOM (2.0)
        _activeLayout.items[1].prompt = "stories";
        _activeLayout.items[1].help = "Tap here for a story";

        _activeLayout.items[2] =  new CAskElement();
        _activeLayout.items[2].componentID = "SbuttonOption3";
        _activeLayout.items[2].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;        // FOR_MOM (2.0)
        _activeLayout.items[2].prompt = "numbers and math";
        _activeLayout.items[2].help = "Tap here for numbers and math";

        _activeLayout.items[3] =  new CAskElement();
        _activeLayout.items[3].componentID = "SbuttonRepeat";
        _activeLayout.items[3].behavior = AS_CONST.SELECT_REPEAT;        // FOR_MOM (2.0)
        _activeLayout.items[3].prompt = "lets do it again";
        _activeLayout.items[3].help = "tap here to do the same thing again";

        _activeLayout.items[4] =  new CAskElement();
        _activeLayout.items[4].componentID = "SbuttonExit";
        _activeLayout.items[4].behavior = AS_CONST.SELECT_EXIT;        // FOR_MOM (2.0)
        _activeLayout.items[4].prompt = "I want to stop using RoboTutor";
        _activeLayout.items[4].help = "tap here to stop using robotutor";

    }


    /**
     * Replaces animator graph so we can trace variables
     */
    public void setAllStickyBehavior() {
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_BEHAVIOR, AS_CONST.QUEUEMAP_KEYS.BUTTON_DESCRIPTION);
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_COMPLETE, "SET_HESITATION_FEEDBACK");
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_BEHAVIOR, "CLEAR_HESITATION_BEHAVIOR");
        //setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.LAUNCH_EVENT, "LAUNCH_BEHAVIOR"); // (2.7) collapsed into one function

        // Home screen button behavior...
        // FOR_MOM... clean this ish up
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR); // FOR_MOM (2.2)
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR); // FOR_MOM (2.2)
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_MATH, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR); // FOR_MOM (2.2)
        setStickyBehavior(AS_CONST.SELECT_REPEAT, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR); // FOR_MOM (2.2)
        setStickyBehavior(AS_CONST.SELECT_EXIT, AS_CONST.QUEUEMAP_KEYS.EXIT_BUTTON_BEHAVIOR);
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

                applyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_BEHAVIOR);

                _describeIndex++;
            } else {

                applyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_COMPLETE);
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

            applyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_BEHAVIOR);

            for (CAskElement element : _activeLayout.items) {

                if (element.componentID.equals(actionid)) {

                    publishValue(AS_CONST.VAR_BUTTONID, element.componentID);
                    publishValue(AS_CONST.VAR_BUT_BEHAVIOR, element.behavior);
                    publishValue(AS_CONST.VAR_HELP_AUDIO, element.help);
                    publishValue(AS_CONST.VAR_PROMPT_AUDIO, element.prompt);

                    applyBehavior(element.behavior); // FOR_MOM (2.1) this is where the behavior gets applied... thru the stickyMap
                    // applyBehaviorNode(element.secondary_behavior...)
                }
            }
        }
    }

    /** This allows us to update the current tutor for a given skill from the CDebugComponent
     *
     */
    @Override
    public void doDebugLaunchAction(String debugTutor) {

        // RoboTutor.SELECTOR_MODE == "DEBUG_MENU"
        doButtonBehavior(debugTutor);

        String buttonBehavior;
        // Update the active skill
        //

        String buttonid = debugTutor;
        String writingTutorID = null, storiesTutorID = null, mathTutorID = null;
        // FOR_MOM (4.1) look up activeSkill every time
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                writingTutorID = buttonid; // update √√√
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                storiesTutorID = buttonid; // update √√√
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                mathTutorID = buttonid; // update √√√
                break;
        }

        // just reselect the current skill and continue with next tutor
        // no skill selection phase
        buttonid = activeSkill; // √
        RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;

        // DATA_MODEL will it crash if we try to edit it multiple times???
        if (writingTutorID != null) studentModel.updateWritingTutorID(writingTutorID);
        if (storiesTutorID != null) studentModel.updateStoryTutorID(storiesTutorID);
        if (mathTutorID != null)    studentModel.updateMathTutorID(mathTutorID);

        buttonBehavior = buttonid;
        // RoboTutor.SELECTOR_MODE == "DEBUG_LAUNCH"

        processTutorSelectMode(buttonBehavior);
    }

    /**
     * Button clicks may come from either the skill selector ASK component or the Difficulty
     * selector ASK component.
     *
     * @param buttonBehavior
     */
    @Override
    public void doButtonBehavior(String buttonBehavior) {

        // RoboTutor.SELECTOR_MODE = "FTR_TUTOR_SELECT"
        Log.d(TAG, "Button Selected: " + buttonBehavior);



        processTutorSelectMode(buttonBehavior);

    }

    /** This allows us to update the current tutor for a given skill from the CDebugComponent
     *
     * Launches from a special debug menu
     */
    @Override
    public void doDebugTagLaunchAction(String tag) {
        // somehow this... goes all the way to "doTaggedButtonBehavior"... dammit.
        Log.d(TAG, "Debug Button with tag: " + tag);

        RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;

        String intent;
        String file;

        intent = tag;

        // specify the file name we're debugging with
        switch (tag) {
            case TCONST.TAG_DEBUG_TAP_COUNT:
                file = ROBO_DEBUG_FILE_TAP_COUNT;
                break;

            case TCONST.TAG_DEBUG_AKIRA:
                file = ROBO_DEBUG_FILE_AKIRA;
                break;

            case TCONST.TAG_DEBUG_ASM:
                file = ROBO_DEBUG_FILE_ASM;
                break;

            default:
                file = ROBO_DEBUG_FILE_BPOP;
        }

        doLaunch(intent, TCONST.TUTOR_NATIVE, TCONST.DEBUG_FILE_PREFIX + file, "DEBUGGER");
    }

    /**
     * FOR_MOM (2.5)
     * Method for processing button press on the TUTOR_SELECT (home) screen
     * @param buttonid
     */
    private void processTutorSelectMode(String buttonid) {

        // check SharedPreferences
        final SharedPreferences prefs = getStudentSharedPreferences();

        String activeTutorId = "";
        HashMap transitionMap = null;
        String rootTutor = "";

        boolean     buttonFound = false;

        // 2. finish RoboTutor or the ActivitySelector, if necessary
        if(buttonid.toUpperCase().equals(AS_CONST.SELECT_EXIT)) {
            // if EXIT, we finish the app
            mTutor.post(TCONST.FINISH);
        }

        // First check if it is a skill selection button =
        // NEW_MENU (7) if it's a repeat... don't change activeTutorId...
        switch (buttonid.toUpperCase()) {

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                activeSkill   = AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING; // √
                activeTutorId = studentModel.getWritingTutorID();
                rootTutor     = matrix.rootSkillWrite;
                transitionMap = matrix.writeTransitions;
                buttonFound   = true;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                activeSkill   = AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES; // √
                activeTutorId = studentModel.getStoryTutorID();
                rootTutor     = matrix.rootSkillStories;
                transitionMap = matrix.storyTransitions;
                buttonFound = true;

                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                activeSkill   = AS_CONST.BEHAVIOR_KEYS.SELECT_MATH; // √
                activeTutorId = studentModel.getMathTutorID();
                rootTutor     = matrix.rootSkillMath;
                transitionMap = matrix.mathTransitions;
                buttonFound   = true;
                break;
        }


        // FOR_MOM (4.1) save activeSkill after setting
        if (buttonid.equals(AS_CONST.SELECT_REPEAT)) {
            buttonid = activeSkill; // √
            // ytf is this different... sometimes STORIES_SELECTED...

            String lastTutor = prefs.getString(LAST_TUTOR, null);
            if (lastTutor != null) { // for when it's the first time...s
                activeTutorId = lastTutor;
            }

        }

        if (buttonFound) {

            // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
            // reenable the ASK buttons if we don't execute the story_tutor
            //
            if (!BuildConfig.NO_ASR_APPS || (transitionMap != matrix.storyTransitions)) {

                // the next tutor to be launched
                CAt_Data tutorToLaunch = (CAt_Data) transitionMap.get(activeTutorId);

                // This is just to make sure we go somewhere if there is a bad link - which
                // there shuoldn't be :)
                //
                if (tutorToLaunch == null) {
                    tutorToLaunch = (CAt_Data) transitionMap.get(rootTutor);
                }

                // #Mod 330 Show TutorID in Banner in debug builds
                // DEBUG_TUTORID is used to communicate the active tutor to the Banner in DEBUG mode
                //
                if (BuildConfig.SHOW_TUTORVERSION) {
                    DEBUG_TUTORID = activeTutorId;
                }

                // This is where we go to the debug view...
                //
                // REMOVE_SA something going on here... "ENDTUTOR" ends the Activity Selector
                if(DEBUG_LANCHER && RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_TUTOR_SELECT)) {

                    mTutor.post(TCONST.ENDTUTOR); // ends current tutor???
                    RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;
                }
                else {
                    doTutorLaunchWithVideosAndStuff(activeTutorId, tutorToLaunch, prefs);
                }

                SharedPreferences.Editor editor = prefs.edit();

                // Serialize the new state
                // #Mod 329 language switch capability
                //
                editor.putString(TCONST.SKILL_SELECTED, activeSkill); // √√√ √√√
                editor.apply();

            } else
                SaskActivity.enableButtons(true);
        }
    }

    /**
     * A big and cumbersome method that will launch a tutor eventually
     * @param prefs
     */
    private void doTutorLaunchWithVideosAndStuff(String activeTutorId, CAt_Data tutorToLaunch, final SharedPreferences prefs) {
        // Update the tutor id shown in the log stream

        if(BuildConfig.SHOW_DEMO_VIDS && false) {


            String whichActivityIsNext = parseActiveTutorForTutorName(activeTutorId);
            final String activityPreferenceKey = whichActivityIsNext + "_TIMES_PLAYED";

            // bpop, write, akira, story, math, etc
            final int timesPlayedActivity = prefs.getInt(activityPreferenceKey, 0); // i = default value
            boolean playDemoVid = timesPlayedActivity < 1; // only play video once

            String pathToFile = getTutorInstructionalVideoPath(activeTutorId, whichActivityIsNext);

            if(playDemoVid && pathToFile != null) {

                playTutorDemoVid(activeTutorId, tutorToLaunch, prefs, activityPreferenceKey, timesPlayedActivity, pathToFile);

            } else {

                CLogManager.setTutor(activeTutorId);

                doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id);

            }
        } else {

            CLogManager.setTutor(activeTutorId);

            doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id);
        }
    }

    /**
     * A big and cumbersome method that plays the tutor video...
     *
     * @param prefs
     * @param activityPreferenceKey
     * @param timesPlayedActivity
     * @param pathToFile
     */
    private void playTutorDemoVid(final String activeTutorId, final CAt_Data tutorToLaunch, final SharedPreferences prefs, final String activityPreferenceKey, final int timesPlayedActivity, String pathToFile) {
        // load SurfaceView to hold the video
        final SurfaceView fullscreenView = (SurfaceView) findViewById(R.id.SvideoSurface);
        fullscreenView.setVisibility(View.VISIBLE);
        final TLangToggle langToggle = (TLangToggle) findViewById(R.id.SlangToggle);
        langToggle.setVisibility(View.INVISIBLE); // prevent user from changing language

        // Here is where we will play the video
        final MediaPlayer mp = new MediaPlayer();

        try {
            // TODO fix this exception handling
            mp.setDataSource(pathToFile);
            mp.setDisplay(fullscreenView.getHolder());
            mp.prepare();

            mp.start();
        } catch (IOException e) {
            e.printStackTrace();

            // if an error occurs with media player, we don't want to freeze the app completely
            CLogManager.setTutor(activeTutorId);

            doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id);
        }


        //MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), videoId);
        //mediaPlayer.setDisplay(fullscreenView.getHolder());
        //mediaPlayer.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
                fullscreenView.setVisibility(View.INVISIBLE);
                langToggle.setVisibility(View.VISIBLE); // prevent from changing language

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(activityPreferenceKey, timesPlayedActivity + 1); // increment to let them know we watched the video
                editor.apply();


                CLogManager.setTutor(activeTutorId);

                doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id);
            }
        });
    }

    /**
     * Parses tutor for its core tutor name
     * e.g. bpop.num:s2n_say_show_mc --> bpop
     * akira:10_2 --> akira
     *
     * @param tutor
     * @return
     */
    private String parseActiveTutorForTutorName(String tutor) {

        // deals with format of tutor names....
        // math:10, read.echo:1, read.hear:2, bpop.ltr.uc:2, etc
        String whichActivityIsNext = tutor;
        int colonIndex = tutor.indexOf(':');
        if(colonIndex > 0) {
            whichActivityIsNext = tutor.substring(0, colonIndex);
        }
        int periodIndex = whichActivityIsNext.indexOf('.');
        if(periodIndex > 0) {
            whichActivityIsNext = whichActivityIsNext.substring(0, periodIndex);
        }

        return whichActivityIsNext;
    }


    /**
     * Gets the video resource to play based on tutor.
     *
     * @param tutor
     * @return
     */
    private String getTutorInstructionalVideoPath(String activeTutorId, String tutor) {

        String PATH_TO_FILE = TCONST.ROBOTUTOR_ASSETS + "/" + "video" + "/";

        // note that this was initially done w/ a "substring" check, but each tutor has a different
        // naming format e.g. math:10 vs. story.hear:1 vs. story.echo:1
        if (activeTutorId.startsWith("bpop")) {
            PATH_TO_FILE += "bpop_demo.mp4";
        } else if (activeTutorId.startsWith("akira")) {
            PATH_TO_FILE += "akira_demo.mp4";
        } else if (activeTutorId.startsWith("math")) {
            PATH_TO_FILE += "asm_demo.mp4";
        } else if (activeTutorId.startsWith("write")) {
            PATH_TO_FILE += "write_demo.mp4";
        } else if (activeTutorId.startsWith("story.read") || activeTutorId.startsWith("story.echo")) {
            PATH_TO_FILE += "read_demo.mp4";
        } else if (activeTutorId.startsWith("numscale") || activeTutorId.startsWith("num.scale")) {
            PATH_TO_FILE += "numscale_demo.mp4";
        } else if (activeTutorId.startsWith("countingx")) {
            PATH_TO_FILE += "countingx_demo.mp4";
        } else {
            return null;
        }

        return PATH_TO_FILE;

    }

    /**
     * NEW_MENU (4) REMOVE_SA this should be moved into the FTR_TUTOR_SELECT screen
     *
     * Adjust the student's position in matrix based on their last performance
     *
     * @param buttonid
     * @return new buttonid, to be used for next screen
     */
    private void adjustPositionFromPreviousPerformance() {

        boolean useMathPlacement = false;
        boolean useWritingPlacement = false;


        SharedPreferences prefs = getStudentSharedPreferences();

        // REMOVE_SA Init the skill pointers...
        // REMOVE_SA ??? figure this out
        //
        String activeTutorId = "";
        HashMap transitionMap = null;
        // FOR_MOM (4.1) look up activeSkill every time
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                activeTutorId = studentModel.getWritingTutorID();
                transitionMap = matrix.writeTransitions;
                useWritingPlacement = prefs.getBoolean("WRITING_PLACEMENT", false);
                placementIndex = prefs.getInt("WRITING_PLACEMENT_INDEX", 0);

                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                activeTutorId = studentModel.getStoryTutorID();
                transitionMap = matrix.storyTransitions;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                activeTutorId = studentModel.getMathTutorID();
                transitionMap = matrix.mathTransitions;
                useMathPlacement = prefs.getBoolean("MATH_PLACEMENT", false);
                placementIndex = prefs.getInt("MATH_PLACEMENT_INDEX", 0);
                break;

        }


        // FOR_MOM (4.0) important part...
        String nextTutor = selectNextTutor(activeTutorId, activeSkill, useWritingPlacement, useMathPlacement, prefs, transitionMap); // √

        RoboTutor.logManager.postEvent_I(TCONST.PLACEMENT_TAG, "nextTutor = " + nextTutor);


        // 3. Set SELECTOR_MODE
        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

        // Update the active skill
        //
        // FOR_MOM (4.1) look up activeSkill every time
        String writingTutorID = null, storiesTutorID = null, mathTutorID = null;
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                writingTutorID = nextTutor;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                storiesTutorID = nextTutor;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                mathTutorID = nextTutor;
                break;
        }

        // Serialize the new state
        // #Mod 329 language switch capability
        //
        SharedPreferences.Editor editor = prefs.edit();

        //editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE); // √√√ √√√
        //Log.wtf("REPEAT_STUFF", "(difficultySelectMode) setting SKILL_SELECTED... " + AS_CONST.SELECT_NONE);

        // only one will have been changed but update all
        //
        if (writingTutorID != null) editor.putString(TCONST.SKILL_WRITING, writingTutorID);
        if (storiesTutorID != null) editor.putString(TCONST.SKILL_STORIES, storiesTutorID);
        if (mathTutorID != null) editor.putString(TCONST.SKILL_MATH, mathTutorID);
        editor.putString(TCONST.LAST_TUTOR, activeTutorId);

        editor.apply();

        RoboTutor.MUST_CALCULATE_NEXT_TUTOR = false;
    }

    /**
     * select Next Tutor
     * @param buttonid
     */
    private String selectNextTutor(String activeTutorId, String activeSkill, boolean useWritingPlacement, boolean useMathPlacement, SharedPreferences prefs, HashMap transitionMap) {
        RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format(Locale.US, "selectNextTutor, w=%s, m=%s",
                String.valueOf(useWritingPlacement),
                String.valueOf(useMathPlacement)));
        // 1. pick the next tutor
        // YYY if placement, we will go by different rules
        PromotionRules rules;
        if(useWritingPlacement || useMathPlacement) {
            rules = new PlacementPromotionRules();
        } else {
            rules = new PerformancePromotionRules();
        }

        PerformanceData performance = new PerformanceData();
        performance.setActivityType(activeTutorId);
        // FOR_MOM (4.1) look up activeSkill every time
        performance.setActiveSkill(activeSkill);

        // need to get the previous tutor and all that jazz...
        String childScope = getChildScope(activeTutorId);

        // get tutor data from last tutor the user played
        TScope lastScope = TScope.root().getChildScope(childScope);
        CTutor lastTutor;
        if(lastScope != null) {
            lastTutor = lastScope.tutor();
            performance.setNumberCorrect(lastTutor.getScore());
            performance.setNumberWrong(lastTutor.getIncorrect());
            performance.setNumberAttempts(lastTutor.getAttempts());
            performance.setTotalNumberQuestions(lastTutor.getTotalQuestions());
        }

        PromotionRules.SelectedActivity selectedActivity = rules.selectActivityByPerformance(performance);
        Log.d(TAG, "PerformancePromotionRules result: " + selectedActivity);

        // YYY use placement logic
        String nextTutor;
        if (useWritingPlacement || useMathPlacement) {
            nextTutor = getNextPlacementTutor(activeTutorId, useMathPlacement, prefs, selectedActivity, transitionMap);

        } else {
            nextTutor = getNextPromotionTutor(activeTutorId, selectedActivity, transitionMap);
        }
        return nextTutor;
    }

    /**
     * get next tutor using Promotion Logic
     * @param selectedActivity
     * @return
     */
    private String getNextPromotionTutor(String activeTutorId, PromotionRules.SelectedActivity selectedActivity, HashMap<String, CAt_Data> transitionMap) {
        // this is
        CAt_Data transitionData = transitionMap.get(activeTutorId);
        switch (selectedActivity) {
            case NEXT:
                return transitionData.next;

            case SAME:
                return transitionData.same;

            case OLD_EASIER:
                return transitionData.easier;

            case OLD_HARDER:
                return transitionData.harder;

            case PREVIOUS:
                // XXX FIXME nextTutor = transitionData.previous;
                // for now... do the super hacky way of iterating through the whole map until we find one who refers to "activeTutorId" via "next"
                String tempNextTutor = null;
                for (Map.Entry<String, CAt_Data> e : transitionMap.entrySet()) {
                    Log.d("TRANSITION_MAP", e.getValue().toString());
                    CAt_Data value = e.getValue();
                    if (value.next.equals(activeTutorId)) {
                        tempNextTutor = e.getKey();
                    }
                }
                // no "next" reference, probably means it's the first item
                if (tempNextTutor == null) {
                    tempNextTutor = activeTutorId;
                }
                return tempNextTutor;
            // XXX FIXME end super hacky code


            case DOUBLE_NEXT:
                // XXX FIXME nextTutor = transitionData.double_next;
                // for now... do the slightly less hacky way of doing "next" of "next"
                String notNextTutor = transitionData.next;

                CAt_Data nextTransitionData = transitionMap.get(notNextTutor);
                return nextTransitionData.next;
            // XXX FIXME end slightly less hacky code
            // XXX note that these will not show up in the debugger graph

            // this shouldn't happen...
            default:
                return transitionData.next;
        }
    }

    /**
     * get next tutor using Placement Logic
     * @param useMathPlacement
     * @param prefs
     * @param selectedActivity
     * @return
     */
    private String getNextPlacementTutor(String activeTutorId, boolean useMathPlacement, SharedPreferences prefs, PromotionRules.SelectedActivity selectedActivity, HashMap<String, CAt_Data> transitionMap) {
        RoboTutor.logManager.postEvent_V(TCONST.PLACEMENT_TAG, "using placement logic");

        String placementKey;
        String placementIndexKey;

        String nextTutor;

        switch(selectedActivity) {

            /// YYY it might be better to keep the placement tutors in a map instead of in an array
            /// YYY logic might be more symmetrical
            /// YYY if (placement) {placementMap.get(activeTutorId)} else {transitionMap.get(activeTutorId)}
            // NEXT is equivalent to passing the placement test and moving to next placement test

            case NEXT:
                // pass to next
                //
                if(useMathPlacement) {
                    int mathPlacementIndex = placementIndex;


                    if (mathPlacementIndex == matrix.mathPlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = matrix.mathPlacement[mathPlacementIndex];
                        // update our preferences to exit PLACEMENT mode
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("MATH_PLACEMENT", false);
                        editor.remove("MATH_PLACEMENT_INDEX");
                        editor.apply();

                        return lastPlacementTest.fail;
                    } else {
                        mathPlacementIndex++; // passing means incrementing by one
                        CPlacementTest_Tutor nextPlacementTest = matrix.mathPlacement[mathPlacementIndex];

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("MATH_PLACEMENT_INDEX", mathPlacementIndex);
                        editor.apply();

                        return nextPlacementTest.tutor; // go to beginning of last level
                    }
                }
                // writingPlacement is only other option
                else {
                    int writingPlacementIndex = placementIndex;

                    if (writingPlacementIndex == matrix.writePlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = matrix.writePlacement[writingPlacementIndex];
                        // update our preferences to exit PLACEMENT mode
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("WRITING_PLACEMENT", false);
                        editor.remove("WRITING_PLACEMENT_INDEX");
                        editor.apply();

                        return lastPlacementTest.fail; // go to beginning of last level
                    } else {
                        writingPlacementIndex++; // passing means incrementing by one
                        CPlacementTest_Tutor nextPlacementTest = matrix.writePlacement[writingPlacementIndex];

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("WRITING_PLACEMENT_INDEX", writingPlacementIndex);
                        editor.apply();

                        return nextPlacementTest.tutor;
                    }

                }


            // SAME occurs when the student doesn't finish and we don't have enough information
            case SAME:
                // play again
                // do nothing
                CAt_Data transitionData = transitionMap.get(activeTutorId);
                return transitionData.same;


            // PREVIOUS is equivalent to failing the placement test and going to first activity in level
            case PREVIOUS:
            default:
                CPlacementTest_Tutor lastPlacementTest;

                // set prefs.usesThingy to false
                if(useMathPlacement) {
                    lastPlacementTest = matrix.mathPlacement[placementIndex];
                    placementKey = "MATH_PLACEMENT";
                    placementIndexKey = "MATH_PLACEMENT_INDEX";

                }
                // useWritePlacement only other option
                else {
                    lastPlacementTest = matrix.writePlacement[placementIndex];
                    placementKey = "WRITING_PLACEMENT";
                    placementIndexKey = "WRITING_PLACEMENT_INDEX";

                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(placementKey, false); // no more placement
                editor.remove(placementIndexKey);
                editor.apply();

                return lastPlacementTest.fail;


        }
    }

    private String getChildScope(String activeTutorId) {
        String childScope = null;
        if(activeTutorId.startsWith("bpop")) {
            childScope = "bubble_pop";

        } else if (activeTutorId.startsWith("akira")) {
            childScope = "akira";

        } else if (activeTutorId.startsWith("math")) {
            childScope = "add_subtract";

        } else if (activeTutorId.startsWith("write")) {
            childScope = "word_copy";

        } else if (activeTutorId.startsWith("story")) {
            childScope = "story_reading";

        } else if (activeTutorId.startsWith("countingx")) {
            childScope = "countingx";
        } else if (activeTutorId.startsWith("num.scale")) {
            childScope = "numberscale";
        }
        return childScope;
    }

    /**
     * gets the stored data for each student based on STUDENT_ID.
     * YYY if this is a student's first time logging in, use PLACEMENT
     */
    private SharedPreferences getStudentSharedPreferences() {
        // each ID name is composed of the STUDENT_ID plus the language i.e. EN or SW
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += mMediaManager.getLanguageFeature(mTutor);

        RoboTutor.logManager.postEvent_I(TAG, "Getting SharedPreferences: " + prefsName);
        return RoboTutor.ACTIVITY.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }


    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void doLaunch(String intent, String intentData, String dataSource, String tutorId) {

        Log.wtf("WARRIOR_MAN", "doLaunch: tutorId = " + tutorId);

        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
        RoboTutor.MUST_CALCULATE_NEXT_TUTOR = true; // needs to calculate next tutor upon launch

        // update the response variable  "<Sresponse>.value"

        publishValue(VAR_INTENT, intent);
        publishValue(VAR_INTENTDATA, intentData);
        publishValue(VAR_DATASOURCE, dataSource);
        publishValue(VAR_TUTOR_ID, tutorId);

        // (2.6)
        //applyBehavior(LAUNCH_EVENT); // (2.6) this was collapsed into the following command
        CTutorEngine.launch(intentData, intent, dataSource, tutorId);
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

        //
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

                //
                // Load the datasource into a separate class...
                matrix = new TransitionMatrixModel(dataPath + dataFile, mTutor.getScope());
                matrix.validateAll();

                initializeStudentDataModel();


                initializeState();
                //loadJSON(new JSONObject(jsonData), mTutor.getScope() );


            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source - " + dataNameDescriptor + " for : " + name() + " : ", e, true);
        }
    }

    /**
     * Initialize the student data model
     */
    private void initializeStudentDataModel() {
        // initialize
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += mMediaManager.getLanguageFeature(mTutor);
        studentModel = new StudentDataModel(RoboTutor.ACTIVITY, prefsName);
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


    /**
     *
     * // UTILITY
     * DESCRIBE_BEHAVIOR -> BUTTON_DESCRIPTION
     * DESCRIBE_COMPLETE -> SET_HESITATION_FEEDBACK
     * SELECT_BEHAVIOR   -> DLEAR_HESITATION_BEHAVIOR
     * LAUNCH_EVENT      -> LAUNCH_BEHAVIOR
     *
     * // BUTTONS....
     * SELECT_WRITING -> BUTTON_BEHAVIOR
     * SELECT_STORIES -> BUTTON_BEHAVIOR
     * SELECT_MATH    -> BUTTON_BEHAVIOR
     *
     * SELECT_REPEAT  -> BUTTON_BEHAVIOR
     * SELECT_EXIT    -> EXIT_BUTTON_BEHAVIOR
     *
     * @param event
     * @param behavior
     */
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





    // *** Seriali`ation




    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // we probably don't need this

    }

    /**
     * initializes everything
     *
     */
    private void initializeState() {

        SharedPreferences prefs = getStudentSharedPreferences(); // YYY

        if(prefs.getAll().entrySet().isEmpty())
            RoboTutor.logManager.postEvent_W(TAG, "SharedPreferences is empty");

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            RoboTutor.logManager.postEvent_D(TAG, "SharedPreferences: " + entry.getKey() + " -- " + entry.getValue().toString());
        }
        activeSkill = prefs.getString(TCONST.SKILL_SELECTED, SELECT_STORIES); // √√√ √√√

        // YYY maybe move this somewhere else???
        String firstTime = prefs.getString("HAS_PLAYED", null);
        // if it's the first time playing, we want to initialize our placement values
        if (firstTime == null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("HAS_PLAYED", String.valueOf(true));
            editor.putBoolean("MATH_PLACEMENT", true);
            editor.putInt("MATH_PLACEMENT_INDEX", 0);
            editor.putBoolean("WRITING_PLACEMENT", true);
            editor.putInt("WRITING_PLACEMENT_INDEX", 0);
            editor.apply();

        }
        // FOR_MOM (4.5) initialize only once??? and then load as needed?
        boolean useMathPlacement = prefs.getBoolean("MATH_PLACEMENT", true);
        boolean useWritingPlacement = prefs.getBoolean("WRITING_PLACEMENT", true);


        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useMathPlacement = %s", useMathPlacement));
        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useWritingPlacement = %s", useWritingPlacement));

        String mathTutorID;
        if(useMathPlacement) {
            int mathPlacementIndex = prefs.getInt("MATH_PLACEMENT_INDEX", 0);
            CPlacementTest_Tutor mathPlacementTutor = matrix.mathPlacement[mathPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathPlacementIndex = %d", mathPlacementIndex));
            mathTutorID = mathPlacementTutor.tutor; // FOR_MOM (4.5) for example, only load mathTutorID when needed...

        } else {
            mathTutorID    = prefs.getString(TCONST.SKILL_MATH,    matrix.rootSkillMath); // does this get overwritten or something???
        }
        RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathTutorID = %s", mathTutorID));

        studentModel.updateMathTutorID(mathTutorID);

        String writingTutorID;

        if (useWritingPlacement) {
            int writingPlacementIndex = prefs.getInt("WRITING_PLACEMENT_INDEX", 0);
            CPlacementTest_Tutor writePlacementTutor = matrix.writePlacement[writingPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writePlacementIndex = %d", writingPlacementIndex));
            writingTutorID = writePlacementTutor.tutor; // FOR_MOM (4.5) for example, only load writingTutorID when needed...
        } else {
            writingTutorID = prefs.getString(TCONST.SKILL_WRITING, matrix.rootSkillWrite);
        }
        RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writingTutorID = %s", writingTutorID));

        // DATA_MODEL (9) only perform on first update
        studentModel.updateWritingTutorID(writingTutorID);

        // stories doesn't have placement testing
        if (studentModel.getStoryTutorID() == null) {
            studentModel.updateStoryTutorID(matrix.rootSkillStories);
        }
    }
}
