package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.robotutor.BuildConfig;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;

/**
 * This View is purpose built to diplay the current app build version name from the Gradle app script
 *
 */
public class TVersionView extends TTextView {

    public TVersionView(Context context) {
        super(context);
        init(context, null);
    }

    public TVersionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TVersionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        setText("RoboTutor " + BuildConfig.VERSION_NAME);
    }
}
