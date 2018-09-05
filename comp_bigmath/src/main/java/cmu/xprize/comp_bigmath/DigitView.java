package cmu.xprize.comp_bigmath;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * android-ConstraintLayoutExamples
 * <p>
 * Created by kevindeland on 8/7/18.
 */
@SuppressLint("AppCompatCustomView")
public class DigitView extends TextView {

    public boolean isCorrect = false;
    public boolean isIncorrect = false;

    public DigitView(Context context) {
        super(context);
    }

    public DigitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DigitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DigitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


}
