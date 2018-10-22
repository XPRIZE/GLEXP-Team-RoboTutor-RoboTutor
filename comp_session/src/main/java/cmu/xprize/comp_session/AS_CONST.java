package cmu.xprize.comp_session;


public class AS_CONST {

    public static final String RIPPLE_DESCRIBE  = "RIPPLE_DESCRIBE";    // postEvent
    public static final String DESCRIBE_NEXT    = "DESCRIBE_NEXT";      // postNamed

    public static final String CANCEL_DESCRIBE  = "CANCEL_DESCRIBE";

    public static final class QUEUEMAP_KEYS {
        public static final String INTRO_STATEMENT_BEHAVIOR = "INTRO_STATEMENT_BEHAVIOR";
        public static final String INPUT_HESITATION_FEEDBACK = "INPUT_HESITATION_FEEDBACK";
        public static final String BUTTON_DESCRIPTION = "BUTTON_DESCRIPTION";
        public static final String BUTTON_BEHAVIOR = "BUTTON_BEHAVIOR";
        public static final String EXIT_BUTTON_BEHAVIOR = "EXIT_BUTTON_BEHAVIOR";
    }

    public static final class ACTIONMAP_KEYS {
        public static final String SET_HESITATION_FEEDBACK = "SET_HESITATION_FEEDBACK";
        public static final String CLEAR_HESITATION_BEHAVIOR = "CLEAR_HESITATION_BEHAVIOR";
    }

    public static final class BEHAVIOR_KEYS {
        public static final String DESCRIBE_BEHAVIOR = "DESCRIBE_BEHAVIOR"; // Behavior Key --> BUTTON_DESCRIPTION (queueMap)
        public static final String SELECT_BEHAVIOR   = "SELECT_BEHAVIOR";   // Behavior Key --> CLEAR_HESITATION_FEEDBACK (actionMap)
        public static final String DESCRIBE_COMPLETE = "DESCRIBE_COMPLETE"; // Behavior Key --> SET_HESITATION_FEEDBACK (actionMap)
        public static final String LAUNCH_EVENT      = "LAUNCH_EVENT";      // Behavior Key --> LAUNCH_BEHAVIOR (actionMap)

        public static final String SELECT_WRITING    = "SELECT_WRITING";    // Behavior Key
        public static final String SELECT_STORIES    = "SELECT_STORIES";    // Behavior Key
        public static final String SELECT_MATH       = "SELECT_MATH";       // Behavior Key

        // behavior options for new menu
        public static final String SELECT_OPTION_0 = "SELECT_OPTION_0";
        public static final String SELECT_OPTION_1 = "SELECT_OPTION_1";
        public static final String SELECT_OPTION_2 = "SELECT_OPTION_2";
    }

    public static final String BUTTON_EVENT      = "BUTTON_EVENT";      // BUTTON_BEHAVIOR --> postEvent(this)
    public static final String TAGGED_BUTTON_EVENT = "TAGGED_BUTTON_EVENT"; // TAG_BUTTON_BEHAVIOR --> postEvent(this)

    public static final String BUTTON1           = "SBUTTON1";
    public static final String BUTTON2           = "SBUTTON2";
    public static final String BUTTON3           = "SBUTTON3";
    public static final String BUTTON4           = "SBUTTON4";
    public static final String BUTTON5           = "SBUTTON5";

    public static final String SELECT_CONTINUE        = "SELECT_CONTINUE";          // get rid of me
    public static final String SELECT_MAKE_HARDER     = "SELECT_MAKE_HARDER";       // get rid of me
    public static final String SELECT_MAKE_EASIER     = "SELECT_MAKE_EASIER";       // get rid of me
    public static final String SELECT_AUTO_DIFFICULTY = "SELECT_AUTO_DIFFICULTY";   // get rid of me
    public static final String SELECT_REPEAT          = "SELECT_REPEAT";
    public static final String SELECT_EXIT            = "SELECT_EXIT";

    public static final String SELECT_NONE            = "SELECT_NONE";

    public static final String VAR_BUTTONID         = ".buttonid";
    public static final String VAR_BUT_BEHAVIOR     = ".buttonBehavior";
    public static final String VAR_DEBUG_TAG        = ".debugTag";
    public static final String VAR_HELP_AUDIO       = ".helpAudio";
    public static final String VAR_PROMPT_AUDIO     = ".promptAudio";

    public static final String VAR_INTENT           = ".intent";
    public static final String VAR_INTENTDATA       = ".intentData";
    public static final String VAR_DATASOURCE       = ".dataSource";
    public static final String VAR_TUTOR_ID         = ".tutorId";

    public static final String REPEAT_DEBUG_TAG = "REPEAT_STUFF";
}

