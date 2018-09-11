package cmu.xprize.comp_bigmath;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cmu.xprize.comp_writebox.CGlyphController_Simple;
import cmu.xprize.comp_writebox.CGlyphInputContainer_Simple;
import cmu.xprize.comp_writebox.IGlyphController_Simple;
import cmu.xprize.comp_writebox.IWritingComponent_Simple;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IPublisher;

import static cmu.xprize.comp_bigmath.BM_CONST.ALL_DIGITS;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_CARRY_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.HUN_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.ONE_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.OPA_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.OPB_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.RESULT_LOCATION;
import static cmu.xprize.comp_bigmath.BM_CONST.TEN_CARRY_DIGIT;
import static cmu.xprize.comp_bigmath.BM_CONST.TEN_DIGIT;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;


/**
 * BigMathComponent
 *
 * <p>
 * Created by kevindeland on 8/6/18.
 */

public class BigMathMechanic {

    private final IBehaviorManager _behaviorManager;
    private final IPublisher _publisher;
    private Context _activity;
    private ViewGroup _viewGroup;
    private BigMathLayoutHelper _layout;
    private StudentActionListener _sai;
    private CBigMath_Data _data;

    // MATH_BEHAVIOR (1) add case for each digit into SAI receiver

    // DATA SOURCE INPUT
    private int operandA = 288; // in subtraction, this is the minuend
    private int operandB = 333; // in subtraction, this is the subtrahend
    private String operator = "+";
    //private int operandA = 478;
    //private int operandB = 386;
    //private String operator = "+";

    public String _currentDigit;


    private int _numDigits;

    //
    boolean EXPAND_HIT_BOX = false; // MATH_FEEDBACK (9) tap within the containing box to add/subtract. Do not tap units. // MATH_TODO fix

    // used to track UI state
    private int currentOpAHun;
    private int currentOpATen;
    private int currentOpAOne;
    private int currentOpBHun;
    private int currentOpBTen;
    private int currentOpBOne;


    private int resultHun = 0;
    private int resultTen = 0;
    private int resultOne = 0;

    // tracking UI for subtraction
    private int subtrahendHun = 0;
    private int subtrahendTen = 0;
    private int subtrahendOne = 0;

    private int minuendHun;
    private int minuendTen;
    private int minuendOne;

    // state values
    private boolean isCarrying = false; // prevent other things from happening during animation

    private boolean isBorrowing = false; // prevent other things from happening during animation
    private boolean hasBorrowedHun = false;
    private boolean hasBorrowedTen = false;


    private static final String BASE_TEN_TAG = "BaseTen";

    public static String APP_PRIVATE_FILES;

    /** hiding these for now
     // carry digit writing
     CGlyphController_Simple _controller_hun_carry;
    CGlyphInputContainer_Simple _inputContainer_hun_carry;
    // you do need these
    CGlyphController_Simple _controller_ten_carry;
    CGlyphInputContainer_Simple _inputContainer_ten_carry;

    // result digit writing
    CGlyphController_Simple _controller_hun;
    CGlyphInputContainer_Simple _inputContainer_hun;
    // you do need these
    CGlyphController_Simple _controller_ten;
    CGlyphInputContainer_Simple _inputContainer_ten;
    // you do need these
    CGlyphController_Simple _controller_one;
    CGlyphInputContainer_Simple _inputContainer_one;
    uncomment in case of design change**/

    // master writing box
    CGlyphController_Simple _controller_master;
    CGlyphInputContainer_Simple _inputContainer_master;

    CGlyphController_Simple _controller_left;
    CGlyphInputContainer_Simple _inputContainer_left;

    private StudentActionListener _studentActionListener;

    public BigMathMechanic(Context activity, IBehaviorManager behaviorManager, IPublisher publisher, ViewGroup viewGroup) {
        _behaviorManager = behaviorManager;
        _publisher = publisher;
        _activity = activity;
        _viewGroup = viewGroup;
        _layout = new BigMathLayoutHelper(activity, viewGroup);

        _studentActionListener = new StudentActionListenerImpl(_behaviorManager, _publisher, this); // won't always work...

    }


    public void setData(CBigMath_Data data) {
        _data = data;

        // max of two digits and sum/difference
        int expected = _data.operation.equals("+") ? _data.dataset[0] + _data.dataset[1] : _data.dataset[0] - _data.dataset[1];
        //_numDigits = Integer.max(String.valueOf(expected).length(),
        // Integer.max(String.valueOf(_data.dataset[0]).length(), String.valueOf(_data.dataset[1]).length()));
        // hacky way to do Integer.max
        _numDigits = String.valueOf(_data.dataset[0]).length();
        if (String.valueOf(_data.dataset[1]).length() > _numDigits)
            _numDigits = String.valueOf(_data.dataset[1]).length();
        if (String.valueOf(_data.dataset[2]).length() > _numDigits)
            _numDigits = String.valueOf(_data.dataset[2]).length();

        _studentActionListener.setData(data, _numDigits);

        _currentDigit = ONE_DIGIT;
    }

    /**
     * Just a temporary placeholder to do all the things.
     * ROBO_MATH Step 1
     */
    void doAllTheThings() {

        // ROBO_MATH √√√ done
        initializeLayout();

        // ROBO_MATH do
        initializeOnClickListeners();

        // ROBO_MATH do
        initializeWriteInputs();

        // ROBO_MATH do for each problem
        resetState();

        Log.wtf("DATA", _data.toString());
        Log.wtf("DATA", _data.operation);
        switch (_data.operation) {
            case "+":
                resetResultsAdd();
                break;

            case "-":
                resetResultsSubtract();
                break;
        }

        initializeNumValues();
        initializeDigitDisplay();
        hideCarryAndBorrowConcrete();
        hideCarryAndBorrowSymbols();

        switch (_data.operation) {
            case "+":
                initializeBaseTensForAddition();
                break;

            case "-":
                initializeBaseTensForSubtraction();
                break;
        }

    }

