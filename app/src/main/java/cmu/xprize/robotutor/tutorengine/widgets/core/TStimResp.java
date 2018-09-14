/**
 Copyright(c) 2015-2017 Kevin Willows
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

import cmu.xprize.fw_component.CStimResp;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

public class TStimResp extends CStimResp implements ITutorObject, IDataSink  {


    private CObjectDelegate mSceneObject;

    private float aspect = 0.82f;  // w/h

    private static final String  TAG = TStimResp.class.getSimpleName();



    public TStimResp(Context context) {
        super(context);
        init(context, null);
    }

    public TStimResp(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TStimResp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    @Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int finalWidth, finalHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec );

        int originalWidth  = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        finalWidth  = (int)(originalHeight * aspect);
        finalHeight = originalHeight;

        setMeasuredDimension(finalWidth, finalHeight);

//        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
//                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
//        super.onMeasure(
//                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }


    private void cachedataSource() {

    }




    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }

    /**
     * TODO: This is incorrect - remove?
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

// TODO: This isn't correct
//                JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.TDESC + "/" + dataSource);

            } else if (dataSource.startsWith("db|")) {
                dataSource = dataSource.substring(3);

            } else if (dataSource.startsWith("[")) {
                dataSource = dataSource.substring(1, dataSource.length()-1);

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), null, false);
        }

        // Pass an array of strings as the data source to the base object
        //
        setDataSource(dataSource.split(","));
    }


    public void next() {
        super.next();
    }

    public void show(Boolean showHide) {  }


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
}
