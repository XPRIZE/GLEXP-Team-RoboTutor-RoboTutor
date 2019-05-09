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

package cmu.xprize.comp_questions;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cmu.xprize.util.CPersonaObservable;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.comp_questions.QN_CONST.FILLER_SPACE;
import static cmu.xprize.comp_questions.QN_CONST.FTR_PLAY_GEN;
import static cmu.xprize.comp_questions.QN_CONST.GEN_QUESTION_CHANCE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_CLZSTATE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_LINESTATE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_PARASTATE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_PMSTATE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_QNSTATE;
import static cmu.xprize.comp_questions.QN_CONST.RTC_VAR_WORDSTATE;
import static cmu.xprize.comp_questions.QN_CONST.SHOW_CLOZE;
import static cmu.xprize.comp_questions.QN_CONST.SHOW_PICMATCH;
import static cmu.xprize.util.TCONST.FTR_USER_READ;
import static cmu.xprize.util.TCONST.FTR_USER_READING;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;


/**
 * This view manager provides student UX for the African Story Book format used
 * in the CMU XPrize submission
 */
public class CQn_ViewManagerASB implements ICQn_ViewManager, ILoadableObject  {

    private Context                 mContext;

    private ListenerBase            mListener;
    private IVManListener mOwner;
    private String                  mAsset;

    private CQn_Component mParent;
    private ImageView               mPageImage;
    private ImageView               mQuestionImage;
    private TextView                mPageText;

    private ImageButton             mPageFlip;
    private ImageButton             mSay;

    // ASB even odd page management

    private ViewGroup               mOddPage;
    private ViewGroup               mEvenPage;

    // uhq
    private int                     numWordsCurPage;
    private boolean                 replayCloze = false;
    private int                     clozeWordsCounter = 0;
    private String                  oldClozePageText;
    private TextView                curClozeTextView;
    private String                  clozeWordToPlay;
    private int                     picmatch_answer;
    private boolean                 picture_match_mode = false;
    private boolean                 cloze_page_mode = false;
    private boolean                 isClozePage = false;
    private ViewGroup               mPicturePage;
    private int                     numPicMatch; // OPEN_SOURCE how does this get set???
    private ImageView               mMatchImage1;
    private ImageView               mMatchImage3;
    private ImageView               mMatchImage2;
    private ImageView               mMatchImage4;
    private ViewGroup               mImageFrame1;
    private ViewGroup               mImageFrame2;
    private ViewGroup               mImageFrame3;
    private ViewGroup               mImageFrame4;
    private ViewGroup               mImageGrid;
    private boolean                 show_image_options = false;

    private ViewGroup               mQuestionPage;
    private int                     mQuestionIndex;
    private TextView                mWord1Text;
    private TextView                mWord2Text;
    private TextView                mWord3Text;
    private TextView                mWord4Text;
    private ViewGroup               mWordFrame1;
    private ViewGroup               mWordFrame2;
    private ViewGroup               mWordFrame3;
    private ViewGroup               mWordFrame4;

    private ClozeQuestion           clozeQuestion;
    private String                  clozeTarget;

    private int                     mOddIndex;
    private int                     mEvenIndex;
    private int                     mCurrViewIndex;

    // state for the current storyName - African Story Book

    private String                  mCurrHighlight = "";
    private int                     mCurrPage;
    private boolean                 mLastPage;
    private int                     mCurrPara;
    private int                     mCurrLine;
    private int                     mCurrWord;
    private int                     mHeardWord;                          // The expected location of mCurrWord in heardWords - see PLRT version of onUpdate below
    private int                     numTotalLines;
    private int                     mCurrLineInStory; // keeps track of which line in the story we're at

    private String                  speakButtonEnable = "DISABLE";
    private String                  speakButtonShow   = "HIDE";
    private String                  pageButtonEnable  = "DISABLE";
    private String                  pageButtonShow    = "HIDE";

    private int                     mPageCount;
    private int                     mParaCount;
    private int                     mLineCount;
    private int                     mWordCount;
    private int                     attemptNum = 1;
    private boolean                 storyBooting;

    private String[]                wordsToDisplay;                      // current sentence words to display - contain punctuation
    private String[]                wordsToSpeak;                        // current sentence words to hear
    private ArrayList<String>       wordsToListenFor;                    // current sentence words to build language model
    private String                  hearRead;
    private Boolean                 echo = false;

    private CASB_Narration[]        rawNarration;                        // The narration segmentation info for the active sentence
    private String                  rawSentence;                         // currently displayed sentence that need to be recognized
    private CASB_Seg                narrationSegment;
    private String[]                splitSegment;
    private int                     splitIndex = TCONST.INITSPLIT;
    private boolean                 endOfSentence = false;
    private ArrayList<String>       spokenWords;
    private int                     utteranceNdx;
    private int                     segmentNdx;
    private String                  page_prompt;

    private int                     numUtterance;
    private CASB_Narration          currUtterance;
    private CASB_Seg[]              segmentArray;
    private int                     numSegments;
    private int                     utterancePrev;
    private int                     segmentPrev;
    private int                     segmentCurr;

    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";
    private String                  futureSentencesFmtd    = "";
    private String                  futureSentences        = "";
    private boolean                 showQuestion           = false;
    private boolean                 showWords              = true;
    private boolean                 showFutureWords        = true;
    private boolean                 showFutureContent      = true;
    private boolean                 listenFutureContent    = false;
    private String                  assetLocation;

    private ArrayList<String>       wordsSpoken;
    private ArrayList<String>       futureSpoken;

    private boolean                 hasNonsensical         = false;
    private boolean                 hasUngrammatical       = false;
    private boolean                 hasPlausible           = false;



    // json loadable
    // ZZZ where the money gets loaded

    public String        license;
    public String        story_name;
    public String        authors;
    public String        illustrators;
    public String        language;
    public String        status;
    public String        copyright;
    public String        titleimage;

    public String        prompt;
    public String        parser;
    // ZZZ the money
    public CASB_data[]   data;

    public ClozeQuestion[] questions;

    public List<Integer>  clozeIndices;

    private String curGenericQuestion = "";

    static final String TAG = "CQn_ViewManagerASB";


    /**
     *
     * @param parent
     * @param listener
     */
    public CQn_ViewManagerASB(CQn_Component parent, ListenerBase listener) {
        Log.d(TAG, "CQn_ViewManagerASB: ");
        mParent = parent;
        mContext = mParent.getContext();

        mOddPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.qn_odd, null);
        mEvenPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.qn_even, null);
        mQuestionPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.qn_generic, null);
//        mPicturePage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.qn_picture, null);

        mOddPage.setVisibility(View.GONE);
        mEvenPage.setVisibility(View.GONE);
        mQuestionPage.setVisibility(View.GONE);
//        mPicturePage.setVisibility(View.GONE);

        mOddIndex  = mParent.addPage(mOddPage);
        mEvenIndex = mParent.addPage(mEvenPage);
        mQuestionIndex = mParent.addPage(mQuestionPage);
