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

package cmu.xprize.robotutor.tutorengine;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_animator;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;

public class CTutorAnimator {

    private TScope           mScope;

    private String           mTutorName;
    private String           mSceneName;

    // State fields
    private scene_animator _currAnimator;

    static private HashMap<String, Integer> _pFeatures;


    // json loadable
    public HashMap<String,scene_animator> animatorMap;


    final private String TAG = "CTutorAnimator";


    public CTutorAnimator(String tutorName, TScope tutorScope) {

        mTutorName = tutorName;
        mScope     = tutorScope;
        _pFeatures = new HashMap<String, Integer>();

        loadAnimatorFactory(mScope);
    }


    public void enterScene(String sceneName) {

        mSceneName = sceneName;

        try {
            _currAnimator = (scene_animator)mScope.mapSymbol(mSceneName);

        } catch (Exception e) {
            e.printStackTrace();
        }

        _currAnimator.seekRoot();
    }


    public void play() {
        if(_currAnimator != null)
                _currAnimator.play();
    }


    public void stop() {
        if(_currAnimator != null)
            _currAnimator.stop();
    }


    public String applyNode() {

        return _currAnimator.applyNode();
    }

    public String gotoNode(String nodeName) {

        return _currAnimator.gotoNode(nodeName);
    }


    static public int queryPFeature(String pid, int size, int cycle) {
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


    //************ Serialization


    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/tutor_descriptor.json
     *
     */
    private void loadAnimatorFactory(TScope scope) {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.AGDESC)), scope);
        } catch (JSONException e) {
            Log.e(TAG, "JSON FORMAT ERROR: " + TCONST.AGDESC + " : " + e);
            System.exit(1);
        }
    }

    public void loadJSON(JSONObject jsonObj, TScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, scope);

    }


}
