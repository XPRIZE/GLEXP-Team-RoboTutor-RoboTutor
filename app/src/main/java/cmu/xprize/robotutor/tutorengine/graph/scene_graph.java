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

package cmu.xprize.robotutor.tutorengine.graph;


import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.DEBUG_HESITATE;


/**
 * This represents the top levelFolder tutor graph object
 */
public class scene_graph extends scene_node implements ILoadableObject2 {

    // State fields
    private scene_node        _currNode;
    private String            _nodeState;

    // json loadable fields
    public String  version;
    public String  rootnode;

    // These are only here to have JSON_Help parse for these maps
    public HashMap nodeMap;
    public HashMap moduleMap;
    public HashMap actionMap;
    public HashMap choiceMap;
    public HashMap constraintMap;
    public HashMap subgraphMap;
    public HashMap queueMap;

    static private final String TAG = "scene_graph";


    /**
     * The tutor_node for
     */
    public scene_graph() {
        _currNode  = this;
    }


    /**
     * When scene is complete we need to ensure all the moduleQueues are shutdown so delayed
     * actions do not occur after destruction.
     */
    public void onDestroy() {

        // Walk the scene_graphqueues to teminate them gracefully
        //
        if (queueMap != null) {

            Iterator<?> tObjects = queueMap.entrySet().iterator();

            while (tObjects.hasNext()) {
                Map.Entry entry = (Map.Entry) tObjects.next();

                scene_queuedgraph sceneQueue = (scene_queuedgraph) entry.getValue();

                sceneQueue.cancelNode();
            }
        }
    }


    /**
     *  Increments the scenegraph polymorphically
     *  potentially called recursively if currNode is a subgraph.
     *
     * @return The
     */
    @Override
    public String applyNode() {

        // TODO: Watch for feature reactivity and preenter when this is implemented.

        if (_currNode != null) {

            // This is the mechanism by which Modules step through multiple internal actions
            // However this is polymorphic where:
            //
            // Simple (Action type) nodes will always return:
            //      NONE (the node is exhausted - no more actions)
            //
            // Complex (Module type) nodes may return:
            //      READY(start state)
            //      WAIT (the result from previous module track (step)) or
            //      NONE (the node is exhausted - no more actions)

            _nodeState = _currNode.next();

            switch(_nodeState) {

                // If the node is exhausted move to next node

                case TCONST.NONE:

                    // When we retrieve the "next" node we automatically apply it
                    // by falling through

                    _currNode = _currNode.nextNode();

                    if (_currNode == null) {
                        RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph.applyNode,event:END_OF_GRAPH");

                        _nodeState = TCONST.END_OF_GRAPH;
                        break;
                    }
                    // otherwise fall through and apply next node action

                case TCONST.WAIT:
                case TCONST.READY:
                case TCONST.DONE:

                    // This may result in a simple state change - method call etc.
                    // which returns TCONST.DONE indicating the event is complete
                    //
                    // It may start a process that needs to complete before continuing.
                    //
                    // A result of TCONST.NONE indicated the complex source node is exhausted.
                    // which will drive a search for the next node
                    //
                    RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph.applyNode,name:" + _currNode.name + ",start State:" + _nodeState + ",mapType:" + _currNode.maptype + ",mapName:" + _currNode.mapname);

                    if(_currNode.testFeatures()) {
                        _nodeState = _currNode.applyNode();
                    }
                    else {
                        _nodeState = TCONST.DONE;
                    }

                    RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph.applyNode,name:" + _currNode.name + ",end State:" + _nodeState);

                    break;
            }
        }

        return _nodeState;
    }


    /**
     */
    @Override
    public String cancelNode() {

        // TODO: Watch for feature reactivity and preenter when this is implemented.

        Log.d(DEBUG_HESITATE, "scene_graph.cancelNode" + _currNode.name);

        if ((_currNode != null) && (_currNode != this)) {

            _currNode.cancelNode();

            _currNode = _currNode.nextNode();

            if (_currNode == null) {
                RoboTutor.logManager.postEvent_I(TAG, "target:node.scenegraph: Processing END Node: ");

                _nodeState = TCONST.END_OF_GRAPH;
            }

            // This may result in a simple state change - method call etc.
            // which returns TCONST.DONE indicating the event is complete
            //
            // It may start a process that needs to complete before continuing.
            //
            // A result of TCONST.NONE indicated the complex source node is exhausted.
            // which will drive a search for the next node
            //
            else {
                RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph,name:" + _currNode.name + ",start State:" + _nodeState + ",mapType:" + _currNode.maptype + ",mapName:" + _currNode.mapname);

                if(_currNode.testFeatures()) {
                    _nodeState = _currNode.applyNode();
                }
                else {
                    _nodeState = TCONST.DONE;
                }
                RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph,name:" + _currNode.name + ",end State:" + _nodeState);
            }
        }

        return _nodeState;
    }


    @Override
    public String next() {
        String result = TCONST.READY;

        try {
            _currNode = (scene_node) getScope().mapSymbol(rootnode);
        }
        catch(Exception e) {
            CErrorManager.logEvent(_logType,"target:node.scenegraph,event:Root Node not found", e, false);
        }

        if(_currNode != null) {

            RoboTutor.logManager.postEvent_I(_logType, "target:node.root,name:" + _currNode.name + ",start State:" + _nodeState + ",mapType:" + _currNode.maptype + ",mapName:" + _currNode.mapname);

            // TODO: Check if preenter is used - I think we only want this for scene preenter/exit
            _currNode.preEnter();
            result = TCONST.READY;
        }
        else {
            RoboTutor.logManager.postEvent_I(_logType, "target:node.scenegraph,event:No Root Node for Scene");
            result = TCONST.NEXTSCENE;
        }

        return result;
    }


    @Override
    public void resetNode() {
        super.resetNode();

        _currNode  = this;
        _nodeState = null;
    }

    @Override
    public void play() {
        _currNode.play();
    }

    @Override
    public void play(Long duration) {
        _currNode.play(duration);
    }


    @Override
    public void stop() {
        _currNode.stop();
    }


    public String gotoNode(String nodeName) {

        _currNode.stop();
        _currNode.preExit();

        try {
            _currNode = (scene_node) getScope().mapSymbol(nodeName);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(_currNode.testFeatures()) {

            // Apply the node and continue
            _currNode.preEnter();

            _nodeState = _currNode.applyNode();
        }
        else {
            _nodeState = TCONST.DONE;
        }

        return _nodeState;
    }

}
