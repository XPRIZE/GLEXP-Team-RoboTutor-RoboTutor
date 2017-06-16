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

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.util.IEventSource;

public interface ITutorGraph {

    public void onDestroy();

    public void terminateQueue();

    public void post(IEventSource source, String command);

    public void setSceneGraph(CSceneGraph sGraph);

    public CSceneGraph getSceneGraph();

    public void setDefDataSource(defdata_tutor dataSources);

    public HashMap getChildMap();

    public HashMap getChildMapByName(String sceneName);

    public String gotoNextScene(boolean push);

}
