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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.TCONST;



public class type_action extends scene_node {

    // json loadable
    public String          id;
    public String          cmd;
    public String          mode = TCONST.AUDIOEVENT;
    public String          value;

    public String          method;          // Invokes method with parms. using apply
    public String          parms;
    public String          decodedParms;

    public String          features = "";   // default to no features

    public String          pid;			    // GUID for stocastic object
    public String          $P;              // probabilities - encoded
    public int             cycle;	    	// recycle distance for looping
    public String[]        _prob;   	    // Array of probabliities for given PID


    // To simplify json syntax we translate a pseudo "Class" type in json spec to true Java type.
    //
    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    static {
        classMap.put("string", String.class);
        classMap.put("bool", Boolean.class);
        classMap.put("boolean", Boolean.class);
        classMap.put("int", Integer.class);
        classMap.put("integer", Integer.class);
        classMap.put("float", Float.class);
        classMap.put("byte", Byte.class);
        classMap.put("long", Long.class);
        classMap.put("short", Short.class);
        classMap.put("object", Object.class);
    }

    final static public String TAG = "type_action";


    @Override
    public boolean testFeatures() {

        boolean        featurePass = false;

        // If this scene is not in the feature set for the tutor then check the next one.

        if(!features.equals(""))
        {
            featurePass = _scope.tutor().testFeatureSet(features);

            if(featurePass)
            {
                // Check Probability Feature if present

                if(hasPFeature())
                {
                    featurePass = testPFeature();
                }
            }
        }

        // unconditional tracks pass automatically - unless they have PFeature

        else
        {
            // Check Probability Feature if present

            if(hasPFeature())
            {
                featurePass = testPFeature();
            }
            else featurePass = true;
        }

        return featurePass;
    }


    public boolean testPFeature() {

        int   iter = _scope.sceneGraph().queryPFeature(pid, _prob.length, cycle);
        float rand = (float)Math.random();

        // It's important to be < not <= because if we have 0 prob we never want it to fire.

        Log.i(_logType, "PFeature: " + ((rand < Float.parseFloat(_prob[iter]))? "passed":"failed"));
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

        String  returnState = TCONST.DONE;
        Map     childMap    = _scope.tutorGraph().getChildMap();

        if (cmd != null) {
            switch (cmd) {

                case TCONST.NEXTSCENE:
                    _scope.tutor().mTutorGraph.post(this, TCONST.NEXTSCENE);
                    break;

                case TCONST.CANCEL_NODE:
                case TCONST.SUBGRAPH_RETURN_AND_GO:
                case TCONST.SUBGRAPH_RETURN_AND_WAIT:
                    _scope.sceneGraph().post(this, cmd);
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

                case TCONST.CMD_DEBUG:
                    Log.d(TAG, "Debug Hit");
                    break;

                // By default try and find a matching actionMap object to execute.
                // Note that you may not call a Module in this way.
                //
                default:
                    IScriptable2 obj = null;

                    try {
                        obj = _scope.mapSymbol(cmd);

                        if(obj != null) {

                            switch(obj.getType()) {
                                case TCONST.MODULE:
                                    Log.e(_logType, "Attempt to call Module: " + cmd + " : Modules may not be called.");
                                    break;

                                case TCONST.NODE:
                                    Log.e(_logType, "Attempt to call Node: " + cmd + " : Nodes may not be called.");
                                    break;

                                case TCONST.CONDITION:
                                    Log.e(_logType, "Attempt to call Condition: " + cmd + " : Conditions may not be called.");
                                    break;

                                default:
                                    if(obj.testFeatures()) {
                                        returnState = obj.applyNode();
                                    }
                                    else {
                                        returnState = TCONST.DONE;
                                    }
                                    break;
                            }
                        }

                    } catch (Exception e) {

                        // TODO: Manage invalid Behavior
                        e.printStackTrace();
                    }

                    break;
            }
        } else if (method != null && !method.equals("")) {

            switch (method) {
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

                    Class[] pType = null;
                    Object[] iparms = null;

                    // TODO: Fixup support for , delimited parm lists
                    // TODO: This will require FSM or REGEX processing to allow : and | in strings.
                    //
                    if (parms != null) {

                        // Support templatized parameters
                        //
                        decodedParms = getScope().parseTemplate(parms);

                        // Break up the parms specification - "parms":"value:type|value:type..."
                        //
                        List<String> parmList = Arrays.asList(parms.split("[:\\|]"));

                        // Create the arrays
                        pType = new Class[parmList.size() / 2];
                        iparms = new Object[parmList.size() / 2];

                        for (int i1 = 1, i2 = 0; i1 < parmList.size(); i1 += 2, i2++) {

                            // Support templatized parameters
                            // decode the template - this must go here so variables may have embedded colons
                            // which are used in the tutor descriptors in the session Manager button
                            // messages
                            //
                            parmList.set(i1-1, getScope().parseTemplate(parmList.get(i1-1)));

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
                                CErrorManager.logEvent(TAG, "Script internal ERROR: " + id + " method: <" + method + "> Not Found: ", e, false);
                            }
                        }
                    }

                    try {
                        // Find the target object by its id
                        // get the method on the target and apply it with the parameter array created above.
                        //
                        if(childMap.containsKey(id)) {
                            //Log.d(TAG, childMap.get(id).toString());
                            // childMap.get(id).getClass();

                            Method _method = childMap.get(id).getClass().getMethod(method, pType);

                            _method.invoke(childMap.get(id), iparms);

                            if(!method.equals(TCONST.LOGSTATE)) {

                                if (parms != null) {
                                    // Note the logging parser expects comma delimiters
                                    //
                                    decodedParms = decodedParms.replaceAll("\\|", ",");
                                    RoboTutor.logManager.postEvent_V(_logType, "target:node.action,view:" + id + ",method:" + method + "," + decodedParms);
                                }
                                else {
                                    RoboTutor.logManager.postEvent_V(_logType, "target:node.action,view:" + id + ",method:" + method );
                                }

                            }
                        }

                        // If it is not a display object then check for scope objects i.e. nodes
                        //
                        else {
                            Method _method = getScope().mapSymbol(id).getClass().getMethod(method, pType);

                            _method.invoke(getScope().mapSymbol(id), iparms);

                            if(!method.equals(TCONST.LOGSTATE)) {
                                if (parms != null) {
                                    // Note the logging parser expects comma delimiters
                                    //
                                    decodedParms = decodedParms.replaceAll("\\|", ",");
                                    RoboTutor.logManager.postEvent_V(_logType, "target:node.action,scopevar:" + id + ",method:" + method + "," + decodedParms);
                                }
                                else {
                                    RoboTutor.logManager.postEvent_V(_logType, "target:node.action,scopevar:" + id + ",method:" + method);
                                }
                            }

                        }
                    }
                    catch (Exception e) {
                        CErrorManager.logEvent(_logType, "target:node.action,error:Script internal ERROR,name:" + id + ",method:" + method + ",parms:" + decodedParms + ",exception:", e, true);
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
