package cmu.xprize.comp_numberscale;

import android.graphics.Color;

/**
 * Created by yuanx on 3/2/2018.
 */

public class NSCONST {
    public static final int COLOR_BLUE = Color.rgb(51, 181, 229);
    public static final int COLOR_GREY = Color.rgb(238, 238, 238);
    public static final int COLOR_WHITE = Color.rgb(255, 255, 255);
    public static final int COLOR_DARKGREY = Color.rgb(147, 147, 147);
    public static final int BAR_MARGIN = 100;
    public static final int TOP_MARGIN = 1200;
    public static final int BOX_MARGIN = 15;
    public static final int TEXT_SIZE = 80;
    public static final float BOX_BOUNDARY_STROKE_WIDTH = 10.0f;

    public static final String PLAY_CHIME  = "PLAY_CHIME";
    public static final String PLAY_NA  = "PLAY_NA";
    public static final String PLAY_CHIME_PLUS  = "PLAY_CHIME_PLUS";
    public static final String PLAY_CHIME_PPLUS  = "PLAY_CHIME_PPLUS";
    public static final String PLAY_INTRO = "PLAY_INTRO";
    public static final String PLAY_TUTOR_PLUS = "PLAY_TUTOR_PLUS";
    public static final String PLAY_TUTOR_MINUS = "PLAY_TUTOR_MINUS";
    public static final String MAX_HIT_REACHED= "MAX_HIT_REACHED";

    public static final String[][] CHIMES = {
            {"49", "51", "53", "54", "56", "57", "58", "59", "60", "61"},
            {"37", "39", "41", "42", "44", "45", "46", "47", "48", "49"},
            {"25", "27", "29", "30", "32", "33", "34", "35", "36", "37"},
            {"13", "15", "17", "18", "20", "21", "22", "23", "24", "25"}
    };




}

