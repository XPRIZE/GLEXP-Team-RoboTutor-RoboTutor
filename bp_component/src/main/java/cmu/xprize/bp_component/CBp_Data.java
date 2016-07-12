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

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CBp_Data implements ILoadableObject{

    // json loadable
    public String        question_type    = "MC";
    public boolean       question_say     = false;
    public boolean       question_show    = false;
    public boolean       response_hear    = false;

    public int[]         response_count   = null;
    public String        response_layout  = "fixed";
    public String[]      response_color   = null;

    public String        stimulus_type    = "reference";
    public int           stimulus_index   = -1;

    public int[]         dataset          = null;



    //************ Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
