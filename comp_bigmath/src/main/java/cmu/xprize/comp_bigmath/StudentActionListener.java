package cmu.xprize.comp_bigmath;

/**
 * android-ConstraintLayoutExamples
 * <p>
 * Created by kevindeland on 8/7/18.
 */

interface StudentActionListener {

    void fireAction(String selection, String action, String input);

    int getExpectedInt(String selection);
    // initialize a new problem
    void setData(CBigMath_Data data, int numDigits);
}
