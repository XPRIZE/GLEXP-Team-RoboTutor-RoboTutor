//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.bp_component;

// global tutor constants

import java.util.HashMap;

public class BP_CONST {

    public static final String[] bubbleColors = {"RED", "BLUE", "PINK", "ORANGE", "GREEN"};

    public static final int BUBBLE_SAMPLE = R.drawable.bubble_r_0;
    public static final int RED_BUBBLE    = R.drawable.bubble_r_0;
    public static final int BLUE_BUBBLE   = R.drawable.bubble_b_0;
    public static final int PINK_BUBBLE   = R.drawable.bubble_p_0;
    public static final int ORANGE_BUBBLE = R.drawable.bubble_o_0;
    public static final int GREEN_BUBBLE  = R.drawable.bubble_g_0;

    public static final int RED_ELONGATED_BUBBLE = R.drawable.elongated_bubble_r;
    public static final int BLUE_ELONGATED_BUBBLE = R.drawable.elongated_bubble_b;
    public static final int PINK_ELONGATED_BUBBLE = R.drawable.elongated_bubble_p;
    public static final int ORANGE_ELONGATED_BUBBLE = R.drawable.elongated_bubble_o;
    public static final int GREEN_ELONGATED_BUBBLE = R.drawable.elongated_bubble_g;

    public static final int RED_BUBBLE_VIBE    = R.drawable.bubble_r_vib;
    public static final int BLUE_BUBBLE_VIBE   = R.drawable.bubble_b_vib;
    public static final int PINK_BUBBLE_VIBE   = R.drawable.bubble_p_vib;
    public static final int ORANGE_BUBBLE_VIBE = R.drawable.bubble_o_vib;
    public static final int GREEN_BUBBLE_VIBE  = R.drawable.bubble_g_vib;

    public static final String REFERENCE = "reference";
    public static final String TEXTDATA  = "text_data";

    public static final int MAX = 1;
    public static final int MIN = 0;

    public static final String SHOW_BUBBLES      = "SHOW_BUBBLES";
    public static final String CLEAR_CONTENT     = "CLEAR_CONTENT";
    public static final String SPAWN_BUBBLE      = "SPAWN_BUBBLE";
    public static final String WIGGLE_BUBBLE     = "WIGGLE_BUBBLE";
    public static final String POP_BUBBLE        = "POP_BUBBLE";
    public static final String INFLATE           = "INFLATE_BUBBLE";
    public static final long   INFLATE_DELAY     = 200;
    public static final String SHOW_STIMULUS     = "SHOW_STIMULUS";
    public static final String REMOVE_BUBBLE     = "REMOVE_BUBBLE";
    public static final String REPLACE_BUBBLE    = "REPLACE_BUBBLE";
    public static final String ZOOM_STIMULUS     = "ZOOM_STIMULUS";
    public static final String MOVE_STIMULUS     = "MOVE_STIMULUS";

    public static final float[] AUDIO_SCALE_RANGE = new float[]{1.05f, 1.1f};

    public static final float  BOUNCE_MAGNITUDE  = 0.16f;
    public static final float  STRETCH_MAGNITUDE = 1.21f;
    public static final float  MIN_VRANGE        = .80f;
    public static final int    MARGIN_LEFT       = 200;
    public static final int    MARGIN_TOP        = 0;
    public static final int    MARGIN_RIGHT      = 200;
    public static final int    MARGIN_BOTTOM     = 250;
    public static final float  STIM_PAD_BOTTOM   = 15;

    public static final float  ANGLE_MIN         = 0;
    public static final float  ANGLE_MAX         = (float) (Math.PI / 3.0);

    public static final float STRETCH_MAX         = 1.21f;
    public static final float STRETCH_MIN         = 0.80f;

    public static final int DESIGNWIDTH           = 2560;
    public static final int DESIGNHEIGHT          = 1620;
    public static final int BUBBLE_DESIGN_RADIUS  = 210;
    public static final float DESIGN_SCALE        = 2.0f;

