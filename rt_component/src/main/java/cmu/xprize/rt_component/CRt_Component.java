package cmu.xprize.rt_component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import cmu.xprize.util.Synthesizer;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TimerUtils;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.Listener;


/**
 *  The Reading Tutor Component
 */
public class CRt_Component extends View  implements IAsrEventListener {

    private Context             mContext;
    private String              word;

    private Listener            listener;
    private Synthesizer         synthesizer;

    private ArrayList<String>       sentences = null;               //list of sentences of the given passage
    private String                  currentSentence;                //currently displayed sentence that need to be recognized
    private HashMap<String, String> suggestions = null;
    private String                  completedSentencesFmtd = "";
    private String                  completedSentences = "";

    // state for the current sentence
    private int             currentIndex = 0;                               // current sentence index in story, -1 if unset
    private int             currIntervention = TCONST.NOINTERVENTION;       //
    private boolean         changingSentence = false;
    private int             completeSentenceIndex = 0;
    private String          sentenceWords[];                                // current sentence words to hear
    private int             expectedWordIndex = 0;                          // index of expected next word in sentence
    private static int[]    creditLevel = null;                             // per-word credit level according to current hyp

    private TextView textCurrentPassage;

    public static String RECOGLANG;

    static final String TAG = "CRt_Component";


    public CRt_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CRt_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CRt_Component(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs ) {

        mContext = context;
    }
    
    public void prepareListener(String ID) {

        // initialize the recognizer. just use defaults
        listener = new Listener(ID);

        listener.init(mContext, RECOGLANG);
        listener.setEventListener(this);
    }


    protected void onPause() {

        // stop listening abortively whenever app pauses or stops (moves to background)
        if (listener != null) {
            listener.deleteLogFiles();
            listener.cancel();
        }
    }


    protected void onStop() {
    }


    protected void onResume() {
    }


