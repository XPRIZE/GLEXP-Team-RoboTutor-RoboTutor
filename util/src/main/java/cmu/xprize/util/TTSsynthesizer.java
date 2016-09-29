package cmu.xprize.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * TODO: this should be a singleton
 * TODO: Add stop / pause / restart
 */
public class TTSsynthesizer extends UtteranceProgressListener implements OnInitListener
{
    private final Context   context;
    private Locale          mLocale;
    private String          mCurrentLocale = "";
    private float           mCurrentRate   = 0;
    private TextToSpeech    tts;
    private boolean         readyToSpeak = false;
    private IReadyListener  tutorRoot;

    private IMediaController mMediaController;
    private boolean          isSpeaking = false;

    static final String FLITE_PACKAGE = "edu.cmu.xprize.flite";

    static final String TAG="TTSsynthesizer";


    public TTSsynthesizer(Context baseContext) {
        this.context = baseContext;
    }


    /**
     * Attach a callback to the tutorRoot to announce
     * service availability
     *
     * @param callback
     */
    public void initializeTTS(IReadyListener callback) {

        tutorRoot = callback;

        tts = new TextToSpeech(context, this, FLITE_PACKAGE);
        //tts = new TextToSpeech(context, this);
    }


    /**
     * TextToSpeech OnInitListener Callback
     * @param status
     */
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            readyToSpeak = true;
        }
        else {
            // TODO: Manage Flite Not Present
        }

        tutorRoot.onServiceReady("TTS", readyToSpeak? 1:0);
    }


    /**
     * Link to the tutor domain media manageer
     */
    public void setMediaManager(IMediaController manager) {
        mMediaController = manager;
    }


    /**
     * Sets the speech rate.
     *
     * This has no effect on any pre-recorded speech.
     */
    public int setSpeechRate(float speechRate) {

        int result = TextToSpeech.SUCCESS;

        if(speechRate != 0 && mCurrentRate != speechRate) {
            result =  tts.setSpeechRate(speechRate);

            if(result == TextToSpeech.SUCCESS)
                    mCurrentRate = speechRate;
        }

        return result;
    }


    /**
     * used by tutor root to test service availability
     * @return
     */
    public boolean isReady() {
        return readyToSpeak;
    }


    /**
     * Set voice by feature string
     *
     * @param langFeature
     */
    public void setLanguage(String langFeature) {

        if(langFeature != null && !langFeature.equals("") && mCurrentLocale != langFeature) {

            switch (langFeature) {
                case "LANG_SW":
                    mLocale = new Locale("swa", "TZA", "female;lxk");
                    break;

                case "LANG_EN":
                    mLocale = new Locale("en", "USA", "female;slt");
                    break;
            }
            tts.setLanguage(mLocale);

            mCurrentLocale = langFeature;
        }
    }


    public void speak(String text) {

        if (readyToSpeak) {
            tts. setOnUtteranceProgressListener(this);

            if(mMediaController != null)
                mMediaController.startSpeaking();

            tts.speak(text.toLowerCase(Locale.US), TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    /**
     * Force stop all utterances and flush the queue
     *
     */
    public void stopSpeaking() {

        if (readyToSpeak) {
            tts. stop();
        }
    }


    public boolean isSpeaking(){

        // Can get a dead object here - ignore
        //
        try {
            isSpeaking = tts.isSpeaking();

            if(!isSpeaking) {

                if(mMediaController != null)
                    mMediaController.stopSpeaking();
            }
        }
        catch(Exception ex) {
            Log.d(TAG, "Possible Dead Object: " + ex);

            isSpeaking = false;

            if(mMediaController != null)
                mMediaController.stopSpeaking();
        }

        return isSpeaking;
    }


    public void shutDown(){
        tts.shutdown();
    }


    @Override
    public void onStart(String utteranceId) {
        isSpeaking = true;
    }

    @Override
    public void onDone(String utteranceId) {
        isSpeaking = false;
        if(mMediaController != null)
            mMediaController.stopSpeaking();
    }

    @Override
    public void onError(String utteranceId) {
        isSpeaking = false;
        if(mMediaController != null)
            mMediaController.stopSpeaking();
    }
}