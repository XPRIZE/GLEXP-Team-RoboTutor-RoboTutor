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

package cmu.xprize.util;

// global tutor constants

import android.os.Environment;

import java.util.HashMap;

public class TCONST {

    // These features are based on the current tutor selection model
    // When no tutor has been selected it should run the tutor select
    // and when it finishes it should run the difficulty select until
    // the user wants to select another tutor.
    //

    public static final String FTR_TUTOR_SELECT       = "FTR_TUTOR_SELECT"; // these are never read anymore, but are still used as features
    public static final String FTR_DEBUG_SELECT       = "FTR_DEBUG_SELECT";
    public static final String FTR_DEBUG_LAUNCH       = "FTR_DEBUG_LAUNCH";

    public static final String FTR_GOODBYE = "GOODBYE";
    public static final int NUM_GOODBYE_SOUND_CLIPS = 9;    // make sure this matches the number of sound clips in
                                                            // the "EXIT_BUTTON_BEHAVIOR" object in activity_selector/animator_graph.json

    public static final String SKILL_WRITING    = "letters";
    public static final String SKILL_STORIES    = "stories";
    public static final String SKILL_MATH       = "numbers";

    public static final String FINISH           = "FINISH";


    // RoboTutor Version spec Index meaning 0.1.2.3
    // Given 4.23.9.8
    // Major release 4 | Feature release 23 | Fix release 9 | compatible Asset Version 8
    //
    public static final int MAJOR_VERSION   = 0;
    public static final int FEATURE_RELEASE = 1;
    public static final int FIX_RELEASE     = 2;
    public static final int ASSET_VERSION   = 3;


    // Spec elements for asset zip files releases
    // For assets to be compatible with Robotutor the ASSET_CODE_VERSION must match the ASSET_VERSION
    //
    public static final int ASSET_CODE_VERSION    = 0;
    public static final int ASSET_RELEASE_VERSION = 1;
    public static final int ASSET_UPDATE_VERSION  = 2;


    // These represent the base name for assets delivered in Zip files and loaded
    // through calls to updateZipAsset

    // They will arrive in files named - RoboTutor_AssetA.0.1.0.zip
    //
    public static final String ROBOTUTOR_ASSET_PATTERN = "rtasset_";
    public static final String CODE_DROP_1_ASSET_PATTERN = "codedrop1_";
    public static final String CODE_DROP_2_ASSET_PATTERN = "codedrop2_";
    public static final String PROTOTYPE_ASSET_PATTERN = "protoassets_";
    public static final String QA_ASSET_PATTERN = "qa_assets";
    public static final String ENGLISH_ASSET_PATTERN = "English_";
    public static final String SWAHILI_ASSET_PATTERN = "Swahili_";

    public static final String COMMAND      = "COMMAND";
    public static final String MODULE       = "MODULE";
    public static final String QUEUE        = "QUEUE";
    public static final String NODE         = "NODE";
    public static final String CONDITION    = "CONDITION";

    public static final String NUMDATA_HEADER   = "{\n" + "\"dataSource\": ";
    public static final boolean ADD_FEATURE     = true;
    public static final boolean DEL_FEATURE     = false;

    public static final String  FTR_PLACE_      = "FTR_PLACE_";
    public static final String  _USED           = "_USED";
    public static final String  FTR_P           = "FTR_P";
    public static final String  FTR_D           = "FTR_D";
    public static final String  _1WORDS         = "_1WORDS";
    public static final String  _2WORDS         = "_2WORDS";
    public static final String  _3WORDS         = "_3WORDS";
    public static final int     MAX_DIGITS      = 4;
    public static final String  NO_DATASOURCE   = "";
    public static final String  DATA_PREFIX     = "DATA_";
    public static final String  DATA_PATH        = "data";

    public static final String FW_PREPLISTENER  = "FW_PREPLISTENER";
    public static final String FW_TTS           = "FW_TTS";
    public static final String SET_LANG_FTR     = "SET_LANGUAGE_FEATURE";
    public static final String VALUE            = "VALUE";
    public static final String NULL             = "NULL";
    public static final String SAY_STIMULUS     = "FTR_SAY";
    public static final String SHOW_STIMULUS    = "FTR_SHOW";


