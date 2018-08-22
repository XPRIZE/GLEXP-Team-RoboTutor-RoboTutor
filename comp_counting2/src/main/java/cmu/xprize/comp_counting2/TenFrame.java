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
    boolean line;
    boolean placevalue;

    TenFrame(int startX, int startY, int holeWidth, int holeHeight,boolean isline) {
        this.startX = startX;
        this.startY = startY;
        this.holeWidth = holeWidth;
        this.holeHeight = holeHeight;
        this.line = isline;
        this.placevalue = false;
    }

    TenFrame(int startX, int startY, int holeWidth, int holeHeight,boolean isline, boolean placevalue) {
        this.startX = startX;
        this.startY = startY;
        this.holeWidth = holeWidth;
        this.holeHeight = holeHeight;
        this.line = isline;
        this.placevalue = placevalue;
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
        if (placevalue){
            if (line){
                if (i < 0 || i > 10) {
                    return null;
                } else {
                    int x = startX+holeWidth/2;
                    int y = startY + holeHeight*(i-1)+holeHeight/2;
                    return new XY (x,y);

                }

            } else {
                if (i < 0 || i > 9) {
                    return null;
                } else if (i<4) {
                    //first col

                    int x = startX+holeWidth/2;
                    int y = startY+holeHeight*(i-1)+holeHeight/2;
                    return new XY(x, y);
                } else if (i<7) {
                    int x = startX+holeWidth*3/2;
                    int y = startY + holeHeight*(i-4)+holeHeight/2;
                    return new XY (x,y);

                } else{
                    int x = startX+holeWidth*5/2;
                    int y = startY + holeHeight*(i-7)+holeHeight/2;
                    return new XY (x,y);
                }

            }

        } else {
            if (i <= 0 || i > 10) {
                return null;
            } else if (i < 6 || line) {
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
        if (!line){
        // vertical lines
        for (int i = 0; i < 5 + 1; i++) {
            c.drawLine(startX + i * holeWidth, startY, startX + i*holeWidth, startY + 2*holeHeight, p);
        }

        for (int i = 0; i < 2 + 1; i++) {
            c.drawLine(startX, startY + i*holeHeight, startX + 5*holeWidth, startY + i*holeHeight, p);
        }}else{

            for (int i = 0; i < 10 + 1; i++) {
                c.drawLine(startX + i * holeWidth, startY, startX + i*holeWidth, startY + holeHeight, p);
            }

            for (int i = 0; i < 2; i++) {
                c.drawLine(startX, startY + i*holeHeight, startX + 10*holeWidth, startY + i*holeHeight, p);
        }}

    }
}
