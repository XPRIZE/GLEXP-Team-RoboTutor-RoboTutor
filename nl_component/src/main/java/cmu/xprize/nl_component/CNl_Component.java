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

package cmu.xprize.nl_component;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCJSGF;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import cmu.xprize.util.Word2Num;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.ListenerBase;
import edu.cmu.xprize.listener.ListenerJSGF;
import edu.cmu.xprize.listener.ListenerPLRT;

import static cmu.xprize.util.TCONST.ASREventMap;

public class CNl_Component extends CStimRespBase implements IAsrEventListener, INl_Implementation {

    protected ListenerBase          mListener;
    protected TTSsynthesizer        mSynthesizer;
    protected String                mLanguage;

    protected String                mProcessorType;
    protected CNl_Processor         mInputProcessor;

    // Tutor scriptable ASR events
    protected String                  _silenceEvent;          // Instant silence begins
    protected String                  _soundEvent;            // Instant a sound is heard
    protected String                  _wordEvent;             // Instant a word is recognized
    protected String                  _timedSilenceEvent;     // Time since silence began
    protected String                  _timedSoundEvent;       // Time since noise began
    protected String                  _timedWordEvent;        // Time since last word recognized
    protected String                  _timedStartEvent;       // Time since recognizer started listening


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

    protected void createInputProcessor(String type) {

        mProcessorType = type;

        switch(mProcessorType) {
            case TCONST.JSGF:
                mInputProcessor = new CNl_JSGF(this, mContext);
                break;

            case TCONST.PLRT:
                mInputProcessor = new CNl_PLRT(this, mContext);
                break;
        }
    }

    protected void prepareListener(TTSsynthesizer rootTTS) {

        // Response variants get hooked to the listener
        //
        if(mIsResponse) {

            // Generate a Project Listen type listener
            // Attach the speech recognizer.

            switch(mProcessorType) {
                case TCONST.JSGF:
                    mListener = new ListenerJSGF();
                    break;

                case TCONST.PLRT:
                    mListener = new ListenerPLRT();
                    break;
            }

            // Configure the mListener language and the callback for ASR Events
            mListener.setEventListener(this);

            // Have connector sub-class in the tutor domain Inject the listener into the MediaManager
            //
            setListener(mListener);

            mInputProcessor.setListener(mListener);
        }

        // attach TTS
        mSynthesizer = rootTTS;
    }


    /**
     *
     * @param language Feature string (e.g. LANG_EN)
     */
    public void setLanguage(String language) {

        mLanguage = TCONST.langMap.get(language);

        // Configure the mListener for our story
        //
        mListener.setLanguage(language);
    }


    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {

        super.onEvent(event);

        switch(event.getType()) {

            // Message from Stimiulus variant to share state with response variant
            //
            case TCONST.SET_LANG_FTR:

                setLanguage((String)event.getString(TCONST.VALUE));
                break;
        }
    }


    /**
     * @Override in Tutor Domain to publish W2N state data
     */
    public void publishState(int error, int warn) {
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
     * sStimulus.String      - "238"
     * sStimulus.Value       - 238
     * sStimulus.Text        - "TWO HUNDRED THIRTY EIGHT"
     *
     * Note: the lists are in order of increasing place value
     *
     * sStimulus.DigitString - ["8", "3", "2"]
     * sStimulus.DigitValue  - [8, 3, 2]
     * sStimulus.DigitText   - ["EIGHT", "THREE", "TWO"]
     *
     * sStimulus.PlaceString - ["8", "30", "200"]
     * sStimulus.PlaceValue  - [8, 30, 200]
     * sStimulus.Placetext   - ["eight", "thirty", "two hundred"]
     *
     */
    @Override
    protected void preProcessStimulus() {

        mInputProcessor.preProcessStimulus(mStimulusString);
    }


    /**
     * Handle callbacks from INl_Processor
     * @param newValue
     */
    @Override
    public void updateNumberString(String newValue) {
        super.updateText(newValue);
    }

    /**
     * Override in Tutor sub-class to access text view id in layout - ViewManger uses this
     * to update the debug hypothesis listing
     * @param newValue
     */
    @Override
    public void updateDebugText(String newValue) {
    }


    /**
     * Override in subclass to provide decoder specific implementations
     *
     * @param enable
     */
    public void listen(Boolean enable) {
        mInputProcessor.listen(enable);
    }



    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START

    /**
     * Overridden in TClass to fire graph behaviors
     *
     */
    public boolean applyBehavior(String event){

        boolean result = false;
        return result;
    }

    /**
     * Overridden in TClass to fire graph behaviors
     *
     * @param nodeName
     */
    public void applyBehaviorNode(String nodeName) {
    }


    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //****************************************************************************
    //*********************  Speech Recognition Interface - Start

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }


    /**
     * Override in subclass to provide decoder specific implementations
     *
     * @param heardWords
     * @param finalResult
     */
    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        mInputProcessor.onUpdate(heardWords, finalResult);
    }

    @Override
    public void onUpdate(String[] heardWords, boolean finalResult) {

        //mInputProcessor.onUpdate(heardWords, finalResult);
    }


    /**
     * Override in tutor domain to update scriptable elements
     * @param error
     */
    public void updateOutcomeState(boolean error) {
    }



    @Override
    public void onASREvent(int eventType) {

        // Here we have to convert from bitmapped event types to string types
        //
        switch (eventType) {

            case TCONST.RECOGNITION_EVENT:
                applyBehavior(TCONST.ASR_RECOGNITION_EVENT);
                break;

            case TCONST.ERROR_EVENT:
                applyBehavior(TCONST.ASR_ERROR_EVENT);
                break;

            case TCONST.SILENCE_EVENT:
                applyBehavior(TCONST.ASR_SILENCE_EVENT);
                break;

            case TCONST.SOUND_EVENT:
                applyBehavior(TCONST.ASR_SOUND_EVENT);
                break;

            case TCONST.WORD_EVENT:
                applyBehavior(TCONST.ASR_WORD_EVENT);
                break;

            case TCONST.TIMEDSILENCE_EVENT:
                applyBehavior(TCONST.ASR_TIMEDSILENCE_EVENT);
                break;

            case TCONST.TIMEDSOUND_EVENT:
                applyBehavior(TCONST.ASR_TIMEDSOUND_EVENT);
                break;

            case TCONST.TIMEDWORD_EVENT:
                applyBehavior(TCONST.ASR_TIMEDWORD_EVENT);
                break;

            case TCONST.TIMEDSTART_EVENT:
                applyBehavior(TCONST.ASR_TIMED_START_EVENT);
                break;
        }
    }


    //*********************  Speech Recognition Interface - End
    //****************************************************************************


}
