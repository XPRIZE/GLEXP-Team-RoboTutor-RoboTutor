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

package cmu.xprize.robotutor.tutorengine.graph.vars;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.type_action;

// TVarBase is the mechanism through which Iscriptable is imposed on built-in types
//
public class TVarBase extends type_action implements ILoadableObject2, IScriptable2 {

    private TScope _scope;


    /**
     * Apply the type_action - it can be of various types:
     *
     */
    @Override
    public String applyNode() {

        _scope.put(name, this);

        return TCONST.DONE;
    }


    @Override
    public String getName() {
        return name;
    }


    /**
     * Note that we depend upon the runtime to take care of type mismatch and boolean operations
     * on non-boolean types - i.e. we can't negate a string or number
     *
     * @param RHS
     * @param lneg
     * @param rneg
     * @return
     */
    @Override
    public TBoolean OR(IScriptable2 RHS, boolean lneg, boolean rneg) {

        Object lLHS =  getValue();
        Object lRHS =  RHS.getValue();

        return new TBoolean((Boolean)(lneg? !(Boolean)lLHS : lLHS) || (Boolean)(lneg? !(Boolean)lRHS : lRHS));
    }

    @Override
    public TBoolean AND(IScriptable2 RHS, boolean lneg, boolean rneg) {

        Object lLHS =  getValue();
        Object lRHS =  RHS.getValue();

        return new TBoolean((Boolean)(lneg? !(Boolean)lLHS : lLHS) && (Boolean)(lneg? !(Boolean)lRHS : lRHS));    }

    @Override
    public TBoolean LT(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean LTEQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean GT(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean GTEQ(IScriptable2 RHS) {
        return null;
    }

    @Override
    public TBoolean EQ(IScriptable2 RHS) {
        return new TBoolean(getValue() == RHS.getValue());
    }

    @Override
    public TBoolean NEQ(IScriptable2 RHS) {
        return new TBoolean(getValue() != RHS.getValue());
    }

    @Override
    public String resolve(int index) {
        return null;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public void set(String value) {

    }

    @Override
    public void add(String value) {

    }

    @Override
    public void subtract(String value) {

    }

    @Override
    public Object evaluate(boolean neg) {
        return false;
    }

    @Override
    public String toString() {
      return null;
    };



    // *** Serialization


    // Note that we don't use the JSonHelper for these objects
    // since they know their own types and can apply them without
    // using reflection.
    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        // Record the scope the variable should be created in if it is instantiated
        _scope = (TScope)scope;

        try {
            type = jsonObj.getString("type");

            if(jsonObj.has("name"))
                 name = jsonObj.getString("name");

            if(jsonObj.has("features"))
                features = jsonObj.getString("features");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
