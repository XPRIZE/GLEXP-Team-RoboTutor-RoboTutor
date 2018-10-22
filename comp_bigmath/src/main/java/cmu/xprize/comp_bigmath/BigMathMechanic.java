package cmu.xprize.comp_bigmath;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cmu.xprize.comp_writebox.CGlyphController_Simple;
import cmu.xprize.comp_writebox.CGlyphInputContainer_Simple;
import cmu.xprize.comp_writebox.IGlyphController_Simple;
import cmu.xprize.comp_writebox.IWritingComponent_Simple;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.util.IBehaviorManager;
import cmu.xprize.util.IPerformanceTracker;
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
    private BigMathAnimationHelper _animator;
    private BigMathProblemState _problemState;
    private final IPerformanceTracker _performance;

    private IHesitationManager _hManager;

    // MATH_BEHAVIOR (1) add case for each digit into SAI receiver
    public String _currentDigit;

    private int _numDigits;
    private boolean ALL_AT_ONCE = true;

    private static final String BASE_TEN_TAG = "BaseTen";

    public static String APP_PRIVATE_FILES;

    // master writing box
    CGlyphController_Simple _controller_master;
    CGlyphInputContainer_Simple _inputContainer_master;

    CGlyphController_Simple _controller_left;
    CGlyphInputContainer_Simple _inputContainer_left;

    private StudentActionListener _studentActionListener;

    public BigMathMechanic(Context activity, IBehaviorManager behaviorManager, IPublisher publisher, IHesitationManager hesitationManager, ViewGroup viewGroup, IPerformanceTracker performance) {
        _behaviorManager = behaviorManager;
        _publisher = publisher;
        _activity = activity;
        _viewGroup = viewGroup;
        _layout = new BigMathLayoutHelper(activity, viewGroup);
        _hManager = hesitationManager;
        _performance = performance;

        _studentActionListener = new StudentActionListenerImpl(_behaviorManager, _publisher, this, _performance); // won't always work...
        _animator = new BigMathAnimationHelper(_activity, _layout, _viewGroup);
    }

    public BigMathProblemState getProblemState() {
        return _problemState;
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

        // initialize problem state
        _problemState = new BigMathProblemState(data);
        _animator.setProblemState(_problemState);

        _studentActionListener.setData(data, _numDigits);

        _currentDigit = ONE_DIGIT;
    }

    // needed in SAI???
    public void moveMinuendToResult() {
        _animator.moveMinuendToResult();
    }

    public void borrowTen() {
        _animator.borrowTen();
    }

    /**
     * Just a temporary placeholder to do all the things.
     */
    void doAllTheThings() {

        initializeLayout();

        initializeOnClickListeners();

        initializeWriteInputs();

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

            // subtraction doesn't have click listeners in the second row, it just receives
            if (numLoc.equals(OPB_LOCATION) && _data.operation.equals("-")) {
                return;
            }

            // BUG_605 waterfall not ready for subtraction
            View.OnClickListener oneListener;
            boolean useWaterfallForOnesDigit = ALL_AT_ONCE && _numDigits >= 2;
            if (useWaterfallForOnesDigit) {
                if (_data.operation.equals("+")) {
                    oneListener = _animator.generateWaterfallClickListener(numLoc, ONE_DIGIT); // MATH_MISC (1) don't allow ghost dots to move
                } else {
                    oneListener = _animator.generateWaterfallSubtractClickListener(ONE_DIGIT);
                }
            } else if (!ALL_AT_ONCE && _numDigits >= 2) {
                oneListener = _animator.generateSingleClickListener(ONE_DIGIT);
            } else {
                oneListener = _animator.generateSingleClickListener(ONE_DIGIT);
            }

            // add listeners to Ones
            for (int i=1; i <= 10; i++) {

                MovableImageView oneView = _layout.getBaseTenConcreteUnitView(numLoc, ONE_DIGIT, i);

                oneView.setOnClickListener(oneListener);
            }

            // BUG_605 next: can we make waterfall not ready for subtraction?
            boolean useWaterfallForMultiDigit = ALL_AT_ONCE && _data.operation.equals("+");

            // add listeners to Tens
            if (_numDigits >= 2)
                for (int i=1; i <= 10; i++) {

                    MovableImageView tenView = _layout.getBaseTenConcreteUnitView(numLoc, TEN_DIGIT, i);
                    tenView.setOnClickListener(ALL_AT_ONCE ? _animator.generateWaterfallClickListener(numLoc, TEN_DIGIT, _data.operation) : _animator.generateSingleClickListener(TEN_DIGIT));
                }

            // add listeners to Hundreds
            if (_numDigits >=3)
                for (int i=1; i <= 5; i++) {

                    MovableImageView hunView = _layout.getBaseTenConcreteUnitView(numLoc, HUN_DIGIT, i);
                    hunView.setOnClickListener(ALL_AT_ONCE ? _animator.generateWaterfallClickListener(numLoc, HUN_DIGIT, _data.operation) : _animator.generateSingleClickListener(HUN_DIGIT));
                }

            // PART 2 (BOX) for (one, ten, hun) will move sequential ones. These may or may not (but probably will) be used.
            if (_numDigits >= 2) // containing box doesn't need to be moved
                _layout.getContainingBox(numLoc, ONE_DIGIT).setOnClickListener(ALL_AT_ONCE ? _animator.generateWaterfallClickListener(numLoc, ONE_DIGIT, _data.operation) : _animator.generateSequentialClickListener(ONE_DIGIT));

            if (_numDigits >= 2)
                _layout.getContainingBox(numLoc, TEN_DIGIT).setOnClickListener(ALL_AT_ONCE ? _animator.generateWaterfallClickListener(numLoc, TEN_DIGIT, _data.operation) :  _animator.generateSequentialClickListener(TEN_DIGIT));

            if (_numDigits >= 3)
                _layout.getContainingBox(numLoc, HUN_DIGIT).setOnClickListener(ALL_AT_ONCE ? _animator.generateWaterfallClickListener(numLoc, HUN_DIGIT, _data.operation) : _animator.generateSequentialClickListener(HUN_DIGIT));

        }

        // PART 3... carry
        if (_numDigits >= 2)
            _layout.getCarryConcreteUnitView(TEN_DIGIT).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _animator.moveTenCarry(v);
                }
            });

        if (_numDigits >= 3)
            _layout.getCarryConcreteUnitView(HUN_DIGIT).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _animator.moveHunCarry(v);
                }
            });


        // PART 4... borrow
        for (int i = 1; i <= 10; i++) {
            if (_numDigits >= 2)
                _layout.getBorrowConcreteUnitView(ONE_DIGIT, i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _animator.moveOneBorrow(v);
                    }
                });

            if (_numDigits >= 3)
                _layout.getBorrowConcreteUnitView(TEN_DIGIT, i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _animator.moveTenBorrow(v);
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
     * Define and initialize the WriteBox inputs.
     */
    private void initializeWriteInputs() {

        // initialize master container
        //_controller_master = (CGlyphController_Simple) findViewById(R.id.glyph_controller);
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

            //compute the expected answer and set the controller's expected character
            int expectedInt = _studentActionListener.getExpectedInt(_digitName);
            String expectedString = Integer.toString(expectedInt);
            _controller.setExpectedChar(expectedString);
            //set the expected character.
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
            _studentActionListener.fireAction(_digitName, "WRITE", result);
            // ruleEngine.registerWrite(character)

            return false;
        }

        @Override
        public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {
            return false;
        }
    }


    // ---------------------------------
    // ---- UI/LAYOUT MANIPULATION  ----
    // ---------------------------------

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
                    _hManager.cancelHesitation();
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
                    _hManager.cancelHesitation();
                    carryTen.setText("");
                }
            });
        }

        // set each digit separately
        // don't set leading zeros
        int digitsInOpA = String.valueOf(_data.dataset[0]).length();
        if (_numDigits >= 3) ((TextView) findViewById(R.id.symbol_opA_hun)).setText(digitsInOpA >= 3 ? String.valueOf(_problemState.getCurrentOpAHun()) : "");
        if (_numDigits >= 2) ((TextView) findViewById(R.id.symbol_opA_ten)).setText(digitsInOpA >= 2 ? String.valueOf(_problemState.getCurrentOpATen()) : "");
        if (_numDigits >= 1) ((TextView) findViewById(R.id.symbol_opA_one)).setText(digitsInOpA >= 1 ? String.valueOf(_problemState.getCurrentOpAOne()) : "");

        int digitsInOpB = String.valueOf(_data.dataset[1]).length();
        if (_numDigits >= 3) ((TextView) findViewById(R.id.symbol_opB_hun)).setText(digitsInOpB >= 3 ? String.valueOf(_problemState.getCurrentOpBHun()): "");
        if (_numDigits >= 2) ((TextView) findViewById(R.id.symbol_opB_ten)).setText(digitsInOpB >= 2 ? String.valueOf(_problemState.getCurrentOpBTen()): "");
        if (_numDigits >= 1) ((TextView) findViewById(R.id.symbol_opB_one)).setText(digitsInOpB >= 1 ? String.valueOf(_problemState.getCurrentOpBOne()): "");

        if (_numDigits >= 3) {
            final DigitView hunsResult = ((DigitView) findViewById(R.id.symbol_result_hun));
            hunsResult.setText(null);

            hunsResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _controller_master.setVisibility(View.VISIBLE);
                    _controller_master.setWritingController(new OnCharacterRecognizedListener(_controller_master, hunsResult)); // ROBO_MATH (5-WAIT)
                    _hManager.cancelHesitation();
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
                    _hManager.cancelHesitation();
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
                    _hManager.cancelHesitation();
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
                hun.isMovable = i <= hunsDigit; // MATH_MISC (tap)
            }
        }


        if (_numDigits >= 2) {
            tensDigit = getTensDigit(numberValue);
            for (int i = 1; i <= 10; i++) {
                ten = _layout.getBaseTenConcreteUnitView(numberLoc, TEN_DIGIT, i);
                ten.setImageDrawable(getDrawable(i <= tensDigit ? (ghost ? R.drawable.blue_ghost_10_h : R.drawable.blue_10_h ) : R.drawable.empty_10_h));
                ten.isMovable = i <= tensDigit;  // MATH_MISC (tap)
            }
        }

        onesDigit = getOnesDigit(numberValue);
        for (int i = 1; i <= 10; i++) {
            one = _layout.getBaseTenConcreteUnitView(numberLoc, ONE_DIGIT, i);
            one.setImageDrawable(getDrawable(i <= onesDigit ? (ghost ? R.drawable.blue_ghost_1 : R.drawable.blue_1 ) : R.drawable.empty_1));
            one.isMovable = i <= onesDigit;  // MATH_MISC (tap) (next... why are ghost ones tapping??)

            Log.wtf("YELLOW", "setting childOf=" + one.getResources().getResourceEntryName(((LinearLayout) one.getParent()).getId()));
            Log.wtf("YELLOW", "setting id ==\t" + one.getResources().getResourceEntryName(one.getId()));
            Log.wtf("YELLOW", "movable? ==\t" + ((MovableImageView) one).isMovable);
            Log.wtf("YELLOW", "one:" + i + " isMovable=" + one.isMovable);
        }


        // results row has an extra in each column
        if (numberLoc.equals(RESULT_LOCATION)) {
            one = _layout.getBaseTenConcreteUnitView(numberLoc, ONE_DIGIT, 10);
            //one.setVisibility(View.INVISIBLE); // only gets revealed by student action
            one.setVisibility(View.VISIBLE);
            one.setImageDrawable(getDrawable(R.drawable.empty_1));
            one.isMovable = false;  // MATH_MISC (tap)

            if (_numDigits >= 2) {
                ten = _layout.getBaseTenConcreteUnitView(numberLoc, TEN_DIGIT, 10);
                //ten.setVisibility(View.INVISIBLE); // only gets revealed by student action
                ten.setVisibility(View.VISIBLE);
                ten.setImageDrawable(getDrawable(R.drawable.empty_10_h));
                ten.isMovable = false;  // MATH_MISC (tap)
            }

            if (_numDigits >= 3) {
                hun = _layout.getBaseTenConcreteUnitView(numberLoc, HUN_DIGIT, 10);
                hun.setVisibility(View.VISIBLE);
                hun.setImageDrawable(getDrawable(R.drawable.empty_100));
                hun.isMovable = false;  // MATH_MISC (tap)
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
                _layout.getBaseTenDigitView(numberLoc, column).setTextColor(Color.BLACK); // idk why it's not already black? (see "addSubtractDigitColor" in colors.xml...)
            }

        }

    }

    // -------------------------------------
    // ---- END UI/LAYOUT MANIPULATION  ----
    // -------------------------------------

    /**
     * prevent other rows from being tapped on
     * @param digit
     */
    public void disableConcreteUnitTappingForOtherRows(String digit) {

        // MATH_MISC (tap)
        switch (digit) {
            case ONE_DIGIT:
                _problemState.setCanTapOnes(true);
                _problemState.setCanTapTens(false);
                _problemState.setCanTapHuns(false);
                break;
            case TEN_DIGIT:
                _problemState.setCanTapOnes(false);
                _problemState.setCanTapTens(true);
                _problemState.setCanTapHuns(false);
                break;
            case HUN_DIGIT:
                _problemState.setCanTapOnes(false);
                _problemState.setCanTapTens(false);
                _problemState.setCanTapHuns(true);
                break;
        }


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
                    MovableImageView unit = _layout.getBaseTenConcreteUnitView(numberLoc, column, i);
                    unit.isMovable = unit.isMovable && enableThisColumn; // TODO this may break individual behavior for tens... but since it's in waterfall mode, it will work
                }
            }

        }
    }

    /* DEBUG BUTTONS */

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return _activity.getDrawable(resID);
        } else {
            return _activity.getResources().getDrawable(resID, _activity.getTheme());
        }
    }
}
