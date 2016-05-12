package cmu.xprize.robotutor.tutorengine.util;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.CMediaPackage;
import cmu.xprize.robotutor.tutorengine.graph.graph_module;
import cmu.xprize.robotutor.tutorengine.graph.graph_node;
import cmu.xprize.robotutor.tutorengine.graph.scene_node;
import cmu.xprize.robotutor.tutorengine.graph.type_action;
import cmu.xprize.robotutor.tutorengine.graph.type_audio;
import cmu.xprize.robotutor.tutorengine.graph.type_cond;
import cmu.xprize.robotutor.tutorengine.graph.type_timeline;
import cmu.xprize.robotutor.tutorengine.graph.type_timer;
import cmu.xprize.robotutor.tutorengine.graph.type_tts;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.robotutor.tutorengine.graph.vars.TChar;
import cmu.xprize.robotutor.tutorengine.graph.vars.TFloat;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TReference;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;

/**
 * Created by Kevin on 2/29/2016.
 */
public class CClassMap2 {

    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        classMap.put("ANIMATOR", scene_node.class);
        classMap.put("NODE", graph_node.class);
        classMap.put("MODULE", graph_module.class);
        classMap.put("TIMELINE", type_timeline.class);
        classMap.put("COMMAND", type_action.class);
        classMap.put("CONDITION", type_cond.class);
        classMap.put("TTS", type_tts.class);
        classMap.put("AUDIO", type_audio.class);
        classMap.put("TIMER", type_timer.class);

        classMap.put("TReference", TReference.class);
        classMap.put("TBoolean", TBoolean.class);
        classMap.put("TString", TString.class);
        classMap.put("TInteger", TInteger.class);
        classMap.put("TFloat", TFloat.class);
        classMap.put("TChar", TChar.class);

        classMap.put("string", String.class);
        classMap.put("bool", Boolean.class);
        classMap.put("int", Integer.class);
        classMap.put("float", Float.class);
        classMap.put("byte", Byte.class);
        classMap.put("long", Long.class);
        classMap.put("short", Short.class);
        classMap.put("object", Object.class);

        classMap.put("SOUNDMAP", CMediaPackage.class);
    }
}