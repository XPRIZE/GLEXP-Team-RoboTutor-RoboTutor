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

package cmu.xprize.comp_ask;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.comp_debug.CDebugComponent;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CClassMap;
import cmu.xprize.util.CTutorData_Metadata;
import cmu.xprize.util.IButtonController;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.ImageLoader;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;
import static cmu.xprize.util.TCONST.Thumb.BPOP_LTR;


public class CAskComponent extends FrameLayout implements ILoadableObject, View.OnTouchListener {

    protected Context               mContext;
    protected String                packageName;
    protected HashMap<View,String>  buttonMap;
    protected ArrayList<View>       buttonList;

    protected CAsk_Data             mDataSource;

    protected IButtonController     mButtonController;

    static private boolean          SINGLE_SELECT = true;

    protected LocalBroadcastManager bManager;

    final private String  TAG = "CAskComponent";



    public CAskComponent(Context context) {
        super(context);
        init(context, null);

    }

    public CAskComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAskComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    /**
     * We must release drawable references to avoid memory leaks
     *
     */
    public void onDestroy() {

        releaseReferences();
    }


    public void setButtonController(IButtonController controller) {

        mButtonController = controller;
    }


    public void cancelPointAt() {

        Intent msg = new Intent(TCONST.CANCEL_POINT);
        bManager.sendBroadcast(msg);
    }


    private void broadcastMsg(String Action) {

        Intent msg = new Intent(Action);

        bManager.sendBroadcast(msg);
    }


    public void pointAtViewByName(String name) {

        int id = getResources().getIdentifier(name, "id", packageName);

        if(id != -1)
            pointAtViewByID(id);
    }

    public void pointAtViewByID(int id) {

        View view = (View) findViewById(id);

        if(view != null)
            pointAtView(view);
    }

    protected void pointAtView(View target) {

        int[] _screenCoord = new int[2];

        target.getLocationOnScreen(_screenCoord);

        PointF centerPt = new PointF(_screenCoord[0] + (target.getWidth() / 2), _screenCoord[1] + (target.getHeight() / 2));
        Intent msg = new Intent(TCONST.POINT_AND_TAP);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{centerPt.x, (float) centerPt.y});

