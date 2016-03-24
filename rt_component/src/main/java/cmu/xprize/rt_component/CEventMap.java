package cmu.xprize.rt_component;

import java.util.HashMap;

import cmu.xprize.util.TCONST;

public class CEventMap {

    static public HashMap<String, Integer> eventMap = new HashMap<String, Integer>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        eventMap.put("SILENCE", TCONST.SILENCE_EVENT);
        eventMap.put("SOUND", TCONST.SOUND_EVENT);
        eventMap.put("WORD", TCONST.WORD_EVENT);
        eventMap.put("SILENCETIMEOUT", TCONST.TIMEDSILENCE_EVENT);
        eventMap.put("SOUNDTIMEOUT", TCONST.TIMEDSOUND_EVENT);
        eventMap.put("WORDTIMEOUT", TCONST.TIMEDWORD_EVENT);
        eventMap.put("STARTTIMEOUT", TCONST.TIMEDSTART_EVENT);
        eventMap.put("ALLTIMEDEVENTS",TCONST.ALLTIMED_EVENTS);
    }
}
