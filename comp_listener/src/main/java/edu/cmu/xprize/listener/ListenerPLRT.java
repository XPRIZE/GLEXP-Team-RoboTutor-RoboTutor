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
 * This is the basic Project Listen Reading Tutor implementation
 *
 * The main Android Listener class. Encapsulates a SpeechRecognizer object to manage the pocketsphinx decoder
 * Implements algorithms from the Project Listen Reading Tutor to customize and enhance the speech recognition
 * for the task of oral reading.
 */
public class ListenerPLRT extends ListenerBase {

    // state for the current ListenFor operation

    private String      sentenceWords[];             // array of sentence words to hear
    private int         iExpected = 0;               // index of expected next word in sentence
    private int         iNextWord = 0;               // Next word expected.
    private HeardWord[] heardWords = null;           // latest total aligned hypothesis
    private long        sentenceStartTime;           // time in ms since epoch
    private long        sentenceStartSamples;        // sample counter at sentence start, for adjusting frame numbers
    private final int   SAMPLES_PER_FRAME = 160;     // number of samples in a centisecond frame at 16000 samples/sec
    private boolean     useTruncations = true;       // Flag whether or not to use truncations.
    private boolean     speaking = false;            // speaking state. [currently unused]

    /**
     * Attach event listener to receive notification callbacks
     */
    public void updateNextWordIndex(int next) {
        Log.d("ASR", "Update NextWord: " + iNextWord);
        iNextWord = next;
    }

