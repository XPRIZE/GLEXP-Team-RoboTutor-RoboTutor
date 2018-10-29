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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CPreferenceCache;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.scene_initializer;
import cmu.xprize.robotutor.tutorengine.graph.type_action;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

import static cmu.xprize.robotutor.tutorengine.CTutorEngine.studentModel;
import static cmu.xprize.util.TCONST.GRAPH_MSG;


/**
 *  Each Tutor instance is represented by a CTutor
 *
 */
public class CTutor implements ILoadableObject2, IEventSource {

    private boolean traceMode = false;

    // This is the local tutor scope in which all top levelFolder objects and variables are defined
    // May have child scopes for local variables -

    private TScope                        mTutorScope;
    private CMediaManager                 mMediaManager;

    private HashMap<String, ITutorScene>  mScenes  = new HashMap<String, ITutorScene>();
    private HashMap<String, ITutorObject> mObjects = new HashMap<String, ITutorObject>();

    private ArrayList<String>            fFeatures = new ArrayList<String>();
    private ArrayList<String>            fDefaults = new ArrayList<String>();

    public Context                       mContext;
    public ILogManager                   mTutorLogManager;
    public ITutorGraph                   mTutorGraph;
    public CSceneGraph                   mSceneGraph;
    public ITutorManager                 mTutorContainer;
    public ViewGroup                     mSceneContainer;

    public String                        mTutorName = "";
    public String                        mTutorId = "";
    public AssetManager                  mAssetManager;
    public boolean                       mTutorActive = false;

    private int                                 _framendx = 0;
    private HashMap<String, scene_initializer>  _sceneMap = new HashMap<String, scene_initializer>();

    private final Handler                mainHandler = new Handler(Looper.getMainLooper());
    private HashMap                      queueMap    = new HashMap();
    private boolean                      mDisabled   = false;

    private UUID uuid;

    /**
     * for tracking student score within an activity
     * REVIEW build into existing architecture more fluidly
     */
    private int score = 0;
    private int incorrect = 0;
    private int attempts = 0;
    private int totalQuestions; // total possible questions to answer in data source

    // json loadable
    public scene_initializer[]              scenedata;
    public defdata_tutor                    dataSource = null;
    public String                           language;
    public String                           navigatorType;
    public HashMap<String,CMediaPackage>    soundMap;

    public String matrix;

    public String engineLanguage;

    private int index = 0;  // test debug

    static private final String  TAG   = CTutor.class.getSimpleName();
    static private final boolean DEBUG = false;



    public CTutor(Context context, String name, String tutorId, ITutorManager tutorContainer, ILogManager logManager, TScope rootScope, String tarLanguage, String featSet, String matrix) {

        mTutorScope      = new TScope(this, name, rootScope);
        mContext         = context;
        mTutorName       = name;
        mTutorId         = tutorId;
        mTutorContainer  = tutorContainer;
        mTutorLogManager = logManager;

        this.matrix = matrix;

        mAssetManager    = context.getAssets();
        // GRAY_SCREEN_BUG this is where Media Manager is initialized
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "p1: Initializing tutor: " + mTutorName);
        mMediaManager    = CMediaController.newMediaManager(mTutorName);

        uuid = UUID.randomUUID();

        setTutorFeatures(featSet);

        // Update the unique instance string for the tutor
        //
        CPreferenceCache.updateTutorInstance(name);

        // Configure the initial language based on the engine_descriptor JSON setting -
        // tutor may override if "language" is set in tutor_description JSON image
        //
        mMediaManager.setLanguageFeature(this, tarLanguage);

        inflateTutor();

        mTutorLogManager.postEvent_I(GRAPH_MSG, "target:ctutor,action:create,tutorname:" + name);

