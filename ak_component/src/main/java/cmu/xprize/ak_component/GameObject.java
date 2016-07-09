package cmu.xprize.ak_component;

import android.graphics.Rect;

/**
 * Created by jacky on 2016/7/1.
 */

public abstract class GameObject{
    protected int width;
    protected int height;
    protected int x;
    protected int y;
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Rect getRectangle() {
        return new Rect(x, y, x+width, y+height);
    }
}
