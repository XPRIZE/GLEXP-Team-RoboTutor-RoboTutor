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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CSpelling_Component extends ConstraintLayout implements ILoadableObject, IPublisher {

    // layout variables
    protected ConstraintLayout Scontent;
    protected Button SdebugButton;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;

    protected List<String> _word;
    protected String _imageFileName;
    protected int _currentWordIndex ;
    protected List<String> _selectableLetters;
    protected List<String> _selectedLetters;
    protected String _fullword;

    TextView message;
    protected LinearLayout mLetterHolder;
    protected LinearLayout mSelectedLetterHolder;
    protected ImageView mImageStimulus;


    // json loadable
    public String bootFeatures;
    public int rows;
    public int cols;
    public CSpelling_Data[] dataSource;
    protected List<CSpelling_Data> _data;

    protected int _attemptCount;

    // View Things
    protected Context mContext;

    private LocalBroadcastManager _bManager;
    private CLetter_Tile wrongLetter;

//    static final String FTR_EOP = "FTR_EOP";
//    static final String WORD_STIM = ".wordStim";
//    static final String SYLLABLE_STIM = ".syllableStim";

//    static final int IMAGE_SIZE = 300;
//    static final int NUM_PROBLEMS = 2;

    protected HashMap<String,Boolean> _FeatureMap = new HashMap<>();
    private String IMAGES_PATH;

    //region override methods

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

    //endregion

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    public void applyBehaviorNode(String nodeName) {
    }

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

    public void onLetterTouch(String letter, int index, CLetter_Tile lt) {
        Log.d("ddd", "touch: " + letter);

        retractFeature(SP_CONST.FTR_CORRECT);
        retractFeature(SP_CONST.FTR_INCORRECT);

        lockLetters();

        String current = "" + _word.get(_currentWordIndex);
        Log.d("ddd", "current index: " + _currentWordIndex);
        Log.d("ddd", "current: " + current);

        publishValue(SP_CONST.SYLLABLE_STIM, letter);

        Log.d("ddd", "features: " + _FeatureMap.toString());
        boolean isCorrect = letter.equalsIgnoreCase(current);
        trackAndLogPerformance(isCorrect, letter);
        if (isCorrect) {
            Log.d("ddd", "correct: " + letter);

            // Update _selectedLetter.
            _selectedLetters.add(letter);

            // Update _selectableLetters.
            _selectableLetters.remove(index);

            updateLetter();
            _currentWordIndex++;
            publishFeature(SP_CONST.FTR_CORRECT);
            if (_currentWordIndex >= _word.size()) {
                Log.d("ddd", "end of word");
                publishFeature(SP_CONST.FTR_EOP);
            }
            applyBehavior("NEXT_NODE");
            Log.d("ddd", "features: " + _FeatureMap.toString());
//            applyBehaviorNode("NEXTNODE");
            _attemptCount = 1;
        } else {
            Log.d("ddd", "incorrect: " + letter);
            lt.indicateError();
            wrongLetter = lt;
            publishFeature(SP_CONST.FTR_INCORRECT);
            applyBehavior("NEXT_NODE");
            Log.d("ddd", "features: " + _FeatureMap.toString());
            _attemptCount++;
        }

    }

    public void revertColor() {
        wrongLetter.revertColor();
        wrongLetter = null;
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

    public void updateImage() {
//        try {
        Log.d("ddd", "loading image");
        String imagePath = IMAGES_PATH + _imageFileName;
//            InputStream in = JSON_Helper.assetManager().open(IMAGES_PATH + _imageFileName);
//            mImageStimulus.setImageBitmap(BitmapFactory.decodeStream(in));
        mImageStimulus.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        mImageStimulus.getLayoutParams().height = SP_CONST.IMAGE_SIZE;
        mImageStimulus.requestLayout();

        pointAtMyView();
//        } catch (IOException e) {
//            Log.d("ddd", "image error");
//            mImageStimulus.setImageBitmap(null);
//            e.printStackTrace();
//        }
    }

    public void updateLetter() {
        mLetterHolder.removeAllViewsInLayout();
        mSelectedLetterHolder.removeAllViewsInLayout();

        int i = 0;
        for (String l : _selectableLetters) {
            CLetter_Tile letter = new CLetter_Tile(mContext, l, i, this);
            i++;
            mLetterHolder.addView(letter);
//            Log.d("ddd", "letter: " + l);
        }

        int j = 0;
        for (String l : _selectedLetters) {
            CLetter_Tile letter = new CLetter_Tile(mContext, l, j, this);
            j++;
            mSelectedLetterHolder.addView(letter);
//            Log.d("ddd", "selected letter: " + l);
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


    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.spelling_layout, this);

        Scontent = (ConstraintLayout) findViewById(R.id.SSpelling);

//        SdebugButton = (Button) findViewById(R.id.debugButton);
//        SdebugButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // NAPOL this is how you go to the next node in an animator graph
//                applyBehavior("DONE_WITH_WORD");
//            }
//        });

        mLetterHolder = (LinearLayout) findViewById(R.id.letterHolder);
        mSelectedLetterHolder = (LinearLayout) findViewById(R.id.blankHolder);
        mImageStimulus = (ImageView) findViewById(R.id.imageStimulus);


        _bManager = LocalBroadcastManager.getInstance(getContext());


//        valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

    }

    public void next() {

        try {
            if (_data != null) {
                updateDataSet(_data.get(_dataIndex));
                _dataIndex++;
            }

        } catch (Exception e) {
            CErrorManager.logEvent(SP_CONST.TAG, "Data Exhuasted: call past end of data", e, false);
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
        Log.d("ddd", "loading dataset" + data.word);
        _attemptCount = 1;
        level = data.level;
        task = data.task;
        layout = data.layout;
        _word = new ArrayList<>(Arrays.asList(data.word));

        // Find a better way than this.
        StringBuilder sb = new StringBuilder();
        for (String c : _word) {
            sb.append(c);
        }
        _fullword = sb.toString();

        _currentWordIndex = 0;
        retractFeature(SP_CONST.FTR_EOP);
//        _selectableLetters = new ArrayList<>(Arrays.asList(_word.split("")));
        _selectableLetters = new ArrayList<>(_word);
        Collections.shuffle(_selectableLetters);
        _selectedLetters = new ArrayList<>();

//        String word = TextUtils.join("", _word);

        _imageFileName = data.image;
        Log.d("ddd", "image: " + data.image);


        Log.d("ddd", "selectable letters: " + _selectableLetters);

        updateLetter();
        updateImage();

        Log.d("ddd", "word stim: ");
        publishValue(SP_CONST.WORD_STIM, data.sound);
        // for each character in word, insert a new empty space into SPACES
        // for each character in word, add to the list. Then add distractors. Then JUMBLE.

        // next ???
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
        View v = findViewById(R.id.letterHolder);

        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);
    }


    /**
     * Updates the stimulus.
     */
    protected void updateStimulus() {
        Guideline bottomGuideline = (Guideline) findViewById(R.id.keyboardBottom);

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

    public ConstraintLayout getContainer() {
        return Scontent;
    }

}
