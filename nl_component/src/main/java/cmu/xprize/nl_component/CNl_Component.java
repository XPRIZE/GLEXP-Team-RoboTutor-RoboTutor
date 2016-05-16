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
import java.util.Arrays;
import java.util.Collections;
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
import edu.cmu.xprize.listener.ListenerJSGF;
import edu.cmu.xprize.listener.ListenerPLRT;

public class CNl_Component extends CStimRespBase implements IAsrEventListener, INl_Implementation {

    protected ListenerBase          mListener;
    protected TTSsynthesizer        mSynthesizer;

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
            mListener.setLanguage(getLanguageFeature());
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
     * sStimulusString      - "238"
     * sStimulusValue       - 238
     * sStimulusText        - "TWO HUNDRED THIRTY EIGHT"
     *
     * Note: the lists are in order of increasing place value
     *
     * sStimulusDigitString - ["8", "3", "2"]
     * sStimulusDigitValue  - [8, 3, 2]
     * sStimulusDigitText   - ["EIGHT", "THREE", "TWO"]
     *
     * sStimulusPlaceString - ["8", "30", "200"]
     * sStimulusPlaceValue  - [8, 30, 200]
     * sStimulusPlacetext   - ["eight", "thirty", "two hundred"]
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
    public void updateText(String newValue) {
        super.updateText(newValue);
    }


    /**
     * Override in subclass to provide decoder specific implementations
     *
     * @param enable
     */
    public void listen(Boolean enable) {
        mInputProcessor.listen(enable);
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


    /**
     * Override in tutor domain to update scriptable elements
     * @param error
     */
    public void updateOutcomeState(boolean error) {
    }



    @Override
    public void onASREvent(int eventType) {

        switch (eventType) {

            case TCONST.RECOGNITION_EVENT:
                Log.d("ASR", "RECOGNITION EVENT");
                applyEventNode(_onRecognition);
                break;

            case TCONST.ERROR_EVENT:
                Log.d("ASR", "RECOGNITION EVENT");
                applyEventNode(_onRecognitionError);
                break;

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