    protected void onRestart() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                say("please read the story aloud");
            }
        }, 2000);

        switchSentence(currentIndex);
    }


    /* Following saves state over possible destroy/recreate cycle,
      * which occurs most commonly on portrait/landscape change.
 	 * We save current sentence (though no credit state) in order to restart from there
 	 */
    protected void onSaveInstanceState(Bundle state) {
//        super.onSaveInstanceState(state);
//        state.putInt("currentIndex", currentIndex);     // just save the current sentence index.
    }

    protected void onDestroy() {
        synthesizer.shutDown();
    }


    /**
     * Produce any random intervention if the user is silent for a specific time
     */
    public void promptToRead() {

        say("Tafadhali sema neno hii kwa, sauti");
    }


    public void speakTargetWord() {    // to speak the Target word

        // Utterance: An unbroken segment of speech
        // (In this case we are breaking when there is an intervention - but it could be as
        // fine grained a the user stopping for 300ms for example)
        //
        // Pause the listener and set it up for a new utterance
        //
        listener.reInitializeListener(true);
        listener.listenFor(sentenceWords, expectedWordIndex);
        say("kutamka hivyo, " + sentenceWords[expectedWordIndex]);
        listener.setPauseListener(false);
    }

    public void speakTargetSentence() {   // to speak the entire Target word sentence

        say("Usijali, i itakuwa kusoma kwa ajili yenu." + currentSentence);
        nextSentence();
    }

    private int getNumWordsCredited() {
        int n = 0;
        for (int cl : creditLevel) {
            if (cl == Listener.HeardWord.MATCH_EXACT)
                n += 1;
        }
        return n;
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
     * @param heardWords Update the sentence credit level with the credit level of the heard words
     */
    private void updateSentence(Listener.HeardWord[] heardWords) {

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

        textCurrentPassage.setText(Html.fromHtml(completedSentencesFmtd + fmtSentence));

        updateCompletedSentence();

        broadcastActiveTextPos(textCurrentPassage, words);
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
    public PointF broadcastActiveTextPos(TextView text, String[] words){

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

    //        CPersonaObservable.broadcastLocation(text, TCONST.LOOKAT, point);
        }
        return point;
    }


    public void updateCompletedSentence() {   // to make auto scroll for the sentences
        int height = textCurrentPassage.getHeight();
        int scrollY = textCurrentPassage.getScrollY();
        Layout layout = textCurrentPassage.getLayout();
        int lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);
        int totalNoOfLines = textCurrentPassage.getLineCount() - 1;
        if (lastVisibleLineNumber < totalNoOfLines) {
            completeSentenceIndex = currentIndex;
            completedSentencesFmtd = "";
            completedSentences     = "";
        }
    }


    /**
     * Show the next available sentence to the user
     */
    private void nextSentence() {
        listener.deleteLogFiles();
        switchSentence(currentIndex + 1);      // for now just loop around single story
    }


    /**
     * Initialize listener with the specified sentence
     *
     * @param index index of the sentence that needs to be initialized
     */
    private void switchSentence(int index) {

        // We've exhausted all the sentences in the story
        if (index == sentences.size()) {

            Log.d("ASR", "End of Story");
            // Kill off the listener.
            // When this returns the recognizerThread is dead and the mic
            // has been disconnected.
            if (listener != null)
                listener.stop();
        }
        if (index > 0) {  // to set grey color for the finished sentence
            completedSentencesFmtd = "<font color='grey'>";
            completedSentences     = "";
            for (int i = completeSentenceIndex; i < index; i++) {
                completedSentences += sentences.get(i);
                completedSentences += ". ";
            }
            completedSentencesFmtd += completedSentences;
            completedSentencesFmtd += "</font>";
        }
        currentIndex    = index % sentences.size();
        currentSentence = sentences.get(currentIndex).trim() + ".";

        // get array or words to hear for new sentence
        sentenceWords = Listener.textToWords(currentSentence);

        // reset all aggregate hyp info for new sentence
        // fills default value 0 = MATCH_UNKNOWN
        creditLevel = new int[sentenceWords.length];
        expectedWordIndex = 0;

        // show sentence and start listening for it
        // If we are starting from the beginning of the sentence then end any current sentence
        if (listener != null) {
            listener.reInitializeListener(true);
            listener.listenFor(sentenceWords, 0);
            listener.setPauseListener(false);
        }

        UpdateSentenceDisplay();
    }


    public static boolean isWordCredited(int index) {
        return index >= 0 && (index == 0 || creditLevel[index - 1] == Listener.HeardWord.MATCH_EXACT);
    }


    /**
     * Produce any random intervention if the user is silent for a specific time
     */
    public void say(String prompt) {

        listener.setPauseListener(true);
        synthesizer.speak(prompt);

        while (synthesizer.isSpeaking()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        listener.setPauseListener(false);
    }


    @Override
    public void onASREvent(int eventType) {

        switch (eventType) {

            case TCONST.SILENCE_EVENT:
                break;

            case TCONST.SOUND_EVENT:
                break;

            case TCONST.WORD_EVENT:
                Log.d("ASR", "WORD EVENT");
                currIntervention = TCONST.SAYWORD;
                listener.configTimedEvent(TCONST.TIMEDSILENCE_EVENT, Long.MAX_VALUE, true);
                listener.configTimedEvent(TCONST.TIMEDWORD_EVENT, 5000, true);
                break;

            case TCONST.TIMEDSILENCE_EVENT:
                Log.d("ASR","SILENCE TIMEOUT");
                promptToRead();
                listener.configTimedEvent(TCONST.TIMEDSILENCE_EVENT, 6000, true);
                break;

            case TCONST.TIMEDSOUND_EVENT:
            case TCONST.TIMEDWORD_EVENT:
                switch(currIntervention) {
                    // As soon as they say anything wait at least 5sec before telling
                    // Them the word
                    case TCONST.INSPEECH:
                        break;

                    case TCONST.SAYWORD:
                        Log.d("ASR","HYPOTHESIS TIMEOUT EVENT");
                        speakTargetWord();
                        listener.configTimedEvent(TCONST.TIMEDWORD_EVENT, 7000, true);
                        break;
                }
                break;
        }
    }

    //****************************************************************************
    //*********************  Speech Recognition Interface - Start

    @Override
    public void onBeginningOfSpeech() {}


    @Override
    public void onEndOfSpeech() {
        UpdateSentenceDisplay();
    }


    @Override
    public void onUpdate(Listener.HeardWord[] heardWords, boolean finalResult) {

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
        if(getNumWordsCredited() == sentenceWords.length) {

            changingSentence = true;
            TimerUtils.pauseTimer();
            listener.setPauseListener(true);

            // schedule advance after short delay to allow time to see last word credited on screen
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    nextSentence();
                    changingSentence = false;
                }
            }, 100);
        }
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************



}
