package edu.cmu.xprize.listener;

import edu.cmu.pocketsphinx.RecognitionListener;


public interface ITutorListener extends RecognitionListener {

    public void onStableResult(String[] hypothesis);
    public void onASREvent(int eventType);
}
