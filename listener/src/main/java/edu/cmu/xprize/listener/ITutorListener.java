package edu.cmu.xprize.listener;

import edu.cmu.pocketsphinx.RecognitionListener;


public interface ITutorListener extends RecognitionListener {

    void onASREvent(int eventType);
}
