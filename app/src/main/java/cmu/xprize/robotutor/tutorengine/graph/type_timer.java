
package cmu.xprize.robotutor.tutorengine.graph;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.common.TCONST;

/**
 *
 {"type": "TIMER", "duration":"20", "repeat":"false", "action":"START", "ontimer":"ACTION_NAME" },
 {"type": "TIMER", "id": "hint_timeout", "action": "START" },
 {"type": "TIMER", "id": "hint_timeout", "action": "STOP" },
 {"type": "TIMER", "id": "hint_timeout", "action": "CANCEL" }

 */

public class type_timer extends graph_node {

    protected Timer     _timer     = null;
    protected String    _timerCmd  = TCONST.NONE;
    protected boolean   _playing   = false;
    protected TimerTask _frameTask = null;
    private   boolean   _reference = true;

    // json loadable
    public String    id;
    public long      period;
    public long      startdelay;
    public boolean   repeat;
    public String    action;
    public String    ontimer;

    final private String TAG = "type_timer";

    /**
     * Apply the timer action
     */
    @Override
    public String applyNode() {

        type_timer obj = null;

        if(!CTutor.hasTimer(id)) {
            switch (action) {

                case TCONST.CREATEANDSTART:
                    _timerCmd = TCONST.START;

                case TCONST.CREATE:
                    _timer     = new Timer(id);
                    _reference = false;
                    CTutor.createTimer(id, this);
                    break;
            }
        }

        if(_reference) {

            try {
                obj = (type_timer) CTutor.mapTimer(id);

                // Apply the reference action to the actual timer
                if(obj != null) {
                    obj._timerCmd = action;
                    obj.applyNode();
                }

            } catch (Exception e) {
                // TODO: Manage invalid Button Behavior
                e.printStackTrace();
            }
        }
        else switch(_timerCmd) {

            case TCONST.START:
                startTimer();
                break;

            case TCONST.STOP:
            case TCONST.CANCEL:
                stopTimer();
                break;
        }

        return TCONST.DONE;
    }


    private void startTimer() {

        _frameTask = new TimerTask() {
            @Override
            public void run() {
                IScriptable obj = null;

                try {
                    obj = CTutor.getScope().mapSymbol(ontimer);
                    obj.applyNode();

                    if (!repeat)
                        stopTimer();

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

            CTutor.removeTimer(id);
        }
    }
}
