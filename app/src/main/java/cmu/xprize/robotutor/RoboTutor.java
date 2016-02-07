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
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;


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
public class RoboTutor extends Activity {

    private CTutorEngine    tutorEngine;
    private ITutorSceneImpl tutorContainer;

    static public String EXTERNFILES;

    private final String TAG = "RoboTutor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.robo_tutor);

        // Get the primary container for tutor scenes
        tutorContainer = (ITutorSceneImpl)findViewById(R.id.tutor_manager);

        EXTERNFILES = getApplicationContext().getExternalFilesDir("").getPath();

        // [kw] TODO: Put this off in a worker thread and let the UI continue.
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

        } catch (IOException e) {
            e.printStackTrace();
        }
        // [kw] TODO: Put this off in a worker thread and let the UI continue.  ^^^^^

        // Initialize the Engine - set the EXTERN File path for file installs
        // Load the default tutor defined in assets/tutors/engine_descriptor.json
        // TODO: Handle tutor creation failure
        tutorEngine = CTutorEngine.getTutorEngine(this, tutorContainer);
        tutorEngine.initialize();
    }

}
