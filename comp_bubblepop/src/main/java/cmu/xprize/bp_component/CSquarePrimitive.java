package cmu.xprize.bp_component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CSquarePrimitive implements IPrimitive {

    private Rect    rect;
    private float   rotate;
    private int     color;
    private int     alpha = 255;

    public CSquarePrimitive(Rect newRect) {
        rect = newRect;
    }

    public void draw(Canvas canvas, Paint paint) {

        if(rotate != 0) {
            canvas.save();
            canvas.rotate(rotate);
        }

        paint.setAlpha(alpha);
        paint.setColor(color);

        canvas.drawRect(rect, paint);

        if(rotate != 0)
            canvas.restore();
    }

    @Override
    public void setColor(int itemColor) {
        color = itemColor;
    }

}
