package cmu.xprize.asm_component;

import android.animation.ObjectAnimator;
import android.widget.TableRow;

/**
 * all multiplication-specific operations implemented here
 */
public class CAsm_MechanicMultiply extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicMultiply";
    public CAsm_MechanicMultiply(CAsm_Component mComponent) {
        super.init(mComponent);

        //User could choose the order of writing result digits
        CAsm_TextLayout resultTextLayout = allAlleys.get(resultIndex).getTextLayout();
        for(int i = 1; i < resultTextLayout.getChildCount(); i++) {
            resultTextLayout.getTextLayout(i).getText(0).setResult();
            resultTextLayout.getTextLayout(i).getText(1).setResult();
        }
    }

    protected String operation = "x";

    @Override
    public void nextDigit() {

        if (mComponent.digitIndex == mComponent.numSlots-1) {
            super.nextDigit();
        }
        else {

            CAsm_DotBag firstBag, secondBag, resultBag;

            allAlleys.get(resultIndex).nextDigit();

            firstBag = allAlleys.get(firstBagIndex).getDotBag();
            secondBag = allAlleys.get(secondBagIndex).getDotBag();
            resultBag = allAlleys.get(resultIndex).getDotBag();

            resultBag.setRows(firstBag.getCols());
            resultBag.setCols(secondBag.getCols());

            mComponent.setDotBagsVisible(true, mComponent.digitIndex);
        }

    }

    @Override
    public void preClickSetup() {

        if (mComponent.digitIndex != mComponent.numSlots-1) {
            return;
        }

        CAsm_DotBag overheadBag = allAlleys.get(overheadIndex).getDotBag();
        overheadBag.setDrawBorder(false);

/*        CAsm_DotBag firstBag = allAlleys.get(firstBagIndex).getDotBag();
        firstBag.wiggle(300, 1, 100, .05f);*/

        allAlleys.get(secondBagIndex).getDotBag().setIsClickable(false);

    }


    @Override
    public void handleClick() {

        super.handleClick();

        int dy;

        CAsm_DotBag firstBag = allAlleys.get(firstBagIndex).getDotBag();
        CAsm_DotBag secondBag = allAlleys.get(secondBagIndex).getDotBag();
        CAsm_DotBag resultBag = allAlleys.get(resultIndex).getDotBag();

        final CAsm_Dot clickedDot = firstBag.findClickedDot();

        if (clickedDot == null) {return;}

        int colsToAdd = secondBag.getCols();
        int resultRows = resultBag.getRows();

        for (int i = 0; i < colsToAdd; i++) {
            resultBag.addDot(resultRows, i);
        }

        if (resultRows == 0) {
            dy = mComponent.alleyMargin + secondBag.getHeight();
        }
        else {
            dy = secondBag.getHeight() + mComponent.alleyMargin + resultBag.getHeight();
        }

        final TableRow newRow = resultBag.getRow(resultRows);


        newRow.setTranslationY(-dy);
        clickedDot.setHollow(true);
        mComponent.playChime();
        ObjectAnimator anim = ObjectAnimator.ofFloat(newRow, "translationY", 0);
        anim.setDuration(1000);
        setAllParentsClip(newRow, false);
        anim.start();

    }

}
