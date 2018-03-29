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

package cmu.xprize.bp_component;

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CBp_Data implements ILoadableObject {

    // json loadable
    public String        question_type    = "MC";           // MC (MultipleChoice) or RISEing bubbles
    public boolean       question_say     = false;          // Narrate stimulus (question) value
    public boolean       question_show    = false;          // show stimulus (question)

    public boolean       gen_question     = true;           // Are questions generated or pre-defined
    public int           answer_index     = -1;             // Where answer is placed in response_set (neg -> random)

    public String[]      response_set     = null;           // Values for this iteration
    public String[]      responsetype_set = null;           // types for mixed response sets
    public String[][]    response_script  = null;           // List of uttereances describing the individual responses

    public int           respCountExact   = 0;              // Number of bubbles to present 0 == gen # in CountRange
    public int[]         respCountRange   = {5,5};          // Number of bubbles to present in response set

    public String        stimulus         = null;           // question value
    public String        stimulus_type    = null;           // question type - reference / text_data
    public String []     stimulus_script  = null;           // List of uttereances describing the question

    public String        answer;                            // must match unique value in response set
    public String        answer_type      = null;           // answer type - reference / text_data
    public String []     answer_script    = null;           // List of uttereances describing the answer

    public String        comp_start_end   = null;
    public String        comp_with_like   = null;

    //************ Serialization

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
