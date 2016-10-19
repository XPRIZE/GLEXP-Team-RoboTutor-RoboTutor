package cmu.xprize.asm_component;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class ASM_CONST {

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

    public static final int alleyMargin  = 3;
    public static final int alleyMarginMul  = 1;
    public static final int rightPadding = 15;

    public static final int textSize     = 18;
    public static final int textSizeMul     = 14;

    public static final int textBoxWidth = 30;
    public static final int textBoxHeight= 50;
    public static final int textBoxWidthMul = 20;
    public static final int textBoxHeightMul= 28;

    public static final int borderWidth  = 2;

    public static final int DESIGNWIDTH  = 2560;
    public static final int DESIGNHEIGHT = 1620;

    public static final String[][] CHIMES = {
            {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
            {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
            {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
            {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
    };

}
