
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
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.TCONST;

/**
 * type_timer are nodes that perform actions on Named timers.  What this means is that the node that
 * creates the timer isn't the one that will stop it or cancel it so they access the actual timer by
 * reference.
 *
 * type_timers with "action" of CREATE or CREATEANDSTART actually create a timer instance and store
 * a reference to themselves in the MediaManager which is mapped by the timer Name.  Other nodes with
 * START / STOP "actions' etc recover a reference to the owner node (not the actual timer) by the timer
 * Name to perform operations on the actual timer through the reference.
 *
 {"type": "TIMER", "duration":"20", "repeat":"false", "action":"START", "ontimer":"ACTION_NAME" },
 {"type": "TIMER", "id": "hint_timeout", "action": "START" },
 {"type": "TIMER", "id": "hint_timeout", "action": "STOP" },
 {"type": "TIMER", "id": "hint_timeout", "action": "CANCEL" }
 */
public class type_timer extends type_action implements IMediaListener {

    protected Timer     _timer     = null;
    protected String    _timerCmd  = TCONST.NONE;
    protected boolean   _playing   = false;
    protected TimerTask _frameTask = null;
    private   boolean   _reference = true;

    private CMediaManager mMediaManager;


    // json loadable
    public String    id;
    public long      period;
    public long      startdelay;
    public boolean   repeat;
    public String    action;
    public String    ontimer;

    final private String TAG = "type_timer";


    public type_timer() {
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
        return "type_timer";
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

        type_timer obj = null;

        // Non reference nodes create the actual java timer and own it.
        // Note that it is expected that the timer will be CREATED before any other
        // nodes access it.
        // However if a call is made against a uninitialized timer it will simply be
        // ignored.
        //
        if (!mMediaManager.hasTimer(id)) {

            switch (action) {
                case TCONST.CREATEANDSTART:
                    _timerCmd = TCONST.START;

                case TCONST.CREATE:
                    createTimer();
                    break;

                default:
                    Log.i(TAG, "Timer: " + id + " - call on uninitialized timer");

                    break;
            }
        }

        // Reference nodes get a reference to the owner timer through the MediaManager
        // and perform their action on the timer by updating the owners "action" and
        // calling appyNode to execute on the actual timer.
        //
        if (_reference) {

            try {
                // Note - we testFeatures on "this" reference-node not the handler owner-node which may have
                // different priviledges.  i.e we may not want to create a handler but always cancel it if the
                // feature set changes in mid-count.
                //
                if(testFeatures()) {

                    obj = (type_timer) mMediaManager.mapTimer(id);

                    // Apply the reference action to the actual timer
                    if (obj != null) {
                        obj._timerCmd = action;
                        obj.applyNode();
                    }
                    else {

                        RoboTutor.logManager.postEvent_D(_logType, "node.timer.applynode:id:" + id + ",error:call on uninitialized timer reference");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else switch (_timerCmd) {

            case TCONST.START:
                startTimer();
                break;

            case TCONST.RESET:
                stopTimer();
                createTimer();
                break;

            case TCONST.RESTART:
                stopTimer();
                createTimer();
                startTimer();
                break;

            case TCONST.STOP:
            case TCONST.CANCEL:
                stopTimer();
                break;
        }

        return TCONST.DONE;
    }


    private void createTimer() {
        _timer     = new Timer(id);
        _reference = false;
        mMediaManager.createTimer(id, this);
    }


    private void startTimer() {

        _frameTask = new TimerTask() {
            @Override
            public void run() {
                IScriptable2 obj = null;

                try {
                    if (!repeat)
                        stopTimer();

                    // Recover the node to be executed when the timer expires
                    // and apply it.
                    //
                    obj = _scope.mapSymbol(ontimer);
                    if(obj != null && obj.testFeatures()) {
                        obj.applyNode();
                    }

                } catch (Exception e) {
                    // TODO: Manage invalid Timer Behavior
                    e.printStackTrace();
                }
            }
        };

        _playing = true;

        if (repeat)
            _timer.scheduleAtFixedRate(_frameTask, startdelay, period);
        else
            _timer.schedule(_frameTask, period);
    }


    private void stopTimer() {

        if(_playing) {
            Log.i(TAG, "Killing Timer: " + name);
            if (_frameTask != null)
                _frameTask.cancel();

            _timer.cancel();
            _timer = null;
            _frameTask = null;

            _playing = false;

            mMediaManager.removeTimer(id);
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
