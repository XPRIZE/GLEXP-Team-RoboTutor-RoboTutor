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

        this.digitIndex = numSlots;
        this.value = val;
        this.id = id;
        this.operation = operation;
        this.numSlots = numSlots;

        int delta = numSlots - getChildCount();

        while (delta > 0) {
            addText(getChildCount());
            delta--;
        }

        while (delta < 0) {
            delText();
            delta++;
        }

        CAsm_Text currText;

        for (int i = 0; i < getChildCount(); i++) {
            currText = (CAsm_Text) getChildAt(i);
            currText.reset();
        }

        if (id == ASM_CONST.OPERATION) {
            setBackground(getResources().getDrawable(R.drawable.underline));
        }
        else {
            setBackground(null);
        }

        setText();

    }

    private void addText(int index){

        CAsm_Text newText = new CAsm_Text(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(textBoxWidth, textBoxHeight);
        newText.setLayoutParams(lp);

        addView(newText, index);

    }

    private void delText(){

        removeViewAt(getChildCount()-1);

    }

    private void setText() {

        int numSlots = getChildCount();
        String[] digits = CAsm_Util.intToDigits(value, numSlots);
        CAsm_Text curText;

        switch(id){

            case ASM_CONST.REGULAR:

                for (int i = 0; i < numSlots; i++) {

                    curText = (CAsm_Text) getChildAt(i);
                    curText.setText(digits[i]);

                }

                break;

            case ASM_CONST.OPERATION:

                for (int i = 0; i < numSlots; i++) {

                    curText = (CAsm_Text) getChildAt(i);

                    if (i == 0) {
                        curText.setText(operation);
                    }
                    else {
                        curText.setText(digits[i]);
                    }

                }

                break;

            case ASM_CONST.RESULT:

                break;

            case ASM_CONST.OVERHEAD:

                break;

            case ASM_CONST.ANIMATOR:

                break;
        }
    }

    public void performNextDigit() {

        digitIndex--;

        if (digitIndex != getChildCount()-1){

            CAsm_Text prevText = (CAsm_Text) getChildAt(digitIndex+1);
            prevText.setTypeface(null);
            prevText.setBackground(null);

        }

        CAsm_Text curText = (CAsm_Text) getChildAt(digitIndex);

        if (curText.getIsStruck()) {
            // crossed out
            return;
        }

        curText.setTextColor(Color.BLACK);
        curText.setTypeface(null, Typeface.BOLD);
        curText.setBackground(null);

        if (id == ASM_CONST.RESULT) {
            curText.setResult();
        }

    }

    public void resetValue(int index) {

        CAsm_Text t = (CAsm_Text) getChildAt(index);
        t.setText("");

    }

    public void resetAllValues() {

        for (int i = 0; i < numSlots; i++) {
            resetValue(i);
        }
    }


    public Integer getNum() {

        if (id != ASM_CONST.RESULT) {return value;}

        int j = 0;
        int digit;

        for (int i = 0; i < numSlots; i++) {

            CAsm_Text t = (CAsm_Text) getChildAt(i);
            String test = t.getText().toString();

            try {
                digit = Integer.parseInt(test);
            } catch (NumberFormatException e) {
                continue;
            }
            j += (int)((Math.pow(10,(getChildCount()-i-1))*digit));
        }

        return j;
    }

    public Integer getDigit(int index) {

        CAsm_Text t = (CAsm_Text) getChildAt(index);
        return t.getDigit();

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
            currText = (CAsm_Text)getChildAt(i);
            if (currText.getIsClicked()) {
                clickedText = currText;
                return clickedText;
            }
        }
        return null;
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


}