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

package cmu.xprize.bp_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import java.util.*;


public class CBP_Component extends FrameLayout implements IEventDispatcher, ILoadableObject {

    // Make this public and static so sub-components may use it during json load to instantiate
    // controls on the fly.
    //
    static public Context           mContext;

    public List<IEventListener>     mListeners          = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews;
    protected boolean               mListenerConfigured = false;

    public CBP_LetterBoxLayout      Scontent;

    protected String                mDataSource;

    protected CBp_Data              _currData;
    public    int                   question_Index;
    public    int                   logQuestionIndex; // TODO: This is the workaround to get correct problem number in log. We should refactor and use existing question_Index instead.
    private   int                   _dataIndex = 0;

    protected IBubbleMechanic       _mechanics;

    private   boolean               correct = false;
    public int                      attempt_count;
    protected int                   correct_Count;

    private final Handler           mainHandler = new Handler(Looper.getMainLooper());
    private HashMap                 queueMap    = new HashMap();
    private boolean                 _qDisabled  = false;

    protected LocalBroadcastManager   bManager;


    // Working data sets
    //

    public ArrayList<String>        wrk_responseSet = null;             // set of response tems
    public ArrayList<String>        wrk_respTypeSet = null;             // text/reference - for mixed response sets
    public ArrayList<String[]>      wrk_response_script = null;         // List of uttereances describing each potential response

    public ArrayList<String>        wrk_answerSet       = null;         // Answer for each stimulus (question)
    public ArrayList<String>        wrk_answerTypeSet   = null;         // text/reference - for mixed stimulus items
    public ArrayList<String[]>      wrk_answer_script   = null;         // List of uttereances describing each answer

    public ArrayList<String>        wrk_stimulusSet     = null;         // set of stimulus items (questions)
    public ArrayList<String>        wrk_stimTypeSet     = null;         // text/reference - for mixed stimulus items
    public ArrayList<String[]>      wrk_stimulus_script = null;         // List of uttereances describing each question


    // Note: Having independent response sets and answer sets means that the response sets are not limited to the
    //       set of answers to questions.
    //

    // json loadable
    public String                   gen_responsetype    = null;         // global text/reference - if all response items are same type
    public String[]                 gen_responseSet     = null;         // set of response tems
    public String[]                 gen_respTypeSet     = null;         // text/reference - for mixed response sets
    public String[][]               gen_response_script = null;         // List of uttereances describing each potential response

    public String                   gen_answertype      = null;         // global text/reference - if all question are same type
    public String[]                 gen_answerSet       = null;         // Answer for each stimulus (question)
    public String[]                 gen_answerTypeSet   = null;         // text/reference - for mixed stimulus items
    public String[][]               gen_answer_script   = null;         // List of uttereances describing each answer

    public String                   gen_stimulustype    = null;         // global text/reference - if all question are same type
    public String[]                 gen_stimulusSet     = null;         // set of stimulus items (questions)
    public String[]                 gen_stimTypeSet     = null;         // text/reference - for mixed stimulus items
    public String[][]               gen_stimulus_script = null;         // List of uttereances describing each question

    public int                      question_count      = 10;           // by default limit to 10 questions.
    public String                   question_sequence   = "SEQUENTIAL"; // question order - random / sequential (i.e. in stimulusSet order)

    public boolean                  question_replacement= false;        // Whether to allow questions to appear more than once when random order
    public boolean                  response_replacement= false;        // Whether to allow response sets to include duplicates (and duplicate correct ans)

    public CBp_Data[]               dataSource          = null;         // Question specific datasource

    public CBpBackground            view_background     = null;         // Set specific background
    public String                   banner_color        = null;         // Set specific banner color
    public int                      mask_alpha          = 205;          // Mask alpha

    public String                   problem_type     = "normal";

    static final String TAG = "CBP_Component";



