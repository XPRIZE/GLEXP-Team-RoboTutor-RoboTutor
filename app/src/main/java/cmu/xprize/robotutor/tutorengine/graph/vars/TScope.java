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

import android.util.Log;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.CSceneGraph;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IScriptable;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.GRAPH_MSG;


public class TScope implements IScope2 {

    // There is one toplevel scope created by tutor_node
    //
    private static TScope rootScope = null;

    private CTutor                        mTutor;

    private HashMap<String, IScriptable2> map;
    private HashMap<String, TScope>       scopes = null;
    private TScope                        parent = null;
    private String                        name;

    static private final String TAG = "TScope";


    public TScope(CTutor ltutor, String scopeName, TScope Parent) {

        mTutor = ltutor;
        name   = scopeName;
        map    = new HashMap<>();
        scopes = new HashMap<>();

        if(Parent != null) {
            parent = Parent;
            parent.addChild(name, this);
        }

        // Initialize the rootscope - scopes are hierarchical

        if(rootScope == null)
                rootScope = this;
    }

    /**
     * This method is designed to give the "Activity_selector" tutor access to the score counted by the game tutors,
     * which is used for performance based promotion.
     * REVIEW there may be a better way to do this
     * @param name
     * @return
     */
    public TScope getChildScope(String name) {

        TScope child = scopes.get(name);

        if(child != null) {
            return child;
        } else {
            return null;
        }
    }

    @Override
    public CTutor tutor() {
        return mTutor;
    }

    @Override
    public ITutorGraph tutorGraph() {
        return mTutor.getTutorGraph();
    }

    @Override
    public CSceneGraph sceneGraph() {
        return mTutor.getSceneGraph();
    }

    @Override
    public String tutorName() {
        return mTutor.getTutorName();
    }


    static public TScope root() {
        return rootScope;
    }


    public TScope getParentScope() {
        return parent;
    }


    public void addChild(String key, TScope scope) {
        scopes.put(key, scope);
    }

    
    /**
     * This permits external modules to call back into the tutor scriptable architecture
     *
     * @param key
     * @param obj
     */
    public void put(String key, IScriptable obj) {
        put(key, (IScriptable2) obj);
    }


    public void put(String key, IScriptable2 obj) {

        if(map.containsKey(key)) {
            CErrorManager.logEvent(TAG, "Duplicate Key : " + key, false);
        }
        map.put(key,obj);
    }


    public void addUpdateVar(String key, IScriptable2 obj) {

        if(obj == null)
            map.remove(key);
        else
            map.put(key,obj);
    }


