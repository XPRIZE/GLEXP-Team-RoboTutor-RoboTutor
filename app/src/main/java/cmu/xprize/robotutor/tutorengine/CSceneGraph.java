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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.graph.tutor_node;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.CErrorManager;
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
    private String           mTutorName;
    private String           mSceneName;
    private CTutorGraph      mTutorGraph;

    private final Handler    mainHandler = new Handler(Looper.getMainLooper());
    private HashMap          queueMap    = new HashMap();
    private boolean          mDisabled   = false;

    // State fields
    private tutor_node               _sceneNode;
    private HashMap<String, Integer> _pFeatures;


    // json loadable
    public HashMap<String,tutor_node> animatorMap;


    final private String TAG = "CSceneGraph";


    public CSceneGraph(CTutor tutor, TScope tutorScope, CTutorGraph tutorGraph) {

        mTutor      = tutor;
        mScope      = tutorScope;
        mTutorGraph = tutorGraph;

        _pFeatures = new HashMap<String, Integer>();

        loadSceneGraphFactory((IScope2)mScope);
    }

    // TODO: add onDestroy to release animatorMap resources


    /**
     * This is the central processsing point of CSceneGraph - It is a message driven pattern
     * on the UI thread.
     */
    public class Queue implements Runnable {

        protected String _command;
        protected String _target;

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, String target) {
            _command = command;
            _target  = target;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                switch (_command) {
                    case TCONST.ENTER_SCENE:

                        mSceneName = _target;

                        try {
                            _sceneNode = (tutor_node) mScope.mapSymbol(mSceneName);

                        } catch (Exception e) {

                            CErrorManager.terminate(TAG, "Scene not found for SceneGraph", e, false);
                        }
                        break;

                    case TCONST.NEXT_NODE:

                        switch (_sceneNode.applyNode()) {

                            // TCONST.NEXTSCENE is used to end the current scene and step through to the
                            // next scene in the TutorGraph.

                            case TCONST.NEXTSCENE:
                                mTutorGraph.post(TCONST.NEXTSCENE);
                                break;

                            // TCONST.WAIT indicates that next node will be driven by a
                            // completion event from the current action or some external user event.

                            case TCONST.WAIT:
                                break;

                            default:
                                post(TCONST.NEXT_NODE);
                                break;
                        }
                        break;

                    case TCONST.PLAY:
                        _sceneNode.play();
                        break;

                    case TCONST.STOP:
                        _sceneNode.stop();
                        break;

                    case TCONST.ENDTUTOR:

                        // disable the input queue permanently in prep for destruction
                        //
                        terminateQueue();

                        mTutorGraph.post(TCONST.ENDTUTOR);
                        break;

                    case TCONST.GOTO_NODE:
                        _sceneNode.gotoNode(_target);
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.terminate(TAG, "Run Error:", e, false);
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
    public void post(String command) {

        enQueue(new Queue(command));
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, String target) {

        enQueue(new Queue(command, target));
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
     * from assets/tutors/<tutorname>/tutor_descriptor.json
     *
     */
    private void loadSceneGraphFactory(IScope2 scope) {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.mTutorName + "/" + TCONST.AGDESC)), scope);

        } catch (JSONException e) {

            CErrorManager.terminate(TAG, "JSON FORMAT ERROR: " + TCONST.AGDESC + " : ", e, false);
        }
    }

    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

    }


}
