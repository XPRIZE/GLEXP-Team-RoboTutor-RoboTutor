//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
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

package edu.cmu.xprize.listener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IReadyListener;
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

    protected String  captureLabel = "";          // label for capture, logging files
    protected boolean IS_LOGGING   = false;

    protected File    configFile;                // config file to use, null => default
    protected File    modelsDir;                 // saved model directory
    protected LogMath logMath;                   // needed for creating Fsgs

    private String acousticModel = LCONST.KIDS;  // LCONST.KIDS | LCONST.ADULT

    protected String   userID;                   // User ID


    // to work around pocketsphinx timing bug: when recognizing continuously across silent pauses,
    // after a pause hyp words from a speech segments before the pause have their reported frame times
    // changed. We use this to save the original pre-pause results
    protected HeardWord[] prePauseResult = null;  // saved results from utterances prior to pause

    protected BufferedWriter bw = null;           // for writing language model files

    protected IAsrEventListener eventListener;    // where to send client notification callbacks

    protected static final String SENTENCE_SEARCH = "sentence";       // label for our search in decoder
    protected static final String JSGF_SEARCH     = "jsgf_search";    // label for our search in decoder

    // This is used to map language "Features" to the associated dictionary filenames
    // Dictionary files are located in the assets/sync/models/lm
    // Note: on Android these are case sensitive filenames
    //
    static private HashMap<String, String> dictMap = new HashMap<String, String>();

    static {
        dictMap.put("LANG_EN", "CMU07A-CAPS.DIC");
        dictMap.put("LANG_SW", "SWAHILI.DIC");
    }

    static private boolean       isReady = false;
    static private String  TAG = "ListenerBase";


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
        setupRecognizer(assets.getExternalDir(), configFile, dictMap.get(langFTR));
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


    /**
     * Construct and initialize the speech recognizer
     */
    protected void setupRecognizer(File assetsDir, File configFile, String langDictionary) {

        try {
            // save path to modelsDir for use when finding fsgs
            modelsDir = new File(assetsDir, "models");

            // if caller specified a configFile, take parameters from that.
            // In this config file must specify *all* non-default pocketsphinx parameters
            if (configFile != null) {
                recognizer = SpeechRecognizerSetup.setupFromFile(configFile).getRecognizer();

            } else {    // init using default config parameters

                switch(acousticModel) {
                    case LCONST.KIDS:

                        // create pocketsphinx SpeechRecognizer using the SpeechRecognizerSetup factory method

                        recognizer = SpeechRecognizerSetup.defaultSetup()
                                // our pronunciation dictionary
                                .setDictionary(new File(modelsDir, "lm/" + langDictionary))

                                // our acoustic model
                                .setAcousticModel(new File(modelsDir, "hmm/en-con-ind"))

                                // this automatically logs raw audio to the specified directory:
                                .setRawLogDir(assetsDir)
                                .setBoolean("-verbose", true)            // maximum log output

                                .setFloat("-samprate", 16000f)

                                .setInteger("-nfft", 512)

                                .setInteger("-frate", 100)

                                .setFloat("-lowerf", 50f)

                                .setFloat("-upperf", 6800f)

                                .setBoolean("-dither", true)

                                .setInteger("-nfilt", 40)

                                .setInteger("-ncep", 13)

                                .setString("-agc", "none")
                                .setFloat("-ascale", 1f)                // 20 in default
                                .setBoolean("-backtrace", true)         // no in default

                                .setDouble("-beam", 1e-80)		        // 1e-48 in default

                                .setBoolean("-bestpath", false)		    // yes in default

//                                .setString("-cmn", "current")
                                .setString("-cmn", "prior")
                                .setBoolean("-compallsen", false)
                                .setBoolean("-dictcase", false)
                                .setFloat("-fillprob", 1e-2f)           // 1e-8 in default
                                .setBoolean("-fwdflat", false)          // yes in default
                                .setInteger("-latsize", 5000)
                                .setFloat("-lpbeam", 1e-5f)	            // 1e-40 in default

                                .setDouble("-lponlybeam", 7e-29)        //

                                .setFloat("-lw", 10f)   	            // 6.5 in default
                                .setInteger("-maxhmmpf", 1500)          // 10000 in default
                                //.setInteger("-maxnewoov", 5000)         // 20 in default

                                .setDouble("-pbeam", 1e-80)             // 1e-48 in default

                                .setFloat("-pip", 1f)

                                .setBoolean("-remove_noise", true)     // yes in default
                                .setBoolean("-remove_silence", true)   // yes in default

                                .setFloat("-silprob", 1f)               // 0.005 in default
                                .setInteger("-topn",  4)

                                .setDouble("-wbeam", 1e-60)             // 7e-29 in default

                                .setFloat("-wip",  1f)                  // 0.65 in default

                                .getRecognizer();

                        break;

                    case LCONST.ADULT:

                        // create pocketsphinx SpeechRecognizer using the SpeechRecognizerSetup factory method

                        recognizer = SpeechRecognizerSetup.defaultSetup()
                                // our pronunciation dictionary
                                //.setDictionary(new File(modelsDir, "lm/CMU07A-CAPS.DIC"))
                                .setDictionary(new File(modelsDir, "lm/" + langDictionary))

                                // our acoustic model
                                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))

                                // this automatically logs raw audio to the specified directory:
                                .setRawLogDir(assetsDir)

		              /* can't get sphinx logfile on Android, log messages go to LogCat facility instead
                        .setString("-logfn", new File(assetsDir, logName).getPath())
		               */
                                .setBoolean("-verbose", true)            // maximum log output

                                // a few other settings we might want to experiment with:

                                // threshold for voice activity detection:
                                .setFloat("-vad_threshold", LCONST.VAD_THRESHOLD)       // default 2.0
                                // other vad parameters:
                                // .setInteger("vad_postspeech", 50)		    // default 50 (centiseconds)
                                // .setInteger("vad_prespeech", 10)				// default 10 (centiseconds)

                                // .setFloat("-silprob", 0.005f)				// default 0.005
                                .setFloat("-fillprob", LCONST.FILLPROB)                 // default 1e-8f
                                // .setFloat("-wip", 0.65f)						// default 0.65

                                .getRecognizer();
                        break;
                }
            }

            // save a log math object to use when constructing FsgModels.
            logMath = new LogMath();
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Recognizer configuration error: ", e, false);
        }
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


    public void listenForSentence(String[] wordsToHear, int startWord){}


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
        if (recognizer != null)
            recognizer.setPauseRecognizer(pauseListener);
    }

    /**
     * return whether or not the listener is alive and actively listening.
     * @return
     */
    public boolean isListening() {

       return (recognizer != null)? recognizer.isListening(): false;
    }

    public void reInitializeListener(boolean restartListener) {
        recognizer.setRestartListener(restartListener);
    }

    public void configTimedEvent(int eventType, long newTimeout) {
        recognizer.configTimedEvent(eventType, newTimeout);
    }

    public void resetTimedEvent(int eventType) {
        recognizer.resetTimedEvent(eventType);
    }

    public void configStaticEvent(int eventType) {
        recognizer.configStaticEvent(eventType);
    }

    public void resetStaticEvent(int eventType) {
        recognizer.resetStaticEvent(eventType);
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

        public HeardWord(String asrWord) {
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

        private static final String[] fieldsToPrint = {"hypWord", "iSentenceWord", "matchLevel", "startTime", "endTime", "utteranceStartTime", "utteranceId", "silence", "latency"};

        public String toString() {
            StringBuilder msg = new StringBuilder();

            msg.append("{");
            for (String fieldName : fieldsToPrint) {
                try {
                    Field field = this.getClass().getDeclaredField(fieldName);
                    msg.append(fieldName);
                    msg.append(": ");
                    msg.append(field.get(this));
                    msg.append(", ");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            msg.delete(msg.length() - 2, msg.length());
            msg.append("}");

            return msg.toString();
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



    /***** Logging */



    /**
     * get the  path to the hypothesis log file for given utterance label
     */
    protected File getHypLogFile(String utteranceLabel) {
        // store it alongside the captured audio file
        return new File(recognizer.rawLogDir, utteranceLabel + "-log.txt");
    }

    /**
     * get time stamp string for current time in milliseconds
     */
    protected String timestampMillis() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS", Locale.US).format(new Date(System.currentTimeMillis()));
    }

    // create and write header of hypothesis log file.
    protected void beginHypLog() {
        Log.i("beginHypLog", "starting hypothesis log");
    }

    // log a partial hypothesis
    protected void logHyp(String timestamp, String hyp, List<Segment> segments, HeardWord[] heardWords) {
        try {
            File hypLog = getHypLogFile(captureLabel);
            BufferedWriter bw = new BufferedWriter(new FileWriter(hypLog.getPath(), true));    // appends

            // write out both the raw result with pocketsphinx times for debugging, and
            // then the adjusted times we have computed for comparison with offline results
            bw.write("## FROM GET PARTIAL RESULT:\n");        // as in reading tutor
            bw.write("    TIME: " + timestamp + "\n");
            bw.write("  DECODER OUTPUT: " + hyp + "\n");
            bw.write("  RAW SEGMENTS:\n");
            for (Segment s : segments) {
                bw.write(s.getWord() + " " + s.getStartFrame() + " " + s.getEndFrame() + "\n");
            }
            bw.write("  SEGMENTATION:\n");
            for (HeardWord hw : heardWords) {
                bw.write(hw.hypWord + " " + hw.startFrame + " " + hw.endFrame + "\n");
            }
            bw.write("\n");

            bw.close();
        } catch (Exception e) {
            Log.e("logHyp", "Error writing hypothesis log file " + e.getMessage());
        }
    }


}
