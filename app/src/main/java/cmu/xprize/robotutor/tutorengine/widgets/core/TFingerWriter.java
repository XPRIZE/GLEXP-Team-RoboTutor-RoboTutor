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

import cmu.xprize.fw_component.CFingerWriter;
import cmu.xprize.fw_component.ITextSink;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.IEventListener;
import cmu.xprize.comp_logging.ILogManager;

public class TFingerWriter extends CFingerWriter implements ITutorObject, IDataSink  {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private ITextSink       mLinkedView;

    private static final String   TAG = "TFingerWriter";



    public TFingerWriter(Context context) {
        super(context);
        init(context, null);
    }

    public TFingerWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TFingerWriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, null);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

    }


    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    @Override
    public void addEventListener(String linkedView) {

        mListeners.add((IEventListener) mTutor.getViewByName(linkedView));
    }

    public void setDataSource(String dataSource) {

    }



    //************************************************************************
    //************************************************************************
    // Tutor scriptiable methods  Start


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }


    public void setRecognizer(String recogId) {
        super.setRecognizer(recogId);
    }

    public void setRecognizer(String recogId, String subset) {
        super.setRecognizer(recogId, subset);
    }


    public void enable(Boolean enable) {
        enableFW(enable);
    }


    public void personaWatch(Boolean enable) {

        super.personaWatch(enable);
    }


    public void onStartWriting(String symbol) {
        super.onStartWriting(symbol);
    }


    protected void applyEventNode(String nodeName) {
        IScriptable2 obj = null;

        if(nodeName != null && !nodeName.equals("")) {
            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if(obj.testFeatures()) {
                    obj.applyNode();
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
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
    }

    // Do deferred configuration - anything that cannot be done until after the
    // view has been inflated and init'd - where it is connected to the TutorEngine
    //
    @Override
    public void onCreate() {

        // Do deferred listeners configuration - this cannot be done until after the
        //
        if(!mListenerConfigured) {
            for (String linkedView : mLinkedViews) {
                addEventListener(linkedView);
            }
            mListenerConfigured = true;
        }
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
