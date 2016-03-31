package cmu.xprize.robotutor.tutorengine;

import android.view.View;

/**
 * Created by Kevin on 3/31/2016.
 */
public interface ITutorSceneAnimator extends ITutorSceneImpl {

    public void addView(View newView);
    public void addView(View newView, int index);
    public void setDisplayedChild(int index);
    public void removeAllViews();

}
