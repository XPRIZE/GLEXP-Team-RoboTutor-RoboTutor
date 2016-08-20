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
    protected CAsm_Component mComponent;
    protected String operation = "";

    float scale;

    // defined alley indices since there will always be a fixed number
    protected int animator3Index = 0;
    protected int animator2Index = 1;
    protected int animator1Index = 2;
    protected int overheadIndex = 3;
    protected int firstBagIndex = 4;
    protected int secondBagIndex = 5;
    protected int resultIndex = 6;

    static final String TAG = "CAsm_MechanicBase";

    protected void init(CAsm_Component mComponent) {

        this.mComponent = mComponent;
        this.allAlleys = mComponent.allAlleys;
        this.scale = mComponent.getResources().getDisplayMetrics().density;

    }

    public void next(){

        reset();

    }

    public void nextDigit(){

        for (CAsm_Alley alley: allAlleys) {
            alley.nextDigit();
        }

        if (mComponent.dotbagsVisible) {
            preClickSetup();
        }

        mComponent.overheadText = null;
        mComponent.overheadVal = null;
        resultIndex = allAlleys.size()-1;

        highlightDigits();

    }

    public void highlightDigits() {

        CAsm_Text text;

        for (CAsm_Alley alley: allAlleys) {
            text = alley.getTextLayout().getTextLayout(mComponent.digitIndex).getText(1);

            if(!(mComponent.operation.equals("x") && allAlleys.indexOf(alley) == resultIndex)) {
                if (!text.getIsStruck()) {
                    CAnimatorUtil.zoomInOut(text, 1.5f, 1500L);
                }
            }
        }

        if(mComponent.operation.equals("x")) {
            CAsm_TextLayout resultTextLayout = allAlleys.get(resultIndex).getTextLayout();
            for(int i = 1; i < resultTextLayout.getChildCount(); i++)
                CAnimatorUtil.zoomInOut(resultTextLayout.getTextLayout(i).getText(1), 1.5f, 1500L);
        }
    }

    public void preClickSetup() {}

    public void handleClick() {

        if (mComponent.getClickPaused()) {
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
                mComponent.updateText(clickedText);
            } else {
                clickedTextLayout.setIsClicked(true);
                clickedText.setIsClicked(true);
            }
        } else {
            mComponent.exitWrite();
        }

    }

    public String getOperation() {return operation;}

    public void correctOverheadText() {
        // whenever they put in the right overhead text

        mComponent.overheadText.setWritable(false);
        if(mComponent.overheadTextSupplement != null)
            mComponent.overheadTextSupplement.setWritable(false);
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

        mComponent.setDotBagsVisible(false);

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
                clickedTextLayout = currTextLayout;
                break;
            }
        }

        return clickedTextLayout;

    }

    public void highlightBorrowable() {

    }
}
