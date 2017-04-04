package cmu.xprize.comp_logging;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Random;

public class CPreferenceCache {

    static private Activity         context;
    static private int              logID   = 0;

    static String                   guidMap = "ABCDEFGHIJKLMNPQRSTUVWXYZ23456789";
    static HashMap<String,String>   idMap;


    static public String initLogPreference(Activity app) {

        context = app;
        idMap   = new HashMap<>();

        return updateTutorGUID(TLOG_CONST.ENGINE_INSTANCE);
    }


    /**
     * This sets a unique ordinal name for the given key of the form - <key>_##
     * ir returns just the number as an int
     *
     * @param key
     * @return
     */
    static public int updateTutorInstance(String key) {

        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int ordinal = prefs.getInt(key, 0);

        String value = key + "_" + ordinal;

        // We set both the unique tutor key and the general "tutor" key
        // "tutor" may then be used to anonymously access the current tutors key
        //
        idMap.put(key, value);
        idMap.put(TLOG_CONST.CURRENT_TUTOR, value);

        editor.putInt(key, ordinal + 1);
        editor.apply();

        return ordinal;
    }


    /**
     * This sets a unique 5 character GUID name for the given key of the form - <key>_<GUID>
     * it returns just the GUID string
     *
     * @param key
     * @return
     */
    static public String updateTutorGUID(String key) {

        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String GUID = getGUID();

        String value = key + "_" + GUID;

        // We set both the unique tutor key and the general "tutor" key
        // "tutor" may then be used to anonymously access the current tutors key
        //
        idMap.put(key, value);
        idMap.put(TLOG_CONST.CURRENT_TUTOR, value);

        editor.putString(key, GUID);
        editor.apply();

        return GUID;
    }


    /**
     * Generate a Globally Unique ID
     * The new Id is stored in the App Preferences as a Boolean map with a value of true
     * to indicate it has been used.
     *
     * @return
     */
    static private String getGUID() {

        boolean extant = true;
        String  guid   = "";

        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Find a unique GUID that doesn't exist in the preferences cache
        //
        do {
            for (int i1 = 0; i1 < TLOG_CONST.GUID_LEN; i1++) {
                try {
                    guid += guidMap.charAt((int) (Math.random() * guidMap.length()));
                }
                catch(Exception e) {
                }
            }
            extant = prefs.getBoolean("GUID_" + guid, false);

        }while(extant);

        editor.putBoolean("GUID_" + guid, true);
        editor.apply();

        return guid;
    }


    /**
     * Return the ID associated with the given key - this may be an ordinal type ID or GUID type
     *
     * @param key
     * @return
     */
    static public String getPrefID(String key) {

        return idMap.get(key);
    }

}
