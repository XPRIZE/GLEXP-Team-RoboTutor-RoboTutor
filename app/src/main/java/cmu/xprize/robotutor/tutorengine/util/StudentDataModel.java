package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CPlacementTest_Tutor;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.LAST_TUTOR;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/18.
 *
 * this *may* be too slow... change to a JSON object, and store all at once
 * see https://stackoverflow.com/questions/7145606/how-android-sharedpreferences-save-store-object
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

    // new way cycles through skills
    private static final boolean NEW_WAY = true;
    private static final String SKILL_SELECTED_KEY = "SKILL_SELECTED";
    // sets to true when student selects repeat
    private static final String IS_REPEATING_LAST_KEY = "IS_REPEATING_LAST";
    private static int SKILL_INDEX = 0;
    private static final String[] SKILL_CYCLE = new String[4];
    static {
        SKILL_CYCLE[0] = SELECT_WRITING;
        SKILL_CYCLE[1] = SELECT_STORIES;
        SKILL_CYCLE[2] = SELECT_WRITING;
        SKILL_CYCLE[3] = SELECT_MATH;
    }

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
        _editor.putBoolean(StudentDataModel.WRITING_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) && !RoboTutor.TURN_OFF_PLACEMENT_FOR_QA);
        _editor.putInt(StudentDataModel.WRITING_PLACEMENT_INDEX_KEY, 0);

        // math: Placement = true. Placement Index starts at 0
        _editor.putBoolean(StudentDataModel.MATH_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) &&  !RoboTutor.TURN_OFF_PLACEMENT_FOR_QA);
        _editor.putInt(StudentDataModel.MATH_PLACEMENT_INDEX_KEY, 0);

        if(NEW_WAY) {
            _editor.putString(SKILL_SELECTED_KEY, SELECT_WRITING);
        }

        _editor.apply();
    }

    /**
     * This sets the tutor IDs
     * @param matrix
     */
    public void initializeTutorPositions(TransitionMatrixModel matrix) {

        // initialize math placement
        boolean useMathPlacement = getMathPlacement() && CTutorEngine.language.equals(LANG_SW) &&  !RoboTutor.TURN_OFF_PLACEMENT_FOR_QA;

        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useMathPlacement = %s", useMathPlacement));
        if(useMathPlacement) {
            int mathPlacementIndex = getMathPlacementIndex();
            CPlacementTest_Tutor mathPlacementTutor = matrix.mathPlacement[mathPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathPlacementIndex = %d", mathPlacementIndex));
            String mathTutorID = mathPlacementTutor.tutor; // does this need to happen every time???
            updateMathTutorID(mathTutorID);
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathTutorID = %s", mathTutorID));
        } else {
            updateMathTutorID(matrix.rootSkillMath);
        }

        // initialize writing placement
        boolean useWritingPlacement = getWritingPlacement() && CTutorEngine.language.equals(LANG_SW) &&  !RoboTutor.TURN_OFF_PLACEMENT_FOR_QA;
        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useWritingPlacement = %s", useWritingPlacement));
        if (useWritingPlacement) {
            int writingPlacementIndex = getWritingPlacementIndex();
            CPlacementTest_Tutor writePlacementTutor = matrix.writePlacement[writingPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writePlacementIndex = %d", writingPlacementIndex));
            String writingTutorID = writePlacementTutor.tutor;
            updateWritingTutorID(writingTutorID);
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writingTutorID = %s", writingTutorID));
        } else {
            updateWritingTutorID(matrix.rootSkillWrite);
        }

        // stories doesn't have placement testing, so initialize at root
        if (getStoryTutorID() == null) {
            updateStoryTutorID(matrix.rootSkillStories);
        }
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
        String activeSkill = _preferences.getString(SKILL_SELECTED_KEY, SELECT_STORIES);
        Log.wtf("ACTIVE_SKILL", "get=" + activeSkill);
        return activeSkill;
    }

    public void updateActiveSkill(String skill) {
        _editor = _preferences.edit();
        _editor.putString(SKILL_SELECTED_KEY, skill);
        _editor.apply();
        Log.wtf("ACTIVE_SKILL", "update=" + skill);
    }

    /**
     * move on to the next skill in cycle
     * OH_BEHAVE (0) call after activity has been completed
     * @return
     */
    public String incrementActiveSkill() {
        SKILL_INDEX = (SKILL_INDEX + 1) % SKILL_CYCLE.length; // 0, 1, 2, 3, 0...
        updateActiveSkill(SKILL_CYCLE[SKILL_INDEX]);

        return SKILL_CYCLE[SKILL_INDEX];
    }

    /**
     * This is needed to perform a repeat.
     * OH_BEHAVE (3) call when student selects repeat
     * @return
     */
    public String getLastSkill() {

        int index = SKILL_INDEX == 0 ? SKILL_CYCLE.length -1 : SKILL_INDEX - 1;

        Log.d("REPEAT_ME", "SKILL_INDEX=" + SKILL_INDEX + ", new_index=" + index);
        return SKILL_CYCLE[index];
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
     * how to get whole model at once, instead of individual elements...
     * @param key
     */
    private void getWholeModel(String key) {
        Gson gson = new Gson();
        String json = _preferences.getString(key, "");
        StudentDataModel newThis = gson.fromJson(json, StudentDataModel.class);
    }

    /**
     * how to save whole model at once, instead of individual elements...
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        Map prefsMap = _preferences.getAll();
        for (Object k : prefsMap.keySet()) {
            builder.append(k)
                    .append("=")
                    .append(prefsMap.get(k))
                    .append(";\n");
        }

        return builder.toString();
    }
}
