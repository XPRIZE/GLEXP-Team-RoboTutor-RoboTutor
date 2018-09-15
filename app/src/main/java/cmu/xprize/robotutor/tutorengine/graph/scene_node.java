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

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.CStartView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.IEventSource;
import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;

import static cmu.xprize.util.TCONST.GRAPH_MSG;

public class scene_node implements ILoadableObject2, IScriptable2, IEventSource, IEventListener, IEventDispatcher
{

    protected IScope2       _scope;
    protected String        _logType;

    private HashMap<IEventListener,IEventListener> mListeners = new HashMap<>();

    // json loadable fields
    public String           parser;      // Used to distinguish different Flash content parsers
    public String           type;        // used by JSON loader to disambiguate object type
    public String           name;

    public String           maptype;
    public String           mapname;

    public String[]         preenter;
    public String[]         preexit;

    public graph_edge[]     edges;


    static private final String TAG = "scene_node";


    /**
     * Simple Constructor
     */
    public scene_node() {

        _logType = GRAPH_MSG;
    }


    protected IScope2 getScope() {

        if(_scope == null) {
            CErrorManager.logEvent(TAG, "Engine Error: Invalid Scope on Object: " + name, null, false);
        }

        return _scope;
    }


    // non-action nodes are not feature reactive.  Always return true
    //
    public boolean testFeatures() {

        return true;
    }


        //************************************************
    // IEvent...  START
    //

    @Override
    public boolean isGraphEventSource() {
        return false;
    }


//    @Override
    public void addViewListener(String listener) {

        try {

            Map childMap    = _scope.tutorGraph().getChildMap();

            if(childMap.containsKey(listener)) {

                IEventListener sink = (IEventListener)childMap.get(listener);

                addEventListener(sink);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEventListener(String listener) {

        try {
            IEventListener sink = (IEventListener) _scope.mapSymbol(listener);

            addEventListener(sink);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEventListener(IEventListener listener) {

        try {
            mListeners.put(listener, listener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatchEvent(IEvent event) {

        Iterator<?> tObjects = mListeners.entrySet().iterator();

        // We use a hashmap to ensure no duplicates - we only call onEvent once
        // per listener instance
        //
        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            IEventListener listener = (IEventListener)entry.getValue();
            listener.onEvent(event);
        }
    }

    // Override to provid class specific functionality
    //
    @Override
    public void onEvent(IEvent eventObject) {
    }

    //
    // IEvent...  END
    //************************************************



    // IScriptable2
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
    public void setName(String newName) {  name = newName; }

    @Override
    public String getName() { return name; }

    @Override
    public String getType() { return type; }

    @Override
    public Object getValue() { return null; }


    /**
     * Next allows modules to loop through multiple actions in a node.
     *
     * @return
     */
    public String next() {
        return TCONST.NONE;
    }


    public scene_node nextNode() {


        scene_node node = null;		// When we run out of tracks we just want to stop

        preExit();

        for(graph_edge edge : edges) {

            RoboTutor.logManager.postEvent_V(TAG, "target:node.edge,action:test,name:" + edge.constraint );

            if(edge.testConstraint())
            {
                RoboTutor.logManager.postEvent_V(TAG, "target:node.edge,action:taken,name:" + edge.edge );

                node = edge.followEdge();


                if(node != null)
                        node.preEnter();

                break;
            }
        }

        return node;
    }


    /**
     */
    public String cancelNode() {
        return TCONST.NONE;
    }


        // TODO: Ideally this would be protected
    // Used by Animation graph to init root animation
    // Note: Modules use this to reinisitalize themselves

    public void preEnter()
    {
        if(preenter != null)
        {
            applyCommandSet("target:node.preenter: ", preenter);
        }
    }


    // TODO: Ideally this would be protected
    // Used by Animation graph to init root animation

    public void preExit()
    {
        if(preexit != null)
        {
            applyCommandSet("target:node,action:preexit", preexit);
        }
    }


    // preenter / preexit action resolution.
    //
    private void applyCommandSet(String tag, String[] commandSet) {

        if(commandSet.length > 0) {

            RoboTutor.logManager.postEvent_D(_logType, tag + ",event:start,name:" + name);

            for (String nodeName : commandSet) {

                try {
                    IScriptable2 node = (IScriptable2) getScope().mapSymbol(nodeName);

                    if(node.testFeatures()) {
                        node.applyNode();
                    }
                } catch (Exception e) {
                    CErrorManager.logEvent(TAG, "Script Internal ERROR: ENTER-EXIT Symbol Not found:" + nodeName + " : ", e, false);
                }
            }

            RoboTutor.logManager.postEvent_D(_logType, tag + ",event:end,name:" + name);
        }
    }

    @Override
    public String getEventSourceName() {
        return name;
    }

    @Override
    public String getEventSourceType() {
        return type;
    }

    public String seekToAnimation(String seek) {
        return null;
    }

    public void play() {   }

    public void play(Long duration) { }

    public void stop() {    }

    public String applyNode() {
        return TCONST.DONE;
    }

    public void resetNode() {}


    public IScriptable2 mapReference(String refName) {

        return null;
    }

    public IScriptable2 mapProperty(String propName) {

        return null;
    }


    // IScriptable2
    @Override
    public TBoolean OR(IScriptable2 RHS, boolean lneg, boolean rneg) {
        boolean result = false;

        if((Boolean)evaluate(lneg) || (Boolean)RHS.evaluate(rneg)) {
            result = true;
        }

        return new TBoolean(result);
    }

    @Override
    public TBoolean AND(IScriptable2 RHS, boolean lneg, boolean rneg) {
        boolean result = false;

        if((Boolean)evaluate(lneg) && (Boolean)RHS.evaluate(rneg)) {
            result = true;
        }

        return new TBoolean(result);
    }

    @Override
    public TBoolean LT(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean LTEQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean GT(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean GTEQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean EQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean NEQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public String resolve(int index) {
        return null;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public Object evaluate(boolean neg) {
        return false;
    }




    // *** Serialization




    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        // CRITICAL
        // Capture the scope for this node -
        // Used when dereferencing Symbols within the nodes execution context
        //
        _scope = scope;

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}


