package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TableRow;

import cmu.xprize.util.TCONST;

/**
 * all multiplication-specific operations implemented here
 */
public class CAsm_MechanicMultiply extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicMultiply";
    public CAsm_MechanicMultiply(CAsm_Component mComponent) {
        super.init(mComponent);

        //User could choose the order of writing result digits
        CAsm_TextLayout resultTextLayout = allAlleys.get(resultOrAddInMultiPart1).getTextLayout();
        for(int i = 1; i < resultTextLayout.getChildCount(); i++) {
            resultTextLayout.getTextLayout(i).getText(0).setVisibility(View.INVISIBLE);
            resultTextLayout.getTextLayout(i).getText(1).setResult();
        }
    }

    protected String operation = "x";

    public void next() {
        super.next();

        CAsm_TextLayout resultInAddition = allAlleys.get(addInMultiPart3).getTextLayout().getTextLayout(mComponent.numSlots-1);
        mComponent.overheadText = resultInAddition.getText(1);
        mComponent.overheadText.setTypeface(null, Typeface.BOLD);
        mComponent.overheadTextSupplement = resultInAddition.getText(0);
        mComponent.overheadTextSupplement.setTypeface(null, Typeface.BOLD);

        for(int i = resultOrAddInMultiPart1 + 1; i < allAlleys.size(); i++)
            allAlleys.get(i).getTextLayout().resetAllValues();

    }

    @Override
    public void nextDigit() {

        if (mComponent.digitIndex == mComponent.numSlots-1) {
            super.nextDigit();
        } else {
//            CAsm_DotBag firstBag, secondBag, resultBag;
            if(mComponent.downwardResult)
                allAlleys.get(resultIndexForMultiBackup).nextDigit(mComponent.downwardResult);
            else
                allAlleys.get(resultOrAddInMultiPart1).nextDigit(mComponent.downwardResult);

/*            firstBag = allAlleys.get(firstBagIndexForMulti).getDotBag();
            secondBag = allAlleys.get(secondBagIndexForMulti).getDotBag();
            resultBag = allAlleys.get(resultOrAddInMultiPart1).getDotBag();

            resultBag.setRows(firstBag.getCols());
            resultBag.setCols(secondBag.getCols());*/

            mComponent.setDotBagsVisible(true, mComponent.digitIndex);
        }

    }

    @Override
    public void preClickSetup() {

/*
        if (mComponent.digitIndex != mComponent.numSlots-1) {
            return;
        }

*/
/*        CAsm_DotBag firstBag = allAlleys.get(firstBagIndex).getDotBag();
        firstBag.wiggle(300, 1, 100, .05f);*//*


        allAlleys.get(secondBagIndexForMulti).getDotBag().setIsClickable(false);
*/

    }

    @Override
    public void handleClick() {

        super.handleClick();

        int dy;

        CAsm_DotBag firstBag = allAlleys.get(firstBagIndexForMulti).getDotBag();
        CAsm_Text secondText = allAlleys.get(secondBagIndexForMulti).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);

        final CAsm_Dot clickedDot = firstBag.findClickedDot();
        if (clickedDot == null) {return;}

        if(!mComponent.downwardResult) {
            dowanwardResult();

            CAsm_Text text = allAlleys.get(resultOrAddInMultiPart1).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);
            text.setText(secondText.getText());
            text.setTypeface(null, Typeface.BOLD);
            allAlleys.get(resultOrAddInMultiPart1).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(0).setTypeface(null, Typeface.BOLD);
            dy = secondText.getHeight() + mComponent.alleyMargin;
            dowanwardTextAnimation(text, dy);
            clickedDot.setHollow(true);

            mComponent.overheadVal = secondText.getDigit();
        } else {
            CAsm_TextLayout secondInAddition = allAlleys.get(addInMultiPart2).getTextLayout().getTextLayout(mComponent.numSlots-1);
            CAsm_Text text = secondInAddition.getText(1);
            if(!text.getText().equals("")) {
                clickedDot.setIsClickable(true);
                return;
            }

            secondInAddition.setBackground(secondInAddition.getResources().getDrawable(R.drawable.underline));
            secondInAddition.getText(0).setText("+");
            secondInAddition.getText(0).setTypeface(null, Typeface.BOLD);
            text.setText(secondText.getText());
            text.setTypeface(null, Typeface.BOLD);

            dy = secondText.getHeight() + mComponent.alleyMargin + text.getHeight();
            dowanwardTextAnimation(text, dy);
            clickedDot.setHollow(true);

            mComponent.overheadVal += secondText.getDigit();
            if(mComponent.overheadVal > 9)
                mComponent.overheadTextSupplement.setResult();
            mComponent.overheadText.setResult();
        }
