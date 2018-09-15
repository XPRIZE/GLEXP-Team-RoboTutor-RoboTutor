package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cmu.xprize.asm_component.ui.CAsm_LayoutManagerInterface;
import cmu.xprize.asm_component.ui.CAsm_LayoutManager_NewMath;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_writebox.ICharRecListener_Simple;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


public class CAsm_Component extends LinearLayout implements IBehaviorManager, ILoadableObject, ICharRecListener_Simple, IPublisher {


    private Context mContext;

    private CAsm_LayoutManagerInterface _layoutManager;

    protected final Handler     mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap           queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    protected String            mDataSource;
    protected int               _dataIndex;
    protected int               _actualDataIndex; // TODO: This is used for random. Need refactor.
    protected boolean           _dataEOI   = false;
    protected int[]             dataset;

    private int[]                 _screenCoord = new int[2];
    private LocalBroadcastManager bManager;

    //current digit
    protected int digitIndex;
    protected int numSlots;

    // task-level info
    protected String task;
    protected String level;

    //corValue is the correct result e.g. 302
    //corDigit is current correct digit  e.g. 3 (third digit)
    //
    protected Integer   corDigit;
    protected Integer   corValue;
    protected String    operation;
    protected String    curImage;

    protected ArrayList<String[]> problemDigits;

    //used for addition
    //
    protected boolean   dotbagsVisible = true;

    //used to show:
    //carrying in addition
    //borrowing in subtraction
    protected Integer   overheadVal = null;
    protected CAsm_Text overheadText = null;
    protected CAsm_Text overheadTextSupplement = null;
    protected int       curOverheadCol = -1;

    //the number of Alleys
    protected int numAlleys = 0;

    private float scale       = getResources().getDisplayMetrics().density;
    protected int alleyMargin = (int) (ASM_CONST.alleyMargin * scale);

    // If user is writing, stop the timer used to show dotbags
    //use TimeStamp to judge if it is the time to show the dotbags
    //
    protected  boolean isWriting = false;
    protected boolean  hasShown = false;
    protected long     startTime;

    // Arithmetic problems will start with the
    //
    protected int               placeValIndex;
    protected String[]          chimes = ASM_CONST.CHIMES[placeValIndex];
    protected String[]          twoRowschimes = new String[20];
    protected String            currentChime;

    // MATHFIX_LAYOUT where the alleys are
    protected ArrayList<CAsm_Alley> allAlleys = new ArrayList<>();

    protected IDotMechanics mechanics = new CAsm_MechanicBase();

    // TODO: wrap in LetterBox
    //protected CAsm_LetterBoxLayout Scontent;

    //Writing
    protected CAsm_Popup mPopup;
    protected CAsm_Popup mPopupSupplement;
    private boolean hasTwoPopup = false;

    public boolean clickPaused = false;
    protected int overheadCorrect = ASM_CONST.NO_INPUT;
    protected int resultCorrect = ASM_CONST.NO_INPUT;
    protected String curNode = "";

    // json loadable
    public String      bootFeatures = "";
    public boolean     random;
    public int         questionCount;
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

        mPopup           = new CAsm_Popup(mContext); // MATHFIX_2 POPUP
        mPopupSupplement = new CAsm_Popup(mContext);

        // Capture the local broadcast manager
        //
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    public void onDestroy() {

        terminateQueue();

        // Remove the write components if active
        //
        exitWrite();

//        if(_mechanics != null) {
//            _mechanics.onDestroy();
//            _mechanics = null;
//        }
    }


    public void setDataSource(CAsm_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }


    /**
     * if using random selection, go until we've exceeded the question count
     * if going through list, just go until we've reached the end
     */
    public boolean dataExhausted() {
        return (random ? _dataIndex >= questionCount : _dataIndex >= dataSource.length);
    }


