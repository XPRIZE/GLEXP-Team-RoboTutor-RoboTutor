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

package cmu.xprize.robotutor.tutorengine.graph;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TFloat;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;


// TODO: enhance logging

public class type_cond extends type_action implements ILoadableObject2 {

    // json loadable fields
    public String   type;     // used by loader to disambiguate object type

    public String   test;
    public String   If;
    public String   Then;
    public String   Else;

    private  int    _i1;      // Condition parse point

    static private final String TAG = "Iff";


    /**
     * Simple Constructor
     */
    public type_cond() { }


    /**
     *  Apply node is invoked when a condition is present in an Action or Module map
     *  within the JSON spec
     */
    @Override
    public String applyNode() {

        // If the feature test passes then fire the event.
        // Otherwise set flag to indicate event was completed/skipped in this case
        // Issue #58 - Make all actions feature reactive.
        //
        if(testFeatures()) {

            evaluate(false);
        }

        return TCONST.DONE;
    }

    public boolean evaluateThenElse(String ThenElse, boolean inverse) {

        boolean result = false;

        switch(ThenElse.toUpperCase()) {
            case TCONST.FALSE:
                result = false;
                break;

            case TCONST.TRUE:
                result = true;
                break;

            default:
                result = applyScript(Then, inverse);
                break;
        }

        return inverse? !result:result;
    }

    public Object evaluate(boolean inverse) {

        boolean result = false;

        try {
            if (test != null) {
                result = _scope.tutor().testFeatureSet(test);

            } else if (If != null) {
                result = Iff(If.trim(), TCONST.STARTSTATE, 0);

                if(result) {
                    result = evaluateThenElse(Then, inverse);
                }
                else {
                    result = evaluateThenElse(Else, inverse);
                }
            }
        }

        // catch Iff exceptions
        catch(Exception e) {
            CErrorManager.logEvent(TAG,"Constraint Format Error: ", e, false);
        }

        return result;
    }


    private boolean applyScript(String script, boolean inverse) {
        IScriptable2 obj    = null;
        boolean      result = false;

        try {
            // If we see '{' we assume a json spec for an action object and
            // generate an anonymous object to execute
            if (script.charAt(0) == '{') {
                type_action nobj = new type_action();

                nobj.loadJSON(new JSONObject(script), getScope());
                result = Boolean.parseBoolean(nobj.applyNode());
            }
            // Otherwise we assume that it is a single Symbol
            else {
                if (obj == null)
                    obj = (IScriptable2)getScope().mapSymbol(script);

                result = Boolean.parseBoolean(obj.applyNode());
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "IFF Script error: " + script, e, false);
        }

        return inverse? !result:result;
    }


    /**
     * This is a simple condition statement parser -
     *
     * @param code
     * @param state
     * @param nest
     * @return
     */
    private boolean Iff(String code, int state, int nest) throws Exception {

        StringBuilder  Symbol    = new StringBuilder();
        StringBuilder  Property  = new StringBuilder();
        boolean        IsVar     = false;
        String         parseStr  = code + TCONST.EOT;
        boolean        DecimalPt = false;

        IScriptable2   LHS       = null;            // Left hand side of binary expression
        IScriptable2   RHS       = null;            // Right hand side of binary expression
        boolean        LNegate   = false;
        boolean        RNegate   = false;
        boolean        negate    = false;
        int            binaryOp  = TCONST.NOOP;

        IScriptable2  resultObj = null;
        boolean       result    = false;

        try {
            do {
                switch (state) {
                    //
                    case TCONST.STARTSTATE:
                        state = TCONST.PARSESTATE;
                        _i1 = 0;
                        break;

                    case TCONST.PARSESTATE:
                        char tChar = parseStr.charAt(_i1);

                        switch (tChar) {
                            case TCONST.EOT:
                                state = TCONST.ENDSUBEXPR;
                                break;

                            case '\'':
                                // parse string expression
                                _i1++;
                                state = TCONST.PARSESTRING;
                                break;


                            case '(':
                                // parse subexpression
                                _i1++;
                                resultObj = new TBoolean(Iff(parseStr, TCONST.PARSESTATE, nest + 1));
                                state = TCONST.BUILDEXPR;
                                break;

                            case ')':

                                if (nest == 0)
                                    throw (new Exception("Nesting Error in Expression"));

                                // [Sub]expression complete
                                if (LHS != null) {
                                    _i1++;
                                    state = TCONST.ENDSUBEXPR;
                                    break;
                                } else throw (new Exception("Empty SubExpression (...)"));

                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                            case '\f':
                                _i1++;
                                break;

                            case '!':
                                if (negate) {
                                    CErrorManager.logEvent(TAG,  "Unexpected '!' at: " + _i1 + " in " + code, null, false);
                                }

                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '=') {
                                    _i1++;
                                    binaryOp = TCONST.NEQUALTO;
                                } else
                                    negate = true;
                                break;

                            case '{':
                                _i1++;
                                if (parseStr.charAt(_i1) == '{') {
                                    IsVar = true;
                                    _i1++;
                                    state = TCONST.PARSEIDENT;
                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '{' at: " + _i1 + " in " + code, null, false);
                                }
                                break;

                            case '&':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '&') {
                                    _i1++;
                                    binaryOp = TCONST.BOOLAND;
                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '&' at: " + _i1 + " in " + code, null, false);
                                }
                                break;

                            case '|':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '|') {
                                    _i1++;
                                    binaryOp = TCONST.BOOLOR;

                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '|' at: " + _i1 + " in " + code, null, false);
                                }
                                break;

                            case '=':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '=') {
                                    _i1++;
                                    binaryOp = TCONST.EQUALTO;

                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '=' at: " + _i1 + " in " + code, null, false);
                                }
                                break;

                            case '<':
                                _i1++;
                                if (LHS != null) {
                                    if (parseStr.charAt(_i1) == '=') {
                                        _i1++;
                                        binaryOp = TCONST.LESSOREQUAL;
                                    } else {
                                        binaryOp = TCONST.LESSTHAN;
                                    }
                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '<' at: " + _i1 + " in " + code, null, false);
                                }
                                break;

                            case '>':
                                _i1++;
                                if (LHS != null) {
                                    if (parseStr.charAt(_i1) == '=') {
                                        _i1++;
                                        binaryOp = TCONST.GREATEROREQUAL;
                                    } else {
                                        binaryOp = TCONST.GREATERTHAN;
                                    }
                                } else {
                                    CErrorManager.logEvent(TAG,   "Unexpected '>' at: " + _i1 + " in " + code, null, false);
                                }
                                break;


                            default:
                                if((tChar >= '0' && tChar <= '9') ||
                                   (tChar == '-'))  {

                                    Symbol.append(tChar);
                                    _i1++;
                                    state = TCONST.PARSENUM;
                                }

                                else if((tChar >= 'A' && tChar <= 'Z') ||
                                        (tChar >= 'a' && tChar <= 'z') ||
                                        (tChar == '_')) {
                                    Symbol.append(tChar);
                                    _i1++;
                                    state = TCONST.PARSEIDENT;
                                }

                        }
                        break;

