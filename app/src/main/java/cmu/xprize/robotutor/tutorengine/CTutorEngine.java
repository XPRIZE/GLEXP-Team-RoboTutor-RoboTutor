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

package cmu.xprize.robotutor.tutorengine;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.BuildConfig;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.graph.defdata_scenes;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.widgets.core.TSceneAnimatorLayout;
import cmu.xprize.util.CLogManager;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.RoboTutor;

/**
 * The tutor engine provides top-levelFolder control over the tutor lifecycle and can support multiple
 * simultaneous tutors.  On creation the tutor engine will instantiate and launch the DefTutor
 * specified in the TCONST.EDESC Json tutor engine specification file.
 *
 * CTutorEngine is a singleton
 *
 */
public class CTutorEngine implements ILoadableObject2 {

    private static TScope                   mRootScope;

    private static CTutorEngine             singletonTutorEngine;

    private CMediaManager                   mMediaManager;

    static public  RoboTutor                Activity;
    static public  ILogManager              TutorLogManager;

    static private HashMap<String,CTutor>   tutorMap        = new HashMap<>();
    static private CTutor                   activeTutor     = null;
    static private CTutor                   deadTutor       = null;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // json loadable
    static public String                         defTutor;
    static public HashMap<String, defdata_tutor> defDataSources;
    static public String                         defFeatures;
    static public String                         language;                       // Accessed from a static context


    final static private String TAG         = "CTutorEngine";


    /**
     * TutorEngine is a Singleton
     *
     * Load and generate the root tutor - This root tutor may be a single-topic tutor
     * or a mananger interface "tutor" permitting access to other sub-tutors.  So if you have complex
     * content management (i.e. student models) it/they should be embodied in the manager interface
     * component logic.
     *
     * @param context
     */
    private CTutorEngine(RoboTutor context) {

        mRootScope      = new TScope(null, "root", null);

        Activity        = context;
        TutorLogManager = CLogManager.getInstance();

        // Load the TCONST.EDESC and generate the root tutor
        //
        loadEngineDescr();
    }


    /**
     * Retrieve the one and only tutorEngine object
     *
     * @param context
     * @return
     */
    static public CTutorEngine getTutorEngine(RoboTutor context) {

        if(singletonTutorEngine == null) {
            singletonTutorEngine = new CTutorEngine(context);
        }

        return singletonTutorEngine;
    }


    /**
     * This is primarily intended as a development API to allow updating the working language
     * at runtime.
     * @param newLang
     */
    static public void setDefaultLanguage(String newLang) {
        language = newLang;
    }


    /**
     * This is primarily intended as a development API to allow updating the working language
     * at runtime.
     */
    static public String getDefaultLanguage() {
        return language;
    }


    /**
     * Called from the Activity when the back button is pressed.
     *
     */
    public boolean onBackButton() {
        boolean result = false;

        return result;
    }


    static public TScope getScope() {

        return mRootScope;
    }


    static public Activity getActivity() {
        return Activity;
    }


    static public void pauseTutor() {

    }


    /**
     *  Used to destroy all tutors when the system calls onDestroy for the app
     *
     */
    static public void killAllTutors() {

        while(tutorMap.size() > 0) {

            Iterator<?> tutorObjects = tutorMap.entrySet().iterator();

            Map.Entry entry = (Map.Entry) tutorObjects.next();

            CTutor tutor = ((CTutor) (entry.getValue()));

            // Note the endTutor call will invalidate this iterator so recreate it
            // on each pass
            //
            //tutor.terminateQueue();
            //tutor.endTutor();
        }

        singletonTutorEngine = null;
    }


    static public void startSessionManager() {

        defdata_tutor tutorBindings = null;

        if(defDataSources != null) {
            tutorBindings = defDataSources.get(defTutor);
        }

        // Sample: how to launch a tutor with a json datasource
//        String datas = "{\"scene_bindings\" : {\"session_manager\": {\"type\": \"SCENEDATA_MAP\", \"databindings\": [{\"name\": \"SsmComponent\",\"datasource\": \"[file]sm_data.json\"}]}}}";
//        launch(defTutor, "native", datas, "" );

        createTutor(defTutor, defFeatures);
        launchTutor(tutorBindings);
    }

    /**
     * Here a tutor is destroying itself - so we need to manage the follow-on process -
     * i.e. start some other activity / tutor or session mamagement task.
     */
    static public void destroyCurrentTutor() {

        // When using the back button within a native tutor we will be killing the one and
        // only tutor so deadTutor will be null
        //
        deadTutor   = activeTutor;
        activeTutor = null;
        RoboTutor.masterContainer.removeView(deadTutor.getTutorContainer());

        startSessionManager();

        Log.d(TAG, "destroyCurrentTutor: " + deadTutor.getTutorName());

        // Get the tutor being killed and do a depth first destruction to allow
        // components to release resources etc.
        //
        deadTutor.onDestroy();
        deadTutor = null;
    }


