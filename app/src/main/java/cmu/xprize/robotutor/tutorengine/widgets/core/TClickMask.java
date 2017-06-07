package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.comp_clickmask.CClickMask;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TClickMask extends CClickMask implements ITutorObjectImpl {


    private CObjectDelegate mSceneObject;


    private String TAG = "TBanner";

    public TClickMask(Context context) {
        super(context);
        init(context, null);
    }

    public TClickMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TClickMask(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    //************************************************************************
    //************************************************************************
    // ITutorObjectImpl  Start

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
    public void onCreate() {}

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }

    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition ) {
        mSceneObject.wiggle(direction, magnitude, duration, repetition);
    }

    @Override
    public void setAlpha(Float alpha) {
        mSceneObject.setAlpha(alpha);
    }

    // ITutorObjectImpl  End
    //************************************************************************
    //************************************************************************
}
