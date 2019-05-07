package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
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
import java.util.logging.LogManager;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.comp_session.CActivitySelector;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.robotutor.tutorengine.util.CycleMatrixActivityMenu;
import cmu.xprize.robotutor.tutorengine.util.IActivityMenu;
import cmu.xprize.robotutor.tutorengine.util.StudentChooseMatrixActivityMenu;
import cmu.xprize.robotutor.tutorengine.util.StudentDataModel;
import cmu.xprize.robotutor.tutorengine.util.TransitionMatrixModel;
import cmu.xprize.robotutor.tutorengine.util.VideoHelper;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.ACTIONMAP_KEYS.CLEAR_HESITATION_BEHAVIOR;
import static cmu.xprize.comp_session.AS_CONST.ACTIONMAP_KEYS.SET_HESITATION_FEEDBACK;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.comp_session.AS_CONST.REPEAT_DEBUG_TAG;
import static cmu.xprize.comp_session.AS_CONST.VAR_DATASOURCE;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENT;
import static cmu.xprize.comp_session.AS_CONST.VAR_INTENTDATA;
import static cmu.xprize.comp_session.AS_CONST.VAR_TUTOR_ID;
import static cmu.xprize.util.TCONST.LANG_EN;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_AKIRA;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_ASM;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_BPOP;
import static cmu.xprize.util.TCONST.ROBO_DEBUG_FILE_TAP_COUNT;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

public class TActivitySelector extends CActivitySelector implements ITutorSceneImpl, IDataSink, IEventSource, IPublisher, ITutorLogger {

    private static boolean          DEBUG_LANCHER = false;
    public  static String           DEBUG_TUTORID = "";

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;
    private TLangToggle             mLangButton;

    private TTextView               SversionText;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private boolean     askButtonsEnabled = false;

    private TransitionMatrixModel matrix; // now holds the transition map things...
    private StudentDataModel studentModel; // holds the StudentDataModel
    private IActivityMenu menu;

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();


    final private String  TAG = "TActivitySelector";
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
        if (Configuration.showDebugLauncher(getContext())) {
            DEBUG_LANCHER = true;
        }

        mLangButton = (TLangToggle)findViewById(R.id.SlangToggle);
        mLangButton.setTransformationMethod(null);

        // Hide the language toggle on the release builds
        // TODO : Use build Variant to ensure release configurations
        //
        if (!Configuration.getLanguageSwitcher(getContext())) {

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

    /**
     * Initialize Layout in Tutor Select mode...
     * Called from AG, don't delete.
     */
    public void setTutorSelectLayout() {

        Log.wtf("STUDENT_MODEL:TUTOR_SELECT", studentModel.toString());

        SaskActivity.setVisibility(VISIBLE);
        SdebugActivity.setVisibility(GONE);

        // redundant code in "nextTutors"
        _activeLayout = menu.initializeActiveLayout();
        CAt_Data[] nextTutors = menu.getTutorsToShow();;
        String layoutName = menu.getLayoutName();

        if (menu instanceof StudentChooseMatrixActivityMenu) {
            SaskActivity.initializeButtonsAndSetButtonImages(layoutName, _activeLayout, null);
        } else if (menu instanceof CycleMatrixActivityMenu) {
            SaskActivity.initializeButtonsAndSetButtonImages(layoutName, _activeLayout, nextTutors);
        }

    }

    /**
     * Initialize Layout in Debug Mode...
     * Called from AG, don't delete.
     */
    public void setDebugLayout() {
        Log.wtf("STUDENT_MODEL:DEBUG", studentModel.toString());
        SaskActivity.setVisibility(GONE);
        SdebugActivity.setVisibility(VISIBLE);

        // Init the skill pointers
        //
        String activeTutorId = "";
        HashMap transitionMap = null;
        String rootTutor = "";

        // look up activeSkill every time?
        String activeSkill = menu.getDebugMenuSkill();
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
     * Replaces animator graph so we can trace variables
     */
    public void setAllStickyBehavior() {
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_BEHAVIOR, AS_CONST.QUEUEMAP_KEYS.BUTTON_DESCRIPTION);
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.DESCRIBE_COMPLETE, SET_HESITATION_FEEDBACK);
        setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.SELECT_BEHAVIOR, CLEAR_HESITATION_BEHAVIOR);
        //setStickyBehavior(AS_CONST.BEHAVIOR_KEYS.LAUNCH_EVENT, "LAUNCH_BEHAVIOR"); // (2.7) collapsed into one function

