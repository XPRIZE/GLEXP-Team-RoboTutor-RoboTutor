package edu.cmu.xprize.listener;

/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 * Copyright (c) 2016 Kevin Willows.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

// Variant of the edu.cmu.pocketsphinx.SpeechRecognizer class code from
// pocketsphinx-android so we can customize it as SpeechRecognizer in our
// package. Needed to access other decoder methods.

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Exchanger;

import cmu.xprize.util.TCONST;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

import static java.lang.String.format;

/**
 * Main class to access recognizer functions. After configuration this class
 * starts a listener thread which records the data and recognizes it using
 * Pocketsphinx engine. Recognition events are passed to a client using
 * {@link RecognitionListener} This class is a variant to customize the
 * SpeechRecognizer class included in the pocketsphinx-android distribution.
 */
public class SpeechRecognizer {

    protected static final String TAG = SpeechRecognizer.class.getSimpleName();
    /**
     * the pocketsphinx Decoder, public so clients can access decoder methods
     */
    public Decoder decoder;

    //private HashMap<String, Decoder> decoderMap;

    /**
     * total number of samples passed to the decoder in the stream. Need to subtract off utterance start frame
     * to map stream-based frame counts pocketsphinx returns to utterance-based counts we want. Volatile since
     * modified by the background thread capturing from the microphone
     */
    public volatile long nSamples;
    public short         mPeak = 0;

    /**
     * size of the buffer to use. For mapping stream-based frame time, we want buffer size to be a multiple of
     * centisecond frame size = 160.  This size receives updated hypothesis 10 times a second
     */
    private static final int BUFFER_SIZE = 1600;            // 1/10 seconds worth at 16 Khz
    /**
     * directory where raw audio captures stored
     */
    public String rawLogDir;

    private Thread recognizerThread;                        // background thread handling audio data

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Collection<ITutorListener> listeners = new HashSet<>();

    private final int        sampleRate;                    // for sample rate check
    private volatile boolean isPausedRecognizer  = false;   // start in the paused state
    private volatile boolean isRunningRecognizer = true;
    private volatile boolean isDecoding          = false;   // start in the not decoding state

    private ASREvents eventManager;

    private ResultEvent nextHypothesis = null;
    private ResultEvent prevHypothesis = null;


    protected SpeechRecognizer(Config config) {
        sampleRate = (int) config.getFloat("-samprate");
        if (config.getFloat("-samprate") != sampleRate)
            throw new IllegalArgumentException("sampling rate must be integer");
        // save the configured raw log directory, for processing raw capture files
        rawLogDir = config.getString("-rawlogdir");
        decoder = new Decoder(config);
        nSamples = 0;

        eventManager = new ASREvents();
    }


    /**
     * Adds listener.
     */
    public void addListener(ITutorListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }


