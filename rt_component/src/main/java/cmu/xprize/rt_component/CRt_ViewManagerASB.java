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

import android.graphics.PointF;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.util.CPersonaObservable;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.Listener;


/**
 * This view manager provides student UX for the African Story Book format used
 * in the CMU XPrize submission
 */
public class CRt_ViewManagerASB implements ICRt_ViewManager, ILoadableObject {

    private Listener                mListener;
    private TextView                mPageText;

    // state for the current sentence
    private int                     currentIndex           = 0;             // current sentence index in story, -1 if unset

    private int                     completeSentenceIndex  = 0;
    private String                  sentenceWords[];                        // current sentence words to hear
    private int                     expectedWordIndex      = 0;             // index of expected next word in sentence
    private static int[]            creditLevel            = null;          // per-word credit level according to current hyp

    private ArrayList<String>       sentences              = null;          //list of sentences of the given passage
    private String                  currentSentence;                        //currently displayed sentence that need to be recognized
    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";

    private IVManListener           _publishListener;

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


    static final String TAG = "CRt_ViewManagerMari";


    public CRt_ViewManagerASB(CRt_Component parent) {

        mPageText = (TextView) parent.findViewById(R.id.SstoryText);
    }


    public void setPublishListener(IVManListener publishListener) {
        _publishListener = publishListener;
    }


    /**
     * Get the first not credited word of the current sentence
     *
     * @return index of uncredited word
     */
    private int getFirstUncreditedWord() {
        for (int i = 0; i < creditLevel.length; i++)
            if (creditLevel[i] != Listener.HeardWord.MATCH_EXACT)
                return i;
        return -1;
    }


    /**
     * Update the displayed sentence based on the newly calculated credit level
     */
    private void UpdateSentenceDisplay() {

        String fmtSentence = "";
        String[] words = currentSentence.split("\\s+");

        for (int i = 0; i < words.length; i++) {

            String styledWord = words[i];                           // default plain

            // show credit status with color
            if (creditLevel[i] == Listener.HeardWord.MATCH_EXACT) {     // match found, but not credited

                styledWord = "<font color='#00B600'>" + styledWord + "</font>";

            } else if (creditLevel[i] == Listener.HeardWord.MATCH_MISCUE) {  // wrongly read

                styledWord = "<font color='red'>" + styledWord + "</font>";

            } else if (creditLevel[i] == Listener.HeardWord.MATCH_TRUNCATION) { //  heard only half the word

            } else {

            }

            if (i == expectedWordIndex) {// style the next expected word
                styledWord.replace("<u>", "");
                styledWord.replace("</u>", "");
                styledWord = "<u>" + styledWord + "</u>";
            }

            fmtSentence += styledWord + " ";

        }
        fmtSentence += "<br>";

        Spanned test = Html.fromHtml(completedSentencesFmtd + fmtSentence);

        mPageText.setText(Html.fromHtml(completedSentencesFmtd + fmtSentence));

        updateCompletedSentence();

        broadcastActiveTextPos(mPageText, words);
    }


    /**
     * Notes:
     * XML story source text must be entered without extra space or linebreaks.
     *
     *     <selectlevel level="1">
     *          <story story="1">
     *              <part part="1">Uninterrupted text</part>
     *          </story>
     *
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
     * to make auto scroll for the sentences
     */
    public void updateCompletedSentence() {

        int height = mPageText.getHeight();
        int scrollY = mPageText.getScrollY();
        Layout layout = mPageText.getLayout();
        int lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);
        int totalNoOfLines = mPageText.getLineCount() - 1;
        if (lastVisibleLineNumber < totalNoOfLines) {
            completeSentenceIndex = currentIndex;
            completedSentencesFmtd = "";
            completedSentences     = "";
        }
    }


    @Override
    public boolean endOfData() {
        return false;
    }

    @Override
    public void onUpdate(Listener.HeardWord[] heardWords, boolean finalResult) {

    }

    /**
     *
     * @param index
     * @return
     */
    public boolean isWordCredited(int index) {
        return index >= 0 && (index == 0 || creditLevel[index - 1] == Listener.HeardWord.MATCH_EXACT);
    }


    /**
     * Get number of exact matches
     * @return
     */
    public int getNumWordsCredited() {
        int n = 0;
        for (int cl : creditLevel) {
            if (cl == Listener.HeardWord.MATCH_EXACT)
                n += 1;
        }
        return n;
    }


    /**
     * Show the next available sentence to the user
     */
    @Override
    public void nextSentence() {
        mListener.deleteLogFiles();
        switchSentence(currentIndex + 1);      // for now just loop around single story
    }


    /**
     * Initialize mListener with the specified sentence
     *
     * @param index index of the sentence that needs to be initialized
     */
    @Override
    public boolean switchSentence(int index) {

        boolean result = true;

        // We've exhausted all the sentences in the story
        if (index == sentences.size()) {

            Log.d("ASR", "End of Story");
            // Kill off the mListener.
            // When this returns the recognizerThread is dead and the mic
            // has been disconnected.
            if (mListener != null)
                mListener.stop();

            result = false;
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
            sentenceWords = Listener.textToWords(currentSentence);

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

            UpdateSentenceDisplay();
        }

        return result;
    }


    /**
     * @param heardWords Update the sentence credit level with the credit level of the heard words
     */
    public void updateSentence(Listener.HeardWord[] heardWords) {

        Log.d("ASR", "New Hypothesis Set:");

        if (heardWords.length >= 1) {

            // record credit level of sentence words
            for (int i = 0; i < creditLevel.length; i++) {
                if (creditLevel[i] != Listener.HeardWord.MATCH_EXACT)            // don't touch words with permanent credit
                    creditLevel[i] = 0;
            }

            for (Listener.HeardWord hw : heardWords) {
                Log.d("ASR", "Heard:" +hw.hypWord);
                // assign the highest credit found among all hypothesis words
                if (hw.matchLevel >= creditLevel[hw.iSentenceWord]) {
                    creditLevel[hw.iSentenceWord] = hw.matchLevel;
                }
            }

            expectedWordIndex = getFirstUncreditedWord();

            // Update the sentence text display to show credit, expected word
            UpdateSentenceDisplay();
        }
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


