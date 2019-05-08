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

package cmu.xprize.robotutor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.CPerfLogManager;
import cmu.xprize.comp_logging.CPreferenceCache;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.comp_logging.IPerfLogManager;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.robotutor.startup.CStartView;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.startup.configuration.ConfigurationItems;
import cmu.xprize.robotutor.tutorengine.CMediaController;

import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;

import cmu.xprize.robotutor.tutorengine.QuickDebugTutor;
import cmu.xprize.robotutor.tutorengine.util.CAssetObject;
import cmu.xprize.util.CDisplayMetrics;
import cmu.xprize.util.CLoaderView;

import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.util.CrashHandler;
import cmu.xprize.robotutor.tutorengine.widgets.core.IGuidView;
import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.LITERACY_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.MATH_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.SONGS_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.STORIES_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.UNKNOWN_MATRIX;
import static cmu.xprize.util.TCONST.CODE_DROP_1_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.CODE_DROP_2_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.ENGLISH_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.GRAPH_MSG;
import static cmu.xprize.util.TCONST.MATH_PLACEMENT;
import static cmu.xprize.util.TCONST.PROTOTYPE_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.QA_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.ROBOTUTOR_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.WRITING_PLACEMENT;


/**
 * <h2>Class Overview</h2>
 * <hr>
 * This class represents the root activity for a Tutor Manager that can display one of many
 * instructional tutors.  Tutors may also link to other Activities that themselves represent
 * Tutor Managers and can vector to specific tutors contained therein.
 * <br>
 * <h3>Developer Overview</h3>
 *
 */
public class RoboTutor extends Activity implements IReadyListener, IRoboTutor {


    // VARIABLES FOR QUICK DEBUG LAUNCH
//    private static final String debugTutorVariant = "placevalue";
//    private static final String debugTutorId = "place.value:1";
//    private static final String debugTutorFile = "[file]place.value__pv-11..99.2D.diff0.3.json";
    private static final boolean QUICK_DEBUG = false;
    private static final String QUICK_DEBUG_TEST_KEY = "akira:missing_audio";

    // BUGS to check...
    // in the future, this could be a cool debugging screen that pops up and leads you through bugs
    // there could also be an in-app bug-reporting system
    private static Map<String, QuickDebugTutor> bugMap = new HashMap<>();
    static {

        // --------
        // RESOLVED
        // --------
        QuickDebugTutor bug1 = new QuickDebugTutor("write.wrd.dic",
                "write.wrd.dic:word.dolch_3rd_grade",
                "[file]write.wrd.dic_word.dolch_3rd_grade.json");
        bug1.setComment("Missing the word 'if' and hangs");
        bugMap.put("write:missing_word", bug1);
        bug1.resolve(); // FIXED

        QuickDebugTutor bug2 = new QuickDebugTutor(
                "numcompare",
                "numcompare:1d",
                "[file]numcompare_1d.json"
                );
        bug2.setComment("Missing audio");
        bug2.setComment("assets/audio/en/cmu/xprize/nd/{{SNumDiscr.digitCompare}}.mp3");
        bug2.setComment("assets/audio/en/cmu/xprize/nd/So the bigger number is.mp3");
        bugMap.put("nd:missing_prompts", bug2);
        bug2.resolve(); // FIXED

        QuickDebugTutor bug5 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff0.17",
                "[file]place.value_pv-100..499.3D.diff0.17.json"
        );
        bug5.setComment("Missing audio 'tap inside the box'; stimulus box is ugly; second screen missing number audio.");
        bug5.setLocation("CCountingXComponent");
        bugMap.put("place.value:many_bugs", bug5);
        bug5.resolve(); // FIXED

        QuickDebugTutor bug6 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug6.setComment("Missing audio on second screen.");
        bug6.setPriority(QuickDebugTutor.Priority.MUST);
        bugMap.put("place.value:missing_audio", bug6);
        bug6.resolve(); // FIXED

        QuickDebugTutor bug11 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug11.setComment("countx audio should just say 'please tap' if place value.");
        bug11.setLocation("countingx/animator_graph.json");
        bug11.setPriority(QuickDebugTutor.Priority.SHOULD);
        bugMap.put("place.value:wrong_audio", bug11);
        bug11.resolve(); // FIXED

