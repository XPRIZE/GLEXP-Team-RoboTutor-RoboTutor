package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cmu.xprize.asm_component.ASM_CONST;
import cmu.xprize.asm_component.CAsm_Alley;
import cmu.xprize.asm_component.CAsm_Component;
import cmu.xprize.asm_component.CAsm_Data;
import cmu.xprize.asm_component.CAsm_DotBag;
import cmu.xprize.asm_component.CAsm_MechanicSubtract;
import cmu.xprize.asm_component.CAsm_Text;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static java.lang.Thread.sleep;

public class TAsmComponent extends CAsm_Component implements ITutorObjectImpl, IDataSink {

    private CTutor           mTutor;
    private CObjectDelegate  mSceneObject;

    static final String TAG = "TAsmComponent";

    //used to store the current features about overhead
    private List<String> curFeatures = new ArrayList<String>();

    public TAsmComponent(Context context) {
        super(context);
    }

    public TAsmComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAsmComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {

        super.init(context, attrs);
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }


    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    public void evaluateWhole () {

        reset();

        boolean correct = isWholeCorrect();

        if(correct) {
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        } else
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
    }

    public void evaluateDigit () {

        reset();
        mTutor.setDelFeature(TCONST.ASM_ADD_PROMPT);
        mTutor.setDelFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
        mTutor.setDelFeature(TCONST.ASM_SUB_PROMPT);

        boolean correct = isDigitCorrect();

        if (correct) {
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
            mTutor.setAddFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);

            saveCurFeaturesAboutOverhead();
            delCurFeaturesAboutOverhead();
        } else {
            if (!mPopup.isActive && !mPopupSupplement.isActive) isWriting = false;

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDotBagsVisible(true, digitIndex);
                }
            }, 3000);

            mTutor.setAddFeature(TCONST.GENERIC_WRONG);

            if (resultCorrect == ASM_CONST.NOT_ALL_INPUT_RIGHT) {
                mTutor.setAddFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);
                resultCorrect = ASM_CONST.NO_INPUT;
            } else if (overheadCorrect == ASM_CONST.ALL_INPUT_RIGHT)
                mTutor.setAddFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
            else
                mTutor.setAddFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);
        }

    }

    public void saveCurFeaturesAboutOverhead() {
        if (mTutor.testFeature(TCONST.ASM_RA_START)) curFeatures.add(TCONST.ASM_RA_START);
        if (mTutor.testFeature(TCONST.ASM_NEXT_NUMBER)) curFeatures.add(TCONST.ASM_NEXT_NUMBER);
        if (mTutor.testFeature(TCONST.ASM_NEXT_RESULT)) curFeatures.add(TCONST.ASM_NEXT_RESULT);
        if (mTutor.testFeature(TCONST.ASM_RESULT_FIRST_TWO)) curFeatures.add(TCONST.ASM_RESULT_FIRST_TWO);
        if (mTutor.testFeature(TCONST.ASM_RESULT_NEXT_OR_LAST)) curFeatures.add(TCONST.ASM_RESULT_NEXT_OR_LAST);
    }

    public void delCurFeaturesAboutOverhead() {
        mTutor.setDelFeature(TCONST.ASM_RA_START);
        mTutor.setDelFeature(TCONST.ASM_NEXT_NUMBER);
        mTutor.setDelFeature(TCONST.ASM_NEXT_RESULT);
        mTutor.setDelFeature(TCONST.ASM_RESULT_FIRST_TWO);
        mTutor.setDelFeature(TCONST.ASM_RESULT_NEXT_OR_LAST);
    }

    public void retrieveCurFeaturesAboutOverhead() {
        for (int i = 0; i < curFeatures.size(); i++) {
            mTutor.setAddFeature(curFeatures.get(i));
        }
    }

    public void reset() {
        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);

        mTutor.setDelFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
        mTutor.setDelFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);

        mTutor.setDelFeature(TCONST.ASM_ALL_DOTS_DOWN);
    }

    public void resetAll() {
        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
        mTutor.setDelFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_CORRECT);
        mTutor.setDelFeature(TCONST.ASM_DIGIT_OR_OVERHEAD_WRONG);

        resetAllAboutAdd();
        resetAllAboutSub();
        resetAllAboutMulti();
    }

    public void resetAllAboutAdd() {
        mTutor.setDelFeature(TCONST.ASM_ADD);
        mTutor.setDelFeature(TCONST.ASM_ADD_PROMPT);
        mTutor.setDelFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
        mTutor.setDelFeature(TCONST.ASM_ALL_DOTS_DOWN);
    }

    public void resetAllAboutSub() {
        mTutor.setDelFeature(TCONST.ASM_SUBTRACT);
        mTutor.setDelFeature(TCONST.ASM_SUB_PROMPT);
    }

    public void resetAllAboutMulti() {
        mTutor.setDelFeature(TCONST.ASM_MULTI);
        mTutor.setDelFeature(TCONST.ASM_MULTI_PROMPT);
        mTutor.setDelFeature(TCONST.ASM_RA_START);
        mTutor.setDelFeature(TCONST.ASM_NEXT_NUMBER);
        mTutor.setDelFeature(TCONST.ASM_NEXT_RESULT);
        mTutor.setDelFeature(TCONST.ASM_RESULT_FIRST_TWO);
        mTutor.setDelFeature(TCONST.ASM_RESULT_NEXT_OR_LAST);
        mTutor.setDelFeature(TCONST.ASM_REPEATED_ADD_DOWN);
    }

    /**
     * Preprocess the data set
     *
     * @param data
     */
    @Override
    protected void updateDataSet(CAsm_Data data) {

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
            if (dataSource.equals(ASM_CONST.LOCAL_FILE)) {
                String jsonData = JSON_Helper.cacheDataByName(ASM_CONST.LOCAL_FILE_PATH);
                loadJSON(new JSONObject(jsonData), null);
            } else if (dataSource.startsWith(TCONST.SOURCEFILE)) {
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

    @Override
    public void playChime() {
        TScope scope = mTutor.getScope();
        for (CAsm_Alley alley: allAlleys) {
            CAsm_DotBag dotBag = alley.getDotBag();
            if (dotBag.getIsAudible()) {
                currentChime = dotBag.getCurrentChime();
            }
        }
        Log.d("PlayChime", currentChime);
        scope.addUpdateVar("TestChimes", new TString(currentChime));
        applyEventNode("PLAY_CHIME");
    }

    public void next() {

        // If wrong reset ALLCORRECT
        if (mTutor.testFeatureSet(TCONST.GENERIC_WRONG))
            mTutor.setDelFeature(TCONST.ALL_CORRECT);

        resetAll();

        super.next();
        resetPlaceValue();

        mTutor.getScope().addUpdateVar(name() + ".image", new TString(curImage));
        if (operation != null) {
            switch (operation) {
                case "+" :
                    mTutor.setAddFeature(TCONST.ASM_ADD);
                    mTutor.getScope().addUpdateVar(name() + ".operand1", new TString(numbers[0] + ""));
                    break;
                case "-" :
                    mTutor.setAddFeature(TCONST.ASM_SUBTRACT);
                    break;
                case "x" :
                    mTutor.setAddFeature(TCONST.ASM_MULTI);
                    mTutor.getScope().addUpdateVar(name() + ".operand1", new TString(numbers[0] + ""));
                    mTutor.getScope().addUpdateVar(name() + ".operand2", new TString(numbers[1] + ""));
                    break;
            }
        }
        if (dataExhausted()) mTutor.setAddFeature(TCONST.FTR_EOI);

        curFeatures.clear();
    }

    public void nextDigit() {

        reset();
        super.nextDigit();
        nextPlaceValue();

        retrieveCurFeaturesAboutOverhead();
    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
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
    public void onCreate() {

    }

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

    @Override
    public void onEvent(IEvent event) {

        mTutor.setDelFeature(TCONST.ASM_ALL_DOTS_DOWN);

        //mTutor.setDelFeature(TCONST.ASM_MULTI);
        super.onEvent(event);

        evaluateWhole();

        applyEventNode("NEXT");
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

    public void exitWrite() {
        super.exitWrite();
    }

    public void highlightOverheadOrResult(String whichToHighlight) {
        mechanics.highlightOverheadOrResult(whichToHighlight);
    }

    public void addMapToTutor(String key, String value) {
        mTutor.getScope().addUpdateVar(name() + key, new TString(value));
    }

    public void delAddFeature(String delFeature, String addFeature) {
        mTutor.setDelFeature(delFeature);
        mTutor.setAddFeature(addFeature);
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex, int startRow) {
        boolean oldState = hasShown;
        super.setDotBagsVisible(_dotbagsVisible, curDigitIndex, startRow);

        if (oldState == false && hasShown == true) {
            if (curStrategy.equals(ASM_CONST.STRATEGY_COUNT_FROM))
                mTutor.setAddFeature(TCONST.ASM_ADD_PROMPT_COUNT_FROM);
            else
                mTutor.setAddFeature(TCONST.ASM_ADD_PROMPT);
            mTutor.setAddFeature(TCONST.ASM_SUB_PROMPT);

            if (curNode.equals(ASM_CONST.NODE_ADD_PROMPT) || curNode.equals(ASM_CONST.NODE_SUB_PROMPT)) {
                mTutor.setAddFeature(TCONST.ASM_CLICK_ON_DOT);
                applyEventNode("NEXT");
            }
        }
    }

    public void updateCurNode(String curNode) {
        this.curNode = curNode;
    }

}
