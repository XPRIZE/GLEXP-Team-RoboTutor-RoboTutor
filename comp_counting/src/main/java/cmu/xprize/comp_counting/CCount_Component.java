package cmu.xprize.comp_counting;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Created by kevindeland on 10/20/17.
 */

public class CCount_Component extends PercentRelativeLayout implements ILoadableObject {

    protected ICountMechanic _mechanics;
    protected PercentRelativeLayout Scontent;


    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    private int startingNumber;
    protected int countTarget;
    protected int delta;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CCount_Data[] dataSource;


    // View Things
    protected Context mContext;

    protected TableLayout dotTable;
    protected TableRow[] dotTableRows;
    protected ArrayList<CCount_Dot> dots;
    protected int numDotsCounted;

    private LocalBroadcastManager bManager;

    private ArrayList<Boolean> dotsTapped;


    static final String TAG = "CCount_Component";
    private CCount_DotBag dotBag;


    public CCount_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CCount_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CCount_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.counting_inc_dec_layout, this);

        Scontent = (PercentRelativeLayout) findViewById(R.id.Scontent);
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

    protected void updateDataSet(CCount_Data data) {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "updateDateSet");


        // first load dataset into fields
        loadDataSet(data);


        _mechanics = new CCount_Mechanics(mContext, this);
        _mechanics.populateView(data);

        // reset view
        // resetView();
        // update stimulus
        updateStimulus();
        updateInitialState();

        // then update visuals
        // addDots(countTarget);
    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CCount_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout; // NOV_1 make this consistent w/ Anthony
        startingNumber = data.dataset[0];
        countTarget = data.dataset[1]; // NOV_1 make this consistent w/ Anthony
        delta = data.dataset[2];
        Log.d(TCONST.COUNTING_DEBUG_LOG, "target=" + countTarget +";index=" + _dataIndex);
    }

    /**
     * Resets the view for the next task.
     */
    protected void resetView() {

        Log.d(TCONST.COUNTING_DEBUG_LOG, "resetting view");

        // first remove al  l views
        removeAllViews();



        switch(layout) {

            // NOV_1 clean this up
            case COUNT_CONST.DEFAULT_LAYOUT:
                Log.d(TCONST.COUNTING_DEBUG_LOG, "selected default");

                //PercentRelativeLayout.LayoutParams contextLayout = new PercentRelativeLayout.LayoutParams();
                TextView goalNumberView = new TextView(mContext);
                goalNumberView.setText(""+this.countTarget);
                addView(goalNumberView);

                // DONE add increment
                CCount_Increment incOne = new CCount_Increment(mContext);
                incOne.setUnits(1);
                CCount_Decrement decOne = new CCount_Decrement(mContext);
                decOne.setUnits(1);

                // TODO add dotbag container

                // create layout parameters
                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                // create table and rows
                dotTable = new TableLayout(mContext);
                dotTable.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                addView(dotTable);

                int numRows = rows, numCols = cols;
                dotTableRows = new TableRow[numRows];

                // make a new row for each new view
                for (int i = 0; i < numRows; i++) {
                    TableRow tableRow = new TableRow(mContext);
                    tableRow.setLayoutParams(tableParams);
                    dotTable.addView(tableRow, i);
                    dotTableRows[i] = tableRow;
                }

                // keep track?
                dotTableRows[0].addView(decOne, 0);
                dotTableRows[0].addView(incOne, 1);

                incOne.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        Log.d(TCONST.COUNTING_DEBUG_LOG, "Increment Tapped: " + numDotsCounted);
                        dotBag.addDot(0, numDotsCounted++);
                        //dotBag.setCols(numDotsCounted);

                        if(numDotsCounted == countTarget) {
                            applyBehavior(COUNT_CONST.DONE_COUNTING_TOUCH);
                        }

                        return false;
                    }
                });

                // create triangle
                // addTriangle();

                break;
        }
    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "pointing at something");
        TextView goalNumberView = _mechanics.getGoalNumberView();

        int[] screenCoord = new int[2];
        goalNumberView.getLocationOnScreen(screenCoord);

        PointF targetPoint = new PointF(screenCoord[0] + goalNumberView.getWidth()/2,
                screenCoord[1] + goalNumberView.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
    }


    /**
     * Updates the GoalNumber to have the stimulus.
     */
    protected void updateStimulus() {

    }

    /**
     * Updates the number of dots, etc to have correct starting state.
     */
    protected void updateInitialState() {

    }


    /*
    deprecated
    // TODO save this for when working with big dot bag
    private void addDots(int numDots) {

        Log.d(TCONST.COUNTING_DEBUG_LOG, "adding dots");

        // empty ArrayList
        dots = new ArrayList<>();

        // create layout parameters
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        // create images for circle or empty
        int circleImage = mContext.getResources().getIdentifier("circle", "drawable", mContext.getPackageName());
        Drawable cImage = ContextCompat.getDrawable(mContext, circleImage);
        int emptyImage = mContext.getResources().getIdentifier("empty", "drawable", mContext.getPackageName());
        Drawable eImage = ContextCompat.getDrawable(mContext, emptyImage);

        int numRows = rows, numCols = cols;

        for (int i = 0;i < numRows; i++) {

            for (int j = 0; j < numCols; j++) {
                CCount_Dot dot = new CCount_Dot(mContext);
                dot.setLayoutParams(rowParams);

                // only add enough circles for the number of problem
                if (dots.size() < numDots) {
                    dot.setImageName("circle");
                }
                else {
                    dot.setImageName("empty");
                    dot.setIsClickable(false); // empty can't be clickable
                }

                dots.add(dot);

                dotTableRows[i].addView(dot); // 2 after?
            }
        }
    }
    */


    /*
    private void addTriangle() {

        Log.d(TCONST.COUNTING_DEBUG_LOG, "adding triangle");

        triangle = new ImageView(mContext);
        RelativeLayout.LayoutParams triangleLayout = new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        triangleLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        triangleLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        triangle.setLayoutParams(triangleLayout);

        int imageResource = mContext.getResources().getIdentifier("triangle", "drawable", mContext.getPackageName());
        Drawable image = ContextCompat.getDrawable(mContext, imageResource);
        triangle.setImageDrawable(image);

        triangle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TCONST.COUNTING_DEBUG_LOG, "Triangle Tapped!!!");


                // if we've tapped all dots, continue to next
                if(numDotsCounted == countTarget) {
                    applyBehavior(COUNT_CONST.DONE_COUNTING_TOUCH);

                }

                return false;
            }
        });

        addView(triangle);
    }
    */


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            //handleClick();
        }

        return true;
    }

    /* not needed at the moment
    // TODO save for big dot bag
    protected void handleClick() {
        Log.d(TCONST.COUNTING_DEBUG_LOG, "Handling click");

        CCount_Dot clickedDot = null;

        for (int i = 0; i < dots.size(); i++) {
            if (dots.get(i).getIsClicked()) {
                clickedDot = dots.get(i);
            }
        }

        if(clickedDot != null) {
            // do after-click cleanup
            clickedDot.setIsClickable(false);
            clickedDot.setHollow(true);
            clickedDot.setIsClicked(false);

            //publishFeature(TCONST.GENERIC_RIGHT);

            numDotsCounted++;
        }

    }
    */

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

    public PercentRelativeLayout getContainer() {
        return Scontent;
    }
}
