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


import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.PerformanceLogItem;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.graph.databinding;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.widgets.core.IDataSink;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

import static cmu.xprize.robotutor.tutorengine.CTutorEngine.studentModel;
import static cmu.xprize.util.TCONST.GRAPH_MSG;


public class CTutorGraph implements ITutorGraph, ILoadableObject2, Animation.AnimationListener, IEventSource {

    private TScope             mRootScope;

    private boolean            traceMode     = false;
    private int                _sceneCnt     = 0;
    private boolean            _inNavigation = false;
    private String             _xType;

    protected ITutorScene      mParent;
    protected CTutor           mTutor;
    protected String           mTutorName;
    protected defdata_tutor    mDefDataSource = null;
    protected ITutorManager    mTutorContainer;
    protected CSceneGraph      mSceneGraph;
    protected ILogManager      mLogManager;

    private final Handler      mainHandler = new Handler(Looper.getMainLooper());
    private HashMap            queueMap    = new HashMap();
    private boolean            mDisabled   = false;

    // json loadable
    public scene_descriptor[] navigatedata;


    // State data
    private HashMap<String, scene_descriptor> _navMap = new HashMap<String, scene_descriptor>();
    private int                               _scenePrev;
    private int                               _sceneCurr;
    private boolean                           _push = false;


    final private String       TAG       = "CTutorGraph";

    /**
     *
     *
     *
     * @param tutor
     * @param name
     * @param tutorScope
     */
    public CTutorGraph(CTutor tutor, String name, ITutorManager tutorContainer, TScope tutorScope) {

        mRootScope = new TScope(tutor, name + "-SceneNavigator", tutorScope);      // Use a unique namespace

        mTutor          = tutor;
        mTutorName      = name;
        mTutorContainer = tutorContainer;
        mLogManager     = CLogManager.getInstance();

        _sceneCurr = 0;
        _scenePrev = 0;

        loadTutorGraphFactory();

        // TODO: Check if this is ever used
        navigatedata[0].instance = tutorContainer;
    }


    @Override
    public String getEventSourceName() {
        return TCONST.EVENT_TUTORGRAPH;
    }
    @Override
    public String getEventSourceType() {
        return TCONST.TYPE_CTUTORGRAPH;
    }


    @Override
    public void setSceneGraph(CSceneGraph sGraph) {
        mSceneGraph = sGraph;
    }


    @Override
    public CSceneGraph getSceneGraph() {
        return mSceneGraph;
    }


    @Override
    public void setDefDataSource(defdata_tutor dataSources) {

        mDefDataSource = dataSources;
    }


    /**
     * Walk the scene descriptors and kill off any remaining scenes
     */
    public void onDestroy() {

        for(scene_descriptor scene : navigatedata) {

            // Do the destruction depth first
            //
            if(scene.instance != null) {

                // If the scene has children - allow them to shutdown gracefully
                //
                if(scene.children != null) {
                    Iterator<?> tObjects = scene.children.entrySet().iterator();

                    // Perform component level Folder cleanup first
                    //
                    while(tObjects.hasNext() ) {
                        Map.Entry entry = (Map.Entry) tObjects.next();

                        ((ITutorObject)(entry.getValue())).onDestroy();
                    }
                }

                // Then tell the container to destruct
                //
                scene.instance.onDestroy();
            }
        }
    }


    public class Queue implements Runnable {

        protected IEventSource _source;;
        protected String       _command;

