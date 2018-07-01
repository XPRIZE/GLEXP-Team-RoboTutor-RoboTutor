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


public class CData_Index implements ILoadableObject{

//    The set name becomes the Story_Index filename e.g.  rtasset_stories_sw_set1.json

//    E:\Projects\GitHUB\RTAsset_Publisher\rtasset_stories_sw_set1.1.1.0\assets\story\sw\level1\level1_1
//    E:\Projects\GitHUB\RTAsset_Publisher\rtasset_stories_sw_set1.1.1.0\assets\audio\sw\cmu\xprize\story_reading\level1

//    "storyName":"Ajali mbaya", "storyFolder": "Level1_1", "levelFolder":"1", "viewtype":"ASB_Data"}

    // json loadable
    public String       storyName;
    public String       levelFolder;  // Note: The associated audio resources are assumed to reside in ...assets\audio\sw\cmu\xprize\story_reading\<levelFolder>
    public String       storyFolder;
    public String       viewtype;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
