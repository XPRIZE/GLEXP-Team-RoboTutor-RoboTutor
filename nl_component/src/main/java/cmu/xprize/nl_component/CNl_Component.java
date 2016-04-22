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

package cmu.xprize.nl_component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.util.CEventMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import cmu.xprize.util.Word2Num;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.ListenerBase;
import edu.cmu.xprize.listener.ListenerPLRT;

public class CNl_Component extends CStimRespBase implements IAsrEventListener {

    protected ListenerBase          mListener;
    protected TTSsynthesizer        mSynthesizer;

    protected String                DATASOURCEPATH;
    protected String                EXTERNPATH;

    private int                     mStimulusNumber;
    private String                  mStimulusText;
    private List                    mStimulusList;
    private String[]                mStimulusTextList;

    private int                     mResponseNumber;
    private String                  mResponseText;
    private List                    mResponseList;
    private ArrayList<String>       mResponseTextList;

    private static int[]            creditLevel            = null;          // per-word credit level according to current hyp
    private int                     expectedWordIndex      = 0;             // index of expected next word in sentence

    // Tutor scriptable ASR events
    private String                  _silenceEvent;          // Instant silence begins
    private String                  _soundEvent;            // Instant a sound is heard
    private String                  _wordEvent;             // Instant a word is recognized
    private String                  _timedSilenceEvent;     // Time since silence began
    private String                  _timedSoundEvent;       // Time since noise began
    private String                  _timedWordEvent;        // Time since last word recognized

    private LocalBroadcastManager bManager;

    static public String TAG = "CNl_Component";



    public CNl_Component(Context context) {
        super(context);
    }

