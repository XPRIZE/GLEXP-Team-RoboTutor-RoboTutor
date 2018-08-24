package cmu.xprize.comp_spelling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.content.Intent;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

@SuppressLint("AppCompatCustomView")
public class CLetter_Tile extends TextView {
    private String _letter;
    private int _index;
    private CSpelling_Component _parent;

    public CLetter_Tile(Context context) {
        super(context);
        init(context, null);
    }

    public CLetter_Tile(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CLetter_Tile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CLetter_Tile(Context context, String letter, int letterIndex, CSpelling_Component parent) {
        super(context);
        _letter = letter;
        _index = letterIndex;
        _parent = parent;

        this.setText(" " + _letter + " ");
        this.setId(letterIndex);
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        this.setTextColor(Color.WHITE);
        this.setBackgroundColor(Color.rgb(240, 200, 65));
        this.setPadding(15,15,15,15);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 0, 0, 0);
        llp.width = 200;
        llp.height = 200;
        llp.gravity = Gravity.CENTER;
        this.setLayoutParams(llp);

//        letter.setBackgroundColor(getResources().getColor(R.color.abc_tint_switch_thumb));

//        LayoutParams layoutParams = this.getLayoutParams();
//        layoutParams.width = spToPx(100, context);
//        layoutParams.height = spToPx(100, context);
//
//        this.setLayoutParams(layoutParams);

//        android:layout_width="100sp"
//        android:layout_height="100sp"
//        android:layout_margin="20dp"
//        android:gravity="center"
//        android:background="@android:color/holo_orange_light"
    }

    private static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    protected void init(Context context, AttributeSet attrs) {
    }

    protected void indicateError() {
        this.setBackgroundColor(Color.rgb(240, 100, 100));
    }

    protected void revertColor() {
        this.setBackgroundColor(Color.rgb(240, 200, 65));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _parent.onLetterTouch(_letter, _index, this);
        }

        return true;
    }
}
