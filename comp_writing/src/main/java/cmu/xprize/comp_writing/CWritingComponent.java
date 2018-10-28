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
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.ltkplus.CGlyph;
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
import static cmu.xprize.util.TCONST.PAGEFLIP_BUTTON;


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
    private   IGlyphController   mActiveController;
    protected int               mActiveIndex;

    protected ImageButton       mReplayButton;
//    protected ImageButton       mScrollRightButton;
//    protected ImageButton       mScrollLeftButton;

    protected LinearLayout      mRecogList;
    protected LinearLayout      mResponseViewList; //amogh added
    protected LinearLayout      mGlyphList;
    protected LinearLayout      mGlyphAnswerList;
    protected RelativeLayout    mResponseScrollLayout;

    protected View mHighlightErrorBoxView;
    protected CWritingBoxLink mWritingBoxLink;

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
    protected ArrayList<String> _senAttemptFTR = new ArrayList<>();
    protected ArrayList<String> _hesitationFTR = new ArrayList<>(); //amogh added
    private int                 _hesitationNo      = 0; //amogh added
    protected ArrayList<String> _audioFTR = new ArrayList<>();

    protected String            mResponse;
    protected String            mStimulus;
    protected String[]          mAudioStimulus;
    protected String            mAnswer;

    protected CGlyphMetricConstraint _metric = new CGlyphMetricConstraint();

    protected List<CWr_Data>    _data;
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

    //amogh added
    // for sentence activites
    protected List<Integer>     _spaceIndices = new ArrayList<Integer>(); //indices with space for
    protected int               currentWordIndex = 0;
    protected ArrayList<Word>   mListWordsAnswer;
    protected ArrayList<Word>   mListWordsInput;
    protected Word              mActiveWord;
    protected String            mWrittenSentence;
    protected StringBuilder     mEditSequence;
    protected StringBuilder     mAlignedSourceSentence;
    protected StringBuilder     mAlignedTargetSentence;
    protected int               mSentenceAttempts;
    //amogh added ends


    protected String punctuationSymbols = ",.;:-_!?";
    protected Map<String, String> punctuationToString;
    protected Map<String, String> punctuationToFeature;
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

        punctuationToString = new HashMap<String, String> ();
        punctuationToString.put(",", "comma");
        punctuationToString.put(".", "period");
        punctuationToString.put("!", "exclamation point");
        punctuationToString.put("?", "question mark");
        punctuationToString.put("-","hyphen");

        punctuationToFeature = new HashMap<String,String>();
        punctuationToFeature.put(",", WR_CONST.FTR_COMMA);
        punctuationToFeature.put(".", WR_CONST.FTR_PERIOD);
        punctuationToFeature.put("!", WR_CONST.FTR_EXCLAIM);
        punctuationToFeature.put("?", WR_CONST.FTR_QUESTION);

        // initialize with four features
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_1);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_2);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_3);
        _attemptFTR.add(WR_CONST.FTR_ATTEMPT_4);

        //amogh added
        _senAttemptFTR.add(WR_CONST.FTR_SEN_ATTEMPT_1);
        _senAttemptFTR.add(WR_CONST.FTR_SEN_ATTEMPT_2);
        _senAttemptFTR.add(WR_CONST.FTR_SEN_ATTEMPT_3);
        _senAttemptFTR.add(WR_CONST.FTR_SEN_ATTEMPT_4);

        _hesitationFTR.add(WR_CONST.FTR_HESITATION_1);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_2);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_3);
        _hesitationFTR.add(WR_CONST.FTR_HESITATION_4);
//        _hesitationFTR.add(WR_CONST.FTR_HESITATION_5);

        //amogh added to initialise the list for audio features:
            _audioFTR.add(WR_CONST.FTR_AUDIO_CAP);
            _audioFTR.add(WR_CONST.FTR_AUDIO_LTR);
            _audioFTR.add(WR_CONST.FTR_AUDIO_PUNC);
            _audioFTR.add(WR_CONST.FTR_AUDIO_SPACE);
            _audioFTR.add(WR_CONST.FTR_INSERT);
            _audioFTR.add(WR_CONST.FTR_DELETE);
            _audioFTR.add(WR_CONST.FTR_REPLACE);
            _audioFTR.add(WR_CONST.FTR_PERIOD);
            _audioFTR.add(WR_CONST.FTR_COMMA);
            _audioFTR.add(WR_CONST.FTR_EXCLAIM);
            _audioFTR.add(WR_CONST.FTR_QUESTION);


//            _audioFTR.add(WR_CONST.FTR_AUDIO_CAP);
//            _audioFTR.add(WR_CONST.FTR_AUDIO_CAP);
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

    // amogh added highlight box.
    //this is to show the highlight box at different levels for a given word (whole word / first edit) based on the level
//animator graph functions

    public void deactivateEditModeInActiveWord(){
        mActiveWord.deactivateEditMode();
    }

    // highlight box functions

    public void showHighlightBoxOnActiveWord(){
        mActiveWord.showHighlightBox();
    }

    public void showHighlightBoxOnFirstEdit(){
        EditOperation firstEditOperationSentence = getFirstEditOperation(mWrittenSentence, mAnswer);
        if(!firstEditOperationSentence.toString().equals("N"))
        firstEditOperationSentence.showHighlightBox();
    }


    public void showHighlightBoxOnFirstEditAndPoint(){
        EditOperation firstEditOperationSentence = getFirstEditOperation(mWrittenSentence, mAnswer);
        if(!firstEditOperationSentence.toString().equals("N"))
            firstEditOperationSentence.showHighlightBox();

            int index = firstEditOperationSentence.index;
            CGlyphController view    = (CGlyphController) mGlyphList.getChildAt(index);
            autoScroll(view);
            view.post(TCONST.HIGHLIGHT);
            view.post(TCONST.HIGHLIGHT);

    }

//    public void show

    public void showHighlightBoxOnFirstEditGroup(){
//        EditOperation firstEditOperationSentence = getFirstEditOperation(mWrittenSentence, mAnswer);
//        firstEditOperationSentence.showHighlightBox();
        int startIndex = 0;
        int endIndex = 0;
        for(int i = 0; i < mGlyphList.getChildCount(); i++){
            CGlyphController c = (CGlyphController) mGlyphList.getChildAt(i);
            if(!c.isCorrect() && c.hasGlyph()){
//                if(i == endIndex + 1){
//                    endIndex++;
//                }
//                else{
//                    startIndex = i;
//                }
                endIndex++;
            }

            //when glyph is correct or hasn't been attempted.
            else{
                if(startIndex == endIndex){
                    startIndex++;
                    endIndex++;
                }
                else{
                    break;
                }
            }
        }

        //return when none of the glyphs was wriiten and wrong.
        if(startIndex == endIndex){
            return;
        }

        //when atleast one of the glyphs was written and was wrong
        else {
            int wid = mResponseViewList.getChildAt(startIndex).getWidth() * (endIndex - startIndex);
            int left = mResponseViewList.getChildAt(startIndex).getLeft();
            int height = mResponseViewList.getChildAt(0).getHeight();

            mHighlightErrorBoxView = new View(getContext());
            mHighlightErrorBoxView.setX((float) left);
            mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid, height));
            mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
            mResponseScrollLayout.addView(mHighlightErrorBoxView);
            mHighlightErrorBoxView.postDelayed(new Runnable() {
                public void run() {
                    mHighlightErrorBoxView.setVisibility(View.GONE);
                }
            }, 4000);
        }
    }

    public void showHighlightBoxOnFirstEditWord(){

        EditOperation firstEditOperationSentence = getFirstEditOperation(mWrittenSentence, mAnswer);

        int activeWordIndex = getActiveWordIndexForEdit(firstEditOperationSentence);

        //if outside the word, highlight the error
        if (activeWordIndex == -1) {

            //highlight the error according to if its insert, replace or delete
            firstEditOperationSentence.showHighlightBox();
        }

        // if inside the word, simply follow what was happening at the word level
        else {
            Word firstEditWord = mListWordsInput.get(activeWordIndex);
            firstEditWord.showHighlightBox();
        }
    }

    public void onErase(int eraseIndex){
            updateSentenceEditSequence();
//            updateExpectedCharacters();
            publishOnEraseState();
            CGlyphController c = (CGlyphController) mGlyphList.getChildAt(eraseIndex);
            if(activityFeature.contains("FTR_SEN_LTR")){

                String expectedChar = mStimulus.substring(eraseIndex,eraseIndex + 1);
                boolean isSpaceExpected = expectedChar.equals(" ");
                if(!isSpaceExpected){
                    c.updateCorrectStatus(false);
                }

                if(activityFeature.contains("FTR_SEN_CORR")){
//                    if(!isSpaceExpected){
//                        c.toggleStimuliGlyph();
//                    }
                }
            }
    }
    //

    //to be called by the animator graph
//    public void showHighlightBox(Integer level, Word w){
//
//        mHighlightErrorBoxView = new View (getContext());
//        int wid = 0;
//        int left = 0;
//        //switch case sets the width, and the position of the box.
//        switch (level){
//
//            case 1:
//
//                break;
//
//            case 2:
//
//                wid = w.getWidth();
//                left = w.getLeft();
//
//                break;
//
//            case 3:
//
//                wid  = mResponseViewList.getChildAt(0).getWidth();
//                left = w.getFirstEditLeft();
//
//                break;
//
//            case 4:
//
//                break;
//        }
//
//        //now that the width and the position of this box has been set, set its drawable and show for some time.
//        mHighlightErrorBoxView.setX((float)left);
//        mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid, 90));
//        mHighlightErrorBoxView.setBackgroundResource(R.drawable.highlight_error);
//        //        MarginLayoutParams mp = (MarginLayoutParams) mHighlightErrorBoxView.getLayoutParams();
//        //        mp.setMargins(100,00,0,100);
//        //                mHighlightErrorBoxView.setX((float)300.00);
//        //        int pos = mResponseViewList.getChildAt(index+2).getLeft();
//        //        mHighlightErrorBoxView.setX(100);
//        //        mHighlightErrorBoxView.setLeft(1000);
//        mResponseScrollLayout.addView(mHighlightErrorBoxView);
//        mHighlightErrorBoxView.postDelayed(new Runnable() {
//            public void run() {
//                mHighlightErrorBoxView.setVisibility(View.GONE);
//            }
//        }, 4000);
//    }

    //to be called by the animator graph
//    public void showHighlightBox(Integer level) {
//
//        //for sentence level, need to check if the first edit is inside a word or not.
//        if(activityFeature.contains("FTR_SEN_SEN")){
//            if(mSentenceAttempts > 0) {
//                //find first edit, it can be R,I,D
//                EditOperation firstEditOperationSentence = getFirstEditOperation(mWrittenSentence, mAnswer);
//
//                int activeWordIndex = getActiveWordIndexForEdit(firstEditOperationSentence);
//
//                //if outside the word, highlight the error
//                if (activeWordIndex == -1) {
//
//                    //highlight the error according to if its insert, replace or delete
//                    firstEditOperationSentence.showHighlightBox();
//                }
//
//                // if inside the word, simply follow what was happening at the word level
//                else {
//                    Word firstEditWord = mListWordsInput.get(activeWordIndex);
////                    if (firstEditWord.getAttempt() > 0) {
//                        showHighlightBox(level, firstEditWord);
////                    }
//                }
//            }
//        }
//
//        //for word level
//        else{
//            if (mActiveWord.getAttempt() > 0 ) {
//                showHighlightBox(level, mActiveWord);
//            }
//        }
//    }


    public void showSampleForActiveWord(Boolean show){
        mActiveWord.showSamples(show);
    }
//    public void showSampleForActiveIndex(Boolean show){
//        mActiveWord.showSamples(show);
//    }

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
        boolean canDelete = checkDelete(mEditSequence, index);

//        if(canDelete){
        //remove from mGlyphList
        mGlyphList.removeViewAt(index);
        //remove from response
        mResponseViewList.removeViewAt(index);

        //update sentence parameters
        updateSentenceEditSequence();
        mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);
        mActiveWord = mListWordsInput.get(currentWordIndex);

        //update the expected characters
//        updateExpectedCharacters();

        if(activityFeature.contains("FTR_SEN_CORR")){

        }

        //for letter level
        else if(activityFeature.contains("FTR_SEN_LTR")){
            if (isComplete()) {
                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
            }
        }

        else if(activityFeature.contains("FTR_SEN_WRD")){
//                    evaluateSentenceWordLevel();
        }
        else if (activityFeature.contains("FTR_SEN_SEN")){

        }
        else{

        }
