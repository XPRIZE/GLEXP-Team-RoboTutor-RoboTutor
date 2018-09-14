package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import cmu.xprize.util.TCONST;

/**
 * all addition specific operations are implemented here
 */
public class CAsm_MechanicAdd extends CAsm_MechanicBase implements IDotMechanics {

    public CAsm_MechanicAdd(CAsm_Component mComponent) {
        super.init(mComponent);
    }

    static final String TAG = "CAsm_MechanicAdd";

    /**
     * Called by C_Component.nextDigit()
     */
    @Override
    public void nextDigit() {

        super.nextDigit();

        CAsm_TextLayout currLayout;
        Integer         currValue;
        int             totalValue = 0;

        for (CAsm_Alley alley: allAlleys) {
            // MATHFIX_2 this must be carrying?
            if (allAlleys.indexOf(alley) == overheadIndex) {
                CAsm_Text cur = alley.getTextLayout().getTextLayout(mComponent.digitIndex).getText(1); // √√√
                ASM_CONST.logAnnoyingReference(alley.getId(), mComponent.digitIndex, 1, "nextDigit()");
                if (cur.getText().equals("1") || cur.isWritable == true) {
                    cur.cancelResult();
                    cur.setText("1");
                    alley.getDotBag().setRows(1);
                    alley.getDotBag().setCols(1);
                    alley.getDotBag().setImage(mComponent.curImage);
                    alley.getDotBag().setIsClickable(true);
                    alley.getDotBag().resetOverflowNum();
                    alley.getDotBag().setDrawBorder(true);
                }
                else {
                    alley.getDotBag().setCols(0);
                    alley.getDotBag().setDrawBorder(false);
                }
            }

            // Ensure the hollow is reset
            //
            alley.getDotBag().setHollow(false);

            currLayout = alley.getTextLayout(); // √√√
            currValue = currLayout.getDigit(mComponent.digitIndex);
            ASM_CONST.logAnnoyingReference(alley.getId(), mComponent.digitIndex, -1, "getDigit()");

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

        // only show dotbags that are being operated on

        CAsm_DotBag currBag;

        for (int i = 0; i < allAlleys.size(); i++) {

            currBag = allAlleys.get(i).getDotBag();

            if(i != overheadIndex) {

                if (i != firstBagIndex && i != secondBagIndex && i != resultIndex) {
                    currBag.setCols(0);
                    currBag.setDrawBorder(false);
                } else {
                    currBag.setDrawBorder(true);
                }
            }
        }
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
            mComponent.delAddFeature(TCONST.ASM_ADD_PROMPT, "");
            mComponent.delAddFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM, "");
            mComponent.delAddFeature("", TCONST.ASM_CLICK_ON_DOT);


            animateAdd(clickedDot, alleyNum);

            mComponent.playChime();
        }

        if (allDotsDown()) {
            mComponent.postEvent(ASM_CONST.SCAFFOLD_RESULT_BEHAVIOR);
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
            newDot = resultBag.addDot(0, resultBag.getCols()); // MATHFIX_3 NEXT NEXT NEXT where does add happen?
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
                    Integer temp = allAlleys.get(overheadIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit(); // √√√
                    ASM_CONST.logAnnoyingReference(overheadIndex, mComponent.digitIndex, 1, "animateAdd.onAnimationEnd()");
                    int number1 = (temp == null || temp.equals("")) ? 0 : temp.intValue();
                    int number2 = allAlleys.get(firstBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit().intValue(); // √√√
                    ASM_CONST.logAnnoyingReference(firstBagIndex, mComponent.digitIndex, 1, "animateAdd.onAnimationEnd()");
                    int number3 = allAlleys.get(secondBagIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1).getDigit().intValue(); // √√√
                    ASM_CONST.logAnnoyingReference(secondBagIndex, mComponent.digitIndex, 1, "animateAdd.onAnimationEnd()");

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



    public boolean allDotsDown() {
        CAsm_DotBag dotBag1 = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag dotBag2 = allAlleys.get(secondBagIndex).getDotBag();

        boolean allDotsDown = true;

        for (int i = 0; i < dotBag1.getRows(); i++)
        for (int j = 0; j < dotBag1.getCols(); j++) {
            if (!dotBag1.getDot(i, j).getIsHollow()) {
                Log.d(TAG, "ROW 1: col: " + j + "is Filled");
                allDotsDown = false;
            }
            else {
                Log.d(TAG, "ROW 1: col: " + j + "is Hollow");
            }
        }
        for (int i = 0; i < dotBag2.getRows(); i++)
        for (int j = 0; j < dotBag2.getCols(); j++) {
            if (!dotBag2.getDot(i, j).getIsHollow()) {
                Log.d(TAG, "ROW 2: col: " + j + "is Filled");
                allDotsDown = false;
            }
            else {
                Log.d(TAG, "ROW 2: col: " + j + "is Hollow");
            }
        }

        Log.d(TAG, "All Done: " +  allDotsDown);

        return allDotsDown;
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

        float transX = -(scale * ASM_CONST.rightPadding + resultBag.getSize() +
                scale * ASM_CONST.textBoxWidth * (mComponent.numSlots - mComponent.digitIndex + 1));

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

                CAsm_Text carryText  = allAlleys.get(overheadIndex).getTextLayout().getTextLayout(mComponent.digitIndex-1).getText(1); // √√√
                ASM_CONST.logAnnoyingReference(overheadIndex, mComponent.digitIndex-1, 1, "createCarryAnimation.onAnimationEnd()");

                carryText.setText("1");
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

        CAsm_Text t = allAlleys.get(overheadIndex).getTextLayout().getTextLayout(mComponent.digitIndex - 1).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(overheadIndex, mComponent.digitIndex-1, 1, "setCarryText()");
        t.setResult();

        return t;

    }

}