    /**
     * Set the words to listen for and the starting position
     *
     * @param wordsToHear -- array of upper-case ASR dictionary words
     * @param startWord   -- 0-based index of word to expect next
     */
    public void listenFor(String[] wordsToHear, int startWord) {

        Log.d("STABLE", "ListenFor: " + TextUtils.join(" ", wordsToHear));

        // try to build the language model. Note this updates dictionary attached to decoder
        FsgModel fsg = generateLM(wordsToHear, startWord);

        if (fsg == null) {
            Log.e("listenFor", "Failed to get language model for " + TextUtils.join(" ", wordsToHear));
            return;
        }

        // Remember the current sentence words and start position -
        // The start position is used by MultiMatch to align the hypothesis with the sentence.
        sentenceWords = wordsToHear;
        iExpected     = startWord;

        // start listening
        if (recognizer != null) {

            // register our language model in the decoder
            // Note that this replaces any model of the same name -
            // i.e. SENTENCE_SEARCH - see: pocketsphinx.c:set_search_internal
            recognizer.addFsgSearch(SENTENCE_SEARCH, fsg);

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
            recognizer.startListening(SENTENCE_SEARCH, captureLabel);

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

        super.setupRecognizer(assetsDir, configFile, langDictionary);

        // use a private implementation to receive events from pocketsphinx
        recognizer.addListener(new IPocketSphinxListener());
    }


    // private inner class to hide our event listener implementation.
    // We receive these events from the SpeechRecognizer object for our own use, and send similar events from the
    // IAsrEventListener interface to our client app
    private class IPocketSphinxListener implements ITutorListener {

        @Override
        public void onStableResult(String[] hypothesis) {
            // NOTE: that hypothesis may be null during shutdown
            if(hypothesis != null) {
                Log.i("ASR", "Part Hyp: " + TextUtils.join(" ", hypothesis));
                processHypothesis(hypothesis, false);
            }
        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            // NOTE: that hypothesis may be null during shutdown
            if(hypothesis != null) {
                Log.i("ASR", "Part Hyp: " + hypothesis.getHypstr());
                processHypothesis(hypothesis, false);
            }
        }

        @Override
        public void onResult(Hypothesis hypothesis) {
            // NOTE: that hypothesis may be null during shutdown
            if(hypothesis != null) {
                Log.i("ASR", "Final Hyp: " + hypothesis.getHypstr());
                processHypothesis(hypothesis, true);
            }
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


    //------------------------------------------------------------------------------------------------
    // helpers for dealing with "asrWords": word strings as they occur in the pronunciation dictionary:
    //    - upper-case by our convention
    //    - may include parenthesized alternate pronunciation suffix, e.g THE(2)
    // wordsToHear coming in should be array of ASR words without pronunciation tags so
    // sentence words dealt with in this module can be assumed to be untagged asrWords
    //------------------------------------------------------------------------------------------------

    /**
     * true if sphinx dictionary asrWord is an exact match to sentence word
     */
    private static boolean asrWordMatches(String asrWord, String sentenceWord) {
        return HeardWord.asrWordText(asrWord).equals(sentenceWord);
    }

    /**
     * true if sphinx dictionary asrWord is a truncation of sentence word
     */
    private static boolean asrWordIsTruncationOf(String asrWord, String sentenceWord) {
        return HeardWord.asrWordText(asrWord).equals("START_" + sentenceWord);
    }

    /**
     * return asrWord representing truncation of given word in our dictionary
     */
    private static String startWord(String word) {
        return "START_" + word;
    }

    /**
     * get tagged name of the Nth pronunciation of base word (ord: 1-based ordinal)
     */
    private static String NthPronName(String word, int ord) {
        return ord == 1 ? word : word + "(" + ord + ")";        // default pronunciation 1 has no tag
    }


    // ------------------------------------------------
    // Language model generation
    // ------------------------------------------------

    /** generate the language model for given asr words
     *
     * @param wordsToHear
     * @param startWord
     * @return
     */
    private FsgModel generateLM(String[] wordsToHear, int startWord) {

        // ensure all sentence words in dictionary
        //
        HashSet<String> wordSet = new HashSet<>(Arrays.asList(wordsToHear));

        for (String word : wordSet) {

            if (recognizer.decoder.lookupWord(word) == null) {    // word not in dictionary

                // Synthesize a pronunciation using English rule-based synthesizer
                //
                String phonemes = Phoneme.toPhoneme(word).trim();

                if (phonemes.isEmpty())
                    continue;

                Log.i("generateLM", "addWord " + word + " pronunciation " + phonemes);
                recognizer.decoder.addWord(word, phonemes, 1); // more efficient to pass 1 (true) on last word only?
            }

            // ensure START_ words for truncated readings are in dictionary
            if(useTruncations && recognizer.decoder.lookupWord(startWord(word)) == null) {
                addTruncations(word);
            }
        }

        // have to write to a temporary file to create LM
        //
        String filename = "lm/fsg.txt";
        File fsgFile = new File(modelsDir, filename);

        try {
            FileWriter fw = new FileWriter(fsgFile.getPath(), false);    // false to overwrite rather than append to existing file
            bw = new BufferedWriter(fw);

            // LM probabilities.
            final double PrCorrect = 0.9;
            final double PrEndEarly = 0.01;
            final double PrTruncate = 0.001;
            final double PrResume = 0.001;
            final double PrRestart = 0.05;
            final double PrRepeat = 0.03;
            final double PrJump = 0.03;

            // write the fsg file header info
            int state_count = wordsToHear.length + 1;
            int final_state = state_count - 1;
            bw.write("FSG_BEGIN sentence\n");
            bw.write("NUM_STATES " + state_count + "\n");
            bw.write("START_STATE " + startWord + "\n");
            bw.write("FINAL_STATE " + final_state + "\n");

            // factor to normalize transition probabilities based on sentence length
            int n = wordsToHear.length - 1;
            if (n < 1) n = 1;

            // add state transitions
            for (int i = 0; i < state_count - 1; i++) {
                // emit word i for transition from state i to i + 1 with probability PrCorrect
                AddFSGTransition(i, i + 1, PrCorrect, wordsToHear[i]);

                //if this is not the last word of the sentence emit null word from transition from state i to the final state with probability PrEndEarly
                if (i != final_state - 1) {
                    AddFSGTransition(i, final_state, PrEndEarly, "");
                }

                // truncations not yet implemented for words not in dictionary
                if(useTruncations && recognizer.decoder.lookupWord(startWord(wordsToHear[i])) != null) {
                    //emit word i truncation for transition from state i to state i with probability PrTruncate
                    AddFSGTransition(i, i, PrTruncate, startWord(wordsToHear[i]));

                    //emit word i truncation for transition from state i to i + 1 with probability PrResume
                    AddFSGTransition(i, i + 1, PrResume, startWord(wordsToHear[i]));
                }

                //if i <> 0 emit null word for jump from state i back to state 0 with probability PrRestart
                if (i != 0) {
                    AddFSGTransition(i, 0, PrRestart / n, "");
                }

                //emit word i for transition from state i to state i with probability PrRepeat
                AddFSGTransition(i, i, PrRepeat / n, wordsToHear[i]);

                // emit null word for jump from state i to state j with probability PrJump for all states j except state 0
                for (int j = 1; j < state_count - 1; j++) {
                    if (i != j) {
                        AddFSGTransition(i, j, PrJump / n, "");
                    }
                }
            }

            // add jump from final state back to start with probability PrRestart
            AddFSGTransition(final_state, 0, PrRestart / n, "");

            // add jump from final state back to each earlier state
            for (int st = 1; st < state_count - 1; st++) {
                AddFSGTransition(final_state, st, PrJump / n, "");
            }

            // done writing the file
            bw.write("FSG_END\n");
            bw.close();
        } catch (IOException e) {
            Log.e("generateLM", "Error writing lm file: " + e);
            return null;
        }

        // now read it back in to get pocketsphinx to build the fsg
        return new FsgModel(fsgFile.getPath(), logMath, LCONST.LMWEIGHT);
    }

    private void AddFSGTransition(int from, int to, double prob, String word) throws IOException {
        bw.write("TRANSITION " + from + " " + to + " " + prob + " " + word + "\n");
    }


    // add entries for all truncations of given ASR word to dictionary
    private void addTruncations(String word) {
        String truncWord = startWord(word);

        // add base entry with a dummy pronunciation which should never be used
        String dummyPron = "DH IH S D AH M IY SH UH D N EH V ER HH AE P AH N";
        recognizer.decoder.addWord(truncWord, dummyPron, 1);

        // do for each pronunciation we find of the word
        for (int i = 1; ; i++) {
            String pron = recognizer.decoder.lookupWord(NthPronName(word, i));
            if (pron == null) break;    // quit when no more pronunciations found

            String[] phonemes = pron.split(" ");
            // truncation includes at least two phonemes and omits at least last two phonemes to prevent false alarms
            for (int truncLen = 2; truncLen <= phonemes.length - 2; truncLen++) {
                String truncPron = TextUtils.join(" ", Arrays.copyOfRange(phonemes, 0, truncLen)); // end arg index is exclusive
                String altPronTag = i == 1 ? "" : i + ":";
                String truncName = truncWord + "(" + altPronTag + truncPron.replace(' ', '_') + ")";
                Log.i("addTruncations", "addWord " + truncName + " pronunciation " + truncPron);
                recognizer.decoder.addWord(truncName, truncPron, 1);
            }
        }
    }


    // handle a partial or final hypothesis from pocketsphinx
    //
    private void processHypothesis(Hypothesis hypothesis, Boolean finalResult) {

        if (hypothesis == null) return;

        // get array of hypothesis words
        String[] asrWords = hypothesis.getHypstr().split("\\s+");

        processHypothesis(asrWords, finalResult);
    }


    // handle a partial or final hypothesis from pocketsphinx
    //
    private void processHypothesis(String[] asrWords, Boolean finalResult) {

        String timestamp = timestampMillis();    // save receipt timestamp for logging

        if (sentenceWords == null) {
            Log.w("processHypothesis", "null sentenceWords, hyp ignored");
            return;
        }

        if (asrWords.length < 1)
            return;

        // get the list of segments
        ArrayList<Segment> segments = new ArrayList<>();
        for (Segment s : recognizer.decoder.seg()) {
            segments.add(s);
        }

        // optional: strip last hyp word if it is not terminated by silence because it is unreliable
        String[] wordsToUse = asrWords;

        if (LCONST.LAST_WORD_LAG) {

            // Find word of last segment in the segmentation detail
            String lastSegmentWord = null;

            for (Segment s : segments) {
                lastSegmentWord = s.getWord();
                Log.d("ASR","segment word: " + lastSegmentWord);
            }

            // Last segment could be silence or filler. Just ensure last segment != last hyp word
            if (lastSegmentWord != null && lastSegmentWord.equals(asrWords[asrWords.length - 1])) {
                Log.i("lag", "ignoring trailing hypword " + lastSegmentWord);
                wordsToUse = Arrays.copyOfRange(asrWords, 0, asrWords.length - 1);
            }
        }

        if (wordsToUse.length >= 1) {

            long multimatchTimer = System.currentTimeMillis();

            // align hyp words with sentence words
            heardWords = doMultiMatch(wordsToUse, sentenceWords);

            Log.d("ASR", "Time in MM: " + (System.currentTimeMillis() - multimatchTimer));

            // fill in detailed word timing metrics
            getWordTimes(heardWords, sentenceWords, segments);

            // post update to client component
            //
            if (eventListener != null) {
                eventListener.onUpdate(heardWords, finalResult);
            }

            // log the partial hypothesis
            if(IS_LOGGING)
                logHyp(timestamp, TextUtils.join(" ", asrWords), segments, heardWords);
        }
    }

    // ------------------------------------------------
    // Multimatch sentence word alignment algorithm:
    // ------------------------------------------------

    static private class MultiMatchScore {    // record kept for one possible word alignment
        int cost;            // penalty for this alignment
        int nMatches;        // number of word matches for this alignment
        int iPrev;            //  sentence index of previous hyp word's alignment

        MultiMatchScore(int inCost, int inMatches, int inPrev) {
            cost = inCost;
            nMatches = inMatches;
            iPrev = inPrev;
        }

        MultiMatchScore() {            // init to very high score before searching for minimum
            cost = 1000000;
            nMatches = 0;
            iPrev = -1;
        }
    }


    // Multimatch costs: scaled by 100 from RT version to use integer arithmetic

    // cost for mismatch hypWord with sentenceWord
    private int mismatchCost(String hypWord, String sentenceWord) {
        if (asrWordMatches(hypWord, sentenceWord))
            return 0;

        if(useTruncations && asrWordIsTruncationOf(hypWord, sentenceWord))
            return 0;

        // else mismatch
        return 100;
    }

    // cost of jump from position i to j
    private int jumpCost(int from, int to) {
        // different cost when LeftToRight alignment is configured
        if (LCONST.ALIGN_L2R)
            return jumpCostL2R(from, to);

        // else normal "chase the reader" alignment
        if (to == from + 1)    // no cost for sequential reading
            return 0;
        if (to == from)        // small cost so advancing over HO HO beats repeating HO
            return 1;
        return 100;            // cost of a jump, any direction or size
    }

    // cost of jump when L2R alignment is being used
    private int jumpCostL2R(int from, int to) {
        if (to == from + 1)    // no cost for sequential reading
            return 0;
        if (to == from)        // small cost so advancing over HO HO beats repeating HO
            return 1;
        if (to == from + 2)    // skip one word: normal jump cost
            return 100;

        return 999999;        // very high cost for any other jump
    }

    // find least-cost alignment of hypWords to sentenceWords
    @SuppressLint("NewApi")
    private HeardWord[] doMultiMatch(String[] hypWords, String[] sentenceWords) {
        // build array or HeardWord's to hold multimatch result
        ArrayList<HeardWord> heardWords = new ArrayList<>();
        ArrayList<MultiMatchScore[]> multiMatchScores = new ArrayList<>();

        int costCalcWords = 0;
        // store scores in matrix, one row per hypWord with one column for each sentence position it could be aligned with
        for (int h = 0; h < hypWords.length; h++) {
            MultiMatchScore multiMatchScore[] = new MultiMatchScore[sentenceWords.length];

            for (int s = 0; s < sentenceWords.length; s++) {

                //@@ TODO: remove this dependency
//                if (!ReadingTutorActivity.isWordCredited(s))
//                    break;
                // This is an experiment - should eliminate matches past expected word.
                // TODO: TEST -> probably needs to be updated dynamically
                if(s > iNextWord+1)
                    break;

                int mismatchCostHere = mismatchCost(hypWords[h], sentenceWords[s]);    // match cost this position
                int matchesHere = asrWordMatches(hypWords[h], sentenceWords[s]) ? 1 : 0;

                if (h == 0) {    // first row, no predecessor => compute jump cost from just before expected start word
                    int cost = mismatchCostHere + jumpCost(iExpected - 1, s);
                    multiMatchScore[s] = new MultiMatchScore(cost, matchesHere, -1);
                    costCalcWords++;
                } else {
                    // find lowest cost we can achieve here from each possible previous hypword alignment
                    MultiMatchScore prevWord[] = multiMatchScores.get(h - 1);
                    MultiMatchScore best = new MultiMatchScore();    // best found so far
                    for (int j = 0; j < costCalcWords; j++) {
                        int cost = prevWord[j].cost + mismatchCostHere + jumpCost(j, s);
                        int matches = prevWord[j].nMatches + matchesHere;
                        if (cost < best.cost ||
                                (cost == best.cost && matches > best.nMatches) ||
                                (cost == best.cost && matches == best.nMatches && jumpCost(j, s) == 0)) {
                            best = new MultiMatchScore(cost, matches, j);
                        }
                    }

                    // record best value possible for this hypword alignment
                    multiMatchScore[s] = best;
                }
            }
            multiMatchScores.add(h, multiMatchScore);
            heardWords.add(h, new HeardWord(hypWords[h]));
        }

        // search last row to find best possible alignment of last hypWord
        int hLast = heardWords.size() - 1;
        int best_alignment = -1;

        MultiMatchScore best = new MultiMatchScore();
        MultiMatchScore multiMatchScore[] = multiMatchScores.get(hLast);

        for (int i = 0; i < costCalcWords; i++) {
            if (multiMatchScore[i].cost < best.cost ||
                    (multiMatchScore[i].cost == best.cost && multiMatchScore[i].nMatches > best.nMatches)) {
                best = multiMatchScore[i];
                best_alignment = i;
            }
        }

        // follow predecessor links backwards through rows to record best alignment of each preceding hyp word
        HeardWord heardWord;
        for (int h = heardWords.size() - 1; h >= 0; --h) {
            heardWord = heardWords.get(h);
            heardWords.remove(h);
            heardWord.iSentenceWord = best_alignment;

            // record match type
            if (asrWordMatches(hypWords[h], sentenceWords[best_alignment]))
                heardWord.matchLevel = HeardWord.MATCH_EXACT;
            else if(useTruncations && asrWordIsTruncationOf(hypWords[h], sentenceWords[best_alignment]))
                heardWord.matchLevel = HeardWord.MATCH_TRUNCATION;
            else if (!hypWords[h].isEmpty())    // sanity check
                heardWord.matchLevel = HeardWord.MATCH_MISCUE;

            // would also record lots of other context about hypWord here

            // update alignment to best predecessor alignment
            best_alignment = multiMatchScores.get(h)[best_alignment].iPrev;
            heardWords.add(h, heardWord);
        }

        // return the aligned word array
        HeardWord words[] = new HeardWord[heardWords.size()];
        words = heardWords.toArray(words);
        return words;
    }

    // fill in word timings for heard words
    private void getWordTimes(HeardWord[] heardWords, String[] sentenceWords, List<Segment> segments) {
        // segments contain more than just hyp words:
        // <sil> for silence, fillers like [COUGH], and (NULL) for null transitions
        // also segments contain altpron tags, while hyp words normally don't (but might with some options?)
        // so walk segment list, matching against hyp words in heardWords array
        // can be segments like <sil> after the last matching hypword, so quit after last hyp word
        int h = 0;            // index of next hypword to match
        for (Segment s : segments) {
            if (h < heardWords.length &&
                    HeardWord.asrWordText(s.getWord()).equals(HeardWord.asrWordText(heardWords[h].hypWord))) {    // segment matches next hyp word

                // fill in utterance boilerplate
                heardWords[h].utteranceStartTime = sentenceStartTime;
                heardWords[h].utteranceId = captureLabel;

                // get start and end frames (centiseconds) relative to start of sentence
                long sentenceStartFrame = sentenceStartSamples / SAMPLES_PER_FRAME;
                heardWords[h].startFrame = s.getStartFrame() - sentenceStartFrame;
                heardWords[h].endFrame = s.getEndFrame() - sentenceStartFrame;

                // get start and end clock times in milliseconds
                heardWords[h].startTime = sentenceStartTime + heardWords[h].startFrame * 10;
                heardWords[h].endTime = sentenceStartTime + heardWords[h].endFrame * 10;

                if (++h >= heardWords.length)    // have matched all heardWords
                    break;
            }
        }

        // work around pocketsphinx bug:
        // patch in previously-computed timings for hyp words before most recent pause
        if (prePauseResult != null && prePauseResult.length <= heardWords.length) {
            System.arraycopy(prePauseResult, 0, heardWords, 0, prePauseResult.length);
        }

        // Now that we have correct times, fill in derived measures latency and silence
        addLatency(heardWords);
    }

    // add in latency and silence measurements for heard words
    private void addLatency(HeardWord[] heardWords) {
        for (int h = 0; h < heardWords.length; h++) {
            HeardWord hw = heardWords[h];        // current hyp word

            if (h == 0) { // special case for first word
                // silence is time from start of utterance
                hw.silence = (int) (hw.startTime - hw.utteranceStartTime);
                continue;    // latency undefined for first word, remains -1 as initialized
            }
            // else on non-first word

            // silence is time from end of previous word.
            hw.silence = (int) (hw.startTime - heardWords[h - 1].endTime);

            // Following gets "best case" [most charitable] latency as computed by the Reading Tutor: if current word is aligned
            // with sentence word s, get time from the end of most recent hyp word aligned with sentence word s-1.
            if (hw.iSentenceWord > 0) {
                for (int hPrev = h - 1; hPrev >= 0; hPrev--) {    // search backwards through prior hyp words
                    HeardWord priorHypWord = heardWords[hPrev];

                    if (priorHypWord.iSentenceWord == hw.iSentenceWord - 1) {
                        hw.latency = (int) (hw.startTime - priorHypWord.endTime);
                        break;
                    }
                }
            }
        }
    }


    // create and write header of hypothesis log file.
    @Override
    protected void beginHypLog() {
        Log.i("beginHypLog", "starting hypothesis log");
        try {
            File hypLog = getHypLogFile(captureLabel);
            BufferedWriter bw = new BufferedWriter(new FileWriter(hypLog.getPath(), false));

            String configPath = "<none>";
            if (this.configFile != null)
                configPath = this.configFile.getPath();

            // write header information. Not exactly the same format as ReadingTutor .hea file
            bw.write("UTTERANCE ID: " + captureLabel + "\n");
            bw.write("CAPTURE FILE:" + getCaptureFile(captureLabel).getPath() + "\n");
            bw.write("START TIME: " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS", Locale.US).format(new Date(sentenceStartTime)) + "\n");
            bw.write("START TIME (ms): " + sentenceStartTime + "\n");
            bw.write("START_SAMPLE: " + sentenceStartSamples + "\n");
            bw.write("START_FRAME: " + sentenceStartSamples / SAMPLES_PER_FRAME + "\n");
            bw.write("CONFIG FILE: " + configPath + "\n");
            bw.write("TEXT: " + TextUtils.join(" ", sentenceWords) + "\n");
            bw.write("\n\n");

            bw.close();
        } catch (IOException e) {
            Log.e("beginHypothesisLog", "Error writing hypothesis log file " + e);
        }
    }


} // end Listener class
