package cmu.xprize.comp_numberscale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import java.util.Random;
import java.util.Vector;


/**
 * Created by yuanx on 2/26/2018.
 */

public class CNumberScale_player extends SurfaceView implements SurfaceHolder.Callback{
    public Context _context;
    private CNumberScale_Component _component;
    private int[] _numberbar;
    private int maxHit;
    private float edgelength;
    private int min;
    private int max;
    private int barIndex;
    private Paint strokePaint;
    private boolean tappable;

    private static final String TAG = "NumberScaleSurfaceView";



    // painting tools
    private SurfaceHolder _holder;

    public CNumberScale_player(Context context) {
        super(context);

        init(context);
    }

    public CNumberScale_player(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CNumberScale_player(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public CNumberScale_player(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }



    protected void init(Context context) {
        _context = context;


        strokePaint = new Paint();
        strokePaint.setColor(NSCONST.COLOR_BLUE);
        strokePaint.setStrokeWidth(10);

        getHolder().addCallback(this);


    }

    public void loadData(int minN,int maxN,int deltaN,int maxHitN) {

        //After loading the dataset, set the min,max, maxHit...
        maxHit = maxHitN;
        min = minN;
        max = maxN;

        setNumberBar();
        redraw();

    }

    protected void setNumberBar(){
        _numberbar = new int[maxHit/2+1];
        int delta = _component.get_delta();
        for(int i=0;i<=maxHit/2;i++){
            _numberbar[i]=min+i*delta;
        }
    }

    public void setComponent(CNumberScale_Component component) {
        _component = component;
    }

    public void enableTapping(boolean tappable) {
        this.tappable = tappable;
        redraw();
    }



    private void redraw() {
        Canvas canvas = _holder.lockCanvas();
        redraw(canvas);
    }

    /**
     * redraw
     */
    private void redraw(Canvas canvas) {
        drawContainingRectangle(canvas);

        //When the dataset is not loaded, draw nothing.
        if (maxHit!=0){
            int margin = NSCONST.BAR_MARGIN;
            int bartop = NSCONST.TOP_MARGIN;
            float left = margin;
            float right = canvas.getWidth() - margin;
            edgelength = (right-left)/(maxHit/2+1);


            if (_holder != null) {
                for (int i=0;i<=barIndex;i++) {
                    drawEachBox(canvas, i);
                }
            }

        }


        // after all drawing, post changes
        _holder.unlockCanvasAndPost(canvas);

    }

    private void drawContainingRectangle(Canvas canvas) {

        float margin=0;
        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin;
        float down = canvas.getHeight() - margin;

        Paint white = new Paint();
        white.setColor(NSCONST.COLOR_WHITE);

        canvas.drawRect(left, up, right, down, white);
    }

    private void drawEachBox(Canvas canvas,int i){

        int margin = NSCONST.BAR_MARGIN;
        float top = (float)NSCONST.TOP_MARGIN;

        float left = margin+i*edgelength;
        float right = margin+(i+1)*edgelength;
        float bottom = top + edgelength;

        Paint text = new Paint();
        text.setColor(NSCONST.COLOR_BLUE);
        text.setTextSize(NSCONST.TEXT_SIZE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
        String num = Integer.toString(_numberbar[i]);


        Paint white = new Paint();
        white.setColor(NSCONST.COLOR_WHITE);



        float top1=top+NSCONST.BOX_MARGIN;
        float left1=left+NSCONST.BOX_MARGIN;
        float right1=right-NSCONST.BOX_MARGIN;
        float bottom1=bottom-NSCONST.BOX_MARGIN;

        //not drawing the box for the first number
        if (i!=0){
            canvas.drawRect(left, top, right, bottom, strokePaint);
            canvas.drawRect(left1,top1,right1,bottom1,white);
        }
        canvas.drawText(num,(left+right)/2,(top+bottom)/2+NSCONST.TEXT_SIZE/3,text);
    }

    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if(!tappable) {
                return true;
            }
            float x = event.getX();
            float y = event.getY();
            System.out.println(x);
            System.out.println(y);
            System.out.println("tap");

            if (x>=550 && x<=850 && y>=470 && y<=760){
                //click on the minus button
                if (barIndex>0){
                    barIndex-=1;
                    _component.minus_delta();
                    tappable = false;
                    _component.playChime();
                }
                System.out.println("minus");
            } else if (x>=1700 && x<=1965 && y>=470 && y<=750){
                if (barIndex<maxHit/2){
                    barIndex+=1;
                    _component.add_delta();
                    tappable = false;
                    _component.playChime();
                }
                //click on the add button
                System.out.println("add");
            }

            Canvas canvas = _holder.lockCanvas();

            redraw(canvas);



            return true;

        }

        return false;
    }

    // SurfaceHolder Callback methods
    /**
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {


        _holder = holder;

        Canvas canvas = _holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto mull canvas");
        } else {
            initializeBackground(canvas, getWidth(), getHeight());
        }
    }

    private void initializeBackground(Canvas canvas, int width, int height) {
        redraw(canvas);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
