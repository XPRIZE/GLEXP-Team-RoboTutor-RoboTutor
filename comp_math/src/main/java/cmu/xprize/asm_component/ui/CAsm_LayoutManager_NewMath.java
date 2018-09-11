package cmu.xprize.asm_component.ui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import cmu.xprize.asm_component.CAsm_Component;
import cmu.xprize.asm_component.CAsm_Dot_New;
import cmu.xprize.asm_component.CAsm_Text;
import cmu.xprize.asm_component.CAsm_Util;
import cmu.xprize.asm_component.R;
import cmu.xprize.util.MathUtil;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 7/18/18.
 */

public class CAsm_LayoutManager_NewMath implements CAsm_LayoutManagerInterface {

    private CAsm_Component _component;
    private Context _context;

    public CAsm_LayoutManager_NewMath(CAsm_Component _component, Context context) {
        this._component = _component;
        this._context = context;
    }

    @Override
    public void initialize() {
        CAsm_Component.inflate(_context, R.layout.newer_math, _component);
    }

    @Override
    public void initializeProblem(int op1, int op2, int result, String operator) {

        // update operator
        setOperator(operator);

        int maxDigits = CAsm_Util.maxDigits(new int[]{op1, op2, result}); // this makes things tricky

        // make the first operand display data
        // MATHFIX_3 NEXT NEXT only display proper number of digits. Might be better to move to LinearLayout.

        // the new way...
        TextView hunsDigit = _component.findViewById(R.id.top_hun_text);
        hunsDigit.setText(op1 >= 100 ? String.valueOf(MathUtil.getHunsDigit(op1)) : null);

        TextView tensDigit = _component.findViewById(R.id.top_ten_text);
        tensDigit.setText(op1 >= 10 ? String.valueOf(MathUtil.getTensDigit(op1)) : null);

        TextView onesDigit = _component.findViewById(R.id.top_one_text);
        onesDigit.setText(String.valueOf(MathUtil.getOnesDigit(op1)));

        // MATHFIX_3 align left properly. Might be better to move to LinearLayout.

        // UPDATE ALLEY OPERAND 2

        hunsDigit = _component.findViewById(R.id.mid_hun_text);
        hunsDigit.setText(op1 >= 100 ? String.valueOf(MathUtil.getHunsDigit(op2)) : null);

        tensDigit = _component.findViewById(R.id.mid_ten_text);
        tensDigit.setText(op1 >= 10 ? String.valueOf(MathUtil.getTensDigit(op2)) : null);

        onesDigit = _component.findViewById(R.id.mid_one_text);
        onesDigit.setText(String.valueOf(MathUtil.getOnesDigit(op2)));

        // RESET RESULT DIGITS
        hunsDigit = _component.findViewById(R.id.low_hun_text);
        hunsDigit.setText("");

        tensDigit = _component.findViewById(R.id.low_ten_text);
        tensDigit.setText("");

        onesDigit = _component.findViewById(R.id.low_one_text);
        onesDigit.setText("");

        // hide all dotbags
        _component.findViewById(R.id.top_dotbag).setVisibility(View.INVISIBLE);
        _component.findViewById(R.id.mid_dotbag).setVisibility(View.INVISIBLE);
        _component.findViewById(R.id.low_dotbag).setVisibility(View.INVISIBLE);

        // for now... show the ones column
    }

    /**
     * Access the View for a Dot.
     *
     * @param numberLoc
     * @param index
     * @return
     */
    private CAsm_Dot_New getDot(String numberLoc, int index) {
        String viewId = numberLoc + "_dot_" + index;
        int resID = _component.getResources().getIdentifier(viewId, "id", _context.getPackageName());
        return (CAsm_Dot_New) _component.findViewById(resID);
    }

    /**
     * Set the operator to either plus or minus
     * @param operator
     */
    private void setOperator(String operator) {
        TextView operatorView = _component.findViewById(R.id.mid_op_text);
        operatorView.setText(operator);
    }


    /**
     * MATHFIX_BUILD see "CAsm_TextLayout.performNextDigit()"
     * @param digit
     */
    @Override
    public void emphasizeCurrentDigitColumn(String digit) {


        Log.d("UI_REF", "emphasizeCurrentDigitColumn()");

        CAsm_Text op1Digit, op2Digit;
        CAsm_Text resultDigit;

        switch(digit) {

            case "one":
                Log.d("UI_REF", "highlighting 1 column");
                op1Digit = _component.findViewById(R.id.top_one_text);
                op2Digit = _component.findViewById(R.id.mid_one_text);
                resultDigit = _component.findViewById(R.id.low_one_text);
                break;

            case "ten":
                op1Digit = _component.findViewById(R.id.top_ten_text);
                op2Digit = _component.findViewById(R.id.mid_ten_text);
                resultDigit = _component.findViewById(R.id.low_ten_text);
                break;

            case "hun":
            default:
                op1Digit = _component.findViewById(R.id.top_hun_text);
                op2Digit = _component.findViewById(R.id.mid_hun_text);
                resultDigit = _component.findViewById(R.id.low_ten_text);
                break;
        }

        emphasizeDigitView(op1Digit);
        emphasizeDigitView(op2Digit);

        setResultBox(resultDigit);
    }

