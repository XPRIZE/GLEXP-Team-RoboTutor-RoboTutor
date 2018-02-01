package cmu.xprize.comp_counting2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2/1/18.
 */

class TenFrame {

    int startX;
    int startY;
    int holeWidth;
    int holeHeight;

    TenFrame(int startX, int startY, int holeWidth, int holeHeight) {
        this.startX = startX;
        this.startY = startY;
        this.holeWidth = holeWidth;
        this.holeHeight = holeHeight;
    }

    /** holds the coordinates for a TenFrame **/
    class XY {

        int x;
        int y;

        public XY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * i is 1-indexed, so 1-> moja, 2-> mbili, etc
     * @param i
     * @return
     */
    XY getLocationOfIthObject(int i) {
        if (i <= 0 || i > 10) {
            return null;
        } else if (i < 6) {
            // first row
            int x = startX + holeWidth * (i - 1) + holeWidth / 2;
            int y = startY + holeHeight / 2;
            return new XY(x, y);
        } else {
            // second row
            int x = startX + holeWidth * (i - 6) + holeWidth / 2;
            int y = startY + holeHeight * 3/2;
            return new XY (x,y);

        }
    }

    /**
     * Draws the ten frame... it's basically ten boxes
     * @param c
     * @param p
     */
    void draw (Canvas c, Paint p) {
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5f);

        // vertical lines
        for (int i = 0; i < 5 + 1; i++) {
            c.drawLine(startX + i * holeWidth, startY, startX + i*holeWidth, startY + 2*holeHeight, p);
        }

        for (int i = 0; i < 2 + 1; i++) {
            c.drawLine(startX, startY + i*holeHeight, startX + 5*holeWidth, startY + i*holeHeight, p);
        }

        for (int i = 1; i < 11; i++) {
            XY xy = getLocationOfIthObject(i);
            c.drawCircle(xy.x, xy.y, 20, p);
            c.drawText("" + i, xy.x, xy.y, p);
        }

    }
}
