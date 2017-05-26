package cmu.xprize.asm_component;

/**
 *
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import cmu.xprize.comp_writing.CGlyphController;
import cmu.xprize.fw_component.CFingerWriter;
import cmu.xprize.util.IEventListener;

public class CAsm_FingerWriter extends CGlyphController {

    public CAsm_FingerWriter(Context context) {
        super(context);
        init(context);
    }

    public CAsm_FingerWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CAsm_FingerWriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public void enable(boolean _enable, ArrayList<IEventListener> listeners) {

//        if (listeners != null) {
//            AddEventListeners(listeners);
//        } else { //Disabling the fingerwriter
//            mListeners.removeAll(mListeners);
//        }

        inhibitInput(!_enable);
    }

}

