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

package cmu.xprize.bp_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.TCONST;

public class CBp_Mechanic_Base implements IBubbleMechanic, View.OnTouchListener, View.OnClickListener {

    protected Context                     mContext;
    protected CBP_Component               mComponent;
    protected CBP_LetterBoxLayout         mParent;
    protected boolean                     mInitialized = false;

    protected boolean                     _isRunning   = false;

    private final Handler                 mainHandler = new Handler(Looper.getMainLooper());
    private HashMap                       queueMap    = new HashMap();
    protected boolean                     _enabled    = true;
    private boolean                       _qDisabled  = false;

    private boolean                       _watchable    = true;
    private int[]                         _screenCoord  = new int[2];
    private long                          _time;
    private long                          _prevTime;

    private LocalBroadcastManager         bManager;

    protected float                       _alpha       = 0.80f;
    protected float[]                     _scaleRange  = {0.85f, 1.3f};

    protected CBp_Data                    _currData;

    protected CBubble[]                   SBubbles;
    protected CBubbleStimulus             SbubbleStumulus;

    protected HashMap<Animator, CBubble>  inflators   = new HashMap<Animator, CBubble>();
    protected HashMap<Animator, CBubble>  translators = new HashMap<Animator, CBubble>();


    static final String TAG = "CBp_Mechanic_Base";



    protected void init(Context context, CBP_Component parent) {

        mContext   = context;
        mComponent = parent;
        mParent    = parent.getContainer();

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(mContext);
        mParent.setOnTouchListener(this);
    }

    @Override
    public void onDestroy() {

        _isRunning = false;

        terminateQueue();

        if(SBubbles != null) {

            for (int i1 = 0; i1 < SBubbles.length; i1++) {
                if (SBubbles[i1] != null) {
                    mParent.removeView((View) SBubbles[i1]);
                    SBubbles[i1].onDestroy();
                    SBubbles[i1] = null;
                }
            }
            if (SbubbleStumulus != null)
                mParent.removeView((View) SbubbleStumulus);

            SBubbles = null;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void populateView(CBp_Data data) {

    }
    @Override
    public void doLayout(int width, int height, CBp_Data data) {

    }


    public void removeBubble(CBubble bubble) {

        mParent.removeView(bubble);

        for (int i1 = 0; i1 < SBubbles.length; i1++) {

            if (SBubbles[i1] == bubble) {
                SBubbles[i1] = null;
                break;
            }
        }
    }


    public void replaceBubble(CBubble bubble) {

        CBubble newBubble;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mParent.removeView(bubble);

        newBubble = (CBubble) View.inflate(mContext, R.layout.bubble_view, null);
        newBubble.setAlpha(0);

        for (int i1 = 0; i1 < SBubbles.length; i1++) {

            if (SBubbles[i1] == bubble) {
                SBubbles[i1] = newBubble;

                mParent.addView(newBubble, layoutParams);
                break;
            }
        }

        mParent.bringChildToFront(SbubbleStumulus);
    }


    private void showStimulus(CBp_Data data) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        SbubbleStumulus = (CBubbleStimulus) View.inflate(mContext, R.layout.bubble_stimulus, null);

        // Set Color: pass in String e.g. "RED" - Cycle through the colors repetitively
        //
        SbubbleStumulus.setScale(0);
        SbubbleStumulus.setAlpha(1.0f);

        switch (data.stimulus_type) {

            case BP_CONST.REFERENCE:

                int[] shapeSet = BP_CONST.drawableMap.get(mComponent.stimulus_data[data.dataset[data.stimulus_index]]);

                Drawable qDrawable = mParent.getResources().getDrawable(shapeSet[0], null);

                SbubbleStumulus.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null);
                break;

            case BP_CONST.TEXTDATA:
                SbubbleStumulus.setContents(0, mComponent.stimulus_data[data.dataset[data.stimulus_index]]);
                break;
        }

        mParent.addView(SbubbleStumulus, layoutParams);
    }


