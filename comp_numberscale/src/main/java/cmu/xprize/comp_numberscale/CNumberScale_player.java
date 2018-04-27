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
    private int maxIndex = 0;
    private int barIndex;
    private Paint strokePaint;
    private boolean tappable = false;
    private boolean greyOutMinus = false;
    private boolean greyOutPlus = false;
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
        _numberbar = new int[maxHit];
        int delta = _component.get_delta();
        for(int i=min;i<=max;i=i+delta){
            _numberbar[maxIndex]=i;
            maxIndex++;
        }
    }

    public void setComponent(CNumberScale_Component component) {
        _component = component;
    }

    public void enableTapping(boolean tappable) {
        this.tappable = tappable;
    }



    private void redraw() {
        Canvas canvas = _holder.lockCanvas();
        redraw(canvas);
    }

    /**
     *
     *
     * redraw
     */
    private void redraw(Canvas canvas) {
        if(_holder!=null){
            _component.inmode = true;

            drawContainingRectangle(canvas);
            //When the dataset is not loaded, draw nothing.
            if (maxIndex!=0){

                int margin = canvas.getWidth()/9;
                int bartop = canvas.getHeight()*5/7;
                float left = margin;
                float right = canvas.getWidth() - margin;
                edgelength = (right-left)/maxIndex;


                if (_holder != null) {
                    for (int i=0;i<maxIndex;i++) {
                        drawEachBox(canvas, i);
                    }
                }

            }


            // after all drawing, post changes


        } else {
            _component.inmode =false;
        }
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

        int margin = canvas.getWidth()/9;
        float top = canvas.getHeight()*5/7;

        float left = margin+i*edgelength;
        float right = margin+(i+1)*edgelength;
        float bottom = top + edgelength;

        Paint text = new Paint();
        text.setColor(NSCONST.COLOR_BLUE);
        text.setTextSize(NSCONST.TEXT_SIZE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
        String num = Integer.toString(_numberbar[i]);

        Paint l = new Paint();
        l.setColor(NSCONST.COLOR_BLUE);
        l.setStrokeWidth(10);


        Paint white = new Paint();
        white.setColor(NSCONST.COLOR_WHITE);



        float top1=top+NSCONST.BOX_MARGIN;
        float left1=left+NSCONST.BOX_MARGIN;
        float right1=right-NSCONST.BOX_MARGIN;
        float bottom1=bottom-NSCONST.BOX_MARGIN;

        //not drawing the box for the first number
        if (i!=0){
            canvas.drawLine(left+edgelength/5,bottom-edgelength/4,right-edgelength/5,bottom-edgelength/4,l);
        }

        if(i!=maxIndex-1){
            canvas.drawText(",",right,bottom-edgelength/5,text);}


        if (i<=barIndex){
            canvas.drawText(num,(left+right)/2,(top+bottom)/2+NSCONST.TEXT_SIZE/3,text);
        }

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
            _component.kill = true;
            int[] minusSpec = _component.minusSpecs;
            int[] addSpec = _component.addSpecs;
            if (x>=minusSpec[0] && x<=minusSpec[1] && y>=minusSpec[2] && y<=minusSpec[3] && !greyOutMinus){
                //click on the minus button
                if (barIndex>0){
                    barIndex-=1;
                    _component.minus_delta();
                    tappable = false;
                    _component.playChime();
                    _component.setNewTimer();
                }

                if (barIndex == 0){
                    greyOutMinus = true;
                    _component.greyOutMinus();
                } else {
                    greyOutMinus=false;
                    _component.ungreyMinus();
                }

                if (barIndex == maxIndex-1) {
                    greyOutPlus = true;
                    _component.greyOutAdd();
                } else {
                    greyOutPlus = false;
                    _component.ungreyAdd();
                }

                System.out.println("minus");
            } else if (x>=addSpec[0] && x<=addSpec[1] && y>=addSpec[2] && y<=addSpec[3]){
                if (barIndex<maxIndex-1){
                    barIndex+=1;
                    _component.add_delta();
                    tappable = false;
                    _component.playChime();
                    _component.setNewTimer();
                }

                if (barIndex == 0){
                    greyOutMinus = true;
                    _component.greyOutMinus();
                } else {
                    greyOutMinus=false;
                    _component.ungreyMinus();
                }

                if (barIndex == maxIndex-1) {
                    greyOutPlus = true;
                    _component.greyOutAdd();
                } else {
                    greyOutPlus = false;
                    _component.ungreyAdd();
                }

                //click on the add button
                System.out.println("add");
            }


            redraw();



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
