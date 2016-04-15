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

package edu.cmu.xprize.listener;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.TimerUtils;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.LogMath;
import edu.cmu.pocketsphinx.Segment;


/**
 * This is the base Listener type
 */
public class ListenerBase {

    private IReadyListener tutorRoot;

    /**
     * our modified SpeechRecognizer object wrapping the pocketsphinx decoder
     */
    protected SpeechRecognizer recognizer;

    static protected ListenerAssets  assets;     // created in init phase -

    protected String captureLabel = "";          // label for capture, logging files
    protected boolean IS_LOGGING = false;

    protected File    configFile;                // config file to use, null => default
    protected File    modelsDir;                 // saved model directory
    protected LogMath logMath;                   // needed for creating Fsgs

    protected String   userID;                   // User ID


    // to work around pocketsphinx timing bug: when recognizing continuously across silent pauses,
    // after a pause hyp words from a speech segments before the pause have their reported frame times
    // changed. We use this to save the original pre-pause results
    protected HeardWord[] prePauseResult = null;  // saved results from utterances prior to pause

    protected BufferedWriter bw = null;           // for writing language model files

    protected IAsrEventListener eventListener;    // where to send client notification callbacks

    protected static final String SENTENCE_SEARCH = "sentence";    // label for our search in decoder

    // This is used to map language "Features" to the associated dictionary filenames
    // Dictionary files are located in the assets/sync/models/lm
    // Note: on Android these are case sensitive filenames
    //
    static private HashMap<String, String> langMap = new HashMap<String, String>();

    static {
        langMap.put("LANG_EN", "CMU07A-CAPS.DIC");
        langMap.put("LANG_SW", "SWAHILI.DIC");
    }

    static private boolean isReady = false;


    public ListenerBase() {
    }

    /**
     * construct Listener using default pocketsphinx config settings
     *
     * @param userID -- string identifying the user. will be prepended to capture files
     */
    public ListenerBase(String userID) {
        this.userID = userID;
        configFile = null;

        // decoder setup deferred until init() call.
    }


    /**
     * construct Listener to setup decoder from a pocketsphinx config file. For path arguments config file must contain
     * absolute paths on the Android device.
     *
     * @param userID -- string identifying the user. will be prepended to capture files
     * @param config -- file of pocketsphinx config settings
     */
    public ListenerBase(String userID, File config) {
        this.userID = userID;
        configFile = config;

        // decoder setup deferred until init() call.
    }


    /**
     * Initialize the listener
     *
     * @param langFTR -- application context for locating resources and external storage
     */
    public void setLanguage(String langFTR) {

        // Configure the phonetic rules that will be used by the decoder
        // TODO: Need to make phoneme lang rules dynamic so we may have multiple recognizers
        //
        Phoneme.setTargetLanguage(langFTR);

        // initialize recognizer for our task
        //
        setupRecognizer(assets.getExternalDir(), configFile, langMap.get(langFTR));
    }


    /**
     * Utility method to initialize the listener assets folder
     *
     * @param callback
     */
    public void configListener(IReadyListener callback) {

        tutorRoot = callback;

        new listenerConfigTask().execute((Context) callback);
    }

    // construct and initialize the speech recognizer
    //
    protected void setupRecognizer(File assetsDir, File configFile, String langDictionary) {
    }

