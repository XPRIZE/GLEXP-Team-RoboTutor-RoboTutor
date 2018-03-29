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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.IMediaListener;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.TCONST;


/**
 * This is a "timer" implementation using handlers instead of Timers.  Using Handlers
 * ensure that the timer runs in the UI Thread and therefore has access to the View hierarchy
 * which is not the case with Timers.  i.e. With a Timer you would not be able to change the
 * visibility state of a given View in the display list.
 *
 */
public class type_handler extends type_action implements IMediaListener {

        protected Handler   _handler   = null;
        protected String    _timerCmd  = TCONST.NONE;
        protected boolean   _playing   = false;
        protected Runnable  _frameTask = null;
        private   boolean   _reference = true;

        private CMediaManager mMediaManager;

        // json loadable
        public String    id;
        public long      period;
        public long      startdelay;
        public boolean   repeat;
        public String    action;
        public String    ontimer;

        final private String TAG = "type_handler";


        public type_handler() {
        }


        //*******************************************************
        //**  Global Media Control Start

        // TODO: Note that there is a constraint on timer restart - we canoot actually
        // pause a timer so we don't pick up at the same point in the count when we resume
        // we are starting it from scratch each time a global pause / play occurs.
        //
        private boolean mWasPlaying = false;

    @Override
    public String sourceName() {
        return "type_handler";
    }

    @Override
    public String resolvedName() {
        return (id == null)? "":id;
    }

    @Override
        public void globalPause() {

            globalStop();
        }

        @Override
        public void globalPlay() {

            if(mWasPlaying) {
                mWasPlaying = false;

                startTimer();
            }
        }

        @Override
        public void globalStop() {

            if(_playing) {
                mWasPlaying = true;

                stopTimer();
            }
        }

    @Override
    public boolean isLooping() {
        return false;
    }


    @Override
    public float getVolume() {
        return -1;
    }


    @Override
        public void onCompletion(CMediaManager.PlayerManager playerManager) {
            // NOOP
        }


        //**  Global Media Control Start
        //*******************************************************


        /**
         * Apply the timer action
         */
        @Override
        public String applyNode() {

            type_handler obj = null;

            // Non reference nodes create the actual java timer and own it.
            // Note that it is expected that the timer will be CREATED before any other
            // nodes access it.  However if a call is made against a uninitialized timer
            // it will simply be ignored.
            //
            if (!mMediaManager.hasHandler(id)) {

                Log.d("ISREADING", "id: " + id + " - action: " + action );

                switch (action) {
                    case TCONST.CREATEANDSTART:

                        _timerCmd = TCONST.START;
                        // Fall through

                    case TCONST.CREATE:

                        createHandler();
                        break;

                    default:
                        RoboTutor.logManager.postEvent_D(_logType, "node.handler.applynode:id:" + id + ",error:call on uninitialized handler");

                        break;
                }
            }

            // Reference nodes get a reference to the owner timer through the MediaManager
            // and perform their action on the timer by updating the owners "action" and
            // calling appyNode to execute on the actual timer.
            //
            if (_reference) {

                Log.d("ISREADING", "id: " + id + " is reference ");

                try {
                    // Note - we testFeatures on "this" reference-node not the handler owner-node which may have
                    // different priviledges.  i.e we may not want to create a handler but always cancel it if the
                    // feature set changes in mid-count.
                    //
                    if(testFeatures()) {

                        obj = (type_handler) mMediaManager.mapHandler(id);

                        // Apply the reference action to the actual timer
                        if (obj != null) {
                            obj._timerCmd = action;
                            obj.applyNode();
                        }
                        else {

                            RoboTutor.logManager.postEvent_D(_logType, "node.handler.applynode:id:" + id + ",error:call on uninitialized handler reference");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Log.d("ISREADING", "id: " + id + " timerCmd: " + _timerCmd);

                switch (_timerCmd) {

                    case TCONST.START:
                        startTimer();
                        break;

                    case TCONST.RESET:
                        destroyTimer();
                        createHandler();
                        break;

                    case TCONST.RESTART:
                        destroyTimer();
                        createHandler();
                        startTimer();
                        break;

                    case TCONST.STOP:
                    case TCONST.CANCEL:
                        destroyTimer();
                        break;
                }
            }
            return TCONST.DONE;
        }


        private void createHandler() {

            _handler = new Handler(Looper.getMainLooper());
            _reference = false;

            Log.d("ISREADING", "created id: " + id  );

            mMediaManager.createHandler(id, this);

            Log.d("ISREADING", "IS_valid id: " + mMediaManager.hasHandler(id));
        }


        private void startTimer() {

            _frameTask = new Runnable() {
                @Override
                public void run() {
                    IScriptable2 obj = null;

                    try {
                        RoboTutor.logManager.postEvent_V(_logType, "target:node.handler,name:" + name + ",event:timeout" + "run_method:" + ontimer);

                        // Recover the node to be executed when the timer expires
                        // and apply it.
                        //
                        obj = _scope.mapSymbol(ontimer);

                        if(obj != null && obj.testFeatures()) {
                            obj.applyNode();
                        }

                        if(repeat) {
                            RoboTutor.logManager.postEvent_V(_logType, "target:node.handler.repeat,name:" + name );

                            _handler.postDelayed(_frameTask, period);
                        }

                        else {

                            _frameTask = null;
                            destroyTimer();
                        }

                    } catch (Exception e) {
                        RoboTutor.logManager.postEvent_V(_logType, "target:node.handler,name:" + name + "event:timeout failed");

                        // TODO: Manage invalid Timer Behavior
                        e.printStackTrace();
                    }
                }
            };

            RoboTutor.logManager.postEvent_I(_logType, "target:node.handler.start,name:" + name + ",timeout:" + period);

            _playing = true;
            _handler.postDelayed(_frameTask, period);
        }



        private void stopTimer() {

            if(_playing) {

                RoboTutor.logManager.postEvent_V(_logType, "target:node.handler.stop,name:" + name);

                if (_frameTask != null)
                    _handler.removeCallbacks(_frameTask);

                _handler   = null;
                _frameTask = null;

                _playing = false;
            }
        }


        private void destroyTimer() {

            if(_playing) {

                RoboTutor.logManager.postEvent_V(_logType, "target:node.handler.destroy,name:" + name);

                stopTimer();

                mMediaManager.removeHandler(id);
            }
            else {

                RoboTutor.logManager.postEvent_V(_logType, "target:node.handler.destroy,state:not_playing,name:" + name);
            }
        }



        // *** Serialization



        @Override
        public void loadJSON(JSONObject jsonObj, IScope2 scope) {

            String langPath;

            super.loadJSON(jsonObj, scope);

            // Custom post processing.

            // Make the id tutor specific - i.e. name must be unique within tutor
            // however MediaManger aggregates them into a single Map so we need to
            // make id which is the map-key tutor specific.
            //
            id = scope.tutor().mTutorName + id;

            mMediaManager = CMediaController.getManagerInstance(scope.tutorName());

        }
}
