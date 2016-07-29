package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

import cmu.xprize.util.IEventListener;

/**
 * Created by dexte on 7/27/2016.
 */
public class Writing_Popup extends PopupWindow {
    private Context mContext;
    private CAsm_Fingerwriter fw;

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
        fw = new CAsm_Fingerwriter(context);
        //fw.setId(R.id.SDigitWriter);
        testy.addView(fw);
        testy.setBackgroundColor(Color.WHITE);
        this.setContentView(testy);
    }

    public void enable(boolean b,ArrayList<IEventListener> listeners) {
        fw.enable(b,listeners);
    }
}
