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

import cmu.xprize.comp_ask.CAskComponent;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;

import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.ILogManager;


public class TAskComponent extends CAskComponent implements IBehaviorManager, ITutorObjectImpl, IDataSink, IEventSource {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    public TAskComponent(Context context) {
        super(context);
    }

    public TAskComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAskComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //**********************************************************
    //**********************************************************
    //*****************  ITutorObjectImpl Implementation

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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

    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition) {

    }

    @Override
    public void setAlpha(Float alpha) {

    }



    //************************************************************************
    //************************************************************************
    // IDataSink Interface START


    @Override
    public boolean dataExhausted() {
        return true;
    }


    @Override
    public void setDataSource(String dataSource) {

        int layoutID = getResources().getIdentifier("layout", "layout", RoboTutor.PACKAGE_NAME);

        _dataIndex = 0;
        _dataEOI   = false;
    }

    // IDataSink Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START


    @Override
    public void setVolatileBehavior(String event, String behavior) {

    }

    @Override
    public void setStickyBehavior(String event, String behavior) {

    }

    @Override
    public boolean applyBehavior(String event) {
        return false;
    }

    @Override
    public void applyBehaviorNode(String nodeName) {

    }


    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IEventSource Interface START

    @Override
    public String getEventSourceName() {
        return name();
    }

    @Override
    public String getEventSourceType() {
        return "Ask_Component";
    }

    // IEventSource Interface END
    //************************************************************************
    //************************************************************************

}
