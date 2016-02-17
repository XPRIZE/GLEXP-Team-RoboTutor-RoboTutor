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

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.scene_initializer;
import cmu.xprize.robotutor.tutorengine.graph.type_action;
import cmu.xprize.robotutor.tutorengine.graph.type_timer;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;


/**
 *  Each Tutor instance is represented by a CTutor
 *
 */
public class CTutor implements ILoadableObject {

    private boolean traceMode = false;

    // This is the root scope in which all top level objects and variables are defined
    // May have child scopes for local variables -

    private static TScope                        mTutorScope;

    private static HashMap<String, ITutorScene>  mScenes  = new HashMap<String, ITutorScene>();
    private static HashMap<String, ITutorObject> mObjects = new HashMap<String, ITutorObject>();

    private static ArrayList<String>            fFeatures = new ArrayList<String>();
    private static ArrayList<String>            fDefaults = new ArrayList<String>();

    static public Context                       mContext;
    static public ITutorLogManager              mTutorLogManager;
    static public ITutorNavigator               mTutorNavigator;
    static public CTutorAnimator                mTutorAnimator;
    static public ITutorSceneImpl               mTutorContainer;

    static public String                        mTutorName;
    static public AssetManager                  mAssetManager;

    static private int _framendx = 0;

    private HashMap<String, scene_initializer>  _sceneMap = new HashMap<String, scene_initializer>();
    static HashMap<String, type_timer>          _timerMap = new HashMap<String, type_timer>();

    // json loadable
    static public scene_initializer[] scenedata;
    static public String              language;
    static public String              engLanguage;
    static public String              navigatorType;

    // This is used to map Language identifiers in tutor_decriptor to audio subdir names
    static public HashMap<String, String> langMap = new HashMap<String, String>();
    static {
        langMap.put("LANG_EN", "audio/en");
        langMap.put("LANG_SW", "audio/sw");
    }

    private static final String  TAG   = LayoutInflater.class.getSimpleName();
    private static final boolean DEBUG = false;



    public CTutor(Context context, String name, ITutorSceneImpl tutorContainer, ITutorLogManager logManager, TScope rootScope, String tarLanguage) {

        mTutorScope      = new TScope(name, rootScope);
        mContext         = context;
        mTutorName       = name;
        mTutorContainer  = tutorContainer;
        mTutorLogManager = logManager;

        mAssetManager    = context.getAssets();

        // Remember what language the engine wnats to use - need to override after TutorDesc
        // has been inflated
        engLanguage = tarLanguage;

        inflateTutor();
    }


    public void inflateTutor() {

        loadTutorFactory();

        // Let the Engine override and tutor setting for language
        if(engLanguage != null)
            language = engLanguage;

        loadSceneNavigator();
    }


    static public ITutorObject getViewById(int findme, ViewGroup container) {
        ITutorObject foundView = null;

        if(container == null)
            container = (ViewGroup)mTutorContainer;

        try {
            for (int i = 0; (foundView == null) && (i < container.getChildCount()); ++i) {

                ITutorObject nextChild = (ITutorObject) container.getChildAt(i);

                if (((View) nextChild).getId() == findme) {
                    foundView = nextChild;
                    break;
                } else {
                    if (nextChild instanceof ViewGroup)
                        foundView = getViewById(findme, (ViewGroup) nextChild);
                }
            }
        }
        catch (Exception e) {
            Log.i(TAG, "View walk error: " + e);
        }
        return foundView;
    }


    static public TScope getScope() {
        return mTutorScope;
    }


    /**
     * This is where the tutor gets kick started
     */
    public void launchTutor() {

        mTutorNavigator.gotoNextScene();
    }


    static public String getLanguage() {

        return langMap.get(language);
    }


    static public String mapLanguage(String _language) {

        return langMap.get(_language);
    }


    // The language ID is also used as a feature to permit conditioning on language
    // within scripts.
    public void setLanguage(String lang) {
        language = lang;
        setAddFeature(lang);
    }


    //*************  Timer Management


    // TODO: Manage name collisions
    static public void createTimer(String id, type_timer timer) {
        _timerMap.put(id, timer);
    }


