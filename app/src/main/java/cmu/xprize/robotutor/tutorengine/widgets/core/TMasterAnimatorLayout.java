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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewAnimator;

import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.comp_logging.ILogManager;

public class TMasterAnimatorLayout extends ViewAnimator implements ITutorManager {

    protected Context        mContext;
    protected CSceneDelegate mTutorScene;
    protected int            insertNdx;

    protected Animation      fade_in, fade_out, slide_in_left, slide_out_right;


    final private String       TAG       = "TMasterAnimatorLayout";


    public TMasterAnimatorLayout(Context context) {
        super(context);
        init(context, null);
    }

    public TMasterAnimatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
        mContext    = context;

        fade_in  = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
        fade_out = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);

        setInAnimation(fade_in);
        setOutAnimation(fade_out);
    }

    @Override
    public void onDestroy() {
        mTutorScene.onDestroy();
    }


    @Override
    public void addView(ITutorSceneImpl newView) {
        addView((View) newView);
    }


    @Override
    public void addView(View newView) {

        insertNdx = indexOfChild((View)newView);

        if(insertNdx == -1) {
            insertNdx = super.getChildCount();
            super.addView((View) newView, insertNdx);
        }

        Log.d(TAG, "ADD > Child Count: " + getChildCount() );
    }


    @Override
    public void addAndShow(ITutorSceneImpl newView) {
        addAndShow((View) newView);
    }


    @Override
    public void addAndShow(View newView) {

        addView(newView);
        super.setDisplayedChild(insertNdx);
    }


    @Override
    public void removeView(ITutorSceneImpl delView) {

        removeView((View) delView);
    }


    @Override
    public void removeView(View delView) {

        super.removeView((View) delView);

        // GRAY_SCREEN_BUG
        Log.d(TAG, "REMOVE > Child Count: " + getChildCount() );
    }


    @Override
    public void setAnimationListener(Animation.AnimationListener callback) {

        fade_in.setAnimationListener(callback);
    }


    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    @Override
    public void setVisibility(String visible) {

        mTutorScene.setVisibility(visible);
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************


    @Override
    public void setName(String name) {
        mTutorScene.setName(name);
    }

    @Override
    public String name() {
        return mTutorScene.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mTutorScene.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mTutorScene.setTutor(tutor);
    }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mTutorScene.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mTutorScene.setLogManager(logManager);
    }


    @Override
    public CSceneDelegate getimpl() {
        return mTutorScene;
    }


    @Override
    public ViewGroup getOwner() {
        return mTutorScene.getOwner();
    }

    @Override
    public String preEnterScene(scene_descriptor scene, String Direction) {
        return mTutorScene.preEnterScene(scene, Direction);
    }

    @Override
    public void onEnterScene() {
        mTutorScene.onEnterScene();
    }

    @Override
    public String preExitScene(String Direction, int sceneCurr) {
        return mTutorScene.preExitScene(Direction, sceneCurr);
    }

    @Override
    public void onExitScene() {
        mTutorScene.onExitScene();
    }
}
