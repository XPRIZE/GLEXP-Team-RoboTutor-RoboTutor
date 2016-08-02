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
    
    private int minuendIndex;
    private int extraIndex;


    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}

    @Override
    public void nextDigit() {

        previouslyBorrowed = willBorrow;
        willBorrow = false;
        dotBagBorrowed = false;

        if (previouslyBorrowed) {
            CAsm_Text borrowedText = allAlleys.get(firstBagIndex).getTextLayout().getText(parent.digitIndex);
            borrowedText.setStruck(true);
        }

        super.nextDigit();

        Integer minuend, subtrahend;

        minuendIndex = (previouslyBorrowed)?overheadIndex:firstBagIndex;
        minuend = allAlleys.get(minuendIndex).getCurrentDigit();

        subtrahend = allAlleys.get(secondBagIndex).getCurrentDigit();

        if (minuend - subtrahend < 0) {

            willBorrow = true;

            parent.overheadIndex = overheadIndex;
            CAsm_TextLayout overheadLayout = allAlleys.get(parent.overheadIndex).getTextLayout();
            overheadLayout.getText(parent.digitIndex-1).setResult();

            CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();
            parent.overheadVal = firstBagLayout.getDigit(parent.digitIndex-1)-1;

        }


    }

    @Override
    public void preClickSetup() {

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

        int correspondingCol;
        CAsm_Dot clickedDot = null;

        CAsm_DotBag minuendDotBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag clickedBag = allAlleys.get(secondBagIndex).getDotBag(); // only one possible dotbag to look at

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
            int remainingDotsToClick = minuendBag.getCols() - clickedBag.getCols();
            correspondingCol = 10 - (remainingDotsToClick - clickedDotCol);
        }
        else {
            correspondingCol = clickedDotCol + dotOffset;
        }

        if (correspondingCol < 0) {
            clickedDot.setIsClickable(true);
            return;
        }

        clickedDot.setHollow(true);

        CAsm_Dot correspondingDot = minuendDotBag.getDot(0, correspondingCol);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (minuendDotBag.getVisibleDots().size() == parent.corDigit) {
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

        else if (minuendDotBag.getVisibleDots().size() == 0) {
            borrow();
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
                changingBag.removeDots(numVisibleDots, numInvisibleDots + numVisibleDots - 1);
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
        minuendIndex -= 1;

        CAsm_DotBag borrowBag = allAlleys.get(minuendIndex).getDotBag();
        CAsm_DotBag firstDotBag = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        // update texts

        CAsm_TextLayout firstBagLayout = allAlleys.get(firstBagIndex).getTextLayout();
        CAsm_Text origSourceText = firstBagLayout.getText(parent.digitIndex-1);
        Integer origSourceDigit = firstBagLayout.getDigit(parent.digitIndex-1);
        origSourceText.setStruck(true);

        CAsm_TextLayout overheadLayout = allAlleys.get(overheadIndex).getTextLayout();
        CAsm_Text updatedSourceText = overheadLayout.getText(parent.digitIndex-1);
        updatedSourceText.setText(String.valueOf(origSourceDigit-1));

        CAsm_Text origDestText = firstBagLayout.getText(parent.digitIndex);
        Integer origDestDigit = firstBagLayout.getDigit(parent.digitIndex);
        origDestText.setStruck(true);

        CAsm_Text updatedDestText = overheadLayout.getText(parent.digitIndex);
        updatedDestText.setText(String.valueOf(10 + origDestDigit));

        borrowBag.setDrawBorder(true);
        borrowBag.setRows(1);
        borrowBag.setCols(10);
        borrowBag.setTranslationX(0);

        resultDotBag.removeDots(0, resultDotBag.getCols()-1);
        resultDotBag.setTranslationX(0);

        dotOffset = borrowBag.getCols() - secondDotBag.getCols();
        secondDotBag.setTranslationX(dotOffset*secondDotBag.getSize());


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

}
