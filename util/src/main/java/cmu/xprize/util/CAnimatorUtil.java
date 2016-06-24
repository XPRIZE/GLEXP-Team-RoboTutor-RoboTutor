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

package cmu.xprize.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.util.ArrayList;


// TODO: CAnimatorUtil needs to be integrated with the MediaManager so that it can kill animations on demand.

public class CAnimatorUtil {

    static private Animator createFloatAnimator(View _tarView, String prop, float endPt, long duration , int repeat, int mode, AccelerateInterpolator interpolator ) {

        final View    targetView = _tarView;
        ValueAnimator vAnimator  = null;

        vAnimator = ObjectAnimator.ofFloat(_tarView, prop, endPt).setDuration(duration);

        vAnimator.setInterpolator(interpolator);
        vAnimator.setRepeatCount(repeat);
        vAnimator.setRepeatMode(mode);

        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                targetView.invalidate();
            }
        });

        return vAnimator;
    }


    static public void zoomInOut(View _tarView, float scale, long duration ) {

        AnimatorSet animation = new AnimatorSet();

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
                //Functionality here
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        animation.play(createFloatAnimator(_tarView, "scaleX", scale, duration, 1, ValueAnimator.REVERSE, new AccelerateInterpolator(2.0f))).with(createFloatAnimator(_tarView, "scaleY", scale, duration, 1, ValueAnimator.REVERSE, new AccelerateInterpolator(2.0f)));

        animation.start();
    }


    static public void wiggle(View _tarView, String direction, float magnitude, long duration, int repetition ) {

        ArrayList<Animator> wiggleColl = new ArrayList<Animator>();

        AnimatorSet animation = new AnimatorSet();

        if(repetition <= 0)
            repetition = 1;

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
                //Functionality here
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        switch(direction.toLowerCase()) {
            case "vertical":
                for (int i = 0; i < repetition; i++) {
                    wiggleColl.add(createFloatAnimator(_tarView, "y", _tarView.getY() + (_tarView.getHeight() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
                    wiggleColl.add(createFloatAnimator(_tarView, "y", _tarView.getY() - (_tarView.getHeight() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
                }
                break;

            default:
                for (int i = 0; i < repetition; i++) {
                    wiggleColl.add(createFloatAnimator(_tarView, "x", _tarView.getX() + (_tarView.getWidth() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
                    wiggleColl.add(createFloatAnimator(_tarView, "x", _tarView.getX() - (_tarView.getWidth() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
                }
                break;
        }

        animation.playSequentially(wiggleColl);

        animation.start();
    }


    static public void Bounce(View _tarView, float magnitude, float scale, long duration, int repetition ) {

        ArrayList<Animator> bounceColl = new ArrayList<Animator>();

        AnimatorSet animation = new AnimatorSet();

        if(repetition <= 0)
            repetition = 1;

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
                //Functionality here
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        for(int i=0; i< repetition ; i++) {
            bounceColl.add(createFloatAnimator(_tarView, "y", _tarView.getY() + (_tarView.getHeight() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
            bounceColl.add(createFloatAnimator(_tarView, "scaleX", scale, duration, 1, ValueAnimator.REVERSE, new AccelerateInterpolator(2.0f)));

            bounceColl.add(createFloatAnimator(_tarView, "y", _tarView.getY() - (_tarView.getHeight() * magnitude), duration, 1, ValueAnimator.REVERSE, null));
        }

        animation.playSequentially(bounceColl);

        animation.start();
    }

    
}