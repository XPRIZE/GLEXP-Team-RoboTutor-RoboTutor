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

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileNotFoundException;

import cmu.xprize.util.IScope;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

public class type_audio extends type_action implements OnPreparedListener, OnCompletionListener {

    // NOTE: we run at a Flash default of 24fps - which is the units int which
    // index and duration are calibrated

    private MediaPlayer  mPlayer;
    private String       mSoundSource;
    private String       mSourcePath;

    private boolean      mPlaying       = false;
    private boolean      mIsReady       = false;
    private boolean      mDeferredStart = false;
    private boolean      mDeferredSeek  = false;
    private long         mSeekPoint     = 0;

    private String       cachedSource   = "";

    private OnCompletionListener listener;


    // json loadable fields
    public String        command;
    public String        lang;
    public String        soundsource;
    public long          index = 0;


    public type_audio() {
    }


    /**
     * TODO: Look at disposing of Media Players once scene is finished - optimization
     */
    @Override
    public void preEnter()
    {
        super.preEnter();

        // Custom post processing.
        // If there are probabilities defined for the feature
        // generate an array of the prob for iterations of this pid
        try {
            String pathResolved = getScope().resolveTemplate(mSourcePath);

            // If the sound source doesn't change then we play the source we have already
            // reduces play latency.
            if(!cachedSource.equals(pathResolved)) {

                cachedSource = pathResolved;
                mIsReady     = false;

                mPlayer = new MediaPlayer();

                AssetFileDescriptor soundData = CTutor.getAssetManager().openFd(pathResolved);

                Log.d(TAG, "Audio Loading: " + pathResolved);

                mPlayer.setDataSource(soundData.getFileDescriptor(), soundData.getStartOffset(), soundData.getLength());
                soundData.close();
                mPlayer.setOnPreparedListener(this);
                mPlayer.setOnCompletionListener(this);
                mPlayer.prepareAsync();
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Audio Error: " + e);
            System.exit(1);

        } catch (Exception e) {
            Log.e(TAG, "Audio frame format error: " + e);
        }
    }


    @Override
    public String applyNode() {
        String status = TCONST.DONE;

        // play on creation if command indicates
        if(command.equals(TCONST.PLAY)) {
            //preEnter();       ## duplicate if this is Root node
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

        if(!mPlaying) {
            if(mIsReady) {
                mPlayer.start();
                mPlaying = true;
            }
            else
                mDeferredStart = true;
        }
    }


    public void stop() {

        pause();
        seek(0L);
    }


    public void pause() {
        if(mPlaying)
            mPlayer.pause();

        mPlaying = false;
    }


    public void seek(long frame) {

        // calc relative frame to seek to

        mSeekPoint = frame - index;

        if(mIsReady) {
            int iframe = (int) (frame * 1000 / TCONST.FPS);

            seekTo(iframe);
        }
        else
            mDeferredSeek = true;

    }


    public void seekTo(int frameTime) {

        // No errors occur - but don't try to seek past the end
        if(frameTime < mPlayer.getDuration())
            mPlayer.seekTo(frameTime);
    }


    public void setOnCompletionListener(OnCompletionListener callback) {
        listener = callback;
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


    @Override
    public void onCompletion(MediaPlayer mp) {

        mPlayer.pause();
        mPlayer.seekTo(0);
        mPlaying = false;

        // Flows automatically increment to next animation node.
        //
        if(mode.equals(TCONST.AUDIOFLOW))
            CTutor.mTutorNavigator.onButtonNext();

        if(listener != null) {
            listener.onCompletion(mp);
        }
    }



    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        String langPath;

        super.loadJSON(jsonObj, scope);

        // Custom post processing.

        // If we have set a language then update the sound source to point to the correct subdir
        // If no language set then use whichever language is used in the Flash XML
        // An audio source can force a language by setting "lang" to a known language ID
        // e.g. LANG_SW | LANG_EN | LANG_FR

        if(lang != null) {
            langPath = CTutor.mapLanguage(lang);
        }
        else {
            langPath = CTutor.getLanguage();
        }

        // Update the path to the sound source file
        mSoundSource = langPath + "/" + soundsource;
        mSourcePath  = TCONST.TUTORROOT + "/" + TCONST.TDATA + "/" + mSoundSource;
    }

}
