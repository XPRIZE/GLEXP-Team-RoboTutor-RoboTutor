package cmu.xprize.ak_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created by jacky on 2016/7/1.
 */

public class CAkQuestionBoard extends View {
    protected int leftNum, rightNum;
    protected float textSize;

    public CAkQuestionBoard(Context context) {
        super(context);
        init();
    }

    public CAkQuestionBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CAkQuestionBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        Random r = new Random();
        leftNum = r.nextInt(50);
        rightNum = leftNum + r.nextInt(50) + 1;
        textSize = 20;
    }

    public void setQuestion(int leftNum, int rightNum) {
        this.leftNum = leftNum;
        this.rightNum = rightNum;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        int width = getWidth();
        int height = getHeight();

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(new RectF(0, 0, width * 5.0f / 12, height), paint);
        canvas.drawRect(new RectF(width * 7.0f / 12.0f, 0, width, height), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        canvas.drawText(String.valueOf(leftNum), width * 5.0f / 24.0f, height / 2.0f + textSize / 4, paint);
        canvas.drawText(String.valueOf(rightNum),width * 19.0f / 24.0f, height / 2.0f + textSize / 4, paint);
    }

}
