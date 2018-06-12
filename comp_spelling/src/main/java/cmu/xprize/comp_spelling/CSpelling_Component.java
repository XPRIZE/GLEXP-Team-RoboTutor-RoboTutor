package cmu.xprize.comp_spelling;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import org.json.JSONObject;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CSpelling_Component extends RelativeLayout implements ILoadableObject {

    protected RelativeLayout Scontent;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected String word;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CSpelling_Data[] dataSource;


    // View Things
    protected Context mContext;

    private LocalBroadcastManager bManager;


    static final String TAG = "CSpelling_Component";


    public CSpelling_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CSpelling_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CSpelling_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.spelling_layout, this);

        Scontent = (RelativeLayout) findViewById(R.id.Scontent);

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

    protected void updateDataSet(CSpelling_Data data) {

        // first load word into fields
        loadDataSet(data);

        updateStimulus();

    }

    /**
     * Loads from current word into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CSpelling_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout;
        word = data.word;

    }

    /**
     * Resets the view for the next task.
     */
    protected void resetView() {


    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.hello);

        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
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

    public RelativeLayout getContainer() {
        return Scontent;
    }
}
