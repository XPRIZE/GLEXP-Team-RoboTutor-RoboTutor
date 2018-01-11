package cmu.xprize.robotutor;

import org.junit.Before;
import org.junit.Test;

import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData.StudentSelfAssessment;
import cmu.xprize.robotutor.tutorengine.util.PerformancePromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules.SelectedActivity;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * RoboTutor
 *
 * Tests the "Self Assessment" promotion rules
 * <p>
 * Created by kevindeland on 1/11/18.
 */

public class PerformanceBasedPromotionRulesTest {


    private PerformanceData performance;

    private PromotionRules rules;

    private SelectedActivity selectedActivity;
    private SelectedActivity expectedSelection;


    @Before
    public void setUp () {

        // initialize new rules
        rules = new PerformancePromotionRules();
    }

    /**
     * If null go to NEXT activity
     */
    @Test
    public void testPerformanceNull() {

        // initialize performance data to clear existing variables (thank you Java garbage collector!)
        performance = new PerformanceData();
        // set desired performance variables i.e. num wrong, num correct, etc

        // specify which activity is expected
        expectedSelection = SelectedActivity.NEXT;

        // execute the "selectActivityByPerformance" method
        selectedActivity = rules.selectActivityByPerformance(performance);

        // perform assertion
        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If student selects "SELECT_REPEAT" go to NEXT activity
     */
    @Test
    public void testSelectRepeat() {

        // initialize performance data to clear existing variables
        performance = new PerformanceData();
        // set desired performance variables i.e. num wrong, num correct, etc
        performance.setSelfAssessment(StudentSelfAssessment.PLAY_AGAIN);

        // specify which activity is expected
        expectedSelection = SelectedActivity.SAME;

        // execute the "selectActivityByPerformance" method
        selectedActivity = rules.selectActivityByPerformance(performance);

        // perform assertion
        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If student played a non-assessable activity, go to NEXT activity
     */
    @Test
    public void testNonAssessableActivity() {

        // test story.read
        performance = new PerformanceData();
        performance.setActivityType("story.read");

        expectedSelection = SelectedActivity.NEXT;
        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);


        // test story.echo
        performance = new PerformanceData();
        performance.setActivityType("story.echo");

        expectedSelection = SelectedActivity.NEXT;
        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);


        // test story.hear
        performance = new PerformanceData();
        performance.setActivityType("story.hear");

        expectedSelection = SelectedActivity.NEXT;
        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);


        // test countingx
        performance = new PerformanceData();
        performance.setActivityType("countingx");

        expectedSelection = SelectedActivity.NEXT;
        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);

    }


    /**
     * If student does not complete enough problems to be assessed, go to NEXT activity
     */
    @Test
    public void testNotEnoughProblems() {

        // TODO continue
        // NEXT after this....
    }

    /**
     * If student gets 0 out of 7 on a 7-question BubblePop problem, go to previous
     */
    @Test
    public void testLowPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);
        performance.setNumberCorrect(0);
        performance.setNumberWrong(7);
        performance.setNumberAttempts(7);

        expectedSelection = SelectedActivity.PREVIOUS;

        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);

    }

    /**
     * If student gets 5 out of 10 on a 10-question BubblePop problem, go to previous
     */
    @Test
    public void testLowToMidPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);
        performance.setNumberCorrect(5);
        performance.setNumberWrong(10);
        performance.setNumberAttempts(10);

        expectedSelection = SelectedActivity.SAME;

        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If student gets 8 out of 10 on a 10-question BubblePop problem, go to selectedActivity
     */
    @Test
    public void testMidToHighPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);
        performance.setNumberCorrect(8);
        performance.setNumberWrong(10);
        performance.setNumberAttempts(10);

        expectedSelection = SelectedActivity.NEXT;

        selectedActivity = rules.selectActivityByPerformance(performance);
        assertEquals(expectedSelection, selectedActivity);
    }

    /**
     * If student gets 10 out of 10 on a 10-question BubblePop problem, go to either selectedActivity or double_next
     */
    @Test
    public void testAboveHighPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);
        performance.setNumberCorrect(10);
        performance.setNumberWrong(10);
        performance.setNumberAttempts(10);

        selectedActivity = rules.selectActivityByPerformance(performance);
        // could be one of two options
        assertTrue(selectedActivity == SelectedActivity.NEXT || selectedActivity == SelectedActivity.DOUBLE_NEXT);
    }

}
