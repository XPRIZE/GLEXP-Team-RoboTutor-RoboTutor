package cmu.xprize.robotutor.tutorengine.util;

/**
 * RoboTutor
 *
 * Old promotion rules: promote based on student's self-assessment.
 *
 * Created by kevindeland on 1/11/18.
 */

public class SelfAssessmentPromotionRules extends PromotionRules {


    @Override
    public PromotionDecision assessPerformance(PerformanceData performance) {

        // a simple one-to-one matching of student's self-assessment to chosen activity

        if(performance.getSelfAssessment() == null) {
            return PromotionDecision.NEXT;
        }

        switch (performance.getSelfAssessment()) {
            case PLAY_AGAIN:
                return PromotionDecision.SAME;

            case LET_ROBOTUTOR_DECIDE:
                return PromotionDecision.NEXT;

            case TOO_EASY:
                return PromotionDecision.DOUBLE_NEXT;

            case TOO_HARD:
                return PromotionDecision.PREVIOUS;

            case JUST_RIGHT:
                return PromotionDecision.NEXT;

            default:
                return PromotionDecision.NEXT;

        }
    }
}