    private void initializeLayout() {

        int[] layouts = {R.layout.bigmath_1d, R.layout.bigmath_2d, R.layout.bigmath_3d};
        _viewGroup.removeAllViews(); // maybe this?
        View.inflate(_activity, layouts[_numDigits - 1], _viewGroup);
    }
    /**
     * Initialize the OnClick performance of the Views
     * TODO these could be significantly refactored
     */
    private void initializeOnClickListeners() {

        // PART 1: (ONE, TEN, HUN) for (OPA, OPB)
        String[] locations = {OPA_LOCATION, OPB_LOCATION};

        for (String numLoc : locations) {

            // add listeners to Ones
            for (int i=1; i <= 10; i++) {

                MovableImageView oneView = _layout.getBaseTenConcreteUnitView(numLoc, ONE_DIGIT, i);
                oneView.setOnClickListener(new BaseTenOnClickAnimateMe(ONE_DIGIT)); // MATH_FEEDBACK (9) should animate everything instead
            }

            // add listeners to Tens
            if (_numDigits >= 2)
                for (int i=1; i <= 10; i++) {

                    MovableImageView tenView = _layout.getBaseTenConcreteUnitView(numLoc, TEN_DIGIT, i);
                    tenView.setOnClickListener(new BaseTenOnClickAnimateMe(TEN_DIGIT)); // MATH_FEEDBACK (9) should animate everything instead
                }

            // add listeners to Hundreds
            if (_numDigits >=3)
                for (int i=1; i <= 5; i++) {

                    MovableImageView hunView = _layout.getBaseTenConcreteUnitView(numLoc, HUN_DIGIT, i);
                    hunView.setOnClickListener(new BaseTenOnClickAnimateMe(HUN_DIGIT)); // MATH_FEEDBACK (9) should animate everything instead
                }


            // PART 2 (BOX) for (one, ten, hun) will move sequential ones. These may or may not (but probably will) be used.
            _layout.getContainingBox(numLoc, ONE_DIGIT).setOnClickListener(new BaseTenOnClickAnimateSequential(ONE_DIGIT));

            if (_numDigits >= 2)
                _layout.getContainingBox(numLoc, TEN_DIGIT).setOnClickListener(new BaseTenOnClickAnimateSequential(TEN_DIGIT));

            if (_numDigits >= 3)
                _layout.getContainingBox(numLoc, HUN_DIGIT).setOnClickListener(new BaseTenOnClickAnimateSequential(HUN_DIGIT));

        }

        // PART 3... carry
        if (_numDigits >= 2)
            _layout.getCarryConcreteUnitView(TEN_DIGIT).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveTenCarry(v);
                }
            });

        if (_numDigits >= 3)
            _layout.getCarryConcreteUnitView(HUN_DIGIT).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveHunCarry(v);
                }
            });


        // PART 4... borrow
        for (int i = 1; i <= 10; i++) {
            if (_numDigits >= 2)
                _layout.getBorrowConcreteUnitView(ONE_DIGIT, i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveOneBorrow(v);
                    }
                });

            if (_numDigits >= 3)
                _layout.getBorrowConcreteUnitView(TEN_DIGIT, i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveTenBorrow(v);
                    }
                });
        }

    }

    /**
     * Mark and display a digit as wrong
     * @param digit
     */
    public void markDigitWrong(String digit) {
        DigitView view;

        switch(digit) {
            case TEN_CARRY_DIGIT:
                view = (DigitView) _layout.getCarryDigitView(TEN_DIGIT);
                break;

            case HUN_CARRY_DIGIT:
                view = (DigitView) _layout.getCarryDigitView(HUN_DIGIT);
                break;

            default:
                view = (DigitView) _layout.getBaseTenDigitView(RESULT_LOCATION, digit);
                break;
        }

        view.isIncorrect = true;
        view.isCorrect = false;
        view.setTextColor(_activity.getResources().getColor(R.color.incorrectDigit));
    }

    /**
     * Mark and display a digit as correct
     */
    public void markDigitCorrect(String digit) {
        DigitView view;

        switch(digit) {
            case TEN_CARRY_DIGIT:
                view = (DigitView) _layout.getCarryDigitView(TEN_DIGIT);
                break;

            case HUN_CARRY_DIGIT:
                view = (DigitView) _layout.getCarryDigitView(HUN_DIGIT);
                break;

            default:
                view = (DigitView) _layout.getBaseTenDigitView(RESULT_LOCATION, digit);
                break;
        }

        view.isCorrect = true;
        view.isIncorrect = false;
        view.setTextColor(_activity.getResources().getColor(R.color.correctDigit));
    }

    /**
     * Click Listener that moves clicked View to the next available space.
     * MATH_FEEDBACK (9) make a clone of this that animates everything instead
     */
    class BaseTenOnClickAnimateMe implements View.OnClickListener {

        private final String _digit;

        BaseTenOnClickAnimateMe(String digit) {
            this._digit = digit;
        }
        @Override
        public void onClick(View v) {
            Log.wtf("YELLOW", "y u no move?");
            animateMe(this._digit, (MovableImageView) v);
        }
    }

    /**
     * Click Listener that moves next available View to next available space.
     */
    class BaseTenOnClickAnimateSequential implements View.OnClickListener {

        private final String _digit;

        BaseTenOnClickAnimateSequential(String digit) {
            this._digit = digit;
        }
        @Override
        public void onClick(View v) {
            animateNextSequential(this._digit, v);
        }
    }

    /**
     * Define and initialize the WriteBox inputs.
     */
    private void initializeWriteInputs() {

        // initialize master container
        //_controller_master = (CGlyphController_Simple) findViewById(R.id.glyph_controller);
        // ROBO_MATH why ain't this workin?
        _controller_master = (CGlyphController_Simple) findViewById(R.id.write_box_right);//.findViewById(R.id.glyph_controller);
        _inputContainer_master = (CGlyphInputContainer_Simple) findViewById(R.id.write_box_right).findViewById(R.id.drawn_box);
        _controller_master.setInputContainer(_inputContainer_master);
        _controller_master.setIsLast(true);
        _controller_master.showBaseLine(false);
        _controller_master.setVisibility(View.INVISIBLE);

        _controller_left = (CGlyphController_Simple) findViewById(R.id.write_box_left);
        _inputContainer_left = (CGlyphInputContainer_Simple) findViewById(R.id.write_box_left).findViewById(R.id.drawn_box);
        _controller_left.setInputContainer(_inputContainer_left);
        _controller_left.setIsLast(true);
        _controller_left.showBaseLine(false);
        _controller_left.setVisibility(View.GONE); // it's a LinearLayout, so we make room to center the other guy


        // carrys
        // tens
        /** no longer needed, with master controller
        if (_numDigits >= 2) {
            _controller_ten_carry = (CGlyphController_Simple) findViewById(R.id.glyph_controller_ten_carry);
            _inputContainer_ten_carry = (CGlyphInputContainer_Simple) findViewById(R.id.drawn_box_ten_carry);

            _controller_ten_carry.setInputContainer(_inputContainer_ten_carry);
            _controller_ten_carry.setIsLast(true);
            _controller_ten_carry.showBaseLine(false);
            _controller_ten_carry.setVisibility(View.INVISIBLE);
            _controller_ten_carry.setWritingController(new OnCharacterRecognizedListener(_controller_ten_carry, (DigitView) findViewById(R.id.symbol_carry_ten)));
        }

        // hundreds
        if (_numDigits >= 3) {
            _controller_hun_carry = (CGlyphController_Simple) findViewById(R.id.glyph_controller_hun_carry);
            _inputContainer_hun_carry = (CGlyphInputContainer_Simple) findViewById(R.id.drawn_box_hun_carry);

            _controller_hun_carry.setInputContainer(_inputContainer_hun_carry);
            _controller_hun_carry.setIsLast(true);
            _controller_hun_carry.showBaseLine(false);
            _controller_hun_carry.setVisibility(View.INVISIBLE);
            _controller_hun_carry.setWritingController(new OnCharacterRecognizedListener(_controller_hun_carry, (DigitView) findViewById(R.id.symbol_carry_hun)));
        }

        // ones
        _controller_one = (CGlyphController_Simple) findViewById(R.id.glyph_controller_one);
        _inputContainer_one = (CGlyphInputContainer_Simple) findViewById(R.id.drawn_box_one);

        _controller_one.setInputContainer(_inputContainer_one);
        _controller_one.setIsLast(true);
        _controller_one.showBaseLine(false);
        _controller_one.setVisibility(View.INVISIBLE);
        _controller_one.setWritingController(new OnCharacterRecognizedListener(_controller_one, (DigitView) findViewById(R.id.symbol_result_one)));

        // tens
        if (_numDigits >= 2) {
            _controller_ten = (CGlyphController_Simple) findViewById(R.id.glyph_controller_ten);
            _inputContainer_ten = (CGlyphInputContainer_Simple) findViewById(R.id.drawn_box_ten);

            _controller_ten.setInputContainer(_inputContainer_ten);
            _controller_ten.setIsLast(true);
            _controller_ten.showBaseLine(false);
            _controller_ten.setVisibility(View.INVISIBLE);
            _controller_ten.setWritingController(new OnCharacterRecognizedListener(_controller_ten, (DigitView) findViewById(R.id.symbol_result_ten)));
        }

        // hundreds
        if (_numDigits >= 3) {
            _controller_hun = (CGlyphController_Simple) findViewById(R.id.glyph_controller_hun);
            _inputContainer_hun = (CGlyphInputContainer_Simple) findViewById(R.id.drawn_box_hun);

            _controller_hun.setInputContainer(_inputContainer_hun);
            _controller_hun.setIsLast(true);
            _controller_hun.showBaseLine(false);
            _controller_hun.setVisibility(View.INVISIBLE);
            _controller_hun.setWritingController(new OnCharacterRecognizedListener(_controller_hun, (DigitView) findViewById(R.id.symbol_result_hun)));
        }
         uncomment in case of design change? **/
    }

    /**
     * class that defines behavior when the Character Recognition is finished.
     */
    private class OnCharacterRecognizedListener implements IWritingComponent_Simple {

        CGlyphController_Simple _controller;
        String _controllerName;
        DigitView _digit;
        String _digitName;


        /**
         * Controller is where the user writes the character.
         * Digit is where the recognized digit is displayed.
         *
         * @param controller where the user writes the character.
         * @param digit where the recognized digit is displayed.
         */
        OnCharacterRecognizedListener(CGlyphController_Simple controller, DigitView digit) {
            this._controller = controller;
            this._controllerName = _activity.getResources().getResourceEntryName(_controller.getId());
            this._digit = digit;
            this._digitName = _activity.getResources().getResourceEntryName(_digit.getId());
        }

        /**
         * What to do when the character recognition is finished.
         *
         * @param result
         * @return
         */
        @Override
        public boolean updateStatus(String result) {

            Log.i("RECOGNIZE", "the answer is " + result + "... inside " + _controllerName);

            _digit.setText(result);
            _digit.isCorrect = true;

            _controller.eraseGlyph();
            _controller.setVisibility(View.INVISIBLE);

            // digit name gets passed as action
            // ROBO_MATH this is where we put a WAIT
            _studentActionListener.fireAction(_digitName, "WRITE", result);
            // ruleEngine.registerWrite(character)

            return false;
        }

        @Override
        public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {
            return false;
        }
    }

    /**
     * Reset some variables used to control logic.
     */
    private void resetState() {
        isBorrowing = false;
        isCarrying = false;
        hasBorrowedHun = false;
        hasBorrowedTen = false;

        _currentDigit = ONE_DIGIT;
    }

    /**
     * Reset result values to 0
     */
    private void resetResultsAdd() {
        resultOne = 0;
        resultTen = 0;
        resultHun = 0;
    }

    /**
     * Reset subtract values.
     */
    private void resetResultsSubtract() {
        subtrahendOne = 0;
        subtrahendTen = 0;
        subtrahendHun = 0;

        minuendOne = getOnesDigit(_data.dataset[0]);
        minuendTen = getTensDigit(_data.dataset[0]);
        minuendHun = getHunsDigit(_data.dataset[0]);
    }

    /**
     * Initialize field values needed for BaseTen
     */
    private void initializeNumValues() {
        currentOpAHun = getHunsDigit(_data.dataset[0]);
        currentOpATen = getTensDigit(_data.dataset[0]);
        currentOpAOne = getOnesDigit(_data.dataset[0]);

        currentOpBHun = getHunsDigit(_data.dataset[1]);
        currentOpBTen = getTensDigit(_data.dataset[1]);
        currentOpBOne = getOnesDigit(_data.dataset[1]);
    }

    /**
     * Initialize the symbolic number display for BaseTen ASM.
     */
    private void initializeDigitDisplay() {

        // display operator
        ((TextView) findViewById(R.id.symbol_opB_op)).setText(_data.operation);

        // initialize carry digits
        if (_numDigits >= 3) {
            final DigitView carryHun = (DigitView) findViewById(R.id.symbol_carry_hun);
            carryHun.setVisibility(View.INVISIBLE);
            carryHun.setText("");
            carryHun.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, carryHun)); // ROBO_MATH (5-WAIT)
                    _controller_master.setVisibility(View.VISIBLE);
                    carryHun.setText(""); // erase text
                }
            });
        }

        if (_numDigits >= 2) {
            final DigitView carryTen = (DigitView) findViewById(R.id.symbol_carry_ten);
            carryTen.setVisibility(View.INVISIBLE);
            carryTen.setText("");
            carryTen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setVisibility(View.VISIBLE);
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, carryTen)); // ROBO_MATH (5-WAIT)
                    carryTen.setText("");
                }
            });
        }

        // set each digit separately
        if (_numDigits >= 3) ((TextView) findViewById(R.id.symbol_opA_hun)).setText(String.valueOf(currentOpAHun));
        if (_numDigits >= 2) ((TextView) findViewById(R.id.symbol_opA_ten)).setText(String.valueOf(currentOpATen));
        if (_numDigits >= 1) ((TextView) findViewById(R.id.symbol_opA_one)).setText(String.valueOf(currentOpAOne));

        if (_numDigits >= 3) ((TextView) findViewById(R.id.symbol_opB_hun)).setText(String.valueOf(currentOpBHun));
        if (_numDigits >= 2) ((TextView) findViewById(R.id.symbol_opB_ten)).setText(String.valueOf(currentOpBTen));
        if (_numDigits >= 1) ((TextView) findViewById(R.id.symbol_opB_one)).setText(String.valueOf(currentOpBOne));

        if (_numDigits >= 3) {
            final DigitView hunsResult = ((DigitView) findViewById(R.id.symbol_result_hun));
            hunsResult.setText(null);

            hunsResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setVisibility(View.VISIBLE);
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, hunsResult)); // ROBO_MATH (5-WAIT)
                    hunsResult.setText("");
                }
            });
        }

        if (_numDigits >= 2) {
            final DigitView tensResult = (DigitView) findViewById(R.id.symbol_result_ten);
            tensResult.setText(null);

            tensResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setVisibility(View.VISIBLE);
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, tensResult)); // ROBO_MATH (5-WAIT)
                    tensResult.setText("");
                }
            });
        }

        if (_numDigits >= 1) {
            final DigitView onesResult = (DigitView) findViewById(R.id.symbol_result_one);
            onesResult.setText(null);

            onesResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setVisibility(View.VISIBLE);
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, onesResult)); // ROBO_MATH (5-WAIT)
                    onesResult.setText("");
                }
            });
        }

    }

    /**
     * Hide all concrete representations for carrying & borrowing.
     */
    private void hideCarryAndBorrowConcrete() {
        // hide all carry concrete units.
        if (_numDigits >= 3) findViewById(R.id.carry_hun).setVisibility(View.INVISIBLE);
        if (_numDigits >= 2) findViewById(R.id.carry_ten).setVisibility(View.INVISIBLE);

        // hide all borrow concrete units.
        for (int i=1; i <= 10; i++) {
            // R.id.borrow_ten_i
            if (_numDigits >= 3) _layout.getBaseTenConcreteUnitView("borrow", TEN_DIGIT, i).setVisibility(View.INVISIBLE);
        }
        for (int i=1; i <= 10; i++) {
            // R.id.borrow_one_i
            if (_numDigits >= 2) _layout.getBaseTenConcreteUnitView("borrow", ONE_DIGIT, i).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hide the symbols for carry and borrow
     */
    private void hideCarryAndBorrowSymbols() {
        if (_numDigits >= 3) findViewById(R.id.symbol_borrow_hun).setVisibility(View.INVISIBLE);
        if (_numDigits >= 2) findViewById(R.id.symbol_borrow_ten).setVisibility(View.INVISIBLE);
        if (_numDigits >= 2) findViewById(R.id.symbol_borrow_one).setVisibility(View.INVISIBLE);
        if (_numDigits >= 3) findViewById(R.id.symbol_borrow_ten_row2).setVisibility(View.INVISIBLE);
    }

    /**
     * Initialize the concrete number display for BaseTen Addition.
     */
    private void initializeBaseTensForAddition() {

        // display concrete representations
        displayNumberBaseTen(OPA_LOCATION, _data.dataset[0]);
        displayNumberBaseTen(OPB_LOCATION, _data.dataset[1]);
        displayNumberBaseTen(RESULT_LOCATION, 0);
    }

    /**
     * Initialize the concrete number display for BaseTen Subtraction.
     */
    private void initializeBaseTensForSubtraction() {

        // display concrete representations
        displayNumberBaseTen(OPA_LOCATION, _data.dataset[0]);
        displayNumberBaseTen(OPB_LOCATION, _data.dataset[1], true); // subtrahend starts out with 0 // MATH_FEEDBACK (6) this should be different for subtraction... display GHOST dots (vs empty dots)
        displayNumberBaseTen(RESULT_LOCATION, 0);
    }

    private void displayNumberBaseTen(String numberLoc, int numberValue) {
        displayNumberBaseTen(numberLoc, numberValue, false);
    }
    /**
     * Display the concrete number values for a given 3-digit number.
     *
     * @param numberLoc top row (operand 1), middle row (operand 2), or bottom row (sum/diff)
     * @param numberValue the number value to be displayed in concrete form.
     */
    private void displayNumberBaseTen(String numberLoc, int numberValue, boolean ghost) {

        MovableImageView hun, ten, one;
        int hunsDigit, tensDigit, onesDigit;


        if (_numDigits >= 3) {
            hunsDigit = getHunsDigit(numberValue);
            final int NUM_HUNDREDS = numberLoc.equals(RESULT_LOCATION) ? 9 : 5;
            for (int i = 1; i <= NUM_HUNDREDS; i++) {
                hun = _layout.getBaseTenConcreteUnitView(numberLoc, HUN_DIGIT, i);
                //hun.setVisibility( i <= hunsDigit ? View.VISIBLE : View.INVISIBLE);
                hun.setImageDrawable(getDrawable(i <= hunsDigit ? (ghost ? R.drawable.blue_ghost_100 : R.drawable.blue_100 ): R.drawable.empty_100));
                hun.isMovable = i <= hunsDigit;
            }
        }


        if (_numDigits >= 2) {
            tensDigit = getTensDigit(numberValue);
            for (int i = 1; i <= 10; i++) {
                ten = _layout.getBaseTenConcreteUnitView(numberLoc, TEN_DIGIT, i);
                ten.setImageDrawable(getDrawable(i <= tensDigit ? (ghost ? R.drawable.blue_ghost_10_h : R.drawable.blue_10_h ) : R.drawable.empty_10_h));
                ten.isMovable = i <= tensDigit;
            }
        }

        onesDigit = getOnesDigit(numberValue);
        for (int i = 1; i <= 10; i++) {
            one = _layout.getBaseTenConcreteUnitView(numberLoc, ONE_DIGIT, i);
            one.setImageDrawable(getDrawable(i <= onesDigit ? (ghost ? R.drawable.blue_ghost_1 : R.drawable.blue_1 ) : R.drawable.empty_1));
            one.isMovable = i <= onesDigit;
        }


        // results row has an extra in each column
        if (numberLoc.equals(RESULT_LOCATION)) {
            one = _layout.getBaseTenConcreteUnitView(numberLoc, ONE_DIGIT, 10);
            //one.setVisibility(View.INVISIBLE); // only gets revealed by student action
            one.setVisibility(View.VISIBLE);
            one.setImageDrawable(getDrawable(R.drawable.empty_1));
            one.isMovable = false;

            if (_numDigits >= 2) {
                ten = _layout.getBaseTenConcreteUnitView(numberLoc, TEN_DIGIT, 10);
                //ten.setVisibility(View.INVISIBLE); // only gets revealed by student action
                ten.setVisibility(View.VISIBLE);
                ten.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                ten.isMovable = false;
            }

            if (_numDigits >= 3) {
                hun = _layout.getBaseTenConcreteUnitView(numberLoc, HUN_DIGIT, 10);
                hun.setVisibility(View.VISIBLE);
                hun.setImageDrawable(getDrawable(R.drawable.empty_100));
                hun.isMovable = false;
            }
        }

    }


    /**
     * Highlight only the current digit Column
     * @param digit
     */
    public void highlightDigitColumn(String digit) {

        String[] numberLocations = {OPA_LOCATION, OPB_LOCATION, RESULT_LOCATION};

        String[] digitColumns = {ONE_DIGIT, TEN_DIGIT, HUN_DIGIT};

        for (String column : digitColumns) {
            if (_numDigits < 3 && column.equals(HUN_DIGIT)) return; // nasty checking
            if (_numDigits < 2 && column.equals(TEN_DIGIT)) return; // nasty checking
            boolean highlightThisColumn = digit.equals(column) || digit.equals(ALL_DIGITS);
            // only highlight some
            int opacity =  highlightThisColumn ? 255 : (int) _activity.getResources().getInteger(R.integer.unused_concrete_alpha);
            float floatPacity = highlightThisColumn ? 1.0f :  (float)((float) _activity.getResources().getInteger(R.integer.unused_concrete_alpha) / 255.0);

            for (String numberLoc : numberLocations) {

                // change concrete representations
                int numUnits = column.equals(HUN_DIGIT) ? (numberLoc.equals(RESULT_LOCATION) ? 10 : 5) : 10;

                for (int i=1; i <=numUnits; i++) {
                    _layout.getBaseTenConcreteUnitView(numberLoc, column, i).setImageAlpha(opacity);

                    try {
                        ImageView helper = _layout.getBaseTenConcreteUnitView(numberLoc, column, i, true);
                        if (helper != null) helper.setImageAlpha(opacity);
                    } catch (Exception e) {
                        Log.e("MATH_BEHAVIOR", "fix me... ");
                        //e.printStackTrace();
                    }
                }

                _layout.getBaseTenDigitView(numberLoc, column).setAlpha(floatPacity);
            }

        }

    }

    /**
     * prevent other rows from being tapped on
     * @param digit
     */
    public void disableConcreteUnitTappingForOtherRows(String digit) {

        String[] numberLocations = {OPA_LOCATION, OPB_LOCATION, RESULT_LOCATION};

        String[] digitColumns = {ONE_DIGIT, TEN_DIGIT, HUN_DIGIT};

        for (String column : digitColumns) {
            if (_numDigits < 3 && column.equals(HUN_DIGIT)) return; // nasty checking
            if (_numDigits < 2 && column.equals(TEN_DIGIT)) return; // nasty checking

            boolean enableThisColumn = digit.equals(column) || digit.equals(ALL_DIGITS);
            // only highlight some

            for (String numberLoc : numberLocations) {

                // change concrete representations
                int numUnits = column.equals(HUN_DIGIT) ? (numberLoc.equals(RESULT_LOCATION) ? 10 : 5) : 10;

                for (int i=1; i <=numUnits; i++) {
                    _layout.getBaseTenConcreteUnitView(numberLoc, column, i).isMovable = enableThisColumn;
                }

                //_layout.getBaseTenDigitView(numberLoc, column);
            }

        }
    }


    /* ===================== */
    /* == BEGIN ANIMATION == */
    /* ===================== */

    /**
     * Move this view to next available location in specified digit column.
     *
     * @param digit
     * @param v
     */
    public void animateMe(String digit, MovableImageView v) {

        Log.wtf("YELLOW", "y u no move?");
        if (EXPAND_HIT_BOX) return;

        Log.wtf("YELLOW", "y u no move?");
        if (v.isMoving || !v.isMovable) return;

        Log.wtf("YELLOW", "y u no move?");
        switch(_data.operation) {
            case "+":
                moveForAddition(v, digit);
                break;

            case "-":
                moveForSubtraction(v, digit);
        }
    }

    /**
     * MATH_FEEDBACK (9)
     * @param digit
     * @param v
     */
    public void animateNextSequential(String digit, View v) {

        if (!EXPAND_HIT_BOX) return;

        String numberLoc = v.getTag().toString();
        moveSequential(numberLoc, digit);
    }

    /**
     *
     * Move this view to the next available location in the tens column of the sum row.
     *
     * @param v the view clicked.
     */
    public void moveTenCarry(View v) {

        if(((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForAddition(v, TEN_DIGIT);

    }

    /**
     *
     * Move this view to the next available location in the tens column of the subtrahend row.
     *
     * @param v
     */
    public void moveTenBorrow(View v) {
        if (((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForSubtraction(v, TEN_DIGIT);
    }

    /**
     *
     * Move this view to the next available location in the ones column of the subtrahend row.
     *
     * @param v
     */
    public void moveOneBorrow(View v) {
        if (((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForSubtraction(v, ONE_DIGIT);
    }

    /**
     *
     * Move this view to the next available location in the hundreds column of the sum row.
     *
     * @param v the view clicked.
     */
    public void moveHunCarry(View v) {

        if(((MovableImageView) v).isMoving || !((MovableImageView) v).isMovable) return;

        moveForAddition(v, HUN_DIGIT);

    }

    /**
     * UI-details
     * Moves a BaseTen ImageView of the type digitPlace.
     *
     * TODO make separate types for one, ten, hun
     * @param v the view to move
     * @param digitPlace the column to move to.
     */
    private void moveForAddition(View v, final String digitPlace) {

        Log.wtf("YELLOW", "y u no move?");
        // CARRY... prevent tapping while in carry mode
        if(isCarrying) {
            return;
        }

        final MovableImageView oldView = (MovableImageView) v;
        Log.wtf("YELLOW", "oldView id ==\t" + oldView.getResources().getResourceEntryName(oldView.getId()));
        final MovableImageView newView = determineNextResultView(digitPlace, false);

        // an animation helper
        final MovableImageView helperView = determineNextResultView(digitPlace, true);

        int[] oldLocation = new int[2], newLocation = new int[2];

        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);


        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {

            boolean isCarry = false;

            @Override
            public void onAnimationStart(Animator animation) {

                // once moved, it can't move again
                oldView.isMovable = false;

                // show both newView (destination) and helperView (temporary placeholder)
                newView.setVisibility(View.VISIBLE);
                if (helperView != null) {
                    helperView.setVisibility(View.VISIBLE);
                }

                switch(digitPlace) {
                    case HUN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        resultHun += 1;
                        break;

                    case TEN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        // if this is the tenth ten, prepare a carry!
                        // MATHFIX_LAYOUT (1) NEXT NEXT NEXT populate the second row instead!!
                        if (resultTen == 9) {
                            this.isCarry = true;
                            isCarrying = true; // prevent other animations...
                            resultTen = 0;
                        } else{
                            resultTen += 1;
                        }
                        break;

                    case ONE_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        // if this is the tenth one, prepare a carry!
                        // MATHFIX_LAYOUT (1) NEXT NEXT NEXT populate the second row instead!!
                        if (resultOne == 9) {
                            this.isCarry = true;
                            isCarrying = true; // prevent other animations
                            resultOne = 0;
                        } else {
                            resultOne += 1;
                        }
                        break;
                }


            }

            @Override
            public void onAnimationEnd(Animator animation) {


                switch(digitPlace) {
                    case HUN_DIGIT:

                        // to show when animating
                        final MovableImageView helperView = determineNextResultView(digitPlace, true);
                        if (helperView != null) {
                            helperView.setVisibility(View.INVISIBLE);
                        }
                        break;

                    case TEN_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(TEN_DIGIT);
                        }
                        break;

                    case ONE_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(ONE_DIGIT);
                        }
                        break;

                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();

    }

    /**
     * UI-details
     * Moves a BaseTen ImageView of the type digitPlace.
     *
     * @param v
     * @param digitPlace hun, ten, or one
     */
    private void moveForSubtraction(View v, final String digitPlace) {

        // this statement checks if we have already tapped enough to fill the subtrahend
        // for example, if the subtrahend is 304 and we have already filled 3 hundreds spaces, return.
        switch(digitPlace) {
            case HUN_DIGIT:
                if (subtrahendHun == getHunsDigit(_data.dataset[1])) {
                    return;
                }
                break;

            case TEN_DIGIT:
                if (subtrahendTen == getTensDigit(_data.dataset[1])) {
                    return;
                }
                break;

            case ONE_DIGIT:
                if (subtrahendOne == getOnesDigit(_data.dataset[1])) {
                    return;
                }
        }

        final MovableImageView oldView = (MovableImageView) v;
        final MovableImageView newView = determineNextSubtrahendView(digitPlace, false);
        // helper for better animation
        final MovableImageView helperView = determineNextSubtrahendView(digitPlace, true);


        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);


        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {


                // once moved, it can't move again
                oldView.isMovable = false;

                // show both newView (destination) and helperView (temporary placeholder)
                newView.setVisibility(View.VISIBLE);
                helperView.setVisibility(View.VISIBLE);

                switch(digitPlace)  {
                    case HUN_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_100));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        subtrahendHun += 1;
                        break;

                    case TEN_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_10_h));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        subtrahendTen += 1;
                        break;

                    case ONE_DIGIT:
                        helperView.setImageDrawable(getDrawable(R.drawable.blue_ghost_1));
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        subtrahendOne += 1;
                        break;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /**
     * Instead of moving the clicked View, move the next available view in this row
     *
     * @param numberLoc
     * @param digit
     */
    private void moveSequential(final String numberLoc, final String digit) {

        if (isCarrying) {
            return;
        }

        final MovableImageView oldView = determineNextTopView(numberLoc, digit);
        // if we're at zero, this will return null
        if(oldView == null) {
            return;
        }
        final MovableImageView newView = determineNextResultView(digit, false);
        final MovableImageView helperView = determineNextResultView(digit, true);

        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);

        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {

            boolean isCarry = false;

            @Override
            public void onAnimationStart(Animator animation) {

                switch(digit) {
                    case HUN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                currentOpAHun--;
                                break;

                            case OPB_LOCATION:
                                currentOpBHun--;
                                break;
                        }

                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        resultHun += 1;
                        break;

                    case TEN_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                currentOpATen--;
                                break;

                            case OPB_LOCATION:
                                currentOpBTen--;
                                break;
                        }

                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        // if this is the tenth ten, prepare a carry!
                        if (resultTen == 9) {
                            this.isCarry = true;
                            isCarrying = true; // prevent other animations...
                            resultTen = 0;
                        } else{
                            resultTen += 1;
                        }
                        break;

                    case ONE_DIGIT:
                        oldView.setImageDrawable(getDrawable(R.drawable.empty_1));
                        oldView.isMovable = false;
                        newView.setVisibility(View.VISIBLE);
                        newView.setImageDrawable(getDrawable(R.drawable.blue_1));

                        // don't forget to decrement
                        switch(numberLoc) {
                            case OPA_LOCATION:
                                currentOpAOne--;
                                break;

                            case OPB_LOCATION:
                                currentOpBOne--;
                                break;
                        }


                        // to show when animating
                        if (helperView != null) {
                            helperView.setVisibility(View.VISIBLE);
                        }

                        // if this is the tenth one, prepare a carry!
                        if (resultOne == 9) {
                            this.isCarry = true;
                            isCarrying = true; // prevent other animations
                            resultOne = 0;
                        } else {
                            resultOne += 1;
                        }
                        break;
                }


            }

            @Override
            public void onAnimationEnd(Animator animation) {


                switch(digit) {
                    case HUN_DIGIT:

                        // to show when animating
                        final MovableImageView helperView = determineNextResultView(digit, true);
                        if (helperView != null) {
                            helperView.setVisibility(View.INVISIBLE);
                        }
                        break;

                    case TEN_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(TEN_DIGIT);
                        }
                        break;

                    case ONE_DIGIT:

                        if(isCarry) {
                            startCarryAnimation(ONE_DIGIT);
                        }
                        break;

                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();

    }

    /**
     * Start the carry animation from 10 ones to 1 ten, or 10 tens to 1 hun.
     * @param digit can be "one" or "ten"
     */
    private void startCarryAnimation(final String digit) {

        Log.i("TURN THE LIGHTS OFF", "CARRY ME HOME");
        final MovableImageView oldView, newView;

        switch (digit) {
            case ONE_DIGIT:
                oldView = (MovableImageView) _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, ONE_DIGIT, 1);
                newView = (MovableImageView) findViewById(R.id.carry_ten);
                break;


            case TEN_DIGIT:
            default:
                oldView = (MovableImageView) _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, TEN_DIGIT, 1);
                newView = (MovableImageView) findViewById(R.id.carry_hun);
                break;
        }

        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);


        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1500);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.wtf("TURN THE LIGHTS OFF", "CARRY ME HOME");
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);

                switch(digit) {

                    case ONE_DIGIT:

                        MovableImageView[] ones = new MovableImageView[10];
                        for (int i = 1; i <= 10; i++) {
                            ones[i-1] = _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, ONE_DIGIT, i);
                            //ones[i-1].setVisibility(View.INVISIBLE);
                            ones[i-1].setImageDrawable(getDrawable(R.drawable.empty_1));
                        }
                        break;

                    case TEN_DIGIT:
                        MovableImageView[] ten = new MovableImageView[10];
                        for (int i = 1; i <= 10; i++) {
                            ten[i-1] = _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, TEN_DIGIT, i);
                            //ten[i-1].setVisibility(View.INVISIBLE);
                            ten[i-1].setImageDrawable(getDrawable(R.drawable.empty_10_h));
                        }
                    default:
                        break;

                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                newView.isMoving = false;
                isCarrying = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /* ===================== */
    /* === END ANIMATION === */
    /* ===================== */

    /**
     * Determines the next open spot in the sum row.
     *
     * @param digitPlace either one, ten, or hun
     * @return the next open View
     */
    private MovableImageView determineNextResultView(String digitPlace, boolean helper) {

        int currentDigit, nextDigit;

        switch (digitPlace) {
            case HUN_DIGIT:
                currentDigit = resultHun;
                break;

            case TEN_DIGIT:
                currentDigit = resultTen;
                break;

            case ONE_DIGIT:
                currentDigit = resultOne;
                break;

            default:
                return null;

        }

        nextDigit = currentDigit + 1;

        return _layout.getBaseTenConcreteUnitView(RESULT_LOCATION, digitPlace, nextDigit, helper);
    }

    /**
     * Determines the next open spot in the subtrahend row (opB).
     *
     * @param digitPlace either one, ten, or hun
     * @param helper
     * @return the next View
     */
    private MovableImageView determineNextSubtrahendView(String digitPlace, boolean helper) {

        int currentDigit, nextDigit;

        switch(digitPlace) {
            case HUN_DIGIT:
                currentDigit = subtrahendHun;
                break;

            case TEN_DIGIT:
                currentDigit = subtrahendTen;
                break;

            case ONE_DIGIT:
                currentDigit = subtrahendOne;
                break;

            default:
                return null;
        }


        nextDigit = currentDigit + 1;

        return _layout.getBaseTenConcreteUnitView(OPB_LOCATION, digitPlace, nextDigit, helper);
    }

    /**
     * Instead of moving clicked View, move the right-most View.
     *
     * Returns null when there are none left in the row.
     *
     * @param digit
     * @param numberLoc
     * @return
     */
    private MovableImageView determineNextTopView(String numberLoc, String digit) {

        int currentDigit = 0;

        // this could be neater... but it works
        switch(digit) {
            case HUN_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = currentOpAHun;
                        break;

                    case OPB_LOCATION:
                        currentDigit = currentOpBHun;
                        break;
                }

                break;

            case TEN_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = currentOpATen;
                        break;

                    case OPB_LOCATION:
                        currentDigit = currentOpBTen;
                        break;
                }

                break;

            case ONE_DIGIT:

                switch(numberLoc) {
                    case OPA_LOCATION:
                        currentDigit = currentOpAOne;
                        break;

                    case OPB_LOCATION:
                        currentDigit = currentOpBOne;
                        break;
                }
                break;
        }

        return _layout.getBaseTenConcreteUnitView(numberLoc, digit, currentDigit);
    }


    /* DEBUG BUTTONS */

    /**
     *
     * Debug button for Highlight hundreds.
     *
     */
    public void highlightPvHuns() {
        highlightUnits(OPA_LOCATION, HUN_DIGIT, true);
        highlightUnits(OPB_LOCATION, HUN_DIGIT, true);
        highlightUnits(RESULT_LOCATION, HUN_DIGIT, true);
    }

    /**
     *
     */
    public void highlightPvTens() {
        highlightUnits(OPA_LOCATION, TEN_DIGIT, true);
        highlightUnits(OPB_LOCATION, TEN_DIGIT, true);
        highlightUnits(RESULT_LOCATION, TEN_DIGIT, true);
    }

    /**
     *
     */
    public void highlightPvOnes() {
        highlightUnits(OPA_LOCATION, ONE_DIGIT, true);
        highlightUnits(OPB_LOCATION, ONE_DIGIT, true);
        highlightUnits(RESULT_LOCATION, ONE_DIGIT, true);
    }


    /**
     *
     * Show the carry option for hundreds
     */
    public void showCarryHun() {
        findViewById(R.id.symbol_carry_num).setVisibility(View.VISIBLE);
        TextView showMe = (TextView) findViewById(R.id.symbol_carry_hun);
        //showMe.setVisibility(showMe.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        showMe.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    public void showCarryTen() {
        findViewById(R.id.symbol_carry_num).setVisibility(View.VISIBLE);
        TextView showMe = (TextView) findViewById(R.id.symbol_carry_ten);
        //showMe.setVisibility(showMe.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        showMe.setVisibility(View.VISIBLE);
    }

    /**
     * set strikethrough
     */
    public void strikeThroughTenBorrow() {
        // this happens afer animation
        TextView borrowFromMe = (TextView) findViewById(R.id.symbol_opA_ten);
        borrowFromMe.setPaintFlags(borrowFromMe.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // strike through
        borrowFromMe.setTextColor(_activity.getResources().getColor(R.color.borrowedColor));
    }

    /**
     * show a blank space to write in above borrow
     */
    public void showBorrowDigitHolder() {
        TextView showMe = (TextView) findViewById(R.id.symbol_borrow_ten);
        showMe.setVisibility(View.VISIBLE);
        showMe.setText("");
        // MATH_BEHAVIOR (1) (this is a redundant method... see bottom guy)
        // BigWriteBox.onRecognized, do "writeNewTenBorrowedValue"
    }

    public void writeNewTenBorrowedValue(int newValue) {

        // MATH_BEHAVIOR (1) (remove showBorrowDigitHolder)
        // then the student writes this value. this will be replaced by actual action
        final DigitView borrowTenDigit = (DigitView) findViewById(R.id.symbol_borrow_ten);
        borrowTenDigit.setVisibility(View.VISIBLE);
        borrowTenDigit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, borrowTenDigit)); // ROBO_MATH (5-WAIT)
                _controller_master.setVisibility(View.VISIBLE);
                borrowTenDigit.setText("");
            }
        });
    }

    public void strikeThroughOneBorrow() {
        // replace one value
        TextView addTenToMe = (TextView) findViewById(R.id.symbol_opA_one);
        addTenToMe.setPaintFlags(addTenToMe.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // strike through
        addTenToMe.setTextColor(_activity.getResources().getColor(R.color.borrowedColor));
    }

    private boolean _borrowTenIsWritten = false;
    private boolean _borrowOneIsWritten = false;
    /**
     * show that we're borrowing
     */
    public void populateOneWithBorrowedTen(int newOneValue) {
        // show left digit box and right digit box
        final DigitView borrowOne = (DigitView) findViewById(R.id.symbol_borrow_one);
        borrowOne.setVisibility(View.VISIBLE);
        borrowOne.setText("");

        // when the box is touched, open up two new boxes
        borrowOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // different type of box
                _controller_master.setVisibility(View.VISIBLE);

                _controller_master.setWritingController(new IWritingComponent_Simple() {
                    @Override
                    public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {
                        return false;
                    }

                    @Override
                    public boolean updateStatus(String _ltkPlusResult) {

                        findViewById(R.id.write_box_right).setVisibility(View.GONE);
                        findViewById(R.id.write_box_digit_right).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.write_box_digit_right)).setText(_ltkPlusResult);

                        _controller_master.eraseGlyph();
                        _borrowOneIsWritten = true;

                        if (_borrowTenIsWritten) {
                            findViewById(R.id.write_box_right).setVisibility(View.GONE);
                            findViewById(R.id.write_box_digit_right).setVisibility(View.GONE);
                            findViewById(R.id.write_box_left).setVisibility(View.GONE);
                            findViewById(R.id.write_box_digit_left).setVisibility(View.GONE);

                            // nasty
                            String answer = ((TextView) findViewById(R.id.write_box_digit_left)).getText() + _ltkPlusResult ;
                            ((DigitView) findViewById(R.id.symbol_borrow_one)).setText(answer);
                        }
                        return false;
                    }
                });

                _controller_left.setVisibility(View.VISIBLE);

                _controller_left.setWritingController(new IWritingComponent_Simple() {
                    @Override
                    public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {
                        return false;
                    }

                    @Override
                    public boolean updateStatus(String _ltkPlusResult) {

                        findViewById(R.id.write_box_left).setVisibility(View.GONE);
                        findViewById(R.id.write_box_digit_left).setVisibility(View.VISIBLE);
                        ((DigitView) findViewById(R.id.write_box_digit_left)).setText(_ltkPlusResult);

                        _controller_left.eraseGlyph();
                        _borrowTenIsWritten = true;

                        if (_borrowOneIsWritten) {
                            findViewById(R.id.write_box_right).setVisibility(View.GONE);
                            findViewById(R.id.write_box_digit_right).setVisibility(View.GONE);
                            findViewById(R.id.write_box_left).setVisibility(View.GONE);
                            findViewById(R.id.write_box_digit_left).setVisibility(View.GONE);

                            // nasty
                            String answer = _ltkPlusResult + ((TextView) findViewById(R.id.write_box_digit_right)).getText();
                            ((DigitView) findViewById(R.id.symbol_borrow_one)).setText(answer);
                        }
                        return false;
                    }
                });
            }
        });
    }

    /**
     *
     * Move the minuend BaseTenobjects to the result row.
     * This will only occur if all the items have been tapped.
     * MATH_FEEDBACK (8) this should be called again
     */
    public void moveMinuendToResult() {
        if (subtrahendHun != getHunsDigit(_data.dataset[1]) ||
                subtrahendTen != getTensDigit(_data.dataset[1]) ||
                subtrahendOne != getOnesDigit(_data.dataset[1])) {

            Toast.makeText(_activity, "Please subtract until you fill the subtrahend", Toast.LENGTH_LONG).show();
            return;
        }

        // do hundreds
        for (int i = 5; i > 0; i--) {
            MovableImageView hun = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, HUN_DIGIT, i);
            if(hun.isMovable) {
                moveForAddition(hun, HUN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }

        // do tens
        for (int i = 10; i > 0; i--) {
            MovableImageView ten = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, TEN_DIGIT, i);
            if(ten.isMovable) {
                moveForAddition(ten, TEN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }
        // borrowed tens
        if (hasBorrowedHun) {
            for (int i = 10; i > 0; i --) {
                MovableImageView ten = _layout.getBaseTenConcreteUnitView("borrow", TEN_DIGIT, i);
                if (ten.isMovable) {
                    moveForAddition(ten, TEN_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
                }
            }
        }

        // do ones
        for (int i = 10; i > 0; i--) {
            MovableImageView one = _layout.getBaseTenConcreteUnitView(OPA_LOCATION, ONE_DIGIT, i);
            if(one.isMovable) {
                moveForAddition(one, ONE_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
            }
        }
        // borrowed ones
        if (hasBorrowedTen) {
            for (int i = 10; i > 0; i --) {
                MovableImageView one = _layout.getBaseTenConcreteUnitView("borrow", ONE_DIGIT, i);
                if (one.isMovable) {
                    moveForAddition(one, ONE_DIGIT); // MATH_SUBTRACT this ain't right. Rename or make new method.
                }
            }
        }
    }

    /**
     *
     * Animate the borrowing of a hundred into the tens column.
     */
    public void borrowHun() {
        if (hasBorrowedHun) return;

        isBorrowing = true;

        final MovableImageView oldView = determineNextTopView(OPA_LOCATION, HUN_DIGIT);
        final MovableImageView newView = (MovableImageView) findViewById(R.id.borrow_hun_helper);

        if (!oldView.isMovable && currentOpAHun == 0) {
            // MATH_SUBTRACT this should be able to take from the borrow column...
            Toast.makeText(_activity, "You have no hundreds to borrow!", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);


        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);
                newView.setImageDrawable(getDrawable(R.drawable.blue_100));

                // our hundred is accounted for
                oldView.setImageDrawable(getDrawable(R.drawable.empty_100));
                oldView.isMovable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                newView.setVisibility(View.INVISIBLE);
                isBorrowing = false;
                hasBorrowedHun = true;

                for(int i=1; i <= 10; i++) {
                    MovableImageView ten = _layout.getBaseTenConcreteUnitView("borrow", TEN_DIGIT, i);
                    ten.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /**
     *
     * An interaction placeholder. Borrow Ten Ones from the Ten Column.
     */
    public void borrowTen() {
        if (hasBorrowedTen) return;

        isBorrowing = true;

        final MovableImageView oldView = determineNextTopView(OPA_LOCATION, TEN_DIGIT);
        // special case...
        if (!oldView.isMovable && currentOpATen == 0) {
            // MATH_SUBTRACT this should be able to take from the borrow column...
            Toast.makeText(_activity, "You have no tens to borrow!", Toast.LENGTH_SHORT).show();
            return;
        }
        final MovableImageView newView = (MovableImageView) findViewById(R.id.borrow_ten_helper);

        int[] oldLocation = new int[2], newLocation = new int[2];
        oldView.getLocationOnScreen(oldLocation);
        newView.getLocationOnScreen(newLocation);
        float dy = newLocation[1] - oldLocation[1];
        Log.d("YELLOW X translation\t", "" + oldLocation[0] + " --> " + newLocation[0]);
        float dx = newLocation[0] - oldLocation[0];
        Log.d("YELLOW Y translation\t", "" + oldLocation[1] + " --> " + newLocation[1]);

        newView.setTranslationX(-dx);
        newView.setTranslationY(-dy);


        ObjectAnimator animX = ObjectAnimator.ofFloat(newView, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newView, "translationY", 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animX, animY);
        animSet.setDuration(1000);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                newView.isMoving = true;
                newView.setVisibility(View.VISIBLE);
                newView.setImageDrawable(getDrawable(R.drawable.blue_10_h));

                oldView.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                oldView.isMovable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // hide the ten that moved and...
                newView.setVisibility(View.INVISIBLE);
                isBorrowing = false;
                hasBorrowedTen = true;

                // ... replace it with ten ones
                for(int i=1; i <= 10; i++) {
                    MovableImageView one = _layout.getBaseTenConcreteUnitView("borrow", ONE_DIGIT, i);
                    one.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animSet.start();
    }

    /**
     * Highlight the concrete unit and digit associated with a unit.
     *
     * @param numberLoc location of the number e.g. top, bottom, left, or right
     * @param digit one, ten, or hun
     * @param suppressOthers un-highlight other digits
     */
    private void highlightUnits(String numberLoc, @Nullable String digit, boolean suppressOthers) {




        // cycle through each digit
        String[] allDigits = {ONE_DIGIT, TEN_DIGIT, HUN_DIGIT};
        for (String d : allDigits) {

            // only perform highlight if it's the digit we're changing
            // remove highlight if suppressOthers is true
            if(d.equals(digit) || suppressOthers) {

                Log.d("HIGHLIGHT", numberLoc + " " +  digit);

                ImageView[] ndUnits = new ImageView[d.equals(HUN_DIGIT) ? 6 : 11];
                for (int i = 1; i < ndUnits.length; i++) {

                    ndUnits[i] = _layout.getBaseTenConcreteUnitView(numberLoc, d, i);
                    if (ndUnits[i].getVisibility() == View.VISIBLE) {
                        // either highlight or remove highlight
                        // just the background
                        // ndUnits[i].setBackgroundColor(d.equals(digit) ? getResources().getColor(R.color.bigMathHighlightColor) : Color.TRANSPARENT);
                    }
                }


                TextView ndSymbolic = _layout.getBaseTenDigitView(numberLoc, d);
                // either highlight or remove highlight
                ndSymbolic.setBackgroundColor(d.equals(digit) ? _activity.getResources().getColor(R.color.bigMathHighlightColor): Color.TRANSPARENT);

                String boxId = d + "_" + numberLoc + "_box";
                int resID = _activity.getResources().getIdentifier(boxId, "id", _activity.getPackageName());
                findViewById(resID).setBackgroundColor(d.equals(digit) ? _activity.getResources().getColor(R.color.bigMathHighlightColor) : Color.TRANSPARENT);
                //findViewById(resID).setBackground(getDrawable(R.drawable.inner_rectangle));
            }
        }

    }

    /**
     * A helper so i don't have to rewrite all these references
     *
     * @param resID
     * @return
     */
    private View findViewById(int resID) {
        return _viewGroup.findViewById(resID);
    }

    /**
     * A helper so I don't have to rewrite all these references
     * @param resID
     * @return
     */
    private Drawable getDrawable(int resID) {
        return _activity.getDrawable(resID);
    }
}
