package cmu.xprize.comp_spelling;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.AUDIO_EVENT;
import static cmu.xprize.util.TCONST.FTR_EOD;
import static cmu.xprize.util.TCONST.TYPE_AUDIO;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CSpelling_Component extends ConstraintLayout implements ILoadableObject, IPublisher, IEventListener {

    //region Class Variables

    // layout variables
    protected ConstraintLayout Scontent;

    // DataSource Variables
    protected int _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    private String _sound;

    private String IMAGES_PATH;

    // json loadable
    public String bootFeatures;
    public CSpelling_Data[] dataSource;
    protected List<CSpelling_Data> _data;

    protected List<String> _word;
    protected int _currentLetterIndex;
    protected List<String> _selectableLetters;
    protected List<String> _selectedLetters;
    protected String _imageFileName;
    protected String _fullword;
    protected int _attemptCount;

    protected boolean lastSyllable;

    // View
    protected Context mContext;
    protected LinearLayout mLetterHolder;
    protected LinearLayout mSelectedLetterHolder;
    protected ImageView mImageStimulus;
    private CLetter_Tile wrongLetter;

    private LocalBroadcastManager _bManager;

    protected HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    //endregion

    //region Override Methods

    @Override
    public void publishState() {

    }

    @Override
    public void publishValue(String varName, String value) {

    }

    @Override
    public void publishValue(String varName, int value) {

    }

    @Override
    public void publishFeatureSet(String featureset) {

    }

    @Override
    public void retractFeatureSet(String featureset) {

    }

    @Override
    public void publishFeature(String feature) {
    }

    @Override
    public void retractFeature(String feature) {

    }

    @Override
    public void publishFeatureMap(HashMap featureMap) {

    }

    @Override
    public void retractFeatureMap(HashMap featureMap) {

    }

    protected String getImagePath() {
        return null;
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}

    protected void trackAndLogPerformance(boolean isCorrect, String selectedSyllable) {

    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    public void applyBehaviorNode(String nodeName) {

    }

    //endregion

    //region Constructors

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

        Scontent = (ConstraintLayout) findViewById(R.id.SSpelling);

        mLetterHolder = (LinearLayout) findViewById(R.id.letterHolder);
        mSelectedLetterHolder = (LinearLayout) findViewById(R.id.blankHolder);
        mImageStimulus = (ImageView) findViewById(R.id.imageStimulus);

        _bManager = LocalBroadcastManager.getInstance(getContext());
    }

    //endregion

    //region View
    public void onLetterTouch(String letter, int index, CLetter_Tile lt) {

        Log.d("ddd", "Touch: " + letter);

        //retractFeature(SP_CONST.FTR_CORRECT);
        retractFeature(SP_CONST.FTR_INCORRECT);

        lockLetters();

        String current = "" + _word.get(_currentLetterIndex);

        // publish the syllable to be pronounced
        String pronunciation = mapLetterToVerbal(letter);
        publishValue(SP_CONST.SYLLABLE_STIM, pronunciation);

        boolean isCorrect = letter.equalsIgnoreCase(current);
        trackAndLogPerformance(isCorrect, letter);
        if (isCorrect) {
            Log.d("ddd", "Correct");
            _selectedLetters.add(letter);
            _selectableLetters.set(index, null);

            applyBehaviorNode("SAY_SYLLABLE");
            updateLetter();
            _currentLetterIndex++;
            // if correct
            //publishFeature(SP_CONST.FTR_CORRECT);
            if (_currentLetterIndex >= _word.size()) {
                Log.d("ddd", "end of word");

                lastSyllable = true;
                publishFeature(SP_CONST.FTR_EOP);
            }

            _attemptCount = 1;
        } else {
            Log.d("ddd", "INCORRECT");
            lt.indicateError();
            wrongLetter = lt;
            publishFeature(SP_CONST.FTR_INCORRECT);
            applyBehavior("NEXT_NODE");
            _attemptCount++;
        }

    }

    /**
     * Map a letter to the pronunciation, e.g. to say "mm" instead of "me"
     *
     * @param letter
     * @return
     */
    private String mapLetterToVerbal(String letter) {
        switch (letter) {
            case "m":
                return "mm";

            case "n":
                return "nn";

            default:
                return letter;
        }
    }

    public void revertColor() {
        wrongLetter.revertColor();
        wrongLetter = null;
    }


    public void updateImage() {

        String imagePath = IMAGES_PATH + _imageFileName;
        mImageStimulus.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        mImageStimulus.getLayoutParams().height = SP_CONST.IMAGE_SIZE;
        mImageStimulus.requestLayout();

//        pointAtMyView();
    }


    /**
     * Called when the syllable is completed
     * @param eventObject
     */
    @Override
    public void onEvent(IEvent eventObject) {

        if (eventObject.getType().equals(TYPE_AUDIO)) {
            String command = (String) eventObject.getString(AUDIO_EVENT);

            // don't advance until it's the last syllable
            if (lastSyllable) {
                publishValue(SP_CONST.WORD_STIM, _sound);
                applyBehavior("NEXT_NODE");
            }
            Log.d("FIX_SPELLING", "onEvent -- " + command);
        }
    }

    public void updateLetter() {

        mLetterHolder.removeAllViewsInLayout();
        mSelectedLetterHolder.removeAllViewsInLayout();

        int i = 0;
        for (String l : _selectableLetters) {

            CLetter_Tile letter;
            if (l == null) {
                letter = new CLetter_Tile(mContext, null, i, this);
            } else {
                letter = new CLetter_Tile(mContext, l, i, this);
            }
            i++;
            mLetterHolder.addView(letter);
        }

        int j = 0;
        for (String l : _selectedLetters) {
            CLetter_Tile letter = new CLetter_Tile(mContext, l, j, this);
            letter.markCorrect();
            j++;
            mSelectedLetterHolder.addView(letter);
        }

        for (int k = _word.size(); k > j; k--) {
            CBlank_Letter b = new CBlank_Letter(mContext);
            mSelectedLetterHolder.addView(b);
        }
    }

    public void lockLetters() {

        for (int i = 0; i < mLetterHolder.getChildCount(); i++) {
            CLetter_Tile lt = (CLetter_Tile) mLetterHolder.getChildAt(i);
            lt.lock();
        }
    }

    public void unlockLetters() {

        Log.d("ddd", "unlock letters");
        for (int i = 0; i < mLetterHolder.getChildCount(); i++) {
            CLetter_Tile lt = (CLetter_Tile) mLetterHolder.getChildAt(i);
            lt.unlock();
        }
    }


    public void pointAtMyView() {
        Log.d("ddd", "pointing");
//        View pointAtView = findViewById(R.id.imageStimulus);
        View pointAtView = mLetterHolder;

        int[] screenCoord = new int[2];
        pointAtView.getLocationOnScreen(screenCoord);

        // Try getting actual x and y instead?

        PointF targetPoint = new PointF(screenCoord[0] + pointAtView.getWidth()/2, screenCoord[1] + pointAtView.getHeight());

        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        Log.d("ddd", "target: " + targetPoint.toString());
        _bManager.sendBroadcast(msg);

    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.letterHolder);

        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);
    }

    public ConstraintLayout getContainer() {
        return Scontent;
    }

    //endregion

    //region Data

    public void next() {

        try {
            if (_data != null) {
                updateDataSet(_data.get(_dataIndex));
                _dataIndex++;
            }
        } catch (Exception e) {
            CErrorManager.logEvent(SP_CONST.TAG, "Data Exhuasted: call past end of data", e, true);
        }

        if (dataExhausted()) {
            publishFeature(FTR_EOD);
        }
    }

    public boolean dataExhausted() {

        return _dataIndex >= _data.size();
    }

    protected void updateDataSet(CSpelling_Data data) {

        loadDataSet(data);
        updateStimulus();
    }

    /**
     * Loads from current word into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    protected void loadDataSet(CSpelling_Data data) {

        lastSyllable = false;

        level = data.level;
        task = data.task;
        layout = data.layout;
        _word = new ArrayList<>(Arrays.asList(data.word));
        _attemptCount = 1;
        _currentLetterIndex = 0;

        _sound = data.sound;

        // TODO: Is there a join function?
        StringBuilder sb = new StringBuilder();
        for (String c : _word) {
            sb.append(c);
        }
        _fullword = sb.toString();

        retractFeature(SP_CONST.FTR_EOP);
        _selectableLetters = new ArrayList<>(_word);
        Collections.shuffle(_selectableLetters);

        _selectedLetters = new ArrayList<>();
        _imageFileName = data.image;

        publishValue(SP_CONST.WORD_STIM, _sound);
    }

    /**
     * Updates the stimulus.
     */
    protected void updateStimulus() {

        updateLetter();
        updateImage();
    }

    public void setDataSource(CSpelling_Data[] dataSource) {
        ArrayList<CSpelling_Data> dataset = new ArrayList<>(Arrays.asList(dataSource));

        _data = new ArrayList<>();

        // For XPrize we limit this to 10 elements from an umlimited random data set
        // used to be : dataSet.size()
        Collections.shuffle(dataset);
        _data = dataset.subList(0, SP_CONST.NUM_PROBLEMS);
        _dataIndex = 0;

        IMAGES_PATH = getImagePath();
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

    //endregion
}
