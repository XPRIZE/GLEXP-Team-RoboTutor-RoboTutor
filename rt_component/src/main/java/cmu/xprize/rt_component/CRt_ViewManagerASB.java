//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

package cmu.xprize.rt_component;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import cmu.xprize.util.CPersonaObservable;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.ListenerBase;


/**
 * This view manager provides student UX for the African Story Book format used
 * in the CMU XPrize submission
 */
public class CRt_ViewManagerASB implements ICRt_ViewManager, ILoadableObject {

    private Context                 mContext;

    private ListenerBase            mListener;
    private IVManListener           mOwner;
    private String                  mAsset;

    private CRt_Component           mParent;
    private ImageView               mPageImage;
    private TextView                mPageText;

    private ImageButton             mPageFlip;
    private ImageButton             mSay;

    // ASB even odd page management

    private ViewGroup               mOddPage;
    private ViewGroup               mEvenPage;

    private int                     mOddIndex;
    private int                     mEvenIndex;
    private int                     mCurrViewIndex;

    // state for the current story - African Story Book

    private String                  mCurrHighlight = "";
    private int                     mCurrPage;
    private boolean                 mLastPage;
    private int                     mCurrPara;
    private int                     mCurrLine;
    private int                     mCurrWord;
    private int                     mHearWord;                          // The expected location of mCurrWord in heardWords - see PLRT version of onUpdate below

    private String                  speakButtonEnable = "";
    private String                  speakButtonShow   = "";
    private String                  pageButtonEnable  = "";
    private String                  pageButtonShow    = "";

    private int                     mPageCount;
    private int                     mParaCount;
    private int                     mLineCount;
    private int                     mWordCount;
    private int                     attemptNum = 1;

    private String                  wordsToDisplay[];                    // current sentence words to display - contain punctuation
    private String                  wordsToSpeak[];                      // current sentence words to hear
    private ArrayList<String>       wordsToListenFor;                    // current sentence words to build language model

    private String                  rawSentence;                         //currently displayed sentence that need to be recognized

    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";
    private String                  futureSentencesFmtd    = "";
    private String                  futureSentences        = "";
    private boolean                 showFutureContent      = true;
    private boolean                 listenFutureContent    = false;

    private ArrayList<String>       wordsSpoken;
    private ArrayList<String>       futureSpoken;


    // json loadable

    public String        license;
    public String        story_name;
    public String        authors;
    public String        illustrators;
    public String        language;
    public String        status;
    public String        copyright;
    public String        titleimage;

    public String        parser;
    public CASB_data     data[];


    static final String TAG = "CRt_ViewManagerASB";



