package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

import cmu.xprize.util.IEventListener;

/**
 *
 */

public class CAsm_Popup extends PopupWindow {

    private Context mContext;
    private CAsm_FingerWriter fw;
    public boolean isActive;

    public CAsm_Popup(Context context) {
        super(context);
        init(context, null);
    }

    public CAsm_Popup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Popup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setBackgroundColor(Color.WHITE);
        this.setContentView(layout);
        layout.setLayoutParams(lp);

        fw = new CAsm_FingerWriter(context);
        layout.addView(fw);

    }

    public void enable(boolean _enable,ArrayList<IEventListener> listeners) {
        fw.enable(_enable,listeners);
    }
}
