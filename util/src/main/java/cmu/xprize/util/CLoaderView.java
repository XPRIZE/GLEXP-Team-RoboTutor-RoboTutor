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

package cmu.xprize.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cmu.xprize.common.R;


public class CLoaderView extends LinearLayout {

    private ViewGroup viewParent = null;

    private LocalBroadcastManager bManager;
    private LoadReceiver          bReceiver;

    private TextView        SprogressTitle;
    private TextView        SprogressMsg1;
    private TextView        SprogressMsg2;

    private ProgressBar     SprogressBarI;
    private ProgressBar     SprogressBarD;


    public CLoaderView(Context context, ViewGroup parent) {
        super(context);
        viewParent = parent;
        init(context, null);
    }

    public CLoaderView(Context context) {
        super(context);
        init(context, null);
    }

    public CLoaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CLoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(TCONST.START_PROGRESSIVE_UPDATE);
        filter.addAction(TCONST.UPDATE_PROGRESS);
        filter.addAction(TCONST.PROGRESS_TITLE);
        filter.addAction(TCONST.PROGRESS_MSG1);
        filter.addAction(TCONST.PROGRESS_MSG2);

        bReceiver = new LoadReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }


    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        SprogressTitle = (TextView) findViewById(R.id.SprogressTitle);
        SprogressMsg1  = (TextView) findViewById(R.id.SprogressMsg1);
        SprogressMsg2  = (TextView) findViewById(R.id.SprogressMsg2);

        SprogressBarI = (ProgressBar) findViewById(R.id.SprogressBarI);
        SprogressBarD = (ProgressBar) findViewById(R.id.SprogressBarD);
    }


    /**
     * Release resources and disconnect from broadcast Manager
     */
    public void onDestroy() {

        try {
            setOnClickListener(null);
            bManager.unregisterReceiver(bReceiver);
        }
        catch(Exception e) {
        }
    }


    class LoadReceiver extends BroadcastReceiver {

        public void onReceive (Context context, Intent intent) {

            Log.d("Loader", "Broadcast received: ");

            switch(intent.getAction()) {

                case TCONST.START_INDETERMINATE_UPDATE:
                    SprogressBarI.setVisibility(VISIBLE);
                    SprogressBarD.setVisibility(GONE);
                    requestLayout();
                    break;

                case TCONST.START_PROGRESSIVE_UPDATE:
                    SprogressBarD.setVisibility(VISIBLE);
                    SprogressBarI.setVisibility(GONE);

                    String maxVal = intent.getStringExtra(TCONST.TEXT_FIELD);

                    SprogressBarD.setProgress(0);
                    SprogressBarD.setMax(Integer.parseInt(maxVal));
                    requestLayout();
                    break;

                case TCONST.UPDATE_PROGRESS:
                    String curVal = intent.getStringExtra(TCONST.TEXT_FIELD);

                    SprogressBarD.setProgress(Integer.parseInt(curVal));
                    SprogressBarD.postInvalidate();
                    break;

                case TCONST.PROGRESS_TITLE:
                    String titleText = intent.getStringExtra(TCONST.TEXT_FIELD);

                    SprogressTitle.setText(titleText);
                    SprogressTitle.setVisibility(VISIBLE);
                    break;

                case TCONST.PROGRESS_MSG1:
                    String msgText1 = intent.getStringExtra(TCONST.TEXT_FIELD);

                    SprogressMsg1.setText(msgText1);
                    SprogressMsg1.setVisibility(VISIBLE);
                    break;

                case TCONST.PROGRESS_MSG2:
                    String msgText2 = intent.getStringExtra(TCONST.TEXT_FIELD);

                    SprogressMsg2.setText(msgText2);
                    SprogressMsg2.setVisibility(VISIBLE);
                    break;

            }
        }
    }

}
