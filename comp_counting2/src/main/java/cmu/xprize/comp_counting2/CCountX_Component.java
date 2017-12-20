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
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Created by kevindeland on 10/20/17.
 */

public class CCountX_Component extends PercentRelativeLayout implements ILoadableObject {

    // Infrastructure
    protected final Handler mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    // Layout
    private RelativeLayout Scontent;
    private CCountX_SurfaceView surfaceView;
    private TextView counterText;

    // DataSource Variables
    protected int _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    private int startingNumber;
    protected int countStart;
    protected int countTarget;
    protected int currentCount;
    protected int delta;


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


    static final String TAG = "CCount_Component";

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


        bManager = LocalBroadcastManager.getInstance(getContext());

    }

    public void next() {


        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
                numDotsCounted = 0;
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    public boolean dataExhausted() {
        return _dataIndex >= dataSource.length;
    }

    protected void updateDataSet(CCountX_Data data) {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "updateDateSet");


        // first load dataset into fields
        loadDataSet(data);


        // reset view
        resetView();
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
        level = data.level;
        task = data.task;
        layout = data.layout; // NOV_1 make this consistent w/ Anthony
        countStart = data.dataset[0];
        countTarget = data.dataset[1];

        Log.d(TCONST.COUNTING_DEBUG_LOG, "target=" + countTarget + ";index=" + _dataIndex);
    }

    /**
     * Reset the view
     */
    protected void resetView() {

        // reset the TextView
        String initialCount = String.valueOf(countStart);
        counterText.setText(initialCount);

        // reset the surfaceView
        surfaceView.clearObjectsToNumber(countStart);
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

    /**
     * Overridden by child class.
     */
    public void playChime() {

    }

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