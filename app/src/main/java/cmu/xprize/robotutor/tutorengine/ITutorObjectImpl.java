package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

public interface ITutorObjectImpl extends ITutorObject {

    public void init(Context context, AttributeSet attrs);

    public CObjectDelegate getimpl();

    public void zoomInOut(Float scale, Long duration);
}