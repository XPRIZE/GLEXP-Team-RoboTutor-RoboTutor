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

package cmu.xprize.robotutor.tutorengine;

import java.util.HashMap;

public interface ITutorNavigator {

    public void initTutorContainer(ITutorSceneImpl rootScene);
    public void onDestroy();

    public CSceneGraph getAnimator();
    public HashMap getChildMap();
    public HashMap getChildMapByName(String sceneName);

    public void questionStart();
    public void questionComplete();
    public void goBackScene();
    public void goNextScene();
    public void goToNamedScene(String name);

    public void goToScene(String tarScene);

    public String gotoNextScene(boolean push);

    public void onNextScene();
}
