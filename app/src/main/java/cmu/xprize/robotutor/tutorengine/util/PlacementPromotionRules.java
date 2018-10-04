package cmu.xprize.robotutor.tutorengine.util;

import java.util.Locale;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.TCONST;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 4/12/18.
 */

public class PlacementPromotionRules extends PromotionRules {

    static final double HIGH_PERFORMANCE_THRESHOLD = 0.9;

    private String TAG = "PlacementPromotionRules";

    /**
     * For this class, we only have three possible outcomes:
     * SAME = repeat (because they didn't do enough problems)
     * NEXT = pass
     * PREVIOUS = fail
     *
     * @param performance
     * @return
     */
    @Override
    public PromotionDecision assessPerformance(PerformanceData performance) {
        RoboTutor.logManager.postEvent_D(TAG, performance.toString());

        // if they want to play again, repeat no matter what
        if (performance.getSelfAssessment() == PerformanceData.StudentSelfAssessment.PLAY_AGAIN) {
            return PromotionDecision.SAME;
        }

        RoboTutor.logManager.postEvent_D(TCONST.PLACEMENT_TAG, performance.toString());

        // now we get into assessment

        String msg = String.format(Locale.US, "Finished activity %s with %d out of %d",
                performance.getActiveSkill(),
                performance.getNumberAttempts(),
                performance.getTotalNumberQuestions());

        RoboTutor.logManager.postEvent_I(TCONST.PLACEMENT_TAG, msg);
        // if they start an activity but don't like it.... what do?
        if (performance.getNumberAttempts() < performance.getTotalNumberQuestions()) {
            return PromotionDecision.SAME;
        }

        // percentage is #correct / #totalQuestions.... NOT #correct / #numAttempts
        // buuuuuut if they back out???

        // YYY what now?
        double percentCorrect = (double) performance.getNumberCorrect() /
                (double) (performance.getNumberAttempts());

        RoboTutor.logManager.postEvent_I(TCONST.PLACEMENT_TAG, String.valueOf(percentCorrect));

        // YYY if they get above 90, keep going. If not, fun is over.
        if (percentCorrect >= HIGH_PERFORMANCE_THRESHOLD) {
            // 50/50 probability
            return PromotionDecision.NEXT;
        } else {
            return PromotionDecision.PREVIOUS;
        }
    }
}
