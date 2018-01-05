package cmu.xprize.comp_counting2;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by kevindeland on 12/11/17.
 */

public abstract class Countable {

    public int x;
    public int y;

    // turns true when circle is being dragged
    protected boolean isDragging;

    // turns true when circle is inside the box
    protected boolean isInsideBox;

    @Override
    public String toString() {
        return "X=" + this.x + "; Y=" + this.y + "; ";
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    public boolean isInsideBox() {
        return isInsideBox;
    }

    public void setInsideBox(boolean insideBox) {
        isInsideBox = insideBox;
    }


    abstract void draw(Canvas c, Paint p);

}
