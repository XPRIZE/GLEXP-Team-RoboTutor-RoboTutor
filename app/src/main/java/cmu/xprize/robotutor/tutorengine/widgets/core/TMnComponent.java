package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.mn_component.CMn_Component;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TMnComponent extends CMn_Component  implements ITutorObjectImpl {

    public TMnComponent(Context context) {
        super(context);
        init(context, null);
    }

    public TMnComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TMnComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {

    }

    @Override
    public void setTutor(CTutor tutor) {

    }

    @Override
    public void setNavigator(ITutorNavigator navigator) {

    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {

    }

    @Override
    public CTutorObjectDelegate getimpl() {
        return null;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {

    }
}
