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

package cmu.xprize.robotutor.startup;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_pointtap.CHandAnimation;
import cmu.xprize.comp_pointtap.HA_CONST;
import cmu.xprize.robotutor.R;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IRoboTutor;


public class CStartView extends FrameLayout {

    private Context      mContext;

    private ImageButton  start;
    private IRoboTutor   callback;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HashMap       queueMap    = new HashMap();
    private boolean       _qDisabled  = false;


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
    }


    public void setCallback(IRoboTutor _callback) {

        callback = _callback;

        start = (ImageButton)findViewById(R.id.SstartSelector);

        // Allow hits anywhere on screen
        //
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callback.onStartTutor();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callback.onStartTutor();
            }
        });
    }



    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch(command) {

            case HA_CONST.ANIMATE_REPEAT:

                CHandAnimation hand = (CHandAnimation) findViewById(R.id.ShandAnimator);

                PointF targetPoint = new PointF(getWidth() / 5, getHeight() /5);

                hand.post(HA_CONST.ANIMATE_MOVE, targetPoint);

                post(HA_CONST.ANIMATE_REPEAT, HA_CONST.TUTOR_RATE);
                break;

        }
    }


    public void startTapTutor() {

        post(HA_CONST.ANIMATE_REPEAT, 1000);
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
