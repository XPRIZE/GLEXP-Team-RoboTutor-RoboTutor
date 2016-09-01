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

package cmu.xprize.comp_writing;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

public class CWritingScrollController extends CLinkedScrollView implements IEventDispatcher, IEventListener, IWritingController {

    public List<IEventListener>     mListeners = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews;
    protected boolean               mListenerConfigured = false;

    protected LinearLayout          mDrawnList;

    private String                  mResponse;
    private String                  mStimulus;


    final private String TAG = "CWritingScrollController";



    public CWritingScrollController(Context context) {
        super(context);
        init(context, null);
    }

    public CWritingScrollController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CWritingScrollController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    protected void init(Context context, AttributeSet attrs) {

        String linkedViews;

        super.init(context, attrs);

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                linkedViews = a.getNonResourceString(R.styleable.RoboTutor_linked_views);

                if(linkedViews != null) {
                    mLinkedViews = Arrays.asList(linkedViews.split(","));
                }

            } finally {
                a.recycle();
            }
        }

    }


    //***********************************************************
    // Event Listener/Dispatcher - Start

    /**
     * Must be Overridden to access mTutor
     * @param linkedView
     */
    @Override
    public void addEventListener(String linkedView) {
    }

    @Override
    public void dispatchEvent(IEvent event) {

        // Do defferred listeners configuration - this cannot be done until after the
        // view has been inflated so cannot be in init()
        //
        if(!mListenerConfigured) {
            for (String linkedView : mLinkedViews) {
                addEventListener(linkedView);
            }
            mListenerConfigured = true;
        }
        for (IEventListener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEvent(IEvent event) {

        View v;

        switch(event.getType()) {

            // Message from Stimiulus variant to share state with response variant
            case TCONST.FW_STIMULUS:
                mStimulus = (String)event.getString(TCONST.FW_VALUE);

                mDrawnList = (LinearLayout) findViewById(R.id.Sdrawn_glyphs);
                mDrawnList.setClipChildren(true);

                for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
                {
                    // create a new view
                    v = LayoutInflater.from(getContext())
                            .inflate(R.layout.drawn_input_comp, null, false);

                    // Control whether glyphs are clipped at the draw view boundry
                    //
                    ((ViewGroup)v).setClipChildren(false);

                    mDrawnList.addView(v);
//                    ((CDrawnInputController)v).setRecognizer(_recognizer);
                    ((CDrawnInputController)v).setLinkedScroll(this);
                    ((CDrawnInputController)v).setWritingController(this);
                }

                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId) {

    }


    /**
     * TODO: rewrite the LTK project format
     * @param recogId
     */
    public void setRecognizer(String recogId, String subset) {

    }


    /**
     * Enable or Disable the finger writer
     * @param enableState
     */
    public void enable(Boolean enableState) {

    }


    /**
     * Enable or disable persona messages.  Whether or not the persona will
     * watch finger motion
     *
     * @param watchEnabled
     */
    public void personaWatch(Boolean watchEnabled) {

//        _watchable = watchEnabled;
    }


    public void onStartWriting(String symbol) {
//        _onStartWriting = symbol;
    }


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    protected void applyEventNode(String nodeName) {
    }


    // Tutor methods  End
    //************************************************************************
    //************************************************************************



    //************************************************************************
    //************************************************************************
    // IWritingController Start

    @Override
    public void onCreate(Context context) {

    }

    /**
     * Note that only the mDrawnList will initiate this call
     *
     * @param child
     */
    public void deleteItem(View child) {
        int index = mDrawnList.indexOfChild(child);

        mDrawnList.removeViewAt(index);
     //   mRecogList.removeViewAt(index);
    }


    /**
     * Note that only the mDrawnList will initiate this call
     *
     * @param child
     */
    public void addItemAt(View child, int inc) {

        int index = mDrawnList.indexOfChild(child);

        // create a new view
        View rv = LayoutInflater.from(getContext())
                .inflate(R.layout.recog_resp_comp, null, false);

        // create a new view
        View dv = LayoutInflater.from(getContext())
                .inflate(R.layout.drawn_input_comp, null, false);

        ((CDrawnInputController)dv).setClipChildren(false);

        mDrawnList.addView(dv, index + inc);

        ((CDrawnInputController)dv).setLinkedScroll(this);
        ((CDrawnInputController)dv).setWritingController(this);

     //   mRecogList.addView(rv, index + inc);
    }


    public void updateGlyph(IDrawnInputController child, String glyph) {

        int index = mDrawnList.indexOfChild((View)child);

       // CResponseContainer respText = (CResponseContainer)mRecogList.getChildAt(index);

        //respText.setResponseChar(glyph);
    }


    @Override
    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB) {

    }

    // IWritingController End
    //************************************************************************
    //************************************************************************


}
