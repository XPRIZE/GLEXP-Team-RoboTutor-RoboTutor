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
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;

import cmu.xprize.util.CAnimatorUtil;

public class CBp_Mechanic_MC extends CBp_Mechanic_Base implements IBubbleMechanic, View.OnClickListener {

    private Context         mContext;
    private CBP_Component   mParent;
    private boolean         mInitialized      = false;
    private boolean         mAnimationStarted = false;

    private CBubble[]       mBubbles;

    private float           _alpha      = 0.80f; //65f;
    private float[]         _scaleRange = {1.0f, 1.3f};
    private float[]         _scales;

    private float           _angleStart = 0;
    private float[]         _angleRange = {0, (float)(Math.PI/4.0)};
    private float           _angleInc;
    private float[]         _rays;
    private Point           _padding = new Point(300, 200);
    private Point           _screenCenter = new Point();

    private HashMap<Animator, CBubble>  inflators = new HashMap<Animator, CBubble>();
    AnimationDrawable       popping;


    static final String TAG = "CBp_Mechanic_MC";


    public CBp_Mechanic_MC(Context context, CBP_Component parent ) {

        mContext = context;
        mParent  = parent;
    }


    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public void onDestroy() {

        for(CBubble bubble : mBubbles) {
            mParent.removeView(bubble);
        }
    }


    protected void runCommand(String command, Object target ) {

        CBubble bubble;

        switch(command) {
            case BP_CONST.INFLATE:
                bubble = (CBubble)target;

                Animator inflator = setupZoomIn(bubble, 600, 0, 0f, bubble.getIntrinsicScale());

                inflators.put(inflator, bubble);
                inflator.start();
                break;

            case BP_CONST.POP_BUBBLE:

                bubble = (CBubble)target;

                bubble.setVisibility(View.INVISIBLE);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                ImageView bubblepop = new ImageView(mContext);

                bubblepop.setScaleX(1.4f);
                bubblepop.setScaleY(1.4f);

                bubblepop.setX(bubble.getX());
                bubblepop.setY(bubble.getY());

                mParent.addView(bubblepop, layoutParams);

                popping = new AnimationDrawable();

                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_b_1, null), 80);
                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_b_2, null), 80);
                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_b_3, null), 70);
                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_b_4, null), 70);
                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_b_5, null), 60);
                popping.addFrame(mParent.getResources().getDrawable(R.drawable.bubble_empty, null), 60);
                popping.setOneShot(true);

