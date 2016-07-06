package cmu.xprize.ak_component;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by jacky on 2016/7/1.
 */

public class Player extends GameObject{
    protected enum Lane{LEFT, MID, RIGHT};

    private Bitmap image;
    protected boolean isPlaying;
    protected int score;
    protected int rearNum;
    private Lane lane;

    public Player(Bitmap res, int w, int h) {
        image = res;
        width = w;
        height = h;
        rearNum = 14;
        lane = Lane.MID;
        x = 487 - width / 2;
        y = 450;
    }

    public void update() {
        switch (lane) {
            case LEFT:
                x = 330 - width / 2;
                break;
            case MID:
                x = 487 - width / 2;
                break;
            case RIGHT:
                x = 667 - width / 2;
                break;
        }
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(image, x, y, null);
        canvas.drawText(String.valueOf(rearNum), x + width/2.0f, y + 1.1f * height, paint);
    }

    public void onTouchEvent(MotionEvent event){
        int touchX = (int)event.getX();
        //Change to left lane
        if(touchX < x + width / 2) {
            lane = lane == Lane.RIGHT? Lane.MID : Lane.LEFT;
            System.out.println("To left");
        }
//        //change to right lane
        else {
            lane = lane == Lane.LEFT? Lane.MID : Lane.RIGHT;
            System.out.println("To right");
        }
    }

    public Lane getLane() {
        return lane;
    }


}
