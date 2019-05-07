package cmu.xprize.robotutor.tutorengine.util;

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
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;

/**
 * StudentChooseMatrixActivityMenu
 *
 * An Activity Menu style where the student chooses which matrix they'll play in.
 * <p>
 * Created by kevindeland on 9/25/18.
 */

public class StudentChooseMatrixActivityMenu implements IActivityMenu {

    TransitionMatrixModel _matrix;
    StudentDataModel _student;
    
    public StudentChooseMatrixActivityMenu(TransitionMatrixModel matrix, StudentDataModel student) {
        this._matrix = matrix;
        this._student = student;
    }

    @Override
    public String getLayoutName() {
        return "ask_activity_selector_2x3";
    }

    @Override
    public CAt_Data[] getTutorsToShow() {
        CAt_Data[] nextTutors  = new CAt_Data[3];
        nextTutors[0] = (CAt_Data) _matrix.writeTransitions.get(_student.getWritingTutorID());
        nextTutors[1] = (CAt_Data) _matrix.storyTransitions.get(_student.getStoryTutorID());
        nextTutors[2] = (CAt_Data) _matrix.mathTransitions.get(_student.getMathTutorID());
        return nextTutors;
    }

    @Override
    public CAsk_Data initializeActiveLayout() {

        CAsk_Data activeLayout = new CAsk_Data();

        activeLayout.items = new CAskElement[5];
        activeLayout.items[0] = new CAskElement();
        activeLayout.items[0].componentID = "SbuttonOption1";
        activeLayout.items[0].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
        activeLayout.items[0].prompt = "reading and writing";
        activeLayout.items[0].help = "reading and writing";

        activeLayout.items[1] = new CAskElement();
        activeLayout.items[1].componentID = "SbuttonOption2";
        activeLayout.items[1].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
        activeLayout.items[1].prompt = "stories";
        activeLayout.items[1].help = "stories";

        activeLayout.items[2] = new CAskElement();
        activeLayout.items[2].componentID = "SbuttonOption3";
        activeLayout.items[2].behavior = AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
        activeLayout.items[2].prompt = "numbers and math";
        activeLayout.items[2].help = "numbers and math";


        activeLayout.items[3] =  new CAskElement();
        activeLayout.items[3].componentID = "SbuttonRepeat";
        activeLayout.items[3].behavior = AS_CONST.SELECT_REPEAT;
        activeLayout.items[3].prompt = "lets do it again";
        activeLayout.items[3].help = "lets do it again";

        activeLayout.items[4] =  new CAskElement();
        activeLayout.items[4].componentID = "SbuttonExit";
        activeLayout.items[4].behavior = AS_CONST.SELECT_EXIT;
        activeLayout.items[4].prompt = "I want to stop using RoboTutor";
        activeLayout.items[4].help = "I want to stop using RoboTutor";

        return activeLayout;
    }

    @Override
    public Map<String, String> getButtonBehaviorMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put(AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.BEHAVIOR_KEYS.SELECT_MATH, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.SELECT_REPEAT, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(AS_CONST.SELECT_EXIT, AS_CONST.QUEUEMAP_KEYS.EXIT_BUTTON_BEHAVIOR);
        return map;
    }

    @Override
    public CAt_Data getTutorToLaunch(String buttonBehavior) {

        String activeTutorId = null;
        HashMap transitionMap = null;
        String rootTutor = null;

        String activeSkill = null;
        
        // this could seriously be cleaned up...
        switch (buttonBehavior.toUpperCase()) {

            case AS_CONST.SELECT_REPEAT:

                RoboTutor.STUDENT_CHOSE_REPEAT = true;

                String lastTutor = _student.getLastTutor(); // MENU_LOGIC why is this only called once?
                if (lastTutor != null) { // for when it's the first time...s
                    activeTutorId = lastTutor;
                }
                if (activeTutorId == null) {
                    rootTutor     = _matrix.getRootSkillByContentArea(SELECT_WRITING);
                }
                activeSkill = _student.getLastSkill();
                transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
                break;

            case SELECT_WRITING:

                activeSkill   = SELECT_WRITING; // √
                activeTutorId = _student.getWritingTutorID();
                rootTutor     = _matrix.getRootSkillByContentArea(SELECT_WRITING);
                transitionMap = _matrix.getTransitionMapByContentArea(SELECT_WRITING);
                break;

            case SELECT_STORIES:

                activeSkill   = SELECT_STORIES; // √
                activeTutorId = _student.getStoryTutorID();
                rootTutor     = _matrix.getRootSkillByContentArea(SELECT_STORIES);
                transitionMap = _matrix.getTransitionMapByContentArea(SELECT_STORIES);
                break;

            case SELECT_MATH:

                activeSkill   = SELECT_MATH; // √
                activeTutorId = _student.getMathTutorID();
                rootTutor     = _matrix.getRootSkillByContentArea(SELECT_MATH);
                transitionMap = _matrix.getTransitionMapByContentArea(SELECT_MATH);
                break;
        }

        // update active skill based on student choice
        if (activeSkill != null && !RoboTutor.STUDENT_CHOSE_REPEAT) _student.updateActiveSkill(activeSkill);

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
        return _student.getActiveSkill();
    }
}
