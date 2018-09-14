package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cmu.xprize.ak_component.AKCONST;
import cmu.xprize.ak_component.CAkPlayer;
import cmu.xprize.ak_component.CAkQuestionBoard;
import cmu.xprize.ak_component.CAk_Component;
import cmu.xprize.ak_component.CAk_Data;
import cmu.xprize.bp_component.CClassMap;
import cmu.xprize.comp_logging.ITutorLogger;
import cmu.xprize.comp_logging.PerformanceLogItem;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.sb_component.CSb_Scoreboard;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEventSource;
import cmu.xprize.util.IPublisher;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.TUTOR_STATE_MSG;

/**
 * Created by jacky on 2016/7/6.
 */

public class TAkComponent extends CAk_Component implements ITutorObject, IDataSink, IPublisher, ITutorLogger,IBehaviorManager, IEventSource {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager   mMediaManager;

    private int         wrongTimes = 0;//record the successive wrong times of the player
    private boolean     first1sign = true;
    private boolean     first2sign = true;
    private boolean     first3sign = true;

    private PercentRelativeLayout curpercentLayout = (PercentRelativeLayout) getChildAt(0);
    private HashMap<String, String> stickyMap   = new HashMap<>();
    private HashMap<String, String> volatileMap = new HashMap<>();

    private HashMap<String,String>  _StringVar  = new HashMap<>();
    private HashMap<String,Integer> _IntegerVar = new HashMap<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    static final String TAG = "TAkComponent";



    public TAkComponent(Context context) {
        super(context);
    }

    public TAkComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAkComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

    }


