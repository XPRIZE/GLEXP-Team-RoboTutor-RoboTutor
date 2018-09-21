package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 */

public class PromotionMechanism {

    private StudentDataModel _studentModel; // holds the StudentDataModel
    private TransitionMatrixModel _matrix; // now holds the transition map things...

    public PromotionMechanism(StudentDataModel studentModel, TransitionMatrixModel matrix) {
        this._studentModel = studentModel;
        this._matrix = matrix;
    }


    private static final String TAG = "PromotionMechanism";


    /**
     *
     * Adjust the student's position in _matrix based on their last performance
     *
     */
    public void adjustPositionFromPreviousPerformance(CTutor tutor) {

        boolean useMathPlacement = false;
        boolean useWritingPlacement = false;

        //
        String activeTutorId = "";
        HashMap transitionMap = null;
        // look up activeSkill every time?
        String activeSkill = _studentModel.getActiveSkill();
        int placementIndex = 0;
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                activeTutorId = _studentModel.getWritingTutorID();
                transitionMap = _matrix.writeTransitions;
                useWritingPlacement = _studentModel.getWritingPlacement();
                placementIndex = _studentModel.getWritingPlacementIndex();

                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                activeTutorId = _studentModel.getStoryTutorID();
                transitionMap = _matrix.storyTransitions;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                activeTutorId = _studentModel.getMathTutorID();
                transitionMap = _matrix.mathTransitions;
                useMathPlacement = _studentModel.getMathPlacement();
                placementIndex = _studentModel.getMathPlacementIndex();
                break;

        }

        // this should be broken up appropriately...
        String nextTutor = selectNextTutor(tutor, activeTutorId, activeSkill, useWritingPlacement, useMathPlacement, transitionMap, placementIndex); // √

        RoboTutor.logManager.postEvent_I(TCONST.PLACEMENT_TAG, "nextTutor = " + nextTutor);


        // 3. Set SELECTOR_MODE
        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

