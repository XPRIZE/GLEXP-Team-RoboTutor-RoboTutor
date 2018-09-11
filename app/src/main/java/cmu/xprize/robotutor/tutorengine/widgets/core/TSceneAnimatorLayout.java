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
        import android.view.animation.AnimationUtils;

        import cmu.xprize.robotutor.R;
        import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
        import cmu.xprize.robotutor.tutorengine.ITutorManager;

public class TSceneAnimatorLayout extends TMasterAnimatorLayout implements ITutorManager {

    final private String       TAG       = "TSceneAnimatorLayout";


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

        fade_in  = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
        fade_out = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);

        setInAnimation(fade_in);
        setOutAnimation(fade_out);
    }



    @Override
    public void setVisibility(String visible) {

        mTutorScene.setVisibility(visible);
    }

}