    public static final String ASM_DIGIT_OR_OVERHEAD_CORRECT   = "ASM_DIGIT_OR_OVERHEAD_CORRECT";
    public static final String ASM_DIGIT_OR_OVERHEAD_WRONG     = "ASM_DIGIT_OR_OVERHEAD_WRONG";
    public static final String ASM_CLICK_ON_DOT                = "ASM_CLICK_ON_DOT";
    public static final String ASM_ALL_DOTS_DOWN               = "ASM_ALL_DOTS_DOWN";

    public static final String ASM_ADD                         = "ASM_ADD";
    public static final String ASM_ADD_PROMPT                  = "ASM_ADD_PROMPT";
    public static final String ASM_ADD_PROMPT_COUNT_FROM       = "ASM_ADD_PROMPT_COUNT_FROM";

    public static final String ASM_SUB                          = "ASM_SUB";
    public static final String ASM_SUB_PROMPT                   = "ASM_SUB_PROMPT";

    public static final String ASM_MULTI                        = "ASM_MULTI";
    public static final String ASM_MULTI_PROMPT                 = "ASM_MULTI_PROMPT";
    public static final String ASM_RA_START                     = "ASM_RA_START";
    public static final String ASM_NEXT_NUMBER                  = "ASM_NEXT_NUMBER";
    public static final String ASM_NEXT_RESULT                  = "ASM_NEXT_RESULT";
    public static final String ASM_RESULT_FIRST_TWO             = "ASM_RESULT_FIRST_TWO";
    public static final String ASM_RESULT_NEXT_OR_LAST          = "ASM_RESULT_NEXT_OR_LAST";
    public static final String ASM_REPEATED_ADD_DOWN            = "ASM_REPEATED_ADD_DOWN";

    public static final String TYPE_CTUTOR      = "CTutor";
    public static final String TYPE_CSCENEGRAPH = "CSceneGraph";
    public static final String TYPE_CTUTORGRAPH = "CTutorGraph";
    public static final String EVENT_SCENEQUEUE = "Scene Queue Event";
    public static final String EVENT_TUTORGRAPH = "Tutor Graph Event";
//    public static final String AUDIO_REF        = "audio_ref";
    public static final String SET_BANNER_COLOR = "SET_BANNER_COLOR";
    public static final String LAST_ATTEMPT     = "FTR_LASTATTEMPT";


    public static final String  FONT_FOLDER     = "fonts/";
    public static final String  SHOWICONS       = "SHOWICONS";
    public static final String  SHOWNUM         = "SHOWNUM";
    public static final String  VIEW_SCALED     = "VIEW_SCALED";
    public static final String  VIEW_NORMAL     = "VIEW_NORMAL";
    public static final String  VIEW_ANIMATE    = "VIEW_ANIMATE";
    public static final String  STROKE_ORIGINAL = "STROKE_ORIGINAL";
    public static final String  STROKE_OVERLAY  = "STROKE_OVERLAY";
    public static final int     GLYPHCOLOR1     = 0xAA000000;
    public static final int     FONTCOLOR1      = 0xFF0000FF;
    public static final int     ERRORCOLOR1     = 0xFFFF0000;
    public static final boolean VOLATILE        = true;
    public static final int     DOT_SIZE        = 40;
    public static final String  CONTAINER_SCALED= "CONTAINER_SCALED";

    public static final String GRUNDSCHRIFT        = "Grundschrift";
    public static final String GRUNDSCHRIFT_Punkt  = "Grundschrift-Punkt";
    public static final String GRUNDSCHRIFT_Kontur = "Grundschrift-Kontur";

    public static final String POINTAT             = "POINTAT";
    public static final String POINTATEND          = "POINTATEND";
    public static final String POINT_AND_TAP       = "POINTANDTAP";
    public static final String POINTAT_COMPLETE    = "POINTAT_COMPLETE";
    public static final String POINT_LIVE          = "POINT_LIVE";
    public static final String POINT_FADE          = "POINT_FADE";
    public static final String CANCEL_POINT        = "CANCEL_POINT";
    public static final String POINT_AT_BUTTON     = "POINT_AT_BUTTON";