//        mPictureIndex = mParent.addPage(mPicturePage);
        mListener = listener;

        clozeIndices = new ArrayList<>();
    }


    /**
     *   The startup sequence for a new storyName is:
     *   Set - storyBooting flag to inhibit startListening so the script can complete whatever
     *   preparation is required before the listener starts.  Otherwise you get junk hypotheses.
     *
     *   Once the script has completed its introduction etc. it calls nextline to cause a line increment
     *   which resets storyBooting and enables the listener for the first sentence in the storyName.
     *
     * @param owner
     * @param assetPath
     */
    public void initStory(IVManListener owner, String assetPath, String location) {

        // FOR_HUI... these should not exist
        try {
            for (int i = 0; i < questions.length; i++) {
                if (questions[i].distractor != null) {
                    clozeIndices.add(i + 1); // TRACE_CLOZE (1) adding indices...
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Missing mcq.json... please add.");
        }

        mCurrLineInStory = 0;
        mOwner        = owner;
        mAsset        = assetPath; // ZZZ assetPath... TCONST.EXTERN
        storyBooting  = true;
        assetLocation = location;  // ZZZ assetLocation... contains storydata.json and images

        Log.d(TCONST.DEBUG_STORY_TAG, String.format("mAsset=%s -- assetLocation=%s", mAsset, assetLocation));

        if (mParent.testFeature(TCONST.FTR_USER_HIDE)) showWords = false;
        if (mParent.testFeature(TCONST.FTR_USER_REVEAL)) showFutureWords = showFutureContent = false;

        //uhq
        if (mParent.testFeature(TCONST.FTR_CLO)) {
            mParent.publishValue(RTC_VAR_CLZSTATE, TCONST.TRUE);
            mParent.publishValue(RTC_VAR_QNSTATE, TCONST.FALSE);
            mParent.publishValue(RTC_VAR_PMSTATE, TCONST.FALSE);
        }else if (mParent.testFeature(TCONST.FTR_GEN)) {
            mParent.publishValue(RTC_VAR_CLZSTATE, TCONST.FALSE);
            mParent.publishValue(RTC_VAR_QNSTATE, TCONST.TRUE);
            mParent.publishValue(RTC_VAR_PMSTATE, TCONST.FALSE);
        } else if (mParent.testFeature(TCONST.FTR_PIC)) {
            mParent.publishValue(RTC_VAR_CLZSTATE, TCONST.FALSE);
            mParent.publishValue(RTC_VAR_QNSTATE, TCONST.FALSE);
            mParent.publishValue(RTC_VAR_PMSTATE, TCONST.TRUE);
        }


        Log.d(TAG, "initStory: showWords = " + showWords + ", showFutureWords = " + showFutureWords + ", showFutureContent = " + showFutureContent);

        mParent.setFeature(TCONST.FTR_STORY_STARTING, TCONST.ADD_FEATURE);

        seekToPage(TCONST.ZERO);

        //TODO: CHECK
        mParent.animatePageFlip(true,mCurrViewIndex);
    }

    /**
     * PICTURE
     */
    @Override
    //Called by animator graph before flipping the page
    public void setPictureMatch(){
        if ((data.length - 1) - mCurrPage >= 3 && data[mCurrPage+1].image != null){
            picture_match_mode = true;
        } else {
            //UHQ: ensure that the last 2 pages are just regular pages, because no more pics to use
            picture_match_mode = false;
        }
    }

    /**
     * Initializes a cloze page.
     * Called by "CLOZE_PAGE_NODE" --> "SET_CLOZE_PAGE"
     */
    @Override
    public void setClozePage(){
        int paracount = data[mCurrPage+1].text.length;
        int numLines = 0;
        numWordsCurPage = 0;
        for(int i = 0; i < paracount; i++){
            int linecount = data[mCurrPage+1].text[i].length;
            numLines+=linecount;
            for (int j = 0; j < linecount;j++){
                int utteranceLen = data[mCurrPage+1].text[i][j].narration.length;
                for(int k = 0; k < utteranceLen; k++){
                    numWordsCurPage+=data[mCurrPage+1].text[i][j].narration[k].segmentation.length;
                }

            }
        }
        int sum=mCurrLineInStory+numLines;
        // TRACE_CLOZE where cloze_page_mode is set to true
        if(this.clozeIndices.contains(mCurrLineInStory+numLines)){
            clozeQuestion = questions[sum-1];
            clozeTarget = clozeQuestion.target;
            cloze_page_mode = true;
            updateClozeButtons();
        } else {
            cloze_page_mode = false;
        }
    }

    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    public void startStory() {
        // reset boot flag to inhibit future calls
        //
        if (storyBooting) {
            mParent.setFeature(TCONST.FTR_STORY_STARTING, TCONST.DEL_FEATURE);
            mParent.publishValue(TCONST.RTC_VAR_QUESTIONSTATE, TCONST.FTR_COMPLETE);
            // Narration Mode (i.e. USER_HEAR) always narrates the story otherwise we
            // start with USER_READ where the student reads aloud and if USER_ECHO
            // is in effect we then toggle between READ and HEAR for each sentence.
            //
            if (mParent.testFeature(TCONST.FTR_USER_HEAR) || mParent.testFeature(TCONST.FTR_USER_HIDE) || mParent.testFeature(TCONST.FTR_USER_PARROT)) {

                hearRead = TCONST.FTR_USER_HEAR;
            } else {
                hearRead = FTR_USER_READ;
                mParent.publishFeature(FTR_USER_READING);
            }

            storyBooting = false;
            speakOrListen();
        }
    }


    public void speakOrListen() {
        Log.d(TAG, "speakOrListen: ");
        if (hearRead.equals(TCONST.FTR_USER_HEAR)) {
            mParent.applyBehavior(TCONST.NARRATE_STORY);
        }
        if (hearRead.equals(FTR_USER_READ)) {
            startListening();
        }
    }


    @Override
    public void onDestroy() {
//        super.onDestroy();
    }


    /**
     * From the script writers perspective there is only one say button and one pageflip button
     * Since there are actually two of each - one on each page view we share the state between them and
     * enforce updates so they are kept in sync with user expectations.
     *
     * @param control
     * @param command
     */
    public void setButtonState(View control, String command) {
        try {
            switch (command) {
                case "ENABLE":
                    control.setEnabled(true);
                    break;
                case "DISABLE":
                    control.setEnabled(false);
                    break;
                case "SHOW":
                    control.setVisibility(View.VISIBLE);
                    break;
                case "HIDE":
                    control.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        catch(Exception e) {
            Log.d(TAG, "result:" + e);
        }
    }


    public void setSpeakButton(String command) {
        switch (command) {
            case "ENABLE":
                speakButtonEnable = command;
                break;
            case "DISABLE":
                speakButtonEnable = command;
                break;
            case "SHOW":
                speakButtonShow = command;
                break;
            case "HIDE":
                speakButtonShow = command;
                break;
        }
        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    public void setPageFlipButton(String command) {
        switch (command) {
            case "ENABLE":
                Log.i("ASB", "ENABLE Flip Button");
                pageButtonEnable = command;
                break;
            case "DISABLE":
                Log.i("ASB", "DISABLE Flip Button");
                pageButtonEnable = command;
                break;
            case "SHOW":
                pageButtonShow = command;
                break;
            case "HIDE":
                pageButtonShow = command;
                break;
        }
        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    private void updateButtons() {
        // Make the button states insensitive to the page - So the script does not have to
        // worry about timing of setting button states.

        setButtonState(mPageFlip, pageButtonEnable);
        setButtonState(mPageFlip, pageButtonShow);

        setButtonState(mSay, speakButtonEnable);
        setButtonState(mSay, speakButtonShow);

        try {
            mPageFlip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB: PAGEFLIP");

                    mParent.onButtonClick(TCONST.PAGEFLIP_BUTTON);
                }
            });

            mSay.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB:onButtonClick SPEAKBUTTON");

                    mParent.onButtonClick(TCONST.SPEAK_BUTTON);
                }
            });
        }
        catch(Exception e) {
            Log.e(TAG, "error:" + e);
        }

    }

    /**
     * define behavior of cloze buttons
     */
    private void updateClozeButtons(){
        Log.d(TAG, "updateClozeButtons: ");
        disableClozeButtons();
        if (isClozePage){
            mWord1Text.setOnTouchListener(new ClozeTouchListener(mWord1Text, mWordFrame1));
            mWord2Text.setOnTouchListener(new ClozeTouchListener(mWord2Text, mWordFrame2));
            mWord3Text.setOnTouchListener(new ClozeTouchListener(mWord3Text, mWordFrame3));
            mWord4Text.setOnTouchListener(new ClozeTouchListener(mWord4Text, mWordFrame4));
        }
    }


    /**
     * Reacts to touch of a cloze button
     */
    private class ClozeTouchListener implements OnTouchListener {

        TextView _wordTextView;
        ViewGroup _wordFrame;

        ClozeTouchListener(TextView wordTextView, ViewGroup wordFrame) {
            this._wordTextView = wordTextView;
            this._wordFrame = wordFrame;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mParent.updateViewColor(_wordFrame, Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mParent.updateViewColor(_wordFrame, Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    disableClozeButtons();
                    String[] options = new String[] {mWord1Text.getText().toString(), mWord2Text.getText().toString(), mWord3Text.getText().toString(), mWord4Text.getText().toString()};
                    if (_wordTextView.getText().toString().equals(clozeTarget)){
                        mParent.updateViewColor(_wordFrame, Color.GREEN);
                        mParent.retractFeature(TCONST.CLOZE_WRONG);
                        mParent.publishFeature(TCONST.CLOZE_CORRECT);
                        mParent.publishValue(SHOW_CLOZE, TCONST.FALSE);
                        mParent.logClozePerformance(true, clozeTarget, _wordTextView.getText().toString(),
                                options, mCurrPage);
                        isClozePage = false;
                        replayCloze = true;
                    }else{
                        mParent.updateViewColor(_wordFrame, Color.RED);
                        mParent.retractFeature(TCONST.CLOZE_CORRECT);
                        mParent.publishFeature(TCONST.CLOZE_WRONG);
                        mParent.logClozePerformance(false, clozeTarget, _wordTextView.getText().toString(),
                                options, mCurrPage);
                    }
                    mParent.nextNode();
                    break;
            }
            return true;
        }
    }


    /**
     * Repeats a string n times
     * @param s string to be repeated
     * @param n number of times to repeat
     * @return a new string consisting of s, repeated n times
     */
    public String repeat(String s, int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(s);
        }
        return builder.toString();
    }


    /**
     * Used in animating the presentation of the cloze choices.
     * Displays the cloze word in the blank in the cloze sentence.
     * Is called once for each of the four cloze words, and always called before publishClozeWord.
     */
    public void showClozeWordInBlank(){
        int wordlen = this.clozeWordToPlay.length();
        int spacelen = wordlen <= 12 ? (12 - wordlen)/2 : 1;
        //TODO: specify whitespace in html tags
        String sideSpace = repeat("&ensp;", spacelen);
        String newWord = "<u>"+ sideSpace + this.clozeWordToPlay + sideSpace+"</u>";
//        this.oldClozePageText = mPageText.getText().toString();
        this.oldClozePageText = this.oldClozePageText.replace("#AAAAAA", "#000000");
        this.oldClozePageText = this.oldClozePageText.replace("#00B600", "#000000");
        Log.d(TAG, "showClozeWordInBlank: oldClozePageText = "+this.oldClozePageText);
        String pageTextHTML = this.oldClozePageText.replace(FILLER_SPACE, newWord);
        mParent.updateTextviewHTML(mPageText, pageTextHTML);
    }

    public void hideClozeWordInBlank(){
        mParent.updateTextviewHTML(mPageText, this.oldClozePageText);
    }

    /**
     * Called by animator_graph as the GET_CLOZE_WORD command.
     * Publishes the current cloze word that is being displayed in the blank in the cloze sentence,
     * so that the animator graph can use it.
     * Converts word to uppercase before publishing because its the way the segmented word files in
     * cmu/xprize/literacy are apparently formatted.
     */
    public void publishClozeWord(){
        TextView[] textViews = {mWord1Text, mWord2Text, mWord3Text, mWord4Text};
        ViewGroup[] wordFrames = {mWordFrame1, mWordFrame2, mWordFrame3, mWordFrame4};

        if (this.clozeWordsCounter > 3) this.clozeWordsCounter = 3; // to prevent outOfBoundsException, just in case
        TextView updateTextView = textViews[this.clozeWordsCounter];
        ViewGroup updateWordFrame = wordFrames[this.clozeWordsCounter];

        this.clozeWordToPlay = updateTextView.getText().toString();
        this.curClozeTextView = updateTextView;
        mParent.updateVisibility(updateTextView, "SHOW");
        mParent.updateVisibility(updateWordFrame, "SHOW");

        Log.d(TAG, "publishClozeWord: clozeWordToPlay = "+this.clozeWordToPlay);
        this.clozeWordsCounter += 1;
        mParent.publishValue(TCONST.RTC_VAR_CLOZEWORD, this.clozeWordToPlay.toUpperCase());
    }

    public void highlightClozeWord(){
        Log.d(TAG, "highlightClozeWord: ");
        mParent.updateTextColor(this.curClozeTextView, Color.GREEN);
        mParent.updateTextSize(this.curClozeTextView, 40);

    }

    public void undoHighlightClozeWord(){
        Log.d(TAG, "undoHighlightClozeWord: ");
        mParent.updateTextColor(this.curClozeTextView, Color.BLACK);
        mParent.updateTextSize(this.curClozeTextView, 30);
        if (this.curClozeTextView == mWord4Text) {
            mParent.retractFeature(TCONST.CLZ_ANIM_INCOMPLETE);
            mParent.publishFeature(TCONST.CLZ_ANIM_COMPLETE);
            enableClozeButtons();
            this.clozeWordsCounter = 0;
        }else{
            mParent.publishFeature(TCONST.CLZ_ANIM_INCOMPLETE);
        }
        mParent.post(TCONST.NEXT_NODE, 2000);
    }

    public void playClozeSentence(){
        //segmentNdx = 0;
        //trackNarration(true);
        String filename = currUtterance.audio.toLowerCase();
        if (filename.endsWith(".wav") || filename.endsWith(".mp3")) {
            filename = filename.substring(0,filename.length()-4);
        }

        // Publish the current utterance within sentence
        //
        mParent.publishValue(TCONST.RTC_VAR_UTTERANCE,  filename);
        // NOTE: Due to inconsistencies in the segmentation data, you cannot depend on it
        // having precise timing information.  As a result the segment may timeout before the
        // audio has completed. To avoid this we use oncomplete in type_audio to push an
        // TRACK_SEGMENT back to this components queue.
        // Tell the script to speak the new uttereance
        //
        Log.d(TAG, "playClozeSentence: curutterance = "+currUtterance);
        mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);

        postDelayedTracker();

