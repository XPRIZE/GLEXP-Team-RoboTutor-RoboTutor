package cmu.xprize.robotutor;

import org.junit.Before;
import org.junit.Test;

import cmu.xprize.robotutor.tutorengine.util.PerformanceData;
import cmu.xprize.robotutor.tutorengine.util.PerformanceData.StudentSelfAssessment;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules;
import cmu.xprize.robotutor.tutorengine.util.PromotionRules.PromotionDecision;
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
    private PromotionDecision expectedSelection;
    // which activity did our rule-engine actually select?
    private PromotionDecision promotionDecision;


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

        promotionDecision = rules.assessPerformance(performance);
        expectedSelection = PromotionDecision.NEXT;

        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If "Play again" do same activity
     */
    @Test
    public void selfAssessRepeat() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.PLAY_AGAIN);

        promotionDecision = rules.assessPerformance(performance);
        expectedSelection = PromotionDecision.SAME;

        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If "Just right" go to selectedActivity activity
     */
    @Test
    public void selfAssessJustRight() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.JUST_RIGHT);

        promotionDecision = rules.assessPerformance(performance);
        expectedSelection = PromotionDecision.NEXT;

        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If "Too hard" go to easier activity
     */
    @Test
    public void selfAssessTooHard() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.TOO_HARD);

        promotionDecision = rules.assessPerformance(performance);
        expectedSelection = PromotionDecision.OLD_EASIER;

        assertEquals(expectedSelection, promotionDecision);
    }

    /**
     * If "Too easy" go to harder activity
     */
    @Test
    public void selfAssessTooEasy() {

        performance = new PerformanceData();
        performance.setSelfAssessment(StudentSelfAssessment.TOO_EASY);

        promotionDecision = rules.assessPerformance(performance);
        expectedSelection = PromotionDecision.OLD_HARDER;

        assertEquals(expectedSelection, promotionDecision);
    }


}
