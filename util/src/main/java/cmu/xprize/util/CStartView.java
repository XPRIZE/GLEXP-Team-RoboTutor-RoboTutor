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

package cmu.xprize.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import cmu.xprize.common.R;


public class CStartView extends FrameLayout {

    private ImageButton  start;
    private IRoboTutor   callback;

    public CStartView(Context context) {
        super(context);
        init(context, null);
    }

    public CStartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CStartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
    }


    public void setCallback(IRoboTutor _callback) {

        callback = _callback;

        start = (ImageButton)findViewById(R.id.SstartSelector);

        // Allow hits anywhere on screen
        //
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callback.onStartTutor();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callback.onStartTutor();
            }
        });
    }
}