        monitorBattery();
    }

    /**
     * log the battery... this should eventually be moved to a separate class so it can be
     * accessed by other classes
     */
    private void monitorBattery() {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, iFilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        String chargeType = isCharging ? "CHARGING" : "UNPLUGGED";


        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        //Log.wtf("BATTERY", String.format("status=%d  isCharging=%s  percent=%f", status, isCharging ? "YES": "NO", batteryPct));
        RoboTutor.logManager.postBattery(TCONST.BATTERY_MSG, String.valueOf(batteryPct), chargeType);

    }


    private void inflateTutor() {

        // Load the "tutor_descriptor.json" file -
        // TODO : Ultimately this is meant to hold the scene layout data -
        loadTutorFactory();

        // Load the tutor graph (scene sequence script data) for the tutor
        // TODO: Fully implement the tutor navigator as a graph
        loadTutorGraph();

        // Load the scene graphs (animation script data) for each scene in the tutor
        loadSceneGraph();
    }


    @Override
    public String getEventSourceName() {
        return mTutorName;
    }
    @Override
    public String getEventSourceType() {
        return TCONST.TYPE_CTUTOR;
    }


    public ITutorManager getTutorContainer() {
        return mTutorContainer;
    }

    /**
     *  Load the tutorGraph - tutor scene sequence script for this tutor
     */
    private void loadTutorGraph() {

        switch(navigatorType) {
            case TCONST.SIMPLENAV:
                mTutorGraph = new CTutorGraph(this, mTutorName, mTutorContainer, mTutorScope);
                break;

            case TCONST.GRAPHNAV:
                //mTutorGraph = new CSceneGraphNavigator(mTutorName);
                break;
        }
    }


    /**
     * Load the scenegraph - scene animation scripts for this tutor
     * Push the scenegraph into the tutorgraph for scripting purposes
     *
     */
    private void loadSceneGraph() {

        mSceneGraph = new CSceneGraph(this, mTutorScope, mTutorGraph);

        mTutorGraph.setSceneGraph(mSceneGraph);
    }


    /**
     * Update the current scene container view
     *
     * @param container
     */
    public void setSceneContainer(ViewGroup container) {
        mSceneContainer = container;
    }


    /**
     * This is where the tutor gets kick started
     * Note we pass the extDataSource if available - otherwise we use the local descriptor
     * source if available and let it manage the databinding. Otherwise it has to be set
     * dynamically by the scenegraph.
     */
    public void launchTutor(defdata_tutor extDataSource) {

        mTutorActive = true;
        mTutorGraph.setDefDataSource((extDataSource != null)? extDataSource:dataSource);
        mTutorGraph.post(this, TCONST.FIRST_SCENE);
    }


    /**
     *
     */
    public void onDestroy() {

        // Release the Tutor resources
        scenedata       = null;
        language        = null;
        navigatorType   = null;
        soundMap        = null;

        // Release the scene graph first so the scene data is still intact during destruction
        // TODO: don't know if this sequencing is required
        mSceneGraph.onDestroy();

        mTutorGraph.onDestroy();
    }

    /**
     * for tracking student score within an activity
     * REVIEW build into existing architecture more fluidly
     */
    public int getScore() {
        return score;
    }

    public int getIncorrect() {
        return incorrect;
    }

    public int getAttempts() {
        return attempts;
    }

    public void countCorrect() {
        score++;
        attempts++;
    }

    public void countIncorrect() {
        incorrect++;
        attempts++;
    }

    public void resetScore() {
        score = 0;
        incorrect = 0;
        attempts = 0;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }


    public UUID getUuid() {
        return uuid;
    }

    public class Queue implements Runnable {

        protected final String _command;

        public Queue(String command) {
            _command = command;
        }

        private void cleanUpTutor() {

            // GRAY_SCREEN_BUG tutor might be cleaned up here
            Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r1: Cleaning up tutor " + mTutorName);
            CMediaController.destroyMediaManager(mTutorName);

            // disable the input queue permanently in prep for destruction
            // walks the queue chain to diaable the tutor and scene queues
            //
            mSceneGraph.terminateQueue();
            mTutorGraph.terminateQueue();
            terminateQueue();

            mTutorActive = false;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                switch (_command) {

                    // This is how you kill a running tutor externally -
                    // When the engine wants to kill a tutor and start another.
                    // killDeadTutor just cleans up the now unused tutor.  What happens
                    // after is the responsability of the poster of the event
                    //
                    case TCONST.KILLTUTOR:

                        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r2: In Queue: " + _command);
                        cleanUpTutor();

                        CTutorEngine.killDeadTutor();
                        break;

                    // This is how a tutor stops itself -
                    // DestroyCurrentTutor should remove the tutor and manage the launch
                    // of some sort of session manager of exit the app completely
                    //
                    case TCONST.ENDTUTOR:

                        // don't do end of tutor assessment when we're ending the default tutor (activity selector)
                        if (!mTutorName.equals(CTutorEngine.defTutor)) {
                            // assess student performance after tutor is completed
                            // TRACE_PROMOTION
                            CTutorEngine.promotionMechanism.assessPerformanceAndAdjustPosition(CTutor.this, RoboTutor.STUDENT_CHOSE_REPEAT);

                            Log.wtf("STUDENT_MODEL:AFTER_ASSESSMENT", studentModel.toString());
                        }

                        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r2: In Queue: " + _command);
                        cleanUpTutor();

                        CTutorEngine.destroyCurrentTutor();
                        break;


                    // This is how a tutor stops itself -
                    // DestroyCurrentTutor should remove the tutor and manage the launch
                    // of some sort of session manager of exit the app completely
                    //
                    case TCONST.FINISH:

                        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r2: In Queue: " + _command);
                        cleanUpTutor();

                        CTutorEngine.destroyCurrentTutor();
                        RoboTutor.ACTIVITY.finish();
                        break;

                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, true);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        mDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        try {
            Iterator<?> tObjects = queueMap.entrySet().iterator();

            while (tObjects.hasNext()) {
                Map.Entry entry = (Map.Entry) tObjects.next();

                mainHandler.removeCallbacks((Queue) (entry.getValue()));
            }
        }
        catch(Exception e) {
            Log.d(TAG, "flushQueue Error: " + e);
        }

    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {

        RoboTutor.logManager.postEvent_V(TAG, "Processing POST to tutorGraph: " + qCommand._command );

        if(!mDisabled) {
            queueMap.put(qCommand, qCommand);

            mainHandler.post(qCommand);
        }
    }


    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {

        enQueue(new Queue(command));
    }




    /**
     * Return the view within the current scene container
     *
     * @param findme
     * @return
     */
    public View getViewByName(String findme) {

        HashMap map = mTutorGraph.getChildMap();

        return (View)map.get(findme);
    }



    public ITutorObject getViewById(int findme, ViewGroup container) {
        ITutorObject foundView = null;

        if(container == null)
            container = (ViewGroup)mSceneContainer;

        try {
            for (int i = 0; (foundView == null) && (i < container.getChildCount()); ++i) {

                ITutorObject nextChild = (ITutorObject) container.getChildAt(i);

                if (((View) nextChild).getId() == findme) {
                    foundView = nextChild;
                    break;
                } else {
                    if (nextChild instanceof ViewGroup)
                        foundView = getViewById(findme, (ViewGroup) nextChild);
                }
            }
        }
        catch (Exception e) {
            Log.i(TAG, "View walk error: " + e);
        }
        return foundView;
    }


    public TScope getScope() {
        return mTutorScope;
    }


    public ITutorGraph getTutorGraph() {
        return mTutorGraph;
    }

    public CSceneGraph getSceneGraph() {
        return mSceneGraph;
    }


    //**************************************************************************
    // Language management

    // The language ID is also used as a feature to permit conditioning on language
    // within scripts.
    //
    public void updateLanguageFeature(String langFtr) {

        // Remove any active language - Only want one language feature active
        delFeature(mMediaManager.getLanguageFeature(this));

        addFeature(langFtr);
    }

    public String getLanguageFeature() {

        return mMediaManager.getLanguageFeature(this);
    }



    // Language management
    //**************************************************************************


    /**
     * This provides sceneGraph nodes access to the Assetmanager through their scopes
     * mTutor field
     *
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream openAsset(String path) throws IOException {
        return mAssetManager.open(path);
    }


    // framendx is a simple counter used it uniquely id a scene instance for logging
    //
    public void incFrameNdx() {
        _framendx++;
    }


    public void add(String Id, ITutorObject obj) {

        mObjects.put(Id, obj);
    }


    public ITutorObject get(String Id) {

        return mObjects.get(Id);
    }


    /**
     *  Scene Creation / Destruction
     *
     * @param scenedata
     * @return
     */
    public View instantiateScene(scene_descriptor scenedata) {

        int i1;
        View tarScene;
        View subScene;

        // Map the sceneid to it's named xml Layout resource.
        //
        String packageName = mContext.getPackageName();
        String sceneId = scenedata.id;

        int id = mContext.getResources().getIdentifier(scenedata.id, "layout", mContext.getPackageName());

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        tarScene = inflater.inflate(id, null );

        if(traceMode) Log.d(TAG, "Creating Scene : " + scenedata.id);

        tarScene.setVisibility(View.VISIBLE);

        // Generate the automation hooks
        automateScene((ITutorSceneImpl) tarScene, scenedata);

        // Parse the JSON spec data for onCreate Commands
        onCreate(scenedata);

        return (View) tarScene;
    }


    private void automateScene(ITutorSceneImpl tutorContainer, scene_descriptor scenedata) {

        // Propogate to children
        //
        HashMap childMap = new HashMap();

        // Record each SCENE Object
        //
        scenedata.instance = tutorContainer;
        scenedata.children = childMap;

        tutorContainer.setParent(mTutorContainer);
        tutorContainer.setTutor(this);
        tutorContainer.setNavigator(mTutorGraph);
        tutorContainer.setLogManager(mTutorLogManager);

        mapChildren(tutorContainer, childMap);

        try {
            Iterator<?> tObjects = childMap.entrySet().iterator();

            // post create / inflate / init / map - here everything is created including the
            // view map to permit findViewByName
            //
            while (tObjects.hasNext()) {
                Map.Entry entry = (Map.Entry) tObjects.next();

                ((ITutorObject) (entry.getValue())).onCreate();
            }
        }
        catch(Exception e) {
            Log.d(TAG, "automateScene Error: " + e);
        }
    }


    private void mapChildren(ITutorSceneImpl tutorContainer, HashMap childMap) {

        ITutorObject child;

        // Add the container as well so we can find it in a getViewByName search
        //
        childMap.put(tutorContainer.name(), tutorContainer);

        int count = ((ViewGroup) tutorContainer).getChildCount();

        // Iterate through all children
        for (int i = 0; i < count; i++) {
            try {
                child = (ITutorObject) ((ViewGroup) tutorContainer).getChildAt(i);

                if(childMap.containsKey(child.name())) {

                    CErrorManager.logEvent(TAG, "ERROR: Duplicate child view in:" + tutorContainer.name() + " - Duplicate of: " + child.name(),  new Exception("no-exception"), false);
                }

                childMap.put(child.name(), child);

                child.setParent(tutorContainer);
                child.setTutor(this);
                child.setNavigator(mTutorGraph);
                child.setLogManager(mTutorLogManager);

                if(child instanceof ITutorSceneImpl) {
                    mapChildren((ITutorSceneImpl)child, childMap);
                }

            } catch (ClassCastException e) {

                CErrorManager.logEvent(TAG, "ERROR: Non-ITutor child view in:" + tutorContainer.name(), e, false);
            }
        }
    }


    private void onCreate(scene_descriptor scenedata) {

        // Parse the oncreate command set

        type_action[] createCmds    = _sceneMap.get(scenedata.id).oncreate;

        // Can have an empty JSON array - so filter that out
        //
        if(createCmds != null) {

            for (type_action cmd : createCmds) {

                if(cmd.testFeatures()) {
                    cmd.applyNode();
                }

            }
        }
    }


    /**
     * generate the working feature set for this tutor instance
     *
     * @param featSet
     */
    public void setTutorFeatures(String featSet) {

        // Ignore "null" feature sets which may come during a tutor launch if there is no
        // features data in the session_manager dataset
        //
        if(!featSet.toUpperCase().equals("NULL")) {

            List<String> featArray = new ArrayList<String>();

            if (featSet != null && featSet.length() > 0)
                featArray = Arrays.asList(featSet.split(":"));

            fFeatures = new ArrayList<String>();

            // Add default features

            for (String feature : fDefaults) {
                fFeatures.add(feature);
            }

            // Add instance feature

            for (String feature : featArray) {
                fFeatures.add(feature);
            }
        }
    }


    /**
     *  get : delimited string of features
     * ## Mod Oct 16 2012 - logging support
     *
     */
    public String getFeatures() {
        StringBuilder builder = new StringBuilder();

        for(String feature: fFeatures) {
            builder.append(feature).append(':');
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }


    /**
     * set : delimited string of features
     * ## Mod Dec 03 2013 - DB state support
     *
     * @param ftrSet
     */
    public void setFeatures(String ftrSet) {
        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(ftrSet.split(","));

        fFeatures.clear();
        
        for (String feature : featArray) {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    public void addFeature(String feature)
    {
        // Add new features - no duplicates

        if(fFeatures.indexOf(feature) == -1)
        {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    public void delFeature(String feature) {
        int fIndex;

        // remove features - no duplicates

        if((fIndex = fFeatures.indexOf(feature)) != -1)
        {
            fFeatures.remove(fIndex);
        }
    }


    //## Mod Jul 01 2012 - Support for NOT operation on features.
    //
    //	
    public boolean testFeature(String element) {
        if(element.charAt(0) == '!')
        {
            return (fFeatures.indexOf(element.substring(1)) != -1)? false : true;
        }
        else {
            return (fFeatures.indexOf(element) != -1) ? true : false;
        }
    }

    public String testFeatureHelper(String element) {
        if(element.charAt(0) == '!') {
            if(element.substring(1).equals("true")) return "false";
            if(element.substring(1).equals("false")) return "true";
            return (fFeatures.indexOf(element.substring(1)) != -1)? "false" : "true";
        }
        else {
            if(element.equals("true")) return "true";
            if(element.equals("false")) return "false";
            return (fFeatures.indexOf(element) != -1) ? "true" : "false";
        }
    }

    // test possibly compound features
    // TODO: Enhance with fsm
    // Doesn't allow inner paren matching
    public boolean testFeatureSet(String featSet) {
        String result = testFeatureSetHelper(featSet);
        return result.equals("true") ? true : false;
    }

    public String testFeatureSetHelper(String featSet) {
        int curParenCount = 0;
        int leftMostOpenParen = -1;

        StringBuffer featSetBuffer = new StringBuffer(featSet);

        while(featSetBuffer.indexOf("(") != -1) {
            for(int i = 0; i < featSetBuffer.length(); i++) {
                String curString = featSetBuffer.substring(i, i+1);

                if(curString.equals("(")) {
                    curParenCount += 1;
                    if(leftMostOpenParen == -1) {
                        leftMostOpenParen = i;
                    }
                }
                if(curString.equals(")")) {
                    curParenCount -= 1;
                    if(curParenCount == 0) {
                        String withParen = featSetBuffer.substring(leftMostOpenParen + 1, i);
                        featSetBuffer.replace(leftMostOpenParen, i+1, testFeatureSetHelper(withParen));
                        leftMostOpenParen = -1;
                        break;
                    }
                }
            }
        }
        return testNonParenFeatureSet(featSetBuffer.toString());
    }

    private String testNonParenFeatureSet(String featSet) {

        String      result = "false";

        List<String> disjFeat = Arrays.asList(featSet.split("\\|"));   // | Disjunctive features
        List<String> conjFeat;                                          // & Conjunctive features

        // match a null set - i.e. empty string means the object is not feature constrained

        if(featSet.equals(""))
            return "true";

        // Check all disjunctive featuresets - one in each element of disjFeat
        // As long as one is true we pass

        for (String dfeature : disjFeat)
        {
            conjFeat   = Arrays.asList(dfeature.split("\\&"));
            result = "true";

            // Check that all conjunctive features are set in fFeatures

            for (String cfeature : conjFeat) {
                if(!(testFeatureHelper(cfeature) == "true"))
                    result = "false";
            }

            if(result == "true")
                break;
        }

        return result;
    }


    public String getTutorName() {
        return mTutorName;
    }
    public String getTutorId() {
        return mTutorId;
    }


    public AssetManager getAssetManager() {
        return mAssetManager;
    }


    // Scriptable graph next command
    public void eventNext() {
        mSceneGraph.post(this, TCONST.NEXT_NODE);
    }

    // Scriptable graph goto command
    public void gotoNode(String nodeID) {
        mSceneGraph.post(this, TCONST.GOTO_NODE, nodeID);
    }



    //************ Serialization


    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/tutor_descriptor.json
     *
     * Note that this is a stopgap until we can replace the Android view inflation mechanism
     * and completely define view layout in TDESC
     */
    private void loadTutorFactory() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.TDESC)), (IScope2)mTutorScope);

        } catch (JSONException e) {
            Log.d(TAG, "error");
        }
    }


    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

        // Use updateLanguageFeature to properly override the Engine language feature
        if(language != null)
            mMediaManager.setLanguageFeature(this, language);

        // push the soundMap into the MediaManager -
        //
        mMediaManager.setSoundPackage(this, soundMap);

        // Create a associative cache for the initialization data
        //
        for(scene_initializer scene : scenedata) {
            _sceneMap.put(scene.id, scene);
        }
    }
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