    protected void execCommand(String command, Object target ) {

        CBubble bubble;
        long    delay = 0;

        switch(command) {

            case BP_CONST.WIGGLE_BUBBLE:

                bubble = (CBubble)target;

                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(bubble.getCenterPosition()));

                Animator wiggler = CAnimatorUtil.configWiggle(bubble, "horizontal",70, 5, 0, 0.10f );

                wiggler.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationStart(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        mComponent.applyEvent(BP_CONST.BUBBLE_WIGGLED);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                wiggler.start();

                break;

            case BP_CONST.REMOVE_BUBBLE:

                bubble = (CBubble)target;

                removeBubble(bubble);

                mComponent.applyEvent(BP_CONST.BUBBLE_POPPED);
                break;

            case BP_CONST.REPLACE_BUBBLE:

                bubble = (CBubble)target;

                replaceBubble(bubble);

                mComponent.applyEvent(BP_CONST.BUBBLE_POPPED);
                break;

            case BP_CONST.SHOW_STIMULUS:

                // This is a 2 step process - first the stimulus is zoomed then moved to the bottom
                // of the screen.
                //
                showStimulus((CBp_Data) target);
                post(BP_CONST.ZOOM_STIMULUS);
                break;

            case BP_CONST.ZOOM_STIMULUS:

                // Persona - look at the stimulus
                Log.d(TAG, "Width: " + mParent.getWidth() + " - HEIGHT: " + mParent.getHeight());

                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(new PointF(mParent.getWidth() / 2, mParent.getHeight() / 2)));

                SbubbleStumulus.setX((mParent.getWidth() - SbubbleStumulus.getWidth()) / 2);
                SbubbleStumulus.setY((mParent.getHeight() - SbubbleStumulus.getHeight()) / 2);

                Animator inflator = CAnimatorUtil.configZoomIn(SbubbleStumulus, 600, 0, new BounceInterpolator(), 0f, 3.0f);

                inflator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationStart(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        post(BP_CONST.MOVE_STIMULUS, 400);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflator.start();

                break;

            case BP_CONST.MOVE_STIMULUS:

                float[] scale = new float[]{(BP_CONST.MARGIN_BOTTOM * .9f) / SbubbleStumulus.getHeight()};

                float height       = SbubbleStumulus.getHeight();
                float scaledHeight = height * scale[0];

                PointF wayPoints[] = new PointF[1];
                PointF posFinal    = new PointF();

                posFinal.x = SbubbleStumulus.getX();
                posFinal.y = mParent.getHeight() - (scaledHeight + ((height - scaledHeight) / 2) + BP_CONST.STIM_PAD_BOTTOM);

                wayPoints[0] = posFinal;

                AnimatorSet inflator2  = CAnimatorUtil.configZoomIn(SbubbleStumulus, 300, 0, new LinearInterpolator(), scale);
                Animator    translator = CAnimatorUtil.configTranslate(SbubbleStumulus, 300, 0, wayPoints);

                inflator2.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationStart(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        mComponent.applyEvent(BP_CONST.STIMULUS_SHOWN);
                        //post(BP_CONST.SHOW_BUBBLES);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflator2.start();
                translator.start();
                break;

            case BP_CONST.CLEAR_CONTENT:

                AnimatorSet deflator           = new AnimatorSet();
                ArrayList<Animator> animations = new ArrayList<Animator>();

                for(int i1 = 0; i1 < SBubbles.length ; i1++) {
                    if(SBubbles[i1] != null)
                        animations.add(CAnimatorUtil.configZoomIn(SBubbles[i1], 600, 0, new AnticipateInterpolator(), 0f));
                }
                if(SbubbleStumulus != null)
                    animations.add(CAnimatorUtil.configZoomIn(SbubbleStumulus, 600, 0, new AnticipateInterpolator(), 0f));


                deflator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationStart(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        // If the bubble still exist.  If the tutor is terminated while this
                        // animation is running this may be null.
                        //
                        if (SBubbles != null) {

                            for (int i1 = 0; i1 < SBubbles.length; i1++) {
                                if (SBubbles[i1] != null) {
                                    mParent.removeView((View) SBubbles[i1]);
                                    SBubbles[i1] = null;
                                }
                            }
                            if (SbubbleStumulus != null)
                                mParent.removeView((View) SbubbleStumulus);

                            SBubbles = null;

                            mComponent.applyEvent(BP_CONST.BUBBLES_CLEARED);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                deflator.playTogether(animations);
                deflator.start();

                break;
        }

    }


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
    private void flushQueue() {

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


    @Override
    public void onClick(View view) {

        CBubble bubble = (CBubble)view;

        mComponent.publishState(bubble);
        mComponent.applyEvent(BP_CONST.BUBBLE_TOUCH_EVENT);
    }


    protected void broadcastLocation(String Action, PointF touchPt) {

        if(_watchable) {
            mParent.getLocationOnScreen(_screenCoord);

            // Let the persona know where to look
            Intent msg = new Intent(Action);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

            bManager.sendBroadcast(msg);
        }
    }


    /**
     * Add Root vector to path
     *
     * @param touchPt
     */
    private void startTouch(PointF touchPt) {

        broadcastLocation(TCONST.LOOKATSTART, touchPt);
    }


    /**
     * Update the glyph path if motion is greater than tolerance - remove jitter
     *
     * @param touchPt
     */
    private void moveTouch(PointF touchPt) {

       broadcastLocation(TCONST.LOOKAT, touchPt);
    }


    /**
     * End the current glyph path
     * TODO: Manage debouncing
     *
     */
    private void endTouch(PointF touchPt) {

        broadcastLocation(TCONST.LOOKATEND, touchPt);
    }


    public boolean onTouch(View view, MotionEvent event) {
        PointF touchPt;
        long   delta;
        final int action = event.getAction();

        // TODO: switch back to setting onTouchListener
        if(_enabled) {

            mParent.onTouchEvent(event);

            touchPt = new PointF(event.getX(), event.getY());

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    _prevTime = _time = System.nanoTime();
                    startTouch(touchPt);
                    break;
                case MotionEvent.ACTION_MOVE:
                    _time = System.nanoTime();
                    moveTouch(touchPt);
                    break;
                case MotionEvent.ACTION_UP:
                    _time = System.nanoTime();
                    endTouch(touchPt);
                    break;
            }
            delta = _time - _prevTime;
        }

        //Log.i(TAG, "Touch Time: " + _time + "  :  " + delta);
        return true;
    }


    public void generateRandomData(CBp_Data data) {

        int stimCount = (data.rand_data)? data.rand_size:data.dataset.length;
        int setSize   = mComponent.stimulus_data.length;

        // Constrain the presentation set size
        //
        if(stimCount > setSize)
            stimCount = setSize;

        // If the first element of the dataset is < 0 it indicates the number of random items
        // to add to the array
        //
        if(data.rand_data) {

            data.dataset = new int[stimCount];

            for(int i1 = 0 ; i1 < stimCount ; i1++) {
                data.dataset[i1] = (int) (Math.random() * setSize);
            }
        }

        // If requested (by -ve entry) - select which bubble is correct at random
        //
        if(data.rand_index)
            data.stimulus_index = (int) (Math.random() * data.dataset.length);

        // If we are using sequential presentations then we substitute the
        // current correct index in the "correct" i.e. stimulus bubble. To
        // ensure there is at least one correct answer.
        //
        if(mComponent.question_sequence.equals(BP_CONST.SEQUENTIAL)) {

            data.dataset[data.stimulus_index] = mComponent.question_Index++;

            // cycle on the stimulus_data
            //
            mComponent.question_Index %= setSize;
        }
    }


    /**
     *
     * @param valueRange
     * @return
     */
    protected float getRandInRange(float[] valueRange) {

        float range = valueRange[BP_CONST.MAX] - valueRange[BP_CONST.MIN];
        float rand  = valueRange[BP_CONST.MIN];

        // check if less than error tolerance.
        //
        if( range > 0.01) {
            rand = (float)(valueRange[BP_CONST.MIN] + Math.random() * range);
        }

        return rand;
    }


    /**
     *
     * @param valueRange
     * @return
     */
    protected int getRandInRange(int[] valueRange) {

        int range = valueRange[BP_CONST.MAX] - valueRange[BP_CONST.MIN];
        int rand  = valueRange[BP_CONST.MIN];

        // check if less than error tolerance.
        //
        if( range > 0) {
            rand = (int)(valueRange[BP_CONST.MIN] + Math.random() * range);
        }

        return rand;
    }


}
