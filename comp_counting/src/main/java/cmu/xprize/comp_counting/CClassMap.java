package cmu.xprize.comp_counting;

import java.util.HashMap;


/**
 * Created by Kevin 2.0 on 10/23/2017
 */
public class CClassMap {

    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        classMap.put("Count_Data", CCount_Data.class);

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