package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created by jacky on 2016/7/1.
 */

public class QuestionBoard extends View {
    protected int leftNum, rightNum;
    protected float textSize;

    protected int width;
    protected int height;
    protected int x;
    protected int y;


    public QuestionBoard(Context context) {
        super(context);
        init();
    }

    public QuestionBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        width = 90;
        height = 30;
        textSize = 20;
        Random r = new Random();

        leftNum = r.nextInt(50);
        rightNum = leftNum + r.nextInt(50) + 1;


//        x = 430;
//        y = 100;
    }

    public void setQuestion(int leftNum, int rightNum) {
        this.leftNum = leftNum;
        this.rightNum = rightNum;
    }

    public void update() {
//        y += dy;
//        x += dx;
//        textSize += Math.abs(dy / 4.0f);
//        width += Math.abs(dx * 3);
//        height += Math.abs(dy / 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        int[] l = new int[2];
        getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int width = getWidth();
        int height = getHeight();

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

//        canvas.drawLine(0,0, 1000, 1000, paint);

//        Rect rect = new Rect(0,0, width, height);
//        canvas.drawRect(rect, paint);
        canvas.drawRect(new RectF(0, 0, width/3.0f, height), paint);
        canvas.drawRect(new RectF(width / 2.0f, 0, 5.0f * width / 6, height), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        canvas.drawText(String.valueOf(leftNum), width/6, height/2 + textSize / 4, paint);
        canvas.drawText(String.valueOf(rightNum), 4 * width/6, height/2 + textSize / 4, paint);
    }


    public Rect getRect() {
        int[] l = new int[2];
        getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = getWidth();
        int h = getHeight();

        return new Rect(x, y, x + w, y + h);
    }
}
