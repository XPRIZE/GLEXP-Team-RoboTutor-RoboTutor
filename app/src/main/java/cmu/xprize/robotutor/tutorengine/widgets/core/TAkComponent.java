package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.json.JSONObject;

import cmu.xprize.ak_component.CAkPlayer;
import cmu.xprize.ak_component.CAkQuestionBoard;
import cmu.xprize.ak_component.CAk_Component;
import cmu.xprize.ak_component.CAk_Data;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.sb_component.CSb_Scoreboard;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Created by jacky on 2016/7/6.
 */

public class TAkComponent extends CAk_Component implements ITutorObjectImpl, IDataSink {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;
    private CMediaManager   mMediaManager;

    private int         wrongTimes = 0;//record the successive wrong times of the player
    private boolean     first1sign = true;
    private boolean     first2sign = true;
    private boolean     first3sign = true;

    private PercentRelativeLayout curpercentLayout = (PercentRelativeLayout) getChildAt(0);

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
        mTutor.getScope().addUpdateVar(name() + ".value", new TInteger(value));

        boolean correct = true;

        reset();

        if(correct)
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        else
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
    }


    private void reset() {

        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
        mTutor.setDelFeature(TCONST.PROMPT_1LEFT);
        mTutor.setDelFeature(TCONST.PROMPT_1MID);
        mTutor.setDelFeature(TCONST.PROMPT_1RIGHT);
        mTutor.setDelFeature(TCONST.PROMPT_2LEFT);
        mTutor.setDelFeature(TCONST.PROMPT_2MID);
        mTutor.setDelFeature(TCONST.PROMPT_2RIGHT);
        mTutor.setDelFeature(TCONST.PROMPT_3);
        mTutor.setDelFeature(TCONST.PROMPT_3V);
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

                for (int i1 = 0; i1 < TCONST.MAX_AKDATA; i1++) {

                    int randomKey = (int) (Math.random() * setSize);

                    Log.d(TAG, "Randomization: " + i1 + " -for- " + (i1+randomKey));

                    temp = datasource[i1];
                    datasource[i1] = datasource[i1+randomKey];
                    datasource[randomKey] = temp;

                    setSize--;
                }
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
        mTutor.setAddFeature(TCONST.ALL_CORRECT);

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

            mTutor.setDelFeature(TCONST.ALL_CORRECT);
        }

        reset();

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOD);

        super.next();
    }

    @Override
    public void playAudio(CAk_Data data){

        String answerString = "";

        TScope scope = mTutor.getScope();

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

        Log.d("PlayAudio", answerString);

        scope.addUpdateVar("TestAudio", new TString(answerString));
        applyEventNode("PLAY_AUDIO");
    }

    public void instructAudio(String instruction){
        TScope scope = mTutor.getScope();
        Log.d("InstructAudio", instruction);
        scope.addUpdateVar("audio", new TString(instruction));
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
                obj.applyNode();

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
                applyEventNode("NEXT");
                ongoingAnimator.remove(finishLineAnimator);
            }
        });

        finishLineAnimator.start();
    }

    public void judge(){
        reset();
        switch(questionBoard.choices.length){
            case 1:
                if(first1sign) {
                    mTutor.setAddFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first1sign = false;
                }else
                    judge_rightwrong();
                break;
            case 2:
                if(first2sign){
                    mTutor.setAddFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first2sign = false;
                }else
                    judge_rightwrong();
                break;
            case 3:
                if(first3sign){
                    mTutor.setAddFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                    first3sign = false;
                }else
                    judge_rightwrong();
                break;
        }
    }

    public void judge_rightwrong(){
        if(questionBoard.answerLane == player.lane){
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
            wrongTimes = 0;
        }else {
            wrongTimes++;
            if(wrongTimes != 2) {
                mTutor.setAddFeature(TCONST.GENERIC_WRONG);
            }else{
                mTutor.setAddFeature(TCONST.GENERIC_SUCCESSIVEWRONG);
                wrongTimes = 0;
            }
        }
    }

    public void judge_instruct(){//judge the prompt type
        reset();
        switch(questionBoard.choices.length){
            case 1:
                if(questionBoard.answerLane == CAkPlayer.Lane.LEFT){
                    mTutor.setAddFeature(TCONST.PROMPT_1LEFT);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.MID){
                    mTutor.setAddFeature(TCONST.PROMPT_1MID);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.RIGHT){
                    mTutor.setAddFeature(TCONST.PROMPT_1RIGHT);
                }
                break;
            case 2:
                if(questionBoard.answerLane == CAkPlayer.Lane.LEFT){
                    mTutor.setAddFeature(TCONST.PROMPT_2LEFT);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.MID){
                    mTutor.setAddFeature(TCONST.PROMPT_2MID);
                }else if(questionBoard.answerLane == CAkPlayer.Lane.RIGHT){
                    mTutor.setAddFeature(TCONST.PROMPT_2RIGHT);
                }
                break;
            case 3:
                if(datasource[_dataIndex - 1].belowString.equals("audio")){//if it is an audio question
                    //TScope scope = mTutor.getScope();
                    //scope.addUpdateVar("TestAudio", new TString(getAboveString(datasource[_dataIndex - 1])));
                    mTutor.setAddFeature(TCONST.PROMPT_3V);
                }else{
                    mTutor.setAddFeature(TCONST.PROMPT_3);
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
        if(extraSpeed != 0) {
            mask.setVisibility(VISIBLE);
            Animator animator = scoreboard.reward(player.getX() + player.carImage.getX(),
                    player.getY(), "+" + extraSpeed);
            LayoutParams params =  (LayoutParams) getLayoutParams();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreboard.removeTextView();
                    scoreboard.increase(extraSpeed);
                    player.score += extraSpeed;
                    new AnimateScoreboard().execute(scoreboard);
                }
            });
            animator.start();
        }
        else {
            scoreboard.increase(extraSpeed);
            new AnimateScoreboard().execute(scoreboard);
        }
    }

    public void decreaseScore() {
        if(extraSpeed != 0) {
            mask.setVisibility(VISIBLE);
            Animator animator = scoreboard.reward(player.getX() + player.carImage.getX(),
                    player.getY(), "-" + extraSpeed);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    scoreboard.removeTextView();
                    if (player.score <= extraSpeed) {
                        scoreboard.decrease(player.score);
                        player.score = 0;
                    } else {
                        scoreboard.decrease(extraSpeed);
                        player.score -= extraSpeed;
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
        mMediaManager = CMediaController.getManagerInstance(mTutor);
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


    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {

    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition) {

    }

    @Override
    public void setAlpha(Float alpha) {

    }

}
