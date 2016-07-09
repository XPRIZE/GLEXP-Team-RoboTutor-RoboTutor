package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;

/**
 * Created by jacky on 2016/6/30.
 */

public class Background extends View implements ILoadableObject{
    private Bitmap image;
    private Context mContext;
    private int x, y, dx, dy;

    public Background(Bitmap res)
    {
        super(null);
        image = res;
    }

    public Background(Context context) {
        super(context);
        init(context, null);
    }

    public Background(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Background(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        mContext = context;
    }


    public void update()
    {
//        y += dy;
//        if(y < -GamePanel.HEIGHT){
//            y=0;
//        }
    }


    @Override
    public void onDraw(Canvas canvas) {
//        Rect mViewRegion = new Rect(0,0, GamePanel.WIDTH, GamePanel.HEIGHT);
////        getDrawingRect(mViewRegion);
//        Drawable d = ContextCompat.getDrawable(mContext, R.drawable.landscape_scratch3);
//        d.setBounds(mViewRegion);
//        d.draw(canvas);
//        super.onDraw(canvas);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect mViewRegion = new Rect(0,0, CAk_Component.WIDTH, CAk_Component.HEIGHT);
//        getDrawingRect(mViewRegion);
        Drawable d = ContextCompat.getDrawable(mContext, R.drawable.landscape_scratch3);
        d.setBounds(mViewRegion);
        d.draw(canvas);
    }


//    @Override
//    public void draw(Canvas canvas)
//    {
//        Rect mViewRegion = new Rect();
//        Drawable d = ContextCompat.getDrawable(mContext, R.drawable.landscape_scratch3);
//        d.setBounds(mViewRegion);
//        d.draw(canvas);
//        super.draw(canvas);
//        canvas.drawBitmap(image, x, y,null);
//        if(y<0)
//        {
//            canvas.drawBitmap(image, x, y+GamePanel.HEIGHT, null);
//        }
//    }

    public void setVector(int dy)
    {
        this.dy = dy;
    }


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

    }
}
