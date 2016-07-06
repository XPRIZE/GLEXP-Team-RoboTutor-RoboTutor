//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

package cmu.xprize.util;

// global tutor constants

import android.content.Context;

import java.io.File;
import java.util.HashMap;

public class TCONST {

    public static final String COMMAND = "COMMAND";
    public static final String MODULE = "MODULE";
    public static final String NODE = "NODE";
    public static final String CONDITION = "CONDITION";


    public static final String NUMDATA_HEADER   = "{\n" + "\"dataSource\": ";
    public static final boolean ADD_FEATURE     = true;
    public static final boolean DEL_FEATURE     = false;
    public static final int     GUID_LEN        = 5;
    public static final String  GUID_UPDATE     = "GUIDUPDATE";

    public static final String  FTR_PLACE_      = "FTR_PLACE_";
    public static final String  _USED           = "_USED";
    public static final String  FTR_P           = "FTR_P";
    public static final String  FTR_D           = "FTR_D";
    public static final String  _1WORDS         = "_1WORDS";
    public static final String  _2WORDS         = "_2WORDS";
    public static final String  _3WORDS         = "_3WORDS";
    public static final int     MAX_DIGITS      = 4;


    //*** Reading Tutor compatible string combinations

    static public HashMap<String, String> numberMap = new HashMap<String, String>();

    static {
        numberMap.put("LANG_EN", "AND,ZERO,ONE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,EIGHT,NINE,TEN,ELEVEN,TWELVE,THIRTEEN,FORTEEN,FIFTEEN,SIXTEEN,SEVENTEEN,EIGHTEEN,NINETEEN,TWENTY,THIRTY,FORTY,FIFTY,SIXTY,SEVENTY,EIGHTY,NINETY,HUNDRED,THOUSAND,MILLION,BILLION,TRILLION,QUADRILLION");
        numberMap.put("LANG_SW", "NA,SIFURI,MOJA,MBILI,TATU,NNE,TANO,SITA,SABA,NANE,TISA,KUMI,ISHIRINI,THELATHINI,AROBAINI,HAMSINI,SITINI,SABINI,THEMANINI,TISINI,MIA,ELFU,MILIONI,BILIONI,TRILIONI,KWADRILIONI");
    }


    // This is used to map "language features" to the story resources
    // these are located in the assets/<lang>
    // Note: on Android these are case sensitive filenames

    static public HashMap<String, String> langMap = new HashMap<String, String>();

    public static final String LANG_AUTO   = "LANG_AUTO";
    public static final String LANG_EFFECT = "LANG_EFFECT";
    public static final String LANG_EN     = "LANG_EN";
    public static final String LANG_SW     = "LANG_SW";

    // This maps features to 2 letter codes used to build filepaths.
    static {
        langMap.put(LANG_EFFECT, "effect");
        langMap.put(LANG_EN,     "en");
        langMap.put(LANG_SW,     "sw");
    }

    // JSON parameter constants

    // Loader Constants

    static final public String TUTORROOT       = "tutors";

    static final public String ALL_ASSETS      = "";
    static final public String INSTALL_FLAG    = "projects";          // if projects folder exists - assume installed

    static final public String LTK_ASSETS      = "projects.zip";
    static final public String LTK_DATA_FILE   = "projects.zip";
    static final public String LTK_DATA_FOLDER = "/";                 // should terminate in path sep '/'

    static final public String EDESC           = "engine_descriptor.json";
    static final public String TDESC           = "tutor_descriptor.json";
    static final public String SGDESC          = "scene_graph.json";
    static final public String SNDESC          = "navigator_descriptor.json";
    static final public String AGDESC          = "animator_graph.json";
    static final public String TDATA           = "trackdata/LIBRARY";
    static final public String AUDIOPATH       = "audio";
    static final public String TASSETS         = "assets";
    static final public String DEFAULT         = "default";

    // CTutorNavigator Constants


    public static final String ENDTUTOR        = "END_TUTOR";           // Terminate a tutor from within
    public static final String KILLTUTOR       = "KILL_TUTOR";          // Kill a tutor exteranlly
    public static final String CONTINUETUTOR   = "CONTINUE_TUTOR";

