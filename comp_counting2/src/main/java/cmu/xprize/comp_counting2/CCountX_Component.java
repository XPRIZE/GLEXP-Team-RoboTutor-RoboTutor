package cmu.xprize.comp_counting2;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import java.util.Arrays;

import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.ltkplus.CGlyphSet;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_counting2.COUNTX_CONST.FTR_COUNTX;
import static cmu.xprize.comp_counting2.COUNTX_CONST.FTR_PLACEVALUE;

/**
 * Created by kevindeland on 10/20/17.
 */

public class CCountX_Component extends PercentRelativeLayout implements ILoadableObject, IPublisher {

    // Infrastructure
    protected final Handler mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    // Layout
    private RelativeLayout Scontent;
    private CCountX_SurfaceView surfaceView;
    private TextView counterText;
    private TextView stimulusText;
    protected TextView checkOne;
    protected TextView checkTen;
    protected TextView checkHundred;

    // DataSource Variables
    protected int _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    private int startingNumber;
    protected int countStart;
    protected int countTarget;
    protected int currentCount;
    protected int currentValue;
    protected int tenPower;
    protected int drawIndex;
    protected String mode = "";
    protected int difficulty = 0;
    protected boolean twoAddition;
    protected float bedge;
    protected boolean tenInited;
    protected int allTaps;


    protected int[] write_numbers;
    protected int[] targetNumbers;
    protected boolean[] writeNumbersTappbale;
    protected boolean canWrite;




    protected IGlyphSink _recognizer;
    protected CGlyphSet _glyphSet;


    // json loadable
    public String bootFeatures;
    public CCountX_Data[] dataSource;


    // View Things
    static protected Context mContext;

    protected TableLayout dotTable;
    protected TableRow[] dotTableRows;

    protected int numDotsCounted;

    private LocalBroadcastManager bManager;

    private ArrayList<Boolean> dotsTapped;


    // Animation stuff
    private boolean isRunning = false;


    static final String TAG = "CCountX_Component";

    // REMOVE private CCount_DotBag dotBag;


