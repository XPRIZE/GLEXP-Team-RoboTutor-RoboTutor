package cmu.xprize.robotutor.tutorengine.util;

import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.TCONST;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 10/16/18.
 */

public class VideoHelper {

    enum videos { read, cloze, story_pic, asm, akira, bpop, write, sentence_writing, numberscale, picmatch, spelling, countingx, bigmath_add, bigmath_sub, numcompare, pv1, pv2, pv3};

    /**
     * "videoId" is used to do two things:
     *  (1) look up in StudentModel how many times the video has been played
     *  (2) look up the video file
     * @param tutor
     * @return
     */
    public static String getVideoIdByTutor(CAt_Data tutor) {

        switch (tutor.tutor_desc) {

            case "math": return videos.asm.toString();

            case "akira":
                return videos.akira.toString();

            case "story.clo.hear":
                return videos.cloze.toString();

            case "story.pic.hear":
                return videos.story_pic.toString();

            case "bpop.mn":
            case "bpop.addsub":
            case "bpop.gl":
            case "bpop.num":
            case "bpop.ltr.mix":
            case "bpop.ltr.lc":
            case "bpop.ltr.uc":
            case "bpop.wrd":
                return videos.bpop.toString();

            case "countingx":
                return videos.countingx.toString();

            case "write.ltr.uc":
            case "write.ltr.uc.dic":
            case "write.ltr.uc.trc":
            case "write.ltr.lc":
            case "write.ltr.lc.dic":
            case "write.ltr.lc.trc":
            case "write.num":
            case "write.num.dic":
            case "write.num.trc":
            case "write.wrd":
            case "write.wrd.dic":
            case "write.wrd.trc":
            case "write.dotCount":
            case "write.arith":
            case "write.missingLtr":
                return videos.write.toString();

            case "write.sen.corr.ltr.stim":
            case "write.sen.copy.ltr":
            case "write.sen.corr.ltr.nostim":
            case "write.sen.dic.ltr":
                return videos.sentence_writing.toString();

            case "numberscale":
                return videos.numberscale.toString();

            case "picmatch":
                return videos.picmatch.toString();

            case "spelling":
                return videos.spelling.toString();

            case "place.value":
                // VID_LAUNCH (next) replace with uniqueness...
                // something something last values 3d or 2d
                char diffLevel;
                int i = tutor.tutor_id.lastIndexOf("diff");
                diffLevel = tutor.tutor_id.charAt(i+4);
                switch (diffLevel) {
                    case '0':
                        return videos.pv1.toString();

                    case '1':
                        return videos.pv2.toString();

                    case '2':
                    default:
                        return videos.pv3.toString();
                }

            case "bigmath":
                if (tutor.tutor_id.contains("add")) {
                    return videos.bigmath_add.toString();
                } else {
                    return videos.bigmath_sub.toString();
                }


            case "numcompare":
                return videos.numcompare.toString();

            default:
                return null;
        }

    }


    /**
     * Get the path of the video
     * @param videoId
     * @return
     */
    public static String getVideoPathById(String videoId) {
        String filepath = TCONST.ROBOTUTOR_ASSETS + "/" + "video" + "/";
        filepath += videoId + ".mp4";
        return filepath;
    }
}
