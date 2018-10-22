//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
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
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

public class TLangToggle extends ToggleButton implements ITutorObject, View.OnClickListener {

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

        // if the LangToggle button remains showing for some reason, and is clicked, don't actually change the language.
        if (!Configuration.languageOverride(getContext())) {
            mLangState    = !mLangState;

            Log.v(QGRAPH_MSG, "event.click: " + " TLangToggle: " + mLangState);

            CTutorEngine.setDefaultLanguage(mLangState? TCONST.LANG_EN: TCONST.LANG_SW);
        } else {
            Log.v(QGRAPH_MSG, "event.click: " + " TLangToggle: IGNORING");
        }

        // #Mod 329 language switch capability
        // When switching languages it may occur on any Selector screens so we want to
        // reload the tutor selector to ensure the internal state is consistent
        //
        RoboTutor.SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT;

        // We need to reload the session manager to reflect the new language specific datasource
        //
        CTutorEngine.destroyCurrentTutor();
    }

    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
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
        mTutor = tutor;
        mSceneObject.setTutor(tutor);

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    // Do deferred configuration - anything that cannot be done until after the
    // view has been inflated and init'd - where it is connected to the TutorEngine
    //
    @Override
    public void onCreate() {

        mLangState = (mMediaManager.getLanguageFeature(mTutor).equals(TCONST.LANG_EN));
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

}