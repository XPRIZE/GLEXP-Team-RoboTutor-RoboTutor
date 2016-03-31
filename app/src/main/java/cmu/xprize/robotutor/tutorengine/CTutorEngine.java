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
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
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
    static public ITutorManager             TutorContainer;
    static public ITutorLogManager          TutorLogManager;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // json loadable
    public String                           defTutor;
    static public String                    language;

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern
    final static private String TAG         = "CTutorEngine";


    /**
     * TutorEngine is a Singleton
     *
     * @param context
     * @param tutorContainer
     */
    private CTutorEngine(RoboTutor context, ITutorManager tutorContainer) {

        mRootScope      = new TScope(null, "root", null);

        Activity        = context;
        TutorContainer  = tutorContainer;
        TutorLogManager = new CTutorLogManager();

        // Initialize the JSON Helper statics - just throw away the object.
        new JSON_Helper(Activity.getAssets(), CacheSource, RoboTutor.EXTERNFILES);

        initialize();
    }


    private void initialize() {

        loadEngineDescr();
        addTutor(defTutor);

        mTutorActive.launchTutor();
    }


    /**
     * Retrieve the one and only tutorEngine object
     *
     * @param context
     * @param tutorContainer
     * @return
     */
    static public CTutorEngine getTutorEngine(RoboTutor context, ITutorManager tutorContainer) {

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


    private void addTutor(String tutorName) {

        mTutorActive = new CTutor(Activity, tutorName, TutorContainer, TutorLogManager, mRootScope, language);

    }


    // Scriptable Launch command
    //
    static public void launch(String intent, String intentData) {

        Intent extIntent = new Intent();
        String extPackage;

        switch(intentData) {
            case "native":
                mTutorActive = new CTutor(Activity, intent, TutorContainer, TutorLogManager, mRootScope, language);

                mTutorActive.launchTutor();
                break;

            case "browser":

                extIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + intent));

                getActivity().startActivity(extIntent);

                break;

            default:

                // This a special allowance for MARi which placed there activities in a different
                // package from there app - so we check for intent of the form "<pkgPath>:<appPath>"
                //
                String[] intParts = intent.split(":");

                // If it is "<pkgPath>:<appPath>"
                //
                if(intParts.length > 1) {
                    extPackage = intParts[0];
                    intent     = intParts[1];
                }
                // Otherwise we expect the activities to be right off the package.
                //
                else {
                    extPackage = intent.substring(0, intent.lastIndexOf('.'));
                }

                extIntent.setClassName(extPackage, intent);
                extIntent.putExtra("intentdata", intentData);

                try {
                    getActivity().startActivity(extIntent);
                }
                catch(Exception e) {
                    Log.e(TAG, "Launch Error: " + e + " : " + intent);
                }
                break;
        }
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
