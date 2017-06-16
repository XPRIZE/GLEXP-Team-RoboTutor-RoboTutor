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

import android.view.ViewGroup;

import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;

public interface ITutorScene extends ITutorObject {

    public ViewGroup getOwner();

    public String preEnterScene(scene_descriptor scene, String Direction );
    public void onEnterScene();
    public String preExitScene(String Direction, int sceneCurr );
    public void onExitScene();
}
