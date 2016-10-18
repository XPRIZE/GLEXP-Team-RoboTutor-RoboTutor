package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.TableRow;

import java.util.ArrayList;

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
        for(int i = 1; i < resultTextLayout.getChildCount()-1; i++) {
            resultTextLayout.getTextLayout(i).getText(0).setVisibility(View.INVISIBLE);
            resultTextLayout.getTextLayout(i).getText(1).setResult();
        }
    }

    protected String operation = "x";
    private int allIndexInAddition[], allValueInAddition[];
    private int curIndexInArray = -1;
    private int curRowIndexInAddition;
    private int multiplicand, multiplier;

    public void next() {
        super.next();

        multiplicand = mComponent.numbers[0];
        multiplier = mComponent.numbers[1];

        if (multiplicand > 0) {
            initArrayInAddition();
            updateOverhead();
        }

        for(int i = resultOrAddInMultiPart1 + 1; i < allAlleys.size(); i++)
            allAlleys.get(i).getTextLayout().resetAllValues();

        allAlleys.get(ASM_CONST.OPERATION_MULTI - 1).getTextLayout().getTextLayout(mComponent.numSlots - 1).setBackground(null);
    }

    private void initArrayInAddition() {
        int length = multiplicand * 2 - 1;
        allIndexInAddition = new int[length];
        allValueInAddition = new int[length];

        allIndexInAddition[0] = 0;
        for (int i = 1; i < length; i++) {
            int lastIndex = allIndexInAddition[i-1];

            if (lastIndex == multiplicand * 2 - 3)
                allIndexInAddition[i] = 2;
            else
                allIndexInAddition[i] = lastIndex + (lastIndex == 0 ? 1 : 2);
        }

        for (int i = 0; i < length; i++) {
            if (i < multiplicand)
                allValueInAddition[i] = multiplier;
            else
                allValueInAddition[i] = multiplier * (i - multiplicand + 2);
        }
    }

    private void updateOverhead() {
        if (mComponent.overheadText != null && mComponent.overheadText.getVisibility() == View.VISIBLE) mComponent.overheadText.cancelResult();
        if (mComponent.overheadTextSupplement != null && mComponent.overheadTextSupplement.getVisibility() == View.VISIBLE) mComponent.overheadTextSupplement.cancelResult();

        curIndexInArray++;
        curRowIndexInAddition = allIndexInAddition[curIndexInArray];
        mComponent.overheadVal = allValueInAddition[curIndexInArray];

        CAsm_TextLayout curColInAddition = allAlleys.get(curRowIndexInAddition).getTextLayout().getTextLayout(mComponent.numSlots-1);
        mComponent.overheadText = curColInAddition.getText(1);
        mComponent.overheadText.setTypeface(null, Typeface.BOLD);
        mComponent.overheadText.setResult();
        mComponent.overheadText.setVisibility(View.VISIBLE);

        mComponent.overheadTextSupplement = curColInAddition.getText(0);
        mComponent.overheadTextSupplement.setTypeface(null, Typeface.BOLD);
        if (mComponent.overheadVal > 9) {
            mComponent.overheadTextSupplement.setResult();
            mComponent.overheadTextSupplement.setVisibility(View.VISIBLE);
        }

        if (mComponent.overheadVal > multiplier) {
            CAsm_TextLayout secondInAddition = allAlleys.get(curRowIndexInAddition-1).getTextLayout().getTextLayout(mComponent.numSlots-1);
            secondInAddition.getText(0).setText("+");
            secondInAddition.setBackground(secondInAddition.getResources().getDrawable(R.drawable.underline_add_in_mul));

            updateDotbagsInAddition();
        }
    }

    private void updateDotbagsInAddition() {
        CAsm_DotBag dotbagOfAddend1 = allAlleys.get(curRowIndexInAddition - 2).getDotBag();
        CAsm_DotBag dotbagOfAddend2 = allAlleys.get(curRowIndexInAddition - 1).getDotBag();
        CAsm_DotBag dotbagOfResult = allAlleys.get(curRowIndexInAddition).getDotBag();

        //Update the dotbag of addend1 in next addition
        if (curRowIndexInAddition - 2 != 0 && dotbagOfAddend1.getVisibility() == View.VISIBLE)
            fillDotbagAutomatically(curRowIndexInAddition - 2);
        else
            updateFirstBagInAdd(curRowIndexInAddition - 2);

        //Update the dotbag of addend2 in next addition
        dotbagOfAddend2.setDrawBorder(true);
        dotbagOfAddend2.setRows(1);
        dotbagOfAddend2.setCols(multiplier);

        //Update the dotbag of result in next addition
        dotbagOfResult.setDrawBorder(true);

        dotbagOfAddend1.setIsClickable(true);
        dotbagOfAddend2.setIsClickable(true);
        dotbagOfAddend2.setVisibility(View.INVISIBLE);
        dotbagOfResult.setVisibility(View.INVISIBLE);

        //reset timer, show dotbags when user hesitates
        mComponent.hasShown = false;
        mComponent.startTime = System.currentTimeMillis();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mComponent.setDotBagsVisible(true, mComponent.digitIndex, curRowIndexInAddition-2);
            }
        }, 3000);
    }

    private void fillDotbagAutomatically(int indexOfLastResult) {
        CAsm_DotBag dotbagOfAddend1 = allAlleys.get(indexOfLastResult-2).getDotBag();
        CAsm_DotBag dotbagOfAddend2 = allAlleys.get(indexOfLastResult-1).getDotBag();
        final CAsm_DotBag dotbagOfResult = allAlleys.get(indexOfLastResult).getDotBag();
        CAsm_Dot curDot;

        AnimatorSet animSet = new AnimatorSet();
        ArrayList<Animator> animList = new ArrayList<Animator>();

        int durTime = 100;
        for (int i = 0; i < dotbagOfAddend1.getRows(); i++) {
            for (int j = 0; j < dotbagOfAddend1.getCols(); j++) {
                curDot = dotbagOfAddend1.getDot(i, j);
                if (!curDot.getIsHollow()) {
                    animList.add(animateAdd(curDot, indexOfLastResult-2, indexOfLastResult, durTime));
                    durTime += 100;
                }
            }
        }

        for (int i = 0; i < dotbagOfAddend2.getRows(); i++) {
            for (int j = 0; j < dotbagOfAddend2.getCols(); j++) {
                curDot = dotbagOfAddend2.getDot(i, j);
                if (!curDot.getIsHollow()) {
                    animList.add(animateAdd(curDot, indexOfLastResult-1, indexOfLastResult, durTime));
                    durTime += 100;
                }
            }
        }

        animSet.playTogether(animList);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mComponent.overheadVal - multiplier < mComponent.corValue)
                    dotbagOfResult.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mComponent.overheadVal - multiplier < mComponent.corValue)
                    dotbagOfResult.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animSet.start();
    }

    private void updateFirstBagInAdd(int indexOfLastResult) {
        CAsm_DotBag bagToChange = allAlleys.get(indexOfLastResult).getDotBag();
        int val = mComponent.overheadVal - multiplier;

        mComponent.hasShown = true;
        bagToChange.setDrawBorder(true);
        bagToChange.setVisibility(View.INVISIBLE);
        bagToChange.setZero();

        int row = val / 10 + 1;
        int col = row > 1 ? 10 : val % 10;
        int count = 0;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                bagToChange.addDot(i, j);
                count++;
                if (count == val) return;
            }
        }
    }

    @Override
    public void nextDigit() {

        if (mComponent.digitIndex == mComponent.numSlots-2) {
            super.nextDigit();
            mComponent.hasShown = true;
            mComponent.overheadText.setResult();
            mComponent.overheadText.setVisibility(View.VISIBLE);
        } else {
//            CAsm_DotBag firstBag, secondBag, resultBag;
            allAlleys.get(resultOrAddInMultiPart1).getTextLayout().performNextDigit();

/*            firstBag = allAlleys.get(firstBagIndexForMulti).getDotBag();
            secondBag = allAlleys.get(secondBagIndexForMulti).getDotBag();
            resultBag = allAlleys.get(resultOrAddInMultiPart1).getDotBag();

            resultBag.setRows(firstBag.getCols());
            resultBag.setCols(secondBag.getCols());*/

            //mComponent.setDotBagsVisible(true, mComponent.digitIndex);
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
        firstBag.wiggle(300, 1, 100, .05f);*/
        /*


        allAlleys.get(secondBagIndexForMulti).getDotBag().setIsClickable(false);
*/

    }

    @Override
    public void handleClick() {

        super.handleClick();

        if (mComponent.overheadVal > multiplier) {
            CAsm_DotBag firstBagInAdd = allAlleys.get(curRowIndexInAddition - 2).getDotBag();
            CAsm_DotBag secondBagInAdd = allAlleys.get(curRowIndexInAddition - 1).getDotBag();
            CAsm_Dot clickedDot;

            if (firstBagInAdd.getIsClicked()) {
                clickedDot = firstBagInAdd.findClickedDot();
                if (clickedDot != null)
                    animateAdd(clickedDot, curRowIndexInAddition - 2, curRowIndexInAddition, 1000).start();
            } else if (secondBagInAdd.getIsClicked()) {
                clickedDot = secondBagInAdd.findClickedDot();
                if (clickedDot != null)
                    animateAdd(clickedDot, curRowIndexInAddition - 1, curRowIndexInAddition, 1000).start();
            }
        }
    }

/*
    private void handleClickedMultiDotbag(CAsm_DotBag firstBag) {

        CAsm_Text secondText = allAlleys.get(secondBagIndexForMulti).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);

        final CAsm_Dot clickedDot = firstBag.findClickedDot();
        if (clickedDot == null) {return;}

        if(!mComponent.downwardResult) {
            //If it is the first time to click dot in dotbag of multiplier, downward the multiplier to addend1;
            dowanwardResult();
            downwardAddend1(secondText);
            clickedDot.setHollow(true);
            copyDotbag(secondBagIndexForMulti, resultOrAddInMultiPart1);
        } else {
            //If it is not the first time to click dot in dotbag of multiplier, downward the multiplier to addend2;
            CAsm_Text text = allAlleys.get(addInMultiPart2).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);
            if(!text.getText().equals("")) {
                clickedDot.setIsClickable(true);
                return;
            }

            //Downward the multiplier to addend2, and copy corresponding dotbag.
            downwardAddend2(secondText, clickedDot);
            copyDotbag(secondBagIndexForMulti, addInMultiPart2);

            //reset timer, show dotbags when user hesitates
            mComponent.hasShown = false;
            mComponent.startTime = System.currentTimeMillis();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mComponent.setDotBagsVisible(true, mComponent.digitIndex);
                }
            }, 3000);

            allAlleys.get(addInMultiPart3).getDotBag().setDrawBorder(true);
        }

    }

    private void downwardAddend1(CAsm_Text oriText) {
        int dy;
        CAsm_Text text = allAlleys.get(resultOrAddInMultiPart1).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1);

        text.setText(oriText.getText());
        text.setTypeface(null, Typeface.BOLD);
        allAlleys.get(resultOrAddInMultiPart1).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(0).setTypeface(null, Typeface.BOLD);
        dy = oriText.getHeight() + mComponent.alleyMargin;
        dowanwardTextAnimation(text, dy);

        mComponent.overheadVal = oriText.getDigit();
    }

    private void downwardAddend2(CAsm_Text oriText, CAsm_Dot clickedDot) {
        int dy;
        CAsm_TextLayout secondInAddition = allAlleys.get(addInMultiPart2).getTextLayout().getTextLayout(mComponent.numSlots-1);
        CAsm_Text text = secondInAddition.getText(1);

        secondInAddition.setBackground(secondInAddition.getResources().getDrawable(R.drawable.underline));
        secondInAddition.getText(0).setText("+");
        secondInAddition.getText(0).setTypeface(null, Typeface.BOLD);
        text.setText(oriText.getText());
        text.setTypeface(null, Typeface.BOLD);

        dy = oriText.getHeight() + mComponent.alleyMargin + text.getHeight();
        dowanwardTextAnimation(text, dy);
        clickedDot.setHollow(true);

        mComponent.overheadVal += oriText.getDigit();
        if(mComponent.overheadVal > 9)
            mComponent.overheadTextSupplement.setResult();
        mComponent.overheadText.setResult();

        if (mComponent.overheadVal == mComponent.corValue) {
            allAlleys.get(addInMultiPart3).getTextLayout().setBackground(null);
            allAlleys.get(resultIndexForMultiBackup).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1).cancelResult();
            allAlleys.get(resultIndexForMultiBackup).getTextLayout().getTextLayout(mComponent.numSlots-1).getText(1).setText("");
            allAlleys.get(resultIndexForMultiBackup).getTextLayout().getTextLayout(mComponent.numSlots-2).getText(1).cancelResult();
            allAlleys.get(resultIndexForMultiBackup).getTextLayout().getTextLayout(mComponent.numSlots-2).getText(1).setText("");
        }
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
            oriText.setText("");
        }
        newSecond.setBackground(newSecond.getResources().getDrawable(R.drawable.underline));
        mComponent.downwardResult = true;
    }

    private void copyDotbag(int oriIndex, int targetIndex) {
        CAsm_DotBag oriDotbag = allAlleys.get(oriIndex).getDotBag();
        CAsm_DotBag targetDotbag = allAlleys.get(targetIndex).getDotBag();

        targetDotbag.setRows(oriDotbag.getRows());
        targetDotbag.setCols(oriDotbag.getCols());
        targetDotbag.setImage(oriDotbag.getImageName());
        targetDotbag.setDrawBorder(true);

        targetDotbag.setVisibility(View.INVISIBLE);
    }
*/

    public AnimatorSet animateAdd(CAsm_Dot clickedDot, int startIndex, int resultIndex, int durTime) {

        // create new dot in result bag, but animate it from position of the clicked dot

        final CAsm_DotBag resultBag = allAlleys.get(resultIndex).getDotBag();

        int dx = determineColumnDX(clickedDot, resultBag.getCols()<10 ? resultBag.getCols() : resultBag.getOverflowNum());
        int dy = determineAlleyDY(startIndex, resultIndex);

        final CAsm_Dot oldDot = clickedDot;
        final CAsm_Dot newDot;
        if(resultBag.getCols() < 10)
            newDot = resultBag.addDot(0, resultBag.getCols());
        else {
            newDot = resultBag.addDot(1, resultBag.getOverflowNum());
            resultBag.addOverflowNum();
        }
        newDot.setTranslationX(-dx);
        newDot.setTranslationY(-dy);
        newDot.setIsClickable(false);

        ObjectAnimator animX = ObjectAnimator.ofFloat(newDot, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newDot, "translationY", 0);

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(durTime);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                oldDot.setHollow(true);
                setAllParentsClip(newDot, false);
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

        return animSetXY;
    }

    private int determineColumnDX(CAsm_Dot startDot, int targetCol) {

        int dotSize = startDot.getWidth();
        return dotSize * (targetCol - startDot.getCol());
    }

    private int determineAlleyDY(int startAlley, int endAlley) {

        int dy = 0;
        for (int i = startAlley + 1; i <= endAlley; i++) {
            dy += mComponent.alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }

    public void correctOverheadText() {
        // whenever they put in the right overhead text
/*        upwardAdditionResult(mComponent.overheadVal == mComponent.corValue);

        //if dotbags are showing, play animation to upward dotbag
        if (mComponent.hasShown)
            upwardAdditionResultDotbag();
        else
            updateFirstBagInAdd();*/

        if (mComponent.overheadVal < mComponent.corValue)
            updateOverhead();
        else {
            mComponent.overheadText.cancelResult();
            mComponent.overheadTextSupplement.cancelResult();
            if (mComponent.hasShown) {
                mComponent.overheadVal += multiplier;
                fillDotbagAutomatically(curRowIndexInAddition);
            }
        }
    }

    public int getCurRow() {
        return curRowIndexInAddition;
    }

/*
    private void upwardAdditionResult(final boolean isFinalResult) {

        mComponent.overheadText.cancelResult();
        mComponent.overheadTextSupplement.cancelResult();

        if(isFinalResult) {
            mComponent.downwardResult = false;
            mComponent.overheadText.setBackground(null);
            mComponent.overheadTextSupplement.setBackground(null);
        }

        CAsm_TextLayout firstTextLayout = allAlleys.get(resultOrAddInMultiPart1).getTextLayout();
        CAsm_Text firstText1 = isFinalResult? firstTextLayout.getTextLayout(mComponent.numSlots-2).getText(1) : firstTextLayout.getTextLayout(mComponent.numSlots-1).getText(0);
        CAsm_Text firstText2 = firstTextLayout.getTextLayout(mComponent.numSlots-1).getText(1);
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

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isFinalResult)
                    mComponent.onEvent(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();

    }

    private void upwardAdditionResultDotbag() {

        CAsm_DotBag firstBagInAdd = allAlleys.get(resultOrAddInMultiPart1).getDotBag();
        CAsm_DotBag secondBagInAdd = allAlleys.get(addInMultiPart2).getDotBag();
        CAsm_Dot curDot;

        AnimatorSet animSet = new AnimatorSet();
        ArrayList<Animator> animList = new ArrayList<Animator>();

        int durTime = 100;
        for (int i = 0; i < firstBagInAdd.getRows(); i++) {
            for (int j = 0; j < firstBagInAdd.getCols(); j++) {
                curDot = firstBagInAdd.getDot(i, j);
                if (!curDot.getIsHollow()) {
                    animList.add(animateAdd(curDot, resultOrAddInMultiPart1, addInMultiPart3, durTime));
                    durTime += 100;
                }
            }
        }

        for (int i = 0; i < secondBagInAdd.getRows(); i++) {
            for (int j = 0; j < secondBagInAdd.getCols(); j++) {
                curDot = secondBagInAdd.getDot(i, j);
                if (!curDot.getIsHollow()) {
                    animList.add(animateAdd(curDot, addInMultiPart2, addInMultiPart3, durTime));
                    durTime += 100;
                }
            }
        }

        animSet.playTogether(animList);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                _upwardAdditionResultDotbag();
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

    private void _upwardAdditionResultDotbag() {

        CAsm_DotBag firstBagInAdd = allAlleys.get(resultOrAddInMultiPart1).getDotBag();
        CAsm_DotBag secondBagInAdd = allAlleys.get(addInMultiPart2).getDotBag();
        CAsm_DotBag resultBagInAdd = allAlleys.get(addInMultiPart3).getDotBag();

        firstBagInAdd.setCols(0);
        firstBagInAdd.copyFrom(resultBagInAdd);
        firstBagInAdd.setClickable(false);

        int dy = allAlleys.get(resultOrAddInMultiPart1).getHeight() * 2 + mComponent.alleyMargin;;

        firstBagInAdd.setTranslationY(dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(firstBagInAdd, "translationY", 0);
        anim.setDuration(1000);
        setAllParentsClip(firstBagInAdd, false);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mComponent.overheadVal == mComponent.corValue)
                    mComponent.onEvent(null);
                else
                    allAlleys.get(resultOrAddInMultiPart1).getDotBag().setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        anim.start();

        secondBagInAdd.setZero();
        secondBagInAdd.setVisibility(View.INVISIBLE);
        resultBagInAdd.setZero();
        resultBagInAdd.setVisibility(View.INVISIBLE);

    }

    private void downwardAdditionResultDotbag() {

        CAsm_DotBag resultBagInAdd = allAlleys.get(addInMultiPart3).getDotBag();
        CAsm_DotBag resultBagInMulti = allAlleys.get(resultIndexForMultiBackup).getDotBag();

        resultBagInMulti.copyFrom(resultBagInAdd);
        //resultBagInMulti.setDrawBorder(true);
        //resultBagInMulti.setRows(resultBagInAdd.getRows());
        //resultBagInMulti.setCols(resultBagInAdd.getCols());

        int dy = mComponent.alleyMargin;;

        resultBagInMulti.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultBagInMulti, "translationY", 0);
        anim.setDuration(1000);
        setAllParentsClip(resultBagInMulti, false);
        anim.start();

        resultBagInAdd.setZero();
        resultBagInAdd.setVisibility(View.INVISIBLE);

    }
*/

}
