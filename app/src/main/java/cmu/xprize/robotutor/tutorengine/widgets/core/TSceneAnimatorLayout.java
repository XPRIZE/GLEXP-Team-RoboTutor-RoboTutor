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
        import android.view.ViewGroup;
        import android.view.animation.Animation;
        import android.view.animation.AnimationUtils;
        import android.widget.ViewAnimator;

        import java.util.ArrayList;

        import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
        import cmu.xprize.robotutor.tutorengine.CTutor;
        import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
        import cmu.xprize.robotutor.tutorengine.ITutorNavListener;
        import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
        import cmu.xprize.robotutor.tutorengine.ITutorManager;
        import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
        import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;

public class TSceneAnimatorLayout extends ViewAnimator implements ITutorManager {

    private Context        mContext;
    private CSceneDelegate mTutorScene;
    private int            mTutorCount = 0;
    private Animation      slide_in_left, slide_out_right;

    private ArrayList<ITutorSceneImpl>  stack = new ArrayList<>();


    public TSceneAnimatorLayout(Context context) {
        super(context);
        init(context, null);
    }

    public TSceneAnimatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
        mContext    = context;

        slide_in_left = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);

        setInAnimation(slide_in_left);
    }

    @Override
    public void onDestroy() {
        mTutorScene.onDestroy();
    }



    @Override
    public void addView(View newView) {
        super.addView(newView);
    }


    @Override
    public void addView(ITutorSceneImpl newView) {

        int insertNdx = super.getChildCount();
        super.addView((View) newView, insertNdx);
        super.setDisplayedChild(insertNdx);
    }


    @Override
    public void setAnimationListener(Animation.AnimationListener callback) {

        slide_in_left.setAnimationListener(callback);
    }


    @Override
    public void pushView(boolean push) {

        View child = getChildAt(0);

        if(push) {
            stack.add(0, (ITutorSceneImpl)child);
            mTutorCount++;
        }

        // Don't automatically pop the only tutor
//        if(mTutorCount > 1)
//            super.removeView(child);
    }


    @Override
    public void popView(boolean push, Animation.AnimationListener callback) {

        ITutorSceneImpl scene = stack.remove(0);

        int index = indexOfChild((View)scene);

        super.setDisplayedChild(index);
    }


    @Override
    public void addView(View newView, int index) {
        super.addView(newView, index);
    }

    @Override
    public void removeView(View delView) {
        super.removeView(delView);
    }


    @Override
    public void removeAllViews() {
        super.removeAllViews();
    }


    public void setDisplayedScene(int index) {
        setDisplayedChild(index);
    }


    //************************************************************************
    //************************************************************************
    // Tutor methods  Start



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
    public void setNavigator(ITutorNavigator navigator) {
        mTutorScene.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {
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
