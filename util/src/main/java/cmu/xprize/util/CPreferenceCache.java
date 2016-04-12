package cmu.xprize.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import java.util.HashMap;

public class CPreferenceCache {

    static private Activity         context;
    static private int              logID   = 0;

    static HashMap<String,String>  idMap;


    static public int initLogPreference(Activity app) {

        context = app;
        idMap   = new HashMap<>();

        return updateTutorInstance(TCONST.ENGINE_INSTANCE);
    }


    static public int updateTutorInstance(String key) {

        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int ordinal = prefs.getInt(key, 0);

        String value = key + "_" + ordinal;

        // We set both the unique tutor key and the general "tutor" key
        // "tutor" may then be used to anonymously access the current tutors key
        //
        idMap.put(key, value);
        idMap.put(TCONST.CURRENT_TUTOR, value);

        editor.putInt(key, ordinal + 1);
        editor.apply();

        return ordinal;
    }


    static public String getPrefID(String key) {

        return idMap.get(key);
    }

}
