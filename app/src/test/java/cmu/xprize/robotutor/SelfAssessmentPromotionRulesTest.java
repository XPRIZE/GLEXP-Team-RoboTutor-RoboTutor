package cmu.xprize.robotutor;

import org.junit.Before;
import org.junit.Test;

import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData.StudentSelfAssessment;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules.SelectedActivity;
import cmu.xprize.robotutor.tutorengine.util.SelfAssessmentPromotionRules;

import static org.junit.Assert.assertEquals;

/**
 * RoboTutor
 *
 * Tests the "Self Assessment" promotion rules
 * <p>
 * Created by kevindeland on 1/11/18.
 */

public class SelfAssessmentPromotionRulesTest {

    // the student performance
    private PerformanceData performance;

    // our rule-engine of choice
    private PromotionRules rules;

    // which activity are we expecting next?
    private SelectedActivity expectedSelection;
    // which activity did our rule-engine actually select?
    private SelectedActivity selectedActivity;


    @Before
    public void setUp () {

        rules = new SelfAssessmentPromotionRules();
    }

    /**
     * If null go to selectedActivity activity
     */
    @Test
    public void selfAssessNull() {
        performance = new PerformanceData();

        selectedActivity = rules.selectActivityByPerformance(performance);
        expectedSelection = SelectedActivity.NEXT;

        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If "Play again" do same activity
     */
    @Test
    public void selfAssessRepeat() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.PLAY_AGAIN);

        selectedActivity = rules.selectActivityByPerformance(performance);
        expectedSelection = SelectedActivity.SAME;

        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If "Just right" go to selectedActivity activity
     */
    @Test
    public void selfAssessJustRight() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);

        selectedActivity = rules.selectActivityByPerformance(performance);
        expectedSelection = SelectedActivity.NEXT;

        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If "Too hard" go to easier activity
     */
    @Test
    public void selfAssessTooHard() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.TOO_HARD);

        selectedActivity = rules.selectActivityByPerformance(performance);
        expectedSelection = SelectedActivity.OLD_EASIER;

        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If "Too easy" go to harder activity
     */
    @Test
    public void selfAssessTooEasy() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.TOO_EASY);

        selectedActivity = rules.selectActivityByPerformance(performance);
        expectedSelection = SelectedActivity.OLD_HARDER;

        assertEquals(expectedSelection, selectedActivity);
    }


}