    public CCountX_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CCountX_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CCountX_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.tapping, this);

        Scontent = (RelativeLayout) findViewById(R.id.Scontent);
        surfaceView = (CCountX_SurfaceView) findViewById(R.id.imageSurface);
        surfaceView.setComponent(this);
        counterText = (TextView) findViewById(R.id.counterText);
        checkOne = (TextView) findViewById(R.id.checkOne);
        checkTen = (TextView) findViewById(R.id.checkTen);
        checkHundred = (TextView) findViewById(R.id.checkHundred);


        bManager = LocalBroadcastManager.getInstance(getContext());
        drawIndex =-10;


        //mainHandler.post(animationRunnable);

    }

    private int runCount = 0;
    private int runWait = 100;
    private Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {

            if(isRunning && surfaceView!= null) {
                surfaceView.wiggleFruit();
                mainHandler.postDelayed(animationRunnable, 10);
                runCount++;

            }

            // if we're done counting, apply behavior to queue and stop thread
            if (surfaceView.doneMovingToTenFrame) {
                postFinalCount();
                applyBehavior(COUNTX_CONST.DONE_MOVING_TO_TEN_FRAME);
                isRunning = false;

            }
        }
    };

    public void next() {


        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
                numDotsCounted = 0;

                surfaceView.hideTenFrame();

                // reset target text
                stimulusText.setText("");
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, true);
        }

    }

    public boolean dataExhausted() {
        return _dataIndex >= dataSource.length;
    }

    protected void updateDataSet(CCountX_Data data) {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "updateDateSet");

        // first load dataset into fields
        loadDataSet(data);


        resetView();

        retractFeature(FTR_PLACEVALUE);
        retractFeature(FTR_COUNTX);
        switch(mode) {
            case "countingx":
                stimulusText = (TextView) findViewById(R.id.countxStimulusText);
                surfaceView.initTenFrame();
                publishFeature(FTR_COUNTX);
                break;

            case "placevalue":
                stimulusText = (TextView) findViewById(R.id.placevalueStimulusText);
                //stimulusText.setBackgroundColor(Color.WHITE);
                surfaceView.initTenFramePlaceValue();
                publishFeature(FTR_PLACEVALUE);
                break;
        }
        stimulusText.setVisibility(VISIBLE);

        //surfaceView.resetCounter(); // functionality already called above

        // reset vieresetView();
        // update stimulus

        // then update visuals
        // addDots(countTarget);
    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CCountX_Data data) {
        level = Integer.toString(data.difficulty);
        task = data.task;
        layout = data.layout; // NOV_1 make this consistent w/ Anthony
        countStart = data.dataset[0];
        hideRecognizer();
        tenInited = false;
        if (data.tenPower.length==1){
            counterText.setVisibility(View.VISIBLE);
            mode = "countingx";
            difficulty=data.difficulty;
            if (data.tenPower[0].equals("one")){
                tenPower = 1;
            } else if (data.tenPower[0].equals("ten")){
                tenPower = 10;
            } else {
                tenPower = 100;
            }
            countTarget = data.dataset[1];


        } else {
            counterText.setVisibility(View.INVISIBLE);
            canWrite = false;
            mode = "placevalue";
            countTarget = data.dataset[1];
            tenPower = 1;
            difficulty = data.difficulty;
            allTaps = 0;

            int one = countTarget%10;
            int ten = countTarget%100-one;
            int hundred  = countTarget - ten-one;
            if (hundred == 0 && one!=0 && ten!=0){
                write_numbers = new int[]{0,-1,-1};
                writeNumbersTappbale =new boolean[]{false,true,true};
                twoAddition = true;
            } else if(hundred!=0 && ten ==0 && one !=0 && difficulty<2) {
                write_numbers =new int[]{-1,-1,-1};
                writeNumbersTappbale =new boolean[]{true,true,true};
                twoAddition = true;
            } else if(hundred!=0 && ten !=0 && one ==0 && difficulty<2) {
                write_numbers =new int[]{-1,-1,-1};
                writeNumbersTappbale =new boolean[]{true,true,true};
                twoAddition = true;
            } else {
                write_numbers =new int[]{-1,-1,-1};
                writeNumbersTappbale =new boolean[]{true,true,true};
                twoAddition = false;
            }
            targetNumbers = new int[]{(countTarget-countTarget%100)/100,(countTarget%100-countTarget%10)/10,countTarget%10};

            for(int i=0;i<targetNumbers.length;i++){
                if(targetNumbers[i] == 0){
                    surfaceView.reachTarget[i] = true;
                }
            }



        }
        trackAndLogPerformance("START","START","tap","CORRECT");


        Log.d(TCONST.COUNTING_DEBUG_LOG, "target=" + countTarget + ";index=" + _dataIndex);
    }

    /**
     * Reset the view
     */
    protected void resetView() {

        // reset the TextView

        if (mode=="placevalue"){
            surfaceView.clearObjectsToNumber(countStart);
        } else {
            String initialCount = String.valueOf(countStart);
            counterText.setText("0");
            surfaceView.clearObjectsToNumber(countStart);
        }


        // reset the surfaceView

    }

    public void updateCount(int count) {
        // reset the TextView
        currentCount = count;
        String initialCount = String.valueOf(count);
        counterText.setText(initialCount);

        if(currentCount == countTarget) {
            applyBehavior(COUNTX_CONST.DONE_COUNTING_TO_N);
        }
    }

    public void updateCountPlaceValue(int hundred, int ten, int one,int currentV){
//        String initialOne = String.valueOf(one);
//        counterText.setText(initialOne);
//
//        String initialTen = String.valueOf(ten*10);
//        counterTextTen.setText(initialTen);
//
//        String initialHundred = String.valueOf(hundred*100);
//        counterTextHundred.setText(initialHundred);
//
//        String sum = String.valueOf(hundred*100+ten*10+one);
//        counterTextSum.setText(sum);

        if(one == countTarget%10) {
            surfaceView.reachTarget[2] = true;
        }

        if(ten*10 == countTarget%100-countTarget%10){
            surfaceView.reachTarget[1] = true;

        }

        if(hundred*100 == countTarget-countTarget%100){
            surfaceView.reachTarget[0] = true;

        }
        currentValue = currentV;
        if(surfaceView.reachTarget[0] && surfaceView.reachTarget[1] && surfaceView.reachTarget[2]) {
            applyBehavior(COUNTX_CONST.DONE_COUNTING_TO_N);
        }
    }

    public void setCheck(boolean hundred,boolean ten,boolean one){
        if (hundred){
            checkHundred.setVisibility(View.VISIBLE);
        }

        if(ten){
            checkTen.setVisibility(View.VISIBLE);
        }

        if(one){
            checkOne.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Overridden by child class.
     *
     * @param v
     */
    public void nextWord(View v) {

    }

    /**
     * Point at a view
     */
    public void pointAtCenterOfActivity() {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "pointing at something");

        int[] screenCoord = new int[2];
        surfaceView.getLocationOnScreen(screenCoord);

        PointF targetPoint = new PointF(screenCoord[0] + surfaceView.getWidth()/2,
                screenCoord[1] + surfaceView.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
    }

    public void playHundredIns(){
        if(mode == "placevalue"){
            if(!surfaceView.tapped){
                postEvent(COUNTX_CONST.PLACEVALUE_INS_H);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(!surfaceView.tapped){
                                    postEvent(COUNTX_CONST.PLACEVALUE_INS_T);
                                }
                            }
                        },
                        4000
                );

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(!surfaceView.tapped){
                                    postEvent(COUNTX_CONST.PLACEVALUE_INS_O);
                                }
                            }
                        },
                        8000
                );
            }




        }

    }

    public void stopQueue(){
        surfaceView.tapped = true;
    }


