package cmu.xprize.asm_component;


import android.graphics.Color;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ASM_CONST {

    //the path of local data file
    public static final String LOCAL_FILE = "LOCAL_FILE";
    public static final String LOCAL_FILE_PATH = "/sdcard/Arithmetic/asm_data.txt";


    //used to judge the current node in animator_graph
    public static final String NODE_USERINPUT = "USERINPUT";
    public static final String NODE_ADD_PROMPT = "ADD_PROMPT";
    public static final String NODE_SUB_PROMPT = "SUB_PROMPT";

    //for add and subtract
    public static final int ANIMATOR3    = 1;
    public static final int ANIMATOR2    = 2;
    public static final int ANIMATOR1    = 3;
    public static final int CARRY_BRW = 4;
    public static final int OPERAND_ROW  = 5;
    public static final int OPERATOR_ROW = 6;
    public static final int RESULT_ROW   = 7;


    public static final int alleyMargin     = 10;
    public static final int rightPadding    = 15;

    public static final int textSize     = 18;  // 18

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

    //used to judge which audio to play after user input
    public static final int NO_INPUT = -1;
    public static final int ALL_INPUT_RIGHT = 0;
    public static final int NOT_ALL_INPUT_RIGHT = 1;

    //used to play the prompts in multiplication when showing repeated addition
    public static final String NUMBER_PREFIX = "Write the ";
    public static final Map<Integer, String> writeNextNumber = new HashMap<Integer, String>();


    public static final String SHOW_BAG_BEHAVIOR        = "SHOW_BAG_BEHAVIOR";
    public static final String ON_ANSWER_BEHAVIOR       = "ON_ANSWER_BEHAVIOR";
    public static final String SCAFFOLD_RESULT_BEHAVIOR = "SCAFFOLD_RESULT_BEHAVIOR";
    public static final String SHOW_SCAFFOLD_BEHAVIOR   = "SHOW_SCAFFOLD_BEHAVIOR";
    public static final String CHIME_FEEDBACK           = "CHIME_FEEDBACK";
    public static final String INPUT_BEHAVIOR           = "INPUT_BEHAVIOR";
    public static final String START_WRITING_BEHAVIOR   = "START_WRITING_BEHAVIOR";
    public static final String NEXT_NODE                = "NEXT_NODE";

    public static final String FTR_NO_ZEROS             = "FTR_NO_ZEROS";
    public static final String FTR_OP_ZERO              = "FTR_OP_ZERO";
    public static final String FTR_ZERO_OP              = "FTR_ZERO_OP";
    public static final String FTR_ALL_ZEROS            = "FTR_ALL_ZEROS";

    public static final String MATH_INSTRUCTION_ADD_BEHAVIOR    = "MATH_INSTRUCTION_ADD_BEHAVIOR";
    public static final String MATH_INSTRUCTION_ADD_0_BEHAVIOR  = "MATH_INSTRUCTION_ADD_0_BEHAVIOR";
    public static final String MATH_INSTRUCTION_0_ADD_BEHAVIOR  = "MATH_INSTRUCTION_0_ADD_BEHAVIOR";
    public static final String MATH_INSTRUCTION_SUB_BEHAVIOR    = "MATH_INSTRUCTION_SUB_BEHAVIOR";
    public static final String MATH_INSTRUCTION_SUB_0_BEHAVIOR  = "MATH_INSTRUCTION_SUB_0_BEHAVIOR";


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

    //indicate which thing to highlight
    public static final String HIGHLIGHT_OVERHEAD = "HIGHLIGHT_OVERHEAD";
    public static final String HIGHLIGHT_RESULT   = "HIGHLIGHT_RESULT";

    // DEBUG VARIABLES
    public static final boolean USE_NEW_MATH = true;
    public static final String TAG_DEBUG_MATHFIX = "MATHFIX";
    public static final String TAG_DEBUG_MATHFIX_UI_REF = "MATHFIX_UI_REF";
    public static final String ANNOYING_REFERENCE_STRING = "Alley(%d).Layout().Layout(%d).Text(%d) -- %s";

    public static final void logAnnoyingReference(int alleyId, int textLayoutId, int textId, String descriptor) {
        Log.d(TAG_DEBUG_MATHFIX_UI_REF, String.format(Locale.getDefault(), ASM_CONST.ANNOYING_REFERENCE_STRING, alleyId, textLayoutId, textId, descriptor));
    }


    public static final int DEBUG_TEXT_COLOR = Color.parseColor("#ff22bb");
    public static final int DEBUG_TEXTLAYOUT_1_COLOR = Color.parseColor("#22ffbb");
    public static final int DEBUG_TEXTLAYOUT_2_COLOR = Color.parseColor("#bb22ff");
}
