
package cmu.xprize.bp_component;

public interface IBubbleMechanic {

    public boolean isInitialized();
    public void onDestroy();

    public void startAnimation();
    public void populateView(CBp_Data data);
    public void doLayout(int width, int height, CBp_Data data);

}
