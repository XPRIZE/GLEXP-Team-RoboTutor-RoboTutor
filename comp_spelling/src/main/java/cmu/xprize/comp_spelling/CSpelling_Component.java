package cmu.xprize.comp_spelling;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;

import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class CSpelling_Component extends ConstraintLayout implements ILoadableObject {

    // layout variables
    protected ConstraintLayout Scontent;
    protected Button SdebugButton;

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;

    protected String _word;
    protected int _currentWordIndex ;
    protected List<String> _selectableLetters;
    protected List<String> _selectedLetters;

    TextView message;
    protected LinearLayout mLetterHolder;
    protected LinearLayout mSelectedLetterHolder;


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

    public void onLetterTouch(String letter, int index) {
        Log.d("ddd", "touch: " + letter);

        String current = "" + _word.charAt(_currentWordIndex);
        Log.d("ddd", "current: " + current);

        if (letter.equalsIgnoreCase(current)) {
            Log.d("ddd", "correct: " + letter);

            // Update _selectedLetter.
            _selectedLetters.add(letter);

            // Update _selectableLetters.
            _selectableLetters.remove(index);

            updateLetter();
            _currentWordIndex++;
        } else {
            Log.d("ddd", "incorrect: " + letter);
        }
    }

    public void updateLetter() {
        mLetterHolder.removeAllViewsInLayout();
        mSelectedLetterHolder.removeAllViewsInLayout();

        int i = 0;
        for (String l : _selectableLetters) {
            CLetter_Tile letter = new CLetter_Tile(mContext, l, i, this);
            i++;
            mLetterHolder.addView(letter);
            Log.d("ddd", "letter: " + l);
        }

        int j = 0;
        for (String l : _selectedLetters) {
            CLetter_Tile letter = new CLetter_Tile(mContext, l, j, this);
            j++;
            mSelectedLetterHolder.addView(letter);
            Log.d("ddd", "selected letter: " + l);
        }

        for (int k = _word.length(); k > j; k--) {
            CLetter_Tile letter = new CLetter_Tile(mContext, "_", k, this);
            mSelectedLetterHolder.addView(letter);
        }
    }


    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.spelling_layout, this);

        Scontent = (ConstraintLayout) findViewById(R.id.SSpelling);

        SdebugButton = (Button) findViewById(R.id.debugButton);
        SdebugButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // NAPOL this is how you go to the next node in an animator graph
                applyBehavior("DONE_WITH_WORD");
            }
        });

        mLetterHolder = (LinearLayout) findViewById(R.id.letterHolder);
        mSelectedLetterHolder = (LinearLayout) findViewById(R.id.blankHolder);

        Log.d("ddd", "init: " + _word);
        _word = "universe";
        _currentWordIndex = 0;
        _selectableLetters = new ArrayList<>(Arrays.asList(_word.split("")));
        Collections.shuffle(_selectableLetters);
        _selectedLetters = new ArrayList<String>();

        Log.d("ddd", "selectable letters: " + _selectableLetters);

        updateLetter();

//        valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

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
        Log.d("ddd", "loading dataset" + data.word);
        level = data.level;
        task = data.task;
        layout = data.layout;
        _word = data.word;

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

        bManager.sendBroadcast(msg);
    }


    /**
     * Updates the stimulus.
     */
    protected void updateStimulus() {

        Guideline bottomGuideline = (Guideline) findViewById(R.id.keyboardBottom);

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

    public ConstraintLayout getContainer() {
        return Scontent;
    }
}
