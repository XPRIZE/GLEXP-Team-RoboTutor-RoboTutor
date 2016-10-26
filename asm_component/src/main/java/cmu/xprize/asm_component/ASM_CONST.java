package cmu.xprize.asm_component;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import cmu.xprize.util.TCONST;

public class ASM_CONST {

    public static final String LOCAL_FILE = "LOCAL_FILE";
    public static final String LOCAL_FILE_PATH = "/sdcard/Arithmetic/asm_data.txt";

    //for add and subtract
    public static final int ANIMATOR3    = 1;
    public static final int ANIMATOR2    = 2;
    public static final int ANIMATOR1    = 3;
    public static final int OVERHEAD     = 4;
    public static final int REGULAR      = 5;
    public static final int OPERATION    = 6;
    public static final int RESULT       = 7;

    //for multiplication
    public static final int REGULAR_MULTI                  = 1;
    public static final int OPERATION_MULTI                = 2;
    public static final int RESULT_OR_ADD_MULTI_PART1     = 3;

    public static final int alleyMargin  = 10;
    public static final int alleyMarginMul  = 3;
    public static final int rightPadding = 15;

    public static final int textSize     = 18;
    public static final int textSizeMul     = 14;

    public static final int textBoxWidth = 30;
    public static final int textBoxHeight= 50;
    public static final int textBoxWidthMul = 20;
    public static final int textBoxHeightMul= 30;

    public static final int borderWidth  = 2;

    public static final int DESIGNWIDTH  = 2560;
    public static final int DESIGNHEIGHT = 1620;

    public static final String[][] CHIMES = {
            {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
            {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
            {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
            {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
    };

    public static final int NO_INPUT_TO_OVERHEAD = -1;
    public static final int ALL_INPUT_TO_OVERHEAD_RIGHT = 0;
    public static final int NOT_ALL_INPUT_TO_OVERHEAD_RIGHT = 1;

    public static final String NUMBER_PREFIX = "Write the ";
    public static final Map<Integer, String> writeNextNumber = new HashMap<Integer, String>();

    static {
        writeNextNumber.put(0, NUMBER_PREFIX + "last");
        writeNextNumber.put(1, NUMBER_PREFIX + "first");
        writeNextNumber.put(2, NUMBER_PREFIX + "second");
        writeNextNumber.put(3, NUMBER_PREFIX + "third");
        writeNextNumber.put(4, NUMBER_PREFIX + "fourth");
        writeNextNumber.put(5, NUMBER_PREFIX + "fifth");
        writeNextNumber.put(6, NUMBER_PREFIX + "sixth");
        writeNextNumber.put(7, NUMBER_PREFIX + "seventh");
        writeNextNumber.put(8, NUMBER_PREFIX + "eighth");
        writeNextNumber.put(9, NUMBER_PREFIX + "ninth");
    }

    public static final String RESULT_PREFIX = "Now add the ";
    public static final String FIRST_TWO = "first two";
    public static final String NEXT = "next";
    public static final String LAST = "last";

    public static final String HIGHLIGHT_OVERHEAD = "HIGHLIGHT_OVERHEAD";
    public static final String HIGHLIGHT_RESULT   = "HIGHLIGHT_RESULT";
}
