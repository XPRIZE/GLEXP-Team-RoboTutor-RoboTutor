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

package cmu.xprize.robotutor.tutorengine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.graph.databinding;
import cmu.xprize.robotutor.tutorengine.graph.defdata_scenes;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.defvar_tutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.util.PromotionMechanism;
import cmu.xprize.robotutor.tutorengine.util.StudentDataModel;
import cmu.xprize.robotutor.tutorengine.util.TransitionMatrixModel;
import cmu.xprize.robotutor.tutorengine.widgets.core.TSceneAnimatorLayout;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.LANG_EN;

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

    public static StudentDataModel          studentModel;
    public static TransitionMatrixModel     matrix;
    public static PromotionMechanism        promotionMechanism;
    public enum MenuType {STUDENT_CHOICE, CYCLE_CONTENT};
    public static MenuType menuType;

    static public  RoboTutor                Activity;
    static public  ILogManager              TutorLogManager;

    static private HashMap<String,CTutor>   tutorMap        = new HashMap<>();
    static private CTutor                   activeTutor     = null;
    static private CTutor                   deadTutor       = null;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // must match incoming json data
    //
    private String                          EXPECTED_VERSION = "1.0";

    // json loadable
    static public String                         descr_version;                 //
    static public String                         defTutor; // defined in engine_descriptor.json
    static public HashMap<String, defvar_tutor>  tutorVariants;
    static public HashMap<String, defdata_tutor> bindingPatterns;
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
        //  any time the language changes, so should the Transition Matrix and the Student Model
        matrix = loadTransitionMatrixModel();
        studentModel = loadStudentModel(matrix);
        promotionMechanism = new PromotionMechanism(studentModel, matrix);
        menuType = getMenuType();
    }


    /**
     * This is primarily intended as a development API to allow updating the working language
     * at runtime.
     */
    static public String getDefaultLanguage() {
        return language;
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

        if(bindingPatterns != null) {
            tutorBindings = bindingPatterns.get(defTutor);
        }

        // These features are based on the current tutor selection model
        // When no tutor has been selected it should run the tutor select
        // and when it finishes it should run the difficulty select until
        // the user wants to select another tutor.
        //

        // Update the tutor id shown in the log stream
        //
        CLogManager.setTutor(defTutor);

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "**: Creating Tutor in startSessionManager: " + defTutor);

        // don't create a new tutor when the screen is off, because on relaunch it will set-in-motion the "KILLTUTOR"
        // https://stackoverflow.com/questions/2474367/how-can-i-tell-if-the-screen-is-on-in-android
        PowerManager powerManager = (PowerManager) Activity.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (powerManager != null && !powerManager.isInteractive()) {
                Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "fx: Not creating new tutor when screen is off...");
                return;
            }
        }

        createAndLaunchTutor(defTutor, RoboTutor.SELECTOR_MODE, null, tutorBindings, null); // where Activity Selector is launched
    }

    /**
     *
     * This launches a new tutor immediately at startup. Used for quick debugging.
     */
    static public void quickLaunch(String tutorVariant, String tutorId, String tutorFile) {

        for (String name: tutorVariants.keySet()){

            String key =name.toString();
            String value = tutorVariants.get(name).tutorName;
            String feats = tutorVariants.get(name).features;
            System.out.println(key + " tutorName: " + value + " Features: " + feats);

        }
        String value = tutorVariants.get(tutorVariant).tutorName;
        String feats = tutorVariants.get(tutorVariant).features;
        System.out.println(tutorVariant + " tutorName: " + value + " Features: " + feats);
        defvar_tutor  tutorDescriptor = tutorVariants.get(tutorVariant);
        defdata_tutor tutorBinding    = bindingPatterns.get(tutorDescriptor.tutorName);

        initializeBindingPattern(tutorBinding, tutorFile);

        createAndLaunchTutor(tutorDescriptor.tutorName , tutorDescriptor.features, tutorId, tutorBinding, null);
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

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "p4: StartSessionManager in 'CTutorEngine.destroyCurrentTutor': " + defTutor);

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

        // GRAY_SCREEN_BUG
        if(activeTutor != null) {

            deadTutor = activeTutor;

            activeTutor = null;

            Log.d(TAG, "Killing Tutor: " + deadTutor.getTutorName());

            RoboTutor.masterContainer.removeView(deadTutor.getTutorContainer());
            Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r3: Killing Active Tutor: " + deadTutor.getTutorName());
            deadTutor.post(TCONST.KILLTUTOR);
        } else {
            Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r3: NULL ACTIVE TUTOR. DeadTutor = " + ((deadTutor == null) ? "null" : deadTutor.getTutorName()));
        }
    }


    /**
     * Create a tutor by name - if a tutor is running already then kill it off first
     *
     * @param tutorName
     * @param features
     */
    static private void createAndLaunchTutor(String tutorName, String features, String tutorId, defdata_tutor dataSource, String matrix) {

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r4: killActiveTutor called from createAndLaunchTutor(" + tutorName + ")");
        killActiveTutor();

        // GRAY_SCREEN_BUG
        Log.d(TAG, "createAndLaunchTutor: " + tutorName + ", " + tutorId);

        // Create a new tutor container relative to the masterContainer
        //
        ViewGroup tutorContainer = new TSceneAnimatorLayout(Activity);
        tutorContainer.inflate(Activity, R.layout.scene_layout, null);
        ((ITutorObject)tutorContainer).setName("tutor_container");

        RoboTutor.masterContainer.addView((ITutorManager)tutorContainer);

        // GRAY_SCREEN_BUG CTutor created --> Media Manager created --> added to map
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "p2: Initializing tutor: " + tutorName);

        activeTutor = new CTutor(Activity, tutorName, tutorId, (ITutorManager)tutorContainer, TutorLogManager, mRootScope, language, features, matrix);

        activeTutor.launchTutor(dataSource);
    }


    static private defdata_scenes parseSceneData(defdata_tutor dataPattern, String[] componentSet) {

        defdata_scenes          sceneData = new defdata_scenes();
        ArrayList<databinding>  bindings  = new ArrayList<>();

        String compData   = null;
        String compName   = null;

        for(String component : componentSet) {

            String[] dataSet = component.split(":");

            if (dataSet.length == 1) {

                compName = "*";
                compData = dataSet[0];

            } else {
                compName = dataSet[0];
                compData = dataSet[1];
            }

            bindings.add(new databinding(compName, compData));
        }

        sceneData.databindings = (databinding[]) bindings.toArray(new databinding[bindings.size()]);

        return sceneData;
    }


    static private defdata_tutor parseDataSpec(String dataSpec) {

        defdata_tutor   dataPattern = new defdata_tutor();
        defdata_scenes  sceneData   = null;
        String          sceneName   = null;

        String[] sceneSet = dataSpec.split(";");

        for(String scene : sceneSet) {

            String[] sceneElements = scene.split("\\|");

            // If there is only 1 element then there is only one scene and its name is implied
            //
            if(sceneElements.length == 1) {

                sceneName = "*";
                sceneData = parseSceneData(dataPattern, sceneElements);
            }
            else {
                sceneName     = sceneElements[0];
                sceneElements = Arrays.copyOfRange(sceneElements, 1, sceneElements.length);

                sceneData = parseSceneData(dataPattern, sceneElements);
            }

            dataPattern.scene_bindings.put(sceneName, sceneData);
        }

        return dataPattern;
    }


    static private void  initComponentBindings(databinding[] targetbindings, databinding[] databindings) {

        for(databinding binding : databindings) {

            if(binding.name.equals("*")) {
                if(targetbindings.length == 1) {
                    targetbindings[0].datasource = binding.datasource;
                }
                else {
                    Log.e(TAG, "ERROR: Incompatible datasource");
                }
            }
            else {
                for(databinding tbinding : targetbindings) {
                    if(tbinding.name.equals(binding.name)) {
                        tbinding.datasource = binding.datasource;
                        break;
                    }
                }
            }

        }
    }


    static private void  initSceneBindings(defdata_tutor bindingPattern, String sceneName, databinding[] databindings) {

        if(sceneName.equals("*")) {
            System.out.println(bindingPattern.scene_bindings.isEmpty());
            if(bindingPattern.scene_bindings.size() == 1) {

                Iterator<?> scenes = bindingPattern.scene_bindings.entrySet().iterator();
                while(scenes.hasNext() ) {

                    Map.Entry scene = (Map.Entry) scenes.next();

                    databinding[] scenebindings = ((defdata_scenes)scene.getValue()).databindings;

                    initComponentBindings(scenebindings, databindings);
                }
            }
            else {
                Log.e(TAG, "ERROR: Incompatible datasource");
            }
        }
        else {
            defdata_scenes compData = bindingPattern.scene_bindings.get(sceneName);

            initComponentBindings(compData.databindings, databindings);
        }

    }


    /**
     * The data spec is encoded as:
     *
     *  <dataspec>...
     *  <dataspec>  = scenename|<scenedata>...
     *  <scenedata> = component:datasource
     *
     *  e.g.
     *      tutor_scene1|sceme_compD:[dataencoding]datasource|sceme_compM:[dataencoding]datasource;
     *      tutor_scene2|sceme_compQ:[dataencoding]datasource; ...
     *
     *
     *
     * @param bindingPattern
     * @param dataSpec
     */
    static private void initializeBindingPattern(defdata_tutor bindingPattern, String dataSpec) {
        System.out.print("dataSpec: ");
        System.out.println(dataSpec);
        defdata_tutor dataBindings = parseDataSpec(dataSpec);

        Iterator<?> scenes = dataBindings.scene_bindings.entrySet().iterator();

        while(scenes.hasNext() ) {

            Map.Entry scene = (Map.Entry) scenes.next();

            String sceneName           = (String)scene.getKey();
            databinding[] databindings = ((defdata_scenes)scene.getValue()).databindings;

            initSceneBindings(bindingPattern, sceneName, databindings);
        }
    }


    /**
     *  Scriptable Launch command
     *
     * @param tutorVariant
     * @param intentType
     */
    static public void launch(String intentType, String tutorVariant, String dataSource, String tutorId, String matrix) {

        Log.d(TAG, "launch: tutorId=" + tutorId);

        Intent extIntent = new Intent();
        String extPackage;

        defvar_tutor  tutorDescriptor = tutorVariants.get(tutorVariant);
        defdata_tutor tutorBinding    = bindingPatterns.get(tutorDescriptor.tutorName);

        // Initialize the tutorBinding from the dataSource spec - this transfers the
        // datasource fields to the prototype tutorVariant bindingPattern which is then
        // used to initialize the tutor itself.
        //
        initializeBindingPattern(tutorBinding, dataSource);

        switch(intentType) {

            // Create a native tutor with the given base features
            // These features are used to determine basic tutor functionality when
            // multiple tutors share a single scenegraph
            //
            case "native":

                Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "p3b: Creating Tutor in 'CTutor.launch': " + tutorDescriptor.tutorName);
                createAndLaunchTutor(tutorDescriptor.tutorName, tutorDescriptor.features, tutorId, tutorBinding, matrix);
                break;

            case "browser":

                extIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + tutorVariant));

                getActivity().startActivity(extIntent);

                break;

            default:

                // This a special allowance for MARi which placed their activities in a different
                // package from their app - so we check for intent of the form "<pkgPath>:<appPath>"
                //
                String[] intentParts = tutorVariant.split(":");

                // If it is "<pkgPath>:<appPath>"
                //
                if(intentParts.length > 1) {
                    extPackage = intentParts[0];
                    tutorVariant     = intentParts[1];
                }
                // Otherwise we expect the activities to be right off the package.
                //
                else {
                    extPackage = tutorVariant.substring(0, tutorVariant.lastIndexOf('.'));
                }

                extIntent.setClassName(extPackage, tutorVariant);
                extIntent.putExtra("intentdata", intentType);

                try {
                    getActivity().startActivity(extIntent);
                }
                catch(Exception e) {
                    Log.e(TAG, "Launch Error: " + e + " : " + tutorVariant);
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
            if(Configuration.languageOverride(getActivity())) {
                language = Configuration.getLanguageFeatureID(getActivity());
                //  any time the language changes, so should the Transition Matrix and the Student Model
            } else {
                language = Configuration.getLanguageFeatureID(getActivity());
                if (language.equals("LANG_NULL")) {
                    language = TCONST.LANG_SW;
                }
            }

            matrix = loadTransitionMatrixModel();
            studentModel = loadStudentModel(matrix);
            promotionMechanism = new PromotionMechanism(studentModel, matrix);
            menuType = getMenuType();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static MenuType getMenuType() {
        return (RoboTutor.OLD_MENU || CTutorEngine.language.equals(LANG_EN)) ?
                MenuType.STUDENT_CHOICE : MenuType.CYCLE_CONTENT;
    }

    /**
     * Loads the student data model. If the student has not played before, initialize with
     * the default starting positions based on {@code matrix}
     *
     * @param matrix what the activity matrix looks like.
     * @return
     */
    private static StudentDataModel loadStudentModel(TransitionMatrixModel matrix) {
        // initialize
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += CTutorEngine.language;
        StudentDataModel model = new StudentDataModel(RoboTutor.ACTIVITY, prefsName);

        // if it's the first time playing, we want to initialize our placement values
        String firstTime = model.getHasPlayed();
        if (firstTime == null) {
            model.createNewStudent();
            model.initializeTutorPositions(matrix);
        }

        return model;
    }

    /**
     *  load the transition matrix model from JSON
     * @return
     */
    private static TransitionMatrixModel loadTransitionMatrixModel() {

        // this is whack and should be moved... see "activity_selector/tutor_descriptor.json"
        String tutorName = "activity_selector";
        //String dataFile = "dev_data.json";
        String dataFile = RoboTutor.MATRIX_FILE;

        // simpler way to refer to languge
        String lang = TCONST.langMap.get(CTutorEngine.language);

        String dataPath = TCONST.TUTORROOT + "/" + tutorName + "/" + TCONST.TASSETS;
        dataPath += "/" +  TCONST.DATA_PATH + "/" + lang + "/";

        String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

        //
        // Load the datasource into a separate class...
        TransitionMatrixModel matrix = new TransitionMatrixModel(dataPath + dataFile, mRootScope);
        matrix.validateAll();
        return matrix;
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
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
