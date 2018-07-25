package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;

import java.util.ArrayList;

import cmu.xprize.util.TCONST;

/**
 * all subtraction-specific operations are implemented here
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";

    private int dotOffset;

    private boolean dotBagBorrowed = false;
    private boolean hasBorrowed = false;

    private int minuendIndex; // vertical column

    private int digitBorrowingIndex = 0; // horizontal column
    private int digitBorrowingCol = 0;
    private boolean isBorrowedValue = true; //To indicate if the current overhead value is borrowed value

    private int borrowBlockStartIndex = -1;
    private int borrowBlockEndIndex = -1;

    public CAsm_MechanicSubtract(CAsm_Component mComponent) {
        super.init(mComponent);
    }


    /**
     * Called by C_Component.nextDigit()
     */
    @Override
    public void nextDigit() {

        if (digitBorrowingIndex <= mComponent.digitIndex && digitBorrowingIndex > 0) {
            if(mComponent.digitIndex < borrowBlockEndIndex)
                mComponent.curOverheadCol = firstBagIndex - 2 - (mComponent.digitIndex - borrowBlockStartIndex);
            else
                mComponent.curOverheadCol = firstBagIndex - 2 - (borrowBlockEndIndex - 1 - borrowBlockStartIndex) + 1;

            if (mComponent.digitIndex == digitBorrowingIndex &&
                    !allAlleys.get(mComponent.curOverheadCol).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getText().equals("") && // √√√
                    !(allAlleys.get(mComponent.curOverheadCol).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getCurrentTextColor() == Color.RED)) { // √√√
                ASM_CONST.logAnnoyingReference(mComponent.curOverheadCol, mComponent.digitIndex, 1, "nextDigit:: check not equal blank or red");
                dotbagBorrow();
            }
        } else if (mComponent.digitIndex < digitBorrowingIndex){
            hasBorrowed = false;

            allAlleys.get(digitBorrowingCol).getTextLayout().getTextLayout(digitBorrowingIndex).getText(1).setBorrowable(false); // √√√
            ASM_CONST.logAnnoyingReference(digitBorrowingCol, digitBorrowingIndex, 1, "setBorrowable(false)");

            if (allAlleys.get(digitBorrowingCol).getTextLayout().getTextLayout(digitBorrowingIndex).getText(0).getText().equals("1")) { // √√√
                allAlleys.get(digitBorrowingCol).getTextLayout().getTextLayout(digitBorrowingIndex).getText(0).setBorrowable(false); // √√√
                ASM_CONST.logAnnoyingReference(digitBorrowingCol, digitBorrowingIndex, 0, "if == 1, setBorrowable(false)");
                mComponent.curOverheadCol = digitBorrowingCol - 1;
            } else if (mComponent.curOverheadCol < overheadIndex)
                mComponent.curOverheadCol = digitBorrowingCol + 1;

        }

        if (mComponent.digitIndex == borrowBlockStartIndex - 1) {
            digitBorrowingCol = 0;
            digitBorrowingIndex = 0;
            borrowBlockStartIndex = -1;
            borrowBlockEndIndex = -1;
        }

        dotBagBorrowed = false;

        minuendIndex = calcMinuendIndex(firstBagIndex);

        super.nextDigit();

        int minuend = allAlleys.get(minuendIndex).getCurrentDigit();
        int subtrahend = allAlleys.get(secondBagIndex).getCurrentDigit(); // REVIEW... throws exception

        if (minuend - subtrahend < 0 && !hasBorrowed) {
            hasBorrowed = true;
            isBorrowedValue = true;

            CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout(); // √√√
            ASM_CONST.logAnnoyingReference(firstBagIndex, -1, -1, "something with borrowing");
            // find first nonzero to borrow from - this should always break in the for loop!
            for (int i = mComponent.digitIndex - 1; i >= 0; i--) {
                if (firstBagLayout.getDigit(i) > 0) {
                    digitBorrowingIndex = i;
                    digitBorrowingCol = firstBagIndex;
                    break;
                }
            }

            makeTextBorrowable(digitBorrowingIndex, digitBorrowingCol);
            borrowBlockStartIndex = digitBorrowingIndex;
            borrowBlockEndIndex = mComponent.digitIndex;
            mComponent.curOverheadCol = firstBagIndex - 2 - (borrowBlockEndIndex - 1 - borrowBlockStartIndex) + 1;
        }
    }

    @Override
    public void preClickSetup() {

        // only show dotbags that are being operated on

        CAsm_DotBag currBag;

        for (int i = 0; i < allAlleys.size(); i++) {

            currBag = allAlleys.get(i).getDotBag();

            if (i != minuendIndex && i != firstBagIndex && i != secondBagIndex) {
                currBag.setCols(0);
                currBag.setDrawBorder(false);
            }
            else{
                currBag.setDrawBorder(true);
            }
        }

        // right align
        CAsm_DotBag minuendBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();

        dotOffset = (minuendBag.getCols()-subtrahendBag.getCols());
        if (dotOffset < 0) {
            minuendBag.setTranslationX(-dotOffset * minuendBag.getSize());
        }
        else {
            subtrahendBag.setTranslationX(dotOffset * subtrahendBag.getSize());
        }


        // for case: x - 0
        if (subtrahendBag.getCols() == 0) {
//            createDownwardBagAnimator(minuendIndex);
        }
/*        else {
            subtrahendBag.wiggle(300, 1, 100, .05f);
        }*/

    }

    @Override
    public void handleClick() {

        super.handleClick();

        if (checkBorrowingText()) {return;} // found text and operated on it

        CAsm_Dot clickedDot = null;

        CAsm_DotBag minuendDotBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag correspondingBag = minuendDotBag;

        if (subtrahendBag.getIsClicked()) {
            clickedDot = subtrahendBag.findClickedDot();
        }
        // make sure dot was clicked
        if (clickedDot == null) {
            return;
        }

        mComponent.delAddFeature(TCONST.ASM_SUB_PROMPT, "");
        mComponent.delAddFeature("", TCONST.ASM_CLICK_ON_DOT);
        int clickedDotCol = clickedDot.getCol();
        int minuend = allAlleys.get(minuendIndex).getCurrentDigit();

        ASM_CONST.logAnnoyingReference(firstBagIndex, mComponent.digitIndex, 1, "getIsStruck()");
        if(minuendIndex != firstBagIndex &&
                !allAlleys.get(firstBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getIsStruck()) { // √√√
            minuend = 10 + allAlleys.get(firstBagIndex).getCurrentDigit();
        }
        int correspondingCol = minuend - subtrahendBag.getCols() + clickedDotCol;

        if (correspondingCol > minuendDotBag.getCols()-1) {
            correspondingBag = allAlleys.get(firstBagIndex).getDotBag();
            correspondingCol -= minuendDotBag.getCols();
        }

        if (correspondingCol < 0) {
            clickedDot.setIsClickable(true);
            return;
        }

        clickedDot.setHollow(true);
        subtrahendBag.setIsAudible(true);
        subtrahendBag.setHallowChime();
        mComponent.playChime();

        CAsm_Dot correspondingDot = correspondingBag.getDot(0, correspondingCol);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (minuendDotBag.getVisibleDots().size() == mComponent.corDigit &&
                subtrahendBag.getIsHollow()) {

            AnimatorSet shrink = createShrinkAnimator(minuendDotBag);
            shrink.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    createDownwardBagAnimator(minuendIndex);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            shrink.start();
        }

        if (allDotsDown()) {
            mComponent.postEvent(ASM_CONST.SCAFFOLD_RESULT_BEHAVIOR);
        }
    }

    public boolean allDotsDown() {
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();

        boolean allDotsDown = true;

        for (int i = 0; i < subtrahendBag.getRows(); i++)
            for (int j = 0; j < subtrahendBag.getCols(); j++)
                if (!subtrahendBag.getDot(i, j).getIsHollow())
                    allDotsDown = false;

        return allDotsDown;
    }

    private AnimatorSet createShrinkAnimator(final CAsm_DotBag changingBag) {

        final int numVisibleDots = changingBag.getVisibleDots().size();
        final int numInvisibleDots = changingBag.getCols() - numVisibleDots;

        int dotSize = changingBag.getSize();
        float currRight = changingBag.getBounds().right;
        float newRight = currRight - (dotSize * numInvisibleDots);

        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator anim = ObjectAnimator.ofFloat(changingBag, "right", currRight, newRight);
        anim.setDuration(1000);
        animSet.play(anim);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                changingBag.setCols(numVisibleDots);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animSet;

    }

    private void dotbagBorrow() {

        // function when you borrow from your neighbor

        dotBagBorrowed = true;

        ASM_CONST.logAnnoyingReference(mComponent.curOverheadCol, mComponent.digitIndex, 1, "check is blank or red");
        if (mComponent.digitIndex == digitBorrowingIndex &&
                !allAlleys.get(mComponent.curOverheadCol).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).equals("") && // √√√
                !(allAlleys.get(mComponent.curOverheadCol).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getCurrentTextColor() == Color.RED)) // √√√
            digitBorrowingCol = mComponent.curOverheadCol;

        CAsm_DotBag borrowBag = allAlleys.get(digitBorrowingCol).getDotBag();
        CAsm_DotBag minuendBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();

        borrowBag.setDrawBorder(true);
        borrowBag.setRows(1);
        if(mComponent.digitIndex < borrowBlockEndIndex) {
            borrowBag.setCols(allAlleys.get(mComponent.curOverheadCol).getTextLayout().getDigit(mComponent.digitIndex)); // √√√
            ASM_CONST.logAnnoyingReference(mComponent.curOverheadCol, mComponent.digitIndex, -1, "borrowing... borrowBag.setCols(this)");
            allAlleys.get(digitBorrowingCol + 1).getTextLayout().getTextLayout(mComponent.digitIndex).getText(0).setStruck(true); // √√√
            ASM_CONST.logAnnoyingReference(digitBorrowingCol + 1, mComponent.digitIndex, 0, "borrowing... setStruck(true)");
            allAlleys.get(digitBorrowingCol + 1).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).setStruck(true); // √√√
            ASM_CONST.logAnnoyingReference(digitBorrowingCol + 1, mComponent.digitIndex, 1, "borrowing... setStruck(true)");
            allAlleys.get(digitBorrowingCol + 1).getTextLayout().getTextLayout(mComponent.digitIndex).getBackground().setAlpha(100);; // √√√
            ASM_CONST.logAnnoyingReference(digitBorrowingCol + 1, mComponent.digitIndex, 1, "borrowing... setAlpha(100)");
            allAlleys.get(firstBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).setStruck(true); // √√√
            ASM_CONST.logAnnoyingReference(firstBagIndex, mComponent.digitIndex, 1, "borrowing... setStruck(true)");
        } else
            borrowBag.setCols(10);

        dotOffset = borrowBag.getCols() - subtrahendBag.getCols();
        subtrahendBag.setTranslationX(dotOffset * subtrahendBag.getSize());
        dotOffset = borrowBag.getCols() - minuendBag.getCols();
        minuendBag.setTranslationX(dotOffset * minuendBag.getSize());

        minuendIndex = digitBorrowingCol;
    }

    private void createDownwardBagAnimator(int startIndex) {

        CAsm_DotBag startDotBag = allAlleys.get(startIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        resultDotBag.setRows(startDotBag.getRows());
        resultDotBag.setCols(startDotBag.getCols());
        resultDotBag.setImage(startDotBag.getImageName());
        resultDotBag.setIsClickable(false);
        resultDotBag.setDrawBorder(true);

        setAllParentsClip(resultDotBag, false);
        startDotBag.setHollow(true);

        int dy = 0;
        for (int i = resultIndex; i > startIndex; i--) {
            dy += allAlleys.get(i).getHeight() + mComponent.alleyMargin;
        }

        AnimatorSet animSet = new AnimatorSet();
        ArrayList<Animator> animList = new ArrayList<Animator>();
        ObjectAnimator anim;

        int duraTime = 2000;
        for (int i = 0; i < resultDotBag.getRows(); i++) {
            for (int j = resultDotBag.getCols()-1; j >= 0; j--) {
                resultDotBag.getDot(i, j).setTranslationY(-dy);
                anim = ObjectAnimator.ofFloat( resultDotBag.getDot(i, j), "translationY", 0);
                anim.setDuration(duraTime);
                animList.add(anim);
                duraTime -= 200;
            }
        }
        animSet.playTogether(animList);
        animSet.start();

    }

    private void makeTextBorrowable(int verticalIndex, int horizontalIndex) {

        isBorrowedValue = !isBorrowedValue;

        CAsm_TextLayout borrowingLayout = allAlleys.get(horizontalIndex).getTextLayout(); // √√√
        borrowingLayout.getTextLayout(verticalIndex).getText(1).setBorrowable(true); // √√√
        ASM_CONST.logAnnoyingReference(horizontalIndex, verticalIndex, 1, "setBorrowable(true)");
        ASM_CONST.logAnnoyingReference(horizontalIndex, verticalIndex, 0, "check not blank");
        if(!borrowingLayout.getTextLayout(verticalIndex).getText(0).getText().equals("")) { // √√√
            borrowingLayout.getTextLayout(verticalIndex).getText(0).setBorrowable(true); // √√√
            ASM_CONST.logAnnoyingReference(horizontalIndex, verticalIndex, 0, "setBorrowable(true)");
        }

        CAsm_TextLayout updatedLayout;
        if(isBorrowedValue) {
            mComponent.overheadVal = 10;
            updatedLayout = allAlleys.get(horizontalIndex).getTextLayout(); // √√√
            mComponent.overheadText = updatedLayout.getTextLayout(verticalIndex + 1).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(horizontalIndex, verticalIndex + 1, 1, "overheadText");
            mComponent.overheadTextSupplement = updatedLayout.getTextLayout(verticalIndex + 1).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(horizontalIndex, verticalIndex + 1, 0, "overheadTextSupplement");
        } else {
            mComponent.overheadVal = borrowingLayout.getDigit(verticalIndex) - 1;
            mComponent.overheadVal =  mComponent.overheadVal < 0 ? 9 :  mComponent.overheadVal;
            updatedLayout = allAlleys.get(horizontalIndex - 2).getTextLayout(); // √√√
            mComponent.overheadText = updatedLayout.getTextLayout(verticalIndex).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(horizontalIndex - 2, verticalIndex, 1, "overheadText");
            mComponent.overheadTextSupplement = updatedLayout.getTextLayout(verticalIndex).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(horizontalIndex - 2, verticalIndex, 0, "overheadTextSupplement");
        }

    }

    private boolean checkBorrowingText() {

        // if they clicked a text they can borrow from, create a place for them to write the answer
        CAsm_TextLayout clickedTextLayout = findClickedTextLayout();

        if (clickedTextLayout == null) {
            return false;
        }

        CAsm_Text clickedText = clickedTextLayout.findClickedText();

        if (clickedText != null && clickedText.getIsBorrowable()) {
            clickedText.setStruck(true);

            if(clickedTextLayout.findClickedTextIndex() == 0) {
                clickedTextLayout.getTextLayout(clickedTextLayout.findClickedTextLayoutIndex()).getText(1).setStruck(true); // √√√
                ASM_CONST.logAnnoyingReference(-1, clickedTextLayout.findClickedTextLayoutIndex(), 1, "setStruck(true)");
            }
            else {
                clickedTextLayout.getTextLayout(clickedTextLayout.findClickedTextLayoutIndex()).getText(0).setStruck(true); // √√√
                ASM_CONST.logAnnoyingReference(-1, clickedTextLayout.findClickedTextLayoutIndex(), 0, "setStruck(true)");
            }

            if (!isBorrowedValue) {
                //Display +1 with underline above the clicked digit
                CAsm_TextLayout layout = allAlleys.get(digitBorrowingCol - 1).getTextLayout(); // √√√
                CAsm_TextLayout tempTextLayout = layout.getTextLayout(digitBorrowingIndex); // √√√
                tempTextLayout.getText(0).setText("+"); // √√√
                ASM_CONST.logAnnoyingReference(digitBorrowingCol - 1, digitBorrowingIndex, 0, "setText(+)");
                tempTextLayout.getText(1).setText("1"); // √√√
                ASM_CONST.logAnnoyingReference(digitBorrowingCol - 1, digitBorrowingIndex, 1, "setText(1)");
                tempTextLayout.setBackground(tempTextLayout.getResources().getDrawable(R.drawable.underline));
            } else {
                clickedTextLayout.getTextLayout(clickedTextLayout.findClickedTextLayoutIndex()).getBackground().setAlpha(100);
                mComponent.overheadTextSupplement.setResult();
            }

            mComponent.overheadText.setResult();
            return true;
        }

        return false;
    }

    @Override
    public void correctOverheadText() {

        super.correctOverheadText();

        if (mComponent.digitIndex < digitBorrowingIndex)
            return;
        else if ((digitBorrowingIndex + 1 == mComponent.digitIndex && isBorrowedValue && borrowBlockEndIndex == mComponent.digitIndex) ||
                (digitBorrowingIndex == mComponent.digitIndex && !isBorrowedValue)) {
            if (!dotBagBorrowed) {
                dotbagBorrow();
            }
        } else {
            if(!isBorrowedValue)
                digitBorrowingCol--;
            else
                digitBorrowingIndex++;

            farBorrowing();
            makeTextBorrowable(digitBorrowingIndex, digitBorrowingCol);
        }
    }

    private void farBorrowing() {

        //if they borrow a few digits away, e.g. 3003 - 1928
        if(!isBorrowedValue) {
            CAsm_Text origText = allAlleys.get(firstBagIndex).getTextLayout().getTextLayout(digitBorrowingIndex).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(firstBagIndex, digitBorrowingIndex, 1, "setStruck(true)");
            origText.setStruck(true);
        }
    }

    private int calcMinuendIndex(int startIndex) {

        // keep going up till you find text that is not struck

        CAsm_Text curText = allAlleys.get(startIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(startIndex, mComponent.digitIndex, 1, "check is struck or blank");
        if ((curText.getIsStruck() || curText.getText().equals(""))) {
            return calcMinuendIndex(startIndex-1);
        } else {
            return startIndex;
        }
    }

    @Override
    public void highlightOverhead() {
        super.highlightOverhead();

        if (digitBorrowingCol == 0 && digitBorrowingIndex == 0 || mComponent.digitIndex < digitBorrowingIndex)
            return;

        CAsm_TextLayout borrowableTextLayout = allAlleys.get(digitBorrowingCol).getTextLayout(); // √√√
        CAsm_Text borrowableText = borrowableTextLayout.getTextLayout(digitBorrowingIndex).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(digitBorrowingCol, digitBorrowingIndex, 1, "highlightText if not struck");

        if (!borrowableText.getIsStruck()) mComponent.highlightText(borrowableText);

        if (!isBorrowedValue && digitBorrowingCol != firstBagIndex) {
            borrowableText = borrowableTextLayout.getTextLayout(digitBorrowingIndex).getText(0); // √√√
            ASM_CONST.logAnnoyingReference(digitBorrowingCol, digitBorrowingIndex, 1, "highlightText if not struck");
            if (!borrowableText.getIsStruck() && !borrowableText.getText().equals(""))
                mComponent.highlightText(borrowableText);
        }
    }

}
