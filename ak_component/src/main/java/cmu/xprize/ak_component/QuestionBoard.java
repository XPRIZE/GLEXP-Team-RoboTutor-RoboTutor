package cmu.xprize.ak_component;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by jacky on 2016/7/1.
 */

public class QuestionBoard extends GameObject {
    protected int leftNum, rightNum;
    protected float textSize;

    public QuestionBoard(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        textSize = 6;
        leftNum = 13;
        rightNum = 20;
        dx = -GamePanel.MOVESPEED / 3;
        dy = GamePanel.MOVESPEED / 2;
    }

    public void setQuestion(int leftNum, int rightNum) {
        this.leftNum = leftNum;
        this.rightNum = rightNum;
    }

    public void update() {
        y += dy;
        x += dx;
        textSize += Math.abs(dy / 4.0f);
        width += Math.abs(dx * 3);
        height += Math.abs(dy / 2);
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        Rect rect = new Rect(x, y, x + width, y + height);
//        canvas.drawRect(rect, paint);
        canvas.drawRect(new RectF(x, y, x + width/3.0f, y + height), paint);
        canvas.drawRect(new RectF(x + width / 2.0f, y, x + 5.0f * width / 6, y + height), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        canvas.drawText(String.valueOf(leftNum), x + width/6, y + height/2 + textSize / 4, paint);
        canvas.drawText(String.valueOf(rightNum), x + 4 * width/6, y + height/2 + textSize / 4, paint);

    }
}