    /**
     * Here a tutor has been killed off externally and need to be cleaned up.
     */
    static public void killDeadTutor() {

        Log.d(TAG, "killDeadTutor: " + deadTutor.getTutorName());

        // Get the tutor being killed and do a depth first destruction to allow
        // components to release resources etc.
        //
        deadTutor.onDestroy();
        deadTutor = null;
    }


    /**
     * Here a tutor is being destroying externally
     */
    static public void killActiveTutor() {

        if(activeTutor != null) {

            deadTutor = activeTutor;

            activeTutor = null;

            Log.d(TAG, "Killing Tutor: " + deadTutor.getTutorName());

            RoboTutor.masterContainer.removeView(deadTutor.getTutorContainer());
            deadTutor.post(TCONST.KILLTUTOR);
        }
    }


    /**
     * Create a tutor by name - if a tutor is running already then kill it off first
     *
     * @param tutorName
     * @param features
     */
    static private void createTutor(String tutorName, String features) {

        killActiveTutor();

        Log.d(TAG, "createTutor: " + tutorName);

        // Create a new tutor container relative to the masterContainer
        //
        ViewGroup tutorContainer = new TSceneAnimatorLayout(Activity);
        tutorContainer.inflate(Activity, R.layout.scene_layout, null);
        ((ITutorObject)tutorContainer).setName("tutor_container");

        RoboTutor.masterContainer.addView((ITutorManager)tutorContainer);

        activeTutor = new CTutor(Activity, tutorName, (ITutorManager)tutorContainer, TutorLogManager, mRootScope, language, features);
    }

    /**
     *  Note: You must call createTutor at some point prior to this call
     */
    static private void launchTutor(defdata_tutor dataSource) {

        activeTutor.launchTutor(dataSource);
    }


    /**
     *  Scriptable Launch command
     *
     * @param intent
     * @param intentData
     * @param features
     */
    static public void launch(String intent, String intentData, String dataSourceJson, String features ) {

        defdata_tutor dynamicDataSource = null;

        Intent extIntent = new Intent();
        String extPackage;

        // Allow the intent to override any engine levelFolder datasource defaults
        //
        if(!dataSourceJson.equals(TCONST.NO_DATASOURCE)) {

            dynamicDataSource = new defdata_tutor();

            try {
                dynamicDataSource.loadJSON(new JSONObject(dataSourceJson), (IScope2)mRootScope);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // If no datasource defined in the external launch request then try and find a engine levelFolder default
        //
        else {
            if(defDataSources != null) {
                dynamicDataSource = defDataSources.get(intent);
            }
        }

        switch(intentData) {

            // Create a native tutor with the given base features
            // These features are used to determine basic tutor functionality when
            // multiple tutors share a single scenegraph
            //
            case "native":
                createTutor(intent, features);
                launchTutor(dynamicDataSource);
                break;

            case "browser":

                extIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + intent));

                getActivity().startActivity(extIntent);

                break;

            default:

                // This a special allowance for MARi which placed their activities in a different
                // package from their app - so we check for intent of the form "<pkgPath>:<appPath>"
                //
                String[] intentParts = intent.split(":");

                // If it is "<pkgPath>:<appPath>"
                //
                if(intentParts.length > 1) {
                    extPackage = intentParts[0];
                    intent     = intentParts[1];
                }
                // Otherwise we expect the activities to be right off the package.
                //
                else {
                    extPackage = intent.substring(0, intent.lastIndexOf('.'));
                }

                extIntent.setClassName(extPackage, intent);
                extIntent.putExtra("intentdata", intentData);
                extIntent.putExtra("features", features);

                try {
                    getActivity().startActivity(extIntent);
                }
                catch(Exception e) {
                    Log.e(TAG, "Launch Error: " + e + " : " + intent);
                }
                break;
        }
    }


    //************ Serialization


    /**
     * Load the Tutor engine specification from JSON file data
     * from assets/tutors/engine_descriptor.json
     *
     */
    public void loadEngineDescr() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.EDESC)), (IScope2)mRootScope);

            // TODO : Use build Variant to ensure release configurations
            //
            if(BuildConfig.LANGUAGE_OVERRIDE) {
                language = BuildConfig.LANGUAGE_FEATURE_ID;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Load the Tutor specification from JSON file data
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope2 scope) {

      JSON_Helper.parseSelf(jsonData, this, CClassMap2.classMap, scope);
    }
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