//            //if the word is complete, release the ON_CORRECT feature.
//            String writtenActiveWord = mActiveWord.getWrittenWordString();
//            String writtenAnswerWord = mActiveWord.getWordAnswer();
//            boolean writtenWordIsCorrect = writtenActiveWord.equals(writtenAnswerWord);
//            if(writtenWordIsCorrect){
//                applyBehavior(WR_CONST.ON_CORRECT);
//            }
//        }

    }


    /**
     * Note that only the mGlyphList will initiate this call
     *
     * @param child
     */
    public void addItemAt(View child, int inc) {
        //add a view, update the response and sentence parameters, update expected characters for all the activities.
        CGlyphController v;
        CStimulusController r;

        int index = mGlyphList.indexOfChild(child);

        //add new stimulus controller in the response
        r = (CStimulusController) LayoutInflater.from(getContext())
                .inflate(R.layout.recog_resp_comp, null, false);
        mResponseViewList.addView(r, index + inc);

        r.setStimulusChar(" ", false);
        r.setLinkedScroll(mDrawnScroll);
        r.setWritingController(this);

        // add a new view
        v = (CGlyphController) LayoutInflater.from(getContext())
                .inflate(R.layout.drawn_input_comp, null, false);

        if (index == mGlyphList.getChildCount() - 1) {
            ((CGlyphController) child).setIsLast(false);
            v.setIsLast(true);
        }

        v.setResponseView(mResponseViewList);
        mGlyphList.addView(v, index + inc);

        //enable the required buttons for this view, add the conditions for other activities
        if (activityFeature.contains("FTR_SEN_COPY")) {
            v.showDeleteSpaceButton(true);
            v.showInsLftButton(true);
            v.showInsRgtButton(true);
        } else if (activityFeature.contains("FTR_SEN_CORR")) {
            activateEditModeValidOnly();
        }

        v.setLinkedScroll(mDrawnScroll);
        v.setWritingController(this);

        //if supposed to be a space here, inhibit input
        //update the sentence parameters
        updateSentenceEditSequence(); //updates mWrittenSentence, mEditSequence, mAlignedSourceSentence, mAlignedTargetSentence
        mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence, mAlignedTargetSentence); //sets the list of written word objects using mAlignedSourceSentence, mAlignedTargetSentence and then gets the number of attempts from the previous list.

        //update the expected characters
//        updateExpectedCharacters();

        if (activityFeature.contains("FTR_SEN_CORR")) {

        }

        //for letter level
        else {
            // if too many insertions, remove views from last.
            int lengthAnswer = mAnswer.length();
            if(mGlyphList.getChildCount() > lengthAnswer + 2){
                mGlyphList.removeViewAt(lengthAnswer + 1);
                mResponseViewList.removeViewAt(lengthAnswer + 1);
            }

            //for letter level copy
            if (activityFeature.contains("FTR_SEN_LTR")) {
                if (isComplete()) {
                    applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
                }
            }

            //for word level copy
            else if (activityFeature.contains("FTR_SEN_WRD")) {
                //since the mListWordsInput is now updated, the parameters for mActiveWord have also changed, so refresh it.
//                mActiveWord = mListWordsInput.get(currentWordIndex);

                //might have to change this. works for now.
//                evaluateSentenceWordLevel();

//                //if the word is complete, release the ON_CORRECT feature.
//                String writtenActiveWord = mActiveWord.getWrittenWordString();
//                String writtenAnswerWord = mActiveWord.getWordAnswer();
//                boolean writtenWordIsCorrect = writtenActiveWord.equals(writtenAnswerWord);
//                if(writtenWordIsCorrect){
//                    applyBehavior(WR_CONST.ON_CORRECT); //should increment the current word index and the mActiveWord also thus changes.
//                    temporaryOnCorrect();
//                }
            }

            // for sentence level copy
            else if (activityFeature.contains("FTR_SEN_SEN")) {

            }
        }

//            }

//            if cannot insert
//            else {
                //increase attempt number for the word or the sentence!
//            }
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

        //store the previous character that was there
        String previousRecognisedChar = gController.getRecognisedChar();

        // Check answer
        mResponse = candidate.getRecChar();

        //when the recognised character is not accurate, just flash that box.
        if(candidate.getVisualConfidence() < 0.1){
            gController.eraseGlyph();
            gController.post(TCONST.HIGHLIGHT);
            return false;
        }

        gController.setRecognisedChar(mResponse);
        _charValid = gController.checkAnswer(mResponse, isAnswerCaseSensitive); //checks the expected string against the drawn response string (note that glyph is not used, just the string)

        _metricValid = _metric.testConstraint(candidate.getGlyph(), this); //measures hor, vert position, height width wrt the drawing box


        //amogh add begins
            //changing _charValid for sentence writing activities
            if(activityFeature.contains("FTR_SEN_LTR")){
                String correctString = mAnswer.substring(mActiveIndex,mActiveIndex + 1);
                boolean isCorrect = mResponse.equals(correctString);
                if(isCorrect) //amogh comment, this will change when insert and delete buttons will be there(then comparison has to be done using the aligned strings.).
                {
                    _charValid = true;
                }
                else{
                    _charValid = false;
                }
            }
            else if(activityFeature.contains("FTR_SEN_WRD")){

            }

        //amogh ends

        _isValid = _charValid && _metricValid; // _isValid essentially means "is a correct drawing"




