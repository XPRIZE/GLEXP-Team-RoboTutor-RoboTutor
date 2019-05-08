package cmu.xprize.robotutor.tutorengine;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 5/7/19.
 */

public class QuickDebugTutor {

    public String tutorVariant;
    public String tutorId;
    public String tutorFile;

    public QuickDebugTutor(String tutorVariant, String tutorId, String tutorFile) {
        this.tutorVariant = tutorVariant;
        this.tutorId = tutorId;
        this.tutorFile = tutorFile;
    }

    // for describing the bug
    public String comment;

    /**
     * Add a comment about the bug.
     *
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean resolved = false;
    public void resolve() {
        resolved = true;
    }

    public void open() {
        resolved = false;
    }

    public String location;
    /**
     * Add a location for where the bug must be fixed within the code.
     *
     * @param  location
     */
    public void setLocation(String location) { this.location = comment;}


    public enum Priority {MUST, SHOULD, COULD, WONT};
    public Priority priority;
    /**
     * set a priority
     *
     * @param priority
     */
    public void setPriority(Priority priority) { this.priority = priority;}

}
