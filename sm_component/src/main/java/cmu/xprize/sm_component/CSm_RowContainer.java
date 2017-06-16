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

package cmu.xprize.sm_component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import cmu.xprize.util.TCONST;

public class CSm_RowContainer extends HorizontalScrollView {

    private Context                 mContext;
    private LinearLayout            mContainer;
    private CSm_Data[]              mButtons;

    static final String TAG = "CSm_RowContainer";


    public CSm_RowContainer(Context context) {
        super(context);
        init(context, null);
    }

    public CSm_RowContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSm_RowContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs ) {

        inflate(getContext(), R.layout.smrow_layout, this);

        mContext   = context;
        mContainer = (LinearLayout)findViewById(R.id.SrowContainer);
    }


    public void buildInterface(CSm_Class rowData, ILaunchListener component, String defColor) {

        String color = defColor;

        ILauncherButton newButton = null;

        mContainer.removeAllViews();
        mButtons = rowData.data;

        if(rowData.background != null) {
            color = rowData.background;
        }

        setBackgroundColor(Color.parseColor(color));
        mContainer.setBackgroundColor(Color.parseColor(color));

        for(CSm_Data Button: mButtons) {

            Log.d(TAG, "Creating Activity Button : " + Button.intent);

            switch(Button.buttontype) {
                case "text":
                    newButton = new CSm_Button(mContext, component);
                    break;

                case "image":
                    newButton = new CSm_ImageButton(mContext, component);
                    break;
            }

            newButton.buildInterface(Button);

            mContainer.addView((View)newButton);
        }
    }

}
