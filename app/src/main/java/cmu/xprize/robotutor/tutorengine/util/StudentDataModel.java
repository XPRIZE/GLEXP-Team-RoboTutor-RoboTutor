package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CPlacementTest_Tutor;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.MENU_BUG_TAG;
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

    // these match TCONST
    private static final String CURRENT_WRITING_TUTOR_KEY   = "letters";
    private static final String CURRENT_STORIES_TUTOR_KEY    = "stories";
    private static final String CURRENT_MATH_TUTOR_KEY       = "numbers";

    private  static final String LAST_TUTOR_PLAYED_KEY = "LAST_TUTOR_PLAYED";


    // new way cycles through skills
    private static final boolean CYCLE_MATRIX = true;
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
        _editor.putString(StudentDataModel.HAS_PLAYED_KEY, String.valueOf(true));

        // writing: Placement = true. Placement Index starts at 0
        _editor.putBoolean(StudentDataModel.WRITING_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) && Configuration.usePlacement(RoboTutor.ACTIVITY));
        _editor.putInt(StudentDataModel.WRITING_PLACEMENT_INDEX_KEY, 0);

        // math: Placement = true. Placement Index starts at 0
        _editor.putBoolean(StudentDataModel.MATH_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY));
        _editor.putInt(StudentDataModel.MATH_PLACEMENT_INDEX_KEY, 0);

        if(CYCLE_MATRIX) {
            _editor.putString(StudentDataModel.SKILL_SELECTED_KEY, SELECT_WRITING);
        }

        _editor.apply();
    }

    /**
     * This sets the tutor IDs
     * @param matrix
     */
    public void initializeTutorPositions(TransitionMatrixModel matrix) {

        // initialize math placement
        boolean useMathPlacement = getMathPlacement() && CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY);

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
        boolean useWritingPlacement = getWritingPlacement() && CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY);
        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useWritingPlacement = %s", useWritingPlacement));
        if (useWritingPlacement) {
            int writingPlacementIndex = getWritingPlacementIndex();
            CPlacementTest_Tutor writePlacementTutor = matrix.writePlacement[writingPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writePlacementIndex = %d", writingPlacementIndex));
            String writingTutorID = writePlacementTutor.tutor;
            updateWritingTutorID(writingTutorID); // MENU_LOGIC (XXX) why is updateWritingTutorID("story.hear::story_1")
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writingTutorID = %s", writingTutorID));
        } else {
            updateWritingTutorID(matrix.rootSkillWrite); // MENU_LOGIC (XXX) why is updateWritingTutorID("story.hear::story_1")
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
        return _preferences.getString(CURRENT_WRITING_TUTOR_KEY, null);
    }

    // MENU_SOLUTION
    // (1) add useful logs https://stackoverflow.com/questions/6891348/is-there-any-way-to-automatically-log-the-method-name-in-android
    // (2) fix wacky logic
    // (3) ship it...
    // MENU_LOGIC id = "story.hear::story_1" ?
    public void updateWritingTutorID(String id) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        _editor = _preferences.edit();
        _editor.putString(StudentDataModel.CURRENT_WRITING_TUTOR_KEY, id);
        _editor.apply();
    }

    public String getStoryTutorID() {
        return _preferences.getString(CURRENT_STORIES_TUTOR_KEY, null);
    }

    public void updateStoryTutorID(String id) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        _editor = _preferences.edit();
        _editor.putString(StudentDataModel.CURRENT_STORIES_TUTOR_KEY, id);
        _editor.apply();
    }

    public String getMathTutorID() {
        return _preferences.getString(CURRENT_MATH_TUTOR_KEY, null);
    }

    public void updateMathTutorID(String id) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        _editor = _preferences.edit();
        _editor.putString(StudentDataModel.CURRENT_MATH_TUTOR_KEY, id);
        _editor.apply();
    }

    public String getActiveSkill() {
        String activeSkill = _preferences.getString(SKILL_SELECTED_KEY, SELECT_WRITING); // MENU_LOGIC should this have a default???
        Log.wtf("ACTIVE_SKILL", "get=" + activeSkill);
        return activeSkill;
    }

    public void updateActiveSkill(String skill) {
        _editor = _preferences.edit();
        _editor.putString(StudentDataModel.SKILL_SELECTED_KEY, skill);
        _editor.apply();
        Log.wtf("ACTIVE_SKILL", "update=" + skill);
    }

    /**
     * move on to the next skill in cycle
     * @return
     */
    public void incrementActiveSkill() {
        SKILL_INDEX = (SKILL_INDEX + 1) % SKILL_CYCLE.length; // 0, 1, 2, 3, 0...
        updateActiveSkill(SKILL_CYCLE[SKILL_INDEX]);

    }

    /**
     * This is needed to perform a repeat.
     *
     * This should behave differently for each Menu
     *
     * @return
     */
    public String getLastSkill() {


        switch(CTutorEngine.menuType) {
            case CYCLE_CONTENT:
                int index = SKILL_INDEX == 0 ? SKILL_CYCLE.length -1 : SKILL_INDEX - 1;

                Log.d("REPEAT_ME", "SKILL_INDEX=" + SKILL_INDEX + ", new_index=" + index); // MENU_LOGIC "0", "3"
                return SKILL_CYCLE[index];

            case STUDENT_CHOICE:

                return getActiveSkill();
        }

        return null;

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
        _editor.putString (StudentDataModel.LAST_TUTOR_PLAYED_KEY, activeTutorId);
        _editor.apply();
    }

    // MENU_LOGIC only called once
    public String getLastTutor() {
        return _preferences.getString(LAST_TUTOR_PLAYED_KEY, null);
    }

    void updateMathPlacement(boolean b) {
        _editor = _preferences.edit();
        _editor.putBoolean(StudentDataModel.MATH_PLACEMENT_KEY, b);
        _editor.apply();
    }

    void updateMathPlacementIndex(Integer i) {
        _editor = _preferences.edit();
        if (i == null) {
            _editor.remove(StudentDataModel.MATH_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(StudentDataModel.MATH_PLACEMENT_INDEX_KEY, i);
        }
        _editor.apply();
    }

    void updateWritingPlacement(boolean b) {
        _editor = _preferences.edit();
        _editor.putBoolean(StudentDataModel.WRITING_PLACEMENT_KEY, b);
        _editor.apply();
    }

    void updateWritingPlacementIndex(Integer i) {
        _editor = _preferences.edit();
        if (i == null) {
            _editor.remove(StudentDataModel.WRITING_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(StudentDataModel.WRITING_PLACEMENT_INDEX_KEY, i);
        }
        _editor.apply();
    }

    /**
     * Gets how many times a tutor has been played, to determine whether or not to play a video
     * @param tutor
     * @return
     */
    public int getTimesPlayedTutor(String tutor) {
        String key = getTimesPlayedKey(tutor);
        return _preferences.getInt(key, 0);
    }

    public void updateTimesPlayedTutor(String tutor, int i) {
        _editor = _preferences.edit();
        _editor.putInt(getTimesPlayedKey(tutor), i);
        _editor.apply();
    }

    private String getTimesPlayedKey(String tutor) {
        return tutor + "_TIMES_PLAYED";
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

    public String toLogString() {
        StringBuilder builder = new StringBuilder();

        Map prefsMap = _preferences.getAll();
        for (Object k : prefsMap.keySet()) {
            builder.append(k)
                    .append("-")
                    .append(prefsMap.get(k))
                    .append("---");
        }

        return builder.toString();
    }
}
