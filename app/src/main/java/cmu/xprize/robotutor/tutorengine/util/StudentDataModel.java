package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 *
 * DATA_MODEL this is too slow... change to a JSON object, and store all at once
 * DATA_MODEL see https://stackoverflow.com/questions/7145606/how-android-sharedpreferences-save-store-object
 */

public class StudentDataModel {

    private static final String TAG = "StudentDataModel";

    private Context _context;
    private String _prefsID;

    private SharedPreferences _preferences;
    private SharedPreferences.Editor _editor;

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
        _editor = _preferences.edit();
        _editor.putString(TCONST.SKILL_WRITING, id);
        _editor.apply();
    }

    public String getStoryTutorID() {
        return _preferences.getString(TCONST.SKILL_STORIES, null);
    }

    public void updateStoryTutorID(String id) {
        _editor = _preferences.edit();
        _editor.putString(TCONST.SKILL_STORIES, id);
        _editor.apply();
    }

    public String getMathTutorID() {
        return _preferences.getString(TCONST.SKILL_MATH, null);
    }

    public void updateMathTutorID(String id) {
        _editor = _preferences.edit();
        _editor.putString(TCONST.SKILL_MATH, id);
        _editor.apply();
    }



    public final static String HAS_PLAYED_KEY = "HAS_PLAYED";
    public final static String MATH_PLACEMENT_KEY = "MATH_PLACEMENT";
    public final static String MATH_PLACEMENT_INDEX_KEY = "MATH_PLACEMENT_INDEX";
    public final static String WRITING_PLACEMENT_KEY = "WRITING_PLACEMENT";
    public final static String WRITING_PLACEMENT_INDEX_KEY = "WRITING_PLACEMENT_INDEX";


    public String getActiveSkill() {
        /// √√√
        return _preferences.getString(TCONST.SKILL_SELECTED, SELECT_STORIES);
    }

    boolean getWritingPlacement() {
        // √√√
        return _preferences.getBoolean(WRITING_PLACEMENT_KEY, false);
    }

    int getWritingPlacementIndex() {
        // √√√
        return _preferences.getInt(WRITING_PLACEMENT_INDEX_KEY, 0);
    }

    boolean getMathPlacement() {
        return _preferences.getBoolean(MATH_PLACEMENT_KEY, false);
    }

    int getMathPlacementIndex() {
        return  _preferences.getInt(MATH_PLACEMENT_INDEX_KEY, 0);
    }

    void updateLastTutor(String activeTutorId) {

        _editor = _preferences.edit();
        _editor.putString (TCONST.LAST_TUTOR, activeTutorId);
        _editor.apply();
    }

    void updateMathPlacement(boolean b) {
        _editor = _preferences.edit();
        _editor.putBoolean(MATH_PLACEMENT_KEY, b);
        _editor.apply();
    }

    void updateMathPlacementIndex(Integer i) {
        _editor = _preferences.edit();
        if (i == null) {
            _editor.remove(MATH_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(MATH_PLACEMENT_INDEX_KEY, i);
        }
        _editor.apply();
    }

    void updateWritingPlacement(boolean b) {
        _editor = _preferences.edit();
        _editor.putBoolean("WRITING_PLACEMENT", b);
        _editor.apply();
    }

    void updateWritingPlacementIndex(Integer i) {
        _editor = _preferences.edit();
        if (i == null) {
            _editor.remove(WRITING_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(WRITING_PLACEMENT_INDEX_KEY, i);
        }
        _editor.apply();
    }


    /**
     * DATA_MODEL how to get whole model at once, instead of individual elements...
     * @param key
     */
    private void getWholeModel(String key) {
        Gson gson = new Gson();
        String json = _preferences.getString(key, "");
        StudentDataModel newThis = gson.fromJson(json, StudentDataModel.class);
    }

    /**
     * DATA_MODEL how to save whole model at once, instead of individual elements...
     * @param model
     */
    private boolean saveWholeModel(StudentDataModel model) {
        _editor = _preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(model);
        Log.d(TAG, "This is what it looks like: " + json);
        _editor.putString("key", json);
        return _editor.commit();
    }
}
