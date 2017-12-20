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

package cmu.xprize.comp_clickmask;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;

import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CDisplayMetrics;
import cmu.xprize.util.TCONST;

import static android.view.View.GONE;
import static cmu.xprize.comp_clickmask.CM_CONST.*;


public class CClickMask extends View implements Animator.AnimatorListener {


    public Context                mContext;

    private Paint                 mPaint             = new Paint();
    private Rect                  mViewRegion        = new Rect();

    private IMaskOwner            mOwner;

    private int                   mColor = Color.parseColor("#75C043");

    private Path                  mask;
    private RectF                 border   = new RectF();
    private ArrayList<CExclusion> exclusions;
    private int[]                 _screenCoord = new int[2];
    private boolean               deferredHide = false;

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;

    public ILogManager            logManager;

    private final  String  TAG = "CClickMask";


    public CClickMask(Context context) {
        super(context);
    }

    public CClickMask(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CClickMask(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);

        setMaskAlpha(128);

        clearExclusions();

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(MASK_SHOWHIDE);
        filter.addAction(MASK_ADDEXCL);
        filter.addAction(MASK_CLREXCL);
        filter.addAction(MASK_SETALPHA);
        filter.addAction(MASK_ANIMATE);

        bReceiver = new ChangeReceiver();

        logManager = CLogManager.getInstance();

        bManager.registerReceiver(bReceiver, filter);
    }


    public void setOwner(IMaskOwner _owner) {
        mOwner = _owner;
    }


    public void showHide(int _show) {

        mOwner.setMasked(_show);
    }


    public void setMaskColor(int newColor) {

        mPaint.setColor(newColor);
    }


    public void setMaskAlpha(int _alpha) {

        if(_alpha > 255) _alpha = 255;
        if(_alpha < 0)   _alpha = 0;

        mPaint.setAlpha(_alpha);
    }


    public void addExclusion(CExclusion _exclude) {

        exclusions.add(_exclude);
    }


    public void clearExclusions() {

        exclusions = new ArrayList<>();
    }


    @Override
    public void setVisibility(int visibility) {

        switch(visibility) {

            case VISIBLE:
                deferredHide = false;
                setAlpha(0);
                super.setVisibility(visibility);
                break;

            case INVISIBLE:
                deferredHide = true;
                break;

            case GONE:
                super.setVisibility(visibility);
                break;
        }
    }



    //************************************************************************
    //************************************************************************
    // AnimatorListener  Start


    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {

        if(deferredHide)
            super.setVisibility(INVISIBLE);
    }

    @Override
    public void onAnimationCancel(Animator animator) {

        if(deferredHide)
            super.setVisibility(INVISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    // AnimatorListener  End
    //************************************************************************
    //************************************************************************



    class ChangeReceiver extends BroadcastReceiver {

        public void onReceive (Context context, Intent intent) {

            switch(intent.getAction()) {

                case MASK_SHOWHIDE:

                    int showmask = intent.getIntExtra(MASK_SHOWHIDE, GONE);

                    showHide(showmask);

                    // Animate the mask
                    //
                    Intent msg = new Intent(MASK_ANIMATE);

                    bManager.sendBroadcast(msg);

                    break;

                case MASK_ANIMATE:

                    Animator fader;

                    if(deferredHide) {
                        fader = CAnimatorUtil.configFadeOut(CClickMask.this, CM_CONST.FADE_TIME);
                    }
                    else {
                        fader = CAnimatorUtil.configFadeIn(CClickMask.this, CM_CONST.FADE_TIME);
                    }
                    fader.start();

                    break;

                // NOTE: Must invalidate when changing exclusions so that onDraw is called before
                //       animation to refresh the animation cache.
                //
                case MASK_ADDEXCL:
                    String exclusiontype = intent.getStringExtra(MASK_TYPE);

                    switch(exclusiontype) {
                        case EXCLUDE_CIRCLE:
                            int exclusionx = intent.getIntExtra(MASK_X, 0);
                            int exclusiony = intent.getIntExtra(MASK_Y, 0);
                            int exclusionr = intent.getIntExtra(MASK_R, 0);

                            // Translate to local coordinate space - i.e. relative to view
                            //
                            getLocationOnScreen(_screenCoord);

                            exclusionx -= _screenCoord[0];
                            exclusiony -= _screenCoord[1];

                            addExclusion(new CExclusion(exclusiontype, exclusionx, exclusiony, exclusionr));
                            invalidate();
                            break;
                        case EXCLUDE_SQUARE:
                            int exclusion_bottom = intent.getIntExtra(MASK_BOTTOM, 0);
                            int exclusion_top = intent.getIntExtra(MASK_TOP, 0);
                            int exclusion_right = intent.getIntExtra(MASK_RIGHT, 0);
                            int exclusion_left = intent.getIntExtra(MASK_LEFT, 0);

                            addExclusion(new CExclusion(exclusiontype, exclusion_left, exclusion_top, exclusion_right, exclusion_bottom, 5, 5));
                            invalidate();
                            break;

                    }
                    break;

                case MASK_CLREXCL:
                    clearExclusions();
                    invalidate();
                    break;

                case MASK_SETCOLOR:
                    int maskColor = intent.getIntExtra(MASK_COLOR, 0);

                    setMaskColor(maskColor);
                    break;

                case MASK_SETALPHA:
                    int maskAlpha = intent.getIntExtra(MASK_ALPHA, 128);

                    setMaskAlpha(maskAlpha);
                    break;
            }
        }
    }


    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        mask = new Path();

        border.set(0,0,getWidth(),getHeight());

        mask.addRect(border, Path.Direction.CW);

        if(exclusions != null) {

            for(CExclusion exclude : exclusions) {

                switch (exclude.type) {
                    case EXCLUDE_CIRCLE:
                        mask.addCircle(exclude.x, exclude.y, exclude.radius, Path.Direction.CCW);
                        break;
                    case EXCLUDE_SQUARE:
                        mask.addRoundRect(exclude.left, exclude.top, exclude.right, exclude.bottom, 30f, 30f, Path.Direction.CCW);
                        break;
                }
            }
        }

        canvas.drawPath(mask, mPaint);
    }

}