    /**
     * Highlight digit... could move to layout.
     *
     * @param digitView
     */
    private void emphasizeDigitView(CAsm_Text digitView) {
        // highlight...
        highlightText(digitView);



        digitView.setTextColor(Color.BLACK);
        digitView.setTypeface(null, Typeface.BOLD);
        digitView.setBackground(null);
    }

    /**
     * Runs the animation that highlights the current column
     * @param t
     */
    private void highlightText(final CAsm_Text t) {
        int colorStart = Color.YELLOW;
        int colorEnd = Color.TRANSPARENT;
        ValueAnimator v = ValueAnimator.ofObject(new ArgbEvaluator(), colorStart, colorEnd);
        v.setDuration(_component.getResources().getInteger(R.integer.highlightDigitDuration));
        v.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Log.wtf("COLOR_ME", animator.getAnimatedValue().toString());
                t.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        v.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                if (t.isWritable) {
                   t.setResult();
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                if (t.isWritable) {
                    t.setResult();

                }
            }
        });
        v.start();
    }

    /**
     * Make this TextBox a result box (i.e. frame it in a box);
     * @param digit
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setResultBox(CAsm_Text digit) {
        digit.setResult();
        digit.setOnTouchListener(resultBoxPopupShower);
    }

    private View.OnTouchListener resultBoxPopupShower = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d("AYY_LMAO", "showing popup");

            // MATHFIX_2 POPUP put in a popup with a writer inside of it.
            view.performClick();
            return true;
        }
    };


    @Override
    public void showDotBagsForDigit(String digit, int op1, int op2) {

        int op1Digit, op2Digit;
        switch(digit) {
            case "one":
                op1Digit = MathUtil.getOnesDigit(op1);
                op2Digit = MathUtil.getOnesDigit(op2);
                break;

            case "ten":
                op1Digit = MathUtil.getTensDigit(op1);
                op2Digit = MathUtil.getTensDigit(op2);
                break;

            case "hun":
            default:
                op1Digit = MathUtil.getHunsDigit(op1);
                op2Digit = MathUtil.getHunsDigit(op2);
                break;
        }

        _component.findViewById(R.id.top_dotbag).setVisibility(View.VISIBLE);
        // show dots [1, N] and hide dots [N:]
        for (int i = 1; i < 10; i++) {
            CAsm_Dot_New topDot = getDot("top", i);
            topDot.setVisibility(i <= op1Digit? View.VISIBLE : View.INVISIBLE);
        }

        _component.findViewById(R.id.mid_dotbag).setVisibility(View.VISIBLE);
        // show dots [1, N] and hide dots [N:]
        for (int i = 1; i < 10; i++) {
            CAsm_Dot_New midDot = getDot("mid", i);
            midDot.setVisibility(i <= op2Digit ? View.VISIBLE : View.INVISIBLE);
        }

        _component.findViewById(R.id.low_dotbag).setVisibility(View.VISIBLE);
        for (int i = 1; i < 10; i++) {
            CAsm_Dot_New midDot = getDot("low", i);
            midDot.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void wiggleDigitAndDotbag(String numLocation, String digit) {

        switch(numLocation) {
            case "top":
                wiggleText(_component.findViewById(R.id.top_one_text));
                wiggleDotbag(_component.findViewById(R.id.top_dotbag));
                break;

            case "mid":
                wiggleText(_component.findViewById(R.id.mid_one_text));
                wiggleDotbag(_component.findViewById(R.id.mid_dotbag));
                break;

            case "op":
                wiggleOperator(_component.findViewById(R.id.mid_op_text));
                break;
        }

    }

    private void wiggleText(View view) {
        wiggle(view, 300, 1, 0, .3f);
    }

    private void wiggleDotbag(View view) {
        wiggle(view, 300, 1, 0, .05f);
    }

    private void wiggleOperator(View view) {
        wiggle(view, 300, 1, 0, .5f);
    }

    private void wiggle(View v, long duration, int repetition, long delay, float magnitude) {
        float currTranslation = v.getTranslationX();
        float offset = magnitude * v.getWidth();
        float[] pts = {currTranslation, offset + currTranslation, currTranslation, currTranslation - offset, currTranslation};

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "translationX", pts);
        anim.setDuration(duration);
        anim.setRepeatCount(repetition);
        anim.setStartDelay(delay);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }


    @Override
    public void wrongDigit(String digit) {

        CAsm_Text digitText;
        switch(digit) {
            case "one":
                digitText = _component.findViewById(R.id.low_one_text);
                break;

            case "ten":
                digitText = _component.findViewById(R.id.low_ten_text);
                break;

            case "hun":
            default:
                digitText = _component.findViewById(R.id.low_hun_text);
                break;
        }
        // only for debugging
        digitText.setText(String.valueOf((int) (Math.random() * 10)));
        digitText.setTextColor(Color.RED);
    }


}
