package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;
import android.widget.TableRow;

import cmu.xprize.util.CAnimatorUtil;

/**
 * Created by mayankagrawal on 7/13/16.
 */
public class CAsm_MechanicMultiply extends CAsm_MechanicBase implements IDotMechanics {

    static final String TAG = "CAsm_MechanicMultiply";
    public CAsm_MechanicMultiply(CAsm_Component parent) {super.init(parent);}

    protected String operation = "x";

    @Override
    public void preClickSetup() {

        DotBag firstBag = allAlleys.get(0).getDotBag();
        firstBag.wiggle(300, 2, 100, .05f);

        allAlleys.get(1).getDotBag().setIsClickable(false);

    }


    @Override
    public void handleClick() {

        int dy;

        DotBag firstBag = allAlleys.get(0).getDotBag();
        DotBag secondBag = allAlleys.get(1).getDotBag();
        DotBag resultBag = allAlleys.get(2).getDotBag();

        final Dot clickedDot = firstBag.findClickedDot();

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
