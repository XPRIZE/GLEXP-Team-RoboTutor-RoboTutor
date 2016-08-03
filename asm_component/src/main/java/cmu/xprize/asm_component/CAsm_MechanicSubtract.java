package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * all subtraction-specific operations are implemented here
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";
    protected String operation = "-";

    private int dotOffset;

    private boolean dotBagBorrowed = false;
    private boolean previouslyBorrowed = false;
    private boolean willBorrow = false;

    private int minuendIndex; // vertical column

    private int digitBorrowingIndex; // horizontal column


    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}

    @Override
    public void nextDigit() {

        previouslyBorrowed = willBorrow;
        willBorrow = false;
        dotBagBorrowed = false;

        minuendIndex = (previouslyBorrowed)?overheadIndex:firstBagIndex;

        if (previouslyBorrowed) {
            CAsm_Text borrowedText = allAlleys.get(minuendIndex+1).getTextLayout().getText(parent.digitIndex);
            borrowedText.setStruck(true);
        }

        super.nextDigit();

        CAsm_DotBag currBag;

        for (int i = 0; i < minuendIndex; i++) {
            currBag = allAlleys.get(i).getDotBag();
            currBag.setDrawBorder(false);
        }

        Integer minuend, subtrahend;

        minuend = allAlleys.get(minuendIndex).getCurrentDigit();
        subtrahend = allAlleys.get(secondBagIndex).getCurrentDigit();

        if (minuend - subtrahend < 0) {

            willBorrow = true;

            CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();

            // find first nonzero to borrow from
            for (int i = parent.digitIndex-1; i >= 0; i--) {

                if (firstBagLayout.getDigit(i) > 0) {
                    digitBorrowingIndex = i;
                    break;
                }
            }

            makeTextBorrowable(digitBorrowingIndex, firstBagIndex);

        }
    }

    @Override
    public void preClickSetup() {

        int extraIndex;

        if (previouslyBorrowed) {
            minuendIndex = overheadIndex;
            extraIndex = firstBagIndex;
        } else {
            minuendIndex = firstBagIndex;
            extraIndex = overheadIndex;
        }

        CAsm_DotBag minuendBag = allAlleys.get(minuendIndex).getDotBag();
        minuendBag.setDrawBorder(true);

        CAsm_DotBag extraBag = allAlleys.get(extraIndex).getDotBag();
        extraBag.setCols(0);
        extraBag.setDrawBorder(false);

        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();
        resultDotBag.setDrawBorder(false);

        // right align
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();

        dotOffset = (minuendBag.getCols()-secondDotBag.getCols());
        if (dotOffset < 0) {
            minuendBag.setTranslationX(-dotOffset * minuendBag.getSize() + translationX);
        }
        else {
            secondDotBag.setTranslationX(dotOffset * secondDotBag.getSize() + translationX);
        }

    }

    @Override
    public void handleClick() {


        super.handleClick();

        checkBorrowingText();

        int correspondingCol;
        CAsm_Dot clickedDot = null;

        CAsm_DotBag minuendDotBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag clickedBag = allAlleys.get(secondBagIndex).getDotBag(); // only one possible dotbag to look at
        CAsm_DotBag correspondingBag = minuendDotBag;

        if (clickedBag.getIsClicked()) {
            clickedDot = clickedBag.findClickedDot();
        }

        // make sure dot was clicked
        if (clickedDot == null) {
            return;
        }


        int clickedDotCol = clickedDot.getCol();

        if (dotBagBorrowed) {

            CAsm_DotBag minuendBag = allAlleys.get(minuendIndex).getDotBag();
            CAsm_DotBag secondMinuendBag = allAlleys.get(minuendIndex+1).getDotBag();

            if (clickedBag.getCols() - clickedDotCol <= secondMinuendBag.getCols()) {
                correspondingBag = secondMinuendBag;
                correspondingCol = clickedDotCol - (clickedBag.getCols() - secondMinuendBag.getCols());
            }

            else {
                int minuendDots = minuendBag.getCols() + secondMinuendBag.getCols();
                correspondingCol = minuendDots + clickedDotCol - clickedBag.getCols();
            }
        }

        else {
            correspondingCol = clickedDotCol + dotOffset;
        }

        if (correspondingCol < 0) {
            clickedDot.setIsClickable(true);
            return;
        }

        clickedDot.setHollow(true);

        CAsm_Dot correspondingDot = correspondingBag.getDot(0, correspondingCol);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (minuendDotBag.getVisibleDots().size() == parent.corDigit &&
                clickedBag.getIsHollow()) {
            AnimatorSet shrink = createShrinkAnimator(minuendDotBag);
            shrink.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    createDownwardBagAnimator(minuendIndex).start();
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

    private void borrow() {

        dotBagBorrowed = true;
        int borrowIndex = minuendIndex-1;

        CAsm_DotBag borrowBag = allAlleys.get(borrowIndex).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        // update texts

        CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();
        CAsm_Text origSourceText = firstBagLayout.getText(parent.digitIndex-1);
        origSourceText.setStruck(true);

        CAsm_TextLayout minuendLayout = allAlleys.get(minuendIndex).getTextLayout();
        CAsm_Text origDestText = minuendLayout.getText(parent.digitIndex);
        Integer origDestDigit = minuendLayout.getDigit(parent.digitIndex);
        origDestText.reset();
        origDestText.setStruck(true);

        CAsm_TextLayout updatedLayout = allAlleys.get(borrowIndex).getTextLayout();
        CAsm_Text updatedDestText = updatedLayout.getText(parent.digitIndex);
        updatedDestText.setText(String.valueOf(10 + origDestDigit));

        borrowBag.setDrawBorder(true);
        borrowBag.setRows(1);
        borrowBag.setCols(10);
        borrowBag.setTranslationX(0);

        resultDotBag.setCols(0);
        resultDotBag.setTranslationX(0);

        dotOffset = borrowBag.getCols() - secondDotBag.getCols();
        secondDotBag.setTranslationX(dotOffset*secondDotBag.getSize());

        minuendIndex = borrowIndex;


    }

    private ObjectAnimator createDownwardBagAnimator(int startIndex) {

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
            dy += allAlleys.get(i).getHeight() + parent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);

        return anim;


    }

    private void makeTextBorrowable(int verticalIndex, int horizontalIndex) {


        CAsm_TextLayout borrowingLayout = allAlleys.get(horizontalIndex).getTextLayout();
        borrowingLayout.getText(verticalIndex).setBorrowable(true);

        parent.overheadVal = borrowingLayout.getDigit(verticalIndex)-1;
        parent.overheadVal = (parent.overheadVal < 0)?10:parent.overheadVal;

        CAsm_TextLayout toBorrowLayout = allAlleys.get(horizontalIndex-1).getTextLayout();
        parent.overheadText = toBorrowLayout.getText(verticalIndex);


    }

    private void checkBorrowingText() {

        // if they clicked a text they can borrow from, create a place for them to write the answer
        CAsm_TextLayout clickedTextLayout = findClickedTextLayout();

        if (clickedTextLayout == null) {
            return;
        }

        CAsm_Text clickedText = clickedTextLayout.findClickedText();

        if (clickedText != null && clickedText.getIsBorrowable()) {
            clickedText.reset();
            parent.overheadText.setResult();
        }

    }

    @Override
    public void correctOverheadText() {

        if (digitBorrowingIndex == parent.digitIndex-1) {
            // perform borrowing animation
            if (!dotBagBorrowed) {
                borrow();
            }
        }

        else {
            digitBorrowingIndex +=1;
            makeTextBorrowable(digitBorrowingIndex, overheadIndex);
        }

    }

}
