//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cmu.xprize.comp_writing.CWritingComponent;
import cmu.xprize.comp_writing.WR_CONST;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.robotutor.tutorengine.CSceneDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILogManager;

import cmu.xprize.robotutor.R;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class TWritingComponent extends CWritingComponent implements IBehaviorManager, ITutorSceneImpl, IDataSink {

    private CTutor                  mTutor;
    private CSceneDelegate          mTutorScene;

    public List<IEventListener>     mListeners          = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews;
    protected boolean               mListenerConfigured = false;

    private HashMap<String, String> volatileMap = new HashMap<>();
    private HashMap<String, String> stickyMap = new HashMap<>();

    private int                     _wrong   = 0;
    private int                     _correct = 0;

    private ArrayList<CDataSourceImg> _dataStack  = new ArrayList<>();

    private ArrayList<String>       _FeatureSet = new ArrayList<>();
    private HashMap<String,Boolean> _FeatureMap = new HashMap<>();

    private static final String  TAG = TWritingComponent.class.getSimpleName();


    public TWritingComponent(Context context) {
        super(context);
    }

    public TWritingComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TWritingComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);

        mTutorScene = new CSceneDelegate(this);
        mTutorScene.init(context, attrs);
    }


    @Override
    public void onCreate() {

        // Obtain the prototype glyphs from the singleton recognizer
        //
        _recognizer = CRecognizerPlus.getInstance();
        _glyphSet   = _recognizer.getGlyphPrototypes(); //new GlyphSet(TCONST.ALPHABET);

        mRecognizedScroll = (CLinkedScrollView) findViewById(R.id.Sstimulus);
        mRecogList        = (LinearLayout) findViewById(R.id.SstimulusList);

        mDrawnScroll = (CLinkedScrollView) findViewById(R.id.SfingerWriter);
        mDrawnList   = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
        mDrawnList.setClipChildren(false);

// TODO: DEBUG only
//        mRecogList.setOnTouchListener(new RecogTouchListener());
//        mDrawnList.setOnTouchListener(new drawnTouchListener());

        mRecognizedScroll.setLinkedScroll(mDrawnScroll);
        mDrawnScroll.setLinkedScroll(mRecognizedScroll);

        // Iniitalize the static behaviors
        //
        setStickyBehavior(TCONST.NEXT_NODE, TCONST.NEXT_NODE);
    }


    @Override
    public void onDestroy() {

    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {
    }


    // Event Listener/Dispatcher - End
    //***********************************************************




    //************************************************************************
    //************************************************************************
    // Tutor Scriptable methods  Start


    public void postEvent(String event) {
        postEvent(event,0);
    }

    public void postEvent(String event, Integer delay) {

        switch (event) {

            case WR_CONST.RIPPLE_DEMO:
                post(WR_CONST.RIPPLE_DEMO, delay);
                break;

            case WR_CONST.RIPPLE_REPLAY:
                post(WR_CONST.RIPPLE_REPLAY, delay);
                break;

            case WR_CONST.RIPPLE_HIGHLIGHT:
                break;

            case WR_CONST.ANIMATE_OVERLAY:
            case WR_CONST.REPLAY_PROTOGLYPH:
            case WR_CONST.ANIMATE_ALIGN:
                post(event);
                break;

        }
    }

    public void pointAtEraseButton() {
        super.pointAtEraseButton();
    }


    public void highlightFields() {
        super.highlightFields();
    }


    public void clear() { super.clear(); }


    public void inhibitInput(Boolean inhibit) { super.inhibitInput(inhibit); }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // IBehaviorManager Interface START

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


    // Execute script target if behavior is defined for this event
    //
    public boolean applyBehavior(String event) {

        boolean result = false;

        if(!(result = super.applyBehavior(event))) {

            if (volatileMap.containsKey(event)) {
                Log.d(TAG, "Processing WC_ApplyEvent: " + event);
                applyBehaviorNode(volatileMap.get(event));

                volatileMap.remove(event);

                result = true;

            } else if (stickyMap.containsKey(event)) {
                applyBehaviorNode(stickyMap.get(event));

                result = true;
            }
        }

        return result;
    }


    /**
     * Apply Events in the Tutor Domain.
     *
     * @param nodeName
     */
    public void applyBehaviorNode(String nodeName) {
        IScriptable2 obj = null;

        if (nodeName != null && !nodeName.equals("") && !nodeName.toUpperCase().equals("NULL")) {

            try {
                obj = mTutor.getScope().mapSymbol(nodeName);

                if (obj != null) {
                    obj.preEnter();
                    obj.applyNode();
                }

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }

    // IBehaviorManager Interface END
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // publish component state data - START

    @Override
    protected void publishState() {

        retractFeature(WR_CONST.ERROR_METRIC);
        retractFeature(WR_CONST.ERROR_CHAR);
        retractFeature(TCONST.GENERIC_RIGHT);
        retractFeature(TCONST.GENERIC_WRONG);

        if(_isValid) {

            publishFeature(TCONST.GENERIC_RIGHT);
        }
        else {

            publishFeature(TCONST.GENERIC_WRONG);

            if(!_metricValid) {
                publishFeature(WR_CONST.ERROR_METRIC);
            }
            if(!_charValid) {
                publishFeature(WR_CONST.ERROR_CHAR);
            }
        }

        applyBehavior(WR_CONST.FIELD_COMPLETE);
    }

    @Override
    public void publishValue(String varName, String value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TString(value));

    }

    @Override
    public void publishValue(String varName, int value) {

        // update the response variable  "<ComponentName>.<varName>"
        mTutor.getScope().addUpdateVar(name() + varName, new TInteger(value));

    }

    @Override
    public void publishFeature(String feature) {

        if(_FeatureSet.indexOf(feature) == -1)
        {
            _FeatureSet.add(feature);
        }

        _FeatureMap.put(feature, true);
        mTutor.setAddFeature(feature);
    }

    @Override
    public void retractFeature(String feature) {

        _FeatureMap.put(feature, false);
        mTutor.setDelFeature(feature);
    }

    // publish component state data - EBD
    //************************************************************************
    //************************************************************************


    //************************************************************************
    //************************************************************************
    // DataSink Implementation Start


    /**
     *
     * @param dataPacket
     */
    public void pushDataSource(String dataPacket) {

        if(dataSource != null) {
            _dataStack.add(new CDataSourceImg());
        }

        setDataSource(dataPacket);
    }


    /**
     *
     */
    public void popDataSource() {

        int popIndex = _dataStack.size()-1;

        if(popIndex >= 0) {
            CDataSourceImg popped = _dataStack.get(popIndex);
            popped.restoreDataSource();
            _dataStack.remove(popIndex);
        }
    }


    /**
     *
     * @param dataPacket
     */
    public void setDataSource(String dataPacket) {

        _correct = 0;
        _wrong   = 0;

        retractFeature(TCONST.ALL_CORRECT);
        retractFeature(TCONST.FWCORRECT);
        retractFeature(TCONST.FWINCORRECT);

        try {
            if (dataPacket.startsWith(TCONST.SOURCEFILE)) {
                dataPacket = dataPacket.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataPacket);

                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), mTutor.getScope() );

                // Pass the loaded json dataSource array
                //
                setDataSource(dataSource);

            } else if (dataPacket.startsWith("db|")) {
                dataPacket = dataPacket.substring(3);

            } else if (dataPacket.startsWith("[")) {

                dataPacket = dataPacket.substring(1, dataPacket.length()-1);

                // Pass an array of strings as the data source.
                //
                setDataSource(dataPacket.split(","));

            } else {
                throw (new Exception("test"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), null, false);
        }

    }


    public void next() {

        retractFeature(TCONST.ALL_CORRECT);
        retractFeature(TCONST.FWCORRECT);
        retractFeature(TCONST.FWINCORRECT);

        super.next();

        // update the Scope response variable  "<Sstimulus>.value"
        //
        mTutor.getScope().addUpdateVar(name() + ".value", new TString(mStimulus));
        mTutor.getScope().addUpdateVar(name() + ".valueUC", new TString(mStimulus.toUpperCase()));

        if(dataExhausted()) {

            // set the script 'Feature'
            mTutor.setAddFeature(TCONST.FTR_EOI);
        }
    }


    class CDataSourceImg {

        private int  _wrongStore   = 0;
        private int  _correctStore = 0;

        private HashMap<String,Boolean> _FeatureStore;

        protected List<String>  _dataStore;
        protected int           _dataIndexStore;
        protected boolean       _dataEOIStore;

        String[]                _dataSourceStore;

        public CDataSourceImg() {

            _correctStore    = _correct;
            _wrongStore      = _wrong;

            _dataStore       = _data;
            _dataIndexStore  = _dataIndex;
            _dataEOIStore    = _dataEOI;

            _FeatureStore    = _FeatureMap;
            _dataSourceStore = dataSource;

            for(String feature : _FeatureSet) {
                mTutor.setDelFeature(feature);
            }

        }

        public void restoreDataSource() {

            _correct = _correctStore;
            _wrong   = _wrongStore;

            _data      = _dataStore;
            _dataIndex = _dataIndexStore;
            _dataEOI   = _dataEOIStore;

            _FeatureMap= _FeatureStore;
            dataSource = _dataSourceStore;

            for(String feature : _FeatureSet) {
                if(_FeatureMap.get(feature)) {
                    mTutor.setAddFeature(feature);
                }
                else {
                    mTutor.setDelFeature(feature);
                }
            }
        }
    }


    // DataSink IMplementation End
    //************************************************************************
    //************************************************************************




    //************************************************************************
    //************************************************************************
    // ITutorObject Implementation Start

    @Override
    public void setName(String name) {
        mTutorScene.setName(name);
    }

    @Override
    public String name() {
        return mTutorScene.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mTutorScene.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mTutor = tutor;
        mTutorScene.setTutor(tutor);
    }

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mTutorScene.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mTutorScene.setLogManager(logManager);
    }


    @Override
    public CSceneDelegate getimpl() {
        return mTutorScene;
    }


    @Override
    public ViewGroup getOwner() {
        return mTutorScene.getOwner();
    }

    @Override
    public String preEnterScene(scene_descriptor scene, String Direction) {
        return mTutorScene.preEnterScene(scene, Direction);
    }

    @Override
    public void onEnterScene() {
        mTutorScene.onEnterScene();
    }

    @Override
    public String preExitScene(String Direction, int sceneCurr) {
        return mTutorScene.preExitScene(Direction, sceneCurr);
    }

    @Override
    public void onExitScene() {
        mTutorScene.onExitScene();
    }

    // ITutorObject Implementation End
    //************************************************************************
    //************************************************************************




    // *** Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        Log.d(TAG, "Loader iteration");
        super.loadJSON(jsonObj, (IScope2) scope);

    }

}
