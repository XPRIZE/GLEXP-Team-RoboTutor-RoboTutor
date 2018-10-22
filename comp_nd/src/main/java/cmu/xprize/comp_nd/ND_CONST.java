package cmu.xprize.comp_nd;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 8/14/18.
 */

public final class ND_CONST {

    // Java variables
    public static final int MAX_HUNS = 4;
    public static final int MAX_TENS = 9;
    public static final int MAX_ONES = 9;



    // Java constants
    public static final String LEFT_NUM = "left";
    public static final String RIGHT_NUM = "right";

    public static final String HUN_DIGIT = "hun";
    public static final String TEN_DIGIT = "ten";
    public static final String ONE_DIGIT = "one";
    public static final String NO_DIGIT = "null";


    // Features.
    static final String FTR_CORRECT = "FTR_CORRECT";
    static final String FTR_WRONG = "FTR_WRONG";

    static final String FTR_ONE_FIRST = "FTR_ONE_FIRST";
    static final String FTR_TEN_FIRST = "FTR_TEN_FIRST";

    // Features that help with number pronunciation
    static final String FTR_SAY_HUNS = "FTR_SAY_HUNS";
    static final String FTR_SAY_NA_TENS = "FTR_SAY_NA_TENS";
    static final String FTR_SAY_TENS = "FTR_SAY_TENS";
    static final String FTR_SAY_NA_ONES = "FTR_SAY_NA_ONES";
    static final String FTR_SAY_ONES = "FTR_SAY_ONES";

    // actionMap???
    static final String NEXTNODE = "NEXTNODE";


    // Named Posts.
    public static final String HESITATION_PROMPT = "HESITATION_PROMPT";


    // queueMap entries
    public static final String INPUT_HESITATION_FEEDBACK = "INPUT_HESITATION_FEEDBACK";
    public static final String HIGHLIGHT_HUNS = "HIGHLIGHT_HUNS";
    public static final String HIGHLIGHT_TENS = "HIGHLIGHT_TENS";
    public static final String HIGHLIGHT_ONES = "HIGHLIGHT_ONES";
    public static final String INDICATE_CORRECT = "INDICATE_CORRECT";

    // actionMap entries
    // cancellation nodes
    public static final String CANCEL_HIGHLIGHT_HUNS = "CANCEL_HIGHLIGHT_HUNS";
    public static final String CANCEL_HIGHLIGHT_TENS = "CANCEL_HIGHLIGHT_TENS";
    public static final String CANCEL_HIGHLIGHT_ONES = "CANCEL_HIGHLIGHT_ONES";
    public static final String CANCEL_INDICATE_CORRECT = "CANCEL_INDICATE_CORRECT";

    // for Logging
    static final String LOG_SCAFFOLD = "ND_SCAFFOLD";


    // Values to Publish
    public static final String VALUE_HUN = ".hun";
    public static final String VALUE_TEN = ".ten";
    public static final String VALUE_ONE = ".one";

    public static final String VALUE_DIGIT_MORE = ".digitMore";
    public static final String VALUE_DIGIT_COMPARE = ".digitCompare";
    public static final String VALUE_DIGIT_LESS = ".digitLess";


    public static final int HESITATION_DELAY = 6000;
}
