package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

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
    int textBoxWidth = (int)(ASM_CONST.textBoxWidth*scale);
    int textBoxHeight = (int)(ASM_CONST.textBoxHeight*scale);

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

        CAsm_TextLayout newTextLayout;

        while (delta > 0) {
            newTextLayout = new CAsm_TextLayout(this.mContext);
            newTextLayout.addText(0);
            newTextLayout.addText(1);
            // MATHFIX_LAYOUT add TextLayout to TextLayout ???
            addView(newTextLayout, getChildCount());
            // distinction between original and added
            setBackgroundColor(ASM_CONST.DEBUG_TEXTLAYOUT_1_COLOR);
            newTextLayout.setBackgroundColor(ASM_CONST.DEBUG_TEXTLAYOUT_2_COLOR);
            Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "addView CAsm_TextLayout to CAsm_TextLayout");

            delta--;
        }

        while (delta < 0) {
            delChild();
            delta++;
        }

        CAsm_Text currText;

        for (int i = 0; i < getChildCount(); i++) {
            currText = getTextLayout(i).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(-1, i,0, "reset()");
            currText.reset();
            currText = getTextLayout(i).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(-1, i, 1, "reset()");
            currText.reset();
        }


        // TODO: this will be a memory leak - must release the drawables explicitly
        //
        if (id == ASM_CONST.OPERATOR_ROW)
            setBackground(getResources().getDrawable(R.drawable.underline, null)); // MATHFIX_LAYOUT here's where the line comes in!
        else
            setBackground(null);

        setDigitTextForAddSubtract();


    }

    private void addText(int index){

        textBoxWidth = (int)(ASM_CONST.textBoxWidth*scale);
        textBoxHeight = (int)(ASM_CONST.textBoxHeight*scale);

        CAsm_Text newText = new CAsm_Text(getContext());

        // MATHFIX_LAYOUT LayoutParams
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(textBoxWidth, textBoxHeight);
        newText.setLayoutParams(lp);

        // MATHFIX_LAYOUT add Text to TextLayout
        addView(newText, index);
        Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "addView CAsm_Text:" + index + " to CAsm_TextLayout:" + id);

    }

    private void delChild(){

        removeViewAt(getChildCount()-1);

    }

    /**
     * MATHFIX_2 finally, this is where the Digit Texts are set
     */
    private void setDigitTextForAddSubtract() {

        int numSlots = getChildCount();
        String[] digits = CAsm_Util.intToDigits(value, numSlots);
        CAsm_TextLayout curTextLayout;

        switch(id){

            case ASM_CONST.OPERAND_ROW:

                for (int i = 0; i < numSlots; i++) {

                    curTextLayout = getTextLayout(i); // √√√
                    curTextLayout.getText(1).setText(digits[i]); // √√√
                    curTextLayout.getText(1).setBackgroundColor(ASM_CONST.DEBUG_TEXT_COLOR); // √√√
                    ASM_CONST.logAnnoyingReference(-1, i, 1, "setText to digit");
                    Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "setText CAsm_Text:1 in CAsm_TextLayout:" + i + " to " + digits[i]);

                }

                break;

            case ASM_CONST.OPERATOR_ROW:

                digits[0] = operation;

                for (int i = 0; i < numSlots; i++) {

                    curTextLayout = getTextLayout(i); // √√√
                    curTextLayout.getText(1).setText(digits[i]); // √√√
                    curTextLayout.getText(1).setBackgroundColor(ASM_CONST.DEBUG_TEXT_COLOR); // √√√
                    ASM_CONST.logAnnoyingReference(-1, i, 1, "setText to digit");
                    Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "setText CAsm_Text:1 in CAsm_TextLayout:" + i + " to " + digits[i]);

                }

                break;

            case ASM_CONST.RESULT_ROW:

                break;

            case ASM_CONST.CARRY_BRW:

                break;

            case ASM_CONST.ANIMATOR1:

                break;

            case ASM_CONST.ANIMATOR2:

                break;

            case ASM_CONST.ANIMATOR3:

                break;
        }
    }

    public void performNextDigit() {

        digitIndex--;

        // MATHFIX_2 UI: update previously added digit
        if (digitIndex != getChildCount()-1 ) {
            CAsm_Text prevText = getTextLayout(digitIndex + 1).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(-1, digitIndex + 1, 1, "performNextDigit()");
            prevText.setTypeface(null);
            prevText.setBackground(null);
            prevText.setWritable(false);
            prevText.setAlpha(0.5f);

            prevText = getTextLayout(digitIndex + 1).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(-1, digitIndex + 1, 0, "performNextDigit()");
            prevText.setTypeface(null);
            prevText.setBackground(null);
            prevText.setWritable(false);
            prevText.setAlpha(0.5f);
        }

        CAsm_Text curText = getTextLayout(digitIndex).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(-1, digitIndex, 1, "performNextDigit()");

        if (curText.getIsStruck()) {
            // crossed out
            return;
        }

        // MATHFIX_2 UI: bold current digit
        curText.setTextColor(Color.BLACK);
        curText.setTypeface(null, Typeface.BOLD);
        curText.setBackground(null);

        if (id == ASM_CONST.RESULT_ROW) {
            getTextLayout(digitIndex).getText(0).reset(); // √√√
            ASM_CONST.logAnnoyingReference(-1, digitIndex, 0, "reset()");
            curText.setResult();
        }

    }

    public void resetValue(int index) {

        CAsm_TextLayout textLayout = (CAsm_TextLayout) getChildAt(index);
        textLayout.getText(0).setText(""); // √√√
        ASM_CONST.logAnnoyingReference(-1, index, 0, "setText(blank)");
        textLayout.getText(1).setText(""); // √√√
        ASM_CONST.logAnnoyingReference(-1, index, 1, "setText(blank)");

    }

    public void resetAllValues() {
        for (int i = 0; i < numSlots; i++) {
            resetValue(i);
        }
    }

    /**
     * Gets the CUMULATIVE number value of this and all TextLayout... i.e. a 3 in the hundreds column returns 300 + t * 10 + o
     *
     * @return
     */
    public Integer getNum() {

//        if (id != ASM_CONST.RESULT_ROW) {return value;}

        int j = 0;
        int digit;

        int num = numSlots;
        for (int i = 1; i < num; i++) {

            CAsm_Text t = getTextLayout(i).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(-1, i, 1, "TextLayout.getNum()");
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

    /**
     * Gets the digit value of this TextLayout, i.e. a 3 in the hundreds column returns 3.
     *
     * @param index
     * @return
     */
    public Integer getDigit(int index) {

        CAsm_Text t = getTextLayout(index).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(-1, index, 1, "TextLayout.getDigit()");
        return t.getDigit();

    }

    /**
     * MATHFIX_2 also look for references of this
     *
     * @param index
     * @return
     */
    public CAsm_TextLayout getTextLayout(int index) {
        return (CAsm_TextLayout) getChildAt(index);
    }

    /**
     * MATHFIX_2 look for references of this, there are a LOT
     *
     * @param index
     * @return
     */
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
            currText = getTextLayout(i).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(-1, i, 0, "TextLayout.findClickedText()");
            if (currText.getIsClicked()) {
                clickedTextIndex = 0;
                clickedTextLayoutIndex = i;
                clickedText = currText;
                return clickedText;
            }
            currText = getTextLayout(i).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(-1, i, 1, "TextLayout.findClickedText()");
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
            getTextLayout(i).setBackground(null); // √√√
            ASM_CONST.logAnnoyingReference(-1, i, -1, "setBackground(null)");
        }
    }
}