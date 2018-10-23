package cmu.xprize.comp_bigmath;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IPerformanceTracker;
import cmu.xprize.util.IPublisher;

import static cmu.xprize.comp_bigmath.BM_CONST.ALL_DIGITS;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_CORRECT;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_IS_BORROW;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_IS_CARRY;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_PROBLEM_DONE;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_PROBLEM_HAS_MORE;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_WRONG;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_CARRY_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.NEXTNODE;
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
    private final IBehaviorManager _behaviorManager;
    private final IPublisher _publisher;
    private final IPerformanceTracker _performance;

    // testing vars
    private int operandA;
    private int operandB;
    private String operator;
    private int _expectedResult;
    private boolean _isCarryOne;
    private boolean _isCarryTen;

    private int _numDigits;


    // need to track when we do carry
    private boolean _hasWrittenOnesResult;
    private boolean _hasCarriedToTens;

    private boolean _hasWrittenTensResult;
    private boolean _hasCarriedToHuns;

    private boolean _isBorrowTen;
    private boolean _isBorrowHun;
    private boolean _isDoubleBorrow; // cascade borrow from hundreds place to ones place

    public StudentActionListenerImpl(IBehaviorManager _behaviorManager, IPublisher publisher, BigMathMechanic bigMath, IPerformanceTracker performance) {
        this._behaviorManager = _behaviorManager;
        this._bigMath = bigMath;
        this._publisher = publisher;
        this._performance = performance;
    }

    @Override
    public void setData(CBigMath_Data data, int numDigits) {
        operandA = data.dataset[0];
        operandB = data.dataset[1];
        _expectedResult = data.dataset[2];
        operator = data.operation;
        _numDigits = numDigits;

        // retract carry and borrow features...
        _publisher.retractFeature(FTR_IS_CARRY);
        _publisher.retractFeature(FTR_IS_BORROW);
        if (operator.equals("+")) setCarryLogic(operandA, operandB);
        else setBorrowLogic(operandA, operandB);

    }

    /**
     * =========================
     * ===== carry logic =======
     * =========================
     *
     * @param a
     * @param b
     */
    private void setCarryLogic(int a, int b) {

        // if a_one + b_one > 9, we must carry to tens column
        _isCarryOne = getOnesDigit(a) + getOnesDigit(b) > 9;
        if (_isCarryOne) {
            _publisher.publishFeature(FTR_IS_CARRY);
        }

        if (_numDigits < 3) return; // only works for hundreds...
        // if (carried_ten) + a_one + b_one > 9, we must carry to the huns column
        _isCarryTen = (_isCarryOne ? 1 : 0) + getTensDigit(a) + getTensDigit(b) > 9;

    }

    /**
     * =========================
     * ===== borrow logic ======
     * =========================
     * @param a
     * @param b
     */
    private void setBorrowLogic(int a, int b) {

        // if a_one < b_one, we need to borrow a ten from a_ten
        _isBorrowTen = getOnesDigit(a) < getOnesDigit(b);
        if (_isBorrowTen) {
            _publisher.publishFeature(FTR_IS_BORROW);
        }

        if (_numDigits < 3) return; // save this for later, when we do 3 digits
        // if a_ten - (borrowed_ten) < b_ten, we need to borrow a hundred from a_hun
        _isBorrowHun = getTensDigit(a) - (_isBorrowTen? 1 : 0) < getTensDigit(b);

        // if we need to borrow a ten from a_ten but there are no tens, then we need to borrow from a_hun
        _isDoubleBorrow = _isBorrowTen && getTensDigit(a) == 0;
    }

    @Override
    public void fireAction(String selection, String action, String input) {

        Log.wtf("RECEIVED SAI", String.format(Locale.US, "%s - %s - %s", selection, action, input));
        //
        // do the thing
        if (action.equals("WRITE")) {

            //_behaviorManager.applyBehaviorNode(NEXTNODE);

            // there must be a better way to organize these...
            int expectedInput;
            switch(selection) {


                // digit names are the selection
                case "symbol_result_hun":

                    // NONE OF THIS IS RIGHT.... IT ONLY HANDLES TWO DIGITS AT THE MOMENT
                    expectedInput = getHunsDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {
                        _performance.trackAndLogPerformance(true, String.valueOf(expectedInput), input);
                        Log.i("YAY", "DONE WITH PROBLEM");
                        _bigMath.markDigitCorrect(HUN_DIGIT);
                        // _publisher.publishFeature(FTR_CORRECT);
                        _bigMath.highlightDigitColumn(ALL_DIGITS);

                        if (operator.equals("-")) {
                            _bigMath.moveMinuendToResult();
                        }

                    } else {
                        _performance.trackAndLogPerformance(false, String.valueOf(expectedInput), input);
                        _bigMath.markDigitWrong(HUN_DIGIT);
                        // _publisher.publishFeature(FTR_WRONG);
                    }

                    break;

                case "symbol_result_ten":
                    expectedInput = getTensDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {

                        _performance.trackAndLogPerformance(true, String.valueOf(expectedInput), input);
                        _bigMath.markDigitCorrect(TEN_DIGIT);
                        _publisher.retractFeature(FTR_WRONG);
                        _publisher.publishFeature(FTR_CORRECT);


                        if(_isCarryTen) {
                            _hasWrittenTensResult = true;
                            if(!_hasCarriedToTens) {
                                break;
                            }
                        }
                        if (_numDigits == 2) {
                            _publisher.retractFeature(FTR_PROBLEM_HAS_MORE);
                            _publisher.publishFeature(FTR_PROBLEM_DONE);
                        } else {
                            _bigMath._currentDigit = HUN_DIGIT;
                            _publisher.retractFeature(FTR_PROBLEM_DONE);
                            _publisher.publishFeature(FTR_PROBLEM_HAS_MORE);
                        }

                        // ----------
                        // next digit
                        // ----------

//                        _bigMath.highlightDigitColumn(HUN_DIGIT);
//                        _bigMath.disableConcreteUnitTappingForOtherRows(HUN_DIGIT);
                        // ----------
                        // ---end----
                        // ----------

                    } else {

                        _performance.trackAndLogPerformance(false, String.valueOf(expectedInput), input);
                        _bigMath.markDigitWrong(TEN_DIGIT);
                        _publisher.retractFeature(FTR_CORRECT);
                        _publisher.publishFeature(FTR_WRONG);
                    }

                    // move to next node
                    Log.wtf("SEPTEMBER", "applying behavior node");
                    _behaviorManager.applyBehaviorNode(NEXTNODE);
                    break;

                case "symbol_result_one":
                    expectedInput = getOnesDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {

                        _performance.trackAndLogPerformance(true, String.valueOf(expectedInput), input);
                        _bigMath.markDigitCorrect(ONE_DIGIT);
                        _publisher.retractFeature(FTR_WRONG);
                        _publisher.publishFeature(FTR_CORRECT);
                        Log.wtf("SEPTEMBER", "publishing correct");


                        if (_numDigits == 1) {
                            _publisher.retractFeature(FTR_PROBLEM_HAS_MORE);
                            _publisher.publishFeature(FTR_PROBLEM_DONE);
                        } else {


                            // ROBO_MATH MATH_AG move this logic to animator graph carry check should be moved here...

                            _bigMath._currentDigit = TEN_DIGIT;
                            _publisher.retractFeature(FTR_PROBLEM_DONE);
                            _publisher.publishFeature(FTR_PROBLEM_HAS_MORE);
                        }

                        // ROBO_MATH MATH_AG move to go with logic
                        if(_isCarryOne) {

                            _hasWrittenOnesResult = true;
                            // can't go to next digit unless we've written carry
                            // MATH_AG this should go inside "NEXT_DIGIT"
                            _bigMath.showCarryTen();
                            if(!_hasCarriedToTens) {
                                break;
                            }
                        }

                        // ----------
                        // next digit
                        // ----------
                        //_bigMath.highlightDigitColumn(TEN_DIGIT);
                        //_bigMath.disableConcreteUnitTappingForOtherRows(TEN_DIGIT);

                        if(_isCarryTen) {
                            // show next carry box
                            _bigMath.showCarryHun();
                        }
                        // ----------
                        // ---end----
                        // ----------

                    } else {
                        _performance.trackAndLogPerformance(false, String.valueOf(expectedInput), input);
                        _bigMath.markDigitWrong(ONE_DIGIT);
                        _publisher.retractFeature(FTR_CORRECT);
                        _publisher.publishFeature(FTR_WRONG);
                        Log.wtf("SEPTEMBER", "publishing wrong");
                    }

                    // move to next node
                    Log.wtf("SEPTEMBER", "applying behavior node");
                    _behaviorManager.applyBehaviorNode(NEXTNODE);
                    break;




                case "symbol_carry_ten":

                    // ROBO_MATH NEXT NEXT NEXT, there should be some behavior here
                    // ROBO_MATH NEXT NEXT NEXT, they should be written in any order (also, this is optional)
                    expectedInput = _isCarryOne ? 1 : 0; //  the zero won't get used...

                    if(input.equals(String.valueOf(expectedInput))) {

                        _performance.trackAndLogPerformance(true, String.valueOf(expectedInput), input);
                        // give some sort of correct feedback???
                        _bigMath.markDigitCorrect(TEN_CARRY_DIGIT);
                        // _publisher.retractFeature(FTR_WRONG);
                        // _publisher.publishFeature(FTR_CORRECT);

                        _hasCarriedToTens = true;
                        // ROBO_MATH (xx) both of these must be done
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
                        _performance.trackAndLogPerformance(false, String.valueOf(expectedInput), input);
                        _bigMath.markDigitWrong(TEN_CARRY_DIGIT);
                    }

                    break;

                case "symbol_carry_hun":

                    expectedInput = _isCarryTen ? 1 : 0; // the zero won't get used...

                    if(input.equals(String.valueOf(expectedInput))) {

                        _performance.trackAndLogPerformance(true, String.valueOf(expectedInput), input);
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
                        _performance.trackAndLogPerformance(false, String.valueOf(expectedInput), input);
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

    //oct 18 start
    public int getExpectedInt(String selection) {

        int expectedInput = 0; //oct18 comment expected input might not be set somewhere(please check)
        switch (selection) {

            // digit names are the selection
            case "symbol_result_hun":
                expectedInput = getHunsDigit(_expectedResult);
                break;

            case "symbol_result_ten":
                expectedInput = getTensDigit(_expectedResult);
                break;

            case "symbol_result_one":
                expectedInput = getOnesDigit(_expectedResult);
                break;

            case "symbol_carry_ten":
                expectedInput = _isCarryOne ? 1 : 0; //  the zero won't get used..
                break;

            case "symbol_carry_hun":

                expectedInput = _isCarryTen ? 1 : 0; // the zero won't get used...
                break;

            case "symbol_borrow_one_1":
                expectedInput = 1; // it's always one, that's the most we can borrow
                break;

            case "symbol_borrow_one_2":
                expectedInput = getOnesDigit(operandA);
                break;

            case "symbol_borrow_one":
                // maybe make all feedback come through me???
                break;

            case "symbol_borrow_ten":
                expectedInput = getTensDigit(operandA) - 1;
                break;
        }

        return expectedInput;
    }
    //end

    int _borrowState;
    /**
     * for setting the tutor state...
     * ROBO_MATH here is where mystery borrow actions happens! ... now to convert it to RoboTutor
     */
    private void _setTutorState(final String state) {

        if (state.equals("waiting_for_borrow")) {
            switch (_borrowState) {
                case -1:
                    _bigMath.borrowTen();
                    break;

                case 0:
                    _playAudio("Now we have one less TEN");
                    _bigMath.strikeThroughTenBorrow();
                    break;

                case 1:
                    _playAudio("HERE, write how many TENS are left");
                    _bigMath.showBorrowDigitHolder();
                    break;

                case 2:
                    _playAudio("Good!");
                    _bigMath.writeNewTenBorrowedValue(getTensDigit(operandA) - 1);
                    break;

                case 3:
                    _playAudio(String.format(Locale.US, "10 and %d makes %d. Write %d",
                            getOnesDigit(operandA), getOnesDigit(operandA) + 10, getOnesDigit(operandA) + 10));
                    _bigMath.strikeThroughOneBorrow();
                    break;

                case 4:
                    _playAudio("Good!");
                    _bigMath.populateOneWithBorrowedTen(getOnesDigit(operandA) + 10);
                    break;

                case 5:
                    _playAudio(String.format(Locale.US, "Now we *can* take away %d from %d",
                            getOnesDigit(operandB), getOnesDigit(operandA) + 10));
                    // enable tapping
                    break;

                case 6:
                    // student taps these
                    break;
            }

            _borrowState++;
        }

    }

    /**
     * a temporary helper to mock the playing of audio
     * @param audio
     */
    private void _playAudio(String audio) {
        Log.i("FAKE_AUDIO", audio);
    }
}
