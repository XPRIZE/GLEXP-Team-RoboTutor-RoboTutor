//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.util.CLoaderView;
import cmu.xprize.util.CLogManager;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.widgets.core.IGuidView;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.CPreferenceCache;
import cmu.xprize.robotutor.startup.CStartView;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.ListenerBase;


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

    static public String        APP_PRIVATE_FILES;
    static public String        LOG_ID = "STARTUP";

    static public float         designDensity   = 2.0f;
    static public float         instanceDensity;
    static public float         densityRescale;

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern

    private boolean             isReady       = false;
    private boolean             engineStarted = false;

    // TODO: This is a temporary log update mechanism - see below
    //
    static private IGuidView    guidCallBack;

    public final static String  LOG_PATH = Environment.getExternalStorageDirectory() + TCONST.ROBOTUTOR_FOLDER;

    private final  String  TAG = "CRoboTutor";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Note = we don't want the system to try and recreate any of our views- always pass null
        super.onCreate(null);

        // Prep the CPreferenceCache
        // Update the globally accessible id object for this engine instance.
        //
        LOG_ID = CPreferenceCache.initLogPreference(this);

        // get the multiplier used for drawables at the current screen density and calc the
        // correction rescale factor for design scale
        //
        instanceDensity = getResources().getDisplayMetrics().density;
        densityRescale  = designDensity / instanceDensity;

        logManager = CLogManager.getInstance();
        logManager.startLogging(LOG_PATH);
        CErrorManager.setLogManager(logManager);

        // TODO : implement time stamps
        logManager.postTimeStamp("Session Start");
        logManager.postEvent(TAG, "onCreate: ");

        // Get the primary container for tutors
        setContentView(R.layout.robo_tutor);
        masterContainer = (ITutorManager)findViewById(R.id.master_container);

        setFullScreen();

        APP_PRIVATE_FILES = getApplicationContext().getExternalFilesDir("").getPath();

        // Initialize the JSON Helper statics - just throw away the object.
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

        // Show the loader
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
        logManager.postEvent(TAG, "onRestoreInstanceState" + bundle);
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
     * Moves new assets to an external folder so the Sphinx code can access it.
     *
     */
    class tutorConfigTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... unused) {

            boolean result = false;

            CTutorAssetManager tutorAssetManager = new CTutorAssetManager(getApplicationContext());

            try {
                // TODO: Don't do this in production
                // At the moment we always reinstall the tutor spec data - for development
                if(CacheSource.equals(TCONST.EXTERN)) {
                    tutorAssetManager.installAssets(TCONST.TUTORROOT);
                    logManager.postEvent(TAG, "INFO: Tutor Assets installed:");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_PROJECT_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_PROJEXCTS);
                    logManager.postEvent(TAG, "INFO: LTK Projects installed:");

                    // Note the Projects Zip file is anticipated to contain a folder called "projects"
                    // containing the ltk data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_PROJEXCTS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent(TAG, "INFO: LTK Projects extracted:");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_GLYPH_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_GLYPHS);
                    logManager.postEvent(TAG, "INFO: LTK Glyphs installed:");

                    // Note the Glyphs Zip file is anticipated to contain a folder called "glyphs"
                    // containing the ltk glyph data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_GLYPHS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent(TAG, "INFO: LTK Glyphs extracted:");
                }

                // Create the one system level LTKPLUS recognizer
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

        logManager.postEvent(TAG, "onServiceReady: " + serviceName + " : is : " + status);

        // As the services come online push a global reference to CTutor
        //
        switch(serviceName) {
            case TCONST.TTS:
                logManager.postEvent(TAG, "Attaching to Flite");

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

                logManager.postEvent(TAG, "Starting TutorEngine");

                // Delete the asset loader utility ASR object
                ASR = null;

                masterContainer.removeView(progressView);

                // Initialize the Engine - set the EXTERN File path for file installs
                // Load the default tutor defined in assets/tutors/engine_descriptor.json
                // TODO: Handle tutor creation failure
                //
                tutorEngine = CTutorEngine.getTutorEngine(RoboTutor.this);

                // TODO: This is a temporary log update mechanism - see below
                //
                masterContainer.addAndShow(startView);
                startView.startTapTutor();
                setFullScreen();
            }
            // Note that it is possible for the masterContainer to be recreated without the
            // engine begin destroyed so we must maintain sync here.
            else {
                logManager.postEvent(TAG, "Restarting TutorEngine");
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

        logManager.postEvent(TAG, "Updated Logging GUID: " + LOG_ID);
        LOG_ID = CPreferenceCache.initLogPreference(this);
        logManager.startLogging(LOG_PATH);

        tutorEngine.startSessionManager();

        startView.flushQueue();
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
        logManager.postEvent(TAG, "onBackPressed");
        logManager.stopLogging();

        tutorEngine.killActiveTutor();

        // Allow the screen to sleep when not in a session
        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // TODO: This is a temporary log update mechanism - see below
        //
        masterContainer.addAndShow(startView);
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
        logManager.postEvent(TAG, "onStart Robotutor: On-Screen");

        // We only want to run the engine start sequence once per onStart call
        //
        engineStarted = false;

        // Debug - determine platform dependent memory limit
        //
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memAvail       = am.getMemoryClass();

        logManager.postEvent(TAG, "Available Memory: " + memAvail);

        // Create the common TTS service
        // Async
        //
        if(TTS == null) {

            logManager.postEvent(TAG, "Creating New TTS");

            TTS = new TTSsynthesizer(this);
            TTS.initializeTTS(this);
        }

        // Create an inert listener for asset initialization only
        // Start the configListener async task to update the listener assets only if required.
        // This moves the listener assets to a local folder where they are accessible by the
        // NDK code (PocketSphinx)
        //
        if(ASR == null) {

            logManager.postEvent(TAG, "Creating New ASR");

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
        logManager.postEvent(TAG, "onRestart Robotutor");
    }


    /**
     *  Deactivate DB Cursors here
     */
    @Override
    protected void onStop() {
        super.onStop();
        logManager.postEvent(TAG, "onStop Robotutor: Off-Screen");

        // Need to do this before releasing TTS
        //
        tutorEngine.killActiveTutor();

        if(TTS != null && TTS.isReady()) {

            logManager.postEvent(TAG, "Releasing Flite");

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
        logManager.postEvent(TAG, "onPause Robotutor");

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
    }


    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();
        logManager.postEvent(TAG, "Resuming Robotutor");

        SharedPreferences prefs = getPreferences(0);

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
        logManager.postEvent(TAG, "onSaveInstanceState Robotutor");
    }


    @Override
    protected void onDestroy() {

        logManager.postEvent(TAG, "onDestroy Glyph_Recognizer: isfinishing - " + isFinishing());

        super.onDestroy();

        if(TTS != null) {
            logManager.postEvent(TAG, "Releasing Flite");

            TTS.shutDown();
            TTS = null;
        }

        logManager.postTimeStamp("Session End");
        logManager.stopLogging();
    }



}

