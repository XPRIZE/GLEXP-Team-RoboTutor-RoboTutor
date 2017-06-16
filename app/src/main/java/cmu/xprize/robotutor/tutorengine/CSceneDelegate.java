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

package cmu.xprize.robotutor.tutorengine;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;

/**
 * All ITutorScene's use an instance of this to drive scene functionality
 *
 */
public class CSceneDelegate implements ITutorScene {

    private ViewGroup           mOwnerViewGroup;

    private String              mTutorId;
    private String              mInstanceId;
    private Context             mContext;

    protected ITutorScene       mParent;
    protected CTutor            mTutor;
    protected CSceneGraph       mAnimator;
    protected ITutorGraph       mNavigator;
    protected ILogManager       mLogManager;


//    public var audioStartTimer:CWOZTimerProxy;
//
//    public static const DEFAULT_MONITOR_INTERVAL:Number = 3000;
//
//    protected var _handler:Timer;
//    protected var _interval:Number = DEFAULT_MONITOR_INTERVAL;


    //## Mod aug 22 2013 - KT updates are single shot per scene

    protected boolean ktUpdated = false;

    // We support 3 types of scene drivers
    // ActionTracks    - simple instances of CActionTrack object with audio/events
    // ActionSequences - sequences of CActionTracks
    // AnimationGraphs = full CSceneGraph support for complex sequences

    private String	seqID;
    private List    seqTrack;
    private int     seqIndex;

    private CSceneGraph animationGraph;


    // Attach the View to this functionality
    public CSceneDelegate(ViewGroup owner) {
        mOwnerViewGroup = owner;
    }

    public ViewGroup getOwner() {return mOwnerViewGroup;}

    final private String TAG = "CSceneDelegate";

    //*** ITutorObject implementation

    @Override
    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        // If this is called prior to inflation then the ID will not be valid so ignore
        // and it must be explicitly set later.
        //
        try {
            int id = mOwnerViewGroup.getId();

            if(id != View.NO_ID)
                mTutorId = context.getResources().getResourceEntryName(id);
        }
        catch(Exception e) {
            Log.w(TAG, "Warning: Unnamed Delegate" + e);
        }

    }

    @Override
    public void onDestroy() {

    }


    @Override
    public void setVisibility(String visible) {

        int visibleFlag;

        switch(visible) {

            case TCONST.VISIBLE:
                visibleFlag = View.VISIBLE;
                break;

            case TCONST.INVISIBLE:
                visibleFlag = View.INVISIBLE;
                break;

            default:
                visibleFlag = View.GONE;
                break;

        }

        mOwnerViewGroup.setVisibility(visibleFlag);
    }

    @Override
    public void setName(String name) { mTutorId = name; }

    @Override
    public String name() { return mTutorId; }

    @Override
    public void setParent(ITutorSceneImpl parent) { mParent = parent; }

    @Override
    public void setTutor(CTutor tutor) { mTutor = tutor; }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) { mNavigator = navigator; }

    @Override
    public void setLogManager(ILogManager logManager) { mLogManager = logManager; }


    //*** ITutorScene Implementation

    @Override
    public String preEnterScene(scene_descriptor scene, String Direction) {
        // By default return the same direction requested
        return Direction;
    }

    @Override
    public void onEnterScene() {

        // Create a unique timestamp for this scene

//            gTutor.timeStamp.createLogAttr("dur_"+name, true);
    }

    @Override
    public String preExitScene(String Direction, int sceneCurr) {

        return TCONST.OKNAV;
    }

    @Override
    public void onExitScene() {

    }

}
