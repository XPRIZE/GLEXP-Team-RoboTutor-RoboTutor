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

package cmu.xprize.comp_questions;


import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CASB_Narration implements ILoadableObject {

    // json loadable
    public String    audio;
    public int       from;
    public int       until;
    public String    utterances;
    public CASB_Seg segmentation[];

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }

}
