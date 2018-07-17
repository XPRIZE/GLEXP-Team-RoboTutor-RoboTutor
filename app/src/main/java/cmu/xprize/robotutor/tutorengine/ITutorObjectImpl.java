package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

public interface ITutorObjectImpl extends ITutorObject {

    // ND_CLEAN get rid of this
    public void init(Context context, AttributeSet attrs);

    // ND_CLEAN get rid of this
    public CObjectDelegate getimpl();

    // ND_CLEAN get rid of this
    public void zoomInOut(Float scale, Long duration);

    // ND_CLEAN get rid of this
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition );

    // ND_CLEAN get rid of this
    public void setAlpha(Float alpha);
}