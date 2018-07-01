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

package cmu.xprize.rt_component;

import android.graphics.PointF;
import android.os.Handler;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cmu.xprize.util.CPersonaObservable;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.ListenerBase;


/**
 *   This view manager emulates the same student interaction that was present in the
 *   initial release of the MARi reading tutor.
 */
public class CRt_ViewManagerMari implements ICRt_ViewManager, ILoadableObject {

    private ListenerBase            mListener;
    private IVManListener           mOwner;
    private TextView                mPageText;

    // state for the current sentence
    private int                     currentIndex           = -1;            // current sentence index in storyName, -1 if unset
    private int                     storyPart              = 0;
    private boolean                 endOfStory             = false;

    private int                     completeSentenceIndex  = 0;
    private String                  sentenceWords[];                        // current sentence words to hear
    private int                     expectedWordIndex      = 0;             // index of expected next word in sentence
    private static int[]            creditLevel = null;                     // per-word credit levelFolder according to current hyp
    private boolean                 changingSentence       = false;

    private ArrayList<String>       sentences              = null;          //list of sentences of the given passage
    private String                  currentSentence;                        //currently displayed sentence that need to be recognized
    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";
    private String                  assetLocation;

    private IVManListener           _publishListener;

    // json loadable
    public String        parser;
    public String        data[];
    public CMari_Data    rhymes[];


    static final String TAG = "CRt_ViewManagerMari";


    public CRt_ViewManagerMari(CRt_Component parent, ListenerBase listener) {

        mPageText = (TextView) parent.findViewById(R.id.SstoryText);
        mListener = listener;
    }


    @Override
    public void initStory(IVManListener owner, String assetPath, String location) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void startStory() {

    }

    public void setSpeakButton(String command) {
    }

    public void setPageFlipButton(String command) {
    }

    @Override
    public void seekToPage(int pageIndex) {

    }

    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    @Override
    public void seekToParagraph(int paraIndex) {

    }

    @Override
    public void nextPara() {

    }

    @Override
    public void prevPara() {

    }

    @Override
    public void seekToLine(int lineIndex) {

    }

    @Override
    public void echoLine() {

    }

    @Override
    public void parrotLine() {
    }

    @Override
    public void nextLine() {

    }

    @Override
    public void prevLine() {

    }

    @Override
    public void seekToWord(int wordIndex) {

    }

    public void setPublishListener(IVManListener publishListener) {
        _publishListener = publishListener;
    }

    @Override
    public void nextWord() {

    }

    @Override
    public void prevWord() {

    }

    @Override
    public void setHighLight(String highlight, boolean update) {

    }