    public static final String STIMULUS_SHOWN     = "STIMULUS_SHOWN";
    public static final String BUBBLE_TOUCH_EVENT = "BUBBLE_TOUCH_EVENT";
    public static final String BUBBLE_POPPED      = "BUBBLE_POPPED";
    public static final String BUBBLE_WIGGLED     = "BUBBLE_WIGGLED";
    public static final String BUBBLES_CLEARED    = "BUBBLES_CLEARED";
    public static final String RANDOM             = "RANDOM";
    public static final String SEQUENTIAL         = "SEQUENTIAL";
    public static final int    FEEDBACK_SIZE      = 300;
    public static final String SHOW_FEEDBACK      = "SHOW_FEEDBACK";
    public static final String FEEDBACK_SHOWN     = "FEEDBACK_SHOWN";
    public static final String ZOOM_FEEDBACK      = "ZOOM_FEEDBACK";
    public static final String SHOW_SCORE         = "SHOW_SCORE";
    public static final String PAUSE_ANIMATION    = "PAUSE_ANIMATION";
    public static final String RESUME_ANIMATION   = "RESUME_ANIMATION";
    public static final int    MAX_ATTEMPT        = 3;

    public static final String SOUND_TRACK        = ".sound_track";
    public static final String QUEST_VAR          = ".questValue";

    public static final String ANSWER_VAR         = ".ansValue";

    public static final String QUEST_VAR_TWO      = ".questValueTwo";
    public static final String ANSWER_VAR_TWO     = ".ansValueTwo";

    public static final String QUEST_VAR_THREE    = ".questValueThree";
    public static final String ANSWER_VAR_THREE   = ".ansValueThree";

    public static final String ANS_VAR         = ".ansValue";

    public static final String QUEST_VAR_HUNDREDS = ".questValueHundreds";
    public static final String QUEST_VAR_TENS     = ".questValueTens";
    public static final String QUEST_VAR_ONES      = ".questValueOnes";
    public static final String ANS_VAR_HUNDREDS= ".ansValueHundreds";
    public static final String ANS_VAR_TENS    = ".ansValueTens";
    public static final String ANS_VAR_ONES    = ".ansValueOnes";

    public static final String QUEST_VAR_STIM_ONE_HUNDREDS = ".questValueStimOneHundreds";
    public static final String QUEST_VAR_STIM_ONE_TENS = ".questValueStimOneTens";
    public static final String QUEST_VAR_OPERAND  = ".questValueOperand";
    public static final String QUEST_VAR_STIM_TWO_HUNDREDS = ".questValueStimTwoHundreds";
    public static final String QUEST_VAR_STIM_TWO_TENS = ".questValueStimTwoTens";

    public static final String ANS_VAR_STIM_ONE_HUNDREDS= ".ansValueStimOneHundreds";
    public static final String ANS_VAR_STIM_ONE_TENS= ".ansValueStimOneTens";
    public static final String ANS_VAR_OPERAND = ".ansValueOperand";
    public static final String ANS_VAR_STIM_TWO_HUNDREDS = ".ansValueStimTwoHundreds";
    public static final String ANS_VAR_STIM_TWO_TENS = ".ansValueStimTwoTens";


    public static final String FTR_ANS_HUNDREDS  = "FTR_ANS_HUNDREDS";
    public static final String FTR_ANS_TENS      = "FTR_ANS_TENS";
    public static final String FTR_QUEST_HUNDREDS = "FTR_QUEST_HUNDREDS";
    public static final String FTR_QUEST_TENS     = "FTR_QUEST_TENS";

    public static final String FTR_ANS_STIM_ONE_HUNDREDS  = "FTR_ANS_STIM_ONE_HUNDREDS";
    public static final String FTR_ANS_STIM_ONE_TENS      = "FTR_ANS_STIM_ONE_TENS";
    public static final String FTR_ANS_STIM_TWO_HUNDREDS  = "FTR_ANS_STIM_TWO_HUNDREDS";
    public static final String FTR_ANS_STIM_TWO_TENS      = "FTR_ANS_STIM_TWO_TENS";

    public static final String FTR_QUEST_STIM_ONE_HUNDREDS = "FTR_QUEST_STIM_ONE_HUNDREDS";
    public static final String FTR_QUEST_STIM_ONE_TENS     = "FTR_QUEST_STIM_ONE_TENS";
    public static final String FTR_QUEST_STIM_TWO_HUNDREDS = "FTR_QUEST_STIM_TWO_HUNDREDS";
    public static final String FTR_QUEST_STIM_TWO_TENS     = "FTR_QUEST_STIM_TWO_TENS";

