package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_0;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_1;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_2;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.comp_session.AS_CONST.REPEAT_DEBUG_TAG;
import static cmu.xprize.comp_session.AS_CONST.SELECT_REPEAT;
import static cmu.xprize.util.TCONST.MENU_BUG_TAG;

/**
 * CycleMatrixActivityMenu
 *
 * an Activity Menu style where the matrix cycles each time,
 * and the student chooses from one of two activities in the same matrix
 * <p>
 * Created by kevindeland on 9/25/18.
 */

public class CycleMatrixActivityMenu implements IActivityMenu {

    TransitionMatrixModel _matrix;
    StudentDataModel _student;

    public CycleMatrixActivityMenu(TransitionMatrixModel matrix, StudentDataModel student) {
        this._matrix = matrix;
        this._student = student;
    }

    @Override
    public String getLayoutName() {
        return "ask_activity_selector_2x2";
    }

    @Override
    public CAt_Data[] getTutorsToShow() {

        CAt_Data[] nextTutors = new CAt_Data[2];
        HashMap transitionMap = _matrix.storyTransitions;
        String tutorId = "";

        CPlacementTest_Tutor[] placement = null;
        boolean isPlacementMode = false;
        int placementIndex = 0;


        // active skill dependent...
        switch(_student.getActiveSkill()) {
            case SELECT_WRITING:
                transitionMap = _matrix.writeTransitions;

                isPlacementMode = _student.getWritingPlacement();
                if (isPlacementMode) {
                    placement = _matrix.writePlacement;
                    placementIndex = _student.getWritingPlacementIndex();
                    tutorId = placement[placementIndex].tutor;
                } else {
                    tutorId = _student.getWritingTutorID();
                }
                break;

            case SELECT_STORIES:
                transitionMap = _matrix.storyTransitions;
                tutorId = _student.getStoryTutorID();
                isPlacementMode = false;
                break;

            case SELECT_MATH:
                transitionMap = _matrix.mathTransitions;

                isPlacementMode = _student.getMathPlacement();
                if (isPlacementMode) {
                    placement = _matrix.mathPlacement;
                    placementIndex = _student.getMathPlacementIndex();
                    tutorId = placement[placementIndex].tutor;
                } else {
                    tutorId = _student.getMathTutorID();
                }
                break;
        }

        // iff in placement mode... show the same tutor twice
        // SUPER_PLACEMENT if the same, do two different icons
        if (isPlacementMode && placement != null) {
            Log.e("SUPER_PLACEMENT", "not ready yet");
            nextTutors[0] = (CAt_Data) transitionMap.get(tutorId);
            nextTutors[1] = nextTutors[0];
        } else {
            nextTutors[0] = (CAt_Data) transitionMap.get(tutorId); // N
            nextTutors[1] = isPlacementMode ? nextTutors[0] : (CAt_Data) transitionMap.get(nextTutors[0].next); // N or N + 1
        }

        return nextTutors;
    }

    @Override
    public CAsk_Data initializeActiveLayout() {
        CAsk_Data activeLayout = new CAsk_Data();

        activeLayout.items = new CAskElement[4];
        // both options will have the same content area, same prompt
        activeLayout.items[0] = new CAskElement();
        activeLayout.items[0].componentID = "SbuttonOption1";
        activeLayout.items[0].behavior = SELECT_OPTION_0;

        activeLayout.items[1] = new CAskElement();
        activeLayout.items[1].componentID = "SbuttonOption2";
        activeLayout.items[1].behavior = SELECT_OPTION_1;

        String prompt = null;
        switch(_student.getActiveSkill()) {
            case SELECT_WRITING:
                prompt = "reading and writing";
                break;

            case SELECT_STORIES:
                prompt = "stories";
                break;

            case SELECT_MATH:
                prompt = "numbers and math";
                break;
        }
        activeLayout.items[0].prompt = prompt;
        activeLayout.items[0].help = prompt;

        activeLayout.items[1].prompt = "something different";
        activeLayout.items[1].help = "something different";

        activeLayout.items[2] =  new CAskElement();
        activeLayout.items[2].componentID = "SbuttonRepeat";
        activeLayout.items[2].behavior = AS_CONST.SELECT_REPEAT;
        activeLayout.items[2].prompt = "lets do it again";
        activeLayout.items[2].help = "lets do it again";

        activeLayout.items[3] =  new CAskElement();
        activeLayout.items[3].componentID = "SbuttonExit";
        activeLayout.items[3].behavior = AS_CONST.SELECT_EXIT;
        activeLayout.items[3].prompt = "I want to stop using RoboTutor";
        activeLayout.items[3].help = "I want to stop using RoboTutor";

        return activeLayout;
    }

    @Override
    public Map<String, String> getButtonBehaviorMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put(SELECT_OPTION_0, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(SELECT_OPTION_1, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.SELECT_REPEAT, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.SELECT_EXIT, AS_CONST.QUEUEMAP_KEYS.EXIT_BUTTON_BEHAVIOR);
        return map;
    }

