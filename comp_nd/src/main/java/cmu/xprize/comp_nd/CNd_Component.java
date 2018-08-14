package cmu.xprize.comp_nd;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.Locale;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_nd.ui.CNd_LayoutManager_BaseTen;
import cmu.xprize.comp_nd.ui.CNd_LayoutManagerInterface;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CNd_Component extends RelativeLayout implements ILoadableObject {


    // ND_SCAFFOLD_TODO
    // (1) modify DS √√√
    // (2) modify CNd_Data √√√
    // (3) if (isWE)... ag? publishFeature? updateStimulus?
    // (4) goThruScaffolding()... use placeholder for now
    // (5) lockScreen() √√√ prevent user from tapping
    // (6) cycleThru()... highlightColumn, sayAudio... should be in AG
    // (7) indicateCorrect()... see below
    // (8) perform on incorrect answer (not today)

    // NEXT: IMPORT
    // - layout √√√
    // - highlightUnits... (trace through and make it work)

    // AUDIO:
        // PROMPTS:
            // "first compare the hundreds"
            // "compare the tens"
            // "compare the ones"
            // "this number has more"
            // "they have the same"
        // PROCEDURE:
            // record audio
            // make new folder for ND_CONST.
            // push into place using RTAsset_Publisher



    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;
    protected boolean isWorkedExample;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CNd_Data[] dataSource;


    // View Things
    protected Context mContext;

    // so that UI can be changed w/o changing behavior model
    protected CNd_LayoutManagerInterface _layoutManager;

    // TUTOR STATE
    protected String _correctChoice; // can be "left" or "right"

    // might need?
    private LocalBroadcastManager bManager;


    static final String TAG = "CNd_Component";


    public CNd_Component(Context context) {
        super(context);
        init(context, null);
    }
    public CNd_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CNd_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        _layoutManager = new CNd_LayoutManager_BaseTen(this, context);

        _layoutManager.initialize();
        _layoutManager.resetView();

    }

    /**
     * called by AG
     */
    public void next() {

        Log.wtf(TAG, "NEXT_NEXT_NEXT");

        retractFeature(ND_CONST.FTR_CORRECT);
        retractFeature(ND_CONST.FTR_WRONG);

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

    private void updateDataSet(CNd_Data data) {

        // first load dataset into fields
        loadDataSet(data);

        // for now, can only be left or right
        if (dataset[0] > dataset[1]) {
            _correctChoice = "left";
        } else if (dataset[0] < dataset[1]) {
            _correctChoice = "right";
        }

    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    private void loadDataSet(CNd_Data data) {
        level = data.level;
        task = data.task;
        layout = data.layout;
        dataset = data.dataset;
        isWorkedExample = data.isWorkedExample;

    }

    /**
     * Point at a view
     * ND_SCAFFOLD_TODO (7) point at the left or right guy
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.num_discrim_layout); // left ? left : right;


        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
    }

    /**
     * Called by AG: Updates the stimulus.
     */
    public void updateStimulus() {

        setVisibility(VISIBLE);

        _layoutManager.displayDigits(dataset[0], dataset[1]);
        _layoutManager.displayConcreteRepresentations(dataset[0], dataset[1]);

        Log.wtf("CHOOSE_ME", "y u no work?");
        _layoutManager.enableChooseNumber(true);

    }


    // (5) prevent user
    public void lockUserInput() {
        _layoutManager.enableChooseNumber(false);
    }

    /**
     * ND_SCAFFOLD repeat for each column
     * ND_SCAFFOLD highlight/lowlight the digit and things
     * ND_SCAFFOLD disable clicking
     * ND_SCAFFOLD when finished, postEvent to go to next node
     */
    public void highlightOnesColumn() {

    }



    /**
     * Called by LayoutManager
     * Can be left or right
     *
     * @param studentChoice can be "left" or "right".
     */
    public void registerStudentChoice(String studentChoice) {

        Log.d(TAG, String.format(Locale.US, "The student chose the number on the %s. The correct answer is on the %s", studentChoice, _correctChoice));

        if (studentChoice.equals(_correctChoice)) {

            Log.d(TAG, "CORRECT!");
            publishFeature(ND_CONST.FTR_CORRECT);
        } else {

            Log.d(TAG, "WRONG!");
            publishFeature(ND_CONST.FTR_WRONG);
        }

    }

    /**
     * Called by AG
     */
    public void doTheWrongThing() {
        Log.d(TAG, "Doing the wrong thing");
        applyBehaviorNode(ND_CONST.NEXTNODE);
    }

    /**
     * Called by AG
     */
    public void doTheRightThing() {
        Log.d(TAG, "Doing the right thing");
        applyBehaviorNode(ND_CONST.NEXTNODE);
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void applyBehaviorNode(String event) { }

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishFeature(String feature) { }

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void retractFeature(String feature) {}

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
