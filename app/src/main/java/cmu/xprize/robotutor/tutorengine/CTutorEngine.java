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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;
import cmu.xprize.robotutor.RoboTutor;

/**
 * The tutor engine is a singleton
 *
 */
public class CTutorEngine {

    private static TScope                   mRootScope;

    private static CTutorEngine             mTutorEngine;
    private static HashMap<String, CTutor>  mTutors = new HashMap<String, CTutor>();
    private static CTutor                   mTutorActive;
    private static String                   mTutorName;

    private String                          mTutorDescrPath;
    private String                          mJSONspec;

    static public RoboTutor                 Activity;
    static public ITutorSceneImpl           TutorContainer;
    static public ITutorLogManager          TutorLogManager;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // json loadable
    public String                           defTutor;
    static public String                    language;


    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern
    final static private String TAG         = "CTUTOR_ENGINE";



    private CTutorEngine(RoboTutor context, ITutorSceneImpl tutorContainer) {

        mRootScope      = new TScope("root", null);

        Activity        = context;
        TutorContainer  = tutorContainer;
        TutorLogManager = new CTutorLogManager();
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

        loadJSON(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.EDESC), mRootScope);
    }


    /**
     * Load the Tutor specification from JSON file data
     *
     * @param jsonData
     */
    public void loadJSON(String jsonData, TScope scope) {

        try {
            JSON_Helper.parseSelf(new JSONObject(jsonData), this, scope);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