        QuickDebugTutor bug3 = new QuickDebugTutor(
                "bpop.gl",
                "bpop.gl:dot.0..9.GL_SD_OFF1_L.bub2.6",
                "[file]bpop.gl_dot.0..9.GL_SD_OFF1_L.bub2.6.json"
        );
        bug3.setComment("Should say 'tap the bigger number' or 'tap the smaller number");
        bug3.setLocation("bubble_pop/animator_graph.json");
        bug3.setPriority(QuickDebugTutor.Priority.MUST);
        bugMap.put("bpop.gl:wrong_prompt", bug3);
        bug3.resolve(); // FIXED

        QuickDebugTutor bug4 = new QuickDebugTutor(
                "bpop.mn",
                "bpop.mn:0..9.MN-SD-UP-OFF1-BL1.incr.4",
                "[file]bpop.mn_0..9.MN-SD-UP-OFF1-BL1.incr.4.json"
        );
        bug4.setComment("Should say 'tap the missing number'");
        bug4.setLocation("bubble_pop/animator_graph.json");
        bug4.setPriority(QuickDebugTutor.Priority.MUST);
        bugMap.put("bpop.mn:wrong_prompt", bug4);
        bug4.resolve(); // FIXED

        QuickDebugTutor bug7 = new QuickDebugTutor(
                "story.parrot",
                "story.parrot::ea2eh_wb_2",
                "[encfolder]ea2eh_wb_2"
        );
        bug7.setComment("Should say 'Read after me'");
        bugMap.put("story.parrot:missing_prompt", bug7);
        bug7.resolve(); // FIXED

        QuickDebugTutor bug8 = new QuickDebugTutor(
                "story.pic.hear",
                "story.pic.hear::1_13",
                "[encfolder]1_13"
        );
        bug8.setComment("Missing audio prompts e.g. 'Which picture?'");
        bugMap.put("story.pic:missing_prompt", bug8);
        bug8.resolve(); // FIXED

        QuickDebugTutor bug12 = new QuickDebugTutor(
                "bpop.gl",
                "bpop.gl:num.100..900.GL_DD_OFF100_L.bub2.30.json",
                "[file]bpop.gl_num.100..900.GL_DD_OFF100_L.bub2.30.json");
        bug12.setComment("Audio not playing");
        bugMap.put("bpop.gl:multidigit_audio", bug12);
        bug12.setPriority(QuickDebugTutor.Priority.MUST);
        bug12.setLocation("bubble_pop/animator_graph.json");
        bug12.resolve(); // FIXED

        QuickDebugTutor bug13 = new QuickDebugTutor(
                "bpop.num",
                "bpop.num:1..4.by.1.asc.q2q.AV.mc.1",
                "[file]bpop.num_1..4.by.1.asc.q2q.AV.mc.1.json"
        );
        bug13.setComment("Number audio not playing");
        bugMap.put("bpop.num:missing_audio", bug13);
        bug13.setPriority(QuickDebugTutor.Priority.MUST);
        bug13.resolve(); // FIXED

        // -------
        // TO FIX
        // -------
        // STATUS: review
        QuickDebugTutor bug9 = new QuickDebugTutor(
                "numcompare",
                "numcompare:1d",
                "[file]numcompare_1d.json"
        );
        bug9.setComment("Audio prompts should not say 'First tap on the ones' if there are only ones.");
        bug9.setPriority(QuickDebugTutor.Priority.COULD);
        bugMap.put("numcompare:wordy_prompt", bug9);

        // STATUS: review
        QuickDebugTutor bug10 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug10.setComment("'Good job' audio is overlapping on second screen.");
        bug10.setPriority(QuickDebugTutor.Priority.COULD);
        bugMap.put("place.value:overlapping_audio", bug10);

