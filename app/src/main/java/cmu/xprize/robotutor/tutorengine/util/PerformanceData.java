package cmu.xprize.robotutor.tutorengine.util;

/**
 * RoboTutor
 *
 * PerformanceData is a class that contains all possible variables that will be used to
 * determine student performance.
 *
 * <p>
 * Created by kevindeland on 1/10/18.
 */

public class PerformanceData {

    public enum StudentSelfAssessment {JUST_RIGHT, TOO_EASY, TOO_HARD, PLAY_AGAIN, LET_ROBOTUTOR_DECIDE};

    private int numberCorrect; // number of correct responses
    private int numberWrong;   // number of wrong responses
    private int numberAttempts;  // number of problems attempted
    private int totalNumberQuestions;  // total number of questions in the problem

    private int timesPassedPreviously;

    private StudentSelfAssessment selfAssessment;

    private String activitySection; // stories, math, shapes, read/write
    private String activityType;

    public PerformanceData() {
    }

    public int getNumberCorrect() {
        return numberCorrect;
    }

    public void setNumberCorrect(int numberCorrect) {
        this.numberCorrect = numberCorrect;
    }

    public int getNumberWrong() {
        return numberWrong;
    }

    public void setNumberWrong(int numberWrong) {
        this.numberWrong = numberWrong;
    }

    public int getNumberAttempts() {
        return numberAttempts;
    }

    public void setNumberAttempts(int numberAttempts) {
        this.numberAttempts = numberAttempts;
    }

    public int getTotalNumberQuestions() {
        return totalNumberQuestions;
    }

    public void setTotalNumberQuestions(int totalNumberQuestions) {
        this.totalNumberQuestions = totalNumberQuestions;
    }

    public int getTimesPassedPreviously() {
        return timesPassedPreviously;
    }

    public void setTimesPassedPreviously(int timesPassedPreviously) {
        this.timesPassedPreviously = timesPassedPreviously;
    }

    public StudentSelfAssessment getSelfAssessment() {
        return selfAssessment;
    }

    public void setSelfAssessment(StudentSelfAssessment selfAssessment) {
        this.selfAssessment = selfAssessment;
    }

    public String getActivitySection() {
        return activitySection;
    }

    public void setActivitySection(String activitySection) {
        this.activitySection = activitySection;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
}