    public static final String OKNAV           = "OKNAV";
    public static final String CANCELNAV       = "CANCELNAV";
    public static final String WOZGOTO         = "WOZGOTO";
    public static final String WOZNEXT         = "WOZNEXT";
    public static final String WOZBACK         = "WOZBACK";

    // data sources
    public static final String ASSETS          = "ASSETS";
    public static final String RESOURCES       = "RESOURCE";
    public static final String EXTERN          = "EXTERN";

    // Navigator types
    final static public String SIMPLENAV       = "SIMPLE_NAVIGATOR";
    final static public String GRAPHNAV        = "GRAPH_NAVIGATOR";
    public static final String NEXT_NODE       = "NEXT_NODE";
    public static final String NEXT_TNODE      = "NEXT_TNODE";
    public static final String NEXTSCENE       = "NEXTSCENE";
    public static final String FIRST_SCENE     = "GOTO_FIRST_SCENE";



    // CActionTrack track types
    // Note these must case-match the layer names in the Flash
    // timeline specification from which CActionTrack is derived
    final static public String SCRIPT          = "Scripts";
    final static public String AUDIO           = "Audio";
    final static public String MIXED           = "Mixed";

    // Broadcast Messages
    final static public String LOADCOMPLETE    = "LOAD_COMPLETE";

    // MEDIA PLAYER
    final static public int  FPS               = 24;    // Which is 41.66- so use MPF to min error
    final static public long MPF               = 42;    // msec/frame - off by .008 sec / frame

    final static public int MAXTRACKLENGTH     = 100000; // just some really big number

    public static final String ABSOLUTE_TYPE   = "ABSOLUTE_TYPE";
    public static final String SEQUENTIAL_TYPE = "SEQUENTIAL_TYPE";
    public static final String UNKNOWN_TYPE    = "UNKNOWN_TYPE";

    // Main Loop states
    public static final String DONE            = "DONE";
    public static final String WAIT            = "WAIT";
    public static final String NONE            = "NONE";
    public static final String READY           = "READY";

    public static final String PLAY            = "PLAY";
    public static final String STOP            = "STOP";
    public static final String NEXT            = "NEXT";
    public static final String GOTO_NODE       = "GOTO_NODE";
    public static final String PAUSE           = "PAUSE";
    public static final String START           = "START";
    public static final String RESET           = "RESET";
    public static final String RESTART         = "RESTART";
    public static final String CANCEL          = "CANCEL";
    public static final String CREATE          = "CREATE";
    public static final String CREATEANDSTART  = "CREATEANDSTART";
    public static final String ENTER_SCENE     = "ENTER_SCENE";

    // Condition parser FSM states
    public static final int STARTSTATE = 0;
    public static final int PARSESTATE = 1;
    public static final int PARSEIDENT = 2;
    public static final int PARSEINDEX = 3;
    public static final int PARSENUM = 4;
    public static final int PARSEVAR = 5;
    public static final int PARSEPROP = 6;
    public static final int RESOLVESYMBOL = 7;
    public static final int ENDSUBEXPR = 8;
    public static final int PARSESTRING = 9;
    public static final int BUILDEXPR = 100;

    // Binary Operations
    public static final int BOOLAND = 0;
    public static final int BOOLOR = 1;
    public static final int EQUALTO = 2;
    public static final int NEQUALTO = 3;
    public static final int LESSOREQUAL = 4;
    public static final int LESSTHAN = 5;
    public static final int GREATEROREQUAL = 6;
    public static final int GREATERTHAN = 7;
    public static final int NOOP = -1;

    public static final char EOT = '\04';


    // type_action - command types
    public static final String CMD_WAIT         = "WAIT";
    public static final String CMD_GOTO         = "GOTONODE";
    public static final String CMD_NEXT         = "NEXT";
    public static final String CMD_LAUNCH       = "LAUNCH-TUTOR";
    public static final String CMD_SET_FEATURE  = "FEATURE-ADD";
    public static final String CMD_DEL_FEATURE  = "FEATURE-DEL";


    // Intrinsic types
    public static final String TREFERENCE  = "TReference";

