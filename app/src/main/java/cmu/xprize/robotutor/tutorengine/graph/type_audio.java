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

package cmu.xprize.robotutor.tutorengine.graph;

import android.util.Log;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.IMediaListener;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.CFileNameHasher;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.AUDIO_EVENT;
import static cmu.xprize.util.TCONST.TYPE_AUDIO;


/**
 * Media players are special objects as there is a system wide limit on how many can be active
 * at one time.  As a result we centralize creation and management of MediaPlayers to CMediaManager
 * where we can cache players across tutors as well as play/pause etc globally
 */
public class type_audio extends type_action implements IMediaListener {

    protected static final String NOOP = "NOOP";

    // NOTE: we run at a Flash default of 24fps - which is the units in which
    // index and duration are calibrated

    protected CFileNameHasher             mFileNameHasher;
    protected CMediaManager               mMediaManager;
    protected CMediaManager.PlayerManager mPlayer;
    protected boolean                     mPreLoaded = false;

    private Timer tempTimer;
    private String mSoundSource;
    private String mSourcePath;
    private String mResolvedName;
    private String mPathResolved;
    private String mRawName;
    private String mLocation;

    private boolean _useHashName = true;
    private boolean _packageInit = false;

    // json loadable fields
    public String command;
    public String lang;
    public String soundsource;
    public String soundpackage;

    public String listeners  = "";
    public String oncomplete = NOOP;

    public boolean  repeat = false;
    public float    volume = -1f;
    public long     index  = 0;


    final static public String TAG = "type_audio";


    public type_audio() {
        Timer tempTimer = new Timer();
        setTempTimer(tempTimer);
        mFileNameHasher = CFileNameHasher.getInstance();
    }

    /**
     * TODO: onDestroy not being called when tutor is killed
     */
    public void onDestroy() {
        stopTempTimer();

        mMediaManager.detachMediaPlayer(this);
    }

    //*******************************************************
    //**  Global Media Control Start

    private boolean mWasPlaying = false;

    @Override
    public String sourceName() {
        Log.d("ULANI type_audio", "sourceName: "+soundsource);
        return soundsource;
    }

    @Override
    public String resolvedName() {
        Log.d("ULANI type_audio", "resolvedName: "+mResolvedName);
        return (mResolvedName == null) ? "" : mResolvedName;
    }

