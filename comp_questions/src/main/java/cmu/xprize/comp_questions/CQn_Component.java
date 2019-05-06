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

package cmu.xprize.comp_questions;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.ListenerBase;
import edu.cmu.xprize.listener.ListenerPLRT;

import static cmu.xprize.util.TCONST.AUDIO_EVENT;
import static cmu.xprize.util.TCONST.TYPE_AUDIO;


/**
 *  The Reading Tutor Component
 */
public class CQn_Component extends ViewAnimator implements IEventListener, IVManListener, IAsrEventListener, ILoadableObject, IPublisher {

    private Context                 mContext;

    protected ListenerBase          mListener;
    protected TTSsynthesizer        mSynthesizer;

    protected ICQn_ViewManager mViewManager;                                   // Created in TQn_Component sub-class in the tutor domain
    protected String                mDataSource;

    private ArrayList<String>       sentences              = null;                  //list of sentences of the given passage
    protected String                currentSentence;                                //currently displayed sentence that need to be recognized
    private HashMap<String, String> suggestions            = null;
    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";

    // state for the current sentence
    protected int                   currentIndex          = 0;                      // current sentence index in storyName, -1 if unset
    private int                     currIntervention      = TCONST.NOINTERVENTION;  //
    private int                     completeSentenceIndex = 0;
    protected String[]              sentenceWords;                                  // current sentence words to hear
    protected int                   expectedWordIndex     = 0;                      // index of expected next word in sentence
    private static int[]            creditLevel           = null;                   // per-word credit levelFolder according to current hyp
    protected String                spokenWord;                                     // current spoken word
    protected int                   attemptCount          = 0;                      // number of attempts

    protected String                DATASOURCEPATH;
    protected String                STORYSOURCEPATH;
    protected String                AUDIOSOURCEPATH;
    protected String                SHAREDPATH;

    private final Handler           mainHandler = new Handler(Looper.getMainLooper());
    private HashMap                 queueMap    = new HashMap();
    private boolean                 _qDisabled  = false;

    protected boolean               _scrollVertical = false;


    private Animation slide_left_to_right;
    private Animation slide_right_to_left;
    private Animation slide_bottom_up;
    private Animation slide_top_down;
    private Animation fade_out;


    // json loadable
    //
    public CData_Index[]      dataSource;



    // This is used to map "type" (class names) in the index to real classes
    //
    static public HashMap<String, Class> viewClassMap = new HashMap<String, Class>();

    static {
        viewClassMap.put("ASB_Data", CQn_ViewManagerASB.class);
        viewClassMap.put("MARi_Data", CQn_ViewManagerMari.class);
    }

    static final String TAG = "CQn_Component";


    public CQn_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CQn_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        Log.d(TAG, "init: "+context.getPackageName());
        inflate(getContext(), R.layout.qn_component, this);

        mContext = context;

