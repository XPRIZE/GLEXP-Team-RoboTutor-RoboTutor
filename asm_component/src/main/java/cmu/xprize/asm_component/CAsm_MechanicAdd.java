package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.EditText;

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
        int dy = determineAlleyDY(alleyNum, allAlleys.size()-1);

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
                if ((resultBag.getCols() >= 10) & resultBag.isNotTranslatedX(translationX)) {
                    performCarry();
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

    private int determineAlleyDY(int startAlley, int endAlley) {

        int dy = 0;
        for (int i = startAlley + 1; i <= endAlley; i++) {
            dy += parent.alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }

    private void performCarry() {

        // shrink result dotbag to dime

        AnimatorSet shrinkAnimation = createShrinkAnimation();
        AnimatorSet carryAnimation = createCarryAnimation();

        AnimatorSet toPlay = new AnimatorSet();
        toPlay.setDuration(1000);
        toPlay.playSequentially(shrinkAnimation, carryAnimation);
        toPlay.start();

    }

    private AnimatorSet createShrinkAnimation () {

        ObjectAnimator anim;
        CAsm_Dot currDot;
        int colsToTranslate;

        final CAsm_DotBag resultBag = allAlleys.get(allAlleys.size()-1).getDotBag();
        int dotSize = resultBag.getSize();

        ArrayList<Animator> allAnimations = new ArrayList<>();

        for (int i = 0; i < resultBag.getCols(); i++) {

            currDot = resultBag.getDot(0, i);
            colsToTranslate = (i < 10)?i:9;
            anim = ObjectAnimator.ofFloat(currDot, "translationX", -colsToTranslate*dotSize);
            allAnimations.add(anim);

        }

        float currRight = resultBag.getBounds().right;
        float newRight = currRight - (dotSize*9);
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
                resultBag.removeDots(1, 9);

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

    private AnimatorSet createCarryAnimation() {

        ObjectAnimator animX, animY;

        CAsm_DotBag animatorBag = allAlleys.get(0).getDotBag();
        final CAsm_DotBag resultBag = allAlleys.get(allAlleys.size()-1).getDotBag();

        final CAsm_Dot carryDot = resultBag.getDot(0,0);
        setAllParentsClip(carryDot, false);

        float transX = -(scale*ASM_CONST.rightPadding +
                scale*ASM_CONST.textBoxWidth*(1.5f) + resultBag.getSize());

        float transY = -determineAlleyDY(1, allAlleys.size()-1);

        animX = ObjectAnimator.ofFloat(carryDot, "translationX", carryDot.getTranslationX(), transX);
        animY = ObjectAnimator.ofFloat(carryDot, "translationY", carryDot.getTranslationY(), transY);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                carryDot.setImageName("star");

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updateCarryText();
                replaceCarryDot();
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

    private void updateCarryText(){

        CAsm_Text textLayout = allAlleys.get(1).getText();
        EditText t = textLayout.getText(parent.digitIndex-1);
        t.setText("1");

    }

    private void replaceCarryDot() {

        CAsm_Dot newDot;
        CAsm_DotBag animatorBag = allAlleys.get(0).getDotBag();
        CAsm_DotBag resultBag = allAlleys.get(allAlleys.size()-1).getDotBag();

        resultBag.removeDots(0,0);

        newDot = animatorBag.addDot(0,0);
        newDot.setImageName("star");
        setAllParentsClip(newDot, false);

        float transX = -(scale*ASM_CONST.rightPadding +
                scale*ASM_CONST.textBoxWidth*(1.5f) + resultBag.getSize());

        float transY = determineAlleyDY(0,1);

        newDot.setTranslationX(transX);
        newDot.setTranslationY(transY);



    }

}
