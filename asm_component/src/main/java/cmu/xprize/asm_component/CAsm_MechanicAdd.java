package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.ArrayList;

/**
 * all addition specific operations are implemented here
 */
public class CAsm_MechanicAdd extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicAdd";
    public CAsm_MechanicAdd(CAsm_Component parent) {super.init(parent);}

    protected String operation = "+";

    @Override
    public void preClickSetup() {
    }


    @Override
    public void handleClick() {

        CAsm_DotBag currBag;
        CAsm_DotBag clickedBag = null;
        CAsm_DotBag resultBag = allAlleys.get(allAlleys.size() - 1).getDotBag();
        int alleyNum = 0;

        for (int i = 0; i < allAlleys.size(); i++) {
            currBag = allAlleys.get(i).getDotBag();
            if (currBag.getIsClicked()) {
                clickedBag = currBag;
                alleyNum = i;
                break;
            }
        }

        if (clickedBag == null) {
            return;
        }

        CAsm_Dot clickedDot = clickedBag.findClickedDot();

        if (clickedDot != null) {
            animateAdd(clickedDot, alleyNum);
        }

    }

    public void animateAdd(CAsm_Dot clickedDot, int alleyNum) {

        final CAsm_DotBag resultBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        int dx = determineColumnDX(clickedDot, resultBag.getCols());
        int dy = determineDY(alleyNum, allAlleys.size()-1);

        final CAsm_Dot oldDot = clickedDot;
        final CAsm_Dot newDot = resultBag.addDot(0, resultBag.getCols());
        newDot.setTranslationX(-dx);
        newDot.setTranslationY(-dy);
        newDot.setIsClickable(false);

        ObjectAnimator animX = ObjectAnimator.ofFloat(newDot, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newDot, "translationY", 0);

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(1000);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                oldDot.setHollow(true);
                setAllParentsClip(newDot, false);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (resultBag.getCols() >= 4) {
                    carry();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animSetXY.start();
    }

    private int determineColumnDX(CAsm_Dot startDot, int targetCol) {

        int dotSize = startDot.getWidth();
        return dotSize * (targetCol - startDot.getCol());
    }

    private int determineDY(int startAlley, int endAlley) {

        int dy = 0;
        for (int i = startAlley + 1; i <= endAlley; i++) {
            dy += parent.alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }

    private void carry() {

        // shrink result dotbag to dime

        CAsm_DotBag resultBag = allAlleys.get(allAlleys.size() - 1).getDotBag();
        shrink(resultBag);

        // move dime to carry location

        // create new result dotbag

    }

    private void shrink (final CAsm_DotBag resultBag) {

        // TODO: works if wait till animations are done

        ObjectAnimator anim;
        float currRight, newRight;
        CAsm_Dot currDot;
        int dotSize = resultBag.getSize();

        ArrayList<Animator> allAnimations = new ArrayList<>();

        for (int i = 0; i < resultBag.getCols(); i++) {

            currDot = resultBag.getDot(0, i);
            anim = ObjectAnimator.ofFloat(currDot, "translationX", -i*dotSize);
            allAnimations.add(anim);
        }

        currRight = resultBag.getBounds().right;
        newRight = currRight - (dotSize*(resultBag.getCols()-1));
        anim = ObjectAnimator.ofFloat(resultBag, "right", currRight, newRight);
        allAnimations.add(anim);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(allAnimations);
        animSet.setDuration(1000);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                carryAnimation();
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

    private void carryAnimation() {

        ObjectAnimator animX, animY;

        CAsm_DotBag animatorBag = allAlleys.get(0).getDotBag();
        CAsm_DotBag resultBag = allAlleys.get(allAlleys.size()-1).getDotBag();
        
        CAsm_Dot carryDot = animatorBag.addDot(0,0);
        carryDot.setImageName("star");
        carryDot.setTranslationY(determineDY(0, allAlleys.size()-1));
        setAllParentsClip(carryDot, false);


        int digitsToTranslate = parent.numSlots - parent.digitIndex;

        float transX = -(scale*ASM_CONST.rightPadding +
                scale*ASM_CONST.textBoxWidth*(digitsToTranslate + .5f) + resultBag.getSize());

        animX = ObjectAnimator.ofFloat(carryDot, "translationX", carryDot.getTranslationX(), transX);
        animY = ObjectAnimator.ofFloat(carryDot, "translationY", carryDot.getTranslationY(), determineDY(0, 1));

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);
        animSet.start();


    }


}
