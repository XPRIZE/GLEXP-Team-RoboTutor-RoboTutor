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


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.graph.scene_graph;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

import static cmu.xprize.util.TCONST.GRAPH_MSG;


/**
 * A CSceneGraph represents a collection of the animation graphs for each scene
 * that constitutes the tutor.
 *
 */
public class CSceneGraph  {

    private TScope           mScope;
    protected String        _logType = GRAPH_MSG;

    protected CTutor         mTutor;
    private String           mGraphName;
    private ITutorGraph      mTutorGraph;

    private final Handler    mainHandler = new Handler(Looper.getMainLooper());
    private HashMap          queueMap    = new HashMap();
    private boolean          mDisabled   = false;

    // State fields
    private scene_graph              _sceneGraph;
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

        if(_sceneGraph != null) {
            _sceneGraph.onDestroy();
        }
    }


    /**
     *
     */
    public void pushGraph() {

        if(_sceneGraph != null) {
            _dataStack.add(_sceneGraph);
        }
    }


    /**
     *
     */
    public boolean popGraph() {

        boolean popped   = false;
        int     popIndex = _dataStack.size()-1;

        if(popIndex >= 0) {
            _sceneGraph = _dataStack.get(popIndex);
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

                RoboTutor.logManager.postEvent_V(_logType, "target:node.scenegraph.run,command:" + _command + ",from:" + _source.getEventSourceName() + ",type:" + _source.getEventSourceType() + ",target:" + _target);

                switch (_command) {
                    case TCONST.ENTER_SCENE:

                        mGraphName = _target;

                        try {
                            _sceneGraph = (scene_graph) mScope.mapSymbol(mGraphName);

                            RoboTutor.logManager.postEvent_V(_logType, "target:node.scenegraph,action:run,event:enterscene,name:" + _sceneGraph.name + ",maptype:" + _sceneGraph.type );

                        } catch (Exception e) {

                            CErrorManager.logEvent(_logType, "target:node.scenegraph,action:run,error:" + "Scene not found for SceneGraph,exception:", e, false);
                        }
                        break;


                    case TCONST.SUBGRAPH_CALL:

                        // Don't permit nested graphs
                        // This is a kludge to sinmplify feedback on buttons - i.e. if the button
                        // is pushed during the feedback about the button itself.
                        //
                        if(_dataStack.size() > 0) {

                            _sceneGraph.cancelNode();
                            popGraph();
                        }

                        mGraphName = _target;

                        // Save the current graph state
                        //
                        pushGraph();

                        try {
                            _sceneGraph = (scene_graph) mScope.mapSymbol(mGraphName);
                            _sceneGraph.resetNode();

                            RoboTutor.logManager.postEvent_V(_logType, "target:node.scenegraph,action:run,event:callgraph,name:" + _sceneGraph.name + ",mapType:" + _sceneGraph.type );

                            // Seek the graph to the root node and execute it
                            //
                            if(_sceneGraph != null) {
                                post(this, TCONST.NEXT_NODE);
                            }

                        } catch (Exception e) {

                            CErrorManager.logEvent(_logType, "target:node.scenegraph,action:run,event:callgraph,ERROR:subgraph not found,exception:", e, false);
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

                        String sceneState;

                        if(_sceneGraph.testFeatures()) {
                            sceneState = _sceneGraph.applyNode();
                        }
                        else {
                            sceneState = TCONST.DONE;
                        }

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
                                System.out.println("WAITING in CSCENEGRAPH");
                                break;

                            default:
                                post(this, TCONST.NEXT_NODE);
                                break;
                        }
                        break;


                    case TCONST.CANCEL_NODE:

                        switch (_sceneGraph.cancelNode()) {

                            // TCONST.NEXTSCENE is used to end the current scene and step through to the
                            // next scene in the TutorGraph.

                            case TCONST.END_OF_GRAPH:

                                // If this is the root graph then we do to the next scene
                                //
                                if(!popGraph()) {
                                    mTutorGraph.post(this, TCONST.NEXTSCENE);
                                }
                                break;

                            default:
                                break;
                        }
                        break;


                    case TCONST.PLAY:
                        _sceneGraph.play();
                        break;

                    case TCONST.PLAY_CLOZE:
                        _sceneGraph.play(TCONST.CLOZE_END);

                    case TCONST.STOP:
                        _sceneGraph.stop();
                        break;


                    case TCONST.ENDTUTOR:

                        // disable the input queue permanently in prep for destruction
                        //
                        terminateQueue();

                        mTutorGraph.post(this, TCONST.ENDTUTOR);
                        break;

                    case TCONST.GOTO_NODE:
                        _sceneGraph.gotoNode(_target);
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(_logType, "target:node.scenegraph,action:run,event:ERROR,exception:", e, false);
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

        RoboTutor.logManager.postEvent_V(_logType, "target:node.scenegraph,action:enqueue,command:" + qCommand._command + ",from:" +  qCommand._source.getEventSourceName() + ",target:" + qCommand._target );

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

            CErrorManager.logEvent(_logType, "target:node.scenegraph,action:loadjsongraphfactory,error:JSON FORMAT ERROR,filename:" + TCONST.AGDESC + ",exception:", e, false);
        }
    }

    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

    }


}
