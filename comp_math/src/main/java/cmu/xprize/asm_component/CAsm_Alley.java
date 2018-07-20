package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;


/**
 * Horizontal Alley that has the text on the left and its associated dotbag on the right
 */
public class CAsm_Alley extends LinearLayout {

    private CAsm_TextLayout STextLayout; // contains numbers
    private CAsm_DotBag     SdotBag;     // contains dots

    private int digitIndex;
    private int val;
    private int id;
    private int numSlots;

    private String operation;
    private String image;

    private boolean clickable;

    float scale = getResources().getDisplayMetrics().density;
    final int rightPadding = (int)(ASM_CONST.rightPadding*scale);

    static final String TAG = "CAsm_Alley";

    public CAsm_Alley(Context context) {
        super(context);
        init(context, null);
    }

    public CAsm_Alley(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Alley(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        createText();
        createDotBag();
        setClipToPadding(false);
    }

    public void update(int _val, String _image, int _id, String _operation, boolean _clickable,
                       int _numSlots) {

        this.id = _id;
        this.val = _val;
        this.operation = _operation;
        this.clickable = _clickable;
        this.digitIndex = _numSlots;
        this.numSlots = _numSlots;
        this.image = _image;


        if(!ASM_CONST.USE_NEW_MATH) {

            STextLayout.resetAllValues();

            if (id != ASM_CONST.OPERATOR_ROW && id != ASM_CONST.OPERAND_ROW)
                STextLayout.resetAllBackground();               // MATHFIX_BUILD what is this?
            STextLayout.update(id, val, operation, numSlots); // MATHFIX_BUILD what is this?

            // MATHFIX_BUILD what is this?
            // DotBag operations
            SdotBag.updateSize();
            SdotBag.setHollow(false);
            SdotBag.setDrawBorder(id != ASM_CONST.ANIMATOR1 && id != ASM_CONST.ANIMATOR2 && id != ASM_CONST.ANIMATOR3); // why
            SdotBag.setVisibility(INVISIBLE);
        }
    }

    private void createText() {

        STextLayout = new CAsm_TextLayout(getContext());

        // MATHFIX_LAYOUT LayoutParams
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, rightPadding, 0);
        STextLayout.setLayoutParams(lp);

        // MATHFIX_LAYOUT Where TextLayout gets added to Alley
        addView(STextLayout, 0);
        Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "addView CAsm_TextLayout to CAsm_Alley:" + id);
    }

    private void createDotBag() {
        // TODO: figure out why it won't show up unless updated

        SdotBag = new CAsm_DotBag(getContext());

        // MATHFIX_LAYOUT LayoutParams
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        SdotBag.setLayoutParams(lp);
        // MATHFIX_LAYOUT Where DotBag gets added to Alley
        addView(SdotBag, 1);
        Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "addView CAsm_DotBag to CAsm_Alley:" + id);

    }

    public void     nextDigit() {

        Integer cols = 0;

        digitIndex--;

        STextLayout.performNextDigit();


        if (id == ASM_CONST.RESULT_ROW)
            cols = 0;
        else {
            cols = STextLayout.getDigit(digitIndex);
            cols = (cols != null)?cols:0;
        }

        if(operation.equals("-")) {
            removeView(SdotBag);
            createDotBag();
        }
        SdotBag.setRows(1);
        SdotBag.setCols(cols);
        SdotBag.setImage(image);
        SdotBag.setIsClickable(clickable);
        SdotBag.resetOverflowNum();
        SdotBag.setVisibility(INVISIBLE);
    }

    public Integer getNum() {return STextLayout.getNum();}

    public CAsm_DotBag getDotBag() {
        return SdotBag;
    }

    public CAsm_TextLayout getTextLayout() {return STextLayout;}

    public Integer getCurrentDigit() {return STextLayout.getDigit(digitIndex);}


}
