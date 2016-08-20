package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Array;

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

public class TAsmComponent extends CAsm_Component implements ITutorObjectImpl {

    private CTutor           mTutor;
    private CObjectDelegate  mSceneObject;

    static final String TAG = "TAsmComponent";

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

    public void evaluateWhole () {


        reset();

        boolean correct = isWholeCorrect();

        if(correct)
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        else
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
    }

    public void evaluateDigit () {

        reset();

        boolean correct = isDigitCorrect();

        if(correct)
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        else
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
    }

    public void reset() {

        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
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
        //
        if(mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {
            mTutor.setDelFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.next();
        resetPlaceValue();
        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOI);

    }

    public void nextDigit() {

        reset();
        super.nextDigit();
        nextPlaceValue();

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
    public void postInflate() {

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
        super.onEvent(event);
        evaluateWhole();
        applyEventNode("NEXT");
    }

    protected void applyEventNode(String nodeName) {
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

    public void highlightBorrowable() {
        if(operation.equals("-")) {
            mechanics.highlightBorrowable();
        }
    }
}
