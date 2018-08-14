package cmu.xprize.comp_nd.ui;

/**
 * CNd_LayoutManagerInterface
 *
 * This provides an abstraction for manipulating the Layout/View of the Number Discrimination Tutor.
 * The purpose of its creation is to de-couple tutor logic and interface manipulation.
 *
 * <p>
 * Created by kevindeland on 7/17/18.
 */

public interface CNd_LayoutManagerInterface {

    /**
     * Initialize the Layout.
     */
    void initialize();

    /**
     * Reset the View for the next problem.
     */
    void resetView();


    /**
     * Display digit text
     *
     * @param left
     * @param right
     */
    void displayDigits(int left, int right);

    /**
     * Display concrete representation
     *
     * @param left
     * @param right
     */
    void displayConcreteRepresentations(int left, int right);


    /**
     * Enable or disable ability to pick a number by clicking.
     * @param enable
     */
    void enableChooseNumber(boolean enable);


    /**
     * Highlight a digit for both numbers.
     *
     * @param digit
     */
    void highlightDigit(String digit);
}
