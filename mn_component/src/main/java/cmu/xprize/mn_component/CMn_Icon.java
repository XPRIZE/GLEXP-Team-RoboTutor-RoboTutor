package cmu.xprize.mn_component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import cmu.xprize.util.TCONST;

public class CMn_Icon  {

    private int _iconStrokeColor = 0xFF000000;
    private int _iconFillColor   = 0xFF000000;
    private int _iconStrokeWidth = 2;

    private RectF   mBounds;
    private String  mIconType = TCONST.OVALICON;
    private Paint   mIconPaint;
    private float   mRadius = 10;


    static final private String TAG ="CMn_Icon";


    public CMn_Icon() {

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setStyle(Paint.Style.STROKE);
        mIconPaint.setColor(_iconStrokeColor);
        mIconPaint.setStrokeWidth(_iconStrokeWidth);
    }


    public void onDraw(Canvas canvas) {

        switch(mIconType) {
            case TCONST.OVALICON:
                canvas.drawOval(mBounds, mIconPaint);
                break;

            case TCONST.RECTICON:
                canvas.drawRoundRect(mBounds, mRadius, mRadius, mIconPaint);
                break;
        }
    }


    public void updateIconBounds(float x, float y, float width, float height) {

        mBounds.left   = x;
        mBounds.right  = x + width;
        mBounds.top    = y;
        mBounds.bottom = y + height;
    }

}