        bManager.sendBroadcast(msg);
    }


    //**********************************************************
    //**********************************************************
    //*****************  DataSink Interface


    public boolean dataExhausted() {
        return true;
    }


    public void setDataSource(String[] dataSource) {}


    /**
     * call it "initializeButtonsAndSetButtonImages"...
     * @param layoutName reference to a layout file
     * @param dataSource what goes into the layout file
     * @param nextActivities what are the next activities to display
     */
    public void initializeButtonsAndSetButtonImages(String layoutName, CAsk_Data dataSource, CAt_Data[] nextActivities) {

        // Keep track of the datasource so we can destroy the references when finished.
        //
        mDataSource = dataSource;

        // here is where layout is set...
        int layoutID = getResources().getIdentifier(layoutName, "layout", packageName);

        removeAllViews();

        // Inflate the layout into "this" AskComponent
        //
        inflate(mContext, layoutID, this);

        // Populate the layout elements
        //
        buttonMap  = new HashMap<>();
        buttonList = new ArrayList<>();

        for(CAskElement element : dataSource.items) {
            if (element == null) break;
            int viewID = getResources().getIdentifier(element.componentID, "id", packageName);

            View view = findViewById(viewID);

            ImageButton ibView = (ImageButton) view;

            buttonMap.put(ibView, element.componentID);
            buttonList.add(ibView);
        }

        enableButtons(true);
        requestLayout();

        // here is where button images start...

        if (nextActivities == null) {


            // fill in with Math, Reading, Lit icons
            CAskElement lit = mDataSource.items[0];
            ImageButton litView = (ImageButton) findViewById(getResources().getIdentifier(lit.componentID, "id", packageName));

            //litView.setImageResource(R.drawable); // button_stories_select

            CAskElement stories = mDataSource.items[1];
            CAskElement math = mDataSource.items[2];


        } else {

            Log.wtf("NEW_MENU", nextActivities[0].tutor_id + " " + nextActivities[1].tutor_id);// + " " + nextActivities[2].tutor_id);

            // This is a pain...
            TCONST.Thumb[] thumbs = new TCONST.Thumb[nextActivities.length];
            String[] icons = new String[nextActivities.length];

            // first two (or three) buttons are set...
            for (int i = 0; i < nextActivities.length; i++) {
                CAskElement element = mDataSource.items[i];
                CImageButton ibView = (CImageButton) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                boolean useOldWay = false;
                // get the correct file name
                String tutorIcon = CTutorData_Metadata.getThumbName(nextActivities[i]); // SUPER_PLACEMENT if the same, do two different icons
                icons[i] = tutorIcon;
                if (tutorIcon == null) {
                    useOldWay = true;
                } else {
                    try {
                        // NEW_THUMBS (3) continue here...
                        ImageLoader.makeBitmapLoader(TCONST.ROBOTUTOR_ASSETS + "/" + TCONST.ICON_ASSETS + "/")
                                .loadBitmap(tutorIcon)
                                .into(ibView);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        useOldWay = true;
                        icons[i] = null;
                    }
                }

                // the old way does it a different way...
                if (useOldWay) {
                    TCONST.Thumb resource = CTutorData_Metadata.getThumbImage(nextActivities[i]); // NEW_THUMBS (0) home screen
                    Log.wtf("NEW_MENU", resource.toString());

                    thumbs[i] = resource;

                    ibView.setImageResource(CDebugComponent.getThumbId(resource)); // NEW_THUMBS (2) setImageResource
                }

            }

            /// check for duplicates
            if (thumbs[0] != null && thumbs[0] == thumbs[1]) {

                CAskElement element = mDataSource.items[1];
                CImageButton ibView = (CImageButton) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                switch (thumbs[0]) {
                    case BPOP_NUM:
                        ibView.setImageResource(R.drawable.thumb_bpop_num_2);
                        break;

                    case BPOP_LTR:
                        ibView.setImageResource(R.drawable.thumb_bpop_ltr_lc_2);
                        break;
                }

            }
        }
    }

    private void releaseReferences() {

        // Note that if you switch at very high speed it is possible to get here before the datasource exists.
        //
        if(mDataSource != null) {

            for (CAskElement element : mDataSource.items) {

                int test = getResources().getIdentifier(element.componentID, "id", packageName);

                ImageButton ibView = (ImageButton) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                ibView.setImageDrawable(null);
            }
        }
    }



    public void enableButtons(boolean enable) {

        for(View view : buttonList) {
            view.setOnTouchListener(enable? this:null);
            view.setClickable(enable);
        }
    }



    /**
     *
     * @param view
     */
    private void startTouch(View view) {

        Log.v(QGRAPH_MSG, "event.click: " + " CAskComponent: button selected");

        if(SINGLE_SELECT)
            enableButtons(false);

        // Support direct connection to button action manager
        //
        if(mButtonController != null) {
            mButtonController.doAskButtonAction(buttonMap.get(view));
        }

        // Also support indirect connection to button action manager
        //
        Intent msg = new Intent(TCONST.ASK_SELECTION);
        msg.putExtra(TCONST.ASK_BUTTON_ID, buttonMap.get(view));

        bManager.sendBroadcast(msg);
    }


    /**
     * Update the glyph path if motion is greater than tolerance - remove jitter
     *
     * @param x
     * @param y
     */
    private void moveTouch(float x, float y) {
    }


    /**
     * End the current glyph path
     * TODO: Manage debouncing
     *
     */
    private void endTouch(float x, float y) {
    }


    public boolean onTouch(View view, MotionEvent event) {
        PointF     p;
        boolean    result = false;
        long       delta;
        final int  action = event.getAction();

        // inhibit input while the recognizer is thinking
        //
        result = true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                startTouch(view);
                // https://stackoverflow.com/questions/5975168/android-button-setpressed-after-onclick
                view.setPressed(true); // TAP_BUTTON here we go!

                result = true;
                break;

            case MotionEvent.ACTION_MOVE:

                result = true;
                break;

            case MotionEvent.ACTION_UP:
                result = true;
                break;
        }

        return result;
    }





    //************ Serialization





    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
    }
}
