package cmu.xprize.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 10/7/18.
 */

public interface IPerformanceTracker {

    void trackAndLogPerformance(boolean correct, Object expected, Object actual);
}
