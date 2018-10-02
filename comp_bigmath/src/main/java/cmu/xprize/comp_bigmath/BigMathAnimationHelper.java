package cmu.xprize.comp_bigmath;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import static cmu.xprize.comp_bigmath.BM_CONST.HUN_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.ONE_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.OPA_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.OPB_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.RESULT_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.TEN_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.WATERFALL_DELAY;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 10/1/18.
 */

public class BigMathAnimationHelper {

    private Context _activity;
    private BigMathLayoutHelper _layout;
    private ViewGroup _viewGroup;
    private BigMathProblemState _problemState;



    // ------------------
    // BEGIN NEEDED FIELDS
    // ------------------


    // configuration settings
    private boolean EXPAND_HIT_BOX = false;



    public BigMathAnimationHelper(Context activity, BigMathLayoutHelper layout, ViewGroup viewGroup) {
        this._activity = activity;
        this._layout = layout;
        this._viewGroup = viewGroup;
    }

    public void setProblemState(BigMathProblemState problemState) {
        this._problemState = problemState;
    }

    public View.OnClickListener generateWaterfallClickListener(String numLoc, String digit, String operation) {
        if (operation.equals("+")) {
            return new BaseTenOnClickAnimateWaterfall(numLoc, digit);
        } else {
            return new BaseTenOnClickAnimateWaterfallSubtract(digit);
        }
    }

    public View.OnClickListener generateWaterfallClickListener(String numLoc, String digit) {
        return new BaseTenOnClickAnimateWaterfall(numLoc, digit);
    }

    /**
     * this animation is lit
     *
     * BUG_605... waterfall subtract
     * BUG_605... waterfall should not happen if digit is not highlighted
     */
    class BaseTenOnClickAnimateWaterfall implements View.OnClickListener {

        private final String _numLoc;
        private final String _digit;

        public BaseTenOnClickAnimateWaterfall(String _numLoc, String _digit) {
            this._numLoc = _numLoc;
            this._digit = _digit;
        }

