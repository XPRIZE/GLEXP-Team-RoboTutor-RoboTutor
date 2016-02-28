package cmu.xprize.mn_component;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CMn_Component extends LinearLayout{

    private Context      mContext;
    private LinearLayout mAlleyContainer;

    public CMn_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        mContext = context;

//        inflate(context, R.layout.mn_layout, this);

//        addAlley();
//        addAlley();
        addAlley();
        addAlley();
        addAlley();
        addAlley();


    }


    private void addAlley() {

        // Defining the layout parameters of the TextView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        lp.weight  = 1;

        // Setting the parameters on the TextView
        CMn_Alley alley = new CMn_Alley(mContext);
        alley.setLayoutParams(lp);

        addView(alley);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l,t,r,b);
    }
//
//    /**
//     * Position the children during a layout pass if the orientation of this
//     * LinearLayout is set to {@link #HORIZONTAL}.
//     *
//     * @see #getOrientation()
//     * @see #setOrientation(int)
//     * @see #onLayout(boolean, int, int, int, int)
//     * @param left
//     * @param top
//     * @param right
//     * @param bottom
//     */
//    void layoutHorizontal(int left, int top, int right, int bottom) {
//        final boolean isLayoutRtl = false;
//        final int paddingTop = mPaddingTop;
//
//        int childTop;
//        int childLeft;
//
//        // Where bottom of child should go
//        final int height = bottom - top;
//        int childBottom = height - mPaddingBottom;
//
//        // Space available for child
//        int childSpace = height - paddingTop - mPaddingBottom;
//
//        final int count = getVirtualChildCount();
//
//        final int majorGravity = mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
//        final int minorGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
//
//        final boolean baselineAligned = mBaselineAligned;
//
//        final int[] maxAscent = mMaxAscent;
//        final int[] maxDescent = mMaxDescent;
//
//        final int layoutDirection = getLayoutDirection();
//        switch (Gravity.getAbsoluteGravity(majorGravity, layoutDirection)) {
//            case Gravity.RIGHT:
//                // mTotalLength contains the padding already
//                childLeft = mPaddingLeft + right - left - mTotalLength;
//                break;
//
//            case Gravity.CENTER_HORIZONTAL:
//                // mTotalLength contains the padding already
//                childLeft = mPaddingLeft + (right - left - mTotalLength) / 2;
//                break;
//
//            case Gravity.LEFT:
//            default:
//                childLeft = mPaddingLeft;
//                break;
//        }
//
//        int start = 0;
//        int dir = 1;
//        //In case of RTL, start drawing from the last child.
//        if (isLayoutRtl) {
//            start = count - 1;
//            dir = -1;
//        }
//
//        for (int i = 0; i < count; i++) {
//            int childIndex = start + dir * i;
//            final View child = getVirtualChildAt(childIndex);
//
//            if (child == null) {
//                childLeft += measureNullChild(childIndex);
//            } else if (child.getVisibility() != GONE) {
//                final int childWidth = child.getMeasuredWidth();
//                final int childHeight = child.getMeasuredHeight();
//                int childBaseline = -1;
//
//                final LinearLayout.LayoutParams lp =
//                        (LinearLayout.LayoutParams) child.getLayoutParams();
//
//                if (baselineAligned && lp.height != LayoutParams.MATCH_PARENT) {
//                    childBaseline = child.getBaseline();
//                }
//
//                int gravity = lp.gravity;
//                if (gravity < 0) {
//                    gravity = minorGravity;
//                }
//
//                switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
//                    case Gravity.TOP:
//                        childTop = paddingTop + lp.topMargin;
//                        if (childBaseline != -1) {
//                            childTop += maxAscent[INDEX_TOP] - childBaseline;
//                        }
//                        break;
//
//                    case Gravity.CENTER_VERTICAL:
//                        // Removed support for baseline alignment when layout_gravity or
//                        // gravity == center_vertical. See bug #1038483.
//                        // Keep the code around if we need to re-enable this feature
//                        // if (childBaseline != -1) {
//                        //     // Align baselines vertically only if the child is smaller than us
//                        //     if (childSpace - childHeight > 0) {
//                        //         childTop = paddingTop + (childSpace / 2) - childBaseline;
//                        //     } else {
//                        //         childTop = paddingTop + (childSpace - childHeight) / 2;
//                        //     }
//                        // } else {
//                        childTop = paddingTop + ((childSpace - childHeight) / 2)
//                                + lp.topMargin - lp.bottomMargin;
//                        break;
//
//                    case Gravity.BOTTOM:
//                        childTop = childBottom - childHeight - lp.bottomMargin;
//                        if (childBaseline != -1) {
//                            int descent = child.getMeasuredHeight() - childBaseline;
//                            childTop -= (maxDescent[INDEX_BOTTOM] - descent);
//                        }
//                        break;
//                    default:
//                        childTop = paddingTop;
//                        break;
//                }
//
//                if (hasDividerBeforeChildAt(childIndex)) {
//                    childLeft += mDividerWidth;
//                }
//
//                childLeft += lp.leftMargin;
//                setChildFrame(child, childLeft + getLocationOffset(child), childTop,
//                        childWidth, childHeight);
//                childLeft += childWidth + lp.rightMargin +
//                        getNextLocationOffset(child);
//
//                i += getChildrenSkipCount(child, childIndex);
//            }
//        }
//    }
//


}
