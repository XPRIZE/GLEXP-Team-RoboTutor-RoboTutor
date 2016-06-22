package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

public interface ITutorObjectImpl extends ITutorObject {

    public void init(Context context, AttributeSet attrs);

    public CObjectDelegate getimpl();

    public void zoomInOut(Float scale, Long duration);

    public void wiggle(Float magnitude, Long duration, Integer repetition );

    public void setAlpha(Float alpha);
}