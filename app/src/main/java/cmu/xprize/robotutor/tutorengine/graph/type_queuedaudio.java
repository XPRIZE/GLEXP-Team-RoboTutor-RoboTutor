package cmu.xprize.robotutor.tutorengine.graph;


import android.util.Log;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

public class type_queuedaudio extends type_audio {



    public type_queuedaudio() {
        super();

        _logType = QGRAPH_MSG;
    }

    //*******************************************************
    //**  IMediaListener Start

    /**
     * Listen to the MediaController for completion events.
     */
    @Override
    public void onCompletion(CMediaManager.PlayerManager playerManager) {

        RoboTutor.logManager.postEvent_D(_logType, "target:node.queuedaudio,event:oncompletion");

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

                RoboTutor.logManager.postEvent_D(_logType, "target:node.queuedaudio:emit.flow");
                dispatchEvent(new CGraphEvent(TCONST.TRACK_COMPLETE, "na", "na"));
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

    //**  IMediaListener END
    //*******************************************************

    //************************************************
    // IGrgaphEvent...  START
    //

    @Override
    public boolean isGraphEventSource() {
        return true;
    }

    //
    // IGrgaphEvent...  END
    //************************************************


}