    public CBP_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CBP_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CBP_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }



    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        inflate(getContext(), R.layout.bubblepop_container, this);

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {

                mDataSource = a.getString(R.styleable.RoboTutor_dataSource);

                String linkedViews;

                linkedViews = a.getNonResourceString(R.styleable.RoboTutor_linked_views);

                if(linkedViews != null) {
                    mLinkedViews = Arrays.asList(linkedViews.split(","));
                }

            } finally {
                a.recycle();
            }
        }

        // Get the letterboxed game container
        //
        Scontent = (CBP_LetterBoxLayout) findViewById(R.id.Scontent);

        // Capture the local broadcast manager
        //
        bManager = LocalBroadcastManager.getInstance(mContext);

        // Allow onDraw to be called to start animations
        //
        setWillNotDraw(false);
    }


    /**
     * The game mechanic uses this to get the game container where it will create
     * all the game controls.  The purpose of this is to make the game resolution invariant.
     *
     * @return
     */
    public CBP_LetterBoxLayout getContainer() {
        return Scontent;
    }


    public void onDestroy() {

        terminateQueue();

        if(_mechanics != null) {
            _mechanics.onDestroy();
            _mechanics = null;
        }
    }


    public void setDataSource(CBp_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;

        // If presenting stimulus values sequentially then we use this to track the current value.
        //
        question_Index = 0;
        logQuestionIndex = 0;
        correct_Count  = 0;
    }


    private String[] populateArray(String source, int size) {

        String[] target = new String[size];

        for(int i1 = 0 ; i1 < size ; i1++) {
            target[i1] = source;
        }

        return target;
    }


    /**
     * These are convenience operations so the datasource doesn't need redundant info
     * in its encoded JSON form.
     */
    protected void preProcessDataSource() {

        // If question count is 0 - show all item in the stimulus set
        //
        if(question_count == 0) {
            question_count = gen_stimulusSet.length;
        }

        // gen_xxx...type values are used when the type is consistent throughout
        // so we populate the xxx...typeSet array with this value.
        //
        if(gen_responsetype != null)
            gen_respTypeSet = populateArray(gen_responsetype, gen_responseSet.length);

        if(gen_answertype != null)
            gen_answerTypeSet = populateArray(gen_answertype, gen_answerSet.length);

        if(gen_stimulustype != null)
            gen_stimTypeSet = populateArray(gen_stimulustype, gen_stimulusSet.length);

        // Once per problem set we init these arraylists.
        // populate the working ArrayList so we can easily delete elements on demand
        // TODO: update jSONHelper to support ArrayList serialization if possible.
        //
        wrk_answerSet       = new ArrayList<String>(Arrays.asList(gen_answerSet));
        wrk_answerTypeSet   = new ArrayList<String>(Arrays.asList(gen_answerTypeSet));

        if(gen_answer_script != null)
            wrk_answer_script   = new ArrayList<String[]>(Arrays.asList(gen_answer_script));


        wrk_stimulusSet     = new ArrayList<String>(Arrays.asList(gen_stimulusSet));
        wrk_stimTypeSet     = new ArrayList<String>(Arrays.asList(gen_stimTypeSet));

        if(gen_stimulus_script != null)
            wrk_stimulus_script = new ArrayList<String[]>(Arrays.asList(gen_stimulus_script));

        // Preprocess the response set data so the size is available for the randomizer
        //
        preProcessQuestion();

        //Randomly shuffle questions in datasource (from stackoverflow)
        if(question_sequence.equals("RANDOM")) {
            for (int i = dataSource.length - 1; i > 0; i--) {
                int j = (int)(Math.floor(Math.random() * (i + 1)));
                CBp_Data f = dataSource[i];
                dataSource[i] = dataSource[j];
                dataSource[j] = f;
            }
        }

    }


    public void next() {

        try {

            if (dataSource != null) {

                updateDataSet(dataSource[_dataIndex]);

                // We cycle through the dataSource question types iteratively
                //
                _dataIndex++;
                _dataIndex %= dataSource.length;

                // Count down the number of questions requested
                // Increment question (stimulus index) for non-random sequences
                //
                question_count--;
                question_Index++;
                logQuestionIndex++;
                attempt_count = BP_CONST.MAX_ATTEMPT;

                Log.d("BPOP", "question Count: " + question_count);
                Log.d("BPOP", "attempt  Count: " + attempt_count);
            } else {
                CErrorManager.logEvent(TAG,  "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }


    public boolean dataExhausted() {
        return (question_count <= 0)? true:false;
    }


    protected void updateDataSet(CBp_Data data) {

        _currData = data;

        if(_mechanics  != null) {
            _mechanics.onDestroy();
            _mechanics = null;
        }
        switch(data.question_type.toLowerCase()) {

            case "mc":
            case "multiplechoice":
            case "multiple-choice":
                _mechanics = new CBp_Mechanic_MC(mContext, this, problem_type);
                break;

            case "rise":
            case "rising":
                _mechanics = new CBp_Mechanic_RISE(mContext, this, problem_type);
                break;
        }

        _mechanics.populateView(_currData);

        requestLayout();
    }


    protected void selectQuestion(CBp_Data data) {


        //***** Select the question from the stimulus set
        //
        // Get the number of questions available.
        //
        int questionCount = wrk_stimulusSet.size();

        // If we are using sequential presentations then we substitute the
        // current correct index in the "correct" i.e. stimulus bubble. To
        // ensure there is at least one correct answer.
        //
        if(question_sequence.toUpperCase().equals(BP_CONST.SEQUENTIAL)) {

            // cycle on the wrk_stimulusSet
            //
           question_Index %= questionCount;
        }
        else {

           question_Index = (int) (Math.random() * questionCount);
        }

        data.stimulus        = wrk_stimulusSet.get(question_Index);
        data.stimulus_type   = wrk_stimTypeSet.get(question_Index);
        if(wrk_stimulus_script != null)
            data.stimulus_script = wrk_stimulus_script.get(question_Index);

        data.answer        = wrk_answerSet.get(question_Index);
        data.answer_type   = wrk_answerTypeSet.get(question_Index);
        if(wrk_answer_script != null)
            data.answer_script = wrk_answer_script.get(question_Index);

        // If not using replacement on random selection - i.e. if questions may NOT be repeated
        // then remove the question/answer entry
        //
        if(!question_replacement && !question_sequence.toUpperCase().equals(BP_CONST.SEQUENTIAL)) {

            wrk_stimulusSet.remove(question_Index);
            wrk_stimTypeSet.remove(question_Index);
            if(wrk_stimulus_script != null)
                wrk_stimulus_script.remove(question_Index);

            wrk_answerSet.remove(question_Index);
            wrk_answerTypeSet.remove(question_Index);
            if(wrk_answer_script != null)
                wrk_answer_script.remove(question_Index);
        }
    }


    /**
     *
     */
    protected void preProcessQuestion() {

        // For each question we repopulate these arraylists
        // populate the working ArrayList so we can easily delete elements on demand
        //
        wrk_responseSet     = new ArrayList<String>(Arrays.asList(gen_responseSet));
        wrk_respTypeSet     = new ArrayList<String>(Arrays.asList(gen_respTypeSet));

        if(gen_response_script != null)
            wrk_response_script = new ArrayList<String[]>(Arrays.asList(gen_response_script));
    }


    protected void selectRandResponse(CBp_Data data, int count, int ansIndex) {

        // First find the actual answer in the response set and trim it out
        // to avoid duplicate answer items in the respsonse set
        //
        String question = data.answer;
        String questype = data.answer_type;

        for(int i1 = 0 ; i1 < wrk_responseSet.size() ; i1++) {

            if(wrk_responseSet.get(i1).equals(question) &&
               wrk_respTypeSet.get(i1).equals(questype)) {

                wrk_responseSet.remove(i1);
                wrk_respTypeSet.remove(i1);
                if(wrk_response_script != null)
                    wrk_response_script.remove(i1);
                break;
            }
        }

        // Reset the presenation / response set
        //
        data.response_set     = new String[count];
        data.responsetype_set = new String[count];
        if(data.response_script != null)
            data.response_script  = new String[count][];

        // Build a presentation set from the responseset samples
        //

        for (int i1 = 0; i1 < count ; i1++) {

            // Place the answer at the requested location in the presentation array
            //
            if(i1 == ansIndex) {

                data.response_set[ansIndex]     = data.stimulus;
                data.responsetype_set[ansIndex] = data.stimulus_type;
                data.responsetype_set[ansIndex] = wrk_respTypeSet.get(0);
                if(data.response_script != null)
                    data.response_script[ansIndex]  = data.stimulus_script;
            }

            // Otherwise place a random choice from the responseSet with or without
            // replacement
            //
            else {

                int randIndex = (int) (Math.random() * wrk_responseSet.size());

                data.response_set[i1] = wrk_responseSet.get(randIndex);
                data.responsetype_set[i1] = wrk_respTypeSet.get(randIndex);
                if (wrk_response_script != null)
                    data.response_script[i1] = wrk_response_script.get(randIndex);

                // If not using replacement - i.e. if responses may NOT be repeated then remove
                // the response entry
                //
                if (!response_replacement) {

                    wrk_responseSet.remove(randIndex);
                    wrk_respTypeSet.remove(randIndex);
                    if (wrk_response_script != null)
                        wrk_response_script.remove(randIndex);
                }
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if(changed || ((_mechanics != null) && !_mechanics.isInitialized())) {
            int width = r - l;
            int height = b - t;

            if(_mechanics != null)
                _mechanics.doLayout(Scontent.getWidth(), Scontent.getHeight(), _currData);
        }
    }


    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);
    }


    public IBubbleMechanic getMechanics() {
        return _mechanics;
    }

    public void UpdateValue(int value) {
    }


    protected boolean isCorrect() {
        return correct;
    }


    public boolean allCorrect(int numCorrect) {
        return (numCorrect == dataSource.length);
    }



    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}


    public void enableTouchEvents() {

        if(_mechanics != null) {
            _mechanics.enableTouchEvents();
        }
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // publish component state data - START


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    protected void publishState(CBubble bubble, CBubbleStimulus bubbleStimulus) {

    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void publishValue(String varName, String value) {
    }

    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void publishValue(String varName, int value) {
    }


    // publish component state data - EBD
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _command;
        protected Object _target;

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, Object target) {
            _command = command;
            _target  = target;
        }

        public String getCommand() {
            return _command;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                if(_mechanics != null) {
                    _mechanics.execCommand(_command, _target);
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error: cmd:" + _command + " tar: " + _target + "  >", e, false);
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

            Log.d(TAG, "Post Cancelled on Flush: " + ((Queue)entry.getValue()).getCommand());

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
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

    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
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

        enQueue(new Queue(command, target), delay);
    }


    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************



    //***********************************************************
    // Event Listener/Dispatcher - Start


    @Override
    public boolean isGraphEventSource() {
        return false;
    }

    @Override
    public void addEventListener(String linkedView) {

    }

    @Override
    public void addEventListener(IEventListener listener) {

    }

    @Override
    public void dispatchEvent(IEvent event) {

        for (IEventListener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************




    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

        addView(view_background);
        bringChildToFront(Scontent);

        if(banner_color != null) {
//             dispatchEvent(new CEvent(TCONST.SET_BANNER_COLOR, TCONST.VALUE , banner_color));

        }
    }
}