    public static final String FEEDBACK_DELAYED    = "FEEDBACK_DELAYED";
    public static final String FEEDBACK_IMMEDIATE  = "FEEDBACK_IMMEDIATE";

    public static final String HIGHLIGHT           = "HIGHLIGHT";
    public static final String SHOW_NORMAL         = "SHOW_NOTMAL";

    public static final float  TRACKER_WEIGHT      = 5f;
    public static final int    TRACKER_COLOR       = 0x11000088;

    public static final String VISIBLE             = "VISIBLE";
    public static final String INVISIBLE           = "INVISIBLE";
    public static final String GONE                = "GONE";

    public static final String ASK_SELECTION       = "ASK_SELECTION";
    public static final String ASK_BUTTON_ID       = "ASK_BUTTON_ID";
    public static final String CANCEL_POINTAT      = "CANCEL_POINTAT";

    public static final String LEFTLANE            = "LEFT";
    public static final String CENTERLANE          = "MID";
    public static final String RIGHTLANE           = "RIGHT";

    public static final String STORY_INTENT        = "story_reading";
    public static final String QUESTIONS_INTENT        = "story_questions";
    public static final String ON_CLICK            = "ON_CLICK";
    public static final String ENCODED_FOLDER      = "[encfolder]";
    public static final String SHARED_MATH         = "[sharedmath]";
    public static final String SHARED_MATH_FOLDER = "shared/shared_math";
    public static final String SHARED_LITERACY     = "[sharedliteracy]";
    public static final String SHARED_LITERACY_AUDIO_FOLDER = "shared/shared_lit"; // TODO fix this abominable inconsistency
    public static final String SHARED_LITERACY_IMAGE_FOLDER = "shared/shared_literacy";
    public static final String SONG                = "[song]";
    public static final String SONG_FOLDER         = "?";
    public static final String STORY_PATH          = "cmu/xprize/story_reading/";
    public static final String WORD_PROBLEMS       = "[wordproblems]";
    public static final String PUNC_STORY = "[punc]";

    public static final String LOCAL_FILE          = "[local_file]";
    public static final String DOWNLOAD_PATH       = "/sdcard/Download";
    public static final String DOWNLOAD_RT_PATH    = "/sdcard/Download/RoboTutor";
    public static final String DOWNLOAD_RT_TUTOR   = "/sdcard/Download/RoboTutor/assets";

    public static final String DEBUG_FILE_PREFIX = "[debug_file]";
    public static final String DEBUG_RT_PATH       = "/sdcard/robo_debug";

    public static final String ARITHMETIC_DATA     = "tutors/add_subtract";
    public static final String AKIRA_DATA          = "tutors/akira";
    public static final String BUBBLEPOP_DATA      = "tutors/bubble_pop";
    public static final String STORY_DATA          = "tutors/story_reading";
    public static final String WRITING_DATA        = "tutors/word_copy";

    public static final String FTR_USER_HEAR       = "FTR_USER_HEAR";
    public static final String FTR_USER_READ       = "FTR_USER_READ";
    public static final String FTR_USER_ECHO       = "FTR_USER_ECHO";
    public static final String FTR_USER_HIDE       = "FTR_USER_HIDE";
    public static final String FTR_USER_REVEAL     = "FTR_USER_REVEAL";
    public static final String FTR_USER_PARROT     = "FTR_USER_PARROT";
    public static final String FTR_USER_READING    = "FTR_USER_READING";
    // UHQ
    public static final String FTR_GEN             = "FTR_GEN";
    public static final String FTR_PIC             = "FTR_PIC";
    public static final String FTR_CLO             = "FTR_CLO";
    public static final String STOP_AUDIO          = "STOP_AUDIO";
    public static final String RTC_VAR_CLOZEWORD   = ".clozeWord";
    public static final String REMOVE_CLOZE_FROM_BLANK = "REMOVE_CLOZE_FROM_BLANK";


