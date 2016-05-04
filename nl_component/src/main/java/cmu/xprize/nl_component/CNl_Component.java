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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.util.CEventMap;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCJSGF;
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

    protected int                   mStimulusNumber;
    protected String                mStimulusText;
    protected List                  mStimulusNumList;
    protected String[]              mStimulusTextList;

    protected int                   mResponseNumber;
    protected String                mResponseText;
    protected List                  mResponseList;
    protected ArrayList<String>     mResponseTextList;

    private static int[]            creditLevel            = null;          // per-word credit level according to current hyp
    private int                     expectedWordIndex      = 0;             // index of expected next word in sentence
    private boolean                 missingConjTolerant    = true;          // Do you need "AND" between number words
    private boolean                 addedConjTolerant      = true;          // English tends to false positive conjunctions (AND)
    private boolean                 repeatedWordIntolerant = true;          // Indicate repeated word errors.
    private String                  cachedLanguageFeature;
    private String                  conjunction;

    // Tutor scriptable ASR events
    private String                  _silenceEvent;          // Instant silence begins
    private String                  _soundEvent;            // Instant a sound is heard
    private String                  _wordEvent;             // Instant a word is recognized
    private String                  _timedSilenceEvent;     // Time since silence began
    private String                  _timedSoundEvent;       // Time since noise began
    private String                  _timedWordEvent;        // Time since last word recognized
    private String                  _timedStartEvent;       // Time since recognizer started listening


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

        // Have connector sub-class in the tutor domain Inject the listener into the MediaManager
        setListener(mListener);
    }


    /**
     * @Override in Tutor Domain to allow the MediaManageer direct access to the recognizer
     */
    public void setListener(ListenerBase listener) {}


    /**
     * @Override in Tutor Domain to allow the MediaManageer direct access to the recognizer
     */
    public void removeListener(ListenerBase listener) {}


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


    /**
     * mStimulus contains the string representation of the data source in numeric form e.g. "34"
     * mStimulusNumList contain the array of positional integers that comprise the number e.g. [3,4]
     * Obtain its mStimulusNumber integer equivalent e.g. "930" = 930
     * Convert it to a mStimulusText string representation e.g. 102 = "one hundred and two"
     *
     */
    @Override
    protected void preProcessStimulus() {

        String[] stimElem =  mStimulus.split("(?!^)");

        mStimulusNumList = new ArrayList<Integer>();

        for(String elem : stimElem) {
            mStimulusNumList.add(Integer.parseInt(elem));
        }

        mStimulusNumber   = Integer.parseInt(mStimulus);
        mStimulusText     = Num2Word.transform(mStimulusNumber, getLanguage()).toUpperCase();
        mStimulusTextList = mStimulusText.split(" ");
    }


    public void listen(Boolean enable) {

        try {
            if (enable) {

                // Listen for a language specific number set
                //
                if (mListener != null) {

                    // Cache the language feature (e.g. "LANG_EN") and
                    // the language specific conjunction used in numbers e.g. "AND"
                    // as in 100 and 23
                    //
                    cachedLanguageFeature = getLanguageFeature();
                    conjunction           = TCJSGF.conjMap.get(cachedLanguageFeature);

                    // We use the stimiulus as the base string to listen for and
                    // add all the other numbers as distractors
                    //
                    String[] distractors = (TCONST.numberMap.get(cachedLanguageFeature).split(","));

                    ArrayList<String> combo = new ArrayList<String>();

                    for(String elem : mStimulusTextList) {
                        combo.add(elem.toUpperCase());
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
            else  {
                mListener.setPauseListener(true);
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Data not initialized - " + e);
            System.exit(1);
        }

    }


    public void configureEvent(String eventString, String symbol) {

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


    public void configureTimedEvent(String eventString, String symbol, Integer timeOut) {

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

            case TCONST.TIMEDSTART_EVENT:
                _timedStartEvent = symbol;
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

        boolean Correct   = false;
        boolean Error     = false;
        int     ErrorType = 0;
        int     maxPlace  = 0;
        int     maxHeard  = 0;

        mResponseTextList   = new ArrayList<String>();

        if (heardWords.length >= 1) {

            for(int i = 0 ; i < mStimulusTextList.length ; i++) {
                mResponseTextList.add("");
            }

            // Reprocess the HeardWords hypothesis to allow incorrect repeated numbers.
            // e.g. If the stimulus is 179 and the user says 171 the second '1' will have
            // an iSentenceWord of 0 instead of 2
            // This is to make the screen updates more responsive to user input. Otherwise when
            // they say 171 in response to a stimulus of 179 there would be no update.
            //
            // If the user makes a repeated word mistake there will be a heardword with an
            // iSentence position that is behind where the previous word was
            //  MIA:0 | MOJA:1 | NA:2 | SABINI:3 | NA:4 | MOJA:1 |
            // So in this case we update the last MOJA iSentenceWord to :5
            //
            if(repeatedWordIntolerant && heardWords.length > 1) {
                for (int i = 1; i < heardWords.length; i++) {
                    if(heardWords[i].iSentenceWord < i) {
                        Log.d("ASR", "Repeated Word <error>");
                        heardWords[i].iSentenceWord = i;
                        break;
                    }
                }
            }

            String logString = "";
            for (int i = 0; i < heardWords.length; i++) {
                logString += heardWords[i].hypWord.toUpperCase() + ":" + heardWords[i].iSentenceWord + " | ";
            }
            Log.d("ASR", "New HypSet: "  + logString);

            // Place the words in order
            // Loop throught as many words as are in the stimulus
            //
            for (int i1 = 0; i1 < mStimulusTextList.length; i1++) {

                // Assign the most recent one to it's associated place value
                // This may result in empty place values and may place some values in the
                // wrong spot.
                //
                for (int i2 = 0; i2 < heardWords.length; i2++) {
                    if(heardWords[i2].iSentenceWord == i1) {
                        mResponseTextList.set(i1, heardWords[i2].hypWord.toUpperCase());

                        // Track the max place where we have placed a word to limit the
                        // conjuntion insertion below
                        if(i1 > maxPlace) maxPlace = i1;
                    }
                }
            }

            // Prune out false positive conjunctions
            // e.g for a stimulus of 100 it hears ONE AND...
            //
            if(addedConjTolerant) {

                for (int i1 = 0; i1 <= maxPlace; i1++) {

                    // If the response is a conjunction and the stimulus
                    // isn't then remove the response conjunction (false positive) and
                    // retest the element if we aren't at the end of the reponse set
                    //
                    if(!mStimulusTextList[i1].equals(conjunction)) {
                        if(mResponseTextList.get(i1).equals(conjunction)) {
                            Log.d("ASR", "Conjunction pruned" );
                            mResponseTextList.remove(i1);

                            if(i1 == maxPlace) break;
                            i1--;
                        }
                    }
                }
            }

            // Insert missing conjunctions
            // If we are tolerant of missing conjunctions then if there we are looking
            // at a conj in the stimulus that is not in the response insert it
            //
            if(missingConjTolerant) {

                for (int i1 = 0; i1 <= maxPlace; i1++) {

                    String respElem = mResponseTextList.get(i1);

                    // If the stimulus place value is a conjunction and the response
                    // isn't then either insert it or replace an empty element
                    //
                    if (mStimulusTextList[i1].equals(conjunction)) {
                        if(!respElem.equals(conjunction)) {
                            if(respElem.equals("")) {
                                Log.d("ASR", "Conjunction Insertion");
                                mResponseTextList.set(i1, conjunction);
                            }
                            else {
                                mResponseTextList.add(i1, conjunction);
                            }
                        }
                    }
                }
            }

            // Prune the "" entries so they don't cause errors in Word2Num.
            //
            for(int i1 = 0 ; i1 < mResponseTextList.size() ; i1++) {
                if(mResponseTextList.get(i1).equals("")) {
                    mResponseTextList.remove(i1--);
                }
            }

            try {
                // Note that Word2Num is not tolerant of missing Conjunctions - in fact it's fussy
                // Word2Num will return -1 for certain error modes
                //
                mResponseNumber = Word2Num.transform(mResponseTextList.toArray(new String[mResponseTextList.size()]), getLanguage());

                mResponse       = new Integer(mResponseNumber).toString();
                mResponseList   = Word2Num.getNumberList();

                Log.d("ASR", "ResponseNumber: " + mResponseNumber);
                Log.d("ASR", "ResponseString: " + mResponse);

                // We update the control directly in this case - unlike the base component
                //
                if(mResponseNumber != -1)
                    updateText(mResponse);

                // Let anyone interested know there is a new recognition set available
                Intent msg = new Intent(TCONST.LISTENER_RESPONSE);
                msg.putExtra(TCONST.FW_VALUE, mResponse);

                bManager.sendBroadcast(msg);


                Log.d("ASR", "Stimulus: " + TextUtils.join(" ", mStimulusTextList));
                Log.d("ASR", "Response: " + TextUtils.join(" ", mResponseTextList));

                int size = Math.min(mStimulusTextList.length,mResponseTextList.size());

                for (int i1 = 0; i1 < size; i1++) {

                    String subElem = mResponseTextList.get(i1);

                    // If we run out of response just continue.
                    // Note this is only relevant when not pruning "" entries
                    //
                    if(subElem == "") {
                        expectedWordIndex = i1;
                        break;
                    }
                    // Check if the next element matches
                    //
                    else if(!mStimulusTextList[i1].equals(subElem)) {
                        expectedWordIndex = i1;

                        ErrorType = mStimulusNumList.size();
                        Error     = TCONST.TRUE_ERROR;
                        Correct   = false;
                        break;
                    }
                    // Note - don't use the response length here as it may have been made
                    // longer than the stimulus by a conj insertion or repeated word.
                    //
                    else if(i1 >= mStimulusTextList.length-1) {
                        Correct   = true;
                        break;
                    }
                }
                if(Correct) {
                    updateOutcomeState(TCONST.FALSE_NOERROR);
                    applyEventNode(_onRecognition);
                }
                else if(Error) {
                    updateOutcomeState(TCONST.TRUE_ERROR);

                    // If they've uttered as many words as are in the stimulus and they are
                    // still incorrect emit a recognition event to terminate.
                    //
                    if(mStimulusTextList.length == mResponseTextList.size())
                        applyEventNode(_onRecognition);
                }

                // Update the expected word in MultiMatch
                //
                mListener.updateNextWordIndex(expectedWordIndex);

            } catch (Exception e) {
                Log.e("ASR", "Number Parser" + e);
              //  System.exit(1);
            }
        }
    }


    /**
     * Override in tutor domain to update scriptable elements
     * @param error
     */
    protected void updateOutcomeState(boolean error) {
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

            case TCONST.TIMEDSTART_EVENT:
                Log.d("ASR", "START TIMEOUT");
                applyEventNode(_timedStartEvent);
                break;
        }
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************


}