//            updateResponseView(mResponse);

        // when not a sentence writing activity
        if (!(activityFeature.contains("FTR_SEN"))){

            //update the controller's correct status
            mActiveController.updateAndDisplayCorrectStatus(_isValid);

            // Update the controller feedback colors
            if (!singleStimulus) {
                stimController.updateStimulusState(_isValid);
            }

            // Depending upon the result we allow the controller to disable other fields if it is working
            // in Immediate feedback mode
            // TODO: check if we need to constrain this to immediate feedback mode
            inhibitInput(mActiveController, !_isValid);
            mActiveController.inhibitInput(_isValid);
            // Publish the state features.
            publishState();

            // Fire the appropriate behavior
            if (isComplete()) {

                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
            }
            else {

                if (!_isValid) {

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

            //amogh added to set the valid character in response.
            CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex);
            String charExpected = gController.getExpectedChar();
            resp.setStimulusChar(mResponse, false);

            //update the controller's correct status
            mActiveController.updateCorrectStatus(_isValid); //sets the _correct in CGlyphInputController, would change for the different sentence level activities.

            if(activityFeature.contains("FTR_SEN_LTR") && activityFeature.contains("FTR_SEN_COPY")){

                // Update the controller feedback colors
                resp.updateResponseState(_isValid);

                //update sentence parameters
                updateSentenceEditSequence();
//                mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);

                //currently -> when the next letter is valid and the previous glyph is supposed to be space. Needs to change.
                    // when the previous glyph is supposed to be space, accept it and inhibit space
//                    if (mActiveIndex > 0 && (mAnswer.substring(mActiveIndex - 1, mActiveIndex).equals(" "))) {
//                CGlyphController gControllerSpace = (CGlyphController) mGlyphList.getChildAt(mActiveIndex - 1);
//                CStimulusController respSpace = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex - 1);
//                respSpace.setStimulusChar("", false);
//                respSpace.updateResponseState(true);
//                gControllerSpace.setIsStimulus("");
//                gControllerSpace.updateCorrectStatus(_isValid);
//                gControllerSpace.inhibitInput(true);
//                    }

                //update the expected characters according to string edit distance.

//                if(!mResponse.equals(charExpected)) {
//                    updateExpectedCharacters();
//                }

                inhibitInput(mActiveController, !_isValid);
                mActiveController.inhibitInput(_isValid);


                publishState();


                // Fire the appropriate behavior
                //Here isComplete just loops through the mGlyphList elements and checks the isCorrect of all. For word level/sentence level feedback, this might not work.
                if (isComplete()) {

                    applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
                }
                else {
                    //when not valid
                    if (!_isValid) {

                        // lots of fun feature updating here
                        publishFeature(WR_CONST.FTR_HAD_ERRORS);

                        int attempt = updateAttemptFeature();

                        if (attempt > 4) {
                            applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                        } else {

                            if(charExpected.equals(" ")) {
                                publishFeature("FTR_SPACE_SAMPLE");
                            }
                            applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                        }

                        if (!_charValid)
                            applyBehavior(WR_CONST.ON_CHAR_ERROR);
                        else if (!_metricValid)
                            applyBehavior(WR_CONST.ON_METRIC_ERROR);
                    }

                    //else when valid
                    else {
                        updateStalledStatus();
                        mActiveController.updateCorrectStatus(true);
                        applyBehavior(WR_CONST.ON_CORRECT); //goto node "GENERAL_CORRECT_BEHAVIOR"
                    }
                }
            }

            //for word level feedback
            else if(activityFeature.contains("FTR_SEN_WRD")){

                mActiveWord = mListWordsInput.get(currentWordIndex);
                int attempts = mActiveWord.getAttempt();

                //evaluate when the word is being written and evaluated for the first time, also making sure that its not the first box (as we are trying to detect the transition to the next word)
                if(attempts == 0 && mActiveIndex > 0){

                    //if punctuation is written or the previous space is left empty
                    CGlyphController previousController = (CGlyphController) mGlyphList.getChildAt(mActiveIndex - 1);
                    boolean isPunctuationDrawn = punctuationSymbols.contains(mResponse);
                    boolean nextWordStarted = previousController.getRecognisedChar().equals("");
                    boolean isLastLetter = (mActiveIndex == mGlyphList.getChildCount() - 1);

                    if (nextWordStarted || isPunctuationDrawn || isLastLetter) {
                        //checks all the words in succession, stops at the first incorrect one, sets it to the active word, applies ON_CORRECT and ON_ERROR behaviors on the way
                        evaluateSentenceWordLevel();
                        }

                    //word has not been evaluated ie correctionAttempts = 0 and not to be evaluated. -> just let the user write
                    else{
//                        attempts = updateAttemptFeature();
//                        applyBehavior(WR_CONST.ON_ERROR);
                    }

                }

                //when incorrect attempt has been made on this word before
                else if(attempts > 0){

                    //check if allowed to write here
                    boolean canReplace = checkReplace(mEditSequence, mActiveIndex);

                    //if replacement is allowed at this index
                    if(canReplace) {
                        boolean responseEqualsTargetReplacement = mResponse.equals(getReplacementTargetString(mEditSequence, mActiveIndex));
                        //if the replacement edit is valid
                        if(responseEqualsTargetReplacement){

                            updateSentenceEditSequence();
                            mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);

                            // if this correct response makes the sentence correct,
                            boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);
                            if (writtenSentenceIsCorrect) {
                                temporaryOnCorrect(); //to update the word
                                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
                            }

                            // if the response is correct(item not yet over), check if the word is correct,apply ON_CORRECT
                            else{
                                //since the replacement is correct, still apply the oncorrect; its just that according to the attempt feature attempt fn will be called.

                                //checking if this completes the word
                                mActiveWord = mListWordsInput.get(currentWordIndex);
                                boolean wordIsCorrect = mActiveWord.updateInputWordCorrectStatus(currentWordIndex);
                                //if the word is correct update its color
                                if (wordIsCorrect) {
                                    //turn blue
                                    //release the oncorrect behavior with word correct feature.
                                    publishFeature(WR_CONST.FTR_WORD_CORRECT);
                                    temporaryOnCorrect();
                                    applyBehavior(WR_CONST.ON_CORRECT);
//                                    temporaryOnCorrect();
                                }

                                //if the replacement is correct but the word is not, turn it in blue and inhibit input if attempt 2/3 when each letter is colored.
                                else{
                                    applyBehavior(WR_CONST.ON_CORRECT);
                                }


//                                evaluateSentenceWordLevel(); // not applied because it is primarily for going over all words in a loop. Although code above is almost same.
                            }
                        }
                        // else (if incorrect letter drawn, but correct place chosen for replacement), revert the glyph to what it was, then increase attempt and release on error behavior.
                        else{
                            //revert the glyph to old one.
                            gController.setPreviousGlyph();// put in animator graph
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                            // in the animator graph -> turn the word blue or red.
                            //set the word as red or blue depending on status.
                        }
                    }

                    //when the glyph drawn is at the wrong place, increase the attempt and replace the old glyph that was there.
                    else{
                        gController.setPreviousGlyph();
                        //lets increase the attempt for this word, this will also release the corresponding feature which can then be used in the animator graph to call the functions that we want.

                        //if the glyph drawn again is the same as the previous one, don't increase the attempt.
                        if(!mResponse.equals(mWrittenSentence.substring(mActiveIndex,mActiveIndex + 1))) {
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                        }
                    }

//                        //if the word is still not correct and the current attempt is wrong.
//                        else  {
//
//                            // increase attempts and update the released feature
//                            attempts = updateAttemptFeature();
//                            if (attempts > 4) {
//                                applyBehavior(WR_CONST.MERCY_RULE);
//                                currentWordIndex++;
//                            } else {
//                                applyBehavior(WR_CONST.ON_ERROR);
//                            }
//                        }
                }
            }

            //for sentence level feedback in the copy activity
            else if(activityFeature.contains("FTR_SEN_SEN") && activityFeature.contains("FTR_SEN_COPY")){

                if(mSentenceAttempts == 0){
                    //when the sentence has not been evaluated yet, evaluate on punctuation
                    if("!?.".contains(mResponse))
                    {
                        evaluateSentenceFirstTime();
                    }
                    //when correct, not end punctuation and attempts = 0, normal recognition only
                    else
                    {}
                }

                //when the sentence attempts > 0,
                // functions to implement -> identify the current word indices(separate), turn those red/blue(easy), turn indivdual red/blue(easy).
                else if(mSentenceAttempts > 0){

                    //set the current active word, so that hesitation and feedback can be shown on this word.
                    currentWordIndex = getActiveWordIndex(mActiveIndex);

                    //amogh comment, the active word initialised here is not right, remove this declaration unless this one is used until the next declaration.
                    if (currentWordIndex != -1){
                        mActiveWord = mListWordsInput.get(currentWordIndex);
                    }

                    //check if allowed to write here
                    boolean canReplace = checkReplace(mEditSequence, mActiveIndex);

                    //if allowed to replace at this position
                    if(canReplace){

                        boolean responseEqualsTargetReplacement = mResponse.equals(getReplacementTargetString(mEditSequence, mActiveIndex));

                        ///if the replacement is correct and at the correct position
                        if(responseEqualsTargetReplacement){

                            updateSentenceEditSequence();
                            mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);

                            //update the indices and text for words in mListWordsInput

                            // if this correct response makes the sentence correct,
                            boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);

                            //if the written sentence is correct
                            if (writtenSentenceIsCorrect) {
                                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
                                clearSentenceAttemptFeatures(); //should go in the animator graph
                            }

                            // if the response is correct at the right place, but there are more corrections to be made, check if the word is correct, turn blue depending on what the attempt level of that sentence is.
                            else{

                                if(currentWordIndex != -1) { //when the replacement is in a word
                                    // check if the word written is correct and release ON_CORRECT. How to check that a word is written correctly? strings should match bw

                                    //refreshing mActiveWord
                                    if (currentWordIndex != -1) {
                                        mActiveWord = mListWordsInput.get(currentWordIndex);
                                    }

                                    String writtenActiveWord = mActiveWord.getWrittenWordString();
                                    String writtenAnswerWord = mListWordsAnswer.get(currentWordIndex).getWordAnswer();
                                    //checking if the letters of the word are correct
                                    boolean writtenWordIsCorrect = writtenActiveWord.equals(writtenAnswerWord);
                                    if (writtenWordIsCorrect) {
                                        publishFeature(WR_CONST.FTR_WORD_CORRECT);
                                        applyBehavior(WR_CONST.ON_CORRECT); //should turn word blue
//                                        temporaryOnCorrectSentence(); //already in animator graph, doesnt deactivate the edits until the letters AND the position of the word is correct.
                                    }

                                    //when correct replacement but the word or the sentence is not yet complete.
                                    else{
                                        applyBehavior(WR_CONST.ON_CORRECT);
                                    }
                                }

                                //when not in a word (open in the sentence/space)
                                else{
                                    //maybe just turn this glyph controller blue?
                                    //and inhibit input?
                                }
                            }
                        }

                        // else (if incorrect letter drawn, but correct place chosen for replacement), revert the glyph to what it was, then increase attempt and release on error behavior.
                        else{
                            //revert the glyph to old one.
                            gController.setPreviousGlyph();// put in animator graph
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                            // in the animator graph -> turn the word blue or red.
                            //set the word as red or blue depending on status.
                        }
                    }

                    //when the glyph drawn is at the wrong place, increase the attempt and replace the old glyph that was there.
                    else{
                        gController.setPreviousGlyph();
                        //lets increase the attempt for this word, this will also release the corresponding feature which can then be used in the animator graph to call the functions that we want.

                        //if the glyph drawn again is the same as the previous one, don't increase the attempt.
                        if(!mResponse.equals(mWrittenSentence.substring(mActiveIndex,mActiveIndex + 1))) {
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                        }
                    }
                }
            }


            //letter level feedback in correction activities
            else if (activityFeature.contains("FTR_SEN_CORR") && activityFeature.contains("FTR_SEN_LTR"))          {
                // Update the controller feedback colors
                resp.updateResponseState(_isValid);

                //update sentence parameters
                updateSentenceEditSequence();

                //inhibit the input if correct replacement.
                inhibitInput(mActiveController, !_isValid);
                mActiveController.inhibitInput(_isValid);

                //Here isComplete just loops through the mGlyphList elements and checks the isCorrect of all. For word level/sentence level feedback, this might not work.
                if (isComplete()) {

                    applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete..
                }

                //when the sentence is not yet complete
                else{
                    //when not valid
                    if (!_isValid) {


                        // lots of fun feature updating here
                        publishFeature(WR_CONST.FTR_HAD_ERRORS);

                        int attempt = updateAttemptFeature();

                        if (attempt > 4) {
                            applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                        } else {
                            if(charExpected.equals(" ")) {
                                publishFeature("FTR_SPACE_SAMPLE");
                            }

                            applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                        }

                        if (!_charValid)
                            applyBehavior(WR_CONST.ON_CHAR_ERROR);
                        else if (!_metricValid)
                            applyBehavior(WR_CONST.ON_METRIC_ERROR);
                    }

                    //else when valid
                    else {
                        mActiveController.updateCorrectStatus(true);
                        applyBehavior(WR_CONST.ON_CORRECT); //goto node "GENERAL_CORRECT_BEHAVIOR"
                    }
                }

            }

            //sentence level feedback in correction activities
            else if (activityFeature.contains("FTR_SEN_CORR") && activityFeature.contains("FTR_SEN_SEN")){
//                //set the current active word, so that hesitation and feedback can be shown on this word.
//                currentWordIndex = getActiveWordIndex(mActiveIndex);
//                if (currentWordIndex != -1){
//                    mActiveWord = mListWordsInput.get(currentWordIndex);
//                }
//
//                //check if allowed to write here
//                boolean canReplace = checkReplace(mEditSequence, mActiveIndex);
//
//                //if allowed to replace at this position
//                if(canReplace){
//
//                    //check if the correct glyph is drawn
//                    //if yes, and update the edit sequence, and check if the sentence is correct now.
//                    boolean responseEqualsTargetReplacement = mResponse.equals(getReplacementTargetString(mEditSequence, mActiveIndex));
//                    if(responseEqualsTargetReplacement){
//                        updateSentenceEditSequence();
//                        mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);
//
//                        //update the indices and text for words in mListWordsInput
//
//                        // if this correct response makes the sentence correct,
//                        boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);
//                        if (writtenSentenceIsCorrect) {
//                            applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
//                            clearSentenceAttemptFeatures(); //should go in the animator graph
//                        }
//
//                        // if the response is correct, but there are more corrections to be made, check if the word is correct, turn blue depending on what the attempt level of that sentence is.
//                        else{
//                            //check if the word written is correct and release ON_CORRECT. How to check that a word is written correctly? strings should match bw
//                            //not sure yet, but let's try to set the condition as the matching of strings in the mListWordsInput and mListWordsAnswer
//                            String writtenActiveWord = mActiveWord.getWrittenWordString();
//                            String writtenAnswerWord = mListWordsAnswer.get(currentWordIndex).getWordAnswer();
//                            boolean writtenWordIsCorrect = writtenActiveWord.equals(writtenAnswerWord);
//                            if(writtenWordIsCorrect){
//                                applyBehavior(WR_CONST.ON_CORRECT);
//                                temporaryOnCorrectSentence();
//
//                            }
//                        }
//                    }
//
//                    // else (if incorrect letter drawn, but correct place chosen for replacement), revert the glyph to what it was, then increase attempt and release on error behavior.
//                    else{
//                        //revert the glyph to old one.
//                        gController.setPreviousGlyph();// put in animator graph
//                        updateAttemptFeature();
//                        applyBehavior(WR_CONST.ON_ERROR);
//                        // in the animator graph -> turn the word blue or red.
//                        //set the word as red or blue depending on status.
//                    }
//                }
//
//                //when the glyph drawn is at the wrong place, increase the attempt and replace the old glyph that was there.
//                else{
//                    gController.setPreviousGlyph();
//                    //lets increase the attempt for this word, this will also release the corresponding feature which can then be used in the animator graph to call the functions that we want.
//                    updateAttemptFeature();
//                    applyBehavior(WR_CONST.ON_ERROR);
//                }
                //update sentence status

                    //check if allowed to write here
                currentWordIndex = getActiveWordIndex(mActiveIndex);

                if(currentWordIndex != -1){
                    mActiveWord = mListWordsInput.get(currentWordIndex);
                }

                    boolean canReplace = checkReplace(mEditSequence, mActiveIndex);

                    //if allowed to replace at this position
                    if(canReplace){

                        boolean responseEqualsTargetReplacement = mResponse.equals(getReplacementTargetString(mEditSequence, mActiveIndex));

                        ///if the replacement is correct and at the correct position
                        if(responseEqualsTargetReplacement){

                            updateSentenceEditSequence();
                            mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);

                            //update the indices and text for words in mListWordsInput

                            // if this correct response makes the sentence correct, go to the next item
                            boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);

                            //if the written sentence is correct
                            if (writtenSentenceIsCorrect) {
                                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
                                clearSentenceAttemptFeatures(); //should go in the animator graph
                            }

                            // if the response is correct at the right place, but there are more corrections to be made, check if the word is correct, turn blue depending on what the attempt level of that sentence is.
                            else{

                                if(currentWordIndex != -1) { //when the replacement is in a word
                                    // check if the word written is correct and release ON_CORRECT. How to check that a word is written correctly? strings should match bw

                                    //refreshing mActiveWord
                                    if (currentWordIndex != -1) {
                                        mActiveWord = mListWordsInput.get(currentWordIndex);
                                    }

                                    String writtenActiveWord = mActiveWord.getWrittenWordString();
                                    String writtenAnswerWord = mListWordsAnswer.get(currentWordIndex).getWordAnswer();
                                    boolean writtenWordIsCorrect = writtenActiveWord.equals(writtenAnswerWord);
                                    if (writtenWordIsCorrect) {
                                        publishFeature(WR_CONST.FTR_WORD_CORRECT);
                                        applyBehavior(WR_CONST.ON_CORRECT); //should turn word blue
//                                        temporaryOnCorrectSentence();

                                    }
                                    //when correct replacement but the word or the sentence is not yet complete.
                                    else{
                                        applyBehavior(WR_CONST.ON_CORRECT);
                                    }
                                }

                                //when not in a word (open in the sentence/space)
                                else{
                                    //maybe just turn this glyph controller blue?
                                    //and inhibit input?
                                }
                            }
                        }

                        // else (if incorrect letter drawn, but correct place chosen for replacement), revert the glyph to what it was, then increase attempt and release on error behavior.
                        else{
                            //revert the glyph to old one.
                            gController.setPreviousGlyph();// put in animator graph
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                            // in the animator graph -> turn the word blue or red.
                            //set the word as red or blue depending on status.
                        }
                    }

                    //when the glyph drawn is at the wrong place(!canReplace), increase the attempt and replace the old glyph that was there.
                    else{

                        //if the letter drawn is the same as the previous one,
                        gController.setPreviousGlyph();
                        //lets increase the attempt for this word, this will also release the corresponding feature which can then be used in the animator graph to call the functions that we want.

                        //if the glyph drawn again is the same as the previous one, don't increase the attempt.
                        if(!mResponse.equals(mWrittenSentence.substring(mActiveIndex,mActiveIndex + 1))) {
                            int attempt = updateAttemptFeature();
                            if (attempt > 4) {
                                applyBehavior(WR_CONST.MERCY_RULE); // goto node "MERCY_RULE_BEHAVIOR"
                            } else {
                                applyBehavior(WR_CONST.ON_ERROR); // goto node "GENERAL_ERROR_BEHAVIOR"
                            }
                        }
                    }


            }
        }
        return _isValid;
    }


    public void autoErase(){
        //auto erase if not valid.
        CGlyphController gController = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
        gController.eraseGlyph();

        CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex);
        resp.setStimulusChar(" ",false);
    }

    //activating edit mode for sentence
    public void activateEditMode(){
        //make the buttons visible
        for (int i = 0; i < mGlyphList.getChildCount(); i++) {
            CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(i);
            controller.showDeleteSpaceButton(true);
            controller.showInsLftButton(true);
            controller.showInsRgtButton(true);
        }

    }

    public void activateEditModeValidOnly(){
        //make the buttons visible
        for (int i = 0; i < mGlyphList.getChildCount(); i++) {
            CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(i);

            if(checkDelete(mEditSequence, i)) {
                controller.showDeleteSpaceButton(true);
            }

            if(checkInsert(mEditSequence, i )) {
                controller.showInsRgtButton(true);
            }

            if(checkInsert(mEditSequence, i + 1)) {
                controller.showInsLftButton(true);
            }
        }
    }

    public void updateExpectedCharacters(){
        int targetIndex = 0;
        for(int i = 0; i < mGlyphList.getChildCount(); i++){

            CGlyphController c = (CGlyphController) mGlyphList.getChildAt(i);
            boolean isInsert = mAlignedSourceSentence.charAt(targetIndex) == '-' && !mResponse.equals("-");
            while(isInsert){
                targetIndex++;
                isInsert = mAlignedSourceSentence.charAt(targetIndex) == '-' && !mResponse.equals("-");
            }

            char targetChar = mAlignedTargetSentence.charAt(targetIndex);
            String expectedChar = Character.toString(targetChar);

            if(expectedChar.equals("-")){
                c.setExpectedChar(" ");
            }

            else {
                c.setExpectedChar(expectedChar);
            }

            //set the protoglyph
            //set the protoglyphs
            if (!expectedChar.equals(" ")) {
//                c.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar)); //amogh comment uncomment when insert and delete.
            }

            targetIndex++;
        }
    }




    /**
     * Designed to enable character-level mercy rule on multi-char sequences...
     * This method not used (does not work as intended)
     */
    public void mercyRuleCleanup() {

        mActiveController.updateAndDisplayCorrectStatus(true);
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

    private void clearSentenceAttemptFeatures(){
        for (String senAttempt : _senAttemptFTR){
            retractFeature(senAttempt);
        }
    }
    //amocorrectionAttgh added

    public void clearHesitationFeatures() {

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
    public int updateSentenceAttemptFeature(){
        clearSentenceAttemptFeatures();
        int attempt = ++mSentenceAttempts;
        if (attempt <= 4){
            publishFeature(_senAttemptFTR.get(attempt - 1));
        }
        return attempt;
    }

    private int updateAttemptFeature() {

        clearAttemptFeatures();
        int attempt;
        // only publish attempt feature for first four attempts... next time will activate mercy rule
        if(activityFeature.contains("FTR_SEN_WRD")){
            attempt = mActiveWord.incAttempt();
        }
        else if(activityFeature.contains("FTR_SEN_SEN")){
            attempt = mActiveWord.incAttempt();
        }
        else{
            attempt = mActiveController.incAttempt();
        }

        if (attempt <= 4)
            publishFeature(_attemptFTR.get(attempt - 1));

        return attempt;
    }

    private int updateHesitationFeature() {

        clearHesitationFeatures();

        int hesitationNo = incHesitationNo();

        // only publish attempt feature for first four attempts... next time will activate mercy rule
        if(hesitationNo <= 4)
            publishFeature(_hesitationFTR.get(hesitationNo-1));

        publishHesitationState(hesitationNo);
        return hesitationNo;
    }

    public void resetHesitationFeature(){
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

    public void cancelAndResetHesitation(String promptName){
        cancelPost(promptName);
        resetHesitationFeature(); //clears hesitation features and resets hesitation number to 0
    }

    public void temporaryOnCorrect(){
        //when transferring to the animator graph,remember to pass the word completed feature and then retract it.
//        deactivateEditModeInActiveWord();  //edit buttons removed for code drop 2 in tanzania
        inhibitWordInput();
        updateLettersWordResponse();
        incrementCurrentWordIndex();
        clearHesitationFeatures(); //although already there in the ON_CORRECT behavior
    }

    public void temporaryOnCorrectSentence(){
//        deactivateEditModeInActiveWord(); //deactivate only if the position of the word is also correct
        inhibitWordInput();
        updateLettersWordResponse();
    }

    //goes over the unverified words and releases apt features.
    public void evaluateSentenceLetterLevel(){

    }

    public void evaluateSentenceWordLevel(){
        //the main structure is that, it updates the sentence parameters, checks if sentence complete, else, checks if all the words not evaluated before are complete, stops at the first incorrect one, takes a call on whether to call error behavior on the incorrect one depending on whether it was being written at the time of evaluation or not.


        //update mEditSequence, mAlignedTarget, mTargetSource with the required changes and aligned source and target string builders
        updateSentenceEditSequence();
        mListWordsInput = getListWordsInputFromAlignedSentences(mAlignedSourceSentence,mAlignedTargetSentence);

        // when the written sentence is correct, apply DATA_COMPLETE_ITEM behavior
        boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);
        if (writtenSentenceIsCorrect) {
            temporaryOnCorrect();
            applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
        }

        //sentence not finished.
        else{
            //starting from the currentWordIndex (since obviously the earlier ones have been evaluated), evaluate words in succession, if correct, turn blue and inhibit input, if wrong, set as mActiveWord, currentWord Index, increase attempt level and disable going to future words.
            for(int i = currentWordIndex; i < mListWordsInput.size(); i++) {

                Word inputWord = mListWordsInput.get(i);

                //need to update the current word index as we are moving forward, this is necessary to make sure that ON_CORRECT behavior works on this word
                currentWordIndex = i;
                mActiveWord = inputWord;

                boolean wordIsCorrect;
//                // so we don't evaluate the correct status everytime, check if the word has been set to correct before.
//                if(inputWord.getWordCorrectStatus()){
//                    wordIsCorrect = true;
//                }
//                else{
                wordIsCorrect = mActiveWord.updateInputWordCorrectStatus(i); //checks indices and strings inside
//                }

                //if the word is correct update its color
                if (wordIsCorrect) {
                    //turn blue
                    //release the oncorrect behavior with word correct feature.
                    publishFeature(WR_CONST.FTR_WORD_CORRECT);
                    applyBehavior(WR_CONST.ON_CORRECT);
                    temporaryOnCorrect();
//                    inputWord.updateWordResponse(); //goes in the animator graph
                }

                //if the word is incorrect, set current word as this, increase attempt if in middle of being written. update colors if
                else {

                    //if this word has already been attempted(ie the case when 1 space has not been deliberately left while continuing writing), increment the word's attempt level (as error) and release corresponding feature
                        boolean writingIsContinued;
                        if(currentWordIndex == 0){
                            writingIsContinued = false;
                        }
                        else{
                            boolean activeWordLengthIsOneOrZero = mActiveWord.getWrittenWordString().length() <= 1 ;
                            boolean previousIsSpace = mWrittenSentence.charAt(mActiveWord.getWordIndices().get(0) - 1) == ' ' ;
                            writingIsContinued = (activeWordLengthIsOneOrZero && previousIsSpace);
                        }

                    //mark the word as wrong only when it was incorrectly written, and not in continuation.
                    if (!writingIsContinued) {
//                        mActiveWord.activateEditMode(); //put in animator graph //edit buttons removed for code drop 2 in tanzania
                        updateAttemptFeature();
                        applyBehavior(WR_CONST.ON_ERROR);
                    }

                    //otherwise it just means that the previous words were correct and you are beginning to write  this word, mActiveWord is set to this word, but no features are to bbe released
                    else {

                    }

                    // break so that it stops at the first incorrect word that is there.
                    break;
                }

                //move to the next iteration only when on_correct is applied
//                while (currentWordIndex == i){
//                    try {
//                    wait();
//                    }
//                    catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                    }
//                }
//                for(;;){
//                    int j = 2 *8;
//                    if(currentWordIndex == i){
//                        j = 2 * 299;
//                    }
//                    else{
//                        j= 2 * 23;
//                        break;
//                    }
//                }
            }
        }

    }

    public void evaluateSentenceFirstTime(){

        // evaluated on end sentence punctuation / hesitation, when the written sentence is correct: apply ON_CORRECT behavior

        //update sentence status
        updateSentenceEditSequence();
        //initialising
        mListWordsInput = getListWordsInputFromAlignedSentences(mAlignedSourceSentence,mAlignedTargetSentence);

        // evaluate sentence
        boolean writtenSentenceIsCorrect = mWrittenSentence.equals(mAnswer);
        if (writtenSentenceIsCorrect) {
            applyBehavior(WR_CONST.DATA_ITEM_COMPLETE);
            clearSentenceAttemptFeatures(); //should go in the animator graph
        }

        //when the written sentence does not match the expected answer
        else{
            publishFeature(WR_CONST.FTR_SEN_EVAL);
            applyBehavior(WR_CONST.ON_ERROR); //activates the edit mode.
        }
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
            CGlyphController c = (CGlyphController) mGlyphList.getChildAt(i1);
            if(!c.isCorrect() && !c.getExpectedChar().equals(" ")) {
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
     * If the user taps the stimulus or the response we try and scroll the tapped char onscreen
     *
     *
     */
    public void stimulusClicked(int touchIndex) {

        CGlyphController   v;
        //check if the touched index is within the range of mGlyphList.
        if(touchIndex >= mGlyphList.getChildCount()){
            touchIndex = mGlyphList.getChildCount() - 1;
        }
        v = (CGlyphController) mGlyphList.getChildAt(touchIndex);

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
            //Imagine the scroll as a longer than screen strip. gx+gw represents the distance bwn start of scroll and end of the view(controller). sx+sw is the distance bwn the start of the scroll and the end of the screen.
            if((gx + gw) > (sx + sw)) {

                mDrawnScroll.captureInitiatorStatus();
                mDrawnScroll.smoothScrollTo(calcOffsetToMakeGlyphVisible(sx, padding), 0);
                mDrawnScroll.releaseInitiatorStatus();
            }

            //else if the start of the screen is ahead of the start of the view, scroll to the start of the view.
            else if(sx > gx) {

                mDrawnScroll.captureInitiatorStatus();
                mDrawnScroll.smoothScrollTo(gx, 0);
                mDrawnScroll.releaseInitiatorStatus();
            }

        }
    }

    public void autoScrollAndPoint(IGlyphController glyphController) {

        CGlyphController view    = (CGlyphController) glyphController;
        int              padding = 2;

        if(view != null) {

            int sx = mDrawnScroll.getScrollX();
            int sw = mDrawnScroll.getWidth();

            int viewWidth = view.getWidth();
            int maxScrollX = mDrawnScroll.getChildAt(0).getMeasuredWidth() - sw;

            int gx = (int) view.getX();
            int gw = viewWidth * 2;



            // If the glyph to the right of the current glyph is partially obscurred then calc
            // the offset to bring it on screen - with some padding (i.e. multiple glyph widths)
            // Capture the initiator status to force the tracker to update in the stimulus field
            //Imagine the scroll as a longer than screen strip. gx+gw represents the distance bwn start of scroll and end of the view(controller). sx+sw is the distance bwn the start of the scroll and the end of the screen.
            if((gx+gw) > (sx + sw)) {

                mDrawnScroll.captureInitiatorStatus();
                int newScroll = calcOffsetToMakeGlyphVisible(sx,padding);
                mDrawnScroll.smoothScrollTo(gx, 0);
                mDrawnScroll.releaseInitiatorStatus();

                int xToPointAt;
                if(gx > maxScrollX){
                    xToPointAt = gx - maxScrollX + viewWidth/2;
                }
                else{
                    xToPointAt = viewWidth/2;
                }

                Intent msg = new Intent(TCONST.POINTAT);
                msg.putExtra(TCONST.SCREENPOINT, new float[]{xToPointAt, (float) ((CGlyphController) glyphController).getBottom()});
                bManager.sendBroadcast(msg);
            }

            //else if the start of the screen is ahead of the start of the view, scroll to the start of the view.
            else if(sx > gx) {

                mDrawnScroll.captureInitiatorStatus();
                mDrawnScroll.smoothScrollTo(gx, 0);
                mDrawnScroll.releaseInitiatorStatus();
                Intent msg = new Intent(TCONST.POINTAT);
                msg.putExtra(TCONST.SCREENPOINT, new float[]{viewWidth/2, (float) ((CGlyphController) glyphController).getBottom()});
                bManager.sendBroadcast(msg);
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

    //this points at the first glyph only
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

    //clear the glyph for current letter, called for letter level sentence copy activity
    public void hideGlyphForActiveIndex(){
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
        //we dont want the stimulus to be erased if the replay skips this glyph(when its already).
        if(!v.isCorrect()) {
            v.hideUserGlyph();
            CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex);
            resp.setStimulusChar(" ", false);
        }
    }

    //amogh added to hide the glyphs for a word only
    public void hideCurrentWordGlyph(){
        mActiveWord.hideWordGlyphs();
    }

    public void clearActiveWordGlyphs(){
        mActiveWord.clearGlyphs();
    }

    public void rippleReplayCurrentWord(String type){
        mActiveWord.rippleReplayWord(type);
        mActiveWord.updatePostReplay();
        if(activityFeature.contains("FTR_SEN_WRD")){
//            evaluateSentenceWordLevel();
        }
        else if(activityFeature.contains("FTR_SEN_SEN")){
            updateSentenceEditSequence();
            mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);
        }
    }

    public void hideSampleForActiveIndex(){
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
        v.showSampleChar(false);
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

    //for hesitation, animate the letter and then disappear
    public void eraseCurrentReplayGlyph(){
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
        v.eraseReplayGlyph();
        invalidate();
    }


    public void rippleReplayDisappearActiveIndex()
    {
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);

        //if already correct move to the next one
        if(v.isCorrect()){
        }

        //if not correct, play replay unless its a space, and update the response, the sentence parameters and the correct status of the glyph
        else {
            v.eraseGlyph();

            String expectedCharString = mAnswer.substring(mActiveIndex,mActiveIndex + 1);

            boolean isSpaceExpected = expectedCharString.equals(" ");

            if (!isSpaceExpected) {
                v.post(WR_CONST.RIPPLE_PROTO);
            }
            else{
//                applyBehavior(WR_CONST.FIELD_REPLAY_COMPLETE);
                publishFeature("FTR_SPACE_REPLAY");
                applyBehavior(WR_CONST.ON_STOP_WRITING); //amogh added for hesitation.
            }
//            v.eraseGlyph();
            applyBehavior(WR_CONST.REPLAY_COMPLETE);
        }
    }

    //for letter level mercy rule, called directly from the animator graph
    public void rippleReplayActiveIndex(){
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);

        //if already correct move to the next one
        if(v.isCorrect()){

            //if complete, pass item complete behavior and return
            if (isComplete()) {
                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
                return;
            }

            //if correct, but not yet complete, increment mActiveIndex and call the fn again
            else if(mActiveIndex < mGlyphList.getChildCount() - 1){
                mActiveIndex++;
                rippleReplayActiveIndex();
            }

            //if reached end, set active index to the first incorrect glyph and call fn again
            else{
                mActiveIndex = 0;
                rippleReplayActiveIndex();
            }
        }

        //if not correct, play replay unless its a space, and update the response, the sentence parameters and the correct status of the glyph
        else {
            v.eraseGlyph();
//            String expectedCharString = v.getExpectedChar(); //comment, this needs to change when insert and delete buttons come in, as then the expected character dynamically changing would also mean changing the protoglyph dynamically, but here expected character is just to enable easy writing.
            String expectedCharString = mAnswer.substring(mActiveIndex,mActiveIndex + 1);

            boolean isSpaceExpected = expectedCharString.equals(" ");

            if (!isSpaceExpected) {
                v.post(WR_CONST.RIPPLE_PROTO);
            }
            else{
//                applyBehavior(WR_CONST.FIELD_REPLAY_COMPLETE);
                publishFeature("FTR_SPACE_REPLAY");
                applyBehavior(WR_CONST.ON_STOP_WRITING); //added to restart hesitation.
            }

            //set the recognised character and set the response color.
            v.setRecognisedChar(expectedCharString);
            CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(mActiveIndex);
            resp.setStimulusChar(expectedCharString, false);
            resp.updateResponseState(true);
            v.inhibitInput(true);
            v.updateCorrectStatus(true);

            //update the sentence parameters
            updateSentenceEditSequence();
//        mListWordsInput = getUpdatedListWordsInput(mListWordsInput, mAlignedSourceSentence,mAlignedTargetSentence);

            //end replay
            //check in the end if the sentence is correct now.
            if(isComplete()){
                applyBehavior(WR_CONST.DATA_ITEM_COMPLETE); // goto node "ITEM_COMPLETE_BEHAVIOR" -- run when item is complete...
            }

            applyBehavior(WR_CONST.REPLAY_COMPLETE);

        }
    }

    //for playing the animation without affecting the response (for hesitation)
    public void rippleReplayActiveIndexHesitation() {
        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
//        v.eraseGlyph();
        String expectedCharString = mAnswer.substring(mActiveIndex,mActiveIndex + 1);
        boolean isSpaceExpected = expectedCharString.equals(" ");
        if (!isSpaceExpected) {
            v.post(WR_CONST.RIPPLE_PROTO);
        }
        else{
//                applyBehavior(WR_CONST.FIELD_REPLAY_COMPLETE);
            publishFeature("FTR_SPACE_REPLAY");
            applyBehavior(WR_CONST.ON_STOP_WRITING); //added to restart hesitation.
        }
    }

    public void rippleReplayWordContinued(){
        //  comment, this might be wrong for the cases when the word is not written at the place it should've been, so to accommodate that, the indices from the answer and not the written part should be received, the glyphs at wrong places(all listindicesanswer) be erased and the correct ones(from listwordsanswer) replaced.
        ArrayList<Integer> activeWordIndices = mActiveWord.listIndicesAnswer;
        int  max = activeWordIndices.get(activeWordIndices.size() - 1);
        if(_fieldIndex <= max) {
            CGlyphController v;
            Word correctWord = mListWordsAnswer.get(currentWordIndex);
            ArrayList<Integer> correctIndices = correctWord.listIndicesAnswer;
            v = (CGlyphController) mGlyphList.getChildAt(_fieldIndex);
            v.post(WR_CONST.RIPPLE_PROTO);
            v.setRecognisedChar(v.getExpectedChar()); //the recognised character is set to the expected character (since the animation).
            //  added to set the valid character in response.

            //  comment move to the animator graph -> call update letters word response,
            CStimulusController resp = (CStimulusController) mResponseViewList.getChildAt(_fieldIndex);
            resp.setStimulusChar(mAnswer.substring(_fieldIndex, _fieldIndex + 1), false);
            resp.updateResponseState(true);
            v.inhibitInput(true);
            _fieldIndex++;
        }
        else{
            applyBehavior(WR_CONST.REPLAY_COMPLETE);
        }
    }

    public void rippleReplay(String type, boolean isDemo) {

        _fieldIndex = 0;
        _replayType = type;
        _isDemo     = isDemo;

        replayNext();
    }
    private void replayNext() {

        //in case of sentence level and word level feedback in the sentence writing activities, just replay the word.
        if(activityFeature.contains("FTR_SEN_WRD") || activityFeature.contains("FTR_SEN_SEN")){
            rippleReplayWordContinued();
        }

        //for non sentence writing activities, when it plays the whole stimulus
        else {
            CStimulusController r;
            CGlyphController v;

            if (_fieldIndex < mGlyphList.getChildCount()) {

                v = (CGlyphController) mGlyphList.getChildAt(_fieldIndex);

                if (v.checkIsStimulus()) {

                    // For write.missingLtr: To omit non-answer Glyphs from replay.
                    _fieldIndex++;
                    replayNext();
                } else {

                    v.post(_replayType);
                    _fieldIndex++;
                }
            } else {
                if (_isDemo)
                    broadcastMsg(TCONST.POINT_FADE);

                applyBehavior(WR_CONST.REPLAY_COMPLETE);
            }
        }
    }

    public void replayActiveGlyph() {

        CGlyphController g;
        for (int i = 0; i < mGlyphList.getChildCount(); i++){
            g = (CGlyphController) mGlyphList.getChildAt(i);
            if(!g.isCorrect()) {
                g.pointAtGlyph();
                break;
            }
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

    public void inhibitSentenceInput(Boolean inhibit){
        IGlyphController glyphController;
        for (int i1 = 0; i1 < mGlyphList.getChildCount(); i1++) {

            glyphController = (IGlyphController) mGlyphList.getChildAt(i1);

            if (glyphController != null) {

                glyphController.inhibitInput(inhibit);
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
            CErrorManager.logEvent(TAG, "Data Exhausted: call past end of data", e, true);
        }
    }



    /**
     * @param data
     * XYZ
     */
    public void updateText(CWr_Data data) {
        CStimulusController r;
        CGlyphController    v;
        CStimulusController resp; //  added

        boolean isStory = data.isStory;
        mStimulus = data.stimulus;
        mAudioStimulus = data.audioStimulus;
        mAnswer = data.answer;
        //  comments
            //remember to empty the containers when new word arrives
        //  comments end

        //  added to initialise words, ideally should be initialised for only the sentence writing activities.
        currentWordIndex = 0; //setting to 0 initially
        mWrittenSentence = "";
        mSentenceAttempts = 0;
        mActiveIndex = 0;

        // Add the recognized response display containers
        //
        _spaceIndices.clear();
        mRecogList.removeAllViews();
        mResponseViewList.removeAllViews(); //  added

        //LOADING THE STIMULUS
        if(!singleStimulus) {

            //for sentence level activities, the stimulus should always be correct ie mAnswer should be loaded in the stimulus
            if(activityFeature.contains("FTR_SEN")){
                for (int i1 = 0; i1 < mAnswer.length(); i1++) {
                    // create a new view
                    r = (CStimulusController) LayoutInflater.from(getContext())
                            .inflate(R.layout.recog_resp_comp, null, false);

                    r.setStimulusChar(mAnswer.substring(i1, i1 + 1), singleStimulus);

                    mRecogList.addView(r);

                    r.setLinkedScroll(mDrawnScroll);
                    r.setWritingController(this);
                }
            }

            //when not a sentence level activity
            else {
                for (int i1 = 0; i1 < mStimulus.length(); i1++) {
                    // create a new view
                    r = (CStimulusController) LayoutInflater.from(getContext())
                            .inflate(R.layout.recog_resp_comp, null, false);

                    r.setStimulusChar(mStimulus.substring(i1, i1 + 1), singleStimulus);

                    mRecogList.addView(r);

                    r.setLinkedScroll(mDrawnScroll);
                    r.setWritingController(this);
                }
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
        //STIMULUS LOADED

        //LOAD GLYPH INPUT CONTAINERS ie WRITING BOXES

        // Add the Glyph input containers
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
                        v.updateAndDisplayCorrectStatus(true);
                    }
                    mGlyphList.addView(v);
                }
                v.setLinkedScroll(mDrawnScroll);
                v.setWritingController(this);
            }
        }

        else if(activityFeature.contains("FTR_SEN_CORR")){
            //load glyph input container
            int stimulusLength = mStimulus.length();
            for(int i1 = 0 ; i1 < stimulusLength ; i1++)
            {
                // create a new view
                v = (CGlyphController)LayoutInflater.from(getContext())
                        .inflate(R.layout.drawn_input_comp, null, false);

                // Last is used for display updates - limits the extent of the baseline
                v.setIsLast(i1 ==  mStimulus.length()-1);

                //  comment - need to check conditions. eg see the next if else.
                String stimulusChar = mStimulus.substring(i1,i1+1);

                //set the stimulus character as the recognised character
                v.setRecognisedChar(stimulusChar);

                if(!stimulusChar.equals(" ")) {
                    v.setStimuliGlyph(_glyphSet.cloneGlyph(stimulusChar));
                }


                v.toggleStimuliGlyph();
                mGlyphList.addView(v);
                v.setLinkedScroll(mDrawnScroll);
                v.setWritingController(this);

                // setting the response view and loading glyph controllers
                v.setResponseView(mResponseViewList);
                resp = (CStimulusController)LayoutInflater.from(getContext())
                        .inflate(R.layout.recog_resp_comp, null, false);
                resp.setStimulusChar(stimulusChar,true);
                resp.setUnderlineVisible(false);
                mResponseViewList.addView(resp);
                resp.setLinkedScroll(mDrawnScroll);
                resp.setWritingController(this);
            }

            //mSentenceAttempts++;
            updateSentenceAttemptFeature();
            //  add finish
        }

        else {

            for(int i1 =0 ; i1 < mAnswer.length() ; i1++)
            {
                // create a new view
                v = (CGlyphController)LayoutInflater.from(getContext())
                        .inflate(R.layout.drawn_input_comp, null, false);

                // Last is used for display updates - limits the extent of the baseline
                v.setIsLast(i1 ==  mAnswer.length()-1);

                //need to set the expected character to something meaningful
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
                //  added
                resp = (CStimulusController)LayoutInflater.from(getContext())
                        .inflate(R.layout.recog_resp_comp, null, false);
                mResponseViewList.addView(resp);
                resp.setUnderlineVisible(false);
                resp.setLinkedScroll(mDrawnScroll);
                resp.setWritingController(this);
                //  add finish
            }
//            initialiseCorrectionHashMap(); //  added
//            initialiseWordIndices(); //  added
        }

        //initialise the mListWordsAnswer for sentence writing activities
        if(activityFeature.contains("FTR_SEN")){
            mListWordsAnswer = new ArrayList<>();
            mListWordsAnswer = getListWords(mAnswer);
            updateSentenceEditSequence();
            mListWordsInput = getListWordsInputFromAlignedSentences(mAlignedSourceSentence,mAlignedTargetSentence);
            mActiveWord = mListWordsAnswer.get(0);

            mActiveIndex = 0;
            mActiveController = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
        }

        //for correction activities set the expected characters and protoglyphs, also show buttons(optional); now that the sentence parameters have been initialised,
        if(activityFeature.contains("FTR_SEN_CORR")){

            int expectedCharIndex = 0;

            // for all glyphs in mGlyphList
            int stimulusLength = mStimulus.length();
            for(int i1 = 0 ; i1 < stimulusLength ; i1++) {

                //if supposed to insert at the index, increment the expectedCharIndex
                while(mAlignedSourceSentence.charAt(expectedCharIndex) == '-'){
                    expectedCharIndex++;
                }

                v = (CGlyphController) mGlyphList.getChildAt(i1);

                //set the expected character
                //set the expected character
                String expectedChar = mAlignedTargetSentence.substring(expectedCharIndex, expectedCharIndex + 1);
                String recognisedChar = v.getRecognisedChar();
                //Make sure that the expected character in places where deletion happens is a space so that during mercy - doesn't play
                if(expectedChar.equals("-")){
                    v.setExpectedChar(" ");
                }
                else{
                    v.setExpectedChar(expectedChar);
                }
                //update the correct status of the glyph.
                if(expectedChar.equals(recognisedChar)){
                    v.updateCorrectStatus(true);
//                    if(activityFeature.contains)
                }

                //set the protoglyphs
                if (!expectedChar.equals(" ")) {
                    v.setProtoGlyph(_glyphSet.cloneGlyph(expectedChar));
                } else {
                    _spaceIndices.add(i1);

                }

                expectedCharIndex++;
            }

            //  comment move this to the animator graph
            //shows all buttons.
//            activateEditModeValidOnly(); //edit buttons removed for code drop 2 in tanzania
        }

//        else if (activityFeature.contains("FTR_SEN")){
////            activateEditMode(); //edit buttons removed for code drop 2 in tanzania
//        }

        //setting the linking lines to be visible when everything loaded.
        if(activityFeature.contains("FTR_SEN")) {
            mWritingBoxLink.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // This is a async callback. get the view size here.
                    mWritingBoxLink.setResponse(mResponseViewList);
                    mWritingBoxLink.setGlyphList(mGlyphList);
//                mWritingBoxLink.invalidate();
                }
            });
        }


    }

    public void refreshWritingBoxLink(){
        mWritingBoxLink.invalidate();
    }



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
                // This is executed when the glyph replay animation ends. We want to play replayNext when not letter level feedback in sentence writing activities; other relevant behaviors
                if(activityFeature.contains("FTR_SEN")){
                    //letter level feedback, sentence activity
                    if(activityFeature.contains("FTR_SEN_LTR")){
                        //in case of sentence writing letter level feedback, it plays for just one letter
                        CGlyphController v = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
                        v.setIsPlaying(false);
                        //if mercy, then release stop writing behavior, else just restart hesitation.
                        //applyBehavior(WR_CONST.ON_STOP_WRITING);
                        //vs
                        applyBehavior(WR_CONST.ON_ANIMATION_COMPLETE);
                        // apply animation complete behavior which maps to stop hesitaiton
                    }
                    //word and sentence level feedback, sentence activity
                    else {
                        applyBehavior(WR_CONST.ON_STOP_WRITING);
                        replayNext();
                    }
                }
                // not a sentence level activity
                else{
                    replayNext();
                }
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

    //  added custom pointing behaviors for sentence writing tutors

//    public void pointAtFirstEdit(){
//        EditOperation = firstEdit = getFirstEditOperation()
//    }

    //points at the first letter of active word
    public void pointAtActiveWord(){
        mActiveWord.pointAtFirstGlyph();
    }

    public void pointAtNextGlyph() {

        CGlyphController g;
        for (int i = 0; i < mGlyphList.getChildCount(); i++){
            g = (CGlyphController) mGlyphList.getChildAt(i);
            if(!g.isCorrect()&&!g.getExpectedChar().equals(" ")) {
//                g.pointAtGlyph();
                autoScrollAndPoint(g);
//                autoScroll(g);
                break;
            }
        }
    }

    //  added ends

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

    //  added
    private ArrayList<Word> getListWords(String answer){
//        String[] wordsStimulus = mStimulus.split(" ");
        String[] words = answer.split(" ");
        int lengthSentence = words.length;
        int letterIndex = 0;
        ArrayList<Integer> wordIndices;
        ArrayList<Word>   listWords = new ArrayList<Word>();
        // iterating over each word.
        for (int j = 0; j < lengthSentence; j++){
            String word = words[j];
            int lengthWord = word.length();
            wordIndices = new ArrayList<>();
            // adding all indices for each word
            for (int i = 0; i<lengthWord; i++){
                wordIndices.add(letterIndex+i);
            }

            Word w = new Word(j , word, wordIndices);
            listWords.add(w);
            // moving on to the next word
            letterIndex += (lengthWord+1);
        }
        return listWords;
    }



    //called by ON_CORRECT, to call on things on mActiveWord (such as updating colors or increasing the current word index.)
    public void inhibitWordInput(){
        mActiveWord.inhibitWordInput();
    }

    public int incrementCurrentWordIndex(){
        currentWordIndex++;
        return currentWordIndex;
    }

    public int incrementActiveIndex(){
        if(mActiveIndex < mGlyphList.getChildCount() - 1) {
            mActiveIndex++;
        }
        return mActiveIndex;
    }

    public void updateSentenceResponse(Boolean sentenceStatus) {
        // change the color for all letters according to the state of the sentence.
        for (int j = 0; j < mListWordsInput.size(); j++) {
            Word word = mListWordsInput.get(j);
            ArrayList<Integer> wordIndices = word.getWordIndices();
            for (int i = 0; i < wordIndices.size(); i++) {
                int index = wordIndices.get(i);
                CStimulusController responseController = (CStimulusController) mResponseViewList.getChildAt(index);
                responseController.updateResponseState(sentenceStatus);
            }
        }
    }

    public void updateWordResponse(){
        mActiveWord.updateWordResponse();
    }

    public void updateLettersWordResponse(){
        mActiveWord.updateLettersWordResponse();
    }



    //  added place for sentence level functions and ideas
    public class Sentence{
        // should have the mAnswer ie the main sentence
        // should have the response sentence.
        //function to get the
        private ArrayList<Word> listWordsInput;
        private String writtenSentence;
        private String answerSentence;
        private StringBuilder editSequence;
        private StringBuilder alignedSourceSentence;
        private StringBuilder alignedTargetSentence;
        private int sentenceAttempts;

        public Sentence(String answerSentence){
        }

        public void updateSentence(){

        }
    }

    //
    public ArrayList<Word> getListWordsInputFromAlignedSentences(StringBuilder alignedSource, StringBuilder alignedTarget){

        ArrayList<Word>   listWords = new ArrayList<Word>();
        int lengthSource = alignedSource.length();
        int wordCount = 0;
        int letterIndex = 0;
        ArrayList<Integer> wordIndices = new ArrayList<>();
        ArrayList<Boolean> correctIndices = new ArrayList<> ();  //an important fact -> this means that the letters are the same in the aligned version ie the position might still be wrong for the words even though the letters match.
        String word = "";

        //iterate over all the characters of the source.
        for (int i = 0; i < lengthSource; i++){

            //if the last letter or space which is not to be deleted, or space insertion needed, make a new word.
            if((i == lengthSource - 1) || (alignedSource.charAt(i) == ' ' && alignedTarget.charAt(i) == ' ') || (alignedTarget.charAt(i) == ' ')){

                //if the last letter and its not '-', include it in the word.
                if(i == lengthSource - 1 && alignedSource.charAt(i) != '-') {
                    if(alignedSource.charAt(i) == alignedTarget.charAt(i)){
                        correctIndices.add(true);
                    }
                    else{
                        correctIndices.add(false);
                    }
                    word += alignedSource.charAt(i);
                    wordIndices.add(letterIndex);
                }
                //when there is a space (which ought to be there), do not
                else if(alignedSource.charAt(i) == ' ' && alignedTarget.charAt(i) == ' '){
                }
                // do nothing when there is supposed to be a space insertion or replacement.(ie cases where there ought to be a space but there isn't)
                else {}
                ArrayList<Integer> copyWordIndices = (ArrayList<Integer>) wordIndices.clone();
                ArrayList<Boolean> correctWordIndices= (ArrayList<Boolean>) correctIndices.clone();
                Word w = new Word(wordCount, word, copyWordIndices, correctWordIndices);
                listWords.add(w);
                word = "";
                wordCount++;
                wordIndices.clear();
                correctIndices.clear();
            }

            // when new word is not to be created, add the current letter to the new word when its not '-'
            else if(alignedSource.charAt(i) != '-'){
                if(alignedSource.charAt(i) == alignedTarget.charAt(i)){
                    correctIndices.add(true);
                }
                else{
                    correctIndices.add(false);
                }
                word += alignedSource.charAt(i);
                wordIndices.add(letterIndex);
            }

            //increment the letter index whenever the character is not '-'
            if (alignedSource.charAt(i) != '-'){
                letterIndex++;
            }
        }

        return listWords;

    }

    //for sentence writing specifically

    public int getActiveWordIndexForEdit(EditOperation e){

        String operation = e.toString();
        int editIndex = e.getIndex();

        //check in each words indices.
        for(int i = 0; i < mListWordsInput.size() ; i++){

            //check if the index is present in any of the words in mListWords.
            Word w = mListWordsInput.get(i);
            boolean present;
            ArrayList<Integer> ind = w.getWordIndices();

            //check if the index at which the edit is to be made is inside the indices of one of the words
            if(ind.contains(editIndex)){
                present = true;
            }
            else{
                present = false;
            }

            //if the edit is an insert, it is not inside a word if its at the first letter.
            if(operation.equals("I")){
                if(ind.get(0) == editIndex) present = false;
            }

            if (present){
                return i;
            }
        }

        //if index is not present in any of the words.
        return -1;
    }

    public int getActiveWordIndex(int index){

        //used when user writes in an input container in sentence level feedback activity.
        for(int i = 0; i < mListWordsInput.size() ; i++){
            //check if the index is present in any of the words in mListWords.
            boolean present = mListWordsInput.get(i).isIndexInWord(index);
            if (present){
                return i;
            }
        }

        //if index is not present in any of the words.
        return -1;
    }

    //After some changes have been made, the word indices and the text would have been updated, but the attempts should remain the same.
    public ArrayList<Word> getUpdatedListWordsInput(ArrayList<Word> oldListWords, StringBuilder alignedSource, StringBuilder alignedTarget){
        ArrayList<Word> newListWords = getListWordsInputFromAlignedSentences(alignedSource,alignedTarget);
        //setting the
        for(int i = 0; i < oldListWords.size(); i++){
            newListWords.get(i).setAttempt(oldListWords.get(i).getAttempt());
        }
        return newListWords;
    }

    //  added function to get user written sentence.
    public String getWrittenSentence(){
        String sen = "";
        for (int i = 0; i < mGlyphList.getChildCount(); i++){
            CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(i);
            String recChar = controller.getRecognisedChar();
            if(recChar.equals("")){
                sen += " ";
            }
            else{
                sen += recChar;
            }
        }
        return sen;
    }

    //gets the target character which should be replaced at an index.
    public String getReplacementTargetString(StringBuilder editSequence, int index){

        for (int i = 0; i <= index; i++) {
            char c = editSequence.charAt(i);
            if (c == 'I') {
                index++;
            }
        }

        char replacementTargetString = mAlignedTargetSentence.charAt(index);
        return String.valueOf(replacementTargetString);
    }

    //  added functions to test the edits.

    //to update the mEditSequence, mAlignedSourceSentence and mAlignedTargetSentence after every change
    public void updateSentenceEditSequence(){
        mWrittenSentence = getWrittenSentence();
        ArrayList<StringBuilder> edits = computeEditsAndAlignedStrings(mWrittenSentence, mAnswer);
        mEditSequence = edits.get(2);
        mAlignedSourceSentence = edits.get(0);
        mAlignedTargetSentence = edits.get(1);
    }

    public boolean checkDelete (StringBuilder editSequence, int index){

        //skipping over I only as that is where "-" appears in the source string.
        for (int i = 0; i <= index; i++){
            char c = editSequence.charAt(i);
            if (c == 'I'){
                index++;
            }
        }

        boolean isDelete= editSequence.charAt(index) == 'D';
        return isDelete;
    }

    public boolean checkReplace(StringBuilder editSequence, int index){

        //skipping over I only as that is where "-" appears in the source string.
        for (int i = 0; i <= index; i++) {
            char c = editSequence.charAt(i);
            if (c == 'I') {
                index++;
            }
        }

        boolean isReplace = editSequence.charAt(index) == 'R';
        return isReplace;
    }

    public boolean checkInsert (StringBuilder editSequence, int index){

        //skipping over I only as that is where "-" appears in the source string.
        for (int i = 0; i < index; i++){
            char c = editSequence.charAt(i);
            if (c == 'I'){
                index++;
            }
        }

        boolean isInsert = editSequence.charAt(index) == 'I';
        return isInsert;
    }
    //  add ends


    public class Word{
//        private ArrayList<Integer> listIndicesStimulus;
        private ArrayList<Integer> listIndicesAnswer; //stores the indices of the letters for this word (for the mAnswer String)
        private ArrayList<Boolean> listCorrectStatus; //stores the status of each word (not correct may also mean that there are no glyphs present inside ), updated after each word is evaluated
        private ArrayList<Boolean> listHasGlyph; //stores the boolean which says if a glyph is present in the controller or not.
//        private String wordStimulus;
        private int attempt;
        private String wordAnswer;
        private int index;
        private boolean wordIsCorrect;

        public Word(int index, String wordAnswer, ArrayList<Integer> listIndicesAnswer) {
            this.index = index;
            this.wordAnswer = wordAnswer;
            this.listIndicesAnswer = listIndicesAnswer;
            this.attempt = 0;
        }

        public Word(int index, String wordAnswer, ArrayList<Integer> listIndicesAnswer, ArrayList<Boolean> listCorrectStatus) {
            this.index = index;
            this.wordAnswer = wordAnswer;
            this.listIndicesAnswer = listIndicesAnswer;
            this.attempt = 0;
            this.listCorrectStatus = listCorrectStatus;
        }

        public String getWordAnswer(){
            return wordAnswer;
        }

        public ArrayList<Integer> getWordIndices(){
            return listIndicesAnswer;
        }

        public ArrayList<Boolean> getLettersStatus() {
            return listCorrectStatus;
        }

        public boolean isIndexInWord(int index){
            if(listIndicesAnswer.contains(index)){
                return true;
            }
            else{
                return false;
            }
        }

        public String getWrittenWordString(){
                String word = "";
                for (int i = 0; i < listIndicesAnswer.size(); i++){
                    CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(listIndicesAnswer.get(i));
                    String recChar = controller.getRecognisedChar();
                    word += recChar;
                }
                return word;
        }

        public void evaluateLettersCorrectStatus(){

        }

        public boolean getWordCorrectStatus(){
            return wordIsCorrect;
        }

        //checks if the input word is the same as the answer(letters AND the indices) - argument is the index of the current word
        public boolean updateInputWordCorrectStatus(int index){

            //indices should be same and letters should be same
            Word answerWord = mListWordsAnswer.get(index);
            ArrayList<Integer> answerIndices = answerWord.getWordIndices();

            //check the letters in the words
            boolean wordStringsEqual = getWrittenWordString().equals(answerWord.getWordAnswer());
            if(!wordStringsEqual) {
                wordIsCorrect = false;
                return false;
            }

            //check the position indices.
            boolean isWordSizeSame = listIndicesAnswer.size() == answerIndices.size();
            if(!isWordSizeSame){
                wordIsCorrect = false;
                return false;
            }

            for(Integer i: listIndicesAnswer) {
                boolean answerContainsLetter = answerIndices.contains(i);
                if(!answerContainsLetter){
                    wordIsCorrect = false;
                    return false;
                }
            }
            wordIsCorrect = true;
            return true;
        }
//        public boolean getWordCorrectStatus(){
//                listCorrectStatus = new ArrayList<>();
//                listHasGlyph = new ArrayList<>();
//
//                //update the correct status and the status of having glyphs for the letters of this word
//                for (int i : listIndicesAnswer){
////                    CStimulusController respController = (CStimulusController) mResponseViewList.getChildAt(i);
//                    CGlyphController g = (CGlyphController) mGlyphList.getChildAt(i);
//                    Boolean status = g.isCorrect();
//                    listCorrectStatus.add(status);
//                    listHasGlyph.add(g.hasGlyph());
//                }
//
//                    //evaluate the word's correct status
//                for(int i = 0; i < listCorrectStatus.size(); i++){
//
//                    //when not correct and glyph present -> set wordIsCorrect to false
//                    if (listCorrectStatus.get(i) == false && listHasGlyph.get(i)){
//                        wordIsCorrect = false;
//                        break;
//                    }
//
//                    //when correct and glyph present set wordIsCorrect to True.
//                    else if (listHasGlyph.get(i) && listCorrectStatus.get(i)){
//                        wordIsCorrect = true;
//                    }
//
//                    // for the remaining case(when the glyph is not present)
//                    else {
//                        return false;
//                    }
//                }
//
//                return wordIsCorrect;
//
////            updateWordStimulus(); //  this is here only for debugging purposes, actually will be called from the animator graph
////            inhibitWordInput(); //   added - this is here only for debugging purposes, actually will be called by the animator graph.
//        }

        public void updateWordResponse(){
            boolean wordStatus = wordIsCorrect;
            // change the color for all written letters according to the state of the word.
                for(int i = 0; i < listCorrectStatus.size(); i++){
                    int index = listIndicesAnswer.get(i);
                    if(wordAnswer.charAt(i) != ' ') {
                        CStimulusController responseController = (CStimulusController) mResponseViewList.getChildAt(index);
                        responseController.updateResponseState(wordStatus);
                    }
                }
        }

        public void updateLettersWordResponse(){
            for(int i = 0; i < listCorrectStatus.size(); i++){
                int index = listIndicesAnswer.get(i);
//                if()
                CStimulusController responseController = (CStimulusController) mResponseViewList.getChildAt(index);
                responseController.updateResponseState(listCorrectStatus.get(i));
            }
        }

        public void inhibitWordInput(){
            for(int i = 0; i < listIndicesAnswer.size(); i++){
                boolean letterIsCorrect = listCorrectStatus.get(i);
                int glyphIndex = listIndicesAnswer.get(i);
                if(letterIsCorrect) {
                    CGlyphController glyphController = (CGlyphController) mGlyphList.getChildAt(glyphIndex);
                    glyphController.inhibitInput(true);
                }
            }
        }

        public void inhibitOthers(){
            for (int i = 0; i < mGlyphList.getChildCount(); i++){
                CGlyphController glyphController = (CGlyphController) mGlyphList.getChildAt(index);
                if(!listIndicesAnswer.contains(i)){
                    glyphController.inhibitInput(true);
                }
                else{
                    glyphController.inhibitInput(false);
                }
            }
        }

        public int incAttempt(){
            attempt++;
            return attempt;
        }

        public int getAttempt(){
            return attempt;
        }

        public int setAttempt(int newAttempts){
            attempt = newAttempts;
            return attempt;
        }

        //gets the width of the word
        public int getWidth(){
            int leftLetterIndex = listIndicesAnswer.get(0);
            int left = mResponseViewList.getChildAt(leftLetterIndex).getLeft();
            int rightLetterIndex = listIndicesAnswer.get(listIndicesAnswer.size()-1);
            int right = mResponseViewList.getChildAt(rightLetterIndex).getRight();
            int wid = right - left;
            return wid;
        }

        //gets the width for the part of word that is written. if nothing is written width 0.
        public int getWrapWidth(){

            String writtenWord = getWrittenWordString();
            int rightLetterIndex = 0;
            int wid = 0;

            //if the word is empty return the width of the word as 0.
            if(writtenWord.equals("")){
                wid = 0;
                return wid;
            }

            //otherwise find the index upto which the word has been written.
            else {
                for (int i = writtenWord.length() - 1; i >= 0; i--) {
                    if (writtenWord.charAt(i) != ' ') {
                        rightLetterIndex = i;
                        break;
                    }
                }

                int leftLetterIndex = listIndicesAnswer.get(0);
                int left = mResponseViewList.getChildAt(leftLetterIndex).getLeft();
                int right = mResponseViewList.getChildAt(rightLetterIndex).getRight();
                wid = right - left;
                return wid;
            }
        }

        //gets the left coordinate of the word
        public int getLeft(){
            int left = mResponseViewList.getChildAt(listIndicesAnswer.get(0)).getLeft();
            return left;
        }

        public int getHeight(){
            int height = mResponseViewList.getChildAt(listIndicesAnswer.get(0)).getHeight();
            return height;
        }

        //gets the first edit of the word, although the index wrt its start point
        public EditOperation getFirstWordEditOperation(){
            String sourceWord = this.getWrittenWordString();
            String targetWord = this.getWordAnswer();
            EditOperation firstEdit = getFirstEditOperation(sourceWord,targetWord);
            return firstEdit;
        }

        //gives the left coordinate of the first edit wrt the origin
        public int getFirstEditLeft(){
            //since the first edit obtained here is wrt this word's start,
            EditOperation firstEdit = this.getFirstWordEditOperation();
            String firstEditOperation = firstEdit.toString();
            int left = firstEdit.getLeft();
            int firstEditIndex = listIndicesAnswer.get(0) + firstEdit.getIndex();

            // if insert at index i -> set the box left (centre of view at i-1); set the box right (centre of view at i).
            if(firstEditOperation.equals("I")){
                //  comment add the case when the index is not first or last.
                left = (mResponseViewList.getChildAt(firstEditIndex - 1).getLeft() + mResponseViewList.getChildAt(firstEditIndex - 1).getRight())/2;
                int right = (mResponseViewList.getChildAt(firstEditIndex).getLeft() + mResponseViewList.getChildAt(firstEditIndex).getRight())/2;
//                int wid = right-left;
            }

            //if delete at index i -> set the box as left and right for the view at i
            else if(firstEditOperation.equals("D")){
                left = mResponseViewList.getChildAt(firstEditIndex).getLeft();
                int right = mResponseViewList.getChildAt(firstEditIndex).getRight();
//                int wid = right-left;
            }

            //if replace at index i -> set the box as left and right for the view at i
            else if(firstEditOperation.equals("R")){
                left = mResponseViewList.getChildAt(firstEditIndex).getLeft();
                int right = mResponseViewList.getChildAt(firstEditIndex).getRight();
//                int wid = right-left;
            }
            else{

            }
            return left;
        }

        public void releaseFirstWordEditAudioFeatures(){
            EditOperation firstEdit = this.getFirstWordEditOperation();
            releaseAudioFeatures(firstEdit);
        }

        public void hideWordGlyphs(){
            CGlyphController   v;

            for(int i1 : this.listIndicesAnswer) {

                v = (CGlyphController) mGlyphList.getChildAt(i1);

                v.hideUserGlyph();
            }
        }

        public void clearGlyphs(){
            CGlyphController v;
            for (int i1 : listIndicesAnswer){
                v = (CGlyphController) mGlyphList.getChildAt(i1);
                v.post(WR_CONST.ERASE_GLYPH);
            }
        }
        private int _fieldIndex;
        public void rippleReplayWord(String command){
            _fieldIndex = listIndicesAnswer.get(0);
            rippleReplayWordContinued();
        }


        public void updatePostReplay(){
            //need to update the
        }


        public void showHighlightBox(){
            mHighlightErrorBoxView = new View (getContext());

            int wid = this.getWrapWidth();
            int left = this.getLeft();
            int height = this.getHeight();


            //now that the width and the position of this box has been set, set its drawable and show for some time.
            mHighlightErrorBoxView.setX((float)left);
            mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid, height));
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
            }, 4000);
        }

        public void activateEditMode(){
            //make the buttons visible
            for (int i : listIndicesAnswer) {
                CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(i);
                controller.showDeleteSpaceButton(true);
                controller.showInsLftButton(true);
                controller.showInsRgtButton(true);
            }
        }

        public void deactivateEditMode(){
            //make the buttons visible
            for (int i : listIndicesAnswer) {
                CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(i);
                controller.showDeleteSpaceButton(false);
                controller.showInsLftButton(false);
                controller.showInsRgtButton(false);
            }
        }

        public void showSamples(boolean show){
            for (int i = 0; i < listIndicesAnswer.size();i++) {
                CGlyphController controller = (CGlyphController) mGlyphList.getChildAt(listIndicesAnswer.get(i));
                boolean letterStatus = listCorrectStatus.get(i);
                if (letterStatus != true){
                    controller.showSampleChar(show);
                }
            }
        }

        //point to the first glyph if its empty.
        public void pointAtFirstGlyph(){
            int firstLetterIndex = listIndicesAnswer.get(0);
            CGlyphController firstGlyph = (CGlyphController) mGlyphList.getChildAt(firstLetterIndex);
            if(!firstGlyph.hasGlyph()) {
                firstGlyph.pointAtGlyph();
            }
        }


        //add a new function to word class here

    }
    //Word class ends

    //  added functions for highlight box
    public EditOperation getFirstEditOperation(String source, String target){
        ArrayList<StringBuilder> edits = computeEditsAndAlignedStrings(source, target);
        StringBuilder alignedSource = edits.get(0);
        StringBuilder alignedTarget = edits.get(1);
        StringBuilder editSeq = edits.get(2);
        EditOperation e = new EditOperation("N",'N', 0);
        char val;
        String op;
        for(int i = 0; i < editSeq.length(); i++){
            char c = editSeq.charAt(i);
            // return the first non "N" operation
            if (c == 'I'){
                val = alignedTarget.charAt(i);
                val = alignedTarget.charAt(i);
                e = new EditOperation("I", val, i);
                break;
            }
            else if(c == 'D'){
                val = alignedSource.charAt(i);
                e = new EditOperation("D", val, i);
                break;
            }
            else if(c == 'R'){
                val = alignedTarget.charAt(i);
                char prev = alignedSource.charAt(i);
                e = new EditOperation("R", val, i, prev);
                break;
            }
        }
        return e;
    }

    public void releaseAudioFeatures(EditOperation e){

        //retract all audio features
        for (String audioFtr : _audioFTR) {
            retractFeature(audioFtr);
        }

        //release new features
        String editName = e.toString();
        int editIndex = e.getIndex();
        String editValue = String.valueOf(e.getValue());

        //for insert operation
        if(editName.equals("I")){

            publishFeature(WR_CONST.FTR_INSERT);

            //insert spacing
            if(editValue.equals(" ")){
                publishFeature(WR_CONST.FTR_AUDIO_SPACE);
            }

            //insert punctuation
            else if (punctuationSymbols.contains(editValue)){
                publishFeature(WR_CONST.FTR_AUDIO_PUNC);
                publishFeature(punctuationToFeature.get(editValue));
                publishValue(WR_CONST.AUDIO_PUNCTUATION, punctuationToString.get(editValue));
            }

            //insert letter
            else{
                publishFeature(WR_CONST.FTR_AUDIO_LTR);
                publishValue(WR_CONST.AUDIO_LETTER, editValue.toUpperCase());
            }


        }

        //for relacement operation
        else if(editName.equals("R")){

            publishFeature(WR_CONST.FTR_REPLACE);

            String prev = String.valueOf(e.getPrevious());

            //replace with space
            if(editValue.equals(" ")){
                publishFeature(WR_CONST.FTR_AUDIO_SPACE);
            }

            //replace with an uppercase letter
            else if(prev.equals(editValue.toLowerCase())){
                publishFeature(WR_CONST.FTR_AUDIO_CAP);
                publishValue(WR_CONST.AUDIO_LETTER, editValue.toUpperCase());
            }

            //replace with punctuation
            else if (punctuationSymbols.contains(editValue)){
                publishFeature(WR_CONST.FTR_AUDIO_PUNC);
                publishFeature(punctuationToFeature.get(editValue));
                publishValue(WR_CONST.AUDIO_PUNCTUATION, punctuationToString.get(editValue));
            }

            //replace with a new letter
            else{
                publishFeature(WR_CONST.FTR_AUDIO_LTR);
                publishValue(WR_CONST.AUDIO_LETTER, editValue.toUpperCase());

            }

        }

        //for delete operation
        else if(editName.equals("D")){

            publishFeature(WR_CONST.FTR_DELETE);

            //delete spacing
            if(editValue.equals("")){
                publishFeature(WR_CONST.FTR_AUDIO_SPACE);
            }

            //delete punctuation
            else if (punctuationSymbols.contains(editValue)){
                publishFeature(WR_CONST.FTR_AUDIO_PUNC);
                publishFeature(punctuationToFeature.get(editValue));
                publishValue(WR_CONST.AUDIO_PUNCTUATION, punctuationToString.get(editValue));
            }

            //delete letter
            else{
                publishFeature(WR_CONST.FTR_AUDIO_LTR);
                publishValue(WR_CONST.AUDIO_LETTER, editValue.toUpperCase());
            }

        }

        //for none operation
        else{
            publishFeature(WR_CONST.FTR_AUDIO_NO_ERROR);
        }
        int i = 1; //just to put a breakpoint and see the features.

    }

    public void releaseFirstEditAudioFeatures() {
        EditOperation firstEdit = getFirstEditOperation(mWrittenSentence, mAnswer);
        releaseAudioFeatures(firstEdit);
    }
