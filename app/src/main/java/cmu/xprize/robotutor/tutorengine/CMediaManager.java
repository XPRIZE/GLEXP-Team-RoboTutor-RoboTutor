//*********************************************************************************
//
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

package cmu.xprize.robotutor.tutorengine;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.util.Log;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.graph.type_handler;
import cmu.xprize.robotutor.tutorengine.graph.type_timelineFL;
import cmu.xprize.robotutor.tutorengine.graph.type_timer;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.GRAPH_MSG;


/**
 * The MediaManager is a per tutor media repository - when a tutor is shut down it's
 * associated MediaManager should be destroyed as well to clean up any operations.
 * Audio , timers etc.
 *
 * This object centralizes access to ongoing processes like audio playback and timelines so
 * that we can pause, play, stop, or destroy them globally.  There is a system wide hard limit on the number
 * of active MediaPlayers in Android. (TODO: need reference)
 *
 * It provides volume control centralization
 *
 * It provides Media caching support
 *
 *
 */
public class CMediaManager {

    private CMediaController                mMediaController;

    private ArrayList<PlayerManager>        mPlayerCache   = new ArrayList<PlayerManager>();
    private HashMap<String, type_timer>     mTimerMap      = new HashMap<String, type_timer>();
    private HashMap<String, type_handler>   mHandlerMap    = new HashMap<String, type_handler>();
    private HashMap<String, type_timelineFL>  mTimeLineMap   = new HashMap<String, type_timelineFL>();

    private HashMap<String, HashMap>        mSoundPackageMap = new HashMap<>();
    private AssetManager                    mAssetManager;
    static private int                      playerCount = 0;

    // Note that there is per tutor Language capability
    //
    private HashMap<String, String>  mLangFtrMap     = new HashMap<>();

    final static public String TAG = "CMediaManager";


    /**
     *
     * @param controller
     * @param manager
     */
    public CMediaManager(CMediaController controller, AssetManager manager) {
        mMediaController = controller;
        mAssetManager    = manager;
    }


    public void restartMediaManager() {

        for(PlayerManager controller : mPlayerCache) {
           controller.kill();
        }

        Iterator<?> timerObjects = mTimerMap.entrySet().iterator();

        while(timerObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) timerObjects.next();

            type_timer timer = ((type_timer)(entry.getValue()));

            timer.globalPause();
        }


        Iterator<?> handlerObjects = mHandlerMap.entrySet().iterator();

        while(handlerObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) handlerObjects.next();

            type_handler handler = ((type_handler)(entry.getValue()));

