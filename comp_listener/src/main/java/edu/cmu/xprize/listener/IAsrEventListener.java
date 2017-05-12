package edu.cmu.xprize.listener;


public interface IAsrEventListener {

    /**
     * Called at the start of utterance.
     */
    void onBeginningOfSpeech();

    /**
     * Called at the end of utterance.
     */
    void onEndOfSpeech();

    /**
     * Called when partial recognition result is available.
     */
    void onUpdate(ListenerBase.HeardWord heardWords[], boolean finalResult);

    /**
     * Called when partial recognition result is available.
     */
    void onUpdate(String heardWords[], boolean finalResult);


    void onASREvent(int eventType);

}