    public static final String FTR_WRD_STARTS_WITH         = "FTR_WORD_STARTS_WITH";
    public static final String FTR_WRD_ENDS_WITH           = "FTR_WORD_ENDS_WITH";

    public static final String FTR_E2N           = "FTR_E2N";
    public static final String FTR_N2E           = "FTR_N2E";

    public static final String SHOW_BUBBLE_MASK          = "SHOW_BUBBLE_MASK";
    public static final String HIDE_MASK          = "HIDE_MASK";

    public static final String SHOW_STIMULUS_MASK = "SHOW_STIMULUS_MASK";


    static public HashMap<String, Integer> bubbleMap = new HashMap<String, Integer>();

    static {
        bubbleMap.put("RED", RED_BUBBLE);
        bubbleMap.put("BLUE", BLUE_BUBBLE);
        bubbleMap.put("PINK", PINK_BUBBLE);
        bubbleMap.put("ORANGE", ORANGE_BUBBLE);
        bubbleMap.put("GREEN", GREEN_BUBBLE);
    }

    static public HashMap<String, Integer> elongatedBubbleMap = new HashMap<String, Integer>();

    static {
        elongatedBubbleMap.put("RED", RED_ELONGATED_BUBBLE);
        elongatedBubbleMap.put("BLUE", BLUE_ELONGATED_BUBBLE);
        elongatedBubbleMap.put("PINK", PINK_ELONGATED_BUBBLE);
        elongatedBubbleMap.put("ORANGE", ORANGE_ELONGATED_BUBBLE);
        elongatedBubbleMap.put("GREEN", GREEN_ELONGATED_BUBBLE);
    }

    static public HashMap<String, Integer> audioBubbleMap = new HashMap<String, Integer>();

    static {
        audioBubbleMap.put("RED", RED_BUBBLE_VIBE);
        audioBubbleMap.put("BLUE", BLUE_BUBBLE_VIBE);
        audioBubbleMap.put("PINK", PINK_BUBBLE_VIBE);
        audioBubbleMap.put("ORANGE", ORANGE_BUBBLE_VIBE);
        audioBubbleMap.put("GREEN", GREEN_BUBBLE_VIBE);
    }


    public static final int[] RED_POP = {R.drawable.bubble_r_1, R.drawable.bubble_r_2, R.drawable.bubble_r_3, R.drawable.bubble_r_4, R.drawable.bubble_r_5, R.drawable.bubble_empty};
    public static final int[] BLUE_POP = {R.drawable.bubble_b_1, R.drawable.bubble_b_2, R.drawable.bubble_b_3, R.drawable.bubble_b_4, R.drawable.bubble_b_5, R.drawable.bubble_empty};
    public static final int[] PINK_POP = {R.drawable.bubble_p_1, R.drawable.bubble_p_2, R.drawable.bubble_p_3, R.drawable.bubble_p_4, R.drawable.bubble_p_5, R.drawable.bubble_empty};
    public static final int[] ORANGE_POP = {R.drawable.bubble_o_1, R.drawable.bubble_o_2, R.drawable.bubble_o_3, R.drawable.bubble_o_4, R.drawable.bubble_o_5, R.drawable.bubble_empty};
    public static final int[] GREEN_POP = {R.drawable.bubble_g_1, R.drawable.bubble_g_2, R.drawable.bubble_g_3, R.drawable.bubble_g_4, R.drawable.bubble_g_5, R.drawable.bubble_empty};

    public static final int[] POP_FRAME_TIME = {80,80,70,70,60,60};

    static public HashMap<String, int[]> popAnimationMap = new HashMap<String, int[]>();

    static {
        popAnimationMap.put("RED",    RED_POP);
        popAnimationMap.put("BLUE",   BLUE_POP);
        popAnimationMap.put("PINK",   PINK_POP);
        popAnimationMap.put("ORANGE", ORANGE_POP);
        popAnimationMap.put("GREEN",  GREEN_POP);
    }



    public static final String[] shapeNames = {"arrow", "circle", "diamond", "heart", "hexagon", "line", "octagon", "oval", "pentagon", "rectangle", "ring", "semicircle", "square", "star", "trapezoid", "triangle"};


