package cmu.xprize.comp_bigmath;

import java.util.HashMap;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CClassMap {

    static public HashMap<String, Class> classMap = new HashMap<String, Class>();

    //
    // This is used to map "type" (class names) used in json HashMap specs to real classes

    static {
        classMap.put("CBigMath_Data", CBigMath_Data.class);

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