package cmu.xprize.asm_component;

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

    // defined alley indices since there will always be a fixed number
    int animatorIndex = 0;
    int overheadIndex = 1;
    int firstBagIndex = 2;
    int secondBagIndex = 3;
    int resultIndex = 4;

    int extraIndex;


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

        int minuendIndex;
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

        int animatedIndex;

        if (previouslyBorrowed) {
            animatedIndex = overheadIndex;
            extraIndex = firstBagIndex;
        } else {
            animatedIndex = firstBagIndex;
            extraIndex = overheadIndex;
        }

        CAsm_DotBag animatedBag = allAlleys.get(animatedIndex).getDotBag();
        animatedBag.setDrawBorder(true);

        CAsm_DotBag extraBag = allAlleys.get(extraIndex).getDotBag();
        extraBag.setCols(0);
        extraBag.setDrawBorder(false);

        // right align
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        dotOffset = (animatedBag.getCols()-secondDotBag.getCols());
        if (dotOffset < 0) {
            animatedBag.setTranslationX(-dotOffset * animatedBag.getSize() + translationX);
            resultDotBag.setTranslationX(-dotOffset * animatedBag.getSize() + translationX);
        }
        else {
            secondDotBag.setTranslationX(dotOffset * secondDotBag.getSize() + translationX);
        }

        // bring result dotbag down

        createDownwardBagAnimator(animatedIndex).start();

    }

    @Override
    public void handleClick() {


        super.handleClick();

        int correspondingCol;
        CAsm_Dot clickedDot = null;
        CAsm_DotBag clickedBag = allAlleys.get(secondBagIndex).getDotBag(); // only one possible dotbag to look at
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        if (clickedBag.getIsClicked()) {
            clickedDot = clickedBag.findClickedDot();
        }

        // make sure dot was clicked
        if (clickedDot == null) {
            return;
        }


        int clickedDotCol = clickedDot.getCol();

        if (dotBagBorrowed) {
            CAsm_DotBag originalBag = allAlleys.get(firstBagIndex).getDotBag();
            int remainingDotsToClick = clickedBag.getCols() - originalBag.getCols();
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

        CAsm_Dot correspondingDot = resultDotBag.getDot(0, correspondingCol);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (resultDotBag.getVisibleDots().size() == parent.corDigit) {
            subtractLayoutChange(clickedDot);
        }

        else if (resultDotBag.getVisibleDots().size() == 0) {
            borrow();
        }
    }

    private void subtractLayoutChange(CAsm_Dot clickedDot) {

        final CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        int numVisibleDots = resultDotBag.getVisibleDots().size();
        int numInvisibleDots = resultDotBag.getCols() - numVisibleDots;

        int dotSize = clickedDot.getWidth();
        float currRight = resultDotBag.getBounds().right;
        float newRight = currRight - (dotSize * numInvisibleDots);


        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "right", currRight, newRight);
        anim.setDuration(1000);
        anim.start();

        resultDotBag.removeDots(numVisibleDots, numInvisibleDots + numVisibleDots - 1);


    }

    private void borrow() {

        dotBagBorrowed = true;

        CAsm_DotBag borrowBag = allAlleys.get(overheadIndex).getDotBag();
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

        createDownwardBagAnimator(overheadIndex).start();

        dotOffset = resultDotBag.getCols() - secondDotBag.getCols();
        secondDotBag.setTranslationX(dotOffset*secondDotBag.getSize());


    }

    private ObjectAnimator createDownwardBagAnimator(int startIndex) {

        CAsm_DotBag firstDotBag = allAlleys.get(startIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        resultDotBag.setRows(firstDotBag.getRows());
        resultDotBag.setCols(firstDotBag.getCols());
        resultDotBag.setImage(firstDotBag.getImageName());
        resultDotBag.setIsClickable(false);

        setAllParentsClip(resultDotBag, false);
        firstDotBag.setHollow(true);

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
