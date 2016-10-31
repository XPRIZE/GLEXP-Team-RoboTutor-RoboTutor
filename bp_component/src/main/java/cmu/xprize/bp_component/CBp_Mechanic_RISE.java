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
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.TCONST;

public class CBp_Mechanic_RISE extends CBp_Mechanic_Base implements IBubbleMechanic {

    private int             _travelTime    = 4800;
    private int             stimNdx        = 0;

    private int             _prevColorNdx  = 0;
    private float           _prevXpos      = 0;

    static final String TAG = "CBp_Mechanic_RISE";


    public CBp_Mechanic_RISE(Context context, CBP_Component parent) {
        super.init(context, parent);
    }

    @Override
    protected void init(Context context, CBP_Component parent) {
        super.init(context, parent);
    }


    @Override
    public void onDraw(Canvas canvas) {

    }


    @Override
    public boolean isInitialized() {
        return mInitialized;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private boolean launchBubble() {

        CBubble nextBubble = null;
        boolean launched   = false;
        int     colorNdx;

        if (SBubbles != null) {

            // Find a bubble that is not currently on screen.
            //
            for (CBubble bubble : SBubbles) {
                if (!bubble.getOnScreen()) {
                    nextBubble = bubble;
                    nextBubble.setOnScreen(true);
                    break;
                }
            }

            // If there is a free bubble then set it up and launch it
            //
            if (nextBubble != null) {

                launched = true;

                do {
                    colorNdx = (int) (Math.random() * BP_CONST.bubbleColors.length);
                } while (colorNdx == _prevColorNdx);

                _prevColorNdx = colorNdx;

                String correctVal = mComponent._stimulus_data[_currData.dataset[_currData.stimulus_index]];

                nextBubble.setColor(BP_CONST.bubbleColors[colorNdx]);
                nextBubble.setScale(getRandInRange(_scaleRange));

                // Cycle on the indexes to display
                //
                stimNdx = (stimNdx + 1) % _currData.dataset.length;

                String stiumulusVal = mComponent._stimulus_data[_currData.dataset[stimNdx]];

                switch (mComponent.stimulus_type) {

                    case BP_CONST.REFERENCE:

                        int[] shapeSet = BP_CONST.drawableMap.get(stiumulusVal);

                        nextBubble.configData(stiumulusVal, correctVal);
                        nextBubble.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null);
                        break;

                    case BP_CONST.TEXTDATA:

                        nextBubble.configData(stiumulusVal, correctVal);
                        nextBubble.setContents(0, stiumulusVal);
                        break;
                }

                float xRange[] = new float[]{0, mParent.getWidth() - (BP_CONST.BUBBLE_DESIGN_RADIUS * nextBubble.getAssignedScale())};
                float xPos;

                do {
                    xPos = getRandInRange(xRange);
                } while (Math.abs(xPos - _prevXpos) < nextBubble.getWidth());

                _prevXpos = xPos;

                nextBubble.setPosition((int) xPos, mParent.getHeight());
                nextBubble.setAlpha(1.0f);

                long timeOfFlight = (long) (_travelTime / nextBubble.getAssignedScale());

                PointF wayPoints[] = new PointF[1];
                PointF posFinal = new PointF();

                posFinal.x = nextBubble.getX();
                posFinal.y = -BP_CONST.BUBBLE_DESIGN_RADIUS * 2.5f * nextBubble.getAssignedScale();

                wayPoints[0] = posFinal;

                Log.d(TAG, "Time of Flight: " + timeOfFlight);
                Log.d(TAG, "Final YPos: " + posFinal.y);

                Animator translator = CAnimatorUtil.configTranslate(nextBubble, timeOfFlight, 0, wayPoints);

                translator.addListener(new Animator.AnimatorListener() {
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

                        CBubble bubble = translators.get(animation);
                        translators.remove(animation);

                        bubble.setOnScreen(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                setupWiggle(nextBubble, 0);
                nextBubble.setOnClickListener(CBp_Mechanic_RISE.this);

                translators.put(translator, nextBubble);
                translator.start();
            }
        }

        return launched;
    }


    protected void setupWiggle(View target, long delay) {

        float[] wayPoints   = new float[]{target.getScaleY() * BP_CONST.STRETCH_MIN,
                                          target.getScaleY() * BP_CONST.STRETCH_MAX,
                                          target.getScaleY() * BP_CONST.STRETCH_MIN};

        AnimatorSet stretch = CAnimatorUtil.configStretch(target, "vertical", 2100, ValueAnimator.INFINITE, delay, wayPoints );

        stretch.start();
    }



    public void execCommand(String command, Object target ) {

        CBubble bubble;
        long    delay = 0;

        super.execCommand(command, target);

        switch(command) {

            case BP_CONST.SHOW_BUBBLES:

                if (!_isRunning && mInitialized) {

                    _isRunning = true;
                    mComponent.post(BP_CONST.SPAWN_BUBBLE);
                }
                break;

            case BP_CONST.PAUSE_ANIMATION:

                if (_isRunning) {
                    for(Animator animation : translators.keySet()) {
                        animation.pause();
                    }

                    _isRunning = false;
                }
                break;

            case BP_CONST.RESUME_ANIMATION:

                if (!_isRunning) {
                    for(Animator animation : translators.keySet()) {
                        animation.resume();
                    }

                    _isRunning = true;
                    mComponent.post(BP_CONST.SPAWN_BUBBLE);
                }
                break;

            case BP_CONST.SPAWN_BUBBLE:

                if(_isRunning) {

                    if (launchBubble()) {

                        int[] launchRange = {_travelTime / mComponent.countRange[BP_CONST.MAX], _travelTime / mComponent.countRange[BP_CONST.MIN]};

                        delay = getRandInRange(launchRange);

                        mComponent.post(BP_CONST.SPAWN_BUBBLE, delay);
                    } else {
                        mComponent.post(BP_CONST.SPAWN_BUBBLE);
                    }
                }
                break;

            case BP_CONST.POP_BUBBLE:

                bubble = (CBubble)target;
                delay  = bubble.pop();

                // stop listening to the bubble
                bubble.setOnClickListener(null);

                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(bubble.getCenterPosition()));

                mComponent.post(BP_CONST.REPLACE_BUBBLE, bubble, delay);
                break;

        }
    }


    @Override
    public void populateView(CBp_Data data) {

        CBubble newBubble;

        // Check if the dataset needs to be generated
        //
        generateRandomData(data);

        _currData = data;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        SBubbles = new CBubble[mComponent.countRange[BP_CONST.MAX]];

        for (int i1 = 0; i1 < mComponent.countRange[BP_CONST.MAX]; i1++) {

            newBubble = (CBubble) View.inflate(mContext, R.layout.bubble_view, null);
            newBubble.setAlpha(0);

            SBubbles[i1] = newBubble;

            mParent.addView(newBubble, layoutParams);
        }
        mInitialized = true;

    }


    @Override
    public void doLayout(int width, int height, CBp_Data data) {}


}
