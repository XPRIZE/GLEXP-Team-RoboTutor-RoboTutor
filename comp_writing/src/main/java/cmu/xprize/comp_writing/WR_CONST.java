package cmu.xprize.comp_writing;

public class WR_CONST {

    public static final int  HLCOLOR        = 0x99FF00FF;
    public static final int  BOX_COLOR      = 0x44A5C4D4;
    public static final int  NORMALCOLOR    = 0xFF000000;
    public static final int  SAMPLE_COLOR   = 0x33000088;

    public static final long RIPPLE_DELAY   = 400L;
    public static final long HIGHLIGHT_TIME = 300;

    public static final String FTR_INPUT_HESITATION     = "FTR_INPUT_HESITATION";

    public static final String DATA_ITEM_COMPLETE       = "DATA_ITEM_COMPLETE";

    public static final String ON_ERROR                 = "ON_ERROR";
    public static final String ON_CHAR_ERROR            = "ON_CHAR_ERROR";
    public static final String ON_METRIC_ERROR          = "ON_METRIC_ERROR";

    public static final String ON_CORRECT               = "ON_CORRECT";

    public static final String MERCY_RULE               = "MERCY_RULE";

    public static final String ON_ERASE                 = "ON_ERASE";

    public static final String POINT_AT_ERASE_BUTTON    = "POINT_AT_ERASE_BUTTON";
    public static final String POINT_AT_REPLAY_BUTTON   = "POINT_AT_REPLAY_BUTTON";
    public static final String POINT_AT_GLYPH           = "POINT_AT_GLYPH";

    public static final String FTR_ATTEMPT_1            = "FTR_ATTEMPT_1";
    public static final String FTR_ATTEMPT_2            = "FTR_ATTEMPT_2";
    public static final String FTR_ATTEMPT_3            = "FTR_ATTEMPT_3";
    public static final String FTR_ATTEMPT_4            = "FTR_ATTEMPT_4";

    //amogh added
    public static final String FTR_SEN_ATTEMPT_1        = "FTR_SEN_ATTEMPT_1";
    public static final String FTR_SEN_ATTEMPT_2        = "FTR_SEN_ATTEMPT_2";
    public static final String FTR_SEN_ATTEMPT_3        = "FTR_SEN_ATTEMPT_3";
    public static final String FTR_SEN_ATTEMPT_4        = "FTR_SEN_ATTEMPT_4";

    public static final String FTR_HESITATION_1            = "FTR_HESITATION_1";
    public static final String FTR_HESITATION_2            = "FTR_HESITATION_2";
    public static final String FTR_HESITATION_3            = "FTR_HESITATION_3";
    public static final String FTR_HESITATION_4            = "FTR_HESITATION_4";
    public static final String FTR_HESITATION_5            = "FTR_HESITATION_5";

    public static final String FTR_WORD_CORRECT            = "FTR_WORD_CORRECT";
    public static final String FTR_SEN_EVAL                = "FTR_SEN_EVAL";
    //amogh add finish


    public static final String ACTION_COMPLETE          = "ACTION_COMPLETE";
    public static final String RIPPLE_REPLAY            = "RIPPLE_REPLAY";
    public static final String RIPPLE_REPLAY_WORD       = "RIPPLE_REPLAY_WORD";
    public static final String RIPPLE_HIGHLIGHT         = "RIPPLE_HIGHLIGHT";
    public static final String STIMULUS_HIGHLIGHT       = "STIMULUS_HIGHLIGHT";
    public static final String GLYPH_HIGHLIGHT          = "GLYPH_HIGHLIGHT";
    public static final String RIPPLE_DEMO              = "RIPPLE_DEMO";
    public static final String FIELD_REPLAY_COMPLETE    = "FIELD_REPLAY_COMPLETE";
    public static final String REPLAY_COMPLETE          = "REPLAY_COMPLETE";
    public static final String ON_ANIMATION_COMPLETE    = "ON_ANIMATION_COMPLETE";
    public static final String ON_START_WRITING         = "ON_START_WRITING";
    public static final String ON_STOP_WRITING          = "ON_STOP_WRITING";

