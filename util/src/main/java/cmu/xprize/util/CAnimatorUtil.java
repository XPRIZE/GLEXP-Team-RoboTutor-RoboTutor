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

package cmu.xprize.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BaseInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.util.Log;
import java.util.Arrays;

import java.util.ArrayList;


// TODO: CAnimatorUtil needs to be integrated with the MediaManager so that it can kill animations on demand.

public class CAnimatorUtil {


    static private Animator createFloatAnimator(View _tarView, String prop, long duration, int repeat, int mode, TimeInterpolator interpolator, float... endPts) {

        return createFloatAnimator(_tarView, prop, duration, repeat, mode, interpolator, 0, endPts);
    }


    static private Animator createFloatAnimator(View _tarView, String prop, long duration, int repeat, int mode, TimeInterpolator interpolator, long delay, float... endPts) {

        ValueAnimator vAnimator = null;
        vAnimator = ObjectAnimator.ofFloat(_tarView, prop, endPts).setDuration(duration);
        vAnimator.setInterpolator(interpolator);

        vAnimator.setRepeatCount(repeat);
        vAnimator.setRepeatMode(mode);
        vAnimator.setStartDelay(delay);

        return vAnimator;
    }

    static public void zoomOut(View _tarView, float scale, long duration){
        ArrayList<Animator> zoomColl = new ArrayList<Animator>();

        AnimatorSet animation = new AnimatorSet();

        zoomColl.add(createFloatAnimator(_tarView, "scaleX", duration, 0, 0, new AccelerateInterpolator(2.0f), _tarView.getScaleX(), scale, scale*_tarView.getScaleX()));
        zoomColl.add(createFloatAnimator(_tarView, "scaleY", duration, 0, 0, new AccelerateInterpolator(2.0f), _tarView.getScaleY(), scale, scale*_tarView.getScaleX()));

        animation.playTogether(zoomColl);
        animation.start();
    }

    static public void zoomInOut(View _tarView, float scale, long duration) {

        ArrayList<Animator> zoomColl = new ArrayList<Animator>();

        AnimatorSet animation = new AnimatorSet();

        zoomColl.add(createFloatAnimator(_tarView, "scaleX", duration, 0, 0, new AccelerateInterpolator(2.0f), _tarView.getScaleX(), scale, _tarView.getScaleX()));
        zoomColl.add(createFloatAnimator(_tarView, "scaleY", duration, 0, 0, new AccelerateInterpolator(2.0f), _tarView.getScaleY(), scale, _tarView.getScaleY()));

        animation.playTogether(zoomColl);
        animation.start();
    }


    static public AnimatorSet fadeInOut(View _tarView, long duration, long delay) {

        float[] wayPoints = new float[3];

        Animator    fadeAnimator;
        AnimatorSet animation = new AnimatorSet();

        wayPoints[0] = 0;
        wayPoints[1] = .30f;
        wayPoints[2] = 0;

        fadeAnimator = createFloatAnimator(_tarView, "alpha", duration, 0, ValueAnimator.RESTART, new LinearInterpolator(), delay, wayPoints);

        animation.play(fadeAnimator);

        return animation;
    }


    static public AnimatorSet configFadeIn(View _tarView, long duration) {

        float[] wayPoints = new float[2];

        Animator    fadeAnimator;
        AnimatorSet animation = new AnimatorSet();

        wayPoints[0] = 0;
        wayPoints[1] = 1;

        fadeAnimator = createFloatAnimator(_tarView, "alpha", duration, 0, ValueAnimator.RESTART, new LinearInterpolator(), 0, wayPoints);

        animation.play(fadeAnimator);

        return animation;
    }


    static public AnimatorSet configFadeOut(View _tarView, long duration) {

        float[] wayPoints = new float[2];

        Animator    fadeAnimator;
        AnimatorSet animation = new AnimatorSet();

        wayPoints[0] = 1;
        wayPoints[1] = 0;

        fadeAnimator = createFloatAnimator(_tarView, "alpha", duration, 0, ValueAnimator.RESTART, new LinearInterpolator(), 0, wayPoints);

        animation.play(fadeAnimator);

        return animation;
    }


    static public void wiggle(View _tarView, String direction, float magnitude, long duration, int repetition ) {

        AnimatorSet animation = configWiggle( _tarView,  direction,  duration,  repetition, magnitude );

        animation.start();
    }

    static public AnimatorSet configWiggle(View _tarView, String direction, long duration, int repetition, float magnitude) {
        return configWiggle(_tarView, direction, duration, repetition, 0, magnitude);
    }

    static public AnimatorSet configWiggle(View _tarView, String direction, long duration, int repetition, long delay, float magnitude) {

        Animator wiggleAnimator;
        int      animatorMode = ValueAnimator.RESTART;
        float[]  wayPoints  = new float[5];

        AnimatorSet animation = new AnimatorSet();

        switch (direction.toLowerCase()) {
            case "vertical":

                    wayPoints[0] = _tarView.getY();
                    wayPoints[1] = _tarView.getY() + (_tarView.getHeight() * magnitude);
                    wayPoints[2] = _tarView.getY();
                    wayPoints[3] = _tarView.getY() - (_tarView.getHeight() * magnitude);
                    wayPoints[4] = _tarView.getY();

                    wiggleAnimator = createFloatAnimator(_tarView, "y", duration, repetition, animatorMode, new LinearInterpolator(), delay, wayPoints);
                    break;

            default:

                    wayPoints[0] = _tarView.getX();
                    wayPoints[1] = _tarView.getX() + (_tarView.getWidth() * magnitude);
                    wayPoints[2] = _tarView.getX();
                    wayPoints[3] = _tarView.getX() - (_tarView.getWidth() * magnitude);
                    wayPoints[4] = _tarView.getX();

                    wiggleAnimator = createFloatAnimator(_tarView, "x", duration, repetition, animatorMode, new LinearInterpolator(), delay, wayPoints);
                    break;
        }

        animation.play(wiggleAnimator);

        return animation;
    }