        QuickDebugTutor bug14 = new QuickDebugTutor(
                "akira",
                "akira:10..100.by.within.des",
                "[file]akira_10..100.by.within.des.json"
        );
        bug14.setComment("Akira prompt number not playing");
        bug14.setLocation("akira/animator_graph.json --> INSTRUCT_3V");
        bugMap.put("akira:missing_audio", bug14);


    }

    public static final String MATRIX_FILE = "dev_data.open.json";

    private static final String LOG_SEQUENCE_ID = "LOG_SEQUENCE_ID";

    public static final boolean OLD_MENU = true;


    private CMediaController    mMediaController;

    private CLoaderView         progressView;
    private CStartView          startView;

    public TTSsynthesizer       TTS = null;
    public ListenerBase         ASR;
    public IGlyphSink           LTKPlus = null;

    static public ITutorManager masterContainer;
    static public ILogManager   logManager;
    static public IPerfLogManager perfLogManager;

    static CTutorAssetManager   tutorAssetManager;
    static public String        VERSION_RT;
    static public ArrayList     VERSION_SPEC;

    static public CDisplayMetrics displayMetrics;

    static public String        APP_PRIVATE_FILES;
    static public String        LOG_ID = "STARTUP";

    static public Activity      ACTIVITY;
    static public String        PACKAGE_NAME;
    static public boolean       DELETE_INSTALLED_ASSETS = false;

    static public String        STUDENT_ID; // received from FaceLogin
    static public String        SESSION_ID; // received from FaceLogin

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern

    private boolean                 isReady       = false;
    private boolean                 engineStarted = false;
    static public boolean           STANDALONE    = false;
    static public String            SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT; // this is only used as a feature, when launching TActivitySelector...
    static public boolean           STUDENT_CHOSE_REPEAT = false;