//                popping = (AnimationDrawable) mParent.getResources().getDrawable(R.drawable.bubble_b_pop, null);

                bubblepop.setBackground(popping);

                popping.start();
                break;
        }
    }


    /**
     *
     * @param valueRange
     * @return
     */
    private float getRandInRange(float[] valueRange) {

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
     *   We are looking for the furthest we can push the bubble out along its vector before it
     *   impinges on either the top bottom or sides.  Note that this is sensitive to the aspect
     *   ratio of the container.
     *
     *   Taking the first quadrant for an example there is a critical angle at which the bubble
     *   will hit the top and the side simultaneously. Above this value it will hit the top before
     *   reaching the side and below it will hit the side before reaching the top.
     *
     *
     *
     * @param angle
     * @param center
     * @param radius
     * @param padding
     */
    private float[] calcVectorRange(Point center, float angle, float radius, Point padding ) {

        float distIntercept = 0;
        Point bounds        = new Point(center.x - padding.x, center.y - padding.y);
        float criticalAngle = (float) Math.atan((bounds.y - radius) / (bounds.x - radius));
        float quadrant      = (float)(Math.PI / 2f);

        if(angle <= quadrant) {

            if(angle < criticalAngle) {

                distIntercept = (float) ((bounds.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((bounds.y - radius) / Math.sin(angle));
            }
        }
        else if(angle <= (quadrant * 2)) {

            if(angle > (Math.PI - criticalAngle)) {

                distIntercept = (float) ((radius - bounds.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((bounds.y - radius) / Math.sin(angle));
            }
        }
        else if(angle <= (quadrant * 3)) {

            if(angle < (Math.PI + criticalAngle)) {

                distIntercept = (float) ((radius - bounds.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - bounds.y) / Math.sin(angle));
            }
        }
        else {

            if(angle > ((2*Math.PI) - criticalAngle)) {

                distIntercept = (float) ((bounds.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - bounds.y) / Math.sin(angle));
            }
        }

        return new float[]{radius * 1.5f, distIntercept};
    }


    public void populateView(CBp_Data data) {

        CBubble newBubble;
        int colorNdx = 0;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mBubbles = new CBubble[data.dataset.length];
        _scales = new float[data.dataset.length];

        for (int i1 = 0; i1 < mBubbles.length; i1++) {

            newBubble = (CBubble) View.inflate(mContext, R.layout.bubble_view, null);

            // Set Color: pass in resource ID - Cycle through the colors repetitively
            newBubble.setColor(BP_CONST.bubbleMap.get(BP_CONST.bubbleColors[colorNdx]));
            colorNdx = (colorNdx + 1) % BP_CONST.bubbleColors.length;

            newBubble.setScale(0);
            newBubble.setAlpha(_alpha);

            mBubbles[i1] = newBubble;

            mParent.addView(newBubble, layoutParams);

            switch (mParent.stimulus_type) {

                case BP_CONST.REFERENCE:

                    int[] shapeSet = BP_CONST.drawableMap.get(mParent.stimulus_data[data.dataset[i1]]);

                    newBubble.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null);
                    break;

                case BP_CONST.TEXTDATA:
                    newBubble.setContents(0, "3");
                    break;
            }
        }
    }


    public void doLayout(int width, int height, CBp_Data data) {

        int bubbleRadius = (mParent.getResources().getDrawable(BP_CONST.RED_BUBBLE, null).getIntrinsicWidth()) / 2;

        // Now we have the bubbles we position them on rays(vectors) eminating from the center of the
        // view.
        //
        _screenCenter.set(width / 2, height / 2);

        if(_angleRange != null)
            _angleStart = getRandInRange(_angleRange);

        _angleInc   = (float)((2 * Math.PI) / data.dataset.length);

        for(int i1 = 0 ; i1 < mBubbles.length ; i1++) {

            // This is the scale the bubble will expand too.
            //
            mBubbles[i1].setIntrinsicScale(getRandInRange(_scaleRange));

            float[] range = calcVectorRange(_screenCenter, _angleStart, bubbleRadius * mBubbles[i1].getIntrinsicScale(), _padding );

            mBubbles[i1].setAngle(_angleStart);
            mBubbles[i1].setPosition(_screenCenter, getRandInRange(range));

            _angleStart += _angleInc;
        }

        mInitialized = true;
    }


    public void startAnimation() {

        long delay = BP_CONST.INFLATE_DELAY;

        if (!mAnimationStarted && mInitialized) {
            mAnimationStarted = true;

            for(CBubble bubble : mBubbles) {
                post(BP_CONST.INFLATE, bubble, delay);

                delay += BP_CONST.INFLATE_DELAY;
            }
        }
    }


    protected void setupWiggle(View target, long delay) {

        AnimatorSet animation = CAnimatorUtil.configWiggle(target, "vertical", 3000, ValueAnimator.INFINITE, delay, 0.16f );

        //animation.addListener(this);
        animation.start();

        AnimatorSet stretch = CAnimatorUtil.configStretch(target, "vertical", 2100, ValueAnimator.INFINITE, delay, 1.21f);

        //stretch.addListener(this);
        stretch.start();
    }


    protected AnimatorSet setupZoomIn(View target, long duration, long delay, float... scales) {

        Log.d(TAG, "X" + target.getX());
        Log.d(TAG, "W" + target.getWidth());

        AnimatorSet animation = CAnimatorUtil.configZoomIn(target, duration, delay, scales );

        animation.addListener(new Animator.AnimatorListener() {
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

                CBubble bubble = inflators.get(animation);

                setupWiggle(bubble, 0);
                bubble.setOnClickListener(CBp_Mechanic_MC.this);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        return animation;
    }


    @Override
    public void onClick(View bubble) {

        post(BP_CONST.POP_BUBBLE, bubble);
    }

}
