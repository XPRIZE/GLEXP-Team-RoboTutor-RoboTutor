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
import android.widget.Button;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.comp_logging.ILogManager;

public class TButton extends Button implements ITutorObject {

    private CObjectDelegate mSceneObject;

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
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }



    public void setDataSource(String dataSource) {

    }


    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    //** Special Object methods for missing method parameter type combinations -
    //   e.g. This is the simplest way instead of using CharSequence

    public void setText(String text) {
        super.setText(text);
    }


    //** Custom Object Methods

    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************



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

    // ITutorObjectImpl  End
    //************************************************************************
    //************************************************************************

}