//    public void releaseFirstEditAudioFeatures(){
//        if (activityFeature.contains("FTR_SEN_WRD")){
//            int wordAttempts = mActiveWord.getAttempt();
////            if(wordAttempts > 0){  // the number of attempts can be passed through the animator graph itself.
//                mActiveWord.releaseFirstWordEditAudioFeatures();
////            }
//        }

//    }

//    public void

    //  added ends

//  added class to handle string computation
    //Some changes to https://codereview.stackexchange.com/questions/126236/levenshtein-distance-with-edit-sequence-and-alignment-in-java
public class EditOperation {

    private  String  operation;
    private  char    value;
    private  char    previous;
    private  int     index;

    public EditOperation(String operation,char value, int index) {
        this.operation = operation;
        this.index = index;
        this.value = value;
    }

    public EditOperation(String operation,char value, int index,char previous) {
        this.operation = operation;
        this.index = index;
        this.value = value;
        this.previous = previous;
    }

    public String toString() {
        return operation;
    }

    public void setValue(char value){
        this.value = value;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public char getValue(){
        return this.value;
    }

    public int getIndex(){
        return this.index;
    }

    public void setPrevious(char previous) {
        this.previous = previous;
    }

    public char getPrevious(){return this.previous; }

    public int getLeft(){

        int left;
        // if insert at index i -> set the box left (centre of view at i-1); set the box right (centre of view at i).
        if(operation.equals("I")){
            //  comment add the case when the index is not first or last.
            left = (mResponseViewList.getChildAt(index - 1).getLeft() + mResponseViewList.getChildAt(index - 1).getRight())/2;
            int right = (mResponseViewList.getChildAt(index).getLeft() + mResponseViewList.getChildAt(index).getRight())/2;
//                int wid = right-left;
        }

        //if delete at index i -> set the box as left and right for the view at i
        else if(operation.equals("D")){
            left = mResponseViewList.getChildAt(index).getLeft();
            int right = mResponseViewList.getChildAt(index).getRight();
//                int wid = right-left;
        }

        //if replace at index i -> set the box as left and right for the view at i
        else if(operation.equals("R")){
            left = mResponseViewList.getChildAt(index).getLeft();
            int right = mResponseViewList.getChildAt(index).getRight();
//                int wid = right-left;
        }

        //if not insertion/deletion/replacement
        else{
            left = 0;
        }

        return left;
    }

    public void showHighlightBox(){
        mHighlightErrorBoxView = new View (getContext());
        int wid = mResponseViewList.getChildAt(0).getWidth();;
        int left = this.getLeft();
        int height = mResponseViewList.getChildAt(0).getHeight();

        //now that the width and the position of this box has been set, set its drawable and show for some time.
        mHighlightErrorBoxView.setX((float)left);
        mHighlightErrorBoxView.setLayoutParams(new LayoutParams(wid, height));
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
        }, 4000);
    }

}


    //main function to get string edit distance.*
    public ArrayList computeEditsAndAlignedStrings(String string1, String string2) {

        ArrayList<EditOperation> listOperations = new ArrayList<EditOperation>();

        string1 = "\u0000" + string1;
        string2 = "\u0000" + string2;

        final int n = string1.length();
        final int m = string2.length();
        // System.out.println("__m__"+m+n);
        final int[][] d = new int[m + 1][n + 1];
        final Map<Point, Point> parentMap = new HashMap<>();

        for (int i = 1; i <= m; ++i) {
            d[i][0] = i;
        }

        for (int j = 1; j <= n; ++j) {
            d[0][j] = j;
        }

        for (int j = 1; j <= n; ++j) {
            for (int i = 1; i <= m; ++i) {
                final int delta = (string1.charAt(j - 1) == string2.charAt(i - 1)) ? 0 : 1;

                int tentativeDistance = d[i - 1][j] + 1;
                String editOperation = "insert";

                if (tentativeDistance > d[i][j - 1] + 1) {
                    tentativeDistance = d[i][j - 1] + 1;
                    editOperation = "delete";
                }

                if (tentativeDistance > d[i - 1][j - 1] + delta) {
                    tentativeDistance = d[i - 1][j - 1] + delta;
                    editOperation = "replace";
                }

                d[i][j] = tentativeDistance;

                switch (editOperation) {
                    case "replace":
                        parentMap.put(new Point(i, j), new Point(i - 1, j - 1));
                        break;

                    case "insert":
                        parentMap.put(new Point(i, j), new Point(i - 1, j));
                        break;

                    case "delete":
                        parentMap.put(new Point(i, j), new Point(i, j - 1));
                        break;
                }
            }
        }

        final StringBuilder topLineBuilder      = new StringBuilder(n + m);
        final StringBuilder bottomLineBuilder = new StringBuilder(n + m);

        Point current = new Point(m, n);
        // System.out.println("__mn__"+m+" , "+n);
        //backtracking through the parent map which stores the arrows for how we arrived there
        while (true) {
            Point predecessor = parentMap.get(current);

            if (predecessor == null) {
                break;
            }
            // System.out.println("__current__"+current.x+" , "+current.y);
            // System.out.println("__predecessor__"+predecessor.x+" , "+predecessor.y);

            if (current.x != predecessor.x && current.y != predecessor.y) {
                final char schar = string1.charAt(predecessor.y);
                final char zchar = string2.charAt(predecessor.x);

                topLineBuilder.append(schar);
                bottomLineBuilder.append(zchar);

                if(schar != zchar){
//                    System.out.println("____substitute____" + schar +"__with___"+zchar+"___at___"+ (current.x-2));
                    EditOperation e = new EditOperation("R",zchar,current.x-2);
                    listOperations.add(e);
                }
                else{
//                    System.out.println("____no change____" + schar +"__with___"+zchar+"___at___"+ (current.x-2));
                    EditOperation e = new EditOperation("N", zchar, current.x -2);
                    listOperations.add(e);
                }
            }

            else if (current.x != predecessor.x) {
//                System.out.println("____inserting____" + string2.charAt(current.y-1) + "___at___"+ (predecessor.y-2));
//                char a = string2.charAt(predecessor.x);
                topLineBuilder.append("-");
                bottomLineBuilder.append(string2.charAt(predecessor.x));
                EditOperation e = new EditOperation("I", string2.charAt(predecessor.x),predecessor.y - 1); //previously current.y
                listOperations.add(e);
            }

            else {
//                System.out.println("____delete____" + string1.charAt(current.y-1) +"___at___"+ (current.y-2));
                topLineBuilder.append(string1.charAt(predecessor.y));
                bottomLineBuilder.append('-');
                EditOperation e = new EditOperation("D", string1.charAt(current.y-1), current.y-2);
                listOperations.add(e);
            }

            current = predecessor;
        }
        Collections.reverse(listOperations);

        topLineBuilder     .deleteCharAt(topLineBuilder.length() - 1);
        bottomLineBuilder .deleteCharAt(bottomLineBuilder.length() - 1);
        listOperations.remove(0);

        topLineBuilder     .reverse();
        bottomLineBuilder .reverse();

        //to debug and see the edit sequence:
        StringBuilder ops = new StringBuilder();
        ArrayList<Integer> changeIndices = new ArrayList<Integer>();
        for (EditOperation elem : listOperations){
            ops = ops.append(elem.toString());
            changeIndices.add(elem.getIndex());
        }
        ArrayList<StringBuilder> results = new ArrayList<StringBuilder>();
        results.add(topLineBuilder);
        results.add(bottomLineBuilder);
        results.add(ops);
        return results;
    }


    //  added ends

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

                    case WR_CONST.HIDE_CURRENT_WORD_GLYPHS:
                        hideCurrentWordGlyph();
                        break;

                    case WR_CONST.HIDE_CURRENT_LETTER_GLYPH:
                        hideGlyphForActiveIndex();
                        break;

                    case WR_CONST.HIDE_SAMPLES:
                        hideSamples();
                        break;

                    case WR_CONST.HIDE_SAMPLE_ACTIVE_INDEX:
                        hideSampleForActiveIndex();
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
                    //  added
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
                        break;
                    //  added ends

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
                    case WR_CONST.RIPPLE_REPLAY_WORD:
                        rippleReplayCurrentWord(_command);
                        break;
                    case WR_CONST.SHOW_TRACELINE: // Show all glyphs trace line.

                        showTraceLine();
                        break;
                    case WR_CONST.HIDE_TRACELINE: // Hide all glyphs trace line.

                        hideTraceLine();
                        break;
                    case WR_CONST.SHOW_SAMPLE:

                        mActiveController = (CGlyphController) mGlyphList.getChildAt(mActiveIndex);
                        boolean isExpectedCharacterSpace = mActiveController.getExpectedChar().equals(" ") || mActiveController.getExpectedChar().equals("");
                        boolean isCorrect = activityFeature.contains("FTR_SEN_LTR") && mActiveController.isCorrect();
                        if(isExpectedCharacterSpace || isCorrect){
                            publishFeature("FTR_SPACE_SAMPLE");
                            break;
                        }

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

                    case WR_CONST.AUTO_ERASE:
                        autoErase();
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
    public void post(String command, String target, String item, long delay) {
        enQueue(new Queue(null, command, target, item), delay);
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

    public void publishHesitationState(int hesNo){
    }


    public void publishOnEraseState(){
    }

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
