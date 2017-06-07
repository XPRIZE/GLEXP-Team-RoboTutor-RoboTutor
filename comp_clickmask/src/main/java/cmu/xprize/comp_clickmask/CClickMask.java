/**
 Copyright 2015 Kevin Willows
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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import cmu.xprize.util.CDisplayMetrics;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_clickmask.CM_CONST.*;


public class CClickMask extends View implements View.OnTouchListener {


    public Context                mContext;

    private Paint                 mPaint             = new Paint();
    private Rect                  mViewRegion        = new Rect();

    private IMaskOwner            mOwner;

    private int                   mColor = Color.parseColor("#75C043");

    private Path                  mask;
    private RectF                 border   = new RectF();
    private ArrayList<CExclusion> exclusions;

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;


    public CClickMask(Context context) {
        super(context);
        init(context, null);
    }

    public CClickMask(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CClickMask(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);

        setMaskAlpha(128);

        clearExclusions();

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(TCONST.MASK_SHOWHIDE);
        filter.addAction(TCONST.MASK_ADDEXCL);
        filter.addAction(TCONST.MASK_CLREXCL);
        filter.addAction(TCONST.MASK_SETALPHA);

        bReceiver = new ChangeReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }


    public void setOwner(IMaskOwner _owner) {
        mOwner = _owner;
    }

    public void showHide(boolean _show) {

        mOwner.setMasked(_show);
    }

    public void setMaskColor(int newColor) {

        mPaint.setColor(newColor);
    }


    public void setMaskAlpha(int _alpha) {

        mPaint.setAlpha(_alpha);
    }


    public void addExclusion(CExclusion _exclude) {

        exclusions.add(_exclude);
    }


    public void clearExclusions() {

        exclusions = new ArrayList<>();
    }


    class ChangeReceiver extends BroadcastReceiver {

        public void onReceive (Context context, Intent intent) {

            float[] point = intent.getFloatArrayExtra(TCONST.SCREENPOINT);

            switch(intent.getAction()) {
                case TCONST.MASK_SHOWHIDE:
                    Boolean showmask = intent.getBooleanExtra(TCONST.MASK_SHOWHIDE, false);

                    showHide(showmask);
                    break;

                case TCONST.MASK_ADDEXCL:
                    String exclusiontype = intent.getStringExtra(TCONST.MASK_TYPE);

                    switch(exclusiontype) {
                        case EXCLUDE_CIRCLE:
                            int exclusionx = intent.getIntExtra(TCONST.MASK_X, 0);
                            int exclusiony = intent.getIntExtra(TCONST.MASK_Y, 0);
                            int exclusionr = intent.getIntExtra(TCONST.MASK_R, 0);

                            addExclusion(new CExclusion(exclusiontype, exclusionx, exclusiony, exclusionr));
                            break;
                    }
                    break;

                case TCONST.MASK_CLREXCL:
                    clearExclusions();
                    break;

                case TCONST.MASK_SETCOLOR:
                    int maskColor = intent.getIntExtra(TCONST.MASK_COLOR, 0);

                    setMaskColor(maskColor);
                    break;

                case TCONST.MASK_SETALPHA:
                    int maskAlpha = intent.getIntExtra(TCONST.MASK_ALPHA, 128);

                    setMaskColor(maskAlpha);
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
                }
            }
        }

        canvas.drawPath(mask, mPaint);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }
}
