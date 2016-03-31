/**
 Copyright 2015 Kevin Willows
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import cmu.xprize.ltk.CStimRespBase;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TStimRespBase extends CStimRespBase implements ITutorObjectImpl {


    private CTutor          mTutor;
    private CObjectDelegate mSceneObject;

    private float aspect   = 0.82f;  // w/h
    private int   _wrong   = 0;
    private int   _correct = 0;

    private static final String  TAG = TStimResp.class.getSimpleName();



    public TStimRespBase(Context context) {
        super(context);
        init(context, null);
    }

    public TStimRespBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TStimRespBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }

    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int finalWidth, finalHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec );

        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        finalWidth  = (int)(originalHeight * aspect);
        finalHeight = originalHeight;

        setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeight * 0.7f);

        setMeasuredDimension(finalWidth, finalHeight);

//        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
//                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
//        super.onMeasure(
//                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }


    private void cachedataSource() {

    }


    @Override
    public void addChar(String newChar) {

        super.addChar(newChar);

        // update the response variable  "<Sresponse>.value"
        mTutor.getScope().addUpdate(name() + ".value", new TString(mValue));

        if(mLinkedView == null)
            mLinkedView = (TStimRespBase)mTutor.getViewById(mLinkedViewID, null);

        if(mLinkedView != null) {

            if(newChar.equals("???")) {
                mTutor.setAddFeature(TCONST.FWUNKNOWN);
                _wrong++;
            }
            else {
                String Stimulus = mLinkedView.getValue();

                if (mValue.equals(Stimulus)) {
                    mTutor.setAddFeature(TCONST.FWCORRECT);
                    _correct++;
                } else {
                    mTutor.setAddFeature(TCONST.FWINCORRECT);
                    _wrong++;
                }

                // Set a flag if they're all correct when we are out of data
                //
                if (mLinkedView.dataExhausted()) {
                    if (_wrong == 0)
                        mTutor.setAddFeature(TCONST.FWALLCORRECT);
                }
            }
        }
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        _correct = 0;
        _wrong   = 0;

        mTutor.setDelFeature(TCONST.FWALLCORRECT);
        mTutor.setDelFeature(TCONST.FWCORRECT);
        mTutor.setDelFeature(TCONST.FWINCORRECT);

        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.TASSETS + "/" + dataSource);

            } else if (dataSource.startsWith("db|")) {
                dataSource = dataSource.substring(3);

            } else if (dataSource.startsWith("[")) {
                dataSource = dataSource.substring(1, dataSource.length()-1);

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Invalid Data Source for : " + name());
            System.exit(1);
        }

        // Pass an array of strings as the data source.
        //
        setDataSource(dataSource.split(","));
    }


    public void next() {

        mTutor.setDelFeature(TCONST.FWALLCORRECT);
        mTutor.setDelFeature(TCONST.FWCORRECT);
        mTutor.setDelFeature(TCONST.FWINCORRECT);

        super.next();

        // update the Scope response variable  "<Sstimulus>.value"
        //
        mTutor.getScope().addUpdate(name() + ".value", new TString(mValue));

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOI);
    }

    public void show(Boolean showHide) {

        super.show(showHide);
    }

    public void clear() {

        super.clear();

    }


    public void setBackGround(String Color) {

        super.setBackGround(Color);
    }


    public void setForeGround(String Color) {

        super.setForeGround(Color);
    }


    /**
     * Deprecated Feb 17 2016
     *
     * @param flagState
     * @param Color
     */
    public void flagError(Boolean flagState, String Color) {
        Log.e(TAG, "Unsuppported Function: " + "flagError");
        System.exit(1);
    }



    // Tutor methods  End
    //************************************************************************
    //************************************************************************




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
