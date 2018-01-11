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
    public SelectedActivity selectActivityByPerformance(PerformanceData performance) {

        // a simple one-to-one matching of student's self-assessment to chosen activity

        if(performance.getSelfAssessment() == null) {
            return SelectedActivity.NEXT;
        }

        switch (performance.getSelfAssessment()) {
            case PLAY_AGAIN:
                return SelectedActivity.SAME;

            case LET_ROBOTUTOR_DECIDE:
                return SelectedActivity.NEXT;

            case TOO_EASY:
                return SelectedActivity.OLD_HARDER;

            case TOO_HARD:
                return SelectedActivity.OLD_EASIER;

            case JUST_RIGHT:
                return SelectedActivity.NEXT;

            default:
                return SelectedActivity.NEXT;

        }
    }
}
