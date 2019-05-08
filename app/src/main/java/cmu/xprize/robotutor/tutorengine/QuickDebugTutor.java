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
}