            handler.globalPause();
        }


        Iterator<?> timelineObjects = mTimeLineMap.entrySet().iterator();

        while(timelineObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) timelineObjects.next();

            type_timelineFL timeline = ((type_timelineFL)(entry.getValue()));

            timeline.globalPause();
        }


        mPlayerCache   = new ArrayList<PlayerManager>();
        mTimerMap      = new HashMap<String, type_timer>();
        mHandlerMap    = new HashMap<String, type_handler>();
        mTimeLineMap   = new HashMap<String, type_timelineFL>();

        mSoundPackageMap = new HashMap<>();
        mLangFtrMap      = new HashMap<String, String>();
    }



    //**************************************************************************
    // Language management START

    // Use two letter language codes as defined here:
    // https://www.w3.org/International/questions/qa-lang-2or3

    public String getLanguageIANA_2(CTutor tTutor) {

        return TCONST.langMap.get(mLangFtrMap.get(tTutor.getTutorName()));
    }

    public String getLanguageFeature(CTutor tTutor) {

        try {
            return mLangFtrMap.get(tTutor.getTutorName());
        }
        catch(Exception e) {
            Log.e(TAG, "Excep:"  + e);

            return("LANG_SW");

        }
    }

    public void setLanguageFeature(CTutor tTutor, String langFtr) {

        mLangFtrMap.put(tTutor.getTutorName(), langFtr);

        tTutor.updateLanguageFeature(langFtr);
    }

    public void setSoundPackage(CTutor tTutor, HashMap soundMap) {

        mSoundPackageMap.put(tTutor.getTutorName(), soundMap);
    }

    /**
     * Add named soundpackage to tutors soundMap
     *
     * @param tTutor
     * @param packageName
     * @param mediaPackage
     */
    public void addSoundPackage(CTutor tTutor, String packageName, CMediaPackage mediaPackage) {

        HashMap<String,CMediaPackage> soundMap;

        soundMap = mSoundPackageMap.get(tTutor.getTutorName());

        soundMap.put(packageName, mediaPackage);
    }

    // TODO: When starting debugging with the screen off - one of these may be null
    // GRAY_SCREEN_BUG X
    public String mapSoundPackage(CTutor tTutor, String packageName, String langOverride) {

        HashMap<String,CMediaPackage> soundMap;
        CMediaPackage   mediaPackage;
        String          autoLang;
        String          soundPackage;

        if(langOverride != null) {
            autoLang = mapLanguageIANA_2(langOverride);
        }
        else {
            autoLang = getLanguageIANA_2(tTutor);
        }

        try {
            // If the tutor is configured for soundpackages in the tutor_descriptor
            // Old tutors may not contain soundMaps - these default to what they expect.
            // NOTE: non-soundMap tutors are deprecated.
            //
            soundMap = mSoundPackageMap.get(tTutor.getTutorName());

            if (soundMap != null) {

                // If the user didn't define a sound package use the default
                //
                if (packageName == null)
                    packageName = TCONST.DEFAULT_SOUND_PACKAGE;

                mediaPackage = soundMap.get(packageName);

                switch (mediaPackage.language) {
                    case TCONST.LANG_AUTO:
                        // Do nothing - Use the standard autoLang
                        break;

                    case TCONST.LANG_EFFECT:
                        // Use the non-language specific path - sound effects etc.
                        autoLang = "effect";
                        break;

                    default:
                        // Use the override language
                        autoLang = mapLanguageIANA_2(langOverride);
                        break;
                }

                soundPackage = autoLang + "/" + mediaPackage.path;

            } else {
                soundPackage = autoLang;
            }
        }
        catch(Exception e) {
            soundPackage = autoLang;
        }

        return soundPackage;
    }


    public String mapPackagePath(CTutor tTutor, String packageName) {

        HashMap<String,CMediaPackage> soundMap;
        CMediaPackage   mediaPackage;
        String          packagePath;

        try {
            // If the tutor is configured for soundpackages in the tutor_descriptor
            // Old tutors may not contain soundMaps - these default to what they expect.
            // NOTE: non-soundMap tutors are deprecated.
            //
            soundMap = mSoundPackageMap.get(tTutor.getTutorName());

            if (soundMap != null) {

                // If the user didn't define a sound package use the default
                //
                if (packageName == null)
                    packageName = TCONST.DEFAULT_SOUND_PACKAGE;

                mediaPackage = soundMap.get(packageName);

                // Note that location and srcPath currently default to RoboTutor specific
                // locations in public memory folders
                //
                packagePath = mediaPackage.srcpath;

            } else {
                packagePath = TCONST.BASE_ASSETS;
            }
        }
        catch(Exception e) {
            packagePath = TCONST.BASE_ASSETS;
        }

        return packagePath;
    }


    public String mapPackageLocation(CTutor tTutor, String packageName) {

        HashMap<String,CMediaPackage> soundMap;
        CMediaPackage   mediaPackage;
        String          packageLocation;

        try {
            // If the tutor is configured for soundpackages in the tutor_descriptor
            // Old tutors may not contain soundMaps - these default to what they expect.
            // NOTE: non-soundMap tutors are deprecated.
            //
            soundMap = mSoundPackageMap.get(tTutor.getTutorName());

            if (soundMap != null) {

                // If the user didn't define a sound package use the default
                //
                if (packageName == null)
                    packageName = TCONST.DEFAULT_SOUND_PACKAGE;

                mediaPackage = soundMap.get(packageName);

                // Note that location and srcPath currently default to RoboTutor specific
                // locations in public memory folders
                //
                packageLocation = mediaPackage.location;

            } else {
                packageLocation = TCONST.EXTERNAL;
            }
        }
        catch(Exception e) {
            packageLocation = TCONST.EXTERNAL;
        }

        return packageLocation;
    }


    public String mapLanguageIANA_2(String _language) {

        return TCONST.langMap.get(_language);
    }

    // Language management END
    //**************************************************************************



    //********************************************************************
    //*************  Timer Management START

    public void createTimer(String key, type_timer owner) {

        if(mTimerMap.containsKey(key)) {
            CErrorManager.logEvent(TAG,  "Duplicate Timer Name:" + key, new Exception("no-exception"), false);
        }
        mTimerMap.put(key, owner);
    }


    public type_timer removeTimer(String key) {
        return mTimerMap.remove(key);
    }


    public type_timer mapTimer(String key) {
        return mTimerMap.get(key);
    }


    public boolean hasTimer(String key) {
        return mTimerMap.containsKey(key);
    }


    //*************  Timer Management END
    //********************************************************************



    //********************************************************************
    //*************  Handler Management START

    public void createHandler(String key, type_handler owner) {

        if(mHandlerMap.containsKey(key)) {
            CErrorManager.logEvent(TAG,  "Duplicate Handler Name: " + key, new Exception("no-exception"), false);
        }
        mHandlerMap.put(key, owner);
    }


    public type_handler removeHandler(String key) {

        Log.d(TAG, "Removing Handler Name: " + key);

        return mHandlerMap.remove(key);
    }


    public type_handler mapHandler(String key) {
        return mHandlerMap.get(key);
    }


    public boolean hasHandler(String key) {
        return mHandlerMap.containsKey(key);
    }


    //*************  Handler Management END
    //********************************************************************




    //********************************************************************
    //*************  Timeline Management START

    public void createTimeLine(String key, type_timelineFL owner) {

        if(mTimeLineMap.containsKey(key)) {
            CErrorManager.logEvent(TAG,  "Duplicate Timer Name:" + key, new Exception("no-exception"), false);
        }
        mTimeLineMap.put(key, owner);
    }

    public type_timelineFL removeTimeLine(String key) {
        Log.d("ULANI", "CMediaManager: removeTimeLine: KEY = "+key);
        return mTimeLineMap.remove(key);
    }


    public type_timelineFL mapTimeLine(String key) {
        return mTimeLineMap.get(key);
    }


    public boolean hasTimeLine(String key) {
        return mTimeLineMap.containsKey(key);
    }


    //*************  Timeline Management END
    //********************************************************************

    public PlayerManager getPlaying(){
        for(PlayerManager playerInstance : mPlayerCache) {

            if(playerInstance.isPlaying()) {
                Log.v(GRAPH_MSG, "CMediaManager.playermanager.attachexisting: manager = "+playerInstance.mDataSource);
                return playerInstance;
            }
        }
        return null;
    }


    //********************************************************************
    //*************  MediaPlayer Management START


    public PlayerManager attachMediaPlayer(String dataSource, String location, IMediaListener owner) {
        System.out.println("ATTACHMEDIAPLAYER");
        System.out.println("DATASOURCE: "+dataSource);
        System.out.println("LOCATION: "+location);
        System.out.println("OWNER: "+owner.sourceName()+ " => "+owner.resolvedName());
        PlayerManager manager = null;

        // First look for an unattached (cached) controller that uses this datasource and reuse it.
        //
        for(PlayerManager playerInstance : mPlayerCache) {

            if(!playerInstance.isAttached() && playerInstance.compareSource(dataSource)) {
                Log.v(GRAPH_MSG, "CMediaManager.playermanager.attachexisting: manager = "+playerInstance.mDataSource);

                manager = playerInstance;
                manager.attach(owner);
                break;
            }
        }

        // If we don't find a usable controller then try and reuse the player that has been unused
        // for the longest period. i.e. assume the oldest unused audioclip has least probability of
        // being reused.
        //
        if(manager == null) {

            PlayerManager oldest   = null;
            long          timeTest = 0L;

            for(PlayerManager managerInstance : mPlayerCache) {

                if(!managerInstance.isAttached()) {
                    if(managerInstance.lastUsed() > timeTest) {
                        oldest = managerInstance;
                    }
                }
            }

            manager = oldest;

            // If we are re-purposing an old controller then we need to release it and
            // reset its dataSource.
            //
            if(manager != null) {

                Log.v(GRAPH_MSG, "CMediaManager.playermanager.repurpose:");

                manager.releasePlayer();

                manager.attach(owner);                        // Need to reattach to a new owner
                manager.createPlayer(dataSource, location);   // Update the datasource
            }
        }

        // If we don't find a RE-usable controller then create a new one
        //
        if(manager == null) {

            Log.v(GRAPH_MSG, "CMediaManager.playermanager.create:"  + mPlayerCache.size());

            manager = new PlayerManager(owner, dataSource, location);

            mPlayerCache.add(manager);
        }

        return manager;
    }


    /**
     * Clean up the media controller for the given object - it is assumed that an owner may only
     * be attached to a single mediaController
     *
     * @param owner
     */
    public void  detachMediaPlayer(Object owner) {
        Log.d("ULANI", "detachMediaPlayer: ");
        // First look for an unattached (cached) controller that uses this datasource and reuse it.
        //
        for(PlayerManager managerInstance : mPlayerCache) {

            if(managerInstance.compareOwner(owner)) {
                Log.d(TAG, "detachMediaPlayer: managerInstance = "+managerInstance.mDataSource);
                managerInstance.detach();
            }
        }
    }

    public void dispMediaPlayers(){
        int count = 0;
        for(PlayerManager managerInstance : mPlayerCache){
            Log.d(TAG, "dispMediaPlayers: datasource"+Integer.toString(count)+" = "+managerInstance.mDataSource);
            count++;
        }
    }

    public class PlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        private  IMediaListener mOwner;

        private MediaPlayer  mPlayer        = null;
        private boolean      mPlaying       = false;
        private boolean      mIsReady       = false;
        private boolean      mIsAlive       = true;

        private Long         lastUsed       = 0L;
        private String       mDataSource = "";

        private boolean      mDeferredStart = false;
        private boolean      mDeferredSeek  = false;
        private long         mSeekPoint     = 0;


        final static public String TAG = "CMediaManager";


        protected PlayerManager(Object owner, String _dataSource, String location) {

            mOwner      = (IMediaListener)owner;
            mDataSource = _dataSource;

            createPlayer(mDataSource, location);
        }

        public int getCurrentPosition(){
            return mPlayer.getCurrentPosition();
        }

        protected void createPlayer(String dataSource, String location) {

            try {
                mIsReady = false;

                // Ensure the player is released
                //
                if(mPlayer != null) {
                    releasePlayer();
                }

                playerCount++;
                Log.v(GRAPH_MSG, "CMediaManager.mediaplayer.create:" + playerCount);
                mPlayer  = new MediaPlayer();

                switch(location) {

                    case TCONST.EXTERNAL:
                        FileInputStream soundFile = new FileInputStream(dataSource);

                        mPlayer.setDataSource(soundFile.getFD());
                        break;

                    default:
                        AssetFileDescriptor soundData = mAssetManager.openFd(dataSource);

                        mPlayer.setDataSource(soundData.getFileDescriptor(), soundData.getStartOffset(), soundData.getLength());
                        soundData.close();
                        break;
                }
                mPlayer.setOnPreparedListener(this);
                mPlayer.setOnCompletionListener(this);
                mPlayer.setLooping(mOwner.isLooping());

                float volume = mOwner.getVolume();

                if(volume > 0) {
                    mPlayer.setVolume(volume, volume);
                }

                mPlayer.prepareAsync();

                Log.v(GRAPH_MSG, "CMediaManager.mediaplayer.loading:" + mOwner.sourceName() + " => " + mOwner.resolvedName() );

            } catch (Exception e) {
                Log.e(GRAPH_MSG, "CMediaManager.mediaplayer.ERROR: " + mOwner.sourceName() + " => " + mOwner.resolvedName() + " => " + e);

                // Do the completion event to keep the tutor moving.
                //
                onCompletion(mPlayer);
            }
        }


        public void releasePlayer() {

            if(mPlayer != null) {

                if(mPlaying) {
                    mPlayer.pause();
                }
                mPlaying = false;
                mIsAlive = true;

                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;

                playerCount--;
                Log.v(GRAPH_MSG, "CMediaManager.playermanager.destroy:"  + playerCount);

                mDataSource = "";
            }
        }


        protected void attach(Object _owner) {
            mOwner = (IMediaListener)_owner;
        }


        /**
         * Note that we leave the MediaPlayer in a playable state
         */
        public void detach() {

            Log.v(GRAPH_MSG, "CMediaManager.playermanager.detach");

            stop();
            mOwner = null;
        }


        protected boolean isAttached() {

            return mOwner != null;
        }


        protected boolean compareSource(String _dataSource) {

            return mDataSource.equals(_dataSource);
        }


        protected Long lastUsed() {

            return lastUsed;
        }


        protected boolean compareOwner(Object _owner) {

            return mOwner == _owner;
        }


        public boolean isPlaying() {
            return mPlaying;
        }


        // TODO : need tighter control over media player lifetime - running out of resources
        // since they aren't being released.
        public void play() {
            Log.d("ULANISTOPAUDIO", "play: ");
            if(!mPlaying && mIsAlive) {

                if(mIsReady) {
                    // TODO: this will need a tweak for background music etc.
                    mMediaController.startSpeaking();

                    Log.v(GRAPH_MSG, "CMediaManager.playermanager.play: " + mDataSource);
                    mPlayer.start();

                    mPlaying       = true;
                    mDeferredStart = false;
                }
                else
                    mDeferredStart = true;
            }
        }

        Runnable stopPlayerTask = new Runnable(){
            @Override
            public void run() {
                mPlayer.stop();
            }};

        public void play(Long duration) {
            System.out.println("Playing something with total duration below");
            Log.d("ULANISTOPAUDIO", "play: "+mPlayer.getDuration()+" "+mDataSource);
//            sLog.d("ULANI", "playing something with total duration: "+ Integer.toString(mPlayer.getDuration()));
            if(!mPlaying && mIsAlive) {

                if(mIsReady) {
                    // TODO: this will need a tweak for background music etc.
                    mMediaController.startSpeaking();

                    Log.v(GRAPH_MSG, "CMediaManager.playermanager.play: " + mDataSource);
                    mPlayer.start();
//                    Handler handler = new Handler();
//                    handler.postDelayed(stopPlayerTask, duration);

                    mPlaying       = true;
                    mDeferredStart = false;
                }
                else
                    mDeferredStart = true;
            }
        }

        // UHQ : STOP THE TRACK WITH THIS
        public void stop() {
            Log.d("ULANISTOPAUDIO", "stop: ");
            Log.v(GRAPH_MSG, "CMediaManager.playermanager.stop: " + mDataSource);

            pause();
            seek(0L);

            //#Mod issue #335 - give the tracka chance to shutdown.  The audio runs in
            // JNI code so this seems to allow it to shutdown and not restart if we are
            // interrupting a clip with another clip.
            //
            try {
                Thread.sleep(10);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        /**
         * TODO: need to recycle the controller
         */
        public void kill() {

            Log.v(GRAPH_MSG, "CMediaManager.playermanager.kill: " + mDataSource);

            mIsAlive = false;

            releasePlayer();
        }


        public void pause() {

            if(mPlaying) {
                mPlayer.pause();
                mMediaController.stopSpeaking();
            }
            mPlaying = false;
        }


        public void seek(long frame) {

            mSeekPoint = frame;

            if(mIsReady) {
                int iframe = (int) (frame * 1000 / TCONST.FPS);

                seekTo(iframe);
            }
            else
                mDeferredSeek = true;

        }


        public void seekTo(int frameTime) {

            // No errors occur - but don't try to seek past the end
            // Note: we don't want to seek after death
            //
            if(mPlayer != null && mIsAlive && frameTime < mPlayer.getDuration())
                mPlayer.seekTo(frameTime);
        }


        @Override
        public void onPrepared(MediaPlayer mp) {

            mIsReady = true;

            // If seek was called before we were ready play
            if(mDeferredSeek)
                seek(mSeekPoint);

            // If play was called before we were ready play
            if(mDeferredStart)
                play();
        }


        /**
         * When play completes we pause the audio stream and seek to origin so it's ready to
         * play if another component want to reuse it.
         *
         * @param mp
         */
        @Override
        public void onCompletion(MediaPlayer mp) {

            // Track when each clip is last used so we can reuse cached clips intelligently
            // by re-purposing the oldest first.  i.e. We want to limit the reloading of clips
            // if possible.
            //
            lastUsed = System.currentTimeMillis();

            // Allow the owning node to do type specific processing of completion event.
            //
            if (mOwner != null) {

                mOwner.onCompletion(this);
            }
        }
    }

    //*************  MediaPlayer Management END
    //********************************************************************

}
