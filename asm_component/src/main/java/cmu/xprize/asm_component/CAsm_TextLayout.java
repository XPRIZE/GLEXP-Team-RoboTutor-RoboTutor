package cmu.xprize.asm_component;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cmu.xprize.util.CAnimatorUtil;

/**
 * horizontal layout of edit texts to display numbers + operation. fixed number of slots for
 * between alleys
 */
public class CAsm_TextLayout extends LinearLayout {

    private Context mContext;

    private int digitIndex;
    private int numSlots;
    private int value;
    private int id;

    private String operation;

    private boolean isClicked;
    private int clickedTextIndex = -1;
    private int clickedTextLayoutIndex = -1;

    float scale = getResources().getDisplayMetrics().density;
    final int textBoxWidth = (int)(ASM_CONST.textBoxWidth*scale);
    final int textBoxHeight = (int)(ASM_CONST.textBoxHeight*scale);

    public CAsm_TextLayout(Context context) {

        super(context);
        init(context, null);
    }

    public CAsm_TextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_TextLayout(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void update(int id, int val, String operation, int numSlots) {

        this.digitIndex = operation.equals("x")? numSlots-2 : numSlots;
        this.value = val;
        this.id = id;
        this.operation = operation;
        this.numSlots = numSlots;

        int delta = numSlots - getChildCount();

        CAsm_TextLayout newTextLayout;

        while (delta > 0) {
            newTextLayout = new CAsm_TextLayout(this.mContext);
            newTextLayout.addText(0);
            newTextLayout.addText(1);
            addView(newTextLayout, getChildCount());

            delta--;
        }

        while (delta < 0) {
            delChild();
            delta++;
        }

        CAsm_Text currText;

        for (int i = 0; i < getChildCount(); i++) {
            currText = getTextLayout(i).getText(0);
            currText.reset();
            currText = getTextLayout(i).getText(1);
            currText.reset();
        }

        if (operation.equals("x")) {
            if (id == ASM_CONST.OPERATION_MULTI) {
                //setBackground(getResources().getDrawable(R.drawable.underline));
                getChildAt(1).setBackground(getResources().getDrawable(R.drawable.underline));
                getChildAt(2).setBackground(getResources().getDrawable(R.drawable.underline));
            } else
                setBackground(null);
            setTextForMultiplication();
        } else {
            if (id == ASM_CONST.OPERATION)
                setBackground(getResources().getDrawable(R.drawable.underline));
            else
                setBackground(null);
            setTextForAddSubtract();
        }


    }

    private void addText(int index){

        CAsm_Text newText = new CAsm_Text(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(textBoxWidth, textBoxHeight);
        newText.setLayoutParams(lp);

        addView(newText, index);

    }

    private void delChild(){

        removeViewAt(getChildCount()-1);

    }

    private void setTextForAddSubtract() {

        int numSlots = getChildCount();
        String[] digits = CAsm_Util.intToDigits(value, numSlots);
        CAsm_TextLayout curTextLayout;

        switch(id){

            case ASM_CONST.REGULAR:

                for (int i = 0; i < numSlots; i++) {

                    curTextLayout = getTextLayout(i);
                    curTextLayout.getText(1).setText(digits[i]);

                }

                break;

            case ASM_CONST.OPERATION:

                digits[0] = operation;

                for (int i = 0; i < numSlots; i++) {

                    curTextLayout = getTextLayout(i);
                    curTextLayout.getText(1).setText(digits[i]);

                }

                break;

            case ASM_CONST.RESULT:

                break;

            case ASM_CONST.OVERHEAD:

                break;

            case ASM_CONST.ANIMATOR1:

                break;

            case ASM_CONST.ANIMATOR2:

                break;

            case ASM_CONST.ANIMATOR3:

                break;
        }
    }

    private void setTextForMultiplication() {

        int numSlots = getChildCount() - 2;
        String[] digits = CAsm_Util.intToDigits(value, numSlots);
        CAsm_TextLayout curTextLayout;

        switch(id){

            case ASM_CONST.REGULAR_MULTI:

                for (int i = 0; i < numSlots; i++) {
                    curTextLayout = getTextLayout(i);
                    curTextLayout.getText(1).setText(digits[i]);
                }

                break;

            case ASM_CONST.OPERATION_MULTI:

                digits[1] = operation;
                for (int i = 0; i < numSlots; i++) {
                    curTextLayout = getTextLayout(i);
                    curTextLayout.getText(1).setText(digits[i]);
                }

                break;

            default : break;
        }
    }

    public void performNextDigit() {

        digitIndex--;

        if ((digitIndex != getChildCount()-1 && !operation.equals("x")) ||
                (digitIndex != getChildCount()-2 && operation.equals("x"))) {
            CAsm_Text prevText = getTextLayout(digitIndex + 1).getText(1);
            prevText.setTypeface(null);
            prevText.setBackground(null);
            prevText.setWritable(false);
            prevText.setAlpha(0.5f);

            prevText = getTextLayout(digitIndex + 1).getText(0);
            prevText.setTypeface(null);
            prevText.setBackground(null);
            prevText.setWritable(false);
            prevText.setAlpha(0.5f);
        }

        CAsm_Text curText = getTextLayout(digitIndex).getText(1);

        if (curText.getIsStruck()) {
            // crossed out
            return;
        }

        curText.setTextColor(Color.BLACK);
        curText.setTypeface(null, Typeface.BOLD);
        curText.setBackground(null);

        if(operation.equals("x")) {
            if ((id == ASM_CONST.RESULT_OR_ADD_MULTI_PART1)) {
                for (int i = 1; i < digitIndex; i++) {
                    getTextLayout(i).getText(0).reset();
                    if (getTextLayout(i).getText(1).isWritable) {
                        getTextLayout(i).getText(1).reset();
                        getTextLayout(i).getText(1).setWritable(true);
                    }
                }
                getTextLayout(digitIndex).getText(0).reset();
            }
        } else if (id == ASM_CONST.RESULT) {
            getTextLayout(digitIndex).getText(0).reset();
            curText.setResult();
        }

    }

    public void resetValue(int index) {

        CAsm_TextLayout textLayout = (CAsm_TextLayout) getChildAt(index);
        textLayout.getText(0).setText("");
        textLayout.getText(1).setText("");

    }

    public void resetAllValues() {
        for (int i = 0; i < numSlots; i++) {
            resetValue(i);
        }
    }

    public Integer getNum() {

//        if (id != ASM_CONST.RESULT) {return value;}

        int j = 0;
        int digit;

        int num = operation.equals("x")? numSlots-2 : numSlots;
        for (int i = 1; i < num; i++) {

            CAsm_Text t = getTextLayout(i).getText(1);
            String test = t.getText().toString();

            try {
                digit = Integer.parseInt(test);
            } catch (NumberFormatException e) {
                continue;
            }
            j += (int)((Math.pow(10,(num-i-1)) * digit));
        }

        return j;
    }

    public Integer getDigit(int index) {

        CAsm_Text t = getTextLayout(index).getText(1);
        return t.getDigit();

    }

    public CAsm_TextLayout getTextLayout(int index) {
        return (CAsm_TextLayout) getChildAt(index);
    }

    public CAsm_Text getText(int index) {
        return (CAsm_Text) getChildAt(index);
    }

    public boolean getIsClicked() {

        if (isClicked) {
            isClicked = false;
            return true;
        } else return false;
    }

    public CAsm_Text findClickedText() {

        CAsm_Text currText;
        CAsm_Text clickedText;
        for (int i = 0; i < getChildCount(); i++) {
            currText = getTextLayout(i).getText(0);
            if (currText.getIsClicked()) {
                clickedTextIndex = 0;
                clickedTextLayoutIndex = i;
                clickedText = currText;
                return clickedText;
            }
            currText = getTextLayout(i).getText(1);
            if (currText.getIsClicked()) {
                clickedTextIndex = 1;
                clickedTextLayoutIndex = i;
                clickedText = currText;
                return clickedText;
            }
        }
        return null;
    }

    public int findClickedTextIndex() {
        return clickedTextIndex;
    }

    public int findClickedTextLayoutIndex() {
        return clickedTextLayoutIndex;
    }

    public void setIsClicked(boolean _isClicked) {
        isClicked = _isClicked;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            setIsClicked(true);
        }
        return false;
    }

    public void resetAllBackground() {
        for (int i = 0; i < numSlots; i++) {
            getTextLayout(i).setBackground(null);
        }
    }
}