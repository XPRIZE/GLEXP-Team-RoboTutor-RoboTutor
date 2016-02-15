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

package cmu.xprize.robotutor.tutorengine.graph;


import android.util.Log;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.common.TCONST;


/**
 * This represents the top level animation graph object
 */
public class scene_animator extends graph_node implements ILoadableObject{

    // State fields
    private graph_node   _currNode;
    private String       _nodeState;

    private HashMap<String, Integer> _pFeatures = new HashMap<String, Integer>();


    // json loadable fields
    public String  version;
    public String  rootnode;

    // These are only here to have JSON_Help parse for these maps
    public HashMap nodeMap;
    public HashMap moduleMap;
    public HashMap actionMap;
    public HashMap choiceMap;
    public HashMap constraintMap;


    static private final String TAG = "ANIMATOR";


    public scene_animator() { }


    public void seekRoot() {
        try {
            _currNode = (graph_node) getScope().mapSymbol(rootnode);
        }
        catch(Exception e) {
            // TODO: manage root node not found
        }

        if(_currNode != null) {
            Log.d(TAG, "Running Node: " + _currNode.name);

            _currNode.preEnter();
            _currNode.applyNode();
        }
        else {
            Log.d(TAG, "No Root Node for Scene");
        }
    }


    /**
     * increments the curranimation polymorphically
     * potentially called recursively if currNode is a subgraph.
     *
     * @return The
     */
    @Override
    public String next() {

        // When we retrieve the "next" node we automatically apply it
        // This may result in a simple state change - method call etc.
        // this returns TCONST.DONE indicating the event is complete
        //
        // It may start a process that need to complete before continuing.
        // returning TCONST.WAIT indicating that next will be driven by a
        // completion event - or some external user event.
        //
        // A result of TCONST.NONE indicated the source node is exhausted.
        // which will drive a search for the next node
        //
        if (_currNode != null) do {

            // Increment the animation polymorphically

            _nodeState = _currNode.next();

            // If the node is exhausted move to next node

            if (_nodeState.equals(TCONST.NONE)) {
                _currNode = _currNode.nextNode();

                if (_currNode != null) {
                    Log.d(TAG, "Running Node: " + _currNode.name);

                    // Apply action nodes
                    _nodeState =_currNode.applyNode();
                }
                else {
                    _nodeState = TCONST.NEXTSCENE;
                    break;
                }
            }

        } while (!_nodeState.equals(TCONST.WAIT) && !_nodeState.equals(TCONST.NEXTSCENE));


        return _nodeState;
    }


    @Override
    public void play() {
        _currNode.play();
    }


    @Override
    public void stop() {
        _currNode.stop();
    }


    public String gotoNode(String nodeName) {

        _currNode.stop();
        _currNode.preExit();

        try {
            _currNode = (graph_node) getScope().mapSymbol(nodeName);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Apply the node and continue
        _currNode.preEnter();

        return _currNode.applyNode();
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

}
