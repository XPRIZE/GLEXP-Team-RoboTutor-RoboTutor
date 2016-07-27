package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.ArrayList;

/**
 * all subtraction-specific operations are implemented here
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";
    protected String operation = "-";

    private int dotOffset;

    // defined alley indices since there will always be a fixed number
    int animatorIndex = 0;
    int overheadIndex = 1;
    int firstBagIndex = 2;
    int secondBagIndex = 3;
    int resultIndex = 4;


    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}


    @Override
    public void preClickSetup() {

        int dy, numAlleys;

        numAlleys = allAlleys.size();

        // 0th index is animator dotbag and 1st is carry/borrow
        CAsm_DotBag borrowBag = allAlleys.get(overheadIndex).getDotBag();
        CAsm_DotBag firstDotBag = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(resultIndex).getDotBag();

        borrowBag.setDrawBorder(false);

        // right align

        dotOffset = (firstDotBag.getCols()-secondDotBag.getCols());
        secondDotBag.setTranslationX(dotOffset*secondDotBag.getSize() + translationX);

        // bring result dotbag down

        resultDotBag.setRows(firstDotBag.getRows());
        resultDotBag.setCols(firstDotBag.getCols());
        resultDotBag.setImage(firstDotBag.getImageName());
        resultDotBag.setIsClickable(false);

        setAllParentsClip(resultDotBag, false);
        firstDotBag.setHollow(true);

        dy = 0;
        for (int i = resultIndex; i > firstBagIndex; i--) {
            dy += allAlleys.get(i).getHeight() + parent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);
        anim.start();

    }

    @Override
    public void handleClick() {

        super.handleClick();

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

        clickedDot.setHollow(true);

        int clickedDotCol = clickedDot.getCol();
        CAsm_Dot correspondingDot = resultDotBag.getDot(0, clickedDotCol + dotOffset);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (resultDotBag.getVisibleDots().size() == parent.corDigit) {
            subtractLayoutChange(clickedDot);
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

}
