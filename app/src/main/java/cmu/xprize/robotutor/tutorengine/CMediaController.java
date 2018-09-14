package cmu.xprize.robotutor.tutorengine;

import android.content.res.AssetManager;
import android.util.Log;

import java.util.HashMap;

import cmu.xprize.util.IMediaController;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.ListenerBase;


/**
 * This is a singleton that manages MediaManager instances for tutors.
 * This allows us to release a tutor's resources by name from managerMap
 */
public class CMediaController implements IMediaController{

    static private HashMap<String, CMediaManager>  managerMap = new HashMap<>();

    static private TTSsynthesizer TTS;
    static private ListenerBase   mListener;
    static private AssetManager   mAssetManager;

    final static public String TAG = "CMediaController";


    private static CMediaController ourInstance = new CMediaController();

    public static CMediaController getInstance() {
        return ourInstance;
    }

    private CMediaController() {
    }

    static public CMediaManager newMediaManager(String parentTutor) {

        // GRAY_SCREEN_BUG this should be initialized, but it's not!!!
        CMediaManager manager = new CMediaManager(ourInstance, mAssetManager);

        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "p0: Putting CMediaManager: " + parentTutor);
        managerMap.put(parentTutor, manager);

        return manager;
    }


    static public CMediaManager getManagerInstance(String parentTutor) {
        Log.d("STOPAUDIO", "getManagerInstance: parentTutor "+parentTutor);
        // GRAY_SCREEN_BUG managerMap is empty, has no entry for "activity_selector"
        CMediaManager manager = managerMap.get(parentTutor);

        return manager;
    }


    static public void destroyMediaManager(String tutor) {

        CMediaManager manager = managerMap.get(tutor);

        if(manager != null) {
            manager.restartMediaManager();
        }

        // GRAY_SCREEN_BUG this is where MediaManager is removed
        Log.d(TCONST.DEBUG_GRAY_SCREEN_TAG, "r0: Removing tutor " + tutor);
        managerMap.remove(tutor);
    }


    public void setAssetManager(AssetManager manager) {
        mAssetManager = manager;
    }


    //**************************************************************************
    // TTS management START

    static public TTSsynthesizer getTTS() {

        return TTS;
    }

    public void setTTS(TTSsynthesizer _tts) {

        TTS = _tts;
        TTS.setMediaManager(this);
    }

    public void startSpeaking() {
        pauseListener();
    }

    public void stopSpeaking() {
        playListener();
    }


    // TTS management END
    //**************************************************************************



    //**************************************************************************
    // ASR management START

    private boolean paused = false;

    /**
     *  Inject the listener into the MediaManageer
     */
    static public void setListener(ListenerBase listener) {
        mListener = listener;
    }


    /**
     *  Remove the listener from the MediaManageer
     */
    static public void removeListener(ListenerBase listener) {
        mListener = null;
    }


    private void pauseListener() {

        if(mListener != null && mListener.isListening()) {

            Log.d(TAG, "pauseListener");

            mListener.setPauseListener(true);
            paused = true;
        }
    }


    private void playListener() {

        if(mListener != null && paused) {

            Log.d(TAG, "playListener");

            mListener.setPauseListener(false);
            paused = false;
        }
    }

    // ASR management END
    //**************************************************************************





}
