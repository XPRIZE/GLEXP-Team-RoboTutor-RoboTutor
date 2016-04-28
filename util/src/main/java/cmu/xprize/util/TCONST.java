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

    public static final String NUMDATA_HEADER = "{\n" + "\"dataSource\": ";


    //*** Reading Tutor compatible string combinations

    static public HashMap<String, String> numberMap = new HashMap<String, String>();

    static {
        numberMap.put("LANG_EN", "AND,ZERO,ONE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,EIGHT,NINE,TEN,ELEVEN,TWELVE,THIRTEEN,FORTEEN,FIFTEEN,SIXTEEN,SEVENTEEN,EIGHTEEN,NINETEEN,TWENTY,THIRTY,FORTY,FIFTY,SIXTY,SEVENTY,EIGHTY,NINETY,HUNDRED,THOUSAND,MILLION");
        numberMap.put("LANG_SW", "NA,SIFURI,MOJA,MBILI,TATU,NNE,TANO,SITA,SABA,NANE,TISA,KUMI,ISHIRINI,THELATHINI,AROBAINI,HAMSINI,SITINI,SABINI,THEMANINI,TISINI,MIA,ELFU,MILIONI");
    }


    // This is used to map "language features" to the story resources
    // these are located in the assets/<lang>
    // Note: on Android these are case sensitive filenames

    static public HashMap<String, String> langMap = new HashMap<String, String>();

    public static final String LANG_EN = "LANG_EN";
    public static final String LANG_SW = "LANG_SW";

    // This maps features to 2 letter codes used to build filepaths.
    static {
        langMap.put(LANG_EN, "en");
        langMap.put(LANG_SW, "sw");
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


    public static final String ENDTUTOR        = "END_TUTOR";
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
    public static final String CANCEL          = "CANCEL";
    public static final String CREATE          = "CREATE";
    public static final String CREATEANDSTART  = "CREATEANDSTART";
    public static final String ENTER_SCENE     = "ENTER_SCENE";

    // Condition parser FSM states
    public static final int STARTSTATE = 0;
    public static final int PARSESTATE = 1;
    public static final int PARSEIDENT = 2;
    public static final int PARSENUM = 3;
    public static final int PARSEVAR = 4;
    public static final int PARSEPROP = 5;
    public static final int RESOLVESYMBOL = 6;
    public static final int ENDSUBEXPR = 7;
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
    public static final String CMD_WAIT    = "WAIT";
    public static final String CMD_GOTO    = "GOTONODE";
    public static final String CMD_NEXT    = "NEXT";
    public static final String CMD_LAUNCH  = "LAUNCH-TUTOR";


    // Intrinsic types
    public static final String TREFERENCE  = "TReference";

    public static final String AUDIOEVENT  = "event";
    public static final String AUDIOSTREAM = "stream";
    public static final String AUDIOFLOW   = "flow";

    public static final String LOOKATSTART = "PERSONA_LOOKAT_START";
    public static final String LOOKAT      = "PERSONA_LOOKAT";
    public static final String LOOKATEND   = "PERSONA_LOOKAT_END";
    public static final String SCREENPOINT = "SCREENPOINT";

    public static final String FWCORRECT    = "FTR_RIGHT";
    public static final String FWINCORRECT  = "FTR_WRONG";
    public static final String FWUNKNOWN    = "FTR_UNRECOGNIZED";
    public static final String FTR_EOI      = "FTR_NOWORDS";
    public static final String FTR_EOD      = "FTR_EOD";
    public static final String FWALLCORRECT = "ALL_CORRECT";

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
    public static final String JSONLOG     = ".json";
    public static final boolean APPEND     = true;


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
    public static final String ENGINE_INSTANCE = "engine_instance";
    public static final String CURRENT_TUTOR   = "tutor";


    // Number Listeneing Component
    public static final String ERR_SINGLEDIGIT = "Single Digit Error";
    public static final String ERR_MULTIDIGIT  = "Multi Digit Error";
    public static final String TEXT_FIELD = ".text";


    // Generic error codes
    public static final String GENERIC_RIGHT = "FTR_RIGHT";
    public static final String GENERIC_WRONG = "FTR_WRONG";
    public static final boolean TRUE_ERROR   = true;
    public static final boolean TRUE_NOERROR = true;
    public static final boolean FALSE_NOERROR = false;
    public static final boolean FALSE_ERROR = false;

}
