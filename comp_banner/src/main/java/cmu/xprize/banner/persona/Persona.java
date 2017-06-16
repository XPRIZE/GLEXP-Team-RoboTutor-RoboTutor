/**
 Copyright(c) 2015-2017 Kevin Willows
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cmu.xprize.banner.persona;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cmu.xprize.banner.R;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

/**
 *
 */
public class Persona extends View {

    private PersonaEye      leftEye;        // left from our perspective
    private PersonaEye      rightEye;       // right from our perspective
    private RectF           bounds;
    private PersonaEyeColor eyeColor;

    private float           pupilaryDist;

    private boolean         canStare;
    private Handler         stareHandler   = new Handler();
    private long            stareDelay     = 600L;
    private stareAnimator   stareAnimate   = new stareAnimator();
    private AnimatorSet     stareAnimation = new AnimatorSet();

    private boolean         canWatch;
    private boolean         canPoke;            // If you poke her in an eye she'll close it
    private boolean         isPoked  = false;   // Eye is closed

    private boolean         canBlink;
    private boolean         isBlinking = false;
    private float           blinkPeriod;        // Time Between Blinks
    private float           blinkVar;           // Variance in time
    private float           blinkDbl;           // Prob of a double blink
    private int             blinkCnt;
    private Timer           blinkTimer = null;

    private Handler         blinkHandler;

    private AnimatorSet     animation = null;
    private TimerTask       blinkTask;

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;

    private int[]           _screenCoord = new int[2];

    private static String   TAG          = "PERSONA";
    private static float    OPENEYE      = 0.15f;
    private static float    CLOSEDEYE    = 1.00f;


