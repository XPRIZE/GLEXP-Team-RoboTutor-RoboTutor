package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.util.TCONST.LAST_TUTOR;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 *
 * DATA_MODEL this *may* be too slow... change to a JSON object, and store all at once
 * DATA_MODEL see https://stackoverflow.com/questions/7145606/how-android-sharedpreferences-save-store-object
 */

public class StudentDataModel {

    private static final String TAG = "StudentDataModel";

    private static SharedPreferences _preferences;
    private static SharedPreferences.Editor _editor;

    private final static String HAS_PLAYED_KEY = "HAS_PLAYED";
    private final static String MATH_PLACEMENT_KEY = "MATH_PLACEMENT";
    private final static String MATH_PLACEMENT_INDEX_KEY = "MATH_PLACEMENT_INDEX";
    private final static String WRITING_PLACEMENT_KEY = "WRITING_PLACEMENT";
    private final static String WRITING_PLACEMENT_INDEX_KEY = "WRITING_PLACEMENT_INDEX";
    private static final String SKILL_SELECTED_KEY = "SKILL_SELECTED";

    /**
     * Constructor
     * @param context needed to call getSharedPreferences
     * @param prefsID the ID of the student
     */
    public StudentDataModel(Context context, String prefsID) {
        _preferences = context.getSharedPreferences(prefsID, Context.MODE_PRIVATE);
    }

    /**
     * Initializes the student with beginning values
     */
    public static void createNewStudent() {
        _editor = _preferences.edit();
        _editor.putString(HAS_PLAYED_KEY, String.valueOf(true));

        // writing: Placement = true. Placement Index starts at 0
        _editor.putBoolean(StudentDataModel.WRITING_PLACEMENT_KEY, true);
        _editor.putInt(StudentDataModel.WRITING_PLACEMENT_INDEX_KEY, 0);

        // math: Placement = true. Placement Index starts at 0
        _editor.putBoolean(StudentDataModel.MATH_PLACEMENT_KEY, true);
        _editor.putInt(StudentDataModel.MATH_PLACEMENT_INDEX_KEY, 0);

        _editor.apply();
    }

    /**
     * Whether the student has played RoboTutor before.
     * If not, will be updated
     *
     * @return "true" or null.
     */
    public String getHasPlayed() {
        return _preferences.getString(StudentDataModel.HAS_PLAYED_KEY, null);
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

    public String getActiveSkill() {
        return _preferences.getString(SKILL_SELECTED_KEY, SELECT_STORIES);
    }

    public void updateActiveSkill(String skill) {
        _editor = _preferences.edit();
        _editor.putString(SKILL_SELECTED_KEY, skill);
        _editor.apply();
    }

    public boolean getWritingPlacement() {
        return _preferences.getBoolean(WRITING_PLACEMENT_KEY, false);
    }

    public int getWritingPlacementIndex() {
        return _preferences.getInt(WRITING_PLACEMENT_INDEX_KEY, 0);
    }

    public boolean getMathPlacement() {
        return _preferences.getBoolean(MATH_PLACEMENT_KEY, false);
    }

    public int getMathPlacementIndex() {
        return  _preferences.getInt(MATH_PLACEMENT_INDEX_KEY, 0);
    }

    void updateLastTutor(String activeTutorId) {
        _editor = _preferences.edit();
        _editor.putString (TCONST.LAST_TUTOR, activeTutorId);
        _editor.apply();
    }

    public String getLastTutor() {
        return _preferences.getString(LAST_TUTOR, null);
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
     * Gets how many times a tutor has been played, to determine whether or not to play a video
     * @param tutor
     * @return
     */
    public int getTimesPlayedTutor(String tutor) {
        String key = tutor + "_TIMES_PLAYED";
        return _preferences.getInt(key, 0);
    }

    public void updateTimesPlayedTutor(String tutor, int i) {
        _editor = _preferences.edit();
        _editor.putInt(tutor, i);
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
