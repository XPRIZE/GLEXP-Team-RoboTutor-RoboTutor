package cmu.xprize.comp_bigmath;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
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

public class CBigMath_Component extends RelativeLayout implements ILoadableObject {

    protected RelativeLayout Scontent;

    private BigMathMechanic _mechanic;
    private BigMathLayoutHelper _layout;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;

    protected  int _numDigits;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CBigMath_Data[] dataSource;


    // CONST
    // name of digit variables
    private static final String ONE_DIGIT = "one";
    private static final String TEN_DIGIT = "ten";
    private static final String HUN_DIGIT = "hun";

    // CONST name of row variables
    private static final String OPA_LOCATION = "opA";
    private static final String OPB_LOCATION = "opB";
    private static final String RESULT_LOCATION = "result";


    // View Things
    protected Context mContext;

    private LocalBroadcastManager bManager;


    static final String TAG = "CBigMath_Component";


    public CBigMath_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CBigMath_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CBigMath_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        // inflate(getContext(), R.layout.bigmath_layout, this);

        Scontent = (RelativeLayout) findViewById(R.id.Scontent);

        _mechanic = new BigMathMechanic(getContext(), this);

    }

    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;

            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, true);
        }

    }

    public boolean dataExhausted() {
        return _dataIndex >= dataSource.length;
    }

    protected void updateDataSet(CBigMath_Data data) {

        // first load dataset into fields
        loadDataSet(data);

        _mechanic.setData(data);
        _mechanic.doAllTheThings();

        // ROBO_MATH continue here... do the thing (See "Step 1")

        // ROBO_MATH... import "BigMathLayoutHelper"

    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CBigMath_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout;
        dataset = data.dataset;

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
    protected void loadLayout() {
        // ROBO_MATH this should do a lot of the things in BigMath

        int[] layouts = {R.layout.bigmath_1d, R.layout.bigmath_2d, R.layout.bigmath_3d};
        int layoutId = layouts[_numDigits - 1];
        inflate(getContext(), layoutId, this);


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
