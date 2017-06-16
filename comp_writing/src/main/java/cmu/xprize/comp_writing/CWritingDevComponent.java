//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
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

package cmu.xprize.comp_writing;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Map;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.comp_logging.CErrorManager;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;


/**
 * TODO: document your custom view class.
 *
 *  !!!! NOTE: This requires com.android.support:percent:23.2.0' at least or the aspect ratio
 *  settings will not work correctly.
 *
 */
public class CWritingDevComponent extends CWritingComponent {

    private TextView          mLtkStats;
    private TextView          mLtkPlus;
    private TextView          mVisualStats;
    private TextView          mFitStatsA;
    private TextView          mFitStatsB;

    private Button            mShowSample;
    private Button            mShowPrototype;
    private Button            mShowBounds;
    private Spinner           mFontSelector;

    private Button            mBoostSample;
    private Button            mBoostPunct;
    private Spinner           mBoostClass;

    final private boolean mMoveRIGHT = false;

    final private String  TAG        = "WritingComp";

    public CWritingDevComponent(Context context) {
        super(context);
        init(null, 0);
    }

    public CWritingDevComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CWritingDevComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        setClipChildren(false);

    }

    public void onCreate() {

        super.onCreate();

        mLtkStats    = (TextView)findViewById(R.id.Smetrics_Ltk);
        mLtkPlus     = (TextView)findViewById(R.id.Smetrics_LtkPlus);
        mVisualStats = (TextView)findViewById(R.id.Smetrics_Visual);
        mFitStatsA   = (TextView)findViewById(R.id.Smetrics_FitA);
        mFitStatsB   = (TextView)findViewById(R.id.Smetrics_FitB);

        mShowSample    = (Button)findViewById(R.id.SshowSample);
        mShowPrototype = (Button)findViewById(R.id.SshowPrototype);
        mShowBounds    = (Button)findViewById(R.id.SshowBounds);
        mFontSelector  = (Spinner) findViewById(R.id.SfontSelector);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.fonts_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFontSelector.setAdapter(adapter);

        mShowSample.setOnClickListener(new showSampleClickListener());
        mShowPrototype.setOnClickListener(new showPrototypeClickListener());
        mShowBounds.setOnClickListener(new showBoundsClickListener());
        mFontSelector.setOnItemSelectedListener(new SpinnerSelectionListener());


        mBoostSample = (Button)findViewById(R.id.SboostExpected);
        mBoostPunct  = (Button)findViewById(R.id.SboostPunct);
        mBoostClass  = (Spinner) findViewById(R.id.SboostClass);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> boostadapter = ArrayAdapter.createFromResource(mContext,
                R.array.boost_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        boostadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBoostClass.setAdapter(boostadapter);

        mBoostSample.setOnClickListener(new boostSampleClickListener());
        mBoostPunct.setOnClickListener(new boostPunctClickListener());
        mBoostClass.setOnItemSelectedListener(new boostClassSelectionListener());
    }


    public class showSampleClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

                CGlyphController comp = (CGlyphController) mGlyphList.getChildAt(i1);

                if(comp.toggleSampleChar()) {
                    Log.v(QGRAPH_MSG, "event.click: " + " Hide Sample");

                    mShowSample.setText("Hide Sample");
                }
                else {
                    Log.v(QGRAPH_MSG, "event.click: " + " Show Sample");

                    mShowSample.setText("Show Sample");
                }
            }
        }
    }

    public class showPrototypeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            boolean result = false;

            for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

                CGlyphController comp = (CGlyphController) mGlyphList.getChildAt(i1);
                result = comp.toggleProtoGlyph();
            }

            if(result) {
                Log.v(QGRAPH_MSG, "event.click: " + " Show User Glyph");

                mShowPrototype.setText("Show User Glyph");
            }
            else {
                Log.v(QGRAPH_MSG, "event.click: " + " Show Prototype");

                mShowPrototype.setText("Show Prototype");
            }

        }
    }


    public class showBoundsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            boolean result = false;

            for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

                CGlyphController comp = (CGlyphController) mGlyphList.getChildAt(i1);
                result = comp.toggleDebugBounds();
            }

            if(result) {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Hide Bounds");

                mShowBounds.setText("Hide Bounds");
            }
            else {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Show Bounds");

                mShowBounds.setText("Show Bounds");
            }

        }
    }


    public class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            // An item was selected. You can retrieve the selected item using
            String fontSelection = (String)parent.getItemAtPosition(pos);

            Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:select Font");

            for(int i1 = 0 ; i1 < mStimulusData.length ; i1++) {

                CGlyphController comp = (CGlyphController) mGlyphList.getChildAt(i1);
                comp.selectFont(fontSelection);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }



    public class boostSampleClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if(_recognizer.toggleExpectedBoost()) {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Boost Expected : YES");

                mBoostSample.setText("Boost Expected : YES");
            }
            else {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Boost Expected : YES");

                mBoostSample.setText("Boost Expected : NO ");
            }

        }
    }


    public class boostPunctClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if(_recognizer.togglePunctBoost()) {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Boost Punctuation : YES");

                mBoostPunct.setText("Boost Punctuation : YES");
            }
            else {
                Log.v(QGRAPH_MSG, "event.click: " + " CWritingDevComponent:Boost Punctuation : NO");

                mBoostPunct.setText("Boost Punctuation : NO ");
            }

        }
    }


    public class boostClassSelectionListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            // An item was selected. You can retrieve the selected item using
            String classSelection = (String)parent.getItemAtPosition(pos);

            _recognizer.setClassBoost(classSelection);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }



    @Override
    public void updateGlyphStats(CRecResult[] ltkPlusCandidates, CRecResult[] charCandidates, CGlyphMetrics metricsA, CGlyphMetrics metricsB) {


        // TODO: This is an experiment to see how full visual processing affects results  SEARCH: VISUALCOMPARE
        String ltkPlusStats = "";

        for(CRecResult candidate : ltkPlusCandidates) {

            ltkPlusStats += candidate.getRecChar() + " : " + String.format("%.3f", candidate.getPlusConfidence());

            if(candidate.isVirtual()) {
                ltkPlusStats += " *";
            }
            ltkPlusStats += "\n";
        }
        mLtkPlus.setText(ltkPlusStats);



        String ltkStats = "";

        for(CRecResult candidate : charCandidates) {

            ltkStats += candidate.getRecChar() + " : " + String.format("%.3f", candidate.Confidence);

            if(candidate.isVirtual()) {
                ltkStats += " *";
            }
            ltkStats += "\n";
        }
        mLtkStats.setText(ltkStats);



        String visualStats = "";

        for(CRecResult candidate : charCandidates) {

            visualStats += candidate.getRecChar() + " : " + String.format("%.3f", candidate.getVisualConfidence()) + "\n";
        }
        mVisualStats.setText(visualStats);



        // These are the fit stats for the sample (i.e. expected) character
        //
        String fitStats = "";

        fitStats += "dX: " + metricsA.getHorizontalDeviation() + "\n";
        fitStats += "dY: " + metricsA.getVerticalDeviation() + "\n";

        fitStats += "dW: " + metricsA.getWidthDeviation() + "\n";
        fitStats += "dH: " + metricsA.getHeightDeviation() + "\n";

        fitStats += "dA: " + metricsA.getAspectDeviation() + "\n";

        mFitStatsA.setText(fitStats);



        // These are the fit stats for the LTK (i.e. inferred) character
        //
        String fitStatsB = "";

        fitStatsB += "dX: " + metricsB.getHorizontalDeviation() + "\n";
        fitStatsB += "dY: " + metricsB.getVerticalDeviation() + "\n";

        fitStatsB += "dW: " + metricsB.getWidthDeviation() + "\n";
        fitStatsB += "dH: " + metricsB.getHeightDeviation() + "\n";

        fitStatsB += "dA: " + metricsB.getAspectDeviation() + "\n";

        mFitStatsB.setText(fitStatsB);
    }



    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _command    = "";
        protected String _target     = "";
        protected String _item       = "";

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, String target) {
            _command = command;
            _target  = target;
        }

        public Queue(String command, String target, String item) {
            _command = command;
            _target  = target;
            _item    = item;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                switch(_target) {
                    case "STIMULUS":
                        break;

                    case "RESPONSE":
                        break;

                    case "RECOGNIZER":
                        _recognizer.execCommand(_command);
                        break;

                    case "GLYPH":
                        break;

                    default:

                        break;
                }


            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new Queue(command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new Queue(command, target, item), delay); }




    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************


}
