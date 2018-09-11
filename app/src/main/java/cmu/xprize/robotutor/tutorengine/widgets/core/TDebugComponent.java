package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.comp_logging.ILogManager;

public class TDebugComponent extends CDebugComponent implements ITutorObject {

    private CTutor              mTutor;
    private CObjectDelegate     mSceneObject;
    private CMediaManager       mMediaManager;


    final private String TAG = "TDebugLayout";

    public TDebugComponent(Context context) {
        super(context);
    }

    public TDebugComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TDebugComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        super.init(context, attrs);
    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    @Override
    public String getLanguage() {

        return mMediaManager.getLanguageIANA_2(mTutor);
    }



    // Tutor methods  End
    //************************************************************************
    //************************************************************************


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
        mTutor = tutor;
        mSceneObject.setTutor(tutor);

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

}