    /**
     * Scan the string for variables - {{symbol_name}}  and replace them in the result with
     * the value of the symbol.
     *
     * @param source
     * @return
     */
    public String parseTemplate(String source) {

        int    _i1    = 0;

        int            state     = TCONST.PARSESTATE;
        StringBuilder  Symbol    = new StringBuilder();
        StringBuilder  result    = new StringBuilder();
        String         parseStr  = source + TCONST.EOT;

        boolean        isArray   = false;
        IScriptable2   arrayObj  = null;
        IScriptable2   resultObj = null;

        try {
            do {
                char tChar = parseStr.charAt(_i1);

                switch (state) {
                    case TCONST.PARSESTATE:
                        switch (tChar) {
                            case TCONST.EOT:
                                state = TCONST.ENDSUBEXPR;
                                break;

                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                            case '\f':
                                _i1++;
                                result.append(tChar);
                                break;

                            case '{':
                                _i1++;
                                if (parseStr.charAt(_i1) == '{') {
                                    _i1++;
                                    state = TCONST.PARSEIDENT;
                                } else {
                                    // It is an isolated { add it to the output and continue
                                    result.append(tChar);
                                }
                                break;

                            default:
                                _i1++;
                                result.append(tChar);
                                break;
                        }
                        break;

                    case TCONST.PARSEIDENT:

                        if ((tChar >= 'A' && tChar <= 'Z') ||
                            (tChar >= 'a' && tChar <= 'z') ||
                            (tChar >= '0' && tChar <= '9') ||
                            (tChar == '.') || (tChar == '_')) {

                            Symbol.append(tChar);
                            _i1++;
                        }

                        // If the symbol references an array object we remember the symbol name and
                        // continue parsing the input as a new symbol which may be either
                        // another variable or a number representing the array index
                        //
                        else if (tChar == '[') {

                            isArray  = true;
                            arrayObj = mapSymbol(Symbol.toString());
                            Symbol   = new StringBuilder();

                            _i1++;
                        }

                        // If we find a closing bracket we process the array dereference request
                        //
                        else if (tChar == ']') {
                            if(!isArray) {
                                CErrorManager.logEvent(TAG, "No open bracket [ found for array reference: " + Symbol + "> in expression" + source, null, false);
                            }

                            _i1++;
                            state = TCONST.PARSEVAR;
                        }
                        else {
                            state = TCONST.PARSEVAR;
                        }

                        break;

                    // Parse tbe ...}} off the end of a variable
                    //
                    case TCONST.PARSEVAR:

                        switch (tChar) {
                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                            case '\f':
                                _i1++;
                                continue;

                            case '}':
                                if (parseStr.charAt(_i1 + 1) == '}') {
                                    _i1++;

                                    // If not an array the Symbol is expected to be a scope variable which
                                    // we dereference and append its string value to the output.
                                    //
                                    if(!isArray) {
                                        resultObj = mapSymbol(Symbol.toString());

                                        if (resultObj == null) {
                                            CErrorManager.logEvent(TAG, "Parser Symbol not found: <" + Symbol + "> in expression " + source, null, false);
                                        }

                                        result.append(resultObj.toString());
                                    }

                                    // If it is an array the arrayObj is a type_array object that resolves
                                    // a java object and an indexed datasource within that object. Symbol is
                                    // either a number or a scope variable representing a numeric index
                                    //
                                    else {
                                        int index;

                                        try {
                                            index = Integer.parseInt(Symbol.toString());
                                        }
                                        catch(Exception e) {
                                            resultObj = mapSymbol(Symbol.toString());

                                            index = resultObj.getIntValue();
                                        }

                                        result.append(arrayObj.resolve(index));
                                    }

                                    // switch back to parsing string
                                    _i1++;
                                    state = TCONST.PARSESTATE;
                                    Log.v(GRAPH_MSG, "tscope.parseTemplate: Symbol Found: " + Symbol);
                                }
                                break;

                            case TCONST.EOT:

                            default:
                                // TODO: Manage Syntax Errors
                                CErrorManager.logEvent(GRAPH_MSG, "Missing '}}' in expression: " + source, null, false);

                        }
                        break;

                    default:
                        break;
                }

            } while (_i1 < parseStr.length() && state != TCONST.ENDSUBEXPR);
        }
        catch(Exception e) {
            // TODO: Manage Syntax Errors
            CErrorManager.logEvent(GRAPH_MSG, "tscope.parseTemplate: Invalid Expression: " + source, e, false);
        }

        return result.toString();
    }


    // Look up the inheritance chain to find the object
    //
    public IScriptable2 mapSymbol(String name) throws  Exception {
        IScriptable2 tarObject = null;
        TScope currScope = this;

        if (!name.equals("")) {
            try {
                // Walk up the scope chain to try and find the named object
                do {
                    tarObject = currScope.map.get(name);

                    if (tarObject == null)
                        currScope = currScope.getParentScope();

                } while (tarObject == null);

                while (tarObject.getType() == TCONST.TREFERENCE) do {
                    tarObject = currScope.map.get(tarObject.getValue());

                    if (tarObject == null)
                        currScope = currScope.getParentScope();

                } while (tarObject == null);

            } catch (Exception e) {

                //TODO : Manage symbol not found
                // NOTE: this is not reliably being flushed to LogCat
                //CErrorManager.logEvent(TAG, "Symbol not found : " + name + " Exception: " , e, false);
                //System.out.printf( "TScope: Symbol not found : %s Exception:\n" , name);

                // Don't exit
                Log.e(TAG, "Scope Symbol not found : " + name + " Exception: " + e);
            }
        }

        return tarObject;
    }

    // Look up the inheritance chain to find the object
    //
    public boolean containsSymbol(String name) throws  Exception {
        IScriptable2 tarObject = null;
        TScope       currScope = this;

        if(!name.equals("")) {
            try {
                // Walk up the scope chain to try and find the named object
                do {
                    tarObject = currScope.map.get(name);

                    if(tarObject == null)
                        currScope = currScope.getParentScope();

                } while (tarObject == null);

            } catch (Exception e) {
                Log.i(TAG, "Symbol not contained : " + name);
            }
        }

        return tarObject != null? true:false;
    }

}