    public boolean loadStory(String storyURL) {


        return true;
    }


    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch(command) {
        }
    }


    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        String logString = "";
        for (int i = 0; i < heardWords.length; i++) {
            logString += heardWords[i].hypWord.toLowerCase() + ":" + heardWords[i].iSentenceWord + " | ";
        }
        Log.i("ASR", "New HypSet: "  + logString);


        // TODO: Change to setPauseRecognizer to flush the queue should obviate the need for
        // changingSentence test.  Validate this is the case.
        //
        // The recongnizer runs asynchronously so ensure we don't process any
        // hypotheses while we are changing sentences otherwise it can skip a sentence.
        // This is because nextSentence is also called asynchronously
        //
        if(changingSentence || finalResult) {
            Log.d("ASR", "Ignoring Hypothesis");
            return;
        }

        updateSentence(heardWords);             // update current sentence state and redraw

        // move on if all words in current sentence have been read
        if(sentenceComplete()) {

            changingSentence = true;
            mListener.setPauseListener(true);

            // schedule advance after short delay to allow time to see last word credited on screen
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //nextSentence(mOwner, null);
                    changingSentence = false;
                }
            }, 100);
        }
    }

    @Override
    public void onUpdate(String[] heardWords, boolean finalResult) {

    }

    @Override
    public void continueListening() {

    }


    /**
     * @param heardWords Update the sentence credit levelFolder with the credit levelFolder of the heard words
     */
    private void updateSentence(ListenerBase.HeardWord[] heardWords) {

        Log.d("ASR", "New Hypothesis Set:");

        if (heardWords.length >= 1) {

            // Reset partial credit levelFolder of sentence words
            //
            for (int i = 0; i < creditLevel.length; i++) {

                // don't touch words with permanent credit
                if (creditLevel[i] != ListenerBase.HeardWord.MATCH_EXACT)
                    creditLevel[i]  = ListenerBase.HeardWord.MATCH_UNKNOWN;
            }

            for (ListenerBase.HeardWord hw : heardWords) {

                Log.d("ASR", "Heard:" + hw.hypWord);

                // assign the highest credit found among all hypothesis words
                //
                if (hw.matchLevel >= creditLevel[hw.iSentenceWord]) {
                    creditLevel[hw.iSentenceWord] = hw.matchLevel;
                }
            }

            expectedWordIndex = getFirstUncreditedWord();

            // Tell the listener when to stop matching words.  We don't want to match words
            // past the current expected word or they will be highlighted
            // This is a MARi induced constraint
            // TODO: make it so we don't need this - use matched past the next word to flag
            // a missed word
            //
            mListener.updateNextWordIndex(expectedWordIndex);

            // Update the sentence text display to show credit, expected word
            //
            UpdateSentenceDisplay();
        }
    }


    /**
     * Update the displayed sentence based on the newly calculated credit levelFolder
     */
    private void UpdateSentenceDisplay() {

        String fmtSentence = "";
        String[] words = currentSentence.split("\\s+");

        for (int i = 0; i < words.length; i++) {

            String styledWord = words[i];                           // default plain

            // show credit status with color
            if (creditLevel[i] == ListenerBase.HeardWord.MATCH_EXACT) {     // match found, but not credited

                styledWord = "<font color='#00B600'>" + styledWord + "</font>";

            } else if (creditLevel[i] == ListenerBase.HeardWord.MATCH_MISCUE) {  // wrongly read

                styledWord = "<font color='red'>" + styledWord + "</font>";

            } else if (creditLevel[i] == ListenerBase.HeardWord.MATCH_TRUNCATION) { //  heard only half the word

            } else {

            }

            if (i == expectedWordIndex) {// style the next expected word
                styledWord.replace("<u>", "");
                styledWord.replace("</u>", "");
                styledWord = "<u>" + styledWord + "</u>";

                //  Publish the word to the component so it can set a scritable varable
                //_publishListener.publishTargetWord(styledWord);
            }

            fmtSentence += styledWord + " ";

        }
        fmtSentence += "<br>";

        mPageText.setText(Html.fromHtml(completedSentencesFmtd + fmtSentence));

        updateCompletedSentence();

        broadcastActiveTextPos(mPageText, words);
    }


    /**
     * Broadcast current target work position for persona eye tracking.
     *
     * Notes:
     * XML storyName source text must be entered without extra space or linebreaks.
     *
     *     <selectlevel levelFolder="1">
     *          <storyName storyName="1">
     *              <part part="1">Uninterrupted text</part>
     *          </storyName>
     *
     * @param text
     * @param words
     * @return
     */
    private PointF broadcastActiveTextPos(TextView text, String[] words){

        PointF point = new PointF(0,0);
        int charPos  = 0;
        int maxPos;

        if(expectedWordIndex >= 0) {

            for (int i1 = 0; i1 < expectedWordIndex; i1++) {
                charPos += words[i1].length() + 1;
            }
            charPos += words[expectedWordIndex].length()-1;
            charPos  = completedSentences.length() + charPos;

            // Note that sending a value greater than maxPos will corrupt the textView
            //
            maxPos  = text.getText().length();
            charPos = (charPos > maxPos) ? maxPos : charPos;

            try {
                Layout layout = text.getLayout();

                point.x = layout.getPrimaryHorizontal(charPos);

                int y = layout.getLineForOffset(charPos);
                point.y = layout.getLineBottom(y);

            } catch (Exception exception) {
                Log.d(TAG, "getActiveTextPos: " + exception.toString());
            }

            CPersonaObservable.broadcastLocation(text, TCONST.LOOKAT, point);
        }
        return point;
    }


    /**
     * Get the first uncredited word of the current sentence
     *
     * @return index of uncredited word
     */
    private int getFirstUncreditedWord() {

        int result = 0;

        for (int i = 0; i < creditLevel.length; i++) {

            if (creditLevel[i] != ListenerBase.HeardWord.MATCH_EXACT) {
                result = i;
                break;
            }
        }
        return result;
    }



    /**
     * to make auto scroll for the sentences
     */
    public void updateCompletedSentence() {

        int lastVisibleLineNumber = 0;
        int totalNoOfLines = 1;

        int height = mPageText.getHeight();
        int scrollY = mPageText.getScrollY();
        Layout layout = mPageText.getLayout();

        if(layout != null) {

            lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);
            totalNoOfLines = mPageText.getLineCount() - 1;
        }
        if (lastVisibleLineNumber < totalNoOfLines) {

            completeSentenceIndex = currentIndex;
            completedSentencesFmtd = "";
            completedSentences = "";
        }
    }


    /**
     *
     * @param index
     * @return
     */
    public boolean isWordCredited(int index) {
        return index >= 0 &&
                (index == 0 || (creditLevel[index - 1] == ListenerBase.HeardWord.MATCH_EXACT));
    }


    /**
     * Get number of exact matches
     * @return
     */
    public int getNumWordsCredited() {

        int n = 0;

        for (int cl : creditLevel) {

            if (cl == ListenerBase.HeardWord.MATCH_EXACT)
                n += 1;
        }
        return n;
    }


    public boolean sentenceComplete() {
        return getNumWordsCredited() >= sentenceWords.length;
    }


    @Override
    public boolean endOfData() {
        return endOfStory;
    }

    @Override
    public void onUpdate(String[] heardWords) {

    }