    static public type_timer removeTimer(String id) {
        return _timerMap.remove(id);
    }


    static public type_timer mapTimer(String id) {
        return _timerMap.get(id);
    }


    static public boolean hasTimer(String id) {
        return _timerMap.containsKey(id);
    }



    // framendx is a simple counter used it uniquely id a scene instance for logging
    //
    static public void incFrameNdx() {
        _framendx++;
    }


    /** Global logging support - each scene instance and subscene animation instance represent
    *                          object instances in the log.
    *                          The frameid is a '.' delimited string representing the:
    *
    *     framendx:graphnode.nodemodule.moduleelement... :animationnode.animationelement...iterationNdx
    *
    * 			Semantics - each ':' represents the root of a new different (sub)graph
    *   e.g.
    *
    * 	  000001:root.start.SstartSplash...:root.Q0A.CSSbSRule1Part1AS...
    */
    private String constructLogName(String attr) {
        String attrName = "L00000";
        String frame;

        frame = Integer.toString(_framendx);

        // Note: name here is the scene name itself which is the context in which we are executing

        //attrName = attrName.slice(0, 6-frame.length) + frame + "_" + name +"_" + attr + "_" + gTutor.gNavigator.iteration.toString();

        //attrName = name +"_" + attr + "_" + gTutor.gNavigator.iteration.toString();

        return attrName;
    }


    static public void add(String Id, ITutorObject obj) {

        mObjects.put(Id, obj);
    }


    static public ITutorObject get(String Id) {

        return mObjects.get(Id);
    }


    static public void addScene(String Id, ITutorScene obj) {

        mScenes.put(Id, obj);
    }


    static public ITutorScene getScene(String Id) {

        return mScenes.get(Id);
    }


    public void clear() {
        if(mScenes != null)
            mScenes.clear();

        if(mObjects != null)
            mObjects.clear();
    }


    public void loadSceneNavigator() {

        switch(navigatorType) {
            case TCONST.SIMPLENAV:
                mTutorNavigator = new CTutorNavigator(this, mTutorName, mTutorScope);
                break;

            case TCONST.GRAPHNAV:
                //mTutorNavigator = new CSceneGraphNavigator(mTutorName);
                break;
        }

        mTutorNavigator.initTutorContainer(mTutorContainer);
        mTutorAnimator = mTutorNavigator.getAnimator();
    }


    // Scene Creation / Destruction
    public View instantiateScene(scene_descriptor scenedata) {

        int i1;
        View tarScene;
        View subScene;

        int id = mContext.getResources().getIdentifier(scenedata.id, "layout", mContext.getPackageName());

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        tarScene = inflater.inflate(id, null );

        if(traceMode) Log.d(TAG, "Creating Scene : " + scenedata.id);

        tarScene.setVisibility(View.VISIBLE);

        mTutorContainer.getOwner().addView(tarScene);

        // Generate the automation hooks
        automateScene((ITutorSceneImpl) tarScene, scenedata);

        // Parse the JSON onCreate spec data
        onCreate(scenedata);

        return (View) tarScene;
    }


    public void automateScene(ITutorSceneImpl tutorContainer, scene_descriptor scenedata) {

        // Propogate to children
        //
        HashMap childMap = new HashMap();

        // Record each SCENE Object
        //
        scenedata.instance = tutorContainer;
        scenedata.children = childMap;

        tutorContainer.setParent(mTutorContainer);
        tutorContainer.setTutor(this);
        tutorContainer.setNavigator(mTutorNavigator);
        tutorContainer.setLogManager(mTutorLogManager);

        mapChildren(tutorContainer, childMap);
    }

    private void mapChildren(ITutorSceneImpl tutorContainer, HashMap childMap) {

        ITutorObject child;

        int count = ((ViewGroup) tutorContainer).getChildCount();

        // Iterate through all children
        for (int i = 0; i < count; i++) {
            try {
                child = (ITutorObject) ((ViewGroup) tutorContainer).getChildAt(i);

                childMap.put(child.name(), child);

                child.setParent(tutorContainer);
                child.setTutor(this);
                child.setNavigator(mTutorNavigator);
                child.setLogManager(mTutorLogManager);

                if(child instanceof ITutorSceneImpl) {
                    mapChildren((ITutorSceneImpl)child, childMap);
                }

            } catch (ClassCastException e) {
                Log.e(TAG, "ERROR: Non-ITutor child view in:" + tutorContainer.name());
                System.exit(1);
            }
        }
    }


