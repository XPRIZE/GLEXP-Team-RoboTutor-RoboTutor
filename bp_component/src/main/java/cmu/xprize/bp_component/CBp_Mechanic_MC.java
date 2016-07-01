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

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CBp_Mechanic_MC implements IBubbleMechanic {

    private Context         mContext;
    private CBP_Component   mParent;

    private CBubble[]       mBubbles;

    private float           _alpha      = 0.65f;
    private float[]         _scaleRange = {0.8f, 1.2f};
    private float[]         _scales;

    private float           _angleStart = 0;
    private float[]         _angleRange = {0, (float)(Math.PI/4.0)};
    private float           _angleInc;
    private float[]         _rays;
    private int             _padding = 30;
    private Point           _screenCenter;

    static final String TAG = "CBp_Mechanic_MC";


    public CBp_Mechanic_MC(Context context, CBP_Component parent ) {

        mContext = context;
        mParent  = parent;
    }


    @Override
    public void onDestroy() {

        for(CBubble bubble : mBubbles) {
            mParent.removeView(bubble);
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
    private float[] calcVectorRange(Point center, float angle, float radius, int padding ) {

        float distIntercept = 0;
        Point bounds        = new Point(center.x - padding, center.y - padding);
        float criticalAngle = (float) Math.atan2(bounds.x - radius, bounds.y - radius);
        float quadrant      = (float)(Math.PI / 2f);

        if(angle <= quadrant) {

            if(angle < criticalAngle) {

                distIntercept = (float) ((bounds.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((bounds.y - radius) / Math.sin(angle));
            }
        }
        if(angle <= (quadrant * 2)) {

            if(angle > (Math.PI - criticalAngle)) {

                distIntercept = (float) ((radius - bounds.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((bounds.y - radius) / Math.sin(angle));
            }
        }
        if(angle <= (quadrant * 3)) {

            if(angle < (Math.PI + criticalAngle)) {

                distIntercept = (float) ((radius - bounds.x) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - bounds.y) / Math.sin(angle));
            }
        }
        if(angle <= (quadrant * 4)) {

            if(angle > ((2*Math.PI) - criticalAngle)) {

                distIntercept = (float) ((bounds.x - radius) / Math.cos(angle));
            }
            else {

                distIntercept = (float) ((radius - bounds.y) / Math.sin(angle));
            }
        }

        return new float[]{radius * 2, distIntercept};
    }


    public void doLayout(int width, int height, CBp_Data data) {

        CBubble newBubble;
        int     colorNdx     = 0;
        int     bubbleRadius = (mParent.getResources().getDrawable(BP_CONST.RED_BUBBLE, null).getIntrinsicWidth()) / 2;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mBubbles = new CBubble[data.dataset.length];
        _scales  = new float[data.dataset.length];

        for(int i1 = 0 ; i1 < mBubbles.length ; i1++) {

            newBubble = (CBubble) View.inflate(mContext, R.layout.bubble_view, null);

            // Set Color: pass in resource ID - Cycle through the colors repetitively
            newBubble.setColor(BP_CONST.bubbleMap.get(BP_CONST.bubbleColors[colorNdx]));
            colorNdx = (colorNdx + 1) % BP_CONST.bubbleColors.length;

            newBubble.setScale(0);
            newBubble.setAlpha(_alpha);

            newBubble.setX(300);
            newBubble.setY(400);

            mBubbles[i1] = newBubble;

            mParent.addView(newBubble, layoutParams);

            switch(mParent.stimulus_type) {

                case BP_CONST.REFERENCE:

                    int[] shapeSet = BP_CONST.drawableMap.get(mParent.stimulus_data[data.dataset[i1]]);

                    newBubble.setContents(shapeSet[(int)(Math.random() * shapeSet.length)], null);
                    break;

                case BP_CONST.TEXTDATA:
                    newBubble.setContents(0, "3");
                    break;
            }
        }

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

            float[] range = calcVectorRange(_screenCenter, _angleStart, bubbleRadius, _padding );

            mBubbles[i1].setLocation(getRandInRange(range));
            mBubbles[i1].setAngle(_angleStart);


        }

    }

}
