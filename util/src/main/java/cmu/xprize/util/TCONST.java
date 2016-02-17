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

public class TCONST {

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
    static final public String DEFAULT         = "default";

    // CTutorNavigator Constants

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
    public static final String GOTONEXTSCENE   = "NAVNEXT";
    public static final String GOTONEXT        = "NEXT";


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

    public static final String PLAY            = "PLAY";
    public static final String STOP            = "STOP";
    public static final String NEXT            = "NEXT";
    public static final String PAUSE           = "PAUSE";
    public static final String START           = "START";
    public static final String CANCEL          = "CANCEL";
    public static final String CREATE          = "CREATE";
    public static final String CREATEANDSTART  = "CREATEANDSTART";

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
    public static final String GOTONODE  = "GOTONODE";
    public static final String NEXTSCENE = "NEXTSCENE";

    // Intrinsic types
    public static final String TREFERENCE  = "TReference";

    public static final String AUDIOEVENT  = "event";
    public static final String AUDIOSTREAM = "stream";
    public static final String AUDIOFLOW   = "flow";

    public static final String   LOOKATSTART = "PERSONA_LOOKAT_START";
    public static final String   LOOKAT      = "PERSONA_LOOKAT";
    public static final String   LOOKATEND   = "PERSONA_LOOKAT_END";
    public static final String   SCREENPOINT = "SCREENPOINT";

    public static final String FWCORRECT    = "FTR_RIGHT";
    public static final String FWINCORRECT  = "FTR_WRONG";
    public static final String FTR_EOI      = "FTR_NOWORDS";
    public static final String FWALLCORRECT = "ALL_CORRECT";

    public static final String FALSE        = "FALSE";
    public static final String TRUE         = "TRUE";
}
