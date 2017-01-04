package cmu.xprize.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import cmu.xprize.common.R;


public class CLinkedScrollView extends HorizontalScrollView implements View.OnTouchListener {

    protected Context        mContext;

    private   boolean        mEnableScrolling   = true;
    protected boolean        mIsInitiator       = false;
    protected boolean        mCaptureInitiator  = false;

    protected boolean        mShowTracker = false;
    protected float          mVisibleRatio;
    protected int            mTrackerWidth;
    private int              mTrackedScroll;
    private Paint            mTrackerPaint;

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

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mShowTracker = a.getBoolean(R.styleable.RoboTutor_show_tracker , false);

            } finally {
                a.recycle();
            }
        }

        setOnTouchListener(this);

        if(mShowTracker) {
            setWillNotDraw(false);

            mTrackerPaint = new Paint();

            mTrackerPaint.setColor(TCONST.TRACKER_COLOR);
            mTrackerPaint.setStyle(Paint.Style.STROKE);
            mTrackerPaint.setStrokeCap(Paint.Cap.ROUND);
            mTrackerPaint.setStrokeWidth(TCONST.TRACKER_WEIGHT);
            mTrackerPaint.setAntiAlias(true);
        }
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

            // Calc the tracker width - used to indicate the current onscreen region of the linked
            // view
            //
            if(mShowTracker) {
                mVisibleRatio = mLinkedScrollView.getWidth() / mLinkedScrollView.getScrollRange();

                mTrackerWidth = (int) (getScrollRange() * mVisibleRatio);
            }
        }
    }


    private void doProportionalScroll(int x, int y) {

        int newScroll = (int) (x * mScrollRatio);

        scrollTo(newScroll, y);

        // This  updates the tracker when not the initiator
        //
        if(mShowTracker) {
            updateTracker(newScroll,  y);
//            mTrackedScroll = newScroll - getScrollX();
//
//            if(mTrackedScroll > 0) {
//                invalidate();
//            }
        }
    }


    private void updateTracker(int newScroll, int y) {

        int oldTracker = mTrackedScroll;

        Log.i(TAG, "PROPORTIONAL SCROLLX  PRE: " + newScroll);
        Log.i(TAG, "PROPORTIONAL SCROLLX  PST: " + getScrollX());

        mTrackedScroll = newScroll - getScrollX();

        // Note that we update whenever there is a change - we wan it to disappear when it hit zero again
        //
        if(mTrackedScroll != oldTracker) {
            invalidate();
        }
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);

        if(mIsInitiator && (mLinkedScrollView != null)) {
            updateScrollRatio();
            mLinkedScrollView.doProportionalScroll(x, y);

//            if(mShowTracker) {
//                updateTracker(x, y);
//            }
        }
    }


    /**
     * This catches the case where the initiator is the tracker - When it is maxed out this causes
     * the tracker to update back to the max point - not an overscroll point
     *
     * @param deltaX
     * @param deltaY
     * @param scrollX
     * @param scrollY
     * @param scrollRangeX
     * @param scrollRangeY
     * @param maxOverScrollX
     * @param maxOverScrollY
     * @param isTouchEvent
     * @return
     */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

        if(mShowTracker) {
            mTrackedScroll = 0;
            invalidate();
        }

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    /**
     * We can't capture initiator status until the linked view releases it
     * TODO: We can stop the linked view in mid fling - should try an address that
     *       Note that it just allows them to get out of sync and does no harm.
     */
    public void captureInitiatorStatus() {

        if((mLinkedScrollView != null) && (!mLinkedScrollView.mCaptureInitiator)) {
            mCaptureInitiator = true;

            mLinkedScrollView.mIsInitiator = false;
            mIsInitiator      = true;
        }
    }
    public void releaseInitiatorStatus() {

        // Allow the other view to capture the initiator status
        mCaptureInitiator = false;
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

                    releaseInitiatorStatus();
                    Log.i(TAG, "DRAWN _ ACTION_UP");
                    break;
            }
            result = super.onTouchEvent(event);
        }

        return result;
    }


    /**
     * TODO: Track Android version changes to ensure they don't clash with this fix
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // NOTE: This is a Kludge to fix the layout issues with nested PercentRelativeLayouts with
        //       aspect. This passes the updated percent based height down the layout chain instead of
        //       parents height which causes the width to lock in an incorrect value since it is based
        //       upon an incorrect height for this child.
        //
        ViewGroup.LayoutParams params = getLayoutParams();

        if(params.height > 0)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.AT_MOST);

        if(params.width > 0)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.AT_MOST);


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    /**
     * TODO: Note: setWillNotDraw seems to set a static ??
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // We need to track in two situations
        // 1. Where the tracking line is scrollable and is scrolling
        // 2. WHere the tracker is scrolling in the view - i.e. the tracking line hits its limit
        //    and the tracker needs to continue to keep pace with the linked scroller
        //
        if(mShowTracker && ((mTrackedScroll > 0) || (getScrollX() > 0))) {

            Rect drawRegion = new Rect();
            getDrawingRect(drawRegion);

            Log.i(TAG, "X: " + getX());
            Log.i(TAG, "SX: " + getScrollX());

            int padding = drawRegion.height() / 5;

            //canvas.drawLine(mTrackedScroll + getScrollX(), drawRegion.bottom - 10, mTrackedScroll + mTrackerWidth + getScrollX(), drawRegion.bottom - 10, mTrackerPaint);

            drawRegion.left  = mTrackedScroll + getScrollX();
            drawRegion.right = mTrackedScroll + getScrollX() + mTrackerWidth ;

            drawRegion.top    += padding / 2;
            drawRegion.bottom -= padding;

            canvas.drawRect(drawRegion, mTrackerPaint);
        }
    }
}
