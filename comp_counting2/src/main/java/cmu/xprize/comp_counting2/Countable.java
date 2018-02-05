package cmu.xprize.comp_counting2;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import static cmu.xprize.comp_counting2.CCountX_Component.TAG;

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



    /** for testing animation **/

    protected int displacementX = 0;
    private int DISPLACEMENT_RATE = 40;
    private int MAX_DISPLACEMENT = 100;
    private int displacementDirection = 1;

    public void wiggle() {
        if (displacementX <= -1 * MAX_DISPLACEMENT) {
            if (displacementDirection < 0) {
                displacementDirection *= -1;
            }
        } else if (displacementX >= MAX_DISPLACEMENT) {
            if (displacementDirection > 0) {
                displacementDirection *= -1;
            }
        }

        displacementX += (displacementDirection * DISPLACEMENT_RATE);
    }


    public boolean moveTowards(int x, int y) {

        Log.d(TAG, "moving from " + this.x + ", " + this.y + " to " + x + ", " + y);

        boolean xDone = false, yDone = false;
        if(this.x == x) {
            xDone = true;
        } else {
            int xspeed = Math.min(Math.abs(x - this.x), Math.abs(DISPLACEMENT_RATE));
            this.x += (x - this.x > 0) ? 1 : -1 * xspeed;
        }

        if(this.y == y) {
            yDone = true;
        } else {
            int yspeed = Math.min(Math.abs(y - this.y), Math.abs(DISPLACEMENT_RATE));
            this.y += (y - this.y > 0) ? 1 : -1 * yspeed;
        }

        return xDone && yDone;
    }

}