//    static public String        SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;


    // TODO: This is a temporary log update mechanism - see below
    //
    static private IGuidView    guidCallBack;

    String hotLogPath;
    String hotLogPathPerf;
    String readyLogPath;
    String readyLogPathPerf;
    String audioLogPath;
    public final static String  DOWNLOAD_PATH  = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
    public final static String  EXT_ASSET_PATH = Environment.getExternalStorageDirectory() + File.separator + TCONST.ROBOTUTOR_ASSET_FOLDER;

    private final  String  TAG = "CRoboTutor";
    private final String ID_TAG = "StudentId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Note = we don't want the system to try and recreate any of our views- always pass null
        //
        super.onCreate(null);

        hotLogPath   = Environment.getExternalStorageDirectory() + TCONST.HOT_LOG_FOLDER;
        readyLogPath = Environment.getExternalStorageDirectory() + TCONST.READY_LOG_FOLDER;

        hotLogPathPerf = Environment.getExternalStorageDirectory() + TCONST.HOT_LOG_FOLDER_PERF;
        readyLogPathPerf = Environment.getExternalStorageDirectory() + TCONST.READY_LOG_FOLDER_PERF;

        audioLogPath = Environment.getExternalStorageDirectory() + TCONST.AUDIO_LOG_FOLDER;

        APP_PRIVATE_FILES = getApplicationContext().getExternalFilesDir("").getPath();

        // Initialize the JSON Helper STATICS - just throw away the object.
        //
        new JSON_Helper(getAssets(), CacheSource, RoboTutor.APP_PRIVATE_FILES);

        ConfigurationItems configurationItems = new ConfigurationItems();
        Configuration.saveConfigurationItems(this, configurationItems);

        Calendar calendar = Calendar.getInstance(Locale.US);
        String initTime     = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(calendar.getTime());
        String sequenceIdString = String.format(Locale.US, "%06d", getNextLogSequenceId());
        // NOTE: Need to include the configuration name when that is fully merged
        String logFilename  = "RoboTutor_" + // TODO TODO TODO there should be a version name in here!!!
                Configuration.configVersion(this) + "_" + BuildConfig.VERSION_NAME + "_" + sequenceIdString +
                "_" + initTime + "_" + Build.SERIAL;

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onCreate");
        // Catch all errors and cause a clean exit -
        // TODO: this doesn't work as expected
        //

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(hotLogPath));

        PACKAGE_NAME = getApplicationContext().getPackageName();
        ACTIVITY     = this;

        // Prep the CPreferenceCache
        // Update the globally accessible id object for this engine instance.
        //
        LOG_ID = CPreferenceCache.initLogPreference(this);

        // RoboTutor Version spec - positional element meaning 0.1.2.3
        // Given 4.23.2.3
        // Major release 4 | Feature release 23 | Fix release 2 | compatible Asset Version 3
        //
        tutorAssetManager = new CTutorAssetManager(getApplicationContext());

        VERSION_RT   = BuildConfig.VERSION_NAME;
        VERSION_SPEC = CAssetObject.parseVersionSpec(VERSION_RT);

        Log.w("LOG_DEBUG", "Beginning new session with LOG_FILENAME = " + logFilename);

        logManager = CLogManager.getInstance();
        logManager.transferHotLogs(hotLogPath, readyLogPath);
        logManager.transferHotLogs(hotLogPathPerf, readyLogPathPerf);

        logManager.startLogging(hotLogPath, logFilename);
        CErrorManager.setLogManager(logManager);

        perfLogManager = CPerfLogManager.getInstance();
        perfLogManager.startLogging(hotLogPathPerf, "PERF_" + logFilename);

        // TODO : implement time stamps
        logManager.postDateTimeStamp(GRAPH_MSG, "RoboTutor:SessionStart");
        logManager.postEvent_I(GRAPH_MSG, "EngineVersion:" + VERSION_RT);

        Log.v(TAG, "External_Download:" + DOWNLOAD_PATH);

        // Get the primary container for tutors
        //
        setContentView(R.layout.robo_tutor);
        masterContainer = (ITutorManager)findViewById(R.id.master_container);

        // Set fullscreen and then get the screen metrics
        //
        setFullScreen();

        // get the multiplier used for drawables at the current screen density and calc the
        // correction rescale factor for design scale
        // This initializes the static object
        //
        displayMetrics = CDisplayMetrics.getInstance(this);

        // Initialize the media manager singleton - it needs access to the App assets.
        //
        mMediaController = CMediaController.getInstance();
        AssetManager mAssetManager = getApplicationContext().getAssets();
        mMediaController.setAssetManager(mAssetManager);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create the start dialog
        // TODO: This is a temporary log update mechanism - see below
        //
        startView = (CStartView)inflater.inflate(R.layout.start_layout, null );
        startView.setCallback(this);

        // Show the Indeterminate loader
        //
        progressView = (CLoaderView)inflater.inflate(R.layout.progress_layout, null );

        masterContainer.addAndShow(progressView);

        // testCrashHandler();
    }


    /**
     * just a fun little method that will throw a null handler exception (when on the right screen)
     */
    private void testCrashHandler() {

        TextView x = findViewById(R.id.SBPopWords);
        x.setText("AYY LMAO");
    }
    /**
     * This file gets the Extras that are passed from FaceLogin and uses them to set the uniqueIDs,
     * SessionID and StudentID
     *
     */
    private void setUniqueIdentifiers() {
        String BUNDLE_TAG = "BUNDLE";

        logManager.postEvent_I(ID_TAG, "RoboTutor:setUniqueIdentifiers");



        if(getIntent() != null && getIntent().getExtras() != null) {

            for (String key : getIntent().getExtras().keySet()) {
                Log.i(BUNDLE_TAG, "INTENT_KEY_FOUND: " + key + " -- " + getIntent().getExtras().get(key));
            }

            STUDENT_ID = getIntent().getExtras().getString(TCONST.STUDENT_ID_VAR);

            if(STUDENT_ID != null) {
                Log.i(BUNDLE_TAG, "studentId passed! " + STUDENT_ID);
                logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);
            } else {
                logManager.postEvent_I(ID_TAG, "NoStudentFound:settingDefault");
                STUDENT_ID = TCONST.DEFAULT_STUDENT_ID;
                logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);
            }

            SESSION_ID = getIntent().getExtras().getString(TCONST.SESSION_ID_VAR);


        } else {
            Log.w(BUNDLE_TAG, "no extras passed!");
            logManager.postEvent_I(ID_TAG, "NoStudentFound:settingDefault");
            STUDENT_ID = TCONST.DEFAULT_STUDENT_ID;
            logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);

        }
    }


    /**
     * Ignore the state bundle
     *
     * @param bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        //super.onRestoreInstanceState(bundle);
        logManager.postEvent_V(TAG, "RoboTutor:onRestoreInstanceState");
    }


    public void reBoot() {

        try {

            Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
            proc.waitFor();

        } catch (Exception ex) {

            logManager.postEvent_V(TAG, "RoboTutor:Could not reboot");
        }

    }

    private void setFullScreen() {

        ((View) masterContainer).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    /**
     *
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        boolean result = super.dispatchTouchEvent(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_UP:
                logManager.postEvent_V(TAG, "RT_SCREEN_RELEASE: X:" + event.getX() + "  Y:" + event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                logManager.postEvent_V(TAG, "RT_SCREEN_MOVE X:" + event.getX() + "  Y:" + event.getY());
                break;

            case MotionEvent.ACTION_DOWN:
                logManager.postEvent_V(TAG, "RT_SCREEN_TOUCH X:" + event.getX() + "  Y:" + event.getY());
                break;
        }

        // Manage system levelFolder timeout here

        return result;
    }


    /**
     * Moves new assets to an external storyFolder so the Sphinx code can access it.
     *
     */
    class tutorConfigTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... unused) {

            Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: tutorConfigTask.doInBackground");
            boolean result = false;

            try {
                // TODO: Don't do this in production
                // At the moment we always reinstall the tutor spec data - for 
              
              
                if(CacheSource.equals(TCONST.EXTERN)) {
                    tutorAssetManager.installAssets(TCONST.TUTORROOT);
                    logManager.postEvent_V(TAG, "INFO:Tutor Assets installed");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_PROJECT_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_PROJEXCTS);
                    logManager.postEvent_V(TAG, "INFO:LTK Projects installed");

                    // Note the Projects Zip file is anticipated to contain a storyFolder called "projects"
                    // containing the ltk data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_PROJEXCTS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent_V(TAG, "INFO:LTK Projects extracted");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_GLYPH_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_GLYPHS);
                    logManager.postEvent_V(TAG, "INFO:LTK Glyphs installed");

                    // Note the Glyphs Zip file is anticipated to contain a storyFolder called "glyphs"
                    // containing the ltk glyph data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_GLYPHS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent_V(TAG, "INFO:LTK Glyphs extracted");
                }

                // Find and install (move to ext_asset_path) any new or updated audio/story assets
                //
                // ZZZ comment out old pattern
                tutorAssetManager.updateAssetPackages(ROBOTUTOR_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH );
                tutorAssetManager.updateAssetPackages(CODE_DROP_1_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);
                tutorAssetManager.updateAssetPackages(CODE_DROP_2_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);
                tutorAssetManager.updateAssetPackages(PROTOTYPE_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);
                tutorAssetManager.updateAssetPackages(QA_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);
                tutorAssetManager.updateAssetPackages(ENGLISH_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);

                // Create the one system levelFolder LTKPLUS recognizer
                //
                LTKPlus = CRecognizerPlus.getInstance();
                LTKPlus.initialize(getApplicationContext(), GCONST.ALPHABET);

                result = true;

            } catch (IOException e) {
                // TODO: Manage exceptions
                e.printStackTrace();
                result = false;
            }
            return result;
        }

                @Override
        protected void onPostExecute(Boolean result) {
            isReady = result;

            onServiceReady("ROOT", result ? 1 : 0);
        }
    }


    /**
     * Callback used by services to announce ready state
     * @param serviceName
     */
    @Override
    public void onServiceReady(String serviceName, int status) {

        logManager.postEvent_V(TAG, "onServiceReady:" + serviceName + "status:" + status);

        // As the services come online push a global reference to CTutor
        //
        switch(serviceName) {
            case TCONST.TTS:
                logManager.postEvent_V(TAG, "flite:attaching");

                mMediaController.setTTS(TTS);
                break;
        }
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onServiceReady: " + serviceName + ":" + status);


        // check whether TTS, ASR, and ROOT are ready before starting engine
        if((TTS != null && TTS.isReady()) && (ASR != null && ASR.isReady()) && isReady) {

            startEngine();
        }
    }


    /**
     * Start the tutor engine once everything is intialized.
     *
     * There are several async init tasks and they all call this when they're finished.
     * The last one ready passes all the tests and starts the engine.
     *
     * TODO: Manage initialization failures
     *
     */
    private void startEngine() {

        if(!engineStarted) {
            engineStarted = true;

            logManager.postEvent_V(TAG, "TutorEngine:Starting");

            // Delete the asset loader utility ASR object
            ASR = null;

            masterContainer.removeView(progressView);

            // Initialize the Engine - set the EXTERN File path for file installs
            // Load the default tutor defined in assets/tutors/engine_descriptor.json
            // TODO: Handle tutor creation failure
            //
            CTutorEngine.getTutorEngine(RoboTutor.this);

            // If running without built-in home screen add a start screen
            //
            if(STANDALONE) {

                // TODO: This is a temporary log update mechanism - see below
                //
                masterContainer.addAndShow(startView);
                startView.startTapTutor();
                setFullScreen();
            }
            // QUICK DEBUG LAUNCH
            else if (QUICK_DEBUG) {

                startQuickLaunch();

                // start whatever tutor we're debugging
            }
            // Otherwise go directly to the sessionManager
            //
            else {
                Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "xx: onStartTutor in 'RoboTutor.startEngine'");
                onStartTutor();
            }

        }
        // Note that it is possible for the masterContainer to be recreated without the
        // engine begin destroyed so we must maintain sync here.
        else {
            logManager.postEvent_V(TAG, "TutorEngine:Restarting");
        }

    }


    // TODO: This is a temporary log update mechanism - see below
    //
    static public void setGUIDCallBack(IGuidView callBack) {

        guidCallBack = callBack;
    }


    // TODO: This is a temporary log update mechanism - see below
    //
    public void onStartTutor() {

        logManager.postEvent_V(TAG, "LOG_GUID:" + LOG_ID );
        LOG_ID = CPreferenceCache.initLogPreference(this);

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "xx: startSessionManager in 'onStartTutor'");
        CTutorEngine.startSessionManager();

        startView.stopTapTutor();
        masterContainer.removeView(startView);
        setFullScreen();

        // Disable screen sleep while in a session
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     *
     * This launches a new tutor immediately at startup. Useful for quick debugging.
     */
    private void startQuickLaunch() {
        logManager.postEvent_V(TAG, "LOG_GUID:" + LOG_ID );
        LOG_ID = CPreferenceCache.initLogPreference(this);

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "xx: startSessionManager in 'onStartTutor'");

        QuickDebugTutor debugMe = bugMap.get(QUICK_DEBUG_TEST_KEY);
        // CTutorEngine.quickLaunch(debugTutorVariant, debugTutorId, debugTutorFile);
        CTutorEngine.quickLaunch(debugMe.tutorVariant, debugMe.tutorId, debugMe.tutorFile);

        startView.stopTapTutor();
        masterContainer.removeView(startView);
        setFullScreen();

        // Disable screen sleep while in a session
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /**
     * TODO: Manage the back button
     */
    @Override
    public void onBackPressed() {
        logManager.postEvent_V(TAG, "RoboTuTor:onBackPressed");

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r4: killActiveTutor called from onBackPressed()");
        CTutorEngine.killActiveTutor();

        // Allow the screen to sleep when not in a session
        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // TODO: This is a temporary log update mechanism - see below
        //
        masterContainer.addAndShow(startView);
        startView.startTapTutor();
        setFullScreen();
    }



    /***  State Management  ****************/


    /**
     *
     */
    @Override
    protected void onStart() {

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onStart");

        super.onStart();

        // On-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStart");

        setUniqueIdentifiers();

        // We only want to run the engine start sequence once per onStart call
        //
        engineStarted = false;

        // Debug - determine platform dependent memory limit
        //
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memAvail       = am.getMemoryClass();

        logManager.postEvent_V(TAG, "AvailableMemory:" + memAvail);

        // Create the common TTS service
        // Async
        //
        if(TTS == null) {

            logManager.postEvent_V(TAG, "Creating:TTS");

            TTS = new TTSsynthesizer(this);
            TTS.initializeTTS(this);
        }

        // Create an inert listener for asset initialization only
        // Start the configListener async task to update the listener assets only if required.
        // This moves the listener assets to a local storyFolder where they are accessible by the
        // NDK code (PocketSphinx)
        //
        if(ASR == null) {

            logManager.postEvent_V(TAG, "Creating:ASR");

            ASR = new ListenerBase("configassets");
            ASR.configListener(this);
        }

        // Start the async task to initialize the tutor
        //
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onStart -- tutorConfigTask");
        new tutorConfigTask().execute();
    }


    /**
     *  requery DB Cursors here
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        logManager.postEvent_V(TAG, "RoboTutor:onRestart");
    }


    /**
     *  Deactivate DB Cursors here
     */
    @Override
    protected void onStop() {

        super.onStop();
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onStop");
        // Off-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStop");

        // Need to do this before releasing TTS
        //
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r4: killActiveTutor called from onStop()");
        CTutorEngine.killActiveTutor();

        if(TTS != null && TTS.isReady()) {

            logManager.postEvent_V(TAG, "flite:release");

            // TODO: This seems to cause a Flite internal problem???
            TTS.shutDown();
            TTS = null;
        }
    }


    /**
     * This callback is mostly used for saving any persistent state the activity is editing, to
     * present a "edit in place" model to the user and making sure nothing is lost if there are
     * not enough resources to start the new activity without first killing this one. This is also
     * a good place to do things like stop animations and other things that consume a noticeable
     * amount of CPU in order to make the switch to the next activity as fast as possible, or to
     * close resources that are exclusive access such as the camera.
     *
     */
    @Override
    protected void onPause() {

        super.onPause();
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onPause");
        logManager.postEvent_V(TAG, "RoboTutor:onPause");
    }


    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onResume");
        logManager.postEvent_V(TAG, "Robotutor:onResume");

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        String restoredText = prefs.getString("text", null);

        if (restoredText != null) {
        }
    }


    /**
     * In general onSaveInstanceState(Bundle) is used to save per-instance state in the activity
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState (Bundle outState) {

        super.onSaveInstanceState(outState);
        logManager.postEvent_V(TAG, "Robotutor:onSaveInstanceState");

//        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        int assetFullOrdinal = prefs.getInt(assetName + TCONST.ASSET_RELEASE_VERSION, 0);
//        int assetIncrOrdinal = prefs.getInt(assetName + TCONST.ASSET_UPDATE_VERSION, 0);
//
//        editor.putInt(assetName + TCONST.ASSET_UPDATE_VERSION , mAssetObject.getVersionField(INDEX_UPDATE, TCONST.ASSET_UPDATE_VERSION));
//        editor.apply();
    }


    @Override
    protected void onDestroy() {

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "rt: onDestroy");
        logManager.postEvent_V(TAG, "RoboTutor:onDestroy");

        Log.v(TAG, "isfinishing:" + isFinishing());

        super.onDestroy();

        if(TTS != null) {
            logManager.postEvent_V(TAG, "flite:release");

            TTS.shutDown();
            TTS = null;
        }

        logManager.postDateTimeStamp(GRAPH_MSG, "RoboTutor:SessionEnd");
        logManager.stopLogging();
        perfLogManager.stopLogging();

        // after logging, transfer logs to READY folder
        logManager.transferHotLogs(hotLogPath, readyLogPath);
        logManager.transferHotLogs(hotLogPathPerf, readyLogPathPerf);

    }

    private int getNextLogSequenceId() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // grab the current sequence id (the one we should use for this current run
        // of the app
        final int logSequenceId = prefs.getInt(LOG_SEQUENCE_ID, 0);

        // increase the log sequence id by 1 for the next usage
        prefs.edit()
                .putInt(LOG_SEQUENCE_ID, logSequenceId + 1)
                .apply();

        return logSequenceId;
    }

    /**
     * gets the stored data for each student based on STUDENT_ID.
     * YYY if this is a student's first time logging in, use PLACEMENT
     */
    public static SharedPreferences getStudentSharedPreferences() {
        // each ID name is composed of the STUDENT_ID plus the language i.e. EN or SW
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += CTutorEngine.language;

        //RoboTutor.logManager.postEvent_I(TAG, "Getting SharedPreferences: " + prefsName);
        return RoboTutor.ACTIVITY.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    /**
     * Get the promotion mode the student is currently in
     * @param matrix
     * @return
     */
    public static String getPromotionMode(String matrix) {

        SharedPreferences prefs = getStudentSharedPreferences();

        boolean placement;
        switch (matrix) {
            case MATH_MATRIX:
                placement = prefs.getBoolean(MATH_PLACEMENT, true);
                break;

            case LITERACY_MATRIX:
                placement = prefs.getBoolean(WRITING_PLACEMENT, true);
                break;

            case STORIES_MATRIX:
            case UNKNOWN_MATRIX:
            case SONGS_MATRIX:
            default:
                placement = false;
        }


        return placement ? "PLACEMENT" : "PROMOTION";

    }
}

