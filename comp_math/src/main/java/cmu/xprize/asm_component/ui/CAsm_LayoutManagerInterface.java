package cmu.xprize.asm_component.ui;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 7/17/18.
 */

public interface CAsm_LayoutManagerInterface {

    /**
     * Initialize the Layout.
     */
    void initialize();

    /**
     * Initialize a problem op1 (plus or minus) op2.
     *
     * @param op1
     * @param op2
     * @param operator
     */
    void initializeProblem(int op1, int op2, int result, String operator);


    // MATHFIX_BUILD start listing what methods might be needed


    /**
     * Emphasize (bold, highlight, etc) the current digit (one, ten, hun, etc)
     *
     * @param digit
     */
    void emphasizeCurrentDigitColumn(String digit);

    /**
     * Show the DotBag with Dots for the given digit
     *
     * @param digit
     * @param op1
     * @param op2
     */
    void showDotBagsForDigit(String digit, int op1, int op2);


    /**
     * Wiggle the digit and dotbag left and right.
     *
     * @param numLocation
     * @param digit
     */
    void wiggleDigitAndDotbag(String numLocation, String digit);

    /**
     * Whatever the number you just wrote, mark it wrong.
     *
     * @param digit
     */
    void wrongDigit(String digit);
}
