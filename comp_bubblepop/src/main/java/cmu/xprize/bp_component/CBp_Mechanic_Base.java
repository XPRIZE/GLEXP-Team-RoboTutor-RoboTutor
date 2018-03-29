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

package cmu.xprize.bp_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.SPEAK_BUTTON;
import static cmu.xprize.util.TCONST.SUBGRAPH;

public class CBp_Mechanic_Base implements IBubbleMechanic, View.OnTouchListener, View.OnClickListener {

    protected Context                     mContext;
    protected CBP_Component               mComponent;
    protected CBP_LetterBoxLayout         mParent;
    protected boolean                     mInitialized      = false;

    protected boolean                     _isRunning        = false;
    protected boolean                     _enableTouchEvent = false;

    private boolean                       _watchable        = true;
    private int[]                         _screenCoord      = new int[2];
    private long                          _time;
    private long                          _prevTime;
    protected boolean                     _enabled    = true;

    protected float                       _alpha       = 0.80f;
    protected float[]                     _scaleRange  = {0.85f, 1.3f};

    protected CBp_Data                    _currData;

    protected CBubble[]                   SBubbles;
    protected CBubbleStimulus             SbubbleStumulus;
    protected CBubbleStimulus             SfeedBack;

    protected HashMap<Animator, CBubble>  inflators   = new HashMap<Animator, CBubble>();
    protected HashMap<Animator, CBubble>  translators = new HashMap<Animator, CBubble>();

    private LocalBroadcastManager         bManager;

    private String          mProblemType;

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

                int[] shapeSet = BP_CONST.drawableMap.get(data.stimulus);

                Drawable qDrawable = mParent.getResources().getDrawable(shapeSet[0], null);