    public static int [] SHAPE_arrow      = {R.drawable.shape_arrow     };
    public static int [] SHAPE_circle     = {R.drawable.shape_circle    };
    public static int [] SHAPE_diamond    = {R.drawable.shape_diamond   };
    public static int [] SHAPE_heart      = {R.drawable.shape_heart     };
    public static int [] SHAPE_hexagon    = {R.drawable.shape_hexagon   };
    public static int [] SHAPE_line       = {R.drawable.shape_line      };
    public static int [] SHAPE_octagon    = {R.drawable.shape_octagon   };
    public static int [] SHAPE_oval       = {R.drawable.shape_oval      };
    public static int [] SHAPE_pentagon   = {R.drawable.shape_pentagon  };
    public static int [] SHAPE_rectangle  = {R.drawable.shape_rectangle };
    public static int [] SHAPE_ring       = {R.drawable.shape_ring      };
    public static int [] SHAPE_semicircle = {R.drawable.shape_semicircle};
    public static int [] SHAPE_square     = {R.drawable.shape_square    };
    public static int [] SHAPE_star       = {R.drawable.shape_star      };
    public static int [] SHAPE_trapezoid  = {R.drawable.shape_trapezoid };
    public static int [] SHAPE_triangle   = {R.drawable.shape_triangle  };

    public static int [] DOT_0  =  {R.drawable.bubble_empty};
    public static int [] DOT_1  =  {R.drawable.dot_1_0};
    public static int [] DOT_2  =  {R.drawable.dot_2_0, R.drawable.dot_2_1};
    public static int [] DOT_3  =  {R.drawable.dot_3_0, R.drawable.dot_3_1, R.drawable.dot_3_2, R.drawable.dot_3_3, R.drawable.dot_3_4, R.drawable.dot_3_5, R.drawable.dot_3_6};
    public static int [] DOT_4  =  {R.drawable.dot_4_0, R.drawable.dot_4_1, R.drawable.dot_4_2, R.drawable.dot_4_3};
    public static int [] DOT_5  =  {R.drawable.dot_5_0, R.drawable.dot_5_1, R.drawable.dot_5_2};
    public static int [] DOT_6  =  {R.drawable.dot_6_0, R.drawable.dot_6_1, R.drawable.dot_6_2, R.drawable.dot_6_3, R.drawable.dot_6_4, R.drawable.dot_6_5};
    public static int [] DOT_7  =  {R.drawable.dot_7_0, R.drawable.dot_7_1};
    public static int [] DOT_8  =  {R.drawable.dot_8_0, R.drawable.dot_8_1, R.drawable.dot_8_2, R.drawable.dot_8_3, R.drawable.dot_8_4};
    public static int [] DOT_9  =  {R.drawable.dot_9_0, R.drawable.dot_9_1, R.drawable.dot_9_2, R.drawable.dot_9_3, R.drawable.dot_9_4};
    public static int [] DOT_10 =  {R.drawable.dot_10_0, R.drawable.dot_10_1, R.drawable.dot_10_2, R.drawable.dot_10_3, R.drawable.dot_10_4, R.drawable.dot_10_5, R.drawable.dot_10_6, R.drawable.dot_10_7, R.drawable.dot_10_8};

    private static int[] TENFRAME_H_0 = {R.drawable.tenframe_h_0};
    private static int[] TENFRAME_H_1 = {R.drawable.tenframe_h_1};
    private static int[] TENFRAME_H_2 = {R.drawable.tenframe_h_2};
    private static int[] TENFRAME_H_3 = {R.drawable.tenframe_h_3};
    private static int[] TENFRAME_H_4 = {R.drawable.tenframe_h_4};
    private static int[] TENFRAME_H_5 = {R.drawable.tenframe_h_5};
    private static int[] TENFRAME_H_6 = {R.drawable.tenframe_h_6};
    private static int[] TENFRAME_H_7 = {R.drawable.tenframe_h_7};
    private static int[] TENFRAME_H_8 = {R.drawable.tenframe_h_8};
    private static int[] TENFRAME_H_9 = {R.drawable.tenframe_h_9};
    private static int[] TENFRAME_H_10 = {R.drawable.tenframe_h_10};

