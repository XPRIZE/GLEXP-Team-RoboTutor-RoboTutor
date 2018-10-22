package cmu.xprize.comp_bigmath;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPerformanceTracker;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_bigmath.BM_CONST.ALL_DIGITS;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_ON_DIGIT_HUN;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_ON_DIGIT_ONE;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_ON_DIGIT_TEN;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_TAP_CONCRETE;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_WRITE_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_DIGIT;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CBigMath_Component extends RelativeLayout implements ILoadableObject, IBehaviorManager, IPublisher, IHesitationManager, IPerformanceTracker {

    protected final Handler mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap           queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    protected BigMathMechanic _mechanic;
    private BigMathLayoutHelper _layout;
    private BigMathProblemState _problemState;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;
    protected String operation;

    protected  int _numDigits;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CBigMath_Data[] dataSource;
    protected CBigMath_Data currentData;


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

        // force write input to a digit
        CRecognizerPlus.getInstance().setClassBoost(GCONST.FORCE_DIGIT);

        bManager = LocalBroadcastManager.getInstance(getContext());

        // initialize mechanic
        _mechanic = new BigMathMechanic(getContext(), this, this, this, this, this);
        _layout = new BigMathLayoutHelper(getContext(), this);
    }

    public void onDestroy() {
        terminateQueue();
    }

    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }

    public void next() {

        Log.wtf("SEPTEMBER", "index = " + _dataIndex);
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

        currentData = data;

        // first load dataset into fields
        loadDataSet(data);

        // features are published inside setData() {... _sal.setData() }
        _mechanic.setData(data);
        _problemState = _mechanic.getProblemState();
        _mechanic.doAllTheThings();
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
        operation = data.operation;
    }

    /**
     * Resets the view for the next task.
     */
    protected void resetView() {


    }


    public void nextDigit() {

        _mechanic.highlightDigitColumn(_mechanic._currentDigit);
        _mechanic.disableConcreteUnitTappingForOtherRows(_mechanic._currentDigit); // MATH_MISC (1) don't allow tapping of other ghost dots

        // publish the right features!!! to play audio!!!
        switch( _mechanic._currentDigit) {
            case ONE_DIGIT:
                //hasUnitsToTap = getOnesDigit(dataset[0]) > 0 && getOnesDigit(dataset[1]) > 0;
                publishFeature(FTR_ON_DIGIT_ONE);
                retractFeature(FTR_ON_DIGIT_TEN);
                retractFeature(FTR_ON_DIGIT_HUN);
                break;

            case TEN_DIGIT:
                //hasUnitsToTap = getTensDigit(dataset[0]) > 0 && getTensDigit(dataset[1]) > 0;
                retractFeature(FTR_ON_DIGIT_ONE);
                publishFeature(FTR_ON_DIGIT_TEN);
                retractFeature(FTR_ON_DIGIT_HUN);
                break;

            case HUN_DIGIT:
                //hasUnitsToTap = getHunsDigit(dataset[0]) > 0 && getHunsDigit(dataset[1]) > 0;
                retractFeature(FTR_ON_DIGIT_ONE);
                retractFeature(FTR_ON_DIGIT_TEN);
                publishFeature(FTR_ON_DIGIT_HUN);
                break;
        }

        boolean hasUnitsToTap = checkIfHasUnitsToTap();

        // MATH_HESITATE if there are concrete units to tap, indicate that we should be tapping
        if (hasUnitsToTap) {
            publishFeature(FTR_TAP_CONCRETE);
            retractFeature(FTR_WRITE_DIGIT);
        } else {
            retractFeature(FTR_TAP_CONCRETE);
            publishFeature(FTR_WRITE_DIGIT);
        }

    }

    private boolean checkIfHasUnitsToTap() {

        Log.d(TAG, "checkIfHasUnitsToTap: Checking has units");
        // if addition
        // check digit ANZ, BNZ
        if (operation.equals("+") &&
                _mechanic._currentDigit.equals(ONE_DIGIT) &&
                _problemState.getCurrentOpAOne() > 0 &&
                _problemState.getCurrentOpBOne() > 0) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has + one units left to tap");
            return true;
        }

        if (operation.equals("+") &&
                _mechanic._currentDigit.equals(TEN_DIGIT) &&
                _problemState.getCurrentOpATen() > 0 &&
                _problemState.getCurrentOpBTen() > 0) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has + ten units left to tap");
            return true;
        }

        if (operation.equals("+") &&
                _mechanic._currentDigit.equals(HUN_DIGIT) &&
                _problemState.getCurrentOpAHun() > 0 &&
                _problemState.getCurrentOpBHun() > 0) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has + hun units left to tap");
            return true;
        }


        // if subtraction
        // check ANZ, BNE

        if (operation.equals("-") &&
                _mechanic._currentDigit.equals(ONE_DIGIT) &&
                _problemState.getSubtrahendOne() < getOnesDigit(_problemState.getData().dataset[1]) ) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has - one units left to tap");
            return true;
        }

        if (operation.equals("-") &&
                _mechanic._currentDigit.equals(TEN_DIGIT) &&
                _problemState.getSubtrahendTen() < getTensDigit(_problemState.getData().dataset[1]) ) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has - ten units left to tap");
            return true;
        }

        if (operation.equals("-") &&
                _mechanic._currentDigit.equals(HUN_DIGIT) &&
                _problemState.getSubtrahendTen() < getHunsDigit(_problemState.getData().dataset[1]) ) {
            Log.d(TAG, "checkIfHasUnitsToTap: Still has - hun units left to tap");
            return true;
        }


        return false;
    }

    /**
     * highlight all digits!
     */
    public void highlightAll() {
        _mechanic.highlightDigitColumn(ALL_DIGITS);
    }

    /**
     * Point at Dot Container
     * @param numLoc
     * @param digit
     */
    public void pointAtDotContainer(String numLoc, String digit) {
        View container = _layout.getContainingBox(numLoc, digit);
        pointAtView(container);
    }

    public void pointAtDigitInput(String numLoc, String digit) {
        TextView view = _layout.getBaseTenDigitView(numLoc, digit);
        pointAtView(view);
    }


    /**
     * Use RoboFinger to point at the center of the view passed.
     * @param v
     */
    private void pointAtView(View v) {
        int[] screenCoord = {0, 0};
        // findViewById(R.id.baseten_layout).getLocationOnScreen(screenCoord); // reference from the parent container

        v.getLocationOnScreen(screenCoord);
        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        Log.wtf("POINTING", "" + targetPoint.x + ", " + targetPoint.y);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
    }


    /**
     * Updates the stimulus.
     */
    protected void loadLayout() {

        int[] layouts = {R.layout.bigmath_1d, R.layout.bigmath_2d, R.layout.bigmath_3d};
        int layoutId = layouts[_numDigits - 1];
        inflate(getContext(), layoutId, this);


    }

    /**
     * Trigger the hesitation prompt. This will play the map "INPUT_HESITATION_FEEDBACK" after 5 seconds.
     */
    public void triggerHesitationTimer() {

        // MATH_HESITATE
        // MATH_HESITATE (1) how do we know when the step can move forward?
        // COL = which column are we on?
        // DOT = are there dots left?

        // MATH_HESITATE (next)
        // IF (COL==1, DOT_A) => {tap on the opA one dots}
        // IF (COL==1, !DOT_A, DOT_B) => {tap on the opB one dots}
        // IF (COL==1, !DOT_A, !DOT_B) => {write in the one box}

        // ON (write ONE) ==> (cancel hesitation)

        // IF (COL==10, DOT_A) => {tap on the ten dots}
        // IF (COL==10, !DOT_A, DOB_B) => {tap on the opb ten dots}
        // IF (COL==10, !DOT_A, !DOT_B) => {write in the ten box}

        // ON (write TEN) ==> (cancel hesitation)


        // MATH_HESITATE if there are concrete units to tap, indicate that we should be tapping
        if (checkIfHasUnitsToTap()) {
            publishFeature(FTR_TAP_CONCRETE);
            retractFeature(FTR_WRITE_DIGIT);
        } else {
            retractFeature(FTR_TAP_CONCRETE);
            publishFeature(FTR_WRITE_DIGIT);
        }
        postNamed("HESITATION_PROMPT", "APPLY_BEHAVIOR", "INPUT_HESITATION_FEEDBACK", (long)5000);
    }

    public void postNamed(String name, String command, String target, Long delay) {
        enQueue(new Queue(name, command, target), delay);
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

    public void cancelHesitationTimer() {
        cancelPost("HESITATION_PROMPT");
    }

    public void cancelPost(String name) {

        Log.d(TAG, "Cancel Post Requested: " + name);

        while(nameMap.containsKey(name)) {

            Log.d(TAG, "Post Cancelled: " + name);

            mainHandler.removeCallbacks((Queue) (nameMap.get(name)));
            nameMap.remove(name);
        }
    }

    @Override
    public void triggerHesitation() {
        postNamed("HESITATION_PROMPT", "APPLY_BEHAVIOR", "INPUT_HESITATION_FEEDBACK", (long)5000);
    }

    @Override
    public void cancelHesitation() {

        cancelHesitationTimer();
    }

    // override in TClass
    @Override
    public void trackAndLogPerformance(boolean correct, Object expected, Object actual) {

    }

    public class Queue implements Runnable {

        protected String _name;
        protected String _command;
        protected String _target;
        protected String _item;

        public Queue(String name, String command) {

            _name = name;
            _command = command;

            if (name != null) {
                nameMap.put(name, this);
            }
        }

        public Queue(String name, String command, String target) {

            this(name, command);
            _target = target;
        }

        @Override
        public void run() {

            try {
                if (_name != null) {
                    nameMap.remove(_name);
                }

                queueMap.remove(this);

                switch (_command) {

                    case TCONST.APPLY_BEHAVIOR:

                        applyBehaviorNode(_target);
                        break;

                    default:
                        break;
                }


            } catch (Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, true);
            }
        }
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public boolean applyBehavior(String event){ return false;}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void applyBehaviorNode(String event) { }

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void setVolatileBehavior(String event, String behavior) {}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void setStickyBehavior(String event, String behavior) {}


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


    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishState() {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishValue(String varName, String value) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishValue(String varName, int value) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishFeatureSet(String featureset) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void retractFeatureSet(String featureset) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishFeature(String feature) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void retractFeature(String feature) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void publishFeatureMap(HashMap featureMap) {}
    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    @Override
    public void retractFeatureMap(HashMap featureMap) {}
}
