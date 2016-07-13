package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by mayankagrawal on 7/13/16.
 */
public class CAsm_MechanicSubtract extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicSubtract";

    public CAsm_MechanicSubtract(CAsm_Component parent) {super.init(parent);}

    @Override
    public void handleClick() {

        Dot clickedDot = null;
        DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();
//      clickedBag will always be the middle number. 3 - 2 = 1. It is referring to 2
        DotBag clickedBag = allAlleys.get(1).getDotBag(); // only one possible dotbag to look at

        if (clickedBag.getIsClicked()) {
            clickedDot = clickedBag.findClickedDot();
        }

        // make sure dot was clicked
        if (clickedDot == null) {
            return;
        }

        int clickedDotCol = clickedDot.getCol();

        clickedDot.setHollow(true);

        Dot correspondingDot = resultDotBag.getDot(0, clickedDotCol);
        correspondingDot.setVisibility(View.INVISIBLE);

        if (resultDotBag.getVisibleDots().size() == parent.corValue) {
            subtractLayoutChange(clickedDot);
        }
    }

    private void subtractLayoutChange(Dot clickedDot) {

        final DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        final ArrayList<Dot> visibleDots = resultDotBag.getVisibleDots();
        final int numVisibleDots = visibleDots.size();
        int numInvisibleDots = allAlleys.get(0).getNum() - numVisibleDots;

        int dotSize = clickedDot.getWidth();
        float currRight = resultDotBag.getBounds().right;
        float newRight = currRight - dotSize * numInvisibleDots;

        float dx = newRight - currRight;

        ArrayList<Animator> allAnimations = new ArrayList<>();

        for (int i = 0; i < numVisibleDots; i++) {
            allAnimations.add(ObjectAnimator.ofFloat(visibleDots.get(i), "translationX", dx));
        }
        allAnimations.add(ObjectAnimator.ofFloat(resultDotBag, "right", currRight, newRight));

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(allAnimations);
        animSet.setDuration(1000);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                for (int i = 0; i < numVisibleDots; i++) {
                    visibleDots.get(i).setTranslationX(0); // reset dots
                }

                resultDotBag.removeInvisibleDots();
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

    @Override
    public void preAnimation() {

        int numAlleys, rows, cols, size, dy;
        String imageName;

        numAlleys = allAlleys.size();

        DotBag firstDotBag = allAlleys.get(0).getDotBag();
        DotBag resultDotBag = allAlleys.get(numAlleys - 1).getDotBag();

        rows = firstDotBag.getRows();
        cols = firstDotBag.getCols();
        imageName = firstDotBag.getImageName();
        size = firstDotBag.getSize();

        resultDotBag.setParams(size, rows, cols, false, imageName);
        setAllParentsClip(resultDotBag, false);

        firstDotBag.setHollow(true);

        // calc distance
        dy = 0;
        for (int i = numAlleys - 1; i > 0; i--) {
            dy += allAlleys.get(i).getHeight() + parent.alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);
        anim.start();

    }


}
