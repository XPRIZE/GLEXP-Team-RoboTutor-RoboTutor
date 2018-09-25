package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 */

public class TransitionMatrixModel implements ILoadableObject {

    private static final String TAG = "TransitionMatrixModel";
    // json loadable
    public String       rootSkillWrite;
    public String       rootSkillStories;
    public String       rootSkillMath;
    private HashMap<String, String>     contentAreaRootSkills;

    public HashMap      writeTransitions;
    public HashMap      storyTransitions;
    public HashMap      mathTransitions;
    private HashMap<String, HashMap>     contentAreaTransitionMaps;

    public CPlacementTest_Tutor[] writePlacement;
    public CPlacementTest_Tutor[]   mathPlacement;

    public TransitionMatrixModel(String datasource, IScope scope) {

        String jsonData = JSON_Helper.cacheData(datasource);

        try {
            loadJSON(new JSONObject(jsonData), scope);
        } catch (JSONException e) {
            e.printStackTrace();
            CErrorManager.logEvent(TAG, "Bad data source  " + datasource, true);
        }

    }

    public String getRootSkillByContentArea(String contentArea) {
        return contentAreaRootSkills.get(contentArea);
    }

    public HashMap getTransitionMapByContentArea(String contentArea) {
        return contentAreaTransitionMaps.get(contentArea);
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

        contentAreaTransitionMaps = new HashMap<>();
        contentAreaTransitionMaps.put(SELECT_WRITING, writeTransitions);
        contentAreaTransitionMaps.put(SELECT_STORIES, storyTransitions);
        contentAreaTransitionMaps.put(SELECT_MATH, mathTransitions);

        contentAreaRootSkills = new HashMap<>();
        contentAreaRootSkills.put(SELECT_WRITING, rootSkillWrite);
        contentAreaRootSkills.put(SELECT_STORIES, rootSkillStories);
        contentAreaRootSkills.put(SELECT_MATH, rootSkillMath);

    }

    /**
     * TODO this should be done in a test case, jeez
     */
    public void validateAll() {
        validateRootVectors();

        // validate tables
        validateTable(writeTransitions,  "writeTransition: ");
        validateTable(storyTransitions,  "storyTransition: ");
        validateTable(mathTransitions ,  "mathTransition: ");

//        validatePlacementProgression(writePlacement, writeTransitions);
//        validatePlacementProgression(mathPlacement, mathTransitions);
    }


    private String validateMap(HashMap map, String key) {

        String result = "";

        if(!map.containsKey(key)) {

            result = "\'" + key + "\'";
        }

        return result;
    }

    /** checks whether each of the root skills exists in the transitions map **/
    private void validateRootVectors() {

        String outcome;

        outcome = validateMap(writeTransitions, rootSkillWrite  );
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillWrite : nomatch");
        }

        outcome = validateMap(storyTransitions, rootSkillStories);
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillStories : nomatch");
        }

        outcome = validateMap(mathTransitions,  rootSkillMath   );
        if(!outcome.equals("")) {

            Log.e(TAG, "Invalid - rootSkillMath : nomatch");
        }

    }


    private void validateTable(HashMap transMap, String transtype) {

        String outcome = "";

        Iterator<?> tObjects = transMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            CAt_Data transition = ((CAt_Data)(entry.getValue()));

            // Validate there are transition entries for all links
            //
            outcome = "";
            outcome = validateVectors(transMap, transition);

            if(!outcome.equals("")) {
                Log.e("Map Fault ", transtype + entry.getKey() + " - MISSING LINKS: " + outcome);
            }

            // Validate there is a Tutor Variant defined for the transition vector
            //
            outcome = "";
            outcome = validateVector(CTutorEngine.tutorVariants, transition.tutor_desc, " - tutor_desc:");

            if(!outcome.equals("")) {
                Log.e("Map Fault ", transtype + entry.getKey() + " - MISSING VARIANT: " +  outcome);
            }

        }
    }


    private String validateVector(HashMap map, String key, String field) {

        String outcome;

        outcome = validateMap(map, key);

        if(!outcome.equals("")) {

            outcome = field + outcome;
        }

        return outcome;
    }


    private String validateVectors(HashMap map, CAt_Data object) {

        String result = "";
        String outcome;

        result = result + validateVector(map, object.tutor_id, " - tutor_id:");

        result = result + validateVector(map, object.easier, " - easier:");

        result = result + validateVector(map, object.harder, " - harder:");

        result = result + validateVector(map, object.same, " - same:");

        result = result + validateVector(map, object.next, " - next:");
        return result;
    }


    /**
     * Checks that every tutor in the Placement Progression is a legit tutor in the transition map
     *
     * @param progression
     */
    private void validatePlacementProgression(CPlacementTest_Tutor[] progression, HashMap transitionMap) {
        // YYY TODO

        StringBuilder outcome = new StringBuilder();

        for(int i = 0; i < progression.length; i++) {

            CPlacementTest_Tutor tutor = progression[0];
            String tutorId = tutor.tutor;

            outcome.append(validateMap(transitionMap, tutorId));

        }

        if (!outcome.toString().equals("")) {
            Log.e(PLACEMENT_TAG, "Placement Error: " + outcome);
        }

    }


}