    public static final String AUDIOEVENT  = "event";
    public static final String AUDIOSTREAM = "stream";
    public static final String AUDIOFLOW   = "flow";

    public static final String LOOKATSTART = "PERSONA_LOOKAT_START";
    public static final String LOOKAT      = "PERSONA_LOOKAT";
    public static final String LOOKATEND   = "PERSONA_LOOKAT_END";
    public static final String GLANCEAT    = "PERSONA_GLANCEAT";
    public static final String SCREENPOINT = "SCREENPOINT";

    public static final String FTR_STORY_STARTING = "FTR_STORY_STARTING";

    public static final String FWCORRECT    = "FTR_RIGHT";
    public static final String FWINCORRECT  = "FTR_WRONG";
    public static final String FWUNKNOWN    = "FTR_UNRECOGNIZED";
    public static final String FTR_EOI      = "FTR_NOWORDS";
    public static final String FTR_EOD      = "FTR_EOD";
    public static final String ALL_CORRECT  = "ALL_CORRECT";

    public static final String FALSE        = "FALSE";
    public static final String TRUE         = "TRUE";
    public static final String OVALICON     = "OVALICON";
    public static final String RECTICON     = "RECTICON";
    public static final String RAND         = "random";
    public static final String MINUSONE     = "-1";


    // PocketSphinx Recognizer Constants
    public static final int UNKNOWNEVENT_TYPE  = 0;

    public static final int TIMEDSTART_EVENT   = 0x01;
    public static final int TIMEDSILENCE_EVENT = 0x02;
    public static final int TIMEDSOUND_EVENT   = 0x04;
    public static final int TIMEDWORD_EVENT    = 0x08;

    public static final int ALLTIMED_EVENTS    = 0x0F;

    public static final int SILENCE_EVENT      = 0x10;
    public static final int SOUND_EVENT        = 0x20;
    public static final int WORD_EVENT         = 0x40;

    public static final int ALLSTATIC_EVENTS   = 0x70;

    public static final int RECOGNITION_EVENT  = 0x100;
    public static final int ERROR_EVENT        = 0x101;

    public static final int ALL_EVENTS         = 0xFFFFFFFF;


    public static final int NOINTERVENTION = 0;
    public static final int INSPEECH       = 1;
    public static final int SAYWORD        = 2;

    public static final String STORYDATA   = "story_data.json";
    public static final String STORYINDEX  = "story_index.json";
    public static final String SOURCEFILE  = "[file]";

    public static final String TTS         = "TTS";
    public static final String ASR         = "ASR";

    public static final String GLYPHLOG    = "glyphlog_";
    public static final String DATASHOP    = "-DS";
    public static final String JSONLOG     = ".json";
    public static final boolean APPEND     = true;

    public static final String GLYPH_DATA  = "glyphdata";


    // LTK messaging constants
    public static final String FW_STIMULUS = "FW_UPDATED";
    public static final String FW_VALUE    = "FW_VALUE";
    public static final String FW_EOI      = "FW_EOI";
    public static final String FW_RESPONSE = "FW_RESPONSE";

    public static final String ROBOTUTOR_FOLDER = "/RoboTutor/";


    // Listener Control message types
    public static final String LISTENER_RESPONSE = "LISTENER_RESPONSE";


    // TTS command constants
    public static final String SAY             = "SAY";
    public static final String SET_RATE        = "SET_RATE";


    // Preference keys
    public static final String ENGINE_INSTANCE = "RoboTutor";
    public static final String CURRENT_TUTOR   = "tutor";


    // Number Listeneing Component
    private static final String[]    placeValue = {".ones",".tens",".hundreds",".thousands",".millions",".billions"};

    public static final String ERR_SINGLEDIGIT  = "Single Digit Error";
    public static final String ERR_MULTIDIGIT   = "Multi Digit Error";
    public static final String TEXT_FIELD       = ".text";

    public static final String PLRT             = "PLRT";
    public static final String JSGF             = "JSGF";

    public static final String DIGIT_STRING_VAR  = ".digitString";
    public static final String PLACE_STRING_VAR  = ".placeString";
    public static final String DIGIT_TEXT_VAR    = ".digitText";
    public static final String PLACE_TEXT_VAR    = ".placeText";