        Map<String, String> buttonBehavior = menu.getButtonBehaviorMap();
        for (String button : buttonBehavior.keySet()) {
            setStickyBehavior(button, buttonBehavior.get(button));
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

                    applyBehavior(element.behavior);
                    // applyBehaviorNode(element.secondary_behavior...)
                }
            }
        }
    }

    /**
     * TODO this and doButtonBehavior are very similar...
     *
     * This allows us to update the current tutor for a given skill from the CDebugComponent
     * @param debugTutor should be a tutorId
     */
    @Override
    public void doDebugLaunchAction(String debugTutor) {

        // RoboTutor.SELECTOR_MODE == "DEBUG_MENU"
        String writingTutorID = null, storiesTutorID = null, mathTutorID = null;

        HashMap transitionMap = null;
        String rootTutor = "";

        // look up activeSkill every time?
        // if student chose repeat, use last skill instead
        switch (menu.getDebugMenuSkill()) {  // this is dependent on which debug menu we're in

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                studentModel.updateWritingTutorID(debugTutor); // DEBUG_MENU_LOGIC (x) this might be wrong...
                rootTutor     = matrix.getRootSkillByContentArea(SELECT_WRITING);
                transitionMap = matrix.getTransitionMapByContentArea(SELECT_WRITING);
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                studentModel.updateStoryTutorID(debugTutor);
                rootTutor     = matrix.getRootSkillByContentArea(SELECT_STORIES);
                transitionMap = matrix.getTransitionMapByContentArea(SELECT_STORIES);
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                studentModel.updateMathTutorID(debugTutor);
                rootTutor     = matrix.getRootSkillByContentArea(SELECT_MATH);
                transitionMap = matrix.getTransitionMapByContentArea(SELECT_MATH);
                break;
        }

        RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_LAUNCH;

        // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
        // reenable the ASK buttons if we don't execute the story_tutor
        //
        /*if (Configuration.noAsrApps(getContext()) && transitionMap == matrix.storyTransitions) {
            SaskActivity.enableButtons(true);
            return;
        }*/

        // the next tutor to be launched
        CAt_Data tutorToLaunch = (CAt_Data) transitionMap.get(debugTutor);

        // This is just to make sure we go somewhere if there is a bad link - which
        // there shuoldn't be :)
        //
        if (tutorToLaunch == null) {
            tutorToLaunch = (CAt_Data) transitionMap.get(rootTutor);
        }

        // #Mod 330 Show TutorID in Banner in debug builds
        // DEBUG_TUTORID is used to communicate the active tutor to the Banner in DEBUG mode
        //
        if (Configuration.showTutorVersion(getContext())) {
            DEBUG_TUTORID = debugTutor;
        }

        // check SharedPreferences

        doTutorLaunchWithVideosAndStuff(tutorToLaunch);
    }

    /**
     * TODO this and doDebugLaunchAction are very similar...
     *
     * Button clicks may come from either the skill selector ASK component or the Difficulty
     * selector ASK component.
     *
     * @param buttonBehavior  one of {SELECT_WRITING, SELECT_STORIES, SELECT_MATH, SELECT_REPEAT, SELECT_EXIT}
     */
    @Override
    public void doButtonBehavior(String buttonBehavior) {

        // add error handling
        try {

            // exit always exits
            if (buttonBehavior.equals(AS_CONST.SELECT_EXIT)) {
                mTutor.post(TCONST.FINISH);
                return;
            }

            RoboTutor.STUDENT_CHOSE_REPEAT = false; // default to false, unless they select REPEAT (needed for debug mode)

            // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
            // reenable the ASK buttons if we don't execute the story_tutor
            //
        /*if (Configuration.noAsrApps(getContext()) && transitionMap == matrix.storyTransitions) {
            SaskActivity.enableButtons(true);
            return;
        }*/

            // the next tutor to be launched
            CAt_Data tutorToLaunch = menu.getTutorToLaunch(buttonBehavior);

            // #Mod 330 Show TutorID in Banner in debug builds
            // DEBUG_TUTORID is used to communicate the active tutor to the Banner in DEBUG mode
            //
            if (Configuration.showTutorVersion(getContext())) {
                DEBUG_TUTORID = tutorToLaunch.tutor_id;
            }

            // This is where we go to the debug view...
            //
            if (DEBUG_LANCHER) {
                mTutor.post(TCONST.ENDTUTOR); // ends current tutor, launches debug tutor instead
                RoboTutor.SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;
            } else {
                doTutorLaunchWithVideosAndStuff(tutorToLaunch);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(REPEAT_DEBUG_TAG + "_CRASH", "StudentModel--" + studentModel.toLogString(), e,true);
            CErrorManager.logEvent(REPEAT_DEBUG_TAG + "_CRASH", Log.getStackTraceString(e), true);
        }
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

        doLaunch(intent, TCONST.TUTOR_NATIVE, TCONST.DEBUG_FILE_PREFIX + file, "DEBUGGER", null);
    }


    //************************************************************************
    //************************************************************************
    // Everything demo video related BEGIN

    /**
     * A big and cumbersome method that will launch a tutor eventually
     */
    private void doTutorLaunchWithVideosAndStuff(CAt_Data tutorToLaunch) {

        String activeTutorId = tutorToLaunch.tutor_id;
        // Update the tutor id shown in the log stream

        if(Configuration.showDemoVids(getContext())) {

            String videoId = VideoHelper.getVideoIdByTutor(tutorToLaunch);
            Log.d("VID_LAUNCH", "videoId=" + videoId);

            if (videoId != null) {

                // bpop, write, akira, story, math, etc
                final int timesPlayedActivity = studentModel.getTimesPlayedTutor(videoId);
                final int TIMES_TO_PLAY_DEMO = 1;
                boolean playDemoVid = timesPlayedActivity < TIMES_TO_PLAY_DEMO; // only play video twice
                Log.d("VID_LAUNCH", videoId + ": " + timesPlayedActivity + " compare to " + TIMES_TO_PLAY_DEMO);

                String pathToFile = VideoHelper.getVideoPathById(videoId);

                if(playDemoVid && pathToFile != null) {

                    // VID_LAUNCH there is redundant information here...
                    playTutorDemoVid(tutorToLaunch, videoId, timesPlayedActivity, pathToFile);
                    // The tutor will be launched upon video completion, so we must return before we launch the tutor.
                    return;
                }
            }
        }

        CLogManager.setTutor(activeTutorId);
        doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id, tutorToLaunch.skill);
    }

    /**
     * A big and cumbersome method that plays the tutor video...
     *
     * @param prefs
     * @param activityPreferenceKey
     * @param timesPlayedActivity
     * @param pathToFile
     */
    private void playTutorDemoVid(final CAt_Data tutorToLaunch, final String timesPlayedId, final int timesPlayedActivity, String pathToFile) {
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
            CLogManager.setTutor(tutorToLaunch.tutor_id);

            doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id, tutorToLaunch.skill);
        }


        //MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), videoId);
        //mediaPlayer.setDisplay(fullscreenView.getHolder());
        //mediaPlayer.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
                fullscreenView.setVisibility(View.INVISIBLE);
                if (Configuration.getLanguageSwitcher(getContext())) {
                    langToggle.setVisibility(View.VISIBLE); // prevent from changing language
                }


                int increment = timesPlayedActivity + 1;
                studentModel.updateTimesPlayedTutor(timesPlayedId, increment); // increment to let them know we watched the video
                Log.d("VID_LAUNCH", timesPlayedId + ": " + timesPlayedActivity + " --> " + increment);

                CLogManager.setTutor(tutorToLaunch.tutor_id);
                doLaunch(tutorToLaunch.tutor_desc, TCONST.TUTOR_NATIVE, tutorToLaunch.tutor_data, tutorToLaunch.tutor_id, tutorToLaunch.skill);
            }
        });
    }

    // Everything demo video related END
    //************************************************************************
    //************************************************************************

    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void doLaunch(String intent, String intentData, String dataSource, String tutorId, String matrix) {

        Log.wtf("WARRIOR_MAN", "doLaunch: tutorId = " + tutorId);

        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

        // TRACE_PROMOTION tutorToLaunch not saved...

        // update the response variable  "<Sresponse>.value"

        publishValue(VAR_INTENT, intent);
        publishValue(VAR_INTENTDATA, intentData);
        publishValue(VAR_DATASOURCE, dataSource);
        publishValue(VAR_TUTOR_ID, tutorId);

        // (2.6)
        //applyBehavior(LAUNCH_EVENT); // (2.6) this was collapsed into the following command
        CTutorEngine.launch(intentData, intent, dataSource, tutorId, matrix);
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

            String QA_MATRIX_TAG = "QA_MATRIX";
            Log.wtf(QA_MATRIX_TAG, "key=" + entry.getKey().toString());
            String key   = entry.getKey().toString();
            String value = "#" + entry.getValue() != null ? entry.getValue().toString() : "null";

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


    /**
     * This method has become mostly empty, as we no longer load the data source from JSON.
     * @param dataNameDescriptor
     */
    @Override
    public void setDataSource(String dataNameDescriptor) {

        //
        // TODO: globally make startWith type TCONST
        try {
            if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                // The new way to load the TransitionMatrix and StudentModel
                matrix = CTutorEngine.matrix;
                studentModel = CTutorEngine.studentModel;

                boolean studentChoice = RoboTutor.OLD_MENU || CTutorEngine.language.equals(LANG_EN);

                switch(CTutorEngine.menuType) {
                    case STUDENT_CHOICE:
                        menu = new StudentChooseMatrixActivityMenu(matrix, studentModel);
                        break;

                    case CYCLE_CONTENT:
                        menu = new CycleMatrixActivityMenu(matrix, studentModel);
                        break;
                }



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
}
