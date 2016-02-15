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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorNavigator;
import cmu.xprize.common.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

public class type_action extends graph_node {

    private scene_animator _parent;

    // json loadable
    public String          id;
    public String          cmd;
    public String          value;

    public String          method;          // Invokes method with parms. using apply
    public String          parms;

    public String          features = "";   // default to no features

    public String          pid;			    // GUID for stocastic object
    public String          $P;              // probabilities - encoded
    public int             cycle;	    	// recycle distance for looping
    public String[]        _prob;   	    // Array of probabliities for given PID


    // To simplify json syntax we translate a pseudo "Class" type in json spec to true Java type.

    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    final static public String TAG = "type_action";


    static {
        classMap.put("string", String.class);
        classMap.put("bool", Boolean.class);
        classMap.put("boolean", Boolean.class);
        classMap.put("int", Integer.class);
        classMap.put("float", Float.class);
        classMap.put("byte", Byte.class);
        classMap.put("long", Long.class);
        classMap.put("short", Short.class);
        classMap.put("object", Object.class);
    }


    public boolean testPFeature() {

        int   iter = _parent.queryPFeature(pid, _prob.length, cycle);
        float rand = (float)Math.random();

        // It's important to be < not <= because if we have 0 prob we never want it to fire.

        return (rand < Float.parseFloat(_prob[iter]));
    }


    public boolean hasPFeature() {
        return (pid != null);
    }


    public void setFeatures(String newFTR) {
        features = newFTR;
    }


    public String getFeatures() {
        return features;
    }


    /**
     * Apply the type_action - it can be of various types:
     *
     */
    @Override
    public String applyNode() {
        Map childMap = CTutorNavigator.getChildMap();

        if(cmd != null) {
            switch(cmd) {
                case TCONST.GOTONODE:
                    CTutor.gotoNode(id);
                    break;
            }
        }

        else  if(method != null && !method.equals("")) {

            switch(method) {
                case "=":
                    try {
                        getScope().mapSymbol(id).set(value);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "+":
                    try {
                        getScope().mapSymbol(id).add(value);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "-":
                    try {
                        getScope().mapSymbol(id).subtract(value);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                // The parameters come in - Name:Class:Name:Class...
                // So odd elements are parameter names and the subsequent
                // element is its encoded base type.

                List<String> parmList = Arrays.asList(parms.split(":"));

                Class[] pcls = new Class[parmList.size() / 2];
                Object[] iparms = new Object[parmList.size() / 2];

                for (int i1 = 1, i2 = 0; i1 < parmList.size(); i1 += 2) {
                    parmList.set(i1, parmList.get(i1).toLowerCase());
                    pcls[i2] = classMap.get(parmList.get(i1));

                    try {
                        iparms[i2] = pcls[i2].getConstructor(new Class[]{String.class}).newInstance(parmList.get(i1 - 1));

                    } catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "ERROR: " + id + " - Method: <" + method + "> Not Found: " + e);
                        System.exit(1);
                    }
                }

                try {
                    //Method _method = Button.class.getMethod("setText", CharSequence.class);

                    Log.d(TAG, childMap.get(id).toString());
                    childMap.get(id).getClass();

                    Method _method = childMap.get(id).getClass().getMethod(method, pcls);

                    _method.invoke(childMap.get(id), iparms);

                } catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR: ApplyNode: " + id + "  " + e);
                    System.exit(1);
                }
                break;
            }
        }

        return TCONST.DONE;
    }



    // *** Serialization


    /**
     * Load the object from the factory data
     *
     * @param jsonObj
     * @param scope
     */
    @Override
    public void loadJSON(JSONObject jsonObj, TScope scope) {

        // Always call super to init _scope - or do it yourself
        //
        super.loadJSON(jsonObj, scope);

        // Custom post processing.
        // If there are probabilities defined for the feature
        // generate an array of the prob for iterations of this pid
        if($P != null)
            _prob = $P.split("|");
    }

}
