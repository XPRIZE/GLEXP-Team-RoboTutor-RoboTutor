package cmu.xprize.asm_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


/**
 * Created by mayankagrawal on 6/27/16.
 */
public class CAsm_Component extends LinearLayout implements ILoadableObject, View.OnClickListener {

    private Context mContext;

    protected String mDataSource;
    private int _dataIndex;

    private int[] numbers;
    protected Integer corValue;
    protected String operation;
    protected String currImage;

    protected int numAlleys = 0;

    private float scale = getResources().getDisplayMetrics().density;
    protected int alleyMargin = (int) (ASM_CONST.alleyMargin * scale);

    protected ArrayList<CAsm_Alley> allAlleys = new ArrayList<>();

    protected IDotMechanics mechanics = new CAsm_MechanicBase();

    protected CAsm_LetterBoxLayout Scontent;

    // json loadable
    public CAsm_Data[] dataSource;

    static final String TAG = "CAsm_Component";

    public CAsm_Component(Context context) {

        super(context);
        init(context, null);
    }

    public CAsm_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Component(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        setOrientation(VERTICAL);
        setOnClickListener(this);

        inflate(getContext(), R.layout.asm_container, this);


        mContext = context;

        if (attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mDataSource = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

        // Get the letterboxed game container
        //
        Scontent = (CAsm_LetterBoxLayout) findViewById(R.id.Scontent);
        Scontent.setOnClickListener(this);


    }

    public void setDataSource(CAsm_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }

    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length);
    }


    protected void updateDataSet(CAsm_Data data) {

        int val, id;
        boolean clickable = true;

        readInData(data);

        // TODO: talk about whether this should be part of base mechanics

        // update alleys
        for (int i = 0; i < numbers.length; i++) {

            val = numbers[i];

            if (i == numbers.length - 2) {
                id = ASM_CONST.OPERATION;
            }

            else if (i == numbers.length - 1) {
                id = ASM_CONST.RESULT;
                val = 0;
                clickable = false;
            }

            else {
                id = ASM_CONST.REGULAR;
            }

            setAlley(i, val, id, operation, clickable);
        }

        // delete extra alleys
        int delta = numAlleys - numbers.length;

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }

        setMechanics();

        mechanics.preClickAnimation();

    }

    private void readInData(CAsm_Data data) {

        numbers = data.dataset;
        currImage = data.image;
        corValue = numbers[numbers.length - 1];
        operation = data.operation;

    }

    private void setMechanics() {

        if ((mechanics.getOperation()).equals(operation)) {
            return; // no need to change mechanics
        }

        switch(operation) {

            case "+":
                mechanics = new CAsm_MechanicAdd(this);
                break;
            case "-":
                mechanics = new CAsm_MechanicSubtract(this);
                break;
        }
    }

    private void setAlley(int index, int val, int id, String operation, boolean clickable) {

        if (index + 1 > numAlleys) {
            addAlley(index, val, id, operation, clickable);
        }

        else {
            CAsm_Alley currAlley = allAlleys.get(index);
            currAlley.update(val, currImage, id, operation, clickable);
        }
    }

    private CAsm_Alley addAlley(int index, int val, int id, String operation, boolean clickable) {

        CAsm_Alley newAlley = new CAsm_Alley(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, alleyMargin);
        newAlley.setLayoutParams(lp);

        newAlley.setParams(val, currImage, id, operation, clickable);

        Scontent.addView(newAlley, index);
        allAlleys.add(index, newAlley);

        numAlleys++;

        return newAlley;
    }

    private void delAlley() {

        int index = numAlleys - 1;
        CAsm_Alley toRemove = allAlleys.get(index);

        toRemove.removeAllViews();
        Scontent.removeView(toRemove);
        allAlleys.remove(index);

        numAlleys--;

    }

    public boolean isCorrect() {

        boolean correct = corValue.equals(allAlleys.get(numAlleys - 1).getNum());

        if (!correct) {
            allAlleys.get(numAlleys-1).getEditText().setText(""); // reset answer text
        }
        return correct;
    }

    public void UpdateValue() {
    }


//  TODO: fix the onTouch to see results

 public void onClick(View v) {mechanics.handleClick();}



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
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

}