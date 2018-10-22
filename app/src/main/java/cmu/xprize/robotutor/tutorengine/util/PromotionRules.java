package cmu.xprize.robotutor.tutorengine.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 1/10/18.
 */

public abstract class PromotionRules {

    // note that "NEXT" is a different meaning from "Next"... NEXT means sequential (i.e. L, T+1)
    public enum PromotionDecision {SAME, NEXT, PREVIOUS, DOUBLE_NEXT, OLD_EASIER, OLD_HARDER};

    public abstract PromotionDecision assessPerformance(PerformanceData performance);
}
