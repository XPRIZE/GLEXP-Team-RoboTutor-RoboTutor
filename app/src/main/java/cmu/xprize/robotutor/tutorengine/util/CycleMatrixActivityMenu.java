package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_0;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_1;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_2;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
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
        boolean isPlacementMode = false;

        // active skill dependent...
        switch(_student.getActiveSkill()) {
            case SELECT_WRITING:
                transitionMap = _matrix.writeTransitions;
                tutorId = _student.getWritingTutorID();
                isPlacementMode = _student.getWritingPlacement();
                break;

            case SELECT_STORIES:
                transitionMap = _matrix.storyTransitions;
                tutorId = _student.getStoryTutorID();
                isPlacementMode = false;
                break;

            case SELECT_MATH:
                transitionMap = _matrix.mathTransitions;
                tutorId = _student.getMathTutorID();
                isPlacementMode = _student.getMathPlacement();
                break;
        }

        // iff in placement mode... show the same tutor twice
        nextTutors[0] = (CAt_Data) transitionMap.get(tutorId); // N
        nextTutors[1] = isPlacementMode ? nextTutors[0] : (CAt_Data) transitionMap.get(nextTutors[0].next); // N or N + 1

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
        activeLayout.items[1].prompt = prompt;
        activeLayout.items[1].help = prompt;

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
        String activeTutorId = "";
        HashMap transitionMap = null;
        String rootTutor = "";

        String activeSkill = null;

        Log.d("OH_BEHAVE", "some behavior here should be different...");

        if (buttonBehavior.equals(SELECT_REPEAT)) {
            Log.d("REPEAT_ME", "repeating behavior");
            RoboTutor.STUDENT_CHOSE_REPEAT = true;
        }

        CAt_Data activeTutor;
        String[] nextTutors = new String[3];

        activeSkill = RoboTutor.STUDENT_CHOSE_REPEAT ? _student.getLastSkill() : _student.getActiveSkill();

        switch(activeSkill) {
            case SELECT_WRITING:
                activeTutorId = _student.getWritingTutorID();
                Log.d("REPEAT_ME", "writingTutor=" + activeTutorId);
                break;

            case SELECT_STORIES:
                activeTutorId = _student.getStoryTutorID();
                Log.d("REPEAT_ME", "storyTutor=" + activeTutorId);
                break;

            case SELECT_MATH:
                activeTutorId = _student.getMathTutorID();
                Log.d("REPEAT_ME", "mathTutor=" + activeTutorId);
                break;
        }

        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "CycleMatrixActivityMenu: activeSkill=" + activeSkill + " -- activeTutorId=" + activeTutorId);
        transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
        activeTutor = (CAt_Data) transitionMap.get(activeTutorId);
        nextTutors[0] = activeTutor.tutor_id;
        //nextTutors[0] = ((CAt_Data) transitionMap.get(activeTutor.easier)).tutor_id; // next hardest tutor!!!
        nextTutors[1] = ((CAt_Data) transitionMap.get(activeTutor.next)).tutor_id; // next hardest tutor!!!

        switch(buttonBehavior.toUpperCase()) {

            case SELECT_OPTION_0:
                activeTutorId = nextTutors[0];
                break;

            case SELECT_OPTION_1:
                // launch the next tutor
                // something like this...
                activeTutorId = nextTutors[1];

                break;

            case SELECT_OPTION_2:
                // launch the next.next tutor
                activeTutorId = nextTutors[2];
                break;

            case AS_CONST.SELECT_REPEAT:

                break;
        }


        // the next tutor to be launched
        CAt_Data tutorToLaunch = (CAt_Data) transitionMap.get(activeTutorId);

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
