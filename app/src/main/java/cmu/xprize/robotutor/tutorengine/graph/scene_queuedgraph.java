package cmu.xprize.robotutor.tutorengine.graph;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.AUDIO_EVENT;
import static cmu.xprize.util.TCONST.DEBUG_HESITATE;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.TYPE_AUDIO;


public class scene_queuedgraph extends scene_module {


    private final Handler   mainHandler = new Handler(Looper.getMainLooper());
    private HashMap         queueMap    = new HashMap();
    private boolean         _qDisabled  = false;

    static final String TAG = "scene_queuedgraph";


    /**
     */
    public scene_queuedgraph() {
        super();

        _logType = QGRAPH_MSG;
    }



    @Override
    public String next() {
        return _moduleState;
    }


    /**
     */
    @Override
    public String cancelNode() {

        // MATH_HESITATE (mimic). -1 is there a thing that happens here???
        Log.d(DEBUG_HESITATE, "scene_queuedgraph.cancelNode" + name);
        // If queue is in progress cancel operations.
        //
        RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,action:cancelnode,name:" + name );
        terminateQueue();

        // If there is an active node e.g. audioqueue - kill it off
        //
        if(_nextAction != null) {

            _nextAction.cancelNode();
            _nextAction = null;
        }

        return TCONST.NONE;
    }


    @Override
    public String applyNode() {

        _qDisabled   = false;

        post(TCONST.APPLY_NODE);

        return _moduleState;
    }


    //************************************************
    // IEvent...  START
    //

    // Override to provid class specific functionality
    //
    @Override
    public void onEvent(IEvent eventObject) {

        try {
            switch (eventObject.getType()) {

                case TYPE_AUDIO:

                    switch ((String) eventObject.getString(AUDIO_EVENT)) {

                        case TCONST.TRACK_COMPLETE:
                            RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,action:post-next_node,event:trackcomplete,name:" + name);
                            post(TCONST.NEXT_NODE);
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
        catch(Exception ex) {
            Log.e(_logType, "ERROR:node.queuedgraph,action:onevent,name:" + name );
        }
    }

    //
    // IEvent...  END
    //************************************************


    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String  _command;
        protected Object  _target;

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, Object target) {
            _command = command;
            _target  = target;
        }


        @Override
        public void run() {


            try {
                queueMap.remove(this);

                switch (_command) {

                    case TCONST.APPLY_NODE:

                        // If queue is in progress cancel operations.
                        //
                        if(_nextAction != null) {

                            Log.d(_logType, "target:node.queuedgraph,action:interrupt.inprogress,name:" + name );

                            terminateQueue();
                            _nextAction.cancelNode();

                            // Restart queue
                            //
                            _qDisabled  = false;
                            _nextAction = null;
                        }

                        Log.d(_logType, "target:node.queuedgraph,action:apply.root,name:" + name );

                        // If the node is completed and reusable then reset
                        //
                        _ndx         = 0;
                        _moduleState = TCONST.READY;

                        preEnter();
                        post(TCONST.NEXT_NODE);
                        break;

                    case TCONST.NEXT_NODE:
                        
                        String result       = TCONST.READY;
                        boolean validAction = false;

                        try {
                            if(_nextAction != null)
                                _nextAction.preExit();

                            // Issue #58 - Make all actions feature reactive.
                            //
                            while(_ndx < tracks.length) {

                                _nextAction = tracks[_ndx];
                                _ndx++;

                                if(_nextAction.testFeatures()) {
                                    validAction = true;
                                    break;
                                }
                            }

                            if(validAction) {

                                RoboTutor.logManager.postEvent_I(_logType, "target:node.queuedgraph,name:" + _nextAction.name + ",startstate:" + _moduleState + ",maptype:" + _nextAction.maptype + ",mapname:" + _nextAction.mapname);

                                if(_nextAction.testFeatures()) {
                                    _nextAction.preEnter();
                                    _moduleState = _nextAction.applyNode();
                                }
                                else {
                                    _moduleState = TCONST.DONE;
                                }
                                RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,name:" + _nextAction.name + ",endstate:" + _moduleState);

                                switch (_moduleState) {

                                    // TCONST.WAIT indicates that next node will be driven by a
                                    // completion event from the current action
                                    //
                                    case TCONST.WAIT:
                                        break;

                                    default:
                                        post(TCONST.NEXT_NODE);
                                        break;
                                }
                            }
                            else {
                                // RESET the nextAction so it will restart
                                //
                                preExit();
                                _nextAction = null;

                                RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,END_GRAPH:COMPLETE");
                            }
                        }
                        catch(Exception e) {
                            // GRAY_SCREEN_BUG here is where the error is caught
                            CErrorManager.logEvent(_logType,"target:node.queuedgraph: apply failed: ", e, false);
                        }
                        break;


                    case TCONST.CANCEL_NODE:

                        cancelNode();
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(_logType, "target:node.queuedgraph: Run Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;

        RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,action:terminatequeue,name:" + name);
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    public void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            RoboTutor.logManager.postEvent_V(_logType, "target:node.queuedgraph,action:removepost,name:" + entry.getKey());

            mainHandler.removeCallbacks((scene_queuedgraph.Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(scene_queuedgraph.Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(scene_queuedgraph.Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new scene_queuedgraph.Queue(command), delay);
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, Object target) {
        post(command, target, 0);
    }
    public void post(String command, Object target, long delay) {

        enQueue(new scene_queuedgraph.Queue(command, target), delay);
    }


    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************





    // *** Serialization




    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        super.loadJSON(jsonObj, (IScope2) scope);

        // Look for GraphEventSource objects that work outside the scene_graph event loop
        // and listen for their events.
        //
        for(type_action node: tracks) {

            if(node.isGraphEventSource()) {
                node.addEventListener(this);
            }
        }
    }

}
