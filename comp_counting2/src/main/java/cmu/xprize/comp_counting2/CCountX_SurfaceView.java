package cmu.xprize.comp_counting2;

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

import java.util.Random;
import java.util.Vector;

/**
 * Created by kevindeland on 12/12/17.
 */

public class CCountX_SurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public Context _context;
    private CCountX_Component _component;

    // where animation happens
    //private AnimationThread thread;

    // painting tools
    private SurfaceHolder _holder;
    private Paint _paint;

    // Collection of Countable objects e.g. bananas or dots or fruits
    private Vector<Countable> _countables;
    private boolean tappable;

    private TenFrame tenFrame;
    private boolean showTenFrame = false;

    private int[] FRUITS = {
            R.drawable.banana,
            R.drawable.pear,
            R.drawable.pineapple,
            R.drawable.tomato
    };

    // holds the image for the fruit Countable being displayed
    private Bitmap _fruitMap;

    private static final String TAG = "CCountXSurfaceView";

    public CCountX_SurfaceView(Context context) {
        super(context);

        init(context);
    }

    public CCountX_SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CCountX_SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public CCountX_SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    /**
     * Initialization called by all the Constructors
     *
     * @param context
     */
    private void init(Context context) {

        _context = context;


        _paint = new Paint();
        _paint.setColor(Color.BLACK);

        _countables = new Vector<>();
        resetCounter();


        getHolder().addCallback(this);
    }

    private void initTenFrame() {
        int holeWidth = 200;
        int holeHeight = 200;


        int x = (getWidth() / 2) - (holeWidth * 5) / 2;
        int y = (getHeight() / 2) - holeHeight;
        tenFrame = new TenFrame(x, y, holeWidth, holeHeight);
    }

    public void setComponent(CCountX_Component component) {
        _component = component;
    }

    private void resetCounter() {
        _fruitMap = generateRandomFruit();
    }


    /**
     * Pick a random fruit to use as object.
     *
     * @return
     */
    private Bitmap generateRandomFruit() {
        // initialize images
        int drawable = FRUITS[(new Random()).nextInt(FRUITS.length)];
        Bitmap immutableBmp = BitmapFactory.decodeResource(getResources(), drawable);
        Bitmap resizedBmp = Bitmap.createScaledBitmap(immutableBmp, COUNTX_CONST.DRAWABLE_RADIUS * 2,
                COUNTX_CONST.DRAWABLE_RADIUS * 2, false);

        return resizedBmp;
    }

    public boolean doneMovingToTenFrame = false;


    public void wiggleFruit() {

        doneMovingToTenFrame = true;
        boolean[] fruitInTenFrame = new boolean[_countables.size()];
        for (int i=0; i < _countables.size(); i++) {
            Countable c = _countables.get(i);

            TenFrame.XY xy = tenFrame.getLocationOfIthObject(i+1);
            fruitInTenFrame[i] = c.moveTowardsAtVelocity(xy.x, xy.y, 40);
            if(!fruitInTenFrame[i]) {
                doneMovingToTenFrame = false;
            }
        }

        redraw();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            if(!tappable) {
                return true;
            }

            Log.v(TAG, "Touched surface! X=" + event.getRawX() + ", Y=" + event.getRawY());
            float x = event.getX();
            float y = event.getY();

            Log.v(TAG, "Holder in onTouch: " + _holder);

            // first lock canvas
            Canvas canvas = _holder.lockCanvas();

            // don't draw if outside the box
            int margin = COUNTX_CONST.BOX_MARGIN;
            if(x < margin || x > canvas.getWidth() - margin || y < margin || y > canvas.getHeight() - margin) {
                redraw(canvas);
                return true;
            }

            Countable countable;
            countable = new CountableImage((int) x, (int) y, _fruitMap);
            _countables.add(countable);


            redraw(canvas);

            // make sure to update the TextView
            _component.updateCount(_countables.size());
            // playChime plays the chime, AND the audio...
            _component.playChime();


        }

        return false;
    }

    /**
     * For when you're too lazy to lock the canvas yourself.
     */
    private void redraw() {
        Canvas canvas = _holder.lockCanvas();
        if (canvas != null) {
            redraw(canvas);
        }
    }

    /**
     * redraws everything contained within the Canvas
     */
    private void redraw(Canvas canvas) {

        if (_holder != null) {
            drawContainingRectangle(canvas);

            // draw each Countable object
            for (Countable c : _countables) {
                c.draw(canvas, _paint);
            }

            if (showTenFrame) {
                tenFrame.draw(canvas, _paint);

                /*
                for (int i = 0; i < _countables.size(); i++) {
                    Countable c = _countables.get(i);

                    TenFrame.XY xy =tenFrame.getLocationOfIthObject(i+1);
                    c.x = xy.x;
                    c.y = xy.y;

                    c.draw(canvas, _paint);
                }
                */
            }

            if(!tappable && COUNTX_CONST.USE_JAIL_BARS) {
                //drawDiagonalBars(canvas);
            }

            drawBorderRectangles(canvas);
            drawRectangleBoundary(canvas);
        }

        // after all drawing, post changes
        _holder.unlockCanvasAndPost(canvas);

    }

    /**
     * Draws the box which counting can happen inside of.
     *
     * @param canvas
     */
    private void drawContainingRectangle(Canvas canvas) {

        int margin = COUNTX_CONST.BOX_MARGIN;
        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin;
        float down = canvas.getHeight() - margin;

        Paint pink = new Paint();
        pink.setColor(COUNTX_CONST.COLOR_PINK);

        canvas.drawRect(left, up, right, down, pink);
    }

    /**
     * Draws bars that indicate the box is not tappable.
     * @param canvas
     */
    private void drawJailBars(Canvas canvas) {

        int margin = COUNTX_CONST.BOX_MARGIN;
        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin;
        float down = canvas.getHeight() - margin;

        float barSpacing = 50;

        Paint jailBars = new Paint();
        jailBars.setStyle(Paint.Style.STROKE);
        jailBars.setStrokeWidth(COUNTX_CONST.BOX_BOUNDARY_STROKE_WIDTH);
        jailBars.setARGB(128, 128, 128, 128);

        for (float x = left; x <= right; x += barSpacing) {
            canvas.drawLine(x, up, x, down, jailBars);
        }
    }

    /**
     * a display mechanism sends diagonal bars over the tapping area
     *
     * @param canvas
     */
    private void drawDiagonalBars(Canvas canvas) {

        int margin = COUNTX_CONST.BOX_MARGIN;
        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin;
        float down = canvas.getHeight() - margin;

        float barSpacing = 50;

        Paint jailBars = new Paint();
        jailBars.setStyle(Paint.Style.STROKE);
        jailBars.setStrokeWidth(COUNTX_CONST.BOX_BOUNDARY_STROKE_WIDTH);
        jailBars.setARGB(128, 128, 128, 128);

        // just pretend we're drawing inside a really big box, then later draw over those lines
        for (float y = up; y <= down * 3; y += barSpacing) {
            float x = y;
            canvas.drawLine(left, y, x, up, jailBars);
        }

    }

    /**
     * Draws the rectangles outside the box so that Countable objects only appear inside box.
     *
     * @param canvas
     */
    private void drawBorderRectangles(Canvas canvas) {

        Paint background = new Paint();
        background.setColor(COUNTX_CONST.COLOR_BACKGROUND);

        int margin = COUNTX_CONST.BOX_MARGIN;

        // top rect
        float left = 0;
        float right = canvas.getWidth();
        float top = 0;
        float bottom = margin;

        canvas.drawRect(left, top, right, bottom, background);

        // left rect
        left = 0; right = margin; top = 0; bottom = canvas.getHeight();
        canvas.drawRect(left, top, right, bottom, background);

        // bottom rect
        left = 0; right = canvas.getWidth(); top = canvas.getHeight() - margin; bottom = canvas.getHeight();
        canvas.drawRect(left, top, right, bottom, background);

        // right rect
        left = canvas.getWidth() - margin; right = canvas.getWidth(); top = 0; bottom = canvas.getHeight();
        canvas.drawRect(left, top, right, bottom, background);
    }


    /**
     * Draws the boundary for the rectangle.
     *
     * @param canvas
     */
    private void drawRectangleBoundary(Canvas canvas) {

        int margin = COUNTX_CONST.BOX_MARGIN;

        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin;
        float down = canvas.getHeight() - margin;

        Paint stroke = new Paint();
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(COUNTX_CONST.BOX_BOUNDARY_STROKE_WIDTH);

        canvas.drawRect(left, up, right, down, stroke);
    }


    /**
     * Reset the number of objects displayed to the number countStart
     * @param countStart
     */
    public void clearObjectsToNumber(int countStart) {

        if(countStart > 0) {
            Log.e(TAG, "Function clearObjectsToNumber not defined for numbers > 0");
        }

        _countables.removeAllElements();
        _fruitMap = generateRandomFruit();

        redraw();
    }

    public void hideTenFrame() {
        showTenFrame = false;
    }

    /**
     * Move items to the DotBag/TenFrame
     */
    public void showTenFrame() {

        // get Index, and XY locations of items
        showTenFrame = true;

        //redraw();

        // animate items so that they move to their proper box in the TenFrame

    }

    /**
     *  make the box either tappable or not
     * @param tappable
     */
    public void enableTapping(boolean tappable) {
        this.tappable = tappable;
        redraw();
    }


    // SurfaceHolder Callback methods
    /**
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.v(TAG, "surfaceCreated");
        _holder = holder;

        Canvas canvas = _holder.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto mull canvas");
        } else {
            initTenFrame();
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

        /*thread.setRunning(false);*/
    }



    private class AnimationThread extends Thread {

        private final SurfaceHolder _surfaceHolder;
        private CCountX_SurfaceView _surfaceView;
        private boolean running = false;
        static final int SLEEP_TIME = 10;

        AnimationThread(SurfaceHolder surfaceHolder, CCountX_SurfaceView surfaceView) {
            _surfaceHolder = surfaceHolder;
            _surfaceView = surfaceView;
        }

        void setRunning(boolean run) { running = run;}

        @Override
        public void run() {

            Canvas c;

            while(running) {

                boolean[] finished = updateAnimationState();

                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);

                    if (!running || c == null) {
                        return;
                    }

                    synchronized (_surfaceHolder) {
                        _surfaceView.draw(c);
                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }

                boolean allFinished = true;
                for(boolean f : finished) {
                    if (!f)
                        allFinished = false;
                }

                if (!running || allFinished) {
                    return;
                }

                try {
                    //
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Animate each countable object to its position
     *
     * @return array of booleans for which are done animating
     */
    private boolean [] updateAnimationState() {
        int step = 10;

        boolean[] done = new boolean[_countables.size()];

        for (int i = 0; i < _countables.size(); i++) {
            Countable c = _countables.get(i);

            TenFrame.XY destination = tenFrame.getLocationOfIthObject(i+1);

            c.x += Math.min(step, destination.x - c.x);
            c.y += Math.min(step, destination.y - c.y);

            if (c.x == destination.x && c.y == destination.y) {
                done[i] = true;
            }

        }

        return done;
    }

}
