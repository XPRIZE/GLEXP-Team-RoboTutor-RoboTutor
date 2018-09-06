package cmu.xprize.comp_bigmath;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IPublisher;

import static cmu.xprize.comp_bigmath.BM_CONST.ALL_DIGITS;
import static cmu.xprize.comp_bigmath.BM_CONST.FEATURES.FTR_CORRECT;
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

    // testing vars
    private int operandA;
    private int operandB;
    private String operator;
    private int _expectedResult;
    private boolean _isCarryOne;
    private boolean _isCarryTen;


    // need to track when we do carry
    private boolean _hasWrittenOnesResult;
    private boolean _hasCarriedToTens;

    private boolean _hasWrittenTensResult;
    private boolean _hasCarriedToHuns;

    private boolean _isBorrowTen;
    private boolean _isBorrowHun;
    private boolean _isDoubleBorrow; // cascade borrow from hundreds place to ones place

    public StudentActionListenerImpl(IBehaviorManager _behaviorManager, IPublisher publisher, BigMathMechanic bigMath) {
        this._behaviorManager = _behaviorManager;
        this._bigMath = bigMath;
        this._publisher = publisher;
    }

    @Override
    public void setData(CBigMath_Data data) {
        operandA = data.dataset[0];
        operandB = data.dataset[1];
        _expectedResult = data.dataset[2];
        operator = data.operation;

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

            // ROBO_MATH NEXT NEXT NEXT
            //_behaviorManager.applyBehaviorNode(NEXTNODE);

            // there must be a better way to organize these...
            int expectedInput;
            switch(selection) {



                // ROBO_MATH here are the reactions to student input
                // ROBO_MATH... these should be replaced with "WAIT" on the action side, and "advanceWithinAnimatorGraph" on this side
                // ROBO_MATH... move this ish to animator graph
                // digit names are the selection
                case "symbol_result_hun":
                    // ROBO_MATH NEXT NEXT NEXT this should be moved into nextDigit
                    expectedInput = getHunsDigit(_expectedResult);

                    if (input.equals(String.valueOf(expectedInput))) {
                        Log.i("YAY", "DONE WITH PROBLEM");
                        _bigMath.markDigitCorrect(HUN_DIGIT);
                        // _publisher.publishFeature(FTR_CORRECT);
                        _bigMath.highlightDigitColumn(ALL_DIGITS);

                        if (operator.equals("-")) {
                            _bigMath.moveMinuendToResult();
                        }

                    } else {
                        _bigMath.markDigitWrong(HUN_DIGIT);
                        // _publisher.publishFeature(FTR_WRONG);
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
                        _publisher.publishFeature(FTR_CORRECT);
                        Log.wtf("SEPTEMBER", "publishing correct");

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
                        _bigMath.markDigitWrong(ONE_DIGIT);
                        _publisher.publishFeature(FTR_WRONG);
                        Log.wtf("SEPTEMBER", "publishing wrong");
                    }

                    // ROBO_MATH... see if this works!
                    Log.wtf("SEPTEMBER", "applying behavior node");
                    _behaviorManager.applyBehaviorNode(NEXTNODE);
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
