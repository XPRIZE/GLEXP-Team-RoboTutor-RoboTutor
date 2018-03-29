package cmu.xprize.comp_counting2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by kevindeland on 12/11/17.
 */

public class CountableImage extends Countable {

    private Bitmap bmp;

    public CountableImage(int x, int y, Bitmap bmp) {
        super();

        this.x = x;
        this.y = y;
        this.bmp = bmp;
    }



    @Override
    void draw(Canvas c, Paint p) {
        // draws at the center
        c.drawBitmap(bmp, this.x - bmp.getWidth() / 2 + displacementX, this.y - bmp.getHeight() / 2, p);
    }

}
