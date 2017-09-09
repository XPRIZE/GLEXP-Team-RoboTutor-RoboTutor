package cmu.xprize.ltkplus;

import java.util.HashMap;

public class GCONST {


    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-+<=>!\"#%&?@()/.,;:\'";
    public static final boolean NOFORCE = false;
    public static final boolean FORCE   = true;

    public static final String BOOST_ALPHA    = "Boost Alphabetic";
    public static final String BOOST_DIGIT    = "Boost Digits";
    public static final String FORCE_DIGIT    = "Force Digits";
    public static final String BOOST_EXCLASS  = "Boost Expected Class";
    public static final String NO_BOOST = "No Boost";
    public static final String RECORD_VERSION = "1.0.0";
    public static final String VERSION_0      = "0";            // Used in RoboTutor.0.4.1 and earlier

    public static final float  LINE_WEIGHT        = 6;
    public static final float  CORNER_RAD         = 40;
    public static final int    EXPECTED_NOT_FOUND = -1;
    public static final int    EXPECT_NONE        = -2;
    public static final float  CALIBRATED_WEIGHT  = 0f;
    public static final float  CALIBRATION_CONST  = 14.288889f;

    public static final String X_CONSTR           = "X";
    public static final String Y_CONSTR           = "Y";
    public static final String W_CONSTR           = "W";
    public static final String H_CONSTR           = "H";
    public static final String A_CONSTR           = "A";

    public static final String FTR_POSHORZ_VIOLATION = "FTR_POSHORZ_VIOLATION";
    public static final String FTR_LEFT_VIOLATION    = "FTR_LEFT_VIOLATION";
    public static final String FTR_RIGHT_VIOLATION   = "FTR_RIGHT_VIOLATION";
    public static final String FTR_POSVERT_VIOLATION = "FTR_POSVERT_VIOLATION";
    public static final String FTR_HIGH_VIOLATION    = "FTR_HIGH_VIOLATION";
    public static final String FTR_LOW_VIOLATION     = "FTR_LOW_VIOLATION";

    public static final String FTR_WIDTH_VIOLATION   = "FTR_WIDTH_VIOLATION";
    public static final String FTR_WIDE_VIOLATION    = "FTR_WIDE_VIOLATION";
    public static final String FTR_NARROW_VIOLATION  = "FTR_NARROW_VIOLATION";
    public static final String FTR_HEIGHT_VIOLATION  = "FTR_HEIGHT_VIOLATION";
    public static final String FTR_TALL_VIOLATION    = "FTR_TALL_VIOLATION";
    public static final String FTR_SHORT_VIOLATION   = "FTR_SHORT_VIOLATION";
    public static final String NO_LOG                = "<no_log>";

    static public HashMap<String, String> glyphMap = new HashMap<String, String>();

    static {
        glyphMap.put("a", "a");
        glyphMap.put("b", "b");
        glyphMap.put("c", "c");
        glyphMap.put("d", "d");
        glyphMap.put("e", "e");
        glyphMap.put("f", "f");
        glyphMap.put("g", "g");
        glyphMap.put("h", "h");
        glyphMap.put("i", "i");
        glyphMap.put("j", "j");
        glyphMap.put("k", "k");
        glyphMap.put("l", "l");
        glyphMap.put("m", "m");
        glyphMap.put("n", "n");
        glyphMap.put("o", "o");
        glyphMap.put("p", "p");
        glyphMap.put("q", "q");
        glyphMap.put("r", "r");
        glyphMap.put("s", "s");
        glyphMap.put("t", "t");
        glyphMap.put("u", "u");
        glyphMap.put("v", "v");
        glyphMap.put("w", "w");
        glyphMap.put("x", "x");
        glyphMap.put("y", "y");
        glyphMap.put("z", "z");

        glyphMap.put("A", "A__");
        glyphMap.put("B", "B__");
        glyphMap.put("C", "C__");
        glyphMap.put("D", "D__");
        glyphMap.put("E", "E__");
        glyphMap.put("F", "F__");
        glyphMap.put("G", "G__");
        glyphMap.put("H", "H__");
        glyphMap.put("I", "I__");
        glyphMap.put("J", "J__");
        glyphMap.put("K", "K__");
        glyphMap.put("L", "L__");
        glyphMap.put("M", "M__");
        glyphMap.put("N", "N__");
        glyphMap.put("O", "O__");
        glyphMap.put("P", "P__");
        glyphMap.put("Q", "Q__");
        glyphMap.put("R", "R__");
        glyphMap.put("S", "S__");
        glyphMap.put("T", "T__");
        glyphMap.put("U", "U__");
        glyphMap.put("V", "V__");
        glyphMap.put("W", "W__");
        glyphMap.put("X", "X__");
        glyphMap.put("Y", "Y__");
        glyphMap.put("Z", "Z__");

        glyphMap.put("0", "0");
        glyphMap.put("1", "1");
        glyphMap.put("2", "2");
        glyphMap.put("3", "3");
        glyphMap.put("4", "4");
        glyphMap.put("5", "5");
        glyphMap.put("6", "6");
        glyphMap.put("7", "7");
        glyphMap.put("8", "8");
        glyphMap.put("9", "9");

        glyphMap.put("-", "minus");
        glyphMap.put("+", "plus");

        glyphMap.put("<", "less");
        glyphMap.put("=", "equal");
        glyphMap.put(">", "greater");

        glyphMap.put("!", "exclamation");
        glyphMap.put("\"", "doublequote");

        glyphMap.put("#", "hash");
        glyphMap.put("%", "percent");
        glyphMap.put("&", "ampersand");
        glyphMap.put("?", "question");

        glyphMap.put("@", "atsign");
        glyphMap.put("(", "leftparen");
        glyphMap.put(")", "rightparen");

        glyphMap.put("/", "foreslash");

        glyphMap.put(".", "period");
        glyphMap.put(",", "comma");
        glyphMap.put(";", "semicolon");
        glyphMap.put(":", "colon");
        glyphMap.put("\'", "singlequote");
    }

    // TODO: make getStroke weight scale sensitive
    //
    public static final float    REPLAY_WEIGHT = 5f;
    public static final float    STROKE_WEIGHT = 45f;


    // We are correcting for specific LipiTK named recognizer errors (i.e. the ALPHANUM recognizer) -
    // Sometime it doesn't produce certain characters that you'd think it should due to training
    // deficiencies - we enumaerate those characters here -
    //
    static public HashMap<String, Boolean> boostMap = new HashMap<String, Boolean>();

    static {
        boostMap.put("i", true);
    }
}
