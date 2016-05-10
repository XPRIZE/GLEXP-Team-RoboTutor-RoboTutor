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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.widgets.core.TTextView;
import cmu.xprize.util.CPreferenceCache;
import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.ProgressLoading;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;
import cmu.xprize.util.TTSsynthesizer;
import cmu.xprize.util.Word2NumFSM;
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
public class RoboTutor extends Activity implements IReadyListener {

    private CTutorEngine        tutorEngine;
    private ITutorManager       tutorContainer;
    private ProgressLoading     progressLoading;
    private CMediaManager       mMediaManager;

    public TTSsynthesizer       TTS;
    public ListenerBase         ASR;
    static public String        EXTERNFILES;

    private boolean             isReady = false;


    private final  String  TAG = "CRoboTutor";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");

        setContentView(R.layout.robo_tutor);

        // Get the primary container for tutors
        tutorContainer = (ITutorManager)findViewById(R.id.tutor_container);

        EXTERNFILES = getApplicationContext().getExternalFilesDir("").getPath();

        // Initialize the media manager singleton - it needs access to the App assets.
        //
        mMediaManager = CMediaManager.getInstance();

        // Show the loader
        //
        progressLoading = new ProgressLoading(this);
        progressLoading.show();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.i(TAG, "onDestroy: ");


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
                if(CTutorEngine.CacheSource.equals(TCONST.EXTERN)) {
                    tutorAssetManager.installAssets(TCONST.TUTORROOT);
                    Log.i(TAG, "INFO: Tutor Assets installed:");
                }

                if(!tutorAssetManager.fileCheck(TCONST.INSTALL_FLAG)) {
                    tutorAssetManager.installAssets(TCONST.LTK_ASSETS);
                    Log.i(TAG, "INFO: LTK Assets copied:");

                    tutorAssetManager.extractAsset(TCONST.LTK_DATA_FILE, TCONST.LTK_DATA_FOLDER);
                    Log.i(TAG, "INFO: LTK Assets installed:");
                }
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

        Log.i(TAG, "onServiceReady: " + serviceName + " : is : " + status);

        // As the services come online push a global reference to CTutor
        //
        switch(serviceName) {
            case TCONST.TTS:
                mMediaManager.setTTS(TTS);
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

        Log.i(TAG, "startEngine");

        if(TTS.isReady() && ASR.isReady() && isReady) {

            // Delete the asset loader utility ASR object
            ASR = null;

            progressLoading.hide();

            // Initialize the Engine - set the EXTERN File path for file installs
            // Load the default tutor defined in assets/tutors/engine_descriptor.json
            // TODO: Handle tutor creation failure
            tutorEngine = CTutorEngine.getTutorEngine(RoboTutor.this, tutorContainer);
        }
    }


    /**
     * TODO: Manage the back button
     */
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");

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
        Log.i(TAG, "onStart Robotutor: On-Screen");

        // Debug - determine platform dependent memory limit
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memAvail = am.getMemoryClass();
        Log.i(TAG, "Available Memory: " + memAvail);

        // Create the common TTS service
        // Async
        //
        TTS = new TTSsynthesizer(this);
        TTS.initializeTTS(this);

        // Create an inert listener for asset initialization only
        // Start the configListener async task to update the listener assets only if required.
        // This moves the listener assets to a local folder where they are accessible by the
        // NDK code (PocketSphinx)
        //
        ASR = new ListenerBase("configassets");
        ASR.configListener(this);

        // Start the async task to initialize the tutor
        //
        new tutorConfigTask().execute();

        // Update the globally accessible id object for this engine instance.
        //
        CPreferenceCache.initLogPreference(this);


    }


    /**
     *  requery DB Cursors here
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart Robotutor");
    }


    /**
     *  Deactivate DB Cursors here
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop Robotutor: Off-Screen");
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
        Log.i(TAG, "onPause Robotutor");

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
    }


    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();
        Log.i(TAG, "Resuming Robotutor");

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
        super.onResume();
        Log.i(TAG, "onSaveInstanceState Robotutor");


    }


}

