package cmu.xprize.asm_component;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.TCONST;

/**
 *
 */
public class CAsm_MechanicBase implements IDotMechanics {

    protected ArrayList<CAsm_Alley> allAlleys;
    protected CAsm_Component mComponent;

    float scale;

    // defined alley indices since there will always be a fixed number
    protected int animator3Index = 0;
    protected int animator2Index = 1;
    protected int animator1Index = 2;
    protected int overheadIndex = 3;
    protected int firstBagIndex = 4;
    protected int secondBagIndex = 5;
    protected int resultIndex = 6;

    protected int clickedTextLayoutIndex = -1;

    static final String TAG = "CAsm_MechanicBase";

    protected void init(CAsm_Component mComponent) {

        this.mComponent = mComponent;
        this.allAlleys  = mComponent.allAlleys;
        this.scale      = mComponent.getResources().getDisplayMetrics().density;

    }


    public void next(){
        reset();
    }


    /**
     * Called by C_Component.nextDigit()
     */
    public void nextDigit(){

        for (CAsm_Alley alley: allAlleys) {
            alley.nextDigit();
        }

        if (mComponent.dotbagsVisible) {
            preClickSetup();
        }

        resultIndex = allAlleys.size()-1;

        highlightDigits();
    }


    public void highlightDigits() {

        CAsm_Text text;

        for (CAsm_Alley alley: allAlleys) {
            text = alley.getTextLayout().getTextLayout(mComponent.digitIndex).getText(1); // √√√
            ASM_CONST.logAnnoyingReference(alley.getId(), mComponent.digitIndex, 1, "highlightDigits()");

            if (!text.getIsStruck()) {
                CAnimatorUtil.zoomInOut(text, 1.5f, 1500L);
            }
        }
    }

    public void preClickSetup() {}

    /**
     * Handle the click on text in MechanicBase
     * Handle the click on dots in MechanicAdd, MechanicSubtract, MechanicMultiply
     */
    public void handleClick() {

        if(mComponent != null) {

            if (mComponent.clickPaused) {
                return;
            }

            CAsm_TextLayout clickedTextLayout = findClickedTextLayout();

            if (clickedTextLayout == null) {
                mComponent.exitWrite();
                return;
            }

            CAsm_Text clickedText = clickedTextLayout.findClickedText();

            if (clickedText != null) {
                if (clickedText.isWritable) {
                    mComponent.delAddFeature(TCONST.ASM_CLICK_ON_DOT, "");
                    if (mComponent.overheadTextSupplement != null) {
                        if ((clickedText.equals(mComponent.overheadText) || clickedText.equals(mComponent.overheadTextSupplement))
                                && mComponent.overheadText.isWritable && mComponent.overheadTextSupplement.isWritable)
                            mComponent.updateText(mComponent.overheadTextSupplement, mComponent.overheadText, clickedTextLayoutIndex < resultIndex);
                        else
                            mComponent.updateText(null, clickedText, clickedTextLayoutIndex < resultIndex);
                    } else
                        mComponent.updateText(null, clickedText, clickedTextLayoutIndex < resultIndex);
                } else {
                    clickedTextLayout.setIsClicked(true);
                    clickedText.setIsClicked(true);
                }
            } else {
                mComponent.exitWrite();
            }
        }
    }

    /**
     * If user input the overhead value correctly
     */
    public void correctOverheadText() {
        // whenever they put in the right overhead text

        mComponent.overheadText.cancelResult();
        if(mComponent.overheadTextSupplement != null)
            mComponent.overheadTextSupplement.cancelResult();
        mComponent.overheadVal = null;
        mComponent.overheadText = null;
        mComponent.overheadTextSupplement = null;

    }

    /* reset any changes made by mechanics */
    public void reset() {

        CAsm_DotBag currBag;

        for (CAsm_Alley alley: allAlleys) {

            currBag = alley.getDotBag();
            currBag.setTranslationX(0);
            currBag.setTranslationY(0);
        }

        mComponent.setDotBagsVisible(false, mComponent.digitIndex);

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

    protected CAsm_TextLayout findClickedTextLayout() {

        CAsm_TextLayout currTextLayout;
        CAsm_TextLayout clickedTextLayout = null;

        for (CAsm_Alley alley: allAlleys) {

            currTextLayout = alley.getTextLayout();

            if (currTextLayout.getIsClicked()) {
                clickedTextLayoutIndex = allAlleys.indexOf(alley);
                clickedTextLayout = currTextLayout;
                break;
            }
        }

        return clickedTextLayout;

    }

    // Overridden by child class.
    public void highlightOverhead() {

    }

    public void highlightResult() {
        mComponent.highlightText(allAlleys.get(resultIndex).getTextLayout().getTextLayout(mComponent.digitIndex).getText(1)); // √√√
        ASM_CONST.logAnnoyingReference(resultIndex, mComponent.digitIndex, 1, "highlightOverheadOrResult()");
        mComponent.delAddFeature(TCONST.ASM_ALL_DOTS_DOWN, "");
    }

}