    public CRt_ViewManagerASB(CRt_Component parent, ListenerBase listener) {

        mParent = parent;
        mContext = mParent.getContext();

        mOddPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.asb_oddpage, null);
        mEvenPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.asb_evenpage, null);

        mOddPage.setVisibility(View.GONE);
        mEvenPage.setVisibility(View.GONE);

        mOddIndex  = mParent.addPage(mOddPage );
        mEvenIndex = mParent.addPage(mEvenPage );

        mListener = listener;
    }




    /**
     *
     * @param owner
     * @param assetPath
     */
    public void initStory(IVManListener owner, String assetPath) {

        mOwner = owner;
        mAsset = assetPath;

        seekToPage(TCONST.ZERO);

        //TODO: CHECK
        mParent.animatePageFlip(true,mCurrViewIndex);
    }


    @Override
    public void onDestroy() {
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

            switch(command) {

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

        switch(command) {

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

        switch(command) {

            case "ENABLE":
                Log.i("ASB", "ENABLE");
                pageButtonEnable = command;
                break;
            case "DISABLE":
                Log.i("ASB", "DISABLE");
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
        //
        setButtonState(mPageFlip, pageButtonEnable);
        setButtonState(mPageFlip, pageButtonShow);

        setButtonState(mSay, speakButtonEnable);
        setButtonState(mSay, speakButtonShow);

        mPageFlip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mParent.onButtonClick(TCONST.PAGEFLIP_BUTTON);
            }
        });

        mSay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mParent.onButtonClick(TCONST.SPEAK_BUTTON);
            }
        });
    }

    /**
     *  This configures the target display components to be populated with data.
     *
     *  mPageImage - mPageText
     *
     */
    public void flipPage() {

        // Note that we use zero based indexing so page zero is first page - i.e. odd
        //
        if(mCurrPage % 2 == 0) {

            mCurrViewIndex = mOddIndex;
            mPageImage = (ImageView) mOddPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mOddPage.findViewById(R.id.SstoryText);

            mPageFlip = (ImageButton) mOddPage.findViewById(R.id.SpageFlip);
            mSay      = (ImageButton) mOddPage.findViewById(R.id.Sspeak);
        }
        else {

            mCurrViewIndex = mEvenIndex;
            mPageImage = (ImageView) mEvenPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mEvenPage.findViewById(R.id.SstoryText);

            mPageFlip = (ImageButton) mEvenPage.findViewById(R.id.SpageFlip);
            mSay      = (ImageButton) mEvenPage.findViewById(R.id.Sspeak);
        }

        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    private void configurePageImage() {

        try {
            InputStream in = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image);

            mPageImage.setImageBitmap(BitmapFactory.decodeStream(in));

        } catch (IOException e) {
            e.printStackTrace();
        }
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


        // Optimization - Skip If not seeking to the very first line
        //
        // Otherwise create 2 things:
        //
        // 1. A visually formatted representation of the words already spoken
        // 2. A list of words already spoken - for use in the Sphinx language model
        //
        if(currPara > 0 || currLine > 0) {

            // First generate all completed paragraphs in their entirity
            //
            for(int paraIndex = 0 ; paraIndex < currPara ; paraIndex++) {

                for (String rawSentence : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        wordsSpoken.add(word);

                    completedSentences += rawSentence;
                }
                if(paraIndex < currPara)
                    completedSentences += "<br><br>";
            }

            // Then generate all completed sentences from the current paragraph
            //
            for(int lineIndex = 0 ; lineIndex <  currLine ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex];
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    wordsSpoken.add(word);

                completedSentences += rawSentence;
            }

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

        rawSentence = data[currPage].text[currPara][currLine];

        // Words that are used to build the display text - include punctuation etc.
        // But are in one-to-one correspondance with the wordsToSpeak
        // NOTE: wordsToSpeak is used in generating the active listening model
        // so it must reflect the current sentence!
        //
        wordsToDisplay = rawSentence.trim().split("\\s+");

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
        if(showFutureContent) {

            // Generate all remaining sentences in the current paragraph
            //
            // Then generate all future sentences from the current paragraph
            //
            for(int lineIndex = currLine+1 ; lineIndex <  mLineCount ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex];
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    futureSpoken.add(word);

                futureSentences += rawSentence;
            }

            // First generate all completed paragraphs in their entirity
            //
            for(int paraIndex = currPara+1 ; paraIndex < mParaCount ; paraIndex++) {

                // Add the paragraph break if not at the end
                //
                futureSentences += "<br><br>";

                for (String rawSentence : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        futureSpoken.add(word);

                    futureSentences += rawSentence;
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

        // Listen for the target word
        //
        startListening();

    }


    /**
     * Push the state out to the tutor domain.
     *
     */
    private void publishStateValues() {

        String cummulativeState = TCONST.RTC_CLEAR;

        // Set the scriptable flag indicating the current state.
        //
        if (mCurrWord >= mWordCount) {
            cummulativeState = TCONST.RTC_LINECOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.NOT_LAST);

        if (mCurrLine >= mLineCount-1) {
            cummulativeState = TCONST.RTC_PARAGRAPHCOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.NOT_LAST);

        if (mCurrPara >= mParaCount-1) {
            cummulativeState = TCONST.RTC_PAGECOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.NOT_LAST);

        if (mCurrPage >= mPageCount-1) {
            cummulativeState = TCONST.RTC_STORYCMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.NOT_LAST);

        // Publish the cumulative state out to the scripting scope in the tutor
        //
        mParent.publishValue(TCONST.RTC_VAR_STATE, cummulativeState);
    }


    /**
     *  Configure for specific Page
     *  Assumes current story
     *
     * @param pageIndex
     */
    @Override
    public void seekToPage(int pageIndex) {

        mCurrPage = pageIndex;

        if(mCurrPage > mPageCount-1) mCurrPage = mPageCount-1;
        if(mCurrPage < TCONST.ZERO)  mCurrPage = TCONST.ZERO;

        incPage(TCONST.ZERO);
    }

    @Override
    public void nextPage() {

        if(mCurrPage < mPageCount-1) {
            incPage(TCONST.INCR);
        }

        // Actually do the page animation
        //
        mParent.animatePageFlip(true, mCurrViewIndex);
    }
    @Override
    public void prevPage() {

        if(mCurrPage > 0) {
            incPage(TCONST.DECR);
        }

        //TODO: CHECK
        mParent.animatePageFlip(false, mCurrViewIndex);
    }

    private void incPage(int direction) {

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


    /**
     *  Configure for specific Paragraph
     *  Assumes current page
     *
     * @param paraIndex
     */
    @Override
    public void seekToParagraph(int paraIndex) {

        mCurrPara = paraIndex;

        if(mCurrPara > mParaCount-1) mCurrPara = mParaCount-1;
        if(mCurrPara < TCONST.ZERO)  mCurrPara = TCONST.ZERO;

        incPara(TCONST.ZERO);
    }

    @Override
    public void nextPara() {

        if(mCurrPara < mParaCount-1) {
            incPara(TCONST.INCR);
        }
    }
    @Override
    public void prevPara() {

        if(mCurrPara > 0) {
            incPara(TCONST.DECR);
        }
    }

    // NOTE: we reset mCurrLine and mCurrWord
    private void incPara(int incr) {

        mCurrPara += incr;

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, TCONST.ZERO, TCONST.ZERO);
    }


    /**
     *  Configure for specific line
     *  Assumes current page and paragraph
     *
     * @param lineIndex
     */
    @Override
    public void seekToLine(int lineIndex) {

        mCurrLine = lineIndex;

        if(mCurrLine > mLineCount-1) mCurrLine = mLineCount-1;
        if(mCurrLine < TCONST.ZERO)  mCurrLine = TCONST.ZERO;

        incLine(TCONST.ZERO);
    }

    @Override
    public void nextLine() {

        if(mCurrLine < mLineCount-1) {
            incLine(TCONST.INCR);
        }
    }
    @Override
    public void prevLine() {

        if(mCurrLine > 0 ) {
            incLine(TCONST.DECR);
        }
    }

    // NOTE: we reset mCurrWord
    private void incLine(int incr) {

        mCurrLine += incr;

        // Update the state vars
        //
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
        mHearWord = 0;

        if(mCurrWord > mWordCount-1) mCurrWord = mWordCount-1;
        if(mCurrWord < TCONST.ZERO)  mCurrWord = TCONST.ZERO;

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, wordIndex);

        incWord(TCONST.ZERO);

        // Start listening from the new position
        //
        startListening();
    }

    @Override
    public void nextWord() {

        if(mCurrWord < mWordCount) {
            incWord(TCONST.INCR);
        }
    }
    @Override
    public void prevWord() {

        if(mCurrWord > 0) {
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

        mCurrWord += incr;

        // Reset the highlight
        setHighLight(TCONST.EMPTY, false);

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
        startListening();
    }

    private void startListening() {

        // We allow the user to say any of the onscreen words but set the priority order of how we would like them matched
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
        if(listenFutureContent) {
            for (String word : futureSpoken) {
                wordsToListenFor.add(word);
            }
        }

        // Start listening
        //
        if (mListener != null) {

            // reset the relative position of mCurrWord in the incoming PLRT heardWords array
            mHearWord = 0;
            mListener.reInitializeListener(true);
            mListener.updateNextWordIndex(mHearWord);

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

        mCurrHighlight = highlight;

        // Update the sentence display
        //
        if(update)
            UpdateDisplay();
    }


    /**
     *  Update the displayed sentence
     */
    private void UpdateDisplay() {

        String fmtSentence = "";

        for (int i = 0; i < wordsToDisplay.length; i++) {

            String styledWord = wordsToDisplay[i];                           // default plain

            if(i < mCurrWord) {
                styledWord = "<font color='#00B600'>" + styledWord + "</font>";
            }

            if (i == mCurrWord) {// style the next expected word

                if(!mCurrHighlight.equals(TCONST.EMPTY))
                    styledWord = "<font color='"+ mCurrHighlight + "'>" + styledWord + "</font>";

                styledWord = "<u>" + styledWord + "</u>";
            }

            fmtSentence += styledWord + " ";
        }

        // Generate the text to be displayed
        //
        String content = completedSentencesFmtd + fmtSentence;

        if(showFutureContent)
            content += futureSentencesFmtd;

        mPageText.setText(Html.fromHtml(content));

        broadcastActiveTextPos(mPageText, wordsToDisplay);

        // Publish the current word / sentence / remaining words for use in scripts
        //
        if(mCurrWord < wordsToSpeak.length) {
            mParent.publishValue(TCONST.RTC_VAR_WORDVALUE, wordsToSpeak[mCurrWord]);

            String remaining[] = Arrays.copyOfRange(wordsToSpeak, mCurrWord, wordsToSpeak.length);

            mParent.publishValue(TCONST.RTC_VAR_REMAINING, TextUtils.join(" ", remaining));
            mParent.publishValue(TCONST.RTC_VAR_SENTENCE,  TextUtils.join(" ", wordsToSpeak));
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

            if(layout != null) {

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
            Log.d(TAG, "getActiveTextPos: " + e.toString());
        }

        return point;
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
                logString += heardWords[i].hypWord.toLowerCase() + ":" + heardWords[i].iSentenceWord + " | ";
            }
            Log.i("ASR", "New HypSet: " + logString);

            while (mHearWord < heardWords.length) {

                if (wordsToSpeak[mCurrWord].equals(heardWords[mHearWord].hypWord)) {

                    nextWord();
                    mHearWord++;

                    mListener.updateNextWordIndex(mHearWord);

                    Log.i("ASR", "RIGHT");
                    attemptNum = 0;
                    result = true;

                } else {

                    mListener.setPauseListener(true);

                    Log.i("ASR", "WRONG");
                    mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum++);
                    result = false;
                    break;
                }
            }

            // Publish the outcome
            mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum);
            mParent.UpdateValue(result);

            mParent.onASREvent(TCONST.RECOGNITION_EVENT);
        }
        catch(Exception e) {
            // TODO: This seem sto be because the activity is not destroyed and the ASR continues
            Log.d("ASR", "onUpdate Fault: " + e);
        }
    }


    /**
     * This is where incoming JSGF ASR data would be processed.
     *
     *  TODO: check if it is possible for the hypothesis to chamge between last update and final hyp
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

}


