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

package cmu.xprize.robotutor.startup;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_pointtap.HA_CONST;
import cmu.xprize.robotutor.R;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;


public class CStartView extends FrameLayout {

    private Context      mContext;

    private ImageButton  start;
    private IRoboTutor   callback;
    private boolean      tutorEnable = false;

    private final Handler mainHandler  = new Handler(Looper.getMainLooper());
    private HashMap       queueMap     = new HashMap();
    private boolean       _qDisabled   = false;
    private int[]         _screenCoord = new int[2];

    private LocalBroadcastManager bManager;

    static final String TAG = "CStartView";


    public CStartView(Context context) {
        super(context);
        init(context, null);
    }

    public CStartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CStartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        mContext = context;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    public void setCallback(IRoboTutor _callback) {

        callback = _callback;

        start = (ImageButton)findViewById(R.id.SstartSelector);

        // Allow hits anywhere on screen
        //
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CStartView: background onStartTutor");

                callback.onStartTutor();
            }
        });
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CStartView: button onStartTutor");

                callback.onStartTutor();
            }
        });
    }



    private void broadcastLocation(String Action, View target) {

        target.getLocationOnScreen(_screenCoord);

        PointF centerPt = new PointF(_screenCoord[0] + (target.getWidth() / 2), _screenCoord[1] + (target.getHeight() / 2));
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{centerPt.x, (float) centerPt.y});

        bManager.sendBroadcast(msg);
    }


    protected void broadcastLocation(String Action, PointF touchPt) {

        getLocationOnScreen(_screenCoord);

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

        bManager.sendBroadcast(msg);
    }


    protected void cancelPointAt() {

        Intent msg = new Intent(TCONST.CANCEL_POINT);
        bManager.sendBroadcast(msg);
    }


    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch(command) {

            case HA_CONST.ANIMATE_REPEAT:

                if(tutorEnable) {
                    float tapRegionX = (getWidth() * 3 / 4);
                    float tapRegionY = (getHeight() * 3 / 4);

                    float padRegionX = (getWidth() / 8);
                    float padRegionY = (getHeight() / 8);

                    PointF targetPoint = new PointF((int) (Math.random() * tapRegionX) + padRegionX, (int) (Math.random() * tapRegionY) + padRegionY);

                    broadcastLocation(TCONST.POINT_AND_TAP, targetPoint);

                    post(HA_CONST.ANIMATE_REPEAT, HA_CONST.TUTOR_RATE);
                }
                break;
        }
    }


    public void startTapTutor() {

        tutorEnable = true;
        post(HA_CONST.ANIMATE_REPEAT, HA_CONST.INIT_RATE);
    }


    public void stopTapTutor() {

        tutorEnable = false;
        flushQueue();
        cancelPointAt();
    }


    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _command;
        protected Object _target;

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, Object target) {
            _command = command;
            _target  = target;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                execCommand(_command, _target);

            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    public void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, Object target) {
        post(command, target, 0);
    }
    public void post(String command, Object target, long delay) {

        enQueue(new Queue(command, target), delay);
    }


    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************




}
