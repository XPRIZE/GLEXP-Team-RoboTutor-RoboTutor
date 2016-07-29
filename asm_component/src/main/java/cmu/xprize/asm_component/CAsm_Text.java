package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.RequiresPermission;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * horizontal layout of edit texts to display numbers + operation. fixed number of slots for
 * between alleys
 */
public class CAsm_Text extends LinearLayout {

    private Context mContext;

    private int digitIndex;
    private int numSlots;
    private int value;
    private int id;

    private String operation;

    private boolean isClicked;

    float scale = getResources().getDisplayMetrics().density;
    final int textSize = (int)(ASM_CONST.textSize*scale);
    final int textBoxWidth = (int)(ASM_CONST.textBoxWidth*scale);
    final int textBoxHeight = (int)(ASM_CONST.textBoxHeight*scale);


    public CAsm_Text(Context context) {

        super(context);
        init(context, null);
    }

    public CAsm_Text(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Text(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        //setClipChildren(false);
        //setClipToPadding(false);
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

        if (id == ASM_CONST.OPERATION) {
            setBackground(getResources().getDrawable(R.drawable.underline));
        }
        else {
            setBackground(null);
        }

        setText();

    }

    private void addText(int index){

        Write_Text newText = new Write_Text(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(textBoxWidth, textBoxHeight);
        newText.setLayoutParams(lp);
        newText.setBackground(null);
        newText.setGravity(Gravity.CENTER);
        newText.setTextSize(textSize);
        newText.setTextColor(Color.BLACK);
        newText.setSingleLine(true);

        addView(newText, index);

    }

    private void delText(){

        removeViewAt(getChildCount()-1);

    }

    private void setText() {

        int numSlots = getChildCount();
        String[] digits = CAsm_Util.intToDigits(value, numSlots);
        Write_Text curText;

        switch(id){

            case ASM_CONST.REGULAR:

                for (int i = 0; i < numSlots; i++) {

                    curText = (Write_Text) getChildAt(i);
                    resetText(curText);
                    curText.setText(digits[i]);

                }

                break;

            case ASM_CONST.OPERATION:

                for (int i = 0; i < numSlots; i++) {

                    curText = (Write_Text) getChildAt(i);
                    resetText(curText);

                    if (i == 0) {
                        curText.setText(operation);
                        curText.setAlpha(1f);
                    }
                    else {
                        curText.setText(digits[i]);
                    }

                }

                break;

            case ASM_CONST.RESULT:

                for (int i = 0; i < numSlots; i++) {
                    curText = (Write_Text) getChildAt(i);
                    resetText(curText);
                }

                break;

            case ASM_CONST.OVERHEAD:

                break;

            case ASM_CONST.ANIMATOR:

                for (int i = 0; i < numSlots; i++) {
                    curText = (Write_Text) getChildAt(i);
                    resetText(curText);
                }

                break;
        }
    }

    public void performNextDigit() {

        Write_Text curText;

        digitIndex--;

        if (digitIndex != getChildCount()-1){

            curText = (Write_Text) getChildAt(digitIndex+1);
            resetText(curText);

        }

        curText = (Write_Text) getChildAt(digitIndex);
        curText.setTextColor(Color.BLACK);
        curText.setAlpha(1.0f);

        if (id == ASM_CONST.RESULT) {
            curText.setEnabled(true);
            curText.setBackground(getResources().getDrawable(R.drawable.back));
            curText.isWritable = true;
        }

    }

    public void resetValue(int index) {

        Write_Text t = (Write_Text) getChildAt(index);
        t.setText("");

    }

    public void resetAllValues() {

        for (int i = 0; i < numSlots; i++) {
            resetValue(i);
        }
    }

    private void resetText(Write_Text toReset) {
        // TODO: change function name?
        toReset.setEnabled(false);
        toReset.isWritable = false;
        toReset.setTextColor(Color.BLACK);
        toReset.setAlpha(.5f);
        toReset.setBackground(null);

    }

    public Integer getNum() {

        if (id != ASM_CONST.RESULT) {return value;}

        int j = 0;
        int digit;

        for (int i = 0; i < numSlots; i++) {

            Write_Text t = (Write_Text) getChildAt(i);
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

        Write_Text t = (Write_Text) getChildAt(index);
        String input = t.getText().toString();

        if (input.equals("") || input.equals(operation)) {
            return null;
        }
        else {
            return Integer.parseInt(input);
        }

    }

    public Write_Text getText(int index) {
        return (Write_Text) getChildAt(index);
    }


    public boolean getIsClicked() {
        if (isClicked) {
            isClicked = false;
            return true;
        } else return false;
    }

    public Write_Text findClickedField() {
        Write_Text currText;
        Write_Text clickedText;
        for (int i = 0; i < getChildCount(); i++) {
            currText = (Write_Text)getChildAt(i);
            if (currText.getIsClicked()) {
                clickedText = currText;
                return clickedText;
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            setIsClicked(true);
        }
        return false;
    }

    public void setIsClicked(boolean b) {
        isClicked = b;
    }

}