    /**
     * Removes listener.
     */
    public void removeListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }


    /**
     * Starts recognition. Does nothing if recognition is active.
     *
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName, String label) {

        Log.i("ASR", format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);

        if (recognizerThread == null) {
            Log.i("ASR", "New Thread");
            recognizerThread = new RecognizerThread(label, rawLogDir);
            recognizerThread.start();
        }

        return true;
    }


    /**
     * Use this to restart the listener with a new utterance.  Once the recognizer is stopped
     * in this fashion it may be restarted with setPauseListener or reInitializeListener - false
     *
     * Utterance: An unbroken segment of speech
     * (It could be as fine grained a the user stopping for 300ms for example)
     *
     * @param pause
     */
    public void setRestartListener(boolean pause) {

        if(recognizerThread != null) {
            setPauseRecognizer(pause);

            // If restarting then the recognizer is paused at this point -
            if (pause) {
                Log.i("ASR", "Restart Recognizer");

                // End the Utterance so we can restart the decoder with a new search
                // Reset isDecoding so whent he thread restarts the decoder will start
                // a new utterance automatically.
                //
                isDecoding = false;
                decoder.endUtt();
            }
        }
    }


    /**
     *
     * @param pausing
     */
    public void setPauseRecognizer(boolean pausing) {

        if(recognizerThread != null) {

            // If we are releasing the thread and it is paused then notify the monitor
            // Don't send notifies when not required
            //
            if (!pausing && isPausedRecognizer) {

                // Note that notify must be within a synchronized block or it will fail
                // as it won't have the monitor currently - even though we know the thread
                // has been stopped.
                //
                synchronized (recognizerThread) {
                    Log.i("ASR", "Resume Thread");
                    isPausedRecognizer = false;
                    try {
                        recognizerThread.notify();
                    }
                    catch(Exception e) {
                        Log.d("ASR", "Exception: " + e);
                    }
                }
            }

            // Otherwise if we are pausing and it is running - don't pause a paused thread
            // this would end up in a deadlock - This can happen if there are nested calls
            //
            // Wait for the thread to pause - Once inside this block we know the
            // recognizerThread is sitting at PAUSED_TAG (search text)
            //
            else if(pausing && !isPausedRecognizer) {

                isPausedRecognizer = true;

                synchronized (recognizerThread) {

                    // Wait for the monitor - i.e. the thread to yield
                    Log.i("ASR", "Paused Thread");

                    // Ensure hypothesis output queue is emptied so there is nothing to process while paused
                    //
                    mainHandler.removeCallbacks(prevHypothesis);
                }
            }
        }

    }


    /**
     * This kills the recognizer thread and sets it for GC
     * @return
     */
    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            // Ensure the recognizerThread is not in the paused state
            // otherwise the interrupt() will signal it and it can loop and get stuck at the
            // PAUSED_TAG.
            isRunningRecognizer = false;
            setPauseRecognizer(false);
            recognizerThread.join();            // waits until it finishes

        } catch (InterruptedException e) {
            Log.i("ASR", "Stop Exception: " + e);
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }

        // Release the resource and reset the flag so it will restart if
        // there is a new recognizerthread created.
        //
        recognizerThread = null;
        isDecoding       = false;

        return true;
    }


    /**
     * Stops recognition. All listeners should receive final result if there is
     * any. Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
    public boolean stop() {
        Log.i("ASR", "Stop Recognition Thread");
        boolean result = stopRecognizerThread();

        if (result) {
            Log.i("ASR", "Stopped");

            final Hypothesis hypothesis = decoder.hyp();
            postResult(hypothesis, true);
        }
        return result;
    }


    /**
     * Cancels recognition. Listeners do not receive final result. Does nothing
     * if recognition is not active.
     *
     * @return true if recognition was actually canceled
     */
    public boolean cancel() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Cancel recognition");
        }
        return result;
    }


    /**
     * Gets name of the currently active search.
     *
     * @return active search name or null if no search was started
     */
    public String getSearchName() {
        return decoder.getSearch();
    }


    /**
     * Add search based on an fsg language model
     */
    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        Log.i("ASR", format("Add FSG %s", searchName));
        decoder.setFsg(searchName, fsgModel);
    }

    /**
     * Adds searches based on JSpeech grammar.
     *
     * @param name search name
     * @param file JSGF file
     */
    public void addGrammarSearch(String name, File file) {
        Log.i(TAG, format("Load JSGF %s", file));
        decoder.setJsgfFile(name, file.getPath());
    }

    /**
     * Adds search based on N-gram language model.
     *
     * @param name search name
     * @param file N-gram model file
     */
    public void addNgramSearch(String name, File file) {
        Log.i(TAG, format("Load N-gram model %s", file));
        decoder.setLmFile(name, file.getPath());
    }

    /**
     * Adds search based on a single phrase.
     *
     * @param name   search name
     * @param phrase search phrase
     */
    public void addKeyphraseSearch(String name, String phrase) {
        decoder.setKeyphrase(name, phrase);
    }


    /**
     * Adds search based on a keyphrase file.
     *
     * @param name search name
     * @param file keyphrase file
     */
    public void addKeywordSearch(String name, File file) {
        decoder.setKws(name, file.getPath());
    }


    /**
     *
     */
    private final class RecognizerThread extends Thread {

        private final String label;                     // label for the current capture
        private boolean      isRecording = false;
        private long         ASRTimer;                  // Used for benchmarking


        // constructor stores utterance id used to name capture file
        public RecognizerThread(String uttid, String rawLogDir) {
            label              = uttid;
            isPausedRecognizer = false;
        }

        @Override
        public void run() {

            synchronized (recognizerThread) {

                String     hypString;
                boolean    hypChanged     = false;
                boolean    flagsDirty     = true;
                boolean    inSpeech       = false;
                short[]    buffer         = new short[BUFFER_SIZE];
                Hypothesis prevHypothesis = null;

                String     lastAudioEvent = TCONST.UNKNOWN_TYPE;

                AudioRecord recorder = null;

                try {
                    recorder = new AudioRecord(
                            AudioSource.VOICE_RECOGNITION, sampleRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, 8192);
                }
                catch(Exception e) {
                    Log.d("ASR", "AudioRecorder Create Failed: " + e);
                }
                isRunningRecognizer = true;

                Log.i("ASR", "Start session");

                // Collect audio samples continuously while not paused and until the
                // Thread is killed.  This allow UI/UX activity while the listener is still
                // listening to the mic
                //
                while(isRunningRecognizer) {

                    // We always start the thread in the paused state
                    //
                    if (isPausedRecognizer) {
                        try {
                            recorder.stop();
                            isRecording = false;

                            // If we are starting a new uttereance stop recording and
                            // flush the input - i.e. clear the recorder
                            //
                            if(!isDecoding) {
                                int nread;

                                recorder.stop();
                                do {
                                    nread = recorder.read(buffer, 0, buffer.length);

                                }while(nread > 0);
                            }

                            // PAUSED_TAG
                            // Notes:
                            // Waits should always be in loops like this
                            // TODO: understand why interrupt causes freeze while in wait state
                            // You should not interrupt() while in a wait
                            //
                            while(isPausedRecognizer) {
                                Log.i("ASR","Recognizer Paused");
                                recognizerThread.wait();
                            }

                        } catch (InterruptedException e) {
                            Log.i("ASR","Wait Exception");
                            e.printStackTrace();
                        }
                    }

                    // Thread interrupt is not currently used but this is the recommened mechanism
                    // should it be required.
                    //
                    if(isInterrupted()) {
                        isRunningRecognizer = false;
                    }

                    if(!isRunningRecognizer) {
                        Log.d("ASR", "Terminating REC Thread");
                        continue;
                    }

                    // We start the thread with the decoder stopped and also when we
                    // restart for a new utterance - we end the uttereance prior to
                    // the decoder fsgsearch update
                    //
                    if(!isDecoding) {
                        Log.i("ASR","Start Decoder");

                        // label utterance with passed-in id
                        decoder.startUtt(label);
                        inSpeech       = false;
                        prevHypothesis = null;
                        lastAudioEvent = TCONST.UNKNOWN_TYPE;
                        isDecoding     = true;
                    }

                    // Ensure we are recording while the thread is running.
                    //
                    if(!isRecording) {
                        Log.i("ASR", "Resume recording");
                        recorder.startRecording();
                        isRecording = true;
                    }

                    // Clean out the buffered input
                    int nread = recorder.read(buffer, 0, buffer.length);

                    if (-1 == nread) {
                        Log.i("ASR","Read Error");
                        throw new RuntimeException("error reading audio buffer");

                    } else if (nread > 0) {

                        publishRMS(buffer, nread);

                        // Reset the Hypothesis Flag - We don't want to emit events unless
                        // there has been an actual change of hypothesis
                        //
                        hypChanged = false;

                            //ASRTimer = System.currentTimeMillis();
                        decoder.processRaw(buffer, nread, false, false);
                            //Log.d("ASR", "Time in processRaw: " + (System.currentTimeMillis() - ASRTimer));

                        nSamples += nread;

                        // InSpeech is true whenever there is a signal heard at the mic above threshold
                        // i.e. false means relative silence -
                        //
                        if (decoder.getInSpeech() != inSpeech) {

                            inSpeech = decoder.getInSpeech();

                            // Measure times from
                            // 1. The last time the mic heard anything
                            // 2. The last time the mic went silent.

                            Log.i("ASR","State Changed: " + inSpeech);

                            if(inSpeech) {
                                eventManager.fireStaticEvent(TCONST.SOUND_EVENT);
                                eventManager.updateStartTime(TCONST.TIMEDSOUND_EVENT, TCONST.TIMEDSILENCE_EVENT);
                                // Hearing a sound resets the silence timer

                            } else {
                                eventManager.fireStaticEvent(TCONST.SILENCE_EVENT);
                                eventManager.updateStartTime(TCONST.TIMEDSILENCE_EVENT, TCONST.UNKNOWNEVENT_TYPE);
                            }
                        }

                        // Get the hypothesis words from the Sphinx decoder
                        //
                            //ASRTimer = System.currentTimeMillis();
                        Hypothesis hypothesis = decoder.hyp();
                            //Log.d("ASR", "Time in Decoder: " + (System.currentTimeMillis() - ASRTimer));

                        // If there is a valid hypothesis string from the decoder continue
                        // Once the decoder returns a hypothesis it will not go back to
                        // null even with long periods of silence.  i.e. Wait until they start speaking
                        //
                        if(hypothesis != null) {

                            // DEBUG
                            hypString = hypothesis.getHypstr();

                            // If this is the first Hypothesis
                            // Record it so we can test for changes and set hypchanged flag
                            //
                            if (prevHypothesis == null) {
                                prevHypothesis = hypothesis;
                                hypChanged = true;

                                Log.i("ASR", "First Hypothesis: " + hypString);

                            } else {
                                // If the hypothesis hasn't changed they have stopped speaking.
                                // or are speaking Noise - not intelligible words
                                // TODO: Route all event traffic through the eventManager
                                //
                                if (prevHypothesis.getHypstr().equals(hypothesis.getHypstr())) {
                                    hypChanged = false;
                                    //Log.i("ASR","Same Hypothesis: " + hypString);

                                } else {
                                    hypChanged     = true;
                                    prevHypothesis = hypothesis;

                                    Log.i("ASR", "Updated Hypothesis: " + hypString);
                                }
                            }
                        }

                        // If the hypothesis has changed let the client know
                        // Update the eventTimer to indicate the last thing that happened
                        // Updating the word event resets silence and noise
                        //
                        if(hypChanged) {
                            eventManager.fireStaticEvent(TCONST.WORD_EVENT);
                            eventManager.updateStartTime(TCONST.TIMEDWORD_EVENT,
                                    TCONST.TIMEDSILENCE_EVENT | TCONST.TIMEDSOUND_EVENT);

                            Log.i("ASR", "Processing Hypothesis");

                            // If there is a new Hypothesis then process it
                            postResult(hypothesis, false);

                        } else {
                            // Watch for timed event firings (timeouts)
                            eventManager.fireTimedEvents();
                        }
                    }
                }

                Log.i("ASR","Stop session");

                recorder.stop();
                int nread = recorder.read(buffer, 0, buffer.length);
                recorder.release();
                decoder.processRaw(buffer, nread, false, false);
                nSamples += nread;
                decoder.endUtt();
                // Remove all pending notifications.
                mainHandler.removeCallbacksAndMessages(null);
                // convert raw capture to wav format
                //convertRawToWav(new File(captureDir, label + ".raw"), new File(captureDir, label + ".wav"));
            }
        }
    }


    private void publishRMS(short[] buffer, int count) {

        double sum = 0;
        Short  peak= 0;

        if(count > 0) {
            for (int i1 = 0; i1 < count; i1++) {
                Short sample = buffer[i1];

                sum = Math.pow(sample, 2);

                if(sample > peak)
                    peak = sample;

                if(sample > mPeak)
                    mPeak = sample;
            }

            double RMS = Math.sqrt(sum / count);

           // Log.i("RMS", "Double: " + RMS + "  - Sample: " + count + "  - local Peak: " + peak + "  - Peak: " + mPeak);
        }
    }


    /**
     * utility to convert raw audio capture file to wav format. Assumes 16Khz mono
     */
    public static void convertRawToWav(File rawFile, File wavFile) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(rawFile);
            output = new FileOutputStream(wavFile);
            // first write appropriate wave file header
            ByteArrayOutputStream hdrBytes = new ByteArrayOutputStream();
            new WaveHeader(WaveHeader.FORMAT_PCM, (short) 1, 16000, (short) 16, (int) rawFile.length()).write(hdrBytes);
            output.write(hdrBytes.toByteArray());
            // then copy raw bytes to output file
            byte[] buffer = new byte[4096];
            int nRead;
            while ((nRead = input.read(buffer)) > 0) {
                output.write(buffer, 0, nRead);
            }
            // finish up
            output.close();
            input.close();
            // on success, delete raw file
            rawFile.delete();
        } catch (Exception e) {
            Log.e("convertRawToWav", "Exception " + e.getMessage());
        } finally {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
            } catch (IOException e) {
                Log.e("convertRawToWav", "Closing streams: " + e.getMessage());
            }
        }
    }


    /**
     * Manage Looper queue so we don't have unprocessed hypotheses stacking up
     * Throw away previous hypothesis and only process the new one.
     *
     * This is done to manage degenerate cases where the hyopthesis becomes
     * very long and MultiMatch is being employed to process it.  In these
     * cirucmstances MM can take prolonged periods to process a result.
     * based on current MM design (Mar 2016)
     *
     * Note this purging is a good idea in any case as a stacked hypothesis is just a
     * waste of time - the newer one is always a better hypothesis.
     *
     * @param hypothesis
     * @param isFinal
     */
    private void postResult(Hypothesis hypothesis, boolean isFinal) {

        // If there is a new Hypothesis then process it
        // Note- initial null is ignored in removeCallBacks
        nextHypothesis = new ResultEvent(hypothesis, isFinal);

        // remove last hypothesis if it hasn't been processed
        mainHandler.removeCallbacks(prevHypothesis);
        mainHandler.post(nextHypothesis);

        prevHypothesis = nextHypothesis;
    }


    private abstract class RecognitionEvent implements Runnable {
        public void run() {
            ITutorListener[] emptyArray = new ITutorListener[0];
            for (ITutorListener listener : listeners.toArray(emptyArray))
                execute(listener);
        }

        protected abstract void execute(ITutorListener listener);
    }

    private class InSpeechChangeEvent extends RecognitionEvent {
        private final boolean state;

        InSpeechChangeEvent(boolean state) {
            this.state = state;
        }

        @Override
        protected void execute(ITutorListener listener) {
            if (state)
                listener.onBeginningOfSpeech();
            else
                listener.onEndOfSpeech();
        }
    }

    private class ResultEvent extends RecognitionEvent {
        protected final Hypothesis hypothesis;
        private final boolean finalResult;


        ResultEvent(Hypothesis hypothesis, boolean finalResult) {
            this.hypothesis = hypothesis;
            this.finalResult = finalResult;
        }


        @Override
        protected void execute(ITutorListener listener) {
            Log.d("ASR", "In Result Thread");
            if (finalResult)
                listener.onResult(hypothesis);
            else
                listener.onPartialResult(hypothesis);
        }
    }

    private class timeOutEvent extends RecognitionEvent {
        protected final int eventType;


        timeOutEvent(int _eventType) {
            eventType = _eventType;
        }


        @Override
        protected void execute(ITutorListener listener) {
            listener.onASREvent(eventType);
        }
    }


    public void configTimedEvent(int eventType, long newTimeout, boolean reset) {
        eventManager.configTimedEvent(eventType, newTimeout, reset);
    }

    public void configStaticEvent(int eventType, boolean listen) {
        eventManager.configStaticEvent(eventType, listen);
    }


    /**
     * ASREvents is a thread safe way to manage events that occur within the recognizerThread
     *
     */
    private class ASREvents {

        int          lastAudioEvent;
        private long audioEventTimer;

        private boolean listenForSilence = false;
        private boolean listenForSound   = false;
        private boolean listenForWords   = false;

        private long lastWordAttempt;
        private long lastSoundHeard;
        private long lastSilence;
        private long startTime;

        private long startGap;
        private long silenceGap;
        private long NoiseGap;
        private long attemptGap;

        private long silenceTimeout;
        private long NoiseTimeout;
        private long wordAttemptTimeout;
        private long startTimeOut;

        private boolean isStartTriggered       = true;
        private boolean isSilenceTriggered     = true;
        private boolean isNoiseTriggered       = true;
        private boolean isWordAttemptTriggered = true;


        public ASREvents() {
            updateStartTime(TCONST.UNKNOWNEVENT_TYPE, TCONST.ALLTIMED_EVENTS);
        }


        public synchronized void configTimedEvent(int eventType, long newTimeout, boolean reset) {

            switch(eventType) {
                case TCONST.TIMEDSILENCE_EVENT:
                    Log.d("ASR", "CONFIG TIMED SILENCE: " + newTimeout + " : " + reset);
                    silenceTimeout     = newTimeout;
                    isSilenceTriggered = false;
                    if(reset)
                        lastSilence = System.currentTimeMillis();
                    break;

                case TCONST.TIMEDSOUND_EVENT:
                    Log.d("ASR", "CONFIG TIMED SOUND: " + newTimeout + " : " + reset);
                    NoiseTimeout     = newTimeout;
                    isNoiseTriggered = false;
                    if(reset)
                        lastSoundHeard = System.currentTimeMillis();
                    break;

                case TCONST.TIMEDWORD_EVENT:
                    Log.d("ASR", "CONFIG TIMED WORD: " + newTimeout + " : " + reset);
                    wordAttemptTimeout     = newTimeout;
                    isWordAttemptTriggered = false;
                    if(reset)
                        lastWordAttempt = System.currentTimeMillis();
                    break;

                case TCONST.TIMEDSTART_EVENT:
                    Log.d("ASR", "CONFIG TIMED WORD: " + newTimeout + " : " + reset);
                    startTimeOut     = newTimeout;
                    isStartTriggered = false;
                    if(reset)
                        startTime = System.currentTimeMillis();
                    break;
            }
        }


        public synchronized void configStaticEvent(int eventType, boolean listen) {

            switch(eventType) {
                case TCONST.SILENCE_EVENT:
                    listenForSilence  = listen;
                    break;

                case TCONST.SOUND_EVENT:
                    listenForSound  = listen;
                    break;

                case TCONST.WORD_EVENT:
                    listenForWords  = listen;
                    break;

            }
        }


        public synchronized void updateStartTime(int eventType, int resetMap) {

            lastAudioEvent  = eventType;

            audioEventTimer = System.currentTimeMillis();

            switch (eventType) {
                case TCONST.TIMEDSILENCE_EVENT:
                    lastSilence = audioEventTimer;
                    break;
                case TCONST.TIMEDSOUND_EVENT:
                    lastSoundHeard = audioEventTimer;
                    break;
                case TCONST.TIMEDWORD_EVENT:
                    lastWordAttempt = audioEventTimer;
                    break;
            }

            if((resetMap & TCONST.TIMEDSILENCE_EVENT) != 0) {
                lastSilence = Long.MAX_VALUE;
            }
            if((resetMap & TCONST.TIMEDSOUND_EVENT) != 0) {
                lastSoundHeard = Long.MAX_VALUE;
            }
            if((resetMap & TCONST.TIMEDWORD_EVENT) != 0) {
                lastWordAttempt = Long.MAX_VALUE;
            }
        }


        public synchronized void fireStaticEvent(int eventType) {

            switch (eventType) {
                case TCONST.SILENCE_EVENT:
                    if(listenForSilence) {
                        Log.i("ASR", "Word Attempt Timout Triggered");
                        mainHandler.post(new timeOutEvent(TCONST.SILENCE_EVENT));
                    }
                    break;
                case TCONST.SOUND_EVENT:
                    if(listenForSound) {
                        Log.i("ASR", "Word Attempt Timout Triggered");
                        mainHandler.post(new timeOutEvent(TCONST.SOUND_EVENT));
                    }
                    break;
                case TCONST.WORD_EVENT:
                    if(listenForWords) {
                        Log.i("ASR", "Word Attempt Timout Triggered");
                        mainHandler.post(new timeOutEvent(TCONST.WORD_EVENT));
                    }
                    break;
            }
        }


        public synchronized void fireTimedEvents() {

            // lastSilence is the time since the mic went silent
            // lastSoundHeard is the time since the mic started hearing sound
            // lastWordAttempt is the time since they said an intelligible word

            long time = System.currentTimeMillis();

            startGap   = (time - startTime);
            silenceGap = (time - lastSilence);
            NoiseGap   = (time - lastSoundHeard);
            attemptGap = (time - lastWordAttempt);

            if (attemptGap > wordAttemptTimeout) {

                if (!isWordAttemptTriggered) {
                    Log.i("ASR", "Word Attempt Timout Triggered");
                    mainHandler.post(new timeOutEvent(TCONST.TIMEDWORD_EVENT));
                    isWordAttemptTriggered = true;
                }
            }

            else if (NoiseGap > NoiseTimeout) {

                if (!isNoiseTriggered) {
                    Log.i("ASR", "Noise Timout Triggered");
                    mainHandler.post(new timeOutEvent(TCONST.TIMEDSOUND_EVENT));
                    isNoiseTriggered = true;
                }
            }

            else if (silenceGap > silenceTimeout) {

                if (!isSilenceTriggered) {
                    Log.i("ASR", "Silence Timout Triggered");
                    mainHandler.post(new timeOutEvent(TCONST.TIMEDSILENCE_EVENT));
                    isSilenceTriggered = true;
                }
            }

            else if (startGap > startTimeOut) {

                if (!isStartTriggered) {
                    Log.i("ASR", "Start Timout Triggered");
                    mainHandler.post(new timeOutEvent(TCONST.TIMEDSTART_EVENT));
                    isStartTriggered = true;
                }
            }
        }
    }

}
