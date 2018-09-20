package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;

import cmu.xprize.comp_numberscale.CNumberScale_player;
import cmu.xprize.util.TCONST;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 */

public class StudentDataModel {

    private Context _context;
    private String _prefsID;
    private SharedPreferences _preferences;

    // DATA_MODEL (1) put placeholders
    // DATA_MODEL (2) insert SharedPreferences functionality
    // DATA_MODEL (3) replace static w/ instance-based
    // DATA_MODEL (4) do for other content-areas
    // DATA_MODEL (9) other small things...

    public StudentDataModel(Context context, String prefsID) {
        this._context = context;
        this._prefsID = prefsID;

        _preferences = getStudentSharedPreferences();
    }

    private SharedPreferences getStudentSharedPreferences() {
        return _context.getSharedPreferences(_prefsID, Context.MODE_PRIVATE);
    }

    public String getWritingTutorID() {
        return _preferences.getString(TCONST.SKILL_WRITING, null);
    }

    public void updateWritingTutorID(String id) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(TCONST.SKILL_WRITING, id);
        editor.apply();
    }

    public String getStoryTutorID() {
        return _preferences.getString(TCONST.SKILL_STORIES, null);
    }

    public void updateStoryTutorID(String id) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(TCONST.SKILL_STORIES, id);
        editor.apply();
    }

    public String getMathTutorID() {
        return _preferences.getString(TCONST.SKILL_MATH, null);
    }

    public void updateMathTutorID(String id) {
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putString(TCONST.SKILL_MATH, id);
        editor.apply();
    }


}
