package cmu.xprize.util;

import java.util.HashMap;

import cmu.xprize.util.TCONST;

public class CEventMap {

    static public HashMap<String, Integer> eventMap = new HashMap<String, Integer>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        eventMap.put("SILENCE_EVENT", TCONST.SILENCE_EVENT);
        eventMap.put("SOUND_EVENT", TCONST.SOUND_EVENT);
        eventMap.put("WORD_EVENT", TCONST.WORD_EVENT);
        eventMap.put("SILENCE_TIMEOUT", TCONST.TIMEDSILENCE_EVENT);
        eventMap.put("SOUND_TIMEOUT", TCONST.TIMEDSOUND_EVENT);
        eventMap.put("WORD_TIMEOUT", TCONST.TIMEDWORD_EVENT);
        eventMap.put("START_TIMEOUT", TCONST.TIMEDSTART_EVENT);
        eventMap.put("ALL_TIMED_EVENTS",TCONST.ALLTIMED_EVENTS);
        eventMap.put("ALL_STATIC_EVENTS",TCONST.ALL_EVENTS);
        eventMap.put("ALL_EVENTS",TCONST.ALL_EVENTS);
    }
}
