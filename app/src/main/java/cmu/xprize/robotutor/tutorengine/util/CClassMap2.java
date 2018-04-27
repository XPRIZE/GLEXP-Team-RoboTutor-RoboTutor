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

package cmu.xprize.robotutor.tutorengine.util;

import java.util.HashMap;

import cmu.xprize.robotutor.tutorengine.graph.defvar_tutor;
import cmu.xprize.robotutor.tutorengine.graph.scene_queuedgraph;
import cmu.xprize.robotutor.tutorengine.graph.type_queuedaction;
import cmu.xprize.robotutor.tutorengine.graph.type_timelineFL;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.robotutor.tutorengine.CMediaPackage;
import cmu.xprize.robotutor.tutorengine.graph.defdata_scenes;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.scene_module;
import cmu.xprize.robotutor.tutorengine.graph.scene_node;
import cmu.xprize.robotutor.tutorengine.graph.scene_graph;
import cmu.xprize.robotutor.tutorengine.graph.type_action;
import cmu.xprize.robotutor.tutorengine.graph.type_audio;
import cmu.xprize.robotutor.tutorengine.graph.type_queuedaudio;
import cmu.xprize.robotutor.tutorengine.graph.type_cond;
import cmu.xprize.robotutor.tutorengine.graph.type_handler;
import cmu.xprize.robotutor.tutorengine.graph.type_tts;
import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.robotutor.tutorengine.graph.vars.TChar;
import cmu.xprize.robotutor.tutorengine.graph.vars.TFloat;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.robotutor.tutorengine.graph.vars.TReference;
import cmu.xprize.robotutor.tutorengine.graph.vars.TString;
import cmu.xprize.util.CPlacementTest_Tutor;

public class CClassMap2 {

    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        classMap.put("ANIMATOR", scene_graph.class);
        classMap.put("SUBGRAPH", scene_graph.class);
        classMap.put("NODE", scene_node.class);
        classMap.put("MODULE", scene_module.class);
        classMap.put("TIMELINE", type_timelineFL.class);
        classMap.put("COMMAND", type_action.class);
        classMap.put("CONDITION", type_cond.class);
        classMap.put("TTS", type_tts.class);
        classMap.put("AUDIO", type_audio.class);
        classMap.put("TIMER", type_handler.class);

        classMap.put("QUEUE", scene_queuedgraph.class);
        classMap.put("QUEUEDAUDIO", type_queuedaudio.class);
        classMap.put("QUEUEDCOMMAND", type_queuedaction.class);

        classMap.put("TRANSITION", CAt_Data.class);
        classMap.put("PLACEMENT", CPlacementTest_Tutor.class);

        classMap.put("TUTORDATA_MAP", defdata_tutor.class);
        classMap.put("TUTORVAR_MAP", defvar_tutor.class);
        classMap.put("SCENEDATA_MAP", defdata_scenes.class);

        classMap.put("STRING_ARRAY", String[].class);

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