    public static final String NARRATE_STORY       = "NARRATE_STORY";
    public static final String TRACK_NARRATION     = "TRACK_NARRATION";
    public static final String START_NARRATION     = "START_NARRATION";
    public static final String SPEAK_UTTERANCE     = "SPEAK_UTTERANCE";

    public static final String SPEAK_EVENT         = "SPEAK_EVENT";

    public static final int    INITSPLIT           = -1;
    public static final String SENTENCE_SPACE      = "  ";
    public static final String WORD_SPACE          = " ";
    public static final String NO_SPACE            = "";
    public static final int    MAX_AKDATA          = 10;

    public static final String FTR_COMPLETE      = "FTR_COMPLETE";
    public static final String FTR_PROMPT        = "FTR_PROMPT";
    public static final String FTR_PAGE_PROMPT   = "FTR_PAGE_PROMPT";

    public static final String START_PROGRESSIVE_UPDATE   = "START_PROGRESSIVE_UPDATE";
    public static final String START_INDETERMINATE_UPDATE = "START_INDETERMINATE_UPDATE";
    public static final String UPDATE_PROGRESS            = "UPDATE_PROGRESS";
    public static final String PROGRESS_TITLE             = "PROGRESS_TITLE";
    public static final String PROGRESS_MSG1              = "PROGRESS_MSG1";
    public static final String PROGRESS_MSG2              = "PROGRESS_MSG2";
    public static final String ASSET_UPDATE_MSG           = "Installing Assets: ";
    public static final String INT_FIELD                  = "INT_FIELD";
    public static final String PLEASE_WAIT                = " - Please Wait.";

    public static final String TUTOR_NATIVE               = "native";
    public static final String TRACK_COMPLETE             = "TRACK_COMPLETE";

    public static final String NEXT_PAGE                  = "NEXT_PAGE";
    public static final String CLOZE_CORRECT              = "CLOZE_CORRECT";
    public static final String CLOZE_WRONG                = "CLOZE_WRONG";
    public static final String PICMATCH_CORRECT              = "PICMATCH_CORRECT";
    public static final String PICMATCH_WRONG                = "PICMATCH_WRONG";
    public static final String NEXT_SCENE                 = "NEXT_SCENE";
    public static final String NEXT_WORD                  = "NEXT_WORD";


    // Core log message types - anumation scenegraph and queued scenegraph
    //
    public static final String TUTOR_STATE_MSG            = "TSTag";
    public static final String GRAPH_MSG                  = "RTag";
    public static final String QGRAPH_MSG                 = "RQTag";
    public static final String LTKPLUS_MSG                = "RLTag";
    public static final String LOGSTATE                   = "logState";
    public static final String BATTERY_MSG                = "Battery";

    public static final String AUDIO_EVENT                = "AUDIO_EVENT";
    public static final String TYPE_AUDIO                 = "type_audio";
    public static final String TRACK_SEGMENT              = "TRACK_SEGMENT";

    // for logging and tracking student performance
    public static final String PERFORMANCE_TAG                  = "PERFORMANCE_TAG";
    public static final boolean CONSIDER_STUDENT_PERFORMANCE    = true;
    public static final boolean OVERRIDE_SELF_ASSESSMENT        = false;
    public static final double HIGH_PERFORMANCE_THRESHOLD       = 0.9; // percent to be upgraded a level
    public static final double MID_PERFORMANCE_THRESHOLD        = 0.5; // percent to pass
    public static final int MIN_ATTEMPTS_TO_GRADE               = 5;   // minimum number of attempts to be graded

    public static final String DEFAULT_STUDENT_ID = "DEBUG";
    public static final String STUDENT_ID_VAR = "studentId";
    public static final String SESSION_ID_VAR = "sessionId";
    public static final String LAST_TUTOR = "LAST_TUTOR_PLAYED";


    static public HashMap<String, Integer> colorMap = new HashMap<String,Integer>();
    //
    // This is used to map "states" to colors