    @Override
    public CAt_Data getTutorToLaunch(String buttonBehavior) {

        //
        String zeroIndexedTutorId = ""; // the current tutor in the left-top position
        HashMap transitionMap = null;
        String rootTutor = "";

        String activeSkill = null;
        String chosenTutorId;

        Log.d("OH_BEHAVE", "some behavior here should be different...");

        if (buttonBehavior.equals(SELECT_REPEAT)) {
            RoboTutor.logManager.postEvent_I(REPEAT_DEBUG_TAG, "repeating behavior");
            RoboTutor.STUDENT_CHOSE_REPEAT = true;
            activeSkill = _student.getLastSkill();
            chosenTutorId = _student.getLastTutor();
            transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
            rootTutor = _matrix.getRootSkillByContentArea(activeSkill);
            RoboTutor.logManager.postEvent_I(REPEAT_DEBUG_TAG, String.format("activeSkill=%s;chosenTutorId=%s;transitionMap=%s;rootTutor=%s", activeSkill, chosenTutorId, activeSkill, rootTutor));

        } else {

            activeSkill = _student.getActiveSkill();
            CAt_Data zeroIndexedTutor;
            String[] nextTutors = new String[3];

            boolean inPlacementMode = false;

            switch(activeSkill) {
                case SELECT_WRITING:
                    zeroIndexedTutorId = _student.getWritingTutorID();
                    Log.d("REPEAT_ME", "writingTutor=" + zeroIndexedTutorId);
                    inPlacementMode = _student.getWritingPlacement();
                    break;

                case SELECT_STORIES:
                    zeroIndexedTutorId = _student.getStoryTutorID();
                    Log.d("REPEAT_ME", "storyTutor=" + zeroIndexedTutorId);
                    break;

                case SELECT_MATH:
                    zeroIndexedTutorId = _student.getMathTutorID();
                    Log.d("REPEAT_ME", "mathTutor=" + zeroIndexedTutorId);
                    inPlacementMode = _student.getMathPlacement();
                    break;
            }


            RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "CycleMatrixActivityMenu: activeSkill=" + activeSkill + " -- activeTutorId=" + zeroIndexedTutorId);
            transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
            rootTutor = _matrix.getRootSkillByContentArea(activeSkill);
            zeroIndexedTutor = (CAt_Data) transitionMap.get(zeroIndexedTutorId);
            nextTutors[0] = zeroIndexedTutor.tutor_id;
            nextTutors[1] = inPlacementMode ? nextTutors[0] : ((CAt_Data) transitionMap.get(zeroIndexedTutor.next)).tutor_id; // next hardest tutor!!! (I don't know WHY this is implemented twice)

            switch(buttonBehavior.toUpperCase()) {

                case SELECT_OPTION_0: // TRACE_PROMOTION looks good...
                    chosenTutorId = nextTutors[0];
                    break;

                case SELECT_OPTION_1: // TRACE_PROMOTION looks good...
                    // launch the next tutor
                    // something like this...
                    chosenTutorId = nextTutors[1];

                    break;

                case SELECT_OPTION_2:
                    // launch the next.next tutor
                    chosenTutorId = nextTutors[2];
                    break;

                default:
                    chosenTutorId = zeroIndexedTutorId; // TRACE_PROMOTION does this break anything? // getLastTutor?
                    break;
            }

            // If they choose the second option, update our position in thematrix //
            // will only be updated if SELECT_OPTION_1 or SELECT_OPTION_2 were selected //
            if (!chosenTutorId.equals(zeroIndexedTutorId)) {
                switch (activeSkill) {
                    case SELECT_WRITING:
                        _student.updateWritingTutorID(chosenTutorId);
                        break;

                    case SELECT_STORIES:
                        _student.updateStoryTutorID(chosenTutorId);
                        break;

                    case SELECT_MATH:
                        _student.updateMathTutorID(chosenTutorId);
                        break;
                }
            }

        }


        // TRACE_PROMOTION doesn't save tutorToLaunch when we choose the second option!!!
        // the next tutor to be launched
        CAt_Data tutorToLaunch = (CAt_Data) transitionMap.get(chosenTutorId);
        Log.wtf(MENU_BUG_TAG, chosenTutorId + " " +  activeSkill);

        // This is just to make sure we go somewhere if there is a bad link - which
        // there shuoldn't be :)
        //
        if (tutorToLaunch == null) {
            tutorToLaunch = (CAt_Data) transitionMap.get(rootTutor);
        }
        return tutorToLaunch;
    }

    @Override
    public String getDebugMenuSkill() {
        return RoboTutor.STUDENT_CHOSE_REPEAT ? _student.getLastSkill() : _student.getActiveSkill(); // DEBUG_MENU_LOGIC (x) lastSkill vs ActiveSkill...
    }
}