    public static int[] BPOP_BACKGROUNDS = {
            R.raw.bark_close_up_crack_207328,
            R.raw.bay_beach_clouds_175717,
            R.raw.beach_beach_chairs_beach_hut_531035,
            R.raw.beach_beach_chairs_coconut_trees_131423,
            R.raw.beach_blue_coast_417144,
            R.raw.beach_boardwalk_boat_132037,
            R.raw.beach_bora_bora_flow_21787,
            R.raw.beach_cc0_coast_103567,
            R.raw.beach_clouds_horizon_96798,
            R.raw.beach_coast_horizon_51809,
            R.raw.beach_horizon_ocean_55637,
            R.raw.beautiful_dark_evening_29435,
            R.raw.birds_of_prey_black_and_white_dawn_748877,
            R.raw.blue_sky_camels_desert_71241,
            R.raw.blue_water_calm_h2o_734973,
            R.raw.bow_river_canada_forest_219972,
            R.raw.camels_desert_landscape_53537,
            R.raw.close_up_dew_green_207299,
            R.raw.clouds_coast_landscape_29049,
            R.raw.clouds_conifer_daylight_388065,
            R.raw.clouds_countryside_daylight_51947,
            R.raw.clouds_cropland_crops_462023,
            R.raw.clouds_daylight_fjord_135157,
            R.raw.clouds_daylight_forest_761517,
            R.raw.clouds_landscape_light_33834,
            R.raw.crafts_drums_handmade_158664,
            R.raw.dawn_dusk_hd_wallpaper_149246,
            R.raw.dawn_dusk_hills_66997,
            R.raw.daylight_farm_forest_771308,
            R.raw.daylight_forest_grass_247600,
            R.raw.desert_egypt_great_sphinx_of_giza_2359,
            R.raw.desert_hd_wallpaper_nature_80454,
            R.raw.forest_nature_river_105170,
            R.raw.galaxy_milky_way_night_7480,
            R.raw.horizon_ocean_salt_water_7321,
            R.raw.mobilechallenge_close_up_dew_807598,
            R.raw.nature_river_rocks_7138

    };

    static public HashMap<String, int[]> drawableMap = new HashMap<String, int[]>();

    static {
        drawableMap.put("arrow"     , SHAPE_arrow      );
        drawableMap.put("circle"    , SHAPE_circle     );
        drawableMap.put("diamond"   , SHAPE_diamond    );
        drawableMap.put("heart"     , SHAPE_heart      );
        drawableMap.put("hexagon"   , SHAPE_hexagon    );
        drawableMap.put("line"      , SHAPE_line       );
        drawableMap.put("octagon"   , SHAPE_octagon    );
        drawableMap.put("oval"      , SHAPE_oval       );
        drawableMap.put("pentagon"  , SHAPE_pentagon   );
        drawableMap.put("rectangle" , SHAPE_rectangle  );
        drawableMap.put("ring"      , SHAPE_ring       );
        drawableMap.put("semicircle", SHAPE_semicircle );
        drawableMap.put("square"    , SHAPE_square     );
        drawableMap.put("star"      , SHAPE_star       );
        drawableMap.put("trapezoid" , SHAPE_trapezoid  );
        drawableMap.put("triangle"  , SHAPE_triangle   );

        drawableMap.put("0"  , TENFRAME_H_0  );
        drawableMap.put("1"  , TENFRAME_H_1  );
        drawableMap.put("2"  , TENFRAME_H_2  );
        drawableMap.put("3"  , TENFRAME_H_3  );
        drawableMap.put("4"  , TENFRAME_H_4  );
        drawableMap.put("5"  , TENFRAME_H_5  );
        drawableMap.put("6"  , TENFRAME_H_6  );
        drawableMap.put("7"  , TENFRAME_H_7  );
        drawableMap.put("8"  , TENFRAME_H_8  );
        drawableMap.put("9"  , TENFRAME_H_9  );
        drawableMap.put("10" , TENFRAME_H_10 );

        /*drawableMap.put("0"  , DOT_0  );
        drawableMap.put("1"  , DOT_1  );
        drawableMap.put("2"  , DOT_2  );
        drawableMap.put("3"  , DOT_3  );
        drawableMap.put("4"  , DOT_4  );
        drawableMap.put("5"  , DOT_5  );
        drawableMap.put("6"  , DOT_6  );
        drawableMap.put("7"  , DOT_7  );
        drawableMap.put("8"  , DOT_8  );
        drawableMap.put("9"  , DOT_9  );
        drawableMap.put("10" , DOT_10 );*/
    }



}