//        mComponent.playChime();
    }

    private void dowanwardTextAnimation(CAsm_Text text, int dy) {
        text.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(text, "translationY", 0);
        anim.setDuration(1000);
        setAllParentsClip(text, false);
        anim.start();
    }

    private void dowanwardResult() {
        CAsm_TextLayout oriResult = allAlleys.get(resultOrAddInMultiPart1).getTextLayout();
        CAsm_TextLayout newResult = allAlleys.get(resultIndexForMultiBackup).getTextLayout();
        CAsm_TextLayout newSecond = allAlleys.get(addInMultiPart3).getTextLayout();
        CAsm_Text oriText, newText;
        for (int i = 1; i < mComponent.numSlots; i++) {
            oriText = oriResult.getTextLayout(i).getText(1);
            newText = newResult.getTextLayout(i).getText(1);
            newText.setText(oriText.getText());
            newText.setBackground(oriText.getBackground());
            newText.setAlpha(oriText.getAlpha());
            newText.setWritable(oriText.isWritable);
            oriText.reset();
        }
        newSecond.setBackground(newSecond.getResources().getDrawable(R.drawable.underline));
        mComponent.downwardResult = true;
    }

    public void correctOverheadText() {
        // whenever they put in the right overhead text
        mComponent.overheadText.cancelResult();
        mComponent.overheadTextSupplement.cancelResult();

        if(mComponent.overheadVal == mComponent.corValue) {
            mComponent.overheadText.setBackground(null);
            mComponent.overheadTextSupplement.setBackground(null);
            return;
        }
        CAsm_TextLayout firstTextLayout = allAlleys.get(resultOrAddInMultiPart1).getTextLayout().getTextLayout(mComponent.numSlots-1);
        CAsm_Text firstText1 = firstTextLayout.getText(0);
        CAsm_Text firstText2 = firstTextLayout.getText(1);
        CAsm_TextLayout secondTextLayout = allAlleys.get(addInMultiPart2).getTextLayout().getTextLayout(mComponent.numSlots-1);
        CAsm_Text secondText1 = secondTextLayout.getText(0);
        CAsm_Text secondText2 = secondTextLayout.getText(1);
        CAsm_Text thirdText1 = allAlleys.get(addInMultiPart3).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(0);
        CAsm_Text thirdText2 = allAlleys.get(addInMultiPart3).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);
        if(!thirdText1.getText().equals("")) {
            firstText1.setText(thirdText1.getText());
            firstText1.setVisibility(View.VISIBLE);
            thirdText1.setText("");
            thirdText1.reset();
        }
        firstText2.setText(thirdText2.getText());
        secondTextLayout.setBackground(null);
        secondText1.setText("");
        secondText2.setText("");
        thirdText2.setText("");
        thirdText2.reset();

        int dy = secondTextLayout.getHeight() + mComponent.alleyMargin + firstTextLayout.getHeight();
        firstTextLayout.setTranslationY(dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(firstTextLayout, "translationY", 0);
        anim.setDuration(1000);
        setAllParentsClip(firstTextLayout, false);
        anim.start();

    }
}
