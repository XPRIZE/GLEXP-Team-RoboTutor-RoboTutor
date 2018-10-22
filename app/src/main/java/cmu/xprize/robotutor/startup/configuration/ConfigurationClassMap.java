package cmu.xprize.robotutor.startup.configuration;

import java.util.HashMap;

public class ConfigurationClassMap {
    static public HashMap<String, Class> classMap = new HashMap<>();

    static {
        classMap.put("ConfigurationItems", ConfigurationItems.class);

        classMap.put("string", String.class);
        classMap.put("bool", Boolean.class);
        classMap.put("int", Integer.class);
        classMap.put("float", Float.class);
        classMap.put("byte", Byte.class);
        classMap.put("long", Long.class);
        classMap.put("short", Short.class);
        classMap.put("object", Object.class);
    }
}