    static {
        colorMap.put(TCONST.COLORWRONG,  new Integer(0xFFFF0000));
        colorMap.put(TCONST.COLORERROR,  new Integer(0x44000000));
        colorMap.put(TCONST.COLORWARNING,new Integer(0xFFFFFF00));
        colorMap.put(TCONST.COLORRIGHT,  new Integer(0xff0000ff));
        colorMap.put(TCONST.COLORNORMAL, new Integer(0xff000000));
        colorMap.put(TCONST.COLORNONE,   new Integer(0x00000000));
    }

    public static final String COLORINDET          = "indeterminate";
    public static final String COLORWRONG          = "wrong";
    public static final String COLORWARNING        = "warning";
    public static final String COLORRIGHT          = "right";
    public static final String COLORERROR          = "error";
    public static final String COLORNORMAL         = "normal";
    public static final String COLORNONE           = "none";

    public static final int STROKE_STIM_UNDERLINE  = 5;


    static public HashMap<String, String> fontMap = new HashMap<String, String>();

    static {
        fontMap.put("grundschrift",         FONT_FOLDER + "Grundschrift.ttf");
        fontMap.put("grundschrift-kontur",  FONT_FOLDER + "Grundschrift-Kontur.otf");
        fontMap.put("grundschrift-punkt",   FONT_FOLDER + "Grundschrift-Punkt.otf");
    }


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
    public static final String MEDIA_STORY = "story";

    // This maps features to 2 letter codes used to build filepaths.
    static {
        langMap.put(LANG_EFFECT, "effect");
        langMap.put(LANG_EN,     "en");
        langMap.put(LANG_SW,     "sw");
    }

    // JSON parameter constants


    // Loader Constants
    static final public String TUTORROOT          = "tutors";

    static final public String BASE_ASSETS        = "assets";
    public static final String STORY_ASSETS       = "story";
    public static final String ICON_ASSETS        = "icons";
    static final public String EXTERNAL           = "external";
    static final public String ROBOTUTOR_ASSETS   = "sdcard/robotutor_assets/assets";
    static final public String LOCAL_STORY_AUDIO  = "sdcard/Download/RoboTutor/assets/story_questions";

    static final public String LTK_PROJECT_ASSETS = "projects";
    static final public String LTK_GLYPH_ASSETS   = "glyphs";

    static final public String LTK_PROJEXCTS      = "projects.zip";
    static final public String LTK_GLYPHS         = "glyphs.zip";
    static final public String LTK_DATA_FOLDER    = "/";                // should terminate in path sep '/'

    static final public String EDESC              = "engine_descriptor.json";
    static final public String TDESC              = "tutor_descriptor.json";
    static final public String SGDESC             = "scene_graph.json";
    static final public String SNDESC             = "navigator_descriptor.json";
    static final public String AGDESC             = "animator_graph.json";
    static final public String TDATA              = "trackdata/LIBRARY";
    static final public String AUDIOPATH          = "audio";
    static final public String TASSETS            = "assets";
    static final public String DEFAULT            = "default";

    // CTutorNavigator Constants
    // TODO this is so annoying... different objects use the same ENDTUTOR var
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
    public static final String EXTERN_SHARED   = "EXTERN_SHARED";
    public static final String DEFINED         = "DEFINED";

    public static final String DEBUG_STORY_TAG = "GENERAL_TSO";
    public static final String DEBUG_HESITATE = "MATH_HESITATE";
    public static final String DEBUG_MENU      = "DEBUG_MENU";

    // Navigator types
    final static public String SIMPLENAV       = "SIMPLE_NAVIGATOR";
    final static public String GRAPHNAV        = "GRAPH_NAVIGATOR";
    public static final String NEXT_NODE       = "NEXT_NODE";
    public static final String CANCEL_NODE     = "CANCEL_NODE";
    public static final String NEXT_TNODE      = "NEXT_TNODE";
    public static final String NEXTSCENE       = "NEXTSCENE";
    public static final String FIRST_SCENE     = "GOTO_FIRST_SCENE";
    public static final String REC_GLYPH       = "REC_GLYPH";


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
    //UHQ
    public static final String PLAY_CLOZE      = "PLAY_CLOZE";
    public static final String CLZ_ANIM_INCOMPLETE = "CLZ_ANIM_INCOMPLETE";
    public static final String CLZ_ANIM_COMPLETE = "CLZ_ANIM_COMPLETE";
    public static long CLOZE_END               = 0L;
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

