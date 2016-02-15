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

package cmu.xprize.robotutor.tutorengine.graph.vars;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.robotutor.tutorengine.graph.IScriptable;

public class TInteger extends TVarBase implements ILoadableObject, IScriptable {

    private int _value;

    public TInteger() {

    }

    public TInteger(String string) {
        _value = Integer.parseInt(string);
    }

    @Override
    public void set(String value) {
        _value = Integer.parseInt(value);
    }

    @Override
    public void add(String value) {
        _value += Integer.parseInt(value);
    }

    @Override
    public void subtract(String value) {
        _value -= Integer.parseInt(value);
    }


    @Override
    public Object evaluate(boolean negate) {
        return _value;
    }


    @Override
    public Object getValue() { return _value; }


    @Override
    public String toString() {

        return String.valueOf(_value);
    };



    // *** Serialization


    /**
     * As TVar objects are loaded from JSON the TVarBase initializes the type and name
     * while the values are set per class polymorphically
     *
     * @param jsonObj
     * @param scope
     */
    public void loadJSON(JSONObject jsonObj, TScope scope) {

        super.loadJSON(jsonObj, scope);

        try {
            _value = jsonObj.getInt("value");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
