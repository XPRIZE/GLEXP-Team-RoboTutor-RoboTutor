package cmu.xprize.comp_counting2;

import android.graphics.Color;

/**
 * Created by kevindeland on 12/13/17.
 */

public class COUNTX_CONST {

    // drawing parameters
    public static final int COLOR_PINK = Color.parseColor("#FFDFEA");
    public static final int COLOR_BACKGROUND = Color.rgb(230, 230, 230);
    public static final int COLOR_YELLOW = Color.rgb(247, 245, 190);
    public static final int COLOR_DARKGREEN =  Color.rgb(144,238,144);
    public static final int COLOR_LIGHTGREEN =  Color.rgb(239, 252, 240);
    public static final int COLOR_BLUE = Color.rgb(51, 181, 229);
    public static final int COLOR_RED = Color.rgb(255, 0, 0);
    public static final int TEXT_SIZE = 150;
    public static final int RESULT_SIZE = 200;

    public static final int COLOR_GREY = Color.rgb(238, 238, 238);
    public static final int COLOR_DARKGREY = Color.rgb(147, 147, 147);

    public static final int DRAWABLE_RADIUS = 100;
    public static final int BOX_MARGIN = 100;
    public static final int BOX_MARGINRIGHT = 100;
    public static final int BOX_MARGINBOTTOM = 300;
    public static final int BOX_TEXTBOTTOM = 100;

    public static final int BOXM = 50;
    public static final int GRID_MARGIN = 30;
    public static final float BOX_BOUNDARY_STROKE_WIDTH = 5.0f;

    public static final boolean USE_JAIL_BARS = true;

    // actions
    public static final String PLAY_COUNT = "PLAY_COUNT";
    public static final String PLAY_CHIME = "PLAY_CHIME";
    public static final String PLAY_CHIME_PLUS = "PLAY_CHIME_PLUS";
    public static final String PLAY_AUDIO = "PLAY_AUDIO";
    public static final String PLAY_TWO_ADDITION = "PLAY_TWO_ADDITION";
    public static final String PLAY_THREE_ADDITION = "PLAY_THREE_ADDITION";
    public static final String PLAY_FINAL_COUNT = "PLAY_FINAL_COUNT";
    public static final String PLACEVALUE_INS_H = "PLACEVALUE_INS_H";
    public static final String PLACEVALUE_INS_T = "PLACEVALUE_INS_T";
    public static final String PLACEVALUE_INS_O = "PLACEVALUE_INS_O";
    public static final String WRITTING_INS = "WRITTING_INS";

    public static final String DONE_COUNTING_TO_N = "DONE_COUNTING_TO_N";

    // FEATURES
    public static final String FTR_COUNTX = "FTR_COUNTX";
    public static final String FTR_PLACEVALUE = "FTR_PLACEVALUE";

    public static final String DONE_MOVING_TO_TEN_FRAME = "DONE_MOVING_TO_TEN_FRAME";
    public static final String DONE_SAYING_FINAL_COUNT = "DONE_SAYING_FINAL_COUNT";

    public static final String[][] CHIMES = {
            {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
            {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
            {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
            {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
    };


}
