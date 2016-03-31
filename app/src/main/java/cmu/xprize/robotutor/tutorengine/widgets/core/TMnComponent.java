package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.mn_component.CMn_Component;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TMnComponent extends CMn_Component  implements ITutorObjectImpl {

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

    //**********************************************************
    //**********************************************************
    //*****************  Tutor Interface

    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

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
            Log.e(TAG, "Invalid Data Source for : " + name());
            System.exit(1);
        }
    }


    public void next() {

        reset();

        super.next();

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOI);
    }


    public TBoolean test() {
        boolean correct = isCorrect();

        if(correct)
            mTutor.setAddFeature("FTR_RIGHT");
        else
            mTutor.setAddFeature("FTR_WRONG");

        return new TBoolean(correct);
    }


    public void reset() {

        mTutor.setDelFeature("FTR_RIGHT");
        mTutor.setDelFeature("FTR_WRONG");
    }



    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

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
    public void setNavigator(ITutorNavigator navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }


    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }
}
