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

import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.robotutor.tutorengine.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;

public class graph_node implements ILoadableObject, IScriptable {

    private TScope          _scope;

    // json loadable fields
    public String           parser;      // Used to distinguish different Flash content parsers
    public String           type;        // used by JSON loader to disambiguate object type
    public String           name;
    public String[]         preenter;
    public String[]         preexit;

    public graph_edge[]     edges;

    static private final String TAG = "graph_node";


    /**
     * Simple Constructor
     */
    public graph_node() {
    }


    protected TScope getScope() {
        if(_scope == null) {
            Log.e(TAG, "Engine Error: Invalid Scope on Object: " + name);
            System.exit(1);
        }

        return _scope;
    }


    // IScriptable
    @Override
    public void set(String value) {

    }

    @Override
    public void add(String value) {

    }

    @Override
    public void subtract(String value) {

    }

    @Override
    public String getName() { return name; }

    @Override
    public String getType() { return type; }

    @Override
    public Object getValue() { return null; }



    public String next() {
        return TCONST.NONE;
    }


    public graph_node nextNode() {

        graph_node node = null;		// When we run out of tracks we just want to stop

        preExit();

        for(graph_edge edge : edges) {

            if(edge.testConstraint())
            {
                node = edge.followEdge();

                if(node != null)
                        node.preEnter();

                break;
            }
        }

        return node;
    }


    // Used by Animation graph to init root animation

    public void preEnter()
    {
        if(preenter != null)
        {
            apply(preenter);
        }
    }

    // Used by Animation graph to init root animation

    public void preExit()
    {
        if(preexit != null)
        {
            apply(preexit);
        }
    }

    // preenter / preexit action resolution.
    //
    private void apply(String[] mapSet) {

        for (String nodeName : mapSet) {
            try {
                IScriptable node = getScope().mapSymbol(nodeName);

                node.applyNode();

            } catch (Exception e) {
                Log.e(TAG, "ERROR: Symbol Not found:" + nodeName + " : " + e);
                System.exit(1);
            }
        }
    }


    public String seekToAnimation(String seek) {
        return null;
    }

    public void play() {    }

    public void stop() {    }

    public String applyNode() {
        return TCONST.DONE;
    }

    public void resetNode() {}


    public IScriptable mapReference(String refName) {

        return null;
    }

    public IScriptable mapProperty(String propName) {

        return null;
    }


    // IScriptable
    @Override
    public TBoolean OR(IScriptable RHS, boolean lneg, boolean rneg) {
        return null;
    }

    @Override
    public TBoolean AND(IScriptable RHS, boolean lneg, boolean rneg) {
        return null;
    }

    @Override
    public TBoolean LT(IScriptable RHS) {
        return null;
    }

    @Override
    public TBoolean LTEQ(IScriptable RHS) {
        return null;
    }

    @Override
    public TBoolean GT(IScriptable RHS) {
        return null;
    }

    @Override
    public TBoolean GTEQ(IScriptable RHS) {
        return null;
    }

    @Override
    public TBoolean EQ(IScriptable RHS) {
        return null;
    }

    @Override
    public TBoolean NEQ(IScriptable RHS) {
        return null;
    }

    @Override
    public Object evaluate(boolean neg) {
        return false;
    }



    // *** Serialization


    public void loadJSON(JSONObject jsonObj, TScope scope) {

        // CRITICAL
        // Capture the scope for this node -
        // Used when dereferencing Symbols within the nodes execution context
        //
        _scope = scope;

        JSON_Helper.parseSelf(jsonObj, this, scope);
    }
}
