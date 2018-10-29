package cmu.xprize.comp_logging;

import java.lang.reflect.Field;

import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.LITERACY_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.MATH_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.SONGS_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.STORIES_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.UNKNOWN_MATRIX;

/**
 * Created by kevindeland on 9/13/17.
 */

public class PerformanceLogItem {

    private long timestamp;
    private String userId;
    private String sessionId;
    private String gameId;
    private String language;
    private String tutorName;
    private String tutorId;
    private String problemName;
    private int problemNumber;
    private String levelName;
    private int totalProblemsCount;

    private String matrixName; // could be literacy or math or whatever

    public final static class MATRIX_TYPE {
        public final static String MATH_MATRIX = "math";
        public final static String LITERACY_MATRIX = "literacy";
        public final static String STORIES_MATRIX = "stories";
        public final static String UNKNOWN_MATRIX = "unknown";
        public final static String SONGS_MATRIX = "songs";
    }


    private int totalSubsteps;
    private int substepNumber;
    private int substepProblem;
    private int attemptNumber;
    private String taskName;

    private String expectedAnswer;
    private String userResponse;
    private String correctness;

    private String distractors;
    private String scaffolding;
    private String promptType;
    private String feedbackType;

    private String promotionMode; // placement or promotion

    private final static String DEBUG_TAG = "DEBUG_PERFORMANCE_LOG";

    //
    // iterative way to print fields in the desired order
    private static final String[] orderedFieldsToPrint = {"timestamp", "userId", "sessionId", "gameId", "language", "tutorName", "tutorId", "matrixName", "levelName", "taskName",
            "problemName", "problemNumber", "substepNumber", "substepProblem", "attemptNumber", "expectedAnswer", "userResponse", "correctness", "feedbackType", "totalProblemsCount", "promotionMode", "scaffolding"};

