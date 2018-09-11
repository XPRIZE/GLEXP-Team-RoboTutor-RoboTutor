package cmu.xprize.asm_component;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;


/**
 *
 */
public class CAsm_DotBag_New extends TableLayout {

    // for drawing dotbag boundary
    final float _SCALE = getResources().getDisplayMetrics().density;
    Paint _borderPaint;
    int _borderWidth;
    RectF _bounds;
    private int _size;

    public CAsm_DotBag_New(Context context) {
        super(context);
        init();
    }

    public CAsm_DotBag_New(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //
    private void init() {
        setWillNotDraw(false); // allow onDraw method to be caled
        setClipChildren(false); // allow dots to animate outside bag
        setClipToPadding(false); // allow dots to be drawn in padding


        _size = (int) getResources().getDimension(R.dimen.dot_size); // MATHFIX_LAYOUT todo... how to get this right???
        _borderWidth = (int)(ASM_CONST.borderWidth * _SCALE);
        _bounds = new RectF();
        _bounds.set(_borderWidth, _borderWidth, _size * 2 - _borderWidth, _size - _borderWidth);

        _borderPaint = new Paint(); // MATHFIX_LAYOUT... is there any way to make this a @drawable? that changes size? for later...
        _borderPaint.setStrokeWidth(_borderWidth);
        _borderPaint.setStyle(Paint.Style.STROKE);
        _borderPaint.setColor(Color.BLACK);
        _borderPaint.setStrokeCap(Paint.Cap.ROUND);
        _borderPaint.setAntiAlias(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {

        Log.d("DRAW_DOTBAG", "drawing dotbag");
        canvas.drawRoundRect(_bounds, _size / 2, _size / 2, _borderPaint);

        Log.d("DRAW_DOTBAG", "draw a rectangle");
        Log.d("DRAW_DOTBAG", "resetting bounds");
    }


    /**
     * adjusts the dotbag to contain more dots
     * @param numberDots
     */
    public void setNumberDots(int numberDots) {
        // ayy lmao
    }
}
