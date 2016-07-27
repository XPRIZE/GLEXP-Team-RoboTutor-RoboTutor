package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import org.json.JSONObject;

import cmu.xprize.ak_component.CAkQuestionBoard;
import cmu.xprize.ak_component.CAk_Component;
import cmu.xprize.ak_component.CAk_Data;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Created by jacky on 2016/7/6.
 */

public class TAkComponent extends CAk_Component implements ITutorObjectImpl, IDataSink {

    private CTutor mTutor;
    private CObjectDelegate mSceneObject;

    private boolean isCorrect;
    private CAkQuestionBoard qb;


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
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
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



    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        // We make the assumption that all are correct until proven wrong
        //
        mTutor.setAddFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);
                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataSource.startsWith("db|")) {


            } else if (dataSource.startsWith("{")) {

                loadJSON(new JSONObject(dataSource), null);

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

        int s=extraSpeed*500;

        LayoutParams params = new LayoutParams(90, 30);
        params.addRule(CENTER_HORIZONTAL);
        addView(questionBoard, params);

        final AnimatorSet questionboardAnimator = CAnimatorUtil.configZoomIn(questionBoard, 3500,
                0, new LinearInterpolator(), 4f);

        ValueAnimator questionboardTranslationAnimator = ObjectAnimator.ofFloat(questionBoard,
                "y", getHeight() * 0.25f, getHeight() * 0.70f);
        questionboardAnimator.setDuration(3500-s);
        questionboardAnimator.setInterpolator(new LinearInterpolator());

        questionboardAnimator.playTogether(questionboardTranslationAnimator);

        questionboardAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                if(judge(questionBoard)){
//                    soundPool.play(correctMedia, 1.0f, 1.0f, 1, 0, 1.0f);
//                    if(speedIsZero==false) {
//                        player.score += 1;
//                        lastCorrect = true;
//                        errornum = 0;
//                    }
//                    else
//                    {
//                        animatorStop=false;
//                        for(int i=0;i<ongoingAnimator.size();i++)
//                        {
//                            ongoingAnimator.get(i).resume();
//                        }
//                        cityAnimator.resume();
//                    }
//                    removeView(questionBoard);
//                }else{
//                    if(speedIsZero==false) {
//                        player.score -= 1;
//                        soundPool.play(incorrectMedia, 1.0f, 1.0f, 1, 0, 1.0f);
//                        lastCorrect = false;
//                        errornum += 1;
//                        if (errornum == 3)
//                            dialog();
//                        removeView(questionBoard);
//                    }
//                    else{
//                        animatorStop=true;
//                        cityAnimator.pause();
//                        for(int i=0;i<ongoingAnimator.size();i++)
//                        {
//                            ongoingAnimator.get(i).pause();
//                            System.out.println(""+ongoingAnimator.get(i));
//                        }
//                        stopQuestionBoard=questionBoard;
//                        //removeView(questionBoard);
//                    }
//                }
                //ongoingAnimator.remove(questionboardAnimator);
                removeView(questionBoard);
                applyEventNode("NEXT");

            }
        });

        questionboardAnimator.start();

        if(flag && teachFinger != null) {
            teachFinger.setVisibility(INVISIBLE);
            flag = false;
        }
    }

    public void judge(){
        reset();
        if(questionBoard.answerLane == player.lane){
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
            player.score += 1;
        }else {
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
            player.score -= 1;
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
    }

    @Override
    public void postInflate() {}

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
