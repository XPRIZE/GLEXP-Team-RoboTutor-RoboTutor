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

package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.comp_logging.ILogManager;

public interface ITutorObject {

    public void init(Context context, AttributeSet attrs);
    public void onCreate();
    public void onDestroy();

    public void setVisibility(String visible);
    public void setName(String name);

    public String name();

    public void setParent(ITutorSceneImpl mParent);
    public void setTutor(CTutor tutor);
    public void setNavigator(ITutorGraph navigator);
    public void setLogManager(ILogManager logManager);

}
