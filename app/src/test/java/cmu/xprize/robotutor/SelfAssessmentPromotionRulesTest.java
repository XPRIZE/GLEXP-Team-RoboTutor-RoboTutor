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


    private PerformanceData studentPerformance;

    private PromotionRules selfAssessRules;

    private SelectedActivity next;
    private SelectedActivity expectedNext;


    @Before
    public void setUp () {

        selfAssessRules = new SelfAssessmentPromotionRules();
    }

    /**
     * If null go to next activity
     */
    @Test
    public void selfAssessNull() {
        studentPerformance = new PerformanceData();

        next = selfAssessRules.selectActivityByPerformance(studentPerformance);
        expectedNext = SelectedActivity.NEXT;

        assertEquals(expectedNext, next);
    }

    /**
     * If "Play again" do same activity
     */
    @Test
    public void selfAssessRepeat() {

        studentPerformance = new PerformanceData();
        studentPerformance.setSelfAssessment(StudentSelfAssessment.PLAY_AGAIN);

        next = selfAssessRules.selectActivityByPerformance(studentPerformance);
        expectedNext = SelectedActivity.SAME;

        assertEquals(expectedNext, next);
    }

    /**
     * If "Just right" go to next activity
     */
    @Test
    public void selfAssessJustRight() {

        studentPerformance = new PerformanceData();
        studentPerformance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);

        next = selfAssessRules.selectActivityByPerformance(studentPerformance);
        expectedNext = SelectedActivity.NEXT;

        assertEquals(expectedNext, next);
    }

    /**
     * If "Too hard" go to easier activity
     */
    @Test
    public void selfAssessTooHard() {

        studentPerformance = new PerformanceData();
        studentPerformance.setSelfAssessment(StudentSelfAssessment.TOO_HARD);

        next = selfAssessRules.selectActivityByPerformance(studentPerformance);
        expectedNext = SelectedActivity.OLD_EASIER;

        assertEquals(expectedNext, next);
    }

    /**
     * If "Too easy" go to harder activity
     */
    @Test
    public void selfAssessTooEasy() {

        studentPerformance = new PerformanceData();
        studentPerformance.setSelfAssessment(StudentSelfAssessment.TOO_EASY);

        next = selfAssessRules.selectActivityByPerformance(studentPerformance);
        expectedNext = SelectedActivity.OLD_HARDER;

        assertEquals(expectedNext, next);
    }


}
