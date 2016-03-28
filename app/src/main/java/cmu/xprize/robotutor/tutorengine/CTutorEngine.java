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

import android.app.Activity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.RoboTutor;

/**
 * The tutor engine is a singleton
 *
 */
public class CTutorEngine implements ILoadableObject2 {

    private static TScope                   mRootScope;

    private static CTutorEngine             mTutorEngine;
    private static HashMap<String, CTutor>  mTutors = new HashMap<String, CTutor>();
    private static CTutor                   mTutorActive;
    private static String                   mTutorName;

    private String                          mTutorDescrPath;
    private String                          mJSONspec;

    static public RoboTutor Activity;
    static public ITutorSceneImpl           TutorContainer;
    static public ITutorLogManager          TutorLogManager;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // json loadable
    public String                           defTutor;
    static public String                    language;

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern
    final static private String TAG         = "CTutorEngine";



    private CTutorEngine(RoboTutor context, ITutorSceneImpl tutorContainer) {

        mRootScope      = new TScope(null, "root", null);

        Activity        = context;
        TutorContainer  = tutorContainer;
        TutorLogManager = new CTutorLogManager();

        // Initialize the JSON Helper statics - just throw away the object.
        new JSON_Helper(Activity.getAssets(), CacheSource, RoboTutor.EXTERNFILES);
    }


    static public CTutorEngine getTutorEngine(RoboTutor context, ITutorSceneImpl tutorContainer) {

        if(mTutorEngine == null) {
            mTutorEngine = new CTutorEngine(context, tutorContainer);
        }

        return mTutorEngine;
    }


    static public TScope getScope() {

        return mRootScope;
    }


    static public Activity getActivity() {
        return Activity;
    }


    static public void add(String Id, ITutorObject obj) {

        mTutorActive.add(Id, obj);
    }


    static public ITutorObject get(String Id) {

        return mTutorActive.get(Id);
    }


    public void clear() {
        mTutorActive.clear();
    }


    public void initialize() {

        loadEngineDescr();
        loadDefTutor();

        mTutorActive.launchTutor();
    }


    private void loadDefTutor() {

        mTutorActive = new CTutor(Activity, defTutor, TutorContainer, TutorLogManager, mRootScope, language);

    }


    //************ Serialization


    /**
     * Load the Tutor engine specification from JSON file data
     * from assets/tutors/engine_descriptor.json
     *
     */
    public void loadEngineDescr() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.EDESC)), (IScope2)mRootScope);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Load the Tutor specification from JSON file data
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope2 scope) {

      JSON_Helper.parseSelf(jsonData, this, CClassMap2.classMap, scope);
    }
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