    static public AnimatorSet configStretch(View _tarView, String direction, long duration, int repetition, float... magnitude) {
        return  configStretch(_tarView,  direction,  duration,  repetition, 0, magnitude );
    }

    static public AnimatorSet configStretch(View _tarView, String direction, long duration, int repetition, long delay, float... wayPoints ) {

        Animator stretchAnimator;
        int      animatorMode = ValueAnimator.RESTART;
//        float[]  wayPoints  = new float[3];


        AnimatorSet animation = new AnimatorSet();

        //float scaleEnd =  _tarView.getScaleY() * magnitude;

        switch(direction.toLowerCase()) {
            case "vertical":

//                wayPoints[0] = _tarView.getScaleY();
//                wayPoints[1] = scaleEnd;
//                wayPoints[2] = _tarView.getScaleY();

                stretchAnimator = createFloatAnimator(_tarView, "scaleY", duration, repetition, animatorMode, new LinearInterpolator(), delay, wayPoints);
                break;

            default:

//                wayPoints[0] = _tarView.getScaleX();
//                wayPoints[1] = scaleEnd;
//                wayPoints[2] = _tarView.getScaleX();

                stretchAnimator = createFloatAnimator(_tarView, "scaleX", duration, repetition, animatorMode, new LinearInterpolator(), delay, wayPoints);
                break;
        }

        animation.play(stretchAnimator);

        return animation;
    }


    static public AnimatorSet configZoomIn(View _tarView, long duration, long delay, TimeInterpolator interpolator, float... absScales) {

        ArrayList<Animator> zoomAnimators = new ArrayList<Animator>();
        AnimatorSet animation = new AnimatorSet();

        zoomAnimators.add(createFloatAnimator(_tarView, "scaleX", duration, 0, 0, interpolator, delay, absScales));
        zoomAnimators.add(createFloatAnimator(_tarView, "scaleY", duration, 0, 0, interpolator, delay, absScales));

        animation.playTogether(zoomAnimators);

        return animation;
    }

    static public AnimatorSet configTranslate(View _tarView, long duration, long delay, PointF... absPos) {

        ArrayList<Animator> moveAnimators = new ArrayList<Animator>();

        float[]  wayPointsX  = new float[absPos.length];
        float[]  wayPointsY  = new float[absPos.length];

        for(int i1 = 0 ; i1 < absPos.length ; i1++) {
            wayPointsX[i1] = absPos[i1].x;
            wayPointsY[i1] = absPos[i1].y;
        }

        AnimatorSet animation = new AnimatorSet();
        moveAnimators.add(createFloatAnimator(_tarView, "x", duration, 0, 0, new LinearInterpolator(), delay, wayPointsX));
        moveAnimators.add(createFloatAnimator(_tarView, "y", duration, 0, 0, new LinearInterpolator(), delay, wayPointsY));

        animation.playTogether(moveAnimators);

        return animation;
    }




    /**
     * comp_writing specific - uses virtual properties
     *
     * @param _tarView
     * @param duration
     * @param delay
     * @param absPos
     * @return
     */
    static public AnimatorSet configTranslator(View _tarView, long duration, long delay, PointF... absPos) {

        ArrayList<Animator> moveAnimators = new ArrayList<Animator>();

        float[]  wayPointsX  = new float[absPos.length];
        float[]  wayPointsY  = new float[absPos.length];

        for(int i1 = 0 ; i1 < absPos.length ; i1++) {
            wayPointsX[i1] = absPos[i1].x;
            wayPointsY[i1] = absPos[i1].y;
        }

        AnimatorSet animation = new AnimatorSet();

        moveAnimators.add(createFloatAnimator(_tarView, "glyphX", duration, 0, 0, new LinearInterpolator(), delay, wayPointsX));
        moveAnimators.add(createFloatAnimator(_tarView, "glyphY", duration, 0, 0, new LinearInterpolator(), delay, wayPointsY));

        animation.playTogether(moveAnimators);

        return animation;
    }


    static public AnimatorSet configScaler(View _tarView, long duration, long delay, PointF... absPos) {

        ArrayList<Animator> moveAnimators = new ArrayList<Animator>();

        float[]  wayPointsX  = new float[absPos.length];
        float[]  wayPointsY  = new float[absPos.length];

        for(int i1 = 0 ; i1 < absPos.length ; i1++) {
            wayPointsX[i1] = absPos[i1].x;
            wayPointsY[i1] = absPos[i1].y;
        }

        AnimatorSet animation = new AnimatorSet();

        moveAnimators.add(createFloatAnimator(_tarView, "glyphScaleX", duration, 0, 0, new LinearInterpolator(), delay, wayPointsX));
        moveAnimators.add(createFloatAnimator(_tarView, "glyphScaleY", duration, 0, 0, new LinearInterpolator(), delay, wayPointsY));

        animation.playTogether(moveAnimators);

        return animation;
    }



}