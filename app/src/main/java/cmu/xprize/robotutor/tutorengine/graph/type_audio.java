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

import android.media.MediaPlayer;

import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.IMediaListener;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.TCONST;


/**
 * Media players are special objects as there is a system wide limit on how many can be active
 * at one time.  As a result we centralize creation and management of MediaPlayers to CMediaManager
 * where we can cache players across tutors as well as play/pause etc them globally
 */
public class type_audio extends type_action implements IMediaListener {

    // NOTE: we run at a Flash default of 24fps - which is the units in which
    // index and duration are calibrated

    private CMediaManager                 mMediaManager;
    private CMediaManager.mediaController mPlayer;

    private String                        mSoundSource;
    private String                        mSourcePath;

    // json loadable fields
    public String        command;
    public String        lang;
    public String        soundsource;
    public long          index = 0;

    final static public String TAG = "type_audio";



    public type_audio() {

        mMediaManager = CMediaManager.getInstance();
    }

    public void onDestroy() {

        mMediaManager.detachMediaPlayer(this);
    }

    //*******************************************************
    //**  Global Media Control Start

    private boolean mWasPlaying = false;

    @Override
    public void globalPause() {

        if(mPlayer.isPlaying()) {
            mWasPlaying = true;

            mPlayer.stop();
        }
    }

    @Override
    public void globalPlay() {

        if(mWasPlaying) {
            mWasPlaying = false;

            mPlayer.play();
        }
    }

    @Override
    public void globalStop() {

        if(mPlayer.isPlaying()) {
            mWasPlaying = true;

            mPlayer.releasePlayer();
        }
    }

    /**
     * Listen to the MediaController for completion events.
     *
     */
    @Override
    public void onCompletion() {

        // Release the mediaController for reuse
        //
        if(mPlayer != null)
            mPlayer.detach();

        // Flows automatically increment to next scenegraph node.
        //
        if(mode.equals(TCONST.AUDIOFLOW))
            _scope.tutor().eventNext();
    }

    //**  Global Media Control Start
    //*******************************************************



    /**
     *
     */
    @Override
    public void preEnter()
    {
        super.preEnter();

        String pathResolved = getScope().resolveTemplate(mSourcePath);

        // This allocates a MediaPController for use by this audio_node. The media controller
        // is a managed global resource of CMediaManager
        //
        mPlayer = mMediaManager.attachMediaPlayer(pathResolved, this);
    }


    @Override
    public String applyNode() {
        String status = TCONST.DONE;

        // play on creation if command indicates
        if(command.equals(TCONST.PLAY)) {

            play();

            // Events return done - so they may play on top of each other.
            // streams and flows WAIT until completion before continuing.
            //
            if(mode.equals(TCONST.AUDIOEVENT))
                status = TCONST.DONE;
            else
                status = TCONST.WAIT;
        }

        return status;
    }


    public void play() {

        if(mPlayer != null)
            mPlayer.play();
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

        if(lang != null) {
            langPath = mMediaManager.mapLanguage(lang);
        }
        else {
            langPath = mMediaManager.getLanguage(_scope.tutor());
        }

        // Update the path to the sound source file
        mSoundSource = TCONST.AUDIOPATH + "/" + langPath + "/" + soundsource;
        mSourcePath  = TCONST.TUTORROOT + "/" + TCONST.TDATA + "/" + mSoundSource;
    }

}
