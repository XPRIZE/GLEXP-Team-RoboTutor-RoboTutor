package cmu.xprize.comp_numberscale;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CNumberScale_Component extends RelativeLayout implements ILoadableObject {


    // Infrastructure
    protected final Handler mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;


    protected ImageView Scontent;
    protected CNumberScale_player player;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;
    protected int countStart;
    protected int delta;
    protected int maxHit;
    private TextView addNumber;
    private TextView minusNumber;
    private TextView displayNumber;
    private int currentHit;
    protected int currentNumber;
    private int min;
    private int max;

    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CNumberScale_Data[] dataSource;


    // View Things
    protected Context mContext;

    private LocalBroadcastManager bManager;


    static final String TAG = "CNumberScale_Component";


    public CNumberScale_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CNumberScale_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CNumberScale_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.numberscale_layout, this);
        player = (CNumberScale_player) findViewById(R.id.numberplayer);

        Scontent = (ImageView) findViewById(R.id.backimage);
        addNumber = (TextView) findViewById(R.id.add);
        minusNumber = (TextView) findViewById(R.id.minus);
        displayNumber = (TextView) findViewById(R.id.display);
        player.setComponent(this);
        currentHit = 0;

    }

    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;

            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    public boolean dataExhausted() {
        return _dataIndex >= dataSource.length;
    }

    protected void updateDataSet(CNumberScale_Data data) {

        // first load dataset into fields
        loadDataSet(data);
        resetView();
        //updateStimulus();

    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CNumberScale_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout;

        countStart = data.dataset[0];
        delta = data.dataset[1];
        maxHit = data.dataset[2];

        min = countStart;
        max = min+delta*maxHit/2;
        currentNumber = countStart;

        player.loadData(min,max,delta,maxHit);

        Log.d(TCONST.COUNTING_DEBUG_LOG, "start=" + countStart +"delta"+delta+ ";index=" + _dataIndex);
    }

    /**
     * Resets the view for the next task.
     */
    protected void resetView() {
        String display = String.valueOf(currentNumber);
        String add = "+"+String.valueOf(delta);
        String minus = "-"+String.valueOf(delta);

        displayNumber.setText(display);
        addNumber.setText(add);
        minusNumber.setText(minus);


    }

    protected void updateView() {
        String display = String.valueOf(currentNumber);
        String add = "+"+String.valueOf(delta);
        String minus = "-"+String.valueOf(delta);

        displayNumber.setText(display);
        addNumber.setText(add);
        minusNumber.setText(minus);
        currentHit = 0;

    }

    public void update_current_hit(){
        currentHit+=1;
    }

    public int get_current_hit(){
        return currentHit;
    }

    public int get_max_hit(){
        return maxHit;
    }

    public int getCurrentNumber() {return currentNumber;}

    public int get_delta(){return delta;}

    public int get_min(){return min;}

    public int get_max(){return max;}


    public void add_delta(){
        currentNumber+=delta;
        updateView();
        update_current_hit();
    }

    public void minus_delta(){
        currentNumber-=delta;
        updateView();
        update_current_hit();

    }


    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {

            player.onTouchEvent(event);
            //handleClick();
        }

        return true;
    }

    //Overriden by child class

    public void playChime(){

    }
    /**
     * Point at a view
     */
    public void pointAtSomething() {
        /*View v = findViewById(R.id.hello);

        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);*/
    }


    /**
     * Updates the stimulus.
     */
    protected void updateStimulus() {

    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}

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
                    case NSCONST.PLAY_CHIME:
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
