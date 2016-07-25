package cmu.xprize.asm_component;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 *
 */
public class CAsm_MechanicBase implements IDotMechanics {

    protected ArrayList<CAsm_Alley> allAlleys;
    protected CAsm_Component parent;
    protected String operation = "";

    float scale;
    float translationX;

    static final String TAG = "CAsm_MechanicBase";

    protected void init(CAsm_Component parent) {

        this.parent = parent;
        this.allAlleys = parent.allAlleys;
        this.scale = parent.getResources().getDisplayMetrics().density;

    }

    public void next(){

        reset();

    }

    public void nextDigit(){

        CAsm_DotBag currBag;

        translationX -= scale*ASM_CONST.textBoxWidth;

        for (CAsm_Alley alley: allAlleys) {
            currBag = alley.getDotBag();
            currBag.setTranslationX(translationX);
        }

        preClickSetup();

    }

    public void preClickSetup() {}

    public void handleClick() {}


    public String getOperation() {return operation;}

    // TODO: fix this function - copied from stack overflow
    protected static void setAllParentsClip(View v, boolean enabled) {
        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }
    
    /* reset any changesmade by mechanics */
    public void reset() {

        CAsm_DotBag currBag;
        translationX = scale*ASM_CONST.textBoxWidth;

        for (CAsm_Alley alley: allAlleys) {

            currBag = alley.getDotBag();
            currBag.setTranslationX(translationX);
            currBag.setTranslationY(0);
        }

    }


}