    public void next() {
        isWriting = false;
        hasShown = false;
        curOverheadCol = -1;

        /*String rand = random ? "TRUE" : "FALSE"; */

        try {
            if (dataSource != null) {

                if(!random) {
                    updateDataSet(dataSource[_dataIndex]);
                } else {
                    // with no replacement?
                    int nextIndex = (new Random()).nextInt(dataSource.length);
                    _actualDataIndex = nextIndex;
                    updateDataSet(dataSource[nextIndex]);

                    boolean replace = true;
                    if(!replace) {
                        ArrayList<CAsm_Data> newDataSource = new ArrayList<>();

                        for (int i=0; i < dataSource.length; i++) {
                            if(i != nextIndex) {
                                newDataSource.add(dataSource[i]);
                            }
                        }

                        dataSource = (CAsm_Data[]) newDataSource.toArray();
                    }
                }

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

        mechanics.next();
    }


    protected void updateDataSet(CAsm_Data data) {

        // init the dataset
        //
        loadDataSet(data);

        // MATHFIX_3 use numSlots. Might have to move to LinearLayout.
        numSlots   = CAsm_Util.maxDigits(dataset) + 1;
        digitIndex = numSlots;

        alleyMargin = (int) (ASM_CONST.alleyMargin * scale);


        // MATHFIX_BUILD use this conditional to reproduce the code, one part at a time
        if (ASM_CONST.USE_NEW_MATH) {

            _layoutManager = new CAsm_LayoutManager_NewMath(this, mContext);

            _layoutManager.initialize();

            _layoutManager.initializeProblem(dataset[0], dataset[1], dataset[2], operation);

        }

        if (ASM_CONST.USE_NEW_MATH) {
            setDebugNewMathButtons();
        }

        if (ASM_CONST.USE_NEW_MATH) {
            String currentDigit = "one";

            //_layoutManager.emphasizeCurrentDigitColumn("one");
            _layoutManager.emphasizeCurrentDigitColumn(currentDigit);

            // this should be moved into the delayed scaffolding
            _layoutManager.showDotBagsForDigit(currentDigit, dataset[0], dataset[1]);

        }



        if (!ASM_CONST.USE_NEW_MATH) {

            updateAllAlleyForAddSubtract();


            setMechanics();


            setSound();


            // Extract an easily accessible set of problem digits.
            // Each element in problemDigits is an array of digits representing
            // the place values of the given operand.
            // NOTE: -1 we don't include the "answer" which is the last element in the
            // dataset.
            //
            problemDigits = new ArrayList<>();

            for (int opIndex = 0; opIndex < dataset.length - 1; opIndex++) {

                problemDigits.add(opIndex, CAsm_Util.intToDigits(dataset[opIndex], numSlots));
            }


            final CAsm_Component xyz = this;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    logLayoutTree(xyz, "");
                }
            });
        }
    }

    /**
     * Give behavior to debug buttons so we can easily test UI functionality.
     */
    private void setDebugNewMathButtons() {
        // MATHFIX_3 NEXT NEXT NEXT... step through the animation sequence, for one digit

        LinearLayout buttonMenu = findViewById(R.id.debug_button_menu);
        String[] debugButtons = {"DIGIT_ONE", "DIGIT_TEN", "DIGIT_HUN",
                "WIGGLY_TOP", "WIGGLY_OP", "WIGGLY_MID",
                "WRONG",
                "POPUP_ONE", "POPUP_TEN", "POPUP_HUN"};

        for(String tag : debugButtons) {
            Button button = new Button(mContext);
            button.setText(tag);
            button.setTag(tag);
            button.setOnClickListener(_debugButtonListener);

            buttonMenu.addView(button);
        }

    }

    /**
     * For defining debug button behavior
     */
    OnClickListener _debugButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getTag().toString()) {

                case "DIGIT_ONE":
                    _layoutManager.emphasizeCurrentDigitColumn("one");
                    _layoutManager.showDotBagsForDigit("one", dataset[0], dataset[1]);
                    break;

                case "DIGIT_TEN":
                    _layoutManager.emphasizeCurrentDigitColumn("ten");
                    _layoutManager.showDotBagsForDigit("ten", dataset[0], dataset[1]);
                    break;

                case "DIGIT_HUN":
                    _layoutManager.emphasizeCurrentDigitColumn("hun");
                    _layoutManager.showDotBagsForDigit("hun", dataset[0], dataset[1]);
                break;

                case "WIGGLY_TOP":
                    _layoutManager.wiggleDigitAndDotbag("top", "one");
                    break;

                case "WIGGLY_OP":
                    _layoutManager.wiggleDigitAndDotbag("op", null);
                    break;

                case "WIGGLY_MID":
                    _layoutManager.wiggleDigitAndDotbag("mid", "one");
                    break;

                case "WRONG":
                    _layoutManager.wrongDigit("one");
                    break;

                case "POPUP_ONE":
                case "POPUP_TEN":
                case "POPUP_HUN":
                    // MATHFIX_3 do me next
                    // show popup
                    break;
            }
        }
    };

    /**
     * for debugging the entire layout tree
     */
    private void logLayoutTree(ViewGroup v, String parents) {

        Log.d("LAYOUT_TREE", parents + " == " + v.getClass().getSimpleName() + " has " + v.getChildCount() + " children.");
        if(this.getChildCount() == 0) {
            Log.d("LAYOUT_TREE", "-- LEAF --");
            return;
        }

        for(int i=0; i < v.getChildCount(); i++) {

            View child = v.getChildAt(i);
            String parentName = parents + "." + v.getClass().getSimpleName() + "(" + i + ")";


            if (child instanceof ViewGroup) {
                logLayoutTree((ViewGroup) child, parentName);
            } else if (child instanceof TextView){
                Log.d("LAYOUT_TREE", parentName + "." + child.getClass().getSimpleName() + " -- " + ((TextView) child).getText());
            } else {
                Log.d("LAYOUT_TREE", parentName + "." + child.getClass().getSimpleName() + " -- " + "LEAF");
            }
        }

    }


    /**
     * This is so we can load a default data set to drive the introduction audio
     *
     * @param data
     */
    protected void loadDataSet(CAsm_Data data) {

        dataset   = data.dataset;
        curImage  = data.image;
        corValue  = dataset[dataset.length - 1];
        operation = data.operation;
        level     = data.level;
        task      = data.task;
    }

    // ----------------------------------
    // --------- INITIALIZATION ---------
    // ----------------------------------

    /**
     * this initializes alleys to hold Digits and DotBags
     */
    private void updateAllAlleyForAddSubtract() {
        int     val, id;
        boolean clickable = true;

        if (!ASM_CONST.USE_NEW_MATH) {
            // don't need any of these
            updateAlley(0, 0, ASM_CONST.ANIMATOR3, operation, false); // EXTRA SPACE animator alley
            updateAlley(1, 0, ASM_CONST.ANIMATOR2, operation, false); // EXTRA SPACE animator alley
            updateAlley(2, 0, ASM_CONST.ANIMATOR1, operation, false); // EXTRA SPACE animator alley
            updateAlley(3, 0, ASM_CONST.CARRY_BRW, operation, true);  // carry/borrow alley

        }

        // update alleys
        for (int i = 0; i < dataset.length; i++) {

            val = dataset[i];

            // i == 0
            if (i == dataset.length - 2) {
                id = ASM_CONST.OPERATOR_ROW;
            }
            // i == 1
            else if (i == dataset.length - 1) {
                id = ASM_CONST.RESULT_ROW;
                val = 0;
                clickable = false;
            }
            // i == 2
            else {
                id = ASM_CONST.OPERAND_ROW;
            }

            // val is the number to be added/subtracted
            // id = ???
            // operation = plus/minus
            // clickable = true/false
            updateAlley(i + 4, val, id, operation, clickable);
        }


        if (!ASM_CONST.USE_NEW_MATH) {
            // delete extra alleys
            // TODO: Eliminate / Fix this delete code
            int delta = numAlleys - (dataset.length + 4);

            if (delta > 0) {
                for (int i = 0; i < delta; i++) {
                    delAlley();
                }
            }
        }
    }

    // MATHFIX_CLEAN move to initialization
    private void updateAlley(int index, int val, int id, String operation, boolean clickable) {

        CAsm_Alley currAlley;

        if (index + 1 > numAlleys) {
            currAlley = addAlley(index);
        }
        else {
            currAlley = allAlleys.get(index);
        }

        // MATHFIX_3 next, use numSlots. Update views to be consistent. Might have to move to LinearLayout
        // change the text digits to be "val"
        // change the dotbags to use image "curImage"
        // change the operation to be "operation" (+/-)
        // numSlots = maxDigits(dataset) + 1 (for the operator)
        currAlley.update(val, curImage, id, operation, clickable, numSlots);
    }

    // MATHFIX_CLEAN move to initialization
    private CAsm_Alley addAlley(int index ) {

        CAsm_Alley newAlley = new CAsm_Alley(mContext); // MATHFIX_LAYOUT TextLayout created here.

        // MATHFIX_LAYOUT LayoutParams
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, alleyMargin);
        newAlley.setLayoutParams(lp);

        //Scontent.addView(newAlley, index);
        // MATHFIX_LAYOUT where alley View gets added
        addView(newAlley, index);
        allAlleys.add(index, newAlley);
        Log.d(ASM_CONST.TAG_DEBUG_MATHFIX, "addView CAsm_Alley:" + index + " to CAsm_Component");

        numAlleys++;

        return newAlley;
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


    private void setSound() {

        switch (operation) {
            case "+":
                //  result dotbag will be the only one playing sound
                allAlleys.get(allAlleys.size() - 1).getDotBag().setIsAudible(true);
                break;

            case "-":
                // minuend dotbag is the only one that plays
                allAlleys.get(3).getDotBag().setIsAudible(true);
                break;
        }
    }


    private void setMechanics() {

        switch(operation) {

            case "+":
                mechanics = new CAsm_MechanicAdd(this);
                break;
            case "-":
                mechanics = new CAsm_MechanicSubtract(this);
                break;
        }
    }


    // ----------------------------------
    // --------- NEXT DIGIT --------------
    // ----------------------------------

    public void nextDigit() {

        digitIndex--;
        isWriting = false;
        hasShown  = false;
        startTime = System.currentTimeMillis();

        try {
            mechanics.nextDigit(); // REVIEW throws exception
        } catch (Exception e) {
            // REVIEW do nothing... this just prevents "corDigit" from be skipped over below, causing issue #139
        }



        corDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots)[digitIndex]);

        // Setup the Zero features used for the MATH_SCAFFOLD_BEHAVIOR Prompts
        //
        clearZeroFeatures();
        setZeroFeatures();

        nextPlaceValue();

    }


    /**
     *  Reset the MATH_SCAFFOLD_BEHAVIOR prompt features
     *
     */
    private void clearZeroFeatures() {

        delAddFeature(ASM_CONST.FTR_NO_ZEROS, null);
        delAddFeature(ASM_CONST.FTR_OP_ZERO, null);
        delAddFeature(ASM_CONST.FTR_ZERO_OP, null);
        delAddFeature(ASM_CONST.FTR_ALL_ZEROS, null);
    }


    /**
     * Determine whether there are zero valued operands to inform the MATH_SCAFFOLD_BEHAVIOR
     * feedback.
     *
     */
    private void setZeroFeatures() {

        int zeroCnt     = 0;
        int operandPosn = 0;
        int zeroPosn    = 0;

        // scan the current placevalue (i.e. digitIndex) of the operands for zeros
        //
        for(String[] placeValues : problemDigits) {

            if(placeValues[digitIndex].equals("0")) {
                zeroCnt++;
                zeroPosn = operandPosn;
            }
            operandPosn++;
        }

        if(zeroCnt == problemDigits.size()) {
            delAddFeature(null, ASM_CONST.FTR_ALL_ZEROS);
        }
        else if(zeroCnt == 0) {
            delAddFeature(null, ASM_CONST.FTR_NO_ZEROS);
        }
        else if(zeroPosn > 0) {
            delAddFeature(null, ASM_CONST.FTR_OP_ZERO);
        }
        else {
            delAddFeature(null, ASM_CONST.FTR_ZERO_OP);
        }
    }


    private void nextPlaceValue() {
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



    // --------------------------------------
    // --------- SHOW DOT BAGS --------------
    // --------------------------------------

    public void setDotBagsVisible(Boolean _dotbagsVisible) {

      //  zoomOperator();
        setDotBagsVisible(_dotbagsVisible, digitIndex);
    }


    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex) {


        setDotBagsVisible(_dotbagsVisible, curDigitIndex, 0);
    }


    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex, int startRow) {

        if (curDigitIndex != digitIndex) return;

        if (System.currentTimeMillis() - startTime < 3000 && _dotbagsVisible) return;

        if (_dotbagsVisible && !hasShown && !isWriting) {

            animateTutorial(curDigitIndex, startRow, 1000);

        } else if(!_dotbagsVisible){
            for (int alley = 0; alley < allAlleys.size(); alley++)
                allAlleys.get(alley).getDotBag().setVisibility(INVISIBLE);
        } else
            return;

        dotbagsVisible = _dotbagsVisible;

        final CAsm_Component xyz = this;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                logLayoutTree(xyz, "");
            }
        });
    }


    // --------------------------------------
    // --------- SHOW TUTORIAL --------------
    // --------------------------------------

    public void animateTutorial(Integer pace) {
        animateTutorial(digitIndex, digitIndex, pace);
    }


    public void animateTutorial(int curDigitIndex, int startRow, int pace) {

        if(curOverheadCol >= 0) {

            if ((allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(0).getText().equals("") // √√√
                    || allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(0).getCurrentTextColor() == Color.RED) && curOverheadCol > 9) { // √√√
                ASM_CONST.logAnnoyingReference(curOverheadCol, digitIndex, 0, "check if blank or red");
                mechanics.highlightOverhead();
                return;
            } else if (allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getText().equals("") // √√√
                    || allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getCurrentTextColor() == Color.RED) { // √√√
                ASM_CONST.logAnnoyingReference(curOverheadCol, digitIndex, 1, "check if blank or red");
                mechanics.highlightOverhead();
                return;
            } else {
                curOverheadCol = -1;
            }
        }

        hasShown = true;

        int delayTime = 0;

        startRow = startRow >= 0? startRow : 0;
        int lastRow = allAlleys.size();

        for (int i = startRow; i < lastRow; i++) {

            final CAsm_Alley curAlley = allAlleys.get(i);
            final int _curDigitIndex = curDigitIndex;

                delayTime = wiggleDigitAndDotbag(curAlley, delayTime, _curDigitIndex, startRow, pace);
        }

        if (!dotbagsVisible)
            mechanics.preClickSetup();
    }

    private int wiggleDigitAndDotbag(final CAsm_Alley curAlley, int delayTime, final int curDigitIndex, int startRow, int pace) {

        final CAsm_DotBag curDB = curAlley.getDotBag();
        final CAsm_TextLayout curTextLayout;


        curTextLayout = curAlley.getTextLayout().getTextLayout(digitIndex); // √√√
        ASM_CONST.logAnnoyingReference(curAlley.getId(), digitIndex, -1, "wiggleDigitAndDotBag");


        CAsm_Text curText = curTextLayout.getText(1); // √√√
        ASM_CONST.logAnnoyingReference(curAlley.getId(), digitIndex, 1, "wiggleDigitAndDotBag");

        if (!curText.getText().equals("") && !curText.getIsStruck()) {
            clickPaused = true;
            Handler h = new Handler();

            //wiggle operator
            if (allAlleys.indexOf(curAlley) == ASM_CONST.OPERATOR_ROW - 1) {
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(curDigitIndex != digitIndex) return;
                        curAlley.getTextLayout().getTextLayout(0).getText(1).wiggle(300, 1, 0, .5f); // √√√
                        ASM_CONST.logAnnoyingReference(curAlley.getId(), 0, 1, "wiggle");
                    }
                }, delayTime);
                delayTime += pace;
            }

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (curDigitIndex != digitIndex) return;
                    clickPaused = false;
                    curDB.setVisibility(VISIBLE);
                    curDB.wiggle(300, 1, 0, .05f);
                    curTextLayout.getText(0).wiggle(300, 1, 0, .3f); // √√√
                    ASM_CONST.logAnnoyingReference(curAlley.getId(), digitIndex, 0, "wiggle()");
                    curTextLayout.getText(1).wiggle(300, 1, 0, .3f); // √√√
                    ASM_CONST.logAnnoyingReference(curAlley.getId(), digitIndex, 1, "wiggle()");
                }
            }, delayTime);
            delayTime += pace;
        } else if (!curText.getIsStruck())
            curDB.setVisibility(VISIBLE);

        return delayTime;
    }



    // -------------------------------------------
    // --------- EMPHASIZE OPERATOR --------------
    // -------------------------------------------

    public void zoomOperator() {

        Handler           h = new Handler();
        final CAsm_Alley  opAlley = allAlleys.get(ASM_CONST.OPERATOR_ROW - 1);

        pointAtOperator();

        //wiggle operator
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                CAnimatorUtil.zoomInOut(opAlley.getTextLayout().getTextLayout(0).getText(1), 2.5f, 1500L); // √√√
                ASM_CONST.logAnnoyingReference(ASM_CONST.OPERATOR_ROW - 1, 0, 1, "zoomInOut");
            }
        }, 20);
    }



    public void pointAtOperator() {

        CAsm_Alley  opAlley  = allAlleys.get(ASM_CONST.OPERATOR_ROW - 1);
        View        operator = opAlley.getTextLayout().getTextLayout(0).getText(1); // √√√
        ASM_CONST.logAnnoyingReference(ASM_CONST.OPERAND_ROW - 1, 0, 1, "pointAtOperator");

        operator.getLocationOnScreen(_screenCoord);

        PointF targetPoint = new PointF(_screenCoord[0] + operator.getWidth(), _screenCoord[1] + operator.getHeight());

        // Let the persona know where to look
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        bManager.sendBroadcast(msg);
    }


    public void playChime() {

    }

    public void resetPlaceValue() {
        placeValIndex = -1;
    }



    // MATHFIX_CLEAN called by T
    /**
     * Compares the digit written in the box to the Whole expected answer. Note that this will only ever
     * return true for answers with 1 digit. For >1 digit numbers, "corValue" will always be >1 digit,
     * but ans will always be one digit.
     * @return
     */
    public boolean isWholeCorrect(String character) {

        int ans;

        ans = allAlleys.get(numAlleys - 1).getNum(); // MATHFIX_WRITE are you kidding me... why not use the event that you *just got*

        Log.wtf("Y_U_DO_THIS", String.format(Locale.US, "compare character=%s to ans=%s to corValue=%s", character, ans, corValue)); // MATHFIX_WRITE these will be the same

        return corValue.equals(ans);

    }


    // ------------------------------------------------------------
    // --------- called by TComponent... tutor logic --------------
    // ------------------------------------------------------------

    // MATHFIX_CLEAN called by T
    public boolean isDigitCorrect() {

        overheadCorrect = ASM_CONST.NO_INPUT;
        resultCorrect   = ASM_CONST.NO_INPUT;
        boolean isOverheadCorrect, bottomCorrect;

        CAsm_TextLayout textLayout;

        textLayout = allAlleys.get(numAlleys - 1).getTextLayout(); // √√√
        ASM_CONST.logAnnoyingReference(numAlleys - 1, -1, -1, "isDigitCorrect");

        // first check bottom answer
        int writtenDigit = textLayout.getDigit(digitIndex);
        bottomCorrect = corDigit.equals(writtenDigit);

        // if the bottom was not correct, and the bottom was not null
        if (!bottomCorrect && textLayout.getDigit(digitIndex) != null) {
            // then
            wrongDigit(textLayout.getTextLayout(digitIndex).getText(1)); // √√√
            ASM_CONST.logAnnoyingReference(numAlleys - 1, digitIndex, 1, "wrongDigit(this)");
            resultCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
        }

        // now check overhead answer
        if (overheadVal != null && overheadVal <= corValue) {
            if (overheadVal < 10)
                isOverheadCorrect = overheadVal.equals(overheadText.getDigit());
            else if (overheadTextSupplement.getDigit() == null || overheadText.getDigit() == null)
                isOverheadCorrect = false;
            else
                isOverheadCorrect = overheadVal.equals(overheadTextSupplement.getDigit() * 10 + overheadText.getDigit());

            if (isOverheadCorrect) {
                mechanics.correctOverheadText();
                overheadCorrect = ASM_CONST.ALL_INPUT_RIGHT;
            } else if (overheadVal < 10 ) {
                if (overheadText.getDigit() != null) {
                    wrongDigit(overheadText);
                    overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                } else
                    overheadCorrect = ASM_CONST.NO_INPUT;
            } else {
                boolean allRight = true, allEmpty = true;

                if (overheadTextSupplement.getDigit() != null) {
                    if (overheadTextSupplement.getDigit() != overheadVal / 10) {
                        wrongDigit(overheadTextSupplement);
                        overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                        allRight = false;
                    } else
                        overheadTextSupplement.cancelResult();
                    allEmpty = false;
                }

                if (overheadText.getDigit() != null) {
                    if (overheadText.getDigit() != overheadVal % 10) {
                        wrongDigit(overheadText);
                        overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                        allRight = false;
                    } else
                        overheadText.cancelResult();
                    allEmpty = false;
                }

                if (allRight) overheadCorrect = ASM_CONST.ALL_INPUT_RIGHT;
                if (allEmpty) overheadCorrect = ASM_CONST.NO_INPUT;
            }
        }

        return bottomCorrect;
    }

    private void wrongDigit(final CAsm_Text t) {
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
                    setDotBagsVisible(true, digitIndex, 0);
                }
            }, 1500);
    }

    /**
     * CALLED BY ANIMATOR_GRAPH
     */
    public void highlightCurrentColumn() {
        //Highlights user's active column.
        for (CAsm_Alley alley: allAlleys) {
            try {
                CAsm_Text text = alley.getTextLayout().getTextLayout(digitIndex).getText(1); // √√√
                ASM_CONST.logAnnoyingReference(alley.getId(), digitIndex, 1, "highlightCurrentColumn");
                if (text.getDigit() != null || text.isWritable) {highlightText(text); }
            } catch (NullPointerException e) { continue;}
        }
    }


    /**
     * CALLED BY
     *  THIS
     *  MECHANIC_BASE/SUBTRACT
     * @param t
     */
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
                    t.setResult();
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                if (t.isWritable) {
                    t.setResult();
                }
            }
        });
        v.start();
    }



    /**
     * CALLED BY MECHANIC_BASE
     */
    public void updateText(CAsm_Text t1, CAsm_Text t2, boolean isClickingBorrowing) {

        isWriting = true;

        if (!mPopup.isActive && !mPopupSupplement.isActive) {

            applyBehavior(ASM_CONST.START_WRITING_BEHAVIOR); // MATHFIX_WRITE this prevents scaffolding from popping up

            // MATHFIX_WRITE is there a simpler way to do this?
            ArrayList<ICharRecListener_Simple> listeners = new ArrayList<>();

            listeners.add(t2);
            listeners.add(this);

            mPopup.showAtLocation(this, Gravity.LEFT, 10, 10); // MATHFIX_WRITE show popup MATHFIX_LAYOUT this should be in Layout
            mPopup.enable(true, listeners);                          // MATHFIX_WRITE listeners contain the CAsm_Text (which updates its value) and this (which detects if choice was correct)

            if(isClickingBorrowing) {
                mPopup.update(t2, 120, -500, 500, 500);

                mPopup.setExpectedDigit(overheadVal.toString());
                Log.d(TAG, "Correct Carry Digit: " + overheadVal.toString());
            }
            else {
                mPopup.update(t2, 60, 20, 500, 500);

                mPopup.setExpectedDigit(corDigit.toString());    // MATHFIX_WRITE NEXT NEXT NEXT does this get used?
                Log.d(TAG, "Correct Answer Digit: " + corDigit.toString());
            }

            mPopup.isActive = true;

            if (t1 != null) {
                hasTwoPopup = true;
                listeners = new ArrayList<>();
                listeners.add(t1);
                listeners.add(this);

                mPopupSupplement.showAtLocation(this, Gravity.LEFT, 10, 10);
                mPopupSupplement.enable(true, listeners);

                if (isClickingBorrowing) {
                    mPopup.update(t2, 920, -500, 500, 500);
                    mPopupSupplement.update(t2, 120, -500, 500, 500);
                } else {
                    mPopup.update(t2, 560, 0, 500, 500);
                    mPopupSupplement.update(t2, 60, 20, 500, 500);
                }

                mPopupSupplement.isActive = true;
            }
        }
    }


    /**
     * Exit the write box.
     *
     * CALLED BY
     *  THIS
     *  MECHANIC_BASE
     */
    public void exitWrite() {

        mPopup.isActive = false;
        mPopup.enable(false,null);
        mPopup.dismiss();

        mPopupSupplement.isActive = false;
        mPopupSupplement.enable(false,null);
        mPopupSupplement.dismiss();


        // restart the hesitation timer
        // formerly the method restartHesitationTimer()
        int delayTime = 3000;
        if(isWriting && !hasShown) {

            startTime = System.currentTimeMillis();
            isWriting = false;

            postEvent(ASM_CONST.SHOW_SCAFFOLD_BEHAVIOR, delayTime);
        }

    }



    // ---------------------------------------------
    // --------- on char recognition ---------------
    // ---------------------------------------------

    // MATHFIX_WRITE NEXT can this be simpler?
    // MATHFIX_WRITE NEXT can we change the digit from here?
    @Override
    public void charRecCallback(String character) {

        if (!hasTwoPopup) {
            mPopup.reset();
            mPopupSupplement.reset();
        } else if (!overheadText.getText().equals("") && overheadTextSupplement.getText().equals("")) {
            mPopup.reset();
            hasTwoPopup = false;
        } else if (overheadText.getText().equals("") && !overheadTextSupplement.getText().equals("")) {
            mPopupSupplement.reset();
            hasTwoPopup = false;
        }

    }


    // -----------------------------------------------------
    // --------- touches handled by MECHANIC ---------------
    // -----------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mechanics.handleClick();
        }
        return true;
    }



    // --------------------------------------------------
    // --------- overridden by TComponent ---------------
    // --------------------------------------------------
    public void delAddFeature(String delFeature, String addFeature) {
    }


    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _name;
        protected String _command;
        protected String _target;
        protected String _item;


        public Queue(String name, String command) {

            _name    = name;
            _command = command;

            if(name != null) {
                Log.d(TAG, "Post Requested: " + name + " - Command: " + command);

                nameMap.put(name, this);
            }
        }

        public Queue(String name, String command, String target) {

            this(name, command);
            _target  = target;
        }

        public Queue(String name, String command, String target, String item) {

            this(name, command, target);
            _item    = item;
        }


        public String getCommand() {
            return _command;
        }


        @Override
        public void run() {

            try {
                if(_name != null) {
                    nameMap.remove(_name);
                }

                queueMap.remove(this);

                switch(_command) {

                    case ASM_CONST.MATH_INSTRUCTION_ADD_BEHAVIOR:
                    case ASM_CONST.MATH_INSTRUCTION_ADD_0_BEHAVIOR:
                    case ASM_CONST.MATH_INSTRUCTION_0_ADD_BEHAVIOR:
                    case ASM_CONST.MATH_INSTRUCTION_SUB_BEHAVIOR:
                    case ASM_CONST.MATH_INSTRUCTION_SUB_0_BEHAVIOR:

                        applyBehaviorNode(_command);
                        break;

                    case ASM_CONST.SHOW_SCAFFOLD_BEHAVIOR:
                    case ASM_CONST.CHIME_FEEDBACK:
                    case ASM_CONST.SCAFFOLD_RESULT_BEHAVIOR:
                    case ASM_CONST.INPUT_BEHAVIOR:
                    case ASM_CONST.NEXT_NODE:

                        applyBehavior(_command);
                        break;

                    default:
                        break;
                }


            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }
        }
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

            Log.d(TAG, "Post Cancelled on Flush: " + ((Queue)entry.getValue()).getCommand());

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Remove named posts
     *
     */
    public void cancelPost(String name) {

        Log.d(TAG, "Cancel Post Requested: " + name);

        while(nameMap.containsKey(name)) {

            Log.d(TAG, "Post Cancelled: " + name);

            mainHandler.removeCallbacks((Queue) (nameMap.get(name)));
            nameMap.remove(name);
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
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

    public void postNamed(String name, String command, String target) {
        postNamed(name, command, target, 0L);
    }

    public void postNamed(String name, String command, String target, Long delay) {
        enQueue(new Queue(name, command, target), delay);
    }

    public void postNamed(String name, String command) {
        postNamed(name, command, 0L);
    }
    public void postNamed(String name, String command, Long delay) {
        enQueue(new Queue(name, command), delay);
    }


    /**
     * Post a command to the queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command, command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new Queue(null, command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new Queue(null, command, target, item), delay); }


    public void postEvent(String event) {

        postEvent(event,0);
    }

    public void postEvent(String event, Integer delay) {

        post(event, delay);
    }

    public void postEvent(String event, String param, Integer delay) {

        post(event, param, delay);
    }

    public void postEvent(String event, String target) {

        post(event, target);
    }


    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // IPublisher - START


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishState() {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishValue(String varName, String value) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishValue(String varName, int value) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishFeatureSet(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void retractFeatureSet(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishFeature(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void retractFeature(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishFeatureMap(HashMap featureMap) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void retractFeatureMap(HashMap featureMap) {
    }


    // IPublisher - EBD
    //************************************************************************
    //************************************************************************




    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START


    @Override
    public void setVolatileBehavior(String event, String behavior) {

    }

    @Override
    public void setStickyBehavior(String event, String behavior) {

    }

    @Override
    public boolean applyBehavior(String event) {
        return false;
    }

    @Override
    public void applyBehaviorNode(String nodeName) {

    }

    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************




    // *** Serialization


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