                    case TCONST.PARSESTRING:
                        tChar = parseStr.charAt(_i1);

                        switch (tChar) {
                            case '\'':
                                // parse string expression
                                _i1++;
                                resultObj = new TString(Symbol.toString());

                                state = TCONST.BUILDEXPR;
                                Log.d(_logType, "String Literal Found: " + Symbol);
                                break;

                            default:
                                _i1++;
                                Symbol.append(tChar);
                                break;
                        }
                        break;

                    case TCONST.PARSENUM:
                        tChar = parseStr.charAt(_i1);

                        if(tChar == '.') {
                            if(DecimalPt) {
                                CErrorManager.logEvent(TAG,   "Unexpected '.' at: " + _i1 + " in " + code, null, false);
                            }

                            Symbol.append(tChar);
                            _i1++;
                            DecimalPt = true;
                        }
                        else if(tChar >= '0' && tChar <= '9') {

                            Symbol.append(tChar);
                            _i1++;

                        } else {

                            // Create approriate literal type
                            if(DecimalPt) {
                                resultObj = new TFloat(Symbol.toString());
                            }
                            else {
                                resultObj = new TInteger(Symbol.toString());
                            }

                            state = TCONST.BUILDEXPR;
                            Log.d(_logType, "Literal Found: " + Symbol);
                        }
                        break;

                    case TCONST.PARSEIDENT:
                        tChar = parseStr.charAt(_i1);

                        // Not currently used.  All properties are intended to be exposed as
                        // fully qualified variables in the Scope - e.g "Sstimulus.digit" is the
                        // name of a preoperty - we don't actually access fields of Java
                        // objects directly
                        //
//                        if(tChar ==  '.') {
//                            _i1++;
//                            state = TCONST.PARSEPROP;
//                            continue;
//                        }
//                        else
                        if ((tChar >= 'A' && tChar <= 'Z') ||
                                (tChar >= 'a' && tChar <= 'z') ||
                                (tChar >= '0' && tChar <= '9') ||
                                (tChar == '_')                 ||
                                (tChar == '.')) {
                            Symbol.append(tChar);
                            _i1++;
                        } else {
                            if (IsVar) {
                                state = TCONST.PARSEVAR;
                            } else {
                                state = TCONST.RESOLVESYMBOL;
                            }
                        }

                        break;

                    // Parse tbe ...}} off the end of a variable
                    //TODO: bug if Object has field like Sstimulus.digit
                    case TCONST.PARSEVAR:
                        tChar = parseStr.charAt(_i1);

