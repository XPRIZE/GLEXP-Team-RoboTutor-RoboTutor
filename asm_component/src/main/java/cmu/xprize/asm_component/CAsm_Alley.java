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

    private EditText num;
    private DotBag   dotBag;

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

        num = new EditText(context);
        dotBag = new DotBag(context);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setParams(int val, String image, int id, String operation, boolean clickable) {

        createEditText(val, id, operation);
        createDotBag(1, val, image, clickable); // TODO: fix hard coded 1
    }

    private void createEditText(int val, int id, String operation) {

        num = new EditText(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                textSize*2, LayoutParams.MATCH_PARENT);
        lp.setMarginEnd(textSize/2);
        num.setLayoutParams(lp);

        num.setBackground(null);
        num.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        num.setTextSize(textSize/2);
        num.setTextColor(Color.BLACK);
        num.setPadding(0, 0, rightPadding, 0);

        updateEditText(val, id, operation);

        addView(num);
    }

    private void createDotBag(int rows, int cols, String imageName, boolean clickable) {

        dotBag = new DotBag(getContext());
        dotBag.setParams(textSize, rows, cols, clickable, imageName);

        addView(dotBag);

    }

    public void update(int val, String image, int id, String operation, boolean clickable) {

        dotBag.update(1, val, image, clickable); // TODO: fix hard coded 1
        updateEditText(val, id, operation);

    }

    private void updateEditText (int val, int id, String operation) {

        if (id == ASM_CONST.REGULAR) {
            num.setText(Integer.toString(val));
            num.setPaintFlags(0);
            num.setEnabled(false);
            num.setBackground(null);
        }
        else if (id == ASM_CONST.OPERATION) {
            num.setText(operation + " " + Integer.toString(val));
            num.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            num.setEnabled(false);
            num.setBackground(null);
        }
        else { // id == ASM_CONST.RESULT
            num.setText("");
            num.setPaintFlags(0);
            num.setEnabled(true);
            num.setBackground(getResources().getDrawable(R.drawable.back));
        }

    }

    public Integer getNum() {

        try {
            return Integer.parseInt(num.getText().toString());
        }
        catch (NumberFormatException e) {
            return null;
        }


    }

    public DotBag getDotBag() {
        return dotBag;
    }

    public EditText getEditText() {
        return num;
    }
}