    public static final String END_OF_GRAPH             = "END_OF_GRAPH";
    public static final String APPLY_NODE               = "APPLY_NODE";

    public static final String APPLY_BEHAVIOR           = "APPLY_BEHAVIOR";

    public static final String SUBGRAPH                 = "SUBGRAPH";
    public static final String SUBGRAPH_CALL            = "SUBGRAPH_CALL";
    public static final String SUBGRAPH_RETURN_AND_GO   = "SUBGRAPH_RETURN_AND_GO";
    public static final String SUBGRAPH_RETURN_AND_WAIT = "SUBGRAPH_RETURN_AND_WAIT";


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
    public static final String CMD_DEBUG        = "DEBUG";
    public static final String CMD_WAIT         = "WAIT";
    public static final String CMD_GOTO         = "GOTONODE";
    public static final String CMD_NEXT         = "NEXT";
    public static final String CMD_LAUNCH       = "LAUNCH-TUTOR";


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
    public static final String STARE_START = "STARE_START";
    public static final String STARE_STOP  = "STARE_STOP";

    public static final String FTR_STORY_STARTING = "FTR_STORY_STARTING";

    public static final String FWCORRECT    = "FTR_RIGHT";
    public static final String FWINCORRECT  = "FTR_WRONG";
    public static final String FWUNKNOWN    = "FTR_UNRECOGNIZED";
    public static final String FTR_EOI      = "FTR_NOWORDS";
    public static final String FTR_EOD      = "FTR_EOD";
    public static final String CONTINUE      = "CONTINUE";
    public static final String ALL_CORRECT  = "ALL_CORRECT";

    public static final String LOG_CORRECT = "CORRECT";
    public static final String LOG_INCORRECT = "INCORRECT";

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


    public static final String ASR_TIMED_START_EVENT    = "ASR_TIMED_START_EVENT";
    public static final String ASR_RECOGNITION_EVENT    = "ASR_RECOGNITION_EVENT";
    public static final String ASR_ERROR_EVENT          = "ASR_ERROR_EVENT";
    public static final String ASR_SILENCE_EVENT        = "ASR_SILENCE_EVENT";
    public static final String ASR_SOUND_EVENT          = "ASR_SOUND_EVENT";
    public static final String ASR_WORD_EVENT           = "ASR_WORD_EVENT";
    public static final String ASR_TIMEDSILENCE_EVENT   = "ASR_TIMEDSILENCE_EVENT";
    public static final String ASR_TIMEDSOUND_EVENT     = "ASR_TIMEDSOUND_EVENT";
    public static final String ASR_TIMEDWORD_EVENT      = "ASR_TIMEDWORD_EVENT";
    public static final String UTTERANCE_COMPLETE_EVENT = "UTTERANCE_COMPLETE_EVENT";

    public static final String ASR_ALL_TIMED_EVENTS   = "ASR_ALL_TIMED_EVENTS";
    public static final String ASR_ALL_STATIC_EVENTS  = "ASR_ALL_STATIC_EVENTS";
    public static final String ASR_ALL_EVENTS         = "ASR_ALL_EVENTS";

    // Map script event names to bitmap ASR Listener conatants.
    //
    static public HashMap<String, Integer> ASREventMap = new HashMap<String, Integer>();

    static {
        ASREventMap.put(ASR_SILENCE_EVENT, TCONST.SILENCE_EVENT);
        ASREventMap.put(ASR_SOUND_EVENT, TCONST.SOUND_EVENT);
        ASREventMap.put(ASR_WORD_EVENT, TCONST.WORD_EVENT);
        ASREventMap.put(ASR_TIMEDSILENCE_EVENT, TCONST.TIMEDSILENCE_EVENT);
        ASREventMap.put(ASR_TIMEDSOUND_EVENT, TCONST.TIMEDSOUND_EVENT);
        ASREventMap.put(ASR_TIMEDWORD_EVENT, TCONST.TIMEDWORD_EVENT);
        ASREventMap.put(ASR_TIMED_START_EVENT, TCONST.TIMEDSTART_EVENT);

        ASREventMap.put(ASR_ALL_TIMED_EVENTS,TCONST.ALLTIMED_EVENTS);
        ASREventMap.put(ASR_ALL_STATIC_EVENTS,TCONST.ALL_EVENTS);
        ASREventMap.put(ASR_ALL_EVENTS,TCONST.ALL_EVENTS);
    }



