package cmu.xprize.comp_counting2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.util.Arrays;
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
    private Vector<Countable> _countablesTen;
    private Vector<Countable> _countablesHundred;
    private boolean tappable;
    private boolean[] drawResult;
    private String writting_box;
    private boolean showTenFrame = false;
    protected boolean[] reachTarget = new boolean[] {false,false,false,false};
    protected int pickedBox = 0;
    protected boolean startWrite = false;
    protected boolean tapped = false;
    protected int[] sides = new int[]{0,0,0,0};
    protected int highlight = -1;




    private TenFrame tenFrame;
    private TenFrame tenFrameTen;
    private TenFrame tenFrameHundred;



    private int[] FRUITS = {
            R.drawable.banana,
            R.drawable.pear,
            R.drawable.pineapple,
            R.drawable.tomato
    };

    private int[] TENS = {
            R.drawable.sq_ten_black,
            R.drawable.sq_ten_blue,
            R.drawable.sq_ten_green,
            R.drawable.sq_ten_teal
    };

    private int[] HUNDREDS = {
            R.drawable.sq_hun_black,
            R.drawable.sq_hun_blue,
            R.drawable.sq_hun_green,
            R.drawable.sq_hun_teal
    };

    private int[] ONES = {
            R.drawable.sq_one_black,
            R.drawable.sq_one_blue,
            R.drawable.sq_one_green,
            R.drawable.sq_one_teal
    };

    private int[] TENSP = {
            R.drawable.sq_tenh_black,
            R.drawable.sq_tenh_blue,
            R.drawable.sq_tenh_green,
            R.drawable.sq_tenh_teal
    };

    //tens
    //hundreds
    //

    // holds the image for the fruit Countable being displayed(place value: one)
    private Bitmap _countableBitmap;
    private Bitmap _countableBitmapTen;
    private Bitmap _countableBitmapHundred;

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
        _countablesTen = new Vector<>();
        _countablesHundred = new Vector<>();


        drawResult = new boolean[] {false,false,false,false};
        writting_box = "";



        //resetCounter();


        getHolder().addCallback(this);
    }

    protected void initTenFrame() {
        if (!_component.tenInited)
        {
        int holeWidth = 200;
        int holeHeight = 200;
        boolean isline =false;
        int x = (getWidth() / 2) - (holeWidth * 5) / 2;
        int y = (getHeight() / 2) - holeHeight;
        if (_component.tenPower==100){
            holeWidth = getWidth()/6;
            holeHeight = getWidth()/6;
            x = (getWidth() / 2) - (holeWidth * 5) / 2;
            y = (getHeight() / 2) - holeHeight;
        }

        if (_component.tenPower==10){
            holeWidth =getWidth()/20;
            holeHeight = getHeight()/3;
            isline=true;
            x = (getWidth() / 2) - (holeWidth * 10) / 2;
            y = (getHeight() / 2) - holeHeight/2;
        }




        tenFrame = new TenFrame(x, y, holeWidth, holeHeight,isline);
        _component.tenInited = true;
        }
    }

    protected void initTenFramePlaceValue() {
        if (!_component.tenInited)
        {
            int margin = COUNTX_CONST.BOX_MARGIN;
            int marginright = COUNTX_CONST.BOX_MARGINRIGHT;
            int marginbottom = COUNTX_CONST.BOX_MARGINBOTTOM;
            int boxmargin = COUNTX_CONST.BOXM;
            int left = margin;
            int right = getWidth() - marginright;
            int up = margin*2;
            int down = getHeight() - marginbottom;
            int rectHeight = (down-up-boxmargin*2)/9;

            float[] oneBox = getOne(left,right,boxmargin);
            float[] tenBox = getTen(left,right,boxmargin);
            float[] hundredBox = getHundred(left,right,boxmargin);


            int hundredFrameHeight = (down-up)/3;
            int hundredFrameWidth = (int)hundredBox[2]/3;


            int tenFrameHeight = (down-up)/11;
            int tenFrameWidth = (int)tenBox[2];

            int oneFrameHeight = (down-up)/11;
            int oneFrameWidth = (int)oneBox[2];



            //tenFrame for hundred
            tenFrameHundred = new TenFrame(((int)hundredBox[0]+(int)hundredBox[1])/2-hundredFrameWidth*3/2, (up+down)/2-hundredFrameHeight*3/2, hundredFrameWidth,hundredFrameHeight, false,true);
            //tenFrame for ten
            tenFrameTen = new TenFrame(((int)tenBox[0]+(int)tenBox[1])/2-tenFrameWidth/2, (up+down)/2-tenFrameHeight*5, tenFrameWidth,tenFrameHeight, true,true);
            //tenFrame for one
            tenFrame = new TenFrame(((int)oneBox[0]+(int)oneBox[1])/2-oneFrameWidth/2,(up+down)/2-oneFrameHeight*5,oneFrameWidth,oneFrameHeight, true,true);
            _component.tenInited = true;


//
//            holeWidth = getWidth()/6;
//            holeHeight = getWidth()/6;
//            x = (getWidth() / 2) - (holeWidth * 5) / 2;
//            y = (getHeight() / 2) - holeHeight;
//
//
//            if (x> margin && x<rectWidth+margin){
//                //tap on the hundred box
//                countable = new CountableImage((int)x, (int) y, _countableBitmapHundred);
//                _countablesHundred.add(countable);
//
//            } else if(x> margin+rectWidth+boxmargin && x<margin+2*rectWidth+boxmargin){
//                //tap on the ten box
//
//                countable = new CountableImage((int)x, (int) y, _countableBitmapTen);
//                _countablesTen.add(countable);
//
//            }  else if (x>margin+2*(rectWidth+boxmargin) && x<right){
//                //tap on the one box
//                countable = new CountableImage((int)x, (int) y, _countableBitmap);
//                _countables.add(countable);
//
//
//
//
//               tenFrame = new TenFrame(x, y, holeWidth, holeHeight,isline);
//            _component.tenInited = true;
       }
    }

    public void setComponent(CCountX_Component component) {
        _component = component;
    }




    /**
     * Pick a random Countable object.
     * Ones = fruit
     * Tens = ten bar
     * Hundreds = hundred square
     *
     * @return
     */
    private Bitmap[] generateRandomCountable() {
        if(_component.mode == "placevalue"){
            int index = (new Random()).nextInt(ONES.length);
            int drawable = ONES[index];
            int drawableTen = TENSP[index];
            int drawableHundred = HUNDREDS[index];
//            _component.drawIndex = drawable;
            Bitmap immutableOneBmp = BitmapFactory.decodeResource(getResources(), drawable);
            Bitmap oneBmp = Bitmap.createScaledBitmap(immutableOneBmp, getHeight()/70,
                    getHeight()/70, false);

            Bitmap immutableTenBmp = BitmapFactory.decodeResource(getResources(), drawableTen);
            Bitmap tenBmp = Bitmap.createScaledBitmap(immutableTenBmp, getWidth()/12,
                    getWidth()/120, false);

            Bitmap immutableHundredBmp = BitmapFactory.decodeResource(getResources(), drawableHundred);
            Bitmap hundredBmp = Bitmap.createScaledBitmap(immutableHundredBmp, getHeight()/8,
                    getHeight()/8, false);



            Bitmap[] result = {oneBmp,tenBmp,hundredBmp};
            return result;


        } else{
            // initialize images
            if(_component.tenPower == 1) {
                // ones uses random fruit
                int drawable = FRUITS[(new Random()).nextInt(FRUITS.length)];

                _component.drawIndex=drawable;

                Bitmap immutableBmp = BitmapFactory.decodeResource(getResources(), drawable);
                Bitmap resizedBmp = Bitmap.createScaledBitmap(immutableBmp, COUNTX_CONST.DRAWABLE_RADIUS * 2,
                        COUNTX_CONST.DRAWABLE_RADIUS * 2, false);
                Bitmap[] result = {resizedBmp};
                return result;

            } else if(_component.tenPower==10){
                // tens uses colored ten block
                int drawable = TENS[(new Random()).nextInt(TENS.length)];

                _component.drawIndex=drawable;

                //int bheight = (int)(down-up-2*gmargin);
                //int bwidth = (int)((right-left-11*gmargin)/10);

                Bitmap immutableBmp = BitmapFactory.decodeResource(getResources(), drawable);
                Bitmap resizedBmp = Bitmap.createScaledBitmap(immutableBmp, getHeight()/20,
                        getHeight()/2, false);

                Bitmap[] result = {resizedBmp};
                return result;
            } else {
                // hundreds uses colored ten square
                int drawable = HUNDREDS[(new Random()).nextInt(HUNDREDS.length)];

                _component.drawIndex=drawable;


                //int bheight = (int)((down-up-3*gmargin)/2);
                //int bwidth = (int)((right-left-6*gmargin)/5);
                //int radius = bheight<bwidth ? bheight : bwidth;

                Bitmap immutableBmp = BitmapFactory.decodeResource(getResources(), drawable);
                Bitmap resizedBmp = Bitmap.createScaledBitmap(immutableBmp, getHeight()/5,
                        getHeight()/5, false);
                Bitmap[] result = {resizedBmp};
                return result;

            }

        }


    }

    public boolean doneMovingToTenFrame = false;

    public void displayAddition(String type){
        if (type == "hundred"){
            drawResult[0] =true;
        } else if (type == "ten"){
            drawResult[1] =true;

        } else if (type == "one"){
            drawResult[2] =true;

        } else{
            drawResult[3] =true;
        }
//        int hundred = _countablesHundred.size()*100;
//        int ten = _countablesTen.size()*10;
//        int one = _countables.size();

        redraw();
    }


    public void displayWrittingBox(String type){
        writting_box = type;
        redraw();
    }

    public void updateHighlight(int n){
        highlight=n;
        redraw();
        if(_component.difficulty ==1 && !Arrays.equals(_component.write_numbers, _component.targetNumbers)){
            if((_component.twoAddition && highlight==2)||(!_component.twoAddition && highlight==3)){
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                highlight = -1;
                                _component.displayWrittingIns();

                                redraw();
                            }},
                        1500
                );}
        }

    }


    public void updateWriteNumber(int writePosition, int number){
        final int writeP = writePosition;
        if (_component.canWrite && _component.writeNumbersTappbale[writePosition]){
            _component.write_numbers[writePosition] = number;
            if (_component.targetNumbers[writePosition] == _component.write_numbers[writePosition]){
                _component.trackAndLogPerformance(Integer.toString(_component.targetNumbers[writePosition]),Integer.toString(_component.write_numbers[writePosition]),"write","CORRECT");
                _component.writeNumbersTappbale[writePosition] = false;
                _component.playCount(_component.write_numbers[writePosition]);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                _component.playAudio("Goodjob"); }},
                        1000
                );
                if (pickedBox+1<=2){
                    pickedBox=pickedBox+1;
                    _component.changeWritePosition(pickedBox);
                }
                redraw();
            } else {
                _component.trackAndLogPerformance(Integer.toString(_component.targetNumbers[writePosition]),Integer.toString(_component.write_numbers[writePosition]),"write","INCORRECT");
                redraw();
                _component.playCount(_component.write_numbers[writePosition]);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                _component.playAudio("try");
                                _component.write_numbers[writeP] = -1;
                                redraw();
                            }},
                        1000
                );


            }

        }

    }


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
            if (_component.mode != "placevalue"){
                Log.v(TAG, "Touched surface! X=" + event.getRawX() + ", Y=" + event.getRawY());
                float x = event.getX();
                float y = event.getY();

                Log.v(TAG, "Holder in onTouch: " + _holder);

                // first lock canvas
                Canvas canvas = _holder.lockCanvas();

                // don't draw if outside the box
                int margin = COUNTX_CONST.BOX_MARGIN;
                float left = margin;
                float right = canvas.getWidth() - margin;
                float up = margin;
                float down = canvas.getHeight() - margin;
                int gmargin = COUNTX_CONST.GRID_MARGIN;

                if(x < margin || x > canvas.getWidth() - margin || y < margin || y > canvas.getHeight() - margin) {
                    redraw(canvas);
                    return true;
                }

                Countable countable;
                if (_component.tenPower ==1){

                    countable = new CountableImage((int)x, (int) y, _countableBitmap);
                    _countables.add(countable);

                } else if(_component.tenPower==10){

                    int centerx = (int)x ;
                    int centery = (int)((down-up)/2);

                    TenFrame.XY destination = tenFrame.getLocationOfIthObject(_countables.size()+1);

                    countable = new CountableImage(destination.x,destination.y, _countableBitmap);
                    _countables.add(countable);

                }  else {
                    int indexx =_countables.size()%5;
                    int indexy =_countables.size()/5;
                    int centerx = (int)x;
                    int centery = (int)y;
                    TenFrame.XY destination = tenFrame.getLocationOfIthObject(_countables.size()+1);
                    countable = new CountableImage(destination.x,destination.y, _countableBitmap);
                    _countables.add(countable);

                }


                _component.updateCount(_countables.size()*_component.tenPower);
                redraw(canvas);

                // make sure to update the TextView
                //*1/*10*
                // playChime plays the chime, AND the audio...
                _component.playChime();

            } else {
                Log.v(TAG, "Touched surface! X=" + event.getRawX() + ", Y=" + event.getRawY());
                float x = event.getX();
                float y = event.getY();
                int currentValue = 0;

                Log.v(TAG, "Holder in onTouch: " + _holder);

                // first lock canvas
                Canvas canvas = _holder.lockCanvas();
                int margin = COUNTX_CONST.BOX_MARGIN;
                int marginright = COUNTX_CONST.BOX_MARGINRIGHT;
                int marginbottom = COUNTX_CONST.BOX_MARGINBOTTOM;
                int boxmargin = COUNTX_CONST.BOXM;
                float left = margin;
                float right = canvas.getWidth() - marginright;
                float up = margin*2;
                float down = canvas.getHeight() - marginbottom;

                float[] oneBox = getOne(left,right,boxmargin);
                float[] tenBox = getTen(left,right,boxmargin);
                float[] hundredBox = getHundred(left,right,boxmargin);


                Countable countable;


                if (x>=hundredBox[0] && x<= hundredBox[1]&& !reachTarget[0]){
                    tapped = true;
                    //tap on the hundred box
                    TenFrame.XY destination = tenFrameHundred.getLocationOfIthObject(_countablesHundred.size()+1);

                    countable = new CountableImage(destination.x,destination.y, _countableBitmapHundred);
                    _countablesHundred.add(countable);
                    currentValue = _countablesHundred.size()*100;
                    _component.allTaps+=1;

                } else if(x>=tenBox[0] && x<= tenBox[1]&& !reachTarget[1]){
                    //tap on the ten box
                    tapped = true;

                    TenFrame.XY destination = tenFrameTen.getLocationOfIthObject(_countablesTen.size()+1);

                    countable = new CountableImage(destination.x,destination.y, _countableBitmapTen);
                    _countablesTen.add(countable);
                    currentValue = _countablesTen.size()*10;
                    _component.allTaps+=1;

                }  else if (x>=oneBox[0] && x<= oneBox[1]&& !reachTarget[2]){
                    tapped = true;
                    //tap on the one box
                    TenFrame.XY destination = tenFrame.getLocationOfIthObject(_countables.size()+1);

                    countable = new CountableImage(destination.x,destination.y, _countableBitmap);

                    _countables.add(countable);
                    currentValue = _countables.size();
                    _component.allTaps+=1;
                } else {
                    redraw(canvas);
                    return true;
                }

                if(_countables.size() == _component.countTarget%10) {
                    reachTarget[2] = true;
                    if (_component.targetNumbers[2]!=0){
                    _component.setCheck(false,false,true);}

                }

                if(_countablesTen.size()*10 == _component.countTarget%100-_component.countTarget%10){
                    reachTarget[1] = true;
                    if (_component.targetNumbers[1]!=0){
                        _component.setCheck(false,true,false);
                    }



                }

                if(_countablesHundred.size()*100 == _component.countTarget-_component.countTarget%100){
                    reachTarget[0] = true;
                    if(_component.targetNumbers[0]!=0){
                        _component.setCheck(true,false,false);
                    }


                }




                redraw(canvas);

                // make sure to update the TextView
                _component.updateCountPlaceValue(_countablesHundred.size(),_countablesTen.size(),_countables.size(), currentValue);
                _component.playChime();
                //*1/*10*
                // playChime plays the chime, AND the audio...
//                _component.playChime();

            }



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
       if(_holder !=null){
            Paint background = new Paint();
            background.setColor(COUNTX_CONST.COLOR_BACKGROUND);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), background);
        }

        if (_holder != null && _component.mode!="") {
            if (_component.mode != "placevalue"){
                drawContainingRectangle(canvas);

                // draw each Countable object
                for (Countable c : _countables) {
                    c.draw(canvas, _paint);
                }

                if (showTenFrame && _component.tenPower==1) {
                    tenFrame.draw(canvas, _paint);
                }



                drawBorderRectangles(canvas);
                drawRectangleBoundary(canvas);

            } else{
                sides[0] = COUNTX_CONST.BOX_MARGIN;
                sides[1] = canvas.getWidth() - COUNTX_CONST.BOX_MARGINRIGHT;
                sides[2] = COUNTX_CONST.BOXM;
                sides[3] = canvas.getDensity();


                //in the place value mode.
                drawRectangles(canvas);

                //drawRectangles and the texts(for the 2nd screen)
                //first draw the Rectangeles

                if(!drawResult[0] && !drawResult[1] && !drawResult[2] && !drawResult[3]){
                    for (Countable c : _countables) {
                        c.draw(canvas, _paint);
                    }
                    for (Countable ct : _countablesTen) {
                        ct.draw(canvas, _paint);
                    }
                    for (Countable ch : _countablesHundred) {
                        ch.draw(canvas, _paint);
                    }
                }





            }

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

    protected float[] getOne(float left, float right,float boxmargin){
        float boxwidth = (right-left-boxmargin*2)/6;
        float[] result = {right-boxwidth,right,boxwidth};
        return result;
    }

    protected float[] getTen(float left, float right,float boxmargin){
        float boxwidth = (right-left -boxmargin*2)/6;
        float bleft = left+boxwidth*3+boxmargin;
        float bright = left+boxwidth*5+boxmargin;
        float[] result = {bleft,bright,boxwidth*2};
        return result;

    }

    protected float[] getHundred(float left, float right,float boxmargin){
        float boxwidth = (right-left -boxmargin*2)/6;
        float[] result = {left,left+boxwidth*3,boxwidth*3};
        return result;

    }

    protected void startWrite(){
        startWrite = true;
        redraw();
    }




    private void drawRectangles(Canvas canvas){
        int margin = COUNTX_CONST.BOX_MARGIN;
        int marginright = COUNTX_CONST.BOX_MARGINRIGHT;
        int marginbottom = COUNTX_CONST.BOX_MARGINBOTTOM;
        int txtbottom = COUNTX_CONST.BOX_TEXTBOTTOM;
        int boxmargin = COUNTX_CONST.BOXM;
        int resultfont = COUNTX_CONST.RESULT_SIZE;
        float left = margin;
        float right = canvas.getWidth() - marginright;
        float up = margin*2;
        float down = canvas.getHeight() - marginbottom;
        float textup = canvas.getHeight()-marginbottom;
        float textdown = canvas.getHeight()-txtbottom;


//        float rectWidth = (right-left-boxmargin*2)/3;
        float rectHeight = (down-up-boxmargin*2)/10;

        _component.setBedge(rectHeight);

        Paint green = new Paint();
        green.setStyle(Paint.Style.STROKE);
        green.setColor(COUNTX_CONST.COLOR_BLUE);
        green.setStrokeWidth(10);

        Paint lightgreen = new Paint();
        lightgreen.setStyle(Paint.Style.FILL);
        lightgreen.setColor(COUNTX_CONST.COLOR_LIGHTGREEN);

        Paint unpicked = new Paint();
        unpicked.setStyle(Paint.Style.STROKE);
        unpicked.setColor(COUNTX_CONST.COLOR_DARKGREY);
        unpicked.setStrokeWidth(8);


        Paint picked = new Paint();
        picked.setStyle(Paint.Style.STROKE);
        picked.setColor(COUNTX_CONST.COLOR_BLUE);
        picked.setStrokeWidth(8);

        Paint pickedFill = new Paint();
        pickedFill.setStyle(Paint.Style.FILL);
        pickedFill.setColor(COUNTX_CONST.COLOR_YELLOW);




        Paint lightblue = new Paint();
        lightblue.setStyle(Paint.Style.FILL);
        lightblue.setColor(COUNTX_CONST.COLOR_BLUE);

        Paint grey = new Paint();
        grey.setStyle(Paint.Style.STROKE);
        grey.setColor(COUNTX_CONST.COLOR_DARKGREY);
        grey.setStrokeWidth(5);

        Paint text = new Paint();
        text.setColor(COUNTX_CONST.COLOR_BLUE);
        text.setTextSize(COUNTX_CONST.TEXT_SIZE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);

        Paint textGrey = new Paint();
        textGrey.setColor(COUNTX_CONST.COLOR_DARKGREY);
        textGrey.setTextSize(COUNTX_CONST.TEXT_SIZE);
        textGrey.setTextAlign(Paint.Align.CENTER);
        textGrey.setFakeBoldText(true);

        Paint textRed = new Paint();
        textRed.setColor(COUNTX_CONST.COLOR_RED);
        textRed.setTextSize(COUNTX_CONST.TEXT_SIZE);
        textRed.setTextAlign(Paint.Align.CENTER);
        textRed.setFakeBoldText(true);


        if (drawResult[0] || drawResult[1] || drawResult[2] || drawResult[3]) {

            canvas.drawRect(left, up, right, textdown, lightgreen);
            canvas.drawRect(left, up, right, textdown, green);
            float textXTen = (right+left)/2;
            float textXOne = textXTen+resultfont+50;
            float textXHundred =  textXTen-resultfont-50;

            float textHeight = 0;
            boolean twoAddition = _component.twoAddition;
            //displayOption: 0 for the three addition view. 1 for hundred and ten; 2 for ten and one.



            if (!twoAddition){
                textHeight = (textdown-up)/4;
            } else {
                textHeight = (textdown-up)/3;
            }


            if(highlight!=-1){
                canvas.drawRect(textXHundred-resultfont/2,up+textHeight*(highlight+1)-resultfont*3/2, textXOne+resultfont,up+textHeight*(highlight+1)-resultfont/4,pickedFill);
            }











            if (drawResult[0]){


                if(!twoAddition){


                    canvas.drawText( String.valueOf(_countablesHundred.size()),textXHundred,up+textHeight-resultfont/2,text);
                    canvas.drawText( "0",textXTen,up+textHeight-resultfont/2,text);
                    canvas.drawText( "0",textXOne,up+textHeight-resultfont/2,text);
                } else {
                    //hundred is always the top
                    canvas.drawText( String.valueOf(_countablesHundred.size()),textXHundred,up+textHeight-resultfont/2,text);
                    canvas.drawText( "0",textXTen,up+textHeight-resultfont/2,text);
                    canvas.drawText( "0",textXOne,up+textHeight-resultfont/2,text);

                }

            }

            if(drawResult[1]){
                if(!twoAddition){
                    canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight*2-resultfont/2,text);
                    canvas.drawText( "0",textXOne,up+textHeight*2-resultfont/2,text);
                } else {
                    if(_component.targetNumbers[0]==0){
                        //if 0 is in the hundred digit, then draw the tens digit in the first place
                        canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight-resultfont/2,text);
                        canvas.drawText( "0",textXOne,up+textHeight-resultfont/2,text);

                    } else if (_component.targetNumbers[2] == 0){
                        canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight*2-resultfont/2,text);
                        canvas.drawText( "0",textXOne,up+textHeight*2-resultfont/2,text);
                        canvas.drawText("+",textXHundred,up+textHeight*2-resultfont/2,text);
                        canvas.drawLine(textXHundred-resultfont/2,up+textHeight*2,textXOne+resultfont/2,up+textHeight*2, green);
                    }

                }

            }

            if(drawResult[2]){
                if (!twoAddition){
                    canvas.drawText( String.valueOf(_countables.size()*1),textXOne,up+textHeight*3-resultfont/2,text);
                    canvas.drawLine(textXHundred-resultfont/2,up+textHeight*3,textXOne+resultfont/2,up+textHeight*3, green);
                    canvas.drawText("+",textXHundred,up+textHeight*3-resultfont/2,text);
                } else {

                    canvas.drawText( String.valueOf(_countables.size()*1),textXOne,up+textHeight*2-resultfont/2,text);
                    canvas.drawLine(textXHundred-resultfont/2,up+textHeight*2,textXOne+resultfont/2,up+textHeight*2, green);
                    canvas.drawText("+",textXHundred,up+textHeight*2-resultfont/2,text);
                }

            }

            if(drawResult[3]){
                if (!twoAddition) {
                    canvas.drawLine(textXHundred-resultfont/2,up+textHeight*3,textXOne+resultfont/2,up+textHeight*3, green);
                    canvas.drawText("+",textXHundred,up+textHeight*3-resultfont/2,text);
                    canvas.drawText( String.valueOf(_countablesHundred.size()),textXHundred,up+textHeight*4-resultfont/2,text);
                    canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight*4-resultfont/2,text);
                    canvas.drawText( String.valueOf(_countables.size()),textXOne,up+textHeight*4-resultfont/2,text);
                } else {
                    canvas.drawLine(textXHundred-resultfont/2,up+textHeight*2,textXOne+resultfont/2,up+textHeight*2, green);
                    canvas.drawText("+",textXHundred,up+textHeight*2-resultfont/2,text);
                    if(_component.targetNumbers[0] !=0 ){
                        canvas.drawText( String.valueOf(_countablesHundred.size()),textXHundred,up+textHeight*3-resultfont/2,text);

                    }
                    canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight*3-resultfont/2,text);
                    canvas.drawText( String.valueOf(_countables.size()),textXOne,up+textHeight*3-resultfont/2,text);
                }

            }

            if(writting_box=="result"){
                int[] current = _component.write_numbers;
                float[] position = {textXHundred,textXTen,textXOne};

                int[] target_numbers = _component.targetNumbers;
                float y =0;
                int startI =0;

                if (twoAddition){
                    y = up+textHeight*3-resultfont/2;
                    if(_component.targetNumbers[0] ==0){
                        startI =1;
                    }

                } else {
                    y = up+textHeight*4-resultfont/2;

                }

                for(int i = startI; i< target_numbers.length; i++){

                    if (target_numbers[i] == current[i]) {
                        canvas.drawText( String.valueOf(target_numbers[i]),position[i],y,text);
                    } else if (current[i] == -1) {
                        float x = position[i];

                        float boxWidth = resultfont;
                        if(i==pickedBox && startWrite){
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,pickedFill);
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,picked);


                        } else{
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,unpicked);
                        }


                    } else {
                        //wrong answer
                        canvas.drawText( String.valueOf(current[i]),position[i],y,textRed);

                    }
                }
            } else if (writting_box=="addition") {
                int[] current = _component.write_numbers;
                float[] positionX = {textXHundred,textXTen,textXOne};
                float[] positionY;
                int startI = 0;
                if (!twoAddition){
                    positionY = new float[] {up+textHeight-resultfont/2,up+textHeight*2-resultfont/2,up+textHeight*3-resultfont/2};
                    canvas.drawText( "0",textXOne,positionY[0],text);
                    canvas.drawText( "0",textXTen,positionY[0],text);
                    canvas.drawText( "0",textXOne,positionY[1],text);
                } else {
                    positionY = new float[] {0,up+textHeight-resultfont/2,up+textHeight*2-resultfont/2};
                    canvas.drawText( "0",textXOne,positionY[1],text);
                    startI =1;
                }

                int[] target_numbers = _component.targetNumbers;

                for(int i = startI; i< target_numbers.length; i++){
                     if (target_numbers[i] == current[i]) {
                        canvas.drawText( String.valueOf(target_numbers[i]),positionX[i],positionY[i],text);
                    } else if (current[i] == -1) {
                        float x = positionX[i];
                        float y = positionY[i];
                        float boxWidth = resultfont;
                        if(i==pickedBox && startWrite){
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,pickedFill);
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,picked);
                        } else{
                            canvas.drawRect(x-boxWidth/2,y-boxWidth, x+boxWidth/2,y,unpicked);
                        }


                    }
                    else {
                        canvas.drawText( String.valueOf(current[i]),positionX[i],positionY[i],textRed);
                    }
                }
            }



        } else{


            Bitmap immutableBackgroundBmp = BitmapFactory.decodeResource(getResources(), R.drawable.greyb);



            for (int x = 0; x <= 2; x += 1) {


                if (x==0){
                    float[] box = getHundred(left,right,boxmargin);
                    float bleft = box[0];
                    float bright = box[1];
                    float boxw = box[2];
                    if(_component.targetNumbers[0]==0){
                        Bitmap oneBmp = Bitmap.createScaledBitmap(immutableBackgroundBmp, (int)boxw,
                                (int)(down-up), false);
                        canvas.drawBitmap(oneBmp,bleft,up,null);
                        canvas.drawRect(bleft,up,bright,down,grey);
                        canvas.drawRect(bleft,textup,bright,textdown,grey);
                        canvas.drawText( String.valueOf(_countablesHundred.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,textGrey);


                    } else {
                        canvas.drawRect(bleft,up,bright,down,lightgreen);
                        canvas.drawRect(bleft,up,bright,down,green);
                        canvas.drawRect(bleft,textup,bright,textdown,lightgreen);
                        canvas.drawRect(bleft,textup,bright,textdown,green);
                        canvas.drawText( String.valueOf(_countablesHundred.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,text);
                    }




                } else if (x==1){
                    float[] box = getTen(left,right,boxmargin);
                    float bleft = box[0];
                    float bright = box[1];
                    float boxw = box[2];

                    if (_component.targetNumbers[1]==0){
                        Bitmap oneBmp = Bitmap.createScaledBitmap(immutableBackgroundBmp, (int)boxw,
                                (int)(down-up), false);
                        canvas.drawBitmap(oneBmp,bleft,up,null);
                        canvas.drawRect(bleft,up,bright,down,grey);
                        canvas.drawRect(bleft,textup,bright,textdown,grey);
                        canvas.drawText( String.valueOf(_countablesTen.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,textGrey);


                    } else {
                        canvas.drawRect(bleft,up,bright,down,lightgreen);
                        canvas.drawRect(bleft,up,bright,down,green);
                        canvas.drawRect(bleft,textup,bright,textdown,lightgreen);
                        canvas.drawRect(bleft,textup,bright,textdown,green);
                        canvas.drawText( String.valueOf(_countablesTen.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,text);


                    }

//                if (reachTarget[x]){
////                    canvas.drawBitmap(backgroundBmp,left+x*(rectWidth+boxmargin),up,null);
//                    canvas.drawRect(left+x*(rectWidth+boxmargin),up,left+(x+1)*rectWidth+x*boxmargin,down,grey);
//                }else{
//                    canvas.drawRect(left+x*(rectWidth+boxmargin),up,left+(x+1)*rectWidth+x*boxmargin,down,lightgreen);
//                    canvas.drawRect(left+x*(rectWidth+boxmargin),up,left+(x+1)*rectWidth+x*boxmargin,down,green);
//                }

                } else{
                    float[] box = getOne(left,right,boxmargin);
                    float bleft = box[0];
                    float bright = box[1];
                    float boxw = box[2];
                    if(_component.targetNumbers[2]==0){
                        Bitmap oneBmp = Bitmap.createScaledBitmap(immutableBackgroundBmp, (int)boxw,
                                (int)(down-up), false);
                        canvas.drawBitmap(oneBmp,bleft,up,null);
                        canvas.drawRect(bleft,up,bright,down,grey);
                        canvas.drawRect(bleft,textup,bright,textdown,grey);
                        canvas.drawText( String.valueOf(_countables.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,textGrey);

                    } else {
                        canvas.drawRect(bleft,up,bright,down,lightgreen);
                        canvas.drawRect(bleft,up,bright,down,green);
                        canvas.drawRect(bleft,textup,bright,textdown,lightgreen);
                        canvas.drawRect(bleft,textup,bright,textdown,green);
                        canvas.drawText( String.valueOf(_countables.size()),bleft+boxw/2,down+COUNTX_CONST.TEXT_SIZE,text);

                    }

//

                }

        }



//            int currentCount = _countablesHundred.size()*100+_countablesTen.size()*10+_countables.size();





//            canvas.drawLine(right+marginright/5,up+textHeight*3,right+marginright*4/5,up+textHeight*3, green);
//            canvas.drawText("+",(textXHundred+right)/2,up+textHeight*3,text);
//            canvas.drawText( String.valueOf(_countablesHundred.size()),textXHundred,up+textHeight*4-COUNTX_CONST.TEXT_SIZE/2,text);
//            canvas.drawText( String.valueOf(_countablesTen.size()),textXTen,up+textHeight*4-COUNTX_CONST.TEXT_SIZE/2,text);
//            canvas.drawText( String.valueOf(_countables.size()),textXOne,up+textHeight*4-COUNTX_CONST.TEXT_SIZE/2,text);




        }




    }
    /**
     * Draws bars that indicate the box is not tappable.
     * @param canvas
     */
    private void drawJailBars(Canvas canvas) {

        int margin = COUNTX_CONST.BOX_MARGIN;
        float left = margin;
        float right = canvas.getWidth() - margin;
        float up = margin*2;
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

    private void drawJailBars(Canvas canvas,float left,float right,float up,float down) {

        float barSpacing = 10;

        Paint jailBars = new Paint();
        jailBars.setStyle(Paint.Style.STROKE);
        jailBars.setStrokeWidth(COUNTX_CONST.BOX_BOUNDARY_STROKE_WIDTH);
        jailBars.setColor(COUNTX_CONST.COLOR_DARKGREY);

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
        float up = margin*2;
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
        if(_component.mode != "placevalue"){
            _countables.removeAllElements();
            _countableBitmap = generateRandomCountable()[0];
        } else{
            _countables.removeAllElements();
            _countablesTen.removeAllElements();
            _countablesHundred.removeAllElements();
            Bitmap[] result = generateRandomCountable();
            _countableBitmap = result[0];
            _countableBitmapTen = result[1];
            _countableBitmapHundred = result[2];


            reachTarget = new boolean[] {false,false,false,false};
            for(int i=0;i<_component.targetNumbers.length;i++){
                if(_component.targetNumbers[i] == 0){
                    reachTarget[i] = true;
                }
            }
            tapped = false;
            highlight = -1;
            drawResult = new boolean[] {false,false,false,false};
            writting_box = "";

            showTenFrame = false;
            pickedBox = 0;
            startWrite = false;
        }




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
            //initTenFrame();
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
