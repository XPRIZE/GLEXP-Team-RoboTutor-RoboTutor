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

import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;



public class graph_edge implements ILoadableObject2 {

    private TScope      mScope;

    // json loadable fields
    public String       constraint;
    public String       edge;


    static private final String TAG = "graph_edge";


    /**
     * Simple Constructor
     */
    public graph_edge() { }


    public boolean testConstraint() {

        boolean result = true;

        type_cond constr = null;

        try {
            constr = (type_cond)mScope.mapSymbol(constraint);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(constr != null)
            result = (boolean)constr.evaluate(false);

        return 	result;
    }



    public scene_node followEdge() {
        scene_node nextNode = null;

        try {
            nextNode = (scene_node)mScope.mapSymbol(edge);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nextNode;
    }



    // *** Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        mScope = (TScope)scope;
        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);
    }


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

    @Override
    public String toString() {
        return "graph_edge:constraint=" + constraint + ",edge=" + edge;
    }
}