        public Queue(IEventSource source, String command) {
            _source  = source;
            _command = command;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                Log.d(TAG, "Processing event: " + _command + " From: " + _source.getEventSourceName() + " TYPE: " + _source.getEventSourceType());

                switch (_command) {

                    case TCONST.FIRST_SCENE:

                        gotoNextScene(true);
                        break;

                    case TCONST.NEXTSCENE:

                        if (gotoNextScene(false).equals(TCONST.ENDTUTOR)) {

                            //mainHandler.post(mTutor.new Queue(TCONST.ENDTUTOR));

                            PerformanceLogItem event = new PerformanceLogItem();
                            event.setUserId(RoboTutor.STUDENT_ID);
                            event.setSessionId(RoboTutor.SESSION_ID);
                            event.setLanguage(CTutorEngine.language);
                            event.setTaskName("ENDTUTOR");
                            event.setTimestamp(System.currentTimeMillis());
                            RoboTutor.perfLogManager.postPerformanceLogWithoutContext(event); // EVELYN_BUG_9_27 can we get the last tutor?

                            mTutor.post(TCONST.ENDTUTOR);
                        }
                        break;

                    case TCONST.ENDTUTOR:

                        // disable the input queue permenantly in prep for destruction
                        // walks the queue chain to diaable scene queue
                        //
                        terminateQueue();

                        mTutor.post(TCONST.ENDTUTOR);
                        break;

                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, true);
            }
        }
    }

    /**
     *  Disable the input queue permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    public void terminateQueue() {

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

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }

    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {

        RoboTutor.logManager.postEvent_V(TAG, "Processing POST to TutorGraph: " + qCommand._command + " - from: " +  qCommand._source.getEventSourceName());

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
    public void post(IEventSource source, String command) {

        enQueue(new Queue(source, command));
    }


    /**
     *
     * @return The result maps child names to Views
     */
    @Override
    public HashMap getChildMap() {

        return navigatedata[_sceneCurr].children;
    }


    /**
     *
     * @param sceneName
     * @return  The result maps child names to Views
     */
    @Override
    public HashMap getChildMapByName(String sceneName) {

        return _navMap.get(sceneName).children;
    }



