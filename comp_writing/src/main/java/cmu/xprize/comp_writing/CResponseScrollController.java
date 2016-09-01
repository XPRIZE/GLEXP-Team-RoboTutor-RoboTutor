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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

public class CResponseScrollController extends CLinkedScrollView implements IEventDispatcher, IEventListener {

    public List<IEventListener>     mListeners = new ArrayList<IEventListener>();
    protected List<String>          mLinkedViews;
    protected boolean               mListenerConfigured = false;

    private LinearLayout            mRecogList;

    private String                  mResponse;
    private String                  mStimulus;


    final private String TAG = "CResponseScrollController";



    public CResponseScrollController(Context context) {
        super(context);
        init(context, null);
    }

    public CResponseScrollController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CResponseScrollController(Context context, AttributeSet attrs, int defStyleAttr) {
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

                mRecogList = (LinearLayout) findViewById(R.id.Srecognized_glyphs);

                for(int i1 =0 ; i1 < mStimulus.length() ; i1++)
                {
                    // create a new view
                    v = LayoutInflater.from(getContext())
                            .inflate(R.layout.recog_resp_comp, null, false);

                    mRecogList.addView(v);
                }

                break;
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    /**
     * Deprecated - in favor of onRecognitionEvent
     *
     * @param symbol
     */
    public void onRecognitionComplete(String symbol) {
        onRecognitionEvent(symbol);
    }


    public void onRecognitionEvent(String symbol) {

    }



}
