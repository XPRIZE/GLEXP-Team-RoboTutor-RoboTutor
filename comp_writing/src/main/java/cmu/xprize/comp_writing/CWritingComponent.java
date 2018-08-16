//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************
/*
 * Edited and commented by Kevin DeLand
 */

package cmu.xprize.comp_writing;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.ltkplus.CGlyphMetricConstraint;
import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.CGlyphSet;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.CClassMap;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.EMPTY;


/**
 * TODO: document your custom view class.
 *
 *  !!!! NOTE: This requires com.android.support:percent:23.2.0' at least or the aspect ratio
 *  settings will not work correctly.
 *
 */
public class CWritingComponent extends PercentRelativeLayout implements IEventListener, IEventDispatcher, IWritingComponent, ILoadableObject, IPublisher, ITutorLogger {

    protected Context               mContext;
    protected char[]                mStimulusData;
    private   List<IEventListener>  mListeners = new ArrayList<IEventListener>();

    protected CLinkedScrollView mRecognizedScroll;
    protected CLinkedScrollView mResponseViewScroll;
    protected CLinkedScrollView mDrawnScroll;
    private   IGlyphController  mActiveController;
    protected int               mActiveIndex;

    protected ImageButton       mReplayButton;

    protected LinearLayout      mRecogList;
    protected LinearLayout      mResponseViewList; //amogh added
    protected LinearLayout      mGlyphList;
    protected LinearLayout      mGlyphAnswerList;
    protected RelativeLayout    mResponseScrollLayout;

    protected View mHighlightErrorBoxView;

    protected int               mMaxLength   = 0; //GCONST.ALPHABET.length();                // Maximum string length

    protected final Handler     mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap           queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    protected boolean           _alwaysTrack = true;
    protected int               _fieldIndex  = 0;
    protected String            _replayType;
    protected boolean           _isDemo;

    protected IGlyphSink        _recognizer;
    protected CGlyphSet         _glyphSet;

    protected boolean           _charValid;
    protected boolean           _metricValid;
    protected boolean           _isValid;
    protected ArrayList<String> _attemptFTR = new ArrayList<>();
    protected ArrayList<String> _hesitationFTR = new ArrayList<>(); //amogh added
    private int                     _hesitationNo      = 0; //amogh added

    protected String            mResponse;
    protected String            mStimulus;
    protected String[]          mAudioStimulus;
    protected String            mAnswer;

    protected CGlyphMetricConstraint _metric = new CGlyphMetricConstraint();

    protected List<CWr_Data>      _data;
    protected int               _dataIndex = 0;
    protected boolean           _dataEOI   = false;

    public    boolean           _immediateFeedback = false;

    protected LocalBroadcastManager bManager;

    protected String            activityFeature; // features of current activity e.g. FTR_LETTERS:FTR_DICTATION


    // json loadable
    public String               bootFeatures = EMPTY;
    public boolean              random       = false;
    public boolean              singleStimulus = false;
    public CWr_Data[]           dataSource;

    //amogh added for sentence correction
    protected List<Integer>     _spaceIndices = new ArrayList<Integer>(); //indices with space for
    protected int currentWordIndex = 0;

    protected  List<Integer>    deleteCorrectionIndices = new ArrayList<>();
    protected  List<Integer>    insertCorrectionIndices = new ArrayList<>();
    protected  List<Integer>    changeCorrectionIndices = new ArrayList<>();
    protected  List<Integer>    currentWordIndices = new ArrayList<>();


    protected HashMap<String,HashMap<String,ArrayList<Integer>>>  corrections = new HashMap<>();
    protected HashMap<Integer,ArrayList<Integer>> wordIndices = new HashMap<>();
    protected HashMap<Integer,Boolean> wordStatus = new HashMap<>();


//    protected  List<Integer>    deleteCorrectionIndices = new ArrayList<>(Arrays.asList());
//    protected  List<Integer>    insertCorrectionIndices = new ArrayList<>(Arrays.asList());
//    protected  List<Integer>    changeCorrectionIndices = new ArrayList<>(Arrays.asList());

    protected int correctionAttempts = 0;
    //amogh add ends

    final private String  TAG        = "CWritingComponent";

    // This is for performance tag.
    // TODO: Need to update datasource to contain these data.
    protected String task;
    protected String level;

    public CWritingComponent(Context context) {
        super(context);
        init(context, null);
    }

    public CWritingComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CWritingComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        mContext = context;

        setClipChildren(false);

        // initialize with four features
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_1);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_2);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_3);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_4);

        //amogh added
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_1);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_2);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_3);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_4);
        //amogh added finished

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    /** Note: This is used with the writing_tutor_comp layout in the dev project
     *  The resource names are different at the momoent
     *
     */
    public void onCreate() {

        CGlyphController v;
        CStimulusController r;

        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //       In normal operation mMaxLength is zero here.
        //
        mStimulusData = new char[mMaxLength];

        // Setup the Recycler for the recognized input views
        mRecognizedScroll = (CLinkedScrollView) findViewById(R.id.Sresponse);
        mRecogList = (LinearLayout) findViewById(R.id.Srecognized_glyphs);
        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //
        for(int i1 =0 ; i1 < mStimulusData.length ; i1++)
        {
            // create a new view
            r = (CStimulusController)LayoutInflater.from(getContext())
                                        .inflate(R.layout.recog_resp_comp, null, false);

            mRecogList.addView(r);
        }
        mRecogList.requestLayout();

        //****************************

        mDrawnScroll = (CLinkedScrollView) findViewById(R.id.SfingerWriter);
        mDrawnScroll.setClipChildren(false);

        mGlyphList = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
        mGlyphList.setClipChildren(false);

        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //
        for(int i1 = 0 ; i1 < mStimulusData.length ; i1++)
        {
            // create a new view
            v = (CGlyphController)LayoutInflater.from(getContext())
                                        .inflate(R.layout.drawn_input_comp, null, false);

            v.setIsLast(i1 ==  mStimulusData.length-1);

            mGlyphList.addView(v);
            ((CGlyphController)v).setLinkedScroll(mDrawnScroll);
            ((CGlyphController)v).setWritingController(this);
        }

        // Obtain the prototype glyphs from the singleton recognizer
        //
        _recognizer = CRecognizerPlus.getInstance();
        _glyphSet   = _recognizer.getGlyphPrototypes(); //new GlyphSet(TCONST.ALPHABET);

        // Note: this is used in the GlyphRecognizer project to initialize the sample
        //
        for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

            CGlyphController comp = (CGlyphController) mGlyphList.getChildAt(i1);

            String expectedChar = mStimulus.substring(i1,i1+1);

            comp.setExpectedChar(expectedChar);
            comp.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar));
        }

