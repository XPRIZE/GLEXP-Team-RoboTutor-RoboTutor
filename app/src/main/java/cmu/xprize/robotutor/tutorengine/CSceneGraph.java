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


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.graph.scene_graph;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;


/**
 * A CSceneGraph represents a collection of the animation graphs for each scene
 * that constitutes the tutor.
 *
 */
public class CSceneGraph  {

    private TScope           mScope;

    protected CTutor         mTutor;
    private String           mGraphName;
    private ITutorGraph      mTutorGraph;

    private final Handler    mainHandler = new Handler(Looper.getMainLooper());
    private HashMap          queueMap    = new HashMap();
    private boolean          mDisabled   = false;

    // State fields
    private scene_graph              _graph;
    private HashMap<String, Integer> _pFeatures;
    private ArrayList<scene_graph>   _dataStack  = new ArrayList<>();


    // json loadable
    public HashMap<String,scene_graph> animatorMap;



    final private String TAG = "CSceneGraph";


    public CSceneGraph(CTutor tutor, TScope tutorScope, ITutorGraph tutorGraph) {

        mTutor      = tutor;
        mScope      = tutorScope;
        mTutorGraph = tutorGraph;

        _pFeatures = new HashMap<String, Integer>();

        loadSceneGraphFactory((IScope2)mScope);
    }

    /**
     *
     */
    public void onDestroy() {

    }


    /**
     *
     */
    public void pushGraph() {

        if(_graph != null) {
            _dataStack.add(_graph);
        }
    }


    /**
     *
     */
    public boolean popGraph() {

        boolean popped   = false;
        int     popIndex = _dataStack.size()-1;

        if(popIndex >= 0) {
            _graph = _dataStack.get(popIndex);
            _dataStack.remove(popIndex);

            popped = true;
        }

        return popped;
    }



    /**
     * This is the central processsing point of CSceneGraph - It is a message driven pattern
     * on the UI thread.
     */
    public class Queue implements Runnable, IEventSource {

        protected IEventSource _source;;
        protected String       _command;
        protected String       _target;


        @Override
        public String getEventSourceName() {
            return TCONST.EVENT_SCENEQUEUE;
        }
        @Override
        public String getEventSourceType() {
            return TCONST.TYPE_CSCENEGRAPH;
        }


        public Queue(IEventSource source, String command) {
            _source  = source;
            _command = command;
        }

        public Queue(IEventSource source, String command, String target) {
            _source  = source;
            _command = command;
            _target  = target;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                Log.d(TAG, "Processing event: " + _command + " From: " + _source.getEventSourceName() + " TYPE: " + _source.getEventSourceType() + " Target: " + _target);

                switch (_command) {
                    case TCONST.ENTER_SCENE:

                        mGraphName = _target;

                        try {
                            _graph = (scene_graph) mScope.mapSymbol(mGraphName);

                            Log.d(TAG, "Processing Enter Scene: " + _graph.name + " - mapType: " + _graph.type );

                        } catch (Exception e) {

                            CErrorManager.logEvent(TAG, "Scene not found for SceneGraph", e, false);
                        }
                        break;


                    case TCONST.SUBGRAPH_CALL:

                        mGraphName = _target;

                        // Save the current graph state
                        //
                        pushGraph();

                        try {
                            _graph = (scene_graph) mScope.mapSymbol(mGraphName);

                            Log.d(TAG, "Processing call graph: " + _graph.name + " - mapType: " + _graph.type );

                            // Seek the graph to the root node and execute it
                            //
                            if(_graph != null) {
                                post(this, TCONST.NEXT_NODE);
                            }

                        } catch (Exception e) {

                            CErrorManager.logEvent(TAG, "subgraph not found", e, false);
                        }
                        break;

                    case TCONST.SUBGRAPH_RETURN_AND_GO:

                        post(this, TCONST.NEXT_NODE);

                    case TCONST.SUBGRAPH_RETURN_AND_WAIT:

                        // Restore the previous state and continue processing it's nodes.
                        //
                        popGraph();
                        break;



                    case TCONST.NEXT_NODE:

                        String sceneState = _graph.applyNode();

                        switch (sceneState) {

                            // TCONST.NEXTSCENE is used to end the current scene and step through to the
                            // next scene in the TutorGraph.

                            case TCONST.END_OF_GRAPH:
                                // If this is the root graph then we do to the next scene
                                //
                                if(!popGraph()) {
                                    mTutorGraph.post(this, TCONST.NEXTSCENE);
                                }
                                break;

                            // TCONST.WAIT indicates that next node will be driven by a
                            // completion event from the current action or some external user event.
                            //
                            case TCONST.WAIT:
                                break;

                            default:
                                post(this, TCONST.NEXT_NODE);
                                break;
                        }
                        break;

                    case TCONST.PLAY:
                        _graph.play();
                        break;

                    case TCONST.STOP:
                        _graph.stop();
                        break;

                    case TCONST.ENDTUTOR:

                        // disable the input queue permanently in prep for destruction
                        //
                        terminateQueue();

                        mTutorGraph.post(this, TCONST.ENDTUTOR);
                        break;

                    case TCONST.GOTO_NODE:
                        _graph.gotoNode(_target);
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
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

        Log.d(TAG, "Processing POST to SceneGraph: " + qCommand._command + " - from: " +  qCommand._source.getEventSourceName() + " - target: " + qCommand._target );

        if(!mDisabled) {
            queueMap.put(qCommand, qCommand);

            mainHandler.post(qCommand);
        }
    }

    /**
     * Post a command to this scenegraph queue
     *
     * @param command
     */
    public void post(IEventSource source, String command) {

        enQueue(new Queue(source, command));
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(IEventSource source, String command, String target) {

        enQueue(new Queue(source, command, target));
    }


    public int queryPFeature(String pid, int size, int cycle) {
        int iter = 0;

        // On subsequent accesses we increment the iteration count
        // If it has surpassed the size of the pFeature array we cycle on the last 'cycle' entries

        if (_pFeatures.containsKey(pid)) {
            iter = _pFeatures.get(pid) + 1;

            if (iter >= size) {
                iter = size - cycle;
            }

            _pFeatures.put(pid, iter);
        }

        // On first touch we have to create the property

        else _pFeatures.put(pid, 0);

        return iter;
    }




    //************ Serialization



    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/animator_graph.json
     *
     */
    private void loadSceneGraphFactory(IScope2 scope) {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.mTutorName + "/" + TCONST.AGDESC)), scope);

        } catch (JSONException e) {

            CErrorManager.logEvent(TAG, "JSON FORMAT ERROR: " + TCONST.AGDESC + " : ", e, false);
        }
    }

    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

    }


}
