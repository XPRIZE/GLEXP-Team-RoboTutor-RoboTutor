package cmu.xprize.mn_component;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CIconContainer extends LinearLayout{

    private Paint borderPaint;
    private int _eyeStrokeColor = 0x000;
    private int _eyeStrokeWidth = 4;

    public CIconContainer(Context context) {
        super(context);
        init(context,null);

    }

    public CIconContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CIconContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        setWillNotDraw(false);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(_eyeStrokeColor);
        borderPaint.setStrokeWidth(_eyeStrokeWidth);
    }


    public void addIcon() {
    }


    @Override
    public void onDraw(Canvas canvas) {

        Rect viewRegion = new Rect();
        getDrawingRect(viewRegion);

        canvas.drawRoundRect(new RectF(viewRegion), 30, 30, borderPaint);
    }



}