//
    }

    /**
     * Disables all the TextButtons in the current cloze page.
     */
    public void disableClozeButtons(){
        setButtonState(mWord1Text, "DISABLE");
        setButtonState(mWord2Text, "DISABLE");
        setButtonState(mWord3Text, "DISABLE");
        setButtonState(mWord4Text, "DISABLE");
    }

    public void hideClozeButtons(){
        setButtonState(mWord1Text, "HIDE");
        setButtonState(mWord2Text, "HIDE");
        setButtonState(mWord3Text, "HIDE");
        setButtonState(mWord4Text, "HIDE");
        mWordFrame1.setVisibility(View.INVISIBLE);
        mWordFrame2.setVisibility(View.INVISIBLE);
        mWordFrame3.setVisibility(View.INVISIBLE);
        mWordFrame4.setVisibility(View.INVISIBLE);
    }

    public void showClozeButtons(){
        setButtonState(mWord1Text, "SHOW");
        setButtonState(mWord2Text, "SHOW");
        setButtonState(mWord3Text, "SHOW");
        setButtonState(mWord4Text, "SHOW");
        mWordFrame1.setVisibility(View.VISIBLE);
        mWordFrame2.setVisibility(View.VISIBLE);
        mWordFrame3.setVisibility(View.VISIBLE);
        mWordFrame4.setVisibility(View.VISIBLE);
    }

    public void enableClozeButtons(){
        setButtonState(mWord1Text, "ENABLE");
        setButtonState(mWord2Text, "ENABLE");
        setButtonState(mWord3Text, "ENABLE");
        setButtonState(mWord4Text, "ENABLE");
    }

    public void resetClozeButtons(){
        mParent.updateViewColor(mWordFrame1, Color.WHITE);
        mParent.updateViewColor(mWordFrame2, Color.WHITE);
        mParent.updateViewColor(mWordFrame3, Color.WHITE);
        mParent.updateViewColor(mWordFrame4, Color.WHITE);
    }

    private void updateImageButtons(){
        Log.d(TAG, "updateImageButtons: ");
        disableImageButtons();
        if (show_image_options && picture_match_mode){
            Log.d(TAG, "updateImageButtons: picmatch_answer = "+picmatch_answer);
            if(this.numPicMatch>=2){
                mMatchImage1.setOnTouchListener(new PicMatchTouchListener(mMatchImage1, mImageFrame1, 0));
                mMatchImage2.setOnTouchListener(new PicMatchTouchListener(mMatchImage2, mImageFrame2, 1));
            }
            if (this.numPicMatch >=3){
                mMatchImage3.setOnTouchListener(new PicMatchTouchListener(mMatchImage3, mImageFrame3, 2));
            }
            if(this.numPicMatch==4){
                mMatchImage4.setOnTouchListener(new PicMatchTouchListener(mMatchImage4, mImageFrame4, 3));
            }
            showImageButtons();
        } else {
            hideImageButtons();
        }
    }

    /**
     * OnTouchListener for listening to PictureMatch touches.
     */
    private class PicMatchTouchListener implements OnTouchListener {

        ImageView _imageView;
        ViewGroup _frame;
        int _index;

        PicMatchTouchListener(ImageView imageView, ViewGroup frame, int index) {
            this._imageView = imageView;
            this._frame = frame;
            this._index = index;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mParent.updateViewAlpha(_imageView, (float) 0.5);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mParent.updateViewAlpha(_imageView, (float) 1.0);
                    break;
                case MotionEvent.ACTION_UP:
                    mParent.updateViewAlpha(_imageView, (float) 1.0);
                    disableImageButtons();
                    if (picmatch_answer == _index){
                        mParent.updateViewColor(_frame, Color.GREEN);
                        mParent.publishFeature(TCONST.PICMATCH_CORRECT);
                        mParent.retractFeature(TCONST.PICMATCH_WRONG);
                        mParent.logPicMatchPerformance(true, picmatch_answer, _index, mCurrPage);
                        picture_match_mode = false;
                        show_image_options = false;
                        hasQuestion();
                    }else{
                        mParent.updateViewColor(_frame, Color.RED);
                        mParent.retractFeature(TCONST.PICMATCH_CORRECT);
                        mParent.publishFeature(TCONST.PICMATCH_WRONG);
                        mParent.logPicMatchPerformance(false, picmatch_answer, _index, mCurrPage);
                    }
                    mParent.nextNode();
                    break;
            }
            return true;
        }
    }

    @Override
    public void resetImageButtons(){
        if(this.numPicMatch>=3){
            mParent.updateViewColor(mImageFrame1, Color.WHITE);
            mParent.updateViewColor(mImageFrame2, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame1, (float) 1.0);
            mParent.updateViewAlpha(mImageFrame2, (float) 1.0);
            mParent.updateViewColor(mImageFrame3, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame3, (float) 1.0);
        }
        if(this.numPicMatch==4){
            mParent.updateViewColor(mImageFrame1, Color.WHITE);
            mParent.updateViewColor(mImageFrame2, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame1, (float) 1.0);
            mParent.updateViewAlpha(mImageFrame2, (float) 1.0);
            mParent.updateViewColor(mImageFrame3, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame3, (float) 1.0);
            mParent.updateViewColor(mImageFrame4, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame4, (float) 1.0);
        }
        if(this.numPicMatch==2){
            mParent.updateViewColor(mImageFrame1, Color.WHITE);
            mParent.updateViewColor(mImageFrame2, Color.WHITE);
            mParent.updateViewAlpha(mImageFrame1, (float) 1.0);
            mParent.updateViewAlpha(mImageFrame2, (float) 1.0);
        }
    }

    @Override
    public void showImageButtons(){
        if(this.numPicMatch==3){
            setButtonState(mMatchImage1, "SHOW");
            setButtonState(mMatchImage2, "SHOW");
            mImageFrame1.setVisibility(View.VISIBLE);
            mImageFrame2.setVisibility(View.VISIBLE);
            setButtonState(mMatchImage3, "SHOW");
            mImageFrame3.setVisibility(View.VISIBLE);
            mImageGrid.setVisibility(View.VISIBLE);
        }
        if(this.numPicMatch==4) {
            setButtonState(mMatchImage1, "SHOW");
            setButtonState(mMatchImage2, "SHOW");
            mImageFrame1.setVisibility(View.VISIBLE);
            mImageFrame2.setVisibility(View.VISIBLE);
            setButtonState(mMatchImage3, "SHOW");
            mImageFrame3.setVisibility(View.VISIBLE);
            setButtonState(mMatchImage4, "SHOW");
            mImageFrame4.setVisibility(View.VISIBLE);
            mImageGrid.setVisibility(View.VISIBLE);
        }
        if(this.numPicMatch==2){
            setButtonState(mMatchImage1, "SHOW");
            setButtonState(mMatchImage2, "SHOW");
            mImageFrame1.setVisibility(View.VISIBLE);
            mImageFrame2.setVisibility(View.VISIBLE);
            mImageGrid.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void enableImageButtons(){
        if(this.numPicMatch==3){
            setButtonState(mMatchImage1, "ENABLE");
            setButtonState(mMatchImage2, "ENABLE");
            setButtonState(mMatchImage3, "ENABLE");
        }
        if(this.numPicMatch==4){
            setButtonState(mMatchImage1, "ENABLE");
            setButtonState(mMatchImage2, "ENABLE");
            setButtonState(mMatchImage3, "ENABLE");
            setButtonState(mMatchImage4, "ENABLE");

        }else{
            setButtonState(mMatchImage1, "ENABLE");
            setButtonState(mMatchImage2, "ENABLE");
        }
    }

    @Override
    public void hideImageButtons(){
        Log.d(TAG, "hideImageButtons: ");
        if(this.numPicMatch==3){
            setButtonState(mMatchImage1, "HIDE");
            setButtonState(mMatchImage2, "HIDE");
            mImageFrame1.setVisibility(View.INVISIBLE);
            mImageFrame2.setVisibility(View.INVISIBLE);
            setButtonState(mMatchImage3, "HIDE");
            mImageFrame3.setVisibility(View.INVISIBLE);
            mImageGrid.setVisibility(View.INVISIBLE);
        }
        if(this.numPicMatch==4){
            setButtonState(mMatchImage1, "HIDE");
            setButtonState(mMatchImage2, "HIDE");
            mImageFrame1.setVisibility(View.INVISIBLE);
            mImageFrame2.setVisibility(View.INVISIBLE);
            setButtonState(mMatchImage3, "HIDE");
            mImageFrame3.setVisibility(View.INVISIBLE);
            setButtonState(mMatchImage4, "HIDE");
            mImageFrame4.setVisibility(View.INVISIBLE);
            mImageGrid.setVisibility(View.INVISIBLE);
        }
        if(this.numPicMatch==2){
            setButtonState(mMatchImage1, "HIDE");
            setButtonState(mMatchImage2, "HIDE");
            mImageFrame1.setVisibility(View.INVISIBLE);
            mImageFrame2.setVisibility(View.INVISIBLE);
            mImageGrid.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void disableImageButtons(){
        Log.d(TAG, "disableImageButtons: ");
        if(this.numPicMatch==3){
            setButtonState(mMatchImage1, "DISABLE");
            setButtonState(mMatchImage2, "DISABLE");
            setButtonState(mMatchImage3, "DISABLE");
        }
        if(this.numPicMatch==4){
            setButtonState(mMatchImage1, "DISABLE");
            setButtonState(mMatchImage2, "DISABLE");
            setButtonState(mMatchImage3, "DISABLE");
            setButtonState(mMatchImage4, "DISABLE");
        }
        if(this.numPicMatch==2){
            setButtonState(mMatchImage1, "DISABLE");
            setButtonState(mMatchImage2, "DISABLE");
        }
        Log.d(TAG, "disableImageButtons: !!!");
    }

    /**
     *  This configures the target display components to be populated with data.
     *
     *  mPageImage - mPageText
     *
     */
    public void flipPage() {
        // OPEN_SOURCE (1)... this does not account for pages without images
        if (mPageCount-mCurrPage <= 3) { // PIC_CHOICE this gave me numPicMatch = 4 when it was supposed to be 2 or 3
            // Last 4 images; don't randomize
            this.numPicMatch = (mPageCount-mCurrPage); // OPEN_SOURCE is this the assignment?
        } else {
            this.numPicMatch = getRandomNumberInRange(2, 4);  // OPEN_SOURCE is this the assignment?
        }
        Log.d(TAG, "flipPage: asdfad "+this.numPicMatch);

        if (mCurrPage % 2 == 0) {
            if (mCurrPage > 0) mPageText.setText(" ");
            mCurrViewIndex = mOddIndex;
            Log.d(TAG, "flipPage: asdfad moddpage");
            mPageImage = mOddPage.findViewById(R.id.SpageImage);
            mPageText  = mOddPage.findViewById(R.id.SstoryText);
            mPageFlip = mOddPage.findViewById(R.id.SpageFlip);
            mSay      = mOddPage.findViewById(R.id.Sspeak);
            setPicMatchView(mOddPage);
            setClozeView(mOddPage);
        } else {
            mCurrViewIndex = mEvenIndex;
            Log.d(TAG, "flipPage: asdfad mevenpage");
            mPageImage = mEvenPage.findViewById(R.id.SpageImage);
            mPageText = mEvenPage.findViewById(R.id.SstoryText);
            mPageFlip = mEvenPage.findViewById(R.id.SpageFlip);
            mSay = mEvenPage.findViewById(R.id.Sspeak);
            setPicMatchView(mEvenPage);
            setClozeView(mEvenPage);
        }
        // Ensure the buttons reflect the current states
        updateButtons();
        if (cloze_page_mode){
            updateClozeButtons();
        } else {
            hideClozeButtons();
            disableClozeButtons();
        }
        if(picture_match_mode){
            if (mCurrPage % 2 == 0){
                mPageText = mOddPage.findViewById(R.id.SstoryTextPicMatch);
            } else {
                mPageText = mEvenPage.findViewById(R.id.SstoryTextPicMatch);
            }
            updateImageButtons();
        } else {
            hideImageButtons();
            disableImageButtons();
        }

    }

    private void setClozeView(ViewGroup vgroup){
        mWord1Text = vgroup.findViewById(R.id.Sword1);
        mWord2Text = vgroup.findViewById(R.id.Sword2);
        mWord3Text = vgroup.findViewById(R.id.Sword3);
        mWord4Text = vgroup.findViewById(R.id.Sword4);
        mWordFrame1 = vgroup.findViewById(R.id.SwordFrame1);
        mWordFrame2 = vgroup.findViewById(R.id.SwordFrame2);
        mWordFrame3 = vgroup.findViewById(R.id.SwordFrame3);
        mWordFrame4 = vgroup.findViewById(R.id.SwordFrame4);
        mWordFrame1.setVisibility(View.INVISIBLE);
        mWordFrame2.setVisibility(View.INVISIBLE);
        mWordFrame3.setVisibility(View.INVISIBLE);
        mWordFrame4.setVisibility(View.INVISIBLE);
    }

    private void setPicMatchView(ViewGroup vgroup){
        Log.d(TAG, "setPicMatchView: aslkdjfalksdf");
        if (this.numPicMatch==3){
            mMatchImage1 = vgroup.findViewById(R.id.SpageImage3_1);
            mMatchImage2 = vgroup.findViewById(R.id.SpageImage3_2);
            mMatchImage3 = vgroup.findViewById(R.id.SpageImage3_3);
            mImageFrame1 = vgroup.findViewById(R.id.SpageFrame3_1);
            mImageFrame2 = vgroup.findViewById(R.id.SpageFrame3_2);
            mImageFrame3 = vgroup.findViewById(R.id.SpageFrame3_3);
            mImageGrid = vgroup.findViewById(R.id.ImageGrid3);
        }
        if (this.numPicMatch==4) {
            mMatchImage1 = vgroup.findViewById(R.id.SpageImage4_1);
            mMatchImage2 = vgroup.findViewById(R.id.SpageImage4_2);
            mMatchImage3 = vgroup.findViewById(R.id.SpageImage4_3);
            mMatchImage4 = vgroup.findViewById(R.id.SpageImage4_4);
            mImageFrame1 = vgroup.findViewById(R.id.SpageFrame4_1);
            mImageFrame2 = vgroup.findViewById(R.id.SpageFrame4_2);
            mImageFrame3 = vgroup.findViewById(R.id.SpageFrame4_3);
            mImageFrame4 = vgroup.findViewById(R.id.SpageFrame4_4);
            mImageGrid = vgroup.findViewById(R.id.ImageGrid4);
        }
        if(this.numPicMatch==2) {
            mMatchImage1 = vgroup.findViewById(R.id.SpageImage2_1);
            mMatchImage2 = vgroup.findViewById(R.id.SpageImage2_2);
            mImageFrame1 = vgroup.findViewById(R.id.SpageFrame2_1);
            mImageFrame2 = vgroup.findViewById(R.id.SpageFrame2_2);
            mImageGrid = vgroup.findViewById(R.id.ImageGrid2);
        }
        Log.d(TAG, "setPicMatchView: mImageGrid = null:"+(mImageGrid == null));
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt(max - min + 1) + min;
    }

    private void configurePageImage() {
        if (picture_match_mode){
            // Do nothing because updateImageButtons will handle it
        } else {
            InputStream in;
            try {
                if (assetLocation.equals(TCONST.EXTERN)) {
                    Log.d(TCONST.DEBUG_STORY_TAG, "loading image " + mAsset + data[mCurrPage].image);
                    in = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image

                } else if (assetLocation.equals(TCONST.EXTERN_SHARED)) {
                    Log.d(TCONST.DEBUG_STORY_TAG, "loading shared image " + mAsset + data[mCurrPage].image);
                    in = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image
                } else {
                    Log.d(TCONST.DEBUG_STORY_TAG, "loading image from asset" + mAsset + data[mCurrPage].image);
                    in = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
                }
                mPageImage.setImageBitmap(BitmapFactory.decodeStream(in));
                mPageImage.bringToFront();
//                mImageFrame1.setVisibility(View.INVISIBLE);
//                mImageFrame2.setVisibility(View.INVISIBLE);
//                mImageFrame3.setVisibility(View.INVISIBLE);
//                mMatchImage1.setImageBitmap(null);
//                mMatchImage3.setImageBitmap(null);
//                mMatchImage2.setImageBitmap(null);

            } catch (IOException e) {
                mPageImage.setImageBitmap(null);
                e.printStackTrace();
            }
        }

    }


    private String[] splitWordOnChar(String[] wordArray, String splitChar) {

        ArrayList<String> wordList = new ArrayList<>();

        for (String word : wordArray) {

            String[] wordSplit = word.split(splitChar);

            if (wordSplit.length > 1) {

                for (int i1 = 0 ; i1 < wordSplit.length-1 ; i1++) {
                    wordList.add(wordSplit[i1] + splitChar);
                }
                wordList.add(wordSplit[wordSplit.length-1]);
            } else {
                wordList.add(wordSplit[0]);
            }
        }

        return wordList.toArray(new String[wordList.size()]);
    }


    private String[] splitRawSentence(String rawSentence) {
        String  sentenceWords[];

        sentenceWords = rawSentence.trim().split("\\s+");

        sentenceWords = stripLeadingTrailing(sentenceWords, "'");
        sentenceWords = splitWordOnChar(sentenceWords, "-");
        sentenceWords = splitWordOnChar(sentenceWords, "'");

        return sentenceWords;
    }


    /**
     * This cleans a raw sentence from the ASB.  This is very idiosyncratic to the ASB content.
     * ASB contains some apostrophes used as single quotes that otherwise confuse the layout
     *
     * We also need to have true apostrophes and hyphenated words split to maintain alignment with
     * the listener.  i.e the displayed words and spoken word arrays should be kept in alignment.
     *
     * @param rawSentence
     * @return
     */
    private String processRawSentence(String rawSentence) {

        String[]      sentenceWords;
        StringBuilder sentence = new StringBuilder();

        sentenceWords = splitRawSentence(rawSentence);

        for (int i1 = 0 ; i1 < sentenceWords.length ; i1++) {

            if (sentenceWords[i1].endsWith("'") || sentenceWords[i1].endsWith("-")) {
                sentence.append(sentenceWords[i1]);
            } else {
                sentence.append(sentenceWords[i1] + ((i1 < sentenceWords.length-1)? TCONST.WORD_SPACE: TCONST.NO_SPACE));
            }
        }

        return sentence.toString();
    }


    private String stripLeadingTrailing(String sentence, String stripChar) {
        if (sentence.startsWith(stripChar)) {
            sentence = sentence.substring(1);
        }
        if (sentence.endsWith(stripChar)) {
            sentence = sentence.substring(0, sentence.length()-1);
        }
        return sentence;
    }


    private String[] stripLeadingTrailing(String[] wordArray, String stripChar) {

        ArrayList<String> wordList = new ArrayList<>();

        for (String word : wordArray) {

            if (word.startsWith(stripChar)) {
                word = word.substring(1);
            }
            if (word.endsWith(stripChar)) {
                word = word.substring(0, word.length()-1);
            }

            wordList.add(word);
        }

        return wordList.toArray(new String[wordList.size()]);
    }


    /**
     * Reconfigure for a specific page / paragraph / line (seeks to)
     *
     * @param currPage
     * @param currPara
     * @param currLine
     */
    private void seekToStoryPosition(int currPage, int currPara, int currLine, int currWord) {

        String otherWordsToSpeak[];

        completedSentencesFmtd = "";
        completedSentences     = "";
        futureSentencesFmtd    = "";
        futureSentences        = "";
        wordsSpoken            = new ArrayList<>();
        futureSpoken           = new ArrayList<>();

        Log.d(TAG, "seekToStoryPosition: Page: " + currPage + " - Paragraph: " + currPara + " - line: " + currLine + " - word: " + currWord);

        // Optimization - Skip If seeking to the very first line
        //
        // Otherwise create 2 things:
        //
        // 1. A visually formatted representation of the words already spoken
        // 2. A list of words already spoken - for use in the Sphinx language model
        //
        if (currPara > 0 || currLine > 0) {

            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = 0 ; paraIndex < currPara ; paraIndex++) {

                for (CASB_Content rawContent : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = splitSentence(rawContent.sentence);

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        wordsSpoken.add(word);

                    completedSentences += processRawSentence(rawContent.sentence) + TCONST.SENTENCE_SPACE;
                }
                if (paraIndex < currPara)
                    completedSentences += "<br><br>";
            }

            // Then generate all completed sentences from the current paragraph
            //
            for (int lineIndex = 0 ; lineIndex <  currLine ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = splitSentence(rawSentence);

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    wordsSpoken.add(word);

                completedSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }

            // Note that we add a space after the sentence.
            //
            completedSentencesFmtd = "<font color='#AAAAAA'>";
            completedSentencesFmtd += completedSentences;
            completedSentencesFmtd += "</font>";
        }


        // Generate the active line of text - target sentence
        // Reset the highlight
        mCurrHighlight = TCONST.EMPTY;

        mCurrPage = currPage;
        mCurrPara = currPara;
        mCurrLine = currLine;

        mPageCount = data.length;
        mParaCount = data[currPage].text.length;
        mLineCount = data[currPage].text[currPara].length;
        rawNarration = data[currPage].text[currPara][currLine].narration;
        rawSentence  = data[currPage].text[currPara][currLine].sentence;
        if (data[currPage].prompt != null) page_prompt = data[currPage].prompt;

        // Words that are used to build the display text - include punctuation etc.
        //
        // NOTE: wordsToSpeak is used in generating the active ASR listening model
        // so it must reflect the current sentence without punctuation!
        //
        // To keep indices into wordsToSpeak in sync with wordsToDisplay we break the words to
        // display if they contain apostrophes or hyphens into sub "words" - e.g. "thing's" -> "thing" "'s"
        // these are reconstructed by the highlight logic without adding spaces which it otherwise inserts
        // automatically.
        //
        //
        wordsToDisplay = splitRawSentence(rawSentence);


        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        //
        wordsToSpeak = splitSentence(rawSentence);

        mCurrWord  = currWord;
        mWordCount = wordsToSpeak.length;

        // If we are showing future content - i.e. we want the entire page to be visible but
        // only the "current" line highlighted.
        // Note we need ...Count vars initialized here
        //
        // Create 2 things:
        //
        // 1. A visually formatted representation of the words not yet spoken
        // 2. A list of future words to be spoken - for use in the Sphinx language model
        //
        if (showFutureContent) {

            // Generate all remaining sentences in the current paragraph
            //
            // Then generate all future sentences from the current paragraph
            //
            for (int lineIndex = currLine+1 ; lineIndex <  mLineCount ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = splitSentence(rawSentence);

                // TRACE_CLOZE this differs from regular ViewManager code
                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    futureSpoken.add(word);

                futureSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }

            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = currPara+1 ; paraIndex < mParaCount ; paraIndex++) {

                // Add the paragraph break if not at the end
                //
                futureSentences += "<br><br>";

                // TRACE_CLOZE this differs from regular ViewManager code
                for (CASB_Content rawSentence : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = splitSentence(rawSentence.sentence);

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        futureSpoken.add(word);

                    futureSentences += processRawSentence(rawSentence.sentence) + TCONST.SENTENCE_SPACE;
                }
            }

            // TODO : parameterize the color

            futureSentencesFmtd = "<font color='#AAAAAA'>";
            futureSentencesFmtd += futureSentences;
            futureSentencesFmtd += "</font>";
        }


        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();

        // Update the sentence display
        //
        UpdateDisplay();

        // Once past the storyName initialization stage - Listen for the target word -
        //
        if (!storyBooting)
            speakOrListen();

    }

    private String[] splitSentence(String sentence) {
        return sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
    }

    private void seekToClozeStoryPosition(int currPage, int currPara, int currLine, int currWord) {
        String otherWordsToSpeak[];

        completedSentencesFmtd = "";
        completedSentences     = "";
        futureSentencesFmtd    = "";
        futureSentences        = "";
        wordsSpoken            = new ArrayList<>();
        futureSpoken           = new ArrayList<>();

        // Optimization - Skip If seeking to the very first line
        //
        // Otherwise create 2 things:
        //
        // 1. A visually formatted representation of the words already spoken
        // 2. A list of words already spoken - for use in the Sphinx language model
        //
        if (currPara > 0 || currLine > 0) {

            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = 0 ; paraIndex < currPara ; paraIndex++) {

                for (CASB_Content rawContent : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = splitSentence(rawContent.sentence);

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        wordsSpoken.add(word);

                    completedSentences += processRawSentence(rawContent.sentence) + TCONST.SENTENCE_SPACE;
                }
                if (paraIndex < currPara)
                    completedSentences += "<br><br>";
            }

            // Then generate all completed sentences from the current paragraph
            //
            for (int lineIndex = 0 ; lineIndex <  currLine ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = splitSentence(rawSentence);

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    wordsSpoken.add(word);

                completedSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }

            // Note that we add a space after the sentence.
            //
            completedSentencesFmtd = "<font color='#AAAAAA'>";
            completedSentencesFmtd += completedSentences;
            completedSentencesFmtd += "</font>";

        }


        // Generate the active line of text - target sentence
        // Reset the highlight
        mCurrHighlight = TCONST.EMPTY;

        mCurrPage = currPage;
        mCurrPara = currPara;
        mCurrLine = currLine;

        mPageCount = data.length;
        mParaCount = data[currPage].text.length;
        mLineCount = data[currPage].text[currPara].length;

        rawNarration = data[currPage].text[currPara][currLine].narration;
        rawSentence  = data[currPage].text[currPara][currLine].sentence;
        if (data[currPage].prompt != null) page_prompt = data[currPage].prompt;

        // Words that are used to build the display text - include punctuation etc.
        //
        // NOTE: wordsToSpeak is used in generating the active ASR listening model
        // so it must reflect the current sentence without punctuation!
        //
        // To keep indices into wordsToSpeak in sync with wordsToDisplay we break the words to
        // display if they contain apostrophes or hyphens into sub "words" - e.g. "thing's" -> "thing" "'s"
        // these are reconstructed by the highlight logic without adding spaces which it otherwise inserts
        // automatically.
        //

        // TRACE_CLOZE
        // If it's the last paragraph of the page, and the last line of the paragraph, we can
        // replace the last word with a _____.
        if (currPara == mParaCount - 1 && currLine == mLineCount - 1){

            if (!rawSentence.contains(FILLER_SPACE)) {
                rawSentence = insertBlankSpaceAtEndOfSentence(rawSentence);
            }
            wordsToDisplay = splitRawSentence(rawSentence);
            wordsToSpeak = splitSentence(rawSentence);

        } else {
            wordsToDisplay = splitRawSentence(rawSentence);
            wordsToSpeak = splitSentence(rawSentence);
        }



        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        //

        mCurrWord  = currWord;

        // Minus 1 to not say the last word on the page
        mWordCount = wordsToSpeak.length;

        // If we are showing future content - i.e. we want the entire page to be visible but
        // only the "current" line highlighted.
        // Note we need ...Count vars initialized here
        //
        // Create 2 things:
        //
        // 1. A visually formatted representation of the words not yet spoken
        // 2. A list of future words to be spoken - for use in the Sphinx language model
        //
        if (showFutureContent) {

            // Generate all remaining sentences in the current paragraph
            //
            // Then generate all future sentences from the current paragraph
            //
            for (int lineIndex = currLine+1 ; lineIndex <  mLineCount ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = splitSentence(rawSentence);

                // not sure what this does
                if (lineIndex == mLineCount-1) {
                    String[] wordsToSpeakTemp = new String[otherWordsToSpeak.length - 1];
                    for (int i = 0; i < otherWordsToSpeak.length - 2; i++) {
                        wordsToSpeakTemp[i] = otherWordsToSpeak[i];
                    }
                    otherWordsToSpeak = wordsToSpeakTemp;
                }
                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    futureSpoken.add(word);

                futureSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }
            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = currPara+1 ; paraIndex < mParaCount ; paraIndex++) {

                // Add the paragraph break if not at the end
                //
                futureSentences += "<br><br>";

                // TRACE_CLOZE this differs from regular ViewManager code
                for (CASB_Content rawSentence : data[currPage].text[paraIndex]) {
                    otherWordsToSpeak = splitSentence(rawSentence.sentence);

                    int lastParaLastSentenceCount = data[currPage].text[mParaCount-1].length - 1;
                    String lastSentence = data[currPage].text[mParaCount-1][lastParaLastSentenceCount].sentence;

                    // don't speak last word
                    if (rawSentence.sentence == lastSentence) {
                        String[] wordsToSpeakTemp = new String[otherWordsToSpeak.length - 1];
                        for (int i = 0; i < otherWordsToSpeak.length - 2; i++) {
                            wordsToSpeakTemp[i] = otherWordsToSpeak[i];
                        }
                        otherWordsToSpeak = wordsToSpeakTemp;

                        // here is where the blank is replaced
                        if (!rawSentence.sentence.contains(" " + FILLER_SPACE) &&
                                rawSentence.sentence == lastSentence){
                            insertBlankSpaceAtEndOfSentence(rawSentence);
                        }
                    }

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        futureSpoken.add(word);

                    futureSentences += processRawSentence(rawSentence.sentence) + TCONST.SENTENCE_SPACE;
                }
            }
            // TODO : parameterize the color

            futureSentencesFmtd = "<font color='#AAAAAA'>";
            futureSentencesFmtd += futureSentences;
            futureSentencesFmtd += "</font>";

        }


        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();

        // Update the sentence display
        //
        UpdateDisplay();

        // Once past the storyName initialization stage - Listen for the target word -
        //
        if (!storyBooting)
            speakOrListen();

    }

    /**
     * Replaces the last word in the raw sentence with a blank
     * @param rawSentence
     */
    private void insertBlankSpaceAtEndOfSentence(CASB_Content rawSentence) {
        int len = rawSentence.sentence.length();
        String punctuation = rawSentence.sentence.substring(len-1, len);
        rawSentence.sentence = rawSentence.sentence.substring(0, rawSentence.sentence.lastIndexOf(" "));
        String lastWord = rawSentence.sentence.substring(rawSentence.sentence.lastIndexOf(" ") + 1);
        rawSentence.sentence = rawSentence.sentence + " " + FILLER_SPACE + punctuation;
    }
    
    private String insertBlankSpaceAtEndOfSentence(String sentence) {
        int len = sentence.length();
        String punctuation = sentence.substring(len-1, len);
        sentence = sentence.substring(0, sentence.lastIndexOf(" "));
        String lastWord = sentence.substring(sentence.lastIndexOf(" ") + 1);
        sentence = sentence + " " + FILLER_SPACE + punctuation;
        return sentence;
    }


    private void initSegmentation(int _uttNdx, int _segNdx) {

        utteranceNdx  = _uttNdx;
        numUtterance  = rawNarration.length;
        currUtterance = rawNarration[utteranceNdx];
        segmentArray  = rawNarration[utteranceNdx].segmentation;

        segmentNdx    = _segNdx;
        numSegments   = segmentArray.length;
        utterancePrev = utteranceNdx == 0 ? 0 : rawNarration[utteranceNdx - 1].until;
        segmentPrev   = utterancePrev;

        // Clean the extension off the end - could be either wav/mp3
        //
        String filename = currUtterance.audio.toLowerCase();
        if (filename.endsWith(".wav") || filename.endsWith(".mp3")) {
            filename = filename.substring(0,filename.length()-4);
        }

        // Publish the current utterance within sentence
        //
        mParent.publishValue(TCONST.RTC_VAR_UTTERANCE,  filename);
        // NOTE: Due to inconsistencies in the segmentation data, you cannot depend on it
        // having precise timing information.  As a result the segment may timeout before the
        // audio has completed. To avoid this we use oncomplete in type_audio to push an
        // TRACK_SEGMENT back to this components queue.
        // Tell the script to speak the new uttereance
        //
        Log.d(TAG, "initSegmentation: curutterance = "+currUtterance);
        mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);
    }


    private void trackNarration(boolean start) {
        Log.d("PLS", "trackNarration: "+mCurrLineInStory);
        if (start) {

            mHeardWord    = 0;
            splitIndex    = TCONST.INITSPLIT;
            endOfSentence = false;
            initSegmentation(0, 0);
            spokenWords   = new ArrayList<String>();

            // Tell the script to speak the new utterance
            //
            narrationSegment = rawNarration[utteranceNdx].segmentation[segmentNdx];

            mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);

            postDelayedTracker();
        } else {

            // NOTE: The narration mode uses the ASR logic to simplify operation.  In doing this
            /// it uses the wordsToSpeak array to progressively highlight the on screen text based
            /// on the timing found in the segmentation data.
            //
            // Special processing to account for apostrophes and hyphenated words
            // Note the system listens for e.g. "WON'T" as [WON] [T] two words so if we provide "won't" then it "won't" match :)
            // and the narration will freeze
            // This is a kludge to account for the fact that segmentation data does not split words with
            // hyphens or apostrophes into separate "words" the way the wordstospeak does.
            // Without this the narration will get out of sync
            //
            if (splitIndex == TCONST.INITSPLIT) {
                splitSegment = narrationSegment.word.toUpperCase().split("[\\-']");

                splitIndex = 0;
                spokenWords.add(splitSegment[splitIndex++]);

            } else if (splitIndex < splitSegment.length){

                spokenWords.add(splitSegment[splitIndex++]);
            } else {

                Log.d(TAG, "HERE");
            }

            // Update the display
            //
            onUpdate(spokenWords.toArray(new String[spokenWords.size()]));

            // If the segment word is complete continue to the next segment - note that this is
            // generally the case.  Words are not usually split by pubctuation
            //
            if (splitIndex >= splitSegment.length) {

                splitIndex = TCONST.INITSPLIT;

                // sentences are built from an array of utterances which are build from an array
                // of segments (i.e. timed words)
                //
                // Note the last segment is not timed.  It is driven by the TRACK_COMPLETE event
                // from the audio mp3 playing.  This is required as the segmentation data is not
                // sufficiently accurate to ensure we don't interrupt a playing utterance.
                if (mCurrPara == mParaCount-1 && mCurrLine == mLineCount-1 && segmentNdx == numSegments-2){
                    segmentNdx++;
                } else {
                    segmentNdx++;
                }

                if (segmentNdx >= numSegments) {
                    // If we haven't consumed all the utterances (i.e "narrations") in the
                    // sentence prep the next
                    //
                    // NOTE: Prep the state and wait for the TRACK_COMPLETE event to invoke
                    // trackSegment to continue or terminate
                    //
                    utteranceNdx++;
                    if (utteranceNdx < numUtterance) {

                        initSegmentation(utteranceNdx, 0);

                    } else {
                        mCurrLineInStory+=1;
                        endOfSentence = true;
                    }
                }
                // All the segments except the last one are timed based on the segmentation data.
                // i.e. the audio plays and this highlights words based on prerecorded durations.
                //
                else {
                    postDelayedTracker();
                }
            }
            // If the segment word is split due to apostrophes or hyphens then consume them
            // before continuing to the next segment.
            //
            else {
                mParent.post(TCONST.TRACK_NARRATION, 0);
            }
        }
    }


    private void postDelayedTracker() {
        narrationSegment = rawNarration[utteranceNdx].segmentation[segmentNdx];
        segmentCurr = utterancePrev + narrationSegment.end;
        if (mCurrPage>0 && numWordsCurPage==2 && cloze_page_mode && !replayCloze) segmentCurr += 10;
        Log.d("ULANISTOPAUDIO", "postDelayedTracker: "+narrationSegment.word+" "+segmentCurr+" "+segmentPrev);

        if (mCurrPage>0 && numWordsCurPage==2 && cloze_page_mode && !replayCloze){
            Log.d("ULANISTOPAUDIO", "postDelayedTracker: stop_audio "+narrationSegment.word+" "+narrationSegment.start+" "+narrationSegment.end);
            mParent.post(TCONST.STOP_AUDIO, new Long((segmentCurr - segmentPrev) * 10));
        }else if (mCurrPage>0 && numWordsCurPage==1 && cloze_page_mode && !replayCloze){
            mParent.post(TCONST.NEXT_NODE, new Long((segmentCurr - segmentPrev) * 10));
        }
        mParent.post(TCONST.TRACK_NARRATION, new Long((segmentCurr - segmentPrev) * 10));
        segmentPrev = segmentCurr;
    }


    private void trackSegment() {

        if (!endOfSentence) {
            Log.d("STOPAUDIOO", "trackSegment: "+rawNarration[utteranceNdx].segmentation[segmentNdx]);
            // Tell the script to speak the new utterance
            //
            mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);

            postDelayedTracker();
        } else {
            mParent.applyBehavior(TCONST.UTTERANCE_COMPLETE_EVENT);
        }
    }


    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch (command) {

            case TCONST.START_NARRATION:
                trackNarration(true);
                break;

            case TCONST.NEXT_WORD:
                generateVirtualASRWord();
                break;

            case TCONST.NEXT_PAGE:
                replayCloze = false; //TODO: figure out the placement of this flag
                nextPage();
                break;

            case TCONST.NEXT_SCENE:
                mParent.nextScene();
                break;

            case TCONST.TRACK_NARRATION:
                trackNarration(false);
                break;

            case TCONST.STOP_AUDIO:
                mParent.stopAudio();
                break;

            case TCONST.TRACK_SEGMENT:
                trackSegment();
                break;

            case TCONST.NEXT_NODE:
                mParent.nextNode();
                break;

            case TCONST.CLZ_ANIM_INCOMPLETE:
                mParent.retractFeature(TCONST.CLZ_ANIM_INCOMPLETE);

            case TCONST.REMOVE_CLOZE_FROM_BLANK:
                mParent.updateTextviewHTML(mPageText, oldClozePageText);

            case TCONST.SPEAK_EVENT:

            case TCONST.UTTERANCE_COMPLETE_EVENT:
                mParent.applyBehavior(command);
                break;

        }
    }

    /**
     * Push the state out to the tutor domain.
     *
     */
    private void publishStateValues() {
//        Log.d(TAG, "publishStateValues: mCurrWord = " + mCurrWord + ", mWordCount = " + mWordCount);

        String cummulativeState = TCONST.RTC_CLEAR;

        // ensure echo state has a valid value.
        //
        mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.FALSE);
        mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.FALSE);

        if (prompt != null) {
            mParent.publishValue(TCONST.RTC_VAR_PROMPT, prompt);
            mParent.publishFeature((TCONST.FTR_PROMPT));
        }

        if (page_prompt != null) {
            mParent.publishValue(TCONST.RTC_VAR_PAGE_PROMPT, page_prompt);
            mParent.publishFeature((TCONST.FTR_PAGE_PROMPT));
        }

        // Set the scriptable flag indicating the current state.
        //
        if (mCurrWord >= mWordCount) {
            Log.d(TAG, "publishStateValues: line finished");
            // In echo mode - After line has been echoed we switch to Read mode and
            // read the next sentence.
            //
            if (mParent.testFeature(TCONST.FTR_USER_ECHO) || mParent.testFeature(TCONST.FTR_USER_REVEAL) || mParent.testFeature(TCONST.FTR_USER_PARROT)) {

                // Read Mode - When user finishes reading switch to Narrate mode and
                // narrate the same sentence - i.e. echo
                //
                if (hearRead.equals(FTR_USER_READ)) {

                    if (!mParent.testFeature(TCONST.FTR_USER_PARROT)) mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.TRUE);

                    hearRead = TCONST.FTR_USER_HEAR;
                    mParent.retractFeature(FTR_USER_READING);

                    //Log.d("ISREADING", "NO");

                    cummulativeState = TCONST.RTC_LINECOMPLETE;
                    mParent.publishValue(RTC_VAR_WORDSTATE, TCONST.LAST);

                    mListener.setPauseListener(true);
                }
                // Narrate mode - switch back to READ and set line complete flags
                //
                else {
                    hearRead = FTR_USER_READ;
                    mParent.publishFeature(FTR_USER_READING);

                    if (mParent.testFeature(TCONST.FTR_USER_PARROT)) mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.TRUE);

                    //Log.d("ISREADING", "YES");

                    cummulativeState = TCONST.RTC_LINECOMPLETE;
                    mParent.publishValue(RTC_VAR_WORDSTATE, TCONST.LAST);
                }
            } else {
                cummulativeState = TCONST.RTC_LINECOMPLETE;
                mParent.publishValue(RTC_VAR_WORDSTATE, TCONST.LAST);
            }
        } else
            mParent.publishValue(RTC_VAR_WORDSTATE, TCONST.NOT_LAST);

        if (mCurrLine >= mLineCount-1) {
            cummulativeState = TCONST.RTC_PARAGRAPHCOMPLETE;
            mParent.publishValue(RTC_VAR_LINESTATE, TCONST.LAST);
        } else
            mParent.publishValue(RTC_VAR_LINESTATE, TCONST.NOT_LAST);

        if (mCurrPara >= mParaCount-1) {
            cummulativeState = TCONST.RTC_PAGECOMPLETE;
            mParent.publishValue(RTC_VAR_PARASTATE, TCONST.LAST);
        } else{
            mParent.publishValue(RTC_VAR_PARASTATE, TCONST.NOT_LAST);
            mParent.publishValue(TCONST.RTC_VAR_CLOZESTATE, TCONST.FTR_COMPLETE);
        }


        if (mCurrPage >= mPageCount-1) {
            cummulativeState = TCONST.RTC_STORYCMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.LAST);
        } else
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.NOT_LAST);

        //Log.d("ULANI", "publishStateValues: isclozepage = "+isClozePage+ " cloze_page_node = "+cloze_page_mode);
        hasQuestion();

        // Publish the cumulative state out to the scripting scope in the tutor
        //
        mParent.publishValue(TCONST.RTC_VAR_STATE, cummulativeState);
    }




    /**
     *  Configure for specific Page
     *  Assumes current storyName
     *  Only called once to find the first page of the story
     * @param pageIndex
     */
    @Override
    public void seekToPage(int pageIndex) {
//        Log.d("CLOZEPAGEISSUE", "seekToPage: pageIndex = "+pageIndex+" mcurrPage = "+mCurrPage);
        mCurrPage = pageIndex;
        if (mCurrPage > mPageCount-1) mCurrPage = mPageCount-1;
        if (mCurrPage < TCONST.ZERO)  mCurrPage = TCONST.ZERO;
        int numLinesCurPage = data[mCurrPage].text.length;
        this.numTotalLines+=numLinesCurPage;
        if (cloze_page_mode){
            if (clozeIndices.contains(this.numTotalLines)){
                this.isClozePage = true;
                incClozePage(TCONST.ZERO);
                mParent.publishValue(TCONST.HAS_DISTRACTOR, TCONST.TRUE);
            } else {
                this.isClozePage = false;
                incPage(TCONST.ZERO);
                mParent.publishValue(TCONST.HAS_DISTRACTOR, TCONST.FALSE);
            }
        } else {
            incPage(TCONST.ZERO);
        }
    }

    @Override
    public void nextPage() {
        if (mCurrPage < mPageCount-1) {
            int paracount = data[mCurrPage].text.length;
            int numLinesNextPage = 0;
            for(int i = 0; i < paracount; i++){
                numLinesNextPage+=data[mCurrPage].text[i].length;
            }
            this.numTotalLines+=numLinesNextPage;
            if (cloze_page_mode){
                // Check if the next page has a cloze question before incrementing the page
                if (this.clozeIndices.contains(this.numTotalLines)){
                    this.isClozePage = true;
                    mParent.publishValue(TCONST.HAS_DISTRACTOR, TCONST.TRUE);
                    incClozePage(TCONST.INCR);
                } else {
                    this.isClozePage = false;
                    incPage(TCONST.INCR);
                    mParent.publishValue(TCONST.HAS_DISTRACTOR, TCONST.FALSE);
                }
            } else if (picture_match_mode){
                incPage(TCONST.INCR);
            } else {
                incPage(TCONST.INCR);
            }
        }
        // Actually do the page animation
        //
//        if(cloze_page_mode && isClozePage){
//            mParent.fadeOutView(mPageImage);
//        } else {
        mParent.animatePageFlip(true, mCurrViewIndex);
        Log.d(TAG, "nextPage: cloze_page_mode = "+cloze_page_mode+" isClozePage = "+isClozePage);
//        }
    }

    @Override
    public void prevPage() {
        if (mCurrPage > 0) {
            incPage(TCONST.DECR);
        }
        //TODO: CHECK
        mParent.animatePageFlip(false, mCurrViewIndex);
    }

    private void incPage(int direction) {
        //MCURRPAGE BEING INCREMENTED
        mCurrPage += direction;

        // This configures the target display components to be populated with data.
        // mPageImage - mPageText
        //
        flipPage();

        configurePageImage();
        // Update the state vars
        // Note that this must be done after flip and configure so the target text and image views
        // are defined
        // NOTE: we reset mCurrPara, mCurrLine and mCurrWord
        //
        seekToStoryPosition(mCurrPage, TCONST.ZERO, TCONST.ZERO, TCONST.ZERO);
    }

    private void incClozePage(int direction) {
        //MCURRPAGE BEING INCREMENTED
        mCurrPage += direction;

        // This configures the target display components to be populated with data.
        // mPageImage - mPageText
        //
        flipPage();

        configurePageImage();

        // Update the state vars
        // Note that this must be done after flip and configure so the target text and image views
        // are defined
        // NOTE: we reset mCurrPara, mCurrLine and mCurrWord
        //
        seekToClozeStoryPosition(mCurrPage, TCONST.ZERO, TCONST.ZERO, TCONST.ZERO);
    }


    /**
     *  Configure for specific Paragraph
     *  Assumes current page
     *
     * @param paraIndex
     */
    @Override
    public void seekToParagraph(int paraIndex) {
        mCurrPara = paraIndex;

        if (mCurrPara > mParaCount-1) {mCurrPara = mParaCount-1;}
        if (mCurrPara < TCONST.ZERO)  {mCurrPara = TCONST.ZERO; }

        if (cloze_page_mode) {
            if (this.isClozePage){
                incClozePara(TCONST.ZERO);
            } else {
                incPara(TCONST.ZERO);
            }
        } else {
            incPara(TCONST.ZERO);
        }


    }

    @Override
    public void nextPara() {
        if (mCurrPara < mParaCount-1) {
            if (cloze_page_mode) {
                if (this.isClozePage){
                    incClozePara(TCONST.INCR);
                } else {
                    incPara(TCONST.INCR);
                }
            } else {
                incPara(TCONST.INCR);
            }
        }
    }


    @Override
    public void prevPara() {
        if (mCurrPara > 0) {
            incPara(TCONST.DECR);
        }
    }

    // NOTE: we reset mCurrLine and mCurrWord
    private void incPara(int incr) {
        mCurrPara += incr;
        System.out.println("ULANISTOPAUDIO: incpara "+mCurrPara+" "+mParaCount+" "+mCurrLine+" "+mLineCount);
//        if (mCurrPara == mParaCount && mLineCount == 1) postDelayedStopAudio();
        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, TCONST.ZERO, TCONST.ZERO);
    }

    // NOTE: we reset mCurrLine and mCurrWord
    private void incClozePara(int incr) {
        mCurrPara += incr;
        System.out.println("ULANISTOPAUDIO: incclozepara "+mCurrPara+" "+mParaCount+" "+mCurrLine+" "+mLineCount);

//        if (mCurrPara == mParaCount && mLineCount == 1) postDelayedStopAudio();

        // Update the state vars
        //
        seekToClozeStoryPosition(mCurrPage, mCurrPara, TCONST.ZERO, TCONST.ZERO);
    }


    /**
     *  Configure for specific line
     *  Assumes current page and paragraph
     *
     * @param lineIndex
     */
    @Override
    public void seekToLine(int lineIndex) {
//        Log.d(TAG, "seekToLine: ");
        mCurrLine = lineIndex;

        if (mCurrLine > mLineCount-1) mCurrLine = mLineCount-1;
        if (mCurrLine < TCONST.ZERO)  mCurrLine = TCONST.ZERO;

        if (cloze_page_mode){
            if (this.isClozePage){
                incClozeLine(TCONST.INCR);
            } else {
                incLine(TCONST.INCR);
            }
        } else {
            incLine(TCONST.INCR);
        }
    }

    @Override
    public void nextLine() {
//        Log.d(TAG, "nextLine: ");
        if (mCurrLine < mLineCount-1) {
            if (cloze_page_mode){
                if (this.isClozePage){
                    incClozeLine(TCONST.INCR);
                } else {
                    incLine(TCONST.INCR);
                }
            } else {
                incLine(TCONST.INCR);
            }
        }
    }

    @Override
    public void prevLine() {

        if (mCurrLine > 0 ) {
            incLine(TCONST.DECR);
        }

    }

    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    private void incLine(int incr) {
//        Log.d("ULANI", "incLine: ");
        // reset boot flag to
        //
        if (storyBooting) {

            storyBooting = false;
            speakOrListen();
        } else {
            mCurrLine += incr;
            // Update the state vars
            //
            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
        }
    }

    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    private void incClozeLine(int incr) {
        // reset boot flag to
        //
        if (storyBooting) {

            storyBooting = false;
            speakOrListen();
        } else {

            mCurrLine += incr;
            // Update the state vars
            //
            seekToClozeStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
        }
    }


    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    @Override
    public void echoLine() {

        // reset the echo flag
        //
        mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.FALSE);

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
    }


    /**
     *
     */
    @Override
    public void parrotLine() {

        mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.FALSE);