    public CNl_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CNl_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    protected void prepareListener(TTSsynthesizer rootTTS) {

        // Response variants get hooked to the listener
        //
        if(mIsResponse) {
            // Generate a Project Listen type listener
            // Attach the speech recognizer.
            mListener = new ListenerPLRT();
            //mListener = new ListenerJSGF();

            // Configure the mListener language and the callback for ASR Events
            mListener.setLanguage(getLanguageFeature());
            mListener.setEventListener(this);
        }

        // attach TTS
        mSynthesizer = rootTTS;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    /**
     * @Override in Tutor Domain to allow access to CTutor language settting
     */
    public String getLanguage() {
        return "en";
    }

    /**
     * @Override in Tutor Domain to allow access to CTutor language settting
     */
    public String getLanguageFeature() {
        return "LANG_EN";
    }


    // mStimulus contains the string representation of the data source in numeric form e.g. "34"
    // mStimulusList contain the array of positional integers that comprise the number e.g. [3,4]
    // Obtain its mStimulusNumber integer equivalent e.g. "930" = 930
    // Convert it to a mStimulusText string representation e.g. 102 = "one hundred and two"
    //
    @Override
    protected void preProcessStimulus() {

        String[] stimElem =  mStimulus.split("(?!^)");

        mStimulusList = new ArrayList<Integer>();

        for(String elem : stimElem) {
            mStimulusList.add(Integer.parseInt(elem));
        }

        mStimulusNumber   = Integer.parseInt(mStimulus);
        mStimulusText     = Num2Word.transform(mStimulusNumber, getLanguage());
        mStimulusTextList = mStimulusText.split(" ");
    }


    public void listen(Boolean enable) {

        try {
            if (enable) {

                // Listen for a language specific number set
                //
                if (mListener != null) {

                    // We use the stimiulus as the base string to listen for and
                    // add all the other numbers as distractors
                    //
                    String[] distractors = (TCONST.numberMap.get(getLanguageFeature()).split(","));

                    ArrayList<String> combo = new ArrayList<String>();

                    for(String elem : mStimulusTextList) {
                        combo.add(elem);
                    }

                    for(String elem : distractors) {
                        combo.add(elem);
                    }

                    mListener.reInitializeListener(true);
                    mListener.listenFor(combo.toArray(new String[combo.size()]),0);
                    mListener.setPauseListener(false);
                }

              // mListener.listenFor(TCONST.numberMap.get(getLanguageFeature()).split(","), 0);
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Data not initialized - " + e);
            System.exit(1);
        }

    }


    public void configureEvent(String symbol, String eventString) {

        int eventType = CEventMap.eventMap.get(eventString);

        switch(eventType) {

            case TCONST.SILENCE_EVENT:
                _silenceEvent = symbol;
                break;

            case TCONST.SOUND_EVENT:
                _soundEvent = symbol;
                break;

            case TCONST.WORD_EVENT:
                _wordEvent = symbol;
                break;
        }
        mListener.configStaticEvent(eventType);
    }
    public void clearEvent(String eventString) {

        int eventType = CEventMap.eventMap.get(eventString);

        mListener.resetStaticEvent(eventType);
    }


    public void configureEvent(String symbol, String eventString, int timeOut) {

        int eventType = CEventMap.eventMap.get(eventString);

        switch(eventType) {

            case TCONST.TIMEDSILENCE_EVENT:
                _timedSilenceEvent = symbol;
                break;

            case TCONST.TIMEDSOUND_EVENT:
                _timedSoundEvent = symbol;
                break;

            case TCONST.TIMEDWORD_EVENT:
                _timedWordEvent = symbol;
                break;
        }
        mListener.configTimedEvent(eventType, timeOut);
    }
    public void clearTimedEvent(String eventString) {

        int eventType = CEventMap.eventMap.get(eventString);

        mListener.resetTimedEvent(eventType);
    }




    //****************************************************************************
    //*********************  Speech Recognition Interface - Start

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        boolean Correct = true;
        mResponseTextList = new ArrayList<String>();

        if (heardWords.length >= 1) {

            for (int i1 = 0; i1 < mStimulusTextList.length; i1++) {
                for (int i2 = 0; i2 < heardWords.length; i2++) {
                    if(heardWords[i2].iSentenceWord == i1) {
                        mResponseTextList.add(heardWords[i2].hypWord.toLowerCase());
                    }
                }
            }

            try {
                mResponseNumber = Word2Num.transform(mResponseTextList.toArray(new String[mResponseTextList.size()]), getLanguage());
                mResponse = new Integer(mResponseNumber).toString();
                mResponseList = Word2Num.getNumberList();

                // We update the control directly in this case - unlike the base component
                //
                updateText(mResponse);

                // Let anyone interested know there is a new recognition set available
                Intent msg = new Intent(TCONST.LISTENER_RESPONSE);
                msg.putExtra(TCONST.FW_VALUE, mResponse);

                bManager.sendBroadcast(msg);

                Log.d("ASR", "Stimulus: " + TextUtils.join(" ", mStimulusTextList));
                Log.d("ASR", "Response: " + TextUtils.join(" ", mResponseTextList));

                for (int i1 = 0; i1 < mStimulusTextList.length; i1++) {

                    if(i1 >= mResponseTextList.size() || !mStimulusTextList[i1].equals(mResponseTextList.get(i1))) {
                        Correct = false;
                        break;
                    }
                }
                if(Correct)
                    applyEventNode(_onRecognition);

            } catch (Exception e) {
                Log.e("ASR", "Number Parser" + e);
              //  System.exit(1);
            }
        }
    }


    /**
     * @param heardWords Update the sentence credit level with the credit level of the heard words
     */
    private void updateSentence(ListenerBase.HeardWord[] heardWords) {

        Log.d("ASR", "New Hypothesis Set:");

        if (heardWords.length >= 1) {

            // Reset partial credit level of sentence words
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

            // Tell the listerner when to stop matching words.  We don't want to match words
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
     * Update the displayed sentence based on the newly calculated credit level
     */
    private void UpdateSentenceDisplay() {

        String fmtSentence = "";
        String[] words = mStimulusText.split("\\s+");

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


    @Override
    public void onASREvent(int eventType) {

        switch (eventType) {

            case TCONST.SILENCE_EVENT:
                Log.d("ASR", "SILENCE EVENT");
                applyEventNode(_silenceEvent);
                break;

            case TCONST.SOUND_EVENT:
                Log.d("ASR", "SOUND EVENT");
                applyEventNode(_soundEvent);
                break;

            case TCONST.WORD_EVENT:
                Log.d("ASR", "WORD EVENT");
                applyEventNode(_wordEvent);
                break;

            case TCONST.TIMEDSILENCE_EVENT:
                Log.d("ASR","SILENCE TIMEOUT");
                applyEventNode(_timedSilenceEvent);
                break;

            case TCONST.TIMEDSOUND_EVENT:
                Log.d("ASR", "SOUND TIMEOUT");
                applyEventNode(_timedSoundEvent);
                break;

            case TCONST.TIMEDWORD_EVENT:
                Log.d("ASR", "WORD TIMEOUT");
                applyEventNode(_timedWordEvent);
                break;
        }
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************


}
