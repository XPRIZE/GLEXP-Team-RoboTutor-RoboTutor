package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;


import java.util.ArrayList;

/**
 * all addition specific operations are implemented here
 */
public class CAsm_MechanicAdd extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicAdd";
    public CAsm_MechanicAdd(CAsm_Component mComponent) {super.init(mComponent);}

    protected String operation = "+";

    @Override
    public void nextDigit() {

        super.nextDigit();

        CAsm_TextLayout currLayout;
        Integer currValue;
        int totalValue = 0;

        for (CAsm_Alley alley: allAlleys) {
            if (allAlleys.indexOf(alley) == overheadIndex) {
                CAsm_Text cur = alley.getTextLayout().getTextLayout(mComponent.digitIndex).getText(1);
                if (cur.getText().equals("") && cur.isWritable == true) {
                    cur.cancelResult();
                    cur.setText("1");
                    alley.getDotBag().setRows(1);
                    alley.getDotBag().setCols(1);
                    alley.getDotBag().setImage(mComponent.currImage);
                    alley.getDotBag().setIsClickable(true);
                    alley.getDotBag().resetOverflowNum();
                }
            }

            currLayout = alley.getTextLayout();
            currValue = currLayout.getDigit(mComponent.digitIndex);

            if (currValue != null) {
                totalValue += currValue;
            }

        }

        if (totalValue > 10) { // need to carry
            mComponent.overheadVal = (totalValue - (totalValue % 10))/10;
            mComponent.overheadText = setCarryText();
        }

    }


    @Override
    public void preClickSetup() {
    }


    @Override
    public void handleClick() {

        super.handleClick();

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
            mComponent.playChime();
        }

    }

    public void animateAdd(CAsm_Dot clickedDot, int alleyNum) {

        // create new dot in result bag, but animate it from position of the clicked dot

        final CAsm_DotBag resultBag = allAlleys.get(resultIndex).getDotBag();

        int dx = determineColumnDX(clickedDot, resultBag.getCols()<10 ? resultBag.getCols() : resultBag.getOverflowNum());
        int dy = determineAlleyDY(alleyNum, resultIndex);

        final CAsm_Dot oldDot = clickedDot;
        final CAsm_Dot newDot;
        if(resultBag.getCols() < 10)
            newDot = resultBag.addDot(0, resultBag.getCols());
        else {
            newDot = resultBag.addDot(1, resultBag.getOverflowNum());
            resultBag.addOverflowNum();
        }
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
                if(resultBag.getCols() >= 10 && resultBag.dotsStatic()) {
                    Integer temp = allAlleys.get(overheadIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit();
                    int number1 = (temp == null || temp.equals("")) ? 0 : temp.intValue();
                    int number2 = allAlleys.get(firstBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit().intValue();
                    int number3 = allAlleys.get(secondBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit().intValue();

                    if (number1 + number2 + number3 == 10 + resultBag.getOverflowNum()) {
                        performCarry();
                    }
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
            dy += mComponent.alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }

    private void performCarry() {

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

        final CAsm_DotBag resultBag = allAlleys.get(resultIndex).getDotBag();
        int dotSize = resultBag.getSize();

        ArrayList<Animator> allAnimations = new ArrayList<>();

        for (int i = 0; i < resultBag.getCols(); i++) {

            currDot = resultBag.getDot(0, i);
            colsToTranslate = (i < 10)?i:9;
            anim = ObjectAnimator.ofFloat(currDot, "translationX", -colsToTranslate*dotSize);
            allAnimations.add(anim);

        }

        float currRight = resultBag.getBounds().right;
        float newRight = currRight - (dotSize*(10-resultBag.getOverflowNum()));

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
                resultBag.setCols(resultBag.getOverflowNum());

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

        final CAsm_DotBag resultBag = allAlleys.get(resultIndex).getDotBag();
        final CAsm_Dot carryDot = resultBag.getDot(0,0);

        setAllParentsClip(carryDot, false);

        float transX = -(scale*ASM_CONST.rightPadding + resultBag.getSize() +
                scale*ASM_CONST.textBoxWidth*(mComponent.numSlots - mComponent.digitIndex + .5f));

        float transY = -determineAlleyDY(4, allAlleys.size()-1);

        animX = ObjectAnimator.ofFloat(carryDot, "translationX", carryDot.getTranslationX(), transX);
        animY = ObjectAnimator.ofFloat(carryDot, "translationY", carryDot.getTranslationY(), transY);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                carryDot.setImageName("ten"); // TODO: get image of "10"
                for(int i = resultBag.getOverflowNum() - 1; i > 0; i--)
                    resultBag.getDot(0,i).setImageName("empty");

                int dy = resultBag.getRow(1).getHeight();
                resultBag.getRow(0).setTranslationY(dy);

                ObjectAnimator anim = ObjectAnimator.ofFloat(resultBag.getRow(0), "translationY", 0);
                anim.setDuration(1000);
                setAllParentsClip(resultBag.getRow(1), false);
                anim.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                // reset dotbag
                resultBag.setRows(1);
                //resultBag.setCols(resultBag.getCols()-1);
                resultBag.setImage(resultBag.getImageName());

                CAsm_TextLayout carryLayout = allAlleys.get(overheadIndex).getTextLayout();
                CAsm_Text carryText = carryLayout.getTextLayout(mComponent.digitIndex-1).getText(1);
                Integer currCarryNum = carryLayout.getDigit(mComponent.digitIndex-1);

                if (currCarryNum == null) {
                    carryText.setText("1");
                }
                else {
                    carryText.setText(String.valueOf(1 + currCarryNum));
                }

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

    private CAsm_Text setCarryText(){

        CAsm_TextLayout textLayout = allAlleys.get(overheadIndex).getTextLayout();
        CAsm_Text t = textLayout.getTextLayout(mComponent.digitIndex - 1).getText(1);
        t.setResult();

        return t;

    }

}
