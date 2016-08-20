package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;

/**
 * all subtraction-specific operations are implemented here
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";
    protected String operation = "-";

    private int dotOffset;

    private boolean dotBagBorrowed = false;

    private int minuendIndex; // vertical column

    private int digitBorrowingIndex; // horizontal column
    private int digitBorrowingCol;
    private boolean isBorrowedValue = true; //To indicate if the current overhead value is borrowed value

    public CAsm_MechanicSubtract(CAsm_Component mComponent) {super.init(mComponent);}

    @Override
    public void nextDigit() {

        dotBagBorrowed = false;

        minuendIndex = calcMinuendIndex(firstBagIndex);

        super.nextDigit();

        int minuend = allAlleys.get(minuendIndex).getCurrentDigit();
        int subtrahend = allAlleys.get(secondBagIndex).getCurrentDigit();

        if (minuend - subtrahend < 0) {

            CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();

            // find first nonzero to borrow from - this should always break in the for loop!
            for (int i = mComponent.digitIndex - 2; i >= 0; i = i - 2) {

                if (firstBagLayout.getDigit(i) > 0) {
                    digitBorrowingIndex = i;
                    digitBorrowingCol = firstBagIndex;
                    break;
                }
            }

            makeTextBorrowable(digitBorrowingIndex, digitBorrowingCol);
        }
    }

    @Override
    public void preClickSetup() {

        // only show dotbags that are being operated on

        CAsm_DotBag currBag;

        for (int i = 0; i < allAlleys.size(); i++) {

            currBag = allAlleys.get(i).getDotBag();

            if (i != minuendIndex & i!= secondBagIndex) {
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
            createDownwardBagAnimator(minuendIndex).start();
        }
        else {
            subtrahendBag.wiggle(300, 1, 100, .05f);
        }

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


        int clickedDotCol = clickedDot.getCol();
        int minuend = allAlleys.get(minuendIndex).getCurrentDigit();
        if(!allAlleys.get(firstBagIndex).getTextLayout().getText(mComponent.digitIndex).getIsStruck())
            minuend = 10 + allAlleys.get(firstBagIndex).getCurrentDigit();
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

    private void dotbagBorrow() {

        // function when you borrow from your neighbor

        dotBagBorrowed = true;

        CAsm_DotBag borrowBag = allAlleys.get(digitBorrowingCol).getDotBag();
        CAsm_DotBag minuendBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();

        borrowBag.setDrawBorder(true);
        borrowBag.setRows(1);
        borrowBag.setCols(10);

        dotOffset = borrowBag.getCols() - subtrahendBag.getCols();
        subtrahendBag.setTranslationX(dotOffset*subtrahendBag.getSize());
        dotOffset = borrowBag.getCols() - minuendBag.getCols();
        minuendBag.setTranslationX(dotOffset*subtrahendBag.getSize());

        minuendIndex = digitBorrowingCol;
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
            dy += allAlleys.get(i).getHeight() + mComponent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);

        return anim;

    }

    private void makeTextBorrowable(int verticalIndex, int horizontalIndex) {

        isBorrowedValue = !isBorrowedValue;

        CAsm_TextLayout borrowingLayout = allAlleys.get(horizontalIndex).getTextLayout();
        borrowingLayout.getText(verticalIndex).setBorrowable(true);
        if(!borrowingLayout.getText(verticalIndex-1).getText().equals(""))
            borrowingLayout.getText(verticalIndex-1).setBorrowable(true);

        CAsm_TextLayout updatedLayout;
        if(isBorrowedValue) {
            mComponent.overheadVal = 10;
            updatedLayout = allAlleys.get(horizontalIndex).getTextLayout();
            mComponent.overheadText = updatedLayout.getText(verticalIndex + 2);
            mComponent.overheadTextSupplement = updatedLayout.getText(verticalIndex + 1);
        } else {
            mComponent.overheadVal = borrowingLayout.getDigit(verticalIndex) - 1;
            mComponent.overheadVal =  mComponent.overheadVal < 0 ? 9 :  mComponent.overheadVal;
            updatedLayout = allAlleys.get(horizontalIndex - 2).getTextLayout();
            mComponent.overheadText = updatedLayout.getText(verticalIndex);
            mComponent.overheadTextSupplement = updatedLayout.getText(verticalIndex - 1);
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
            if (!isBorrowedValue) {
                clickedText.setStruck(true);
                int clickedIndex = clickedTextLayout.indexOfChild(clickedText);
                if(clickedIndex % 2 == 0)
                    clickedTextLayout.getText(clickedIndex + 1).setStruck(true);
                else
                    clickedTextLayout.getText(clickedIndex - 1).setStruck(true);

                //Display +1 with underline above the clicked digit
                CAsm_TextLayout layout = allAlleys.get(digitBorrowingCol - 1).getTextLayout();
                layout.getText(digitBorrowingIndex - 1).setText("+");
                layout.getText(digitBorrowingIndex - 1).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                layout.getText(digitBorrowingIndex - 1).getPaint().setAntiAlias(true);

                layout.getText(digitBorrowingIndex).setText("1");
                layout.getText(digitBorrowingIndex).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                layout.getText(digitBorrowingIndex).getPaint().setAntiAlias(true);

                mComponent.overheadText.setResult();
            } else {
                clickedText.setStruck(true);
                clickedTextLayout.getText(clickedTextLayout.indexOfChild(clickedText) - 1).setStruck(true);
                mComponent.overheadText.setResult();
                mComponent.overheadTextSupplement.setResult();
            }
            return true;
        }

        return false;
    }

    @Override
    public void correctOverheadText() {

        super.correctOverheadText();

        if (digitBorrowingIndex + 2 == mComponent.digitIndex && isBorrowedValue) {
            // perform borrowing animation
            if (!dotBagBorrowed) {
                dotbagBorrow();
            }
        }

        else {
            if(!isBorrowedValue)
                digitBorrowingCol--;
            else
                digitBorrowingIndex += 2;

            farBorrowing();
            makeTextBorrowable(digitBorrowingIndex, digitBorrowingCol);
        }
    }

    private void farBorrowing() {

        //if they borrow a few digits away, e.g. 3003 - 1928
        if(!isBorrowedValue) {
            CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();
            CAsm_Text origText = firstBagLayout.getText(digitBorrowingIndex);
            origText.setStruck(true);
        }
    }

    private int calcMinuendIndex(int startIndex) {

        // keep going up till you find text that is not struck

        CAsm_Text curText = allAlleys.get(startIndex).getTextLayout().getText(mComponent.digitIndex);
        if ((curText.getIsStruck() || curText.getText().equals(""))) {
            return calcMinuendIndex(startIndex-1);
        } else {
            return startIndex;
        }
    }

    @Override
    public void highlightBorrowable() {
        CAsm_TextLayout borrowableTextLayout = allAlleys.get(digitBorrowingCol).getTextLayout();
        CAsm_Text borrowableText = borrowableTextLayout.getText(digitBorrowingIndex);
        if(!borrowableText.getIsStruck()) mComponent.highlightText(borrowableText);

        if(!isBorrowedValue && digitBorrowingCol != firstBagIndex) {
            borrowableText = borrowableTextLayout.getText(digitBorrowingIndex - 1);
            if(!borrowableText.getIsStruck()) mComponent.highlightText(borrowableText);
        }
    }

}