//***************** Navigation Behaviors *******************************



    //*********************************************
    //*********************************************
    //*********************************************
    // Inter Scene Navigation
    //

    //*************** Navigator getter setters -
    // these within a subclass to set the root of a navigation sequence

    protected int getScenePrev() {
        return _scenePrev;
    }
    protected void setScenePrev(int scenePrevINT) {
        _scenePrev = scenePrevINT;
    }


    protected int  getSceneCurr() {
        return _sceneCurr;
    }
    protected void setSceneCurr(int sceneCurrINT) {
        _sceneCurr = sceneCurrINT;
    }


    protected int  sceneCurrINC() {
        String             features;
        ArrayList<String>  featSet= new ArrayList<String>();
        Boolean            match = false;

        _sceneCurr++;

        // If new scene has features, check that it is being used in the current tutor feature set
        // Note: You must ensure that there is a match for the last scene in the sequence

        while((features = navigatedata[_sceneCurr].features) != null)
        {
            // If this scene is not in the feature set for the tutor then check the next one.
            if(!mTutor.testFeatureSet(features)) _sceneCurr++;
            else break;
        }

        return _sceneCurr;
    }


    protected int sceneCurrDEC() {
        String             features;
        ArrayList<String>  featSet= new ArrayList<String>();
        Boolean            match = false;

        _sceneCurr--;

        // If new scene has features, check that it is being used in the current tutor feature set
        // Note: You must ensure that there is a match for the last scene in the sequence

        while((features = navigatedata[_sceneCurr].features) != null)
        {
            // If this scene is not in the feature set for the tutor then check the next one.

            if(!mTutor.testFeatureSet(features)) _sceneCurr--;
            else break;
        }

        return _sceneCurr;
    }



    private int findSceneOrd(String tarScene) {
        
        if(traceMode) Log.i(TAG, "findSceneOrd: " + tarScene);

        // returns the scene ordinal in the sequence array or 0
        //
        return _navMap.get(tarScene).index;
    }


    /**
     * gotoNextScene manual entry point
     */
    @Override
    public String gotoNextScene(boolean push) {

        String result = TCONST.ENDTUTOR;

        if(traceMode) Log.i(TAG, "gotoNextScene: ");

        String newScene = "";
        String redScene = "";

        // Local push - used in onAnimationEnd to save previous scene on stack when starting
        // a new tutor
        _push = push;

        // TODO: This is a stopgap until we have full tutorgraph capabilities.
        //
        if (_sceneCurr < _sceneCnt-1) {

            // remember current frame
            //
            if (traceMode)
                Log.d(TAG, "scenePrev: " + _scenePrev + "  - sceneCurr: " + _sceneCurr);

            _scenePrev = _sceneCurr;

            // Do scene Specific termination
            //
            if (traceMode)
                Log.d(TAG, "navigatedata[_sceneCurr]: " + navigatedata[_sceneCurr].id);

            navigatedata[_scenePrev].instance.onExitScene();

            // increment the current scene - this is Feature reactive
            sceneCurrINC();

            if (navigatedata[_sceneCurr].instance == null) {
                mTutor.instantiateScene(navigatedata[_sceneCurr]);
            }

            // Update the tutor SceneContainer used to enumerate components.
            //
            mTutor.setSceneContainer((ViewGroup)navigatedata[_sceneCurr].instance);

            //@@ Action Logging
            //            var logData:Object = {'navevent':'navnext', 'curscene':_scenePrev, 'newscene':redScene};
            //            //var xmlVal:XML = <navnext curscene={_scenePrev} newscene={redScene}/>
            //
            //            gLogR.logNavEvent(logData);
            //@@ Action Logging

            // On terminate behaviors
            navigatedata[_scenePrev].instance.onExitScene();

            preEnterScene();

            // Do the scene transition - add callback for when IN animation ends
            mTutorContainer.setAnimationListener(this);
            mTutorContainer.addView(navigatedata[_sceneCurr].instance);

            result = TCONST.CONTINUETUTOR;
        }
        // TODO: This is a stop gap to cleanup scenes
        //
        else {
            mTutorContainer.removeView(navigatedata[_sceneCurr].instance);
        }
        return result;
    }


    private void preEnterScene() {

        Map  childMap = getChildMap();

        // Walk all the components that have datasources defined and initialize them.
        //
        if(mDefDataSource != null) {

            String sceneName = navigatedata[_sceneCurr].id;

            databinding[] bindings = (databinding[]) mDefDataSource.scene_bindings.get(sceneName).databindings;

            for(databinding binding : bindings) {

                IDataSink dataSink = (IDataSink) childMap.get(binding.name);

                if(dataSink != null) {

                    mLogManager.postEvent_I(GRAPH_MSG, "target:ctutorgraph,action:preenterscene,setdatasource:" + binding.datasource);

                    dataSink.setDataSource(binding.datasource);

                    // Add a feature to show the component has been initialized with a datasource
                    // to inhibit the default graph datasource.
                    //
                    mTutor.addFeature(TCONST.DATA_PREFIX + binding.name.toUpperCase());
                }
                else {
                    Log.e(TAG, "Default Data Binding - View not found by name: " + binding.name);
                }
            }
        }
    }


    /** Animation Listener START *************************/
    //

    @Override
    public void onAnimationStart(Animation animation) {

    }

    // Performed immediately after scene is fully onscreen
    //@@ Mod Jul 18 2013 - public -> private
    //
    @Override
    public void onAnimationEnd(Animation animation) {

        if(traceMode) Log.d(TAG, "doEnterScene: " + _sceneCurr);

        mTutorContainer.setAnimationListener(null);

        // increment the global frame ID - for logging
        mTutor.incFrameNdx();

        //## Mod Sep 12 2013 - This is a special case to handle the first preenter event for an animationGraph.
        //                     The root node of the animation graph is parsed in the preEnter stage of the scene
        //                     creation so the scene is not yet on stage. This call ensures that the scene
        //                     associated with the animation object has been instantiated.
        //
        //	TODO: This should be rationalized with the standard preEnter when all the preEnter customizations
        //  TODO: in CWOZScene derivatives have been moved to the XML (JSON) spec.
        //
        navigatedata[_sceneCurr].instance.onEnterScene();

        mSceneGraph.post(this, TCONST.ENTER_SCENE, navigatedata[_sceneCurr].id);
        mSceneGraph.post(this, TCONST.NEXT_NODE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //
    /** Animation Listener END *************************/




    //************ Serialization



    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/navigator_descriptor.json
     *
     * This is only used here until we have the scenegraph implementation in place.
     * This provides a simple linear or mapped access to scenes.
     *
     */
    private void loadTutorGraphFactory() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.SNDESC)), (IScope2)mRootScope);

        } catch (JSONException e) {
            Log.d(TAG, "Error" );
        }
    }


    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {
        int i1 = 0;

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

        // shortcut to length
        _sceneCnt = navigatedata.length;

        // Generate a hash map for all the scenes in the tutor
        for(scene_descriptor scene : navigatedata) {
            scene.index = i1++;
            _navMap.put(scene.id, scene);
        }
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
