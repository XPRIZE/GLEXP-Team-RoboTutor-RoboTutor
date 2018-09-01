package cmu.xprize.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 7/17/18.
 */

public class MathUtil {


    /**
     * Get hundreds digit of a 3-digit number.
     *
     * @param numberValue must be 3 digits or less.
     * @return hundreds digit
     */
    public static int getHunsDigit(int numberValue) {
        return numberValue / 100;
    }

    /**
     * Get tens digit of a number.
     *
     * @param numberValue must be positive.
     * @return tens digit
     */
    public static int getTensDigit(int numberValue) {
        return (numberValue / 10) % 10;
    }

    /**
     * Get ones digit of a number.
     *
     * @param numberValue must be positive.
     * @return ones digit
     */
    public static int getOnesDigit(int numberValue) {
        return numberValue % 10;
    }

}
