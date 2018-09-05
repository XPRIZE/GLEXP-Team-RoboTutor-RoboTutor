package cmu.xprize.comp_bigmath;

import android.util.Log;

import java.util.Locale;

import static cmu.xprize.comp_bigmath.BM_CONST.ALL_DIGITS;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_CARRY_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.ONE_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.TEN_CARRY_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.TEN_DIGIT;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/5/18.
 */

public class StudentActionListenerImpl implements StudentActionListener{

    private final BigMathMechanic _bigMath;
    // testing vars
    int operandA = 21;
    int operandB = 17;
    String operator = "-";
    int _expectedResult;
    boolean _isCarryOne;
    boolean _isCarryTen;


    // need to track when we do carry
    private boolean _hasWrittenOnesResult;
    private boolean _hasCarriedToTens;

    private boolean _hasWrittenTensResult;
    private boolean _hasCarriedToHuns;

    boolean _isBorrowTen;
    boolean _isBorrowHun;
    boolean _isDoubleBorrow; // cascade borrow from hundreds place to ones place

    public StudentActionListenerImpl(BigMathMechanic bigMath) {
        this._bigMath = bigMath;
    }

    @Override
    public void fireAction(String selection, String action, String input) {

        Log.wtf("RECEIVED SAI", String.format(Locale.US, "%s - %s - %s", selection, action, input));
        //
        // do the thing
        if (action.equals("WRITE")) {

            // there must be a better way to organize these...
            int expectedInput;
            switch(selection) {

                // ROBO_MATH here are the reactions to student input
                // digit names are the selection
                case "symbol_result_hun":
                    expectedInput = getHunsDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {
                        Log.i("YAY", "DONE WITH PROBLEM");
                        _bigMath.markDigitCorrect(HUN_DIGIT);
                        _bigMath.highlightDigitColumn(ALL_DIGITS);

                        if (operator.equals("-")) {
                            _bigMath.moveMinuendToResult();
                        }

                    } else {
                        _bigMath.markDigitWrong(HUN_DIGIT);
                    }

                    break;

                case "symbol_result_ten":
                    expectedInput = getTensDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {

                        _bigMath.markDigitCorrect(TEN_DIGIT);

                        if(_isCarryTen) {
                            _hasWrittenTensResult = true;
                            if(!_hasCarriedToTens) {
                                break;
                            }
                        }

                        // ----------
                        // next digit
                        // ----------
                        _bigMath.highlightDigitColumn(HUN_DIGIT);
                        _bigMath.disableConcreteUnitTappingForOtherRows(HUN_DIGIT);
                        // ----------
                        // ---end----
                        // ----------

                    } else {
                        _bigMath.markDigitWrong(TEN_DIGIT);
                    }
                    break;

                case "symbol_result_one":
                    expectedInput = getOnesDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {

                        _bigMath.markDigitCorrect(ONE_DIGIT);

                        if(_isCarryOne) {

                            _hasWrittenOnesResult = true;
                            // can't go to next digit unless we've written carry
                            if(!_hasCarriedToTens) {
                                break;
                            }
                        }

                        // ----------
                        // next digit
                        // ----------
                        _bigMath.highlightDigitColumn(TEN_DIGIT);
                        _bigMath.disableConcreteUnitTappingForOtherRows(TEN_DIGIT);

                        if(_isCarryTen) {
                            // show next carry box
                            _bigMath.showCarryHun();
                        }
                        // ----------
                        // ---end----
                        // ----------

                    } else {
                        _bigMath.markDigitWrong(ONE_DIGIT);
                    }
                    break;




                case "symbol_carry_ten":

                    expectedInput = _isCarryOne ? 1 : 0; //  the zero won't get used...

                    if(input.equals(String.valueOf(expectedInput))) {

                        _bigMath.markDigitCorrect(TEN_CARRY_DIGIT);

                        _hasCarriedToTens = true;
                        if (_hasWrittenOnesResult) {

                            // ----------
                            // next digit
                            // ----------
                            _bigMath.highlightDigitColumn(TEN_DIGIT);
                            _bigMath.disableConcreteUnitTappingForOtherRows(TEN_DIGIT);

                            if(_isCarryTen) {
                                // show next carry box
                                _bigMath.showCarryHun();
                            }
                            // ----------
                            // ---end----
                            // ----------
                        }


                    } else {
                        _bigMath.markDigitWrong(TEN_CARRY_DIGIT);
                    }

                    break;

                case "symbol_carry_hun":

                    expectedInput = _isCarryTen ? 1 : 0; // the zero won't get used...

                    if(input.equals(String.valueOf(expectedInput))) {

                        _bigMath.markDigitCorrect(HUN_CARRY_DIGIT);

                        _hasCarriedToHuns = true;
                        if (_hasWrittenTensResult) {
                            // ----------
                            // next digit
                            // ----------
                            _bigMath.highlightDigitColumn(HUN_DIGIT);
                            _bigMath.disableConcreteUnitTappingForOtherRows(HUN_DIGIT);
                            // ----------
                            // ---end----
                            // ----------
                        }
                    } else {
                        _bigMath.markDigitWrong(HUN_CARRY_DIGIT);
                    }
                    break;

                // MATH_BEHAVIOR (1) advance this along the "animator graph"!!!
                case "symbol_borrow_one_1":
                    expectedInput = 1; // it's always one, that's the most we can borrow
                    Log.d("MATHFIX_WRITE_4", "received input " + input);
                    break;

                case "symbol_borrow_one_2":
                    expectedInput = getOnesDigit(operandA); // will be the ones digit
                    Log.d("MATHFIX_WRITE_4", "received input " + input);
                    break;

                // MATH_FEEDBACK (5) do something here???
                case "symbol_borrow_one":
                    // maybe make all feedback come through me???

                    break;

                case "symbol_borrow_ten":
                    expectedInput = getTensDigit(operandA) - 1; // tens digit minus one
                    Log.d("MATHFIX_WRITE_4", "received input " + input + "; expected " + expectedInput);
                    break;

            }
        }

    }
}