// TODO: Dev only
//        mRecogList.setOnTouchListener(new RecogTouchListener());
//        mGlyphList.setOnTouchListener(new drawnTouchListener());

        mRecognizedScroll.setLinkedScroll(mDrawnScroll);
        mDrawnScroll.setLinkedScroll(mRecognizedScroll);
    }


    public void onDestroy() {

        terminateQueue();
    }


    private void broadcastMsg(String Action) {

        Intent msg = new Intent(Action);

        bManager.sendBroadcast(msg);
    }

    //amogh added to initialise the correction hashmap
    public void initialiseCorrectionHashMap() {
        ArrayList<Integer> replaceCapitalize= new ArrayList<Integer>(Arrays.asList(0,4));
        ArrayList<Integer> insertPunctuationIndices = new ArrayList<Integer>(Arrays.asList(2,21));
        ArrayList<Integer> replaceCapitalizeIndices = new ArrayList<Integer>(Arrays.asList(0,4));
        HashMap<String,ArrayList<Integer>> insertMap = new HashMap<String, ArrayList<Integer>>();
        HashMap<String,ArrayList<Integer>> replaceMap = new HashMap<String, ArrayList<Integer>>();
        insertMap.put("Punctuation",insertPunctuationIndices);
        replaceMap.put("Capitalize",replaceCapitalizeIndices);
        corrections.put("Insert", insertMap);
        corrections.put("Replace", replaceMap);
    }

    public void initialiseWordIndices(){
        ArrayList<Integer> Indices;
        wordIndices.put(0,new ArrayList<Integer>(Arrays.asList(0,1,2,3)));
        wordIndices.put(1,new ArrayList<Integer>(Arrays.asList(4,5,6,7,8,9,10,11,12,13)));
        wordIndices.put(2,new ArrayList<Integer>(Arrays.asList(14,15,16)));
        wordIndices.put(3,new ArrayList<Integer>(Arrays.asList(17,18,19,20,21)));
    }

    public void updateWord(){
        ArrayList<Integer> wordInd= wordIndices.get(currentWordIndex);
        for (int i : wordInd){
            CStimulusController respController = (CStimulusController) mResponseViewList.getChildAt(i);
            CGlyphController g = (CGlyphController) mGlyphList.getChildAt(i);
            Boolean stat = g.isCorrect();

            respController.updateStimulusState(stat);

        }
    }

    //amogh add initialising hashmap ends
    //amogh added to check animator graph

    public void showHighlightBox2(){
        mHighlightErrorBoxView = new View (getContext());
        mHighlightErrorBoxView.setLayoutParams(new LayoutParams(60,90));
//        mHighlightErrorBoxView.setId();
        mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
//        MarginLayoutParams mp = (MarginLayoutParams) mHighlightErrorBoxView.getLayoutParams();
//        mp.setMargins(100,00,0,100);
//                mHighlightErrorBoxView.setX((float)300.00);
//        int pos = mResponseViewList.getChildAt(index+2).getLeft();
        mHighlightErrorBoxView.setX(100);
//        mHighlightErrorBoxView.setLeft(1000);
        mResponseScrollLayout.addView(mHighlightErrorBoxView);
        mHighlightErrorBoxView.postDelayed(new Runnable() {
            public void run() {
                mHighlightErrorBoxView.setVisibility(View.GONE);
            }
        }, 5000);
    }
    public void showHighlightBox(Integer level){
        mHighlightErrorBoxView = new View (getContext());
//        int level = 0;    protected List<Integer>     _spaceIndices = new ArrayList<Integer>();

        int wid = 0;
        switch (level){
            case 1:
//                break;
            case 2:
                ArrayList<Integer> wordInd = wordIndices.get(currentWordIndex);
                int leftLetter = wordInd.get(0);
                int left = mResponseViewList.getChildAt(leftLetter).getLeft();
                int rightLetter = wordInd.get(wordInd.size()-1);
                int right = mResponseViewList.getChildAt(rightLetter-1).getRight();
                wid = right-left;
                mHighlightErrorBoxView.setX((float)left);
                break;
            case 3:
//                int left = mResponseViewList.getChildAt(0).getLeft();
//                int right = mResponseViewList.getChildAt(mResponseViewList.getChildCount()-1).getRight();
//                wid = right-left;
//                mHighlightErrorBoxView.setX((float)left);
                wordInd = wordIndices.get(currentWordIndex);
                leftLetter = wordInd.get(0);
                left = mResponseViewList.getChildAt(leftLetter).getLeft();
                rightLetter = wordInd.get(1);
                right = mResponseViewList.getChildAt(rightLetter-1).getRight();
                wid = right-left;
                mHighlightErrorBoxView.setX((float)left);
                break;
            case 4:
                break;
        }
        mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid,90));
//        mHighlightErrorBoxView.setId();
        mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
