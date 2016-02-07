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

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ILoadableObject;
import cmu.xprize.robotutor.tutorengine.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.vars.TFloat;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.util.JSON_Helper;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;


public class type_cond extends type_action implements ILoadableObject {

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

        evaluate(false);

        return TCONST.DONE;
    }


    public Object evaluate(boolean inverse) {

        boolean result = false;

        try {
            if (test != null) {
                result = CTutor.testFeatureSet(test);

            } else if (If != null) {
                result = Iff(If.trim(), TCONST.STARTSTATE, 0);

                if(result) {
                    result = applyScript(Then, inverse);
                }
                else {
                    result = applyScript(Else, inverse);
                }
            }
        }

        // catch Iff exceptions
        catch(Exception e) {
            // TODO: manage Iff errors
        }

        return result;
    }


    private boolean applyScript(String script, boolean inverse) {
        IScriptable obj    = null;
        boolean     result = false;

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
                    obj = getScope().mapSymbol(script);

                result = Boolean.parseBoolean(obj.applyNode());
            }
        }
        catch(Exception e) {
            Log.e(TAG, "IFF Script error: " + script);
            System.exit(1);
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

        IScriptable LHS          = null;            // Left hand side of binary expression
        IScriptable RHS          = null;            // Right hand side of binary expression
        boolean        LNegate   = false;
        boolean        RNegate   = false;
        boolean        negate    = false;
        int            binaryOp  = TCONST.NOOP;

        IScriptable resultObj = null;
        boolean     result    = false;

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
                                    Log.e(TAG, "Unexpected '!' at: " + _i1 + " in " + code);
                                    System.exit(1);
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
                                    Log.e(TAG, "Unexpected '{' at: " + _i1 + " in " + code);
                                    System.exit(1);
                                }
                                break;

                            case '&':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '&') {
                                    _i1++;
                                    binaryOp = TCONST.BOOLAND;
                                } else {
                                    Log.e(TAG, "Unexpected '&' at: " + _i1 + " in " + code);
                                    System.exit(1);
                                }
                                break;

                            case '|':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '|') {
                                    _i1++;
                                    binaryOp = TCONST.BOOLOR;
                                } else {
                                    Log.e(TAG, "Unexpected '|' at: " + _i1 + " in " + code);
                                    System.exit(1);
                                }
                                break;

                            case '=':
                                _i1++;
                                if (LHS != null && parseStr.charAt(_i1) == '=') {
                                    _i1++;
                                    binaryOp = TCONST.EQUALTO;
                                } else {
                                    Log.e(TAG, "Unexpected '=' at: " + _i1 + " in " + code);
                                    System.exit(1);
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
                                    Log.e(TAG, "Unexpected '<' at: " + _i1 + " in " + code);
                                    System.exit(1);
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
                                    Log.e(TAG, "Unexpected '>' at: " + _i1 + " in " + code);
                                    System.exit(1);
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

                    case TCONST.PARSENUM:
                        tChar = parseStr.charAt(_i1);

                        if(tChar == '.') {
                            if(DecimalPt) {
                                Log.e(TAG, "Unexpected '.' at: " + _i1 + " - in : " + code);
                                System.exit(1);
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
                            Log.i(TAG, "Literal Found: " + Symbol);
                        }
                        break;

                    case TCONST.PARSEIDENT:
                        tChar = parseStr.charAt(_i1);

                        if(tChar ==  '.') {
                            _i1++;
                            state = TCONST.PARSEPROP;
                            continue;
                        }
                        else if ((tChar >= 'A' && tChar <= 'Z') ||
                                (tChar >= 'a' && tChar <= 'z') ||
                                (tChar >= '0' && tChar <= '9') ||
                                (tChar == '_')) {
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
                    case TCONST.PARSEVAR:
                        tChar = parseStr.charAt(_i1);

                        switch (tChar) {
                            case '.':
                                _i1++;
                                state = TCONST.PARSEPROP;
                                continue;

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
                            Log.e(TAG, "IFF invalid expression: " + code);
                            System.exit(1);
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

                        resultObj = getScope().mapSymbol(Symbol.toString());

                        if(Property.length() != 0) {
                            //resultObj = resultObj.mapProperty(Property.toString());
                        }

                        state = TCONST.BUILDEXPR;
                        Log.i(TAG, "Symbol Found: " + Symbol);
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
                              Log.e(TAG, "Invalid Expression missing Operator: " + code);
                              System.exit(1);
                            }
                            RHS = resultObj;
                            RNegate = negate;
                        }

                        // Iff there is a binary op in effect than we have received the RHS of an
                        // expression.

                        if (binaryOp != TCONST.NOOP) {
                            Log.i(TAG, "Evaluating RHS: " + RHS.getType());

                            // Evaluate the result and assign it to LHS to allow chaining.
                            // i.e. A && B && C  - we evaluate left to right i.e. after
                            // evaluating LHS(A) && RHS(B) the becomes LHS(A&&B).
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
                            Log.i(TAG, "LHS Type : " + LHS.getType());
                            Log.i(TAG, "LHS Value: " + result);
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
            Log.e(TAG, "Invalid Expression: " + code);
            System.exit(1);
        }

        // Do terminal evaluation - must be boolean outcome
        try {
            result = (boolean) LHS.evaluate(LNegate);

            Log.i(TAG, "(sub)Expression evaluates: " + result + "  :  " + code);

        } catch (Exception e) {
            Log.e(TAG, "Value does not evaluate to boolean in expression: " + code);
            System.exit(1);
        }

        return result;
    }

}
