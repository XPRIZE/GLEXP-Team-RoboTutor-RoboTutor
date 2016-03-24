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
import android.os.Bundle;
import android.os.Handler;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TTSsynthesizer;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TimerUtils;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.Listener;



/**
 *  The Reading Tutor Component
 */
public class CRt_Component extends PercentRelativeLayout implements IVManListener, IAsrEventListener, ILoadableObject {

    private Context                 mContext;
    private String                  word;

    private Listener                listener;
    private TTSsynthesizer          synthesizer;

    protected ICRt_ViewManager      mViewManager;                                   // Created in TRt_Component sub-class
    protected String                mDataSource;

    private ArrayList<String>       sentences              = null;                  //list of sentences of the given passage
    private String                  currentSentence;                                //currently displayed sentence that need to be recognized
    private HashMap<String, String> suggestions            = null;
    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";

    private ImageView               mPageIImage;
    private TextView                mPageText;

    // state for the current sentence
    private int                     currentIndex          = 0;                      // current sentence index in story, -1 if unset
    private int                     currIntervention      = TCONST.NOINTERVENTION;  //
    private int                     completeSentenceIndex = 0;
    private String                  sentenceWords[];                                // current sentence words to hear
    private int                     expectedWordIndex     = 0;                      // index of expected next word in sentence
    private static int[]            creditLevel           = null;                   // per-word credit level according to current hyp

    // Tutor scriptable events
    private String                  _silenceEvent;          // Instant silence begins
    private String                  _soundEvent;            // Instant a sound is heard
    private String                  _wordEvent;             // Instant a word is recognized
    private String                  _timedSilenceEvent;     // Time since silence began
    private String                  _timedSoundEvent;       // Time since noise began
    private String                  _timedWordEvent;        // Time since last word recognized


    public static String            RECOGLANG;

    // This is used to map "type" (class names) in the index to real classes
    //
    static public HashMap<String, Class> viewClassMap = new HashMap<String, Class>();

    static {
        viewClassMap.put("ASB_Data", CRt_ViewManagerASB.class);
        viewClassMap.put("MARi_Data", CRt_ViewManagerMari.class);
    }

    // json loadable
    public CData_Index[]      dataSource;


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

    public void init(Context context, AttributeSet attrs ) {

        inflate(getContext(), R.layout.rt__component, this);

        mContext = context;

        mPageIImage = (ImageView) findViewById(R.id.SpageImage);
        mPageText   = (TextView) findViewById(R.id.SstoryText);
    }


    protected void prepareListener(Listener rootListener, TTSsynthesizer rootTTS) {

        // Attach the speech recognizer.
        listener = rootListener;
        listener.setEventListener(this);

        // attach TTS
        synthesizer = rootTTS;
    }



    //*************************************************
    //****** Activity state support START

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

        mViewManager.switchSentence(currentIndex);
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

    //****** Activity state support END
    //*************************************************



    //****************************************************************************
    //*********************  Speech Recognition Interface - Start

    @Override
    public void onBeginningOfSpeech() {}


    @Override
    public void onEndOfSpeech() {}


    @Override
    public void onUpdate(Listener.HeardWord[] heardWords, boolean finalResult) {

        mViewManager.onUpdate(heardWords, finalResult);             // update current sentence state and redraw
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



    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start


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
        mViewManager.nextSentence();
    }


    /**
     *
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


    /**
     *
     * @param language Feature string (e.g. LANG_EN)
     */
    public void setLanguage(String language) {

        // TODO: manage language switching - currently ASR uses CTutor default language
        //listener.init(mContext, RECOGLANG);
        RECOGLANG = language;

        // Configure the listener for out story
        listener.setLanguage(language);
    }


    public void configTimedEvent(String symbol, String eventString, int timeOut, boolean reset) {

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
        listener.configTimedEvent(eventType, timeOut, reset);
    }


    public void clearTimedEvent(String eventString) {

        int eventType = CEventMap.eventMap.get(eventString);

        listener.configTimedEvent(eventType, Long.MAX_VALUE, true);
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    protected void applyEventNode(String nodeName) {
    }

    public void publishTargetWord(String word) {
    }

    public void publishTargetWordIndex(int index) {
    }

    public void publishTargetSentence(String sentence) {
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************



    protected boolean isCorrect() {

        boolean correct = false;

        return correct;
    }


    public void next() {

        try {
            if (mViewManager != null) {

                mViewManager.nextSentence();

            } else {
                Log.e(TAG, "Error no DataSource : ");
                System.exit(1);
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Data Exhuasted: call past end of data");
            System.exit(1);
        }

    }


    public boolean dataExhausted() {
        return mViewManager.endOfData();
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
