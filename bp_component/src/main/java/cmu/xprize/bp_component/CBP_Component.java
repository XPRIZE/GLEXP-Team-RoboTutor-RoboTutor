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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CBP_Component extends FrameLayout implements ILoadableObject, View.OnClickListener, Animator.AnimatorListener  {

    // Make this public and static so sub-components may use it during json load to instantiate
    // controls on the fly.
    //
    static public Context   mContext;

    protected String        mDataSource;
    private   int           _dataIndex = 0;

    private IBubbleMechanic _mechanics;

    private boolean        correct = false;

    // json loadable
    public String          stimulus_type;
    public String[]        stimulus_data;
    public int             stimulus_count;
    public CBp_Data[]      dataSource;
    public CBpBackground   view_background;

    static final String TAG = "CBP_Component";

    private CBp_Data        _currData;

    CBubble bubble1;
    CBubble bubble2;
    boolean mAnimationStarted = false;
    AnimationDrawable popping;
    ArrayList<AnimatorSet>  animatorList = new ArrayList<AnimatorSet>();




    public CBP_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CBP_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CBP_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }



    public void init(Context context, AttributeSet attrs) {

        inflate(getContext(), R.layout.bubblepop_layout, this);

        mContext = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mDataSource  = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

        // Allow onDraw to be called to start animations
        //
        setWillNotDraw(false);
    }


    public void setDataSource(CBp_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }


    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG,  "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }


    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length)? true:false;
    }


    protected void updateDataSet(CBp_Data data) {

        Log.d(TAG, "test");

        _currData = data;

        if(_mechanics  != null)
            _mechanics.onDestroy();

        switch(data.question_type) {
            case "multiple-choice":
                _mechanics = new CBp_Mechanic_MC(mContext, this);
                break;

            case "rising":
                _mechanics = new CBp_Mechanic_RISE(mContext, this);
                break;
        }

        requestLayout();
    }


    protected void setupWiggle(View target, long delay) {

        AnimatorSet animation = CAnimatorUtil.configWiggle(target, "vertical", 3000, ValueAnimator.INFINITE, delay, 0.16f );

        animation.addListener(this);
        animation.start();

        AnimatorSet stretch = CAnimatorUtil.configStretch(target, "vertical", 2100, ValueAnimator.INFINITE, delay, 1.21f);

        stretch.addListener(this);
        stretch.start();
    }

    protected void setupZoomIn(View target, long duration, long delay, float... scales) {

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
            public void onAnimationEnd(Animator arg0) {
                setupWiggle(bubble1, 0);
                setupWiggle(bubble2, 0);
                bubble1.setOnClickListener(CBP_Component.this);
                bubble2.setOnClickListener(CBP_Component.this);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        animation.start();
    }

    @Override
    public void onClick(View bubble) {

        bubble.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView bubblepop = new ImageView(mContext);

        bubblepop.setScaleX(1.4f);
        bubblepop.setScaleY(1.4f);

        bubblepop.setX(bubble.getX());
        bubblepop.setY(bubble.getY());

        addView(bubblepop, layoutParams);

        popping = new AnimationDrawable();

        popping.addFrame(getResources().getDrawable(R.drawable.bubble_b_1, null), 80);
        popping.addFrame(getResources().getDrawable(R.drawable.bubble_b_2, null), 80);
        popping.addFrame(getResources().getDrawable(R.drawable.bubble_b_3, null), 70);
        popping.addFrame(getResources().getDrawable(R.drawable.bubble_b_4, null), 70);
        popping.addFrame(getResources().getDrawable(R.drawable.bubble_b_5, null), 60);
        popping.addFrame(getResources().getDrawable(R.drawable.bubble_empty, null), 60);
        popping.setOneShot(true);

        bubblepop.setBackground(popping);

//        popping = (AnimationDrawable) getResources().getDrawable(R.drawable.bubble_b_pop, null);
//
//        bubblepop.setBackgroundDrawable(popping);

        post(new Starter());
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        animation.start();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    class Starter implements Runnable {

        public void run() {
            popping.start();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        int width    = r - l;
        int height   = b - t;

        _mechanics.doLayout(width, height, _currData);
    }


    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(!mAnimationStarted && bubble1 != null) {
            mAnimationStarted = true;

            setupZoomIn(bubble1, 600, 0, 0f, 1.0f);
            bubble1.setTranslationZ(5f);

            setupZoomIn(bubble2, 600, 0, 0f, 1.4f);
//            setupWiggle(bubble1, 0);
//            setupWiggle(bubble2, 100);
        }

    }




    public void UpdateValue(int value) {
    }


    protected boolean isCorrect() {


        return correct;
    }


    public boolean allCorrect(int numCorrect) {
        return (numCorrect == dataSource.length);
    }




    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

        addView(view_background);
    }

}
