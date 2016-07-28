package cmu.xprize.asm_component;

import android.util.Log;
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

    public void handleClick() {

        CAsm_Text currText;
        CAsm_Text clickedText = null;
        int alleyNum = 0;
        Log.v("Test for Null", Integer.toString(allAlleys.size()));

        for (int i = 0; i < this.allAlleys.size(); i++) {
            currText = this.allAlleys.get(i).getText();
            if (currText.getIsClicked()) {
                clickedText = currText;
                break;
            }
        }

        if (clickedText == null) {
            parent.exitWrite();
            return;
        }

        Write_Text clickedField = clickedText.findClickedField();

        if (clickedField != null && clickedField.isWritable) { //TODO: Decide whether this is private.
            parent.enterNumber(clickedField);
        } else {
            parent.exitWrite();
        }
    }

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
