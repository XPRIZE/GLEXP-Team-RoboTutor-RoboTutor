package cmu.xprize.comp_counting2;

import android.graphics.Color;

/**
 * Created by kevindeland on 12/13/17.
 */

public class COUNTX_CONST {

    // drawing parameters
    public static final int COLOR_PINK = Color.rgb(255, 150, 150);
    public static final int COLOR_BACKGROUND = Color.LTGRAY;
    public static final int DRAWABLE_RADIUS = 100;
    public static final int BOX_MARGIN = 100;
    public static final float BOX_BOUNDARY_STROKE_WIDTH = 10.0f;

    public static final boolean USE_JAIL_BARS = true;

    // actions
    public static final String PLAY_CHIME = "PLAY_CHIME";
    public static final String DONE_COUNTING_TO_N = "DONE_COUNTING_TO_N";

    public static final String DONE_MOVING_TO_TEN_FRAME = "DONE_MOVING_TO_TEN_FRAME";

    public static final String[][] CHIMES = {
            {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
            {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
            {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
            {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
    };


}