    public static final int NOINTERVENTION = 0;
    public static final int INSPEECH       = 1;
    public static final int SAYWORD        = 2;

    public static final String STORYDATA   = "storydata.json";
    public static final String STORYMCQ    = "mcq.json";
    public static final String STORYINDEX  = "story_index.json";
    public static final String SOURCEFILE  = "[file]";
    public static final String ASSETFILE   = "[asset]";


    public static final String TTS         = "TTS";
    public static final String ASR         = "ASR";

    public static final String GLYPH_DATA  = "GLYPH_DATA";


    // LTK messaging constants
    public static final String FW_STIMULUS = "FW_UPDATED";
    public static final String FW_VALUE    = "FW_VALUE";
    public static final String FW_EOI      = "FW_EOI";
    public static final String FW_RESPONSE = "FW_RESPONSE";

    public static final String WRITINGTUTOR_FOLDER      = "/WritingTutor/";
    public static final String HOT_LOG_FOLDER = "/RoboTutor_HOT/";
    public static final String READY_LOG_FOLDER = "/RoboTutor/";

    public static final String  HOT_LOG_FOLDER_PERF = "/RoboTutor_HOT/"; // use same as normal logs
    public static final String READY_LOG_FOLDER_PERF = "/RoboTutor/";    // use same as normal logs

    public static final String AUDIO_LOG_FOLDER = "/RoboTutor Audio/";

    public static final String ROBOTUTOR_ASSET_FOLDER   = "/robotutor_assets/";
    public static final String GLYPHS_FOLDER            = "/glyphs/";


    // Listener Control message types
    public static final String LISTENER_RESPONSE = "LISTENER_RESPONSE";


    // TTS command constants
    public static final String SAY             = "SAY";
    public static final String SET_RATE        = "SET_RATE";


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
    public static final String NEXTTURN = "THRD_WRONG";
    public static final String GENERIC_SUCCESSIVEWRONG = "FTR_SWRONG";
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

    public static final String PAGEFLIP_BUTTON = "PAGE_FLIP_CLICK";
    public static final String SPEAK_BUTTON    = "SPEAK_CLICK";

    public static final String EMPTY = "";

    public static final int INCR = 1;
    public static final int DECR = -1;

    public static final String RTC_VAR_ECHOSTATE   = ".echoState";
    public static final String RTC_VAR_PARROTSTATE = ".parrotState";
    // Generic question state flag
    public static final String RTC_VAR_QUESTIONSTATE = ".questionState";
    public static final String RTC_VAR_CLOZESTATE = ".clozeState";
    public static final String TO_CLOZE = ".toCloze";

    public static final String RTC_VAR_SILENCESTATE = ".silenceState";
    // Generic question audio file
    public static final String RTC_QUESTION_AUDIO = ".questionAudio";
    public static final String RTC_SILENCE = ".silence";
    public static final String COMPLETE = "COMPLETE";
    public static final String RTC_VAR_PAGESTATE   = ".pageState";
    public static final String RTC_VAR_PARASTATE   = ".paraState";
    public static final String RTC_VAR_LINESTATE   = ".lineState";
    public static final String RTC_VAR_WORDSTATE   = ".wordState";
    public static final String RTC_VAR_ATTEMPT     = ".attempt";
    public static final String LAST                = "LAST";
    public static final String NOT_LAST            = "NOT_LAST";
    //UHQ
    public static final String HAS_DISTRACTOR      = ".distractor";


