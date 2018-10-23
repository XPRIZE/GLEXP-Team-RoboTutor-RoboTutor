package cmu.xprize.comp_nd;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_nd.ui.CNd_LayoutManager_BaseTen;
import cmu.xprize.comp_nd.ui.CNd_LayoutManagerInterface;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_ONES;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_TENS;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_INDICATE_CORRECT;
import static cmu.xprize.comp_nd.ND_CONST.FTR_ONE_FIRST;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_NA_ONES;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_NA_TENS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_ONES;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_TENS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_TEN_FIRST;
import static cmu.xprize.comp_nd.ND_CONST.HESITATION_PROMPT;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_ONES;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_TENS;
import static cmu.xprize.comp_nd.ND_CONST.HUN_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.INDICATE_CORRECT;
import static cmu.xprize.comp_nd.ND_CONST.INPUT_HESITATION_FEEDBACK;
import static cmu.xprize.comp_nd.ND_CONST.NO_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.ONE_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.TEN_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_COMPARE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_LESS;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_MORE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_HUN;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_ONE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_TEN;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;
import static cmu.xprize.util.TCONST.DEBUG_HESITATE;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CNd_Component extends RelativeLayout implements ILoadableObject {


    // ND_SCAFFOLD √√√ BEHAVIOR
    // (3) if (isWE), perform Scaffolding // put it right in updateStimulus (probably) NEXT NEXT NEXT
    // (8) when incorrect answer, perform Scaffolding // put it right in xyz
    // when incorrect answer, don't perform the final step of telling them which number

    // ND_SCAFFOLD √√√ PROMPTS
    // AUDIO:
        // PROMPTS: see ~/RoboTutor/ProtoAssets/ProtoAssets_ND/assets/audio/sw/cmu/xprize/proto_nd

    // Let's compare numbers √√√
    // Which number is bigger? √√√
    // Correct! √√√
    // Try again. √√√
    // so tap on this number √√√
    // is the same as √√√
    // is more than √√√
    // "so compare these numbers"



            // "first compare the hundreds"
            // "compare the tens"
            // "compare the ones"
            // "this number has more"
            // "they have the same"
        // PROCEDURE:
            // record audio
            // make new folder for ND_CONST...
            // record numdiscr in "tutor_descriptor.json"
            // push into place using RTAsset_Publisher


    // ND_SCAFFOLD PROMPTS 2
    // Tap on the bigger number
    // "so compare tens"
    // "so X is bigger than Y"
    // "so this number is bigger"
    // "now say this number"
    //

    // Queue Things
    protected final Handler mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap           queueMap     = new HashMap();
    protected HashMap nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;
    //

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;
    protected boolean isWorkedExample;

    // these are hard-coded for now
    protected boolean random = true; // set all to random for now...
    public int questionCount = 10;

    // gets set w/ data source variables
    protected int numDigits;


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

    // Needed for sending broadcasts to RoboFinger
    private LocalBroadcastManager _bManager;


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

        _bManager = LocalBroadcastManager.getInstance(getContext());

        _layoutManager = new CNd_LayoutManager_BaseTen(this, context);

        _layoutManager.initialize();
        _layoutManager.resetView();

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

    /**
     * called by AG
     */
    public void next() {

        Log.wtf(TAG, "NEXT_NEXT_NEXT");

        retractFeature(ND_CONST.FTR_CORRECT);
        retractFeature(ND_CONST.FTR_WRONG);

        try {
            if (dataSource != null) {
                if (!random) {
                    updateDataSet(dataSource[_dataIndex]);
                } else {
                    int nextIndex = (new Random()).nextInt(dataSource.length);
                    updateDataSet(dataSource[nextIndex]);

                    boolean replace = true;
                    if (!replace) {
                        ArrayList<CNd_Data> newDataSource = new ArrayList<CNd_Data>();
                        for (int i=0; i < dataSource.length; i++) {
                            if (i != nextIndex) {
                                newDataSource.add(dataSource[i]);
                            }
                        }
                        dataSource = (CNd_Data[]) newDataSource.toArray();
                    }
                }

                _dataIndex++;

            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    /**
     * Return whether data source has run out.
     * @return whether data source has run out.
     */
    public boolean dataExhausted() {
        return random ?
                _dataIndex >= questionCount :
                _dataIndex >= dataSource.length;
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
        try {
            level = data.level;
            task = data.task;
            layout = data.layout;
            dataset = data.dataset;
            isWorkedExample = _dataIndex == 0; //data.isWorkedExample; // begin with example

            // get numDigits
            numDigits = dataset[0] > dataset[1] ? String.valueOf(dataset[0]).length() : String.valueOf(dataset[1]).length();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * points at the correct digit
     */
    public void pointAtCorrectDigit() {
        // point at the left or right guy

        Log.wtf("THIS_IS_A_TEST", "pointing to " + _correctChoice);

        View digitView;
        if (_correctChoice.equals("left")) {
            digitView = findViewById(R.id.symbol_left_num);
        } else {
            digitView = findViewById(R.id.symbol_right_num);
        }

        Log.wtf("THIS_IS_A_TEST", "pointing to " + digitView);

        int[] screenCoord = new int[2];
        digitView.getLocationOnScreen(screenCoord);

        PointF targetPoint = new PointF(screenCoord[0] + digitView.getWidth()/2,
                screenCoord[1] + digitView.getHeight());

        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);

    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.num_discrim_layout); // left ? left : right;


        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);
    }

    /**
     * Called by AG: Updates the stimulus.
     */
    public void updateStimulus() {

        try {

            setVisibility(VISIBLE);

            _layoutManager.displayDigits(dataset[0], dataset[1]);
            _layoutManager.displayConcreteRepresentations(dataset[0], dataset[1]);

            _layoutManager.enableChooseNumber(true);

            publishNumberNameAudio();

            // - twenties
            // - ones

            // This controls for which digit we will say "first"
            if (numDigits == 1) {
                publishFeature(FTR_ONE_FIRST);
                retractFeature(FTR_TEN_FIRST);
            } else if (numDigits == 2) {
                retractFeature(FTR_ONE_FIRST);
                publishFeature(FTR_TEN_FIRST);
            } else if (numDigits == 3) {
                retractFeature(FTR_ONE_FIRST);
                retractFeature(FTR_TEN_FIRST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Send features and values to the animator graph, so it knows how to say the number name audio
     */
    private void publishNumberNameAudio() {

        int correctAnswer = dataset[0] > dataset[1] ? dataset[0] : dataset[1];

        publishValue(VALUE_HUN, String.valueOf(getHunsDigit(correctAnswer) * 100));
        publishValue(VALUE_TEN, String.valueOf(getTensDigit(correctAnswer) * 10));
        publishValue(VALUE_ONE, String.valueOf(getOnesDigit(correctAnswer)));

        // determines how we know which digits to say
        boolean nzH = getHunsDigit(correctAnswer) != 0;
        boolean nzT = getTensDigit(correctAnswer) != 0;
        boolean nzO = getOnesDigit(correctAnswer) != 0;
        // play Huns digit if... (Huns n/e 0)
        // play na ten if.... (Huns n/e 0) and (Tens n/e 0)
        // play Tens digit if... (Tens n/e 0)
        // play na ones if ... (Huns n/e 0 or Tens n/e 0) and (Ones n/e 0)
        // play Ones digit if... (Ones n/e 0)
        publishOrRetractFeature(FTR_SAY_HUNS, nzH);
        publishOrRetractFeature(FTR_SAY_NA_TENS, nzH && nzT);
        publishOrRetractFeature(FTR_SAY_TENS, nzT);
        publishOrRetractFeature(FTR_SAY_NA_ONES, (nzH || nzT) && nzO);
        publishOrRetractFeature(FTR_SAY_ONES, nzO);
    }

    /**
     * if it's a worked example, do the scaffolding. Otherwise, set the hesitation feedback.
     */
    public void playWorkedExampleOrSetHesitation() {
        // begin with example
        if(isWorkedExample) {
            applyBehaviorNode(getStartingHighlightByNumDigits()); // only highlight the necessary digits!!!
        } else {
            triggerHesitationFeedback();
        }
    }

    public void triggerHesitationFeedback() {
        postNamed(HESITATION_PROMPT, TCONST.APPLY_BEHAVIOR, INPUT_HESITATION_FEEDBACK, (long) ND_CONST.HESITATION_DELAY);
    }

    /**
     * What is the first QueueMap item to be called?
     * @return one of {HIGHLIGHT_ONES, HIGHLIGHT_TENS, HIGHLIGHT_HUNS}
     */
    private String getStartingHighlightByNumDigits() {
        String[] numDigitsToBehavior = {null, HIGHLIGHT_ONES, HIGHLIGHT_TENS, HIGHLIGHT_HUNS};

        return numDigitsToBehavior[numDigits];
    }


    // (5) prevent user
    // deprecated -- don't lock user input anymore
    public void lockUserInput() {
        _layoutManager.enableChooseNumber(false);
    }

    public void enableUserInput() {
        _layoutManager.enableChooseNumber(true);
    }


    private String _currentHighlightDigit = null;

    /**
     * highlight digit and concretes in huns column
     */
    public void highlightHunsColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight huns at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(HUN_DIGIT);
        _currentHighlightDigit = HUN_DIGIT;
        publishDigitAudioValues(HUN_DIGIT);
    }


    /**
     * highlight digit and concretes in tens column
     */
    public void highlightTensColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight tens at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(TEN_DIGIT);
        _currentHighlightDigit = TEN_DIGIT;
        publishDigitAudioValues(TEN_DIGIT);
    }

    /**
     * highlight digit and concretes in ones column
     */
    public void highlightOnesColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight ones at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(ONE_DIGIT);
        _currentHighlightDigit = ONE_DIGIT;
        publishDigitAudioValues(ONE_DIGIT);
    }

    private void publishDigitAudioValues(String digit) {

        int digitLeft = 0, digitRight = 0;
        switch(digit) {
            case HUN_DIGIT:
                digitLeft = getHunsDigit(dataset[0]);
                digitRight = getHunsDigit(dataset[1]);
                break;

            case TEN_DIGIT:
                digitLeft = getTensDigit(dataset[0]);
                digitRight = getTensDigit(dataset[1]);
                break;

            case ONE_DIGIT:
                digitLeft = getOnesDigit(dataset[0]);
                digitRight = getOnesDigit(dataset[1]);
                break;
        }

        publishValue(VALUE_DIGIT_MORE,
                String.valueOf(digitLeft > digitRight ?
                        digitLeft : digitRight));

        publishValue(VALUE_DIGIT_COMPARE, digitLeft == digitRight ?
                //"is equal to" : "is greater than");
                "is the same as" : "is bigger than"); // temporary placeholder

        publishValue(VALUE_DIGIT_LESS,
                String.valueOf(digitLeft > digitRight ?
                        digitRight: digitLeft));
    }

    /**
     * decide whether to highlight next digit, or indicate the correct answer
     */
    public void highlightNextScaffoldDigit() {

        if (_currentHighlightDigit == null) return;

        cancelPost(HESITATION_PROMPT); // prevent hesitation prompt

        switch(_currentHighlightDigit) {
            case HUN_DIGIT:
                if (getHunsDigit(dataset[0]) == getHunsDigit(dataset[1])) {
                    applyBehaviorNode(HIGHLIGHT_TENS);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;

            case TEN_DIGIT:
                if (getTensDigit(dataset[0]) == getTensDigit(dataset[1])) {
                    applyBehaviorNode(HIGHLIGHT_ONES);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;

            case ONE_DIGIT:
                // they are equal... this functionality does not yet exist
                if (getHunsDigit(dataset[0]) == getHunsDigit(dataset[1])) {
                    applyBehaviorNode(INDICATE_CORRECT);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;
        }

    }

    public void clearHighlight() {
        Log.wtf("THIS_IS_A_TEST", "clear highlight at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(NO_DIGIT);
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
            trackPerformance(true, studentChoice);
        } else {

            Log.d(TAG, "WRONG!");
            publishFeature(ND_CONST.FTR_WRONG);
            trackPerformance(false, studentChoice);
        }

    }

    /**
     * Called by AG
     */
    public void doTheWrongThing() {
        Log.d(TAG, "Doing the wrong thing");


        // Cancel all the scaffolding queue Maps.
        applyBehaviorNode(CANCEL_HIGHLIGHT_HUNS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_TENS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_ONES);
        applyBehaviorNode(CANCEL_INDICATE_CORRECT);

        // Reset the highlight.
        _layoutManager.highlightDigit(NO_DIGIT);

        // Prevent hesitation prompt.
        cancelPost(HESITATION_PROMPT);

        // Restart the scaffolding process.
        applyBehaviorNode(getStartingHighlightByNumDigits());

        // Continue to next part of nodeMap.
        applyBehaviorNode(ND_CONST.NEXTNODE);


    }

    /**
     * Called by AG
     */
    public void doTheRightThing() {
        Log.d(TAG, "Doing the right thing");

        // Cancel all the scaffolding queue Maps.
        applyBehaviorNode(CANCEL_HIGHLIGHT_HUNS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_TENS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_ONES);
        applyBehaviorNode(CANCEL_INDICATE_CORRECT);

        // Prevent hesitation prompt.
        cancelPost(HESITATION_PROMPT); // prevent hesitation prompt

        // Reset the highlight.
        _layoutManager.highlightDigit(NO_DIGIT);

        // Continue to next part of nodeMap.
        applyBehaviorNode(ND_CONST.NEXTNODE);
    }

    // Must override in TClass
   protected void trackPerformance(boolean isCorrect, String choice) {}


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

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishOrRetractFeature(String feature, boolean p) {}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishValue(String varName, String value) {
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

    /**
     * Remove named posts
     */
    public void cancelPost(String name) {

        Log.d(TAG, "Cancel Post Requested: " + name);

        while(nameMap.containsKey(name)) {

            Log.d(TAG, "Post Cancelled: " + name);

            mainHandler.removeCallbacks((Queue) (nameMap.get(name)));
            nameMap.remove(name);
        }
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

    public class Queue implements  Runnable {

        protected String _name;
        protected String _command;
        protected String _target;
        protected String _item;

        public Queue(String name, String command) {

            _name    = name;
            _command = command;

            if(name != null) {
                nameMap.put(name, this);
            }
        }

        public Queue(String name, String command, String target) {

            this(name, command);
            _target  = target;
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

                        Log.d(DEBUG_HESITATE, "applybehavior: " + _target);

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
}
