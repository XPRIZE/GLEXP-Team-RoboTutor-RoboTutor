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

    private int minuendIndex; // vertical column

    private int digitBorrowingIndex; // horizontal column


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
            for (int i = mComponent.digitIndex-1; i >= 0; i--) {

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
        int correspondingCol = minuend - subtrahendBag.getCols() + clickedDotCol;

        if (correspondingCol > minuendDotBag.getCols()-1) {
            correspondingBag = allAlleys.get(minuendIndex+1).getDotBag();
            correspondingCol -= minuendDotBag.getCols();
        }

        if (correspondingCol < 0) {
            clickedDot.setIsClickable(true);
            return;
        }

        clickedDot.setHollow(true);

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
        int borrowIndex = minuendIndex-1;

        CAsm_DotBag borrowBag = allAlleys.get(borrowIndex).getDotBag();
        CAsm_DotBag subtrahendBag = allAlleys.get(secondBagIndex).getDotBag();

        // update texts

        CAsm_TextLayout minuendLayout = allAlleys.get(minuendIndex).getTextLayout();
        CAsm_Text origText = minuendLayout.getText(mComponent.digitIndex);
        Integer origDigit = minuendLayout.getDigit(mComponent.digitIndex);
        origText.reset();
        origText.setStruck(true);

        CAsm_TextLayout updatedLayout = allAlleys.get(borrowIndex).getTextLayout();
        CAsm_Text updatedDestText = updatedLayout.getText(mComponent.digitIndex);
        updatedDestText.setText(String.valueOf(10 + origDigit));
        updatedDestText.setTextSize(updatedDestText.getTextSize()/2);

        borrowBag.setDrawBorder(true);
        borrowBag.setRows(1);
        borrowBag.setCols(10);

        dotOffset = borrowBag.getCols() - subtrahendBag.getCols();
        subtrahendBag.setTranslationX(dotOffset*subtrahendBag.getSize());

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
            dy += allAlleys.get(i).getHeight() + mComponent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);

        return anim;

    }

    private void makeTextBorrowable(int verticalIndex, int horizontalIndex) {

        CAsm_TextLayout borrowingLayout = allAlleys.get(horizontalIndex).getTextLayout();
        borrowingLayout.getText(verticalIndex).setBorrowable(true);

        mComponent.overheadVal = borrowingLayout.getDigit(verticalIndex)-1;
        mComponent.overheadVal = (mComponent.overheadVal < 0)?10:mComponent.overheadVal;

        CAsm_TextLayout updatedLayout = allAlleys.get(horizontalIndex-1).getTextLayout();
        mComponent.overheadText = updatedLayout.getText(verticalIndex);


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
            mComponent.overheadText.setResult();
            return true;
        }

        return false;
    }

    @Override
    public void correctOverheadText() {

        super.correctOverheadText();

        if (digitBorrowingIndex == mComponent.digitIndex-1) {
            // perform borrowing animation
            if (!dotBagBorrowed) {
                dotbagBorrow();
            }
        }

        else {
            digitBorrowingIndex +=1;
            farBorrowing();
            makeTextBorrowable(digitBorrowingIndex, overheadIndex);
        }
    }

    private void farBorrowing() {

        //if they borrow a few digits away, e.g. 3003 - 1928

        CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();
        CAsm_Text origText = firstBagLayout.getText(digitBorrowingIndex);
        origText.setStruck(true);

        CAsm_TextLayout updatedLayout = allAlleys.get(firstBagIndex-1).getTextLayout();
        CAsm_Text updatedText = updatedLayout.getText(digitBorrowingIndex);
        updatedText.setText(String.valueOf(10 + origText.getDigit()));
        updatedText.setTextSize(updatedText.getTextSize()/2);

    }

    private int calcMinuendIndex(int startIndex) {

        // keep going up till you find text that is not struck

        if (allAlleys.get(startIndex).getTextLayout().getText(mComponent.digitIndex).getIsStruck()) {
            return calcMinuendIndex(startIndex-1);
        }
        else {
            return startIndex;
        }

    }

    /**
     *     If user has inputted bottom digit correctly without filling the overhead text correctly,
     *     the tutor will fill the overhead text automatically.
     */
    @Override
    public void fillOverheadAutomatically() {
        CAsm_Text text = allAlleys.get(overheadIndex).getTextLayout().getText(digitBorrowingIndex);

        if(text.getText().equals("") || text == null) {
            allAlleys.get(firstBagIndex).getTextLayout().getText(digitBorrowingIndex).setStruck(true);
            allAlleys.get(overheadIndex).getTextLayout().getText(digitBorrowingIndex).setResult();
            allAlleys.get(overheadIndex).getTextLayout().getText(digitBorrowingIndex).setText(mComponent.overheadVal.toString());
        }else {
            allAlleys.get(overheadIndex).getTextLayout().getText(digitBorrowingIndex).setStruck(true);
            allAlleys.get(animatorIndex).getTextLayout().getText(digitBorrowingIndex).setResult();
            allAlleys.get(animatorIndex).getTextLayout().getText(digitBorrowingIndex).setText(mComponent.overheadVal.toString());
        }
    }

}