    public static final String RTC_VAR_STATE         = ".storyState";
    public static final String RTC_PARAGRAPHCOMPLETE = "PARAGRAPH_COMPLETE";
    public static final String RTC_PAGECOMPLETE      = "PAGE_COMPLETE";
    public static final String RTC_STORYCMPLETE      = "STORY_COMPLETE";
    public static final String RTC_LINECOMPLETE      = "LINE_COMPLETE";
    //Generic question state of completion
    public static final String RTC_CLEAR             = "";

    public static final String RTC_VAR_PROMPT      = ".prompt";
    public static final String RTC_VAR_PAGE_PROMPT = ".page_prompt";
    public static final String RTC_VAR_WORDVALUE   = ".currentWord";
    public static final String RTC_VAR_INDEX       = ".wordindex";
    public static final String RTC_VAR_REMAINING   = ".remainingWords";
    public static final String RTC_VAR_SENTENCE    = ".sentence";
    public static final String RTC_VAR_UTTERANCE   = ".utterance";

    //Akira Game Prompt Situation
    public static final String PROMPT_1LEFT = "PROMPT_1LEFT";
    public static final String PROMPT_1MID = "PROMPT_1MID";
    public static final String PROMPT_1RIGHT = "PROMPT_1RIGHT";
    public static final String PROMPT_2LEFT = "PROMPT_2LEFT";
    public static final String PROMPT_2MID = "PROMPT_2MID";
    public static final String PROMPT_2RIGHT = "PROMPT_2RIGHT";
    public static final String PROMPT_3 = "PROMPT_3";
    public static final String PROMPT_3V = "PROMPT_3V";


    // Writing behavior...
    public static final int WRITING_DATA_LIMIT = 10;

    // Counting
    public static final String COUNTING_DEBUG_LOG = "COUNTING_DEBUG_LOG";


    // Data source debugger

    public static final String TAG_DEBUG_AKIRA = "akira";
    public static final String TAG_DEBUG_ASM = "math";
    public static final String TAG_DEBUG_TAP_COUNT = "countingx";
    public static final String TAG_DEBUG_BPOP_LTR = "bpop.ltr.mix";
    public static final String TAG_DEBUG_BPOP_WRD = "bpop.wrd";
    public static final String TAG_DEBUG_BPOP_PHON = "bpop.phon";
    public static final String TAG_DEBUG_BPOP_NUM = "bpop.num";
    public static final String TAG_DEBUG_BPOP_SHP = "bpop.shp";
    public static final String TAG_DEBUG_BPOP_EX = "bpop.ex";

    public static final String ROBO_DEBUG_FILE_TAP_COUNT = "countingx_test.json";
    public static final String ROBO_DEBUG_FILE_AKIRA = "akira_test.json";
    public static final String ROBO_DEBUG_FILE_ASM = "math_test.json";
    public static final String ROBO_DEBUG_FILE_BPOP = "bpop.json";


    // Debugger Thumb key words
    // NEW_THUMBS trace me
    public enum Thumb {
        AKIRA, BPOP_NUM, GL, MN, BPOP_LTR, CX_1, CX_10, CX_100, MATH, NUMSCALE, STORY_1, STORY_2, STORY_3, STORY_4, STORY_5, SONG, STORY_NONSTORY, WRITE, SPELLING, PICMATCH, NUMCOMPARE, COMPREHENSION, PLACEVALUE, BIGMATH, NOTHING
    }

    // debug vals
    public static final String DEBUG_GRAY_SCREEN_TAG = "DEBUG_GRAY_SCREEN";

    public static final String DEBUG_AUDIO_FILE = "DEBUG_AUDIO";

    public static final String PLACEMENT_TAG = "DEBUG_PLACEMENT";
    public static final String MENU_BUG_TAG  = "MENU_BUG";
    public static final String MATH_PLACEMENT = "MATH_PLACEMENT"; // TOGGLE_PLACEMENT trace me...
    public static final String MATH_PLACEMENT_INDEX = "MATH_PLACEMENT_INDEX";
    public static final String WRITING_PLACEMENT = "WRITING_PLACEMENT";
    public static final String WRITING_PLACEMENT_INDEX = "WRITING_PLACEMENT_INDEX";
}