        // Update the active skill
        //
        // look up activeSkill every time?
        String writingTutorID = null, storiesTutorID = null, mathTutorID = null;
        switch (activeSkill) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                writingTutorID = nextTutor;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                storiesTutorID = nextTutor;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                mathTutorID = nextTutor;
                break;
        }

        // Serialize the new state
        // #Mod 329 language switch capability
        //


        //editor.putString(TCONST.SKILL_SELECTED, AS_CONST.SELECT_NONE); // √√√ √√√
        //Log.wtf("REPEAT_STUFF", "(difficultySelectMode) setting SKILL_SELECTED... " + AS_CONST.SELECT_NONE);

        // only one will have been changed but update all
        //
        if (writingTutorID != null) _studentModel.updateWritingTutorID(writingTutorID);
        if (storiesTutorID != null) _studentModel.updateStoryTutorID(storiesTutorID);
        if (mathTutorID != null) _studentModel.updateMathTutorID(mathTutorID);

        _studentModel.updateLastTutor(activeTutorId);

        RoboTutor.MUST_CALCULATE_NEXT_TUTOR = false;
    }


    /**
     * select Next Tutor
     */
    private String selectNextTutor(CTutor lastTutor, String activeTutorId, String activeSkill, boolean useWritingPlacement, boolean useMathPlacement, HashMap transitionMap, int placementIndex) {
        RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format(Locale.US, "selectNextTutor, w=%s, m=%s",
                String.valueOf(useWritingPlacement),
                String.valueOf(useMathPlacement)));
        // 1. pick the next tutor
        // YYY if placement, we will go by different rules
        PromotionRules rules;
        if(useWritingPlacement || useMathPlacement) {
            rules = new PlacementPromotionRules();
        } else {
            rules = new PerformancePromotionRules();
        }

        PerformanceData performance = new PerformanceData();
        performance.setActivityType(activeTutorId);
        // look up activeSkill every time?
        performance.setActiveSkill(activeSkill);


        // can this work from last tutor???
        performance.setNumberCorrect(lastTutor.getScore());
        performance.setNumberWrong(lastTutor.getIncorrect());
        performance.setNumberAttempts(lastTutor.getAttempts());
        performance.setTotalNumberQuestions(lastTutor.getTotalQuestions());


        PromotionRules.SelectedActivity selectedActivity = rules.selectActivityByPerformance(performance);
        Log.d(TAG, "PerformancePromotionRules result: " + selectedActivity);

        // YYY use placement logic
        String nextTutor;
        if (useWritingPlacement || useMathPlacement) {
            nextTutor = getNextPlacementTutor(activeTutorId, useMathPlacement, selectedActivity, transitionMap, placementIndex);

        } else {
            nextTutor = getNextPromotionTutor(activeTutorId, selectedActivity, transitionMap);
        }
        return nextTutor;
    }

    /**
     * get next tutor using Promotion Logic
     * @param selectedActivity
     * @return
     */
    private String getNextPromotionTutor(String activeTutorId, PromotionRules.SelectedActivity selectedActivity, HashMap<String, CAt_Data> transitionMap) {
        // this is
        CAt_Data transitionData = transitionMap.get(activeTutorId);
        switch (selectedActivity) {
            case NEXT:
                return transitionData.next;

            case SAME:
                return transitionData.same;

            case OLD_EASIER:
                return transitionData.easier;

            case OLD_HARDER:
                return transitionData.harder;

            case PREVIOUS:
                // XXX FIXME nextTutor = transitionData.previous;
                // for now... do the super hacky way of iterating through the whole map until we find one who refers to "activeTutorId" via "next"
                String tempNextTutor = null;
                for (Map.Entry<String, CAt_Data> e : transitionMap.entrySet()) {
                    Log.d("TRANSITION_MAP", e.getValue().toString());
                    CAt_Data value = e.getValue();
                    if (value.next.equals(activeTutorId)) {
                        tempNextTutor = e.getKey();
                    }
                }
                // no "next" reference, probably means it's the first item
                if (tempNextTutor == null) {
                    tempNextTutor = activeTutorId;
                }
                return tempNextTutor;
            // XXX FIXME end super hacky code


            case DOUBLE_NEXT:
                // XXX FIXME nextTutor = transitionData.double_next;
                // for now... do the slightly less hacky way of doing "next" of "next"
                String notNextTutor = transitionData.next;

                CAt_Data nextTransitionData = transitionMap.get(notNextTutor);
                return nextTransitionData.next;
            // XXX FIXME end slightly less hacky code
            // XXX note that these will not show up in the debugger graph

            // this shouldn't happen...
            default:
                return transitionData.next;
        }
    }

    /**
     * get next tutor using Placement Logic
     */
    private String getNextPlacementTutor(String activeTutorId, boolean useMathPlacement, PromotionRules.SelectedActivity selectedActivity, HashMap<String, CAt_Data> transitionMap, int placementIndex) {
        RoboTutor.logManager.postEvent_V(TCONST.PLACEMENT_TAG, "using placement logic");

        String placementKey;
        String placementIndexKey;

        String nextTutor;

        switch(selectedActivity) {

            /// YYY it might be better to keep the placement tutors in a map instead of in an array
            /// YYY logic might be more symmetrical
            /// YYY if (placement) {placementMap.get(activeTutorId)} else {transitionMap.get(activeTutorId)}
            // NEXT is equivalent to passing the placement test and moving to next placement test

            case NEXT:
                // pass to next
                //
                if(useMathPlacement) {
                    int mathPlacementIndex = placementIndex;


                    if (mathPlacementIndex == _matrix.mathPlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = _matrix.mathPlacement[mathPlacementIndex];
                        // update our preferences to exit PLACEMENT mode
                        _studentModel.updateMathPlacement(false);
                        _studentModel.updateMathPlacementIndex(null);


                        return lastPlacementTest.fail;
                    } else {
                        mathPlacementIndex++; // passing means incrementing by one
                        CPlacementTest_Tutor nextPlacementTest = _matrix.mathPlacement[mathPlacementIndex];

                        _studentModel.updateMathPlacementIndex(mathPlacementIndex);

                        return nextPlacementTest.tutor; // go to beginning of last level
                    }
                }
                // writingPlacement is only other option
                else {
                    int writingPlacementIndex = placementIndex;

                    if (writingPlacementIndex == _matrix.writePlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = _matrix.writePlacement[writingPlacementIndex];
                        // update our preferences to exit PLACEMENT mode

                        _studentModel.updateWritingPlacement(false);
                        _studentModel.updateWritingPlacementIndex(null); //editor.remove("WRITING_PLACEMENT_INDEX");


                        return lastPlacementTest.fail; // go to beginning of last level
                    } else {
                        writingPlacementIndex++; // passing means incrementing by one
                        CPlacementTest_Tutor nextPlacementTest = _matrix.writePlacement[writingPlacementIndex];


                        _studentModel.updateWritingPlacementIndex(writingPlacementIndex); //editor.putInt("WRITING_PLACEMENT_INDEX", writingPlacementIndex);


                        return nextPlacementTest.tutor;
                    }

                }


                // SAME occurs when the student doesn't finish and we don't have enough information
            case SAME:
                // play again
                // do nothing
                CAt_Data transitionData = transitionMap.get(activeTutorId);
                return transitionData.same;


            // PREVIOUS is equivalent to failing the placement test and going to first activity in level
            case PREVIOUS:
            default:
                CPlacementTest_Tutor lastPlacementTest;

                // set prefs.usesThingy to false
                if(useMathPlacement) {
                    lastPlacementTest = _matrix.mathPlacement[placementIndex];
                    placementKey = "MATH_PLACEMENT";
                    placementIndexKey = "MATH_PLACEMENT_INDEX";
                    _studentModel.updateMathPlacement(false); // editor.putBoolean(placementKey, false); // no more placement
                    _studentModel.updateMathPlacementIndex(null);

                }
                // useWritePlacement only other option
                else {
                    lastPlacementTest = _matrix.writePlacement[placementIndex];
                    placementKey = "WRITING_PLACEMENT";
                    placementIndexKey = "WRITING_PLACEMENT_INDEX";
                    _studentModel.updateWritingPlacement(false); // editor.putBoolean(placementKey, false); // no more placement
                    _studentModel.updateWritingPlacementIndex(null); // editor.remove(placementIndexKey);
                }

                return lastPlacementTest.fail;


        }
    }


    private String getChildScope(String activeTutorId) {
        String childScope = null;
        if(activeTutorId.startsWith("bpop")) {
            childScope = "bubble_pop";

        } else if (activeTutorId.startsWith("akira")) {
            childScope = "akira";

        } else if (activeTutorId.startsWith("math")) {
            childScope = "add_subtract";

        } else if (activeTutorId.startsWith("write")) {
            childScope = "word_copy";

        } else if (activeTutorId.startsWith("story")) {
            childScope = "story_reading";

        } else if (activeTutorId.startsWith("countingx")) {
            childScope = "countingx";
        } else if (activeTutorId.startsWith("num.scale")) {
            childScope = "numberscale";
        }
        return childScope;
    }
}
