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

package cmu.xprize.comp_ask;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.IButtonController;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


public class CAskComponent extends FrameLayout implements ILoadableObject, View.OnClickListener  {

    protected Context               mContext;
    protected String                packageName;
    protected HashMap<View,String>  buttonMap;
    protected ArrayList<View>       buttonList;

    protected IButtonController     mButtonController;

    static private boolean          SINGLE_SELECT = false;

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


    public void onDestroy() {

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


    public void setDataSource(CAsk_Data dataSource) {

        int layoutID = getResources().getIdentifier(dataSource.layoutID, "layout", packageName);

        removeAllViews();

        // Inflate the layout into "this" AskComponent
        //
        inflate(mContext, layoutID, this);

        // Populate the layout elements
        //
        buttonMap  = new HashMap<>();
        buttonList = new ArrayList<>();

        for(CAskElement element : dataSource.items) {

            switch(element.datatype) {
                case ASK_CONST.IMAGE:
                    ImageView iView = (ImageView) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    iView.setImageResource(getResources().getIdentifier(element.resource, "drawable", packageName));
                    break;

                case ASK_CONST.TEXT:
                    TextView tView = (TextView) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    tView.setText(element.resource);
                    break;

                case ASK_CONST.IMAGEBUTTON:
                    int test = getResources().getIdentifier(element.componentID, "id", packageName);

                    ImageButton ibView = (ImageButton) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    ibView.setImageResource(getResources().getIdentifier(element.resource, "drawable", packageName));

                    buttonMap.put(ibView, element.componentID);
                    buttonList.add(ibView);
                    break;

                case ASK_CONST.TEXTBUTTON:
                    Button tbView = (Button) findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    tbView.setText(element.resource);

                    buttonMap.put(tbView, element.componentID);
                    buttonList.add(tbView);
                    break;
            }
        }

        enableButtons(true);
        requestLayout();
    }


    @Override
    public void onClick(View view) {

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


    public void enableButtons(boolean enable) {

        for(View view : buttonList) {
            view.setOnClickListener(enable? this:null);
            view.setClickable(enable);

        }
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