    public PerformanceLogItem() {
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();

        for (String fieldName : orderedFieldsToPrint) {
            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                if(field != null) {
                    msg.append(fieldName);
                    msg.append(": ");
                    msg.append(field.get(this));
                    msg.append(", ");
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        String result = msg.toString();
        return result.substring(0, result.length() - 2); // don't forget to slice off that last comma
    }


    /**
     * sets matrix based on which tutor....
     * TODO move into new class
     * TODO even better, just pass the "skills" variable
     * @param tutorId
     */
    private void setMatrixNameByTutorId(String tutorId) {

        if (isAlwaysMathTutor(tutorId)) {
            matrixName = MATH_MATRIX;
        } else if (isAkiraTutor(tutorId)) {
            setMatrixAkira(tutorId);
        } else if (isBpopTutor(tutorId)) {
            setMatrixBpop(tutorId);
        } else if (isWriteTutor(tutorId)) {
            setMatrixWrite(tutorId);
        } else if (isStoryTutor(tutorId)) {
            setMatrixStory(tutorId);
        } else if (isAlwaysLitMatrix(tutorId)) {
            matrixName = LITERACY_MATRIX;
        } else {
            matrixName = UNKNOWN_MATRIX;
        }

    }

    /**
     * set matrix name if it's a Story tutor
     * @param tutorName
     */
    private void setMatrixStory(String tutorName) {
        if (tutorName.contains("A..Z")
                || tutorName.contains("wrd")
                || tutorName.contains("syl")
                || tutorName.contains("vow")
                || tutorName.contains("abcdefg_EnglishTune_LowerCase")
                || tutorName.contains("ABCDEFG_EnglishTune_UpperCase")
                || tutorName.contains("LC_Vowel_Song_1")
                || tutorName.contains("LC_Vowel_Song_2")
                || tutorName.contains("UC_Vowel_Song_1")
                || tutorName.contains("UC_Vowel_Song_2")
                || tutorName.contains("letters_alphabet_song")
                || tutorName.contains("comm")
                || tutorName.contains("ltr")
                || tutorName.contains("syl")
                || tutorName.contains("nafasi")
                || tutorName.contains("herufi_kubwa")
                || tutorName.contains("kituo")){
            matrixName = LITERACY_MATRIX;

        } else if (tutorName.contains("1..4")
                || tutorName.contains("0..10")
                || tutorName.contains("0..20")
                || tutorName.contains("0..50")
                || tutorName.contains("0..100")
                || tutorName.contains("10..100")
                || tutorName.contains("50..100")
                || tutorName.contains("100..900")
                || tutorName.contains("100..1000")
                || tutorName.contains("Counting_Fingers_Toes")
                || tutorName.contains("Number_Song_2")
                || tutorName.contains("numbers_counting_song")
                || tutorName.contains("word_problem")) {
            matrixName = MATH_MATRIX;

        } else if (tutorName.contains("Garden_Song")
                || tutorName.contains("Safari_Song")
                || tutorName.contains("School_Welcome_Song")
                || tutorName.contains("Kusoma_Welcome_Song")) {
            matrixName = SONGS_MATRIX;

        } else if (tutorName.contains("story_")) {

            if (tutorName.startsWith("story.echo")
                    || tutorName.startsWith("story.parrot")
                    || tutorName.startsWith("story.read")) {
                matrixName = LITERACY_MATRIX;

            } else if (tutorName.startsWith("story.hear")
                    || tutorName.startsWith("story.clo.hear")
                    || tutorName.startsWith("story.pic.hear")
                    || tutorName.startsWith("story.gen.hear")) {
                matrixName = STORIES_MATRIX;

            } else {
                matrixName = UNKNOWN_MATRIX;
            }

        } else {
            matrixName = UNKNOWN_MATRIX;
        }
    }

    /**
     * set matrix name if it's a Write tutor
     * @param tutorName
     */
    private void setMatrixWrite(String tutorName) {

        if (tutorName.contains("ltr")
                || tutorName.contains("missingLtr")
                || tutorName.contains("wrd")) {
            matrixName = LITERACY_MATRIX;
        } else if (tutorName.contains("arith")
                || tutorName.contains("num")){
            matrixName = MATH_MATRIX;
        } else {
            matrixName = UNKNOWN_MATRIX;
        }
    }

    /**
     * set matrix name if it's a BubblePop tutor
     * @param tutorName
     */
    private void setMatrixBpop(String tutorName) {

        if (tutorName.contains("ltr")
                || tutorName.contains("wrd")
                || tutorName.contains("syl")) {
            matrixName = LITERACY_MATRIX;
        }

        else if (tutorName.contains("mn")
                || tutorName.contains("gl")
                || tutorName.contains("addsub")
                || tutorName.contains("num")) {
            matrixName = MATH_MATRIX;
        } else {
            matrixName = UNKNOWN_MATRIX;
        }

    }

    /**
     * set matrix name if it's an Akira tutor
     * @param tutorName
     */
    private void setMatrixAkira(String tutorName) {
        if (tutorName.contains("ltr")
                || tutorName.contains("wrd")
                || tutorName.contains("syl")) {
            matrixName = LITERACY_MATRIX;
        } else {
            matrixName = MATH_MATRIX;
        }
    }


    //
    // for checking tutor type based on name
    //
    private boolean isStoryTutor(String tutorName) {
        return tutorName.startsWith("story");
    }

    private boolean isWriteTutor(String tutorName) {
        return tutorName.startsWith("write");
    }

    private boolean isBpopTutor(String tutorName) {
        return tutorName.startsWith("bpop");
    }

    private boolean isAlwaysLitMatrix(String tutorName) {
        return tutorName.startsWith("spelling")
                || tutorName.startsWith("picmatch");
    }

    private boolean isAkiraTutor(String tutorName) {
        return tutorName.startsWith("akira");
    }

    private boolean isAlwaysMathTutor(String tutorName) {
        return tutorName.startsWith("num.scale")
                || tutorName.startsWith("math")
                || tutorName.startsWith("countingx")
                || tutorName.startsWith("place.value")
                || tutorName.startsWith("placevalue")
                || tutorName.startsWith("bigmath")
                || tutorName.startsWith("numcompare");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {

        this.tutorName = tutorName;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        if(tutorId == null) {
            return;
        }

        this.tutorId = tutorId.replaceAll(":", "_");


        if(tutorId != null) {
            setMatrixNameByTutorId(tutorId);
        }
    }

    public void setMatrixNameBySkillId(String skill) {
        if (skill == null) {
            return;
        }
        switch (skill) {

            case "letters":
                matrixName = LITERACY_MATRIX;
                break;

            case "numbers":
                matrixName = MATH_MATRIX;
                break;

            case "stories":
                matrixName = STORIES_MATRIX;
                break;
        }
    }

    public String getPromotionMode() {
        return promotionMode;
    }

    public void setPromotionMode(String promotionMode) {
        this.promotionMode = promotionMode;
    }

    public String getMatrixName() {
        return matrixName;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public int getProblemNumber() {
        return problemNumber;
    }

    public void setProblemNumber(int problemNumber) {
        this.problemNumber = problemNumber;
    }

    public int getTotalProblemsCount() { return totalProblemsCount; }

    public void setTotalProblemsCount(int totalProblemsCount) { this.totalProblemsCount = totalProblemsCount; }

    public int getTotalSubsteps() { return totalSubsteps; }

    public void setTotalSubsteps(int totalSubsteps) {
        this.totalSubsteps = totalSubsteps;
    }

    public int getSubstepNumber() {
        return substepNumber;
    }

    public void setSubstepNumber(int substepNumber) {
        this.substepNumber = substepNumber;
    }

    public int getSubstepProblem() {
        return substepProblem;
    }

    public void setSubstepProblem(int substepProblem) {
        this.substepProblem = substepProblem;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public String getCorrectness() {
        return correctness;
    }

    public void setCorrectness(String correctness) {
        this.correctness = correctness;
    }

    public String getScaffolding() {
        return scaffolding;
    }

    public void setScaffolding(String scaffolding) {
        this.scaffolding = scaffolding;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDistractors() {
        return distractors;
    }

    public void setDistractors(String distractors) {
        this.distractors = distractors;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelName() { return levelName; }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() { return taskName; }
}