    public static final String PLACE4_WORDS_VAR  = ".place4Words";
    public static final String PLACE3_WORDS_VAR  = ".place3Words";
    public static final String PLACE2_WORDS_VAR  = ".place2Words";
    public static final String PLACE1_WORDS_VAR  = ".place1Words";

    public static final String DIGIT4_WORDS_VAR  = ".digit4Words";
    public static final String DIGIT3_WORDS_VAR  = ".digit3Words";
    public static final String DIGIT2_WORDS_VAR  = ".digit2Words";
    public static final String DIGIT1_WORDS_VAR  = ".digit1Words";


    // Generic error codes
    public static final String GENERIC_RIGHT  = "FTR_RIGHT";
    public static final String GENERIC_WRONG  = "FTR_WRONG";
    public static final boolean TRUE_ERROR    = true;
    public static final boolean TRUE_NOERROR  = true;
    public static final boolean FALSE_NOERROR = false;
    public static final boolean FALSE_ERROR   = false;


    // MediaManager constants.
    public static final String MEDIA_AUDIO           = "MEDIA_AUDIO";
    public static final String MEDIA_TIMELINE        = "MEDIA_TIMELINE";
    public static final String DEFAULT_SOUND_PACKAGE = "default";


    // ASR (automated speech recognition) constants
    public static final Long   STABLE_TIME        = 300L;    // Time a word has to be stable before it is emitted.
    public static final String FINAL_HYPOTHESIS   = "FINALHYPOTHESIS";
    public static final String PARTIAL_HYPOTHESIS = "PARTIALHYPOTHESIS";
    public static final String STABLE_HYPOTHESIS  = "STABLEHYPOTHESIS";

    public static final String RAW_HYPOTHESES     = "RAWHYPOTHESES";
    public static final String STABLE_HYPOTHESES  = "STABLEHYPOTHESES";

    // Number parser states.
    public static final int W2N_ZERO     = 1;
    public static final int W2N_DIGIT    = 2;
    public static final int W2N_TENS     = 3;
    public static final int W2N_TEENS    = 4;
    public static final int W2N_HUNDREDS = 5;
    public static final int W2N_POWER    = 6;
    public static final int W2N_CONJ     = 7;
    public static final int W2N_EOD      = 8;
    public static final int W2N_ERR      = 9;

    public static final int W2N_VALUE_UPDATE    = 10;

    public static final int UNSET = -1;                             // NOTE: things a dependent upon this being -1
    public static final int ZERO  = 0;

    // W2N Error Types
    public static final int NO_ERROR                     = 0;       //
    public static final int ERRW2N_NO_DATA               = 1;       // empty string set passed in
    public static final int ERRW2N_LEADING_CONJ          = 2;       // leading NA
    public static final int ERRW2N_NONTERM_ZERO          = 3;       // zero must always be alone - This occurs when something follows zero
    public static final int ERRW2N_NONSOLITARY_ZERO      = 4;       // zero must always be alone - This occurs when something proceeds zero
    public static final int ERRW2N_MISSING_CONJ          = 5;       // We don't tolerate missing NA in swahili
    public static final int ERRW2N_MISSING_HUNDRED_MULTI = 6;       // They uttered tens/hundreds/power after mia
    public static final int ERRW2N_HUNDRED_ADDED_CONJ    = 7;       // They added a na after mia
    public static final int ERRW2N_INCREASING_POWER      = 8;       // They've uttered an increasing power sequnce - e.g. thousand .. million
    public static final int ERRW2N_REPEATED_POWER        = 9;       // They've uttered a second power e.g. "million thousand"
    public static final int ERRW2N_POWER_CONJ            = 10;      // They've uttered a power followed by na
    public static final int ERRW2N_INCREASING_MULTIPLIER = 11;      // power multiples must be monotonically recreasing 100's - 10's - 1's
    public static final int ERRW2N_REPEATED_CONJ         = 12;      // na na
    public static final int ERRW2N_INTERNAL              = 13;      // shouldn't occur
    public static final int ERRW2N_ZERO_HUNDRED_MULTI    = 14;      // mia sifuri
    public static final int ERRW2N_INVALID_TEXT          = 15;      // The input text is invalid - should not happen in practice
    public static final int ERRW2N_INVALID_CONJ          = 16;      // e.g. six and | twenty and | eleven and

