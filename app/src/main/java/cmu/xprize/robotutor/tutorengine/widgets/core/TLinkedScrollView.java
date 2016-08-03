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

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.util.ILogManager;

public class TLinkedScrollView  extends HorizontalScrollView implements ITutorObjectImpl, View.OnTouchListener{

    private CObjectDelegate mSceneObject;

    private   boolean        mEnableScrolling   = true;
    protected boolean        mIsInitiator       = false;
    protected boolean        mCaptureInitiator  = false;

    private TLinkedScrollView mLinkedScrollView;

    private float            mScrollRatio;
    private boolean          mRatioDirty = true;

    final private String TAG = "LinkScrollView";


    public TLinkedScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public TLinkedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TLinkedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        setOnTouchListener(this);
    }


    public boolean isEnableScrolling() {
        return mEnableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.mEnableScrolling = enableScrolling;
    }

    public float getScrollRange() {
        return computeHorizontalScrollRange();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isEnableScrolling()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void setLinkedScroll(TLinkedScrollView linkedScroll) {
        mLinkedScrollView  = linkedScroll;
    }

    public void updateScrollRatio() {

        if(mRatioDirty && (mLinkedScrollView != null)) {
            mRatioDirty  = false;
            mScrollRatio = getScrollRange() / mLinkedScrollView.getScrollRange();

            if(mIsInitiator)
                mLinkedScrollView.updateScrollRatio();
        }
    }

    public void doProportionalScroll(int x, int y) {

        scrollTo((int) (x * mScrollRatio), y);

        Log.i(TAG, "PROPORTIONAL SCROLL :  x: " + x + " y:" + y);
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);

        if(mIsInitiator && (mLinkedScrollView != null)) {
            updateScrollRatio();
            mLinkedScrollView.doProportionalScroll(x, y);
        }
        //Log.i(TAG, "SCROLLCHANGED :  x: " + x + " y:" + y + "  RANGE: " + computeHorizontalScrollRange() );
    }


//    @Override
//    public void computeScroll() {
//        super.computeScroll();
//
//        Log.i(TAG, "computeScroll");
//        if(mIsInitiator && (mLinkedScrollView != null)) {
//            updateScrollRatio();
//
//            mLinkedScrollView.doProportionalScroll(getScrollX(), getScrollY());
//        }
//    }

    /**
     * We can't capture initiator status until the linked view releases it
     * TODO: We can stop the linked view in mid fling - should try an address that
     *       Note that it just allows them to get out of sync and does no harm.
     */
    private void captureInitiatorStatus() {

        if((mLinkedScrollView != null) && (!mLinkedScrollView.mCaptureInitiator)) {
            mCaptureInitiator              = true;

            mLinkedScrollView.mIsInitiator = false;
            mIsInitiator      = true;
        }

    }


    /**
     * Only one of the linked scrollviews can be the controlling (initiator) view (i.e. the one that drives the updates)
     * otherwise there would be a circular reference.  However in a multi-touch environment both views may
     * be touched before one is released.  Therefore we need to lock mIsInitiator to the first one until
     * it is released. But we can't release mIsInitiator on ACTION_UP or we will inhibit fling events.
     * So we use mCaptureInitiator to inhibit the linked view from capturing the initiator status.
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PointF touchPt;
        boolean result = true;
        final int action = event.getAction();

        touchPt = new PointF(event.getX(), event.getY());

        if (isEnableScrolling()) {

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    captureInitiatorStatus();
                    Log.i(TAG, "DRAWN _ ACTION_DOWN");
                    break;

                case MotionEvent.ACTION_MOVE:
                    captureInitiatorStatus();
                    Log.i(TAG, "DRAWN _ ACTION_MOVE");
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow the other view to capture the initiator status
                    mCaptureInitiator = false;
                    Log.i(TAG, "DRAWN _ ACTION_UP");
                    break;
            }
            result = super.onTouchEvent(event);
        }

        return result;
    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    public void setDataSource(String dataSource) {

    }

    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void postInflate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition ) {
        mSceneObject.wiggle(direction, magnitude, duration, repetition);
    }

    @Override
    public void setAlpha(Float alpha) {
        mSceneObject.setAlpha(alpha);
    }
}
