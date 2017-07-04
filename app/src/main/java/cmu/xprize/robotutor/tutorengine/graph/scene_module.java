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

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.TCONST;


public class scene_module extends scene_node implements ILoadableObject2 {

    // Default to READY state so if it is first node next will return a valid
    // node status
    //
    protected int               _ndx         = 0;
    protected String            _moduleState = TCONST.READY;
    protected type_action       _nextAction  = null;

    // json loadable fields
    public type_action[]      tracks;
    public Boolean            reuse;

    public HashMap            moduleMap;
    public HashMap            actionMap;
    public HashMap            choiceMap;
    public HashMap            constraintMap;

    static private final String TAG = "scene_module";


    /**
     * Simple Constructor
     */
    public scene_module() {
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


    /**
     * As with all overrides  - "next" should only ever be called from applyNode in
     * tutor_node
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


    /**
     * TODO: Examine externalize the loop mechanism so we can interrupt more readily
     * Note that modules and nodes are not feature reactive
     *
     * @return
     */
    @Override
    public String applyNode() {

        String         features;
        boolean        featurePass = false;
        boolean        validAction = false;

        // If the node is completed and reusable then reset
        //
        resetNode();

        RoboTutor.logManager.postEvent_I(_logType, "target:node.module,name:" + name + ",event:start,modulestate:" + _moduleState);

        // TODO: At the moment this loop is used to allow Module callouts from timer events
        // TODO: Make it so that timer events run in their own graph so this is not needed.
        //
        do {
            validAction = false;

            // Issue #58 - Make all actions feature reactive.
            //
            while(_ndx < tracks.length) {

                _nextAction = tracks[_ndx];
                _ndx++;

                if(_nextAction.testFeatures()) {
                    validAction = true;
                    break;
                }
            }

            if(validAction)
            {
                _nextAction.preEnter();

                if(_nextAction.testFeatures()) {
                    _moduleState = _nextAction.applyNode();
                }
                else {
                    _moduleState = TCONST.DONE;
                }
            }
            else {
                _moduleState = TCONST.NONE;
            }

        }while(_moduleState.equals(TCONST.DONE));

        RoboTutor.logManager.postEvent_I(_logType, "target:node.module,name:" + name + ",event:end,modulestate:" + _moduleState);

        return _moduleState;
    }


    /**
     */
    public String cancelNode() {

        _ndx = 0;
        _moduleState = TCONST.READY;

        if(_nextAction != null)
            _nextAction.cancelNode();

        return TCONST.NONE;
    }


    // Symbol resolution - This allows local maps within modules.
    //
    public IScriptable2 mapSymbol(String symbolName) throws Exception {

        IScriptable2 result = null;

        if(actionMap != null && ((result = (type_action) actionMap.get(symbolName)) == null)) {

            if(moduleMap != null && ((result = (scene_module) moduleMap.get(symbolName)) == null)) {

                if((choiceMap != null) && ((result = (type_choiceset) choiceMap.get(symbolName)) == null)) {

                    if (constraintMap != null)
                        result = (type_cond) constraintMap.get(symbolName);
                }
            }
        }

        if(result == null)
            throw(new Exception("Local Symbol not found: " + symbolName));

        return result;
    }
}
