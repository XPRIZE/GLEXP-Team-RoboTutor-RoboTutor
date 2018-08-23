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
import android.graphics.PointF;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
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
    private int                     picmatch_answer;
    private boolean                 picture_match_mode = false;
    private boolean                 cloze_page_mode = false;
    private boolean                 isClozePage = false;
    private boolean                 match_is_right;
    private ViewGroup               mPicturePage;
    private ImageView               mMatchImageLeft;
    private ImageView               mMatchImageRight;
    private ImageView               mMatchImageCenter;
    private boolean                 show_image_options = false;

    private int                     mPictureIndex;
    private ViewGroup               mQuestionPage;
    private int                     mQuestionIndex;
    private TextView                mWord1Text;
    private TextView                mWord2Text;
    private TextView                mWord3Text;
    private TextView                mWord4Text;
    private ClozeQuestion           clozeQuestion;

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
        mPicturePage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.qn_picture, null);

        mOddPage.setVisibility(View.GONE);
        mEvenPage.setVisibility(View.GONE);
        mQuestionPage.setVisibility(View.GONE);
        mPicturePage.setVisibility(View.GONE);

        mOddIndex  = mParent.addPage(mOddPage);
        mEvenIndex = mParent.addPage(mEvenPage);
        mQuestionIndex = mParent.addPage(mQuestionPage);
        mPictureIndex = mParent.addPage(mPicturePage);
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
        for(int i = 0; i < questions.length; i++){
            if (questions[i].distractor != null) {
                clozeIndices.add(i+1);
            }
        }
        int numPara = 0;
        int numLineCountdown = 0;
        for(int i = 0; i < data.length; i++){
            numPara += data[i].text.length;
            for(int j = 0; j < data[i].text.length; j++){
                numLineCountdown += data[i].text[j].length;
            }
        }
        Log.d("CLOZEPAGEISSUE", "initStory: questions.length = "+questions.length);
        Log.d("CLOZEPAGEISSUE", "initStory: numpara = "+numPara);
        Log.d("CLOZEPAGEISSUE", "initStory: numlines = "+numLineCountdown);
        //If numlines > numquestions theres something wrong
        Log.d("CLOZEPAGEISSUE", "initStory: data.length (number of pages) = "+data.length);
        mCurrLineInStory = 0;
        mOwner        = owner;
        mAsset        = assetPath; // ZZZ assetPath... TCONST.EXTERN
        storyBooting  = true;
        assetLocation = location;  // ZZZ assetLocation... contains storydata.json and images

        Log.d(TCONST.DEBUG_STORY_TAG, String.format("mAsset=%s -- assetLocation=%s", mAsset, assetLocation));

        if (mParent.testFeature(TCONST.FTR_USER_HIDE)) showWords = false;
        if (mParent.testFeature(TCONST.FTR_USER_REVEAL)) showFutureWords = showFutureContent = false;

        Log.d(TAG, "initStory: showWords = " + showWords + ", showFutureWords = " + showFutureWords + ", showFutureContent = " + showFutureContent);

        mParent.setFeature(TCONST.FTR_STORY_STARTING, TCONST.ADD_FEATURE);

        seekToPage(TCONST.ZERO);

        //TODO: CHECK
        mParent.animatePageFlip(true,mCurrViewIndex);
    }

    @Override
    public void setPictureMatch(){
        if ((data.length - 1) - mCurrPage >= 3){
            picture_match_mode = true;
        } else {
            //UHQ: ensure that the last 2 pages are just regular pages, because no more pics to use
            picture_match_mode = false;
        }
    }

    @Override
    public void setClozePage(){
        //Log.d("ULANICLOZEPAGEISSUE", "setClozePage: ");
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
        Log.d("ULANISTOPAUDIO", "setClozePage: numwordscurpage= " +numWordsCurPage);
        int sum=mCurrLineInStory+numLines;
//        Log.d("ULANICLOZEPAGEISSUE", "setClozePage: numTotalLines = "+sum);
//        Log.d("CLOZEPAGEISSUE", "setClozePage: mcurrlineinstory= "+mCurrLineInStory+" mcurrpage = "+mCurrPage);
//        Log.d("CLOZEPAGEISSUE", "setClozePage: clozeIndices = "+clozeIndices.toString());
        if(this.clozeIndices.contains(mCurrLineInStory+numLines)){
            clozeQuestion = questions[sum-1];
            TCONST.TARGET = clozeQuestion.target;
            Log.d(TAG, "setClozePage: clozepagenode = true");
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
//            if (mParent.testFeature(TCONST.FTR_PIC)){
//                picture_match_mode = true;
//            }

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

    private void updateClozeButtons(){
        Log.d(TAG, "updateClozeButtons: ");
        setButtonState(mWord1Text, "DISABLE");
        setButtonState(mWord1Text, "HIDE");
        setButtonState(mWord2Text, "DISABLE");
        setButtonState(mWord2Text, "HIDE");
        setButtonState(mWord3Text, "DISABLE");
        setButtonState(mWord3Text, "HIDE");
        setButtonState(mWord4Text, "DISABLE");
        setButtonState(mWord4Text, "HIDE");

        mWord1Text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB: WORD1");
                Log.d(TAG, "onClick: "+mWord1Text.getText().toString());
                Log.d(TAG, "onClick: target "+TCONST.TARGET);
                if (mWord1Text.getText().toString().equals(TCONST.TARGET)){
                    mWord1Text.setBackgroundColor(6618880);
                    Log.d(TAG, "onClick: correct");
                    mParent.retractFeature(TCONST.CLOZE_WRONG);
                    mParent.publishFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
                    isClozePage = false;
                }else{
                    mWord1Text.setBackgroundColor(16724480);
                    Log.d(TAG, "onClick: incorrect");
                    mParent.retractFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishFeature(TCONST.CLOZE_WRONG);
                }
                mParent.nextNode();
            }
        });

        mWord2Text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB: WORD2");
                Log.d(TAG, "onClick: "+mWord2Text.getText().toString());
                Log.d(TAG, "onClick: target "+TCONST.TARGET);
                if (mWord2Text.getText().toString().equals(TCONST.TARGET)){
                    mWord1Text.setBackgroundColor(6618880);
                    Log.d(TAG, "onClick: correct");
                    mParent.retractFeature(TCONST.CLOZE_WRONG);
                    mParent.publishFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
                    isClozePage = false;
                }else{
                    mWord1Text.setBackgroundColor(16724480);
                    Log.d(TAG, "onClick: incorrect");
                    mParent.retractFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishFeature(TCONST.CLOZE_WRONG);
                }
                mParent.nextNode();

            }
        });

        mWord3Text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB: WORD3");
                Log.d(TAG, "onClick: "+mWord3Text.getText().toString());
                Log.d(TAG, "onClick: target "+TCONST.TARGET);
                if (mWord3Text.getText().toString().equals(TCONST.TARGET)){
                    mWord1Text.setBackgroundColor(6618880);
                    Log.d(TAG, "onClick: correct");
                    mParent.retractFeature(TCONST.CLOZE_WRONG);
                    mParent.publishFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
                    isClozePage = false;
                }else{
                    mWord1Text.setBackgroundColor(16724480);
                    Log.d(TAG, "onClick: incorrect");
                    mParent.retractFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishFeature(TCONST.CLOZE_WRONG);
                }
                mParent.nextNode();

            }
        });

        mWord4Text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CQn_ViewManagerASB: WORD4");
                Log.d(TAG, "onClick: "+mWord4Text.getText().toString());
                Log.d(TAG, "onClick: target "+TCONST.TARGET);
                if (mWord4Text.getText().toString().equals(TCONST.TARGET)){
                    mWord1Text.setBackgroundColor(6618880);
                    Log.d(TAG, "onClick: correct");
                    mParent.retractFeature(TCONST.CLOZE_WRONG);
                    mParent.publishFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
                    isClozePage = false;
                }else{
                    mWord1Text.setBackgroundColor(16724480);
                    Log.d(TAG, "onClick: wrong");
                    mParent.retractFeature(TCONST.CLOZE_CORRECT);
                    mParent.publishFeature(TCONST.CLOZE_WRONG);
                }
                mParent.nextNode();
            }
        });

    }

    private void updateImageButtons(){
        disableImageButtons();
        if (show_image_options && picture_match_mode){
            mMatchImageRight.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                disableImageButtons();
                if (picmatch_answer == 2){
                    mParent.publishFeature(TCONST.PICMATCH_CORRECT);
                    mParent.retractFeature(TCONST.PICMATCH_WRONG);
                    picture_match_mode = false;
                    show_image_options = false;
                    hasQuestion();
                }else{
                    mParent.retractFeature(TCONST.PICMATCH_CORRECT);
                    mParent.publishFeature(TCONST.PICMATCH_WRONG);
                }
                mParent.nextNode();
                }
            });
            mMatchImageCenter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                disableImageButtons();
                if (picmatch_answer == 1){
                    mParent.publishFeature(TCONST.PICMATCH_CORRECT);
                    mParent.retractFeature(TCONST.PICMATCH_WRONG);
                    picture_match_mode = false;
                    show_image_options = false;
                    hasQuestion();
                }else{
                    mParent.retractFeature(TCONST.PICMATCH_CORRECT);
                    mParent.publishFeature(TCONST.PICMATCH_WRONG);
                }
                mParent.nextNode();
                }
            });
            mMatchImageLeft.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                disableImageButtons();
                if (picmatch_answer == 0){
                    mParent.publishFeature(TCONST.PICMATCH_CORRECT);
                    mParent.retractFeature(TCONST.PICMATCH_WRONG);
                    picture_match_mode = false;
                    show_image_options = false;
                    hasQuestion();
                }else{
                    mParent.retractFeature(TCONST.PICMATCH_CORRECT);
                    mParent.publishFeature(TCONST.PICMATCH_WRONG);
                }
                mParent.nextNode();
                }
            });
            showImageButtons();
        } else {
            hideImageButtons();
        }
    }

    public void showImageButtons(){
        setButtonState(mMatchImageLeft, "SHOW");
        setButtonState(mMatchImageCenter, "SHOW");
        setButtonState(mMatchImageRight, "SHOW");
    }

    @Override
    public void enableImageButtons(){
        setButtonState(mMatchImageLeft, "ENABLE");
        setButtonState(mMatchImageCenter, "ENABLE");
        setButtonState(mMatchImageRight, "ENABLE");
    }

    public void hideImageButtons(){
        setButtonState(mMatchImageLeft, "HIDE");
        setButtonState(mMatchImageCenter, "HIDE");
        setButtonState(mMatchImageRight, "HIDE");
    }

    @Override
    public void disableImageButtons(){
        setButtonState(mMatchImageLeft, "DISABLE");
        setButtonState(mMatchImageCenter, "DISABLE");
        setButtonState(mMatchImageRight, "DISABLE");
    }

    /**
     *  This configures the target display components to be populated with data.
     *
     *  mPageImage - mPageText
     *
     */
    public void flipPage() {
        if (mCurrPage % 2 == 0) {
            if (mCurrPage > 0) mPageText.setText(" ");
            mCurrViewIndex = mOddIndex;
            mMatchImageLeft = mOddPage.findViewById(R.id.SpageImage1);
            mMatchImageCenter = mOddPage.findViewById(R.id.SpageImage2);
            mMatchImageRight = mOddPage.findViewById(R.id.SpageImage3);
            mPageImage = (ImageView) mOddPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mOddPage.findViewById(R.id.SstoryText);
            mPageFlip = (ImageButton) mOddPage.findViewById(R.id.SpageFlip);
            mSay      = (ImageButton) mOddPage.findViewById(R.id.Sspeak);
            mWord1Text = (TextView) mOddPage.findViewById(R.id.Sword1);
            mWord2Text = (TextView) mOddPage.findViewById(R.id.Sword2);
            mWord3Text = (TextView) mOddPage.findViewById(R.id.Sword3);
            mWord4Text = (TextView) mOddPage.findViewById(R.id.Sword4);
        } else {
            mCurrViewIndex = mEvenIndex;
            mMatchImageLeft = mEvenPage.findViewById(R.id.SpageImage1);
            mMatchImageCenter = mEvenPage.findViewById(R.id.SpageImage2);
            mMatchImageRight = mEvenPage.findViewById(R.id.SpageImage3);
            mPageImage = (ImageView) mEvenPage.findViewById(R.id.SpageImage);
            mPageText = (TextView) mEvenPage.findViewById(R.id.SstoryText);
            mPageFlip = (ImageButton) mEvenPage.findViewById(R.id.SpageFlip);
            mSay = (ImageButton) mEvenPage.findViewById(R.id.Sspeak);
            mWord1Text = (TextView) mEvenPage.findViewById(R.id.Sword1);
            mWord2Text = (TextView) mEvenPage.findViewById(R.id.Sword2);
            mWord3Text = (TextView) mEvenPage.findViewById(R.id.Sword3);
            mWord4Text = (TextView) mEvenPage.findViewById(R.id.Sword4);
        }
        // Ensure the buttons reflect the current states
        updateButtons();
        if (cloze_page_mode){
            updateClozeButtons();
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

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
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
                mMatchImageLeft.setImageBitmap(null);
                mMatchImageRight.setImageBitmap(null);
                mMatchImageCenter.setImageBitmap(null);

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

                    otherWordsToSpeak = rawContent.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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
        wordsToDisplay = splitRawSentence(rawSentence);


        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        //
        wordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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

                for (CASB_Content rawSentence : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = rawSentence.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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

                    otherWordsToSpeak = rawContent.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

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
        if (currLine == mLineCount){
            String[] wordsToDisplayTemp = splitRawSentence(rawSentence);
            String res1 = "";
            for(int i = 0; i < wordsToDisplayTemp.length-1; i++){
                wordsToDisplay[i] = wordsToDisplayTemp[i];
                res1 += wordsToDisplayTemp[i];
            }
            String res = "";
            String[] wordsToSpeakTemp = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
            for(int i = 0; i < wordsToSpeakTemp.length-1; i++){
                wordsToSpeak[i] = wordsToSpeakTemp[i];
                res += wordsToSpeakTemp[i];
            }

        } else {
            wordsToDisplay = splitRawSentence(rawSentence);
            wordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
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
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
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

                for (CASB_Content rawSentence : data[currPage].text[paraIndex]) {
                    otherWordsToSpeak = rawSentence.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
                    if (rawSentence.sentence == data[currPage].text[mParaCount-1][mLineCount-1].sentence) {
                        String[] wordsToSpeakTemp = new String[otherWordsToSpeak.length - 1];
                        for (int i = 0; i < otherWordsToSpeak.length - 2; i++) {
                            wordsToSpeakTemp[i] = otherWordsToSpeak[i];
                        }
                        otherWordsToSpeak = wordsToSpeakTemp;

                        if (!rawSentence.sentence.contains(" ____________") &&
                                rawSentence.sentence == data[currPage].text[mParaCount-1][mLineCount-1].sentence){
                            int len = rawSentence.sentence.length();
                            String punctuation = rawSentence.sentence.substring(len-1, len);
                            rawSentence.sentence = rawSentence.sentence.substring(0, rawSentence.sentence.lastIndexOf(" "));
                            String lastWord = rawSentence.sentence.substring(rawSentence.sentence.lastIndexOf(" ") + 1);
                            rawSentence.sentence = rawSentence.sentence + " ____________"+ punctuation;
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
        //        mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);
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
//                    if (mCurrPage>0 && numWordsCurPage==2){
//                        mParent.stopAudio();
//                    }
                    postDelayedTracker();
                }
            }
            // If the segment word is split due to apostrophes or hyphens then consume them
            // before continuing to the next segment.
            //
            else {
                mParent.post(TCONST.TRACK_NARRATION, 0);
            }
            if (mCurrPage>0 && numWordsCurPage==1 && cloze_page_mode){
                Log.d("ULANISTOPAUDIO", "postDelayedTracker: "+narrationSegment.word+" "+narrationSegment.start+" "+narrationSegment.end);
                mParent.post(TCONST.STOP_AUDIO, new Long(0));
                mParent.post(TCONST.NEXT_NODE, new Long(2000));
            }

        }
    }


    private void postDelayedTracker() {

        narrationSegment = rawNarration[utteranceNdx].segmentation[segmentNdx];
        segmentCurr = utterancePrev + narrationSegment.end;
        Log.d("ULANISTOPAUDIO", "postDelayedTracker: "+narrationSegment.word+" "+segmentCurr+" "+segmentPrev);

//        if (mCurrPage>0 && numWordsCurPage==2){
//            Log.d("ULANISTOPAUDIO", "postDelayedTracker: "+narrationSegment.word+" "+narrationSegment.start+" "+narrationSegment.end);
//            mParent.post(TCONST.TRACK_NARRATION, new Long(0));
//        }else{
        mParent.post(TCONST.TRACK_NARRATION, new Long((segmentCurr - segmentPrev) * 10));
        segmentPrev = segmentCurr;
    //}
    }

    private void postDelayedStopAudio(){
        int seg_length = rawNarration[utteranceNdx].segmentation.length;
        int lastWordStart = rawNarration[utteranceNdx].segmentation[seg_length-1].start;
        mParent.post(TCONST.STOP_AUDIO, new Long(lastWordStart * 10));
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
                    mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);

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
                    mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);
                }
            } else {
                cummulativeState = TCONST.RTC_LINECOMPLETE;
                mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);
            }
        } else
            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.NOT_LAST);

        if (mCurrLine >= mLineCount-1) {
            cummulativeState = TCONST.RTC_PARAGRAPHCOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.LAST);
        } else
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.NOT_LAST);

        if (mCurrPara >= mParaCount-1) {
            cummulativeState = TCONST.RTC_PAGECOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.LAST);
        } else{
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.NOT_LAST);
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
//        Log.d("CLOZEPAGEISSUE", "seekToPage: numLinesCurPage = "+numLinesCurPage);
//        Log.d("CLOZEPAGEISSUE", "seekToPage: mCurrLineInStory = "+mCurrLineInStory);
        this.numTotalLines+=numLinesCurPage;
        if (cloze_page_mode){
            if (clozeIndices.contains(this.numTotalLines)){
//                Log.d(TAG, "seekToPage: numtotallines = "+numTotalLines);
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
//        Log.d("ULANICLOZEPAGEISSUE", "nextPage: mcurrpage = "+mCurrPage);
        if (mCurrPage < mPageCount-1) {
            int paracount = data[mCurrPage].text.length;
            int numLinesNextPage = 0;
            for(int i = 0; i < paracount; i++){
                numLinesNextPage+=data[mCurrPage].text[i].length;
            }

//            Log.d("ULANICLOZEPAGEISSUE", "nextPage: "+data[mCurrPage].text[mCurrPara][mCurrLine]);
            //Log.d("CLOZEPAGEISSUE", "seekToPage: numLinesCurPage (mcurrpage+1) = "+numLinesNextPage);
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
        mParent.animatePageFlip(true, mCurrViewIndex);
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
            System.out.println("ULANISTOPAUDIO: incline "+mCurrPara+" "+mParaCount+" "+mCurrLine+" "+mLineCount);
//            if (mCurrPara == mParaCount && mCurrLine == mLineCount) postDelayedStopAudio();
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
//        Log.d(TAG, "incClozeLine: ");
//        Log.d("CLOZEPAGEINCLINE", "incClozeLine: ");
        // reset boot flag to
        //
        if (storyBooting) {

            storyBooting = false;
            speakOrListen();
        } else {

            mCurrLine += incr;
            System.out.println("ULANISTOPAUDIO: incclozeline "+mCurrPara+" "+mParaCount+" "+mCurrLine+" "+mLineCount);
//            if (mCurrPara == mParaCount && mCurrLine == mLineCount) postDelayedStopAudio();
//            Log.d("CLOZEPAGEINCLINE", "incClozeLine: number of lines left = "+numLinesCountdown);

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
        //
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
        String DATASOURCEPATH  = "sdcard/robotutor_assets/assets/audio/sw/cmu/xprize/story_reading/generic_questions/";
        File dir = new File(DATASOURCEPATH);
        File[] allfiles = dir.listFiles();
        File[] files = new File[15];
        int count = 0;
        for (File file : allfiles){
            if (file.getName().contains(" ") && !file.getName().contains("5 second pause")) {
                files[count] = file;
                count++;
            }
        }
        Random rand = new Random();
        File file = files[rand.nextInt(files.length)];
        this.curGenericQuestion = "generic_questions/"+file.getName();
        mParent.publishValue(TCONST.RTC_QUESTION_AUDIO, this.curGenericQuestion);
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

    private void printArray(List<Integer> a){
        for (Integer s : a){
            System.out.println(s);
        }
    }

    @Override
    public void displayClozeQuestion(){
//        Log.d("ULANICLOZEPAGEISSUE", "displayClozeQuestion: mCurrLineInStory = "+mCurrLineInStory+" mcurrpage = "+mCurrPage+" "+data[mCurrPage].text[mCurrPara]);
//        this.clozeQuestion = questions[mCurrLineInStory-1];
        ArrayList<String> nonsensical = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.nonsensical));
        ArrayList<String> ungrammatical = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.ungrammatical));
        ArrayList<String> plausible = new ArrayList<>(Arrays.asList(clozeQuestion.distractor.plausible));
//        TCONST.TARGET = clozeQuestion.target;
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
        mWord1Text.setVisibility(View.VISIBLE);
        mWord1Text.bringToFront();
        mWord2Text.setText(Html.fromHtml(finalOrder.get(1)));
        mWord2Text.setVisibility(View.VISIBLE);
        mWord2Text.bringToFront();
        mWord3Text.setText(Html.fromHtml(finalOrder.get(2)));
        mWord3Text.setVisibility(View.VISIBLE);
        mWord3Text.bringToFront();
        mWord4Text.setText(Html.fromHtml(finalOrder.get(3)));
        mWord4Text.setVisibility(View.VISIBLE);
        mWord4Text.bringToFront();
        if(this.isClozePage) {
//            Log.d(TAG, "updateClozeButtons: isclozepage");
            setButtonState(mWord1Text, "ENABLE");
            setButtonState(mWord1Text, "SHOW");
            setButtonState(mWord2Text, "ENABLE");
            setButtonState(mWord2Text, "SHOW");
            setButtonState(mWord3Text, "ENABLE");
            setButtonState(mWord3Text, "SHOW");
            setButtonState(mWord4Text, "ENABLE");
            setButtonState(mWord4Text, "SHOW");
        }
        mParent.animatePageFlip(true, mCurrViewIndex);
    }


    @Override
    public void displayPictureMatching(){
        InputStream in1;
        InputStream in2;
        InputStream in3;
        ArrayList<String> imgs = new ArrayList<>();
        for (int i = mCurrPage+1; i < data.length-1; i++){
            if (!imgs.contains(data[i].image)){
                imgs.add(data[i].image);
            }
        }
        Collections.shuffle(imgs);
        String randImg1 = imgs.get(0);
        String randImg2 = imgs.get(1);
        try {
            if (assetLocation.equals(TCONST.EXTERN)) {
//                Log.d(TCONST.DEBUG_STORY_TAG, "loading image " + mAsset + data[mCurrPage].image+" "+mCurrPage);
                in1 = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image
                in2 = new FileInputStream(mAsset + randImg1); // ZZZ load image
                in3 = new FileInputStream(mAsset + randImg2);
            } else {
//                Log.d(TCONST.DEBUG_STORY_TAG, "loading image from asset" + mAsset + data[mCurrPage].image);
                in1 = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
                in2 = JSON_Helper.assetManager().open(mAsset + randImg1); // ZZZ load image
                in3 = JSON_Helper.assetManager().open(mAsset + randImg2);
            }
            int targetIdx = getRandomNumberInRange(0, 2);
            this.picmatch_answer = targetIdx;
            if (targetIdx == 2){
                mMatchImageRight.setImageBitmap(BitmapFactory.decodeStream(in1));
                mMatchImageCenter.setImageBitmap(BitmapFactory.decodeStream(in2));
                mMatchImageLeft.setImageBitmap(BitmapFactory.decodeStream(in3));
            } else if (targetIdx == 1){
                mMatchImageRight.setImageBitmap(BitmapFactory.decodeStream(in2));
                mMatchImageCenter.setImageBitmap(BitmapFactory.decodeStream(in1));
                mMatchImageLeft.setImageBitmap(BitmapFactory.decodeStream(in3));
            } else {
                mMatchImageRight.setImageBitmap(BitmapFactory.decodeStream(in2));
                mMatchImageCenter.setImageBitmap(BitmapFactory.decodeStream(in3));
                mMatchImageLeft.setImageBitmap(BitmapFactory.decodeStream(in1));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mMatchImageLeft.bringToFront();
        mMatchImageCenter.bringToFront();
        mMatchImageRight.bringToFront();
        show_image_options = true;
        updateImageButtons();
        mParent.animatePageFlip(true, mCurrViewIndex);
    }

    @Override
    public void hasClozeDistractor(){
//        Log.d("ULANI", "hasClozeDistractor: numTotalLines = "+this.numTotalLines + " mCurrLineInStory = "+mCurrLineInStory+" isclozepage = "+isClozePage);
//        Log.d("ULANI", "hasClozeDistractor: cloze_page_mode = "+cloze_page_mode+" mcurrpara = "+mCurrPara+" mparacount ="+mParaCount+" mcurrpage = "+mCurrPage+" mpagecount = "+mPageCount);
        if (mCurrPage <= mPageCount-1) {
            if (isClozePage && mCurrPara >= mParaCount-1){
//                Log.d(TAG, "hasClozeDistractor: clozeIndices contains numTotalLines");
                mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.TRUE);
                mParent.publishValue(TCONST.SHOW_PICMATCH, TCONST.FALSE);
            } else {
                mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
                mParent.publishValue(TCONST.SHOW_PICMATCH, TCONST.FALSE);
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
            mParent.publishValue(TCONST.SHOW_PICMATCH, TCONST.TRUE);
            mParent.publishValue(TCONST.SHOW_CLOZE, TCONST.FALSE);
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

}
