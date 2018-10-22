package cmu.xprize.robotutor.tutorengine.util;


import android.util.Log;

import java.util.Arrays;
import java.util.List;

import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 1/11/18.
 */

public class PerformancePromotionRules extends PromotionRules {

    private static final int MIN_NUM_ATTEMPTS = 2;
    private String TAG = "PerformancePromotionRules";

    List<String> nonAssessableActivities = Arrays.asList("story.read", "story.echo", "story.hear", "countingx", "numscale", "story");

    List<String> lenientActivities = Arrays.asList("write", "math");

    static final double LOW_PERFORMANCE_THRESHOLD = 0.5;
    static final double MID_PERFORMANCE_THRESHOLD = 0.83;
    static final double HIGH_PERFORMANCE_THRESHOLD = 0.9;

    static final double LOW_LENIENT_PERFORMANCE_THRESHOLD = 0.4;
    static final double MID_LENIENT_PERFORMANCE_THRESHOLD = 0.55;
    static final double HIGH_LENIENT_PERFORMANCE_THRESHOLD = 0.7;


    // what if there are not enough total questions???
    // for reference... here is a simulation of percentages for #correct out of #problems {1,7}... and the respective promotion
    double[] p1 = {0.0, 1.0}; // {PREV, HIGH}
    double[] p2 = {0.0, 0.5, 1.0}; // {PREV, STAY, HIGH}
    double[] p3 = {0.0, 0.333, 0.667, 1.0}; // {PREV, PREV, STAY, HIGH}
    double[] p4 = {0.0, 0.25, 0.5, 0.75, 1.0}; // {PREV, PREV, STAY, NEXT, HIGH}
    double[] p5 = {0.0, 0.20, 0.40, 0.60, 0.80, 0.90}; // {PREV, PREV, STAY, STAY, NEXT, HIGH}
    double[] p6 = {0.0, 0.167, 0.333, 0.50, 0.667, 0.833, 1.0}; // {PREV, PREV, PREV, STAY, STAY, NEXT, HIGH}
    double[] p7 = {0.0, 0.14, 0.28, 0.43, 0.57, 0.72, 0.86, 1.0}; // {PREV, PREV, PREV, STAY, STAY, NEXT, NEXT, HIGH}


    @Override
    public PromotionDecision assessPerformance(PerformanceData performance) {
        RoboTutor.logManager.postEvent_D(TAG, performance.toString());

        // if they want to play again, repeat no matter what
        if (performance.getSelfAssessment() == PerformanceData.StudentSelfAssessment.PLAY_AGAIN) {
            return PromotionDecision.SAME;
        }


        if (performance.getActiveSkill().equals(AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES) && !RoboTutor.STUDENT_CHOSE_REPEAT) {
            return PromotionDecision.NEXT;
        }

        // test Non-assessable activities
        if (performance.getActivityType() != null) {
            for (String type : nonAssessableActivities) {
                if (performance.getActivityType().startsWith(type)) {
                    return PromotionDecision.NEXT;
                }
            }
        }

        // now we get into assessment

        // if they start an activity but don't like it.... what do?
        if (performance.getNumberAttempts() <= MIN_NUM_ATTEMPTS && performance.getTotalNumberQuestions() > 3) {
            // equiprobably go to next or previous
            return Math.random() > 0.5 ? PromotionDecision.NEXT : PromotionDecision.PREVIOUS;
        }

        // prevent divide by zero
        /*if (performance.getTotalNumberQuestions() == 0) {
            return SelectedActivity.NEXT;
        }*/

        // percentage is #correct / #totalQuestions.... NOT #correct / #numAttempts
        // buuuuuut if they back out???


        double percentCorrect = (double) performance.getNumberCorrect() / (double) performance.getNumberAttempts();
        Log.d("PERCENT_CORRECT", "" + percentCorrect);

        boolean useLenientThresholds = isLenientActivity(performance.getActivityType());
        Log.i(TAG, (useLenientThresholds ? "U" : "Not u") + "sing lenient thresholds");;

        double high_threshold = useLenientThresholds ? HIGH_LENIENT_PERFORMANCE_THRESHOLD: HIGH_PERFORMANCE_THRESHOLD;
        double mid_threshold = useLenientThresholds ? MID_LENIENT_PERFORMANCE_THRESHOLD: MID_PERFORMANCE_THRESHOLD;
        double low_threshold = useLenientThresholds ? LOW_LENIENT_PERFORMANCE_THRESHOLD: LOW_PERFORMANCE_THRESHOLD;
        RoboTutor.logManager.postEvent_I(TAG, (useLenientThresholds ? "U" : "Not u") + "sing lenient thresholds -- ");;
        RoboTutor.logManager.postEvent_I(TAG, "high_threshold " + high_threshold);;
        RoboTutor.logManager.postEvent_I(TAG, "mid_threshold " + mid_threshold);;
        RoboTutor.logManager.postEvent_I(TAG, "low_threshold " + low_threshold);;

        if (percentCorrect >= high_threshold) {
            // 50/50 probability
            return Math.random() > 0.5 ? PromotionDecision.NEXT : PromotionDecision.DOUBLE_NEXT;
        } else if (percentCorrect >= mid_threshold) {
            // pass to next
            return PromotionDecision.NEXT;
        } else if (percentCorrect >= low_threshold) {
            // repeat
            return PromotionDecision.SAME;
        } else {
            // drop down to lower level
            return PromotionDecision.PREVIOUS;
        }
    }

    /**
     * if it's a math or writing activity, be slightly more lenient
     * @param activityType
     * @return
     */
    private boolean isLenientActivity(String activityType) {

        return activityType.startsWith("math")
                || activityType.startsWith("write");

    }
}