        /**
         * Moves new assets to an external folder so the Sphinx code can access it.
         *
         */
    class listenerConfigTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Context... params) {

            boolean result = false;
            try {
                // sync assets from resources to filesystem via ListenerAssets class
                // This takes a modest but noticeable amount of time
                //
                assets = new ListenerAssets(params[0]);
                assets.syncAssets();
                result = true;

            } catch (IOException e) {
                // TODO: Manage exceptions
                Log.d("ASR", "init Failed: " + e);
                result = false;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isReady = result;
            tutorRoot.onServiceReady("ASR", isReady? 1:0);
        }
    }

    public void listenFor(String[] wordsToHear, int startWord){}

    public void updateNextWordIndex(int next){
    }

    /**
     * used by tutor root to test service availability
     * @return
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Stop the listener. Will send final hypothesis event
     */
    public void stop() {
        if (recognizer != null)
            recognizer.stop();
    }

    /**
     * Cancel the listener. Does not send final hypothesis event
     */
    public void cancel() {
        if (recognizer != null)
            recognizer.cancel();
    }


    /**
     * Attach event listener to receive notification callbacks
     */
    public void setEventListener(IAsrEventListener callbackSink) {
        eventListener = callbackSink;
    }

    public void setPauseListener(boolean pauseListener) {
        recognizer.setPauseRecognizer(pauseListener);
    }

    public void reInitializeListener(boolean restartListener) {
        recognizer.setRestartListener(restartListener);
    }

    public void configTimedEvent(int eventType, long newTimeout, boolean reset) {
        recognizer.configTimedEvent(eventType, newTimeout, reset);
    }

    public void configStaticEvent(int eventType, boolean listen) {
        recognizer.configStaticEvent(eventType, listen);
    }




    /**
     * get the path to the capture file for given utterance label
     */
    public File getCaptureFile(String utteranceLabel) {
        return new File(recognizer.rawLogDir, utteranceLabel + ".wav");
    }


    public void deleteLogFiles() {
        if (recognizer == null)
            return;
        new File(recognizer.rawLogDir, captureLabel + "-log.txt").delete();
        new File(recognizer.rawLogDir, captureLabel + ".raw").delete();
    }





    /**
     * class used to hold info about heard words in recognition results.
     */
    public static class HeardWord {

        /**
         * hypothesis word text as in dictionary (upper case) without pronunciation tag
         */
        public String hypWord;

        /**
         * 0-based index of aligned sentence word, -1 if none
         */
        public int iSentenceWord;        // index of aligned sentence word, -1 if none

        /**
         * degree of match to sentence word coded as follows
         */
        public int matchLevel;


        /**
         * default value: no information
         */
        public static final int MATCH_UNKNOWN = 0;
        /**
         * heard wrong word
         */
        public static final int MATCH_MISCUE = 1;
        /**
         * heard truncated prefix of word
         */
        public static final int MATCH_TRUNCATION = 2;
        /**
         * heard exact match
         */
        public static final int MATCH_EXACT = 3;


        /**
         * start time of word, milliseconds since epoch
         */
        public long startTime;
        /**
         * end time of word, milliseconds since epoch
         */
        public long endTime;
        /**
         * start of word in centiseconds since utterance start
         */
        public long startFrame;
        /**
         * end of word in centiseconds since utterance start
         */
        public long endFrame;
        /**
         * start time of utterance, ms since epoch
         */
        public long utteranceStartTime;
        /**
         * utterance ID used for capture file
         */
        public String utteranceId;
        /**
         * ms of silence that preceded word
         */
        public int silence;
        /**
         * ms from end of reading of previous sentence word to start of this one
         */
        public int latency;

        protected HeardWord(String asrWord) {
            hypWord = asrWordText(asrWord);            // strip any pronunciation tags
            iSentenceWord = -1;
            matchLevel = MATCH_UNKNOWN;
            startTime = -1;
            endTime = -1;
            startFrame = -1;
            endFrame = -1;
            utteranceStartTime = -1;
            utteranceId = "";
            silence = -1;
            latency = -1;
        }

        /**
         * return word text stripped from possible parenthesized alternate pronunciation tag in sphinx result words
         */
        protected static String asrWordText(String taggedWord) {
            int iParen = taggedWord.indexOf('(');
            return (iParen >= 0) ? taggedWord.substring(0, iParen) : taggedWord;
        }
    }

    /**
     * utility function to convert text string into canonical-format word array
     *
     * @param text -- text string including punctuation
     */
    public static String[] textToWords(String text) {
        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        return text.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");
    }


}