//        MarginLayoutParams mp = (MarginLayoutParams) mHighlightErrorBoxView.getLayoutParams();
//        mp.setMargins(100,00,0,100);
//                mHighlightErrorBoxView.setX((float)300.00);
//        int pos = mResponseViewList.getChildAt(index+2).getLeft();
//        mHighlightErrorBoxView.setX(100);
//        mHighlightErrorBoxView.setLeft(1000);
        mResponseScrollLayout.addView(mHighlightErrorBoxView);
        mHighlightErrorBoxView.postDelayed(new Runnable() {
            public void run() {
                mHighlightErrorBoxView.setVisibility(View.GONE);
            }
        }, 5000);
    }
    //amogh added ends

    //************************************************************************
    //************************************************************************
    // IWritingController Start

    /**
     * Note that only the mGlyphList will initiate this call
     *
     * @param child
     */
    //amogh edited
    public void deleteItem(View child) {
        int index = mGlyphList.indexOfChild(child);
        if (! deleteCorrectionIndices.contains(index)) {
            mGlyphList.removeViewAt(index);
            mResponseViewList.removeViewAt(index);
        }
        else{
            correctionAttempts++;
            if (correctionAttempts > 2){
                mHighlightErrorBoxView = new View (getContext());
                int wid = mResponseViewList.getChildAt(index+3).getLeft() - mResponseViewList.getChildAt(index+2).getLeft();
                mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid,90));
//        mHighlightErrorBoxView.setId();
                mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
//        MarginLayoutParams mp = (MarginLayoutParams) mHighlightErrorBoxView.getLayoutParams();
//        mp.setMargins(100,00,0,100);
//                mHighlightErrorBoxView.setX((float)300.00);
                int pos = mResponseViewList.getChildAt(index+2).getLeft();
                mHighlightErrorBoxView.setX((float)pos);
//        mHighlightErrorBoxView.setLeft(1000);
                mResponseScrollLayout.addView(mHighlightErrorBoxView);
                mHighlightErrorBoxView.postDelayed(new Runnable() {
                    public void run() {
                        mHighlightErrorBoxView.setVisibility(View.GONE);
                    }
                }, 2000);
            }
        }
//        mRecogList.removeViewAt(index); //amogh commented to avoid removal from stimulus
    }


    /**
     * Note that only the mGlyphList will initiate this call
     *
     * @param child
     */
    public void addItemAt(View child, int inc) {

        CGlyphController v;
        CStimulusController r;

        int index = mGlyphList.indexOfChild(child);

        // create a new view

//        r = (CStimulusController)LayoutInflater.from(getContext())  //amogh commented to avoid removal from stimulus
//                                    .inflate(R.layout.recog_resp_comp, null, false);   //amogh commented to avoid removal from stimulus
////amogh commented to avoid removal from stimulus
//        mRecogList.addView(r, index + inc);          //amogh commented to avoid removal from stimulus
//
//        r.setLinkedScroll(mDrawnScroll);         //amogh commented to avoid removal from stimulus
//        r.setWritingController(this);          //amogh commented to avoid removal from stimulus

//amogh added for adding a view to the response view also, and also to check if the letter
        if (!insertCorrectionIndices.contains(index+inc)) {

            r = (CStimulusController) LayoutInflater.from(getContext())
                    .inflate(R.layout.recog_resp_comp, null, false);
            mResponseViewList.addView(r, index + inc);

            r.setLinkedScroll(mDrawnScroll);
            r.setWritingController(this);
            //amogh added ends

            // create a new view
            v = (CGlyphController) LayoutInflater.from(getContext())
                    .inflate(R.layout.drawn_input_comp, null, false);

            // Update the last child flag
            //
            if (index == mGlyphList.getChildCount() - 1) {
                ((CGlyphController) child).setIsLast(false);
                v.setIsLast(true);
            }

            mGlyphList.addView(v, index + inc);

            v.setLinkedScroll(mDrawnScroll);
            v.setWritingController(this);
        }
    }


    /**
     *
     */
    public void clear() {
        // Add the recognized response display containers
        //
        mRecogList.removeAllViews();

        // Add the Glyph input containers
        //
        mGlyphList.removeAllViews();
    }

    public boolean updateStatus(IGlyphController glyphController, CRecResult[] _ltkPlusCandidates) {

        mActiveController = glyphController;

        mActiveIndex = mGlyphList.indexOfChild((View) mActiveController);

        CRecResult candidate = _ltkPlusCandidates[0];
        CStimulusController stimController = (CStimulusController) mRecogList.getChildAt(mActiveIndex);
        CGlyphController gController = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);

        publishValue(WR_CONST.CANDIDATE_VAR, candidate.getRecChar().toLowerCase());
        publishValue(WR_CONST.EXPECTED_VAR, mActiveController.getExpectedChar().toLowerCase());


        // Avoid caseSensitive for words activity
        boolean isAnswerCaseSensitive = true;

        boolean isWordActivity = activityFeature.contains("FTR_WORDS");
        boolean isMissingLtrActivity = activityFeature.contains("FTR_MISSING_LTR");

        if (isWordActivity || isMissingLtrActivity) {
            isAnswerCaseSensitive = false;
        }

        // Check answer
        mResponse = candidate.getRecChar();
        _charValid = gController.checkAnswer(mResponse, isAnswerCaseSensitive);

        _metricValid = _metric.testConstraint(candidate.getGlyph(), this);
        _isValid = _charValid && _metricValid; // _isValid essentially means "is a correct drawing"

        //amogh added to set the valid character in response.

        CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex);
        String charExpected = gController.getExpectedChar();
        resp.setStimulusChar(mResponse, false);
