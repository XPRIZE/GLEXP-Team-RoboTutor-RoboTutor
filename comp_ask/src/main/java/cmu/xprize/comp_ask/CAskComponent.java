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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CAskComponent extends FrameLayout implements ILoadableObject, View.OnClickListener  {

    protected Context               mContext;
    protected String                packageName;
    protected HashMap<View,String>  buttonMap;

    protected IButtonController     mButtonController;

    // json loadable
    public CAsk_Data            dataSource;

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
    }


    public void onDestroy() {

    }


    public void setmButtonController(IButtonController controller) {

        mButtonController = controller;
    }

    //**********************************************************
    //**********************************************************
    //*****************  DataSink Interface


    public boolean dataExhausted() {
        return true;
    }


    public void setDataSource(CAsk_Data dataSource) {

        int layoutID = getResources().getIdentifier(dataSource.layoutID, "layout", packageName);

        removeAllViews();

        ViewGroup gView = (ViewGroup)inflate(mContext, layoutID, this);

        // Populate the layout elements
        //
        buttonMap = new HashMap<>();

        for(CAskElement element : dataSource.items) {

            switch(element.datatype) {
                case ASK_CONST.IMAGE:
                    ImageView iView = (ImageView) gView.findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    iView.setImageResource(getResources().getIdentifier(element.resource, "drawable", packageName));
                    break;

                case ASK_CONST.TEXT:
                    TextView tView = (TextView) gView.findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    tView.setText(element.resource);
                    break;

                case ASK_CONST.IMAGEBUTTON:
                    int test = getResources().getIdentifier(element.componentID, "id", packageName);

                    ImageButton ibView = (ImageButton) gView.findViewById(getResources().getIdentifier(element.componentID, "id", packageName));

                    ibView.setImageResource(getResources().getIdentifier(element.resource, "drawable", packageName));

                    buttonMap.put(ibView, element.componentID);
                    break;

                case ASK_CONST.TEXTBUTTON:
//                    TextButton tbView = (TextView) gView.findViewById(getResources().getIdentifier(element.componentID, "id", packageName));
//
//                    tbView.setText(element.resource);
//
//                    buttonMap.put(tbView, element.componentID);
                    break;
            }
        }

        gView.requestLayout();
    }


    public void setDataSource(String[] dataSource) {}



    @Override
    public void onClick(View view) {

        mButtonController.doButtonAction(buttonMap.get(view));
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
