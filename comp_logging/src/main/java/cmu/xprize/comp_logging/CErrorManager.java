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

package cmu.xprize.comp_logging;

import android.util.Log;

import cmu.xprize.comp_logging.ILogManager;

/**
 * This was added with Android Studio 2.1 and PIXEL C -- simple logEvent could cause the logcat
 * to miss the error message
 *
 */
public class CErrorManager {

    static ILogManager mLogManager;

    static public void setLogManager(ILogManager manager) {
        mLogManager = manager;
    }


    static public void logEvent(String TAG, String Msg, boolean printTrace) {

        Log.e(TAG, Msg);

        try {
            Thread.sleep(400);
        } catch (InterruptedException e1) {
        }

        mLogManager.postError(TAG, Msg);

        // Still cannot exit as it may skip the error message
        //
        //System.exit(1);
    }

    static public void logEvent(String TAG, String Msg, Exception e, boolean printTrace) {

        if(printTrace && e != null)
            e.printStackTrace();

        if(e != null)
            Log.e(TAG, Msg + e);
        else
            Log.e(TAG, Msg);

        try {
            Thread.sleep(600);
        } catch (InterruptedException e1) {
        }

        if(e != null)
            mLogManager.postError(TAG, Msg, e);
        else
            mLogManager.postError(TAG, Msg);

        // Still cannot exit as it may skip the error message
        //
        //System.exit(1);
    }
}
