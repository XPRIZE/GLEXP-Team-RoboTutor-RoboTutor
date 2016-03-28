package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Kevin on 1/16/2016.
 */
public interface ITutorSceneImpl extends ITutorScene {

    void init(Context context, AttributeSet attrs);

    CSceneDelegate getimpl();
}