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

import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.TCONST;

public class type_action extends graph_node {

    // json loadable
    public String          id;
    public String          cmd;
    public String          mode = TCONST.AUDIOEVENT;
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

        int   iter = _scope.sceneGraph().queryPFeature(pid, _prob.length, cycle);
        float rand = (float)Math.random();

        // It's important to be < not <= because if we have 0 prob we never want it to fire.

        Log.d(TAG, "PFeature: " + ((rand < Float.parseFloat(_prob[iter]))? "passed":"failed"));
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

        String returnState = TCONST.DONE;
        Map    childMap    = _scope.tutorGraph().getChildMap();

        if(cmd != null) {
            switch(cmd) {

                // System level command to launch a new Tutor Instance.
                //
                case TCONST.CMD_LAUNCH:
                    try {
                        // We demand a parm list of the form intent:String|intentdata:String
                        //
                        List<String> parmList = Arrays.asList(parms.split("[:\\|]"));

                        // Resolve any variables in the parameters.
                        // Session manager uses TScope variables to store intents
                        //
                        String intent     = getScope().resolveTemplate(parmList.get(0));
                        String intentData = getScope().resolveTemplate(parmList.get(2));
                        String features   = getScope().resolveTemplate(parmList.get(4));

                        CTutorEngine.launch(intent, intentData, features);
                    }
                    catch(Exception e) {
                        Log.e(TAG, "Launch Command Invalid: " + e);
                    }
                    break;

                case TCONST.CMD_GOTO:
                    _scope.tutor().gotoNode(id);
                    break;

                case TCONST.CMD_NEXT:
                    _scope.tutor().eventNext();
                    break;

                case TCONST.CMD_WAIT:
                    returnState = TCONST.WAIT;
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
                    // The parameters come in - Name:Class|Name:Class...
                    // So in the split array the odd elements are parameter values and the
                    // even elements are the associated base-Class(type).

                    Class[]  pType  = null;
                    Object[] iparms = null;

                    // TODO: Fixup support for , delimited parm lists
                    // TODO: This will require FSM or REGEX processing to allow : and | in strings.
                    //
                    if(parms != null) {

                        // Break up the parms specification - "parms":"value:type|value:type..."
                        //
                        List<String> parmList = Arrays.asList(parms.split("[:\\|]"));

                        // Create the arrays
                        pType  = new Class[parmList.size() / 2];
                        iparms = new Object[parmList.size() / 2];

                        for (int i1 = 1, i2 = 0; i1 < parmList.size(); i1 += 2, i2++) {

                            // Force lowercase on classname (type) and translate to Class object
                            //
                            parmList.set(i1, parmList.get(i1).toLowerCase());
                            pType[i2] = classMap.get(parmList.get(i1));

                            // Generate the actual parameter object to pass to the method
                            //
                            try {
                                iparms[i2] = pType[i2].getConstructor(new Class[]{String.class}).newInstance(parmList.get(i1 - 1));

                            } catch (Exception e) {
                                // TODO: Update this exception -  it is actually an invalid parm type error
                                e.printStackTrace();
                                Log.e(TAG, "ERROR: " + id + " - Method: <" + method + "> Not Found: " + e);
                                System.exit(1);
                            }
                        }
                    }

                    try {
                        // Find the target object by its id
                        // get the method on the target and apply it with the parameter array created above.
                        //
                        Log.d(TAG, childMap.get(id).toString());
                        childMap.get(id).getClass();

                        Method _method = childMap.get(id).getClass().getMethod(method, pType);

                        _method.invoke(childMap.get(id), iparms);

                    } catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "ERROR: "+ id + " - Apply Method: " + method + "   Parms: "+ parms + " : " + e);
                        System.exit(1);
                    }
                    break;
            }
        }

        return returnState;
    }



    // *** Serialization


    /**
     * Load the object from the factory data
     *
     * @param jsonObj
     * @param scope
     */
    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        // Always call super to init _scope - or do it yourself
        //
        super.loadJSON(jsonObj, scope);

        // Custom post processing.
        // If there are probabilities defined for the feature
        // generate an array of the prob for iterations of this pid
        if($P != null)
            _prob = $P.split("\\|");
    }

}
