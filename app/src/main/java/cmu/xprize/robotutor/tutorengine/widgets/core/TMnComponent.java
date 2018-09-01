package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONObject;

import cmu.xprize.mn_component.CMn_Component;
import cmu.xprize.mn_component.CMn_Data;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TMnComponent extends CMn_Component  implements ITutorObject, IDataSink {

    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;


    static final private String TAG = "TMnComponent";


    public TMnComponent(Context context) {
        super(context);
        initT(context, null);
    }

    public TMnComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initT(context, attrs);
    }

    public TMnComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initT(context, attrs);
    }

    public void initT(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
        mTutor.getScope().addUpdateVar(name() + ".value", null);
    }


    @Override
    public void UpdateValue(int value) {

        // update the Scope response variable  "<varname>.value"
        //
        mTutor.getScope().addUpdateVar(name() + ".value", new TInteger(value));

        boolean correct = isCorrect();

        reset();

        if(correct)
            mTutor.addFeature(TCONST.GENERIC_RIGHT);
        else
            mTutor.addFeature(TCONST.GENERIC_WRONG);
    }


    private void reset() {

        mTutor.delFeature(TCONST.GENERIC_RIGHT);
        mTutor.delFeature(TCONST.GENERIC_WRONG);
    }


    /**
     * Preprocess the data set
     *
     * @param data
     */
    protected void updateDataSet(CMn_Data data) {

        int index = 0;

        mTutor.getScope().addUpdateVar(name() + ".numcol", new TInteger(data.dataset.length));

        for(int elem : data.dataset) {
            mTutor.getScope().addUpdateVar(name() + ".col" + index++, new TInteger(elem));
        }

        // Let the compoenent process the new data set
        //
        super.updateDataSet(data);
    }


    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
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
        mTutor.addFeature(TCONST.ALL_CORRECT);

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

            mTutor.delFeature(TCONST.ALL_CORRECT);
        }

        reset();

        super.next();

        if(dataExhausted())
            mTutor.addFeature(TCONST.FTR_EOI);
    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }


    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

    //    * Note that the parameters here have to be non-intrinsic types so that the scripting engine can instantiate
    //    * them. i.e. they must have a constructor.


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
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }



    /**
     * Note that wiggle is implemented in the super class not the delegate to allow subcomponent wiggles
     * This override targets just the icons on the question column if no index/part spec are given.
     *
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     */
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition ) {
        super.wiggle(direction, magnitude, duration, repetition);
    }


    /**
     * Note that wiggle is implemented in the super class not the delegate to allow subcomponent wiggles
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     * @param part
     */
    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition, String part) {
        super.wiggle(direction, magnitude, duration, repetition, part);
    }


    /**
     * Note that wiggle is implemented in the super class not the delegate to allow subcomponent wiggles
     * @param direction
     * @param magnitude
     * @param duration
     * @param repetition
     * @param part
     * @param index
     */
    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition, String part, Integer index ) {
        super.wiggle(direction, magnitude, duration, repetition, part, index);
    }
}
