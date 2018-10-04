package cmu.xprize.robotutor;

import org.junit.Before;
import org.junit.Test;

import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData.StudentSelfAssessment;
import cmu.xprize.robotutor.tutorengine.util.PerformancePromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules.PromotionDecision;

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

    // the student performance
    private PerformanceData performance;

    // our rule-engine of choice
    private PromotionRules rules;

    // which activity are we expecting next?
    private PromotionDecision expectedSelection;
    // which activity did our rule-engine actually select?
    private PromotionDecision promotionDecision;


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
        expectedSelection = PromotionDecision.NEXT;

        // execute the "assessPerformance" method
        promotionDecision = rules.assessPerformance(performance);

        // perform assertion
        assertEquals(expectedSelection, promotionDecision);
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
        expectedSelection = PromotionDecision.SAME;

        // execute the "assessPerformance" method
        promotionDecision = rules.assessPerformance(performance);

        // perform assertion
        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If student played a non-assessable activity, go to NEXT activity
     *
     * TODO problems:
     * TODO  - once you reach one of these, there's no way to reach previous activities
     */
    @Test
    public void testNonAssessableActivity() {

        // test story.read
        performance = new PerformanceData();
        performance.setActivityType("story.read");

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // test story.echo
        performance = new PerformanceData();
        performance.setActivityType("story.echo");

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // test story.hear
        performance = new PerformanceData();
        performance.setActivityType("story.hear");

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // test write
        performance = new PerformanceData();
        performance.setActivityType("write");

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // test countingx
        performance = new PerformanceData();
        performance.setActivityType("countingx");

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);

    }


    /**
     * If student does not complete enough problems to be assessed, go to NEXT activity
     *
     */
    @Test
    public void testNotEnoughAttempts() {

        // student does not try any questions. Go to next?
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(0);
        performance.setNumberWrong(0);
        performance.setNumberAttempts(0);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // student tries one question and quits. Go to next?
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(0);
        performance.setNumberWrong(1);
        performance.setNumberAttempts(1);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // student tries two questions and quits. Go to next?
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(0);
        performance.setNumberWrong(2);
        performance.setNumberAttempts(2);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);

    }

    /**
     * Weird edge case...  what if they have a low number of attempts, but there are also a low number of problems?
     */
    @Test
    public void testLowAttemptsLowTotalQuestions() {
        // what if student quits a problem with only three problems???
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(1);
        performance.setNumberWrong(0);
        performance.setNumberAttempts(1);
        performance.setTotalNumberQuestions(3);

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If the problem has a low number of questions, go to NEXT activity
     * TODO questions:
     * TODO   - what is the threshold for number of total questions?
     */
    @Test
    public void testLowQuestionTotal() {

        // student gets 0/3 correct, go to previous
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(0);
        performance.setNumberWrong(3);
        performance.setNumberAttempts(3);
        performance.setTotalNumberQuestions(3);

        expectedSelection = PromotionDecision.PREVIOUS;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // student gets 1/3 correct, go to previous
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(1);
        performance.setNumberWrong(2);
        performance.setNumberAttempts(3);
        performance.setTotalNumberQuestions(3);

        expectedSelection = PromotionDecision.PREVIOUS;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // student gets 2/3 correct, stay here
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(2);
        performance.setNumberWrong(1);
        performance.setNumberAttempts(3);
        performance.setTotalNumberQuestions(3);

        expectedSelection = PromotionDecision.SAME;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


        // student gets 3/3 correct, go to next
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(3);
        performance.setNumberWrong(0);
        performance.setNumberAttempts(3);
        performance.setTotalNumberQuestions(3);

        expectedSelection = PromotionDecision.NEXT;
        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);


    }

    /**
     * If student gets 0 out of 10 on a 10-question BubblePop problem, go to previous
     */
    @Test
    public void testLowPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(0);
        performance.setNumberWrong(10);
        performance.setNumberAttempts(10);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.PREVIOUS;

        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);

    }

    /**
     * If student gets 5 out of 10 on a 10-question BubblePop problem, go to previous
     */
    @Test
    public void testLowToMidPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(5);
        performance.setNumberWrong(5);
        performance.setNumberAttempts(10);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.SAME;

        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If student gets 8 out of 10 on a 10-question BubblePop problem, go to selectedActivity
     */
    @Test
    public void testMidToHighPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(8);
        performance.setNumberWrong(2);
        performance.setNumberAttempts(10);
        performance.setTotalNumberQuestions(10);

        expectedSelection = PromotionDecision.NEXT;

        promotionDecision = rules.assessPerformance(performance);
        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If student gets 10 out of 10 on a 10-question BubblePop problem, go to either selectedActivity or double_next
     */
    @Test
    public void testAboveHighPerformanceBubblePop() {
        performance = new PerformanceData();
        performance.setActivityType("bpop");
        performance.setNumberCorrect(10);
        performance.setNumberWrong(0);
        performance.setNumberAttempts(10);
        performance.setTotalNumberQuestions(10);

        promotionDecision = rules.assessPerformance(performance);
        // could be one of two options
        assertTrue(promotionDecision == PromotionDecision.NEXT || promotionDecision == PromotionDecision.DOUBLE_NEXT);
    }

}
