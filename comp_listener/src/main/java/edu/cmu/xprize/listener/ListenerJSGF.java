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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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
 * This implementation uses the JSpeech Grammar Format
 *
 * The main Android Listener class. Encapsulates a SpeechRecognizer object to manage the pocketsphinx decoder
 * Implements algorithms from the Project Listen Reading Tutor to customize and enhance the speech recognition
 * for the task of oral reading.
 */
public class ListenerJSGF extends ListenerBase {

    // state for the current ListenFor operation

    private String sentenceWords[];             // array of sentence words to hear
    private int    iExpected = 0;               // index of expected next word in sentence
    private int    iNextWord = 0;               // Next word expected.
    private HeardWord[] heardWords = null;      // latest total aligned hypothesis
    private long   sentenceStartTime;           // time in ms since epoch
    private long   sentenceStartSamples;        // sample counter at sentence start, for adjusting frame numbers
    private final int SAMPLES_PER_FRAME = 160;  // number of samples in a centisecond frame at 16000 samples/sec
    private boolean speaking = false;           // speaking state. [currently unused]

    public final static String   ROBOTUTOR_DATA_PATH     = Environment.getExternalStorageDirectory().getPath();
    //public final static String ROBOTUTOR_ASSETS_JSGF   =  "/RoboTutor/util/asr/";
    public final static String   ROBOTUTOR_ASSETS_JSGF   =  "/RoboTutor/";
    public final static String   JSGF_TEMP               =  "jsgf.gram";

    public String testGrammar = "#JSGF V1.0;\n" +
            "\n" +
            "grammar sentence;\n" +
            "\n" +
            "<word> = FOR|FORWARD|WARD;\n" +
            "public <sentence> = <word> <word> <word> <word> <word> <word> <word> <word> <word> <word> <word>;\n";

//            "<word> =  SIKU| MOJA| MIMI |NA |DADA |YANGU| TULIKUWA| TUKITEMBEA |KARIBU |NA |BARABARA;\n" +
//            "public <sentence> = <word> <word> <word> <word> <word> <word> <word> <word> <word> <word> <word>;\n";
                    //Siku moja, mimi na dada yangu tulikuwa tukitembea karibu na barabara.


    /**
     * Attach event listener to receive notification callbacks
     */
    public void updateNextWordIndex(int next) {
        iNextWord = next > 0? next:0;
    }


    // ------------------------------------------------
    // Language model generation
    // ------------------------------------------------

    // generate the language model for given asr words
    private void generateLM(String[] wordsToHear) {

        // ensure all sentence words in dictionary
        HashSet<String> wordSet = new HashSet<>(Arrays.asList(wordsToHear));

        for (String word : wordSet) {
            if (recognizer.decoder.lookupWord(word) == null) {    // word not in dictionary

                // Synthesize a pronunciation using English rule-based synthesizer
                String phonemes = Phoneme.toPhoneme(word).trim();
                if (phonemes.isEmpty())
                    continue;
                Log.i("generateLM", "addWord " + word + " pronunciation " + phonemes);
                recognizer.decoder.addWord(word, phonemes, 1); // more efficient to pass 1 (true) on last word only?
            }
        }
    }


    public void listenFor(String[] wordsToHear, int startWord) {

        // Ensure all the words are in the language model
        //
        generateLM(wordsToHear);

        // generate a grammar that allows any words
        //
        String grammar = "#JSGF V1.0;\n" + "\n" +
                "grammar sentence;\n" +
                "\n" +
                "<word> = " + TextUtils.join("|", wordsToHear) + ";\n" +
                "public <sentence> = ";

        for(int i1 = 0; i1 < wordsToHear.length ; i1++) {
            grammar += "<word>";
        }
        grammar += ";\n";

        // Listen for a sequence
        //
        listenFor(grammar);
    }


    public void listenForSentence(String[] wordsToHear, int startWord) {

        // Ensure all the words are in the language model
        //
        generateLM(wordsToHear);

        // generate a grammar that allows any words
        //
        String grammar = "#JSGF V1.0;\n" + "\n" +
                "grammar sentence;\n" +
                "\n" +
                "public <sentence> = " + TextUtils.join(" ", wordsToHear) + ";\n";

        // Listen for a sequence
        //
        listenFor(grammar);
    }


