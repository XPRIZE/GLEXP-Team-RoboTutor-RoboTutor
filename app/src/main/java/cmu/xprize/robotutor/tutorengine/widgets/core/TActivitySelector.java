package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.comp_session.CActivitySelector;
import cmu.xprize.comp_session.CAt_Data;
import cmu.xprize.robotutor.BuildConfig;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.HAS_ORPHAN_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CClassMap2.classMap;

public class TActivitySelector extends CActivitySelector implements IBehaviorManager, ITutorSceneImpl, IDataSink, IEventSource {

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;
    private CMediaManager           mMediaManager;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap   = new HashMap<>();

    private String rootSkillWrite   = null;
    private String rootSkillRead    = null;
    private String rootSkillMath    = null;
    private String rootSkillShapes  = null;

    private ArrayList<String>       _FeatureSet = new ArrayList<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();


    final private String  TAG = "TActivitySelector";



    public TActivitySelector(Context context) {
        super(context);
    }

    public TActivitySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TActivitySelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        SaskActivity = (TAskComponent)findViewById(R.id.SaskActivity);

        SaskActivity.setButtonController(this);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        mTutorScene.onDestroy();
    }

    @Override
    public void setVisibility(String visible) {

    }


    public void setLayout(String name) {

        for(CAsk_Data layout : dataSource) {

            if(layout.name.equals(name)) {

                _activeLayout = layout;

                SaskActivity.setDataSource(layout);
                break;
            }
        }

    }


    public void rippleDescribe() {
        _describeIndex = 0;

        describeNext();
    }

    public void describeNext() {

        if(_describeIndex < _activeLayout.items.length) {

            publishValue(AS_CONST.VAR_BUTTONID,     _activeLayout.items[_describeIndex].componentID);
            publishValue(AS_CONST.VAR_BUT_BEHAVIOR, _activeLayout.items[_describeIndex].behavior);
            publishValue(AS_CONST.VAR_HELP_AUDIO,   _activeLayout.items[_describeIndex].help);
            publishValue(AS_CONST.VAR_PROMPT_AUDIO, _activeLayout.items[_describeIndex].prompt);

            applyBehavior(AS_CONST.DESCRIBE_BEHAVIOR);

            _describeIndex++;
        }
        else {

            applyBehavior(AS_CONST.DESCRIBE_COMPLETE);
        }

    }


    @Override
    public void doButtonAction(String actionid) {

        applyBehavior(AS_CONST.SELECT_BEHAVIOR);

        for(CAskElement element : _activeLayout.items) {

            if(element.componentID.equals(actionid)) {

                publishValue(AS_CONST.VAR_BUTTONID,     element.componentID);
                publishValue(AS_CONST.VAR_BUT_BEHAVIOR, element.behavior);
                publishValue(AS_CONST.VAR_HELP_AUDIO,   element.help);
                publishValue(AS_CONST.VAR_PROMPT_AUDIO, element.prompt);

                applyBehavior(element.behavior);
            }
        }
    }


    @Override
    public void doButtonBehavior(String buttonid) {

        Log.d(TAG, "Button Selected: " + buttonid);

        switch(buttonid.toUpperCase()) {
            case AS_CONST.SELECT_WRITING:
                doLaunch(letters[0].intent, letters[0].intentdata, letters[0].datasource, letters[0].features);
                break;

            case AS_CONST.SELECT_STORIES:

                // Special Flavor processing to exclude ASR apps - this was a constraint for BETA trials
                // reenable the ASK buttons if we don't execute the story_tutor
                //
                if(!BuildConfig.NO_ASR_APPS)
                    doLaunch(stories[0].intent, stories[0].intentdata, stories[0].datasource, stories[0].features);
                else
                    SaskActivity.enableButtons(true);
                break;

            case AS_CONST.SELECT_MATH:
                doLaunch(numbers[0].intent, numbers[0].intentdata, numbers[0].datasource, numbers[0].features);
                break;

            case AS_CONST.SELECT_SHAPES:
                doLaunch(shapes[0].intent, shapes[0].intentdata, shapes[0].datasource, shapes[0].features);
                break;

            case AS_CONST.SELECT_ROBOTUTOR:
                doLaunch(letters[0].intent, letters[0].intentdata, letters[0].datasource, letters[0].features);
                break;


            //  Difficulty selection


            case AS_CONST.SELECT_CONTINUE:
                break;

            case AS_CONST.SELECT_MAKE_HARDER:
                break;

            case AS_CONST.SELECT_MAKE_EASIER:
                break;

            case AS_CONST.SELECT_AUTO_DIFFICULTY:
                break;

            case AS_CONST.SELECT_REPEAT:
                break;

            case AS_CONST.SELECT_EXIT:
                RoboTutor.TUTORSELECTED = false;

                mTutor.post(TCONST.ENDTUTOR);
                break;



        }

    }


    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    @Override
    public void doLaunch(String intent, String intentData, String dataSource, String features) {

        RoboTutor.TUTORSELECTED = true;

        // update the response variable  "<Sresponse>.value"

        mTutor.getScope().addUpdateVar(name() + ".intent", new TString(intent));
        mTutor.getScope().addUpdateVar(name() + ".intentData", new TString(intentData));
        mTutor.getScope().addUpdateVar(name() + ".dataSource", new TString(dataSource));
        mTutor.getScope().addUpdateVar(name() + ".features", new TString(features));

        applyBehavior(AS_CONST.LAUNCH_EVENT);
    }


    //************************************************************************
    //************************************************************************
    // ITutorObject Implementation Start

    @Override
    public void setName(String name) {
        mTutorScene.setName(name);
    }

    @Override
    public String name() {
        return mTutorScene.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mTutorScene.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {

        mTutor = tutor;
        mTutorScene.setTutor(tutor);

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor);
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mTutorScene.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mTutorScene.setLogManager(logManager);
    }


    @Override
    public CSceneDelegate getimpl() {
        return mTutorScene;
    }


    @Override
    public ViewGroup getOwner() {
        return mTutorScene.getOwner();
    }

    @Override
    public String preEnterScene(scene_descriptor scene, String Direction) {
        return mTutorScene.preEnterScene(scene, Direction);
    }

    @Override
    public void onEnterScene() {
        mTutorScene.onEnterScene();
    }

    @Override
    public String preExitScene(String Direction, int sceneCurr) {
        return mTutorScene.preExitScene(Direction, sceneCurr);
    }

    @Override
    public void onExitScene() {
        mTutorScene.onExitScene();
    }

    // ITutorObject Implementation End
    //************************************************************************
    //************************************************************************





    //************************************************************************
    //************************************************************************
    // DataSink Implementation Start


    @Override
    public void setDataSource(String dataNameDescriptor) {

        // TODO: globally make startWith type TCONST
        try {
            if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.SOURCEFILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS;
                dataPath += "/" +  TCONST.DATA_PATH + "/" + mMediaManager.getLanguageIANA_2(mTutor) + "/";

                String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source - " + dataNameDescriptor + " for : " + name() + " : ", e, false);
        }
    }

    // DataSink Implementation END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START

    public void setVolatileBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (volatileMap.containsKey(event)) {
                volatileMap.remove(event);
            }
        } else {
            volatileMap.put(event, behavior);
        }
    }


    public void setStickyBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (stickyMap.containsKey(event)) {
                stickyMap.remove(event);
            }
        } else {
            stickyMap.put(event, behavior);
        }
    }


    // Execute scirpt target if behavior is defined for this event
    //
    public boolean applyBehavior(String event) {

        boolean result = false;

        if (volatileMap.containsKey(event)) {
            Log.d(TAG, "Processing BP_ApplyEvent: " + event);
            applyBehaviorNode(volatileMap.get(event));

            volatileMap.remove(event);

            result = true;

        } else if (stickyMap.containsKey(event)) {
            applyBehaviorNode(stickyMap.get(event));

            result = true;
        }

        return result;
    }


    /**
     * Apply Events in the Tutor Domain.
     *
     * @param nodeName
     */
    @Override
    public void applyBehaviorNode(String nodeName) {
        IScriptable2 obj = null;

        if (nodeName != null && !nodeName.equals("") && !nodeName.toUpperCase().equals("NULL")) {

            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if (obj != null) {

                    switch(obj.getType()) {

                        case TCONST.SUBGRAPH:

                            mTutor.getSceneGraph().post(this, TCONST.SUBGRAPH_CALL, nodeName);
                            break;

                        case TCONST.MODULE:

                            // Disallow module "calls"
                            Log.e(TAG, "MODULE Behaviors are not supported");
                            break;

                        default:

                            obj.preEnter();
                            obj.applyNode();
                            break;
                    }
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }


    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IEventSource Interface START


    @Override
    public String getEventSourceName() {
        return name();
    }

    @Override
    public String getEventSourceType() {
        return "Activity_Selector";
    }


    // IEventSource Interface END
    //************************************************************************
    //************************************************************************

    //************************************************************************
    //************************************************************************
    // publish component state data - START

    @Override
    public void publishState() {
    }

    @Override
    public void publishValue(String varName, String value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));

    }

    @Override
    public void publishValue(String varName, int value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TInteger(value));

    }

    @Override
    public void publishFeature(String feature) {

        trackFeatures(feature);

        _FeatureMap.put(feature, true);
        mTutor.setAddFeature(feature);
    }

    /**
     * Note that we may retract features before they're published to add them to the
     * FeatureSet that should be pushed/popped when using pushDataSource
     * e.g. we want EOD to track even if it has never been set
     *
     * @param feature
     */
    @Override
    public void retractFeature(String feature) {

        trackFeatures(feature);

        _FeatureMap.put(feature, false);
        mTutor.setDelFeature(feature);
    }

    /**
     * _FeatureSet keeps track of used features
     *
     * @param feature
     */
    private void trackFeatures(String feature) {

        if(_FeatureSet.indexOf(feature) == -1)
        {
            _FeatureSet.add(feature);
        }
    }

    // publish component state data - EBD
    //************************************************************************
    //************************************************************************





    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        Log.d(TAG, "Loader iteration");

        // Note we load in the TClass as we need to use the tutor classMap to permit
        // instantiation of type_audio objects
        //
        JSON_Helper.parseSelf(jsonObj, this, classMap, scope);

        for(CAt_Data transition : transitions) {

            switch(transition.skill) {
                case TCONST.SKILL_WRITING:
                    if(rootSkillWrite == null) {
                        rootSkillWrite = transition.tutor_id;
                    }

                    if(!writeMap.containsKey(transition.tutor_id)) {
                        writeMap.put(transition.tutor_id, transition);
                    }
                    else {
                        Log.d(TAG, "Skill Transitions: " + transition.skill + " - Duplicate key: " + transition.tutor_id);
                    }
                    break;

                case TCONST.SKILL_READING:
                    if(rootSkillRead == null) {
                        rootSkillRead = transition.tutor_id;
                    }

                    if(!readMap.containsKey(transition.tutor_id)) {
                        readMap.put(transition.tutor_id, transition);
                    }
                    else {
                        Log.d(TAG, "Skill Transitions: " + transition.skill + " - Duplicate key: " + transition.tutor_id);
                    }
                    break;

                case TCONST.SKILL_MATH:
                    if(rootSkillMath == null) {
                        rootSkillMath = transition.tutor_id;
                    }

                    if(!mathMap.containsKey(transition.tutor_id)) {
                        mathMap.put(transition.tutor_id, transition);
                    }
                    else {
                        Log.d(TAG, "Skill Transitions: " + transition.skill + " - Duplicate key: " + transition.tutor_id);
                    }
                    break;

                case TCONST.SKILL_SHAPES:
                    if(rootSkillShapes == null) {
                        rootSkillShapes = transition.tutor_id;
                    }

                    if(!shapeMap.containsKey(transition.tutor_id)) {
                        shapeMap.put(transition.tutor_id, transition);
                    }
                    else {
                        Log.d(TAG, "Skill Transitions: " + transition.skill + " - Duplicate key: " + transition.tutor_id);
                    }
                    break;
            }
        }

        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String writingLevelVector = prefs.getString(TCONST.SKILL_WRITING, rootSkillWrite);
        String readingLevelVector = prefs.getString(TCONST.SKILL_READING, rootSkillRead);
        String mathLevelVector    = prefs.getString(TCONST.SKILL_MATH, rootSkillMath);
        String shapeLevelVector   = prefs.getString(TCONST.SKILL_SHAPES, rootSkillShapes);


//            editor.putInt(assetName + TCONST.ASSET_UPDATE_VERSION , mAssetObject.getVersionField(INDEX_UPDATE, TCONST.ASSET_UPDATE_VERSION));
//            editor.apply();
    }

}
