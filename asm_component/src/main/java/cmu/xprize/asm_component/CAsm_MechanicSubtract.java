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

    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}


    @Override
    public void preClickSetup() {

        int dy, numAlleys;

        numAlleys = allAlleys.size();

        // 0th index is animator dotbag and 1st is carry/borrow
        CAsm_DotBag firstDotBag = allAlleys.get(2).getDotBag();
        CAsm_DotBag secondDotBag = allAlleys.get(3).getDotBag();
        CAsm_DotBag resultDotBag = allAlleys.get(4).getDotBag();

        // right align

        dotOffset = (firstDotBag.getCols()-secondDotBag.getCols());
        secondDotBag.setTranslationX(dotOffset*secondDotBag.getSize() + translationX);

        // bring result dotbag down

        resultDotBag.update(
                firstDotBag.getRows(), firstDotBag.getCols(), firstDotBag.getImageName(), false);
        setAllParentsClip(resultDotBag, false);
        firstDotBag.setHollow(true);

        dy = 0;
        for (int i = numAlleys - 1; i > 0; i--) {
            dy += allAlleys.get(i).getHeight() + parent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);
        anim.start();

    }

    @Override
    public void handleClick() {

        CAsm_Dot clickedDot = null;
        CAsm_DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();
        CAsm_DotBag clickedBag = allAlleys.get(2).getDotBag(); // only one possible dotbag to look at

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

        final CAsm_DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        final ArrayList<CAsm_Dot> visibleDots = resultDotBag.getVisibleDots();
        final int numVisibleDots = visibleDots.size();
        int numInvisibleDots = allAlleys.get(0).getCurrentDigit() - numVisibleDots;

        int dotSize = clickedDot.getWidth();
        float currRight = resultDotBag.getBounds().right;
        float newRight = currRight - (dotSize * numInvisibleDots);


        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "right", currRight, newRight);
        anim.setDuration(1000);
        anim.start();

        resultDotBag.removeDots(numVisibleDots, numInvisibleDots + numVisibleDots - 1);



    }

}