        @Override
        public void onClick(View view) {

            if (_digit.equals(ONE_DIGIT) && !_problemState.isCanTapOnes()) return;
            if (_digit.equals(TEN_DIGIT) && !_problemState.isCanTapTens()) return;
            if (_digit.equals(HUN_DIGIT) && !_problemState.isCanTapHuns()) return;

            int numUnits = getDigitValue(_numLoc, _digit);

            // for each digit to animate, do the waterfall thing
            for (int i = 0; i < numUnits; i++)
                (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveSequential(_numLoc, _digit, _problemState.getData().operation.equals("-"));
                    }
                }, WATERFALL_DELAY * i);
        }
    }

    /**
     * Utility function for getting digithTh digit of the number at numLoc
     * @param numLoc
     * @param digit
     * @return
     */
    private int getDigitValue(String numLoc, String digit) {
        int number = 0;

        switch(numLoc) {
            case OPA_LOCATION:
                number = _problemState.getData().dataset[0];
                break;

            case OPB_LOCATION:
                number = _problemState.getData().dataset[1];
                break;
        }

        switch(digit) {
            case ONE_DIGIT:
                return getOnesDigit(number);

            case TEN_DIGIT:
                return getTensDigit(number);

            case HUN_DIGIT:
                return getHunsDigit(number);
        }

        return -1;
    }

    public View.OnClickListener generateSingleClickListener(String digit) {
        return new BaseTenOnClickAnimateMe(digit);
    }

    /**
     * Click Listener that moves clicked View to the next available space.
     */
    class BaseTenOnClickAnimateMe implements View.OnClickListener {

        private final String _digit;

        BaseTenOnClickAnimateMe(String digit) {
            this._digit = digit;
        }
        @Override
        public void onClick(View v) {
            Log.wtf("YELLOW", "moving " + _digit);
            Log.wtf("YELLOW", "moving parent =" + v.getResources().getResourceEntryName(((LinearLayout) v.getParent()).getId()));
            Log.wtf("YELLOW", "moving id ==\t" + v.getResources().getResourceEntryName(v.getId()));
            Log.wtf("YELLOW", "movable? ==\t" + ((MovableImageView) v).isMovable);
            animateView(this._digit, (MovableImageView) v);
        }
    }

    public View.OnClickListener generateSequentialClickListener(String digit) {
        return new BaseTenOnClickAnimateSequential(digit);
    }

    /**
     * Click Listener that moves next available View to next available space.
     */
    class BaseTenOnClickAnimateSequential implements View.OnClickListener {

        private final String _digit;

        BaseTenOnClickAnimateSequential(String digit) {
            this._digit = digit;
        }
        @Override
        public void onClick(View v) {
            if (!EXPAND_HIT_BOX) return; // might be redundant?

            if (_digit.equals(ONE_DIGIT) && !_problemState.isCanTapOnes()) return;
            if (_digit.equals(TEN_DIGIT) && !_problemState.isCanTapTens()) return;
            if (_digit.equals(HUN_DIGIT) && !_problemState.isCanTapHuns()) return;

            String numberLoc = v.getTag().toString();
            moveSequential(numberLoc, _digit, _problemState.getData().operation.equals("-")); // MATH_MISC (1) should be an overall conditional that prevents bad dots from moving
        }
    }

    /**
     * Move this view to next available location in specified digit column.
     *
     * @param digit
     * @param v
     */
    public void animateView(String digit, MovableImageView v) {


        if (EXPAND_HIT_BOX) return; // might be redundant?

        if (v.isMoving || !v.isMovable) return;  // MATH_MISC (tap)

        switch(_problemState.getData().operation) {
            case "+":
                moveForAddition(v, digit);
                break;

            case "-":
                moveForSubtraction(v, digit);
        }
    }

    /**
     *
     * Move this view to the next available location in the ones column of the subtrahend row.
     *
     * @param v
     */
    public void moveOneBorrow(View v) {
        if (((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForSubtraction(v, ONE_DIGIT);
    }

    /**
     *
     * Move this view to the next available location in the tens column of the subtrahend row.
     *
     * @param v
     */
    public void moveTenBorrow(View v) {
        if (((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForSubtraction(v, TEN_DIGIT);
    }

    /**
     *
     * Move this view to the next available location in the tens column of the sum row.
     *
     * @param v the view clicked.
     */
    public void moveTenCarry(View v) {

        if(((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForAddition(v, TEN_DIGIT);

    }

    /**
     *
     * Move this view to the next available location in the hundreds column of the sum row.
     *
     * @param v the view clicked.
     */
    public void moveHunCarry(View v) {

        if(((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForAddition(v, HUN_DIGIT);

    }

    /**
     * Instead of moving the clicked View, move the next available view in this row
     *
     * @param numberLoc
     * @param digit
     */
    public void moveSequential(final String numberLoc, final String digit, boolean subtract) {

        if (_problemState.isCarrying()) {
            return;
        }

        final MovableImageView oldView = determineNextTopView(numberLoc, digit);
        // if we're at zero, this will return null
        if(oldView == null) {
            return;
        }
        final MovableImageView newView = determineNextResultView(digit, false);
        final MovableImageView helperView = determineNextResultView(digit, true);

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {

            boolean isCarry = false;

            @Override
            public void onAnimationStart(Animator animation) {

                switch(digit) {
                    case HUN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                _problemState.decrementCurrentOpAHun();
                                break;

                            case OPB_LOCATION:
                                _problemState.decrementCurrentOpBHun();
                                break;
                        }

                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        _problemState.incrementResultHun();
                        break;

                    case TEN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                _problemState.decrementCurrentOpATen();
                                break;

                            case OPB_LOCATION:
                                _problemState.decrementCurrentOpBTen();
                                break;
                        }

                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        // if this is the tenth ten, prepare a carry!
                        if (_problemState.getResultTen() == 9) {
                            this.isCarry = true;
                            _problemState.setCarrying(true); // prevent other animations...
                            _problemState.setResultTen(0);
                        } else{
                            _problemState.incrementResultTen();
                        }
                        break;

                    case ONE_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                _problemState.decrementCurrentOpAOne();
                                break;

                            case OPB_LOCATION:
                                _problemState.decrementCurrentOpBOne();
                                break;
                        }


                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        // if this is the tenth one, prepare a carry!
                        if (_problemState.getResultOne() == 9) {
                            this.isCarry = true;
                            _problemState.setCarrying(true); // prevent other animations
                            _problemState.setResultOne(0);
                        } else {
                            _problemState.incrementResultOne();
                        }
                        break;
                }


            }

            @Override
            public void onAnimationEnd(Animator animation) {


                switch(digit) {
                    case HUN_DIGIT:

                        // to show when animating
                        final MovableImageView helperView = determineNextResultView(digit, true);
                        if (helperView != null) {
                            helperView.setVisibility(View.INVISIBLE);
                        }
                        break;

                    case TEN_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(TEN_DIGIT);
                        }
                        break;

                    case ONE_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(ONE_DIGIT);
                        }
                        break;

                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();

    }

    /**
     * UI-details
     * Moves a BaseTen ImageView of the type digitPlace.
     * MATH_HESITATE we need to know when we've got nothing left to move!
     * can we clean up some animation???
     *
     * TODO make separate types for one, ten, hun
     * @param v the view to move
     * @param digitPlace the column to move to.
     */
    private void moveForAddition(View v, final String digitPlace) {

        Log.wtf("YELLOW", "y u no move?");
        // CARRY... prevent tapping while in carry mode
        if(_problemState.isCarrying()) {
            return;
        }

        final MovableImageView oldView = (MovableImageView) v;
        Log.wtf("YELLOW", "oldView id ==\t" + oldView.getResources().getResourceEntryName(oldView.getId()));
        final MovableImageView newView = determineNextResultView(digitPlace, false);

        // an animation helper
        final MovableImageView helperView = determineNextResultView(digitPlace, true);

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {

            boolean isCarry = false;

            @Override
            public void onAnimationStart(Animator animation) {

                // once moved, it can't move again
                oldView.isMovable = false;

                // show both newView (destination) and helperView (temporary placeholder)
                newView.setVisibility(View.VISIBLE);
                if (helperView != null) {
                    helperView.setVisibility(View.VISIBLE);
                }

                switch(digitPlace) {
                    case HUN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        _problemState.incrementResultHun();
                        break;

                    case TEN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        // if this is the tenth ten, prepare a carry!
                        // MATHFIX_LAYOUT (1) NEXT NEXT NEXT populate the second row instead!!
                        if (_problemState.getResultTen() == 9) {
                            this.isCarry = true;
                            _problemState.setCarrying(true); // prevent other animations...
                            _problemState.setResultTen(0);
                        } else{
                            _problemState.incrementResultTen();
                        }
                        break;

                    case ONE_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        // if this is the tenth one, prepare a carry!
                        // MATHFIX_LAYOUT (1) NEXT NEXT NEXT populate the second row instead!!
                        if (_problemState.getResultOne() == 9) {
                            this.isCarry = true;
                            _problemState.setCarrying(true); // prevent other animations
                            _problemState.setResultOne(0);
                        } else {
                            _problemState.incrementResultOne();
                        }
                        break;
                }


            }

            @Override
            public void onAnimationEnd(Animator animation) {


                switch(digitPlace) {
                    case HUN_DIGIT:

                        // to show when animating
                        final MovableImageView helperView = determineNextResultView(digitPlace, true);
                        if (helperView != null) {
                            helperView.setVisibility(View.INVISIBLE);
                        }
                        break;

                    case TEN_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(TEN_DIGIT);
                        }
                        break;

                    case ONE_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(ONE_DIGIT);
                        }
                        break;

                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();

    }

    /**
     * Instead of moving clicked View, move the right-most View.
     *
     * Returns null when there are none left in the row.
     *
     * @param digit
     * @param numberLoc
     * @return
     */
    private MovableImageView determineNextTopView(String numberLoc, String digit) {

        int currentDigit = 0;

        // this could be neater... but it works
        switch(digit) {
            case HUN_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = _problemState.getCurrentOpAHun();
                        break;

                    case OPB_LOCATION:
                        currentDigit = _problemState.getCurrentOpBHun();
                        break;
                }

                break;

            case TEN_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = _problemState.getCurrentOpATen();
                        break;

                    case OPB_LOCATION:
                        currentDigit = _problemState.getCurrentOpBTen();
                        break;
                }

                break;

            case ONE_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = _problemState.getCurrentOpAOne();
                        break;

                    case OPB_LOCATION:
                        currentDigit = _problemState.getCurrentOpBOne();
                        break;
                }
                break;
        }

        return _layout.getBaseTenConcreteUnitView(numberLoc, digit, currentDigit);
    }

    /**
     * Determines the next open spot in the sum row.
     *
     * @param digitPlace either one, ten, or hun
     * @return the next open View
     */
    private MovableImageView determineNextResultView(String digitPlace, boolean helper) {

        int currentDigit, nextDigit;

        switch (digitPlace) {
            case HUN_DIGIT:
                currentDigit = _problemState.getResultHun();
                break;

            case TEN_DIGIT:
                currentDigit = _problemState.getResultTen();
                break;

            case ONE_DIGIT:
                currentDigit = _problemState.getResultOne();
                break;

            default:
                return null;

        }

        nextDigit = currentDigit + 1;

        return _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, digitPlace, nextDigit, helper);
    }

    /**
     * Start the carry animation from 10 ones to 1 ten, or 10 tens to 1 hun.
     * @param digit can be "one" or "ten"
     */
    private void startCarryAnimation(final String digit) {

        Log.i("TURN THE LIGHTS OFF", "CARRY ME HOME");
        final MovableImageView oldView, newView;

        switch (digit) {
            case ONE_DIGIT:
                oldView = (MovableImageView) _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, ONE_DIGIT, 1);
                newView = (MovableImageView) findViewById(R.id.carry_ten);
                break;


            case TEN_DIGIT:
            default:
                oldView = (MovableImageView) _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, TEN_DIGIT, 1);
                newView = (MovableImageView) findViewById(R.id.carry_hun);
                break;
        }

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1500);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.wtf("TURN THE LIGHTS OFF", "CARRY ME HOME");
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);

                switch(digit) {

                    case ONE_DIGIT:

                        MovableImageView[] ones = new MovableImageView[10];
                        for (int i = 1; i <= 10; i++) {
                            ones[i-1] = _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, ONE_DIGIT, i);
                            //ones[i-1].setVisibility(View.INVISIBLE);
                            ones[i-1].setImageDrawable(getDrawable(R.drawable.empty_1));
                        }
                        break;

                    case TEN_DIGIT:
                        MovableImageView[] ten = new MovableImageView[10];
                        for (int i = 1; i <= 10; i++) {
                            ten[i-1] = _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, TEN_DIGIT, i);
                            //ten[i-1].setVisibility(View.INVISIBLE);
                            ten[i-1].setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        }
                    default:
                        break;

                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                newView.isMoving = false;
                _problemState.setCarrying(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    public View.OnClickListener generateWaterfallSubtractClickListener(String digit) {
        return new BaseTenOnClickAnimateWaterfallSubtract(digit);
    }

    class BaseTenOnClickAnimateWaterfallSubtract implements View.OnClickListener {


        private final String _digit;

        public BaseTenOnClickAnimateWaterfallSubtract(String _digit) {
            this._digit = _digit;
        }

        @Override
        public void onClick(View view) {

            if (_digit.equals(ONE_DIGIT) && !_problemState.isCanTapOnes()) return;
            if (_digit.equals(TEN_DIGIT) && !_problemState.isCanTapTens()) return;
            if (_digit.equals(HUN_DIGIT) && !_problemState.isCanTapHuns()) return;

            int numUnits = getDigitValue(OPB_LOCATION, _digit); // gets how many in opB...

            // if we're moving to result, move whatever is left in the OPA row
            final boolean hasDotsLeft = hasDotsLeftToSubtract(_digit);
            if (!hasDotsLeft) {
                if (_digit.equals(ONE_DIGIT)) numUnits = _problemState.getCurrentOpAOne();
                else if (_digit.equals(TEN_DIGIT)) numUnits = _problemState.getCurrentOpATen();
                else if (_digit.equals(HUN_DIGIT)) numUnits = _problemState.getCurrentOpAHun();
            }

            // for each digit to animate, do the waterfall thing
            for (int i = 0; i < numUnits; i++)
                (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveSequentialSubtraction(_digit, !hasDotsLeft);
                    }
                }, WATERFALL_DELAY * i);
        }
    }

    /**
     * Check if we have already subtracted the dots
     * @param digit
     * @return
     */
    private boolean hasDotsLeftToSubtract(String digit) {
        // check that subtract has things l
        switch(digit) {
            case HUN_DIGIT:
                if (_problemState.getSubtrahendHun() == getHunsDigit(_problemState.getData().dataset[1])) {
                    return false;
                }
                break;

            case TEN_DIGIT:
                if (_problemState.getSubtrahendTen() == getTensDigit(_problemState.getData().dataset[1])) {
                    return false ;
                }
                break;

            case ONE_DIGIT:
                if (_problemState.getSubtrahendOne() == getOnesDigit(_problemState.getData().dataset[1])) {
                    return false;
                }
        }

        return true;
    }


    /**
     * Move the next subtraction in line. Will only move up to OPB number...
     * @param digitPlace
     */
    private void moveSequentialSubtraction(final String digitPlace, final boolean toResult) {
        // BUG_605 NEXT NEXT NEXT
        final MovableImageView oldView = determineNextTopView(OPA_LOCATION, digitPlace);
        // if we're at zero, this will return null
        if (oldView == null) return;

        final MovableImageView newView = toResult ? determineNextResultView(digitPlace, false) : determineNextSubtrahendView(digitPlace, false);
        final MovableImageView helperView = toResult ? determineNextResultView(digitPlace, true) : determineNextSubtrahendView(digitPlace, true);

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

                // BUG_605 (***) awfully alike
                // once moved, it can't move again
                oldView.isMovable = false;
                // show both newView (destination) and helperView (temporary placeholder)
                newView.setVisibility(View.VISIBLE);
                helperView.setVisibility(View.VISIBLE);

                switch(digitPlace)  {
                    case HUN_DIGIT:
                        helperView.setImageDrawable(getDrawable(!toResult ? R.drawable.blue_ghost_100 : R.drawable.empty_100));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        _problemState.decrementCurrentOpAHun();
                        // either increment subrahend or result, depending on where we're moving the dot
                        if (!toResult) _problemState.incrementSubtrahendHun();
                        else _problemState.incrementResultHun();

                        break;

                    case TEN_DIGIT:
                        helperView.setImageDrawable(getDrawable(!toResult ? R.drawable.blue_ghost_10_h : R.drawable.empty_10_h));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        _problemState.decrementCurrentOpATen();
                        // either increment subrahend or result, depending on where we're moving the dot
                        if (!toResult) _problemState.incrementSubtrahendTen();
                        else _problemState.incrementResultTen();
                        break;

                    case ONE_DIGIT:
                        helperView.setImageDrawable(getDrawable(!toResult ? R.drawable.blue_ghost_1 : R.drawable.empty_1));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        _problemState.decrementCurrentOpAOne();
                        // either increment subrahend or result, depending on where we're moving the dot
                        if (!toResult) _problemState.incrementSubtrahendOne();
                        else _problemState.incrementResultOne();
                        break;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animSet.start();
    }

    /**
     * Generate an animation that moves from oldView to newView, for duration.
     * @param oldView
     * @param newView
     * @param duration
     * @return
     */
    private AnimatorSet generateViewToViewAnimatorSet(View oldView, View newView, long duration) {

        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);

        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(duration);

        return animSet;
    }

    /**
     * UI-details
     * Moves a BaseTen ImageView of the type digitPlace.
     *
     * @param v
     * @param digitPlace hun, ten, or one
     */
    private void moveForSubtraction(View v, final String digitPlace) {

        // this statement checks if we have already tapped enough to fill the subtrahend
        // for example, if the subtrahend is 304 and we have already filled 3 hundreds spaces, return.
        if(!hasDotsLeftToSubtract(digitPlace)) return;

        final MovableImageView oldView = (MovableImageView) v;
        final MovableImageView newView = determineNextSubtrahendView(digitPlace, false);
        // helper for better animation
        final MovableImageView helperView = determineNextSubtrahendView(digitPlace, true);


        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

                // BUG_605 (***) awfully alike
                // once moved, it can't move again
                oldView.isMovable = false;
                // show both newView (destination) and helperView (temporary placeholder)
                newView.setVisibility(View.VISIBLE);
                helperView.setVisibility(View.VISIBLE);

                switch(digitPlace)  {
                    case HUN_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_100));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        _problemState.incrementSubtrahendHun();
                        break;

                    case TEN_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_10_h));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        _problemState.incrementSubtrahendTen();
                        break;

                    case ONE_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_1));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        _problemState.incrementSubtrahendOne();
                        break;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /**
     * Determines the next open spot in the subtrahend row (opB).
     *
     * @param digitPlace either one, ten, or hun
     * @param helper
     * @return the next View
     */
    private MovableImageView determineNextSubtrahendView(String digitPlace, boolean helper) {

        int currentDigit, nextDigit;

        switch(digitPlace) {
            case HUN_DIGIT:
                currentDigit = _problemState.getSubtrahendHun();
                break;

            case TEN_DIGIT:
                currentDigit = _problemState.getSubtrahendTen();
                break;

            case ONE_DIGIT:
                currentDigit = _problemState.getSubtrahendOne();
                break;

            default:
                return null;
        }


        nextDigit = currentDigit + 1;

        return _layout.getBaseTenConcreteUnitView(OPB_LOCATION, digitPlace, nextDigit, helper);
    }

    /**
     *
     * Move the minuend BaseTenobjects to the result row.
     * This will only occur if all the items have been tapped.
     * MATH_FEEDBACK (8) this should be called again
     */
    public void moveMinuendToResult() {
        if (_problemState.getSubtrahendHun() != getHunsDigit(_problemState.getData().dataset[1]) ||
                _problemState.getSubtrahendTen() != getTensDigit(_problemState.getData().dataset[1]) ||
                _problemState.getSubtrahendOne() != getOnesDigit(_problemState.getData().dataset[1])) {

            Toast.makeText(_activity, "Please subtract until you fill the subtrahend", Toast.LENGTH_LONG).show();
            return;
        }

        // do hundreds
        for (int i = 5; i > 0; i--) {
            MovableImageView hun = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, HUN_DIGIT, i);
            if(hun.isMovable) {
                moveForAddition(hun, HUN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }

        // do tens
        for (int i = 10; i > 0; i--) {
            MovableImageView ten = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, TEN_DIGIT, i);
            if(ten.isMovable) {
                moveForAddition(ten, TEN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }
        // borrowed tens
        if (_problemState.isHasBorrowedHun()) {
            for (int i = 10; i > 0; i --) {
                MovableImageView ten = _layout.getBaseTenConcreteUnitView("borrow", TEN_DIGIT, i);
                if (ten.isMovable) {
                    moveForAddition(ten, TEN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
                }
            }
        }

        // do ones
        for (int i = 10; i > 0; i--) {
            MovableImageView one = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, ONE_DIGIT, i);
            if(one.isMovable) {
                moveForAddition(one, ONE_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }
        // borrowed ones
        if (_problemState.isHasBorrowedTen()) {
            for (int i = 10; i > 0; i --) {
                MovableImageView one = _layout.getBaseTenConcreteUnitView("borrow", ONE_DIGIT, i);
                if (one.isMovable) {
                    moveForAddition(one, ONE_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
                }
            }
        }
    }

    /**
     *
     * Animate the borrowing of a hundred into the tens column.
     */
    public void borrowHun() {
        if (_problemState.isHasBorrowedHun()) return;

        _problemState.setBorrowing(true);

        final MovableImageView oldView = determineNextTopView(OPA_LOCATION, HUN_DIGIT);
        final MovableImageView newView = (MovableImageView) findViewById(R.id.borrow_hun_helper);

        if (!oldView.isMovable && _problemState.getCurrentOpAHun() == 0) {
            // MATH_SUBTRACT this should be able to take from the borrow column...
            Toast.makeText(_activity, "You have no hundreds to borrow!", Toast.LENGTH_SHORT).show();
            return;
        }

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);
                newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                // our hundred is accounted for
                oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                oldView.isMovable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                newView.setVisibility(View.INVISIBLE);
                _problemState.setBorrowing(false);
                _problemState.setHasBorrowedHun(true);

                for(int i=1; i <= 10; i++) {
                    MovableImageView ten = _layout.getBaseTenConcreteUnitView("borrow", TEN_DIGIT, i);
                    ten.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /**
     *
     * An interaction placeholder. Borrow Ten Ones from the Ten Column.
     */
    public void borrowTen() {
        if (_problemState.isHasBorrowedTen()) return;

        _problemState.setBorrowing(true);

        final MovableImageView oldView = determineNextTopView(OPA_LOCATION, TEN_DIGIT);
        // special case...
        if (!oldView.isMovable && _problemState.getCurrentOpATen() == 0) {
            // MATH_SUBTRACT this should be able to take from the borrow column...
            Toast.makeText(_activity, "You have no tens to borrow!", Toast.LENGTH_SHORT).show();
            return;
        }
        final MovableImageView newView = (MovableImageView) findViewById(R.id.borrow_ten_helper);

        AnimatorSet animSet = generateViewToViewAnimatorSet(oldView, newView, 1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);
                newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                oldView.isMovable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // hide the ten that moved and...
                newView.setVisibility(View.INVISIBLE);
                _problemState.setBorrowing(false);
                _problemState.setHasBorrowedTen(true);

                // ... replace it with ten ones
                for(int i=1; i <= 10; i++) {
                    MovableImageView one = _layout.getBaseTenConcreteUnitView("borrow", ONE_DIGIT, i);
                    one.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }


    /**
     * A helper so i don't have to rewrite all these references
     *
     * @param resID
     * @return
     */
    private View findViewById(int resID) {
        return _viewGroup.findViewById(resID);
    }

    /**
     * A helper so I don't have to rewrite all these references
     * @param resID
     * @return
     */
    private Drawable getDrawable(int resID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return _activity.getDrawable(resID);
        } else {
            return _activity.getResources().getDrawable(resID, _activity.getTheme());
        }
    }
}

