package cmu.xprize.asm_component;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cmu.xprize.util.CAnimatorUtil;

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

        if (parent.dotbagsVisible) {
            preClickSetup();
        }

        parent.overheadIndex = null;
        parent.overheadVal = null;

        highlightDigits();

    }

    public void highlightDigits() {

        CAsm_Text text;

        for (int i = 0; i < allAlleys.size(); i++) {
            text = allAlleys.get(i).getTextLayout().getText(parent.digitIndex);

            if (text.getIsStruck()) {
                text.reset();
                text.setStruck(true);
            }
            else {
                CAnimatorUtil.zoomInOut(text, 1.5f, 1500L);
            }
        }
    }

    public void preClickSetup() {}

    public void handleClick() {

        CAsm_TextLayout currTextLayout;
        CAsm_TextLayout clickedTextLayout = null;

        for (int i = 0; i < this.allAlleys.size(); i++) {

            currTextLayout = this.allAlleys.get(i).getTextLayout();
            if (currTextLayout.getIsClicked()) {
                clickedTextLayout = currTextLayout;
                break;
            }
        }

        if (clickedTextLayout == null) {
            parent.exitWrite();
            return;
        }

        CAsm_Text clickedText = clickedTextLayout.findClickedText();

        if (clickedText != null && clickedText.isWritable) { //TODO: Decide whether this is private.
            parent.updateText(clickedText);
        } else {
            parent.exitWrite();
        }
    }

    public String getOperation() {return operation;}

    
    /* reset any changes made by mechanics */
    public void reset() {

        CAsm_DotBag currBag;
        translationX = scale*ASM_CONST.textBoxWidth;

        for (CAsm_Alley alley: allAlleys) {

            currBag = alley.getDotBag();
            currBag.setTranslationX(translationX);
            currBag.setTranslationY(0);
            currBag.setVisibility(View.VISIBLE);
        }

    }

    // TODO: fix this function - copied from stack overflow
    protected static void setAllParentsClip(View v, boolean enabled) {
        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }


}
