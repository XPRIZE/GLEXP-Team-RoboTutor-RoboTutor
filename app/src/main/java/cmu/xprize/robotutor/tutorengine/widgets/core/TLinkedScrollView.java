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
import android.view.View;

import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.comp_logging.ILogManager;

public class TLinkedScrollView  extends CLinkedScrollView implements ITutorObject, View.OnTouchListener{

    private CObjectDelegate mSceneObject;


    final private String TAG = "LinkScrollView";


    public TLinkedScrollView(Context context) {
        super(context);
    }

    public TLinkedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TLinkedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        super.init(context, attrs);
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    // this is literally the only use of this method
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }

    //************************************************************************
    //************************************************************************
    // Tutor methods  Start

    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId) {

    }


    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId, String subset) {

    }


    /**
     * Enable or Disable the finger writer
     * @param enableState
     */
    public void enable(Boolean enableState) {

    }


    /**
     * Enable or disable persona messages.  Whether or not the persona will
     * watch finger motion
     *
     * @param watchEnabled
     */
    public void personaWatch(Boolean watchEnabled) {

//        _watchable = watchEnabled;
    }


    public void onStartWriting(String symbol) {
//        _onStartWriting = symbol;
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    protected void applyEventNode(String nodeName) {
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************




    /**
     * Deprecated - in favor of onRecognitionEvent
     *
     * @param symbol
     */
    public void onRecognitionComplete(String symbol) {
        onRecognitionEvent(symbol);
    }


    public void onRecognitionEvent(String symbol) {    }



    public void setDataSource(String dataSource) {
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

}