//    public void playTenIns(){
//        if(mode == "placevalue" && !surfaceView.tapped){
//            postEvent(COUNTX_CONST.PLACEVALUE_INS_T);
//        }
//
//    }
//
//    public void playOneIns(){
//        if(mode == "placevalue" && !surfaceView.tapped){
//            postEvent(COUNTX_CONST.PLACEVALUE_INS_O);
//        }
//
//    }


    public void pointAtHundred(){
        if(!surfaceView.tapped){
            int[] sides = surfaceView.sides;
            float[] r = surfaceView.getHundred(sides[0],sides[1],sides[2]);
            float left = r[0];
            float right = r[1];


            PointF targetPoint = new PointF((left+right)/2,surfaceView.getHeight()/2);
            Intent msg = new Intent(TCONST.POINTAT);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

            bManager.sendBroadcast(msg);
        }



    }

    public void pointAtWrittingBox(){
            int[] sides = surfaceView.sides;
            float[] r = surfaceView.getOne(sides[0],sides[1],sides[2]);
            float left = r[0];
            float right = r[1];


            PointF targetPoint = new PointF((left+right)/2,surfaceView.getHeight()/2);
            Intent msg = new Intent(TCONST.POINTAT);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

            bManager.sendBroadcast(msg);



    }

    public void pointAtTen(){
        if(!surfaceView.tapped){
            int[] sides = surfaceView.sides;
            float[] r = surfaceView.getTen(sides[0],sides[1],sides[2]);
            float left = r[0];
            float right = r[1];


            PointF targetPoint = new PointF((left+right)/2,surfaceView.getHeight()/2);
            Intent msg = new Intent(TCONST.POINTAT);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

            bManager.sendBroadcast(msg);
        }

    }

    public void pointAtOne(){
        if(!surfaceView.tapped){
            int[] sides = surfaceView.sides;
            float[] r = surfaceView.getOne(sides[0],sides[1],sides[2]);
            float left = r[0];
            float right = r[1];


            PointF targetPoint = new PointF((left+right)/2,surfaceView.getHeight()/2);
            Intent msg = new Intent(TCONST.POINTAT);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

            bManager.sendBroadcast(msg);
        }

    }

    /**
     * Update stimulus and point to it
     */
    public void updateAndIndicateStimulus() {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "indicating stimulus");

        // update target text
        String stimulus = String.valueOf(countTarget);
        stimulusText.setText(stimulus);

        // point to it using RoboFinger
        /*float width = stimulusText.getWidth();

        float scaleStimulusVal = 3.0f;
        if(width > 500) {
            scaleStimulusVal = 2.5f;
        }
        if(width > 1000) {
            scaleStimulusVal = 1.25f;
        }
        if(width > 2000) {
            scaleStimulusVal = 1.0f;
        }

        final float inverseScale = 1 / scaleStimulusVal;

        Animator inflator = CAnimatorUtil.configZoomIn(stimulusText, 600, 0, new BounceInterpolator(), 0f, scaleStimulusVal);
        inflator.start();
        inflator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Animator deflator = CAnimatorUtil.configZoomIn(stimulusText, 600, 0, new LinearInterpolator(), 0f, inverseScale);
                deflator.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        */

        int[] screenCoord = new int[2];
        stimulusText.getLocationOnScreen(screenCoord);

        PointF targetPoint = new PointF(screenCoord[0] + stimulusText.getWidth()/2,
                screenCoord[1] + stimulusText.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);

    }

    /**
     * Overridden by child class.
     */
    public void playChime() {

    }

    public void playCount(int count){

    }

    public void donePlaying(){


    }

    public void playAudio(String filename){}

    public void playTwoAddition(){}

    public void playThreeAddition(){}

    public void initRecognizer(int writePosition){
    }


    public void hideRecognizer(){

    }

    public void changeWritePosition(int writePosition){}

    /**
     * Overridden by child class.
     */
    public void postFinalCount() {

    }

    public void playFinalCount(){}

    public void setBedge(float length){
        if(bedge == 0){
            bedge = length;
        }
    }

    public void demonstrateTenFrame() {
        trackAndLogPerformance("END","END","tap","CORRECT");


        if (mode == "placevalue"){
            checkHundred.setVisibility(View.INVISIBLE);
            checkTen.setVisibility(View.INVISIBLE);
            checkOne.setVisibility(View.INVISIBLE);
            trackAndLogPerformance("STARTWRITING",String.valueOf(difficulty),"tap","CORRECT");

            if (difficulty == 0) {
                String[] displayOptions = {"hundred","ten","one"};
                if(twoAddition){
                    if(targetNumbers[0]==0){
                        surfaceView.displayAddition("ten");
                        surfaceView.displayAddition("one");
                        surfaceView.displayAddition("result");
                        playTwoAddition();

                    } else if (targetNumbers[1]==0){
                        surfaceView.displayAddition("hundred");
                        surfaceView.displayAddition("one");
                        surfaceView.displayAddition("result");
                        playTwoAddition();
                    } else {
                        surfaceView.displayAddition("hundred");
                        surfaceView.displayAddition("ten");
                        surfaceView.displayAddition("result");
                        playTwoAddition();
                    }
                } else {
                    surfaceView.displayAddition("hundred");
                    surfaceView.displayAddition("ten");
                    surfaceView.displayAddition("one");
                    surfaceView.displayAddition("result");
                    playThreeAddition();
                }

            } else if (difficulty == 1){
                if (twoAddition){
                    if(targetNumbers[0]==0){

                        surfaceView.displayAddition("ten");
                        surfaceView.displayAddition("one");
                    } else if (targetNumbers[1] == 0){
                        surfaceView.displayAddition("hundred");
                        surfaceView.displayAddition("one");
                    } else {
                        surfaceView.displayAddition("hundred");
                        surfaceView.displayAddition("ten");
                    }

                    playTwoAddition();
                } else{
                    surfaceView.displayAddition("hundred");
                    surfaceView.displayAddition("ten");
                    surfaceView.displayAddition("one");
                    playThreeAddition();

                }


                initRecognizer(0);
                if (twoAddition && targetNumbers[0]==0){
                    changeWritePosition(1);
                    surfaceView.pickedBox = 1;
                } else {
                    changeWritePosition(0);
                    surfaceView.pickedBox = 0;
                }
                //draw the writting box for students to write result.
                surfaceView.displayWrittingBox("result");



            } else {
                surfaceView.displayAddition("final");
                surfaceView.displayWrittingBox("addition");
                playCount(countTarget);
                initRecognizer(0);
                if (twoAddition){
                    changeWritePosition(1);
                    surfaceView.pickedBox = 1;
                } else {
                    changeWritePosition(0);
                    surfaceView.pickedBox = 0;
                }
                surfaceView.startWrite();
                if(countTarget<=100){
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    displayWrittingIns();

                                }
                            },
                            2500
                    );
                } else {
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    displayWrittingIns();
                                }
                            },
                            3400
                    );
                }










            }

        } else {
            surfaceView.showTenFrame();
            isRunning = true;
            mainHandler.post(animationRunnable);
        }
        //surfaceView.moveItemsToTenFrame();
    }



    public void updateWriteNumber(int writePosition, int number){
        surfaceView.updateWriteNumber(writePosition, number);
        if( Arrays.equals(write_numbers, targetNumbers)){
            hideRecognizer();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if(targetNumbers[0]==0 || (targetNumbers[1]==0 && difficulty<2) || (targetNumbers[2] == 0 && difficulty<2)){
                                playTwoAddition();

                            } else {
                                playThreeAddition();
                            }
                        }},
                    2500
            );
        }

    }

    public void higlightFirst(){
        surfaceView.updateHighlight(0);
    }

    public void highlightSecond(){
        surfaceView.updateHighlight(1);

    }
    public void highlightThird(){
        surfaceView.updateHighlight(2);
        if(twoAddition){
            surfaceView.startWrite();
        }

    }

    public void highlightForth(){
        surfaceView.updateHighlight(3);
        surfaceView.startWrite();

    }

    public void clearHighlight(){
        surfaceView.updateHighlight(-1);
    }

    public void displayWrittingIns(){

    }

    public void enableWriting(){
        canWrite = true;

    }

    protected void trackAndLogPerformance(String expected,String actual,String movement,String cor) {}



    /**
     * prevent the student from tapping
     */
    public void disableTapping() {
        surfaceView.enableTapping(false);
    }

    /**
     * allow the student to tap
     */
    public void enableTapping() {
        surfaceView.enableTapping(true);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            //handleClick();
        }

        return true;
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event) {
        return false;
    }

    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;
    }

    public RelativeLayout getContainer() {
        return Scontent;
    }



    public void postEvent(String event) {
        postEvent(event, 0);
    }

    public void postEvent(String event, Integer delay) {

        post(event, delay);
    }

    public void post(String command, long delay) {

        enQueue(new Queue(command, command), delay);
    }

    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    @Override
    public void publishState() {

    }

    @Override
    public void publishValue(String varName, String value) {

    }

    @Override
    public void publishValue(String varName, int value) {

    }

    @Override
    public void publishFeatureSet(String featureset) {

    }

    @Override
    public void retractFeatureSet(String featureset) {

    }

    @Override
    public void publishFeature(String feature) {

    }

    @Override
    public void retractFeature(String feature) {

    }

    @Override
    public void publishFeatureMap(HashMap featureMap) {

    }

    @Override
    public void retractFeatureMap(HashMap featureMap) {

    }

    /**
     * This is how Component-specific commands are added to the Queue.
     */
    public class Queue implements Runnable {

        String _name;
        String _command;
        String _target;
        String _item;

        Queue(String name, String command) {
            _name = name;
            _command = command;

            nameMap.put(name, this);
        }

        @Override
        public void run() {

            Log.d("COUNTINGX_DEBUG_TAG", "Running queue: _command=" + _command);
            try {
                if(_name != null) {
                    nameMap.remove(_name);
                }

                queueMap.remove(this);

                switch(_command) {
                    case COUNTX_CONST.WRITTING_INS:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLACEVALUE_INS_H:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLACEVALUE_INS_T:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLACEVALUE_INS_O:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_COUNT:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_THREE_ADDITION:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_TWO_ADDITION:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_AUDIO:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_CHIME_PLUS:
                        applyBehavior(_command);
                        break;
                    case COUNTX_CONST.PLAY_CHIME:
                        applyBehavior(_command);
                        break;

                    default:
                        break;
                }

            }  catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }

        }
    }

}