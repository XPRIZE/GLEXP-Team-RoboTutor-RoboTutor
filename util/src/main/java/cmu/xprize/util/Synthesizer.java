package cmu.xprize.util;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Karya Technologies on 05-06-2015.
 *
 * Purpose : Initialize the synthesizer and reads the provided
 * Version : 1.0
 * History : Initial Draft
 */

public class Synthesizer implements OnInitListener
{
    private final Context context;
    private Locale        mLocale;
    private TextToSpeech  tts;
    private boolean       readyToSpeak = false;

    static final String FLITE_PACKAGE = "edu.cmu.xprize.flite";

    static final String TAG="Synthesizer";

    public Synthesizer(Context baseContext) {
        this.context = baseContext;
    }

    public void initOrInstallTTS() {
        tts = new TextToSpeech(context, this, FLITE_PACKAGE);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mLocale = new Locale("swa","TZA","female;lxk");
            tts.setLanguage(mLocale);
            readyToSpeak = true;
        }
        else {
            // TODO: Manage Flite Not Present
        }
    }


    public void speak(String text) {
        if (readyToSpeak)
            tts.speak(text.toLowerCase(Locale.US), TextToSpeech.QUEUE_FLUSH, null);
    }


    public boolean isSpeaking(){

        boolean result = false;

        // Can get a dead object here - ignore
        try {
            result = tts.isSpeaking();
        }
        catch(Exception ex) {
            Log.d(TAG, "Possible Dead Object: " + ex);
        }

        return result;
    }


    public void shutDown(){
        tts.shutdown();
    }
}