    /**
     * Create a Persona object
     *
     * @param context
     * @param attrs
     */
    public Persona(Context context, AttributeSet attrs) throws XmlPullParserException, IOException {
        super(context, attrs);

        XmlResourceParser parser;

        try {
            parser = getResources().getXml(R.xml.persona);
            // see: http://stackoverflow.com/questions/19955542/xmlpullparserexception-while-parsing-a-resource-file-in-android
            parser.next();
            parser.next();
            loadXML(parser);
        } catch (XmlPullParserException e) {
            Log.e("PERSONA", "XML Spec Invalid: " + e.getMessage());
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(QGRAPH_MSG, "event.click: " + " Persona: on touch");

                //startBlink();
            }
        });

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(TCONST.LOOKATSTART);
        filter.addAction(TCONST.LOOKAT);
        filter.addAction(TCONST.LOOKATEND);
        filter.addAction(TCONST.GLANCEAT);
        filter.addAction(TCONST.STARE_STOP);

        bReceiver = new ChangeReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }


    /**
     * Release resources and disconnect from broadcast Manager
     */
    public void onDestroy() {

        try {
            canBlink = false;
            canStare = false;

            if(blinkTimer != null) {
                blinkTimer.cancel();
                blinkTimer.purge();
                blinkTimer = null;
            }

            cancelStare();

            setOnClickListener(null);
            bManager.unregisterReceiver(bReceiver);
        }
        catch(Exception e) {
        }
    }


    /**
     * Update the gaze
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        leftEye.onDraw(canvas, eyeColor);
        rightEye.onDraw(canvas, eyeColor);
    }


    /**
     * Cause the eyes to look at a given screen location
     *
     * @param screenPos
     */
    public void lookAt(PointF screenPos) {

        leftEye.lookAt(screenPos);
        rightEye.lookAt(screenPos);

        invalidate();
    }

    public void lookAt(PointF screenPos, int duration) {

        lookAt(screenPos);

        new CountDownTimer(duration, duration) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                leftEye.setEyeLocation();
                rightEye.setEyeLocation();
                startBlink();
            }
        }.start();

    }


    public void watch(boolean startStop) {
        canWatch = startStop;
    }


    public void blink(boolean startStop) {
        canBlink = startStop;
    }



    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if(changed)
        {
            RectF containerBox = new RectF(left, top, right, bottom);

            leftEye.alignEye(containerBox, bounds);
            rightEye.alignEye(containerBox, bounds);
        }
    }


    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;

        int widthPixels = View.MeasureSpec.getSize( widthMeasureSpec );
        int heightPixels = View.MeasureSpec.getSize( heightMeasureSpec );

        //    Log.i("Persona:", "measureWidth: " + widthPixels + "  measureHeight: " + heightPixels);

        leftEye.setSizeByWidth(widthPixels);
        leftEye.setSizeByHeight(heightPixels);

        rightEye.setSizeByWidth(widthPixels);
        rightEye.setSizeByHeight(heightPixels);

        rightEye.setPupilDistance(pupilaryDist);

        bounds = new RectF();
        bounds.union(leftEye.getEyeBounds());
        bounds.union(rightEye.getEyeBounds());

        setMeasuredDimension((int)bounds.width(), (int)bounds.height());
    }


    protected float getPupilaryDist() {return pupilaryDist; }
    protected void setPupilaryDist(float newDist) { pupilaryDist = newDist; }

    // TODO: clean up STORE_STOP - doesn't need point

    class ChangeReceiver extends BroadcastReceiver {
        public void onReceive (Context context, Intent intent) {

//            Log.d("Persona", "Broadcast recieved: ");

            float[] point = intent.getFloatArrayExtra(TCONST.SCREENPOINT);

            getLocationOnScreen(_screenCoord);

            // Translate to local coordinate space - i.e. relative to view
            point[0] -= _screenCoord[0];
            point[1] -= _screenCoord[1];

            PointF localPt = new PointF(point[0], point[1]);

            switch(intent.getAction()) {

                case TCONST.STARE_STOP:
                    cancelStare();
                    break;

                case TCONST.LOOKATSTART:
                    startTouch(localPt);

                case TCONST.LOOKAT:
                    moveTouch(localPt);
                    break;

                case TCONST.LOOKATEND:
                    endTouch(localPt);
                    break;

                case TCONST.GLANCEAT:
                    startTouch(localPt);
                    moveTouch(localPt);
                    endTouch(localPt);
                    break;
            }
        }
    }


    /**
     *
     * TODO: Manage blinks while poked
     *
     * @param touchPt
     */
    private void startTouch(PointF touchPt) {

        cancelStare();

        if(canPoke) {
            if(leftEye.checkPoke(touchPt) || rightEye.checkPoke(touchPt))
                isPoked = true;
            invalidate();
        }

        if(!isPoked && canWatch) {
            lookAt(touchPt);
        }
    }


    /**
     *
     *
     * @param touchPt
     */
    private void moveTouch(PointF touchPt) {
        if(!isPoked && canWatch) {
            lookAt(touchPt);
        }
    }


    /**
     *
     * @param touchPt
     */
    private void endTouch(PointF touchPt) {
        if(isPoked)
        {
            leftEye.openEye(OPENEYE);
            rightEye.openEye(OPENEYE);

            isPoked = false;
            invalidate();
        }
        postDelayedStare();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        final PointF touchPt = new PointF(event.getX(),event.getY());
        final int    action  = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startTouch(touchPt);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(touchPt);
                break;
            case MotionEvent.ACTION_UP:
                endTouch(touchPt);
                break;
        }

        // Be sure to call the superclass implementation
        return true;
    }

    private void postDelayedStare() {

        if(canStare) {
            //Log.i(TAG, "Posting Stare Request");
            stareHandler.postDelayed(stareAnimate, stareDelay);
        }
    }

    private void cancelStare() {
        //Log.i(TAG, "Cancelling Stare Request");

        stareHandler.removeCallbacks(stareAnimate);
        stareAnimation.cancel();
    }

    private Animator createStareAnimation(Object target, String prop, float endPt ) {

        ValueAnimator stareAnim = null;

        stareAnim = ObjectAnimator.ofFloat(target, prop, endPt).setDuration(550);
        stareAnim.setInterpolator(new AccelerateInterpolator(1.0f));
        stareAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        return stareAnim;
    }


    class stareAnimator implements Runnable {
        public void run() {
            ArrayList<Animator> stareColl = new ArrayList<Animator>();

            stareAnimation = new AnimatorSet();

            //Log.i(TAG, "Starting Stare");

            stareColl.add(createStareAnimation(leftEye, "pupilX",  leftEye.getEyeLocation().x));
            stareColl.add(createStareAnimation(leftEye, "pupilY",  leftEye.getEyeLocation().y));
            stareColl.add(createStareAnimation(rightEye, "pupilX", rightEye.getEyeLocation().x));
            stareColl.add(createStareAnimation(rightEye, "pupilY", rightEye.getEyeLocation().y));

            stareAnimation.playTogether(stareColl);

            stareAnimation.start();
        }
    }


    private void postDelayedBlink() {
        if (blinkTimer == null) {
            blinkTimer   = new Timer("BLINK");
            blinkHandler = new Handler();
            isBlinking   = true;

            blinkTask = new TimerTask() {
                public void run() {
                    blinkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startBlink();
                        }
                    });
                }
            };

            Random r = new Random();

            float bvar = r.nextFloat() * blinkVar;
            long blinkTime = (long) (blinkPeriod - (blinkVar / 2) + bvar);

            //Log.i("PERSONA:", "Blink Time|Var: " + blinkTime + " | " + bvar);

            blinkCnt = (r.nextFloat() < blinkDbl) ? 3 : 1;

            blinkTimer.schedule(blinkTask, blinkTime);
        }
    }


    private Animator createAnimation(Object target) {

        ValueAnimator   blinkAnim = null;

        blinkAnim = ObjectAnimator.ofFloat(target, "blink", OPENEYE, CLOSEDEYE).setDuration(150);

        blinkAnim.setInterpolator(new AccelerateInterpolator(2.0f));
        blinkAnim.setRepeatCount(blinkCnt);
        blinkAnim.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });


        return blinkAnim;
    }


    public void startBlink() {
        animation = new AnimatorSet();

        //Log.i(TAG, "Starting Blink");

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
            public void onAnimationEnd(Animator arg0) {
                if(canBlink) {
                    //Log.i(TAG, "Animation End");
                    isBlinking = false;
                    blinkTimer.purge();
                    blinkTimer = null;
                    postDelayedBlink();
                }
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        animation.play(createAnimation(leftEye)).with(createAnimation(rightEye));

        animation.start();
    }

    public void exciteEyes(int duration) {

        final float radiusFactor = 1.1f;
        final float initOpenEye = OPENEYE;

        OPENEYE = OPENEYE / 2.0f;

        PointF leftRadius = leftEye.getEyeRadius();
        PointF rightRadius = rightEye.getEyeRadius();

        leftEye.setRadiusX(leftRadius.x * radiusFactor);
        leftEye.setRadiusY(leftRadius.y * radiusFactor);
        rightEye.setRadiusX(rightRadius.x * radiusFactor);
        rightEye.setRadiusY(rightRadius.y * radiusFactor);

        leftEye.setEyeLocation();
        rightEye.setEyeLocation();

        startBlink();


        new CountDownTimer(duration, duration) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                OPENEYE = initOpenEye;

                PointF leftRadius = leftEye.getEyeRadius();
                PointF rightRadius = rightEye.getEyeRadius();

                leftEye.setRadiusX(leftRadius.x / radiusFactor);
                leftEye.setRadiusY(leftRadius.y / radiusFactor);
                rightEye.setRadiusX(rightRadius.x / radiusFactor);
                rightEye.setRadiusY(rightRadius.y / radiusFactor);

                leftEye.setEyeLocation();
                rightEye.setEyeLocation();

                startBlink();

            }
        }.start();

    }


    public void droopEyes(int duration) {

        final float initOpenEye = OPENEYE;

        OPENEYE = 3*OPENEYE;
        startBlink();

        new CountDownTimer(duration, duration) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                OPENEYE = initOpenEye;
                startBlink();

            }
        }.start();

    }


    //***************************************************
    //*** Resource Loader Parser


    /**
     * Load the Persona specification from XML file data
     *
     * @param parser
     */
    public void loadXML(XmlResourceParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "persona");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            switch(name.toLowerCase()) {
                case "eye":
                    leftEye = new PersonaEye();
                    leftEye.loadXML(parser);
                    rightEye = new PersonaEye(leftEye);
                    break;

                case "eyecolor":
                    eyeColor = new PersonaEyeColor();
                    eyeColor.loadXML(parser);
                    break;

                case "pupilarydist":
                    setPupilaryDist(new Float(readField(parser, "pupilarydist")));
                    break;

                case "canwatch":
                    canWatch = (new Boolean(readField(parser, "canwatch")));
                    break;

                case "canblink":
                    canBlink = (new Boolean(readField(parser, "canblink")));
                    break;

                case "canstare":
                    canStare = (new Boolean(readField(parser, "canstare")));
                    break;

                case "canpoke":
                    canPoke = (new Boolean(readField(parser, "canpoke")));
                    break;

                case "blinkperiod":
                    blinkPeriod = (new Float(readField(parser, "blinkperiod")));
                    break;

                case "blinkvar":
                    blinkVar = (new Float(readField(parser, "blinkvar")));
                    break;

                case "blinkdbl":
                    blinkDbl = (new Float(readField(parser, "blinkdbl")));
                    break;

                default:
                    skip(parser);
                    break;
            }
        }

        if(canBlink)
            postDelayedBlink();

    }


    /**
     * Process arbitrary field strings
     *
     * @param parser
     * @param field
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readField(XmlPullParser parser, String field) throws IOException, XmlPullParserException {

        String strData = null;

        parser.require(XmlPullParser.START_TAG, null, field);

        if (parser.next() == XmlPullParser.TEXT) {
            strData = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, field);

        return strData;
    }


    /**
     * Skip TAGs that we don't know about
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
