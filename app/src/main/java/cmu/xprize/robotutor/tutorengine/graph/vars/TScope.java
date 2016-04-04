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

import android.util.Log;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.util.IScriptable;
import cmu.xprize.util.TCONST;


public class TScope implements IScope2 {

    // There is one toplevel scope created by scene_animator
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


    @Override
    public CTutor tutor() {
        return mTutor;
    }

    @Override
    public ITutorNavigator graph() {
        return mTutor.getTutorGraph();
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
            Log.e(TAG, "Duplicate Key : " + key);
            System.exit(1);
        }
        map.put(key,obj);
    }

    public void addUpdate(String key, IScriptable2 obj) {
        map.put(key,obj);
    }


    /**
     * Scan the string for variables - {{symbol_name}}  and replace them in the result with
     * the value of the symbol.
     *
     * @param source
     * @return
     */
    public String resolveTemplate(String source) {

        int    _i1    = 0;

        int            state     = TCONST.PARSESTATE;
        StringBuilder  Symbol    = new StringBuilder();
        StringBuilder  result    = new StringBuilder();
        String         parseStr  = source + TCONST.EOT;

        IScriptable2    resultObj = null;

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
                        } else {
                            state = TCONST.PARSEVAR;
                        }

                        break;

                    // Parse tbe ...}} off the end of a variable
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
                                    resultObj = mapSymbol(Symbol.toString());

                                    if (resultObj == null) {
                                        Log.e(TAG, "Symbol not found: <" + Symbol + "> in expression" + source);
                                        System.exit(1);
                                    }

                                    result.append(resultObj.toString());

                                    // switch back to parsing string
                                    _i1++;
                                    state = TCONST.PARSESTATE;
                                    Log.i(TAG, "Symbol Found: " + Symbol);
                                }
                                break;

                            case TCONST.EOT:

                            default:
                                // TODO: Manage Syntax Errors
                                Log.e(TAG, "Missing '}}' in expression: " + source);
                                System.exit(1);

                        }
                        break;

                    default:
                        break;
                }

            } while (_i1 < parseStr.length() && state != TCONST.ENDSUBEXPR);
        }
        catch(Exception e) {
            // TODO: Manage Syntax Errors
            Log.e(TAG, "Invalid Expression: " + source);
            System.exit(1);
        }

        return result.toString();
    }


    // Look up the inheritance chain to find the object
    //
    public IScriptable2 mapSymbol(String name) throws  Exception {
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

                while(tarObject.getType() == TCONST.TREFERENCE)  do {
                    tarObject = currScope.map.get(tarObject.getValue());

                    if(tarObject == null)
                        currScope = currScope.getParentScope();

                } while (tarObject == null);

            } catch (Exception e) {
                //TODO : Manage symbol not found
                Log.e(TAG, "Symbol not found : " + name);
                System.exit(1);
            }
        }

        return tarObject;
    }
}