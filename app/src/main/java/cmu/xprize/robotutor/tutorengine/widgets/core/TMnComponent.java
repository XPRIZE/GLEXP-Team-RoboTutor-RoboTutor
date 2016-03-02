package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.mn_component.CMn_Component;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TMnComponent extends CMn_Component  implements ITutorObjectImpl {

    private CTutorObjectDelegate mSceneObject;


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
        mSceneObject = new CTutorObjectDelegate(this);
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

        try {
            if (dataSource.startsWith("file|")) {
                dataSource = dataSource.substring(5);

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + CTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);
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
            CTutor.setAddFeature(TCONST.FTR_EOI);
    }


    public TBoolean test() {
        boolean correct = isCorrect();

        if(correct)
            CTutor.setAddFeature("FTR_RIGHT");
        else
            CTutor.setAddFeature("FTR_WRONG");

        return new TBoolean(correct);
    }


    public void reset() {

        CTutor.setDelFeature("FTR_RIGHT");
        CTutor.setDelFeature("FTR_WRONG");
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
    public CTutorObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {
        mSceneObject.zoomInOut(scale, duration);
    }
}
