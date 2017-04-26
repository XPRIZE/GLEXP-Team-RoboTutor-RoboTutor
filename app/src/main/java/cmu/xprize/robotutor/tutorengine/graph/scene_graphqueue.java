package cmu.xprize.robotutor.tutorengine.graph;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.TCONST;


public class scene_graphqueue extends scene_module {


    private final Handler   mainHandler = new Handler(Looper.getMainLooper());
    private HashMap         queueMap    = new HashMap();
    private boolean         _qDisabled  = false;

    static final String TAG = "scene_graphqueue";


    /**
     */
    public scene_graphqueue() {
    }



    @Override
    public String next() {
        return _moduleState;
    }


    /**
     */
    @Override
    public String cancelNode() {

        // If queue is in progress cancel operations.
        //
        Log.d(TAG, "Processing Terminate on: " + name );
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
    // IGrgaphEvent...  START
    //

    // Override to provid class specific functionality
    //
    @Override
    public void onEvent(IGraphEvent eventObject) {

        switch(eventObject.getType()) {

            case TCONST.TRACK_COMPLETE:

                Log.d(TAG, "Processing QEvent on: " +name + " :event -  " + TCONST.TRACK_COMPLETE);
                post(TCONST.NEXT_NODE);
                break;
        }
    }

    //
    // IGrgaphEvent...  END
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

                Log.d(TAG, "Processing run: " + _command + " on: " + name + " Target: " + _target);

                switch (_command) {

                    case TCONST.APPLY_NODE:

                        // If queue is in progress cancel operations.
                        //
                        if(_nextAction != null) {

                            terminateQueue();
                            _nextAction.cancelNode();

                            // Restart queue
                            //
                            _qDisabled  = false;
                            _nextAction = null;
                        }

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

//                                if(_nextAction.name.equals("WAIT on Error")) {
//                                    Log.d(TAG, "We are here");
//                                }

                                if(_nextAction.testFeatures()) {
                                    Log.d(TAG, "Processing action: " + _nextAction.name);
                                    validAction = true;
                                    break;
                                }
                            }

                            if(validAction) {

                                _nextAction.preEnter();
                                _moduleState = _nextAction.applyNode();

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
                            }
                        }
                        catch(Exception e) {
                            CErrorManager.logEvent(TAG,"modqueue apply failed: ", e, false);
                        }
                        break;


                    case TCONST.CANCEL_NODE:

                        cancelNode();
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
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

            Log.d(TAG, "Removing Post: " + entry.getKey());

            mainHandler.removeCallbacks((scene_graphqueue.Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(scene_graphqueue.Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(scene_graphqueue.Queue qCommand, long delay) {

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

        enQueue(new scene_graphqueue.Queue(command), delay);
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

        enQueue(new scene_graphqueue.Queue(command, target), delay);
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