//        Log.d(TAG, "parrotLine");

        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
    }


    /**
     *  Configure for specific word
     *  Assumes current page, paragraph and line
     *
     * @param wordIndex
     */
    @Override
    public void seekToWord(int wordIndex) {

        mCurrWord = wordIndex;
        mHeardWord = 0;

        if (mCurrWord > mWordCount-1) mCurrWord = mWordCount-1;
        if (mCurrWord < TCONST.ZERO)  mCurrWord = TCONST.ZERO;

        // Update the state vars
        // note that both regular and cloze pages can occur in cloze mode
        if (cloze_page_mode){
            if (this.isClozePage){
                seekToClozeStoryPosition(mCurrPage, mCurrPara, mCurrLine, wordIndex);

            } else {
                seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, wordIndex);
            }
        } else {
            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, wordIndex);
        }


        incWord(TCONST.ZERO);

        // Start listening from the new position
        //
        speakOrListen();
    }


    @Override
    public void nextWord() {

        if (mCurrWord < mWordCount) {
            incWord(TCONST.INCR);
        }
    }
    @Override
    public void prevWord() {

        if (mCurrWord > 0) {
            incWord(TCONST.DECR);
        }
    }

    /**
     * We assume this has been bounds checked prior to call
     *
     * There is one optimization here - when simply moving through the sentence we do not rebuild
     * the entire state.  We just publish any change of state
     *
     * @param incr
     */
    private void incWord(int incr) {
        Log.d("ULANISTOPAUDIO", "incWord: numWordsCurPage = "+numWordsCurPage);
        if(mCurrPage!=0) numWordsCurPage-=1;

        mCurrWord += incr;

        // For instances where we are advancing the word manually through a script it is required
        // that you reset the highlight and the FTR_WRONG so the next word is highlighted correctly
        //
        setHighLight(TCONST.EMPTY, false);
        mParent.UpdateValue(true);

        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();

        // Update the sentence display
        //
        UpdateDisplay();

    }

    /**
     * This picks up listening from the last word - so it seeks to wherever we are in the
     * current sentence and listens from there.
     */
    public void continueListening() {
        speakOrListen();
    }


    private void startListening() {

        // We allow the user to say any of the onscreen words but set the priority order of how we
        // would like them matched  Note that if the listener is not explicitly listening for a word
        // it will just ignore it if spoken.
        //
        // for the current target word.
        // 1. Start with the target word on the target sentence
        // 2. Add the words from there to the end of the sentence - just to permit them
        // 3. Add the words alread spoken from the other lines - just to permit them
        //
        // "Permit them": So the language model is listening for them as possibilities.
        //
        wordsToListenFor = new ArrayList<>();

        for (int i1 = mCurrWord; i1 < wordsToSpeak.length; i1++) {
            wordsToListenFor.add(wordsToSpeak[i1]);
        }
        for (int i1 = 0; i1 < mCurrWord; i1++) {
            wordsToListenFor.add(wordsToSpeak[i1]);
        }
        for (String word : wordsSpoken) {
            wordsToListenFor.add(word);
        }

        // If we want to listen for all the words that are visible
        //
        if (listenFutureContent) {
            for (String word : futureSpoken) {
                wordsToListenFor.add(word);
            }
        }

        // Start listening
        //
        if (mListener != null) {

            // reset the relative position of mCurrWord in the incoming PLRT heardWords array
            mHeardWord = 0;
            mListener.reInitializeListener(true);
            mListener.updateNextWordIndex(mHeardWord);

            mListener.listenFor(wordsToListenFor.toArray(new String[wordsToListenFor.size()]), 0);
            mListener.setPauseListener(false);
        }
    }


    /**
     * Scipting mechanism to update target word highlight
     * @param highlight
     */
    @Override
    public void setHighLight(String highlight, boolean update) {
        Log.d(TAG, "setHighLight: "+highlight);
        mCurrHighlight =  highlight;

        // Update the sentence display
        //
        if (update)
            if (mCurrPage>0 && numWordsCurPage==1){
                mParent.stopAudio();
            }
            UpdateDisplay();
    }

    /**
     * Method to decide whether we will play generic questions or not
     */
    @Override
    public void decideToPlayGenericQuestion() {

        double chance = Math.random();
        Log.d("GEN_QUESTION_CHANCE", "Comparing " + chance + " to " + GEN_QUESTION_CHANCE);

        if (chance < GEN_QUESTION_CHANCE) {
            mParent.publishFeature(FTR_PLAY_GEN);
        } else {
            mParent.retractFeature(FTR_PLAY_GEN);
        }

    }

    /**
     * Method to insert generic question between pages
     */
    @Override
    public void genericQuestions(){
        mParent.publishValue(TCONST.RTC_VAR_QUESTIONSTATE, TCONST.FTR_COMPLETE);
    }

    /**
     * Method to insert generic question between pages
     */
    @Override
    public void displayGenericQuestion(){
        mCurrViewIndex = mQuestionIndex;
        mPageImage = (ImageView) mQuestionPage.findViewById(R.id.SgenericQuestionImage);
        mPageText.setText(Html.fromHtml(""));
        mParent.animatePageFlip(true, mCurrViewIndex);
    }

    /**
     * Method to randomly choose a generic question to use
     */
    @Override
    public void setRandomGenericQuestion(){
        try {
            /*String DATASOURCEPATH = "sdcard/robotutor_assets/assets/audio/sw/cmu/xprize/story_reading/generic_questions/questions";
            File dir = new File(DATASOURCEPATH);
            File[] allfiles = dir.listFiles();*/

            String[] genericQuestions =
            {"Who is this story about",
            "What has happened so far",
            "What is this story about"};

            Log.wtf("COMPREHEND", "files: " + genericQuestions.length);
            /*File[] files = new File[15];
            int count = 0;
            for (File file : allfiles) {
                Log.wtf("COMPREHEND", file.getName());
                if (file.getName().contains(" ") && !file.getName().contains("5 second pause")) {
                    Log.wtf("COMPREHEND", "adding" + file.getName());
                    files[count] = file;
                    count++;
                } else {
                    Log.wtf("COMPREHEND", "not adding"  + file.getName());
                }
            }*/
            Random rand = new Random();
            String filename = genericQuestions[rand.nextInt(genericQuestions.length)];
            this.curGenericQuestion = filename;
            mParent.publishValue(TCONST.RTC_QUESTION_AUDIO, this.curGenericQuestion);
        } catch (Exception e) {
            Log.wtf("COMPREHEND", "found the error");
            e.printStackTrace();
        }
    }

    /**
     * Finds the last sentence of the current page, which will be used as a cloze question
     */
    @Override
    public void setClozeQuestion(){
//        Log.d(TAG, "setClozeQuestion: ");
        if (isClozePage){
            int numLinesCurPage = data[mCurrPage].text.length;
            this.clozeQuestion = questions[numLinesCurPage-1];
            if (questions[numLinesCurPage-1].distractor.nonsensical != null && questions[numLinesCurPage-1].distractor.nonsensical.length > 0){
                hasNonsensical = true;
            }
            if (questions[numLinesCurPage-1].distractor.ungrammatical != null && questions[numLinesCurPage-1].distractor.ungrammatical.length > 0){
                hasUngrammatical = true;
            }
            if (questions[numLinesCurPage-1].distractor.plausible != null && questions[numLinesCurPage-1].distractor.plausible.length > 0){
                hasPlausible = true;
            }
        }

//        updateClozeButtons();
    }

    private String printArray(String[] a){
        String result = "";
        for (int i = 0; i < a.length; i++){
            result+=a[i];
            result+=" ";
        }
        return result;
    }

    /**
     * TRACE_CLOZE here is where the cloze question text is set
     */
    @Override
    public void displayClozeQuestion(){
        ArrayList<String> nonsensical = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.nonsensical));
        ArrayList<String> ungrammatical = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.ungrammatical));
        ArrayList<String> plausible = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.plausible));
        Collections.shuffle(nonsensical);
        Collections.shuffle(ungrammatical);
        Collections.shuffle(plausible);
        ArrayList<String> finalOrder = new ArrayList<>();
        if (clozeQuestion.distractor.plausible.length == 0) {
            if (clozeQuestion.distractor.nonsensical.length == 0){
                finalOrder = ungrammatical;
            } else if (clozeQuestion.distractor.ungrammatical.length == 0){
                finalOrder = nonsensical;
            } else {
                finalOrder.add(ungrammatical.get(0));
                finalOrder.add(ungrammatical.get(1));
                finalOrder.add(nonsensical.get(0));
            }
        } else if(clozeQuestion.distractor.ungrammatical.length == 0){
            if (clozeQuestion.distractor.nonsensical.length == 0){
                finalOrder = plausible;
            } else if (clozeQuestion.distractor.plausible.length == 0){
                finalOrder = nonsensical;
            } else {
                finalOrder.add(plausible.get(0));
                finalOrder.add(plausible.get(1));
                finalOrder.add(nonsensical.get(0));
            }
        } else if(clozeQuestion.distractor.nonsensical.length == 0){
            if (clozeQuestion.distractor.ungrammatical.length == 0){
                finalOrder = plausible;
            } else if (clozeQuestion.distractor.plausible.length == 0){
                finalOrder = ungrammatical;
            } else {
                finalOrder.add(plausible.get(0));
                finalOrder.add(plausible.get(1));
                finalOrder.add(ungrammatical.get(0));
            }
        }else{
            finalOrder.add(plausible.get(0));
            finalOrder.add(ungrammatical.get(0));
            finalOrder.add(nonsensical.get(0));
        }
        finalOrder.add(clozeQuestion.target);
        Collections.shuffle(finalOrder);
        mPageImage.setImageBitmap(null);
        mWord1Text.setText(Html.fromHtml(finalOrder.get(0)));
        mWord1Text.bringToFront();
        mWord2Text.setText(Html.fromHtml(finalOrder.get(1)));
        mWord2Text.bringToFront();
        mWord3Text.setText(Html.fromHtml(finalOrder.get(2)));
        mWord3Text.bringToFront();
        mWord4Text.setText(Html.fromHtml(finalOrder.get(3)));
        mWord4Text.bringToFront();
        if (this.isClozePage) {
            mParent.fadeOutView(mPageImage);
//            enableClozeButtons();
//            showClozeButtons();
        }
    }


    /**
     *
     * PIC_CHOICE look at this!
     * PIC_CHOICE this should def be refactored
     */
    @Override
    public void displayPictureMatching(){

        // OPEN_SOURCE picmatch... place debug breakpoint here.
        if (numPicMatch==2){
            InputStream in1;
            InputStream in2;
            ArrayList<String> imgs = new ArrayList<>();
            for (int i = mCurrPage+1; i < data.length; i++){ // PIC_CHOICE it's not adding the last image (page_09)
                if (!imgs.contains(data[i].image)){
                    imgs.add(data[i].image);
                }
            }
            Collections.shuffle(imgs);
            String randImg1 = imgs.get(0); // PIC_CHOICE this is supposed to be the correct answer
            try {
                if (assetLocation.equals(TCONST.EXTERN)) {
                    in1 = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image
                    in2 = new FileInputStream(mAsset + randImg1); // ZZZ load image
                } else {
                    in1 = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
                    in2 = JSON_Helper.assetManager().open(mAsset + randImg1); // ZZZ load image
                }
                int targetIdx = getRandomNumberInRange(0, 1);
                this.picmatch_answer = targetIdx;
                if (targetIdx == 1){
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in2));
                } else {
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMatchImage1.bringToFront();
            mMatchImage2.bringToFront();
        }
        if (numPicMatch==3){
            InputStream in1;
            InputStream in2;
            InputStream in3;
            ArrayList<String> imgs = new ArrayList<>();
            for (int i = mCurrPage+1; i < data.length; i++){ // OPEN_SOURCE data[2].image == "null"... why?
                if (!imgs.contains(data[i].image)){
                    imgs.add(data[i].image);
                }
            }
            Collections.shuffle(imgs);
            String randImg1 = imgs.get(0);
            String randImg2 = imgs.get(1);
            try {
                if (assetLocation.equals(TCONST.EXTERN)) {
                    in1 = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image
                    in2 = new FileInputStream(mAsset + randImg1); // ZZZ load image
                    in3 = new FileInputStream(mAsset + randImg2); // OPEN_SOURCE null image... why?
                } else {
                    in1 = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
                    in2 = JSON_Helper.assetManager().open(mAsset + randImg1); // ZZZ load image
                    in3 = JSON_Helper.assetManager().open(mAsset + randImg2);
                }
                int targetIdx = getRandomNumberInRange(0, 2);
                this.picmatch_answer = targetIdx;
                if (targetIdx == 2){
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in3));
                } else if (targetIdx == 1){
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in3));
                } else {
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in3));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMatchImage1.bringToFront();
            mMatchImage2.bringToFront();
            mMatchImage3.bringToFront();
        }
        if (numPicMatch==4){
            InputStream in1;
            InputStream in2;
            InputStream in3;
            InputStream in4;
            ArrayList<String> imgs = new ArrayList<>();
            for (int i = mCurrPage+1; i < data.length; i++){ // PIC_CHOICE should be data.length
                if (!imgs.contains(data[i].image)){
                    imgs.add(data[i].image); // PIC_CHOICE this array might end tooo early
                }
            }
            Collections.shuffle(imgs);
            String randImg1 = imgs.get(0); // PIC_CHOICE this may have been wrong?
            String randImg2 = imgs.get(1); // PIC_CHOICE out of bounds error in story_2
            String randImg3 = imgs.get(2);
            try {
                if (assetLocation.equals(TCONST.EXTERN)) {
                    in1 = new FileInputStream(mAsset + data[mCurrPage].image); // PIC_CHOICE correct image // ZZZ load image
                    in2 = new FileInputStream(mAsset + randImg1); // ZZZ load image
                    in3 = new FileInputStream(mAsset + randImg2);
                    in4 = new FileInputStream(mAsset + randImg3);
                } else {
                    in1 = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
                    in2 = JSON_Helper.assetManager().open(mAsset + randImg1); // ZZZ load image
                    in3 = JSON_Helper.assetManager().open(mAsset + randImg2);
                    in4 = JSON_Helper.assetManager().open(mAsset + randImg3);
                }
                int targetIdx = getRandomNumberInRange(0, 3);
                this.picmatch_answer = targetIdx;
                if (targetIdx == 3) {
                    mMatchImage4.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in3));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in4));
                }
                if (targetIdx == 2){
                    mMatchImage4.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in4));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in3));
                } else if (targetIdx == 1){
                    mMatchImage4.setImageBitmap(BitmapFactory.decodeStream(in4));
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in3));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in1));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in2));
                } else {
                    mMatchImage4.setImageBitmap(BitmapFactory.decodeStream(in4));
                    mMatchImage3.setImageBitmap(BitmapFactory.decodeStream(in3));
                    mMatchImage2.setImageBitmap(BitmapFactory.decodeStream(in2));
                    mMatchImage1.setImageBitmap(BitmapFactory.decodeStream(in1));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            mMatchImage1.bringToFront();
            mMatchImage2.bringToFront();
            mMatchImage3.bringToFront();
            mMatchImage4.bringToFront();
        }
        show_image_options = true;
        updateImageButtons();
        mParent.animatePageFlip(true, mCurrViewIndex);
    }

    @Override
    public void hasClozeDistractor(){
        if (mCurrPage <= mPageCount-1) {
            if (isClozePage && mCurrPara >= mParaCount-1){
                mParent.publishValue(SHOW_CLOZE, TCONST.TRUE);
                mParent.publishValue(SHOW_PICMATCH, TCONST.FALSE);
            } else {
                mParent.publishValue(SHOW_CLOZE, TCONST.FALSE);
                mParent.publishValue(SHOW_PICMATCH, TCONST.FALSE);
            }
        }
    }

    @Override
    public void hasQuestion(){
        //TODO: either call one or the other depending on ftr
        hasClozeDistractor();
        hasPictureMatch();
    }

    public void hasPictureMatch(){
        if (picture_match_mode && mCurrPara >= mParaCount-1 && mCurrPage % 2 == 1) {
            mParent.publishValue(SHOW_PICMATCH, TCONST.TRUE);
            mParent.publishValue(SHOW_CLOZE, TCONST.FALSE);
        }
//        } else {
//            mParent.publishValue(TCONST.SHOW_PICMATCH, TCONST.FALSE);
//            mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
//        }
    }



    /**
     *  Update the displayed sentence
     */
    private void UpdateDisplay() {
        if (showWords) {
            String fmtSentence = "";

            for (int i = 0; i < wordsToDisplay.length; i++) {

                String styledWord = wordsToDisplay[i];                           // default plain
//                Log.d(TAG, "Styled word: " + styledWord);

                if (i < mCurrWord) {
                    styledWord = "<font color='#00B600'>" + styledWord + "</font>";
                }

                if (i == mCurrWord) {// style the next expected word

                    if (!mCurrHighlight.equals(TCONST.EMPTY))
                        styledWord = "<font color='" + mCurrHighlight + "'>" + styledWord + "</font>";

                    styledWord = "<u>" + styledWord + "</u>";
                }

                if (showFutureWords || i < mCurrWord) {
                    if (wordsToDisplay[i].endsWith("'") || wordsToDisplay[i].endsWith("-")) {
                        fmtSentence += styledWord;
                    } else {
                        fmtSentence += styledWord + ((i < wordsToDisplay.length - 1) ? TCONST.WORD_SPACE : TCONST.NO_SPACE);
                    }
                }
            }

            // Generate the text to be displayed
            //
            String content = completedSentencesFmtd + fmtSentence;

            if (showFutureContent)
                content += TCONST.SENTENCE_SPACE + futureSentencesFmtd;

            mPageText.setText(Html.fromHtml(content));
            if (cloze_page_mode) this.oldClozePageText = content;
//            Log.d(TAG, "Story Sentence Text: " + content);

            if (showWords && (showFutureWords || mCurrWord > 0)) broadcastActiveTextPos(mPageText, wordsToDisplay);

            // Publish the current word / sentence / remaining words for use in scripts
            //
            if (mCurrWord < wordsToSpeak.length) {
                mParent.publishValue(TCONST.RTC_VAR_WORDVALUE, wordsToSpeak[mCurrWord]);

                String remaining[] = Arrays.copyOfRange(wordsToSpeak, mCurrWord, wordsToSpeak.length);

                mParent.publishValue(TCONST.RTC_VAR_REMAINING, TextUtils.join(" ", remaining));
                mParent.publishValue(TCONST.RTC_VAR_SENTENCE,  TextUtils.join(" ", wordsToSpeak));
            }
        }
    }


    /**
     *
     * @param text
     * @param words
     * @return
     */
    private PointF broadcastActiveTextPos(TextView text, String[] words){

        PointF  point   = new PointF(0,0);
        int     charPos = 0;
        int     maxPos;

        try {
            Layout layout = text.getLayout();

            if (layout != null && mCurrWord < words.length) {

                // Point to the start of the Target sentence (mCurrLine)
                charPos  = completedSentences.length();

                // Find the starting character of the current target word
                for (int i1 = 0; i1 <= mCurrWord; i1++) {
                    charPos += words[i1].length() + 1;
                }

                // Look at the end of the target word
                charPos -= 1;

                // Note that sending a value greater than maxPos will corrupt the textView - so
                // guarantee this will never happen.
                //
                maxPos  = text.getText().length();
                charPos = (charPos > maxPos) ? maxPos : charPos;

                point.x = layout.getPrimaryHorizontal(charPos);

                int y = layout.getLineForOffset(charPos);
                point.y = layout.getLineTop(y);

                CPersonaObservable.broadcastLocation(text, TCONST.LOOKAT, point);
            }

        } catch (Exception e) {
            Log.d(TAG, "broadcastActiveTextPos: " + e.toString());
        }

        return point;
    }


    /**
     * This is where we process words being narrated
     *
     */
    @Override
    public void onUpdate(String[] heardWords) {

        boolean result    = true;
        String  logString = "";

        for (int i = 0; i < heardWords.length; i++) {
            logString += heardWords[i].toLowerCase() + " | ";
        }
        Log.i("ASR", "Update Words Spoken: " + logString);

        while (mHeardWord < heardWords.length) {

            if (wordsToSpeak[mCurrWord].equals(heardWords[mHeardWord])) {

                nextWord();
                mHeardWord++;

                Log.i("ASR", "RIGHT");
                attemptNum = 0;
                result = true;
            } else {
                Log.e(TAG, "Input Error in narrator no match found - mCurrWord ->" + wordsToSpeak[mCurrWord] + " -> heardWords: " + heardWords[mHeardWord]);

                nextWord();
                mHeardWord++;

                attemptNum = 0;
                result = true;
            }
            mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord - 1, heardWords[mHeardWord - 1], attemptNum, false, result);
        }

        // Publish the outcome
        mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum);
        mParent.UpdateValue(result);
    }


    /**
     * This is where the incoming PLRT ASR data is processed.
     *
     * Provided the input matches the model sentence it continues in sequence through the sentence
     * words. If there is an error it seeks the listener to only "hear" words form the error to the
     * end of sentence and continues like this iteratively as required.  The goal is to eliminate the
     * word shuffling that Multi-Match does and simplify the process. Ultimately this should migrate
     * to using 2 simultaneous decoders one for the correct sentence and one for any other "distractor"
     * words. i.e. other words in the sentence in this case.
     *
     *  TODO: check if it is possible for the hypothesis to chamge between last update and final hyp
     */
    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        boolean result    = true;
        String  logString = "";

        try {
            for (int i = 0; i < heardWords.length; i++) {
                if (heardWords[i] != null) {
                    logString += heardWords[i].hypWord.toLowerCase() + ":" + heardWords[i].iSentenceWord + " | ";
                } else {
                    logString += "VIRTUAL | ";
                }
            }

            while ((mCurrWord < wordsToSpeak.length) && (mHeardWord < heardWords.length)) {

                if (wordsToSpeak[mCurrWord].equals(heardWords[mHeardWord].hypWord)) {

                    nextWord();
                    mHeardWord++;

                    mListener.updateNextWordIndex(mHeardWord);

                    Log.i("ASR", "RIGHT");
                    attemptNum = 0;
                    result = true;
                    mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord - 1, heardWords[mHeardWord - 1].hypWord, attemptNum, heardWords[mHeardWord - 1].utteranceId == "", result);

                } else {

                    mListener.setPauseListener(true);

                    Log.i("ASR", "WRONG");
                    attemptNum++;
                    result = false;
                    mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord, heardWords[mHeardWord].hypWord, attemptNum, heardWords[mHeardWord].utteranceId == "", result);
                    break;
                }
            }

            Log.i("ASR", "Update New HypSet: " + logString + " - Attempt: " + attemptNum);

            // Publish the outcome
            mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum);
            mParent.UpdateValue(result);

            mParent.onASREvent(TCONST.RECOGNITION_EVENT);

        } catch (Exception e) {

            Log.e("ASR", "onUpdate Fault: " + e);
        }
    }


    public void generateVirtualASRWord() {

        mListener.setPauseListener(true);

        ListenerBase.HeardWord words[] = new ListenerBase.HeardWord[mHeardWord+1];

        words[mHeardWord] = new ListenerBase.HeardWord(wordsToSpeak[mCurrWord]);

        onUpdate(words, false);

        mListener.setPauseListener(false);
//        startListening();
    }


    /**
     * This is where incoming JSGF ASR data would be processed.
     *
     *  TODO: check if it is possible for the hypothesis to change between last update and final hyp
     */
    @Override
    public void onUpdate(String[] heardWords, boolean finalResult) {

//        String logString = "";
//
//        for (String hypWord :  heardWords) {
//            logString += hypWord.toLowerCase() + ":" ;
//        }
//        Log.i("ASR", "New JSGF HypSet: "  + logString);
//
//
//        mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum++);
//
//        mParent.onASREvent(TCONST.RECOGNITION_EVENT);

    }


    @Override
    public boolean endOfData() {
        return false;
    }


    //************ Serialization


    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {
        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
    }

    public int getmCurrPara(){
        return mCurrPara;
    }

    public int getmCurrLine(){
        return mCurrLine;
    }

    public int getmParaCount(){
        return mParaCount;
    }

    public int getmLineCount(){
        return mLineCount;
    }

    public int getSegmentNdx(){
        return segmentNdx;
    }

    public int getNumSegments(){
        return numSegments;
    }

    public int getUtteranceNdx(){
        return utteranceNdx;
    }

    public int getNumUtterance(){
        return numUtterance;
    }

    public boolean getEndOfSentence(){
        return endOfSentence;
    }

    public CASB_Narration[] getRawNarration(){
        return rawNarration;
    }

    public int getUtterancePrev(){
        return utterancePrev;
    }

    public int getSegmentPrev(){
        return segmentPrev;
    }

    @Override
    public boolean isClozeMode() { return cloze_page_mode;}

    @Override
    public boolean isGenMode() { return false;}

    @Override
    public boolean isPicMode() {return picture_match_mode;}
}
