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

package cmu.xprize.bp_component;

import android.content.Context;
import android.graphics.Canvas;

public class CBp_Mechanic_RISE implements IBubbleMechanic {

    private Context         mContext;
    private CBP_Component   mParent;
    private boolean         mInitialized = false;

    private CBubble[]       mBubbles;

    static final String TAG = "CBp_Mechanic_RISE";


    public CBp_Mechanic_RISE(Context context, CBP_Component parent) {

        mContext = context;
        mParent  = parent;
    }


    @Override
    public void onDraw(Canvas canvas) {

    }


    @Override
    public boolean isInitialized() {
        return mInitialized;
    }


    @Override
    public void onDestroy() {

        for(CBubble bubble : mBubbles) {
            mParent.removeView(bubble);
        }
    }

    @Override
    public void startAnimation() {

    }

    @Override
    public void populateView(CBp_Data data) {

    }

    @Override
    public void doLayout(int width, int height, CBp_Data data) {

    }

}