    public void listenFor(String jSgrammar) {

        String   outPath;

        outPath = ROBOTUTOR_DATA_PATH + ROBOTUTOR_ASSETS_JSGF;

        File outputFile = new File(outPath);

        if(!outputFile.exists())
            outputFile.mkdir();

        outPath +=  JSGF_TEMP;

        try {
            OutputStream out = new FileOutputStream(outPath);

            byte[] bytes = jSgrammar.getBytes();
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {

        }

        outputFile = new File(outPath);

        listenFor(outputFile);
    }


    /**
     * Set the words to listen for and the starting position
     *
     * @param jSgrammar
     */
    public void listenFor(File jSgrammar) {

        Log.d("ASR", "ListenFor: " + jSgrammar);

        // start listening
        if (recognizer != null) {

            // register our language model in the decoder
            // Note that this replaces any model of the same name -
            // i.e. SENTENCE_SEARCH - see: pocketsphinx.c:set_search_internal
            recognizer.addGrammarSearch(JSGF_SEARCH, jSgrammar);

            // generate and remember label from timestamp to use for captures - Logging
            captureLabel = userID + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(new Date());

            // reset prePause cache
            prePauseResult = null;

            // save stream offset of start of utterance, for converting stream-based frame times
            // to utterance-based times.
            sentenceStartSamples = recognizer.nSamples;
            // record start time now
            sentenceStartTime = System.currentTimeMillis();

            //TimerUtils.startTimer();

            // start background thread for capturing audio from microphone
            recognizer.startListening(JSGF_SEARCH, captureLabel);

            // start per-capture log file for tracing sequence of partial hypotheses for this target
            if(IS_LOGGING)
                beginHypLog();

        }
    }


    /**
     * Construct and initialize the speech recognizer
     */
    @Override
    protected void setupRecognizer(File assetsDir, File configFile, String langDictionary) {

        // save path to modelsDir for use when finding fsgs
        modelsDir = new File(assetsDir, "models");

        // if caller specified a configFile, take parameters from that.
        // In this config file must specify *all* non-default pocketsphinx parameters
        if (configFile != null) {
            recognizer = SpeechRecognizerSetup.setupFromFile(configFile).getRecognizer();

        } else {    // init using default config parameters

            // create pocketsphinx SpeechRecognizer using the SpeechRecognizer2Setup factory method
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
        }

        // save a log math object to use when constructing FsgModels.
        logMath = new LogMath();

        // use a private implementation to receive events from pocketsphinx
        recognizer.addListener(new IPocketSphinxListener());
    }


    // private inner class to hide our event listener implementation.
    // We receive these events from the SpeechRecognizer object for our own use, and send similar events from the
    // IAsrEventListener interface to our client app
    private class IPocketSphinxListener implements ITutorListener {

        @Override
        public void onStableResult(String[] hypothesis) {
            Log.i("ASR", "Part Hyp: " + TextUtils.join(" ", hypothesis));
            processHypothesis(hypothesis);
        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            Log.i("ASR", "Part Hyp: " + hypothesis.getHypstr());
            processHypothesis(hypothesis, false);
        }

        @Override
        public void onResult(Hypothesis hypothesis) {
            Log.i("ASR", "Final Hyp: " + hypothesis.getHypstr());
            processHypothesis(hypothesis, true);
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onTimeout() {

        }

        @Override
        public void onBeginningOfSpeech() {
            speaking = true;

            // forward to listener client app
            if (eventListener != null)
                eventListener.onBeginningOfSpeech();
        }

        @Override
        public void onEndOfSpeech() {
            speaking = false;

            // starting a pause: remember the alignment and timing results
            prePauseResult = heardWords;

            // forward to listener client app
            if (eventListener != null)
                eventListener.onEndOfSpeech();
        }

        @Override
        public void onASREvent(int eventType) {
            eventListener.onASREvent(eventType);
        }
    }


    // handle a partial or final hypothesis from pocketsphinx
    private void processHypothesis(Hypothesis hypothesis, Boolean finalResult) {


    }


    // handle a partial or final hypothesis from pocketsphinx
    private void processHypothesis(String[] hypothesis) {

        // post update to client component
        //
        if (eventListener != null) {
            eventListener.onUpdate(hypothesis, false);
        }
    }

} // end Listener class