//            updateResponseView(mResponse);
        //amogh added to handle spacing.
        if (_isValid) {

            if (_spaceIndices.contains(mActiveIndex - 1)) {
                CGlyphController gControllerSpace = (CGlyphController) mGlyphList.getChildAt(mActiveIndex - 1);
                CStimulusController respSpace = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex - 1);
                respSpace.setStimulusChar("", false);
                gControllerSpace.setIsStimulus("");
                gControllerSpace.updateCorrectStatus(_isValid);
            }
        } else {
//            mActiveController.
        }
        //amogh add ends

        if (!activityFeature.contains("FTR_SEN_WRD")){
            // Update the controller feedback colors
            //
            mActiveController.updateCorrectStatus(_isValid);


            if (!singleStimulus) {
                stimController.updateStimulusState(_isValid);
            }

            // Depending upon the result we allow the controller to disable other fields if it is working
            // in Immediate feedback mode
            // TODO: check if we need to constrain this to immediate feedback mode
            //
            inhibitInput(mActiveController, !_isValid);

            // Publish the state features.
            //
            publishState();

            // Fire the appropriate behavior
            //
            if (isComplete()) {

                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
            }
            else {

                if (!_isValid) {

                    // lots of fun feature updating here
                    publishFeature(WR_CONST.FTR_HAD_ERRORS);

                    int attempt = updateAttemptFeature();

                    if (attempt > 4) {
                        applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                    } else {
                        applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                    }

                    if (!_charValid)
                        applyBehavior(WR_CONST.ON_CHAR_ERROR);
                    else if (!_metricValid)
                        applyBehavior(WR_CONST.ON_METRIC_ERROR);
                } else {
                    updateStalledStatus();

                    applyBehavior(WR_CONST.ON_CORRECT); //goto node "GENERAL_CORRECT_BEHAVIOR"
                }
            }
        }
        // for sentence activities (word level and sentence level feedback)
        else{
//            currentWordIndices.add(mActiveIndex);
            //for immediate feedback
            if(activityFeature.contains("FTR_SEN_LTR")){

            }
            //for word level feedback
            else if(activityFeature.contains("FTR_SEN_WRD")){
                if (_spaceIndices.contains(mActiveIndex - 1)) {

//                    CGlyphController gControllerSpace = (CGlyphController) mGlyphList.getChildAt(mActiveIndex - 1);
//                    CStimulusController respSpace = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex - 1);
//                    respSpace.setStimulusChar("", false);
//                    gControllerSpace.setIsStimulus("");
//                    gControllerSpace.updateCorrectStatus(_isValid);
//                    for (int wordIndex : currentWordIndices){
//                        CGlyphController gControllerWord = (CGlyphController) mGlyphList.getChildAt(wordIndex);//amogh change name
//                        CStimulusController stimulusSpaceWord = (CStimulusController) mRecogList.getChildAt(wordIndex);
//                        gControllerWord.displayCorrectStatus();
//                        stimulusSpaceWord.updateStimulusState(gControllerWord.isCorrect());
//                    }
//                    currentWordIndices.clear();
//                    currentWordIndices.add(mActiveIndex);
                    updateWord();
                }

            }
        }
        return _isValid;
    }

    /**
     * Designed to enable character-level mercy rule on multi-char sequences...
     * This method not used (does not work as intended)
     */
    public void mercyRuleCleanup() {

        mActiveController.updateCorrectStatus(true);
        if(isComplete()) {
            applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
        } else {
            applyBehavior(WR_CONST.ON_CORRECT);
        }
    }


    private void updateStalledStatus() {

        CGlyphController   v;

        retractFeature(WR_CONST.FTR_INPUT_STALLED);

        v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex+1);

        if(v != null) {

            if(!v.getGlyphStarted())
                publishFeature(WR_CONST.FTR_INPUT_STALLED);
        }
    }

    /**
     * Retracts all features that indicate which attempt the student is on
     */
    private void  clearAttemptFeatures() {

        for (String attempt : _attemptFTR) {
            retractFeature(attempt);
        }
    }
    //amogh added
    private void clearHesitationFeatures() {

        for (String hes : _hesitationFTR) {
            retractFeature(hes);
        }
    }
    //amogh added ends

    /**
     * Updates and returns the student attempt. Also returns which attempt the student is on.
     *
     * @return
     */
    private int updateAttemptFeature() {

        clearAttemptFeatures();

        int attempt = mActiveController.incAttempt();

        // only publish attempt feature for first four attempts... next time will activate mercy rule
        if(attempt <= 4)
            publishFeature(_attemptFTR.get(attempt-1));

        return attempt;
    }

    private int updateHesitationFeature() {

        clearHesitationFeatures();

        int hesitationNo = incHesitationNo();

        // only publish attempt feature for first four attempts... next time will activate mercy rule
        if(hesitationNo <= 4)
            publishFeature(_hesitationFTR.get(hesitationNo-1));

        return hesitationNo;
    }

    private void resetHesitationFeature(){
        clearHesitationFeatures();
        resetHesitationNo();
    }
    //amogh added
    private int getHesitationNo() {
        return _hesitationNo;
    }

    private int resetHesitationNo() {
        _hesitationNo = 0;
        return _hesitationNo;
    }

    private int incHesitationNo() {
        if(_hesitationNo < 4) {
            _hesitationNo++;
        }
        return _hesitationNo;
    }
    //amogh added ends

    /**
     * Iterates through each character in a word to see if it's been drawn completely.
     *
     * @return
     */
    private boolean isComplete() {

        boolean result = true;

        for(int i1 = 0 ; i1 < mGlyphList.getChildCount() ; i1++) {

            if(!((CGlyphController)mGlyphList.getChildAt(i1)).isCorrect()) {
                result = false;
                break;
            }
        }
        return result;
    }


    public void resetResponse(IGlyphController child) {

        int index = mGlyphList.indexOfChild((View)child);

        CStimulusController respText = (CStimulusController)mRecogList.getChildAt(index);

        respText.resetStimulusState();
    }


    /**
     * If the user taps the stimulus we try and scroll the tapped char onscreen
     *
     * @param controller
     */
    public void stimulusClicked(CStimulusController controller) {

        CGlyphController   v;
        int index = mResponseViewList.indexOfChild(controller); //amogh edited

        v = (CGlyphController) mGlyphList.getChildAt(index);

        // Capture the initiator status to force the tracker to update in the stimulus field
        //
        if(_alwaysTrack || v.hasGlyph()) {

            mDrawnScroll.captureInitiatorStatus();
            mDrawnScroll.smoothScrollTo(calcOffsetToCenterGlyph(v), 0);
            mDrawnScroll.releaseInitiatorStatus();
        }
    }


    private int calcOffsetToCenterGlyph(CGlyphController glyph) {

        int  newScroll = 0;

        int sc = mDrawnScroll.getWidth() / 2;
        int gc = glyph.getWidth() / 2;
        int gx = (int) glyph.getX();

        newScroll = gx - (sc - gc);

        return newScroll;
    }



    private int calcOffsetToMakeGlyphVisible(int scrollX, int padding) {

        int                i1 = 0;
        int                newScroll = 0;
        int                skip = padding;
        CGlyphController   v;

        for(i1 = 0 ; i1 < mGlyphList.getChildCount() ; i1++) {

            v = (CGlyphController) mGlyphList.getChildAt(i1);

            newScroll = (int) v.getX();

            if(skip <= 0 )
                break;

            // once we find a view that is visible we skip past it by skip count if possible
            //
            if(newScroll >= scrollX) {

                skip--;
            }
        }

        return newScroll;
    }


    public void autoScroll(IGlyphController glyphController) {

        CGlyphController view    = (CGlyphController) glyphController;
        int              padding = 2;

        if(view != null) {

            int sx = mDrawnScroll.getScrollX();
            int sw = mDrawnScroll.getWidth();

            int gx = (int) view.getX();
            int gw = view.getWidth() * 2;

            // If the glyph to the right of the current glyph is partially obscurred then calc
            // the offset to bring it on screen - with some padding (i.e. multiple glyph widths)
            // Capture the initiator status to force the tracker to update in the stimulus field
            //
            if((gx+gw) > (sx + sw)) {

                mDrawnScroll.captureInitiatorStatus();
                mDrawnScroll.smoothScrollTo(calcOffsetToMakeGlyphVisible(sx, padding), 0);
                mDrawnScroll.releaseInitiatorStatus();
            }
            else if(sx > gx) {

                mDrawnScroll.captureInitiatorStatus();
                mDrawnScroll.smoothScrollTo(gx, 0);
                mDrawnScroll.releaseInitiatorStatus();
            }

        }
    }


    // Debug component requirement
    @Override
    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB) {
    }


    public void rippleHighlight() {

        long delay = 0;
        CStimulusController r;
        CGlyphController   v;

        for(int i1 = 0 ; i1 < mRecogList.getChildCount() ; i1++) {

            r = (CStimulusController)mRecogList.getChildAt(i1);

            r.post(TCONST.HIGHLIGHT, delay);
            delay += WR_CONST.RIPPLE_DELAY;
        }

        delay = 0;
        for(int i1 = 0; i1 < mGlyphList.getChildCount() ; i1++) {

            v = (CGlyphController) mGlyphList.getChildAt(i1);

            v.post(TCONST.HIGHLIGHT, delay);
            delay += WR_CONST.RIPPLE_DELAY;
        }

    }

    public void highlightStimulus() {

        long delay = 0;
        CStimulusController r;

        for(int i1 = 0 ; i1 < mRecogList.getChildCount() ; i1++) {

            r = (CStimulusController)mRecogList.getChildAt(i1);

            r.post(TCONST.HIGHLIGHT, delay);
            delay += WR_CONST.RIPPLE_DELAY;
        }
    }

    public void highlightGlyph() {

        long delay = 0;
        CGlyphController   v;

        for(int i1 = 0; i1 < mGlyphList.getChildCount() ; i1++) {

            v = (CGlyphController) mGlyphList.getChildAt(i1);

            v.post(TCONST.HIGHLIGHT, delay);
            delay += WR_CONST.RIPPLE_DELAY;
        }

    }

    public void pointAtGlyph() {
        _fieldIndex = 0;

        pointAtInputGlyph();
    }
    public void pointAtInputGlyph() {

        CGlyphController glyphInput;

        if( _fieldIndex < mGlyphList.getChildCount()) {

            glyphInput = (CGlyphController) mGlyphList.getChildAt(_fieldIndex);

            if (glyphInput.checkIsStimulus()) {

                // For write.missingLtr: To not point at non-answer Glyphs.
                _fieldIndex++;
                pointAtInputGlyph();
            } else {

                glyphInput.pointAtGlyph();
            }

        }
    }

    public void hideGlyphs() {

        CGlyphController   v;

        for(int i1 = 0; i1 < mGlyphList.getChildCount() ; i1++) {

            v = (CGlyphController) mGlyphList.getChildAt(i1);

            v.hideUserGlyph();
        }
    }


    public void hideSamples() {

        CGlyphController   v;

        for(int i1 = 0; i1 < mGlyphList.getChildCount() ; i1++) {

            v = (CGlyphController) mGlyphList.getChildAt(i1);

            v.showSampleChar(false);
        }
    }


    /**
     * in multi-char, highlights next character
     */
    public void highlightNext() {

        CStimulusController r;
        CGlyphController   v;

        r = (CStimulusController)mRecogList.getChildAt(mActiveIndex+1);

        if(r != null) {

            r.post(TCONST.HIGHLIGHT);
        }

        v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex+1);

        if(v != null) {
            v.post(TCONST.HIGHLIGHT);
        }
    }


    public void rippleReplay(String type, boolean isDemo) {

        _fieldIndex = 0;
        _replayType = type;
        _isDemo     = isDemo;

        replayNext();
    }
    private void replayNext() {

        CStimulusController r;
        CGlyphController   v;

        if( _fieldIndex < mGlyphList.getChildCount()) {

            v = (CGlyphController) mGlyphList.getChildAt(_fieldIndex);

            if (v.checkIsStimulus()) {

                // For write.missingLtr: To omit non-answer Glyphs from replay.
                _fieldIndex++;
                replayNext();
            } else {

                v.post(_replayType);
                _fieldIndex++;
            }
        }
        else {
            if(_isDemo)
              broadcastMsg(TCONST.POINT_FADE);

            applyBehavior(WR_CONST.REPLAY_COMPLETE);
        }
    }


    private void showTraceLine() {

        CGlyphController   v;

        for (int i = 0; i < mGlyphList.getChildCount(); i++) {
            v = (CGlyphController) mGlyphList.getChildAt(i);
            v.post("SHOW_SAMPLE");
        }
    }

    private void hideTraceLine() {

        CGlyphController   v;

        for (int i = 0; i < mGlyphList.getChildCount(); i++) {
            v = (CGlyphController) mGlyphList.getChildAt(i);
            v.post("HIDE_SAMPLE");
        }
    }


    public boolean scanForPendingRecognition(IGlyphController source) {

        boolean          result = false;
        IGlyphController glyphController;

        for(int i1 = 0; i1 < mGlyphList.getChildCount() ; i1++) {

            glyphController = (IGlyphController) mGlyphList.getChildAt(i1);

            if(glyphController != source) {

                result = glyphController.firePendingRecognition();

                if(result) {
                    inhibitInput(source, true);
                }
            }
        }

        return result;
    }


    public void inhibitInput(boolean inhibit) {

        inhibitInput(null, inhibit);
    }

    public void inhibitInput(IGlyphController source, boolean inhibit) {

        boolean          result = false;
        IGlyphController glyphController;
        int              i1 = 0;

        if(_immediateFeedback) {

            for (i1 = 0; i1 < mGlyphList.getChildCount(); i1++) {

                glyphController = (IGlyphController) mGlyphList.getChildAt(i1);

                if (glyphController != source) {

                    glyphController.inhibitInput(inhibit);
                }
            }
        }
    }

    public void showEraseButton(Boolean show) {

        if(mActiveController != null) {
            mActiveController.showEraseButton(show);
        }
    }

    // IWritingController End
    //************************************************************************
    //************************************************************************




    //**********************************************************
    //**********************************************************
    //*****************  DataSink Interface

    public boolean dataExhausted() {
        return (_dataIndex >= _data.size());
    }

    /**
     * Either randomizes or leaves it.
     *
     * @param dataSource
     */
    public void setDataSource(CWr_Data[] dataSource) {

        if(random) {

            ArrayList<CWr_Data> dataSet = new ArrayList<CWr_Data>(Arrays.asList(dataSource));

            // _data takes the form - ["92","3","146"]
            //
            _data = new ArrayList<CWr_Data>();

            // For XPrize we limit this to 10 elements from an umlimited random data set
            // used to be : dataSet.size()
            for (int i1 = 0; i1 < TCONST.WRITING_DATA_LIMIT ; i1++) {
                int randIndex = (int) (Math.random() * dataSet.size());

                _data.add(dataSet.get(randIndex));
                dataSet.remove(randIndex);
            }
        }
        else {

            _data = new ArrayList<CWr_Data>(Arrays.asList(dataSource));
        }

        _dataIndex = 0;
        _dataEOI   = false;
    }


    public void next() {
        try {
            if (_data != null) {

                retractFeature(WR_CONST.FTR_HAD_ERRORS);

                // XYZ
                updateText(_data.get(_dataIndex));
                mDrawnScroll.scrollTo(0, 0);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }

    //amogh added
    public void updateResponseView(String a)
    {
        mResponseViewList.removeAllViews();
        CStimulusController resp;
        resp = (CStimulusController)LayoutInflater.from(getContext()).inflate(R.layout.recog_resp_comp, null, false);
        resp.setStimulusChar(a,false);
        mResponseViewList.addView(resp);
        resp.setLinkedScroll(mDrawnScroll);
        resp.setWritingController(this);
    }

    /**
     * @param data
     * XYZ
     */
    public void updateText(CWr_Data data) {

        CStimulusController r;
        CGlyphController    v;
        CStimulusController resp; //amogh added

        boolean isStory = data.isStory;
        mStimulus = data.stimulus;
        mAudioStimulus = data.audioStimulus;
        mAnswer = data.answer;

        //amogh added
        //load the indices for the different corrections
        if(activityFeature.contains("FTR_SEN_CORR")){

        }
        // Add the recognized response display containers
        //
        _spaceIndices.clear();
        mRecogList.removeAllViews();
        mResponseViewList.removeAllViews(); //amogh added
        // XYZ check if is story
        if(isStory) {
            // mStimulus = getStoryStimulus(storyName, storyLine);
            // mAudioStimulus = getStoryAudio(storyName, storyLine);
            // mAnswer = mStimulus;
        }

        //amogh added to debug
//        try = (View) LayoutInflater.from(getContext()).inflate((R.drawable.highlight_error,null,false));

        if(!singleStimulus) {
            for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
            {
                // create a new view
                r = (CStimulusController)LayoutInflater.from(getContext())
                        .inflate(R.layout.recog_resp_comp, null, false);

                r.setStimulusChar(mStimulus.substring(i1, i1 + 1), singleStimulus);

                mRecogList.addView(r);

                r.setLinkedScroll(mDrawnScroll);
                r.setWritingController(this);
            }
        } else {
            // create a new view
            r = (CStimulusController)LayoutInflater.from(getContext())
                    .inflate(R.layout.recog_resp_comp, null, false);

            r.setStimulusChar(mStimulus, singleStimulus);

            mRecogList.addView(r);

            r.setLinkedScroll(mDrawnScroll);
            r.setWritingController(this);
        }

        // Add the Glyph input containers
        //
        mGlyphList.removeAllViews();
        mGlyphList.setClipChildren(false);

        // For write.missingLtr: We need non-answer Glyphs to behave like simple text stimulus
        if (activityFeature.contains("FTR_MISSING_LTR")) {

            int answerIndex = 0;

            for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
            {
                // create a new view
                v = (CGlyphController)LayoutInflater.from(getContext())
                        .inflate(R.layout.drawn_input_comp, null, false);

                // Last is used for display updates - limits the extent of the baseline


                String Char = mStimulus.substring(i1,i1+1);

                if (Char.equals("_")) {

                    String expectedChar = mAnswer.substring(answerIndex,answerIndex+1);

                    v.setExpectedChar(expectedChar);
                    answerIndex++;

                    v.setIsLast(answerIndex ==  mAnswer.length());

                    if(!expectedChar.equals(" ")) {
                        v.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar));
                    }

                    mGlyphList.addView(v);
                } else {

                    v.setIsStimulus(Char);
                    if(!Char.equals(" ")) {
                        v.setProtoGlyph(_glyphSet.cloneGlyph(Char));
                        v.updateCorrectStatus(true);
                    }
                    mGlyphList.addView(v);
                }
                v.setLinkedScroll(mDrawnScroll);
                v.setWritingController(this);
            }
        }

        //amogh added
        //add for sentence correction
        else if(activityFeature.contains("FTR_SEN_CORR")){
            //load glyph input container
            int expectedIndex = 0; //in order to set the expected answer correctly
            for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
            {
                // create a new view
                v = (CGlyphController)LayoutInflater.from(getContext())
                        .inflate(R.layout.drawn_input_comp, null, false);

                // Last is used for display updates - limits the extent of the baseline
                v.setIsLast(i1 ==  mStimulus.length()-1);
                v.setRespIndex(i1); //to remove the letter from the response when the glyph is removed by swiping on a glyph
                //amogh comment - needs edits, need a proper computation function to be able to calculate the differences between the
                //

                String expectedChar = mAnswer.substring(expectedIndex,expectedIndex+1);
                if(!deleteCorrectionIndices.contains(i1)){
                    expectedIndex++;
                }

                v.setExpectedChar(expectedChar);

                //amogh comment - need to check conditions. eg see the next if else.
                String stimulusChar = mStimulus.substring(i1,i1+1);
                if(!stimulusChar.equals(" ")) {
                    v.setStimuliGlyph(_glyphSet.cloneGlyph(stimulusChar));
                }

                if(!expectedChar.equals(" ")) {
                    v.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar));
                }
                else{
                    _spaceIndices.add(i1);

                    mActiveController.setWordIndex(currentWordIndex);
                    currentWordIndex++;
                }

                v.toggleStimuliGlyph();
                mGlyphList.addView(v);
//                v.toggleProtoGlyph(); //can help to debug and see the expected character
//                v.toggleSampleChar();
                v.setLinkedScroll(mDrawnScroll);
                v.setWritingController(this);

                // setting the response view and loading glyph controllers
                v.setResponseView(mResponseViewList);
                resp = (CStimulusController)LayoutInflater.from(getContext())
                        .inflate(R.layout.recog_resp_comp, null, false);
                resp.setStimulusChar(stimulusChar,true);
                mResponseViewList.addView(resp);
                resp.setLinkedScroll(mDrawnScroll);
                resp.setWritingController(this);
            }
        //amogh add finish

        }
        else {

            for(int i1 =0 ; i1 < mAnswer.length() ; i1++)
            {
                // create a new view
                v = (CGlyphController)LayoutInflater.from(getContext())
                        .inflate(R.layout.drawn_input_comp, null, false);

                // Last is used for display updates - limits the extent of the baseline
                v.setIsLast(i1 ==  mAnswer.length()-1);
                v.setRespIndex(i1);
                String expectedChar = mAnswer.substring(i1,i1+1);

                v.setExpectedChar(expectedChar);

                if(!expectedChar.equals(" ")) {
                    v.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar));
                }
                else{
                    _spaceIndices.add(i1);
                }

                mGlyphList.addView(v);
