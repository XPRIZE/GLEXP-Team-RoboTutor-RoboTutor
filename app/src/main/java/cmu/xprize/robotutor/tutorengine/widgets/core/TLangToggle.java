//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

        package cmu.xprize.robotutor.tutorengine.widgets.core;

        import android.content.Context;
        import android.util.AttributeSet;
        import android.view.View;
        import android.widget.ToggleButton;

        import cmu.xprize.robotutor.tutorengine.CMediaManager;
        import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
        import cmu.xprize.robotutor.tutorengine.CTutor;
        import cmu.xprize.robotutor.tutorengine.CTutorEngine;
        import cmu.xprize.robotutor.tutorengine.ITutorGraph;
        import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
        import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
        import cmu.xprize.util.ILogManager;
        import cmu.xprize.util.TCONST;

public class TLangToggle extends ToggleButton implements ITutorObjectImpl, View.OnClickListener {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager   mMediaManager;

    private Boolean         mLangState = false;


    public TLangToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TLangToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TLangToggle(Context context) {
        super(context);
        init(context, null);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        setOnClickListener(this);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onClick(View v) {
        mLangState    = !mLangState;
        mMediaManager = CMediaManager.getInstance();

        CTutorEngine.changeDefaultLanguage(mLangState? TCONST.LANG_EN: TCONST.LANG_SW);
    }

    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

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
    }

    // Do deferred configuration - anything that cannot be done until after the
    // view has been inflated and init'd - where it is connected to the TutorEngine
    //
    @Override
    public void postInflate() {

        mLangState = (mMediaManager.getLanguageFeature(mTutor) == TCONST.LANG_EN);
        setChecked(mLangState);
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
    public void setAlpha(Float alpha) {
        mSceneObject.setAlpha(alpha);
    }

}