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

package cmu.xprize.comp_pointtap;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;

public class CHandAnimation extends PercentRelativeLayout {

    private Context       mContext;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HashMap       _queueMap   = new HashMap();
    private boolean       _qDisabled  = false;

    private FrameLayout   _rippleContainer;
    private CRipple[]     _ripples = new CRipple[HA_CONST.RIPPLE_COUNT];
    private ImageView     _hand;
    private float         _offsetX;
    private float         _offsetY;
    private float         _offsetRippleX;
    private float         _offsetRippleY;

    private int           _color;
    private float         _strokeWeight;
    private float         _radIncrement;
    private float         _scaleFactor;

    static final String TAG = "CHandAnimation";


    public CHandAnimation(Context context) {
        super(context);
        init(context, null);
    }

    public CHandAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,  attrs);
    }

    public CHandAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,  attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.hand_animate, this);

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                _color        = a.getInt(R.styleable.RoboTutor_ripple_color, HA_CONST.DEFCOLOR);
                _strokeWeight = a.getFloat(R.styleable.RoboTutor_stroke_weight, HA_CONST.DEFSTROKEWEIGHT);
                _radIncrement = a.getFloat(R.styleable.RoboTutor_radius_inc, HA_CONST.DEFRADIUSINC);
                _scaleFactor  = a.getFloat(R.styleable.RoboTutor_scale_factor, HA_CONST.DEF_SCALE);

            } finally {
                a.recycle();
            }
        }

        _rippleContainer = (FrameLayout) findViewById(R.id.Sripples);
        _hand            = (ImageView) findViewById(R.id.Shand);

        initRipples(_color, _strokeWeight, _radIncrement);
    }


    public void initRipples(int color, float strokeWeight, float radIncrement) {

        float radius = HA_CONST.BASE_RADIUS;

        _offsetRippleX = radius + (_ripples.length * radIncrement) + (strokeWeight / 2);
        _offsetRippleY = radius + (_ripples.length * radIncrement) + (strokeWeight / 2);

        for(int i1 = 0 ; i1 < _ripples.length ; i1++) {
            _ripples[i1] = new CRipple(mContext);

            _ripples[i1].initRipple(color, strokeWeight, radius);

            _ripples[i1].setOrigin(_offsetRippleX, _offsetRippleY);

            _rippleContainer.addView(_ripples[i1]);
            radius += radIncrement;
        }
    }


    public void animateMoveTap(PointF target) {

        post(HA_CONST.ANIMATE_TAP, target);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        _hand.setPivotX(_hand.getWidth()  * _scaleFactor);
        _hand.setPivotY(_hand.getHeight() * _scaleFactor);

        _hand.setScaleX(_scaleFactor);
        _hand.setScaleY(_scaleFactor);
    }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);

        if(changed) {

            _offsetX = (int)(-(_hand.getWidth() * _hand.getScaleX())/5);
            _offsetY = (int)(-(_hand.getHeight() * _hand.getScaleY())/10);
        }
    }

    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch(command) {

            case HA_CONST.ANIMATE_MOVE:

                PointF tarPoint    = (PointF) target;

                _rippleContainer.setX(tarPoint.x - _offsetRippleX);
                _rippleContainer.setY(tarPoint.y - _offsetRippleY);

                tarPoint.x += _offsetX;
                tarPoint.y += _offsetY;

                PointF wayPoints[] = new PointF[2];

                wayPoints[0] = new PointF(tarPoint.x * 2, tarPoint.y * 2);
                wayPoints[1] = tarPoint;

                AnimatorSet fader      = CAnimatorUtil.configFadeIn(_hand, HA_CONST.TRANSLATE_TIME);
                Animator    translator = CAnimatorUtil.configTranslate(_hand, HA_CONST.TRANSLATE_TIME, 0, wayPoints);

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

                        post(HA_CONST.ANIMATE_TAP);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                fader.start();
                translator.start();
                break;

            case HA_CONST.ANIMATE_TAP:

                delay = 0;

                for (CRipple iripple : _ripples) {
                    post(HA_CONST.ANIMATE_RIPPLE, iripple, delay);

                    delay += HA_CONST.RIPPLE_DELAY;
                }

                post(HA_CONST.ANIMATE_HANDOFF, _hand, HA_CONST.HAND_TIMEOUT);
                break;


            case HA_CONST.ANIMATE_HANDOFF:
                AnimatorSet fadeHand = CAnimatorUtil.configFadeOut(_hand, 500);
                fadeHand.start();
                break;

            case HA_CONST.ANIMATE_RIPPLE:

                Animator ripple = CAnimatorUtil.fadeInOut((View)target, HA_CONST.RIPPLE_DELAY * 2);

                ripple.addListener(new Animator.AnimatorListener() {
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
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                ripple.start();
                break;
        }
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
                _queueMap.remove(this);

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

        Iterator<?> tObjects = _queueMap.entrySet().iterator();

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
            _queueMap.put(qCommand, qCommand);

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
