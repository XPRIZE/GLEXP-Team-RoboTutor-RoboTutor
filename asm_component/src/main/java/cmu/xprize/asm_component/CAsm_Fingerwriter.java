package cmu.xprize.asm_component;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.fw_component.CFingerWriter;

/**
 * Created by dexte on 7/27/2016.
 */

public class CAsm_Fingerwriter extends CFingerWriter {
    //TODO: Come up with better names.

    public CAsm_Fingerwriter(Context context) {
        super(context);
        init();
    }

    public CAsm_Fingerwriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CAsm_Fingerwriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setRecognizer("EN_STD_NUM");
        this.mListenerConfigured = true;
        //TODO: Add Listeners so that they can find when the text is popping up.
    }

    public void enable(boolean b) {
        Boolean state = new Boolean(b);
        this.enableFW(b);
    }

}

