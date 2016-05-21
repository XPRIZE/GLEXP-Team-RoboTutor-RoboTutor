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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    private int                     mPageCount;
    private int                     mParaCount;
    private int                     mLineCount;
    private int                     mWordCount;

    private String                  wordsToDisplay[];                    // current sentence words to display - contain punctuation
    private String                  wordsToSpeak[];                      // current sentence words to hear
    private ArrayList<String>       wordsToListenFor;                    // current sentence words to build language model

    private String                  rawSentence;                         //currently displayed sentence that need to be recognized

    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";
    private ArrayList<String>       wordsSpoken;


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

        startListening();

        //TODO: CHECK
        mParent.flipPage(true,mCurrViewIndex);
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
        }
        else {

            mCurrViewIndex = mEvenIndex;
            mPageImage = (ImageView) mEvenPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mEvenPage.findViewById(R.id.SstoryText);
        }
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
    private void setCurrentState(int currPage, int currPara, int currLine) {

        completedSentencesFmtd = "";
        wordsSpoken            = new ArrayList<>();

        // If not seeking to the very first line
        //
        if(currPara > 0 || currLine > 0) {

            for(int paraIndex = 0 ; paraIndex < currPara ; paraIndex++) {

                for(int lineIndex = 0 ; lineIndex <  currLine ; lineIndex++) {

                    rawSentence = data[currPage].text[currPara][currLine];
                    wordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : wordsToSpeak)
                        wordsSpoken.add(word);

                    completedSentences += rawSentence;
                }

                if(paraIndex < currPara)
                    completedSentences += "<br><br>";
            }

            completedSentencesFmtd = "<font color='grey'>";
            completedSentencesFmtd += completedSentences;
            completedSentencesFmtd += "</font>";
        }

        mCurrPage = currPage;
        mCurrPara = currPara;
        mCurrLine = currLine;

        mPageCount = data.length;
        mParaCount = data[currPage].text.length;
        mLineCount = data[currPage].text[currPara].length;

        rawSentence = data[currPage].text[currPara][currLine];

        // Words that are used to build the display text - include punctuation etc.
        // But are in one-to-one correspondance with the wordsToSpeak
        //
        wordsToDisplay = rawSentence.trim().split("\\s+");

        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        //
        wordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

        mCurrWord  = TCONST.ZERO;
        mWordCount = wordsToSpeak.length;

        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();
    }


    /**
     * Push the state out to the tutor domain.
     *
     */
    private void publishStateValues() {

        String cummulativeState = TCONST.RTC_CLEAR;

        // Set the scriptable flag indicating the current state.
        //
        if (mCurrWord >= mWordCount-1) {
            cummulativeState = TCONST.RTC_LINECOMPLETE;
            mParent.publishValue(TCONST.RTC__VAR_WORDSTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC__VAR_WORDSTATE, TCONST.NOT_LAST);

        if (mCurrLine >= mLineCount) {
            cummulativeState = TCONST.RTC_PARAGRAPHCOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.NOT_LAST);

        if (mCurrPara >= mParaCount) {
            cummulativeState = TCONST.RTC_PAGECOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.LAST);
        }
        else
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.NOT_LAST);

        if (mCurrPage >= mPageCount) {
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

        //TODO: CHECK
        mParent.flipPage(true, mCurrViewIndex);
    }
    @Override
    public void prevPage() {

        if(mCurrPage > 0) {
            incPage(TCONST.DECR);
        }

        //TODO: CHECK
        mParent.flipPage(false, mCurrViewIndex);
    }

    private void incPage(int direction) {

        mCurrPage += direction;

        // Update the state vars
        //
        setCurrentState(mCurrPage, mCurrPara, mCurrLine);

        // This configures the target display components to be populated with data.
        // mPageImage - mPageText
        //
        flipPage();

        configurePageImage();
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

    private void incPara(int incr) {

        mCurrPara += incr;

        // Update the state vars
        //
        setCurrentState(mCurrPage, mCurrPara, mCurrLine);
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

    private void incLine(int incr) {

        mCurrLine += incr;

        // Update the state vars
        //
        setCurrentState(mCurrPage, mCurrPara, mCurrLine);
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

        if(mCurrWord > mWordCount-1) mCurrWord = mWordCount-1;
        if(mCurrWord < TCONST.ZERO)  mCurrWord = TCONST.ZERO;

        // Update the state vars
        //
        setCurrentState(mCurrPage, mCurrPara, mCurrLine);

        incWord(TCONST.ZERO);
    }

    @Override
    public void nextWord() {

        if(mCurrWord < mWordCount-1) {
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

        mCurrHighlight = TCONST.EMPTY;

        startListening();
    }


    private void startListening() {

        // Update the sentence highlighting
        //
        UpdateDisplay();

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

        // Start listening
        //
        if (mListener != null) {

            mListener.reInitializeListener(true);

            mListener.listenFor(wordsToListenFor.toArray(new String[wordsToListenFor.size()]), 0);
            mListener.setPauseListener(false);
        }
    }


    /**
     * Scipting mechanism to update target word highlight
     * @param highlight
     */
    @Override
    public void setHighLight(String highlight) {

        mCurrHighlight = highlight;

        UpdateDisplay();
    }


    /**
     *  Update the displayed sentence
     */
    private void UpdateDisplay() {

        String fmtSentence = "";

        for (int i = 0; i < wordsToDisplay.length; i++) {

            String styledWord = wordsToDisplay[i];                           // default plain

            if (i == mCurrWord) {// style the next expected word

                if(!mCurrHighlight.equals(TCONST.EMPTY))
                    styledWord = "<font color='"+ mCurrHighlight + "'>" + styledWord + "</font>";

                styledWord = "<u>" + styledWord + "</u>";
            }

            fmtSentence += styledWord + " ";
        }

        mPageText.setText(Html.fromHtml(completedSentencesFmtd + fmtSentence));

        broadcastActiveTextPos(mPageText, wordsToDisplay);
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
     * This is where the incoming ASR data is processed.
     *
     *  TODO: check if it is possible for the hypothesis to chamge between last update and final hyp
     */
    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        String logString = "";

        for (int i = 0; i < heardWords.length; i++) {
            logString += heardWords[i].hypWord.toLowerCase() + ":" + heardWords[i].iSentenceWord + " | ";
        }
        Log.i("ASR", "New HypSet: "  + logString);




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


