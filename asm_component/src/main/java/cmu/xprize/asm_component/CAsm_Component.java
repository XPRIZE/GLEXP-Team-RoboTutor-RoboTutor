package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.text.AllCapsTransformationMethod;
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
    protected CAsm_Text overheadTextSupplement = null;
    protected int curOverheadCol = -1;

    protected int numAlleys = 0;

    private float scale = getResources().getDisplayMetrics().density;
    protected int alleyMargin = (int) (ASM_CONST.alleyMargin * scale);

    protected  boolean isWriting = false;
    protected boolean hasShown = false;
    protected long startTime;

    boolean downwardResult = false;

//    Arithmetic problems will start with the
    protected int               placeValIndex;
    protected String[]          chimes = ASM_CONST.CHIMES[placeValIndex];
    protected String[]          twoRowschimes = new String[20];
    protected String            currentChime;

    protected ArrayList<CAsm_Alley> allAlleys = new ArrayList<>();

    protected IDotMechanics mechanics = new CAsm_MechanicBase();

    // TODO: wrap in LetterBox
    //protected CAsm_LetterBoxLayout Scontent;

    // json loadable
    public CAsm_Data[] dataSource;

    //Writing
    private CAsm_Popup mPopup;

    private boolean clickPaused = false;

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

    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex) {
            if (curDigitIndex != digitIndex) return;
            if (System.currentTimeMillis() - startTime < 3000 && _dotbagsVisible) return;
            if (operation.equals("x") && !_dotbagsVisible) return;

            if (_dotbagsVisible && !hasShown && !isWriting) {
                if(curOverheadCol >= 0)
                    if(allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getText().equals("")
                            || allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getCurrentTextColor() == Color.RED) {
                        mechanics.highlightBorrowable();
                        return;
                    } else
                        curOverheadCol = -1;

                hasShown = true;

                int delayTime = 0;
                for (int alley = 0; alley < allAlleys.size(); alley++) {
                    final CAsm_Alley curAlley = allAlleys.get(alley);
                    final int _curDigitIndex = curDigitIndex;
                    delayTime = wiggleDigitAndDotbag(curAlley, delayTime, _curDigitIndex);
                }

                if (!dotbagsVisible)
                    mechanics.preClickSetup();

            } else if(!_dotbagsVisible){
                for (int alley = 0; alley < allAlleys.size(); alley++)
                    allAlleys.get(alley).getDotBag().setVisibility(INVISIBLE);
            } else
                return;

            dotbagsVisible = _dotbagsVisible;
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible) {
        setDotBagsVisible(_dotbagsVisible, digitIndex);
    }

    public int wiggleDigitAndDotbag(final CAsm_Alley curAlley, int delayTime, final int curDigitIndex) {
        final CAsm_DotBag curDB = curAlley.getDotBag();
        final CAsm_TextLayout curTextLayout = curAlley.getTextLayout().getTextLayout(digitIndex);
        CAsm_Text curText = curTextLayout.getText(1);

        if (!curText.getText().equals("") && !curText.getIsStruck()) {
            clickPaused = true;
            Handler h = new Handler();

            //wiggle operator
            if((allAlleys.indexOf(curAlley) == ASM_CONST.OPERATION - 1 && !operation.equals("x")) ||
                    (allAlleys.indexOf(curAlley) == ASM_CONST.OPERATION_MULTI - 1 && operation.equals("x")) ) {
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(curDigitIndex != digitIndex) return;
                        curAlley.getTextLayout().getTextLayout(0).getText(1).wiggle(300, 1, 0, .5f);
                    }
                }, delayTime);
                delayTime += 1000;
            }

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(curDigitIndex != digitIndex) return;
                    clickPaused = false;
                    curDB.setVisibility(VISIBLE);
                    curDB.wiggle(300, 1, 0, .05f);
                    curTextLayout.getText(0).wiggle(300, 1, 0, .3f);
                    curTextLayout.getText(1).wiggle(300, 1, 0, .3f);
                }
            }, delayTime);
            delayTime += 1000;
        } else if (!curText.getIsStruck())
            curDB.setVisibility(VISIBLE);

        return delayTime;
    }

    public void next() {
        isWriting = false;

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
        isWriting = false;
        hasShown = false;
        startTime = System.currentTimeMillis();

        mechanics.nextDigit();

        corDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots)[digitIndex]);

        if(operation.equals("x")) {
            if((corDigit.equals(allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout().getDigit(digitIndex)) && !downwardResult) ||
                    (corDigit.equals(allAlleys.get(ASM_CONST.RESULT_MULTI_BACKUP - 1).getTextLayout().getDigit(digitIndex)) && downwardResult))
                nextDigit();
        }
    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length);
    }

    protected void updateDataSet(CAsm_Data data) {
        // TODO: talk about whether this should be part of base mechanics
        readInData(data);

        numSlots = CAsm_Util.maxDigits(numbers) + 1;
        digitIndex = numSlots;

        if (operation.equals("x"))
            updateAllAlleyForMultiplication();
        else
            updateAllAlleyForAddSubtract();

        setMechanics();
        setSound();

    }

    private void updateAllAlleyForAddSubtract() {
        int val, id;
        boolean clickable = true;

        updateAlley(0, 0, ASM_CONST.ANIMATOR3, operation, false); // animator alley
        updateAlley(1, 0, ASM_CONST.ANIMATOR2, operation, false); // animator alley
        updateAlley(2, 0, ASM_CONST.ANIMATOR1, operation, false); // animator alley
        updateAlley(3, 0, ASM_CONST.OVERHEAD, operation, true); // carry/borrow alley

        // update alleys
        for (int i = 0; i < numbers.length; i++) {

            val = numbers[i];

            if (i == numbers.length - 2) {
                id = ASM_CONST.OPERATION;
            } else if (i == numbers.length - 1) {
                id = ASM_CONST.RESULT;
                val = 0;
                clickable = false;
            } else {
                id = ASM_CONST.REGULAR;
            }

            updateAlley(i + 4, val, id, operation, clickable);
        }

        // delete extra alleys
        int delta = numAlleys - (numbers.length + 4);

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }
    }

    private void updateAllAlleyForMultiplication() {

        // update alleys
        updateAlley(0, numbers[0], ASM_CONST.REGULAR_MULTI, operation, true);
        updateAlley(1, numbers[1], ASM_CONST.OPERATION_MULTI, operation, false);
        updateAlley(2, numbers[2], ASM_CONST.RESULT_OR_ADD_MULTI_PART1, operation, false);
        updateAlley(3, 0, ASM_CONST.ADD_MULTI_PART2, operation, false);
        updateAlley(4, 0, ASM_CONST.ADD_MULTI_PART3, operation, false);
        updateAlley(5, 0, ASM_CONST.RESULT_MULTI_BACKUP, operation, false);

    }

    private void readInData(CAsm_Data data) {

        numbers = data.dataset;
        currImage = data.image;
        corValue = numbers[numbers.length - 1];
        operation = data.operation;

    }

    private void setSound() {
        switch (operation) {
            case "+":
//                result dotbag will be the only one playing sound
                allAlleys.get(allAlleys.size() - 1).getDotBag().setIsAudible(true);
                break;
            case "-":
//                minuend dotbag is the only one that plays
                allAlleys.get(3).getDotBag().setIsAudible(true);
                break;
        }
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

    public void nextPlaceValue() {
        placeValIndex++;
        chimes = ASM_CONST.CHIMES[placeValIndex % 4 + 1];
        String [] firstRowChimes = chimes;
        String [] secondRowChimes = ASM_CONST.CHIMES[(placeValIndex - 1) % 4 + 1];
        for(int i = 0;i < 10; i++){
            twoRowschimes[i]  = firstRowChimes[i];
        }
        for(int i = 10;i< 19; i++){
            twoRowschimes[i]  = secondRowChimes[i-10+1];
        }
        for (CAsm_Alley alley: allAlleys) {
            CAsm_DotBag dotBag = alley.getDotBag();
            if(dotBag.getRows() == 1){
                dotBag.setChimes(chimes);
                dotBag.setChimeIndex(-1);
            }else{
                dotBag.setChimes(twoRowschimes);
                dotBag.setChimeIndex(-1);
            }
        }
    }

    public void playChime() {

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

        int ans;
        if (operation.equals("x")) {
            if(downwardResult)
                ans = allAlleys.get(ASM_CONST.RESULT_MULTI_BACKUP - 1).getNum();
            else
                ans = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getNum();
        } else
            ans = allAlleys.get(numAlleys - 1).getNum();

        return corValue.equals(ans);

    }

    public boolean isDigitCorrect() {

        boolean overheadCorrect, bottomCorrect;

        CAsm_TextLayout textLayout;
        if(operation.equals("x")) {
            if (downwardResult)
                textLayout = allAlleys.get(ASM_CONST.RESULT_MULTI_BACKUP - 1).getTextLayout();
            else
                textLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
        } else
            textLayout = allAlleys.get(numAlleys - 1).getTextLayout();

        //For multiplication, user can change the order of writing result.
        //e.g. If the result is 123, user input 1 first. We need to confirm the “1” is a correct input.
        if(operation.equals("x")) checkOtherBottomCorrect(textLayout);

        // first check bottom answer
        bottomCorrect = corDigit.equals(textLayout.getDigit(digitIndex));

        if (!bottomCorrect) {
            wrongDigit(textLayout.getTextLayout(digitIndex).getText(1));
        }

        // now check overhead answer
        if (overheadVal != null) {
            if (overheadVal < 10)
                overheadCorrect = overheadVal.equals(overheadText.getDigit());
            else if (overheadTextSupplement.getDigit() == null || overheadText.getDigit() == null)
                overheadCorrect = false;
            else
                overheadCorrect = overheadVal.equals(overheadTextSupplement.getDigit() * 10 + overheadText.getDigit());

            if (overheadCorrect)
                mechanics.correctOverheadText();
            else if (overheadVal < 10)
                wrongDigit(overheadText);
            else {
                if (overheadTextSupplement.getDigit() != null && overheadTextSupplement.getDigit() != overheadVal/10) wrongDigit(overheadTextSupplement);
                if (overheadText.getDigit() != null && overheadText.getDigit() != overheadVal%10) wrongDigit(overheadText);
            }
        }

//        overheadCorrect = (overheadVal == null); // make sure there is no new overhead val

        return (bottomCorrect);
    }

    public void checkOtherBottomCorrect(CAsm_TextLayout textLayout) {
        int otherBottomCorrect = 0;
        for(int i = 1; i < digitIndex; i++) {
            Integer curDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots)[i]);
            Integer digit = textLayout.getDigit(i);
            if(digit != null && !digit.equals(""))
                otherBottomCorrect = curDigit.equals(textLayout.getDigit(i))? 1 : 2;
            if(otherBottomCorrect == 0 && i+ 1 == digitIndex)
                break;
            if(otherBottomCorrect == 1) {
                textLayout.getTextLayout(i).getText(1).reset();
                i = digitIndex;
            } else if(otherBottomCorrect == 2) {
                wrongDigit(textLayout.getTextLayout(i).getText(1));
                i = digitIndex;
            }
        }
    }

    public void wrongDigit(final CAsm_Text t) {
            //Indicates that the digit the user entered is wrong w/ red text.
            t.setTextColor(Color.RED);
            clickPaused = true;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    t.setText("");
                    t.setTextColor(Color.BLACK);
                    clickPaused = false;
                }
            }, 1500);
    }

    public boolean getClickPaused() {return clickPaused;}

    public void highlightText(final CAsm_Text t) {
        //Useful to highlight individual Text-fields to call importance to them.
        int colorStart = Color.YELLOW;
        int colorEnd = Color.TRANSPARENT;
        ValueAnimator v = ValueAnimator.ofObject(new ArgbEvaluator(),colorStart,colorEnd);
        v.setDuration(1250);
        v.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                t.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        v.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                if (t.isWritable) {
                    //For multiplication, user could choose the order of writing result digits
                    if(operation.equals("x")) {
                        CAsm_TextLayout resultTextLayout;
                        if (downwardResult)
                            resultTextLayout = allAlleys.get(ASM_CONST.RESULT_MULTI_BACKUP - 1).getTextLayout();
                        else
                            resultTextLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
                        for(int i = 1; i <= digitIndex; i++) {
                            if(resultTextLayout.getTextLayout(i).getText(1).isWritable)
                                resultTextLayout.getTextLayout(i).getText(1).setResult();
                        }
                    } else
                        t.setResult();
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                if (t.isWritable) {
                    //For multiplication, user could choose the order of writing result digits
                    if(operation.equals("x")) {
                        CAsm_TextLayout resultTextLayout;
                        if (downwardResult)
                            resultTextLayout = allAlleys.get(ASM_CONST.RESULT_MULTI_BACKUP - 1).getTextLayout();
                        else
                            resultTextLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
                        for(int i = 1; i <= digitIndex; i++) {
                            if(resultTextLayout.getTextLayout(i).getText(1).isWritable)
                                resultTextLayout.getTextLayout(i).getText(1).setResult();
                        }
                    } else
                        t.setResult();
                }
            }
        });
        v.start();
    }

    public void highlightCurrentColumn() {
        //Highlights user's active column.
        for (CAsm_Alley alley: allAlleys) {
            try {
                CAsm_Text text = alley.getTextLayout().getTextLayout(digitIndex).getText(1);
                if (text.getDigit() != null || text.isWritable) {highlightText(text); }
            } catch (NullPointerException e) { continue;}
        }
    }

    public void updateText(CAsm_Text t, boolean isClickingBorrowing) {
        isWriting = true;
        if (!mPopup.isActive) {
            ArrayList<IEventListener> listeners = new ArrayList<>();
            listeners.add(t);
            listeners.add(this);
            mPopup.showAtLocation(this, Gravity.LEFT, 10, 10);
            mPopup.enable(true, listeners);

            if(isClickingBorrowing)
                mPopup.update(t, 120, -300, 300, 300);
            else
                mPopup.update(t, 60, 0, 300, 300);

            mPopup.isActive = true;
        }
    }

    public void exitWrite() {

        mPopup.isActive = false;
        mPopup.enable(false,null);
        mPopup.dismiss();

        resetHesitationTimer(3000);
    }

    public void resetHesitationTimer(int delayTime) {
        if(isWriting && !hasShown) {
            startTime = System.currentTimeMillis();
            isWriting = false;

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDotBagsVisible(true, digitIndex);
                }
            }, delayTime);
        }
    }

    public void onEvent(IEvent event) {
        mPopup.isActive = false;
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