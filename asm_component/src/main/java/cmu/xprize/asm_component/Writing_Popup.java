package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Created by dexte on 7/27/2016.
 */
public class Writing_Popup extends PopupWindow {
    private Context mContext;

    public Writing_Popup(Context context) {
        super(context);
        init(context, null);
    }

    public Writing_Popup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Writing_Popup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        //setClipChildren(false);
        //setClipToPadding(false);
        LinearLayout testy = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        testy.setLayoutParams(lp);
        CAsm_Fingerwriter fw = new CAsm_Fingerwriter(context);
        testy.addView(fw);
        this.setContentView(testy);
    }
}
