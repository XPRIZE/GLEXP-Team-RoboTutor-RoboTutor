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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.comp_session.CActivitySelector;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformancePromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
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
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_AKIRA;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_ASM;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_BPOP;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_TAP_COUNT;
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

    CAt_Data tutorToLaunch;


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

    /** This allows us to update the current tutor for a given skill from the CDebugComponent
     *
     */
    @Override
    public void doDebugTagLaunchAction(String tag) {

        publishValue(AS_CONST.VAR_DEBUG_TAG, tag);

        applyBehavior(AS_CONST.SELECT_DEBUG_TAG_LAUNCH);
    }

    @Override
    public void doTaggedButtonBehavior(String tag) {
        Log.d(TAG, "Debug Button with tag: " + tag);

        // sometimes figuring out new code is like driving a new route in a slightly familiar city...
        // you are driving along unfamiliar roats and you're like "where the heck am I?"
        // then you turn a corner and all of a sudden you know you're exactly where you are...
        // and you're like "huh! I never would have guessed that this is where I'd end up!"
        performButtonBehavior(tag, true);

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
        performButtonBehavior(buttonid, false);


    }

    /**
     *
     *
     * @param buttonid
     * @param roboDebugger
     */
    private void performButtonBehavior(String buttonid, boolean roboDebugger) {
        // If we are in debug mode then there is a third selection phase where we are presented
        // the transition table for the active skill - The author can select a new target tutor
        // from any of the transition entries.
        //
        if(RoboTutor.SELECTOR_MODE.equals(TCONST.FTR_DEBUG_SELECT)) {
            buttonid = processDebugSelectMode(buttonid, roboDebugger);
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
            processTutorSelectMode(buttonid, roboDebugger);

        }
    }

    /**
     * Method for processing button press on the TUTOR_SELECT (home) screen
     * @param buttonid
     */
    private void processTutorSelectMode(String buttonid, boolean roboDebugger) {

        if (roboDebugger) {

            String intent;
            String file;

            intent = buttonid;

            // specify the file name we're debugging with
            switch (buttonid) {
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

            doLaunch(intent, TCONST.TUTOR_NATIVE, TCONST.DEBUG_FILE_PREFIX + file);
            return;

        }

        boolean     buttonFound = false;

        // If user selects "Let robotutor decide" then use student model to decide skill to work next
        // At the moment default to Stories
        //
        if (buttonid.toUpperCase().equals(AS_CONST.SELECT_ROBOTUTOR)) {

            int next = (new Random()).nextInt(3);
            switch(next) {
                case 0:
                    buttonid = AS_CONST.SELECT_WRITING;
                    break;

                case 1:
                    buttonid = AS_CONST.SELECT_STORIES;
                    break;

                case 2:
                    buttonid = AS_CONST.SELECT_MATH;
                    break;
            }

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

        // check SharedPreferences
        final SharedPreferences prefs = getStudentSharedPreferences();

        if (buttonFound) {

            // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
            // reenable the ASK buttons if we don't execute the story_tutor
            //
            if (!BuildConfig.NO_ASR_APPS || (transitionMap != storyTransitions)) {

                tutorToLaunch = (CAt_Data) transitionMap.get(activeTutor);

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

                    if(BuildConfig.SHOW_DEMO_VIDS) {


                        String whichActivityIsNext = parseActiveTutorForTutorName(activeTutor);
                        final String activityPreferenceKey = whichActivityIsNext + "_TIMES_PLAYED";

                        // bpop, write, akira, story, math, etc
                        final int timesPlayedActivity = prefs.getInt(activityPreferenceKey, 0); // i = default value
                        boolean playDemoVid = timesPlayedActivity < 1; // only play video once

                        String pathToFile = getTutorInstructionalVideoPath(whichActivityIsNext);

                        if(playDemoVid && pathToFile != null) {

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
                                CLogManager.setTutor(activeTutor);

                                doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data);
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


                                    CLogManager.setTutor(activeTutor);

                                    doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data);
                                }
                            });
                        } else {

                            CLogManager.setTutor(activeTutor);

                            doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data);

                        }
                    } else {

                        CLogManager.setTutor(activeTutor);

                        doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data);
                    }



                }

                SharedPreferences.Editor editor = prefs.edit();

                // Serialize the new state
                // #Mod 329 language switch capability
                //
                editor.putString(TCONST.SKILL_SELECTED, activeSkill);
                editor.apply();

            } else
                SaskActivity.enableButtons(true);
        }
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
    private String getTutorInstructionalVideoPath(String tutor) {

        String PATH_TO_FILE = TCONST.ROBOTUTOR_ASSETS + "/" + "video" + "/";

        // note that this was initially done w/ a "substring" check, but each tutor has a different
        // naming format e.g. math:10 vs. story.hear:1 vs. story.echo:1
        if (activeTutor.startsWith("bpop")) {
            PATH_TO_FILE += "bpop_demo.mp4";
        } else if (activeTutor.startsWith("akira")) {
            PATH_TO_FILE += "akira_demo.mp4";
        } else if (activeTutor.startsWith("math")) {
            PATH_TO_FILE += "asm_demo.mp4";
        } else if (activeTutor.startsWith("write")) {
            PATH_TO_FILE += "write_demo.mp4";
        } else if (activeTutor.startsWith("story.read") || activeTutor.startsWith("story.echo")) {
            PATH_TO_FILE += "read_demo.mp4";
        } else {
            return null;
        }

        return PATH_TO_FILE;

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

        // 1. pick the next tutor
        PromotionRules rules = new PerformancePromotionRules();
        PerformanceData performance = new PerformanceData();
        performance.setSelfAssessment(buttonid.toUpperCase());
        performance.setActivityType(activeTutor);

        // need to get the previous tutor and all that jazz...

        String childScope = null;
        if(activeTutor.startsWith("bpop")) {
            childScope = "bubble_pop";

        } else if (activeTutor.startsWith("akira")) {
            childScope = "akira";

        } else if (activeTutor.startsWith("math")) {
            childScope = "add_subtract";

        } else if (activeTutor.startsWith("write")) {
            childScope = "word_copy";

        } else if (activeTutor.startsWith("story")) {
            childScope = "story_reading";

        } else if (activeTutor.startsWith("countingx")) {
            childScope = "countingx";

        }

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

        CAt_Data transitionData = transitionMap.get(activeTutor);
        switch(selectedActivity) {
            case NEXT:
                nextTutor = transitionData.next;
                break;

            case SAME:
                nextTutor = transitionData.same;
                break;

            case OLD_EASIER:
                nextTutor = transitionData.easier;
                break;

            case OLD_HARDER:
                nextTutor = transitionData.harder;
                break;

            case PREVIOUS:
                // XXX FIXME nextTutor = transitionData.previous;
                // for now... do the super hacky way of iterating through the whole map until we find one who refers to "activeTutor" via "next"
                String tempNextTutor = null;
                for (Map.Entry<String, CAt_Data> e : transitionMap.entrySet()) {
                    Log.d("TRANSITION_MAP", e.getValue().toString());
                    CAt_Data value = e.getValue();
                    if (value.next.equals(activeTutor)) {
                        tempNextTutor = e.getKey();
                    }
                }
                // no "next" reference, probably means it's the first item
                if (tempNextTutor == null) {
                    tempNextTutor = activeTutor;
                }
                nextTutor = tempNextTutor;
                // XXX FIXME end super hacky code

                break;

            case DOUBLE_NEXT:
                // XXX FIXME nextTutor = transitionData.double_next;
                // for now... do the slightly less hacky way of doing "next" of "next"
                String notNextTutor = transitionData.next;

                CAt_Data nextTransitionData = transitionMap.get(notNextTutor);
                nextTutor = nextTransitionData.next;
                // XXX FIXME end slightly less hacky code
                // XXX note that these will not show up in the debugger graph
                break;
        }

        // 2. finish RoboTutor or the ActivitySelector, if necessary
        if(buttonid.toUpperCase().equals(AS_CONST.SELECT_EXIT)) {
            // if EXIT, we finish the app
            mTutor.post(TCONST.FINISH);

        } else if (buttonid.toUpperCase().equals(AS_CONST.SELECT_REPEAT) ||
                buttonid.toUpperCase().equals(AS_CONST.SELECT_AUTO_DIFFICULTY)){

            buttonid = activeSkill;

        } else {
            // unless they tap "REPEAT", go back to the main menu
            mTutor.post(TCONST.ENDTUTOR);
        }

        // 3. Set SELECTOR_MODE
        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

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
    private String processDebugSelectMode(String buttonid, boolean roboDebugger) {
        // Update the active skill
        //
        if(roboDebugger){
            // we know which selector mode we're in, so we can return without changing anything
            RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;
            return buttonid;

        }

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

    /**
     * gets the stored data for each student based on STUDENT_ID.
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
