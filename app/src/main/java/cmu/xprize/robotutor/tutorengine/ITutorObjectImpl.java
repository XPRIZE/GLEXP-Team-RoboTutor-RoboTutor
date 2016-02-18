package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

public interface ITutorObjectImpl extends ITutorObject {

    void init(Context context, AttributeSet attrs);

    CTutorObjectDelegate getimpl();

    public void zoomInOut(Float scale, Long duration);
}