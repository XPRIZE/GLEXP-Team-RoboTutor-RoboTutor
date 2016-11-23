//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.IMediaListener;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.CFileNameHasher;
import cmu.xprize.util.TCONST;


/**
 * Media players are special objects as there is a system wide limit on how many can be active
 * at one time.  As a result we centralize creation and management of MediaPlayers to CMediaManager
 * where we can cache players across tutors as well as play/pause etc globally
 */
public class type_audio extends type_action implements IMediaListener {

    // NOTE: we run at a Flash default of 24fps - which is the units in which
    // index and duration are calibrated

    private CFileNameHasher             mFileNameHasher;
    private CMediaManager               mMediaManager;
    private CMediaManager.PlayerManager mPlayer;
    private boolean                     mPreLoaded = false;

    private String mSoundSource;
    private String mSourcePath;
    private String mResolvedName;
    private String mPathResolved;

    private boolean _useHashName = true;

    // json loadable fields
    public String command;
    public String lang;
    public String soundsource;
    public String soundpackage;

    public boolean  repeat = false;
    public float    volume = -1f;
    public long     index  = 0;


    final static public String TAG = "type_audio";


    public type_audio() {
        mFileNameHasher = CFileNameHasher.getInstance();
    }

    /**
     * TODO: onDestroy not being called when tutor is killed
     */
    public void onDestroy() {

        mMediaManager.detachMediaPlayer(this);
    }

    //*******************************************************
    //**  Global Media Control Start

    private boolean mWasPlaying = false;

    @Override
    public String sourceName() {
        return soundsource;
    }

    @Override
    public String resolvedName() {
        return (mResolvedName == null) ? "" : mResolvedName;
    }

    @Override
    public void globalPause() {

        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mWasPlaying = true;

                mPlayer.stop();
            }
        }
    }

    @Override
    public void globalPlay() {

        if (mPlayer != null) {
            if (mWasPlaying) {
                mWasPlaying = false;

                Log.i(TAG, "global play");
                mPlayer.play();
            }
        }
    }

    @Override
    public void globalStop() {

        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mWasPlaying = true;

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
                Log.d(TAG, "Processing: Audio Flow");
                _scope.tutor().eventNext();
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

        mPathResolved = getScope().parseTemplate(mSourcePath);

        Log.i(TAG, "Preloading audio: " + mPathResolved);

        int endofPath = mPathResolved.lastIndexOf("/") + 1;

        // Extract the path and name portions
        // NOTE: ASSUME mp3 - trim the mp3 - we just want the filename text to generate the Hash
        //
        String pathPart = mPathResolved.substring(0, endofPath);
        String namePart = mPathResolved.substring(endofPath, mPathResolved.length() - 4);

        // Note we keep this decomposition to provide the resolved name for debug messages
        //
        if (_useHashName) {

            // Permit actual hash's in the script using the # prefix
            //
            if (namePart.startsWith("#")) {
                mResolvedName = namePart.substring(1);
            }
            // Otherwise generate the hash from the text
            else {
                mResolvedName = mFileNameHasher.generateHash(namePart);
            }
        } else {
            mResolvedName = namePart;
        }

        // add the extension back on the generated filename hash
        //
        mPathResolved = pathPart + mResolvedName + ".mp3";

        // This allocates a MediaPController for use by this audio_node. The media controller
        // is a managed global resource of CMediaManager
        //
        mPlayer = mMediaManager.attachMediaPlayer(mPathResolved, owner);

        mPreLoaded = true;
    }


    @Override
    public String applyNode() {

        String status = TCONST.DONE;

        // If the feature test passes then fire the event.
        // Otherwise set flag to indicate event was completed/skipped in this case
        // Issue #58 - Make all actions feature reactive.
        //
        if (testFeatures()) {

            // Non type_timeline audio tracks are not preloaded. So do it inline. This just has a
            // higher latency between the call and when the audio is actually ready to play.
            //
            if (!mPreLoaded) {
                preLoad(this);
            }
            mPreLoaded = false;

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
                //
                // TCONST.FLOWEVENT automatically advances
                else {
                    status = TCONST.WAIT;
                }
            }
        }

        return status;
    }


    @Override
    public String cancelNode() {

        stop();

        mPlayer.detach();
        mPlayer = null;

        Log.i(TAG, "cancelNode - PlayerDetached");

        return TCONST.NONE;
    }


    public void play() {

        if(mPlayer != null) {
            Log.i(TAG, "play");
            mPlayer.play();

            // AUDIOEVENT mode tracks are fire and forget - i.e. we discnnect from the player
            // and let it continue to completion independently.
            //
            // This allows this Audio element to be reused immediately - So we can fire another
            // instance of the sound while the other is still playing.
            //
            if(mode == TCONST.AUDIOEVENT) {
                mPlayer = null;
            }
        }
    }


    public void stop() {

        if(mPlayer != null)
            mPlayer.stop();
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



    // *** Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        String langPath;

        super.loadJSON(jsonObj, scope);


        // Custom post processing.

        // If we have set a language then update the sound source to point to the correct subdir
        // If no language set then use whichever language is used in the Flash XML
        // An audio source can force a language by setting "lang" to a known language ID
        // e.g. LANG_SW | LANG_EN | LANG_FR

        mMediaManager = CMediaController.getManagerInstance(_scope.tutor());

        langPath = mMediaManager.mapSoundPackage(_scope.tutor(), soundpackage, lang);

        // Update the path to the sound source file
        //
        mSoundSource = TCONST.AUDIOPATH + "/" + langPath + "/" + soundsource;
        mSourcePath  = TCONST.TUTORROOT + "/" + TCONST.TDATA + "/" + mSoundSource;
    }

}
