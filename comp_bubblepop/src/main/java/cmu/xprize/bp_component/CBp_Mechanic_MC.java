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
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.TCONST;

/**
 *
 */
public class CBp_Mechanic_MC extends CBp_Mechanic_Base implements IBubbleMechanic {

    private Paint           mPaint       = new Paint();
    private Rect            mViewRegion  = new Rect();

    private float           _angle      = 0;
    private float[]         _angleRange = {BP_CONST.ANGLE_MIN, BP_CONST.ANGLE_MAX};
    private float           _angleInc;

    // Note: the bubble is smaller than than the pop animation so it fits well inside these bounds
    // only the pop will hit the margins -
    // TODO: possibly change the bubble drawable and then recenter the pop drawable as required.
    //
    private Rect            _margins    = new Rect(BP_CONST.MARGIN_LEFT, BP_CONST.MARGIN_TOP, BP_CONST.MARGIN_RIGHT, BP_CONST.MARGIN_BOTTOM);
    private Point           _viewCenter = new Point();

    //Maximum bubble height accounting for stretch and bounce (210 * 1.3 + 210 * .16) * 1.21
    static float maxBubbleHeight = (float) (371);

    static final String TAG = "CBp_Mechanic_MC";

    private String          mProblemType;

    public CBp_Mechanic_MC(Context context, CBP_Component parent, String problem_type) {
        super.init(context, parent);
        mProblemType = problem_type;
    }