//    **********************************************************
//    **********************************************************
//    *****************  Tutor Interface


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    @Override
    public void UpdateValue(int value) {

        // update the Scope response variable  "<varname>.value"
        //
        publishValue(AKCONST.VAR_VALUE, value);

        boolean correct = true;

        reset();

        if(correct)
            publishFeature(TCONST.GENERIC_RIGHT);
        else
            publishFeature(TCONST.GENERIC_WRONG);
    }


    private void reset() {

        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
        retractFeature(TCONST.GENERIC_WRONG);
        retractFeature(TCONST.PROMPT_1LEFT);
        retractFeature(TCONST.PROMPT_1MID);
        retractFeature(TCONST.PROMPT_1RIGHT);
        retractFeature(TCONST.PROMPT_2LEFT);
        retractFeature(TCONST.PROMPT_2MID);
        retractFeature(TCONST.PROMPT_2RIGHT);
        retractFeature(TCONST.PROMPT_3);
        retractFeature(TCONST.PROMPT_3V);
    }


    /**
     * Preprocess the data set
     *
     * @param data
     */
    @Override
    protected void updateDataSet(CAk_Data data) {

        // Let the compoenent process the new data set
        //
        super.updateDataSet(data);
    }



    public void generateRandomData() {

        int         dataCount = datasource.length;
        int         setSize   = dataCount;
        CAk_Data    temp;


        try {
            // Constrain the presentation set size to TCONST.MAX_AKDATA and
            // randomize it from the entire population
            //
            if (dataCount > TCONST.MAX_AKDATA) {

                // set how many total questions we have
                mTutor.setTotalQuestions(TCONST.MAX_AKDATA);

                for (int i1 = 0; i1 < TCONST.MAX_AKDATA; i1++) {

                    int randomKey = (int) (Math.random() * setSize);

                    Log.d(TAG, "Randomization: " + i1 + " -for- " + (i1+randomKey));

                    temp = datasource[i1];
                    datasource[i1] = datasource[i1+randomKey];
                    datasource[randomKey] = temp;

                    setSize--;
                }
            } else {
                // set how many total questions we have
                mTutor.setTotalQuestions(dataCount);
            }
        }
        catch (Exception e) {
            Log.d(TAG, "Randomization fault: " );
        }
    }


    /**
     *
     * @param dataNameDescriptor
     */
    public void setDataSource(String dataNameDescriptor) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        // We make the assumption that all are correct until proven wrong
        //
        publishFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        try {
            if (dataNameDescriptor.startsWith(TCONST.LOCAL_FILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.LOCAL_FILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.DOWNLOAD_RT_TUTOR + "/" + mTutor.getTutorName() + "/";
                dataPath +=  mMediaManager.getLanguageIANA_2(mTutor) + "/";

                String jsonData = JSON_Helper.cacheDataByName(dataPath + dataFile);
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataNameDescriptor.startsWith(TCONST.DEBUG_FILE_PREFIX)) { // this must be reproduced in every robo_debuggable component

                String dataFile = dataNameDescriptor.substring(TCONST.DEBUG_FILE_PREFIX.length());

                String dataPath = TCONST.DEBUG_RT_PATH + "/";
                String jsonData = JSON_Helper.cacheDataByName(dataPath + dataFile);
                loadJSON(new JSONObject(jsonData), mTutor.getScope());


            } else if (dataNameDescriptor.startsWith(TCONST.SOURCEFILE)) {

                String dataFile = dataNameDescriptor.substring(TCONST.SOURCEFILE.length());

                // Generate a langauage specific path to the data source -
                // i.e. tutors/word_copy/assets/data/<iana2_language_id>/
                // e.g. tutors/word_copy/assets/data/sw/
                //
                String dataPath = TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS;
                dataPath += "/" +  TCONST.DATA_PATH + "/" + mMediaManager.getLanguageIANA_2(mTutor) + "/";

                String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

                // Load the datasource in the component module - i.e. the superclass
                //
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

                // If the dataset has more than TCONST.MAX_AKDATA points - randomize the presenation set
                //
                generateRandomData();

            } else if (dataNameDescriptor.startsWith("db|")) {


            } else if (dataNameDescriptor.startsWith("{")) {

                loadJSON(new JSONObject(dataNameDescriptor), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), e, false);
        }
    }


    public void next() {

        // If wrong reset ALLCORRECT
        //
        if(mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {

            retractFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.next();

        if(dataExhausted())
            publishFeature(TCONST.FTR_EOD);
    }

    public void same() {

        // If wrong reset ALLCORRECT
        //
        if(mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {

            retractFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.same();

        if(dataExhaustedForSame())
            publishFeature(TCONST.FTR_EOD);
    }


    //Helper function that converts 3 digit number to list of digits
    private int[] getListDigits(int num) {
        int hundredsDigit = 0;  int tensDigit = 0;
        if(num >= 100) {
            hundredsDigit = (num / 100) * 100;
        }
        num = num % 100;
        return (new int[]{hundredsDigit, num});
    }

    @Override
    public void playAudio(CAk_Data data){
        TScope scope = mTutor.getScope();
        String answerString = "";


        switch(data.answer)  {
            case TCONST.LEFTLANE:
                answerString = data.choices[0];
                break;

            case TCONST.CENTERLANE:
                answerString = data.choices[1];
                break;

            case TCONST.RIGHTLANE:
                if(data.choices.length > 2)
                    answerString = data.choices[2];
                else
                    answerString = data.choices[1];
                break;

        }

        if(answerString != null && answerString.matches("[-+]?\\d*\\.?\\d+")) {
            removeFeature(AKCONST.FTR_TEST_AUDIO);

            int currentNumber = Integer.parseInt(answerString);
            if (currentNumber<=100||currentNumber%100==0){
                scope.addUpdateVar("CurrentCount", new TString(String.valueOf(currentNumber)));
                postEvent(AKCONST.PLAY_CHIME);
            } else {
                scope.addUpdateVar("CurrentCountt", new TString(String.valueOf((int)(currentNumber-currentNumber%100))));
                scope.addUpdateVar("CurrentCount",new TString(String.valueOf((int)(currentNumber%100))));
                postEvent(AKCONST.PLAY_CHIME_PLUS);

            }





        }


        else {
            publishFeature(AKCONST.FTR_TEST_AUDIO);
            publishValue(AKCONST.TESTAUDIO, answerString);
            applyEventNode("PLAY_WORD_AUDIO");
        }


    }

    public void instructAudio(String instruction){

        TScope scope = mTutor.getScope();
        Log.d("InstructAudio", instruction);

        publishValue(AKCONST.VAR_AUDIO, instruction);

        applyEventNode("PAUSE");
        applyEventNode("INSTRUCT_AUDIO");
        applyEventNode("RESUME");
    }

    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }

    public void applyEventNode(String nodeName) {
        IScriptable2 obj = null;

        if(nodeName != null && !nodeName.equals("")) {
            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if(obj.testFeatures()) {
                    obj.applyNode();
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }


    public void postQuestionBoard() {
        final CAkQuestionBoard questionBoard = this.questionBoard; //new CAkQuestionBoard(mContext);
        final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

        int s = extraSpeed * 400;

        LayoutParams params = new LayoutParams(360, 80);
        params.addRule(CENTER_HORIZONTAL);
        percentLayout.addView(questionBoard, params);
        player.bringToFront();
        scoreboard.bringToFront();
        questionBoard.bringToFront();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        AnimatorSet tmp;
        if(size.x > 1400)
            tmp = CAnimatorUtil.configZoomIn(questionBoard, 3500,
                    0, new LinearInterpolator(), 4f);
        else
            tmp = CAnimatorUtil.configZoomIn(questionBoard, 3500,
                    0, new LinearInterpolator(), 2f);

        final AnimatorSet questionboardAnimator = tmp;
        ongoingAnimator.add(questionboardAnimator);
        ValueAnimator questionboardTranslationAnimator = ObjectAnimator.ofFloat(questionBoard,
                "y", getHeight() * 0.25f, getHeight() * 0.65f);
        questionboardAnimator.setDuration(5000-s);
        questionboardAnimator.setInterpolator(new LinearInterpolator());

        questionboardAnimator.playTogether(questionboardTranslationAnimator);

        questionboardAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                questionBoard_exist = false;
                super.onAnimationEnd(animation);
                percentLayout.removeView(questionBoard);
                applyEventNode("NEXT");
                ongoingAnimator.remove(questionboardAnimator);
            }
        });

        questionboardAnimator.start();

        if(flag && teachFinger != null) {
            teachFinger.setVisibility(INVISIBLE);
            flag = false;
        }
    }

    public void postFinishLine() {
        int s = extraSpeed * 400;
        final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

        final ImageView finishLine = new ImageView(mContext);
        LayoutParams params = new LayoutParams(getWidth()/3, getHeight()/10);
        params.addRule(CENTER_HORIZONTAL);
        finishLine.setImageResource(cmu.xprize.ak_component.R.drawable.finishline);

        percentLayout.addView(finishLine, params);
        player.bringToFront();
        scoreboard.bringToFront();

        final AnimatorSet finishLineAnimator = CAnimatorUtil.configZoomIn(finishLine, 3500,
                0, new LinearInterpolator(), 4f);

        ongoingAnimator.add(finishLineAnimator);

        ValueAnimator finishLineTranslationAnimator = ObjectAnimator.ofFloat(finishLine,
                "y", 0.25f * getHeight(), 0.9f * getHeight());
        finishLineAnimator.setDuration(5000-s);
        finishLineAnimator.setInterpolator(new LinearInterpolator());

        finishLineAnimator.playTogether(finishLineTranslationAnimator);

        finishLineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ongoingAnimator.remove(finishLineAnimator);
                postSplashScreen();
            }
        });

        finishLineAnimator.start();
    }

    public void postSplashScreen() {
        final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

        final ImageView splashScreen = new ImageView(mContext);
        splashScreen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LayoutParams params = new LayoutParams(getWidth(), getHeight());
        params.addRule(CENTER_HORIZONTAL);
        splashScreen.setImageResource(cmu.xprize.ak_component.R.drawable.splash);

        percentLayout.addView(splashScreen, params);

        splashScreen.bringToFront();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                applyEventNode("NEXT");
            }
        }, 1000); //Timer is in ms heree
    }

    public void judge(){
        reset();
        switch(questionBoard.choices.length){
            case 1:
                if(first1sign) {
                    publishFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first1sign = false;
                }else
                    judge_rightwrong();
                break;
            case 2:
                if(first2sign){
                    publishFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first2sign = false;
                }else
                    judge_rightwrong();
                break;
            case 3:
                if(first3sign){
                    publishFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first3sign = false;
                }else
                    judge_rightwrong();
                break;
        }
    }

    public void judge_rightwrong(){

        boolean lastResponseWasCorrect = questionBoard.answerLane == player.lane;

        if(lastResponseWasCorrect){
            publishFeature(TCONST.GENERIC_RIGHT);
            wrongTimes = 0;

        }else {
            wrongTimes++;
            if (wrongTimes == 3){
                publishFeature(TCONST.NEXTTURN);
                wrongTimes = 0;
            } else if(wrongTimes != 2) {
                retractFeature(TCONST.FTR_EOD);
                publishFeature(TCONST.GENERIC_WRONG);
            }else{
                retractFeature(TCONST.FTR_EOD);
                publishFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
            }
            /*
            if(wrongTimes != 2) {
                publishFeature(TCONST.GENERIC_WRONG);
            }else{
                publishFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                wrongTimes = 0;
            }*/

        }

        trackAndLogPerformance(lastResponseWasCorrect);

    }

    /**
     *
     * This method is to separate correctness-checking which informs game behavior from
     * tracking performance for Activity Selection and for Logging.
     *
     * @param lastResponseWasCorrect
     */
    private void trackAndLogPerformance(boolean lastResponseWasCorrect) {

        if(lastResponseWasCorrect) {
            mTutor.countCorrect();;
        } else {
            mTutor.countIncorrect();
        }

        PerformanceLogItem event = new PerformanceLogItem();

        event.setUserId(RoboTutor.STUDENT_ID);
        event.setSessionId(RoboTutor.SESSION_ID);
        event.setGameId(mTutor.getUuid().toString());  // a new tutor is generated for each game, so this will be unique
        event.setLanguage(CTutorEngine.language);
        event.setTutorName(mTutor.getTutorName());
        Log.wtf("WARRIOR_MAN", mTutor.getTutorId());
        event.setTutorId(mTutor.getTutorId());
        event.setPromotionMode(RoboTutor.getPromotionMode(event.getMatrixName()));
        event.setLevelName(level);
        event.setTaskName(task);
        event.setProblemName(generateProblemName());
        event.setTotalProblemsCount(mTutor.getTotalQuestions());
        event.setProblemNumber(_dataIndex);
        event.setSubstepNumber(1); // always 1 for Akira
        event.setAttemptNumber(1); // always 1 for Akira
        event.setExpectedAnswer(questionBoard.answerLane.toString());
        event.setUserResponse(player.lane.toString());
        event.setCorrectness(lastResponseWasCorrect ? TCONST.LOG_CORRECT : TCONST.LOG_INCORRECT);

        event.setTimestamp(System.currentTimeMillis());

        RoboTutor.perfLogManager.postPerformanceLog(event);
    }


    /**
     * Generate a name for the problem. In this case, the problem name will look something like
     * 200_100_300 or BELOW_ABOVE1_ABOVE2
     * if the below number is represented by o's (see akira_2_2.json), it will look like
     * o6_1_7
     *
     * @return
     */
    public String generateProblemName() {

        StringBuilder nameBuilder;
        try {
            nameBuilder = new StringBuilder();
            CAk_Data currentProblem = datasource[_dataIndex - 1];


            // belowString can be one of two formats: an integer, or a collection of o's that represent a quantity
            // the purpose of these conditionals is to distinguish between the two

            if(currentProblem.belowString.matches("\\d+")) {
                nameBuilder.append(currentProblem.belowString);
            } else if (currentProblem.belowString.contains("o")) {
                int quantity = countCharOccurrences(currentProblem.belowString, 'o');
                nameBuilder.append("o");
                nameBuilder.append(quantity);
            } else {
                nameBuilder.append(currentProblem.belowString);
            }

            nameBuilder.append("_");

            for (String choice : currentProblem.choices) {
                nameBuilder.append(choice);
                nameBuilder.append("_");
            }

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

        } catch(Exception e) {
            nameBuilder = new StringBuilder("error_generating_problem_name");
        }

        return nameBuilder.toString();
    }

    private int countCharOccurrences(String str, char c) {

        int count = 0;
        for (int i=0; i < str.length(); i++) {
            if(str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public void judge_instruct(){//judge the prompt type
        reset();
        switch(questionBoard.choices.length){
            case 1:
                if(questionBoard.answerLane == CAkPlayer.Lane.LEFT){
                    publishFeature(TCONST.PROMPT_1LEFT);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.MID){
                    publishFeature(TCONST.PROMPT_1MID);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.RIGHT){
                    publishFeature(TCONST.PROMPT_1RIGHT);
                }
                break;
            case 2:
                if(questionBoard.answerLane == CAkPlayer.Lane.LEFT){
                    publishFeature(TCONST.PROMPT_2LEFT);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.MID){
                    publishFeature(TCONST.PROMPT_2MID);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.RIGHT){
                    publishFeature(TCONST.PROMPT_2RIGHT);
                }
                break;
            case 3:
                if(datasource[_dataIndex - 1].belowString.equals("audio")){//if it is an audio question
                    //TScope scope = mTutor.getScope();
                    //scope.addUpdateVar("TestAudio", new TString(getAboveString(datasource[_dataIndex - 1])));
                    publishFeature(TCONST.PROMPT_3V);
                }else{
                    publishFeature(TCONST.PROMPT_3);
                }
                break;
        }
    }

    public void instruct_finger(){
        teachFinger.bringToFront();
        teachFinger.setPostion(questionBoard.answerLane);
        teachFinger.setVisibility(VISIBLE);
    }

    public void indicateCarText(){
        teachFinger.bringToFront();
        teachFinger.setPostion(player.getLane());
        teachFinger.setVisibility(VISIBLE);
    }

    public void indicate1SignText(){
        teachFinger.bringToFront();
        teachFinger.setPostion(CAkPlayer.Lane.SIGH1);
        teachFinger.setVisibility(VISIBLE);
    }

    public void indicate2SignLeft(){
        teachFinger.bringToFront();
        teachFinger.setPostion(CAkPlayer.Lane.SIGH2L);
        teachFinger.setVisibility(VISIBLE);
    }

    public void indicate2SignRight(){
        teachFinger.bringToFront();
        teachFinger.setPostion(CAkPlayer.Lane.SIGH2R);
        teachFinger.setVisibility(VISIBLE);
    }

    public void indicate3Sign(){
        teachFinger.bringToFront();
        switch(questionBoard.answerLane){
            case LEFT:
                teachFinger.setPostion(CAkPlayer.Lane.SIGH3L);
                break;
            case MID:
                teachFinger.setPostion(CAkPlayer.Lane.SIGH3M);
                break;
            case RIGHT:
                teachFinger.setPostion(CAkPlayer.Lane.SIGH3R);
                break;
        }
        teachFinger.setVisibility(VISIBLE);
    }

    public void hideFinger(){
        teachFinger.setVisibility(INVISIBLE);
    }

    public void crash() {
        player.crash();
    }

    public void pause() {
        isRunning = false;
        for(Animator animator : ongoingAnimator)
            animator.pause();
    }


    public void resume() {
        isRunning = true;
        for(Animator animator : ongoingAnimator)
            animator.resume();
    }

    public void prompt_pause(){
        isRunning = false;
        for(Animator animator : ongoingAnimator)
            animator.pause();
        LayoutParams params = new LayoutParams(360, 80);
        params.addRule(CENTER_HORIZONTAL);
        curpercentLayout.addView(questionBoard, params);
    }

    public void prompt_resume(){
        isRunning = true;
        for(Animator animator : ongoingAnimator)
            animator.resume();
        teachFinger.setVisibility(INVISIBLE);
        curpercentLayout.removeView(questionBoard);
    }


    public void increaseScore() {
        //hardcoded to 1 in codedrop 1
        if(extraSpeed != 0) {
            mask.setVisibility(VISIBLE);
            Animator animator = scoreboard.reward(player.getX() + player.carImage.getX(),
                    player.getY(), "+" + 1);
            LayoutParams params =  (LayoutParams) getLayoutParams();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreboard.removeTextView();
                    scoreboard.increase(1);
                    player.score += 1;
                    new AnimateScoreboard().execute(scoreboard);
                }
            });
            animator.start();
        }
        else {
            scoreboard.increase(1);
            new AnimateScoreboard().execute(scoreboard);
        }
    }

    public void decreaseScore() {
        if(extraSpeed != 0) {
            mask.setVisibility(VISIBLE);
            Animator animator = scoreboard.reward(player.getX() + player.carImage.getX(),
                    player.getY(), " ");
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreboard.removeTextView();
                    if (player.score <= extraSpeed) {
                        scoreboard.decrease(player.score);
                        player.score = 0;
                    } else {
                        int d = 0;
                        scoreboard.decrease(d);
                        player.score -= 0;
                    }
                    new AnimateScoreboard().execute(scoreboard);
                }
            });
            animator.start();
        }else {
            scoreboard.decrease(extraSpeed);
            new AnimateScoreboard().execute(scoreboard);
        }
    }

    private class AnimateScoreboard extends AsyncTask<CSb_Scoreboard, Integer, CSb_Scoreboard> {
        @Override
        protected CSb_Scoreboard doInBackground(CSb_Scoreboard... params) {
            CSb_Scoreboard scoreboard = params[0];
            synchronized (scoreboard.lock) {
                while(!scoreboard.isAnimating){
                    try {
                        scoreboard.lock.wait();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                scoreboard.isAnimating = false;
            }

            return scoreboard;
        }

        @Override
        protected void onPostExecute(CSb_Scoreboard scoreboard) {
            super.onPostExecute(scoreboard);
            applyEventNode("NEXT");
            mask.setVisibility(INVISIBLE);
        }


    }


    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

    @Override
    public void onDestroy() {

    }

    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {

        mTutor = tutor;
        mSceneObject.setTutor(tutor);

        // The media manager is tutor specific so we have to use the tutor to access
        // the correct instance for this component.
        //
        mMediaManager = CMediaController.getManagerInstance(mTutor.getTutorName());
    }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }


    //***********************************************************
    // ITutorLogger - Start

    private void extractHashContents(StringBuilder builder, HashMap map) {

        Iterator<?> tObjects = map.entrySet().iterator();

        while(tObjects.hasNext() ) {

            builder.append(',');

            Map.Entry entry = (Map.Entry) tObjects.next();

            String key   = entry.getKey().toString();
            String value = "#" + entry.getValue().toString();

            builder.append(key);
            builder.append(value);
        }
    }

    private void extractFeatureContents(StringBuilder builder, HashMap map) {

        StringBuilder featureset = new StringBuilder();

        Iterator<?> tObjects = map.entrySet().iterator();

        // Scan to build a list of active features
        //
        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean value = (Boolean) entry.getValue();

            if(value) {
                featureset.append(entry.getKey().toString() + ";");
            }
        }

        // If there are active features then trim the last ',' and add the
        // comma delimited list as the "$features" object.
        //
        if(featureset.length() != 0) {
            featureset.deleteCharAt(featureset.length()-1);

            builder.append(",$features#" + featureset.toString());
        }
    }

    @Override
    public void logState(String logData) {

        StringBuilder builder = new StringBuilder();

        extractHashContents(builder, _StringVar);
        extractHashContents(builder, _IntegerVar);
        extractFeatureContents(builder, _FeatureMap);

        RoboTutor.logManager.postTutorState(TUTOR_STATE_MSG, "target#akira," + logData + builder.toString());
    }
    public void setVolatileBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (volatileMap.containsKey(event)) {
                volatileMap.remove(event);
            }
        } else {
            volatileMap.put(event, behavior);
        }
    }
    public void setStickyBehavior(String event, String behavior) {

        if (behavior.toUpperCase().equals(TCONST.NULL)) {

            if (stickyMap.containsKey(event)) {
                stickyMap.remove(event);
            }
        } else {
            stickyMap.put(event, behavior);
        }
    }


    // Execute scirpt target if behavior is defined for this event
    //
    @Override
    public boolean applyBehavior(String event) {

        boolean result = false;

        if (stickyMap.containsKey(event)) {

            RoboTutor.logManager.postEvent_D(QGRAPH_MSG, "target:" + TAG + ",action:applybehavior,type:sticky,behavior:" + event);
            applyBehaviorNode(stickyMap.get(event));


            result = true;
        }

        return result;
    }


    /**
     * Apply Events in the Tutor Domain.
     *
     * @param nodeName
     */
    @Override
    public void applyBehaviorNode(String nodeName) {
        IScriptable2 obj = null;

        if (nodeName != null && !nodeName.equals("") && !nodeName.toUpperCase().equals("NULL")) {

            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if (obj != null) {
                    switch(obj.getType()) {
                        case TCONST.QUEUE:

                            if(obj.testFeatures()) {
                                obj.applyNode();

                            }
                            break;

                        default:

                            if(obj.testFeatures()) {
                                obj.preEnter();
                                obj.applyNode();
                            }
                            break;
                    }
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }
    // ITutorLogger - End
    //***********************************************************



    //************************************************************************
    //************************************************************************
    // publish component state data - START

    @Override
    public void publishState() {
    }

    @Override
    public void publishValue(String varName, String value) {

        _StringVar.put(varName,value);

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));

    }

    @Override
    public void publishValue(String varName, int value) {

        _IntegerVar.put(varName,value);

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TInteger(value));

    }

    @Override
    public void publishFeatureSet(String featureSet) {

        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(featureSet.split(","));

        for(String feature : featArray) {

            _FeatureMap.put(feature, true);
            publishFeature(feature);
        }
    }

    @Override
    public void retractFeatureSet(String featureSet) {

        // Add new features - no duplicates
        List<String> featArray = Arrays.asList(featureSet.split(","));

        for(String feature : featArray) {

            _FeatureMap.put(feature, false);
            retractFeature(feature);
        }
    }

    @Override
    public void publishFeature(String feature) {

        _FeatureMap.put(feature, true);
        mTutor.addFeature(feature);
    }

    public void removeFeature(String feature) {
        _FeatureMap.remove(feature);
        mTutor.delFeature(feature);
    }

    /**
     * Note that we may retract features before they're published to add them to the
     * FeatureSet that should be pushed/popped when using pushDataSource
     * e.g. we want EOD to track even if it has never been set
     *
     * @param feature
     */
    @Override
    public void retractFeature(String feature) {

        _FeatureMap.put(feature, false);
        mTutor.delFeature(feature);
    }

    /**
     *
     * @param featureMap
     */
    @Override
    public void publishFeatureMap(HashMap featureMap) {

        Iterator<?> tObjects = featureMap.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean active = (Boolean)entry.getValue();

            if(active) {
                String feature = (String)entry.getKey();

                mTutor.addFeature(feature);
            }
        }
    }

    /**
     *
     * @param featureMap
     */
    @Override
    public void retractFeatureMap(HashMap featureMap) {

        Iterator<?> tObjects = featureMap.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            Boolean active = (Boolean)entry.getValue();

            if(active) {
                String feature = (String)entry.getKey();

                mTutor.delFeature(feature);
            }
        }
    }

    // publish component state data - EBD
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

        super.loadJSON(jsonData, scope);
    }

    @Override
    public String getEventSourceName() {
        return name();
    }

    @Override
    public String getEventSourceType() {
        return "TAKComponent";
    }
}
