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
     * @return
     */
    String getLayoutName();

    /**
     * Which tutors are shown as menu options for the students to choose.
     * @return
     */
    CAt_Data[] getTutorsToShow();

    /**
     * What do the menu buttons look like?
     * @return
     */
    CAsk_Data initializeActiveLayout();

    /**
     * A map of the button names to the behavior they perform
     * @return
     */
    Map<String, String> getButtonBehaviorMap();

    CAt_Data getTutorToLaunch(String buttonBehavior);

}
