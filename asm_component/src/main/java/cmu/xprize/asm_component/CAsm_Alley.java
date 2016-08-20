package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * Horizontal Alley that has the text on the left and its associated dotbag on the right
 */
public class CAsm_Alley extends LinearLayout {

    private CAsm_TextLayout STextLayout;
    private CAsm_DotBag SdotBag;

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

        STextLayout.resetAllValues();
        if(id != ASM_CONST.OPERATION && id != ASM_CONST.REGULAR)
            STextLayout.resetAllBackground();
        STextLayout.update(id, val, operation, numSlots);

        SdotBag.setDrawBorder(id != ASM_CONST.ANIMATOR1 && id != ASM_CONST.ANIMATOR2 && id != ASM_CONST.ANIMATOR3);

    }

    private void createText() {

        STextLayout = new CAsm_TextLayout(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, rightPadding, 0);
        STextLayout.setLayoutParams(lp);
        addView(STextLayout, 0);
    }

    private void createDotBag() {
        // TODO: figure out why it won't show up unless updated

        SdotBag = new CAsm_DotBag(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        SdotBag.setLayoutParams(lp);
        addView(SdotBag, 1);

    }

    public void nextDigit() {

        Integer cols;

        digitIndex--;

        STextLayout.performNextDigit();

        if (id == ASM_CONST.RESULT) {
            cols = 0;
        }
        else {
            cols = STextLayout.getDigit(digitIndex);
            cols = (cols != null)?cols:0;
        }

        SdotBag.setRows(1);
        SdotBag.setCols(cols);
        SdotBag.setImage(image);
        SdotBag.setIsClickable(clickable);
        SdotBag.resetOverflowNum();

    }

    public Integer getNum() {return STextLayout.getNum();}

    public CAsm_DotBag getDotBag() {
        return SdotBag;
    }

    public CAsm_TextLayout getTextLayout() {return STextLayout;}

    public Integer getCurrentDigit() {return STextLayout.getDigit(digitIndex);}


}
