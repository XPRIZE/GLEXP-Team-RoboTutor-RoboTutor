package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;


/**
 * Created by mayankagrawal on 6/29/16.
 */
public class CAsm_Alley extends LinearLayout {

    private EditText SText;
    private DotBag   SdotBag;

    float scale = getResources().getDisplayMetrics().density;
    final int textSize = (int)(ASM_CONST.textSize*scale);
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

        SText = new EditText(context);
        SdotBag = new DotBag(context);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setParams(int val, String image, int id, String operation, boolean clickable) {

        createEditText(val, id, operation);
        createDotBag(1, val, image, clickable); // TODO: fix hard coded 1
    }

    public void update(int val, String image, int id, String operation, boolean clickable) {

        SdotBag.update(1, val, image, clickable); // TODO: fix hard coded 1
        updateEditText(val, id, operation);

    }

    private void createEditText(int val, int id, String operation) {

        SText = new EditText(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                textSize*2, LayoutParams.MATCH_PARENT);
        lp.setMarginEnd(textSize/2);
        SText.setLayoutParams(lp);

        SText.setBackground(null);
        SText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        SText.setTextSize(textSize/2);
        SText.setTextColor(Color.BLACK);
        SText.setPadding(0, 0, rightPadding, 0);

        updateEditText(val, id, operation);

        addView(SText);
    }

    private void createDotBag(int rows, int cols, String imageName, boolean clickable) {

        SdotBag = new DotBag(getContext());
        SdotBag.setParams(textSize, rows, cols, clickable, imageName);

        addView(SdotBag);

    }


    private void updateEditText (int val, int id, String operation) {

        if (id == ASM_CONST.REGULAR) {
            SText.setText(Integer.toString(val));
            SText.setPaintFlags(0);
            SText.setEnabled(false);
            SText.setBackground(null);
        }
        else if (id == ASM_CONST.OPERATION) {
            SText.setText(operation + " " + Integer.toString(val));
            SText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            SText.setEnabled(false);
            SText.setBackground(null);
        }
        else { // id == ASM_CONST.RESULT
            SText.setText("");
            SText.setPaintFlags(0);
            SText.setEnabled(true);
            SText.setBackground(getResources().getDrawable(R.drawable.back));
        }

    }

    public Integer getNum() {

        try {
            return Integer.parseInt(SText.getText().toString());
        }
        catch (NumberFormatException e) {
            return null;
        }

    }

    public DotBag getDotBag() {
        return SdotBag;
    }

    public EditText getEditText() {
        return SText;
    }
}
