package cmu.xprize.asm_component;

import android.animation.ObjectAnimator;
import android.widget.TableRow;

/**
 * all multiplication-specific operations implemented here
 */
public class CAsm_MechanicMultiply extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicMultiply";
    public CAsm_MechanicMultiply(CAsm_Component parent) {super.init(parent);}

    protected String operation = "x";

    @Override
    public void preClickSetup() {

        CAsm_DotBag firstBag = allAlleys.get(0).getDotBag();
        firstBag.wiggle(300, 2, 100, .05f);

        allAlleys.get(1).getDotBag().setIsClickable(false);

    }


    @Override
    public void handleClick() {

        super.handleClick();

        int dy;

        CAsm_DotBag firstBag = allAlleys.get(0).getDotBag();
        CAsm_DotBag secondBag = allAlleys.get(1).getDotBag();
        CAsm_DotBag resultBag = allAlleys.get(2).getDotBag();

        final CAsm_Dot clickedDot = firstBag.findClickedDot();

        if (clickedDot == null) {return;}

        int colsToAdd = secondBag.getCols();
        int resultRows = resultBag.getRows();

        for (int i = 0; i < colsToAdd; i++) {
            resultBag.addDot(resultRows, i);
        }

        if (resultRows == 0) {
            dy = parent.alleyMargin + secondBag.getHeight();
        }
        else {
            dy = secondBag.getHeight() + parent.alleyMargin + resultBag.getHeight();
        }

        final TableRow newRow = resultBag.getRow(resultRows);


        newRow.setTranslationY(-dy);
        clickedDot.setHollow(true);

        ObjectAnimator anim = ObjectAnimator.ofFloat(newRow, "translationY", 0);
        anim.setDuration(1000);
        anim.start();

    }

}
