package cmu.xprize.comp_picmatch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */
public class CPicMatch_Component extends RelativeLayout implements ILoadableObject {

    static final String TAG = "CPicMatch_Component";

    // views
    protected ConstraintLayout Scontent;
    protected TextView promptView;
    protected ImageView[] optionViews;

    // DataSource Variables
    protected int _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected String prompt;
    protected String[] images;

    protected int attempts = 0;
    protected static final int NUM_PROBLEMS = 10;

    // json loadable
    public String bootFeatures;
    public CPicMatch_Data[] dataSource;
    protected List<CPicMatch_Data> _data;

    // View Things
    protected Context context;

    private LocalBroadcastManager bManager;

    public CPicMatch_Component(Context context) {
        super(context);
        init(context, null);
    }


    public CPicMatch_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CPicMatch_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        this.context = context;

        inflate(getContext(), R.layout.picmatch_layout_2, this);

        Scontent = findViewById(R.id.Scontent);

        promptView = findViewById(R.id.prompt);

        optionViews = new ImageView[4];
        optionViews[0] = findViewById(R.id.option_1);
        optionViews[0].setVisibility(View.INVISIBLE);
        optionViews[1] = findViewById(R.id.option_2);
        optionViews[1].setVisibility(View.INVISIBLE);
        optionViews[2] = findViewById(R.id.option_3);
        optionViews[2].setVisibility(View.INVISIBLE);
        optionViews[3] = findViewById(R.id.option_4);
        optionViews[3].setVisibility(View.INVISIBLE);

    }

    // ALAN_HILL (2) this is called by animator_graph
    public void next() {

        try {
            if (_data != null) {
                updateDataSet(_data.get(_dataIndex));

                _dataIndex++;

            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }

    public boolean isDataExhausted() {
        return _dataIndex >= _data.size();
    }

    protected void updateDataSet(CPicMatch_Data data) {
        // first load dataset into fields
        loadDataSet(data);

        updateStimulus();
    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CPicMatch_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout;
        prompt = data.prompt;
        images = data.images;
        attempts = 0;
    }

    /**
     * Resets the view for the next task.
     */
    protected void resetView() {
        // TODO: complete this method?
    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.prompt);

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
        promptView.setText(prompt);

        for (int i = 0; i < optionViews.length; i++) {

            try {
                ImageLoader.makeBitmapLoader()
                        .loadBitmap(images[i])
                        .into(optionViews[i]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*ImageLoader.with(this.context)
                    .loadDrawable(images[i])
                    .into(optionViews[i]);
                    */

            optionViews[i].setVisibility(View.VISIBLE);
            optionViews[i].setOnClickListener(new StudentChoiceListener(i));
        }
    }

    private int unpressIndex = 0; // for some reason it only lets me change one background opacity at a time...

    class StudentChoiceListener implements View.OnClickListener {

        int _index;
        StudentChoiceListener(int index) {
            this._index = index;
        }

        @Override
        public void onClick(View view) {
            attempts++;
            retractFeature("FTR_CORRECT");
            retractFeature("FTR_WRONG");

            unpressIndex = _index;
            // press every incorrect one optionViews[_index].getBackground().setAlpha(255);
            Log.wtf("UNPRESS", "pressing: " + optionViews[_index].getResources().getResourceName(optionViews[_index].getId()));

            if(prompt.equals(images[_index])) {
                publishFeature("FTR_CORRECT"); // ALAN_HILL (3) search animator graph for this term
                optionViews[_index].setBackgroundColor(Color.parseColor(PM_CONST.HOLO_GREEN));
                trackAndLogPerformance(true, prompt);
            } else {
                publishFeature("FTR_WRONG"); // ALAN_HILL (3) search animator graph for this term
                trackAndLogPerformance(false, images[_index]);
                optionViews[_index].setBackgroundColor(Color.parseColor(PM_CONST.HOLO_ORANGE));
            }


            applyBehavior("STUDENT_CHOICE_EVENT"); // ALAN_HILL (3) search animator graph for this term
        }
    }

    /**
     * reset the image to unpressed state
     */
    public void resetImages() {
        // try only unpressing one???
        //optionViews[unpressIndex].getBackground().setAlpha(63);

        Log.wtf("UNPRESS", "unpressing images");
        for (ImageView optionView : optionViews) {
            Log.wtf("UNPRESS", "unpressing: " + optionView.getResources().getResourceName(optionView.getId()));
            optionView.setBackgroundColor(Color.parseColor(PM_CONST.HOLO_PURPLE));
            optionView.getBackground().setAlpha(63); // wtf... why is this not working for all option views?
        }
    }

    // Must override in TClass
    protected void trackAndLogPerformance(boolean correct, String choice) {

    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}

    // Overridden in TClass
    public void publishFeature(String feature) {}

    // Overridden in TClass
    public void retractFeature(String feature) {}

    /**
     * Load the data source
     *
     * @param jsonData the jason objects to be parsed and loaded
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {


        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);

        ArrayList<CPicMatch_Data> dataset = new ArrayList<>(Arrays.asList(dataSource));
        _data = new ArrayList<>();
        Collections.shuffle(dataset);
        _data = dataset.subList(0, NUM_PROBLEMS);
        _dataIndex = 0;
    }

    public ConstraintLayout getContainer() {
        return Scontent;
    }
}
