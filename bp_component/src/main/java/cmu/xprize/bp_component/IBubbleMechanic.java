
package cmu.xprize.bp_component;

import android.graphics.Canvas;

public interface IBubbleMechanic {

    public void onDraw(Canvas canvas);
    public boolean isInitialized();
    public void onDestroy();

    public void populateView(CBp_Data data);
    public void doLayout(int width, int height, CBp_Data data);

    public void post(String command);
    public void post(String command, long delay);
    public void post(String command, Object target);
    public void post(String command, Object target, long delay);

}
