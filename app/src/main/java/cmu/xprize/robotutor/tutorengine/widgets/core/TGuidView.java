package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.robotutor.RoboTutor;

public class TGuidView extends TTextView implements IGuidView {
    public TGuidView(Context context) {
        super(context);
        init(context, null);
    }

    public TGuidView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TGuidView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        // TODO: This is a temporary log update mechanism - see below
        //
        RoboTutor.setGUIDCallBack(this);
        updateText();
    }

    public void updateText() {

        setText(RoboTutor.LOG_ID);
    }

}
