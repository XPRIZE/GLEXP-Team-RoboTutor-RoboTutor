package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

/**
 * Created by mayankagrawal on 7/13/16.
 */
public class CAsm_MechanicAdd extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicAdd";

    public CAsm_MechanicAdd(CAsm_Component parent) {super.init(parent);}

    @Override
    public void preClickAnimation() {
    }

    @Override
    public String getOperation() {
        return "+";
    }

    @Override
    public void handleClick() {

        DotBag currBag;
        DotBag clickedBag = null;
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

        Dot clickedDot = clickedBag.findClickedDot();

        if (clickedDot != null) {
            animateAdd(clickedDot, alleyNum);
        }


    }

    public void animateAdd(Dot clickedDot, int alleyNum) {

        DotBag resultBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        int dx = determineDX(clickedDot, resultBag.getCols());
        int dy = determineDY(alleyNum);

        final Dot oldDot = clickedDot;
        final Dot newDot = resultBag.addDot(0, resultBag.getCols());
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

    private int determineDX(Dot startDot, int targetCol) {

        int dotSize = startDot.getWidth();
        return dotSize * (targetCol - startDot.getCol());
    }

    private int determineDY(int alleyNum) {

        int dy = 0;
        for (int i = alleyNum + 1; i < allAlleys.size(); i++) {
            dy += parent.alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }



}
