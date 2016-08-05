package cmu.xprize.asm_component;

/**
 * Created by dexte on 8/2/2016.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import cmu.xprize.fw_component.CFingerWriter;
import cmu.xprize.util.IEventListener;

public class CAsm_FingerWriter extends CFingerWriter {

    public CAsm_FingerWriter(Context context) {
        super(context);
        init();
    }

    public CAsm_FingerWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CAsm_FingerWriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setRecognizer("EN_STD_NUM");
        this.mListenerConfigured = true;
    }

    public void enable(boolean _enable, ArrayList<IEventListener> listeners) {

        if (listeners != null) {
            AddEventListeners(listeners);
        } else { //Disabling the fingerwriter
            mListeners.removeAll(mListeners);
        }
        this.enableFW(_enable);
    }

    private void AddEventListeners(ArrayList<IEventListener> listeners) {

        for (IEventListener listener: listeners) {
            this.mListeners.add(listener);
            Log.v("mListeners",Integer.toString(mListeners.size()));
        }
    }

}


