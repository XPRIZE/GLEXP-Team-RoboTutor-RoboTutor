package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TButton extends Button implements ITutorObjectImpl {

    private CTutorObjectDelegate mSceneObject;

    final private String TAG = "TButton";


    public TButton(Context context) {
        super(context);

        init(context, null);
    }


    public TButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }


    public TButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CTutorObjectDelegate(this);
        mSceneObject.init(context, attrs);

    }

    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void setNavigator(ITutorNavigator navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

    @Override
    public CTutorObjectDelegate getimpl() {
        return mSceneObject;
    }


    //** Special Object methods for missing method parameter type combinations -
    //   e.g. This is the simplest way instead of using CharSequence

    public void setText(String text) {
        super.setText(text);
    }


    //** Custom Object Methods

    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }

}
