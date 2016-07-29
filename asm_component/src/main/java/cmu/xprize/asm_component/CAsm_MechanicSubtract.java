package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Paint;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * all subtraction-specific operations are implemented here
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";
    protected String operation = "-";

    private int dotOffset;
    private boolean borrowed = false;

    // defined alley indices since there will always be a fixed number
    int animatorIndex = 0;
    int overheadIndex = 1;
    int firstBagIndex = 2;
    int secondBagIndex = 3;
    int resultIndex = 4;


    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}

    @Override
    public void nextDigit(){
        super.nextDigit();
        borrowed = false;
    }

    @Override
    public void preClickSetup() {

        int dy;

        // 0th index is animator dotbag and 1st is carry/borrow
        CAsm_DotBag borrowBag = allAlleys.get(overheadIndex).getDotBag();
        CAsm_DotBag firstDotBag = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        borrowBag.setDrawBorder(false);

        // right align

        dotOffset = (firstDotBag.getCols()-secondDotBag.getCols());
        if (dotOffset < 0) {
            firstDotBag.setTranslationX(-dotOffset * firstDotBag.getSize() + translationX);
            resultDotBag.setTranslationX(-dotOffset * firstDotBag.getSize() + translationX);
        }
        else {
            secondDotBag.setTranslationX(dotOffset * secondDotBag.getSize() + translationX);
        }

        // bring result dotbag down

        createDownwardBagAnimator(firstBagIndex).start();

    }

    @Override
    public void handleClick() {

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

        if (borrowed) {
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
            // need to borrow
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

        borrowed = true;

        CAsm_DotBag borrowBag = allAlleys.get(overheadIndex).getDotBag();
        CAsm_DotBag firstDotBag = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        // update texts

        CAsm_Text firstBagLayout = allAlleys.get(firstBagIndex).getText();
        EditText origSourceText = firstBagLayout.getText(parent.digitIndex-1);
        Integer origSourceDigit = firstBagLayout.getDigit(parent.digitIndex-1);
        origSourceText.setPaintFlags(origSourceText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        CAsm_Text overheadLayout = allAlleys.get(overheadIndex).getText();
        EditText updatedSourceText = overheadLayout.getText(parent.digitIndex-1);
        updatedSourceText.setText(String.valueOf(origSourceDigit-1));

        EditText origDestText = firstBagLayout.getText(parent.digitIndex);
        Integer origDestDigit = firstBagLayout.getDigit(parent.digitIndex);
        origDestText.setPaintFlags(origDestText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        EditText updatedDestText = overheadLayout.getText(parent.digitIndex);
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
