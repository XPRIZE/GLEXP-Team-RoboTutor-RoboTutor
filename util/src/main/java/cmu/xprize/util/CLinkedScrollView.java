package cmu.xprize.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;


public class CLinkedScrollView extends HorizontalScrollView implements View.OnTouchListener{

    protected Context        mContext;

    private   boolean        mEnableScrolling   = true;
    protected boolean        mIsInitiator       = false;
    protected boolean        mCaptureInitiator  = false;

    private CLinkedScrollView mLinkedScrollView;

    private float            mScrollRatio;
    private boolean          mRatioDirty = true;

    final private String TAG = "LinkScrollView";


    public CLinkedScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public CLinkedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CLinkedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs ) {

        mContext = context;

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

    public void setLinkedScroll(CLinkedScrollView linkedScroll) {
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

        if (mEnableScrolling) {

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // TODO: DBUG DEBUG DEBUG   START

        int size = 1298;

        final int specMode = MeasureSpec.getMode(widthMeasureSpec);
        final int specSize = MeasureSpec.getSize(widthMeasureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }

        // NOTE: This is a Kludge to fix the layout issues with nested PercentRelativeLayouts with
        //       aspect. This passes the updated percent based height down the layout chain instead of
        //       parents height which causes the width to lock in an incorrect value since it is based
        //       upon an incorrect height for this child.
        //
        ViewGroup.LayoutParams params = getLayoutParams();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.AT_MOST);

        // TODO: DBUG DEBUG DEBUG  END

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        Log.d(TAG, "width  : " + getMeasuredWidth());
        Log.d(TAG, "height : " + getMeasuredHeight());

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
