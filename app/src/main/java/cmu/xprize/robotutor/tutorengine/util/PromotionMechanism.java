package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.MENU_BUG_TAG;
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
     * MENU_LOGIC: weird behavior observed... "WRITE" is the only one that doesn't increment on BACKBUTTON. MATH and STORIES both increment
     *
     */
    public void assessPerformanceAndAdjustPosition(CTutor lastTutorPlayed, boolean wasRepeat) {

        String lastSkillPlayed = wasRepeat ? _studentModel.getLastSkill() : _studentModel.getActiveSkill();

        String nextTutor = selectNextTutor(lastTutorPlayed, lastSkillPlayed, wasRepeat);
        RoboTutor.logManager.postEvent_I(TCONST.PLACEMENT_TAG, "nextTutor = " + nextTutor);


        // 3. Set SELECTOR_MODE
        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

        _studentModel.updateLastTutor(lastTutorPlayed.getTutorId());

        // this will only happen if (wasRepeat && placement)
        if (nextTutor == null) {
            return;
        }
        switch (lastSkillPlayed) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:
                _studentModel.updateWritingTutorID(nextTutor);
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:
                _studentModel.updateStoryTutorID(nextTutor);
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:
                _studentModel.updateMathTutorID(nextTutor);
                break;
        }

        // NEW_MENU this should only be incremented in the CYCLE_CONTENT menu type
        if (!wasRepeat) {

            switch (CTutorEngine.menuType) {
                case CYCLE_CONTENT:
                    _studentModel.incrementActiveSkill(); // MENU_LOGIC... activeSkill should be incremented after assessment
                    break;

                case STUDENT_CHOICE:
                    _studentModel.updateActiveSkill(lastSkillPlayed);
                    break;
            }

        }
    }


    /**
     * select Next Tutor
     * MENU_LOGIC note that in some cases, "activeSkill" doesn't match "lastTutor"... (NEXT NEXT NEXT)
     */
    private String selectNextTutor(CTutor lastTutorPlayed, String lastSkillPlayed, boolean wasRepeat) {

        //
        String activeTutorId = "";
        HashMap transitionMap = null;
        String rootTutor = ""; // needed in case our matrix changes for some reason...
        int placementIndex = 0;
        boolean useMathPlacement = false;
        boolean useWritingPlacement = false;

        switch (lastSkillPlayed) { // √

            case AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING:

                activeTutorId = _studentModel.getWritingTutorID(); // TRACE_PROMOTION if we choose second option, this isn't updated
                transitionMap = _matrix.writeTransitions;
                rootTutor = _matrix.rootSkillWrite;
                useWritingPlacement = _studentModel.getWritingPlacement();
                placementIndex = _studentModel.getWritingPlacementIndex();
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES:

                activeTutorId = _studentModel.getStoryTutorID();
                transitionMap = _matrix.storyTransitions;
                rootTutor = _matrix.rootSkillStories;
                break;

            case AS_CONST.BEHAVIOR_KEYS.SELECT_MATH:

                activeTutorId = _studentModel.getMathTutorID();
                transitionMap = _matrix.mathTransitions;
                useMathPlacement = _studentModel.getMathPlacement();
                placementIndex = _studentModel.getMathPlacementIndex();
                rootTutor = _matrix.rootSkillMath;
                break;
        }

        // SUPER_PLACEMENT (***) if the student already did the placement test, then we can just skip the whole process
        // SUPER_PLACEMENT (edge case): if the student backs out on the first try... they won't be assessed again until after they stop repeating
        if (wasRepeat && (useMathPlacement || useWritingPlacement)) return null;

        // TRACE_PROMOTION this might be different...
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, String.format("compare lastTutorPlayed=%s == activeTutorId=%s", lastTutorPlayed.getTutorId(), activeTutorId));
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "selectNextTutor -- activeTutorId=" + activeTutorId+ " -- activeSkill=" + lastSkillPlayed);
        RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format(Locale.US, "selectNextTutor -- w=%s -- m=%s",
                String.valueOf(useWritingPlacement),
                String.valueOf(useMathPlacement)));
        // 1. pick the next tutor
        // YYY if placement, we will go by different rules
        PromotionRules rules;
        if(useWritingPlacement || useMathPlacement) {
            rules = new PlacementPromotionRules(); // SUPER_PLACEMENT
        } else {
            rules = new PerformancePromotionRules();
        }

        PerformanceData performance = new PerformanceData();
        performance.setActivityType(activeTutorId);
        // look up activeSkill every time?
        performance.setActiveSkill(lastSkillPlayed);


        // can this work from last tutor???
        performance.setNumberCorrect(lastTutorPlayed.getScore());
        performance.setNumberWrong(lastTutorPlayed.getIncorrect());
        performance.setNumberAttempts(lastTutorPlayed.getAttempts());
        performance.setTotalNumberQuestions(lastTutorPlayed.getTotalQuestions());


        PromotionRules.PromotionDecision promotionDecision = rules.assessPerformance(performance);
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "PerformancePromotionRules result = " + promotionDecision);

        // YYY use placement logic
        String nextTutor;
        if (useWritingPlacement || useMathPlacement) {
            nextTutor = getNextPlacementTutor(activeTutorId, useMathPlacement, promotionDecision, transitionMap, placementIndex, wasRepeat); // SUPER_PLACEMENT how to know if it was a repeat???

        } else {
            nextTutor = getNextPromotionTutor(activeTutorId, promotionDecision, transitionMap, rootTutor);
            // MENU_LOGIC:::: nextTutor = "story.hear::story_1";
        }
        return nextTutor; // MENU_LOGIC:::: nextTutor = "story.hear::story_1";
    }

    /**
     * get next tutor using Promotion Logic
     * @param activeTutorId the last tutor played
     * @param promotionDecision NEXT, SAME, PREVIOUS, DOUBLE_NEXT...
     * @param transitionMap the transitionMap to get the next tutor from
     * @param rootTutor needed for "just-in-case"
     * @return
     */
    private String getNextPromotionTutor(String activeTutorId, PromotionRules.PromotionDecision promotionDecision, HashMap<String, CAt_Data> transitionMap, String rootTutor) {
        // MENU_SOLUTION... Log.info all of these
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "activeTutorId=" + activeTutorId + " -- ");

        // Prevent NPE by setting as rootTutor.
        CAt_Data transitionData = transitionMap.get(activeTutorId);
        if (transitionData == null) {
            CErrorManager.logEvent(TAG, "ERROR: no entry found for " + activeTutorId + ". Using rootTutor instead.", false);
            transitionData = transitionMap.get(rootTutor);
        }
        switch (promotionDecision) {
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
     * SUPER_PLACEMENT --- if they keep repeating the first placement, it will keep pushing them through. Solution: only increment if this is their first time.
     * // WRITING_PLACEMENT_INDEX=5;
     */
    private String getNextPlacementTutor(String activeTutorId, boolean useMathPlacement, PromotionRules.PromotionDecision promotionDecision, HashMap<String, CAt_Data> transitionMap, int placementIndex, boolean wasRepeat) {
        RoboTutor.logManager.postEvent_V(TCONST.PLACEMENT_TAG, "using placement logic");

        switch(promotionDecision) {

            /// YYY it might be better to keep the placement tutors in a map instead of in an array
            /// YYY logic might be more symmetrical
            /// YYY if (placement) {placementMap.get(activeTutorId)} else {transitionMap.get(activeTutorId)}
            // NEXT is equivalent to passing the placement test and moving to next placement test

            // SUPER_PLACEMENT... thought-experiment. Call this method twice. Second time it will be a repeat.
            // NEXT->NEXT. NEXT->SAME. NEXT->FAIL.
            // SAME->NEXT. SAME->SAME. SAME->FAIL.
            // FAIL->NEXT. FAIL->SAME. FAIL->FAIL.
            // if (wasRepeat)... can we just return the currentWriting/Math tutor???

            case NEXT:
                // pass to next
                //
                if(useMathPlacement) {
                    // SUPER_PLACEMENT try this NEXT NEXT NEXT
                    if (wasRepeat) {
                        return _studentModel.getMathTutorID();
                    }

                    int mathPlacementIndex = placementIndex;

                    if (mathPlacementIndex == _matrix.mathPlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = _matrix.mathPlacement[mathPlacementIndex]; // off-by-one??? they'll never reach it :)
                        // update our preferences to exit PLACEMENT mode
                        _studentModel.updateMathPlacement(false);
                        _studentModel.updateMathPlacementIndex(null);


                        return lastPlacementTest.fail;
                    } else {
                        // SUPER_PLACEMENT should only increment if they did a new one
                        mathPlacementIndex++; // passing means incrementing by one
                        CPlacementTest_Tutor nextPlacementTest = _matrix.mathPlacement[mathPlacementIndex];

                        _studentModel.updateMathPlacementIndex(mathPlacementIndex);

                        return nextPlacementTest.tutor; // go to beginning of last level
                    }
                }
                // writingPlacement is only other option
                else {
                    // SUPER_PLACEMENT try this NEXT NEXT NEXT trace this...
                    if (wasRepeat) {
                        return _studentModel.getWritingTutorID();
                    }
                    int writingPlacementIndex = placementIndex;

                    if (writingPlacementIndex == _matrix.writePlacement.length) {
                        // student has made it to the end
                        CPlacementTest_Tutor lastPlacementTest = _matrix.writePlacement[writingPlacementIndex]; // off-by-one??? they'll never reach it :)
                        // update our preferences to exit PLACEMENT mode

                        _studentModel.updateWritingPlacement(false);
                        _studentModel.updateWritingPlacementIndex(null); //editor.remove("WRITING_PLACEMENT_INDEX");


                        return lastPlacementTest.fail; // go to beginning of last level
                    } else {
                        // SUPER_PLACEMENT should only increment if they did a new one
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
                    _studentModel.updateMathPlacement(false); // editor.putBoolean(placementKey, false); // no more placement
                    _studentModel.updateMathPlacementIndex(null);

                }
                // useWritePlacement only other option
                else {
                    lastPlacementTest = _matrix.writePlacement[placementIndex];
                    _studentModel.updateWritingPlacement(false); // editor.putBoolean(placementKey, false); // no more placement
                    _studentModel.updateWritingPlacementIndex(null); // editor.remove(placementIndexKey);
                }

                return lastPlacementTest.fail;


        }
    }
}
