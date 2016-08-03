package cmu.xprize.asm_component;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class ASM_CONST {

    public static final int ANIMATOR     = 1;
    public static final int OVERHEAD     = 2;
    public static final int REGULAR      = 3;
    public static final int OPERATION    = 4;
    public static final int RESULT       = 5;

    public static final int alleyMargin  = 20;
    public static final int rightPadding = 20;

    public static final int textSize     = 15;
    public static final int textBoxWidth = textSize*2;
    public static final int textBoxHeight= textSize*4;


    public static final int borderWidth  = 3;

    public static final int DESIGNWIDTH  = 2560;
    public static final int DESIGNHEIGHT = 1620;

    public static final String[][] CHIMES = {
                                                {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
                                                {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
                                                {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
                                                {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
                                            };

}