    // English specific
    public static final int ERRW2N_LEADING_HUNDRED       = 30;      // Can't start by saying 'hundred'
    public static final int ERRW2N_LEADING_POWER         = 31;      // Can't start by saying 'thousand' etc
    public static final int ERRW2N_REPEATED_HUNDRED      = 32;      // Hundred Hundred

    public static final int ERRW2N_REPEATED_DIGIT        = 33;      // e.g. saying "two seven"
    public static final int ERRW2N_INVALID_DIGIT         = 34;      // e.g. saying "hundred two"
    public static final int ERRW2N_INVALID_TENS          = 35;      // e.g. saying "tirteen hundred"
    public static final int ERRW2N_INVALID_HUNDREDS      = 36;      // e.g. saying "thousand hundred"
    public static final int ERRW2N_INVALID_POWER         = 37;      // e.g. saying "and thousand"


    public static final String[] W2N_ERRORMSG = {"NO_ERROR","NO_DATA","LEADING_CONJ","NONTERM_ZERO","NONSOLITARY_ZERO","MISSING_CONJ","MISSING_HUNDRED_MULTI","HUNDRED_ADDED_CONJ","INCREASING_POWER","REPEATED_POWER","POWER_CONJ","INCREASING_MULTIPLIER","REPEAT_CONJ","INTERNAL","ZERO_HUNDRED_MULTI","INVALID_TEXT"};

    // W2N Warning types
    public static final int NO_WARNING                = 0;          //
    public static final int W2N_HYPOTHESIS            = 1;          // This is potentially a complete utterance
    public static final int W2N_DANGLING_HUNDRED_WARN = 2;          // mia with no multiplier yet
    public static final int W2N_DANGLING_POWER_WARN   = 3;          // power with no multiplier yet
    public static final int W2N_DANGLING_CONJ_WARN    = 4;          // conjunction with no follow on value

    public static final String[] W2N_WARNMSG = {"NO_WARNING","HYPOTHESIS","DANGLING_HUNDRED_WARN","DANGLING_POWER_WARN","DANGLING_CONJ_WARN"};

    // Coordinating conjunction
    public static final String CC_SW_NA  = "NA";
    public static final String CC_EN_AND = "AND";
    public static final String NUM_EOD   = "NUM_EOD";

    // ASB constants
    public static final int NEXTPAGE =  1;
    public static final int PREVPAGE = -1;


    // READING Tutor State names  -- RTC Reading Tutor Component

    public static final String PAGEFLIP_BUTTON = "PAGE_FLIP_BUTTON";
    public static final String SPEAK_BUTTON    = "SPEAK_BUTTON";

    public static final String EMPTY = "";

    public static final int INCR = 1;
    public static final int DECR = -1;

    public static final String RTC_VAR_PAGESTATE  = ".pageState";
    public static final String RTC_VAR_PARASTATE  = ".paraState";
    public static final String RTC_VAR_LINESTATE  = ".lineState";
    public static final String RTC_VAR_WORDSTATE  = ".wordState";
    public static final String RTC_VAR_ATTEMPT    = ".attempt";
    public static final String LAST               = "LAST";
    public static final String NOT_LAST           = "NOT_LAST";

    public static final String RTC_VAR_STATE         = ".storyState";
    public static final String RTC_PARAGRAPHCOMPLETE = "PARAGRAPH_COMPLETE";
    public static final String RTC_PAGECOMPLETE      = "PAGE_COMPLETE";
    public static final String RTC_STORYCMPLETE      = "STORY_COMPLETE";
    public static final String RTC_LINECOMPLETE      = "LINE_COMPLETE";
    public static final String RTC_CLEAR             = "";

    public static final String RTC_VAR_WORDVALUE  = ".currentWord";
    public static final String RTC_VAR_INDEX      = ".wordindex";
    public static final String RTC_VAR_REMAINING  = ".remainingWords";
    public static final String RTC_VAR_SENTENCE   = ".sentence";

}