//                v.toggleProtoGlyph();
                v.setLinkedScroll(mDrawnScroll);
                v.setWritingController(this);
                v.setResponseView(mResponseViewList);
                //amogh added
                resp = (CStimulusController)LayoutInflater.from(getContext())
                        .inflate(R.layout.recog_resp_comp, null, false);
                mResponseViewList.addView(resp);
                resp.setLinkedScroll(mDrawnScroll);
                resp.setWritingController(this);
                //amogh add finish
            }
            initialiseCorrectionHashMap(); //amogh added
            initialiseWordIndices(); //amogh added
        }
        //amogh added


        //adding the view for highlight error box:
//        mHighlightErrorBoxView = new View (getContext());
//        mHighlightErrorBoxView.setLayoutParams(new LayoutParams(60,60));
////        mHighlightErrorBoxView.setId();
//        mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
////        MarginLayoutParams mp = (MarginLayoutParams) mHighlightErrorBoxView.getLayoutParams();
////        mp.setMargins(100,00,0,100);
//        mHighlightErrorBoxView.setX((float)300.00);
////        mHighlightErrorBoxView.setLeft(1000);
//        mResponseScrollLayout.addView(mHighlightErrorBoxView);
////        mHighlightErrorBoxView.postDelayed(new Runnable() {
////            public void run() {
////                mHighlightErrorBoxView.setVisibility(View.GONE);
////            }
////        }, 5000);


