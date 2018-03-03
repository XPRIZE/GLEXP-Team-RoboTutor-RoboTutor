package cmu.xprize.comp_numberscale;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
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

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CNumberScale_Component extends RelativeLayout implements ILoadableObject {

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
    private int currentNumber;
    protected ImageView reset;

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
        reset = (ImageView) findViewById(R.id.reset);
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
        dataset = data.dataset;

        countStart = data.dataset[0];
        currentNumber = countStart;
        delta = data.dataset[1];
        maxHit = data.dataset[2];


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
    public void reset_current_number(){
        currentNumber = countStart;
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


}
