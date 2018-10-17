package cmu.xprize.robotutor.tutorengine.util;

import cmu.xprize.util.TCONST;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 10/16/18.
 */

public class VideoHelper {

    /**
     * Gets the video resource to play based on tutor.
     *
     * @return
     */
    public static String getTutorInstructionalVideoPath(String activeTutorId) {

        String PATH_TO_FILE = TCONST.ROBOTUTOR_ASSETS + "/" + "video" + "/";

        // note that this was initially done w/ a "substring" check, but each tutor has a different
        // naming format e.g. math:10 vs. story.hear:1 vs. story.echo:1
        if (activeTutorId.startsWith("bpop")) {
            PATH_TO_FILE += "bpop_demo.mp4";
        } else if (activeTutorId.startsWith("akira")) {
            PATH_TO_FILE += "akira_demo.mp4";
        } else if (activeTutorId.startsWith("math")) {
            PATH_TO_FILE += "asm_demo.mp4";
        } else if (activeTutorId.startsWith("write")) {
            PATH_TO_FILE += "write_demo.mp4";
        } else if (activeTutorId.startsWith("story.read") || activeTutorId.startsWith("story.echo")) {
            PATH_TO_FILE += "read_demo.mp4";
        } else if (activeTutorId.startsWith("numscale") || activeTutorId.startsWith("num.scale")) {
            PATH_TO_FILE += "numscale_demo.mp4";
        } else if (activeTutorId.startsWith("countingx")) {
            PATH_TO_FILE += "countingx_demo.mp4";
        } else {
            return null;
        }

        return PATH_TO_FILE;
    }
}
