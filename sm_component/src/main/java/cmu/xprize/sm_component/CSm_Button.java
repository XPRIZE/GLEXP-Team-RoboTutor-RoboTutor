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

package cmu.xprize.sm_component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class CSm_Button extends Button implements ILauncherButton, View.OnClickListener {

    private Context                     mContext;
    private ILaunchListener             mComponent;
    private CSm_Data                    mData;


    public CSm_Button(Context context, ILaunchListener component) {
        super(context);
        init(context, null);

        mComponent = component;
        setOnClickListener(this);
    }

    public CSm_Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSm_Button(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs ) {
        mContext = context;
    }


    public void buildInterface(CSm_Data buttonData) {

        mData = buttonData;

        // Otherwise it ends up forced to uppercase
        setTransformationMethod(null);

        setText(mData.buttonvalue);
        //int id = mContext.getResources().getIdentifier(Button.tutorname, "layout", mContext.getPackageName());


    }

    /**
     * Tell the component to initialize the scope variables for the scripting engine.
     * i.e. this is so you can launch a tutor from an animationGraph using scoped variables
     * that appear as display object fields
     * see: CTutorEngine.Launch
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        mComponent.setTutorIntent(mData.intent, mData.intentdata, mData.datasource, mData.features);
    }
}
