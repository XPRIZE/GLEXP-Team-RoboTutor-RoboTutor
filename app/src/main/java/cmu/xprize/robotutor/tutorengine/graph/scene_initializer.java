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

package cmu.xprize.robotutor.tutorengine.graph;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;

public class scene_initializer implements ILoadableObject {

    // json loadable
    public String          id;

    public scene_view[]    scene_layout;
    public type_action[]  oncreate;
    public type_action[]  onenter;
    public String          features;

    public void loadJSON(JSONObject jsonObj, TScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, scope);
    }
}
