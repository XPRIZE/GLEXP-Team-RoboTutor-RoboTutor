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
import android.widget.ImageButton;

import cmu.xprize.banner.CBanner;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.util.ILogManager;


public class TBanner extends CBanner implements ITutorObjectImpl, View.OnClickListener {

    private CObjectDelegate mSceneObject;
    private TTextView       mVersion;
    private ImageButton     mBackButton;


    public TBanner(Context context) {
        super(context);
        init(context, null);
    }

    public TBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        mVersion    = (TTextView)findViewById(R.id.StutorVersion);
        mBackButton = (ImageButton)findViewById(R.id.Sbackbutton);

        mBackButton.setOnClickListener(this);
    }


    /**
     * This is the main banner back button implementation - This terminates the current tutor and
     * returns to the session manager
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        mBackButton.setOnClickListener(null);
        mSceneObject.endTutor();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSceneObject.onDestroy();
    }


    public void setDataSource(String dataSource) {
    }


    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    public void setVersionID(String versionID) {
        mVersion.setText(versionID);
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
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void postInflate() {}

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