    @Override
    protected void init(Context context, CBP_Component parent) {
        super.init(context, parent);
    }


    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDraw(Canvas canvas) {

        // Create a paint object to deine the line parameters
        mPaint      = new Paint();
        mViewRegion = new Rect();

        mPaint.setColor(Color.parseColor("#FF00FF"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);

        mParent.getDrawingRect(mViewRegion);

        mViewRegion.top    += _margins.top;
        mViewRegion.bottom -= _margins.bottom;
        mViewRegion.left   += _margins.left;
        mViewRegion.right  -= _margins.right;

        canvas.drawRect(mViewRegion, mPaint);

        mPaint.setColor(Color.parseColor("#000000"));
        canvas.drawCircle(_viewCenter.x, _viewCenter.y , 13f, mPaint);

        int _bubbleIntrinsicRadius = (mParent.getResources().getDrawable(BP_CONST.BUBBLE_SAMPLE, null).getIntrinsicWidth()) / 2;

        for(int i1 = 0; i1 < SBubbles.length ; i1++) {

            float[] range = SBubbles[i1].getRange();
            float   angle = SBubbles[i1].getAngle();

            PointF pos1 = SBubbles[i1].getPosition(_viewCenter, range[0], angle);
            PointF pos2 = SBubbles[i1].getPosition(_viewCenter, range[1], angle);

            float irad = BP_CONST.BUBBLE_DESIGN_RADIUS * SBubbles[i1].getAssignedScale();
            float brad = irad + BP_CONST.BUBBLE_DESIGN_RADIUS * BP_CONST.BOUNCE_MAGNITUDE;
            float srad = brad * BP_CONST.STRETCH_MAGNITUDE;

            mPaint.setColor(Color.parseColor("#00FFFF"));
            canvas.drawCircle(pos2.x, pos2.y , irad, mPaint);

            mPaint.setColor(Color.parseColor("#000000"));
            canvas.drawCircle(pos2.x, pos2.y , 10, mPaint);

            mPaint.setColor(Color.parseColor("#000000"));
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(pos2.x, pos2.y , brad, mPaint);

            mPaint.setColor(Color.parseColor("#00FF00"));
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(pos2.x, pos2.y , srad, mPaint);

            mPaint.setColor(Color.parseColor("#FF0000"));
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(pos1.x, pos1.y , 13f, mPaint);

            mPaint.setColor(Color.parseColor("#0000FF"));
            mPaint.setStyle(Paint.Style.FILL);
            PointF pos3 = SBubbles[i1].getCenterPosition();
            mPaint.setAlpha(100);
            canvas.drawCircle(pos3.x, pos3.y , irad, mPaint);
            mPaint.setAlpha(255);
            canvas.drawCircle(pos3.x, pos3.y , 13f, mPaint);


            mPaint.setColor(Color.parseColor("#000000"));
            canvas.drawLine(_viewCenter.x, _viewCenter.y ,pos2.x, pos2.y , mPaint);
        }
    }


    protected void setupWiggle(View target, long delay) {

        AnimatorSet animation = CAnimatorUtil.configWiggle(target, "vertical", 3000, ValueAnimator.INFINITE, delay, BP_CONST.BOUNCE_MAGNITUDE );

        float[] wayPoints = new float[]{target.getScaleY(),
                                        target.getScaleY() * BP_CONST.STRETCH_MAGNITUDE,
                                        target.getScaleY()};

        AnimatorSet stretch   = CAnimatorUtil.configStretch(target, "vertical", 2100, ValueAnimator.INFINITE, delay, wayPoints );

        animation.start();
        stretch.start();
    }


    public void execCommand(String command, Object target ) {

        CBubble bubble;
        long    delay = 0;

        super.execCommand(command, target);

        switch(command) {

            case BP_CONST.SHOW_BUBBLES:
                delay = BP_CONST.INFLATE_DELAY;

                if (mInitialized) {

                    for(CBubble ibubble : SBubbles) {
                        mComponent.post(BP_CONST.INFLATE, ibubble, delay);
                        delay += BP_CONST.INFLATE_DELAY;
                    }

                }
                break;

            case BP_CONST.INFLATE:
                bubble = (CBubble)target;

                // Persona - look at the stimulus
                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(new PointF(bubble.getX() + bubble.getWidth()/2, bubble.getY()+ bubble.getHeight()/2)));

                Animator inflator = CAnimatorUtil.configZoomIn(bubble, 600, 0, new BounceInterpolator(), 0f, bubble.getAssignedScale());

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

                        CBubble bubble = inflators.get(animation);
                        inflators.remove(animation);

                        setupWiggle(bubble, 0);
                        bubble.setOnClickListener(CBp_Mechanic_MC.this);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                inflators.put(inflator, bubble);
                inflator.start();
                break;

            case BP_CONST.POP_BUBBLE:

                bubble = (CBubble)target;
                delay  = bubble.pop();

                // stop listening to the bubble
                bubble.setOnClickListener(null);

                broadcastLocation(TCONST.GLANCEAT, mParent.localToGlobal(bubble.getCenterPosition()));

                mComponent.post(BP_CONST.REMOVE_BUBBLE, bubble, delay);
                break;

        }
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
     *   Note: the bubble is dimensionally smaller than than the pop animation so it fits well
     *   inside these bounds only the pop animation will hit the margins - See TODO above
     *
     * @param angle
     * @param radius
     */
    private float[] calcVectorRange(float angle, float radius ) {

        // Account for the bubble bounce and stretch so that it doesn't go outside the margin bounds.
        // Note: BOUNCE_MAGNITUDE is a relative value while STRETCH_MAGNITUDE is an absolute multiple
        //
        radius += radius * BP_CONST.BOUNCE_MAGNITUDE;
        radius  = radius * BP_CONST.STRETCH_MAGNITUDE;

        float distIntercept = 0;
        Point coordCenter   = new Point(_viewCenter.x - _margins.right, _viewCenter.y - _margins.top);
        float criticalAngle = (float) Math.atan((coordCenter.y - radius) / (coordCenter.x - radius));
        float quadrant      = (float)(Math.PI / 2f);


        if(angle <= quadrant) {

            if(angle < criticalAngle) {

                distIntercept = (float) ((coordCenter.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((coordCenter.y - radius) / Math.sin(angle));
            }
        }
        else if(angle <= (quadrant * 2)) {

            if(angle > (Math.PI - criticalAngle)) {

                distIntercept = (float) ((radius - coordCenter.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((coordCenter.y - radius) / Math.sin(angle));
            }
        }
        else if(angle <= (quadrant * 3)) {

            if(angle < (Math.PI + criticalAngle)) {

                distIntercept = (float) ((radius - coordCenter.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - coordCenter.y) / Math.sin(angle));
            }
        }
        else {

            if(angle > ((2*Math.PI) - criticalAngle)) {

                distIntercept = (float) ((coordCenter.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - coordCenter.y) / Math.sin(angle));
            }
        }

        //return new float[]{radius * 1.5f, distIntercept};
        return new float[]{distIntercept * BP_CONST.MIN_VRANGE, distIntercept};
    }


    public void populateView(CBp_Data data) {

        CBubble newBubble;

        // Check if the response_set needs to be generated
        //
        generateRandomData(data);

        // Start the bubbles with a random color and cycle the colors
        int    colorNdx   = (int)(Math.random() * BP_CONST.bubbleColors.length);
        String correctVal = data.answer;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        SBubbles = new CBubble[data.response_set.length];

        for (int i1 = 0; i1 < SBubbles.length; i1++) {

            newBubble = (CBubble) View.inflate(mContext, R.layout.bubble_view, null);

            String bubbleColor = BP_CONST.bubbleColors[colorNdx];

            // Set Color: pass in String e.g. "RED" - Cycle through the colors repetitively
            //
//            if(data.stimulus_type.equals(TCONST.AUDIO_REF)) {
//                newBubble.setFeedbackColor(bubbleColor);
//            }


            colorNdx = (colorNdx + 1) % BP_CONST.bubbleColors.length;


            newBubble.setScale(0);
            newBubble.setAlpha(_alpha);

            SBubbles[i1] = newBubble;

            mParent.addView(newBubble, layoutParams);

            String responseVal = data.response_set[i1];
            String responseTyp = data.responsetype_set[i1];

            switch (responseTyp) {

                case BP_CONST.REFERENCE:

                    try {
                        int[] shapeSet = BP_CONST.drawableMap.get(responseVal);

                        newBubble.configData(responseVal, correctVal, mProblemType);
                        newBubble.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null);

                        //Moved set color to here to so that text would be known when setting the color(generating  bubble)
                        newBubble.setColor(bubbleColor);

                    }
                    catch(Exception e) {
                        Log.e(TAG, "Invalid Datatset: " + responseVal);
                    }
                    break;

                case BP_CONST.TEXTDATA:

                    newBubble.configData(responseVal, correctVal, mProblemType);
                    newBubble.setContents(0, responseVal);

                    //Moved set color to here too so that text would be known when setting the color(generating  bubble)
                    newBubble.setColor(bubbleColor);
                    break;
            }
        }
    }


    public void doLayout(int width, int height, CBp_Data data) {

        // Now we have the bubbles we position them on rays(vectors) eminating from the center of the
        // view.
        //
        _viewCenter.set((width + _margins.left - _margins.right) / 2, (height + _margins.top - _margins.bottom) / 2);

        if(_angleRange != null)
            _angle = getRandInRange(_angleRange);

        _angleInc   = (float)((2 * Math.PI) / data.response_set.length);

        for(int i1 = 0; i1 < SBubbles.length ; i1++) {

            // This is the scale the bubble will expand too.
            //
            //SBubbles[i1].setAssignedScale(getRandInRange(_scaleRange));
            SBubbles[i1].setAssignedScale(1.0f);

            Paint paint = new Paint();

            String text = SBubbles[i1].getTextView().getText().toString();
            String singleLine = text;

            //Calculate approximations of height and width of bubble instantenously so that calculations for
            //arranging the bubble can be done
            int bubbleHeight = BP_CONST.BUBBLE_DESIGN_RADIUS;
            int bubbleWidth = BP_CONST.BUBBLE_DESIGN_RADIUS;

            //If multiple lines, change what one line is
            if(text.matches(".*\n.*")) {
                int index = text.indexOf("\n");
                singleLine = text.substring(0, index);
            }

            int numChar = singleLine.length();
            bubbleWidth = bubbleWidth - 30 * numChar;
            bubbleWidth = 190;

            float[] _widthRange = calcVectorRange(_angle, bubbleWidth * SBubbles[i1].getAssignedScale());
            float[] _heightRange = calcVectorRange(_angle,  bubbleHeight * SBubbles[i1].getAssignedScale());

            SBubbles[i1].setVectorPosition(_viewCenter, getRandInRange(_widthRange), getRandInRange(_heightRange), _angle);
            _angle += _angleInc;
        }

        mInitialized = true;
    }


}
