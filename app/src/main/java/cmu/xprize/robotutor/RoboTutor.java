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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.util.CAssetObject;
import cmu.xprize.util.CDisplayMetrics;
import cmu.xprize.util.CLoaderView;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.widgets.core.IGuidView;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CPreferenceCache;
import cmu.xprize.robotutor.startup.CStartView;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.util.TCONST.GRAPH_MSG;
import static cmu.xprize.util.TCONST.ROBOTUTOR_ASSET_PATTERN;


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

    private CTutorEngine        tutorEngine;
    private CMediaController    mMediaController;

    private CLoaderView         progressView;
    private CStartView          startView;

    public TTSsynthesizer       TTS = null;
    public ListenerBase         ASR;
    public IGlyphSink           LTKPlus = null;

    static public ITutorManager masterContainer;
    static public ILogManager   logManager;

    static CTutorAssetManager   tutorAssetManager;
    static public String        VERSION_RT;
    static public ArrayList     VERSION_SPEC;

    static public CDisplayMetrics displayMetrics;

    static public String        APP_PRIVATE_FILES;
    static public String        LOG_ID = "STARTUP";

    static public Activity      ACTIVITY;
    static public String        PACKAGE_NAME;
    static public boolean       DELETE_INSTALLED_ASSETS = false;

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern

    private boolean                 isReady       = false;
    private boolean                 engineStarted = false;
    static public boolean           STANDALONE    = false;
    static public String            SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;
//    static public String        SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;


    // TODO: This is a temporary log update mechanism - see below
    //
    static private IGuidView    guidCallBack;

    public final static String  LOG_PATH       = Environment.getExternalStorageDirectory() + TCONST.ROBOTUTOR_FOLDER;
    public final static String  DOWNLOAD_PATH  = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
    public final static String  EXT_ASSET_PATH = Environment.getExternalStorageDirectory() + File.separator + TCONST.ROBOTUTOR_ASSET_FOLDER;

    private final  String  TAG = "CRoboTutor";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Note = we don't want the system to try and recreate any of our views- always pass null
        //
        super.onCreate(null);


        // Catch all errors and cause a clean exit -
        // TODO: this doesn't work as expected
        //
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//
//            @Override
//            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
//
//                System.exit(2);
//            }
//        });


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

        logManager = CLogManager.getInstance();
        logManager.startLogging(LOG_PATH);
        CErrorManager.setLogManager(logManager);

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

        APP_PRIVATE_FILES = getApplicationContext().getExternalFilesDir("").getPath();

        // Initialize the JSON Helper STATICS - just throw away the object.
        //
        new JSON_Helper(getAssets(), CacheSource, RoboTutor.APP_PRIVATE_FILES);

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

            boolean result = false;

            try {
                // TODO: Don't do this in production
                // At the moment we always reinstall the tutor spec data - for development
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
                tutorAssetManager.updateAssetPackages(ROBOTUTOR_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH );

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
        startEngine();
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

        if((TTS != null && TTS.isReady()) &&
           (ASR != null && ASR.isReady()) && isReady) {

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
                tutorEngine = CTutorEngine.getTutorEngine(RoboTutor.this);

                // If running without built-in home screen add a start screen
                //
                if(STANDALONE) {

                    // TODO: This is a temporary log update mechanism - see below
                    //
                    masterContainer.addAndShow(startView);
                    startView.startTapTutor();
                    setFullScreen();
                }
                // Otherwise go directly to the sessionManager
                //
                else {
                    onStartTutor();
                }

            }
            // Note that it is possible for the masterContainer to be recreated without the
            // engine begin destroyed so we must maintain sync here.
            else {
                logManager.postEvent_V(TAG, "TutorEngine:Restarting");
            }
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

        tutorEngine.startSessionManager();

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

        tutorEngine.killActiveTutor();

        // Allow the screen to sleep when not in a session
        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // TODO: This is a temporary log update mechanism - see below
        //
        masterContainer.addAndShow(startView);
        startView.startTapTutor();
        setFullScreen();

        if(tutorEngine != null) {
            if(tutorEngine.onBackButton()) {
                super.onBackPressed();
            }
        }
    }



    /***  State Management  ****************/


    /**
     *
     */
    @Override
    protected void onStart() {

        super.onStart();

        // On-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStart");

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
        // Off-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStop");

        // Need to do this before releasing TTS
        //
        tutorEngine.killActiveTutor();

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
        logManager.postEvent_V(TAG, "RoboTutor:onPause");

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
    }


    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();
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
    }



}

