package cmu.xprize.comp_session;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_ask.CAskComponent;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.IButtonController;
import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.TCONST;


public class CActivitySelector extends FrameLayout implements IButtonController, ILoadableObject, IPublisher {

    protected Context           mContext;

    protected CAskComponent     SaskActivity;
    protected CDebugComponent   SdebugActivity;

    protected final Handler     mainHandler  = new Handler(Looper.getMainLooper());
    protected HashMap           queueMap     = new HashMap();
    protected HashMap           nameMap      = new HashMap();
    protected boolean           _qDisabled   = false;

    protected CAsk_Data         _activeLayout;
    protected int               _describeIndex;

    protected LocalBroadcastManager bManager;

    final private String  TAG = "CActivitySelector";



    public CActivitySelector(Context context) {
        super(context);
        init(context, null);
    }

    public CActivitySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CActivitySelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    public void onDestroy() {

        terminateQueue();
    }


    @Override
    protected void onDraw(Canvas canvas) {
    }

    public void rippleDescribe() {
    }

    public void describeNext() {
    }

    public void cancelPointAt() {

        Intent msg = new Intent(TCONST.CANCEL_POINT);
        bManager.sendBroadcast(msg);
    }



    //************************************************************************
    //************************************************************************
    // IButtonController Interface START

    @Override
    public void doDebugLaunchAction(String debugTutor) {

    }

    @Override
    public void doDebugTagLaunchAction(String tag) {

    }

    @Override
    public void doButtonBehavior(String buttonid) {
    }

    @Override
    public void doAskButtonAction(String actionid) {
    }

    /**
     * The session manager set the \<varname\>.intent and intentData scoped variables
     * for use by the scriptable Launch command. see type_action
     *
     * @param intent
     * @param intentData
     */
    public void doLaunch(String intent, String intentData, String dataSource, String tutorId, String matrix) {
    }

    // IButtonController Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START
    // TODO fix architecture...
    // this doesn't even need to "implement IBehaviorManager"? THere's never an object declared as IBehaviorManager... these methods are only used internally

    //@Override
    public void setVolatileBehavior(String event, String behavior) {

    }

    //@Override
    public void setStickyBehavior(String event, String behavior) {

    }

    //@Override
    public boolean applyBehavior(String event) {
        return false;
    }

    //@Override
    public void applyBehaviorNode(String nodeName) {

    }

    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _name;
        protected String _command;
        protected String _target;
        protected String _item;


        public Queue(String name, String command) {

            _name    = name;
            _command = command;

            if(name != null) {
                nameMap.put(name, this);
            }
        }

        public Queue(String name, String command, String target) {

            this(name, command);
            _target  = target;
        }

        public Queue(String name, String command, String target, String item) {

            this(name, command, target);
            _item    = item;
        }


        @Override
        public void run() {

            try {
                if(_name != null) {
                    nameMap.remove(_name);
                }

                queueMap.remove(this);

                switch(_command) {

                    case TCONST.APPLY_BEHAVIOR:

                        applyBehaviorNode(_target);
                        break;

                    case TCONST.POINT_AT_BUTTON:

                        SaskActivity.pointAtViewByName(_target);
                        break;

                    case TCONST.CANCEL_POINTAT:

                        cancelPointAt();
                        break;

                    case AS_CONST.RIPPLE_DESCRIBE:

                        rippleDescribe();
                        break;

                    case AS_CONST.DESCRIBE_NEXT:

                        describeNext();
                        break;

                    case AS_CONST.BUTTON_EVENT:

                        doButtonBehavior(_target);
                        break;

                    case AS_CONST.CANCEL_DESCRIBE:

                        break;

                    default:
                        break;
                }


            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, true);
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
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Remove named posts
     *
     */
    public void cancelPost(String name) {

        Log.d(TAG, "Cancel Post Requested: " + name);

        while(nameMap.containsKey(name)) {

            Log.d(TAG, "Post Cancelled: " + name);

            mainHandler.removeCallbacks((Queue) (nameMap.get(name)));
            nameMap.remove(name);
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

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

    public void postNamed(String name, String command, String target) {
        postNamed(name, command, target, 0L);
    }

    public void postNamed(String name, String command, String target, Long delay) {
        enQueue(new Queue(name, command, target), delay);
    }

    public void postNamed(String name, String command) {
        postNamed(name, command, 0L);
    }
    public void postNamed(String name, String command, Long delay) {
        enQueue(new Queue(name, command), delay);
    }


    /**
     * Post a command to the queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(null, command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new Queue(null, command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new Queue(null, command, target, item), delay); }


    public void postEvent(String event) {

        postEvent(event,0);
    }

    public void postEvent(String event, Integer delay) {

        post(event, delay);
    }

    public void postEvent(String event, String param, Integer delay) {

        post(event, param, delay);
    }

    public void postEvent(String event, String target) {

        post(event, target);
    }



    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************

    //************************************************************************
    //************************************************************************
    // IPublisher - START


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishState() {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishValue(String varName, String value) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishValue(String varName, int value) {
    }

    @Override
    public void publishFeatureSet(String featureset) {

    }

    @Override
    public void retractFeatureSet(String featureset) {

    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishFeature(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void retractFeature(String feature) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void publishFeatureMap(HashMap featureMap) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    @Override
    public void retractFeatureMap(HashMap featureMap) {
    }


    // IPublisher - EBD
    //************************************************************************
    //************************************************************************




    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

    }



}
