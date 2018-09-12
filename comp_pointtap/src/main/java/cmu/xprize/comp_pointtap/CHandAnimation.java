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

package cmu.xprize.comp_pointtap;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.TCONST;

public class CHandAnimation extends PercentRelativeLayout implements Animator.AnimatorListener {

    private Context       mContext;

    private final Handler mainHandler       = new Handler(Looper.getMainLooper());
    private HashMap       _queueMap         = new HashMap();
    private boolean       _qDisabled        = false;
    private boolean       _cancelAnimation  = false;

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

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;
    private int[]                 _screenCoord = new int[2];

    private AnimatorSet           _animation;
    private String[]              _animatorSeq;
    private int                   _animatorNdx;
    private boolean               _inAnimation = false;
    private PointF                _animationPoint;

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
                _color        = a.getInt(R.styleable.RoboTutor_ripple_color, HA_CONST.RIPPLE_COLOR);
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

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(TCONST.POINTAT);
        filter.addAction(TCONST.POINTATEND);
        filter.addAction(TCONST.POINT_AND_TAP);
        filter.addAction(TCONST.POINT_LIVE);
        filter.addAction(TCONST.POINT_FADE);
        filter.addAction(TCONST.CANCEL_POINT);

        bReceiver = new ChangeReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }


    public void onDestroy() {

        if(bManager != null) {
            bManager.unregisterReceiver(bReceiver);
        }
    }


    class ChangeReceiver extends BroadcastReceiver {
        public void onReceive (Context context, Intent intent) {

            float[] coord;
            View    target;

//            Log.d("Indicate", "Broadcast recieved: ");

            switch(intent.getAction()) {

                case TCONST.CANCEL_POINT:

                    _cancelAnimation = true;

                    flushQueue();

                    if(_animation != null) {
                        _animation.cancel();
                    }
                    _hand.setAlpha(0f);

                    break;

                case TCONST.POINT_AND_TAP:

                    _cancelAnimation = false;

                    if(!_inAnimation) {

                        _inAnimation = true;

                        coord = intent.getFloatArrayExtra(TCONST.SCREENPOINT);

                        _animationPoint = new PointF(coord[0], coord[1]);
                        _animatorSeq = new String[]{HA_CONST.ANIMATE_MOVE, HA_CONST.ANIMATE_TAP, HA_CONST.ANIMATE_FADE};
                        post(HA_CONST.STARTSEQ);
                    }
                    break;

                case TCONST.POINTAT:
                    Log.d("ddd", "pointat: ");
                    _cancelAnimation = false;

                    if(!_inAnimation) {
                        Log.d("ddd", "inanimation");

                        _inAnimation = true;

                        coord = intent.getFloatArrayExtra(TCONST.SCREENPOINT);

                        _animationPoint = new PointF(coord[0], coord[1]);
                        _animatorSeq = new String[]{HA_CONST.ANIMATE_MOVE, HA_CONST.ANIMATE_FADE};
                        post(HA_CONST.STARTSEQ);
                    }
                    break;


                case TCONST.POINT_LIVE:

                    _cancelAnimation = false;

                    coord = intent.getFloatArrayExtra(TCONST.SCREENPOINT);

                    _hand.setAlpha(1.0f);
                    _hand.setX(coord[0] + _offsetX);
                    _hand.setY(coord[1] + _offsetY);
                    invalidate();

                    break;


                case TCONST.POINT_FADE:

                    _cancelAnimation = false;

                    if(!_inAnimation) {

                        _inAnimation = true;
                        _animatorSeq = new String[]{HA_CONST.ANIMATE_FADE};
                        post(HA_CONST.STARTSEQ);
                    }
                    break;
            }
        }
    }


    public void initRipples(int color, float strokeWeight, float radIncrement) {

        float radius = HA_CONST.BASE_RADIUS;

        // Calcu;ate the ripple center offset
        // Offsets are required since Android clips anumated views regardless of clipChildren
        // settings so the view cannot draw outside it's bounods
        //
        _offsetRippleX = radius + (_ripples.length * radIncrement) + (strokeWeight / 2);
        _offsetRippleY = radius + (_ripples.length * radIncrement) + (strokeWeight / 2);

        for(int i1 = 0 ; i1 < _ripples.length ; i1++) {
            _ripples[i1] = new CRipple(mContext);

            _ripples[i1].initRipple(color, strokeWeight, radius);

            _ripples[i1].setOrigin(_offsetRippleX, _offsetRippleY);

            _rippleContainer.addView(_ripples[i1]);

            radius       += radIncrement;
            strokeWeight *= 0.7;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);

        if(changed) {

            // Set the pivot point to the upper left corner - we scale from their not the center
            // which is the default.
            //
            _hand.setPivotX(0.0f);
            _hand.setPivotY(0.0f);

            // Correct for Android DPI variance - i.e. the hand will be a different
            // size based on display dpi
            _scaleFactor *= HA_CONST.DESIGN_SIZE / _hand.getWidth();

            _hand.setScaleX(_scaleFactor);
            _hand.setScaleY(_scaleFactor);

            // Generate the offset to the finger tip - used to position over the target point
            // Offsets are required since Android clips "animated" views regardless of clipChildren
            // settings so the view cannot draw outside it's bounds. So we cannot move the drawables
            // origin to have the finger tip at 0,0 (note that static images clip correctly)
            //
            _offsetX = (int)(-(_hand.getWidth() * _hand.getScaleX())/5);
            _offsetY = (int)(-(_hand.getHeight() * _hand.getScaleY())/10);
        }
    }


    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch(command) {

            case HA_CONST.STARTSEQ:
                _animatorNdx = 0;
                post(_animatorSeq[0], _animationPoint);
                break;

            case HA_CONST.ANIMATE_MOVE:

                PointF tarPoint    = (PointF) target;

                // Move the ripple center to the target point
                //
                _rippleContainer.setX(tarPoint.x - _offsetRippleX);
                _rippleContainer.setY(tarPoint.y - _offsetRippleY);

                // Move the finger tip to the target point
                //
                tarPoint.x += _offsetX;
                tarPoint.y += _offsetY;

                PointF wayPoints[] = new PointF[2];

                wayPoints[0] = new PointF(tarPoint.x * 1.5f, tarPoint.y * 1.5f);
                wayPoints[1] = tarPoint;

                Animator fader      = CAnimatorUtil.configFadeIn(_hand, HA_CONST.TRANSLATE_TIME);
                Animator translator = CAnimatorUtil.configTranslate(_hand, HA_CONST.TRANSLATE_TIME, 0, wayPoints);

                _animation = new AnimatorSet();
                _animation.playTogether(fader,translator);
                _animation.addListener(CHandAnimation.this);
                _animation.start();
                break;

            case HA_CONST.ANIMATE_TAP:

                ArrayList<Animator> ripples = new ArrayList<>();
                delay = 0;

                for (CRipple iripple : _ripples) {

                    ripples.add(CAnimatorUtil.fadeInOut(iripple, HA_CONST.RIPPLE_DELAY * 2, delay));

                    delay += HA_CONST.RIPPLE_DELAY;
                }

                _animation = new AnimatorSet();
                _animation.playTogether(ripples);
                _animation.addListener(CHandAnimation.this);
                _animation.start();
                break;


            case HA_CONST.ANIMATE_FADE:

                _animation = new AnimatorSet();
                _animation = CAnimatorUtil.configFadeOut(_hand, 500);
                _animation.addListener(CHandAnimation.this);
                _animation.start();
                break;

//            case HA_CONST.ANIMATE_RIPPLE:
//
//                Animator ripple = CAnimatorUtil.fadeInOut((View)target, HA_CONST.RIPPLE_DELAY * 2);
//
//                ripple.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationCancel(Animator arg0) {
//                        //Functionality here
//                    }
//
//                    @Override
//                    public void onAnimationStart(Animator arg0) {
//                        //Functionality here
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator arg0) {
//                        //Functionality here
//                    }
//                });
//
//                ripple.start();
//                break;
        }
    }


    //************************************************************************
    //************************************************************************
    // Animator Listener  -- Start

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {

        _animatorNdx++;

        if(!_cancelAnimation && (_animatorNdx < _animatorSeq.length)) {

            post(_animatorSeq[_animatorNdx], _animationPoint);
        }
        else {
            _inAnimation = false;
            broadcast(TCONST.POINTAT_COMPLETE);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }



    protected void broadcast(String Action) {

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        bManager.sendBroadcast(msg);
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
