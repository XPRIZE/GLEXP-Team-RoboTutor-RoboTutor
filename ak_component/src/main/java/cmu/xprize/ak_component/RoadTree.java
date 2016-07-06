package cmu.xprize.ak_component;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by jacky on 2016/7/1.
 */

public class RoadTree extends GameObject {
    private Bitmap image;

    public RoadTree(Bitmap res, int x, int y, int dx, int dy, int w, int h){
        image = res;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        width = w;
        height = h;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);
    }

    public void update() {
        x += dx;
        y += dy;
        width += Math.abs(dx);
        height += Math.abs(dy);
        //image = Bitmap.createScaledBitmap(image, width, height, false);
    }

}