//        showHighlightBox();

        //amogh added ends
    }

    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start


    //amogh added
    //functions to modify the indices in sentence correction activities

    private void incrementCorrectionLists(int index){
        for (int i=0; i<deleteCorrectionIndices.size(); i++)
        {
            if (deleteCorrectionIndices.get(i) > index){
                deleteCorrectionIndices.set(i, deleteCorrectionIndices.get(i).intValue() + 1);
            }

            if (changeCorrectionIndices.get(i) > index){
                changeCorrectionIndices.set(i, changeCorrectionIndices.get(i).intValue() + 1);
            }

            if (insertCorrectionIndices.get(i) > index){
                changeCorrectionIndices.set(i, changeCorrectionIndices.get(i).intValue() + 1);
            }

        }
    }

    private void decrementCorrectionLists(int index){
        for (int i=0; i<deleteCorrectionIndices.size(); i++)
        {
            if (deleteCorrectionIndices.get(i) > index){
                deleteCorrectionIndices.set(i, deleteCorrectionIndices.get(i).intValue() - 1);
            }

            if (changeCorrectionIndices.get(i) > index){
                changeCorrectionIndices.set(i, changeCorrectionIndices.get(i).intValue() - 1);
            }

            if (insertCorrectionIndices.get(i) > index){
                changeCorrectionIndices.set(i, changeCorrectionIndices.get(i).intValue() - 1);
            }

        }
    }

    //amogh added ends

    public void setConstraint(String constraint) {

        _metric.setConstraint(constraint);
    }

    /**
     * Manage component defined (i.e. specific) events
     *
     * @param event
     * @return  true of event handled
     */
    public boolean applyBehavior(String event){

        boolean result = false;

        switch(event) {

            case WR_CONST.FIELD_REPLAY_COMPLETE:
                replayNext();
                break;
        }


        return result;
    }

    /**
     * Overridden in TClass to fire graph behaviors
     *
     * @param nodeName
     */
    public void applyBehaviorNode(String nodeName) {
    }

    public void pointAtEraseButton() {

        IGlyphController glyphInput;

        for (int i1 = 0; i1 < mGlyphList.getChildCount(); i1++) {

            glyphInput = (IGlyphController) mGlyphList.getChildAt(i1);

            if(glyphInput.hasError()) {
                glyphInput.pointAtEraseButton();
                break;
            }
        }
    }


    /**
     * Overloaded in TClass
     */
    public void pointAtReplayButton() {
    }


    public void cancelPointAt() {

        Intent msg = new Intent(TCONST.CANCEL_POINT);
        bManager.sendBroadcast(msg);
    }


    public void highlightFields() {

        post(TCONST.HIGHLIGHT, 500);
    }


    /**
     * See TClass subclass for implementation
     * @param targetNode
     */
    protected void callSubGgraph(String targetNode) {
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************


    //***********************************************************
    // Event Listener/Dispatcher - Start


    @Override
    public boolean isGraphEventSource() {
        return false;
    }

    /**
     * Must be Overridden in app module to access tutor engine
     * @param linkedView
     */
    @Override
    public void addEventListener(String linkedView) {
    }

    @Override
    public void addEventListener(IEventListener listener) {

    }

    @Override
    public void dispatchEvent(IEvent event) {

        for (IEventListener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {

        CGlyphController v;
        CStimulusController r;

        switch(event.getType()) {

            case TCONST.FW_EOI:
                _dataEOI = true;        // tell the response that the data is exhausted
                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    public class RecogTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            PointF touchPt;
            final int action = event.getAction();

            touchPt = new PointF(event.getX(), event.getY());

            //Log.i(TAG, "ActionID" + action);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "RECOG _ ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "RECOG _ ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "RECOG _ ACTION_UP");
                    break;
            }
            return true;
        }
    }


    public class drawnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            PointF touchPt;
            final int action = event.getAction();

            touchPt = new PointF(event.getX(), event.getY());

            //Log.i(TAG, "ActionID" + action);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "DRAWN _ ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "DRAWN _ ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "DRAWN _ ACTION_UP");
                    break;
            }
            return true;
        }
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

                    case WR_CONST.HIDE_GLYPHS:
                        hideGlyphs();
                        break;

                    case WR_CONST.HIDE_SAMPLES:
                        hideSamples();
                        break;

                    case WR_CONST.RIPPLE_HIGHLIGHT:

                        rippleHighlight();
                        break;

                    case WR_CONST.HIGHLIGHT_NEXT:

                        highlightNext();
                        break;

                    case WR_CONST.INHIBIT_OTHERS:

                        if(mActiveController != null)
                            inhibitInput(mActiveController, true);
                        break;

                    case WR_CONST.CLEAR_ATTEMPT:
                        // Clear attempt after correct behavior so it can use it to determine on
                        // which attempt they succeeded - We don't want this persisting if they
                        // subsequently keep getting them correct
                        //
                        clearAttemptFeatures();
                        break;
                    //amogh added
                    case WR_CONST.CLEAR_HESITATION:
                        //clear the hesitation number in feedback
                        clearHesitationFeatures();
                        break;

                    case WR_CONST.RESET_HESITATION:
                        //clear the hesitation number in feedback
                        resetHesitationFeature();
                        break;

                    case WR_CONST.INC_HESITATION:
                        updateHesitationFeature();
                    //amogh added ends

                    case TCONST.APPLY_BEHAVIOR:

                        applyBehaviorNode(_target);
                        break;

                    case WR_CONST.RIPPLE_DEMO:

                        rippleReplay(_command, true);
                        break;

                    case WR_CONST.RIPPLE_REPLAY:
                    case WR_CONST.RIPPLE_PROTO:

                        rippleReplay(_command, false);
                        break;
                    case WR_CONST.SHOW_TRACELINE: // Show all glyphs trace line.

                        showTraceLine();
                        break;
                    case WR_CONST.HIDE_TRACELINE: // Hide all glyphs trace line.

                        hideTraceLine();
                        break;
                    case WR_CONST.SHOW_SAMPLE:
                    case WR_CONST.HIDE_SAMPLE:
                    case WR_CONST.ERASE_GLYPH:
                    case WR_CONST.DEMO_PROTOGLYPH:
                    case WR_CONST.ANIMATE_PROTOGLYPH:
                    case WR_CONST.ANIMATE_OVERLAY:
                    case WR_CONST.ANIMATE_ALIGN:

                        if(mActiveController != null)
                            mActiveController.post(_command);
                        break;

                    case WR_CONST.POINT_AT_ERASE_BUTTON:

                        pointAtEraseButton();
                        break;

                    case WR_CONST.POINT_AT_REPLAY_BUTTON:

                        pointAtReplayButton();
                        break;

                    case WR_CONST.POINT_AT_GLYPH:

                        pointAtGlyph();
                        break;

                    case WR_CONST.CANCEL_POINTAT:

                        cancelPointAt();
                        break;

                    case WR_CONST.STIMULUS_HIGHLIGHT:

                        highlightStimulus();
                        break;

                    case WR_CONST.GLYPH_HIGHLIGHT:

                        highlightGlyph();
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

        while(nameMap.containsKey(name)) {

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

        enQueue(new Queue(null, command), delay);
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

    @Override
    public void publishFeatureSet(String featureset) {

    }

    @Override
    public void retractFeatureSet(String featureset) {

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


    //***********************************************************
    // ITutorLogger - Start

    @Override
    public void logState(String logData) {
    }

    // ITutorLogger - End
    //***********************************************************



    //************ Serialization



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