        slide_left_to_right  = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_to_right);
        slide_right_to_left  = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_to_left);
        slide_top_down       = AnimationUtils.loadAnimation(mContext, R.anim.slide_top_down);
        slide_bottom_up      = AnimationUtils.loadAnimation(mContext, R.anim.slide_bottom_up);
        fade_out              = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
    }


    public void onDestroy() {

        terminateQueue();

        if (mListener != null) {
            mListener.stop();
            mListener = null;
        }

        if (mViewManager != null) {
            mViewManager.onDestroy();
            mViewManager = null;
        }

        //mSynthesizer.shutDown();
    }


    protected void prepareListener(TTSsynthesizer rootTTS) {
        // Generate a Project Listen type listener
        // Attach the speech recognizer.
        mListener = new ListenerPLRT();
//        mListener = new ListenerJSGF();
        mListener.setEventListener(this);

        // Have connector sub-class in the tutor domain Inject the listener into the MediaManager
        //
        setListener(mListener);

        // attach TTS
        mSynthesizer = rootTTS;
    }


    public void nextScene() {
    }

    public void nextNode() {
    }


    public void decideToPlayGenericQuestion() {

    }
    
    public void setRandomGenericQuestion() {
        mViewManager.setRandomGenericQuestion();
    }

    public void setClozeQuestion() {
        mViewManager.setClozeQuestion();
    }

    public void genericQuestions() {
        mViewManager.genericQuestions();
    }

    public void displayGenericQuestion() {
        mViewManager.displayGenericQuestion();
    }

    public void displayClozeQuestion() {
        mViewManager.displayClozeQuestion();
    }

    public void setPictureMatch() {
        mViewManager.setPictureMatch();
    }

    public void setClozePage() {
        mViewManager.setClozePage();
    }

    public void displayPictureMatching() { mViewManager.displayPictureMatching(); }

    public void hasClozeDistractor() {mViewManager.displayClozeQuestion();}

    public void hasQuestion() {mViewManager.hasQuestion();}
    /**
     *
     * @param language Feature string (e.g. LANG_EN)
     */
    public void configListenerLanguage(String language) {

        // Configure the mListener for our storyName
        //
        mListener.setLanguage(language);
    }


    /**
     * @Override in Tutor Domain to allow the MediaManageer direct access to the recognizer
     */
    public void setListener(ListenerBase listener) {}


    /**
     * @Override in Tutor Domain to allow the MediaManageer direct access to the recognizer
     */
    public void removeListener(ListenerBase listener) {}



    //*************************************************
    //****** ViewManager Support - START

    /**
     * Override in TClass
     * @param feature
     * @param fadd
     */
    public void setFeature(String feature, boolean fadd) {}
    public boolean testFeature(String feature) {
        return false;
    }


    public int addPage(View newView) {
        Log.d(TAG, "addPage: ");
        int insertNdx = super.getChildCount();
        super.addView((View) newView, insertNdx);

        return insertNdx;
    }

    /**
     *
     * @param forward
     * @param index
     */
    public void animatePageFlip(boolean forward, int index) {
        if (forward) {
            if (_scrollVertical)
                setInAnimation(slide_bottom_up);
            else
                setInAnimation(slide_right_to_left);
        }
        else {
            if (_scrollVertical)
                setInAnimation(slide_top_down);
            else
                setInAnimation(slide_left_to_right);
        }
        setDisplayedChild(index);
    }

    public void fadeOutView(View v){
        v.setAnimation(fade_out);
    }

    public void updateViewColor(View v, int color){
        v.setBackgroundColor(color);
    }

    public void updateTextSize(TextView v, int size){
        v.setTextSize(size);
    }

    public void updateTextColor(TextView v, int color){
        v.setTextColor(color);
    }

    public void updateVisibility(View v, String state){
        if (state == "SHOW"){
            v.setVisibility(VISIBLE);
        }
        if (state == "HIDE"){
            v.setVisibility(INVISIBLE);
        }
    }

    public void updateViewAlpha(View v, float alpha){
        v.setAlpha(alpha);
    }

    public void updateTextviewHTML(TextView v, String html){
        v.setText(Html.fromHtml(html));
    }

    //****** ViewManager Support - START
    //*************************************************



    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START

    /**
     * Overridden in TClass to fire graph behaviors
     *
     */
    public boolean applyBehavior(String event){

        boolean result = false;
        return result;
    }

    /**
     * Overridden in TClass to fire graph behaviors
     *
     * @param nodeName
     */
    public void applyBehaviorNode(String nodeName) {
    }



    public void stopAudio(){

    }

    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************



    //*************************************************
    //****** Activity state support START

    protected void onPause() {

        // stop listening abortively whenever app pauses or stops (moves to background)
        if (mListener != null) {
            mListener.deleteLogFiles();
            mListener.cancel();
        }
    }


    protected void onStop() {
    }


    protected void onResume() {
    }


    protected void onRestart() {

        //mViewManager.switchSentence(currentIndex);
    }


    /* Following saves state over possible destroy/recreate cycle,
      * which occurs most commonly on portrait/landscape change.
 	 * We save current sentence (though no credit state) in order to restart from there
 	 */
    protected void onSaveInstanceState(Bundle state) {
//        super.onSaveInstanceState(state);
//        state.putInt("currentIndex", currentIndex);     // just save the current sentence index.
    }


    //****** Activity state support END
    //*************************************************



    //****************************************************************************
    //*********************  Speech Recognition Interface (ASR) - Start

    @Override
    public void onBeginningOfSpeech() {}


    @Override
    public void onEndOfSpeech() {}


    /**
     * Route ASR events to the appropriate ViewMannager for the content type
     *
     * @param heardWords
     * @param finalResult
     */
    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        mViewManager.onUpdate(heardWords, finalResult);             // update current sentence state and redraw
    }


    /**
     * Route ASR events to the appropriate ViewMannager for the content type
     *
     * @param heardWords
     * @param finalResult
     */
    @Override
    public void onUpdate(String[] heardWords, boolean finalResult) {

        mViewManager.onUpdate(heardWords, finalResult);             // update current sentence state and redraw
    }


    @Override
    public void onASREvent(int eventType) {

        Log.d(TAG, "onASREvent: " +  eventType);

        // Here we have to convert from bitmapped event types to string types
        //
        switch (eventType) {

            case TCONST.RECOGNITION_EVENT:
                applyBehavior(TCONST.ASR_RECOGNITION_EVENT);
                break;

            case TCONST.ERROR_EVENT:
                applyBehavior(TCONST.ASR_ERROR_EVENT);
                break;

            case TCONST.SILENCE_EVENT:
                applyBehavior(TCONST.ASR_SILENCE_EVENT);
                break;

            case TCONST.SOUND_EVENT:
                applyBehavior(TCONST.ASR_SOUND_EVENT);
                break;

            case TCONST.WORD_EVENT:
                applyBehavior(TCONST.ASR_WORD_EVENT);
                break;

            case TCONST.TIMEDSILENCE_EVENT:
                applyBehavior(TCONST.ASR_TIMEDSILENCE_EVENT);
                break;

            case TCONST.TIMEDSOUND_EVENT:
                applyBehavior(TCONST.ASR_TIMEDSOUND_EVENT);
                break;

            case TCONST.TIMEDWORD_EVENT:
                applyBehavior(TCONST.ASR_TIMEDWORD_EVENT);
                break;
        }
    }


    //*********************  Speech Recognition Interface - End
    //****************************************************************************



    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start


    public void startStory() {
        mViewManager.startStory();
    }

    public void speakTargetSentence() {   // to speak the entire Target word sentence

    }


    /**
     */
    public void speakTargetWord() {

    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void onButtonClick(String buttonName) {
    }



    //************************************************************************
    //************************************************************************
    // IPublisher - START

    @Override
    public void publishState() {

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

    @Override
    public void publishFeatureSet(String featureset) {

    }

    @Override
    public void retractFeatureSet(String featureset) {

    }

    @Override
    public void publishFeature(String feature) {

    }

    @Override
    public void retractFeature(String feature) {

    }

    @Override
    public void publishFeatureMap(HashMap featureMap) {

    }

    @Override
    public void retractFeatureMap(HashMap featureMap) {

    }

    // IPublisher - END
    //************************************************************************
    //************************************************************************


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void updateContext(String sentence, int index, String[] wordList, int wordIndex, String word, int attempts, boolean virtual, boolean correct) {
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public void UpdateValue(boolean correct) {
    }


    public void setSpeakButton(String command) {

        mViewManager.setSpeakButton(command);
    }

    public void setPageFlipButton(String command) {

        mViewManager.setPageFlipButton(command);
    }

    public void enableImageButtons(){
        mViewManager.enableImageButtons();
    }

    public void disableImageButtons(){
        mViewManager.disableImageButtons();
    }

    public void resetImageButtons(){
        mViewManager.resetImageButtons();
    }

    public void showImageButtons(){
        mViewManager.showImageButtons();
    }

    public void hideImageButtons(){
        mViewManager.hideImageButtons();
    }

    public void enableClozeButtons(){
        mViewManager.enableClozeButtons();
    }

    public void disableClozeButtons(){
        mViewManager.disableClozeButtons();
    }

    public void resetClozeButtons(){
        mViewManager.resetClozeButtons();
    }

    public void showClozeButtons(){
        mViewManager.showClozeButtons();
    }

    public void hideClozeButtons(){
        mViewManager.hideClozeButtons();
    }

    public void showClozeWordInBlank() { mViewManager.showClozeWordInBlank(); }

    public void hideClozeWordInBlank() {mViewManager.hideClozeWordInBlank(); }

    public void publishClozeWord() { mViewManager.publishClozeWord(); }

    public void highlightClozeWord() { mViewManager.highlightClozeWord(); }

    public void undoHighlightClozeWord() { mViewManager.undoHighlightClozeWord(); }

    public void playClozeSentence() { mViewManager.playClozeSentence(); }
    // Tutor methods  End
    //************************************************************************
    //************************************************************************



    protected boolean isCorrect() {

        boolean correct = false;

        return correct;
    }


    /**
     * TODO: this currently only supports extern assets - need to allow for internal assets
     *
     * @param EXTERNPATH
     */
    public void loadStory(String EXTERNPATH, String viewType, String assetLocation) {

        loadStory(EXTERNPATH, viewType, assetLocation, null);
    }


    /**
     * sometimes the storydata.json file is in one repo (assetLocation), but the other assets needed (images)
     * are in a shared location (sharedAssetLocation)
     *
     * @param EXTERNPATH
     * @param viewType
     * @param assetLocation
     * @param SHAREDEXTERNPATH
     */
    public void loadStory(String EXTERNPATH, String viewType, String assetLocation, String SHAREDEXTERNPATH) {

        Log.d(TCONST.DEBUG_STORY_TAG, String.format("assetLocation=%s -- EXTERNPATH=%s", assetLocation, EXTERNPATH));
        Class<?> storyClass = viewClassMap.get(viewType);
        try {
            // Generate the View manager for the storyName - specified in the data
            //
            // ooooh maybe check if it's math and make text closer to image
            mViewManager = (ICQn_ViewManager) storyClass.getConstructor(CQn_Component.class, ListenerBase.class).newInstance(this, mListener);

            Log.d(TAG, "loadStory: loaded mviewmanager");
            Log.d(TAG, "loadStory: "+TCONST.STORYDATA);
            // ZZZ it loads the story data JUST FINE
            String jsonData = JSON_Helper.cacheDataByName(EXTERNPATH + TCONST.STORYDATA);
            Log.d(TCONST.DEBUG_STORY_TAG, "logging jsonData:");
            mViewManager.loadJSON(new JSONObject(jsonData), null);

            // UHQ load the relevant mcq json data for cloze questions
            // FOR_HUI: should not load if story.pic.hear or story.gen.hear
            String jsonMcq = JSON_Helper.cacheDataByName(EXTERNPATH + TCONST.STORYMCQ);
            Log.d(TCONST.DEBUG_STORY_TAG, "logging jsonMcq:");
            mViewManager.loadJSON(new JSONObject(jsonMcq), null);

        } catch (Exception e) {
            // TODO: Manage Exceptions
            CErrorManager.logEvent(TAG, "Story Parse Error: ", e, false);
            if (e.getCause()!=null) {
                Log.d(TAG, "loadStory: "+e.getCause());
            }
        }

        if (assetLocation.equals(TCONST.EXTERN_SHARED)) {
            Log.d(TCONST.DEBUG_STORY_TAG, "SHARED!");
            // we are done using sharedAssetLocation
            EXTERNPATH = SHAREDEXTERNPATH;
        }
        //
        // ZZZ what are these values?
        // ZZZ EXTERNPATH = TCONST.EXTERN
        // ZZZ assetLocation contains storydata.json and images
        mViewManager.initStory(this, EXTERNPATH, assetLocation);

    }


    /**
     * TODO: this currently only supports extern assets - need to allow for internal assets
     *
     * @param storyName
     */
    public void setStory(String storyName, String assetLocation) {

        for (int i1 = 0 ; i1 < dataSource.length ; i1++ ) {

            if(storyName.equals(dataSource[i1].storyName)) {

                // Generate a cached path to the storyName asset data
                //
                String EXTERNPATH =DATASOURCEPATH + dataSource[i1].levelFolder + "/" + dataSource[i1].storyFolder + "/";

                loadStory(EXTERNPATH, dataSource[i1].viewtype, assetLocation);

                // we're done
                break;
            }
        }
    }


    public void next() {

        try {
            if (mViewManager != null) {

                mViewManager.nextWord();

            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: next called past end of data", e, false);
        }

    }


    public boolean dataExhausted() {
        return mViewManager.endOfData();
    }



    //************************************************************************
    //************************************************************************
    // IEventListener  -- Start


    @Override
    public void onEvent(IEvent eventObject) {


        // We expect AUDIO_EVENTS from the narration type_audio nodes to let us know when
        // they are complete with an UTTERANCE_COMPLETE_EVENT
        //
        if (mViewManager != null) {
            try {
                switch (eventObject.getType()) {

                    case TYPE_AUDIO:

                        // We expect AUDIO_EVENTS from the narration type_audio nodes to let us know when
                        // they are complete with an UTTERANCE_COMPLETE_EVENT
                        //
                        mViewManager.execCommand((String) eventObject.getString(AUDIO_EVENT), null);
                        break;

                    default:
                        break;
                }
            } catch (Exception ex) {
                Log.e(TAG, "ERROR:node.queuedgraph,action:onevent");
            }
        }
    }

    // Override...
    public void logClozePerformance(boolean correct, String expected, String studentChoice, String[] options, int page) {

    }

    // Override...
    public void logPicMatchPerformance(boolean correct, int expected, int studentChoice, int page) {

    }

    // IEventListener  -- End
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

                if (mViewManager != null) {
                    mViewManager.execCommand(_command, _target);
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error: cmd:" + _command + " tar: " + _target + "  >", e, true);
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

        while (tObjects.hasNext() ) {
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
        enQueue(qCommand, 0L);
    }
    private void enQueue(Queue qCommand, Long delay) {

        if (!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if (delay > 0L) {
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
        post(command, 0L);
    }
    public void post(String command, Long delay) {

        enQueue(new Queue(command), delay);
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, Object target) {
        post(command, target, 0L);
    }
    public void post(String command, Object target, Long delay) {

        enQueue(new Queue(command, target), delay);
    }


    // Component Message Queue  -- End
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

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
    }
}
