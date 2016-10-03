package cmu.xprize.util;

import android.util.Log;

public interface IBehaviorManager {

    public void setVolatileBehavior(String event, String behavior);
    public void setStickyBehavior(String event, String behavior);
    public boolean applyBehavior(String event);
    public void applyBehaviorNode(String nodeName);

}