    public static final String REPLAY_USERGLYPH         = "REPLAY_USERGLYPH";
    public static final String REPLAY_PROTOGLYPH        = "REPLAY_PROTOGLYPH";
    public static final String REPLAY_DEFAULT           = "REPLAY_DEFAULT";
    public static final String RIPPLE_PROTO             = "RIPPLE_PROTO";
    public static final String ANIMATE_OVERLAY          = "ANIMATE_OVERLAY";
    public static final String FIELD_COMPLETE           = "FIELD_COMPLETE";
    public static final String ANIMATE_ALIGN            = "ANIMATE_ALIGN";
    public static final String HIGHLIGHT_NEXT           = "HIGHLIGHT_NEXT";
    public static final String SHOW_TRACELINE           = "SHOW_TRACELINE";
    public static final String HIDE_TRACELINE           = "HIDE_TRACELINE";
    public static final String SHOW_SAMPLE              = "SHOW_SAMPLE";
    public static final String ERASE_GLYPH              = "ERASE_GLYPH";
    public static final String DEMO_PROTOGLYPH          = "DEMO_PROTOGLYPH";
    public static final String ANIMATE_PROTOGLYPH       = "ANIMATE_PROTOGLYPH";
    public static final String HIDE_SAMPLE              = "HIDE_SAMPLE";
    public static final String HIDE_GLYPHS              = "HIDE_GLYPHS";
    public static final String HIDE_CURRENT_LETTER_GLYPH = "HIDE_CURRENT_LETTER_GLYPH";
    public static final String HIDE_CURRENT_WORD_GLYPHS = "HIDE_CURRENT_WORD_GLYPHS";
    public static final String HIDE_SAMPLES             = "HIDE_SAMPLES";
    public static final String HIDE_SAMPLE_ACTIVE_INDEX = "HIDE_SAMPLE_ACTIVE_INDEX";
    public static final String INHIBIT_OTHERS           = "INHIBIT_OTHERS";
    public static final String CLEAR_ATTEMPT            = "CLEAR_ATTEMPT";
    public static final String CLEAR_HESITATION         = "CLEAR_HESITATION"; //amogh added
    public static final String RESET_HESITATION         = "RESET_HESITATION"; //amogh added
    public static final String INC_HESITATION           = "INC_HESITATION"; //amogh added


    public static final String CANDIDATE_VAR            = ".candidate";
    public static final String EXPECTED_VAR             = ".expected";
    public static final String VALUE_VAR                = ".value";
    public static final String AUDIO_STIM_1             = ".audiostim1";
    public static final String AUDIO_STIM_2             = ".audiostim2";
    public static final String AUDIO_STIM_3             = ".audiostim3";

    public static final String FTR_STIM_1_CONCAT            = "FTR_STIM_1_CONCAT";
    public static final String AUDIO_STIM_1_CONCAT_HUNDREDS = ".audioStimOneConcatHundreds";
    public static final String AUDIO_STIM_1_CONCAT_TENS     = ".audioStimOneConcatTens";
    public static final String AUDIO_STIM_1_CONCAT_ONES     = ".audioStimOneConcatOnes";

    public static final String FTR_STIM_3_CONCAT            = "FTR_STIM_3_CONCAT";
    public static final String AUDIO_STIM_3_CONCAT_HUNDREDS = ".audioStimThreeConcatHundreds";
    public static final String AUDIO_STIM_3_CONCAT_TENS     = ".audioStimThreeConcatTens";
    public static final String AUDIO_STIM_3_CONCAT_ONES     = ".audioStimThreeConcatOnes";

    public static final String FTR_ANS_CONCAT            = "FTR_ANS_CONCAT";
    public static final String AUDIO_ANS                 = ".audioAns";
    public static final String AUDIO_ANS_CONCAT_HUNDREDS = ".audioAnsConcatHundreds";
    public static final String AUDIO_ANS_CONCAT_TENS     = ".audioAnsConcatTens";
    public static final String AUDIO_ANS_CONCAT_ONES     = ".audioAnsConcatOnes";

    public static final String AUDIO_LETTER     = ".audioLetter";
    public static final String AUDIO_PUNCTUATION     = ".audioPunctuation";

    public static final String FTR_INPUT_STALLED        = "FTR_INPUT_STALLED";
    public static final String FTR_HAD_ERRORS           = "FTR_HAD_ERRORS";

    public static final String ERROR_METRIC             = "FTR_ERROR_METRIC";
    public static final String ERROR_CHAR               = "FTR_ERROR_CHAR";

    public static final String ON_REPLAY_COMMAND        = "ON_REPLAY_COMMAND";
    public static final String WRITE_BEHAVIOR           = "WRITE_BEHAVIOR";
    public static final String CANCEL_POINTAT           = "CANCEL_POINTAT";

    public static final String AUTO_ERASE               = "AUTO_ERASE";

    //amogh added audio features
    public static final String FTR_AUDIO_CAP = "FTR_AUDIO_CAP";
    public static final String FTR_AUDIO_PUNC = "FTR_AUDIO_PUNC";
    public static final String FTR_AUDIO_LTR = "FTR_AUDIO_LTR";
    public static final String FTR_AUDIO_SPACE = "FTR_AUDIO_SPACE";
    public static final String FTR_INSERT = "FTR_INSERT";
    public static final String FTR_DELETE = "FTR_DELETE";
    public static final String FTR_REPLACE = "FTR_REPLACE";
    public static final String FTR_PERIOD = "FTR_PERIOD";
    public static final String FTR_EXCLAIM = "FTR_EXCLAIM";
    public static final String FTR_QUESTION = "FTR_QUESTION";
    public static final String FTR_COMMA = "FTR_COMMA";
    public static final String FTR_AUDIO_NO_ERROR = "FTR_AUDIO_NO_ERROR0";
//    public static final String FTR_INSERT = "FTR_INSERT";
//    public static final String FTR_INSERT = "";
//    public static final String FTR_INSERT = "";
//    public static final String FTR_INSERT = "";
//    public static final String FTR_INSERT = "";

}