//    /**
//     * Show the next available sentence to the user
//     */
//    @Override
//    public void nextSentence(IVManListener callback, String assetPath) {
//
//        if(mListener != null)
//            mListener.deleteLogFiles();
//
//        mOwner = callback;
//        switchSentence(currentIndex + 1);      // for now just loop around single storyName
//    }


    /**
     * Initialize mListener with the specified sentence
     *
     * @param index index of the sentence that needs to be initialized
     */
    private boolean switchSentence(int index) {

        boolean result = true;

        // We've exhausted all the sentences in the storyName
        if (index == sentences.size()) {

            Log.d("ASR", "End of Story");
            // Kill off the mListener.
            // When this returns the recognizerThread is dead and the mic
            // has been disconnected.
            if (mListener != null)
                mListener.stop();

            endOfStory = true;
            result =  false;
        }
        else {
            if (index > 0) {  // to set grey color for the finished sentence
                completedSentencesFmtd = "<font color='grey'>";
                completedSentences = "";
                for (int i = completeSentenceIndex; i < index; i++) {
                    completedSentences += sentences.get(i);
                    completedSentences += ". ";
                }
                completedSentencesFmtd += completedSentences;
                completedSentencesFmtd += "</font>";
            }
            currentIndex = index % sentences.size();
            currentSentence = sentences.get(currentIndex).trim() + ".";

            // get array or words to hear for new sentence
            sentenceWords = ListenerBase.textToWords(currentSentence);

            // reset all aggregate hyp info for new sentence
            // fills default value 0 = MATCH_UNKNOWN
            creditLevel = new int[sentenceWords.length];
            expectedWordIndex = 0;

            // show sentence and start listening for it
            // If we are starting from the beginning of the sentence then end any current sentence
            if (mListener != null) {
                mListener.reInitializeListener(true);
                mListener.listenFor(sentenceWords, 0);
                mListener.setPauseListener(false);
            }

//            _publishListener.publishTargetSentence(currentSentence);
//            _publishListener.publishTargetWordIndex(expectedWordIndex);

            UpdateSentenceDisplay();
        }

        return result;
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

        Pattern pattern = Pattern.compile("[0-9]{2,}(,*\\d*)");
        Matcher matcher = pattern.matcher(data[storyPart]);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(0).replaceAll(",", ""));
            matcher.appendReplacement(sb, Num2Word.transform(number, "LANG_EN"));
        }
        matcher.appendTail(sb);
        System.out.println(sb.toString());
        sentences = new ArrayList<String>(Arrays.asList(sb.toString().split("\\.")));
    }
}



