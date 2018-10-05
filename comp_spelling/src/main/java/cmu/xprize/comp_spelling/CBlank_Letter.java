package cmu.xprize.comp_spelling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

@SuppressLint("AppCompatCustomView")
public class CBlank_Letter extends ImageView {

    public CBlank_Letter(Context context) {
        super(context);
        init(context, null);

        this.setBackgroundResource(R.drawable.underline);
        this.setPadding(
            SP_CONST.LETTER_TILE_PADDING,
            SP_CONST.LETTER_TILE_PADDING,
            SP_CONST.LETTER_TILE_PADDING,
            SP_CONST.LETTER_TILE_PADDING
        );

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 0, 0, 0);
        llp.width = SP_CONST.LETTER_TILE_WIDTH;
        llp.height = SP_CONST.LETTER_TILE_HEIGHT;
        this.setLayoutParams(llp);
    }

    public CBlank_Letter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CBlank_Letter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) { }
}
