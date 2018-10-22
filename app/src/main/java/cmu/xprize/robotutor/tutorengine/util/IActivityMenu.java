package cmu.xprize.robotutor.tutorengine.util;

import java.util.Map;

import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.util.CAt_Data;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/25/18.
 */

public interface IActivityMenu {

    /**
     * The name of the layout file to inflate
     * @return name of the layout file to inflate
     */
    String getLayoutName();

    /**
     * Which tutors are shown as menu options for the students to choose.
     * @return the tutors to be shown
     */
    CAt_Data[] getTutorsToShow();

    /**
     * What do the menu buttons look like?
     * @return a layout configuration
     */
    CAsk_Data initializeActiveLayout();

    /**
     * A map of the button names to the behavior they perform
     * @return
     */
    Map<String, String> getButtonBehaviorMap();

    /**
     *
     * @param buttonBehavior what was the button pressed?
     * @return tutor to launch
     */
    CAt_Data getTutorToLaunch(String buttonBehavior);

    /**
     * Get the matrix of the debug menu
     * @return stories, writing, math, etc
     */
    String getDebugMenuSkill();

}