    private void onCreate(scene_descriptor scenedata) {

        // Parse the oncreate command set

        type_action[] createCmds    = _sceneMap.get(scenedata.id).oncreate;

        for(type_action cmd : createCmds) {
            cmd.applyNode();
        }
    }


    // generate the working feature set for this instance
    //
    public void setTutorFeatures(String featSet) {
        List<String> featArray = new ArrayList<String>();

        if(featSet.length() > 0)
            featArray = Arrays.asList(featSet.split(":"));

        fFeatures = new ArrayList<String>();

        // Add default features 

        for (String feature : fDefaults)
        {
            fFeatures.add(feature);
        }

        // Add instance feature

        for (String feature : featArray) {
            fFeatures.add(feature);
        }
    }


    // get : delimited string of features
    //## Mod Oct 16 2012 - logging support
    //
    public String getFeatures() {
        StringBuilder builder = new StringBuilder();

        for(String feature: fFeatures) {
            builder.append(feature).append(':');
        }
        builder.deleteCharAt(builder.length());

        return builder.toString();
    }


    // set : delimited string of features
    //## Mod Dec 03 2013 - DB state support
    //
    public void setFeatures(String ftrSet) {
        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(ftrSet.split(","));

        fFeatures.clear();
        
        for (String feature : featArray) {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    static public void setAddFeature(String feature)
    {
        // Add new features - no duplicates

        if(fFeatures.indexOf(feature) == -1)
        {
            fFeatures.add(feature);
        }
    }


    // udpate the working feature set for this instance
    //
    static public void setDelFeature(String feature) {
        int fIndex;

        // remove features - no duplicates

        if((fIndex = fFeatures.indexOf(feature)) != -1)
        {
            fFeatures.remove(fIndex);
        }
    }


    //## Mod Jul 01 2012 - Support for NOT operation on features.
    //
    //	
    static private boolean testFeature(String element)
    {
        if(element.charAt(0) == '!')
        {
            return (fFeatures.indexOf(element.substring(1)) != -1)? false:true;
        }
        else {
            return (fFeatures.indexOf(element) != -1) ? true : false;
        }
    }


    // test possibly compound features
    //
    static public boolean testFeatureSet(String featSet) {
        List<String> disjFeat = Arrays.asList(featSet.split("\\|"));   // | Disjunctive features
        List<String> conjFeat;                                          // & Conjunctive features

        // match a null set - i.e. empty string means the object is not feature constrained

        if(featSet.equals(""))
                    return true;

        // Check all disjunctive featuresets - one in each element of disjFeat
        // As long as one is true we pass

        for (String dfeature : disjFeat)
        {
            conjFeat = Arrays.asList(dfeature.split("\\&"));

            // Check that all conjunctive features are set in fFeatures 

            for (String cfeature : conjFeat) {
                if(testFeature(cfeature))
                                return true;
            }
        }
        return false;
    }


    static public String getTutorName() {
        return mTutorName;
    }


    static public AssetManager getAssetManager() {
        return mAssetManager;
    }


    static public void gotoNode(String nodeID) {
        mTutorAnimator.gotoNode(nodeID);
    }



    //************ Serialization


    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/tutor_descriptor.json
     *
     * Note that this is a stopgap until we can replace the Android view inflation mechanism
     * and completely define view layout in TDESC
     */
    private void loadTutorFactory() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.TDESC)), mTutorScope);

        } catch (JSONException e) {
            Log.d(TAG, "error");
        }
    }


    public void loadJSON(JSONObject jsonObj, TScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, scope);

        // Use setLanguage to properly configure the language feature
        if(language != null)
            setLanguage(language);

        // Create a associative cache for the initialization data
        //
        for(scene_initializer scene : scenedata) {
            _sceneMap.put(scene.id, scene);
        }
    }

}
