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

import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;

public class graph_module extends graph_node implements ILoadableObject2 {

    private int               _ndx;
    private String            _moduleState;
    type_action               _nextAction;

    // json loadable fields
    public type_action[]      tracks;
    public Boolean            reuse;

    public HashMap            moduleMap;
    public HashMap            actionMap;
    public HashMap            choiceMap;
    public HashMap            constraintMap;

    static private final String TAG = "graph_module";


    /**
     * Simple Constructor
     */
    public graph_module() {
    }


    @Override
    public void preEnter()
    {
        resetNode();
        super.preEnter();
    }


    /**
     * As with all overrides of "next" it should only ever be called from applyNode in
     * scene_animator
     *
     * @return
     */
    @Override
    public String next() {

        if(_nextAction != null)
            _nextAction.preExit();

        if(_ndx >= tracks.length)
            _moduleState = TCONST.NONE;

        return _moduleState;
    }



    @Override
    public String applyNode() {

        String         features;
        boolean        featurePass = false;

        // If new scene has features, check that it is being used in the current tutor feature set
        // Note: You must ensure that there is a match for the last scene in the sequence

        do {
            if(_ndx < tracks.length)
            {
                _nextAction = tracks[_ndx];

                _ndx++;

                features = _nextAction.features;

                // If this scene is not in the feature set for the tutor then check the next one.

                if(!features.equals(""))
                {
                    featurePass = _scope.tutor().testFeatureSet(features);

                    if(featurePass)
                    {
                        // Check Probability Feature if present

                        if(_nextAction.hasPFeature())
                        {
                            featurePass = _nextAction.testPFeature();
                        }
                    }
                }

                // unconditional tracks pass automatically - unless they have PFeature

                else
                {
                    // Check Probability Feature if present

                    if(_nextAction.hasPFeature())
                    {
                        featurePass = _nextAction.testPFeature();
                    }
                    else featurePass = true;
                }

                // If the feature test passes then fire the event.
                // Otherwise set flag to indicate event was completed/skipped in this case
                if(featurePass)
                {
                    Log.d(TAG, "Animation Feature: " + features + " passed:" + featurePass);

                    _nextAction.preEnter();

                    _moduleState = _nextAction.applyNode();

                    break;		// leave the loop
                }
                else {
                    Log.i(TAG, "Feature Test Failed: ");
                    _moduleState = TCONST.DONE;
                }
            }
            else {
                _moduleState = TCONST.NONE;
                break;
            }

        }while(_moduleState.equals(TCONST.DONE));


        return _moduleState;
    }


    /**
     * If we have exhausted the node check if it can be reused - if so reinitialize it for
     * the next time it is called.
     *
    */
    @Override
    public void resetNode() {

        if(_ndx >= tracks.length)
        {
            if(reuse) {
                _ndx         = 0;
                _moduleState = TCONST.READY;
            }
        }

    }


    // Symbol resolution - This allows local maps within modules.
    //
    public IScriptable2 mapSymbol(String symbolName) throws Exception {

        IScriptable2 result = null;

        if(actionMap != null && ((result = (type_action) actionMap.get(symbolName)) == null)) {

            if(moduleMap != null && ((result = (graph_module) moduleMap.get(symbolName)) == null)) {

                if((choiceMap != null) && ((result = (type_choiceset) choiceMap.get(symbolName)) == null)) {

                    if (constraintMap != null)
                        result = (type_cond) constraintMap.get(symbolName);
                }
            }
        }

        if(result == null)
            throw(new Exception("Symbol not found: " + symbolName));

        return result;
    }
}
