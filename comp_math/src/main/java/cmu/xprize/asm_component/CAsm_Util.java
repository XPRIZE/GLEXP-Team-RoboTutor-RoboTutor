package cmu.xprize.asm_component;

/**
 *
 */
public class CAsm_Util {

    public static String[] intToDigits(int toConvert, int numSlots) {

        char[] charArray = String.valueOf(toConvert).toCharArray();
        String[] toReturn = new String[numSlots];
        int numBlanks = numSlots - charArray.length;

        for (int i = 0; i < numBlanks; i++) {
            toReturn[i] = "";
        }
        for (int i = numBlanks; i < numSlots; i++) {
            toReturn[i] = Character.toString(charArray[i - numBlanks]);
        }

        return toReturn;

    }

    /**
     * Looks through an array of numbers and finds the highest number of digits
     *
     * @param nums
     * @return
     */
    public static int maxDigits(int[] nums) {

        int max = 0;
        int numDigits;

        for (int num: nums) {

            numDigits = String.valueOf(num).toCharArray().length;
            max = (max>numDigits)?max:numDigits;

        }

        return max;
    }
}