                SbubbleStumulus.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null, false);
                break;

            case BP_CONST.TEXTDATA:
                SbubbleStumulus.setContents(0, data.stimulus, false);
                break;
        }

        mParent.addView(SbubbleStumulus, layoutParams);
    }


    private void showFeedback(Integer correctCount) {

        SfeedBack = (CBubbleStimulus) mParent.findViewById(R.id.Sfeedback);

        if(SfeedBack == null) {

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(BP_CONST.FEEDBACK_SIZE, BP_CONST.FEEDBACK_SIZE);
            SfeedBack = (CBubbleStimulus) View.inflate(mContext, R.layout.bubble_stimulus, null);

            SfeedBack.setScale(0f);
            SfeedBack.setAlpha(1.0f);

            SfeedBack.setId(R.id.Sfeedback);

            mParent.addView(SfeedBack, layoutParams);
        }

        SfeedBack.setContents(0, correctCount.toString(), true);

        mParent.bringChildToFront(SfeedBack);
    }


    public void execCommand(String command, Object target ) {

        CBubble  bubble;
        Animator inflator;
        long     delay = 0;

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

                        mComponent.applyBehavior(BP_CONST.BUBBLE_WIGGLED);
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

                mComponent.applyBehavior(BP_CONST.BUBBLE_POPPED);
                break;

            case BP_CONST.REPLACE_BUBBLE:

                bubble = (CBubble)target;

                replaceBubble(bubble);

                mComponent.applyBehavior(BP_CONST.BUBBLE_POPPED);
                break;

            case BP_CONST.SHOW_STIMULUS:

                // This is a 2 step process - first the stimulus is zoomed then moved to the bottom
                // of the screen.
                //
                showStimulus((CBp_Data) target);
                mComponent.post(BP_CONST.ZOOM_STIMULUS);
                break;


            case BP_CONST.SHOW_SCORE:

                // We have to be sure that the feedback has been created even if they always
                // were in error - where no "SHOW_FEEDBACK" would be given
                showFeedback((Integer) target);

                SfeedBack.setX((mParent.getWidth() - SfeedBack.getWidth()) / 2);
                SfeedBack.setY((mParent.getHeight() - SfeedBack.getHeight()) / 2);

                inflator = CAnimatorUtil.configZoomIn(SfeedBack, 600, 0, new BounceInterpolator(), 0f, 3.0f);

                inflator.start();

                break;

            case BP_CONST.SHOW_FEEDBACK:

                showFeedback((Integer) target);
                mComponent.post(BP_CONST.ZOOM_FEEDBACK);
                break;

            case BP_CONST.ZOOM_FEEDBACK:

                Log.d(TAG, "Width: " + mParent.getWidth() + " - HEIGHT: " + mParent.getHeight());
                Log.d(TAG, "Width: " + SfeedBack.getWidth() + " - HEIGHT: " + SfeedBack.getHeight());

                SfeedBack.setX(mParent.getWidth() - SfeedBack.getWidth());
                SfeedBack.setY(0);

                inflator = CAnimatorUtil.configZoomIn(SfeedBack, 600, 0, new BounceInterpolator(), 0f, 1.0f);

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

                        mComponent.applyBehavior(BP_CONST.FEEDBACK_SHOWN);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflator.start();
                break;

            case BP_CONST.ZOOM_STIMULUS:

                // Persona - look at the stimulus
                Log.d(TAG, "Width: " + mParent.getWidth() + " - HEIGHT: " + mParent.getHeight());

                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(new PointF(mParent.getWidth() / 2, mParent.getHeight() / 2)));

                float height = SbubbleStumulus.getHeight();
                float width = SbubbleStumulus.getWidth();

                SbubbleStumulus.setX((mParent.getWidth() - width) / 2);
                SbubbleStumulus.setY((mParent.getHeight() - height) / 2);

                float scaleStimulusVal = 3.0f;
                if(width > 500) {
                    scaleStimulusVal = 2.5f;
                }
                if(width > 1000) {
                    scaleStimulusVal = 1.25f;
                }
                if(width > 2000) {
                    scaleStimulusVal = 1.0f;
                }
                inflator = CAnimatorUtil.configZoomIn(SbubbleStumulus, 600, 0, new BounceInterpolator(), 0f, scaleStimulusVal);

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
                        mComponent.post(BP_CONST.MOVE_STIMULUS, 400);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflator.start();

                break;

            case BP_CONST.MOVE_STIMULUS:

                height       = SbubbleStumulus.getHeight();

                float[] scale = new float[]{(BP_CONST.MARGIN_BOTTOM * .9f) / 381};
                float scaledHeight = height * scale[0];

                PointF wayPoints[] = new PointF[1];
                PointF posFinal    = new PointF();

                posFinal.x = SbubbleStumulus.getX();
                posFinal.y = mParent.getHeight() - (scaledHeight + ((height - scaledHeight) / 2) + BP_CONST.STIM_PAD_BOTTOM);
                wayPoints[0] = posFinal;
                SbubbleStumulus.setCenterPoint(new PointF(SbubbleStumulus.getX(), mParent.getHeight() - (height / 2)));

                AnimatorSet inflatorSet = CAnimatorUtil.configZoomIn(SbubbleStumulus, 300, 0, new LinearInterpolator(), scale);
                Animator    translator = CAnimatorUtil.configTranslate(SbubbleStumulus, 300, 0, wayPoints);


                inflatorSet.addListener(new Animator.AnimatorListener() {
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
                        mComponent.applyBehavior(BP_CONST.STIMULUS_SHOWN);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflatorSet.start();
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

                            mComponent.applyBehavior(BP_CONST.BUBBLES_CLEARED);
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


    @Override
    public void enableTouchEvents() {
        _enableTouchEvent = true;
    }


    @Override
    public void onClick(View view) {

        Log.v(QGRAPH_MSG, "event.click: " + " CBp_Mechanic_Base: bubble touch");

        CBubble bubble = (CBubble)view;

        if(_enableTouchEvent) {

            _enableTouchEvent = false;

            mComponent.publishState(bubble, SbubbleStumulus);
            mComponent.applyBehavior(BP_CONST.BUBBLE_TOUCH_EVENT);
        }
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

        int respSize;       // response set size - # to select from
        int ansIndex;

        if(data.gen_question) {

            if(data.respCountExact > 0) {
                respSize = data.respCountExact;
            }
            else {
                respSize = getRandInRange(data.respCountRange);
            }

            //***** build a presentation set from the response sample set
            //
            mComponent.preProcessQuestion();

            // Get the number of response samples available.
            //
            int setSize = mComponent.wrk_responseSet.size();

            // Constrain the presentation set size - in case there is an error
            // in the problem generator.
            //
            if(respSize > setSize)
                respSize = setSize;

            // If requested, select which bubble is correct at random
            // within the response set
            //
            if(data.answer_index < 0)
                ansIndex = (int) (Math.random() * respSize);
            else
                ansIndex = data.answer_index;

            //***** Select the question from the stimulus set
            // This populates the stimulus and answer fields of "data"
            //
            mComponent.selectQuestion(data);

            //***** Select respSize items from the response set
            // This populates the response fields of "data" with a selection from the
            // generator sets and ensures the answer is in the result set at the specified
            // ansIndex location.
            //
            mComponent.selectRandResponse(data, respSize, ansIndex);
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