                        switch (tChar) {
//                            case '.':
//                                _i1++;
//                                state = TCONST.PARSEPROP;
//                                continue;

                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                            case '\f':
                                _i1++;
                                continue;

                            case '}':
                                if (parseStr.charAt(_i1 + 1) == '}') {
                                    _i1+=2;
                                    state = TCONST.RESOLVESYMBOL;
                                }
                                break;

                            case TCONST.EOT:

                            default:
                                throw (new Exception("Missing '}}' in expression at: " + _i1));
                        }
                        break;


                    case TCONST.PARSEPROP:
                        tChar = parseStr.charAt(_i1);

                        if(tChar ==  '.') {
                            CErrorManager.logEvent(TAG, "IFF invalid expression: " + code, null, false);
                        }
                        else if ((tChar >= 'A' && tChar <= 'Z') ||
                                (tChar >= 'a' && tChar <= 'z') ||
                                (tChar >= '0' && tChar <= '9') ||
                                (tChar == '_')) {
                            Property.append(tChar);
                            _i1++;
                        } else {
                            if (IsVar) {
                                state = TCONST.PARSEVAR;
                            } else {
                                state = TCONST.PARSEIDENT;
                            }
                        }
                        break;


                    case TCONST.RESOLVESYMBOL:

                        resultObj = (IScriptable2)getScope().mapSymbol(Symbol.toString());

                        if(Property.length() != 0) {
                            //resultObj = resultObj.mapProperty(Property.toString());
                        }

                        state = TCONST.BUILDEXPR;
                        Log.d(_logType, "Symbol Found: " + Symbol);
                        break;


                    // This is only entered when there is a new Symbol available in resultObj;
                    // So
                    case TCONST.BUILDEXPR:

                        Symbol   = new StringBuilder();
                        Property = new StringBuilder();

                        if (LHS == null) {
                            LHS = resultObj;
                            LNegate = negate;
                        } else {

                            if(binaryOp == TCONST.NOOP) {
                              CErrorManager.logEvent(TAG, "Invalid Expression missing Operator: " + code, null, false);
                            }
                            RHS = resultObj;
                            RNegate = negate;
                        }

                        // Iff there is a binary op in effect than we have received the RHS of an
                        // expression.

                        if (binaryOp != TCONST.NOOP) {
                            Log.d(_logType, "Evaluating RHS: " + RHS.getType());

                            // Evaluate the result and assign it to LHS to allow chaining.
                            // i.e. A && B && C  - we evaluate left to right i.e. after
                            // evaluating LHS(A) && RHS(B) the becomes LHS(A&&B).
                            //
                            switch (binaryOp) {
                                case TCONST.BOOLOR:
                                    LHS = LHS.OR(RHS, LNegate, RNegate);
                                    break;
                                case TCONST.BOOLAND:
                                    LHS = LHS.AND(RHS, LNegate, RNegate);
                                    break;
                                case TCONST.LESSTHAN:
                                    LHS = LHS.LT(RHS);
                                    break;
                                case TCONST.LESSOREQUAL:
                                    LHS = LHS.LTEQ(RHS);
                                    break;
                                case TCONST.GREATERTHAN:
                                    LHS = LHS.GT(RHS);
                                    break;
                                case TCONST.GREATEROREQUAL:
                                    LHS = LHS.GTEQ(RHS);
                                    break;
                                case TCONST.EQUALTO:
                                    LHS =LHS.EQ(RHS);
                                    break;
                                case TCONST.NEQUALTO:
                                    LHS =LHS.NEQ(RHS);
                                    break;
                            }

                            result = (boolean) LHS.evaluate(LNegate);

                            // reset the expression state

                            negate = false;
                            LNegate = false;
                            RNegate = false;
                            RHS = null;
                            binaryOp = TCONST.NOOP;
                        }
                        else {
                            Log.d(_logType, "LHS Type : " + LHS.getType());
                            Log.d(_logType, "LHS Value: " + result);
                        }

                        // Check for end of input - otherwise reset state
                        if (parseStr.charAt(_i1) == TCONST.EOT) {
                            state = TCONST.ENDSUBEXPR;
                        }
                        else {
                            // Move to next char in parse source
                            state = TCONST.PARSESTATE;
                        }
                        break;

                    default:
                        break;
                }

            } while (_i1 < parseStr.length() && state != TCONST.ENDSUBEXPR);
        }
        catch(Exception e) {
            // TODO: Manage Syntax Errors
            CErrorManager.logEvent(TAG, "Invalid Expression: " + code, null, false);
        }

        // Do terminal evaluation - must be boolean outcome
        try {
            result = (boolean) LHS.evaluate(LNegate);

            // Only record the final result not the subexpressions.
            //
            if(nest == 0) {
                RoboTutor.logManager.postEvent_V(_logType, "target:node.type_cond.iff,comment:(sub)Expression evaluates to,result:" + result + ",code: " + code);
            }

        } catch (Exception e) {

            CErrorManager.logEvent(TAG,"Value does not evaluate to boolean in expression: " + code, null, false);
        }

        return result;
    }


}
