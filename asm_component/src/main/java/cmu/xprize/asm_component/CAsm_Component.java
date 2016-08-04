package cmu.xprize.asm_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CAsm_Component extends LinearLayout implements ILoadableObject, IEventListener {


    private Context mContext;

    protected String mDataSource;
    private int _dataIndex;

    private int[] numbers;

    protected int digitIndex;
    protected int numSlots;

    protected Integer corDigit;
    protected Integer corValue;
    protected String operation;
    protected String currImage;

    protected boolean dotbagsVisible = true;

    protected Integer overheadVal = null;
    protected CAsm_Text overheadText = null;

    protected int numAlleys = 0;

    private float scale = getResources().getDisplayMetrics().density;
    protected int alleyMargin = (int) (ASM_CONST.alleyMargin * scale);

//    Arithmetic problems will start with the
    protected int               placeValIndex;
    protected int               chimeIndex;
    protected String[]          chimes = ASM_CONST.CHIMES[placeValIndex];
    protected String            currentChime = chimes[chimeIndex];

    protected ArrayList<CAsm_Alley> allAlleys = new ArrayList<>();

    protected IDotMechanics mechanics = new CAsm_MechanicBase();

    // TODO: wrap in LetterBox
    //protected CAsm_LetterBoxLayout Scontent;

    // json loadable
    public CAsm_Data[] dataSource;

    //Writing
    private CAsm_Popup mPopup;

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


        //inflate(getContext(), R.layout.asm_container, this);

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
        //Scontent = (CAsm_LetterBoxLayout) findViewById(R.id.Scontent);
        //Scontent.setOnClickListener(this);
        mPopup = new CAsm_Popup(mContext);

    }

    public void setDataSource(CAsm_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }


    public void setDotBagsVisible(Boolean _dotbagsVisible) {

        for (int alley = 0; alley < allAlleys.size(); alley++) {
            CAsm_Alley curAlley = allAlleys.get(alley);
            CAsm_DotBag curDB = curAlley.getDotBag();

                if (_dotbagsVisible) {
                    curDB.setVisibility(VISIBLE);

                } else {
                    curDB.setVisibility(INVISIBLE);
                }
            }

            if (_dotbagsVisible && !dotbagsVisible) {
                mechanics.preClickSetup();
            }

            dotbagsVisible = _dotbagsVisible;

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

        mechanics.next();

    }


    public void nextDigit() {

        digitIndex--;

        for (CAsm_Alley alley: allAlleys) {
            alley.nextDigit();
        }

        corDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots)[digitIndex]);

        mechanics.nextDigit();

    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length);
    }


    protected void updateDataSet(CAsm_Data data) {

        // TODO: talk about whether this should be part of base mechanics

        int val, id;
        boolean clickable = true;

        readInData(data);

        numSlots = CAsm_Util.maxDigits(numbers) + 2;
        digitIndex = numSlots;

        updateAlley(0, 0, ASM_CONST.ANIMATOR, operation, false); // animator alley
        updateAlley(1, 0, ASM_CONST.OVERHEAD, operation, true); // carry/borrow alley

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

            updateAlley(i+2, val, id, operation, clickable);
        }

        // delete extra alleys
        int delta = numAlleys - (numbers.length+2);

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }

        setMechanics();

    }

    private void readInData(CAsm_Data data) {

        numbers = data.dataset;
        currImage = data.image;
        corValue = numbers[numbers.length - 1];
        operation = data.operation;
//        setChime(operation);

    }
//  Todo: Bring back the mechanics
//    private void setChime(String operation) {
//        if (operation.equals("+")) {
//            chimeIndex = 0;
//            currentChime = chimes[chimeIndex];
//        } else if (operation.equals("-")){
//
//        }
//    }

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
            case "x":
                mechanics = new CAsm_MechanicMultiply(this);
                break;
        }
    }

    private void updateAlley(int index, int val, int id, String operation, boolean clickable) {

        if (index + 1 > numAlleys) {
            addAlley(index, val, id, operation, clickable);
        }

        else {
            CAsm_Alley currAlley = allAlleys.get(index);
            currAlley.update(val, currImage, id, operation, clickable, numSlots);
        }
    }

    private CAsm_Alley addAlley(int index, int val, int id, String operation, boolean clickable) {

        CAsm_Alley newAlley = new CAsm_Alley(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, alleyMargin);
        newAlley.setLayoutParams(lp);

        newAlley.update(val, currImage, id, operation, clickable, numSlots);

        //Scontent.addView(newAlley, index);
        addView(newAlley, index);
        allAlleys.add(index, newAlley);

        numAlleys++;

        return newAlley;
    }

    public void nextChime() {
        chimeIndex++;
        chimes = ASM_CONST.CHIMES[placeValIndex % 4];
        currentChime = chimes[chimeIndex % chimes.length];
    }

    public void nextPlaceValue() {
        placeValIndex++;
        chimes = ASM_CONST.CHIMES[placeValIndex % 4];
        currentChime = chimes[chimeIndex % chimes.length];
    }

    public void resetChime() {
        chimeIndex = -1;
    }

    public void resetPlaceValue() {
        placeValIndex = -1;
    }


    private void delAlley() {

        int index = numAlleys - 1;
        CAsm_Alley toRemove = allAlleys.get(index);

        toRemove.removeAllViews();
        //Scontent.removeView(toRemove);
        removeView(toRemove);
        allAlleys.remove(index);

        numAlleys--;

    }

    public boolean isWholeCorrect() {

        int ans = allAlleys.get(numAlleys - 1).getNum();
        return corValue.equals(ans);

    }

    public boolean isDigitCorrect() {

        boolean overheadCorrect, bottomCorrect;

        // first check bottom answer
        CAsm_TextLayout textLayout = allAlleys.get(numAlleys - 1).getTextLayout();
        bottomCorrect = corDigit.equals(textLayout.getDigit(digitIndex));

        if (!bottomCorrect) {
            textLayout.getText(digitIndex).setText("");
        }

        // now check overhead answer
        if (overheadVal != null) {
            overheadCorrect = overheadVal.equals(overheadText.getDigit());

            if (overheadCorrect) {
                mechanics.correctOverheadText();
            }
            else {
                overheadText.setText("");
            }
        }

        overheadCorrect = (overheadVal == null); // make sure there is no new overhead val

        return (bottomCorrect & overheadCorrect);

    }

    public void updateText(CAsm_Text t) {

        ArrayList<IEventListener> listeners = new ArrayList<>();
        listeners.add(t);
        listeners.add(this);

        mPopup.showAtLocation(this, Gravity.LEFT, 10, 10);
        mPopup.enable(true,listeners);
        mPopup.update(t,50,50,300,300);

    }

    public void exitWrite() {
        mPopup.enable(false,null);
        mPopup.dismiss();
    }

    public void onEvent(IEvent event) {
        mPopup.enable(false,null);
        mPopup.dismiss();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mechanics.handleClick();
        }
        return true;
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