    @Override
    public void globalPause() {
        Log.d("ULANI", "globalPause: wasPlaying= "+mWasPlaying);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mWasPlaying = true;

                mPlayer.stop();
            }
        }
    }

    @Override
    public void globalPlay() {
        Log.d(TAG, "globalPlay: wasplaying = "+mWasPlaying);
        if (mPlayer != null) {
            if (mWasPlaying) {
                mWasPlaying = false;

                RoboTutor.logManager.postEvent_D(_logType, "target:node.audio,action:globalplay,name:"+mRawName);
                mPlayer.play();
            }
        }
    }

    @Override
    public void globalStop() {
        Log.d(TAG, "globalStop: wasplaying= "+mWasPlaying);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mWasPlaying = true;

                RoboTutor.logManager.postEvent_D(_logType, "target:node.audio,action:globalstop,name:"+mRawName);
                mPlayer.releasePlayer();
            }
        }
    }

    @Override
    public boolean isLooping() {
        return repeat;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    /**
     * Listen to the MediaController for completion events.
     */
    @Override
    public void onCompletion(CMediaManager.PlayerManager playerManager) {

        // Support emitting events if components need state info from the audio
        //
        if(!oncomplete.equals(NOOP)) {

            CEvent event = new CEvent(TYPE_AUDIO, AUDIO_EVENT, oncomplete);

            dispatchEvent(event);
        }

        // If not an AUDIOEVENT then we disconnect the player to allow reuse
        //
        if (!mode.equals(TCONST.AUDIOEVENT)) {

            // Release the mediaController for reuse
            //
            if (mPlayer != null) {
                mPlayer.detach();
                mPlayer = null;
            }

            // Flows automatically emit a NEXT_NODE event to scenegraph.
            //
            if (mode.equals(TCONST.AUDIOFLOW)) {
                RoboTutor.logManager.postEvent_V(_logType, "target:node.audio,event:oncompletion,type:flow,emit:eventNext,name:"+mRawName);
                _scope.tutor().eventNext();
            }
            else {
                RoboTutor.logManager.postEvent_V(_logType, "target:node.audio,event:oncompletion,type:stream,name:"+mRawName);
            }
        }
        // If this is an AUDIOEVENT type then the mPlayer was released already but we need
        // to let the independent playerManager know that it is no longer needed.
        //
        else {
            if (playerManager != null) {
                playerManager.detach();
            }
        }
    }

    //**  Global Media Control Start
    //*******************************************************


    /**
     * This is an optimization used in timeLines to preload the assets - timeline tracks act as the
     * owner so that they are informed directly of audio completion events.  This is necessary to
     * keep them sync'd with the mediaManager attach states - otherwise they don't know when their
     * audio players have been detached and may perform operations on a re-purposed players.
     */
    public void preLoad(IMediaListener owner) {
        System.out.println("PRELOAD");
        System.out.println("OWNER: "+owner.sourceName()+" => "+owner.resolvedName());

        // Perform late binding to the sound package.
        //
        initSoundPackage();

        mPathResolved = getScope().parseTemplate(mSourcePath);

        RoboTutor.logManager.postEvent_D(_logType, "target:node.audio,action:preload,name:" + mPathResolved);
        RoboTutor.logManager.postEvent_D(TCONST.DEBUG_AUDIO_FILE, "target:node.audio,action:preload,name:" + mPathResolved);

        int endofPath = mPathResolved.lastIndexOf("/") + 1;

        // Extract the path and name portions
        // NOTE: ASSUME mp3 - trim the mp3 - we just want the filename text to generate the Hash
        // TODO: Don't assume mp3 or even an extension
        //
        String pathPart = mPathResolved.substring(0, endofPath);

        mRawName = mPathResolved.substring(endofPath, mPathResolved.length() - 4);

        // Note we keep this decomposition to provide the resolved name for debug messages
        //
        if (_useHashName) {

            // Permit actual hash's in the script using the # prefix
            //
            if (mRawName.startsWith("#")) {
                mResolvedName = mRawName.substring(1);
            }
            // Otherwise generate the hash from the text
            else {
                mResolvedName = mFileNameHasher.generateHash(mRawName);
            }
        } else {
            mResolvedName = mRawName;
        }

        // add the extension back on the generated filename hash
        //
        mPathResolved = pathPart + mResolvedName + ".mp3";

        // This allocates a MediaPController for use by this audio_node. The media controller
        // is a managed global resource of CMediaManager
        //
        mPlayer = mMediaManager.attachMediaPlayer(mPathResolved, mLocation, owner);

        mPreLoaded = true;
    }


    @Override
    public String applyNode() {
        System.out.println("APPLY NODE");

        String status = TCONST.DONE;

        // If the feature test passes then fire the event.
        // Otherwise set flag to indicate event was completed/skipped in this case
        // Issue #58 - Make all actions feature reactive.
        //
        if (testFeatures()) {

            // Non type_timelineFL audio tracks are not preloaded. So do it inline. This just has a
            // higher latency between the call and when the audio is actually ready to play.
            //
            if (!mPreLoaded) {
                preLoad(this);
            }
            mPreLoaded = false;

            // Support having components listen for audio events.
            //
            if(!listeners.equals("")) {

                String[] compNames = listeners.split(",");

                for(String name : compNames) {

                    addViewListener(name);
                }
            }

            // play on creation if command indicates
            if (command.equals(TCONST.PLAY)) {

                play();

                // Events return done - so they may play on top of each other.
                // streams and flows WAIT until completion before continuing.
                //
                if (mode.equals(TCONST.AUDIOEVENT)) {
                    status = TCONST.DONE;
                }

                // TCONST.STREAMEVENT or TCONST.FLOWEVENT wait for completion
                // TCONST.FLOWEVENT automatically advances on completion
                else {
                    status = TCONST.WAIT;
                }
            }
//            else if(command.equals(TCONST.PLAY_CLOZE)){
//                play(TCONST.CLOZE_END);
//            }

        }

        return status;
    }


    @Override
    public String cancelNode() {

        if(mPlayer != null) {

            stop();

            mPlayer.detach();
            mPlayer = null;
        }

        RoboTutor.logManager.postEvent_D(_logType, "target:node.audio,action:cancelnode,name:" + mRawName);

        return TCONST.NONE;
    }

    public void setTempTimer(Timer temp) {
        temp.scheduleAtFixedRate(
                new TimerTask(){
                      public void run(){
                          if (mPlayer!= null) {
//                              Log.d("ULANI", "TEMPTIMER: real time narrationSegment current position = " + mPlayer.getCurrentPosition());
                          }
                      }
                  },0,      // run first occurrence immediately
                100);
        this.tempTimer = temp;
    }

    public void stopTempTimer(){
        this.tempTimer.cancel();
    }
    public void play() {
        if(mPlayer != null) {
            RoboTutor.logManager.postEvent_I(_logType, "target:node.audio,action:play,name:" + mRawName);
            mPlayer.play();

            // AUDIOEVENT mode tracks are fire and forget - i.e. we disconnect from the player
            // and let it continue to completion independently.
            //
            // This allows this Audio element to be reused immediately - So we can fire another
            // instance of the sound while the other is still playing.
            //
            if(mode == TCONST.AUDIOEVENT) {
                RoboTutor.logManager.postEvent_V(_logType, "target:node.audio,type:event,action:complete,name:" + mRawName);

                mPlayer = null;
            }
        }
    }

    public void play(long duration){
        if(mPlayer != null) {
            RoboTutor.logManager.postEvent_I(_logType, "target:node.audio,action:play,name:" + mRawName);
            mPlayer.play(duration);

            // AUDIOEVENT mode tracks are fire and forget - i.e. we disconnect from the player
            // and let it continue to completion independently.
            //
            // This allows this Audio element to be reused immediately - So we can fire another
            // instance of the sound while the other is still playing.
            //
            if(mode == TCONST.AUDIOEVENT) {
                RoboTutor.logManager.postEvent_V(_logType, "target:node.audio,type:event,action:complete,name:" + mRawName);

                mPlayer = null;
            }
        }
    }

    public void stop() {
        if(mPlayer != null){
            mPlayer.stop();
        }
    }


    public void pause() {

        if(mPlayer != null)
            mPlayer.pause();
    }


    public void seek(long frame) {

        if(mPlayer != null)
            mPlayer.seek(frame);
    }


    public void seekTo(int frameTime) {

        if(mPlayer != null)
            mPlayer.seekTo(frameTime);
    }

    public void detach() {

        if(mPlayer != null)
            mPlayer.detach();
    }

    /**
     * Note that we do late binding to the soundpackage as some tutors update the package contents
     * on load.  e.g. the story_reading tutor creates a custom "story" package that is based on the
     * story folder location.
     */
    public void initSoundPackage() {

        String langPath;
        String assetPath;

        if(!_packageInit) {

            _packageInit = true;

            // If we have set a language then update the sound source to point to the correct subdir
            // If no language set then use whichever language is used in the Flash XML
            // An audio source can force a language by setting "lang" to a known language Feature ID
            // e.g. LANG_SW | LANG_EN | LANG_FR

            // GRAY_SCREEN_BUG _scope.tutorName = "activity_selector"
            // GRAY_SCREEN_BUG returns null
            mMediaManager = CMediaController.getManagerInstance(_scope.tutorName());

            // GRAY_SCREEN_BUG X
            langPath = mMediaManager.mapSoundPackage(_scope.tutor(), soundpackage, lang);

            // Update the path to the sound source file
            // #Mod Dec 13/16 - Moved audio/storyName assets to external storage
            //
            mSoundSource = TCONST.AUDIOPATH + "/" + langPath + "/" + soundsource;

            assetPath = mMediaManager.mapPackagePath(_scope.tutor(), soundpackage);

            mSourcePath = assetPath + "/" + mSoundSource;

            mLocation = mMediaManager.mapPackageLocation(_scope.tutor(), soundpackage);
        }
    }



    // *** Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        super.loadJSON(jsonObj, scope);
    }

}
